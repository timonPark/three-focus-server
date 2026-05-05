<!-- part 1/2 of rest-patterns.md -->

# REST Design Patterns

## Resource-Oriented Architecture

REST APIs are built around resources, not actions. Resources are the nouns of your API.

### Resource Identification

**Good Resource URIs:**
```
GET    /users                  # Collection
GET    /users/{id}             # Individual resource
GET    /users/{id}/orders      # Nested collection
POST   /users                  # Create resource
PUT    /users/{id}             # Replace resource
PATCH  /users/{id}             # Update resource
DELETE /users/{id}             # Delete resource
```

**Bad Resource URIs:**
```
POST   /getUser                # Verb in URI
POST   /createUser             # Verb in URI
GET    /user?action=delete     # Action as query param
```

### Resource Naming Conventions

- Use plural nouns for collections: `/users`, `/orders`, `/products`
- Use lowercase and hyphens for readability: `/shipping-addresses`
- Avoid deep nesting (max 2-3 levels): `/users/{id}/orders/{orderId}`
- Use query parameters for filtering: `/users?status=active&role=admin`

## HTTP Method Semantics

### Safe and Idempotent Methods

| Method | Safe | Idempotent | Use Case |
|--------|------|------------|----------|
| GET | Yes | Yes | Retrieve resource(s) |
| POST | No | No | Create resource, non-idempotent operations |
| PUT | No | Yes | Replace entire resource |
| PATCH | No | No | Partial update |
| DELETE | No | Yes | Remove resource |
| HEAD | Yes | Yes | Get metadata only |
| OPTIONS | Yes | Yes | Get allowed methods |

### Method Usage

**GET - Retrieve Resources**
```http
GET /users/123
Accept: application/json

Response: 200 OK
{
  "id": 123,
  "name": "John Doe",
  "email": "john@example.com",
  "created_at": "2024-01-15T10:30:00Z"
}
```

**POST - Create Resources**
```http
POST /users
Content-Type: application/json

{
  "name": "Jane Smith",
  "email": "jane@example.com"
}

Response: 201 Created
Location: /users/124
{
  "id": 124,
  "name": "Jane Smith",
  "email": "jane@example.com",
  "created_at": "2024-01-16T14:20:00Z"
}
```

**PUT - Replace Resource**
```http
PUT /users/123
Content-Type: application/json

{
  "name": "John Doe Updated",
  "email": "john.new@example.com"
}

Response: 200 OK
{
  "id": 123,
  "name": "John Doe Updated",
  "email": "john.new@example.com",
  "updated_at": "2024-01-17T09:15:00Z"
}
```

**PATCH - Partial Update**
```http
PATCH /users/123
Content-Type: application/json

{
  "email": "john.updated@example.com"
}

Response: 200 OK
{
  "id": 123,
  "name": "John Doe",
  "email": "john.updated@example.com",
  "updated_at": "2024-01-17T10:00:00Z"
}
```

**DELETE - Remove Resource**
```http
DELETE /users/123

Response: 204 No Content
```

## HTTP Status Codes

### Success Codes (2xx)

- **200 OK** - Request succeeded (GET, PUT, PATCH)
- **201 Created** - Resource created (POST), include Location header
- **202 Accepted** - Request accepted for async processing
- **204 No Content** - Success with no response body (DELETE)

### Redirection (3xx)

- **301 Moved Permanently** - Resource permanently moved
- **302 Found** - Temporary redirect
- **304 Not Modified** - Cached version is still valid

### Client Errors (4xx)

- **400 Bad Request** - Invalid request syntax or validation error
- **401 Unauthorized** - Authentication required or failed
- **403 Forbidden** - Authenticated but not authorized
- **404 Not Found** - Resource doesn't exist
- **405 Method Not Allowed** - HTTP method not supported for resource
- **409 Conflict** - Request conflicts with current state (e.g., duplicate)
- **422 Unprocessable Entity** - Valid syntax but semantic errors
- **429 Too Many Requests** - Rate limit exceeded

### Server Errors (5xx)

- **500 Internal Server Error** - Unexpected server error
- **502 Bad Gateway** - Invalid response from upstream server
- **503 Service Unavailable** - Server temporarily unavailable
- **504 Gateway Timeout** - Upstream server timeout

