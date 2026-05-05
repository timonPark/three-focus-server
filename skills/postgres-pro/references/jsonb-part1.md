<!-- part 1/2 of jsonb.md -->

# JSONB Operations

## JSONB vs JSON

```sql
-- Use JSONB (binary, indexed, faster)
CREATE TABLE documents (
  id SERIAL PRIMARY KEY,
  data JSONB NOT NULL
);

-- NOT json (text storage, no indexing)
-- Only use json if you need to preserve exact formatting/whitespace
```

## JSONB Operators

### Retrieval Operators

```sql
-- -> returns JSONB
SELECT data -> 'user' FROM documents;                    -- {"id": 123, "name": "Alice"}
SELECT data -> 'user' -> 'name' FROM documents;          -- "Alice" (still JSONB)

-- ->> returns text
SELECT data ->> 'status' FROM documents;                 -- active (text)
SELECT data -> 'user' ->> 'name' FROM documents;         -- Alice (text)

-- #> for nested paths (JSONB)
SELECT data #> '{user,address,city}' FROM documents;    -- "NYC" (JSONB)

-- #>> for nested paths (text)
SELECT data #>> '{user,address,city}' FROM documents;   -- NYC (text)

-- Array access
SELECT data -> 'tags' -> 0 FROM documents;               -- First tag
SELECT jsonb_array_elements(data -> 'tags') FROM documents;  -- Expand array
```

### Containment Operators

```sql
-- @> contains (most useful for indexing)
SELECT * FROM documents WHERE data @> '{"status": "active"}';
SELECT * FROM documents WHERE data @> '{"tags": ["postgresql"]}';
SELECT * FROM documents WHERE data -> 'user' @> '{"role": "admin"}';

-- <@ is contained by
SELECT * FROM documents WHERE '{"status": "active"}' <@ data;

-- ? key exists
SELECT * FROM documents WHERE data ? 'email';
SELECT * FROM documents WHERE data -> 'user' ? 'email';

-- ?| any key exists
SELECT * FROM documents WHERE data ?| ARRAY['email', 'phone'];

-- ?& all keys exist
SELECT * FROM documents WHERE data ?& ARRAY['email', 'phone'];
```

### Modification Operators

```sql
-- || concatenate/merge (shallow)
UPDATE documents SET data = data || '{"updated_at": "2024-01-01"}'::jsonb;

-- - remove key
UPDATE documents SET data = data - 'temp_field';

-- #- remove nested path
UPDATE documents SET data = data #- '{user,temp_field}';

-- jsonb_set for deep updates
UPDATE documents
SET data = jsonb_set(data, '{user,email}', '"new@example.com"'::jsonb)
WHERE id = 123;

-- jsonb_insert
UPDATE documents
SET data = jsonb_insert(data, '{tags,0}', '"new-tag"'::jsonb)
WHERE id = 123;
```

## JSONB Indexing

### GIN Index (Default for containment)

```sql
-- Standard GIN index (for @>, ?, ?&, ?| operators)
CREATE INDEX idx_documents_data ON documents USING GIN(data);

-- Queries that benefit:
SELECT * FROM documents WHERE data @> '{"status": "active"}';
SELECT * FROM documents WHERE data ? 'email';
SELECT * FROM documents WHERE data ?& ARRAY['email', 'phone'];
```

### GIN Index on Specific Path

```sql
-- Index specific path for better performance
CREATE INDEX idx_documents_status ON documents USING GIN((data -> 'status'));
CREATE INDEX idx_documents_user ON documents USING GIN((data -> 'user'));

-- Smaller index, faster queries on specific paths
SELECT * FROM documents WHERE data -> 'status' @> '"active"';
```

### GIN Index with jsonb_path_ops

```sql
-- Smaller, faster index for @> queries only
CREATE INDEX idx_documents_path_ops ON documents USING GIN(data jsonb_path_ops);

-- Good for: WHERE data @> '{"key": "value"}'
-- Bad for: WHERE data ? 'key' (not supported)
-- ~20% smaller than default GIN, faster for containment
```

### B-tree Index on Extracted Values

```sql
-- Index extracted value (most selective)
CREATE INDEX idx_documents_status_btree ON documents((data ->> 'status'));
CREATE INDEX idx_documents_user_id ON documents((CAST(data -> 'user' ->> 'id' AS INTEGER)));

-- Enables efficient equality and range queries
SELECT * FROM documents WHERE data ->> 'status' = 'active';
SELECT * FROM documents WHERE CAST(data -> 'user' ->> 'id' AS INTEGER) > 1000;
```

### Expression Index for Nested Values

```sql
-- Index deep nested value
CREATE INDEX idx_documents_user_email ON documents((data #>> '{user,email}'));

-- Enables:
SELECT * FROM documents WHERE data #>> '{user,email}' = 'user@example.com';
```

