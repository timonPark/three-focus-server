<!-- part 2/2 of data.md -->

        // Won't rollback transaction if notification fails
        try {
            notificationService.sendCompletionEmail(order);
        } catch (NotificationException e) {
            log.error("Failed to send notification for order {}", orderId, e);
        }
    }
}
```

## Auditing Configuration

```java
@Configuration
@EnableJpaAuditing
public class JpaAuditingConfig {

    @Bean
    public AuditorAware<String> auditorProvider() {
        return () -> {
            Authentication authentication = SecurityContextHolder
                .getContext()
                .getAuthentication();

            if (authentication == null || !authentication.isAuthenticated()) {
                return Optional.of("system");
            }

            return Optional.of(authentication.getName());
        };
    }
}

@MappedSuperclass
@EntityListeners(AuditingEntityListener.class)
@Getter @Setter
public abstract class AuditableEntity {

    @CreatedDate
    @Column(nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @CreatedBy
    @Column(nullable = false, updatable = false, length = 100)
    private String createdBy;

    @LastModifiedDate
    @Column(nullable = false)
    private LocalDateTime updatedAt;

    @LastModifiedBy
    @Column(nullable = false, length = 100)
    private String updatedBy;
}
```

## Projections

```java
// Interface-based projection
public interface UserSummary {
    Long getId();
    String getUsername();
    String getEmail();

    @Value("#{target.firstName + ' ' + target.lastName}")
    String getFullName();
}

// Class-based projection (DTO)
public record UserSummaryDto(
    Long id,
    String username,
    String email
) {}

// Usage
public interface UserRepository extends JpaRepository<User, Long> {
    List<UserSummary> findAllBy();

    <T> List<T> findAllBy(Class<T> type);
}

// Service usage
List<UserSummary> summaries = userRepository.findAllBy();
List<UserSummaryDto> dtos = userRepository.findAllBy(UserSummaryDto.class);
```

## Query Optimization

```java
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class UserQueryService {
    private final UserRepository userRepository;
    private final EntityManager entityManager;

    // N+1 problem solved with JOIN FETCH
    @Query("SELECT DISTINCT u FROM User u " +
           "LEFT JOIN FETCH u.addresses " +
           "LEFT JOIN FETCH u.roles " +
           "WHERE u.active = true")
    List<User> findAllActiveWithAssociations();

    // Batch fetching
    @BatchSize(size = 25)
    @OneToMany(mappedBy = "user")
    private List<Order> orders;

    // EntityGraph for dynamic fetching
    @EntityGraph(attributePaths = {"addresses", "roles"})
    List<User> findAllByActiveTrue();

    // Pagination to avoid loading all data
    public Page<User> findAllUsers(Pageable pageable) {
        return userRepository.findAll(pageable);
    }

    // Native query for complex queries
    @Query(value = """
        SELECT u.* FROM users u
        INNER JOIN orders o ON u.id = o.user_id
        WHERE o.created_at >= :since
        GROUP BY u.id
        HAVING COUNT(o.id) >= :minOrders
        """, nativeQuery = true)
    List<User> findFrequentBuyers(@Param("since") LocalDateTime since,
                                  @Param("minOrders") int minOrders);
}
```

## Database Migrations (Flyway)

```sql
-- V1__create_users_table.sql
CREATE TABLE users (
    id BIGSERIAL PRIMARY KEY,
    email VARCHAR(100) NOT NULL UNIQUE,
    password VARCHAR(100) NOT NULL,
    username VARCHAR(50) NOT NULL UNIQUE,
    active BOOLEAN NOT NULL DEFAULT true,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    version BIGINT NOT NULL DEFAULT 0
);

CREATE INDEX idx_users_email ON users(email);
CREATE INDEX idx_users_username ON users(username);
CREATE INDEX idx_users_active ON users(active);

-- V2__create_addresses_table.sql
CREATE TABLE addresses (
    id BIGSERIAL PRIMARY KEY,
    user_id BIGINT NOT NULL REFERENCES users(id) ON DELETE CASCADE,
    street VARCHAR(200) NOT NULL,
    city VARCHAR(100) NOT NULL,
    country VARCHAR(2) NOT NULL,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP
);

CREATE INDEX idx_addresses_user_id ON addresses(user_id);
```

## Quick Reference

| Annotation | Purpose |
|------------|---------|
| `@Entity` | Marks class as JPA entity |
| `@Table` | Specifies table details and indexes |
| `@Id` | Marks primary key field |
| `@GeneratedValue` | Auto-generated primary key strategy |
| `@Column` | Column constraints and mapping |
| `@OneToMany/@ManyToOne` | One-to-many/many-to-one relationships |
| `@ManyToMany` | Many-to-many relationships |
| `@JoinColumn/@JoinTable` | Join column/table configuration |
| `@Transactional` | Declares transaction boundaries |
| `@Query` | Custom JPQL/native queries |
| `@Modifying` | Marks query as UPDATE/DELETE |
| `@EntityGraph` | Defines fetch graph for associations |
| `@Version` | Optimistic locking version field |
