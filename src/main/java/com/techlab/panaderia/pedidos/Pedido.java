package com.techlab.panaderia.pedidos;

import jakarta.persistence.*;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "orders")
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
    
    // Default constructor
    public Pedido() {
        this.fechaCreacion = LocalDateTime.now();
        this.status = OrderStatus.PENDING;
        this.total = BigDecimal.ZERO;
    }
    
    // Constructor with userId
    public Pedido(Long userId) {
        this();
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
    
    // Getters and Setters
    public Long getId() {
        return id;
    }
    
    public void setId(Long id) {
        this.id = id;
    }
    
    public Long getUserId() {
        return userId;
    }
    
    public void setUserId(Long userId) {
        this.userId = userId;
    }
    
    public LocalDateTime getFechaCreacion() {
        return fechaCreacion;
    }
    
    public void setFechaCreacion(LocalDateTime fechaCreacion) {
        this.fechaCreacion = fechaCreacion;
    }
    
    public BigDecimal getTotal() {
        return total;
    }
    
    public void setTotal(BigDecimal total) {
        this.total = total;
    }
    
    public OrderStatus getStatus() {
        return status;
    }
    
    public void setStatus(OrderStatus status) {
        this.status = status;
    }
    
    public List<LineaPedido> getLineas() {
        return lineas;
    }
    
    public void setLineas(List<LineaPedido> lineas) {
        this.lineas = lineas;
    }
    
    public Long getVersion() {
        return version;
    }
    
    public void setVersion(Long version) {
        this.version = version;
    }
    
    @Override
    public String toString() {
        return "Pedido{" +
                "id=" + id +
                ", userId=" + userId +
                ", fechaCreacion=" + fechaCreacion +
                ", total=" + total +
                ", status=" + status +
                ", lineas=" + lineas.size() +
                '}';
    }
}
