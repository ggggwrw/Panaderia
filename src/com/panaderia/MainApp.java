package com.panaderia;

import com.panaderia.excepciones.StockInsuficienteException;
import com.panaderia.pedidos.LineaPedido;
import com.panaderia.pedidos.Pedido;
import com.panaderia.productos.Producto;
import com.panaderia.servicios.PedidoService;
import com.panaderia.servicios.ProductoService;
import java.util.InputMismatchException;
import java.util.Scanner;

public class MainApp {

    private static final Scanner scanner = new Scanner(System.in);
    private static final ProductoService productoService = new ProductoService();
    private static final PedidoService pedidoService = new PedidoService();

    public static void main(String[] args) {
        int opcion;
        boolean salir = false;

        while (!salir) {
            mostrarMenu();
            try {
                opcion = leerOpcion();
                scanner.nextLine();

                switch (opcion) {
                    case 1: agregarProducto(); break;
                    case 2: listarProductos(); break;
                    case 3: buscarOActualizarProducto(); break;
                    case 4: eliminarProducto(); break;
                    case 5: crearPedido(); break;
                    case 6: listarPedidos(); break;
                    case 7: salir = true; System.out.println("¡Adiós! Gracias por usar el Sistema de Panadería. 🥐"); break;
                    default: System.out.println("Opción no válida. Intente de nuevo.");
                }
            } catch (InputMismatchException e) {
                System.out.println("❌ Error: Ingrese un número para la opción del menú.");
                scanner.nextLine();
            } catch (Exception e) {
                System.out.println("❌ Ocurrió un error inesperado: " + e.getMessage());
            }
            if (!salir) {
                System.out.println("\nPresione ENTER para continuar...");
                scanner.nextLine();
            }
        }
        scanner.close();
    }
    private static void mostrarMenu() {
        System.out.println("\n==================================");
        System.out.println(" SISTEMA DE GESTIÓN - PANADERÍA");
        System.out.println("==================================");
        System.out.println("1) Agregar producto");
        System.out.println("2) Listar productos");
        System.out.println("3) Buscar/Actualizar producto");
        System.out.println("4) Eliminar producto");
        System.out.println("5) Crear un pedido");
        System.out.println("6) Listar pedidos (Opcional)");
        System.out.println("7) Salir");
        System.out.print("Elija una opción: ");
    }

    private static int leerOpcion() throws InputMismatchException {
        return scanner.nextInt();
    }

// ---------------------- 1. Agregar Producto ----------------------

    private static void agregarProducto() {
        System.out.println("\n--- Agregar Producto de Panadería ---");
        try {
            System.out.print("Nombre: ");
            String nombre = scanner.nextLine();

            System.out.print("Precio (double): ");
            double precio = Double.parseDouble(scanner.nextLine());

            System.out.print("Stock (int): ");
            int stock = Integer.parseInt(scanner.nextLine());

            if (precio <= 0 || stock < 0) {
                System.out.println("❌ Error de validación: El precio debe ser positivo y el stock no negativo.");
                return;
            }

            Producto nuevoProducto = new Producto(nombre, precio, stock);
            productoService.agregarProducto(nuevoProducto);
            System.out.println("✅ Producto agregado con ID: " + nuevoProducto.getId());

        } catch (NumberFormatException e) {
            // Manejo de excepción de conversión (try/catch)
            System.out.println("❌ Error: Asegúrese de ingresar valores numéricos válidos para Precio y Stock.");
        }
    }

// ---------------------- 2. Listar Productos ----------------------

    private static void listarProductos() {
        System.out.println("\n--- Listado de Productos en Inventario ---");
        if (productoService.listarProductos().isEmpty()) {
            System.out.println("¡Inventario vacío! No hay productos registrados.");
        } else {
            for (Producto p : productoService.listarProductos()) {
                System.out.println(p);
            }
        }
    }

// ---------------------- 3. Buscar/Actualizar Producto ----------------------

    private static void buscarOActualizarProducto() {
        System.out.println("\n--- Buscar/Actualizar Producto ---");
        try {
            System.out.print("Ingrese ID del producto a buscar: ");
            int id = Integer.parseInt(scanner.nextLine());

            Producto producto = productoService.buscarProductoPorId(id);

            if (producto == null) {
                System.out.println("❌ Producto no encontrado.");
                return;
            }

            System.out.println("\nProducto encontrado:");
            System.out.println(producto);

            System.out.print("\n¿Desea actualizar el Stock (S/N)? ");
            String respuesta = scanner.nextLine().toUpperCase();

            if (respuesta.equals("S")) {
                System.out.print("Nuevo Stock (int): ");
                int nuevoStock = Integer.parseInt(scanner.nextLine());

                if (nuevoStock < 0) {
                    System.out.println("❌ Error de validación: El stock no puede ser negativo.");
                    return;
                }

                int diferencia = nuevoStock - producto.getStock();

                if (productoService.actualizarStock(producto.getId(), diferencia)) {
                    System.out.println("✅ Stock actualizado correctamente. Nuevo stock: " + producto.getStock());
                } else {
                    System.out.println("❌ Error al actualizar el stock.");
                }
            }

        } catch (NumberFormatException e) {
            System.out.println("❌ Error: El ID y el nuevo Stock deben ser números enteros.");
        }
    }

// ---------------------- 4. Eliminar Producto ----------------------

    private static void eliminarProducto() {
        System.out.println("\n--- Eliminar Producto ---");
        try {
            System.out.print("Ingrese ID del producto a eliminar: ");
            int id = Integer.parseInt(scanner.nextLine());

            System.out.print("⚠️ ¿Confirma la eliminación del producto ID " + id + " (S/N)? ");
            String confirmacion = scanner.nextLine().toUpperCase();

            if (confirmacion.equals("S")) {
                if (productoService.eliminarProducto(id)) {
                    System.out.println("✅ Producto eliminado correctamente.");
                } else {
                    System.out.println("❌ Producto con ID " + id + " no encontrado.");
                }
            } else {
                System.out.println("Operación de eliminación cancelada.");
            }

        } catch (NumberFormatException e) {
            System.out.println("❌ Error: El ID debe ser un número entero.");
        }
    }

// ---------------------- 5. Crear un Pedido ----------------------

    private static void crearPedido() {
        System.out.println("\n--- Crear Nuevo Pedido ---");
        Pedido nuevoPedido = new Pedido();
        boolean agregandoProductos = true;

        while (agregandoProductos) {
            listarProductos();
            if (productoService.listarProductos().isEmpty()) {
                System.out.println("No hay productos disponibles para crear un pedido. Agregue productos primero.");
                break;
            }

            try {
                System.out.print("\nIngrese ID del producto (0 para finalizar el pedido): ");
                int id = Integer.parseInt(scanner.nextLine());

                if (id == 0) {
                    agregandoProductos = false;
                    continue;
                }

                Producto producto = productoService.buscarProductoPorId(id);
                if (producto == null) {
                    System.out.println("❌ Producto no encontrado.");
                    continue;
                }

                System.out.print("Cantidad deseada: ");
                int cantidad = Integer.parseInt(scanner.nextLine());

                if (cantidad <= 0) {
                    System.out.println("La cantidad debe ser mayor a 0.");
                    continue;
                }

                if (cantidad > producto.getStock()) {
                    throw new StockInsuficienteException("Stock insuficiente: Solo quedan " + producto.getStock() + " unidades de " + producto.getNombre() + ".");
                }

                LineaPedido linea = new LineaPedido(producto, cantidad);
                nuevoPedido.agregarLinea(linea);

                productoService.actualizarStock(id, -cantidad);
                System.out.println("✅ Producto añadido al pedido. Stock actualizado: " + producto.getStock());

            } catch (NumberFormatException e) {
                System.out.println("❌ Error: Ingrese un ID o Cantidad numérica válida.");
            } catch (StockInsuficienteException e) {
                System.out.println("⚠️ Error al crear pedido: " + e.getMessage());
            }
        }

        if (!nuevoPedido.getLineas().isEmpty()) {
            pedidoService.agregarPedido(nuevoPedido);
            System.out.println("\n----------------------------------");
            System.out.println("✅ Pedido ID " + nuevoPedido.getId() + " CREADO EXITOSAMENTE.");
            System.out.println("Costo Total: $" + String.format("%.2f", nuevoPedido.calcularCostoTotal()));
            System.out.println("----------------------------------");
        } else if (!agregandoProductos) {
            System.out.println("Pedido finalizado sin productos añadidos.");
        }
    }

// ---------------------- 6. Listar Pedidos ----------------------

    private static void listarPedidos() {
        System.out.println("\n--- Historial de Pedidos ---");
        if (pedidoService.listarPedidos().isEmpty()) {
            System.out.println("Aún no hay pedidos registrados.");
        } else {
            for (Pedido p : pedidoService.listarPedidos()) {
                System.out.println(p);
            }
        }
    }
}