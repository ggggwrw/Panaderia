package panaderia.servicios;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import panaderia.dto.ProductDTO;
import panaderia.excepciones.ResourceNotFoundException;
import panaderia.productos.Product;
import panaderia.repositories.ProductRepository;

import java.util.List;

@Service
@RequiredArgsConstructor
public class ProductService {
    
    private final ProductRepository productRepository;
    
    @Transactional(readOnly = true)
    public List<Product> findAll() {
        return productRepository.findAll();
    }
    
    @Transactional(readOnly = true)
    public List<Product> search(String query) {
        if (query == null || query.trim().isEmpty()) {
            return productRepository.findAll();
        }
        return productRepository.searchByNombreOrCategoria(query.trim());
    }
    
    @Transactional(readOnly = true)
    public Product findById(Long id) {
        return productRepository.findById(id)
            .orElseThrow(() -> new ResourceNotFoundException("Producto", id));
    }
    
    @Transactional
    public Product create(ProductDTO dto) {
        Product product = Product.builder()
            .nombre(dto.getNombre())
            .descripcion(dto.getDescripcion())
            .precio(dto.getPrecio())
            .categoria(dto.getCategoria())
            .imageUrl(dto.getImageUrl())
            .stock(dto.getStock())
            .build();
        return productRepository.save(product);
    }
    
    @Transactional
    public Product update(Long id, ProductDTO dto) {
        Product product = findById(id);
        product.setNombre(dto.getNombre());
        product.setDescripcion(dto.getDescripcion());
        product.setPrecio(dto.getPrecio());
        product.setCategoria(dto.getCategoria());
        product.setImageUrl(dto.getImageUrl());
        product.setStock(dto.getStock());
        return productRepository.save(product);
    }
    
    @Transactional
    public void delete(Long id) {
        if (!productRepository.existsById(id)) {
            throw new ResourceNotFoundException("Producto", id);
        }
        productRepository.deleteById(id);
    }
    
    @Transactional
    public Product updateStock(Long id, int stockChange) {
        Product product = findById(id);
        int newStock = product.getStock() + stockChange;
        if (newStock < 0) {
            throw new IllegalArgumentException("El stock no puede ser negativo");
        }
        product.setStock(newStock);
        return productRepository.save(product);
    }
}
