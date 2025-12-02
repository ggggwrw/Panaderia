package com.techlab.panaderia.servicios;

import com.techlab.panaderia.excepciones.BadRequestException;
import com.techlab.panaderia.excepciones.ResourceNotFoundException;
import com.techlab.panaderia.excepciones.StockInsuficienteException;
import com.techlab.panaderia.pedidos.*;
import com.techlab.panaderia.productos.Product;
import com.techlab.panaderia.productos.ProductRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

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
    
    /**
     * Create a new order with the given items
     * Validates stock availability and calculates total
     */
    public Pedido createOrder(Long userId, List<OrderItemRequest> items) {
        if (items == null || items.isEmpty()) {
            throw new BadRequestException("El pedido debe contener al menos un producto");
        }
        
        Pedido pedido = new Pedido(userId);
        
        // Validate all products exist and have sufficient stock
        for (OrderItemRequest item : items) {
            Product product = productRepository.findById(item.getProductId())
                    .orElseThrow(() -> new ResourceNotFoundException("Producto", "id", item.getProductId()));
            
            if (item.getCantidad() <= 0) {
                throw new BadRequestException("La cantidad debe ser mayor que 0 para el producto: " + product.getNombre());
            }
            
            if (product.getStock() < item.getCantidad()) {
                throw new StockInsuficienteException(
                        product.getId(),
                        product.getNombre(),
                        item.getCantidad(),
                        product.getStock()
                );
            }
            
            // Create order line
            LineaPedido linea = new LineaPedido(
                    product.getId(),
                    product.getNombre(),
                    item.getCantidad(),
                    product.getPrecio()
            );
            pedido.addLinea(linea);
        }
        
        // Calculate total
        pedido.calculateTotal();
        
        // Save the order
        return pedidoRepository.save(pedido);
    }
    
    /**
     * Get order by ID
     */
    @Transactional(readOnly = true)
    public Pedido getOrderById(Long id) {
        return pedidoRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Pedido", "id", id));
    }
    
    /**
     * Get all orders for a user
     */
    @Transactional(readOnly = true)
    public List<Pedido> getOrdersByUserId(Long userId) {
        return pedidoRepository.findByUserId(userId);
    }
    
    /**
     * Get all orders
     */
    @Transactional(readOnly = true)
    public List<Pedido> getAllOrders() {
        return pedidoRepository.findAll();
    }
    
    /**
     * Update order status with stock management
     */
    public Pedido updateOrderStatus(Long orderId, OrderStatus newStatus) {
        Pedido pedido = getOrderById(orderId);
        OrderStatus currentStatus = pedido.getStatus();
        
        // Validate status transition
        validateStatusTransition(currentStatus, newStatus);
        
        // Handle stock changes based on status transition
        if (newStatus == OrderStatus.CONFIRMED && currentStatus == OrderStatus.PENDING) {
            // Decrement stock when confirming order
            decrementStock(pedido);
        } else if (newStatus == OrderStatus.CANCELED && currentStatus == OrderStatus.CONFIRMED) {
            // Restore stock when canceling a confirmed order
            restoreStock(pedido);
        }
        
        pedido.setStatus(newStatus);
        return pedidoRepository.save(pedido);
    }
    
    /**
     * Decrement stock for all items in the order
     */
    private void decrementStock(Pedido pedido) {
        for (LineaPedido linea : pedido.getLineas()) {
            Product product = productRepository.findById(linea.getProductId())
                    .orElseThrow(() -> new ResourceNotFoundException("Producto", "id", linea.getProductId()));
            
            // Re-check stock availability
            if (product.getStock() < linea.getCantidad()) {
                throw new StockInsuficienteException(
                        product.getId(),
                        product.getNombre(),
                        linea.getCantidad(),
                        product.getStock()
                );
            }
            
            product.setStock(product.getStock() - linea.getCantidad());
            productRepository.save(product);
        }
    }
    
    /**
     * Restore stock for all items in the order (when canceling)
     */
    private void restoreStock(Pedido pedido) {
        for (LineaPedido linea : pedido.getLineas()) {
            Product product = productRepository.findById(linea.getProductId())
                    .orElseThrow(() -> new ResourceNotFoundException("Producto", "id", linea.getProductId()));
            
            product.setStock(product.getStock() + linea.getCantidad());
            productRepository.save(product);
        }
    }
    
    /**
     * Validate status transition
     */
    private void validateStatusTransition(OrderStatus current, OrderStatus target) {
        // Define valid transitions
        switch (current) {
            case PENDING:
                if (target != OrderStatus.CONFIRMED && target != OrderStatus.CANCELED) {
                    throw new BadRequestException(
                            String.format("Transición de estado inválida: %s -> %s", current, target));
                }
                break;
            case CONFIRMED:
                if (target != OrderStatus.SHIPPED && target != OrderStatus.CANCELED) {
                    throw new BadRequestException(
                            String.format("Transición de estado inválida: %s -> %s", current, target));
                }
                break;
            case SHIPPED:
                if (target != OrderStatus.DELIVERED) {
                    throw new BadRequestException(
                            String.format("Transición de estado inválida: %s -> %s", current, target));
                }
                break;
            case DELIVERED:
            case CANCELED:
                throw new BadRequestException(
                        String.format("No se puede cambiar el estado de un pedido %s", current));
            default:
                throw new BadRequestException("Estado de pedido no reconocido: " + current);
        }
    }
    
    /**
     * DTO for order item request
     */
    public static class OrderItemRequest {
        private Long productId;
        private Integer cantidad;
        
        public OrderItemRequest() {
        }
        
        public OrderItemRequest(Long productId, Integer cantidad) {
            this.productId = productId;
            this.cantidad = cantidad;
        }
        
        public Long getProductId() {
            return productId;
        }
        
        public void setProductId(Long productId) {
            this.productId = productId;
        }
        
        public Integer getCantidad() {
            return cantidad;
        }
        
        public void setCantidad(Integer cantidad) {
            this.cantidad = cantidad;
        }
    }
}
