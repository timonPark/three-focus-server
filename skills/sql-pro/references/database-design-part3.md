<!-- part 3/3 of database-design.md -->

## Temporal/Historical Data

```sql
-- Slowly Changing Dimension Type 2 (SCD2) - Full history
CREATE TABLE customer_history (
    customer_history_id SERIAL PRIMARY KEY,
    customer_id INT NOT NULL,
    name VARCHAR(100) NOT NULL,
    email VARCHAR(255) NOT NULL,
    address TEXT,
    valid_from TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    valid_to TIMESTAMP,
    is_current BOOLEAN NOT NULL DEFAULT true,

    CHECK (valid_to IS NULL OR valid_to > valid_from)
);

-- Ensure only one current record per customer
CREATE UNIQUE INDEX idx_customer_current ON customer_history(customer_id)
WHERE is_current = true;

-- Temporal tables (PostgreSQL system-versioning)
CREATE TABLE products (
    product_id SERIAL PRIMARY KEY,
    name VARCHAR(200) NOT NULL,
    price DECIMAL(10,2) NOT NULL,
    sys_period TSTZRANGE NOT NULL DEFAULT tstzrange(CURRENT_TIMESTAMP, NULL)
);

CREATE TABLE products_history (LIKE products);

CREATE TRIGGER versioning_trigger
BEFORE INSERT OR UPDATE OR DELETE ON products
FOR EACH ROW EXECUTE FUNCTION versioning('sys_period', 'products_history', true);
```

## Soft Deletes

```sql
-- Soft delete pattern
CREATE TABLE posts (
    post_id SERIAL PRIMARY KEY,
    title VARCHAR(255) NOT NULL,
    content TEXT NOT NULL,
    author_id INT NOT NULL REFERENCES users(user_id),
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    deleted_at TIMESTAMP,  -- NULL = active, non-NULL = deleted
    deleted_by INT REFERENCES users(user_id)
);

-- Index for filtering active records
CREATE INDEX idx_posts_active ON posts(created_at DESC)
WHERE deleted_at IS NULL;

-- View for active posts only
CREATE VIEW active_posts AS
SELECT post_id, title, content, author_id, created_at, updated_at
FROM posts
WHERE deleted_at IS NULL;
```

## Audit Trails

```sql
-- Audit table pattern
CREATE TABLE audit_log (
    audit_id BIGSERIAL PRIMARY KEY,
    table_name VARCHAR(100) NOT NULL,
    record_id BIGINT NOT NULL,
    action VARCHAR(10) NOT NULL CHECK (action IN ('INSERT', 'UPDATE', 'DELETE')),
    old_values JSONB,
    new_values JSONB,
    changed_by INT REFERENCES users(user_id),
    changed_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP
);

CREATE INDEX idx_audit_table_record ON audit_log(table_name, record_id);
CREATE INDEX idx_audit_timestamp ON audit_log(changed_at DESC);

-- Trigger function for automatic auditing
CREATE OR REPLACE FUNCTION audit_trigger_func()
RETURNS TRIGGER AS $$
BEGIN
    IF (TG_OP = 'DELETE') THEN
        INSERT INTO audit_log (table_name, record_id, action, old_values)
        VALUES (TG_TABLE_NAME, OLD.product_id, 'DELETE', row_to_json(OLD));
        RETURN OLD;
    ELSIF (TG_OP = 'UPDATE') THEN
        INSERT INTO audit_log (table_name, record_id, action, old_values, new_values)
        VALUES (TG_TABLE_NAME, NEW.product_id, 'UPDATE', row_to_json(OLD), row_to_json(NEW));
        RETURN NEW;
    ELSIF (TG_OP = 'INSERT') THEN
        INSERT INTO audit_log (table_name, record_id, action, new_values)
        VALUES (TG_TABLE_NAME, NEW.product_id, 'INSERT', row_to_json(NEW));
        RETURN NEW;
    END IF;
END;
$$ LANGUAGE plpgsql;

CREATE TRIGGER products_audit
AFTER INSERT OR UPDATE OR DELETE ON products
FOR EACH ROW EXECUTE FUNCTION audit_trigger_func();
```

## Schema Design Best Practices

1. **Choose appropriate data types**: Use smallest type that fits (INT vs BIGINT, VARCHAR(50) vs TEXT)
2. **Index foreign keys**: Always index FK columns for JOIN performance
3. **Avoid NULLs when possible**: Use NOT NULL with defaults
4. **Use constraints**: Enforce data integrity at database level
5. **Normalize to 3NF**: Then denormalize strategically for performance
6. **Consider soft deletes**: For auditing and data recovery
7. **Plan for growth**: Use BIGINT for high-volume PKs
8. **Document schema**: Comment tables and complex constraints
9. **Version control**: Track schema changes with migrations
10. **Test with realistic data**: Validate design with production-scale data
