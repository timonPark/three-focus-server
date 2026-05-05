<!-- part 2a/3 of extensions.md -->

## pgvector (Vector Similarity Search)

```sql
CREATE EXTENSION IF NOT EXISTS vector;

-- Create table with vector column
CREATE TABLE embeddings (
  id SERIAL PRIMARY KEY,
  content TEXT,
  embedding vector(1536)  -- OpenAI embeddings are 1536 dimensions
);

-- Add vector index (HNSW for better performance)
CREATE INDEX ON embeddings USING hnsw (embedding vector_cosine_ops);
-- or IVFFlat for memory efficiency:
-- CREATE INDEX ON embeddings USING ivfflat (embedding vector_cosine_ops);

-- Insert vectors
INSERT INTO embeddings (content, embedding)
VALUES ('Hello world', '[0.1, 0.2, 0.3, ...]');

-- Similarity search (cosine distance)
SELECT
  content,
  1 - (embedding <=> '[0.1, 0.2, ...]') as similarity
FROM embeddings
ORDER BY embedding <=> '[0.1, 0.2, ...]'
LIMIT 10;

-- Distance operators
-- <-> L2 distance (Euclidean)
-- <#> negative inner product
-- <=> cosine distance (most common for embeddings)

-- Set index parameters for better recall
SET hnsw.ef_search = 100;  -- Higher = better recall, slower query

-- Bulk insert optimization
SET maintenance_work_mem = '2GB';
```

## pgcrypto (Encryption and Hashing)

```sql
CREATE EXTENSION IF NOT EXISTS pgcrypto;

-- Hash passwords (use bcrypt)
INSERT INTO users (email, password_hash)
VALUES ('user@example.com', crypt('password123', gen_salt('bf', 10)));

-- Verify password
SELECT * FROM users
WHERE email = 'user@example.com'
  AND password_hash = crypt('password123', password_hash);

-- Generate random values
SELECT gen_random_uuid();
SELECT gen_random_bytes(32);

-- Encrypt/decrypt data
SELECT
  pgp_sym_encrypt('sensitive data', 'encryption-key'),
  pgp_sym_decrypt(encrypted_column, 'encryption-key')
FROM table_name;

-- Digest functions
SELECT digest('data', 'sha256');
SELECT encode(digest('data', 'sha256'), 'hex');
```

## postgres_fdw (Foreign Data Wrapper)

```sql
CREATE EXTENSION IF NOT EXISTS postgres_fdw;

-- Create foreign server
CREATE SERVER remote_db
FOREIGN DATA WRAPPER postgres_fdw
OPTIONS (host 'remote-host', port '5432', dbname 'remote_db');

-- User mapping
CREATE USER MAPPING FOR current_user
SERVER remote_db
OPTIONS (user 'remote_user', password 'remote_password');

-- Import foreign schema
IMPORT FOREIGN SCHEMA public
FROM SERVER remote_db
INTO remote_schema;

-- Or create specific foreign table
CREATE FOREIGN TABLE remote_users (
  id INTEGER,
  email TEXT,
  created_at TIMESTAMPTZ
)
SERVER remote_db
OPTIONS (schema_name 'public', table_name 'users');

-- Query remote table (transparent)
SELECT * FROM remote_users WHERE created_at > NOW() - INTERVAL '1 day';

-- Join local and remote tables
SELECT
  l.id,
  l.name,
  r.email
FROM local_table l
JOIN remote_users r ON l.user_id = r.id;
```

## pg_repack (Online Table Reorganization)

