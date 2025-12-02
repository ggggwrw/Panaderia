package panaderia.servicios;

import panaderia.excepciones.ResourceNotFoundException;
import panaderia.productos.Product;
import panaderia.productos.ProductRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
@Transactional
class ProductServiceTest {

    @Autowired
    private ProductService productService;

    @Autowired
    private ProductRepository productRepository;

    private Product testProduct;

    @BeforeEach
    void setUp() {
        testProduct = new Product("Test Product", BigDecimal.valueOf(5.99), 100);
        testProduct.setCategoria("Test Category");
        testProduct.setDescripcion("Test Description");
    }

    @Test
    @DisplayName("Should create a new product successfully")
    void testCreateProduct() {
        Product created = productService.createProduct(testProduct);

        assertNotNull(created.getId());
        assertEquals("Test Product", created.getNombre());
        assertEquals(BigDecimal.valueOf(5.99), created.getPrecio());
        assertEquals(100, created.getStock());
    }

    @Test
    @DisplayName("Should get product by ID")
    void testGetProductById() {
        Product created = productService.createProduct(testProduct);
        Product found = productService.getProductById(created.getId());

        assertEquals(created.getId(), found.getId());
        assertEquals(created.getNombre(), found.getNombre());
    }

    @Test
    @DisplayName("Should throw exception when product not found")
    void testGetProductByIdNotFound() {
        assertThrows(ResourceNotFoundException.class, () -> {
            productService.getProductById(99999L);
        });
    }

    @Test
    @DisplayName("Should update product successfully")
    void testUpdateProduct() {
        Product created = productService.createProduct(testProduct);

        Product updateData = new Product("Updated Product", BigDecimal.valueOf(9.99), 200);
        updateData.setCategoria("Updated Category");

        Product updated = productService.updateProduct(created.getId(), updateData);

        assertEquals("Updated Product", updated.getNombre());
        assertEquals(BigDecimal.valueOf(9.99), updated.getPrecio());
        assertEquals(200, updated.getStock());
    }

    @Test
    @DisplayName("Should delete product successfully")
    void testDeleteProduct() {
        Product created = productService.createProduct(testProduct);
        Long productId = created.getId();

        productService.deleteProduct(productId);

        assertThrows(ResourceNotFoundException.class, () -> {
            productService.getProductById(productId);
        });
    }

    @Test
    @DisplayName("Should search products by name")
    void testSearchProductsByName() {
        productService.createProduct(testProduct);

        List<Product> results = productService.searchProducts("Test");

        assertFalse(results.isEmpty());
        assertTrue(results.stream().anyMatch(p -> p.getNombre().contains("Test")));
    }

    @Test
    @DisplayName("Should search products by category")
    void testSearchProductsByCategory() {
        productService.createProduct(testProduct);

        List<Product> results = productService.searchProducts("Category");

        assertFalse(results.isEmpty());
    }

    @Test
    @DisplayName("Should get all products when search is empty")
    void testSearchProductsEmptySearch() {
        productService.createProduct(testProduct);

        List<Product> results = productService.searchProducts("");
        List<Product> allProducts = productService.getAllProducts();

        assertEquals(allProducts.size(), results.size());
    }

    @Test
    @DisplayName("Should update stock correctly")
    void testUpdateStock() {
        Product created = productService.createProduct(testProduct);
        int originalStock = created.getStock();

        Product updated = productService.updateStock(created.getId(), -10);

        assertEquals(originalStock - 10, updated.getStock());
    }

    @Test
    @DisplayName("Should throw exception when stock becomes negative")
    void testUpdateStockNegative() {
        Product created = productService.createProduct(testProduct);

        assertThrows(IllegalArgumentException.class, () -> {
            productService.updateStock(created.getId(), -200);
        });
    }

    @Test
    @DisplayName("Should check stock availability correctly")
    void testHasAvailableStock() {
        Product created = productService.createProduct(testProduct);

        assertTrue(productService.hasAvailableStock(created.getId(), 50));
        assertFalse(productService.hasAvailableStock(created.getId(), 200));
    }
}
