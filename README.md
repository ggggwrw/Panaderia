# Panadería API REST 🥐

API REST para la gestión de una panadería, desarrollada con Spring Boot.

## Características

- **Gestión de Productos**: CRUD completo con búsqueda por nombre/categoría
- **Gestión de Pedidos**: Creación, actualización de estado, historial por usuario
- **Validación de Stock**: Control de inventario con excepciones personalizadas
- **BigDecimal para Precios**: Precisión decimal para valores monetarios
- **Optimistic Locking**: Control de concurrencia con @Version
- **Base de datos H2**: Para desarrollo y pruebas
- **OpenAPI/Swagger**: Documentación interactiva de la API

## Tecnologías

- Java 17
- Spring Boot 3.2.0
- Spring Data JPA
- H2 Database (desarrollo)
- MySQL (producción)
- Swagger/OpenAPI 3

## Requisitos

- Java 17 o superior
- Maven 3.6+

## Instalación y Ejecución Local

```bash
# Clonar el repositorio
git clone https://github.com/ggggwrw/Panaderia.git
cd Panaderia

# Compilar el proyecto
mvn clean install

# Ejecutar la aplicación
mvn spring-boot:run
```

La aplicación estará disponible en: `http://localhost:8080`

## Documentación API

- **Swagger UI**: http://localhost:8080/swagger-ui.html
- **OpenAPI JSON**: http://localhost:8080/v3/api-docs
- **H2 Console**: http://localhost:8080/h2-console
  - JDBC URL: `jdbc:h2:mem:panaderiadb`
  - Usuario: `sa`
  - Password: (vacío)

## Endpoints

### Productos (`/api/products`)

| Método | Endpoint | Descripción |
|--------|----------|-------------|
| GET | `/api/products` | Listar todos los productos |
| GET | `/api/products?search={query}` | Buscar productos por nombre o categoría |
| GET | `/api/products/{id}` | Obtener producto por ID |
| POST | `/api/products` | Crear nuevo producto |
| PUT | `/api/products/{id}` | Actualizar producto |
| DELETE | `/api/products/{id}` | Eliminar producto |

### Pedidos (`/api/orders`)

| Método | Endpoint | Descripción |
|--------|----------|-------------|
| POST | `/api/orders` | Crear nuevo pedido |
| GET | `/api/orders/{id}` | Obtener pedido por ID |
| PUT | `/api/orders/{id}/status` | Actualizar estado del pedido |
| GET | `/api/users/{userId}/orders` | Obtener pedidos de un usuario |

## Ejemplos de Request/Response

### Crear Producto

**Request:**
```http
POST /api/products
Content-Type: application/json

{
    "nombre": "Pan Integral",
    "descripcion": "Pan elaborado con harina integral",
    "precio": 4.00,
    "categoria": "Panes",
    "imageUrl": "https://example.com/pan-integral.jpg",
    "stock": 25
}
```

**Response (201 Created):**
```json
{
    "id": 9,
    "nombre": "Pan Integral",
    "descripcion": "Pan elaborado con harina integral",
    "precio": 4.00,
    "categoria": "Panes",
    "imageUrl": "https://example.com/pan-integral.jpg",
    "stock": 25,
    "version": 0
}
```

### Crear Pedido

**Request:**
```http
POST /api/orders
Content-Type: application/json

{
    "userId": 1,
    "items": [
        {"productId": 1, "cantidad": 2},
        {"productId": 2, "cantidad": 5}
    ]
}
```

**Response (201 Created):**
```json
{
    "id": 1,
    "userId": 1,
    "fechaCreacion": "2024-01-15T10:30:00",
    "total": 13.25,
    "status": "PENDING",
    "lineas": [
        {"id": 1, "productId": 1, "cantidad": 2, "precioUnitario": 3.50},
        {"id": 2, "productId": 2, "cantidad": 5, "precioUnitario": 1.25}
    ]
}
```

### Actualizar Estado de Pedido

**Request:**
```http
PUT /api/orders/1/status
Content-Type: application/json

{
    "status": "CONFIRMED"
}
```

**Response (200 OK):**
```json
{
    "id": 1,
    "userId": 1,
    "fechaCreacion": "2024-01-15T10:30:00",
    "total": 13.25,
    "status": "CONFIRMED",
    "lineas": [...]
}
```

## Estados de Pedido

| Estado | Descripción |
|--------|-------------|
| PENDING | Pedido creado, pendiente de confirmación |
| CONFIRMED | Pedido confirmado, stock descontado |
| SHIPPED | Pedido enviado |
| DELIVERED | Pedido entregado |
| CANCELED | Pedido cancelado (stock restaurado si fue confirmado) |

### Transiciones de Estado Válidas

```
PENDING → CONFIRMED → SHIPPED → DELIVERED
    ↓          ↓          ↓
 CANCELED   CANCELED   CANCELED
```

## Manejo de Errores

### Error 400 - Bad Request (Validación)
```json
{
    "status": 400,
    "message": "Error de validación",
    "errors": {
        "nombre": "El nombre es obligatorio",
        "precio": "El precio no puede ser negativo"
    },
    "timestamp": "2024-01-15T10:30:00"
}
```

### Error 404 - Not Found
```json
{
    "status": 404,
    "message": "Producto con ID 99 no encontrado",
    "timestamp": "2024-01-15T10:30:00"
}
```

### Error 409 - Conflict (Stock Insuficiente)
```json
{
    "status": 409,
    "message": "Stock insuficiente para 'Pan de Campo': solicitado 100, disponible 50",
    "timestamp": "2024-01-15T10:30:00"
}
```

## Ejecutar Tests

```bash
# Ejecutar todos los tests
mvn test

# Ejecutar tests con reporte detallado
mvn test -Dtest=ProductServiceTest

mvn test -Dtest=OrderServiceTest
```

## Configuración

### H2 (Desarrollo - por defecto)
El archivo `application.properties` viene configurado para H2.

### MySQL (Producción)
Descomentar las líneas de MySQL en `application.properties`:

```properties
spring.datasource.url=jdbc:mysql://localhost:3306/panaderia?useSSL=false&serverTimezone=UTC
spring.datasource.driverClassName=com.mysql.cj.jdbc.Driver
spring.datasource.username=root
spring.datasource.password=your_password
spring.jpa.database-platform=org.hibernate.dialect.MySQLDialect
spring.jpa.hibernate.ddl-auto=update
spring.sql.init.mode=never
```

## Estructura del Proyecto

```
src/
├── main/
│   ├── java/panaderia/
│   │   ├── PanaderiaApplication.java    # Clase principal
│   │   ├── config/                       # Configuraciones
│   │   ├── controllers/                  # Controladores REST
│   │   │   ├── ProductController.java
│   │   │   └── OrderController.java
│   │   ├── excepciones/                  # Excepciones personalizadas
│   │   │   ├── BadRequestException.java
│   │   │   ├── GlobalExceptionHandler.java
│   │   │   ├── ResourceNotFoundException.java
│   │   │   └── StockInsuficienteException.java
│   │   ├── pedidos/                      # Dominio de pedidos
│   │   │   ├── CreateOrderRequest.java
│   │   │   ├── LineaPedido.java
│   │   │   ├── OrderItemRequest.java
│   │   │   ├── OrderStatus.java
│   │   │   ├── Pedido.java
│   │   │   ├── PedidoRepository.java
│   │   │   └── UpdateStatusRequest.java
│   │   ├── productos/                    # Dominio de productos
│   │   │   ├── Product.java
│   │   │   └── ProductRepository.java
│   │   └── servicios/                    # Servicios de negocio
│   │       ├── OrderService.java
│   │       └── ProductService.java
│   └── resources/
│       ├── application.properties        # Configuración
│       ├── schema.sql                    # Esquema de BD
│       └── data.sql                      # Datos de ejemplo
└── test/
    └── java/panaderia/servicios/
        ├── ProductServiceTest.java
        └── OrderServiceTest.java
```

## Licencia

Este proyecto es para fines educativos.
