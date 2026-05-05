<!-- part 1/3 of mysql-tuning.md -->

# MySQL Tuning

## InnoDB Memory Configuration

### Buffer Pool

```sql
-- Recommended: 70-80% of system RAM for dedicated MySQL server
-- For 16GB RAM server:
SET GLOBAL innodb_buffer_pool_size = 12884901888;  -- 12GB

-- Check buffer pool usage
SHOW GLOBAL STATUS LIKE 'Innodb_buffer_pool_%';

-- Buffer pool hit ratio (target: >99%)
SELECT
    (1 - (Innodb_buffer_pool_reads / Innodb_buffer_pool_read_requests)) * 100 as hit_ratio
FROM (
    SELECT
        VARIABLE_VALUE as Innodb_buffer_pool_reads
    FROM performance_schema.global_status
    WHERE VARIABLE_NAME = 'Innodb_buffer_pool_reads'
) reads,
(
    SELECT
        VARIABLE_VALUE as Innodb_buffer_pool_read_requests
    FROM performance_schema.global_status
    WHERE VARIABLE_NAME = 'Innodb_buffer_pool_read_requests'
) requests;

-- Buffer pool instances (for multi-core systems)
-- Recommended: 1 instance per 1GB, max 64
SET GLOBAL innodb_buffer_pool_instances = 8;
```

### Sort and Join Buffers

```sql
-- Sort buffer per connection
SET GLOBAL sort_buffer_size = 2097152;  -- 2MB

-- Join buffer for full joins
SET GLOBAL join_buffer_size = 2097152;  -- 2MB

-- Temporary table size
SET GLOBAL tmp_table_size = 67108864;  -- 64MB
SET GLOBAL max_heap_table_size = 67108864;  -- 64MB

-- Monitor temp table usage
SHOW GLOBAL STATUS LIKE 'Created_tmp%';
```

## Query Cache (Deprecated in 8.0)

```sql
-- MySQL 5.7 and earlier
-- Note: Removed in MySQL 8.0
SET GLOBAL query_cache_type = 1;
SET GLOBAL query_cache_size = 67108864;  -- 64MB

-- Check query cache effectiveness
SHOW STATUS LIKE 'Qcache%';

-- Query cache hit ratio
SELECT
    Qcache_hits / (Qcache_hits + Com_select) * 100 as cache_hit_ratio
FROM (
    SELECT VARIABLE_VALUE as Qcache_hits
    FROM performance_schema.global_status
    WHERE VARIABLE_NAME = 'Qcache_hits'
) hits,
(
    SELECT VARIABLE_VALUE as Com_select
    FROM performance_schema.global_status
    WHERE VARIABLE_NAME = 'Com_select'
) selects;
```

## InnoDB Performance Settings

### Log Files and Flushing

```sql
-- InnoDB log file size (larger = better write performance)
-- Recommended: 1-2GB for write-heavy workloads
SET GLOBAL innodb_log_file_size = 1073741824;  -- 1GB

-- Log buffer size
SET GLOBAL innodb_log_buffer_size = 16777216;  -- 16MB

-- Flush method (O_DIRECT for dedicated server, avoids double buffering)
-- Set in my.cnf
innodb_flush_method = O_DIRECT

-- Flush log at transaction commit
-- 1 = full ACID (default, safest)
-- 2 = write to OS cache, flush every second
-- 0 = write and flush every second (fastest, risk data loss)
SET GLOBAL innodb_flush_log_at_trx_commit = 1;

-- For replication slaves or analytics (trade safety for speed)
SET GLOBAL innodb_flush_log_at_trx_commit = 2;
```

### I/O Configuration

```sql
-- Read I/O threads
SET GLOBAL innodb_read_io_threads = 8;

-- Write I/O threads
SET GLOBAL innodb_write_io_threads = 8;

-- I/O capacity (IOPS your storage can handle)
-- For SSD: 5000-20000
SET GLOBAL innodb_io_capacity = 10000;
SET GLOBAL innodb_io_capacity_max = 20000;

-- Flush method for optimal I/O
-- my.cnf:
innodb_flush_method = O_DIRECT
innodb_flush_neighbors = 0  -- Disable for SSD
```

### Thread Configuration

```sql
-- Max connections
SET GLOBAL max_connections = 200;

-- Thread cache (reuse threads)
SET GLOBAL thread_cache_size = 100;

-- Check thread cache effectiveness
SHOW STATUS LIKE 'Threads_%';
SHOW STATUS LIKE 'Connections';

-- Thread cache hit ratio (target: >90%)
SELECT
    (1 - (Threads_created / Connections)) * 100 as thread_cache_hit_ratio
FROM (
    SELECT VARIABLE_VALUE as Threads_created
    FROM performance_schema.global_status
    WHERE VARIABLE_NAME = 'Threads_created'
) created,
(
    SELECT VARIABLE_VALUE as Connections
    FROM performance_schema.global_status
    WHERE VARIABLE_NAME = 'Connections'
) conns;
```

