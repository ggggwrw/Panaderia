package panaderia.servicios;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import panaderia.excepciones.ResourceNotFoundException;
import panaderia.productos.Product;
import panaderia.productos.ProductRepository;

import java.util.List;

@Service
@Transactional
public class ProductService {

    private final ProductRepository productRepository;

    public ProductService(ProductRepository productRepository) {
        this.productRepository = productRepository;
    }

    public List<Product> findAll() {
        return productRepository.findAll();
    }

    public List<Product> search(String query) {
        if (query == null || query.trim().isEmpty()) {
            return productRepository.findAll();
        }
        return productRepository.searchByNombreOrCategoria(query.trim());
    }

    public Product findById(Long id) {
        return productRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Producto", id));
    }

    public Product create(Product product) {
        return productRepository.save(product);
    }

    public Product update(Long id, Product updatedProduct) {
        Product existingProduct = findById(id);
        
        existingProduct.setNombre(updatedProduct.getNombre());
        existingProduct.setDescripcion(updatedProduct.getDescripcion());
        existingProduct.setPrecio(updatedProduct.getPrecio());
        existingProduct.setCategoria(updatedProduct.getCategoria());
        existingProduct.setImageUrl(updatedProduct.getImageUrl());
        existingProduct.setStock(updatedProduct.getStock());
        
        return productRepository.save(existingProduct);
    }

    public void delete(Long id) {
        if (!productRepository.existsById(id)) {
            throw new ResourceNotFoundException("Producto", id);
        }
        productRepository.deleteById(id);
    }

    public Product updateStock(Long id, int newStock) {
        Product product = findById(id);
        product.setStock(newStock);
        return productRepository.save(product);
    }
}
