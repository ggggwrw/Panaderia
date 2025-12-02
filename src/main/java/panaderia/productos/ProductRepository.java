package panaderia.productos;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ProductRepository extends JpaRepository<Product, Long> {

    @Query("SELECT p FROM Product p WHERE LOWER(p.nombre) LIKE LOWER(CONCAT('%', :search, '%')) " +
           "OR LOWER(p.categoria) LIKE LOWER(CONCAT('%', :search, '%'))")
    List<Product> searchByNombreOrCategoria(@Param("search") String search);

    List<Product> findByCategoria(String categoria);
}
