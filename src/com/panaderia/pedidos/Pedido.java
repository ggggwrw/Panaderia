package com.panaderia.pedidos;

import java.util.ArrayList;
import java.util.List;

public class Pedido {
    private static int contadorId = 0;
    private int id;
    private List<LineaPedido> lineas;

    public Pedido() {
        this.id = ++contadorId;
        this.lineas = new ArrayList<>();
    }

    public void agregarLinea(LineaPedido linea) {
        this.lineas.add(linea);
    }

    public double calcularCostoTotal() {
        double total = 0;
        for (LineaPedido linea : lineas) {
            total += linea.calcularSubtotal();
        }
        return total;
    }

    // Getters
    public int getId() { return id; }
    public List<LineaPedido> getLineas() { return lineas; }

    @Override
    public String toString() {
        String info = "\n=== Pedido ID: " + id + " ===\n";
        info += "Productos:\n";
        for (LineaPedido linea : lineas) {
            info += linea.toString() + "\n";
        }
        info += "Costo Total: $" + String.format("%.2f", calcularCostoTotal()) + "\n";
        return info;
    }
}