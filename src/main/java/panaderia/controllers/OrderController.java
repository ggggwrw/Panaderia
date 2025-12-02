package panaderia.controllers;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import panaderia.dto.CreateOrderDTO;
import panaderia.dto.OrderResponseDTO;
import panaderia.dto.UpdateOrderStatusDTO;
import panaderia.pedidos.Pedido;
import panaderia.servicios.OrderService;

import java.util.List;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api")
@RequiredArgsConstructor
@Tag(name = "Orders", description = "API para gestión de pedidos")
public class OrderController {
    
    private final OrderService orderService;
    
    @PostMapping("/orders")
    @Operation(summary = "Crear un nuevo pedido")
    public ResponseEntity<OrderResponseDTO> createOrder(@Valid @RequestBody CreateOrderDTO dto) {
        Pedido pedido = orderService.createOrder(dto);
        return new ResponseEntity<>(orderService.toResponseDTO(pedido), HttpStatus.CREATED);
    }
    
    @PutMapping("/orders/{id}/status")
    @Operation(summary = "Actualizar el estado de un pedido")
    public ResponseEntity<OrderResponseDTO> updateOrderStatus(
            @PathVariable Long id,
            @Valid @RequestBody UpdateOrderStatusDTO dto) {
        Pedido pedido = orderService.updateStatus(id, dto.getStatus());
        return ResponseEntity.ok(orderService.toResponseDTO(pedido));
    }
    
    @GetMapping("/orders/{id}")
    @Operation(summary = "Obtener un pedido por ID")
    public ResponseEntity<OrderResponseDTO> getOrderById(@PathVariable Long id) {
        Pedido pedido = orderService.findById(id);
        return ResponseEntity.ok(orderService.toResponseDTO(pedido));
    }
    
    @GetMapping("/users/{userId}/orders")
    @Operation(summary = "Obtener historial de pedidos de un usuario")
    public ResponseEntity<List<OrderResponseDTO>> getOrdersByUserId(@PathVariable Long userId) {
        List<Pedido> pedidos = orderService.findByUserId(userId);
        List<OrderResponseDTO> response = pedidos.stream()
            .map(orderService::toResponseDTO)
            .collect(Collectors.toList());
        return ResponseEntity.ok(response);
    }
}
