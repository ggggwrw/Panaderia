package panaderia.dto;

import jakarta.validation.constraints.*;
import lombok.*;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class OrderItemDTO {
    
    @NotNull(message = "El productId es obligatorio")
    private Long productId;
    
    @Min(value = 1, message = "La cantidad debe ser al menos 1")
    private int cantidad;
}
