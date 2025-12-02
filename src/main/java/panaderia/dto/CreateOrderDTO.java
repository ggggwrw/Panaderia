package panaderia.dto;

import jakarta.validation.Valid;
import jakarta.validation.constraints.*;
import lombok.*;
import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class CreateOrderDTO {
    
    private Long userId;
    
    @NotEmpty(message = "El pedido debe contener al menos un item")
    @Valid
    private List<OrderItemDTO> items;
}
