package panaderia.servicios;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import panaderia.dto.CreateOrderDTO;
import panaderia.dto.OrderItemDTO;
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
import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class OrderServiceTest {

    @Mock
    private PedidoRepository pedidoRepository;

    @Mock
    private ProductRepository productRepository;

    @InjectMocks
    private OrderService orderService;

    private Product testProduct1;
    private Product testProduct2;
    private Pedido testPedido;

    @BeforeEach
    void setUp() {
        testProduct1 = Product.builder()
            .id(1L)
            .nombre("Pan de Campo")
            .precio(new BigDecimal("3.50"))
            .stock(50)
            .version(0L)
            .build();

        testProduct2 = Product.builder()
            .id(2L)
            .nombre("Croissant")
            .precio(new BigDecimal("1.25"))
            .stock(100)
            .version(0L)
            .build();

        LineaPedido linea1 = LineaPedido.builder()
            .id(1L)
            .productId(1L)
            .cantidad(2)
            .precioUnitario(new BigDecimal("3.50"))
            .build();

        LineaPedido linea2 = LineaPedido.builder()
            .id(2L)
            .productId(2L)
            .cantidad(3)
            .precioUnitario(new BigDecimal("1.25"))
            .build();

        List<LineaPedido> lineas = new ArrayList<>();
        lineas.add(linea1);
        lineas.add(linea2);

        testPedido = Pedido.builder()
            .id(1L)
            .userId(1L)
            .fechaCreacion(LocalDateTime.now())
            .total(new BigDecimal("10.75"))
            .status(OrderStatus.PENDING)
            .lineas(lineas)
            .version(0L)
            .build();

        linea1.setPedido(testPedido);
        linea2.setPedido(testPedido);
    }

    @Test
    void createOrder_WithSufficientStock_CreatesOrder() {
        OrderItemDTO item1 = new OrderItemDTO(1L, 2);
        OrderItemDTO item2 = new OrderItemDTO(2L, 3);
        CreateOrderDTO dto = CreateOrderDTO.builder()
            .userId(1L)
            .items(Arrays.asList(item1, item2))
            .build();

        when(productRepository.findById(1L)).thenReturn(Optional.of(testProduct1));
        when(productRepository.findById(2L)).thenReturn(Optional.of(testProduct2));
        when(pedidoRepository.save(any(Pedido.class))).thenAnswer(invocation -> {
            Pedido pedido = invocation.getArgument(0);
            pedido.setId(1L);
            return pedido;
        });

        Pedido result = orderService.createOrder(dto);

        assertNotNull(result);
        assertEquals(OrderStatus.PENDING, result.getStatus());
        assertEquals(2, result.getLineas().size());
        // Total: (2 * 3.50) + (3 * 1.25) = 7.00 + 3.75 = 10.75
        assertEquals(new BigDecimal("10.75"), result.getTotal());
        verify(pedidoRepository, times(1)).save(any(Pedido.class));
    }

    @Test
    void createOrder_WithInsufficientStock_ThrowsException() {
        OrderItemDTO item = new OrderItemDTO(1L, 100); // More than available (50)
        CreateOrderDTO dto = CreateOrderDTO.builder()
            .userId(1L)
            .items(Arrays.asList(item))
            .build();

        when(productRepository.findById(1L)).thenReturn(Optional.of(testProduct1));

        StockInsuficienteException exception = assertThrows(
            StockInsuficienteException.class,
            () -> orderService.createOrder(dto)
        );

        assertTrue(exception.getMessage().contains("Stock insuficiente"));
        verify(pedidoRepository, never()).save(any(Pedido.class));
    }

    @Test
    void createOrder_WithNonExistingProduct_ThrowsException() {
        OrderItemDTO item = new OrderItemDTO(99L, 5);
        CreateOrderDTO dto = CreateOrderDTO.builder()
            .userId(1L)
            .items(Arrays.asList(item))
            .build();

        when(productRepository.findById(99L)).thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class, () -> orderService.createOrder(dto));
        verify(pedidoRepository, never()).save(any(Pedido.class));
    }

    @Test
    void updateStatus_PendingToConfirmed_DecrementsStock() {
        when(pedidoRepository.findById(1L)).thenReturn(Optional.of(testPedido));
        when(productRepository.findById(1L)).thenReturn(Optional.of(testProduct1));
        when(productRepository.findById(2L)).thenReturn(Optional.of(testProduct2));
        when(productRepository.save(any(Product.class))).thenAnswer(i -> i.getArgument(0));
        when(pedidoRepository.save(any(Pedido.class))).thenReturn(testPedido);

        Pedido result = orderService.updateStatus(1L, OrderStatus.CONFIRMED);

        assertNotNull(result);
        assertEquals(OrderStatus.CONFIRMED, result.getStatus());
        assertEquals(48, testProduct1.getStock()); // 50 - 2
        assertEquals(97, testProduct2.getStock()); // 100 - 3
        verify(productRepository, times(2)).save(any(Product.class));
    }

    @Test
    void updateStatus_ConfirmedToCanceled_RestoresStock() {
        testPedido.setStatus(OrderStatus.CONFIRMED);
        
        when(pedidoRepository.findById(1L)).thenReturn(Optional.of(testPedido));
        when(productRepository.findById(1L)).thenReturn(Optional.of(testProduct1));
        when(productRepository.findById(2L)).thenReturn(Optional.of(testProduct2));
        when(productRepository.save(any(Product.class))).thenAnswer(i -> i.getArgument(0));
        when(pedidoRepository.save(any(Pedido.class))).thenReturn(testPedido);

        Pedido result = orderService.updateStatus(1L, OrderStatus.CANCELED);

        assertNotNull(result);
        assertEquals(OrderStatus.CANCELED, result.getStatus());
        assertEquals(52, testProduct1.getStock()); // 50 + 2
        assertEquals(103, testProduct2.getStock()); // 100 + 3
    }

    @Test
    void updateStatus_DeliveredOrder_ThrowsException() {
        testPedido.setStatus(OrderStatus.DELIVERED);
        when(pedidoRepository.findById(1L)).thenReturn(Optional.of(testPedido));

        assertThrows(BadRequestException.class, 
            () -> orderService.updateStatus(1L, OrderStatus.CANCELED));
    }

    @Test
    void updateStatus_InvalidTransition_ThrowsException() {
        when(pedidoRepository.findById(1L)).thenReturn(Optional.of(testPedido));

        assertThrows(BadRequestException.class, 
            () -> orderService.updateStatus(1L, OrderStatus.DELIVERED));
    }

    @Test
    void findById_ExistingId_ReturnsOrder() {
        when(pedidoRepository.findById(1L)).thenReturn(Optional.of(testPedido));

        Pedido result = orderService.findById(1L);

        assertNotNull(result);
        assertEquals(1L, result.getId());
        verify(pedidoRepository, times(1)).findById(1L);
    }

    @Test
    void findById_NonExistingId_ThrowsException() {
        when(pedidoRepository.findById(anyLong())).thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class, () -> orderService.findById(99L));
    }

    @Test
    void findByUserId_ReturnsUserOrders() {
        List<Pedido> expectedOrders = Arrays.asList(testPedido);
        when(pedidoRepository.findByUserIdOrderByFechaCreacionDesc(1L)).thenReturn(expectedOrders);

        List<Pedido> result = orderService.findByUserId(1L);

        assertNotNull(result);
        assertEquals(1, result.size());
        verify(pedidoRepository, times(1)).findByUserIdOrderByFechaCreacionDesc(1L);
    }
}
