package panaderia.servicios;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import panaderia.dto.ProductDTO;
import panaderia.excepciones.ResourceNotFoundException;
import panaderia.productos.Product;
import panaderia.repositories.ProductRepository;

import java.math.BigDecimal;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ProductServiceTest {

    @Mock
    private ProductRepository productRepository;

    @InjectMocks
    private ProductService productService;

    private Product testProduct;
    private ProductDTO testProductDTO;

    @BeforeEach
    void setUp() {
        testProduct = Product.builder()
            .id(1L)
            .nombre("Pan de Campo")
            .descripcion("Pan artesanal")
            .precio(new BigDecimal("3.50"))
            .categoria("Panes")
            .stock(50)
            .version(0L)
            .build();

        testProductDTO = ProductDTO.builder()
            .nombre("Pan de Campo")
            .descripcion("Pan artesanal")
            .precio(new BigDecimal("3.50"))
            .categoria("Panes")
            .stock(50)
            .build();
    }

    @Test
    void findAll_ReturnsAllProducts() {
        List<Product> expectedProducts = Arrays.asList(testProduct);
        when(productRepository.findAll()).thenReturn(expectedProducts);

        List<Product> result = productService.findAll();

        assertNotNull(result);
        assertEquals(1, result.size());
        assertEquals("Pan de Campo", result.get(0).getNombre());
        verify(productRepository, times(1)).findAll();
    }

    @Test
    void findById_ExistingId_ReturnsProduct() {
        when(productRepository.findById(1L)).thenReturn(Optional.of(testProduct));

        Product result = productService.findById(1L);

        assertNotNull(result);
        assertEquals("Pan de Campo", result.getNombre());
        verify(productRepository, times(1)).findById(1L);
    }

    @Test
    void findById_NonExistingId_ThrowsException() {
        when(productRepository.findById(anyLong())).thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class, () -> productService.findById(99L));
        verify(productRepository, times(1)).findById(99L);
    }

    @Test
    void create_ValidProduct_ReturnsCreatedProduct() {
        when(productRepository.save(any(Product.class))).thenReturn(testProduct);

        Product result = productService.create(testProductDTO);

        assertNotNull(result);
        assertEquals("Pan de Campo", result.getNombre());
        assertEquals(new BigDecimal("3.50"), result.getPrecio());
        verify(productRepository, times(1)).save(any(Product.class));
    }

    @Test
    void update_ExistingProduct_ReturnsUpdatedProduct() {
        ProductDTO updateDTO = ProductDTO.builder()
            .nombre("Pan de Campo Actualizado")
            .descripcion("Nueva descripción")
            .precio(new BigDecimal("4.00"))
            .categoria("Panes")
            .stock(60)
            .build();

        Product updatedProduct = Product.builder()
            .id(1L)
            .nombre("Pan de Campo Actualizado")
            .descripcion("Nueva descripción")
            .precio(new BigDecimal("4.00"))
            .categoria("Panes")
            .stock(60)
            .version(1L)
            .build();

        when(productRepository.findById(1L)).thenReturn(Optional.of(testProduct));
        when(productRepository.save(any(Product.class))).thenReturn(updatedProduct);

        Product result = productService.update(1L, updateDTO);

        assertNotNull(result);
        assertEquals("Pan de Campo Actualizado", result.getNombre());
        assertEquals(new BigDecimal("4.00"), result.getPrecio());
        verify(productRepository, times(1)).findById(1L);
        verify(productRepository, times(1)).save(any(Product.class));
    }

    @Test
    void delete_ExistingProduct_DeletesSuccessfully() {
        when(productRepository.existsById(1L)).thenReturn(true);
        doNothing().when(productRepository).deleteById(1L);

        assertDoesNotThrow(() -> productService.delete(1L));
        verify(productRepository, times(1)).existsById(1L);
        verify(productRepository, times(1)).deleteById(1L);
    }

    @Test
    void delete_NonExistingProduct_ThrowsException() {
        when(productRepository.existsById(anyLong())).thenReturn(false);

        assertThrows(ResourceNotFoundException.class, () -> productService.delete(99L));
        verify(productRepository, times(1)).existsById(99L);
        verify(productRepository, never()).deleteById(anyLong());
    }

    @Test
    void search_WithValidQuery_ReturnsMatchingProducts() {
        List<Product> expectedProducts = Arrays.asList(testProduct);
        when(productRepository.searchByNombreOrCategoria("Pan")).thenReturn(expectedProducts);

        List<Product> result = productService.search("Pan");

        assertNotNull(result);
        assertEquals(1, result.size());
        verify(productRepository, times(1)).searchByNombreOrCategoria("Pan");
    }

    @Test
    void search_WithEmptyQuery_ReturnsAllProducts() {
        List<Product> expectedProducts = Arrays.asList(testProduct);
        when(productRepository.findAll()).thenReturn(expectedProducts);

        List<Product> result = productService.search("");

        assertNotNull(result);
        assertEquals(1, result.size());
        verify(productRepository, times(1)).findAll();
    }

    @Test
    void updateStock_ValidChange_UpdatesSuccessfully() {
        when(productRepository.findById(1L)).thenReturn(Optional.of(testProduct));
        when(productRepository.save(any(Product.class))).thenReturn(testProduct);

        Product result = productService.updateStock(1L, -10);

        assertNotNull(result);
        assertEquals(40, result.getStock());
        verify(productRepository, times(1)).save(any(Product.class));
    }

    @Test
    void updateStock_NegativeResult_ThrowsException() {
        when(productRepository.findById(1L)).thenReturn(Optional.of(testProduct));

        assertThrows(IllegalArgumentException.class, () -> productService.updateStock(1L, -100));
    }
}
