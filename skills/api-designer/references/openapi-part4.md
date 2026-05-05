<!-- part 4/6 of openapi.md -->

### Security Schemes

```yaml
components:
  securitySchemes:
    bearerAuth:
      type: http
      scheme: bearer
      bearerFormat: JWT
      description: JWT access token

    apiKey:
      type: apiKey
      in: header
      name: X-API-Key
      description: API key for authentication

    oauth2:
      type: oauth2
      flows:
        authorizationCode:
          authorizationUrl: https://auth.example.com/oauth/authorize
          tokenUrl: https://auth.example.com/oauth/token
          scopes:
            users:read: Read user data
            users:write: Create and update users
            users:delete: Delete users
```

Apply security globally or per-operation:

```yaml
# Global security
security:
  - bearerAuth: []

# Or per-operation
paths:
  /users:
    get:
      security:
        - bearerAuth: []
        - apiKey: []  # Alternative auth method
```

### Responses

Reusable response definitions:

```yaml
components:
  responses:
    NotFound:
      description: Resource not found
      content:
        application/json:
          schema:
            $ref: '#/components/schemas/Error'
          example:
            error:
              code: RESOURCE_NOT_FOUND
              message: The requested resource was not found

    Unauthorized:
      description: Authentication required
      headers:
        WWW-Authenticate:
          schema:
            type: string
          description: Authentication method
      content:
        application/json:
          schema:
            $ref: '#/components/schemas/Error'

    ValidationError:
      description: Validation failed
      content:
        application/json:
          schema:
            $ref: '#/components/schemas/Error'
          example:
            error:
              code: VALIDATION_ERROR
              message: Request validation failed
              details:
                - field: email
                  code: INVALID_FORMAT
                  message: Email must be a valid email address

    RateLimitExceeded:
      description: Rate limit exceeded
      headers:
        X-RateLimit-Limit:
          schema:
            type: integer
          description: Request limit per hour
        X-RateLimit-Remaining:
          schema:
            type: integer
          description: Remaining requests
        X-RateLimit-Reset:
          schema:
            type: integer
            format: int64
          description: Time when limit resets (Unix timestamp)
        Retry-After:
          schema:
            type: integer
          description: Seconds to wait before retry
      content:
        application/json:
          schema:
            $ref: '#/components/schemas/Error'
```

### Examples

```yaml
components:
  examples:
    UserListSuccess:
      summary: Successful user list response
      value:
        data:
          - id: 1
            email: john@example.com
            name: John Doe
            status: active
            created_at: "2024-01-15T10:30:00Z"
          - id: 2
            email: jane@example.com
            name: Jane Smith
            status: active
            created_at: "2024-01-16T14:20:00Z"
        pagination:
          offset: 0
          limit: 20
          total: 150
          has_more: true

    CreateUserBasic:
      summary: Create user with minimal fields
      value:
        email: newuser@example.com
        name: New User
```

