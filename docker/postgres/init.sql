-- Match the current JPA entities. Safe to run again on an existing volume.
-- Docker automatically runs this script only on a fresh PostgreSQL volume.
\set ON_ERROR_STOP on

SELECT 'CREATE DATABASE product_db'
WHERE NOT EXISTS (SELECT FROM pg_database WHERE datname = 'product_db')
\gexec
SELECT 'CREATE DATABASE order_db'
WHERE NOT EXISTS (SELECT FROM pg_database WHERE datname = 'order_db')
\gexec

\connect product_db
BEGIN;

CREATE TABLE IF NOT EXISTS categories (
  id uuid PRIMARY KEY,
  created_at timestamp(6) NOT NULL,
  created_by varchar(36),
  updated_at timestamp(6) NOT NULL,
  updated_by varchar(36),
  deleted_at timestamp(6),
  name varchar(255) NOT NULL
);

CREATE TABLE IF NOT EXISTS products (
  id uuid PRIMARY KEY,
  created_at timestamp(6) NOT NULL,
  created_by varchar(36),
  updated_at timestamp(6) NOT NULL,
  updated_by varchar(36),
  deleted_at timestamp(6),
  name varchar(255) NOT NULL,
  category_id uuid NOT NULL REFERENCES categories(id)
);

CREATE TABLE IF NOT EXISTS product_variants (
  id uuid PRIMARY KEY,
  created_at timestamp(6) NOT NULL,
  created_by varchar(36),
  updated_at timestamp(6) NOT NULL,
  updated_by varchar(36),
  deleted_at timestamp(6),
  variant_name varchar(255) NOT NULL,
  price double precision NOT NULL,
  description varchar(255) NOT NULL,
  product_id uuid NOT NULL REFERENCES products(id)
);

CREATE TABLE IF NOT EXISTS inventory (
  id uuid PRIMARY KEY,
  created_at timestamp(6) NOT NULL,
  created_by varchar(36),
  updated_at timestamp(6) NOT NULL,
  updated_by varchar(36),
  deleted_at timestamp(6),
  name varchar(255) NOT NULL,
  quantity integer NOT NULL,
  product_variant_id uuid NOT NULL UNIQUE REFERENCES product_variants(id)
);

CREATE TABLE IF NOT EXISTS processed_orders (
  order_id uuid PRIMARY KEY,
  version bigint,
  success boolean NOT NULL,
  failure_reason varchar(255)
);

COMMIT;

\connect order_db
BEGIN;

CREATE TABLE IF NOT EXISTS orders (
  id uuid PRIMARY KEY,
  created_at timestamp(6) NOT NULL,
  created_by varchar(36),
  updated_at timestamp(6) NOT NULL,
  updated_by varchar(36),
  deleted_at timestamp(6),
  status varchar(255) NOT NULL CHECK (status IN ('NEW', 'PREPARED', 'REJECTED')),
  total_amount numeric(19, 2) NOT NULL,
  failure_reason varchar(255)
);

CREATE TABLE IF NOT EXISTS order_items (
  id uuid PRIMARY KEY,
  created_at timestamp(6) NOT NULL,
  created_by varchar(36),
  updated_at timestamp(6) NOT NULL,
  updated_by varchar(36),
  deleted_at timestamp(6),
  order_id uuid NOT NULL REFERENCES orders(id),
  product_variant_id uuid NOT NULL,
  quantity integer NOT NULL,
  unit_price numeric(19, 2) NOT NULL
);

-- product_variant_id refers to product_db, so there is no cross-database FK.
COMMIT;
