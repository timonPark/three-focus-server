<!-- part 1/4 of error-handling.md -->

# API Error Handling

## Error Response Design

Consistent, informative error responses are critical for API usability.

## Standard Error Format

### Basic Error Response

```json
{
  "error": {
    "code": "RESOURCE_NOT_FOUND",
    "message": "User with ID 123 not found",
    "details": null
  }
}
```

### RFC 7807 Problem Details

Standardized error format (application/problem+json):

```http
HTTP/1.1 404 Not Found
Content-Type: application/problem+json

{
  "type": "https://api.example.com/errors/resource-not-found",
  "title": "Resource Not Found",
  "status": 404,
  "detail": "User with ID 123 does not exist",
  "instance": "/users/123"
}
```

**Fields:**
- `type` - URI reference identifying error type
- `title` - Short, human-readable summary
- `status` - HTTP status code
- `detail` - Human-readable explanation specific to this occurrence
- `instance` - URI reference for this specific occurrence

### Extended Error Response

```json
{
  "error": {
    "code": "VALIDATION_ERROR",
    "message": "Request validation failed",
    "details": [
      {
        "field": "email",
        "code": "INVALID_FORMAT",
        "message": "Email must be a valid email address"
      },
      {
        "field": "age",
        "code": "OUT_OF_RANGE",
        "message": "Age must be between 18 and 120"
      }
    ],
    "request_id": "req_123456",
    "timestamp": "2024-01-15T10:30:00Z",
    "documentation_url": "https://api.example.com/docs/errors#validation-error"
  }
}
```

## Error Categories

### 1. Validation Errors (400 Bad Request)

Client sent invalid data.

```http
POST /users
Content-Type: application/json

{
  "name": "",
  "email": "invalid-email",
  "age": 15
}

Response: 400 Bad Request
{
  "error": {
    "code": "VALIDATION_ERROR",
    "message": "Request validation failed",
    "details": [
      {
        "field": "name",
        "code": "REQUIRED",
        "message": "Name is required"
      },
      {
        "field": "email",
        "code": "INVALID_FORMAT",
        "message": "Email must be a valid email address"
      },
      {
        "field": "age",
        "code": "OUT_OF_RANGE",
        "message": "Age must be at least 18",
        "constraints": {
          "min": 18,
          "max": 120
        }
      }
    ]
  }
}
```

