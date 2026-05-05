<!-- part 1/3 of maintenance.md -->

# Database Maintenance

## VACUUM Fundamentals

### Why VACUUM is Critical

PostgreSQL uses MVCC (Multi-Version Concurrency Control):
- Updates/deletes don't remove old rows immediately
- Old rows marked as "dead tuples"
- VACUUM reclaims space from dead tuples
- Without VACUUM: table bloat, degraded performance, transaction ID wraparound

### VACUUM Variants

```sql
-- Standard VACUUM (non-blocking, reclaims space for reuse)
VACUUM users;
VACUUM;  -- All tables

-- VACUUM FULL (locks table, rewrites entire table, reclaims disk space)
VACUUM FULL users;
-- Use pg_repack instead for production (non-blocking alternative)

-- VACUUM VERBOSE (shows details)
VACUUM VERBOSE users;

-- VACUUM ANALYZE (vacuum + update statistics)
VACUUM ANALYZE users;
```

### VACUUM Monitoring

```sql
-- Check when tables were last vacuumed
SELECT
  schemaname,
  relname,
  last_vacuum,
  last_autovacuum,
  n_dead_tup,
  n_live_tup,
  round(100.0 * n_dead_tup / NULLIF(n_live_tup + n_dead_tup, 0), 2) as dead_pct
FROM pg_stat_user_tables
ORDER BY n_dead_tup DESC;

-- Check vacuum progress (PG 9.6+)
SELECT
  pid,
  datname,
  relid::regclass,
  phase,
  heap_blks_total,
  heap_blks_scanned,
  heap_blks_vacuumed,
  round(100.0 * heap_blks_scanned / NULLIF(heap_blks_total, 0), 2) as pct_complete
FROM pg_stat_progress_vacuum;
```

## Autovacuum Configuration

```sql
-- Global settings (postgresql.conf)
autovacuum = on
autovacuum_max_workers = 3
autovacuum_naptime = 60s  -- Check interval

-- Vacuum thresholds
autovacuum_vacuum_threshold = 50
autovacuum_vacuum_scale_factor = 0.2
-- Triggers when: dead_tuples > threshold + (scale_factor * total_tuples)
-- Default: 50 + (0.2 * 1000000) = 200,050 dead tuples for 1M row table

-- Analyze thresholds
autovacuum_analyze_threshold = 50
autovacuum_analyze_scale_factor = 0.1

-- Performance settings
autovacuum_vacuum_cost_delay = 2ms  -- Lower = faster, more I/O impact
autovacuum_vacuum_cost_limit = 200
```

### Per-Table Autovacuum Tuning

```sql
-- High-churn table: vacuum more aggressively
ALTER TABLE orders SET (
  autovacuum_vacuum_scale_factor = 0.05,  -- 5% instead of 20%
  autovacuum_vacuum_threshold = 1000,
  autovacuum_analyze_scale_factor = 0.02
);

-- Large, stable table: vacuum less often
ALTER TABLE archive_logs SET (
  autovacuum_vacuum_scale_factor = 0.5,
  autovacuum_vacuum_threshold = 5000
);

-- Very high-churn table: disable cost delays
ALTER TABLE sessions SET (
  autovacuum_vacuum_cost_delay = 0
);

-- View table settings
SELECT
  relname,
  reloptions
FROM pg_class
WHERE relname = 'orders';
```

## ANALYZE (Statistics)

```sql
-- Update statistics for query planner
ANALYZE users;
ANALYZE;  -- All tables

-- Check statistics freshness
SELECT
  schemaname,
  relname,
  last_analyze,
  last_autoanalyze,
  n_mod_since_analyze
FROM pg_stat_user_tables
ORDER BY n_mod_since_analyze DESC;

-- Increase statistics target for high-cardinality columns
ALTER TABLE users ALTER COLUMN email SET STATISTICS 1000;
-- Default is 100, range is 0-10000
-- Higher = better estimates, slower ANALYZE

-- View column statistics
SELECT
  tablename,
  attname,
  n_distinct,      -- Estimated unique values
  correlation,     -- Physical vs logical ordering (-1 to 1)
  null_frac        -- Percentage of nulls
FROM pg_stats
WHERE tablename = 'users';
```

