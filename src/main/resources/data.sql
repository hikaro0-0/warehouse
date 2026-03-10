INSERT INTO warehouses (name, address) VALUES ('North Hub', '12 Industrial Ave');
INSERT INTO warehouses (name, address) VALUES ('South Hub', '7 Logistics Park');

INSERT INTO suppliers (name, contact_email) VALUES ('TechSource', 'sales@techsource.example');
INSERT INTO suppliers (name, contact_email) VALUES ('OfficeLine', 'orders@officeline.example');

INSERT INTO categories (name, description) VALUES ('Electronics', 'Devices and peripherals');
INSERT INTO categories (name, description) VALUES ('Office', 'Office inventory');
INSERT INTO categories (name, description) VALUES ('Premium', 'High margin products');

INSERT INTO products (sku, name, quantity, warehouse_id, supplier_id)
VALUES ('SKU-100', 'Laptop', 10, 1, 1);
INSERT INTO products (sku, name, quantity, warehouse_id, supplier_id)
VALUES ('SKU-101', 'Keyboard', 50, 1, 2);
INSERT INTO products (sku, name, quantity, warehouse_id, supplier_id)
VALUES ('SKU-102', 'Mouse', 75, 2, 2);
INSERT INTO products (sku, name, quantity, warehouse_id, supplier_id)
VALUES ('SKU-103', 'Monitor', 15, 2, 1);

INSERT INTO product_categories (product_id, category_id)
SELECT p.id, c.id FROM products p, categories c WHERE p.sku = 'SKU-100' AND c.name = 'Electronics';
INSERT INTO product_categories (product_id, category_id)
SELECT p.id, c.id FROM products p, categories c WHERE p.sku = 'SKU-100' AND c.name = 'Premium';
INSERT INTO product_categories (product_id, category_id)
SELECT p.id, c.id FROM products p, categories c WHERE p.sku = 'SKU-101' AND c.name = 'Electronics';
INSERT INTO product_categories (product_id, category_id)
SELECT p.id, c.id FROM products p, categories c WHERE p.sku = 'SKU-101' AND c.name = 'Office';
INSERT INTO product_categories (product_id, category_id)
SELECT p.id, c.id FROM products p, categories c WHERE p.sku = 'SKU-102' AND c.name = 'Electronics';
INSERT INTO product_categories (product_id, category_id)
SELECT p.id, c.id FROM products p, categories c WHERE p.sku = 'SKU-103' AND c.name = 'Electronics';
INSERT INTO product_categories (product_id, category_id)
SELECT p.id, c.id FROM products p, categories c WHERE p.sku = 'SKU-103' AND c.name = 'Premium';

INSERT INTO shipments (reference_number, supplier_id) VALUES ('SHIP-001', 1);
INSERT INTO shipments (reference_number, supplier_id) VALUES ('SHIP-002', 2);

INSERT INTO shipment_products (shipment_id, product_id)
SELECT s.id, p.id FROM shipments s, products p WHERE s.reference_number = 'SHIP-001' AND p.sku = 'SKU-100';
INSERT INTO shipment_products (shipment_id, product_id)
SELECT s.id, p.id FROM shipments s, products p WHERE s.reference_number = 'SHIP-001' AND p.sku = 'SKU-103';
INSERT INTO shipment_products (shipment_id, product_id)
SELECT s.id, p.id FROM shipments s, products p WHERE s.reference_number = 'SHIP-002' AND p.sku = 'SKU-101';
INSERT INTO shipment_products (shipment_id, product_id)
SELECT s.id, p.id FROM shipments s, products p WHERE s.reference_number = 'SHIP-002' AND p.sku = 'SKU-102';
