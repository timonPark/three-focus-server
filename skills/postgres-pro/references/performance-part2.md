<!-- part 2/2 of performance.md -->

### Problem: Sequential scan on large table

```sql
-- Bad: Full table scan
SELECT * FROM orders WHERE user_id = 123;
-- Solution: Add index
CREATE INDEX idx_orders_user ON orders(user_id);
```

### Problem: Index not used

```sql
-- Bad: Function prevents index usage
SELECT * FROM users WHERE LOWER(email) = 'user@example.com';
-- Solution: Expression index
CREATE INDEX idx_users_email_lower ON users(LOWER(email));

-- Bad: Implicit type conversion
SELECT * FROM users WHERE id = '123';  -- id is integer
-- Solution: Use correct type
SELECT * FROM users WHERE id = 123;
```

### Problem: Large JOIN inefficiency

```sql
-- Bad: Nested loop on large tables
EXPLAIN ANALYZE
SELECT * FROM orders o JOIN users u ON o.user_id = u.id;

-- Solutions:
-- 1. Ensure indexes exist on join columns
CREATE INDEX idx_orders_user ON orders(user_id);
-- 2. Update statistics
ANALYZE orders, users;
-- 3. Increase work_mem if hash join would be better
SET work_mem = '256MB';
```

### Problem: COUNT(*) slow

```sql
-- Bad: Full table scan
SELECT COUNT(*) FROM orders WHERE status = 'pending';

-- Solutions:
-- 1. Partial index
CREATE INDEX idx_orders_pending ON orders(id) WHERE status = 'pending';

-- 2. Approximate count for large tables
SELECT reltuples::bigint FROM pg_class WHERE relname = 'orders';

-- 3. Materialized count for reports
CREATE MATERIALIZED VIEW order_counts AS
SELECT status, COUNT(*) FROM orders GROUP BY status;
CREATE UNIQUE INDEX ON order_counts(status);
REFRESH MATERIALIZED VIEW CONCURRENTLY order_counts;
```

## Connection Pooling

```sql
-- Check active connections
SELECT count(*) FROM pg_stat_activity WHERE state = 'active';

-- Connection limit reached? Use pgBouncer
-- pgbouncer.ini:
-- [databases]
-- mydb = host=localhost port=5432 dbname=mydb
-- [pgbouncer]
-- pool_mode = transaction
-- max_client_conn = 1000
-- default_pool_size = 25
```

## Configuration Tuning

```sql
-- Memory settings (for 16GB RAM server)
shared_buffers = 4GB           -- 25% of RAM
effective_cache_size = 12GB    -- 75% of RAM
work_mem = 64MB                -- Per operation
maintenance_work_mem = 1GB     -- For VACUUM, CREATE INDEX

-- Checkpoint tuning
checkpoint_completion_target = 0.9
wal_buffers = 16MB
checkpoint_timeout = 10min

-- Query planner
random_page_cost = 1.1         -- Lower for SSD (default 4.0)
effective_io_concurrency = 200 -- Higher for SSD

-- Parallelism (Postgres 10+)
max_parallel_workers_per_gather = 4
max_parallel_workers = 8
```

## Performance Monitoring

```sql
-- Slow queries (requires pg_stat_statements)
SELECT
  query,
  calls,
  mean_exec_time,
  max_exec_time,
  stddev_exec_time
FROM pg_stat_statements
ORDER BY mean_exec_time DESC
LIMIT 20;

-- Cache hit ratio (should be > 99%)
SELECT
  sum(blks_hit) * 100.0 / sum(blks_hit + blks_read) as cache_hit_ratio
FROM pg_stat_database;

-- Index usage
SELECT
  schemaname,
  tablename,
  indexname,
  idx_scan,
  idx_tup_read,
  idx_tup_fetch
FROM pg_stat_user_indexes
WHERE idx_scan = 0
  AND indexrelname NOT LIKE '%pkey';  -- Unused indexes
```
