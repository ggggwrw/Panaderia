package panaderia.dto;

import lombok.*;
import panaderia.pedidos.OrderStatus;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class OrderResponseDTO {
    
    private Long id;
    private Long userId;
    private LocalDateTime fechaCreacion;
    private BigDecimal total;
    private OrderStatus status;
    private List<OrderLineResponseDTO> lineas;
    
    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class OrderLineResponseDTO {
        private Long id;
        private Long productId;
        private String productName;
        private int cantidad;
        private BigDecimal precioUnitario;
        private BigDecimal subtotal;
    }
}
