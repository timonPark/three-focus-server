<!-- part 1/2 of versioning.md -->

# API Versioning Strategies

## Why Version APIs?

API versioning allows you to evolve your API while maintaining backward compatibility for existing clients. Breaking changes require a new version.

### Breaking Changes

Changes that require a new version:
- Removing or renaming fields
- Changing field types (string to integer)
- Adding required fields to requests
- Changing response structure
- Removing endpoints
- Changing HTTP status codes for same scenario
- Changing authentication mechanisms

### Non-Breaking Changes

Safe changes that don't require a new version:
- Adding new endpoints
- Adding optional request fields
- Adding new fields to responses (clients should ignore unknown fields)
- Fixing bugs
- Performance improvements
- Adding new HTTP methods to existing resources

## Versioning Strategies

### 1. URI Versioning

Most common and visible approach. Version is part of the URL path.

```http
GET /v1/users/123
GET /v2/users/123
```

**Advantages:**
- Clear and visible in URLs
- Easy to understand and implement
- Simple routing and caching
- Can run multiple versions simultaneously

**Disadvantages:**
- Violates REST principle (same resource, different URIs)
- Requires updating client code to change version
- Can lead to URI proliferation

**Implementation:**
```
/v1/users
/v1/products
/v2/users      # New version with breaking changes
/v2/products
```

### 2. Header Versioning

Version specified in HTTP headers (Accept header or custom header).

**Accept Header:**
```http
GET /users/123
Accept: application/vnd.myapi.v1+json

GET /users/123
Accept: application/vnd.myapi.v2+json
```

**Custom Header:**
```http
GET /users/123
API-Version: 1

GET /users/123
API-Version: 2
```

**Advantages:**
- URIs remain stable
- More RESTful (same resource, same URI)
- Separates versioning from resource identification

**Disadvantages:**
- Less visible (harder to debug)
- More complex routing
- Difficult to test in browser
- Cache complexity

### 3. Query Parameter Versioning

Version specified as query parameter.

```http
GET /users/123?version=1
GET /users/123?version=2

# or
GET /users/123?api-version=1
GET /users/123?api-version=2
```

**Advantages:**
- Simple to implement
- Easy to test
- Visible in URLs

**Disadvantages:**
- Pollutes query string
- Not semantic (version not a filter)
- Can interfere with other query params

### 4. Content Negotiation

Client specifies desired version through content negotiation.

```http
GET /users/123
Accept: application/vnd.myapi+json; version=1

GET /users/123
Accept: application/vnd.myapi+json; version=2
```

**Advantages:**
- Very RESTful
- Flexible content type negotiation
- Stable URIs

**Disadvantages:**
- Complex implementation
- Less intuitive for developers
- Harder to test

## Recommended Approach

**URI versioning is recommended for most APIs** because:
- It's the most explicit and discoverable
- Easy to understand and debug
- Simple to implement and maintain
- Clear separation between versions

```
/v1/users
/v2/users
/v3/users
```

## Version Format

### Major Versions Only

Use simple major versions (v1, v2, v3) for public APIs:
```
/v1/users
/v2/users
```

**Advantages:**
- Simple and clear
- Easy to communicate
- Forces thoughtful breaking changes

### Date-Based Versions

Some APIs use dates for versions:
```
/2024-01-01/users
/2024-06-15/users
```

**Used by:** Stripe, GitHub API

**Advantages:**
- Clear when version was released
- Easy to understand timeline
- No confusion about major/minor

**Disadvantages:**
- Less intuitive for clients
- Harder to understand what changed

## Version Lifecycle

### 1. Introduction Phase

New version is released alongside existing version:
```
/v1/users  # Still supported
/v2/users  # New version available
```

Announce new version:
- Blog post explaining changes
- Migration guide
- Breaking changes list
- Timeline for v1 deprecation

### 2. Deprecation Phase
