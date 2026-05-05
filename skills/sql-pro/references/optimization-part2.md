<!-- part 2/2 of optimization.md -->

## Partitioning Strategies

```sql
-- Range partitioning by date (PostgreSQL)
CREATE TABLE orders (
    order_id SERIAL,
    customer_id INT,
    order_date DATE NOT NULL,
    total DECIMAL(10,2)
) PARTITION BY RANGE (order_date);

CREATE TABLE orders_2024_q1 PARTITION OF orders
    FOR VALUES FROM ('2024-01-01') TO ('2024-04-01');

CREATE TABLE orders_2024_q2 PARTITION OF orders
    FOR VALUES FROM ('2024-04-01') TO ('2024-07-01');

-- Partition pruning in action
EXPLAIN SELECT * FROM orders WHERE order_date >= '2024-02-01' AND order_date < '2024-03-01';
-- Only scans orders_2024_q1 partition

-- List partitioning by category
CREATE TABLE products (
    product_id SERIAL,
    category VARCHAR(50) NOT NULL,
    name VARCHAR(200)
) PARTITION BY LIST (category);

CREATE TABLE products_electronics PARTITION OF products
    FOR VALUES IN ('electronics', 'computers', 'phones');

CREATE TABLE products_clothing PARTITION OF products
    FOR VALUES IN ('clothing', 'shoes', 'accessories');

-- Hash partitioning for even distribution
CREATE TABLE users (
    user_id SERIAL,
    email VARCHAR(255)
) PARTITION BY HASH (user_id);

CREATE TABLE users_p0 PARTITION OF users
    FOR VALUES WITH (MODULUS 4, REMAINDER 0);
CREATE TABLE users_p1 PARTITION OF users
    FOR VALUES WITH (MODULUS 4, REMAINDER 1);
```

## Materialized Views

```sql
-- Create materialized view for expensive aggregations
CREATE MATERIALIZED VIEW daily_sales_summary AS
SELECT
    DATE_TRUNC('day', order_date) as day,
    COUNT(*) as order_count,
    SUM(total) as revenue,
    AVG(total) as avg_order_value,
    COUNT(DISTINCT customer_id) as unique_customers
FROM orders
GROUP BY DATE_TRUNC('day', order_date);

CREATE UNIQUE INDEX idx_daily_sales_day ON daily_sales_summary (day);

-- Refresh strategy
REFRESH MATERIALIZED VIEW CONCURRENTLY daily_sales_summary;

-- Auto-refresh with trigger (PostgreSQL)
CREATE OR REPLACE FUNCTION refresh_daily_sales()
RETURNS TRIGGER AS $$
BEGIN
    REFRESH MATERIALIZED VIEW CONCURRENTLY daily_sales_summary;
    RETURN NULL;
END;
$$ LANGUAGE plpgsql;

CREATE TRIGGER trigger_refresh_daily_sales
AFTER INSERT OR UPDATE OR DELETE ON orders
FOR EACH STATEMENT
EXECUTE FUNCTION refresh_daily_sales();
```

## Query Hints and Optimization

```sql
-- PostgreSQL: Force index usage (use sparingly)
SET enable_seqscan = OFF;
SELECT /*+ IndexScan(orders idx_orders_customer) */ * FROM orders WHERE customer_id = 123;
SET enable_seqscan = ON;

-- SQL Server: Query hints
SELECT * FROM orders WITH (INDEX(idx_orders_customer_date))
WHERE customer_id = 123;

-- Force specific join type
SELECT * FROM customers c
INNER MERGE JOIN orders o ON c.customer_id = o.customer_id;

-- MySQL: Index hints
SELECT * FROM orders USE INDEX (idx_orders_customer_date)
WHERE customer_id = 123;

SELECT * FROM orders FORCE INDEX (idx_orders_customer_date)
WHERE customer_id = 123;

-- PostgreSQL: Parallel query tuning
SET max_parallel_workers_per_gather = 4;
ALTER TABLE large_table SET (parallel_workers = 4);
```

## Performance Monitoring Queries

```sql
-- PostgreSQL: Find slow queries
SELECT
    query,
    calls,
    total_exec_time,
    mean_exec_time,
    max_exec_time,
    rows / calls as avg_rows
FROM pg_stat_statements
ORDER BY mean_exec_time DESC
LIMIT 20;

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

-- Table bloat detection
SELECT
    schemaname,
    tablename,
    pg_size_pretty(pg_total_relation_size(schemaname||'.'||tablename)) as size,
    n_dead_tup,
    n_live_tup,
    ROUND(n_dead_tup * 100.0 / NULLIF(n_live_tup + n_dead_tup, 0), 2) as dead_pct
FROM pg_stat_user_tables
WHERE n_dead_tup > 1000
ORDER BY n_dead_tup DESC;
```

## Best Practices Checklist

1. Always run EXPLAIN ANALYZE before optimizing
2. Create indexes on foreign keys and WHERE/JOIN columns
3. Use covering indexes for frequent queries
4. Keep statistics up to date (ANALYZE regularly)
5. Avoid SELECT *, specify needed columns
6. Use EXISTS instead of IN for subqueries
7. Filter early, aggregate late
8. Consider partitioning for large tables (>10M rows)
9. Use materialized views for expensive aggregations
10. Monitor slow query log and pg_stat_statements
