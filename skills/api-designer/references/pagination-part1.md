<!-- part 1/3 of pagination.md -->

# Pagination Patterns

## Why Paginate?

Large collections can't be returned all at once due to:
- Performance (slow queries, large payloads)
- Memory constraints (server and client)
- Network timeouts
- Poor user experience

Always paginate collection endpoints.

## Pagination Strategies

### 1. Offset-Based Pagination

Most common and intuitive. Uses `offset` (skip) and `limit` (page size).

**Request:**
```http
GET /users?offset=20&limit=10
```

**Response:**
```json
{
  "data": [
    {"id": 21, "name": "User 21"},
    {"id": 22, "name": "User 22"}
  ],
  "pagination": {
    "offset": 20,
    "limit": 10,
    "total": 150,
    "has_more": true
  },
  "links": {
    "first": "/users?offset=0&limit=10",
    "prev": "/users?offset=10&limit=10",
    "next": "/users?offset=30&limit=10",
    "last": "/users?offset=140&limit=10"
  }
}
```

**Advantages:**
- Simple to implement
- Easy to understand
- Random access (jump to any page)
- Shows total count

**Disadvantages:**
- Performance degrades with large offsets (database scans many rows)
- Inconsistent results if data changes during pagination
- Inefficient for real-time data
- Database must count total rows (expensive)

**Use when:**
- Small to medium datasets
- Data doesn't change frequently
- Need random page access
- Need total count

### 2. Page-Based Pagination

Simplified offset pagination using page numbers.

**Request:**
```http
GET /users?page=3&per_page=10
```

**Response:**
```json
{
  "data": [...],
  "pagination": {
    "page": 3,
    "per_page": 10,
    "total_pages": 15,
    "total_count": 150
  },
  "links": {
    "first": "/users?page=1&per_page=10",
    "prev": "/users?page=2&per_page=10",
    "next": "/users?page=4&per_page=10",
    "last": "/users?page=15&per_page=10"
  }
}
```

**Calculation:**
- `offset = (page - 1) * per_page`
- `total_pages = ceil(total_count / per_page)`

**Same pros/cons as offset-based, but:**
- More intuitive for users (page 1, page 2)
- Common in web applications

### 3. Cursor-Based Pagination

Uses an opaque cursor (pointer) to the next set of results.

**Request:**
```http
GET /users?limit=10
GET /users?cursor=eyJpZCI6MTIzfQ&limit=10
```

**Response:**
```json
{
  "data": [
    {"id": 21, "name": "User 21"},
    {"id": 22, "name": "User 22"}
  ],
  "pagination": {
    "next_cursor": "eyJpZCI6MzB9",
    "prev_cursor": "eyJpZCI6MjB9",
    "has_more": true
  },
  "links": {
    "next": "/users?cursor=eyJpZCI6MzB9&limit=10",
    "prev": "/users?cursor=eyJpZCI6MjB9&limit=10"
  }
}
```

**Cursor structure (base64 encoded):**
```json
{"id": 30, "sort": "created_at"}
```

**Implementation:**
```sql
-- First page
SELECT * FROM users ORDER BY created_at DESC LIMIT 10;

-- Next page (cursor points to last item)
SELECT * FROM users
WHERE created_at < '2024-01-15T10:30:00Z'
ORDER BY created_at DESC
LIMIT 10;
```

**Advantages:**
- Consistent results (no skipped/duplicate items)
- Efficient for large datasets
- Works well with real-time data
- No expensive COUNT query
- Better database performance

**Disadvantages:**
- No random access (can't jump to page 10)
- No total count
- More complex to implement
- Cursor is opaque (users can't modify it)

**Use when:**
- Large datasets
- Data changes frequently
- Infinite scroll UI
- Real-time feeds
- Performance is critical

