package panaderia.dto;

import jakarta.validation.constraints.NotNull;
import lombok.*;
import panaderia.pedidos.OrderStatus;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class UpdateOrderStatusDTO {
    
    @NotNull(message = "El status es obligatorio")
    private OrderStatus status;
}
