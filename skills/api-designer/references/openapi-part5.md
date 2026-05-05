<!-- part 5/6 of openapi.md -->

## Data Types

### Primitive Types

```yaml
# String
type: string
example: "Hello World"

# String with format
type: string
format: email
example: "user@example.com"

# Integer
type: integer
format: int64
example: 123

# Number (float)
type: number
format: double
example: 99.99

# Boolean
type: boolean
example: true

# Date-time
type: string
format: date-time
example: "2024-01-15T10:30:00Z"

# Date
type: string
format: date
example: "2024-01-15"

# UUID
type: string
format: uuid
example: "550e8400-e29b-41d4-a716-446655440000"

# URI
type: string
format: uri
example: "https://example.com/users/123"
```

### Arrays

```yaml
type: array
items:
  type: string
minItems: 1
maxItems: 10
uniqueItems: true
example: ["tag1", "tag2", "tag3"]

# Array of objects
type: array
items:
  $ref: '#/components/schemas/User'
```

### Objects

```yaml
type: object
required:
  - name
  - email
properties:
  name:
    type: string
  email:
    type: string
    format: email
  age:
    type: integer
    minimum: 0
    maximum: 120

# Additional properties
additionalProperties: false  # Strict - no extra properties
additionalProperties: true   # Allow any extra properties
additionalProperties:        # Extra properties must be strings
  type: string
```

### Enums

```yaml
type: string
enum:
  - active
  - inactive
  - suspended
default: active
```

### OneOf / AnyOf / AllOf

```yaml
# OneOf - exactly one schema matches
oneOf:
  - $ref: '#/components/schemas/CreditCard'
  - $ref: '#/components/schemas/BankAccount'

# AnyOf - one or more schemas match
anyOf:
  - $ref: '#/components/schemas/User'
  - $ref: '#/components/schemas/Organization'

# AllOf - all schemas must match (inheritance)
allOf:
  - $ref: '#/components/schemas/BaseUser'
  - type: object
    properties:
      admin_level:
        type: integer
```

