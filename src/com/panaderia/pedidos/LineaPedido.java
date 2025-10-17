package com.panaderia.pedidos;

import com.panaderia.productos.Producto;

public class LineaPedido {
    private Producto producto;
    private int cantidad;

    public LineaPedido(Producto producto, int cantidad) {
        this.producto = producto;
        this.cantidad = cantidad;
    }

    public Producto getProducto() { return producto; }
    public int getCantidad() { return cantidad; }

    public double calcularSubtotal() {
        return producto.getPrecio() * cantidad;
    }

    @Override
    public String toString() {
        return "  - " + producto.getNombre() + " (ID: " + producto.getId() + ") x " + cantidad +
                " unidades. Subtotal: $" + String.format("%.2f", calcularSubtotal());
    }
}