<!-- part 2b/3 of extensions.md -->

```sql
CREATE EXTENSION IF NOT EXISTS pg_repack;

-- Repack table (removes bloat, rebuilds indexes)
-- Run via command line, not SQL:
-- pg_repack -d mydb -t users

-- Check bloat before repack
SELECT
  schemaname,
  tablename,
  pg_size_pretty(pg_total_relation_size(schemaname||'.'||tablename)) as size,
  pg_size_pretty(pg_total_relation_size(schemaname||'.'||tablename) -
                 pg_relation_size(schemaname||'.'||tablename)) as index_size
FROM pg_tables
WHERE schemaname = 'public'
ORDER BY pg_total_relation_size(schemaname||'.'||tablename) DESC;

-- Repack entire database
-- pg_repack -d mydb

-- Repack with custom order
-- pg_repack -d mydb -t users -o "created_at DESC"
```

## timescaledb (Time-Series Data)

```sql
CREATE EXTENSION IF NOT EXISTS timescaledb;

-- Create hypertable (must have time column)
CREATE TABLE metrics (
  time TIMESTAMPTZ NOT NULL,
  device_id INTEGER,
  temperature DOUBLE PRECISION,
  humidity DOUBLE PRECISION
);

-- Convert to hypertable
SELECT create_hypertable('metrics', 'time');

-- Set chunk interval (default 7 days)
SELECT set_chunk_time_interval('metrics', INTERVAL '1 day');

-- Add compression
ALTER TABLE metrics SET (
  timescaledb.compress,
  timescaledb.compress_segmentby = 'device_id',
  timescaledb.compress_orderby = 'time DESC'
);

-- Automatic compression policy (compress chunks older than 7 days)
SELECT add_compression_policy('metrics', INTERVAL '7 days');

-- Retention policy (drop chunks older than 30 days)
SELECT add_retention_policy('metrics', INTERVAL '30 days');

-- Continuous aggregates (materialized views for time-series)
CREATE MATERIALIZED VIEW metrics_hourly
WITH (timescaledb.continuous) AS
SELECT
  time_bucket('1 hour', time) AS bucket,
  device_id,
  AVG(temperature) as avg_temp,
  MAX(temperature) as max_temp,
  MIN(temperature) as min_temp
FROM metrics
GROUP BY bucket, device_id;

-- Refresh policy
SELECT add_continuous_aggregate_policy('metrics_hourly',
  start_offset => INTERVAL '3 hours',
  end_offset => INTERVAL '1 hour',
  schedule_interval => INTERVAL '1 hour'
);
```

## Extension Recommendations by Use Case

**Query Performance Monitoring:**
- `pg_stat_statements` (essential)
- `pg_stat_kcache` (cache hit statistics)

**Text Search:**
- `pg_trgm` (fuzzy matching, LIKE optimization)
- Built-in full-text search (no extension needed)

**Spatial Data:**
- `postgis` (comprehensive spatial features)

**Vector Embeddings / AI:**
- `pgvector` (for semantic search, RAG applications)

**Time-Series:**
- `timescaledb` (automatic partitioning, compression)

**Data Security:**
- `pgcrypto` (hashing, encryption)
- `pg_audit` (audit logging)

**UUID Support:**
- `uuid-ossp` (UUID generation)

**Cross-Database Queries:**
- `postgres_fdw` (query remote PostgreSQL)
- `file_fdw` (query CSV files)

**Table Maintenance:**
- `pg_repack` (online bloat removal)
