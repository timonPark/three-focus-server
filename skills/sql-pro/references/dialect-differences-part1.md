<!-- part 1/3 of dialect-differences.md -->

# Database Dialect Differences

## Auto-Incrementing Primary Keys

```sql
-- PostgreSQL
CREATE TABLE users (
    user_id SERIAL PRIMARY KEY,  -- or BIGSERIAL for BIGINT
    name VARCHAR(100)
);
-- Alternative (PostgreSQL 10+)
CREATE TABLE users (
    user_id INT GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
    name VARCHAR(100)
);

-- MySQL
CREATE TABLE users (
    user_id INT AUTO_INCREMENT PRIMARY KEY,
    name VARCHAR(100)
);

-- SQL Server
CREATE TABLE users (
    user_id INT IDENTITY(1,1) PRIMARY KEY,
    name VARCHAR(100)
);

-- Oracle
CREATE TABLE users (
    user_id NUMBER GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
    name VARCHAR2(100)
);
-- Or using sequence (older approach)
CREATE SEQUENCE user_id_seq;
CREATE TABLE users (
    user_id NUMBER DEFAULT user_id_seq.NEXTVAL PRIMARY KEY,
    name VARCHAR2(100)
);
```

## String Concatenation

```sql
-- PostgreSQL (strict - automatic casting)
SELECT first_name || ' ' || last_name AS full_name FROM users;
SELECT CONCAT(first_name, ' ', last_name) AS full_name FROM users;  -- NULL-safe

-- MySQL (automatic type conversion)
SELECT CONCAT(first_name, ' ', last_name) AS full_name FROM users;
SELECT first_name + ' ' + last_name FROM users;  -- ERROR in MySQL

-- SQL Server
SELECT first_name + ' ' + last_name AS full_name FROM users;
SELECT CONCAT(first_name, ' ', last_name) AS full_name FROM users;  -- 2012+

-- Oracle
SELECT first_name || ' ' || last_name AS full_name FROM users;
SELECT CONCAT(first_name, last_name) FROM users;  -- Only 2 arguments!
```

## Date/Time Functions

```sql
-- Current timestamp
-- PostgreSQL
SELECT CURRENT_TIMESTAMP, NOW(), CURRENT_DATE, CURRENT_TIME;

-- MySQL
SELECT CURRENT_TIMESTAMP, NOW(), CURDATE(), CURTIME();

-- SQL Server
SELECT GETDATE(), SYSDATETIME(), CAST(GETDATE() AS DATE);

-- Oracle
SELECT SYSDATE, SYSTIMESTAMP, TRUNC(SYSDATE) FROM DUAL;

-- Date arithmetic
-- PostgreSQL
SELECT order_date + INTERVAL '7 days' FROM orders;
SELECT order_date - INTERVAL '1 month' FROM orders;
SELECT AGE(CURRENT_DATE, birth_date) FROM users;  -- Interval type

-- MySQL
SELECT DATE_ADD(order_date, INTERVAL 7 DAY) FROM orders;
SELECT DATE_SUB(order_date, INTERVAL 1 MONTH) FROM orders;
SELECT DATEDIFF(CURRENT_DATE, birth_date) FROM users;  -- Days only

-- SQL Server
SELECT DATEADD(day, 7, order_date) FROM orders;
SELECT DATEADD(month, -1, order_date) FROM orders;
SELECT DATEDIFF(year, birth_date, GETDATE()) FROM users;

-- Oracle
SELECT order_date + 7 FROM orders;  -- +7 days
SELECT ADD_MONTHS(order_date, -1) FROM orders;
SELECT MONTHS_BETWEEN(SYSDATE, birth_date) / 12 FROM users;

-- Date formatting
-- PostgreSQL
SELECT TO_CHAR(order_date, 'YYYY-MM-DD') FROM orders;

-- MySQL
SELECT DATE_FORMAT(order_date, '%Y-%m-%d') FROM orders;

-- SQL Server
SELECT FORMAT(order_date, 'yyyy-MM-dd') FROM orders;
SELECT CONVERT(VARCHAR(10), order_date, 120) FROM orders;  -- Style 120 = yyyy-MM-dd

-- Oracle
SELECT TO_CHAR(order_date, 'YYYY-MM-DD') FROM orders;
```

## LIMIT/OFFSET (Pagination)

```sql
-- PostgreSQL & MySQL
SELECT * FROM products
ORDER BY product_id
LIMIT 10 OFFSET 20;

-- SQL Server (2012+)
SELECT * FROM products
ORDER BY product_id
OFFSET 20 ROWS FETCH NEXT 10 ROWS ONLY;

-- SQL Server (older - ROW_NUMBER)
SELECT * FROM (
    SELECT *, ROW_NUMBER() OVER (ORDER BY product_id) as rn
    FROM products
) x
WHERE rn BETWEEN 21 AND 30;

-- Oracle (12c+)
SELECT * FROM products
ORDER BY product_id
OFFSET 20 ROWS FETCH NEXT 10 ROWS ONLY;

-- Oracle (older - ROWNUM)
SELECT * FROM (
    SELECT a.*, ROWNUM rnum FROM (
        SELECT * FROM products ORDER BY product_id
    ) a
    WHERE ROWNUM <= 30
)
WHERE rnum > 20;
```

## Boolean Data Type

```sql
-- PostgreSQL (native BOOLEAN)
CREATE TABLE users (
    user_id SERIAL PRIMARY KEY,
    is_active BOOLEAN DEFAULT true
);
SELECT * FROM users WHERE is_active = true;

-- MySQL (TINYINT(1) or BOOLEAN alias)
CREATE TABLE users (
    user_id INT AUTO_INCREMENT PRIMARY KEY,
    is_active BOOLEAN DEFAULT 1  -- Stored as TINYINT(1)
);
SELECT * FROM users WHERE is_active = 1;

-- SQL Server (BIT)
CREATE TABLE users (
    user_id INT IDENTITY(1,1) PRIMARY KEY,
    is_active BIT DEFAULT 1
);
SELECT * FROM users WHERE is_active = 1;

-- Oracle (no native boolean in tables, use NUMBER or CHAR)
CREATE TABLE users (
    user_id NUMBER GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
    is_active NUMBER(1) DEFAULT 1 CHECK (is_active IN (0, 1))
);
SELECT * FROM users WHERE is_active = 1;
```

