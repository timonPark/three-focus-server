<!-- part 3/4 of error-handling.md -->

## Error Code Catalog

Define standard error codes for your API:

```json
{
  "VALIDATION_ERROR": {
    "status": 400,
    "description": "Request validation failed",
    "subcodes": {
      "REQUIRED": "Required field is missing",
      "INVALID_FORMAT": "Field has invalid format",
      "OUT_OF_RANGE": "Value is out of allowed range",
      "INVALID_ENUM": "Value is not in allowed set"
    }
  },
  "AUTHENTICATION_ERROR": {
    "status": 401,
    "description": "Authentication failed",
    "subcodes": {
      "MISSING_TOKEN": "No authentication token provided",
      "INVALID_TOKEN": "Token is invalid",
      "EXPIRED_TOKEN": "Token has expired"
    }
  },
  "AUTHORIZATION_ERROR": {
    "status": 403,
    "description": "Insufficient permissions",
    "subcodes": {
      "INSUFFICIENT_PERMISSIONS": "Missing required permission",
      "RESOURCE_FORBIDDEN": "Access to resource is forbidden"
    }
  },
  "RESOURCE_NOT_FOUND": {
    "status": 404,
    "description": "Resource not found"
  },
  "CONFLICT_ERROR": {
    "status": 409,
    "description": "Request conflicts with current state",
    "subcodes": {
      "RESOURCE_ALREADY_EXISTS": "Resource already exists",
      "CONCURRENT_MODIFICATION": "Resource was modified by another request"
    }
  },
  "RATE_LIMIT_EXCEEDED": {
    "status": 429,
    "description": "Rate limit exceeded"
  },
  "INTERNAL_SERVER_ERROR": {
    "status": 500,
    "description": "Internal server error"
  }
}
```

## Validation Error Details

### Field-Level Validation

```json
{
  "error": {
    "code": "VALIDATION_ERROR",
    "message": "Request validation failed",
    "details": [
      {
        "field": "credit_card.number",
        "code": "INVALID_FORMAT",
        "message": "Credit card number must be 16 digits",
        "value_provided": "1234",
        "constraints": {
          "pattern": "^[0-9]{16}$"
        }
      },
      {
        "field": "items[0].quantity",
        "code": "OUT_OF_RANGE",
        "message": "Quantity must be at least 1",
        "value_provided": 0,
        "constraints": {
          "min": 1,
          "max": 1000
        }
      }
    ]
  }
}
```

### Cross-Field Validation

```json
{
  "error": {
    "code": "VALIDATION_ERROR",
    "message": "Request validation failed",
    "details": [
      {
        "fields": ["start_date", "end_date"],
        "code": "INVALID_RANGE",
        "message": "End date must be after start date",
        "values_provided": {
          "start_date": "2024-01-20",
          "end_date": "2024-01-15"
        }
      }
    ]
  }
}
```

