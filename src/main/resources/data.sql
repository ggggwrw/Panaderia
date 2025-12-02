-- Sample data for Panaderia API
-- This script inserts initial sample products for testing

-- Insert sample products
INSERT INTO products (nombre, descripcion, precio, categoria, image_url, stock) VALUES
    ('Pan de Campo', 'Pan artesanal tradicional, horneado en horno de leña', 3.50, 'Panes', 'https://example.com/images/pan-campo.jpg', 50),
    ('Croissant', 'Croissant de mantequilla francés, crujiente por fuera y suave por dentro', 1.25, 'Bollería', 'https://example.com/images/croissant.jpg', 120),
    ('Tarta de Frutas', 'Tarta con base de hojaldre y frutas frescas de temporada', 15.00, 'Pasteles', 'https://example.com/images/tarta-frutas.jpg', 10),
    ('Baguette', 'Baguette tradicional francesa, perfecta para bocadillos', 2.00, 'Panes', 'https://example.com/images/baguette.jpg', 80),
    ('Donut de Chocolate', 'Donut glaseado con cobertura de chocolate belga', 1.75, 'Bollería', 'https://example.com/images/donut-chocolate.jpg', 60),
    ('Pan de Centeno', 'Pan integral de centeno, rico en fibra', 4.00, 'Panes', 'https://example.com/images/pan-centeno.jpg', 30),
    ('Empanada de Carne', 'Empanada casera rellena de carne especiada', 3.00, 'Salados', 'https://example.com/images/empanada-carne.jpg', 45),
    ('Palmera de Hojaldre', 'Palmera de hojaldre caramelizada', 1.50, 'Bollería', 'https://example.com/images/palmera.jpg', 90),
    ('Tarta de Queso', 'Tarta de queso cremosa al estilo New York', 18.00, 'Pasteles', 'https://example.com/images/tarta-queso.jpg', 8),
    ('Pan de Molde Integral', 'Pan de molde integral, ideal para tostadas', 2.50, 'Panes', 'https://example.com/images/pan-molde.jpg', 40);
