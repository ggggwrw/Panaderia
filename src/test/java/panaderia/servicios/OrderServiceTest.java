package panaderia.servicios;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import panaderia.excepciones.BadRequestException;
import panaderia.excepciones.ResourceNotFoundException;
import panaderia.excepciones.StockInsuficienteException;
import panaderia.pedidos.*;
import panaderia.productos.Product;
import panaderia.productos.ProductRepository;

import java.math.BigDecimal;
import java.util.Arrays;
import java.util.Collections;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class OrderServiceTest {

    @Mock
    private PedidoRepository pedidoRepository;

    @Mock
    private ProductRepository productRepository;

    @InjectMocks
    private OrderService orderService;

    private Product testProduct;

    @BeforeEach
    void setUp() {
        testProduct = new Product("Pan de Campo", "Pan artesanal", 
                new BigDecimal("3.50"), "Panes", "http://example.com/pan.jpg", 50);
        testProduct.setId(1L);
    }

    @Test
    void createOrder_WithSufficientStock_ShouldCreateOrder() {
        CreateOrderRequest request = new CreateOrderRequest(
                1L, 
                Arrays.asList(new OrderItemRequest(1L, 5))
        );

        when(productRepository.findById(1L)).thenReturn(Optional.of(testProduct));
        when(pedidoRepository.save(any(Pedido.class))).thenAnswer(invocation -> {
            Pedido pedido = invocation.getArgument(0);
            pedido.setId(1L);
            return pedido;
        });

        Pedido result = orderService.createOrder(request);

        assertNotNull(result);
        assertEquals(1L, result.getUserId());
        assertEquals(OrderStatus.PENDING, result.getStatus());
        assertEquals(new BigDecimal("17.50"), result.getTotal());
        assertEquals(1, result.getLineas().size());
        verify(pedidoRepository, times(1)).save(any(Pedido.class));
    }

    @Test
    void createOrder_WithInsufficientStock_ShouldThrowException() {
        CreateOrderRequest request = new CreateOrderRequest(
                1L, 
                Arrays.asList(new OrderItemRequest(1L, 100)) // More than available stock (50)
        );

        when(productRepository.findById(1L)).thenReturn(Optional.of(testProduct));

        assertThrows(StockInsuficienteException.class, () -> {
            orderService.createOrder(request);
        });

        verify(pedidoRepository, never()).save(any(Pedido.class));
    }

    @Test
    void createOrder_WithEmptyItems_ShouldThrowBadRequestException() {
        CreateOrderRequest request = new CreateOrderRequest(1L, Collections.emptyList());

        assertThrows(BadRequestException.class, () -> {
            orderService.createOrder(request);
        });
    }

    @Test
    void createOrder_WithNonExistentProduct_ShouldThrowResourceNotFoundException() {
        CreateOrderRequest request = new CreateOrderRequest(
                1L, 
                Arrays.asList(new OrderItemRequest(99L, 5))
        );

        when(productRepository.findById(99L)).thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class, () -> {
            orderService.createOrder(request);
        });
    }

    @Test
    void updateStatus_FromPendingToConfirmed_ShouldDecrementStock() {
        Pedido pedido = new Pedido();
        pedido.setId(1L);
        pedido.setStatus(OrderStatus.PENDING);
        LineaPedido linea = new LineaPedido(1L, 5, new BigDecimal("3.50"));
        pedido.agregarLinea(linea);

        when(pedidoRepository.findById(1L)).thenReturn(Optional.of(pedido));
        when(productRepository.findById(1L)).thenReturn(Optional.of(testProduct));
        when(pedidoRepository.save(any(Pedido.class))).thenReturn(pedido);
        when(productRepository.save(any(Product.class))).thenReturn(testProduct);

        Pedido result = orderService.updateStatus(1L, OrderStatus.CONFIRMED);

        assertEquals(OrderStatus.CONFIRMED, result.getStatus());
        assertEquals(45, testProduct.getStock()); // 50 - 5
        verify(productRepository, times(1)).save(testProduct);
    }

    @Test
    void updateStatus_FromConfirmedToCanceled_ShouldRestoreStock() {
        testProduct.setStock(45); // Already decremented

        Pedido pedido = new Pedido();
        pedido.setId(1L);
        pedido.setStatus(OrderStatus.CONFIRMED);
        LineaPedido linea = new LineaPedido(1L, 5, new BigDecimal("3.50"));
        pedido.agregarLinea(linea);

        when(pedidoRepository.findById(1L)).thenReturn(Optional.of(pedido));
        when(productRepository.findById(1L)).thenReturn(Optional.of(testProduct));
        when(pedidoRepository.save(any(Pedido.class))).thenReturn(pedido);
        when(productRepository.save(any(Product.class))).thenReturn(testProduct);

        Pedido result = orderService.updateStatus(1L, OrderStatus.CANCELED);

        assertEquals(OrderStatus.CANCELED, result.getStatus());
        assertEquals(50, testProduct.getStock()); // 45 + 5 restored
        verify(productRepository, times(1)).save(testProduct);
    }

    @Test
    void updateStatus_InvalidTransition_ShouldThrowBadRequestException() {
        Pedido pedido = new Pedido();
        pedido.setId(1L);
        pedido.setStatus(OrderStatus.DELIVERED);

        when(pedidoRepository.findById(1L)).thenReturn(Optional.of(pedido));

        assertThrows(BadRequestException.class, () -> {
            orderService.updateStatus(1L, OrderStatus.PENDING);
        });
    }

    @Test
    void findById_WhenOrderExists_ShouldReturnOrder() {
        Pedido pedido = new Pedido();
        pedido.setId(1L);
        pedido.setUserId(1L);

        when(pedidoRepository.findById(1L)).thenReturn(Optional.of(pedido));

        Pedido result = orderService.findById(1L);

        assertNotNull(result);
        assertEquals(1L, result.getId());
    }

    @Test
    void findById_WhenOrderNotExists_ShouldThrowException() {
        when(pedidoRepository.findById(99L)).thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class, () -> {
            orderService.findById(99L);
        });
    }

    @Test
    void findByUserId_ShouldReturnUserOrders() {
        Pedido pedido1 = new Pedido();
        pedido1.setId(1L);
        pedido1.setUserId(1L);

        Pedido pedido2 = new Pedido();
        pedido2.setId(2L);
        pedido2.setUserId(1L);

        when(pedidoRepository.findByUserId(1L)).thenReturn(Arrays.asList(pedido1, pedido2));

        var result = orderService.findByUserId(1L);

        assertEquals(2, result.size());
    }
}
