<!-- part 2/3 of pagination.md -->

### 4. Keyset Pagination

Similar to cursor but uses actual field values instead of opaque cursor.

**Request:**
```http
GET /users?after_id=20&limit=10
GET /users?after_created_at=2024-01-15T10:30:00Z&limit=10
```

**Response:**
```json
{
  "data": [
    {"id": 21, "name": "User 21", "created_at": "2024-01-15T11:00:00Z"},
    {"id": 22, "name": "User 22", "created_at": "2024-01-15T11:30:00Z"}
  ],
  "pagination": {
    "after_id": 30,
    "limit": 10,
    "has_more": true
  },
  "links": {
    "next": "/users?after_id=30&limit=10"
  }
}
```

**Implementation:**
```sql
SELECT * FROM users
WHERE id > 20
ORDER BY id ASC
LIMIT 10;
```

**Advantages:**
- Very efficient (uses index)
- Transparent cursor (human readable)
- Consistent results
- Simple implementation

**Disadvantages:**
- Requires indexed column
- No random access
- Sorting limited to cursor field
- Complex for multi-field sorting

**Use when:**
- Simple ordering (by ID, timestamp)
- Need efficient pagination
- Want transparent cursor
- Have proper indexes

### 5. Seek Pagination (Time-Based)

Specialized keyset pagination for time-series data.

**Request:**
```http
GET /events?since=2024-01-15T10:00:00Z&until=2024-01-15T11:00:00Z&limit=100
```

**Response:**
```json
{
  "data": [...],
  "pagination": {
    "since": "2024-01-15T10:00:00Z",
    "until": "2024-01-15T11:00:00Z",
    "limit": 100,
    "has_more": true
  },
  "links": {
    "next": "/events?since=2024-01-15T11:00:00Z&until=2024-01-15T12:00:00Z&limit=100"
  }
}
```

**Use for:**
- Time-series data
- Logs and events
- Activity streams
- Analytics data

## Default Limits

Always set reasonable defaults and maximum limits:

```json
{
  "default_limit": 20,
  "max_limit": 100,
  "min_limit": 1
}
```

**Validation:**
```http
GET /users?limit=1000

Response: 400 Bad Request
{
  "error": {
    "code": "INVALID_LIMIT",
    "message": "Limit must be between 1 and 100. Default is 20."
  }
}
```

## Response Format

### Standard Pagination Object

```json
{
  "data": [...],
  "pagination": {
    "limit": 10,
    "offset": 20,
    "total": 150,
    "has_more": true,
    "has_previous": true
  }
}
```

### Link Header (RFC 5988)

```http
Link: </users?offset=0&limit=10>; rel="first",
      </users?offset=10&limit=10>; rel="prev",
      </users?offset=30&limit=10>; rel="next",
      </users?offset=140&limit=10>; rel="last"
```

**Used by:** GitHub API

### Embedded Links

```json
{
  "data": [...],
  "_links": {
    "self": { "href": "/users?offset=20&limit=10" },
    "first": { "href": "/users?offset=0&limit=10" },
    "prev": { "href": "/users?offset=10&limit=10" },
    "next": { "href": "/users?offset=30&limit=10" },
    "last": { "href": "/users?offset=140&limit=10" }
  }
}
```

## Sorting with Pagination

Always support sorting when paginating:

```http
GET /users?sort=created_at&order=desc&limit=10
GET /users?sort=-created_at&limit=10                    # Descending
GET /users?sort=last_name,first_name&limit=10           # Multi-field
```

**For cursor pagination, cursor must include sort fields:**
```json
{
  "cursor": {
    "id": 123,
    "created_at": "2024-01-15T10:30:00Z",
    "sort_fields": ["created_at", "id"]
  }
}
```

## Filtering with Pagination
