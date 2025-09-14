DROP TABLE IF EXISTS products;
CREATE TABLE products
(
    id             BIGSERIAL PRIMARY KEY,
    sku            VARCHAR(255) UNIQUE,
    name           VARCHAR(255) NOT NULL,
    unit_price     INTEGER      NOT NULL,
    offer_quantity INTEGER,
    offer_price    INTEGER
);

DROP TABLE IF EXISTS orders;
CREATE TABLE orders
(
    id          BIGINT PRIMARY KEY GENERATED ALWAYS AS IDENTITY,
    final_price INT       NOT NULL,
    created_at  TIMESTAMP NOT NULL
);

CREATE TABLE order_items
(
    id                BIGINT PRIMARY KEY GENERATED ALWAYS AS IDENTITY,
    order_id          BIGINT       NOT NULL REFERENCES orders (id),
    product_sku       VARCHAR(255) NOT NULL,
    quantity          INTEGER      NOT NULL
);