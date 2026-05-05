<!-- part 1/2 of query-patterns.md -->

# Query Patterns

## Common Table Expressions (CTEs)

```sql
-- Basic CTE for readability
WITH active_users AS (
    SELECT user_id, username, created_at
    FROM users
    WHERE is_active = true
      AND last_login >= CURRENT_DATE - INTERVAL '30 days'
),
user_orders AS (
    SELECT user_id, COUNT(*) as order_count, SUM(total) as total_spent
    FROM orders
    WHERE status = 'completed'
    GROUP BY user_id
)
SELECT
    u.username,
    u.created_at,
    COALESCE(o.order_count, 0) as orders,
    COALESCE(o.total_spent, 0) as lifetime_value
FROM active_users u
LEFT JOIN user_orders o ON u.user_id = o.user_id
WHERE COALESCE(o.order_count, 0) > 0
ORDER BY o.total_spent DESC;

-- CTE with multiple references (avoiding duplicate computation)
WITH monthly_sales AS (
    SELECT
        DATE_TRUNC('month', sale_date) as month,
        product_id,
        SUM(quantity) as total_quantity,
        SUM(amount) as total_amount
    FROM sales
    WHERE sale_date >= '2024-01-01'
    GROUP BY DATE_TRUNC('month', sale_date), product_id
)
SELECT
    current.month,
    current.product_id,
    current.total_amount,
    current.total_amount - COALESCE(previous.total_amount, 0) as growth,
    ROUND(100.0 * (current.total_amount - COALESCE(previous.total_amount, 0))
        / NULLIF(previous.total_amount, 0), 2) as growth_pct
FROM monthly_sales current
LEFT JOIN monthly_sales previous
    ON current.product_id = previous.product_id
    AND current.month = previous.month + INTERVAL '1 month';
```

## Recursive CTEs

```sql
-- Organizational hierarchy traversal
WITH RECURSIVE org_hierarchy AS (
    -- Anchor member: top-level managers
    SELECT
        employee_id,
        name,
        manager_id,
        1 as level,
        ARRAY[employee_id] as path,
        name as hierarchy_path
    FROM employees
    WHERE manager_id IS NULL

    UNION ALL

    -- Recursive member: employees reporting to current level
    SELECT
        e.employee_id,
        e.name,
        e.manager_id,
        h.level + 1,
        h.path || e.employee_id,
        h.hierarchy_path || ' > ' || e.name
    FROM employees e
    INNER JOIN org_hierarchy h ON e.manager_id = h.employee_id
    WHERE NOT e.employee_id = ANY(h.path)  -- Prevent cycles
)
SELECT
    employee_id,
    REPEAT('  ', level - 1) || name as indented_name,
    level,
    hierarchy_path
FROM org_hierarchy
ORDER BY path;

-- Bill of materials (parts explosion)
WITH RECURSIVE parts_explosion AS (
    SELECT
        part_id,
        component_id,
        quantity,
        1 as level,
        ARRAY[part_id] as path
    FROM bill_of_materials
    WHERE part_id = 'PRODUCT-123'

    UNION ALL

    SELECT
        pe.part_id,
        bom.component_id,
        pe.quantity * bom.quantity,
        pe.level + 1,
        pe.path || bom.part_id
    FROM parts_explosion pe
    INNER JOIN bill_of_materials bom ON pe.component_id = bom.part_id
    WHERE NOT bom.part_id = ANY(pe.path)
)
SELECT
    component_id,
    SUM(quantity) as total_quantity,
    MAX(level) as max_depth
FROM parts_explosion
GROUP BY component_id;
```

## Advanced JOIN Patterns

```sql
-- Self-join for finding gaps in sequences
SELECT
    a.order_id as current_id,
    MIN(b.order_id) as next_id,
    MIN(b.order_id) - a.order_id - 1 as gap_size
FROM orders a
LEFT JOIN orders b ON b.order_id > a.order_id
GROUP BY a.order_id
HAVING MIN(b.order_id) - a.order_id > 1;

-- LATERAL join for correlated subqueries (PostgreSQL)
SELECT
    c.customer_id,
    c.name,
    recent.order_date,
    recent.total
FROM customers c
CROSS JOIN LATERAL (
    SELECT order_date, total
    FROM orders o
    WHERE o.customer_id = c.customer_id
    ORDER BY order_date DESC
    LIMIT 3
) recent;

-- Anti-join pattern (records in A not in B)
SELECT u.user_id, u.email
FROM users u
LEFT JOIN orders o ON u.user_id = o.user_id
WHERE o.order_id IS NULL;

-- Using EXISTS (more efficient than IN for large sets)
SELECT u.user_id, u.email
FROM users u
WHERE NOT EXISTS (
    SELECT 1
    FROM orders o
    WHERE o.user_id = u.user_id
);
```

