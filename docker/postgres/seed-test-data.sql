-- Run manually against product_db. Re-running preserves existing rows and stock.
\set ON_ERROR_STOP on
BEGIN;

INSERT INTO categories (id, name, created_at, updated_at) VALUES
('10000000-0000-0000-0000-000000000001', 'Demo merchandise', now(), now())
ON CONFLICT (id) DO NOTHING;

INSERT INTO products (id, name, category_id, created_at, updated_at) VALUES
('20000000-0000-0000-0000-000000000001', 'Demo T-Shirt', '10000000-0000-0000-0000-000000000001', now(), now()),
('20000000-0000-0000-0000-000000000002', 'Demo Mug', '10000000-0000-0000-0000-000000000001', now(), now())
ON CONFLICT (id) DO NOTHING;

INSERT INTO product_variants (id, variant_name, price, description, product_id, created_at, updated_at) VALUES
('30000000-0000-0000-0000-000000000001', 'T-Shirt Black M', 100000, 'Demo variant with available stock', '20000000-0000-0000-0000-000000000001', now(), now()),
('30000000-0000-0000-0000-000000000002', 'T-Shirt White L', 120000, 'Demo variant with low stock', '20000000-0000-0000-0000-000000000001', now(), now()),
('30000000-0000-0000-0000-000000000003', 'Mug White', 80000, 'Demo variant with no stock', '20000000-0000-0000-0000-000000000002', now(), now())
ON CONFLICT (id) DO NOTHING;

INSERT INTO inventory (id, name, quantity, product_variant_id, created_at, updated_at) VALUES
('40000000-0000-0000-0000-000000000001', 'Demo T-Shirt Black M stock', 10, '30000000-0000-0000-0000-000000000001', now(), now()),
('40000000-0000-0000-0000-000000000002', 'Demo T-Shirt White L stock', 2, '30000000-0000-0000-0000-000000000002', now(), now()),
('40000000-0000-0000-0000-000000000003', 'Demo Mug White stock', 0, '30000000-0000-0000-0000-000000000003', now(), now())
ON CONFLICT (id) DO NOTHING;

COMMIT;

SELECT pv.id AS variant_id, pv.variant_name, pv.price, i.quantity
FROM product_variants pv JOIN inventory i ON i.product_variant_id = pv.id
WHERE pv.id IN ('30000000-0000-0000-0000-000000000001',
                '30000000-0000-0000-0000-000000000002',
                '30000000-0000-0000-0000-000000000003')
ORDER BY pv.id;
