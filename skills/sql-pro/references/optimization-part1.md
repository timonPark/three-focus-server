<!-- part 1/2 of optimization.md -->

# Query Optimization

## EXPLAIN Plan Analysis

```sql
-- PostgreSQL EXPLAIN ANALYZE
EXPLAIN (ANALYZE, BUFFERS, VERBOSE)
SELECT
    c.customer_id,
    c.name,
    COUNT(o.order_id) as order_count,
    SUM(o.total) as lifetime_value
FROM customers c
LEFT JOIN orders o ON c.customer_id = o.customer_id
WHERE c.created_at >= '2024-01-01'
GROUP BY c.customer_id, c.name
HAVING COUNT(o.order_id) > 5;

/*
Key metrics to analyze:
- Planning Time: Time to generate plan
- Execution Time: Actual runtime
- Seq Scan: Table scans (bad for large tables)
- Index Scan: Using indexes (good)
- Rows: Estimated vs actual (large difference = stale stats)
- Buffers: shared hit = cache, read = disk I/O
- Loops: Nested loop iterations
*/

-- MySQL EXPLAIN
EXPLAIN FORMAT=JSON
SELECT * FROM orders o
INNER JOIN customers c ON o.customer_id = c.customer_id
WHERE o.order_date >= '2024-01-01'
  AND c.country = 'US';

-- SQL Server execution plan
SET STATISTICS IO ON;
SET STATISTICS TIME ON;

SELECT ...;

-- Check actual vs estimated rows
SELECT * FROM sys.dm_exec_query_stats;
```

## Index Design and Optimization

```sql
-- Covering index (all columns in index)
CREATE INDEX idx_orders_covering ON orders (
    customer_id,
    order_date
) INCLUDE (total, status);

-- Query uses index-only scan (no table access needed)
SELECT customer_id, order_date, total, status
FROM orders
WHERE customer_id = 123
  AND order_date >= '2024-01-01';

-- Composite index (order matters!)
CREATE INDEX idx_orders_customer_date ON orders (customer_id, order_date DESC);
-- Good: WHERE customer_id = X AND order_date > Y
-- Good: WHERE customer_id = X
-- Bad: WHERE order_date > Y (doesn't use index)

-- Partial/Filtered index (smaller, faster)
CREATE INDEX idx_active_orders ON orders (customer_id, order_date)
WHERE status = 'active';

-- Only used when query includes the filter
SELECT * FROM orders
WHERE customer_id = 123
  AND status = 'active'
  AND order_date >= '2024-01-01';

-- Expression/Function-based index
CREATE INDEX idx_users_lower_email ON users (LOWER(email));

-- Now this uses the index
SELECT * FROM users WHERE LOWER(email) = 'user@example.com';

-- GIN index for arrays/JSONB (PostgreSQL)
CREATE INDEX idx_products_tags ON products USING GIN (tags);
SELECT * FROM products WHERE tags @> ARRAY['electronics', 'sale'];

CREATE INDEX idx_orders_metadata ON orders USING GIN (metadata jsonb_path_ops);
SELECT * FROM orders WHERE metadata @> '{"priority": "high"}';
```

## Index Maintenance

```sql
-- PostgreSQL: Find missing indexes
SELECT
    schemaname,
    tablename,
    seq_scan,
    seq_tup_read,
    idx_scan,
    seq_tup_read / seq_scan as avg_seq_read
FROM pg_stat_user_tables
WHERE seq_scan > 0
  AND seq_tup_read / seq_scan > 10000
ORDER BY seq_tup_read DESC;

-- Find unused indexes
SELECT
    schemaname,
    tablename,
    indexname,
    idx_scan,
    pg_size_pretty(pg_relation_size(indexrelid)) as index_size
FROM pg_stat_user_indexes
WHERE idx_scan = 0
  AND indexrelname NOT LIKE 'pg_toast%'
ORDER BY pg_relation_size(indexrelid) DESC;

-- Find duplicate indexes
SELECT
    pg_size_pretty(SUM(pg_relation_size(idx))::BIGINT) as size,
    (array_agg(idx))[1] as idx1,
    (array_agg(idx))[2] as idx2,
    (array_agg(idx))[3] as idx3
FROM (
    SELECT
        indexrelid::regclass as idx,
        (indrelid::text ||E'\n'|| indclass::text ||E'\n'||
         indkey::text ||E'\n'|| COALESCE(indexprs::text,'')||E'\n'||
         COALESCE(indpred::text,'')) as key
    FROM pg_index
) sub
GROUP BY key
HAVING COUNT(*) > 1
ORDER BY SUM(pg_relation_size(idx)) DESC;

-- Reindex to reduce bloat
REINDEX INDEX CONCURRENTLY idx_orders_customer_date;

-- Update statistics
ANALYZE orders;
ANALYZE VERBOSE;  -- Show progress
```

## Query Rewriting Patterns

```sql
-- Avoid SELECT DISTINCT when possible
-- Bad: Forces sort/dedup
SELECT DISTINCT customer_id FROM orders WHERE status = 'active';

-- Good: Use EXISTS
SELECT customer_id FROM customers c
WHERE EXISTS (
    SELECT 1 FROM orders o
    WHERE o.customer_id = c.customer_id
      AND o.status = 'active'
);

-- Avoid NOT IN with NULLs
-- Bad: NULL handling issues and poor performance
SELECT * FROM customers
WHERE customer_id NOT IN (SELECT customer_id FROM orders);

-- Good: Use NOT EXISTS
SELECT * FROM customers c
WHERE NOT EXISTS (
    SELECT 1 FROM orders o WHERE o.customer_id = c.customer_id
);

-- Push down filtering early
-- Bad: Filter after JOIN
SELECT c.*, o.*
FROM customers c
JOIN orders o ON c.customer_id = o.customer_id
WHERE c.country = 'US' AND o.order_date >= '2024-01-01';

-- Good: Use WHERE in subquery/CTE to reduce JOIN size
WITH us_customers AS (
    SELECT customer_id, name
    FROM customers
    WHERE country = 'US'
),
recent_orders AS (
    SELECT customer_id, order_id, total
    FROM orders
    WHERE order_date >= '2024-01-01'
)
SELECT c.*, o.*
FROM us_customers c
JOIN recent_orders o ON c.customer_id = o.customer_id;

-- Avoid scalar subqueries in SELECT
-- Bad: N+1 problem
SELECT
    p.product_id,
    p.name,
    (SELECT COUNT(*) FROM reviews WHERE product_id = p.product_id) as review_count
FROM products p;

-- Good: Single JOIN with GROUP BY
SELECT
    p.product_id,
    p.name,
    COUNT(r.review_id) as review_count
FROM products p
LEFT JOIN reviews r ON p.product_id = r.product_id
GROUP BY p.product_id, p.name;
```

