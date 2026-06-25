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

### 2. 무한 트리 응원 구조의 N+1 문제

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
