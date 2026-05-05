<!-- part 2/3 of database-design.md -->

## Constraints and Validation

```sql
-- CHECK constraints
CREATE TABLE employees (
    employee_id SERIAL PRIMARY KEY,
    first_name VARCHAR(50) NOT NULL,
    last_name VARCHAR(50) NOT NULL,
    email VARCHAR(255) NOT NULL UNIQUE,
    salary DECIMAL(12,2) NOT NULL,
    hire_date DATE NOT NULL,
    birth_date DATE NOT NULL,

    CONSTRAINT chk_salary_positive CHECK (salary > 0),
    CONSTRAINT chk_email_format CHECK (email ~* '^[A-Za-z0-9._%+-]+@[A-Za-z0-9.-]+\.[A-Z]{2,}$'),
    CONSTRAINT chk_hire_after_birth CHECK (hire_date > birth_date + INTERVAL '16 years'),
    CONSTRAINT chk_hire_not_future CHECK (hire_date <= CURRENT_DATE)
);

-- Unique constraints (including composite)
CREATE TABLE user_preferences (
    user_id INT NOT NULL,
    preference_key VARCHAR(50) NOT NULL,
    preference_value TEXT,

    CONSTRAINT uq_user_preference UNIQUE (user_id, preference_key)
);

-- NOT NULL constraints with defaults
CREATE TABLE products (
    product_id SERIAL PRIMARY KEY,
    name VARCHAR(200) NOT NULL,
    description TEXT,
    price DECIMAL(10,2) NOT NULL DEFAULT 0.00,
    is_active BOOLEAN NOT NULL DEFAULT true,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP
);

-- Exclusion constraints (PostgreSQL - prevent overlapping ranges)
CREATE TABLE room_bookings (
    booking_id SERIAL PRIMARY KEY,
    room_id INT NOT NULL,
    booked_during TSTZRANGE NOT NULL,

    EXCLUDE USING GIST (
        room_id WITH =,
        booked_during WITH &&
    )  -- Prevent overlapping bookings for same room
);
```

## Indexing Strategy

```sql
-- Index foreign keys (critical for JOIN performance)
CREATE INDEX idx_orders_customer_id ON orders(customer_id);
CREATE INDEX idx_order_items_order_id ON order_items(order_id);
CREATE INDEX idx_order_items_product_id ON order_items(product_id);

-- Composite index for common queries
CREATE INDEX idx_orders_customer_date ON orders(customer_id, order_date DESC);
-- Supports:
-- WHERE customer_id = ? AND order_date > ?
-- WHERE customer_id = ? ORDER BY order_date DESC

-- Partial index for common filters
CREATE INDEX idx_active_products ON products(category, price)
WHERE is_active = true AND deleted_at IS NULL;

-- Unique index for business rules
CREATE UNIQUE INDEX idx_users_active_email ON users(LOWER(email))
WHERE deleted_at IS NULL;
-- Ensures no duplicate emails among active users
```

## Common Design Patterns

```sql
-- Polymorphic associations (flexible but harder to enforce integrity)
CREATE TABLE comments (
    comment_id SERIAL PRIMARY KEY,
    commentable_type VARCHAR(50) NOT NULL,  -- 'Post', 'Photo', 'Video'
    commentable_id INT NOT NULL,
    content TEXT NOT NULL,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,

    -- Cannot enforce FK without triggers/application logic
    CHECK (commentable_type IN ('Post', 'Photo', 'Video'))
);

-- Better: Separate tables with proper FKs
CREATE TABLE post_comments (
    comment_id SERIAL PRIMARY KEY,
    post_id INT NOT NULL REFERENCES posts(post_id),
    content TEXT NOT NULL,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

CREATE TABLE photo_comments (
    comment_id SERIAL PRIMARY KEY,
    photo_id INT NOT NULL REFERENCES photos(photo_id),
    content TEXT NOT NULL,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

-- Many-to-many with attributes (junction/bridge table)
CREATE TABLE students (
    student_id SERIAL PRIMARY KEY,
    name VARCHAR(100) NOT NULL
);

CREATE TABLE courses (
    course_id SERIAL PRIMARY KEY,
    course_name VARCHAR(100) NOT NULL
);

CREATE TABLE enrollments (
    enrollment_id SERIAL PRIMARY KEY,
    student_id INT NOT NULL REFERENCES students(student_id),
    course_id INT NOT NULL REFERENCES courses(course_id),
    enrollment_date DATE NOT NULL DEFAULT CURRENT_DATE,
    grade CHAR(2),
    status VARCHAR(20) DEFAULT 'active',

    UNIQUE (student_id, course_id),
    CHECK (status IN ('active', 'completed', 'dropped'))
);

-- Self-referencing hierarchy
CREATE TABLE categories (
    category_id SERIAL PRIMARY KEY,
    category_name VARCHAR(100) NOT NULL,
    parent_category_id INT REFERENCES categories(category_id),
    level INT NOT NULL DEFAULT 0,

    CHECK (category_id != parent_category_id)  -- Prevent self-reference
);

-- Adjacency list example
INSERT INTO categories VALUES
    (1, 'Electronics', NULL, 0),
    (2, 'Computers', 1, 1),
    (3, 'Laptops', 2, 2),
    (4, 'Desktops', 2, 2);
```

