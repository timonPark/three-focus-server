<!-- part 2/3 of postgresql-tuning.md -->

## VACUUM and Autovacuum

### Autovacuum Configuration

```sql
-- Enable autovacuum (should always be on)
ALTER SYSTEM SET autovacuum = on;

-- Autovacuum worker settings
ALTER SYSTEM SET autovacuum_max_workers = 4;
ALTER SYSTEM SET autovacuum_naptime = '30s';

-- Thresholds for triggering autovacuum
ALTER SYSTEM SET autovacuum_vacuum_scale_factor = 0.1;  -- 10% dead tuples
ALTER SYSTEM SET autovacuum_vacuum_threshold = 50;

-- Analyze thresholds
ALTER SYSTEM SET autovacuum_analyze_scale_factor = 0.05;  -- 5% changed
ALTER SYSTEM SET autovacuum_analyze_threshold = 50;

-- Per-table autovacuum settings for high-churn tables
ALTER TABLE busy_table SET (
    autovacuum_vacuum_scale_factor = 0.01,  -- More aggressive
    autovacuum_vacuum_cost_delay = 2,       -- Faster vacuum
    autovacuum_vacuum_cost_limit = 1000
);
```

### Manual Vacuum Operations

```sql
-- Full vacuum (locks table, reclaims space)
VACUUM FULL users;  -- Use sparingly, requires exclusive lock

-- Regular vacuum (non-locking)
VACUUM (ANALYZE, VERBOSE) users;

-- Check table bloat
SELECT
    schemaname, tablename,
    pg_size_pretty(pg_total_relation_size(schemaname||'.'||tablename)) as total_size,
    pg_size_pretty(pg_relation_size(schemaname||'.'||tablename)) as table_size,
    n_dead_tup,
    n_live_tup,
    round(n_dead_tup * 100.0 / NULLIF(n_live_tup + n_dead_tup, 0), 2) as dead_pct
FROM pg_stat_user_tables
WHERE n_live_tup > 0
ORDER BY n_dead_tup DESC;

-- Monitor autovacuum activity
SELECT
    schemaname, relname,
    last_vacuum, last_autovacuum,
    last_analyze, last_autoanalyze,
    vacuum_count, autovacuum_count,
    analyze_count, autoanalyze_count
FROM pg_stat_user_tables
ORDER BY last_autovacuum DESC NULLS LAST;
```

## Connection Pooling

### Configuration

```sql
-- Max connections (keep reasonable to manage memory)
ALTER SYSTEM SET max_connections = 200;

-- Reserved connections for superuser
ALTER SYSTEM SET superuser_reserved_connections = 3;

-- Connection lifecycle
ALTER SYSTEM SET idle_in_transaction_session_timeout = '5min';
ALTER SYSTEM SET statement_timeout = '30s';  -- Per-query timeout

-- Monitor connections
SELECT
    state,
    count(*),
    max(now() - state_change) as max_idle_time
FROM pg_stat_activity
WHERE state IS NOT NULL
GROUP BY state;

-- Find long-running queries
SELECT
    pid,
    now() - pg_stat_activity.query_start AS duration,
    query,
    state
FROM pg_stat_activity
WHERE (now() - pg_stat_activity.query_start) > interval '5 minutes'
  AND state != 'idle';
```

## Lock Management

### Lock Monitoring

```sql
-- Check current locks
SELECT
    locktype,
    relation::regclass,
    mode,
    granted,
    pid,
    pg_blocking_pids(pid) as blocked_by
FROM pg_locks
WHERE NOT granted
ORDER BY relation;

-- Find blocking queries
SELECT
    blocked_locks.pid AS blocked_pid,
    blocked_activity.usename AS blocked_user,
    blocking_locks.pid AS blocking_pid,
    blocking_activity.usename AS blocking_user,
    blocked_activity.query AS blocked_statement,
    blocking_activity.query AS blocking_statement
FROM pg_catalog.pg_locks blocked_locks
JOIN pg_catalog.pg_stat_activity blocked_activity ON blocked_activity.pid = blocked_locks.pid
JOIN pg_catalog.pg_locks blocking_locks ON blocking_locks.locktype = blocked_locks.locktype
    AND blocking_locks.relation = blocked_locks.relation
    AND blocking_locks.pid != blocked_locks.pid
JOIN pg_catalog.pg_stat_activity blocking_activity ON blocking_activity.pid = blocking_locks.pid
WHERE NOT blocked_locks.granted;

-- Deadlock configuration
ALTER SYSTEM SET deadlock_timeout = '1s';
ALTER SYSTEM SET log_lock_waits = on;
```

