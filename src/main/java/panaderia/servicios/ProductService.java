package panaderia.servicios;

import panaderia.excepciones.ResourceNotFoundException;
import panaderia.productos.Product;
import panaderia.productos.ProductRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@Transactional
public class ProductService {
    
    private final ProductRepository productRepository;
    
    public ProductService(ProductRepository productRepository) {
        this.productRepository = productRepository;
    }
    
    /**
     * Get all products
     */
    @Transactional(readOnly = true)
    public List<Product> getAllProducts() {
        return productRepository.findAll();
    }
    
    /**
     * Search products by name or category
     */
    @Transactional(readOnly = true)
    public List<Product> searchProducts(String search) {
        if (search == null || search.trim().isEmpty()) {
            return productRepository.findAll();
        }
        return productRepository.searchByNameOrCategory(search.trim());
    }
    
    /**
     * Get a product by ID
     */
    @Transactional(readOnly = true)
    public Product getProductById(Long id) {
        return productRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Producto", "id", id));
    }
    
    /**
     * Create a new product
     */
    public Product createProduct(Product product) {
        return productRepository.save(product);
    }
    
    /**
     * Update an existing product
     */
    public Product updateProduct(Long id, Product productDetails) {
        Product product = getProductById(id);
        
        product.setNombre(productDetails.getNombre());
        product.setDescripcion(productDetails.getDescripcion());
        product.setPrecio(productDetails.getPrecio());
        product.setCategoria(productDetails.getCategoria());
        product.setImageUrl(productDetails.getImageUrl());
        product.setStock(productDetails.getStock());
        
        return productRepository.save(product);
    }
    
    /**
     * Delete a product
     */
    public void deleteProduct(Long id) {
        Product product = getProductById(id);
        productRepository.delete(product);
    }
    
    /**
     * Update stock for a product
     */
    public Product updateStock(Long id, int quantityChange) {
        Product product = getProductById(id);
        int newStock = product.getStock() + quantityChange;
        
        if (newStock < 0) {
            throw new IllegalArgumentException("El stock no puede ser negativo");
        }
        
        product.setStock(newStock);
        return productRepository.save(product);
    }
    
    /**
     * Check if there is sufficient stock for a product
     */
    @Transactional(readOnly = true)
    public boolean hasAvailableStock(Long productId, int quantity) {
        Product product = getProductById(productId);
        return product.getStock() >= quantity;
    }
}
