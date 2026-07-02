# 삶은달걀

> 준비하는 당신을 위한 성장 기록 및 응원 서비스

📎 **서비스 주소**: [life-is-egg.com](https://life-is-egg.com)

---

## 📌 프로젝트 소개

취업 준비, 시험 준비 등 미래를 준비하는 준비생들을 위한 자기관리 및 마음 공유 서비스입니다.

| 기능 | 설명 |
|------|------|
| 일기(Post) | 작성 및 피드 열람, 익명 작성 지원, UUID 기반 URL |
| 응원(Cheer) | 무한 깊이 트리 구조, 셀프 조인 + LinkedHashMap으로 N+1 없이 구현 |
| 목표(Goal) | 설정 및 자동 완료 처리 |
| 일정(Schedule) | 일정 관리 |
| 알림(Alarm) | 폴링 방식 구현 (30초 간격, 커스텀 이벤트로 즉시 반영) |
| 대시보드 | nativeQuery 기반 통계 제공 (RAND, TIMESTAMPDIFF 활용) |

---

## 🏗️ 아키텍처

```
[사용자 브라우저]
       │
       ▼
[EC2 - Ubuntu 22.04 / t3.micro / 서울 리전]
       │
  [nginx]  ← HTTPS 처리(Let's Encrypt), 8080 포트 외부 차단
  ├── /        → React 정적 파일 서빙
  └── /api/    → Spring Boot :8080 (리버스 프록시, /api prefix 제거 후 전달)
       │
  [Spring Boot 4.0.2 / Java 21]
       │
  [MariaDB]

[GitHub Actions]
  ├── backend  → Gradle 빌드(CI) → jar SCP → systemd 재시작
  └── frontend → npm build → 정적 파일 EC2 갱신
```

> nginx를 리버스 프록시로 두어 8080 포트를 외부에 노출하지 않고, HTTPS 처리와 정적 파일 서빙을 분리했습니다.

---

## 🛠️ 기술 스택

### Backend

| 분류 | 기술 |
|------|------|
| Language | Java 21 |
| Framework | Spring Boot 4.0.2 |
| ORM | Spring Data JPA |
| Security | Spring Security + JWT (jjwt 0.12.3) |
| Database | MariaDB |
| API 문서 | springdoc-openapi 2.3.0 (개발 환경 전용, 운영 비활성화) |
| 빌드 | Gradle |

### Frontend

| 분류 | 기술 |
|------|------|
| Framework | React + Vite |
| HTTP | Axios |

### Infra

| 분류 | 기술 |
|------|------|
| 서버 | AWS EC2 (t3.micro, 서울 리전) |
| 웹서버 | nginx |
| OS | Ubuntu 22.04 |
| CI/CD | GitHub Actions |
| HTTPS | Let's Encrypt |

---

## 🗄️ ERD

<img width="1687" height="915" alt="Copy of Untitled Diagram" src="https://github.com/user-attachments/assets/dc32fb05-5866-4c2d-9130-6382ad9d69d0" />


---

## 🔧 트러블슈팅

### 1. EC2 메모리 부족으로 서버 먹통

**증상**
EC2에서 Gradle 빌드 중 SSH 연결 포함 서버 전체가 먹통됨

**원인**
t3.micro는 RAM 1GB로, Gradle 데몬이 메모리를 전부 점유하면서 OOM 발생

**해결**
- EC2에서 직접 빌드하는 방식 제거
- GitHub Actions에서 Gradle 빌드 후 jar만 SCP로 EC2에 전송하는 방식으로 전환
- EC2에 스왑 메모리 설정으로 재발 방지

---

### 2. 무한 응원 구조의 N+1 문제

**증상**
응원(Cheer) 조회 시 댓글 수만큼 추가 쿼리 발생

**원인**
부모-자식 관계의 셀프 조인 구조에서 자식 응원을 개별 조회

**해결**
- 전체 응원을 단일 쿼리로 조회 후 LinkedHashMap으로 트리 구조 조립
- 삽입 순서를 보장하면서 N+1 없이 무한 깊이 트리 구현

---

### 3. 배포 환경 로그인 시 403 오류 — CORS Preflight 미허용

**증상**
배포 환경에서 로그인 요청 시 403. 로컬에서는 정상 동작

**원인 1 — CORS allowedMethods 누락**
브라우저는 실제 요청 전 OPTIONS Preflight 요청을 먼저 보내는데, `allowedMethods`에 OPTIONS가 없어 차단됨.
로컬은 동일 출처라 Preflight가 발생하지 않아 문제 없이 동작

**해결 1**
CORS 설정 `allowedMethods`에 OPTIONS 추가, 운영 도메인 허용 추가

**원인 2 — Spring Security가 CORS 필터보다 먼저 요청 차단**
OPTIONS를 추가해도 Spring Security가 CORS 필터 앞단에서 요청을 가로채 차단

**해결 2**
`SecurityConfig`의 `authorizeHttpRequests`에 OPTIONS 요청 전체 `permitAll()` 추가

---


## 🔧 쿼리 성능 최적화 (2026.06.24 ~ 06.26)

## 한 줄 요약

N+1 쿼리 문제를 발견하고 해결했으며, 반복 조회가 많은 통계 API에 Redis 캐싱을 적용해 쿼리 수와 응답 시간을 줄였다.

---

## 1. 배경

- 배포 완료된 개인 프로젝트(삶은달걀)의 실제 성능을 점검하기 위해, 더미 데이터(user 30 / post 150 / cheer 1,000+ / goal·schedule 각 240)를 시드 데이터로 구축
- Hibernate SQL 로그(`org.hibernate.SQL: DEBUG`)를 활용해 주요 API의 실제 쿼리 실행 횟수와 응답 시간을 측정
- 측정 대상: 또래 피드 조회, 응원 조회, 대시보드 통계 조회

## 2. N+1 문제 발견 및 해결 — 또래 피드 조회

**문제**

```java
return posts.stream()
        .map(post -> new PostFeedResponse(post, cheerRepository.countByPost(post)))
        .toList();
```

게시글 10개를 조회한 뒤, 게시글마다 응원 수를 개별 쿼리로 호출 → 게시글 수에 비례해 쿼리가 증가하는 N+1 패턴

**해결**

- `post_id IN (...) GROUP BY post_id` 형태의 배치 쿼리로 응원 수를 한 번에 조회
- 조회 결과를 `Map<Long, Long>`으로 변환해 메모리에서 게시글과 매칭

```java
List<Long> postIds = posts.stream().map(Post::getId).toList();
Map<Long, Long> cheerCountMap = cheerRepository.countByPostIds(postIds).stream()
        .collect(Collectors.toMap(row -> (Long) row[0], row -> (Long) row[1]));
```

**측정 결과 (로컬 기준)**

|항목|Before|After|개선율|
|---|---|---|---|
|쿼리 개수|11개 (posts 1 + cheers 10)|2개 (posts 1 + cheers 1)|**약 82% 감소**|
|응답시간|241ms|173ms|약 28% 감소|

> 로컬 환경은 DB와 애플리케이션이 같은 머신에서 동작해 네트워크 왕복 비용이 거의 없는 환경이다. 실제 운영 서버(DB와 애플리케이션이 분리된 구조)에서는 쿼리 1회당 왕복 비용이 더 크기 때문에, 쿼리 수 감소에 따른 응답시간 개선 효과는 더 크게 나타날 것으로 예상된다.

## 3. N+1 의심 지점 추가 점검 (문제 없음 확인)

| 지점         | 쿼리 개수                         | 결론                                                                            |
| ---------- | ----------------------------- | ----------------------------------------------------------------------------- |
| 응원의 트리 조회  | 2개 (post 1 + cheer flat 조회 1) | N+1 없음. 댓글/답글을 flat하게 한 번에 조회한 뒤 메모리에서 트리 구조로 조립하는 방식이라 데이터 양과 무관하게 쿼리 수가 고정됨 |
| 대시보드 통계 조회 | 8개                            | N+1은 아니지만(통계 항목별 쿼리 1개씩, 정직한 구조), 호출 빈도가 높아 캐싱 적용 1순위로 선정                     |

## 4. Redis 캐싱 적용 — 대시보드 통계 조회

**선정 이유**

- 대시보드 통계는 매 요청마다 쿼리 8개(목표 진행률, 카테고리별 학습시간, 일기/응원 수 등)를 실행
- 사용자가 새 글/응원/일정을 추가하지 않는 한 단기간 내 결과가 거의 변하지 않는 데이터 → 캐싱에 적합

**구현**

- Redis 연동: Docker 기반 로컬 Redis, Spring Boot `spring-boot-starter-data-redis` + `spring-boot-starter-cache`
- `@Cacheable(value = "dashboardStats", key = "#userId")` 적용, TTL 5분
- 값 직렬화는 JSON 방식(`GenericJacksonJsonRedisSerializer`)으로 구성해 캐시 데이터의 가독성과 유지보수성 확보

**측정 결과**

|항목|1차 호출 (DB)|2차 호출 (캐시 히트)|개선율|
|---|---|---|---|
|쿼리 개수|8개|**0개**|100%|
|응답시간|976ms|110ms|**약 89% 감소**|

## 5. 트러블슈팅

- Spring Boot 4 업그레이드로 인한 패키지/클래스 변경 대응
    - `RedisCacheManagerBuilderCustomizer` 패키지 이동(`org.springframework.boot.cache.autoconfigure`)
    - `GenericJackson2JsonRedisSerializer` deprecated → Jackson 3 기반 `GenericJacksonJsonRedisSerializer`로 교체
- 캐시 역직렬화 실패(`InvalidDefinitionException`) 해결: `@Builder`만 사용하던 응답 DTO에 기본 생성자(`@NoArgsConstructor`)와 `@Setter`를 추가해 Jackson이 캐시된 JSON으로부터 객체를 재구성할 수 있도록 함
- 측정/시드 데이터 생성용 테스트가 일반 테스트와 충돌하지 않도록 Gradle 설정에서 별도 패키지로 분리(`exclude '**/seed/**'`)

### 4. Map.of() null 값으로 인한 NullPointerException

**증상**
응원 작성, 알림 읽음 처리 API 응답에서 NPE 발생

**원인**
`Map.of()`는 null 값을 허용하지 않음. 응답 body에 `"data", null` 형태로 넣으면 런타임 예외 발생

**해결**
null 값이 필요 없는 응답은 해당 키 자체를 제거하거나 빈 응답으로 처리

---

### 5. 배포 환경에서 API 요청이 localhost:8080으로 하드코딩

**증상**
프론트엔드 배포 후 API 요청이 운영 서버가 아닌 localhost:8080으로 향함

**원인**
API base URL이 코드에 하드코딩되어 배포 환경에서도 로컬 주소 사용

**해결**
- `frontend/.env.production`에 `VITE_API_BASE_URL` 추가
- nginx `/api/` 프록시 설정에서 `/api` prefix 제거 후 백엔드로 전달
- `.env.production` 경로에서 중복 `/api` 제거 (별도 핫픽스)
