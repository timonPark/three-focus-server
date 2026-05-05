<!-- part 3/3 of maintenance.md -->

## Lock Monitoring

```sql
-- Current locks
SELECT
  l.pid,
  a.usename,
  a.query,
  l.mode,
  l.locktype,
  l.granted,
  l.relation::regclass
FROM pg_locks l
JOIN pg_stat_activity a ON l.pid = a.pid
WHERE NOT l.granted
ORDER BY l.pid;

-- Blocking queries
SELECT
  blocked_locks.pid AS blocked_pid,
  blocked_activity.usename AS blocked_user,
  blocking_locks.pid AS blocking_pid,
  blocking_activity.usename AS blocking_user,
  blocked_activity.query AS blocked_statement,
  blocking_activity.query AS blocking_statement
FROM pg_catalog.pg_locks blocked_locks
JOIN pg_catalog.pg_stat_activity blocked_activity ON blocked_activity.pid = blocked_locks.pid
JOIN pg_catalog.pg_locks blocking_locks
  ON blocking_locks.locktype = blocked_locks.locktype
  AND blocking_locks.database IS NOT DISTINCT FROM blocked_locks.database
  AND blocking_locks.relation IS NOT DISTINCT FROM blocked_locks.relation
  AND blocking_locks.page IS NOT DISTINCT FROM blocked_locks.page
  AND blocking_locks.tuple IS NOT DISTINCT FROM blocked_locks.tuple
  AND blocking_locks.virtualxid IS NOT DISTINCT FROM blocked_locks.virtualxid
  AND blocking_locks.transactionid IS NOT DISTINCT FROM blocked_locks.transactionid
  AND blocking_locks.classid IS NOT DISTINCT FROM blocked_locks.classid
  AND blocking_locks.objid IS NOT DISTINCT FROM blocked_locks.objid
  AND blocking_locks.objsubid IS NOT DISTINCT FROM blocked_locks.objsubid
  AND blocking_locks.pid != blocked_locks.pid
JOIN pg_catalog.pg_stat_activity blocking_activity ON blocking_activity.pid = blocking_locks.pid
WHERE NOT blocked_locks.granted;
```

## Transaction ID Wraparound

```sql
-- Check distance to wraparound (should be < 1 billion)
SELECT
  datname,
  age(datfrozenxid) as xid_age,
  2147483647 - age(datfrozenxid) as xids_remaining
FROM pg_database
ORDER BY age(datfrozenxid) DESC;

-- Per-table wraparound status
SELECT
  schemaname,
  relname,
  age(relfrozenxid) as xid_age,
  pg_size_pretty(pg_total_relation_size(schemaname||'.'||relname)) as size
FROM pg_stat_user_tables
ORDER BY age(relfrozenxid) DESC
LIMIT 20;

-- Prevent wraparound: VACUUM FREEZE
VACUUM FREEZE;  -- All databases
VACUUM FREEZE users;  -- Specific table
```

## Maintenance Checklist

**Daily:**
- Monitor autovacuum activity
- Check for long-running queries
- Verify replication lag (if applicable)
- Check cache hit ratio

**Weekly:**
- Review slow queries from pg_stat_statements
- Check for table/index bloat
- Review unused indexes
- Monitor disk space usage

**Monthly:**
- Review autovacuum settings
- Reindex heavily updated indexes
- Update statistics on large tables
- Review database growth trends

**Quarterly:**
- Test backup restoration
- Review and optimize slow queries
- Capacity planning
- PostgreSQL version updates

## Helpful Maintenance Queries

```sql
-- Database size
SELECT
  pg_database.datname,
  pg_size_pretty(pg_database_size(pg_database.datname)) as size
FROM pg_database
ORDER BY pg_database_size(pg_database.datname) DESC;

-- Largest tables
SELECT
  schemaname,
  tablename,
  pg_size_pretty(pg_total_relation_size(schemaname||'.'||tablename)) as total_size,
  pg_size_pretty(pg_relation_size(schemaname||'.'||tablename)) as table_size,
  pg_size_pretty(pg_total_relation_size(schemaname||'.'||tablename) -
                 pg_relation_size(schemaname||'.'||tablename)) as index_size
FROM pg_tables
ORDER BY pg_total_relation_size(schemaname||'.'||tablename) DESC
LIMIT 20;

-- Connection count by state
SELECT
  state,
  count(*) as count
FROM pg_stat_activity
GROUP BY state
ORDER BY count DESC;

-- Reset statistics (after performance testing)
SELECT pg_stat_reset();
SELECT pg_stat_statements_reset();
```
