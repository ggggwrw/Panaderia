package panaderia.controllers;

import lombok.Data;
import panaderia.pedidos.OrderStatus;
import panaderia.pedidos.Pedido;
import panaderia.servicios.OrderService;
import panaderia.servicios.OrderService.OrderItemRequest;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api")
@Tag(name = "Pedidos", description = "API para gestión de pedidos")
public class OrderController {
    
    private final OrderService orderService;
    
    public OrderController(OrderService orderService) {
        this.orderService = orderService;
    }
    
    @Operation(summary = "Crear nuevo pedido",
               description = "Crea un nuevo pedido con los productos especificados. Valida stock disponible y calcula el total.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "201", description = "Pedido creado exitosamente",
                    content = @Content(mediaType = "application/json",
                            schema = @Schema(implementation = Pedido.class))),
            @ApiResponse(responseCode = "400", description = "Datos inválidos o pedido vacío"),
            @ApiResponse(responseCode = "404", description = "Producto no encontrado"),
            @ApiResponse(responseCode = "409", description = "Stock insuficiente para algún producto")
    })
    @PostMapping("/orders")
    public ResponseEntity<Pedido> createOrder(@Valid @RequestBody CreateOrderRequest request) {
        Pedido pedido = orderService.createOrder(request.getUserId(), request.getItems());
        return new ResponseEntity<>(pedido, HttpStatus.CREATED);
    }
    
    @Operation(summary = "Obtener pedido por ID",
               description = "Obtiene los detalles de un pedido específico por su ID")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Pedido encontrado",
                    content = @Content(mediaType = "application/json",
                            schema = @Schema(implementation = Pedido.class))),
            @ApiResponse(responseCode = "404", description = "Pedido no encontrado")
    })
    @GetMapping("/orders/{id}")
    public ResponseEntity<Pedido> getOrderById(
            @Parameter(description = "ID del pedido") @PathVariable Long id) {
        Pedido pedido = orderService.getOrderById(id);
        return ResponseEntity.ok(pedido);
    }
    
    @Operation(summary = "Listar todos los pedidos",
               description = "Obtiene una lista de todos los pedidos en el sistema")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Lista de pedidos obtenida exitosamente")
    })
    @GetMapping("/orders")
    public ResponseEntity<List<Pedido>> getAllOrders() {
        List<Pedido> pedidos = orderService.getAllOrders();
        return ResponseEntity.ok(pedidos);
    }
    
    @Operation(summary = "Obtener pedidos de un usuario",
               description = "Obtiene el historial de pedidos de un usuario específico")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Lista de pedidos del usuario")
    })
    @GetMapping("/users/{userId}/orders")
    public ResponseEntity<List<Pedido>> getOrdersByUserId(
            @Parameter(description = "ID del usuario") @PathVariable Long userId) {
        List<Pedido> pedidos = orderService.getOrdersByUserId(userId);
        return ResponseEntity.ok(pedidos);
    }
    
    @Operation(summary = "Actualizar estado del pedido",
               description = "Actualiza el estado de un pedido. Al confirmar (CONFIRMED), se descuenta el stock. Al cancelar (CANCELED) un pedido confirmado, se restaura el stock.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Estado actualizado exitosamente",
                    content = @Content(mediaType = "application/json",
                            schema = @Schema(implementation = Pedido.class))),
            @ApiResponse(responseCode = "400", description = "Transición de estado inválida"),
            @ApiResponse(responseCode = "404", description = "Pedido no encontrado"),
            @ApiResponse(responseCode = "409", description = "Stock insuficiente al confirmar")
    })
    @PutMapping("/orders/{id}/status")
    public ResponseEntity<Pedido> updateOrderStatus(
            @Parameter(description = "ID del pedido") @PathVariable Long id,
            @Valid @RequestBody UpdateStatusRequest request) {
        Pedido updatedPedido = orderService.updateOrderStatus(id, request.getStatus());
        return ResponseEntity.ok(updatedPedido);
    }
    
    /**
     * DTO for creating an order
     */
    @Data
    public static class CreateOrderRequest {
        private Long userId;
        
        @NotEmpty(message = "El pedido debe contener al menos un producto")
        private List<OrderItemRequest> items;
    }
    
    /**
     * DTO for updating order status
     */
    @Data
    public static class UpdateStatusRequest {
        @NotNull(message = "El estado es obligatorio")
        private OrderStatus status;
    }
}
