-- Sample data for Panaderia API

-- Products
INSERT INTO product (nombre, descripcion, precio, categoria, image_url, stock, version) VALUES
('Pan de Campo', 'Pan artesanal tradicional argentino, ideal para el mate', 3.50, 'Panes', NULL, 50, 0),
('Croissant', 'Croissant de mantequilla francés, crujiente y hojaldrado', 1.25, 'Bollería', NULL, 120, 0),
('Tarta de Frutas', 'Tarta con frutas frescas de temporada y crema pastelera', 15.00, 'Tartas', NULL, 10, 0),
('Baguette', 'Pan francés tradicional con corteza crujiente', 2.50, 'Panes', NULL, 30, 0),
('Medialuna', 'Medialuna dulce argentina, tierna y esponjosa', 0.80, 'Bollería', NULL, 100, 0),
('Pan Integral', 'Pan elaborado con harina integral, rico en fibra', 4.00, 'Panes', NULL, 25, 0),
('Empanada de Carne', 'Empanada argentina rellena de carne cortada a cuchillo', 2.00, 'Empanadas', NULL, 40, 0),
('Torta de Chocolate', 'Torta de chocolate belga con ganache', 25.00, 'Tortas', NULL, 5, 0);
