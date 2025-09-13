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