package panaderia.servicios;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import panaderia.dto.CreateOrderDTO;
import panaderia.dto.OrderItemDTO;
import panaderia.dto.OrderResponseDTO;
import panaderia.excepciones.BadRequestException;
import panaderia.excepciones.ResourceNotFoundException;
import panaderia.excepciones.StockInsuficienteException;
import panaderia.pedidos.LineaPedido;
import panaderia.pedidos.OrderStatus;
import panaderia.pedidos.Pedido;
import panaderia.productos.Product;
import panaderia.repositories.PedidoRepository;
import panaderia.repositories.ProductRepository;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class OrderService {
    
    private final PedidoRepository pedidoRepository;
    private final ProductRepository productRepository;
    
    @Transactional
    public Pedido createOrder(CreateOrderDTO dto) {
        List<LineaPedido> lineas = new ArrayList<>();
        BigDecimal total = BigDecimal.ZERO;
        
        // Validate all products and stock first
        for (OrderItemDTO item : dto.getItems()) {
            Product product = productRepository.findById(item.getProductId())
                .orElseThrow(() -> new ResourceNotFoundException("Producto", item.getProductId()));
            
            if (item.getCantidad() > product.getStock()) {
                throw new StockInsuficienteException(product.getNombre(), item.getCantidad(), product.getStock());
            }
            
            LineaPedido linea = LineaPedido.builder()
                .productId(product.getId())
                .cantidad(item.getCantidad())
                .precioUnitario(product.getPrecio())
                .build();
            
            lineas.add(linea);
            total = total.add(linea.getSubtotal());
        }
        
        Pedido pedido = Pedido.builder()
            .userId(dto.getUserId())
            .fechaCreacion(LocalDateTime.now())
            .status(OrderStatus.PENDING)
            .total(total)
            .lineas(new ArrayList<>())
            .build();
        
        for (LineaPedido linea : lineas) {
            pedido.addLinea(linea);
        }
        
        return pedidoRepository.save(pedido);
    }
    
    @Transactional
    public Pedido updateStatus(Long orderId, OrderStatus newStatus) {
        Pedido pedido = pedidoRepository.findById(orderId)
            .orElseThrow(() -> new ResourceNotFoundException("Pedido", orderId));
        
        OrderStatus currentStatus = pedido.getStatus();
        
        // Validate status transitions
        validateStatusTransition(currentStatus, newStatus);
        
        // Handle stock changes based on status transition
        if (newStatus == OrderStatus.CONFIRMED && currentStatus == OrderStatus.PENDING) {
            // Decrease stock when confirming
            decrementStock(pedido);
        } else if (newStatus == OrderStatus.CANCELED && 
                   (currentStatus == OrderStatus.CONFIRMED || currentStatus == OrderStatus.SHIPPED)) {
            // Restore stock when canceling a confirmed or shipped order
            restoreStock(pedido);
        }
        
        pedido.setStatus(newStatus);
        return pedidoRepository.save(pedido);
    }
    
    private void validateStatusTransition(OrderStatus current, OrderStatus newStatus) {
        // Define valid transitions
        switch (current) {
            case PENDING:
                if (newStatus != OrderStatus.CONFIRMED && newStatus != OrderStatus.CANCELED) {
                    throw new BadRequestException("Un pedido PENDING solo puede pasar a CONFIRMED o CANCELED");
                }
                break;
            case CONFIRMED:
                if (newStatus != OrderStatus.SHIPPED && newStatus != OrderStatus.CANCELED) {
                    throw new BadRequestException("Un pedido CONFIRMED solo puede pasar a SHIPPED o CANCELED");
                }
                break;
            case SHIPPED:
                if (newStatus != OrderStatus.DELIVERED && newStatus != OrderStatus.CANCELED) {
                    throw new BadRequestException("Un pedido SHIPPED solo puede pasar a DELIVERED o CANCELED");
                }
                break;
            case DELIVERED:
            case CANCELED:
                throw new BadRequestException("Un pedido " + current + " no puede cambiar de estado");
        }
    }
    
    private void decrementStock(Pedido pedido) {
        for (LineaPedido linea : pedido.getLineas()) {
            Product product = productRepository.findById(linea.getProductId())
                .orElseThrow(() -> new ResourceNotFoundException("Producto", linea.getProductId()));
            
            if (linea.getCantidad() > product.getStock()) {
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
    
    @Transactional(readOnly = true)
    public Pedido findById(Long id) {
        return pedidoRepository.findById(id)
            .orElseThrow(() -> new ResourceNotFoundException("Pedido", id));
    }
    
    @Transactional(readOnly = true)
    public List<Pedido> findByUserId(Long userId) {
        return pedidoRepository.findByUserIdOrderByFechaCreacionDesc(userId);
    }
    
    @Transactional(readOnly = true)
    public OrderResponseDTO toResponseDTO(Pedido pedido) {
        List<OrderResponseDTO.OrderLineResponseDTO> lineaDTOs = pedido.getLineas().stream()
            .map(linea -> {
                String productName = productRepository.findById(linea.getProductId())
                    .map(Product::getNombre)
                    .orElse("Producto eliminado");
                
                return OrderResponseDTO.OrderLineResponseDTO.builder()
                    .id(linea.getId())
                    .productId(linea.getProductId())
                    .productName(productName)
                    .cantidad(linea.getCantidad())
                    .precioUnitario(linea.getPrecioUnitario())
                    .subtotal(linea.getSubtotal())
                    .build();
            })
            .collect(Collectors.toList());
        
        return OrderResponseDTO.builder()
            .id(pedido.getId())
            .userId(pedido.getUserId())
            .fechaCreacion(pedido.getFechaCreacion())
            .total(pedido.getTotal())
            .status(pedido.getStatus())
            .lineas(lineaDTOs)
            .build();
    }
}
