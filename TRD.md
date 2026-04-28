# TRD (Technical Requirements Document)

## 1. 환경 요구사항

- JDK: 현재 기준 안정화된 버전 사용 (LTS 버전 권장, 현재 JDK 21)
- Spring Boot: 현재 기준 안정화된 버전 사용 (현재 3.4.5 → Initializr 기준 최신 3.5.0 적용)
- 언어: Kotlin (현재 안정화 버전 2.1.20)
- DB: PostgreSQL (docker-compose.yaml 파일로 구축)

## 2. 빌드 도구

- Gradle (Kotlin DSL) 사용
- 빌드 파일: build.gradle.kts

## 3. 아키텍처

- 레이어드 아키텍처 적용: Controller → Service → Repository
- 패키지 구조: domain 기반으로 분리

```
com.threefocus
├── domain
│   ├── auth
│   ├── todo
│   ├── top3
│   ├── schedule
│   └── share
├── global
│   ├── config
│   ├── security
│   └── exception
```

## 4. API 설계

- REST API 방식
- PRD 기반 API 목록:

| 도메인 | 기능 | 메서드 |
|--------|------|--------|
| 인증 | 회원가입 / 로그인 | POST |
| 할 일(Todo) | CRUD | GET / POST / PUT / DELETE |
| Top3 선택 | 오늘의 Top3 지정 / 조회 | GET / POST |
| 시간 배치 | 할 일에 시간 할당 | PUT |
| 일정 공유 | 공유 링크 생성 | POST |

## 5. 인증 / 보안

- Spring Security + JWT 기반 인증
- Access Token / Refresh Token 전략 사용

## 6. DB 접근

- Spring Data JPA + Hibernate 사용 (단건 CUD — Create, Update, Delete)
- jOOQ 사용 (Query — 조회 및 복잡한 쿼리 처리)
- DB 마이그레이션: Flyway 사용

## 7. 환경 분리

- application.yml 기반으로 환경 분리
- 환경 종류: local / dev / prod

## 8. API 문서화

- SpringDoc OpenAPI 3.0 (Swagger UI) 사용
