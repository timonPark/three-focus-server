<!-- part 2/2 of versioning.md -->


Mark old version as deprecated but keep it running:

```http
GET /v1/users/123

Response:
Deprecation: true
Sunset: Wed, 15 Jan 2025 00:00:00 GMT
Link: </v2/users/123>; rel="successor-version"

{
  "id": 123,
  "name": "John Doe"
}
```

**Deprecation Headers:**
- `Deprecation: true` - Indicates version is deprecated
- `Sunset: <date>` - When version will be removed (RFC 8594)
- `Link: <url>; rel="successor-version"` - Points to new version

### 3. Sunset Phase

Old version is shut down on announced date.

Return 410 Gone for deprecated endpoints:
```http
GET /v1/users/123

Response: 410 Gone
{
  "error": {
    "code": "VERSION_SUNSET",
    "message": "API v1 was sunset on 2025-01-15. Please use v2.",
    "documentation_url": "https://api.example.com/docs/migration-v1-to-v2"
  }
}
```

## Deprecation Policy

### Recommended Timeline

1. **Announce deprecation** - At least 6 months before sunset
2. **Support period** - Run both versions for 6-12 months
3. **Sunset date** - Clear date communicated in advance
4. **Grace period** - 30 days of 410 Gone responses before complete shutdown

### Communication Channels

- API response headers
- Email to registered developers
- Blog posts and changelog
- Dashboard notifications
- Documentation updates
- Status page announcements

## Migration Strategy

### Provide Migration Guide

```markdown
# Migrating from v1 to v2

## Breaking Changes

### User Resource Changes

**v1:**
```json
{
  "id": 123,
  "name": "John Doe",
  "email": "john@example.com"
}
```

**v2:**
```json
{
  "id": 123,
  "first_name": "John",
  "last_name": "Doe",
  "email": "john@example.com"
}
```

**Migration:**
- Split `name` field into `first_name` and `last_name`
- Update client code to use new fields
```

### Offer Tools

- Migration scripts
- SDK updates
- API diff viewer
- Compatibility layer (temporary)

## Version Discovery

### Root Endpoint

```http
GET /

Response:
{
  "versions": {
    "v1": {
      "status": "deprecated",
      "sunset_date": "2025-01-15",
      "documentation_url": "https://api.example.com/docs/v1"
    },
    "v2": {
      "status": "current",
      "documentation_url": "https://api.example.com/docs/v2"
    },
    "v3": {
      "status": "beta",
      "documentation_url": "https://api.example.com/docs/v3"
    }
  }
}
```

### Version Info Endpoint

```http
GET /v2/version

Response:
{
  "version": "v2",
  "released": "2024-01-15",
  "status": "stable",
  "sunset_date": null
}
```

## OpenAPI Versioning

### Separate Specs per Version

```
openapi-v1.yaml
openapi-v2.yaml
openapi-v3.yaml
```

Each spec is complete and independent.

### Single Spec with Servers

```yaml
openapi: 3.1.0
info:
  title: My API
  version: 2.0.0
servers:
  - url: https://api.example.com/v1
    description: Version 1 (deprecated)
  - url: https://api.example.com/v2
    description: Version 2 (current)
```

## Best Practices

1. **Version from day one** - Start with /v1, not /api
2. **Major versions only** - Use v1, v2, v3 (not v1.1, v1.2)
3. **Long deprecation periods** - Give clients time to migrate (6-12 months)
4. **Clear communication** - Use headers, docs, emails
5. **Maintain old versions** - Support at least 2 versions simultaneously
6. **Document changes** - Provide detailed migration guides
7. **Use semantic versioning** - For internal/SDK versioning
8. **Never break without warning** - Always announce breaking changes
9. **Provide tools** - Migration scripts, updated SDKs
10. **Monitor usage** - Track which versions are being used

## Anti-Patterns

Avoid these mistakes:

- **Breaking changes without version bump** - Breaks existing clients
- **Too many versions** - Maintenance nightmare (max 2-3 active versions)
- **Short deprecation periods** - Frustrates developers
- **No migration path** - Makes upgrades painful
- **Surprise sunsets** - Breaks production apps without warning
- **Inconsistent versioning** - Different strategies for different endpoints
- **Versioning individual endpoints** - Use consistent version across API
