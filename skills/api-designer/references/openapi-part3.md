<!-- part 3/6 of openapi.md -->

## Components

Reusable components for your API spec.

### Schemas

```yaml
components:
  schemas:
    User:
      type: object
      required:
        - id
        - email
        - name
      properties:
        id:
          type: integer
          format: int64
          readOnly: true
          example: 123
        email:
          type: string
          format: email
          example: john@example.com
        name:
          type: string
          minLength: 1
          maxLength: 100
          example: John Doe
        status:
          type: string
          enum:
            - active
            - inactive
            - suspended
          default: active
        created_at:
          type: string
          format: date-time
          readOnly: true
          example: "2024-01-15T10:30:00Z"
        metadata:
          type: object
          additionalProperties:
            type: string

    CreateUserRequest:
      type: object
      required:
        - email
        - name
      properties:
        email:
          type: string
          format: email
        name:
          type: string
          minLength: 1
          maxLength: 100
        metadata:
          type: object
          additionalProperties:
            type: string

    UserListResponse:
      type: object
      properties:
        data:
          type: array
          items:
            $ref: '#/components/schemas/User'
        pagination:
          $ref: '#/components/schemas/Pagination'

    Pagination:
      type: object
      properties:
        offset:
          type: integer
          minimum: 0
        limit:
          type: integer
          minimum: 1
        total:
          type: integer
          minimum: 0
        has_more:
          type: boolean

    Error:
      type: object
      required:
        - error
      properties:
        error:
          type: object
          required:
            - code
            - message
          properties:
            code:
              type: string
              example: RESOURCE_NOT_FOUND
            message:
              type: string
              example: User with ID 123 not found
            details:
              type: object
            request_id:
              type: string
              example: req_abc123
```

