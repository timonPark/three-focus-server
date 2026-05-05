<!-- part 2/4 of error-handling.md -->

### 2. Authentication Errors (401 Unauthorized)

Missing or invalid authentication credentials.

```http
GET /users/123
Authorization: Bearer invalid_token

Response: 401 Unauthorized
WWW-Authenticate: Bearer realm="api", error="invalid_token"

{
  "error": {
    "code": "INVALID_TOKEN",
    "message": "The access token is invalid or has expired",
    "details": {
      "reason": "token_expired",
      "expired_at": "2024-01-15T10:00:00Z"
    }
  }
}
```

**Common auth error codes:**
- `MISSING_TOKEN` - No auth token provided
- `INVALID_TOKEN` - Token is malformed or invalid
- `EXPIRED_TOKEN` - Token has expired
- `REVOKED_TOKEN` - Token has been revoked

### 3. Authorization Errors (403 Forbidden)

Authenticated but not authorized to perform action.

```http
DELETE /users/123
Authorization: Bearer valid_token

Response: 403 Forbidden
{
  "error": {
    "code": "INSUFFICIENT_PERMISSIONS",
    "message": "You do not have permission to delete this user",
    "details": {
      "required_permission": "users:delete",
      "your_permissions": ["users:read", "users:update"]
    }
  }
}
```

### 4. Not Found Errors (404 Not Found)

Resource doesn't exist.

```http
GET /users/99999

Response: 404 Not Found
{
  "error": {
    "code": "RESOURCE_NOT_FOUND",
    "message": "User with ID 99999 not found",
    "details": {
      "resource_type": "User",
      "resource_id": "99999"
    }
  }
}
```

### 5. Conflict Errors (409 Conflict)

Request conflicts with current state.

```http
POST /users
Content-Type: application/json

{
  "email": "existing@example.com",
  "name": "John Doe"
}

Response: 409 Conflict
{
  "error": {
    "code": "RESOURCE_ALREADY_EXISTS",
    "message": "User with email 'existing@example.com' already exists",
    "details": {
      "field": "email",
      "value": "existing@example.com",
      "existing_resource": "/users/123"
    }
  }
}
```

### 6. Rate Limiting (429 Too Many Requests)

Client exceeded rate limit.

```http
GET /users

Response: 429 Too Many Requests
Retry-After: 60
X-RateLimit-Limit: 100
X-RateLimit-Remaining: 0
X-RateLimit-Reset: 1705320000

{
  "error": {
    "code": "RATE_LIMIT_EXCEEDED",
    "message": "You have exceeded the rate limit",
    "details": {
      "limit": 100,
      "window": "1 hour",
      "retry_after": 60,
      "reset_at": "2024-01-15T11:00:00Z"
    }
  }
}
```

### 7. Server Errors (500 Internal Server Error)

Unexpected server error.

```http
GET /users/123

Response: 500 Internal Server Error
{
  "error": {
    "code": "INTERNAL_SERVER_ERROR",
    "message": "An unexpected error occurred. Please try again later.",
    "request_id": "req_123456",
    "timestamp": "2024-01-15T10:30:00Z"
  }
}
```

**Never expose:**
- Stack traces
- Database errors
- Internal paths
- Sensitive configuration

### 8. Service Unavailable (503 Service Unavailable)

Service temporarily unavailable.

```http
GET /users

Response: 503 Service Unavailable
Retry-After: 300

{
  "error": {
    "code": "SERVICE_UNAVAILABLE",
    "message": "Service is temporarily unavailable due to maintenance",
    "details": {
      "retry_after": 300,
      "maintenance_end": "2024-01-15T12:00:00Z"
    }
  }
}
```

