package panaderia.pedidos;

import jakarta.persistence.*;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "orders")
@Data
@NoArgsConstructor
public class Pedido {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @Column(name = "user_id")
    private Long userId;
    
    @Column(name = "fecha_creacion", nullable = false)
    private LocalDateTime fechaCreacion;
    
    @Column(nullable = false, precision = 10, scale = 2)
    private BigDecimal total;
    
    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private OrderStatus status;
    
    @OneToMany(mappedBy = "pedido", cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.EAGER)
    private List<LineaPedido> lineas = new ArrayList<>();
    
    @Version
    private Long version;
    
    // Constructor that initializes default values
    {
        this.fechaCreacion = LocalDateTime.now();
        this.status = OrderStatus.PENDING;
        this.total = BigDecimal.ZERO;
    }
    
    // Constructor with userId
    public Pedido(Long userId) {
        this.userId = userId;
    }
    
    // Helper method to add a line item
    public void addLinea(LineaPedido linea) {
        lineas.add(linea);
        linea.setPedido(this);
    }
    
    // Helper method to remove a line item
    public void removeLinea(LineaPedido linea) {
        lineas.remove(linea);
        linea.setPedido(null);
    }
    
    // Calculate total from line items
    public void calculateTotal() {
        this.total = lineas.stream()
                .map(LineaPedido::getSubtotal)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
    }
}
