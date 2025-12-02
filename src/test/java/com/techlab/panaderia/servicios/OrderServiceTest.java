package com.techlab.panaderia.servicios;

import com.techlab.panaderia.excepciones.BadRequestException;
import com.techlab.panaderia.excepciones.ResourceNotFoundException;
import com.techlab.panaderia.excepciones.StockInsuficienteException;
import com.techlab.panaderia.pedidos.OrderStatus;
import com.techlab.panaderia.pedidos.Pedido;
import com.techlab.panaderia.productos.Product;
import com.techlab.panaderia.productos.ProductRepository;
import com.techlab.panaderia.servicios.OrderService.OrderItemRequest;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
@Transactional
class OrderServiceTest {

    @Autowired
    private OrderService orderService;

    @Autowired
    private ProductRepository productRepository;

    private Product product1;
    private Product product2;

    @BeforeEach
    void setUp() {
        // Create test products
        product1 = new Product("Product 1", BigDecimal.valueOf(10.00), 50);
        product1 = productRepository.save(product1);

        product2 = new Product("Product 2", BigDecimal.valueOf(20.00), 30);
        product2 = productRepository.save(product2);
    }

    @Test
    @DisplayName("Should create order with sufficient stock")
    void testCreateOrderWithSufficientStock() {
        List<OrderItemRequest> items = Arrays.asList(
                new OrderItemRequest(product1.getId(), 5),
                new OrderItemRequest(product2.getId(), 3)
        );

        Pedido pedido = orderService.createOrder(1L, items);

        assertNotNull(pedido.getId());
        assertEquals(OrderStatus.PENDING, pedido.getStatus());
        assertEquals(2, pedido.getLineas().size());
        // Total: 5 * 10 + 3 * 20 = 50 + 60 = 110
        assertEquals(BigDecimal.valueOf(110.00).setScale(2), pedido.getTotal().setScale(2));
    }

    @Test
    @DisplayName("Should throw StockInsuficienteException when stock is insufficient")
    void testCreateOrderWithInsufficientStock() {
        List<OrderItemRequest> items = Collections.singletonList(
                new OrderItemRequest(product1.getId(), 100) // More than available stock
        );

        StockInsuficienteException exception = assertThrows(StockInsuficienteException.class, () -> {
            orderService.createOrder(1L, items);
        });

        assertTrue(exception.getMessage().contains("Stock insuficiente"));
    }

    @Test
    @DisplayName("Should throw BadRequestException when order is empty")
    void testCreateOrderWithEmptyItems() {
        assertThrows(BadRequestException.class, () -> {
            orderService.createOrder(1L, Collections.emptyList());
        });
    }

    @Test
    @DisplayName("Should throw ResourceNotFoundException when product not found")
    void testCreateOrderWithNonExistentProduct() {
        List<OrderItemRequest> items = Collections.singletonList(
                new OrderItemRequest(99999L, 1)
        );

        assertThrows(ResourceNotFoundException.class, () -> {
            orderService.createOrder(1L, items);
        });
    }

    @Test
    @DisplayName("Should get order by ID")
    void testGetOrderById() {
        List<OrderItemRequest> items = Collections.singletonList(
                new OrderItemRequest(product1.getId(), 5)
        );

        Pedido created = orderService.createOrder(1L, items);
        Pedido found = orderService.getOrderById(created.getId());

        assertEquals(created.getId(), found.getId());
        assertEquals(created.getTotal(), found.getTotal());
    }

    @Test
    @DisplayName("Should throw exception when order not found")
    void testGetOrderByIdNotFound() {
        assertThrows(ResourceNotFoundException.class, () -> {
            orderService.getOrderById(99999L);
        });
    }

    @Test
    @DisplayName("Should confirm order and decrement stock")
    void testConfirmOrderDecrementsStock() {
        int initialStock = product1.getStock();
        int orderQuantity = 5;

        List<OrderItemRequest> items = Collections.singletonList(
                new OrderItemRequest(product1.getId(), orderQuantity)
        );

        Pedido pedido = orderService.createOrder(1L, items);
        assertEquals(OrderStatus.PENDING, pedido.getStatus());

        // Confirm the order
        Pedido confirmed = orderService.updateOrderStatus(pedido.getId(), OrderStatus.CONFIRMED);

        assertEquals(OrderStatus.CONFIRMED, confirmed.getStatus());

        // Check stock was decremented
        Product updatedProduct = productRepository.findById(product1.getId()).orElseThrow();
        assertEquals(initialStock - orderQuantity, updatedProduct.getStock());
    }

    @Test
    @DisplayName("Should cancel confirmed order and restore stock")
    void testCancelConfirmedOrderRestoresStock() {
        int initialStock = product1.getStock();
        int orderQuantity = 5;

        List<OrderItemRequest> items = Collections.singletonList(
                new OrderItemRequest(product1.getId(), orderQuantity)
        );

        Pedido pedido = orderService.createOrder(1L, items);
        orderService.updateOrderStatus(pedido.getId(), OrderStatus.CONFIRMED);

        // Verify stock was decremented
        Product afterConfirm = productRepository.findById(product1.getId()).orElseThrow();
        assertEquals(initialStock - orderQuantity, afterConfirm.getStock());

        // Cancel the order
        Pedido canceled = orderService.updateOrderStatus(pedido.getId(), OrderStatus.CANCELED);

        assertEquals(OrderStatus.CANCELED, canceled.getStatus());

        // Check stock was restored
        Product afterCancel = productRepository.findById(product1.getId()).orElseThrow();
        assertEquals(initialStock, afterCancel.getStock());
    }

    @Test
    @DisplayName("Should throw BadRequestException for invalid status transition")
    void testInvalidStatusTransition() {
        List<OrderItemRequest> items = Collections.singletonList(
                new OrderItemRequest(product1.getId(), 5)
        );

        Pedido pedido = orderService.createOrder(1L, items);

        // Try to ship a pending order (should fail - must be confirmed first)
        assertThrows(BadRequestException.class, () -> {
            orderService.updateOrderStatus(pedido.getId(), OrderStatus.SHIPPED);
        });
    }

    @Test
    @DisplayName("Should get orders by user ID")
    void testGetOrdersByUserId() {
        List<OrderItemRequest> items = Collections.singletonList(
                new OrderItemRequest(product1.getId(), 5)
        );

        orderService.createOrder(1L, items);
        orderService.createOrder(1L, items);
        orderService.createOrder(2L, items);

        List<Pedido> user1Orders = orderService.getOrdersByUserId(1L);
        List<Pedido> user2Orders = orderService.getOrdersByUserId(2L);

        assertEquals(2, user1Orders.size());
        assertEquals(1, user2Orders.size());
    }

    @Test
    @DisplayName("Should calculate total correctly with multiple items")
    void testOrderTotalCalculation() {
        List<OrderItemRequest> items = Arrays.asList(
                new OrderItemRequest(product1.getId(), 3), // 3 * 10 = 30
                new OrderItemRequest(product2.getId(), 2)  // 2 * 20 = 40
        );

        Pedido pedido = orderService.createOrder(1L, items);

        // Total should be 30 + 40 = 70
        assertEquals(0, BigDecimal.valueOf(70.00).compareTo(pedido.getTotal()));
    }
}
