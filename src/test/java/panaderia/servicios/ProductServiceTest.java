package panaderia.servicios;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import panaderia.excepciones.ResourceNotFoundException;
import panaderia.productos.Product;
import panaderia.productos.ProductRepository;

import java.math.BigDecimal;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ProductServiceTest {

    @Mock
    private ProductRepository productRepository;

    @InjectMocks
    private ProductService productService;

    private Product testProduct;

    @BeforeEach
    void setUp() {
        testProduct = new Product("Pan de Campo", "Pan artesanal", 
                new BigDecimal("3.50"), "Panes", "http://example.com/pan.jpg", 50);
        testProduct.setId(1L);
    }

    @Test
    void findAll_ShouldReturnAllProducts() {
        Product product2 = new Product("Croissant", "Croissant de mantequilla", 
                new BigDecimal("1.25"), "Bollería", "http://example.com/croissant.jpg", 100);
        product2.setId(2L);

        when(productRepository.findAll()).thenReturn(Arrays.asList(testProduct, product2));

        List<Product> result = productService.findAll();

        assertEquals(2, result.size());
        verify(productRepository, times(1)).findAll();
    }

    @Test
    void findById_WhenProductExists_ShouldReturnProduct() {
        when(productRepository.findById(1L)).thenReturn(Optional.of(testProduct));

        Product result = productService.findById(1L);

        assertNotNull(result);
        assertEquals("Pan de Campo", result.getNombre());
        assertEquals(new BigDecimal("3.50"), result.getPrecio());
    }

    @Test
    void findById_WhenProductNotExists_ShouldThrowException() {
        when(productRepository.findById(99L)).thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class, () -> {
            productService.findById(99L);
        });
    }

    @Test
    void create_ShouldSaveAndReturnProduct() {
        Product newProduct = new Product("Baguette", "Pan francés", 
                new BigDecimal("2.50"), "Panes", "http://example.com/baguette.jpg", 30);

        when(productRepository.save(any(Product.class))).thenReturn(newProduct);

        Product result = productService.create(newProduct);

        assertNotNull(result);
        assertEquals("Baguette", result.getNombre());
        verify(productRepository, times(1)).save(newProduct);
    }

    @Test
    void update_WhenProductExists_ShouldUpdateAndReturnProduct() {
        Product updatedData = new Product("Pan de Campo Actualizado", "Nueva descripción", 
                new BigDecimal("4.00"), "Panes", "http://example.com/pan-nuevo.jpg", 60);

        when(productRepository.findById(1L)).thenReturn(Optional.of(testProduct));
        when(productRepository.save(any(Product.class))).thenReturn(testProduct);

        Product result = productService.update(1L, updatedData);

        assertEquals("Pan de Campo Actualizado", result.getNombre());
        assertEquals(new BigDecimal("4.00"), result.getPrecio());
        verify(productRepository, times(1)).save(testProduct);
    }

    @Test
    void update_WhenProductNotExists_ShouldThrowException() {
        Product updatedData = new Product("Test", "Test", new BigDecimal("1.00"), "Test", null, 10);

        when(productRepository.findById(99L)).thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class, () -> {
            productService.update(99L, updatedData);
        });
    }

    @Test
    void delete_WhenProductExists_ShouldDeleteProduct() {
        when(productRepository.existsById(1L)).thenReturn(true);

        productService.delete(1L);

        verify(productRepository, times(1)).deleteById(1L);
    }

    @Test
    void delete_WhenProductNotExists_ShouldThrowException() {
        when(productRepository.existsById(99L)).thenReturn(false);

        assertThrows(ResourceNotFoundException.class, () -> {
            productService.delete(99L);
        });
    }

    @Test
    void search_WithValidQuery_ShouldReturnMatchingProducts() {
        when(productRepository.searchByNombreOrCategoria("pan"))
                .thenReturn(Arrays.asList(testProduct));

        List<Product> result = productService.search("pan");

        assertEquals(1, result.size());
        assertEquals("Pan de Campo", result.get(0).getNombre());
    }

    @Test
    void search_WithNullQuery_ShouldReturnAllProducts() {
        when(productRepository.findAll()).thenReturn(Arrays.asList(testProduct));

        List<Product> result = productService.search(null);

        assertEquals(1, result.size());
        verify(productRepository, times(1)).findAll();
    }
}
