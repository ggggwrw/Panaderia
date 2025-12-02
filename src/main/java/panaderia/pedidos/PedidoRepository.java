package panaderia.pedidos;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface PedidoRepository extends JpaRepository<Pedido, Long> {
    
    /**
     * Find all orders for a specific user
     */
    List<Pedido> findByUserId(Long userId);
    
    /**
     * Find all orders with a specific status
     */
    List<Pedido> findByStatus(OrderStatus status);
    
    /**
     * Find all orders for a user with a specific status
     */
    List<Pedido> findByUserIdAndStatus(Long userId, OrderStatus status);
}
