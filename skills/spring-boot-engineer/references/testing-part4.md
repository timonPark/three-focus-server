<!-- part 4/4 of testing.md -->

## Testing Configuration

```java
// application-test.yml
spring:
  datasource:
    url: jdbc:h2:mem:testdb
    driver-class-name: org.h2.Driver
  jpa:
    hibernate:
      ddl-auto: create-drop
    show-sql: true
    properties:
      hibernate:
        format_sql: true
  security:
    user:
      name: test
      password: test

logging:
  level:
    org.hibernate.SQL: DEBUG
    org.hibernate.type.descriptor.sql.BasicBinder: TRACE

// Test Configuration Class
@TestConfiguration
public class TestConfig {

    @Bean
    @Primary
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder(4); // Faster for tests
    }

    @Bean
    public Clock fixedClock() {
        return Clock.fixed(
            Instant.parse("2024-01-01T00:00:00Z"),
            ZoneId.of("UTC")
        );
    }
}
```

## Test Fixtures with @DataJpaTest

```java
@Component
public class TestDataFactory {

    public static User createUser(String email, String username) {
        return User.builder()
            .email(email)
            .password("encodedPassword")
            .username(username)
            .active(true)
            .createdAt(LocalDateTime.now())
            .updatedAt(LocalDateTime.now())
            .build();
    }

    public static UserCreateRequest createUserRequest() {
        return new UserCreateRequest(
            "test@example.com",
            "Password123",
            "testuser",
            25
        );
    }
}
```

## Quick Reference

| Annotation | Purpose |
|------------|---------|
| `@SpringBootTest` | Full application context integration test |
| `@WebMvcTest` | Test MVC controllers with mocked services |
| `@WebFluxTest` | Test reactive controllers |
| `@DataJpaTest` | Test JPA repositories with in-memory database |
| `@MockBean` | Add mock bean to Spring context |
| `@WithMockUser` | Mock authenticated user for security tests |
| `@Testcontainers` | Enable Testcontainers support |
| `@ActiveProfiles` | Activate specific Spring profiles for test |

## Testing Best Practices

- Write tests following AAA pattern (Arrange, Act, Assert)
- Use descriptive test names with @DisplayName
- Mock external dependencies, use real DB with Testcontainers
- Achieve 85%+ code coverage
- Test happy path and edge cases
- Use @Transactional for test data cleanup
- Separate unit tests from integration tests
- Use parameterized tests for multiple scenarios
- Test security rules and validation
- Keep tests fast and independent
