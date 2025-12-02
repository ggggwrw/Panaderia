-- Schema for Panaderia API
-- This script creates the necessary tables for the application

-- Products table
CREATE TABLE IF NOT EXISTS products (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    nombre VARCHAR(255) NOT NULL,
    descripcion VARCHAR(500),
    precio DECIMAL(10, 2) NOT NULL,
    categoria VARCHAR(100),
    image_url VARCHAR(500),
    stock INT NOT NULL DEFAULT 0,
    version BIGINT DEFAULT 0,
    CONSTRAINT chk_precio_positive CHECK (precio > 0),
    CONSTRAINT chk_stock_non_negative CHECK (stock >= 0)
);

-- Orders table
CREATE TABLE IF NOT EXISTS orders (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    user_id BIGINT,
    fecha_creacion TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    total DECIMAL(10, 2) NOT NULL DEFAULT 0,
    status VARCHAR(20) NOT NULL DEFAULT 'PENDING',
    version BIGINT DEFAULT 0,
    CONSTRAINT chk_status_valid CHECK (status IN ('PENDING', 'CONFIRMED', 'SHIPPED', 'DELIVERED', 'CANCELED'))
);

-- Order lines table
CREATE TABLE IF NOT EXISTS order_lines (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    pedido_id BIGINT NOT NULL,
    product_id BIGINT NOT NULL,
    product_name VARCHAR(255),
    cantidad INT NOT NULL,
    precio_unitario DECIMAL(10, 2) NOT NULL,
    CONSTRAINT fk_order_line_order FOREIGN KEY (pedido_id) REFERENCES orders(id) ON DELETE CASCADE,
    CONSTRAINT chk_cantidad_positive CHECK (cantidad > 0),
    CONSTRAINT chk_precio_unitario_positive CHECK (precio_unitario > 0)
);

-- Indexes for better query performance
CREATE INDEX IF NOT EXISTS idx_products_nombre ON products(nombre);
CREATE INDEX IF NOT EXISTS idx_products_categoria ON products(categoria);
CREATE INDEX IF NOT EXISTS idx_orders_user_id ON orders(user_id);
CREATE INDEX IF NOT EXISTS idx_orders_status ON orders(status);
CREATE INDEX IF NOT EXISTS idx_order_lines_pedido_id ON order_lines(pedido_id);
CREATE INDEX IF NOT EXISTS idx_order_lines_product_id ON order_lines(product_id);
