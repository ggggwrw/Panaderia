package com.techlab.panaderia.productos;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ProductRepository extends JpaRepository<Product, Long> {
    
    /**
     * Search products by name or category containing the search term (case-insensitive)
     */
    @Query("SELECT p FROM Product p WHERE LOWER(p.nombre) LIKE LOWER(CONCAT('%', :search, '%')) " +
           "OR LOWER(p.categoria) LIKE LOWER(CONCAT('%', :search, '%'))")
    List<Product> searchByNameOrCategory(@Param("search") String search);
    
    /**
     * Find all products by category
     */
    List<Product> findByCategoria(String categoria);
    
    /**
     * Find products with stock greater than zero
     */
    List<Product> findByStockGreaterThan(Integer stock);
}
