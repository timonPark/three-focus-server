<!-- part 3/3 of monitoring-analysis.md -->

## Cross-Platform Monitoring

### Resource Utilization

```sql
-- PostgreSQL: Database size growth
SELECT
    current_database() as database,
    pg_size_pretty(pg_database_size(current_database())) as size,
    (SELECT pg_size_pretty(sum(pg_total_relation_size(schemaname||'.'||tablename)))
     FROM pg_tables
     WHERE schemaname = 'public') as public_schema_size;

-- MySQL: Database size
SELECT
    table_schema as database,
    ROUND(SUM(data_length + index_length) / 1024 / 1024, 2) as size_mb
FROM information_schema.tables
WHERE table_schema NOT IN ('information_schema', 'performance_schema', 'mysql', 'sys')
GROUP BY table_schema
ORDER BY size_mb DESC;
```

### Health Check Queries

```sql
-- PostgreSQL: Overall health
SELECT
    'connections' as metric,
    count(*) as current,
    current_setting('max_connections')::int as max
FROM pg_stat_activity
UNION ALL
SELECT
    'cache_hit_ratio',
    round((sum(heap_blks_hit) / NULLIF(sum(heap_blks_hit) + sum(heap_blks_read), 0) * 100)::numeric, 2),
    95
FROM pg_statio_user_tables
UNION ALL
SELECT
    'database_size_gb',
    round((pg_database_size(current_database()) / 1024.0 / 1024.0 / 1024.0)::numeric, 2),
    NULL;

-- MySQL: Overall health
SELECT 'connections' as metric,
       (SELECT COUNT(*) FROM information_schema.processlist) as current,
       @@max_connections as max
UNION ALL
SELECT 'buffer_pool_hit_ratio',
       ROUND((1 - (
           (SELECT VARIABLE_VALUE FROM performance_schema.global_status WHERE VARIABLE_NAME = 'Innodb_buffer_pool_reads') /
           (SELECT VARIABLE_VALUE FROM performance_schema.global_status WHERE VARIABLE_NAME = 'Innodb_buffer_pool_read_requests')
       )) * 100, 2),
       95
UNION ALL
SELECT 'slow_queries',
       (SELECT VARIABLE_VALUE FROM performance_schema.global_status WHERE VARIABLE_NAME = 'Slow_queries'),
       NULL;
```

## Alert Thresholds

### PostgreSQL Alerts

```sql
-- Connection pool nearing capacity
SELECT
    count(*) as current_connections,
    current_setting('max_connections')::int as max_connections,
    CASE
        WHEN count(*) > current_setting('max_connections')::int * 0.9 THEN 'CRITICAL'
        WHEN count(*) > current_setting('max_connections')::int * 0.8 THEN 'WARNING'
        ELSE 'OK'
    END as status
FROM pg_stat_activity;

-- Cache hit ratio degradation
WITH cache_stats AS (
    SELECT
        round((sum(heap_blks_hit) / NULLIF(sum(heap_blks_hit) + sum(heap_blks_read), 0) * 100)::numeric, 2) as hit_ratio
    FROM pg_statio_user_tables
)
SELECT
    hit_ratio,
    CASE
        WHEN hit_ratio < 90 THEN 'CRITICAL'
        WHEN hit_ratio < 95 THEN 'WARNING'
        ELSE 'OK'
    END as status
FROM cache_stats;

-- Replication lag (on standby)
SELECT
    CASE
        WHEN pg_last_wal_receive_lsn() = pg_last_wal_replay_lsn() THEN 0
        ELSE EXTRACT(EPOCH FROM (now() - pg_last_xact_replay_timestamp()))
    END as lag_seconds,
    CASE
        WHEN EXTRACT(EPOCH FROM (now() - pg_last_xact_replay_timestamp())) > 60 THEN 'CRITICAL'
        WHEN EXTRACT(EPOCH FROM (now() - pg_last_xact_replay_timestamp())) > 10 THEN 'WARNING'
        ELSE 'OK'
    END as status;
```

### MySQL Alerts

```sql
-- InnoDB buffer pool efficiency
SELECT
    ROUND((1 - (
        (SELECT VARIABLE_VALUE FROM performance_schema.global_status WHERE VARIABLE_NAME = 'Innodb_buffer_pool_reads') /
        (SELECT VARIABLE_VALUE FROM performance_schema.global_status WHERE VARIABLE_NAME = 'Innodb_buffer_pool_read_requests')
    )) * 100, 2) as buffer_pool_hit_ratio,
    CASE
        WHEN (1 - (
            (SELECT VARIABLE_VALUE FROM performance_schema.global_status WHERE VARIABLE_NAME = 'Innodb_buffer_pool_reads') /
            (SELECT VARIABLE_VALUE FROM performance_schema.global_status WHERE VARIABLE_NAME = 'Innodb_buffer_pool_read_requests')
        )) * 100 < 90 THEN 'CRITICAL'
        WHEN (1 - (
            (SELECT VARIABLE_VALUE FROM performance_schema.global_status WHERE VARIABLE_NAME = 'Innodb_buffer_pool_reads') /
            (SELECT VARIABLE_VALUE FROM performance_schema.global_status WHERE VARIABLE_NAME = 'Innodb_buffer_pool_read_requests')
        )) * 100 < 95 THEN 'WARNING'
        ELSE 'OK'
    END as status;

-- Replication lag (on replica)
SELECT
    Seconds_Behind_Master as lag_seconds,
    CASE
        WHEN Slave_IO_Running = 'No' OR Slave_SQL_Running = 'No' THEN 'CRITICAL - Replication stopped'
        WHEN Seconds_Behind_Master > 300 THEN 'CRITICAL'
        WHEN Seconds_Behind_Master > 60 THEN 'WARNING'
        ELSE 'OK'
    END as status
FROM (SHOW SLAVE STATUS) s;
```

## Monitoring Best Practices

1. **Establish baselines** - Record normal performance metrics
2. **Track trends** - Monitor daily/weekly patterns
3. **Set thresholds** - Define warning and critical levels
4. **Automate alerts** - Use monitoring tools (Prometheus, Grafana, Datadog)
5. **Regular reviews** - Weekly performance analysis meetings
6. **Document changes** - Track configuration and schema modifications
7. **Capacity planning** - Monitor growth and forecast needs
8. **Test queries** - Validate optimizations in staging first
