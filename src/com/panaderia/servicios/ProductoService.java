package com.panaderia.servicios;

import com.panaderia.productos.Producto;
import java.util.ArrayList;
import java.util.List;

public class ProductoService {
    private List<Producto> inventario;

    public ProductoService() {
        this.inventario = new ArrayList<>();
        inventario.add(new Producto("Pan de Campo", 3.50, 50));
        inventario.add(new Producto("Croissant", 1.25, 120));
        inventario.add(new Producto("Tarta de Frutas", 15.00, 10));
    }

    public void agregarProducto(Producto producto) {
        inventario.add(producto);
    }

    public List<Producto> listarProductos() {
        return inventario;
    }

    public Producto buscarProductoPorId(int id) {
        for (Producto p : inventario) {
            if (p.getId() == id) {
                return p;
            }
        }
        return null;
    }

    public boolean eliminarProducto(int id) {
        Producto p = buscarProductoPorId(id);
        if (p != null) {
            inventario.remove(p);
            return true;
        }
        return false;
    }

    public boolean actualizarStock(int idProducto, int cantidad) {
        Producto p = buscarProductoPorId(idProducto);
        if (p != null) {
            if (p.getStock() + cantidad >= 0) {
                p.setStock(p.getStock() + cantidad);
                return true;
            }
        }
        return false;
    }
}