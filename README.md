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

## 기능 추가 프로세스

새 기능을 추가할 때는 아래 순서로 진행합니다.

```
1. DB 마이그레이션     src/main/resources/db/migration/V{n}__description.sql
2. Entity             domain/{도메인}/entity/
3. DTO                domain/{도메인}/dto/
4. Repository         domain/{도메인}/repository/
                        - JPA  : CUD (UserRepository)
                        - jOOQ : Query (UserQueryRepository)
5. Service            domain/{도메인}/service/
6. Controller         domain/{도메인}/controller/
7. 테스트              test/.../service/{도메인}ServiceTest.kt     (단위)
                      test/.../controller/{도메인}ControllerTest.kt (통합)
```

> 마이그레이션 파일은 한 번 적용되면 수정 불가 — 변경이 필요하면 새 버전(V{n+1})으로 추가합니다.

## DB 접근 전략

- **JPA** — 단건 CUD (Create, Update, Delete)
- **jOOQ** — Query (조회 및 복잡한 쿼리)

## DB 구조

![ER Diagram](er-diagram.drawio.svg)

> 스키마 변경 시 `er-diagram.drawio`를 수정 후 SVG를 재내보내기 해주세요.
> draw.io 앱에서 파일을 열고 **File → Export As → SVG**로 `er-diagram.drawio.svg`를 덮어쓰면 됩니다.

## API 목록

| 도메인 | 메서드 | 엔드포인트 | 설명 | 인증 필요 |
|--------|--------|-----------|------|----------|
| 인증 | POST | `/api/auth/sign-up` | 회원가입 | X |
| 인증 | POST | `/api/auth/login` | 로그인 | X |
| 인증 | POST | `/api/auth/refresh` | 액세스 토큰 재발급 | X |
| 할 일 | POST | `/api/todos` | 할 일 생성 | O |
| 할 일 | GET | `/api/todos?date={date}` | 날짜별 할 일 조회 | O |
| 할 일 | PUT | `/api/todos/{todoId}` | 할 일 수정 | O |
| 할 일 | DELETE | `/api/todos/{todoId}` | 할 일 삭제 (cascade) | O |
| Top3 | POST | `/api/top3` | Top3 지정 | O |
| Top3 | GET | `/api/top3?date={date}` | 날짜별 Top3 조회 | O |
| 시간 배치 | PUT | `/api/schedules` | 시간 배치 (upsert) | O |
| 시간 배치 | GET | `/api/schedules?date={date}` | 일정 시각화 조회 | O |
| 시간 배치 | DELETE | `/api/schedules/{todoId}` | 시간 배치 취소 | O |
| 공유 | POST | `/api/shares` | 공유 링크 생성 | O |
| 공유 | GET | `/api/shares/{shareToken}` | 공유 일정 조회 | X |

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

#### Swagger에서 인증이 필요한 API 테스트하기

1. `/api/auth/login` 또는 `/api/auth/sign-up`으로 `accessToken` 발급
2. Swagger UI 상단 **Authorize** 버튼 클릭
3. `Bearer Authentication` 항목에 발급받은 `accessToken` 값 입력 후 **Authorize**
4. 이후 인증 필요 API를 자유롭게 테스트

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

## 테스트

### 전체 테스트 실행

```bash
./gradlew test
```

### 도메인별 테스트 실행

```bash
./gradlew test --tests "com.threefocus.domain.*"
```

### 테스트 구조

각 도메인은 Service 단위 테스트와 Controller 통합 테스트로 구성됩니다.

```
src/test/kotlin/com/threefocus/domain/
├── auth/
│   ├── service/AuthServiceTest.kt        # 단위 테스트 (MockitoExtension)
│   └── controller/AuthControllerTest.kt  # 통합 테스트 (WebMvcTest)
├── todo/
│   ├── service/TodoServiceTest.kt
│   └── controller/TodoControllerTest.kt
├── top3/
│   ├── service/Top3ServiceTest.kt
│   └── controller/Top3ControllerTest.kt
├── schedule/
│   ├── service/ScheduleServiceTest.kt
│   └── controller/ScheduleControllerTest.kt
└── share/
    ├── service/ShareServiceTest.kt
    └── controller/ShareControllerTest.kt
```

### 테스트 전략

- **Service Tests** — `@ExtendWith(MockitoExtension::class)` + Mockito 순수 단위 테스트
- **Controller Tests** — `@WebMvcTest` + `@Import(SecurityConfig::class)` + `@MockBean` 서비스
  - 인증 필요 엔드포인트: `@WithMockUser(username = "{userId}")` 사용
  - `@MockBean JwtTokenProvider`, `@MockBean UserDetailsService` 필수

## 인증

JWT Bearer Token 방식을 사용합니다. 로그인 또는 회원가입 후 발급된 `accessToken`을 요청 헤더에 포함해야 합니다.

```
Authorization: Bearer {accessToken}
```
