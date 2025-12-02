package panaderia.servicios;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import panaderia.excepciones.BadRequestException;
import panaderia.excepciones.ResourceNotFoundException;
import panaderia.excepciones.StockInsuficienteException;
import panaderia.pedidos.*;
import panaderia.productos.Product;
import panaderia.productos.ProductRepository;

import java.math.BigDecimal;
import java.util.List;

@Service
@Transactional
public class OrderService {

    private final PedidoRepository pedidoRepository;
    private final ProductRepository productRepository;

    public OrderService(PedidoRepository pedidoRepository, ProductRepository productRepository) {
        this.pedidoRepository = pedidoRepository;
        this.productRepository = productRepository;
    }

    public Pedido createOrder(CreateOrderRequest request) {
        if (request.getItems() == null || request.getItems().isEmpty()) {
            throw new BadRequestException("El pedido debe contener al menos un producto");
        }

        Pedido pedido = new Pedido();
        pedido.setUserId(request.getUserId());

        // Validate all products and stock before creating order lines
        for (OrderItemRequest item : request.getItems()) {
            Product product = productRepository.findById(item.getProductId())
                    .orElseThrow(() -> new ResourceNotFoundException("Producto", item.getProductId()));

            if (item.getCantidad() <= 0) {
                throw new BadRequestException("La cantidad debe ser mayor a 0 para el producto: " + product.getNombre());
            }

            if (product.getStock() < item.getCantidad()) {
                throw new StockInsuficienteException(product.getNombre(), item.getCantidad(), product.getStock());
            }

            LineaPedido linea = new LineaPedido(
                    product.getId(),
                    item.getCantidad(),
                    product.getPrecio()
            );
            pedido.agregarLinea(linea);
        }

        pedido.calcularTotal();
        return pedidoRepository.save(pedido);
    }

    public Pedido updateStatus(Long orderId, OrderStatus newStatus) {
        Pedido pedido = findById(orderId);
        OrderStatus currentStatus = pedido.getStatus();

        // Validate status transitions
        validateStatusTransition(currentStatus, newStatus);

        // Handle stock changes based on status transitions
        if (newStatus == OrderStatus.CONFIRMED && currentStatus == OrderStatus.PENDING) {
            // Decrement stock when confirming
            decrementStock(pedido);
        } else if (newStatus == OrderStatus.CANCELED) {
            // Restore stock if it was already decremented (confirmed or later)
            if (currentStatus == OrderStatus.CONFIRMED || 
                currentStatus == OrderStatus.SHIPPED) {
                restoreStock(pedido);
            }
        }

        pedido.setStatus(newStatus);
        return pedidoRepository.save(pedido);
    }

    private void validateStatusTransition(OrderStatus current, OrderStatus newStatus) {
        // Define valid transitions
        boolean isValid = switch (current) {
            case PENDING -> newStatus == OrderStatus.CONFIRMED || newStatus == OrderStatus.CANCELED;
            case CONFIRMED -> newStatus == OrderStatus.SHIPPED || newStatus == OrderStatus.CANCELED;
            case SHIPPED -> newStatus == OrderStatus.DELIVERED || newStatus == OrderStatus.CANCELED;
            case DELIVERED, CANCELED -> false; // Terminal states
        };

        if (!isValid) {
            throw new BadRequestException(
                    String.format("No se puede cambiar el estado de %s a %s", current, newStatus));
        }
    }

    private void decrementStock(Pedido pedido) {
        for (LineaPedido linea : pedido.getLineas()) {
            Product product = productRepository.findById(linea.getProductId())
                    .orElseThrow(() -> new ResourceNotFoundException("Producto", linea.getProductId()));

            // Check stock again in case of concurrent modifications
            if (product.getStock() < linea.getCantidad()) {
                throw new StockInsuficienteException(product.getNombre(), linea.getCantidad(), product.getStock());
            }

            product.setStock(product.getStock() - linea.getCantidad());
            productRepository.save(product);
        }
    }

    private void restoreStock(Pedido pedido) {
        for (LineaPedido linea : pedido.getLineas()) {
            Product product = productRepository.findById(linea.getProductId())
                    .orElseThrow(() -> new ResourceNotFoundException("Producto", linea.getProductId()));

            product.setStock(product.getStock() + linea.getCantidad());
            productRepository.save(product);
        }
    }

    public Pedido findById(Long id) {
        return pedidoRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Pedido", id));
    }

    public List<Pedido> findByUserId(Long userId) {
        return pedidoRepository.findByUserId(userId);
    }

    public List<Pedido> findAll() {
        return pedidoRepository.findAll();
    }
}
