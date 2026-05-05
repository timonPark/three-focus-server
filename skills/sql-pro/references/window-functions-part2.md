<!-- part 2/2 of window-functions.md -->

## Advanced Analytics

```sql
-- Percentile calculations
SELECT
    employee_id,
    salary,
    PERCENT_RANK() OVER (ORDER BY salary) as pct_rank,
    CUME_DIST() OVER (ORDER BY salary) as cumulative_dist,
    PERCENTILE_CONT(0.5) WITHIN GROUP (ORDER BY salary) OVER () as median_salary,
    PERCENTILE_DISC(0.9) WITHIN GROUP (ORDER BY salary) OVER () as p90_salary
FROM employees;

-- Cohort retention analysis
WITH user_cohorts AS (
    SELECT
        user_id,
        DATE_TRUNC('month', signup_date) as cohort_month,
        DATE_TRUNC('month', activity_date) as activity_month
    FROM user_activity
),
cohort_sizes AS (
    SELECT
        cohort_month,
        COUNT(DISTINCT user_id) as cohort_size
    FROM user_cohorts
    GROUP BY cohort_month
)
SELECT
    uc.cohort_month,
    uc.activity_month,
    EXTRACT(MONTH FROM AGE(uc.activity_month, uc.cohort_month)) as months_since_signup,
    COUNT(DISTINCT uc.user_id) as active_users,
    cs.cohort_size,
    ROUND(100.0 * COUNT(DISTINCT uc.user_id) / cs.cohort_size, 2) as retention_pct
FROM user_cohorts uc
JOIN cohort_sizes cs ON uc.cohort_month = cs.cohort_month
GROUP BY uc.cohort_month, uc.activity_month, cs.cohort_size
ORDER BY uc.cohort_month, months_since_signup;

-- Time-series gap filling
SELECT
    date_series.date,
    COALESCE(s.revenue, 0) as revenue,
    AVG(s.revenue) OVER (
        ORDER BY date_series.date
        ROWS BETWEEN 6 PRECEDING AND CURRENT ROW
    ) as ma_7day
FROM generate_series(
    '2024-01-01'::DATE,
    '2024-12-31'::DATE,
    '1 day'::INTERVAL
) AS date_series(date)
LEFT JOIN sales s ON date_series.date = s.sale_date;
```

## Conditional Aggregation with Windows

```sql
-- Filter within window function
SELECT
    product_id,
    sale_date,
    quantity,
    SUM(quantity) FILTER (WHERE quantity > 10) OVER (
        PARTITION BY product_id
        ORDER BY sale_date
    ) as cumulative_large_orders,
    COUNT(*) FILTER (WHERE quantity > 100) OVER (
        PARTITION BY product_id
    ) as total_bulk_orders
FROM sales;

-- Multiple conditions
SELECT
    customer_id,
    order_date,
    total,
    COUNT(*) FILTER (WHERE total > 1000) OVER (
        PARTITION BY customer_id
    ) as high_value_order_count,
    AVG(total) FILTER (WHERE total < 100) OVER (
        PARTITION BY customer_id
    ) as avg_small_order_value
FROM orders;
```

## Performance Considerations

```sql
-- Avoid multiple window passes - combine into one
-- Bad: Multiple scans
SELECT
    product_id,
    (SELECT AVG(price) FROM products) as avg_price,
    (SELECT MAX(price) FROM products) as max_price
FROM products;

-- Good: Single window pass
SELECT DISTINCT
    AVG(price) OVER () as avg_price,
    MAX(price) OVER () as max_price
FROM products;

-- Materialize expensive windows
CREATE MATERIALIZED VIEW product_rankings AS
SELECT
    product_id,
    category,
    sales_count,
    RANK() OVER (PARTITION BY category ORDER BY sales_count DESC) as category_rank,
    PERCENT_RANK() OVER (ORDER BY sales_count DESC) as overall_percentile
FROM product_sales_summary;

CREATE INDEX idx_product_rankings_category ON product_rankings(category, category_rank);
```

## Common Patterns

1. **Top N per Group**: Use ROW_NUMBER() with WHERE rn <= N
2. **Running Totals**: SUM() OVER (ORDER BY date)
3. **Moving Averages**: AVG() with ROWS BETWEEN N PRECEDING
4. **Session Analysis**: LAG() to detect time gaps
5. **Deduplication**: ROW_NUMBER() OVER (PARTITION BY key ORDER BY priority) WHERE rn = 1
6. **Percentiles**: PERCENT_RANK() or PERCENTILE_CONT()
7. **Year-over-Year**: LAG(value, 12) OVER (ORDER BY month)
8. **Cohort Analysis**: PARTITION BY cohort_date, aggregate over activity periods
