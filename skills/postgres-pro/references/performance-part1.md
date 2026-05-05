<!-- part 1/2 of performance.md -->

# Performance Optimization

## EXPLAIN ANALYZE Fundamentals

```sql
-- Basic EXPLAIN ANALYZE
EXPLAIN (ANALYZE, BUFFERS, VERBOSE)
SELECT u.id, u.name, COUNT(o.id) as order_count
FROM users u
LEFT JOIN orders o ON u.id = o.user_id
WHERE u.created_at > '2024-01-01'
GROUP BY u.id, u.name;

-- Key metrics to watch:
-- Planning Time: Time spent creating query plan
-- Execution Time: Actual query execution time
-- Shared Hit Blocks: Data found in cache (good)
-- Shared Read Blocks: Data read from disk (slow)
-- Rows: Estimated vs actual row counts
```

## Reading EXPLAIN Output

```
Seq Scan on users  (cost=0.00..1234.56 rows=10000 width=32)
                    ^^^^^^^^^^^^^^^^^^^^  ^^^^^^     ^^^^^^^^
                    startup..total cost   estimate   row width

Actual time: 0.123..45.678 rows=9876 loops=1
             ^^^^^^^^^^^^^^^  ^^^^^^^^  ^^^^^^^
             first..last row  actual    iterations
```

**Node types (fastest to slowest):**
- Index Only Scan - Best, data from index only
- Index Scan - Good, uses index + heap lookup
- Bitmap Index Scan - Good for multiple conditions
- Seq Scan - Table scan, OK for small tables
- Seq Scan on large table - Problem, needs index

## Index Strategies

### B-tree Indexes (Default)

```sql
-- Single column index
CREATE INDEX idx_users_email ON users(email);

-- Multi-column index (order matters!)
CREATE INDEX idx_orders_user_date ON orders(user_id, created_at DESC);
-- Good for: WHERE user_id = X ORDER BY created_at DESC
-- Good for: WHERE user_id = X AND created_at > Y
-- Bad for: WHERE created_at > Y (doesn't use index)

-- Partial index (smaller, faster)
CREATE INDEX idx_active_users ON users(email) WHERE active = true;

-- Expression index
CREATE INDEX idx_users_lower_email ON users(LOWER(email));
-- Enables: WHERE LOWER(email) = 'user@example.com'

-- Covering index (includes extra columns)
CREATE INDEX idx_orders_covering ON orders(user_id) INCLUDE (total, created_at);
-- Enables Index Only Scan
```

### GIN Indexes (JSONB, arrays, full-text)

```sql
-- JSONB containment
CREATE INDEX idx_data_gin ON documents USING GIN(data);
-- Enables: WHERE data @> '{"status": "active"}'

-- JSONB specific paths
CREATE INDEX idx_data_status ON documents USING GIN((data -> 'status'));

-- Array operations
CREATE INDEX idx_tags_gin ON posts USING GIN(tags);
-- Enables: WHERE tags @> ARRAY['postgresql', 'performance']

-- Full-text search
CREATE INDEX idx_content_fts ON articles USING GIN(to_tsvector('english', content));
-- Enables: WHERE to_tsvector('english', content) @@ to_tsquery('postgresql & performance')
```

### GiST Indexes (Spatial, ranges, nearest neighbor)

```sql
-- PostGIS spatial index
CREATE INDEX idx_locations_geom ON locations USING GIST(geom);
-- Enables: WHERE ST_DWithin(geom, point, 1000)

-- Range types
CREATE INDEX idx_bookings_range ON bookings USING GIST(during);
-- Enables: WHERE during && '[2024-01-01, 2024-01-31]'::daterange

-- Nearest neighbor (KNN)
CREATE INDEX idx_locations_gist ON locations USING GIST(coordinates);
-- Enables: ORDER BY coordinates <-> point('0,0') LIMIT 10
```

### BRIN Indexes (Large, naturally ordered tables)

```sql
-- Time-series data (insert-only, sorted by time)
CREATE INDEX idx_metrics_time_brin ON metrics USING BRIN(timestamp);
-- Very small index, good for WHERE timestamp > NOW() - INTERVAL '1 day'

-- Works well with:
-- - Log tables
-- - Time-series metrics
-- - Append-only tables with natural order
```

## Statistics and Planner

```sql
-- Update statistics (do after bulk changes)
ANALYZE users;
ANALYZE;  -- All tables

-- Check statistics freshness
SELECT schemaname, tablename, last_analyze, last_autoanalyze
FROM pg_stat_user_tables
WHERE schemaname = 'public';

-- Increase statistics target for high-cardinality columns
ALTER TABLE users ALTER COLUMN email SET STATISTICS 1000;
-- Default is 100, increase for better selectivity estimates

-- View column statistics
SELECT * FROM pg_stats WHERE tablename = 'users' AND attname = 'email';
```

## Query Optimization Patterns

