Gestión de Inventario
Agregar Producto (Opción 1): Permite ingresar nombre, precio y stock del nuevo producto.

Listar Productos (Opción 2): Muestra todos los productos registrados en el ProductoService.

Buscar/Actualizar Producto (Opción 3): Permite buscar por ID. Si se encuentra, se da la opción de actualizar el stock, validando que el valor no sea negativo.

Eliminar Producto (Opción 4): Elimina un producto por su ID.

Crear un Pedido (Opción 5):Permite al usuario añadir múltiples productos por su ID y cantidad.
Validación de Stock: Utiliza un operador relacional (cantidad > stock) y lanza la excepción StockInsuficienteException si el pedido excede el inventario.
Disminución de Stock: Si la línea de pedido es válida, utiliza un operador aritmético para restar la cantidad solicitada del stock disponible en ProductoService.
Costo Total: El objeto Pedido calcula el costo total sumando los subtotales de cada LineaPedido.

Listar Pedidos (Opción 6): Muestra el historial de pedidos realizados, incluyendo el costo total y los productos asociados a cada uno.
