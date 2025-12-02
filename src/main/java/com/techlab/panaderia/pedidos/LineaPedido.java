package com.techlab.panaderia.pedidos;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import java.math.BigDecimal;

@Entity
@Table(name = "order_lines")
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
    
    // Default constructor
    public LineaPedido() {
    }
    
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
    
    // Getters and Setters
    public Long getId() {
        return id;
    }
    
    public void setId(Long id) {
        this.id = id;
    }
    
    public Pedido getPedido() {
        return pedido;
    }
    
    public void setPedido(Pedido pedido) {
        this.pedido = pedido;
    }
    
    public Long getProductId() {
        return productId;
    }
    
    public void setProductId(Long productId) {
        this.productId = productId;
    }
    
    public String getProductName() {
        return productName;
    }
    
    public void setProductName(String productName) {
        this.productName = productName;
    }
    
    public Integer getCantidad() {
        return cantidad;
    }
    
    public void setCantidad(Integer cantidad) {
        this.cantidad = cantidad;
    }
    
    public BigDecimal getPrecioUnitario() {
        return precioUnitario;
    }
    
    public void setPrecioUnitario(BigDecimal precioUnitario) {
        this.precioUnitario = precioUnitario;
    }
    
    @Override
    public String toString() {
        return "LineaPedido{" +
                "id=" + id +
                ", productId=" + productId +
                ", productName='" + productName + '\'' +
                ", cantidad=" + cantidad +
                ", precioUnitario=" + precioUnitario +
                ", subtotal=" + getSubtotal() +
                '}';
    }
}
