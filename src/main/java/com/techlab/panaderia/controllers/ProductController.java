package com.techlab.panaderia.controllers;

import com.techlab.panaderia.productos.Product;
import com.techlab.panaderia.servicios.ProductService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/products")
@Tag(name = "Productos", description = "API para gestión de productos de panadería")
public class ProductController {
    
    private final ProductService productService;
    
    public ProductController(ProductService productService) {
        this.productService = productService;
    }
    
    @Operation(summary = "Listar todos los productos", 
               description = "Obtiene una lista de todos los productos. Soporta búsqueda por nombre o categoría mediante el parámetro 'search'")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Lista de productos obtenida exitosamente",
                    content = @Content(mediaType = "application/json",
                            schema = @Schema(implementation = Product.class)))
    })
    @GetMapping
    public ResponseEntity<List<Product>> getAllProducts(
            @Parameter(description = "Término de búsqueda para filtrar por nombre o categoría")
            @RequestParam(required = false) String search) {
        List<Product> products = productService.searchProducts(search);
        return ResponseEntity.ok(products);
    }
    
    @Operation(summary = "Obtener producto por ID", 
               description = "Obtiene los detalles de un producto específico por su ID")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Producto encontrado",
                    content = @Content(mediaType = "application/json",
                            schema = @Schema(implementation = Product.class))),
            @ApiResponse(responseCode = "404", description = "Producto no encontrado",
                    content = @Content)
    })
    @GetMapping("/{id}")
    public ResponseEntity<Product> getProductById(
            @Parameter(description = "ID del producto") @PathVariable Long id) {
        Product product = productService.getProductById(id);
        return ResponseEntity.ok(product);
    }
    
    @Operation(summary = "Crear nuevo producto", 
               description = "Crea un nuevo producto con los datos proporcionados")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "201", description = "Producto creado exitosamente",
                    content = @Content(mediaType = "application/json",
                            schema = @Schema(implementation = Product.class))),
            @ApiResponse(responseCode = "400", description = "Datos inválidos",
                    content = @Content)
    })
    @PostMapping
    public ResponseEntity<Product> createProduct(@Valid @RequestBody Product product) {
        Product createdProduct = productService.createProduct(product);
        return new ResponseEntity<>(createdProduct, HttpStatus.CREATED);
    }
    
    @Operation(summary = "Actualizar producto", 
               description = "Actualiza los datos de un producto existente")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Producto actualizado exitosamente",
                    content = @Content(mediaType = "application/json",
                            schema = @Schema(implementation = Product.class))),
            @ApiResponse(responseCode = "404", description = "Producto no encontrado",
                    content = @Content),
            @ApiResponse(responseCode = "400", description = "Datos inválidos",
                    content = @Content)
    })
    @PutMapping("/{id}")
    public ResponseEntity<Product> updateProduct(
            @Parameter(description = "ID del producto") @PathVariable Long id,
            @Valid @RequestBody Product productDetails) {
        Product updatedProduct = productService.updateProduct(id, productDetails);
        return ResponseEntity.ok(updatedProduct);
    }
    
    @Operation(summary = "Eliminar producto", 
               description = "Elimina un producto por su ID")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "204", description = "Producto eliminado exitosamente"),
            @ApiResponse(responseCode = "404", description = "Producto no encontrado",
                    content = @Content)
    })
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteProduct(
            @Parameter(description = "ID del producto") @PathVariable Long id) {
        productService.deleteProduct(id);
        return ResponseEntity.noContent().build();
    }
}
