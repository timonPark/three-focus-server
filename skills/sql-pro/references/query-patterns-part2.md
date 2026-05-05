<!-- part 2/2 of query-patterns.md -->

## Subquery Optimization

```sql
-- Scalar subquery in SELECT (use sparingly - can cause N+1)
SELECT
    p.product_id,
    p.name,
    (SELECT COUNT(*) FROM reviews r WHERE r.product_id = p.product_id) as review_count,
    (SELECT AVG(rating) FROM reviews r WHERE r.product_id = p.product_id) as avg_rating
FROM products p;

-- Better: Use JOINs with aggregation
SELECT
    p.product_id,
    p.name,
    COALESCE(r.review_count, 0) as review_count,
    r.avg_rating
FROM products p
LEFT JOIN (
    SELECT
        product_id,
        COUNT(*) as review_count,
        AVG(rating) as avg_rating
    FROM reviews
    GROUP BY product_id
) r ON p.product_id = r.product_id;

-- Correlated subquery for filtering
SELECT
    order_id,
    customer_id,
    total
FROM orders o1
WHERE total > (
    SELECT AVG(total)
    FROM orders o2
    WHERE o2.customer_id = o1.customer_id
);

-- Better: Use window functions
SELECT
    order_id,
    customer_id,
    total
FROM (
    SELECT
        order_id,
        customer_id,
        total,
        AVG(total) OVER (PARTITION BY customer_id) as avg_customer_total
    FROM orders
) x
WHERE total > avg_customer_total;
```

## PIVOT/UNPIVOT Operations

```sql
-- PostgreSQL CROSSTAB (requires tablefunc extension)
CREATE EXTENSION IF NOT EXISTS tablefunc;

SELECT * FROM crosstab(
    'SELECT customer_id, product_category, SUM(amount)
     FROM sales
     GROUP BY customer_id, product_category
     ORDER BY customer_id, product_category',
    'SELECT DISTINCT product_category FROM sales ORDER BY 1'
) AS ct(customer_id INT, electronics NUMERIC, clothing NUMERIC, food NUMERIC);

-- Manual PIVOT with CASE
SELECT
    customer_id,
    SUM(CASE WHEN product_category = 'electronics' THEN amount ELSE 0 END) as electronics,
    SUM(CASE WHEN product_category = 'clothing' THEN amount ELSE 0 END) as clothing,
    SUM(CASE WHEN product_category = 'food' THEN amount ELSE 0 END) as food
FROM sales
GROUP BY customer_id;

-- UNPIVOT pattern (row to column)
SELECT customer_id, 'electronics' as category, electronics as amount
FROM customer_sales WHERE electronics > 0
UNION ALL
SELECT customer_id, 'clothing', clothing
FROM customer_sales WHERE clothing > 0
UNION ALL
SELECT customer_id, 'food', food
FROM customer_sales WHERE food > 0;
```

## Set Operations

```sql
-- UNION for combining distinct results
SELECT product_id FROM active_products
UNION
SELECT product_id FROM featured_products;

-- UNION ALL for better performance (includes duplicates)
SELECT user_id, 'signup' as event FROM signups WHERE date = CURRENT_DATE
UNION ALL
SELECT user_id, 'purchase' as event FROM purchases WHERE date = CURRENT_DATE;

-- INTERSECT for common records
SELECT email FROM newsletter_subscribers
INTERSECT
SELECT email FROM premium_members;

-- EXCEPT for difference (A - B)
SELECT email FROM all_users
EXCEPT
SELECT email FROM unsubscribed_users;
```

## Performance Tips

1. **CTE Materialization**: PostgreSQL 12+ materializes CTEs by default. Use `WITH cte AS MATERIALIZED` or `NOT MATERIALIZED` to control
2. **JOIN Order**: Database optimizers handle this, but put smaller tables first in manual optimization
3. **EXISTS vs IN**: Use EXISTS for correlated checks, IN for small static lists
4. **Subquery vs JOIN**: Prefer JOINs for readability and optimizer friendliness
5. **UNION ALL vs UNION**: Use UNION ALL when duplicates are acceptable (no deduplication cost)
