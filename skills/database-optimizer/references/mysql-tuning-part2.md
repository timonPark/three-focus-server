<!-- part 2/3 of mysql-tuning.md -->

## Query Optimization

### Slow Query Log

```sql
-- Enable slow query logging
SET GLOBAL slow_query_log = 'ON';
SET GLOBAL long_query_time = 1.0;  -- Log queries > 1 second
SET GLOBAL log_queries_not_using_indexes = 'ON';

-- Slow query log file location
SET GLOBAL slow_query_log_file = '/var/log/mysql/slow-query.log';

-- Analyze slow query log with pt-query-digest
-- $ pt-query-digest /var/log/mysql/slow-query.log

-- Check slow query status
SHOW GLOBAL STATUS LIKE 'Slow_queries';
```

### Performance Schema

```sql
-- Enable performance schema (my.cnf)
performance_schema = ON

-- Top queries by total execution time
SELECT
    DIGEST_TEXT,
    COUNT_STAR as exec_count,
    ROUND(AVG_TIMER_WAIT / 1000000000000, 3) as avg_time_sec,
    ROUND(SUM_TIMER_WAIT / 1000000000000, 3) as total_time_sec,
    ROUND((SUM_TIMER_WAIT / SUM(SUM_TIMER_WAIT) OVER ()) * 100, 2) as pct
FROM performance_schema.events_statements_summary_by_digest
ORDER BY SUM_TIMER_WAIT DESC
LIMIT 10;

-- Full table scans
SELECT * FROM sys.statements_with_full_table_scans
ORDER BY exec_count DESC
LIMIT 10;

-- Tables with high I/O
SELECT
    object_schema,
    object_name,
    count_read,
    count_write,
    count_fetch,
    SUM_TIMER_WAIT / 1000000000000 as total_latency_sec
FROM performance_schema.table_io_waits_summary_by_table
WHERE object_schema NOT IN ('mysql', 'performance_schema', 'sys')
ORDER BY SUM_TIMER_WAIT DESC
LIMIT 10;
```

## Index Optimization

### Index Statistics

```sql
-- Update index statistics
ANALYZE TABLE users;

-- Check index cardinality
SHOW INDEX FROM users;

-- Find duplicate/redundant indexes
SELECT
    a.table_schema,
    a.table_name,
    a.index_name as index1,
    a.column_name,
    b.index_name as index2
FROM information_schema.statistics a
JOIN information_schema.statistics b
    ON a.table_schema = b.table_schema
    AND a.table_name = b.table_name
    AND a.seq_in_index = b.seq_in_index
    AND a.column_name = b.column_name
    AND a.index_name != b.index_name
WHERE a.table_schema NOT IN ('mysql', 'information_schema', 'performance_schema', 'sys')
ORDER BY a.table_schema, a.table_name, a.index_name;

-- Find unused indexes
SELECT
    object_schema,
    object_name,
    index_name
FROM performance_schema.table_io_waits_summary_by_index_usage
WHERE index_name IS NOT NULL
  AND count_star = 0
  AND object_schema NOT IN ('mysql', 'performance_schema', 'sys')
ORDER BY object_schema, object_name;
```

### Covering Indexes

```sql
-- Create covering index
CREATE INDEX idx_users_email_name_created
ON users(email, name, created_at);

-- Query can use covering index
EXPLAIN
SELECT name, created_at FROM users WHERE email = 'user@example.com';
-- Look for "Using index" in Extra column

-- Force index usage for testing
SELECT name FROM users FORCE INDEX (idx_users_email_name_created)
WHERE email = 'user@example.com';
```

## Partitioning

### Range Partitioning

```sql
-- Create partitioned table
CREATE TABLE events (
    id BIGINT NOT NULL AUTO_INCREMENT,
    event_type VARCHAR(50),
    created_at DATETIME NOT NULL,
    data JSON,
    PRIMARY KEY (id, created_at)
) PARTITION BY RANGE (YEAR(created_at)) (
    PARTITION p2023 VALUES LESS THAN (2024),
    PARTITION p2024 VALUES LESS THAN (2025),
    PARTITION p2025 VALUES LESS THAN (2026),
    PARTITION pmax VALUES LESS THAN MAXVALUE
);

-- Query with partition pruning
EXPLAIN PARTITIONS
SELECT * FROM events
WHERE created_at >= '2024-01-01' AND created_at < '2024-02-01';
-- Should show "partitions: p2024"

-- Add new partition
ALTER TABLE events
ADD PARTITION (PARTITION p2026 VALUES LESS THAN (2027));

-- Drop old partition (fast delete)
ALTER TABLE events DROP PARTITION p2023;
```

### List Partitioning

```sql
-- Partition by discrete values
CREATE TABLE orders (
    id BIGINT NOT NULL AUTO_INCREMENT,
    user_id BIGINT,
    status VARCHAR(20),
    PRIMARY KEY (id, status)
) PARTITION BY LIST COLUMNS(status) (
    PARTITION p_pending VALUES IN ('pending', 'processing'),
    PARTITION p_completed VALUES IN ('completed', 'shipped'),
    PARTITION p_cancelled VALUES IN ('cancelled', 'refunded')
);
```

