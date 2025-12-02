package panaderia.pedidos;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Entity
@Table(name = "order_lines")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class LineaPedido {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "pedido_id", nullable = false)
    @JsonIgnore
    private Pedido pedido;
    
    @Column(name = "product_id", nullable = false)
    private Long productId;
    
    @Column(name = "product_name")
    private String productName;
    
    @Column(nullable = false)
    private Integer cantidad;
    
    @Column(name = "precio_unitario", nullable = false, precision = 10, scale = 2)
    private BigDecimal precioUnitario;
    
    // Constructor with required fields
    public LineaPedido(Long productId, String productName, Integer cantidad, BigDecimal precioUnitario) {
        this.productId = productId;
        this.productName = productName;
        this.cantidad = cantidad;
        this.precioUnitario = precioUnitario;
    }
    
    // Calculate subtotal for this line
    public BigDecimal getSubtotal() {
        return precioUnitario.multiply(BigDecimal.valueOf(cantidad));
    }
}
