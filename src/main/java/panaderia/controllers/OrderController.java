package panaderia.controllers;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import panaderia.pedidos.CreateOrderRequest;
import panaderia.pedidos.Pedido;
import panaderia.pedidos.UpdateStatusRequest;
import panaderia.servicios.OrderService;

import java.util.List;

@RestController
@RequestMapping("/api")
@Tag(name = "Orders", description = "API para gestión de pedidos")
public class OrderController {

    private final OrderService orderService;

    public OrderController(OrderService orderService) {
        this.orderService = orderService;
    }

    @PostMapping("/orders")
    @Operation(summary = "Crear pedido", description = "Crea un nuevo pedido validando existencia de productos y stock")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "201", description = "Pedido creado correctamente"),
            @ApiResponse(responseCode = "400", description = "Datos de pedido inválidos"),
            @ApiResponse(responseCode = "404", description = "Producto no encontrado"),
            @ApiResponse(responseCode = "409", description = "Stock insuficiente")
    })
    public ResponseEntity<Pedido> createOrder(@Valid @RequestBody CreateOrderRequest request) {
        Pedido pedido = orderService.createOrder(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(pedido);
    }

    @PutMapping("/orders/{id}/status")
    @Operation(summary = "Actualizar estado del pedido", description = "Actualiza el estado del pedido. Al confirmar, se decrementa el stock. Al cancelar, se restaura si ya fue decrementado.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Estado actualizado correctamente"),
            @ApiResponse(responseCode = "400", description = "Transición de estado inválida"),
            @ApiResponse(responseCode = "404", description = "Pedido no encontrado"),
            @ApiResponse(responseCode = "409", description = "Stock insuficiente al confirmar")
    })
    public ResponseEntity<Pedido> updateOrderStatus(
            @Parameter(description = "ID del pedido") @PathVariable Long id,
            @Valid @RequestBody UpdateStatusRequest request) {
        Pedido pedido = orderService.updateStatus(id, request.getStatus());
        return ResponseEntity.ok(pedido);
    }

    @GetMapping("/orders/{id}")
    @Operation(summary = "Obtener pedido por ID", description = "Obtiene los detalles de un pedido específico")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Pedido encontrado"),
            @ApiResponse(responseCode = "404", description = "Pedido no encontrado")
    })
    public ResponseEntity<Pedido> getOrderById(
            @Parameter(description = "ID del pedido") @PathVariable Long id) {
        Pedido pedido = orderService.findById(id);
        return ResponseEntity.ok(pedido);
    }

    @GetMapping("/users/{userId}/orders")
    @Operation(summary = "Obtener pedidos por usuario", description = "Obtiene el historial de pedidos de un usuario")
    @ApiResponse(responseCode = "200", description = "Lista de pedidos del usuario")
    public ResponseEntity<List<Pedido>> getOrdersByUserId(
            @Parameter(description = "ID del usuario") @PathVariable Long userId) {
        List<Pedido> pedidos = orderService.findByUserId(userId);
        return ResponseEntity.ok(pedidos);
    }
}
