<!-- part 3/3 of pagination.md -->


Combine filtering with pagination:

```http
GET /users?status=active&role=admin&offset=0&limit=10
```

**Important:** Apply filters before pagination:
1. Filter records
2. Count filtered results
3. Apply pagination
4. Return paginated subset

## Total Count

### Include Total Count

```json
{
  "data": [...],
  "pagination": {
    "total": 1523,
    "limit": 10,
    "offset": 20
  }
}
```

**Pros:**
- Clients know total results
- Can calculate total pages
- Better UX (show "Page 3 of 153")

**Cons:**
- COUNT query is expensive
- Slows down response
- Inaccurate for large/changing datasets

### Omit Total Count

```json
{
  "data": [...],
  "pagination": {
    "has_more": true,
    "limit": 10
  }
}
```

**Use when:**
- Large datasets (COUNT is too slow)
- Real-time data (count changes constantly)
- Cursor pagination
- Infinite scroll UI

### Optional Total Count

Let client request total count:

```http
GET /users?limit=10&include_total=true
```

## Edge Cases

### Empty Results

```json
{
  "data": [],
  "pagination": {
    "offset": 0,
    "limit": 10,
    "total": 0,
    "has_more": false
  }
}
```

### Last Page

```json
{
  "data": [{"id": 150, "name": "Last User"}],
  "pagination": {
    "offset": 140,
    "limit": 10,
    "total": 150,
    "has_more": false
  },
  "links": {
    "first": "/users?offset=0&limit=10",
    "prev": "/users?offset=130&limit=10",
    "next": null
  }
}
```

### Out of Range

```http
GET /users?offset=10000&limit=10

Response: 200 OK (empty results)
{
  "data": [],
  "pagination": {
    "offset": 10000,
    "limit": 10,
    "total": 150,
    "has_more": false
  }
}
```

Or return 404 for pages that don't exist:
```http
GET /users?page=1000&per_page=10

Response: 404 Not Found
{
  "error": {
    "code": "PAGE_NOT_FOUND",
    "message": "Page 1000 does not exist. Total pages: 15"
  }
}
```

## Best Practices

1. **Always paginate collections** - Never return unbounded lists
2. **Set reasonable defaults** - Default limit of 20-50 items
3. **Enforce maximum limits** - Prevent excessive loads (max 100-1000)
4. **Include has_more flag** - Tell clients if more results exist
5. **Provide navigation links** - Make it easy to get next/prev pages
6. **Document pagination** - Explain cursor format, limits, defaults
7. **Be consistent** - Use same pagination pattern across all endpoints
8. **Consider performance** - Choose strategy based on data size/type
9. **Support sorting** - Let clients control result order
10. **Handle edge cases** - Empty results, last page, invalid cursors

## Comparison Matrix

| Feature | Offset | Page | Cursor | Keyset |
|---------|--------|------|--------|--------|
| Performance | Poor for large offsets | Poor | Excellent | Excellent |
| Random access | Yes | Yes | No | No |
| Total count | Yes | Yes | No | Optional |
| Consistency | Poor | Poor | Excellent | Excellent |
| Complexity | Simple | Simple | Medium | Medium |
| Real-time data | Poor | Poor | Excellent | Excellent |
| Database load | High | High | Low | Low |
| Use case | Small datasets | Web UIs | Feeds/streams | Large datasets |
