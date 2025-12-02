package panaderia.excepciones;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

@ResponseStatus(HttpStatus.CONFLICT)
public class StockInsuficienteException extends RuntimeException {

    public StockInsuficienteException(String message) {
        super(message);
    }

    public StockInsuficienteException(String productName, int requested, int available) {
        super(String.format("Stock insuficiente para '%s': solicitado %d, disponible %d", 
                productName, requested, available));
    }
}
