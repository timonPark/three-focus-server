<!-- part 4/4 of error-handling.md -->

## Request ID Tracking

Always include request ID for debugging:

```http
Response Headers:
X-Request-ID: req_abc123

Response Body:
{
  "error": {
    "code": "INTERNAL_SERVER_ERROR",
    "message": "An unexpected error occurred",
    "request_id": "req_abc123"
  }
}
```

Clients can reference request ID in support tickets.

## Error Documentation

Document all possible errors for each endpoint:

```yaml
/users/{id}:
  get:
    responses:
      '200':
        description: Success
      '401':
        description: Authentication failed
        content:
          application/json:
            schema:
              $ref: '#/components/schemas/Error'
            examples:
              missing_token:
                value:
                  error:
                    code: MISSING_TOKEN
                    message: No authentication token provided
              invalid_token:
                value:
                  error:
                    code: INVALID_TOKEN
                    message: Token is invalid or expired
      '404':
        description: User not found
        content:
          application/json:
            schema:
              $ref: '#/components/schemas/Error'
            examples:
              not_found:
                value:
                  error:
                    code: RESOURCE_NOT_FOUND
                    message: User with ID 123 not found
```

## Retry Guidance

Help clients understand if they should retry:

```json
{
  "error": {
    "code": "SERVICE_UNAVAILABLE",
    "message": "Service temporarily unavailable",
    "retry": {
      "retryable": true,
      "retry_after": 60,
      "max_retries": 3,
      "backoff": "exponential"
    }
  }
}
```

### Retryable Errors

- 408 Request Timeout
- 429 Too Many Requests (with Retry-After)
- 500 Internal Server Error (sometimes)
- 502 Bad Gateway
- 503 Service Unavailable
- 504 Gateway Timeout

### Non-Retryable Errors

- 400 Bad Request
- 401 Unauthorized
- 403 Forbidden
- 404 Not Found
- 409 Conflict
- 422 Unprocessable Entity

## Multi-Language Support

Support error messages in multiple languages:

```http
GET /users/invalid
Accept-Language: es

Response: 404 Not Found
Content-Language: es
{
  "error": {
    "code": "RESOURCE_NOT_FOUND",
    "message": "Usuario con ID 'invalid' no encontrado"
  }
}
```

Always include `code` so clients can implement their own translations.

## Best Practices

1. **Use standard HTTP status codes** - Don't return 200 for errors
2. **Include machine-readable codes** - Error codes for client logic
3. **Provide human-readable messages** - Clear explanations
4. **Be specific but safe** - Don't expose sensitive information
5. **Include request ID** - For tracking and debugging
6. **Document all errors** - Every possible error for each endpoint
7. **Be consistent** - Same format across all endpoints
8. **Help clients retry** - Indicate if error is retryable
9. **Validate early** - Return validation errors immediately
10. **Log errors server-side** - Track errors for monitoring

## Anti-Patterns

Avoid these mistakes:

- **Generic error messages** - "Error occurred" without details
- **Exposing stack traces** - Security risk
- **Inconsistent error format** - Different structure per endpoint
- **Missing error codes** - Only human-readable messages
- **Wrong status codes** - Returning 200 with error in body
- **No request ID** - Makes debugging impossible
- **Undocumented errors** - Clients don't know what to expect
- **Too much information** - Exposing internal implementation
