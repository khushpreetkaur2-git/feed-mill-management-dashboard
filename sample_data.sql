INSERT OR IGNORE INTO suppliers(name, phone, city) VALUES
('Haryana Grains Supply', '9990001111', 'Karnal'),
('Shakti Agro Traders', '9990002222', 'Panipat'),
('North India Nutrition', '9990003333', 'Rohtak');

INSERT OR IGNORE INTO raw_materials(name, unit, current_stock, reorder_level, unit_cost) VALUES
('Maize', 'kg', 12000, 5000, 24.50),
('Soybean Meal', 'kg', 6200, 3000, 42.00),
('Rice Bran', 'kg', 2800, 2500, 18.25),
('Mineral Mix', 'kg', 900, 1000, 78.00),
('Limestone Powder', 'kg', 1600, 800, 8.50);

INSERT INTO purchases(material_id, supplier_id, quantity, unit_cost, purchase_date)
SELECT material_id, 1, 5000, 24.50, '2026-07-05' FROM raw_materials WHERE name='Maize';
INSERT INTO purchases(material_id, supplier_id, quantity, unit_cost, purchase_date)
SELECT material_id, 2, 2500, 42.00, '2026-07-10' FROM raw_materials WHERE name='Soybean Meal';
INSERT INTO purchases(material_id, supplier_id, quantity, unit_cost, purchase_date)
SELECT material_id, 3, 1000, 78.00, '2026-07-15' FROM raw_materials WHERE name='Mineral Mix';

INSERT INTO production_batches(feed_type, quantity_produced, production_date, estimated_cost, status) VALUES
('Broiler Starter', 3500, '2026-07-18', 118000, 'Completed'),
('Broiler Finisher', 4200, '2026-07-25', 139500, 'Completed'),
('Layer Feed', 3000, '2026-08-03', 94500, 'Completed'),
('Broiler Starter', 3900, '2026-08-14', 130000, 'Completed');

INSERT INTO sales(customer_name, feed_type, quantity, unit_price, sale_date) VALUES
('Green Farm Poultry', 'Broiler Starter', 1200, 41.00, '2026-07-20'),
('Kisan Poultry House', 'Broiler Finisher', 1800, 43.50, '2026-07-28'),
('Sunrise Layers', 'Layer Feed', 1300, 40.00, '2026-08-06'),
('Green Farm Poultry', 'Broiler Starter', 1600, 42.00, '2026-08-17');
