<!-- part 2/3 of dialect-differences.md -->

## JSON/JSONB Support

```sql
-- PostgreSQL (JSONB - binary, indexable)
CREATE TABLE events (
    event_id SERIAL PRIMARY KEY,
    event_data JSONB NOT NULL
);

INSERT INTO events (event_data) VALUES ('{"user_id": 123, "action": "login"}');

SELECT event_data->>'user_id' as user_id FROM events;
SELECT * FROM events WHERE event_data @> '{"action": "login"}';
SELECT * FROM events WHERE event_data->>'user_id' = '123';

CREATE INDEX idx_events_data ON events USING GIN (event_data);

-- MySQL (8.0+)
CREATE TABLE events (
    event_id INT AUTO_INCREMENT PRIMARY KEY,
    event_data JSON NOT NULL
);

SELECT JSON_EXTRACT(event_data, '$.user_id') as user_id FROM events;
SELECT * FROM events WHERE JSON_EXTRACT(event_data, '$.action') = 'login';

CREATE INDEX idx_events_user ON events ((CAST(event_data->>'$.user_id' AS UNSIGNED)));

-- SQL Server (2016+)
CREATE TABLE events (
    event_id INT IDENTITY(1,1) PRIMARY KEY,
    event_data NVARCHAR(MAX) CHECK (ISJSON(event_data) = 1)
);

SELECT JSON_VALUE(event_data, '$.user_id') as user_id FROM events;
SELECT * FROM events WHERE JSON_VALUE(event_data, '$.action') = 'login';

-- Oracle (12c+)
CREATE TABLE events (
    event_id NUMBER GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
    event_data CLOB CHECK (event_data IS JSON)
);

SELECT JSON_VALUE(event_data, '$.user_id') as user_id FROM events;
SELECT * FROM events WHERE JSON_EXISTS(event_data, '$.action?(@ == "login")');
```

## String Comparison (Case Sensitivity)

```sql
-- PostgreSQL (case-sensitive by default)
SELECT * FROM users WHERE email = 'USER@EXAMPLE.COM';  -- Won't match 'user@example.com'
SELECT * FROM users WHERE LOWER(email) = LOWER('USER@EXAMPLE.COM');
SELECT * FROM users WHERE email ILIKE 'user@example.com';  -- Case-insensitive

-- MySQL (case-insensitive by default with utf8_general_ci collation)
SELECT * FROM users WHERE email = 'USER@EXAMPLE.COM';  -- Matches 'user@example.com'
SELECT * FROM users WHERE email COLLATE utf8_bin = 'user@example.com';  -- Case-sensitive

-- SQL Server (depends on collation, usually case-insensitive)
SELECT * FROM users WHERE email = 'USER@EXAMPLE.COM';  -- Usually matches
SELECT * FROM users WHERE email COLLATE Latin1_General_BIN = 'user@example.com';  -- Case-sensitive

-- Oracle (case-sensitive by default)
SELECT * FROM users WHERE email = 'USER@EXAMPLE.COM';  -- Won't match 'user@example.com'
SELECT * FROM users WHERE UPPER(email) = UPPER('user@example.com');
```

## Recursive CTEs

```sql
-- PostgreSQL
WITH RECURSIVE subordinates AS (
    SELECT employee_id, name, manager_id, 1 as level
    FROM employees WHERE manager_id IS NULL
    UNION ALL
    SELECT e.employee_id, e.name, e.manager_id, s.level + 1
    FROM employees e
    INNER JOIN subordinates s ON e.manager_id = s.employee_id
)
SELECT * FROM subordinates;

-- MySQL (8.0+) - Same syntax as PostgreSQL
WITH RECURSIVE subordinates AS (
    SELECT employee_id, name, manager_id, 1 as level
    FROM employees WHERE manager_id IS NULL
    UNION ALL
    SELECT e.employee_id, e.name, e.manager_id, s.level + 1
    FROM employees e
    INNER JOIN subordinates s ON e.manager_id = s.employee_id
)
SELECT * FROM subordinates;

-- SQL Server - No RECURSIVE keyword
WITH subordinates AS (
    SELECT employee_id, name, manager_id, 1 as level
    FROM employees WHERE manager_id IS NULL
    UNION ALL
    SELECT e.employee_id, e.name, e.manager_id, s.level + 1
    FROM employees e
    INNER JOIN subordinates s ON e.manager_id = s.employee_id
)
SELECT * FROM subordinates;

-- Oracle - CONNECT BY (traditional hierarchical queries)
SELECT employee_id, name, manager_id, LEVEL
FROM employees
START WITH manager_id IS NULL
CONNECT BY PRIOR employee_id = manager_id;
```

## Window Functions - Frame Specifications

```sql
-- PostgreSQL - Full support
SELECT
    order_date,
    total,
    SUM(total) OVER (
        ORDER BY order_date
        RANGE BETWEEN INTERVAL '7 days' PRECEDING AND CURRENT ROW
    ) as rolling_7day
FROM orders;

-- MySQL (8.0+) - Limited RANGE support (no intervals)
SELECT
    order_date,
    total,
    SUM(total) OVER (
        ORDER BY order_date
        ROWS BETWEEN 6 PRECEDING AND CURRENT ROW
    ) as rolling_7rows
FROM orders;

-- SQL Server - Full support
SELECT
    order_date,
    total,
    SUM(total) OVER (
        ORDER BY order_date
        ROWS BETWEEN 6 PRECEDING AND CURRENT ROW
    ) as rolling_7rows
FROM orders;

-- Oracle - Full support
SELECT
    order_date,
    total,
    SUM(total) OVER (
        ORDER BY order_date
        RANGE BETWEEN INTERVAL '7' DAY PRECEDING AND CURRENT ROW
    ) as rolling_7day
FROM orders;
```

