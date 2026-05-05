<!-- part 6/6 of openapi.md -->

## Validation

### String Validation

```yaml
type: string
minLength: 1
maxLength: 100
pattern: "^[a-zA-Z0-9_-]+$"
format: email
```

### Number Validation

```yaml
type: integer
minimum: 0
maximum: 100
exclusiveMinimum: true  # > 0 instead of >= 0
multipleOf: 5
```

### Array Validation

```yaml
type: array
minItems: 1
maxItems: 10
uniqueItems: true
```

## Tags

Organize endpoints into logical groups:

```yaml
tags:
  - name: Users
    description: User management operations
  - name: Orders
    description: Order management
  - name: Products
    description: Product catalog

paths:
  /users:
    get:
      tags:
        - Users
```

## Documentation

### Markdown Support

```yaml
description: |
  # User Management

  This endpoint allows you to manage users.

  ## Features
  - Create users
  - Update profiles
  - Delete accounts

  ## Authentication
  Requires JWT bearer token.

  ## Example
  ```json
  {
    "name": "John Doe",
    "email": "john@example.com"
  }
  ```
```

## Code Generation

Generate SDKs from OpenAPI spec:

```bash
# Generate TypeScript client
openapi-generator-cli generate \
  -i openapi.yaml \
  -g typescript-axios \
  -o ./client

# Generate Python client
openapi-generator-cli generate \
  -i openapi.yaml \
  -g python \
  -o ./python-client

# Generate server stub
openapi-generator-cli generate \
  -i openapi.yaml \
  -g nodejs-express-server \
  -o ./server
```

## Validation Tools

Validate OpenAPI spec:

```bash
# Using Swagger CLI
swagger-cli validate openapi.yaml

# Using Spectral (advanced linting)
spectral lint openapi.yaml
```

## Best Practices

1. **Use components** - Reuse schemas, responses, parameters
2. **Add examples** - Include realistic examples for all schemas
3. **Document thoroughly** - Every endpoint, parameter, response
4. **Version your spec** - Track changes to the specification
5. **Validate regularly** - Use tools to catch errors
6. **Use $ref** - Reference components instead of duplicating
7. **Include error responses** - Document all possible errors
8. **Add operationId** - Unique ID for each operation (for code gen)
9. **Tag endpoints** - Organize into logical groups
10. **Provide security schemes** - Document authentication clearly
