-- Schema: app
CREATE SCHEMA IF NOT EXISTS app;

-- Table: seller
CREATE TABLE IF NOT EXISTS app.seller (
    id SERIAL PRIMARY KEY,
    name VARCHAR(255) NOT NULL,
    contact_info VARCHAR(255),
    registration_date TIMESTAMP NOT NULL
);

-- Table: transaction
CREATE TABLE IF NOT EXISTS app.transaction (
    id SERIAL PRIMARY KEY,
    seller_id INTEGER NOT NULL REFERENCES app.seller(id),
    amount DECIMAL(10,2) NOT NULL,
    payment_type VARCHAR(50) NOT NULL,
    transaction_date TIMESTAMP NOT NULL
);