<img width="1687" height="915" alt="Copy of Untitled Diagram" src="https://github.com/user-attachments/assets/5e87c89c-78ec-4f13-8c31-993055dacc83" /># 🥚 삶은달걀

> 준비하는 당신을 위한 성장 기록 및 응원 서비스

**📎 서비스 주소:** [life-is-egg.com](https://life-is-egg.com)  

---

## 📌 프로젝트 소개

취업 준비, 시험 준비 등 미래를 준비하는 준비생들을 위한 자기관리, 마음 공유 서비스 삶은 달걀입니다. 

- 일기(Post) 작성 및 피드 열람
- 무한 깊이 트리 구조의 응원(Cheer) 기능
- 목표(Goal) 설정 및 자동 완료 처리
- 일정(Schedule) 관리
- 알림(Alarm) 폴링 방식 구현
- 대시보드 통계 제공

---

## 🏗️ 아키텍처

```
[사용자 브라우저]
       │
       ▼
[EC2 - Ubuntu 22.04 / t3.micro / 서울 리전]
       │
  [nginx]
  ├── / → React 정적 파일 서빙 (빌드 산출물)
  └── /api/ → Spring Boot :8080 (리버스 프록시, /api prefix 제거 후 전달)
       │
  [Spring Boot 4.0.2 / Java 21]
       │
  [MariaDB]

[GitHub Actions]
  ├── backend CI/CD → EC2 배포 자동화
  └── frontend CI/CD → 빌드 후 EC2 정적 파일 갱신
```

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
| API 문서 | springdoc-openapi 2.3.0 (개발 환경 전용) |
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

<img width="1687" height="915" alt="Copy of Untitled Diagram" src="https://github.com/user-attachments/assets/43ff42bb-3fcb-4188-a5d4-2d1399312e37" />



## 🔧 트러블슈팅

### 1. `Map.of()` null 값으로 인한 NullPointerException

**증상**
응원 작성, 알람 읽음 처리 API 응답에서 NPE 발생

**원인**
`Map.of()`는 null 값을 허용하지 않음. 응답 body에 `"data", null` 형태로 넣으면 런타임에 예외 발생

**해결**
응답에서 `"data", null` 제거. null 값이 필요 없는 응답은 해당 키 자체를 빼거나 빈 응답으로 처리

---

### 2. 배포 환경에서 API 요청이 localhost:8080으로 하드코딩

**증상**
프론트엔드 배포 후 API 요청이 운영 서버가 아닌 `localhost:8080`으로 향함

**원인**
API base URL이 코드에 하드코딩되어 있어 배포 환경에서도 로컬 주소 사용

**해결**
- `frontend/.env.production`에 `VITE_API_BASE_URL` 추가
- nginx `/api/` 프록시 설정에서 `/api` prefix 제거 후 백엔드로 전달
- `.env.production` 경로에서 중복 `/api` 제거 (별도 핫픽스)

---

### 3. 배포 환경 로그인 시 403 오류 — CORS Preflight 미허용

**증상**
배포 환경에서 로그인 요청 시 403. 로컬에서는 정상 동작

**원인 1 — CORS allowedMethods 누락**
브라우저는 실제 요청 전 `OPTIONS` Preflight 요청을 보내는데, `allowedMethods`에 `OPTIONS`가 없어 차단됨.  
로컬은 동일 출처라 Preflight 자체가 발생하지 않아 문제 없이 동작

**해결 1**
CORS 설정 `allowedMethods`에 `OPTIONS` 추가, 운영 도메인 허용 추가

**원인 2 — Spring Security가 CORS 필터보다 먼저 요청 차단**
`allowedMethods`에 OPTIONS를 추가해도 Spring Security가 CORS 필터 앞단에서 요청을 가로채 차단

**해결 2**
`SecurityConfig`의 `authorizeHttpRequests`에 OPTIONS 요청 전체 `permitAll()` 추가

---

### 4. 목표 추가/수정 API 호출 누락 및 오류

**증상**
- 목표 저장 버튼 클릭 시 아무 반응 없음
- 수정 클릭 시 로그인 페이지로 튕김
- 목표 추가 시 null 참조 오류 발생

**원인**
- `handleSubmit`에 API 호출 코드 자체가 누락됨
- 수정 시 `unit` 필드 누락
- 추가 로직에서 `api.put` → `api.post`로 써야 하는데 반대로 작성 (`editTarget.id` null 참조로 연결)
- axios 인터셉터에서 403도 로그인 페이지로 튕기도록 처리되어 있어 목표 수정 후 리다이렉트

**해결**
- `handleSubmit`에 추가(`POST /api/goals`), 수정(`PUT /api/goals/{id}`) API 호출 복구
- 수정 시 `unit` 필드 포함
- axios 인터셉터 403 처리 제거, 401만 로그인 페이지로 리다이렉트
