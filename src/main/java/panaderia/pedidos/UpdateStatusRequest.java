package panaderia.pedidos;

import jakarta.validation.constraints.NotNull;

public class UpdateStatusRequest {

    @NotNull(message = "El estado es obligatorio")
    private OrderStatus status;

    public UpdateStatusRequest() {
    }

    public UpdateStatusRequest(OrderStatus status) {
        this.status = status;
    }

    public OrderStatus getStatus() {
        return status;
    }

    public void setStatus(OrderStatus status) {
        this.status = status;
    }
}
