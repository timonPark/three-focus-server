<!-- part 2/4 of cloud.md -->

## Spring Cloud Gateway

```java
@SpringBootApplication
public class GatewayApplication {
    public static void main(String[] args) {
        SpringApplication.run(GatewayApplication.class, args);
    }

    @Bean
    public RouteLocator customRouteLocator(RouteLocatorBuilder builder) {
        return builder.routes()
            .route("user-service", r -> r
                .path("/api/users/**")
                .filters(f -> f
                    .rewritePath("/api/users/(?<segment>.*)", "/users/${segment}")
                    .addRequestHeader("X-Gateway", "Spring-Cloud-Gateway")
                    .circuitBreaker(config -> config
                        .setName("userServiceCircuitBreaker")
                        .setFallbackUri("forward:/fallback/users")
                    )
                    .retry(config -> config
                        .setRetries(3)
                        .setStatuses(HttpStatus.SERVICE_UNAVAILABLE)
                    )
                )
                .uri("lb://user-service")
            )
            .route("order-service", r -> r
                .path("/api/orders/**")
                .filters(f -> f
                    .rewritePath("/api/orders/(?<segment>.*)", "/orders/${segment}")
                    .requestRateLimiter(config -> config
                        .setRateLimiter(redisRateLimiter())
                        .setKeyResolver(userKeyResolver())
                    )
                )
                .uri("lb://order-service")
            )
            .build();
    }

    @Bean
    public RedisRateLimiter redisRateLimiter() {
        return new RedisRateLimiter(10, 20); // replenishRate, burstCapacity
    }

    @Bean
    public KeyResolver userKeyResolver() {
        return exchange -> Mono.just(
            exchange.getRequest().getHeaders().getFirst("X-User-Id")
        );
    }
}

// application.yml (Gateway)
spring:
  cloud:
    gateway:
      discovery:
        locator:
          enabled: true
          lower-case-service-id: true
      default-filters:
        - DedupeResponseHeader=Access-Control-Allow-Origin
      globalcors:
        cors-configurations:
          '[/**]':
            allowed-origins: "*"
            allowed-methods:
              - GET
              - POST
              - PUT
              - DELETE
            allowed-headers: "*"
```

## Circuit Breaker - Resilience4j

```java
@Service
@RequiredArgsConstructor
public class ExternalApiService {
    private final WebClient webClient;

    @CircuitBreaker(name = "externalApi", fallbackMethod = "getFallbackData")
    @Retry(name = "externalApi")
    @RateLimiter(name = "externalApi")
    public Mono<ExternalData> getData(String id) {
        return webClient
            .get()
            .uri("/data/{id}", id)
            .retrieve()
            .bodyToMono(ExternalData.class)
            .timeout(Duration.ofSeconds(3));
    }

    private Mono<ExternalData> getFallbackData(String id, Exception e) {
        log.warn("Fallback triggered for id: {}, error: {}", id, e.getMessage());
        return Mono.just(new ExternalData(id, "Fallback data", LocalDateTime.now()));
    }
}

// application.yml
resilience4j:
  circuitbreaker:
    instances:
      externalApi:
        register-health-indicator: true
        sliding-window-size: 10
        minimum-number-of-calls: 5
        permitted-number-of-calls-in-half-open-state: 3
        automatic-transition-from-open-to-half-open-enabled: true
        wait-duration-in-open-state: 5s
        failure-rate-threshold: 50
        event-consumer-buffer-size: 10

  retry:
    instances:
      externalApi:
        max-attempts: 3
        wait-duration: 1s
        enable-exponential-backoff: true
        exponential-backoff-multiplier: 2

  ratelimiter:
    instances:
      externalApi:
        limit-for-period: 10
        limit-refresh-period: 1s
        timeout-duration: 0s
```

