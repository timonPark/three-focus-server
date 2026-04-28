# Three Focus Servers

오늘 해야 할 일 중 가장 중요한 3가지를 선택하고, 시간표에 배치하여 하루를 집중적으로 관리하는 Three Focus 앱의 백엔드 서버입니다.

## 기술 스택

| 항목 | 버전 |
|------|------|
| JDK | 21 (LTS) |
| Kotlin | 2.1.20 |
| Spring Boot | 3.4.5 |
| Gradle | Kotlin DSL |
| DB | PostgreSQL 16 |
| ORM | Spring Data JPA + jOOQ |
| 인증 | Spring Security + JWT |
| DB 마이그레이션 | Flyway |
| API 문서 | SpringDoc OpenAPI 3.0 (Swagger UI) |

## 프로젝트 구조

```
src/main/kotlin/com/threefocus
├── ThreeFocusApplication.kt
├── domain
│   ├── auth         # 회원가입, 로그인
│   ├── todo         # 할 일 CRUD
│   ├── top3         # 오늘의 Top3 선택
│   ├── schedule     # 시간 배치
│   └── share        # 일정 공유
└── global
    ├── config       # Security, Swagger 설정
    ├── security     # JWT 처리
    └── exception    # 공통 예외 처리
```

각 도메인은 `controller / service / repository / entity / dto` 구조로 구성됩니다.

## DB 접근 전략

- **JPA** — 단건 CUD (Create, Update, Delete)
- **jOOQ** — Query (조회 및 복잡한 쿼리)

## API 목록

| 도메인 | 엔드포인트 | 메서드 | 인증 필요 |
|--------|-----------|--------|----------|
| 인증 | `/api/auth/sign-up` | POST | X |
| 인증 | `/api/auth/login` | POST | X |
| 할 일 | `/api/todos` | POST | O |
| 할 일 | `/api/todos?date={date}` | GET | O |
| 할 일 | `/api/todos/{todoId}` | PUT | O |
| 할 일 | `/api/todos/{todoId}` | DELETE | O |
| Top3 | `/api/top3` | POST | O |
| Top3 | `/api/top3?date={date}` | GET | O |
| 시간 배치 | `/api/schedules` | PUT | O |
| 공유 | `/api/shares` | POST | O |
| 공유 | `/api/shares/{shareToken}` | GET | X |

## 시작하기

### 사전 요구사항

- JDK 21
- Docker / Docker Compose

### 1. DB 실행

```bash
docker-compose up -d
```

### 2. 애플리케이션 실행

```bash
./gradlew bootRun
```

기본 실행 환경은 `local`이며, `application-local.yml` 설정이 적용됩니다.

### 3. API 문서 확인

서버 실행 후 브라우저에서 접속:

```
http://localhost:8080/swagger-ui.html
```

## 환경 설정

`src/main/resources` 아래 환경별 설정 파일이 분리되어 있습니다.

| 파일 | 용도 |
|------|------|
| `application.yml` | 공통 설정 (active profile 지정) |
| `application-local.yml` | 로컬 개발 환경 |
| `application-dev.yml` | 개발 서버 환경 |
| `application-prod.yml` | 운영 환경 |

환경 변수로 profile을 변경할 수 있습니다:

```bash
./gradlew bootRun --args='--spring.profiles.active=dev'
```

## 인증

JWT Bearer Token 방식을 사용합니다. 로그인 또는 회원가입 후 발급된 `accessToken`을 요청 헤더에 포함해야 합니다.

```
Authorization: Bearer {accessToken}
```
