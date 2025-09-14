-- Initialize database schema for checkout service
-- This script will be executed when PostgreSQL container starts

-- Grant all privileges to the postgres user
GRANT ALL PRIVILEGES ON DATABASE checkout_db TO postgres;

-- Create products table
DROP TABLE IF EXISTS products CASCADE;
CREATE TABLE products
(
    id             BIGSERIAL PRIMARY KEY,
    sku            VARCHAR(255) UNIQUE,
    name           VARCHAR(255) NOT NULL,
    unit_price     INTEGER      NOT NULL,
    offer_quantity INTEGER,
    offer_price    INTEGER
);

-- Create orders table
DROP TABLE IF EXISTS orders CASCADE;
CREATE TABLE orders
(
    id          BIGINT PRIMARY KEY GENERATED ALWAYS AS IDENTITY,
    final_price INT       NOT NULL,
    created_at  TIMESTAMP NOT NULL
);

-- Create order_items table
DROP TABLE IF EXISTS order_items CASCADE;
CREATE TABLE order_items
(
    id                BIGINT PRIMARY KEY GENERATED ALWAYS AS IDENTITY,
    order_id          BIGINT       NOT NULL REFERENCES orders (id),
    product_sku       VARCHAR(255) NOT NULL,
    quantity          INTEGER      NOT NULL
);

-- Insert sample products for testing
INSERT INTO products (sku, name, unit_price, offer_quantity, offer_price) VALUES
('A', 'Apple', 50, 3, 130),
('B', 'Banana', 30, 2, 45),
('C', 'Cherry', 20, NULL, NULL),
('D', 'Date', 15, 5, 60);

-- Create indexes for better performance
CREATE INDEX IF NOT EXISTS idx_products_sku ON products(sku);
CREATE INDEX IF NOT EXISTS idx_order_items_order_id ON order_items(order_id);
CREATE INDEX IF NOT EXISTS idx_order_items_product_sku ON order_items(product_sku);
