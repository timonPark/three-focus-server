<!-- part 1/6 of openapi.md -->

# OpenAPI 3.1 Specification

## What is OpenAPI?

OpenAPI (formerly Swagger) is a standard for describing REST APIs. It enables:
- Interactive documentation
- Code generation (SDKs, clients, servers)
- API testing tools
- Contract validation
- Mock servers

## Basic Structure

### Minimal OpenAPI 3.1 Spec

```yaml
openapi: 3.1.0
info:
  title: My API
  version: 1.0.0
  description: A sample API
  contact:
    name: API Support
    email: support@example.com
    url: https://example.com/support
  license:
    name: Apache 2.0
    url: https://www.apache.org/licenses/LICENSE-2.0.html

servers:
  - url: https://api.example.com/v1
    description: Production server
  - url: https://staging-api.example.com/v1
    description: Staging server
  - url: http://localhost:3000/v1
    description: Local development

paths:
  /users:
    get:
      summary: List users
      description: Retrieve a paginated list of users
      operationId: listUsers
      tags:
        - Users
      responses:
        '200':
          description: Successful response
          content:
            application/json:
              schema:
                type: object
                properties:
                  data:
                    type: array
                    items:
                      $ref: '#/components/schemas/User'

components:
  schemas:
    User:
      type: object
      required:
        - id
        - email
      properties:
        id:
          type: integer
          format: int64
          example: 123
        email:
          type: string
          format: email
          example: john@example.com
        name:
          type: string
          example: John Doe
```

## Info Object

Metadata about the API:

```yaml
info:
  title: Users API
  version: 1.0.0
  description: |
    # Users API

    This API manages user accounts and profiles.

    ## Features
    - User CRUD operations
    - Authentication with JWT
    - Role-based authorization

  termsOfService: https://example.com/terms

  contact:
    name: API Support Team
    email: api-support@example.com
    url: https://example.com/support

  license:
    name: MIT
    url: https://opensource.org/licenses/MIT

  x-api-id: users-api-v1
  x-audience: external
```

## Servers

Define API base URLs:

```yaml
servers:
  - url: https://api.example.com/v1
    description: Production
    variables:
      version:
        default: v1
        enum:
          - v1
          - v2

  - url: https://{environment}.example.com/v1
    description: Dynamic environment
    variables:
      environment:
        default: api
        enum:
          - api
          - staging
          - dev
```

