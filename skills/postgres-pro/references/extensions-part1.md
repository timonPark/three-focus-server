<!-- part 1/3 of extensions.md -->

# PostgreSQL Extensions

## Extension Management

```sql
-- List available extensions
SELECT * FROM pg_available_extensions ORDER BY name;

-- List installed extensions
SELECT * FROM pg_extension;

-- Install extension
CREATE EXTENSION IF NOT EXISTS pg_stat_statements;
CREATE EXTENSION IF NOT EXISTS "uuid-ossp";
CREATE EXTENSION IF NOT EXISTS pg_trgm;

-- Drop extension
DROP EXTENSION pg_stat_statements;

-- Update extension
ALTER EXTENSION pg_stat_statements UPDATE TO '1.10';
```

## pg_stat_statements (Query Performance)

```sql
-- Install and configure
CREATE EXTENSION pg_stat_statements;

-- postgresql.conf:
-- shared_preload_libraries = 'pg_stat_statements'
-- pg_stat_statements.max = 10000
-- pg_stat_statements.track = all

-- Top 10 slowest queries by mean time
SELECT
  query,
  calls,
  total_exec_time,
  mean_exec_time,
  max_exec_time,
  stddev_exec_time,
  rows
FROM pg_stat_statements
ORDER BY mean_exec_time DESC
LIMIT 10;

-- Most frequently called queries
SELECT
  query,
  calls,
  total_exec_time,
  mean_exec_time
FROM pg_stat_statements
ORDER BY calls DESC
LIMIT 10;

-- Most time-consuming queries (total time)
SELECT
  query,
  calls,
  total_exec_time / 1000 as total_seconds,
  mean_exec_time,
  (total_exec_time / sum(total_exec_time) OVER ()) * 100 as percentage
FROM pg_stat_statements
ORDER BY total_exec_time DESC
LIMIT 10;

-- Reset statistics
SELECT pg_stat_statements_reset();
```

## uuid-ossp (UUID Generation)

```sql
CREATE EXTENSION IF NOT EXISTS "uuid-ossp";

-- Generate UUIDs
SELECT uuid_generate_v1();    -- Time-based + MAC address
SELECT uuid_generate_v4();    -- Random (most common)

-- Use in tables
CREATE TABLE users (
  id UUID PRIMARY KEY DEFAULT uuid_generate_v4(),
  email TEXT NOT NULL,
  created_at TIMESTAMPTZ DEFAULT NOW()
);

-- Insert
INSERT INTO users (email) VALUES ('user@example.com')
RETURNING id;
```

## pg_trgm (Fuzzy String Matching)

```sql
CREATE EXTENSION IF NOT EXISTS pg_trgm;

-- Similarity search
SELECT
  email,
  similarity(email, 'john@example.com') as sim
FROM users
WHERE similarity(email, 'john@example.com') > 0.3
ORDER BY sim DESC;

-- LIKE optimization with trigram index
CREATE INDEX idx_users_email_trgm ON users USING GIN(email gin_trgm_ops);

-- Now these queries use index:
SELECT * FROM users WHERE email ILIKE '%john%';
SELECT * FROM users WHERE email % 'jon@example.com';  -- Similar to

-- Trigram operators
SELECT 'hello' % 'helo';              -- True (similar)
SELECT similarity('hello', 'helo');   -- 0.5
SELECT word_similarity('hello', 'hello world');  -- 1.0

-- Set similarity threshold
SET pg_trgm.similarity_threshold = 0.5;
SELECT * FROM users WHERE email % 'searchtext';
```

## PostGIS (Spatial and Geographic)

```sql
CREATE EXTENSION IF NOT EXISTS postgis;

-- Create spatial table
CREATE TABLE locations (
  id SERIAL PRIMARY KEY,
  name TEXT NOT NULL,
  geom GEOMETRY(Point, 4326)  -- WGS84 lat/lng
);

-- Add spatial index
CREATE INDEX idx_locations_geom ON locations USING GIST(geom);

-- Insert point (longitude, latitude)
INSERT INTO locations (name, geom)
VALUES ('NYC', ST_SetSRID(ST_MakePoint(-74.0060, 40.7128), 4326));

-- Distance queries (in meters)
SELECT
  name,
  ST_Distance(
    geom::geography,
    ST_SetSRID(ST_MakePoint(-73.9857, 40.7484), 4326)::geography
  ) as distance_meters
FROM locations
ORDER BY distance_meters
LIMIT 10;

-- Within radius (1km = 1000m)
SELECT * FROM locations
WHERE ST_DWithin(
  geom::geography,
  ST_SetSRID(ST_MakePoint(-74.0060, 40.7128), 4326)::geography,
  1000
);

-- Bounding box query (very fast with GIST index)
SELECT * FROM locations
WHERE geom && ST_MakeEnvelope(-74.1, 40.6, -73.9, 40.8, 4326);

-- Contains query
SELECT * FROM zones
WHERE ST_Contains(geom, ST_SetSRID(ST_MakePoint(-74.0060, 40.7128), 4326));

-- Area calculation
SELECT
  name,
  ST_Area(geom::geography) / 1000000 as area_km2
FROM zones;

-- GeoJSON export
SELECT
  name,
  ST_AsGeoJSON(geom) as geojson
FROM locations;
```

