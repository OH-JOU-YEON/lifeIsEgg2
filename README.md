# 🥚 삶은달걀

> 준비하는 당신을 위한 성장 기록 및 응원 서비스

📎 **서비스 주소**: [life-is-egg.com](https://life-is-egg.com)

취업 준비, 시험 준비처럼 긴 시간을 버텨야 하는 준비생들이 하루를 기록하고, 서로 응원하고, 목표와 일정을 관리할 수 있는 서비스입니다.
기획부터 백엔드·프론트엔드 개발, AWS 배포와 CI/CD, 운영 중 성능 개선과 리팩터링까지 1인 진행 했습니다. 


---

## 📌 주요 기능

| 기능 | 설명 |
|------|------|
| 일기 (Post) | 작성 및 또래 피드 열람, 공개/비공개 설정, UUID 기반 URL |
| 응원 (Cheer) | 일기에 응원·답글 작성, 무한 깊이 트리 구조, 소프트 삭제 |
| 알림 (Alarm) | 내 일기 응원 / 내 응원 답글 알림, 30초 폴링 + 커스텀 이벤트로 즉시 반영 |
| 목표 (Goal) | 주간·월간 목표 설정 및 자동 완료 처리 |
| 일정 (Schedule) | 카테고리별 일정 관리 |
| 대시보드 | 목표 진행률, 카테고리별 시간, 활동 요약 통계 (Redis 캐싱) |
| 신고 (Report) | 부적절한 일기·응원 신고 |
| 인증 (Auth) | JWT 기반 로그인, Gmail SMTP 이메일 인증 |

---

## 🏗️ 아키텍처
<img 
width="6400" height="3760" alt="lifeIsEgg_architecture (1)" src="https://github.com/user-attachments/assets/887d696f-7849-4209-96fd-78810fa38718" />

- **nginx**가 HTTPS(Let's Encrypt)를 처리하고, `/`는 React 정적 파일을, `/api/`는 Spring Boot(:8080)로 리버스 프록시합니다. 8080 포트는 외부에 노출하지 않습니다.
- **MariaDB**와 **Redis**는 같은 EC2 인스턴스에서 동작합니다. 사용자 규모와 비용을 고려한 단일 인스턴스 구성입니다.
- **GitHub Actions**가 `main` 브랜치 push 시 백엔드(Gradle 빌드 → jar SCP → systemd 재시작)와 프론트엔드(npm build → 정적 파일 SCP)를 배포합니다.
<!-- TODO: 서버 nginx 설정의 proxy_pass 확인 후, /api prefix 제거 여부를 명시할지 결정 -->

---

## 🛠️ 기술 스택

### Backend

| 분류 | 기술 |
|------|------|
| Language | Java 21 |
| Framework | Spring Boot 4.0.2 |
| ORM | Spring Data JPA (Hibernate) |
| Security | Spring Security + JWT (jjwt 0.12.3) |
| Database | MariaDB |
| Cache | Redis (Spring Cache) |
| Mail | Spring Mail (Gmail SMTP) |
| API 문서 | springdoc-openapi 2.3.0 (개발 환경 전용, 운영 비활성화) |
| Test | JUnit 5, Spring Boot Test |
| Build | Gradle |

### Frontend

| 분류 | 기술 |
|------|------|
| Framework | React 19 + Vite |
| Styling | Tailwind CSS |
| HTTP | Axios (인터셉터로 토큰 주입·401 처리) |
| Routing | React Router |

### Infra

| 분류 | 기술 |
|------|------|
| 서버 | AWS EC2 (t3.micro, 서울 리전), Ubuntu 22.04 |
| 웹서버 | nginx |
| HTTPS | Let's Encrypt |
| CI/CD | GitHub Actions |
| 프로세스 관리 | systemd |

---

## 💡 핵심 설계와 개선

### 1. 도메인 간 결합도 낮추기 — DDD-lite 리팩터링 (2026.07)

**배경**

새 도메인(타이머 등) 추가를 앞두고 구조를 점검한 결과, `CheerService`가 `AlarmService`를 직접 호출하고 `PostService`·`ReportService`·`DashboardService`가 다른 도메인의 Repository를 직접 주입받고 있었습니다.
애그리거트 사이에 "같은 트랜잭션 안에서 지켜야 하는 불변식이 있는가"를 먼저 판단했고, 없다는 결론에 따라 **쓰기 경로는 이벤트로, 읽기·검증 경로는 포트(DIP)로** 분리했습니다.

**① Cheer → Alarm 이벤트 기반 분리**

```java
// Before
alarmService.createCheerAlarm(post.getUser(), post, cheer);

// After
eventPublisher.publishEvent(new CheerCreatedEvent(targets, post.getId(), post.getUuid(), cheer.getId()));
```

- `CheerCreatedEvent` / `CheerDeletedEvent`를 도입하고, `AlarmService`가 `@TransactionalEventListener(AFTER_COMMIT)` + `REQUIRES_NEW`로 구독합니다.
- 알림 생성이 실패해도 응원 작성 트랜잭션은 롤백되지 않습니다.
- `Alarm`이 `Cheer`/`Post` 엔티티 대신 `postId`/`postUuid`/`cheerId`만 보유하도록 바꿔, 알림 도메인이 다른 도메인 엔티티를 알지 못하게 했습니다.

**② 포트(DIP) 적용**

| Before | After |
|---|---|
| `PostService` → `CheerRepository` 직접 참조 | `PostService` → `CheerCountPort` (Post가 정의, Cheer가 구현) |
| `ReportService` → `PostRepository`, `CheerRepository` 직접 참조 | `ReportService` → `PostLookupPort`, `CheerLookupPort` |

**③ 대시보드 전략 패턴**

- `DashboardService`가 Goal·Schedule·Post·Cheer 4개 Repository를 직접 참조하던 구조를 `DashboardMetricContributor` 인터페이스로 전환했습니다.
- 각 도메인이 자기 패키지에 Contributor 구현체를 두고, `DashboardService`는 `List<DashboardMetricContributor>`를 순회하며 `DashboardMetricsContext`에 결과를 모읍니다.
- **새 도메인을 추가해도 `DashboardService`는 수정하지 않고** 구현체 하나만 추가하면 됩니다.

---

### 2. 쿼리 성능 개선 — N+1 제거와 Redis 캐싱 (2026.06)

**측정 환경**: 로컬, 시드 데이터(user 30 / post 150 / cheer 1,000+ / goal·schedule 각 240), Hibernate SQL 로그로 쿼리 수와 응답 시간 측정

**① 또래 피드 조회 N+1 제거**

게시글 10개를 조회한 뒤 게시글마다 응원 수를 따로 조회해, 게시글 수에 비례해 쿼리가 늘어나는 구조였습니다.

```java
// Before: 게시글마다 COUNT 쿼리
.map(post -> new PostFeedResponse(post, cheerRepository.countByPost(post)))

// After: IN + GROUP BY 한 번으로 조회 후 Map으로 매칭
Map<Long, Long> cheerCountMap = cheerRepository.countByPostIds(postIds).stream()
        .collect(Collectors.toMap(row -> (Long) row[0], row -> (Long) row[1]));
```

| 항목 | Before | After |
|---|---|---|
| 쿼리 수 | 11개 | **2개** (약 82% 감소) |
| 응답 시간 | 241ms | 173ms |

> 로컬은 DB와 애플리케이션이 같은 머신이라 네트워크 왕복 비용이 거의 없습니다. 핵심 성과는 응답 시간보다 **데이터가 늘어나도 쿼리 수가 고정되는 구조**로 바꾼 것입니다.

**② 응원 트리 조회 — 점검 결과 N+1 없음**

응원을 게시글 기준으로 한 번에 평탄하게 조회한 뒤, `LinkedHashMap`으로 메모리에서 트리를 조립합니다. 작성 순서를 유지하면서 깊이와 무관하게 쿼리 수가 2개로 고정됩니다.

**③ 대시보드 통계 Redis 캐싱**

대시보드는 요청마다 통계 쿼리 8개를 실행하지만, 사용자가 새 기록을 남기지 않는 한 결과가 거의 바뀌지 않아 캐싱 1순위로 선정했습니다.

- `@Cacheable(value = "dashboardStats", key = "#userId")`, TTL 5분
- 값은 JSON(`GenericJacksonJsonRedisSerializer`)으로 직렬화

| 항목 | 첫 호출 (DB) | 캐시 히트 |
|---|---|---|
| 쿼리 수 | 8개 | **0개** |
| 응답 시간 | 976ms | **110ms** (약 89% 감소) |

---

### 3. 제한된 서버 자원에서의 배포·운영

- **EC2에서 직접 빌드하던 방식 제거**: t3.micro(RAM 1GB)에서 Gradle 빌드 중 서버 전체가 멈추는 문제를 겪고, GitHub Actions에서 빌드한 jar만 전송하는 방식으로 바꿨습니다. 스왑 메모리도 설정했습니다.
- **Docker 도입 보류**: 컨테이너화를 검토했지만, t3.micro의 메모리 여유로는 얻는 이점보다 오버헤드가 크다고 판단해 systemd 기반 운영을 유지했습니다.
- **운영 설정 분리**: 운영 환경에서는 Swagger를 비활성화하고, 스키마는 자동 반영 없이 배포 전 수동 마이그레이션(`ALTER TABLE` + 기존 데이터 백필)으로 반영합니다.

---

## 🔧 트러블슈팅

### EC2 메모리 부족으로 서버 먹통

- **증상**: EC2에서 Gradle 빌드 중 SSH를 포함해 서버 전체가 응답하지 않음
- **원인**: RAM 1GB인 t3.micro에서 Gradle 데몬이 메모리를 모두 점유해 OOM 발생
- **해결**: 빌드를 GitHub Actions로 옮기고 jar만 SCP로 전송, EC2에 스왑 메모리 설정

### 답글 있는 응원 삭제 시 FK 제약 위반

- **증상**: 답글이 달린 응원을 삭제하면 `cheers.parent_id → cheers.id` FK 제약 위반으로 실패
- **원인**: 하드 삭제 구조에서 자식이 참조하는 부모 행을 삭제하려고 함
- **해결**: 소프트 삭제로 전환(`Cheer.deleted`). 삭제된 응원은 "삭제된 응원입니다"로 표시하고 답글은 유지

### `@TransactionalEventListener` + `@Transactional` 조합 시 구동 실패

- **증상**: `AFTER_COMMIT` 리스너에 기본 `@Transactional`을 붙이면 애플리케이션이 구동되지 않음
- **원인**: 원본 트랜잭션이 끝난 뒤 실행되는 리스너라 `REQUIRED`(기본값)로는 합류할 트랜잭션이 없음
- **해결**: `@Transactional(propagation = Propagation.REQUIRES_NEW)`로 명시


---

## 🧭 알려진 한계

- **대시보드 캐시 반영 지연**: 쓰기 시 캐시를 무효화하지 않아, 새 기록이 대시보드에 최대 5분 늦게 반영됩니다.
- **알림 이벤트 유실 가능성**: `AFTER_COMMIT` 이후 알림 처리 중 서버가 종료되면 해당 알림은 유실될 수 있습니다. 현재 규모에서는 허용 범위로 판단했습니다.
- **알림 폴링 방식**: 30초 간격 폴링으로 구현해 실시간성에 한계가 있습니다.

---

## 📁 프로젝트 구조

```
src/main/java/com/ohjeon/life_is_egg
├── domain
│   ├── alarm       # 알림 (Cheer 이벤트 구독)
│   ├── auth        # 회원가입·로그인·이메일 인증
│   ├── cheer       # 응원 트리, 이벤트 발행
│   ├── dashboard   # 통계 (port / support)
│   ├── goal
│   ├── post        # 일기 (port)
│   ├── report      # 신고 (port)
│   └── schedule
└── global
    ├── config      # Security, Redis, JPA
    ├── exception
    └── jwt
frontend/           # React + Vite
.github/workflows/  # 배포 워크플로우
```

각 도메인은 `controller / service / repository / entity / dto` 구조이며, 리팩터링 대상이었던 Cheer→Alarm 쓰기 흐름은 `event`로, Post·Report·Dashboard의 타 도메인 조회는 `port`로 분리되어 있습니다.

