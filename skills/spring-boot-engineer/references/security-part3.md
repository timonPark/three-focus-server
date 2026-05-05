<!-- part 3/3 of security.md -->

## Authentication Service

```java
@Service
@RequiredArgsConstructor
@Transactional
public class AuthenticationService {
    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtService jwtService;
    private final AuthenticationManager authenticationManager;

    public AuthenticationResponse register(RegisterRequest request) {
        if (userRepository.existsByEmail(request.email())) {
            throw new DuplicateResourceException("Email already registered");
        }

        User user = User.builder()
            .email(request.email())
            .password(passwordEncoder.encode(request.password()))
            .username(request.username())
            .active(true)
            .roles(Set.of(Role.builder().name("USER").build()))
            .build();

        user = userRepository.save(user);

        String accessToken = jwtService.generateToken(convertToUserDetails(user));
        String refreshToken = jwtService.generateRefreshToken(convertToUserDetails(user));

        return new AuthenticationResponse(accessToken, refreshToken);
    }

    public AuthenticationResponse login(LoginRequest request) {
        authenticationManager.authenticate(
            new UsernamePasswordAuthenticationToken(
                request.email(),
                request.password()
            )
        );

        User user = userRepository.findByEmail(request.email())
            .orElseThrow(() -> new UsernameNotFoundException("User not found"));

        String accessToken = jwtService.generateToken(convertToUserDetails(user));
        String refreshToken = jwtService.generateRefreshToken(convertToUserDetails(user));

        return new AuthenticationResponse(accessToken, refreshToken);
    }

    public AuthenticationResponse refreshToken(RefreshTokenRequest request) {
        String username = jwtService.extractUsername(request.refreshToken());

        User user = userRepository.findByEmail(username)
            .orElseThrow(() -> new UsernameNotFoundException("User not found"));

        UserDetails userDetails = convertToUserDetails(user);

        if (!jwtService.isTokenValid(request.refreshToken(), userDetails)) {
            throw new InvalidTokenException("Invalid refresh token");
        }

        String accessToken = jwtService.generateToken(userDetails);

        return new AuthenticationResponse(accessToken, request.refreshToken());
    }

    private UserDetails convertToUserDetails(User user) {
        return org.springframework.security.core.userdetails.User
            .builder()
            .username(user.getEmail())
            .password(user.getPassword())
            .authorities(user.getRoles().stream()
                .map(role -> new SimpleGrantedAuthority("ROLE_" + role.getName()))
                .collect(Collectors.toList()))
            .build();
    }
}
```

## Method Security

```java
@Service
@RequiredArgsConstructor
public class UserService {
    private final UserRepository userRepository;

    @PreAuthorize("hasRole('ADMIN')")
    public List<User> getAllUsers() {
        return userRepository.findAll();
    }

    @PreAuthorize("hasRole('ADMIN') or #userId == authentication.principal.id")
    public User getUserById(Long userId) {
        return userRepository.findById(userId)
            .orElseThrow(() -> new ResourceNotFoundException("User not found"));
    }

    @PreAuthorize("isAuthenticated()")
    @PostAuthorize("returnObject.email == authentication.principal.username")
    public User updateProfile(Long userId, UserUpdateRequest request) {
        User user = getUserById(userId);
        // Update logic
        return userRepository.save(user);
    }

    @Secured({"ROLE_ADMIN", "ROLE_MANAGER"})
    public void deleteUser(Long userId) {
        userRepository.deleteById(userId);
    }
}
```

## OAuth2 Resource Server (JWT)

```java
@Configuration
@EnableWebSecurity
public class OAuth2ResourceServerConfig {

    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        http
            .authorizeHttpRequests(auth -> auth
                .requestMatchers("/public/**").permitAll()
                .anyRequest().authenticated()
            )
            .oauth2ResourceServer(oauth2 -> oauth2
                .jwt(jwt -> jwt
                    .jwtAuthenticationConverter(jwtAuthenticationConverter())
                )
            );

        return http.build();
    }

    @Bean
    public JwtDecoder jwtDecoder() {
        return JwtDecoders.fromIssuerLocation("https://auth.example.com");
    }

    @Bean
    public JwtAuthenticationConverter jwtAuthenticationConverter() {
        JwtGrantedAuthoritiesConverter grantedAuthoritiesConverter =
            new JwtGrantedAuthoritiesConverter();
        grantedAuthoritiesConverter.setAuthoritiesClaimName("roles");
        grantedAuthoritiesConverter.setAuthorityPrefix("ROLE_");

        JwtAuthenticationConverter jwtAuthenticationConverter =
            new JwtAuthenticationConverter();
        jwtAuthenticationConverter.setJwtGrantedAuthoritiesConverter(
            grantedAuthoritiesConverter);

        return jwtAuthenticationConverter;
    }
}
```

## Quick Reference

| Annotation | Purpose |
|------------|---------|
| `@EnableWebSecurity` | Enables Spring Security |
| `@EnableMethodSecurity` | Enables method-level security annotations |
| `@PreAuthorize` | Checks authorization before method execution |
| `@PostAuthorize` | Checks authorization after method execution |
| `@Secured` | Role-based method security |
| `@WithMockUser` | Mock authenticated user in tests |
| `@AuthenticationPrincipal` | Inject current user in controller |

## Security Best Practices

- Always use HTTPS in production
- Store JWT secret in environment variables
- Use strong password encoding (BCrypt with strength 12+)
- Implement token refresh mechanism
- Add rate limiting to authentication endpoints
- Validate all user inputs
- Log security events
- Keep dependencies updated
- Use CSRF protection for state-changing operations
- Implement proper session timeout
