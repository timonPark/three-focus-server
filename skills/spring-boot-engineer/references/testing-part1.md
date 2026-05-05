<!-- part 1/4 of testing.md -->

# Testing - Spring Boot Test

## Unit Testing with JUnit 5

```java
@ExtendWith(MockitoExtension.class)
class UserServiceTest {

    @Mock
    private UserRepository userRepository;

    @Mock
    private PasswordEncoder passwordEncoder;

    @InjectMocks
    private UserService userService;

    @Test
    @DisplayName("Should create user successfully")
    void shouldCreateUser() {
        // Given
        UserCreateRequest request = new UserCreateRequest(
            "test@example.com",
            "Password123",
            "testuser",
            25
        );

        User user = User.builder()
            .id(1L)
            .email(request.email())
            .username(request.username())
            .build();

        when(userRepository.existsByEmail(request.email())).thenReturn(false);
        when(passwordEncoder.encode(request.password())).thenReturn("encodedPassword");
        when(userRepository.save(any(User.class))).thenReturn(user);

        // When
        UserResponse response = userService.create(request);

        // Then
        assertThat(response).isNotNull();
        assertThat(response.email()).isEqualTo(request.email());

        verify(userRepository).existsByEmail(request.email());
        verify(passwordEncoder).encode(request.password());
        verify(userRepository).save(any(User.class));
    }

    @Test
    @DisplayName("Should throw exception when email already exists")
    void shouldThrowExceptionWhenEmailExists() {
        // Given
        UserCreateRequest request = new UserCreateRequest(
            "test@example.com",
            "Password123",
            "testuser",
            25
        );

        when(userRepository.existsByEmail(request.email())).thenReturn(true);

        // When & Then
        assertThatThrownBy(() -> userService.create(request))
            .isInstanceOf(DuplicateResourceException.class)
            .hasMessageContaining("Email already registered");

        verify(userRepository, never()).save(any(User.class));
    }
}
```

## Integration Testing with @SpringBootTest

```java
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@ActiveProfiles("test")
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
class UserIntegrationTest {

    @Autowired
    private TestRestTemplate restTemplate;

    @Autowired
    private UserRepository userRepository;

    @BeforeEach
    void setUp() {
        userRepository.deleteAll();
    }

    @Test
    @Order(1)
    @DisplayName("Should create user via API")
    void shouldCreateUserViaApi() {
        // Given
        UserCreateRequest request = new UserCreateRequest(
            "test@example.com",
            "Password123",
            "testuser",
            25
        );

        // When
        ResponseEntity<UserResponse> response = restTemplate.postForEntity(
            "/api/v1/users",
            request,
            UserResponse.class
        );

        // Then
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.CREATED);
        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody().email()).isEqualTo(request.email());
        assertThat(response.getHeaders().getLocation()).isNotNull();
    }

    @Test
    @Order(2)
    @DisplayName("Should return validation error for invalid request")
    void shouldReturnValidationError() {
        // Given
        UserCreateRequest request = new UserCreateRequest(
            "invalid-email",
            "short",
            "u",
            15
        );

        // When
        ResponseEntity<ValidationErrorResponse> response = restTemplate.postForEntity(
            "/api/v1/users",
            request,
            ValidationErrorResponse.class
        );

        // Then
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody().errors()).isNotEmpty();
    }
}
```

