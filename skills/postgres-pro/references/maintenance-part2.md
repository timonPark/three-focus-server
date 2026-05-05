<!-- part 2/3 of maintenance.md -->

## Bloat Detection and Removal

### Detect Table Bloat

```sql
-- Approximate bloat calculation
SELECT
  schemaname,
  tablename,
  pg_size_pretty(pg_total_relation_size(schemaname||'.'||tablename)) as total_size,
  pg_size_pretty(pg_relation_size(schemaname||'.'||tablename)) as table_size,
  round(100 * pg_relation_size(schemaname||'.'||tablename)::numeric /
        NULLIF(pg_total_relation_size(schemaname||'.'||tablename), 0), 2) as table_pct,
  n_dead_tup,
  round(100.0 * n_dead_tup / NULLIF(n_live_tup + n_dead_tup, 0), 2) as dead_pct
FROM pg_stat_user_tables
WHERE pg_total_relation_size(schemaname||'.'||tablename) > 10485760  -- > 10MB
ORDER BY pg_total_relation_size(schemaname||'.'||tablename) DESC;
```

### Detect Index Bloat

```sql
-- Unused indexes
SELECT
  schemaname,
  tablename,
  indexname,
  idx_scan,
  pg_size_pretty(pg_relation_size(indexrelid)) as index_size
FROM pg_stat_user_indexes
WHERE idx_scan = 0
  AND indexrelname NOT LIKE '%pkey'
ORDER BY pg_relation_size(indexrelid) DESC;

-- Index size vs table size
SELECT
  schemaname,
  tablename,
  pg_size_pretty(pg_relation_size(schemaname||'.'||tablename)) as table_size,
  pg_size_pretty(pg_total_relation_size(schemaname||'.'||tablename) -
                 pg_relation_size(schemaname||'.'||tablename)) as indexes_size,
  round(100.0 * (pg_total_relation_size(schemaname||'.'||tablename) -
                 pg_relation_size(schemaname||'.'||tablename))::numeric /
        NULLIF(pg_relation_size(schemaname||'.'||tablename), 0), 2) as index_ratio_pct
FROM pg_stat_user_tables
WHERE pg_total_relation_size(schemaname||'.'||tablename) > 10485760
ORDER BY pg_total_relation_size(schemaname||'.'||tablename) DESC;
```

### Remove Bloat

```sql
-- Option 1: VACUUM FULL (locks table)
VACUUM FULL users;

-- Option 2: pg_repack (online, no locks)
-- Command line: pg_repack -d mydb -t users

-- Option 3: REINDEX (for index bloat)
REINDEX TABLE users;
REINDEX INDEX CONCURRENTLY idx_users_email;  -- Non-blocking (PG 12+)

-- Option 4: CLUSTER (rewrite table in index order, locks table)
CLUSTER users USING users_pkey;
```

## pg_stat Monitoring Views

### pg_stat_activity (Current Queries)

```sql
-- Active queries
SELECT
  pid,
  usename,
  application_name,
  client_addr,
  state,
  query_start,
  state_change,
  query
FROM pg_stat_activity
WHERE state = 'active'
  AND query NOT LIKE '%pg_stat_activity%'
ORDER BY query_start;

-- Long-running queries
SELECT
  pid,
  now() - query_start as duration,
  state,
  query
FROM pg_stat_activity
WHERE state = 'active'
  AND (now() - query_start) > interval '5 minutes'
ORDER BY duration DESC;

-- Kill long-running query
SELECT pg_cancel_backend(pid);  -- Graceful
SELECT pg_terminate_backend(pid);  -- Forceful

-- Idle transactions (bad, hold locks)
SELECT
  pid,
  usename,
  state,
  now() - state_change as idle_duration,
  query
FROM pg_stat_activity
WHERE state = 'idle in transaction'
  AND (now() - state_change) > interval '1 minute';
```

### pg_stat_database (Database-wide Stats)

```sql
SELECT
  datname,
  numbackends,  -- Active connections
  xact_commit,
  xact_rollback,
  round(100.0 * xact_rollback / NULLIF(xact_commit + xact_rollback, 0), 2) as rollback_pct,
  blks_read,
  blks_hit,
  round(100.0 * blks_hit / NULLIF(blks_hit + blks_read, 0), 2) as cache_hit_ratio,
  tup_returned,
  tup_fetched,
  tup_inserted,
  tup_updated,
  tup_deleted
FROM pg_stat_database
WHERE datname = current_database();
```

### pg_stat_user_tables (Table Stats)

```sql
SELECT
  schemaname,
  relname,
  seq_scan,        -- Sequential scans (high = may need index)
  seq_tup_read,
  idx_scan,        -- Index scans
  idx_tup_fetch,
  n_tup_ins,
  n_tup_upd,
  n_tup_del,
  n_tup_hot_upd,   -- HOT updates (good, in-page updates)
  n_live_tup,
  n_dead_tup,
  last_vacuum,
  last_autovacuum,
  last_analyze,
  last_autoanalyze
FROM pg_stat_user_tables
ORDER BY seq_scan DESC;  -- Tables with most sequential scans
```

### pg_stat_user_indexes (Index Usage)

```sql
-- Index usage efficiency
SELECT
  schemaname,
  tablename,
  indexname,
  idx_scan,
  idx_tup_read,
  idx_tup_fetch,
  pg_size_pretty(pg_relation_size(indexrelid)) as index_size
FROM pg_stat_user_indexes
ORDER BY idx_scan;  -- Low idx_scan = potentially unused index

-- Index hit ratio
SELECT
  schemaname,
  tablename,
  indexname,
  idx_scan,
  idx_tup_read,
  idx_tup_fetch,
  CASE WHEN idx_tup_read > 0
    THEN round(100.0 * idx_tup_fetch / idx_tup_read, 2)
    ELSE 0
  END as hit_ratio
FROM pg_stat_user_indexes
WHERE idx_scan > 0
ORDER BY hit_ratio;
```

### pg_statio_user_tables (I/O Stats)

```sql
SELECT
  schemaname,
  relname,
  heap_blks_read,   -- Disk reads
  heap_blks_hit,    -- Cache hits
  round(100.0 * heap_blks_hit / NULLIF(heap_blks_hit + heap_blks_read, 0), 2) as cache_hit_ratio,
  idx_blks_read,
  idx_blks_hit,
  toast_blks_read,
  toast_blks_hit
FROM pg_statio_user_tables
WHERE heap_blks_read + heap_blks_hit > 0
ORDER BY heap_blks_read DESC;
```

