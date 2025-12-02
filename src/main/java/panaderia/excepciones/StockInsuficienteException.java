package panaderia.excepciones;

public class StockInsuficienteException extends RuntimeException {
    
    public StockInsuficienteException(String message) {
        super(message);
    }
    
    public StockInsuficienteException(String productName, int requested, int available) {
        super(String.format("Stock insuficiente para '%s': solicitado %d, disponible %d", 
                productName, requested, available));
    }
}
