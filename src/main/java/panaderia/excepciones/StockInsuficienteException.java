package panaderia.excepciones;

import lombok.Getter;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

@Getter
@ResponseStatus(HttpStatus.CONFLICT)
public class StockInsuficienteException extends RuntimeException {
    
    private final Long productId;
    private final String productName;
    private final Integer requestedQuantity;
    private final Integer availableStock;
    
    public StockInsuficienteException(String message) {
        super(message);
        this.productId = null;
        this.productName = null;
        this.requestedQuantity = null;
        this.availableStock = null;
    }
    
    public StockInsuficienteException(Long productId, String productName, Integer requestedQuantity, Integer availableStock) {
        super(String.format("Stock insuficiente para producto '%s' (ID: %d). Solicitado: %d, Disponible: %d",
                productName, productId, requestedQuantity, availableStock));
        this.productId = productId;
        this.productName = productName;
        this.requestedQuantity = requestedQuantity;
        this.availableStock = availableStock;
    }
}
