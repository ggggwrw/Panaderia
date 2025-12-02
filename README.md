# Panaderia API

REST API para el sistema de gestión de panadería. Permite gestionar productos, pedidos e inventario.

## 📋 Requisitos Previos

- Java 17 o superior
- Maven 3.6+

## 🚀 Cómo Ejecutar

### Desarrollo (H2 Database)

```bash
# Clonar el repositorio
git clone https://github.com/ggggwrw/Panaderia.git
cd Panaderia

# Compilar y ejecutar
mvn spring-boot:run
```

La aplicación estará disponible en: `http://localhost:8080`

### Consola H2

Accede a la consola H2 para visualizar la base de datos:
- URL: `http://localhost:8080/h2-console`
- JDBC URL: `jdbc:h2:mem:panaderiadb`
- Usuario: `sa`
- Password: (vacío)

### Ejecutar Tests

```bash
mvn test
```

## 📚 Documentación API

### Swagger UI

Accede a la documentación interactiva de la API:
- Swagger UI: `http://localhost:8080/swagger-ui.html`
- OpenAPI JSON: `http://localhost:8080/v3/api-docs`

## 🔗 Endpoints

### Productos

| Método | Endpoint | Descripción |
|--------|----------|-------------|
| GET | `/api/products` | Listar todos los productos |
| GET | `/api/products?search={query}` | Buscar por nombre o categoría |
| GET | `/api/products/{id}` | Obtener producto por ID |
| POST | `/api/products` | Crear nuevo producto |
| PUT | `/api/products/{id}` | Actualizar producto |
| DELETE | `/api/products/{id}` | Eliminar producto |

### Pedidos

| Método | Endpoint | Descripción |
|--------|----------|-------------|
| POST | `/api/orders` | Crear nuevo pedido |
| GET | `/api/orders/{id}` | Obtener pedido por ID |
| PUT | `/api/orders/{id}/status` | Actualizar estado del pedido |
| GET | `/api/users/{userId}/orders` | Historial de pedidos por usuario |

## 📝 Ejemplos de Uso

### Crear Producto

**Request:**
```bash
curl -X POST http://localhost:8080/api/products \
  -H "Content-Type: application/json" \
  -d '{
    "nombre": "Pan de Centeno",
    "descripcion": "Pan artesanal de centeno integral",
    "precio": 4.50,
    "categoria": "Panes",
    "imageUrl": "https://example.com/pan-centeno.jpg",
    "stock": 30
  }'
```

**Response (201 Created):**
```json
{
  "id": 11,
  "nombre": "Pan de Centeno",
  "descripcion": "Pan artesanal de centeno integral",
  "precio": 4.50,
  "categoria": "Panes",
  "imageUrl": "https://example.com/pan-centeno.jpg",
  "stock": 30,
  "version": 0
}
```

### Listar Productos

**Request:**
```bash
curl http://localhost:8080/api/products
```

### Buscar Productos

**Request:**
```bash
curl "http://localhost:8080/api/products?search=pan"
```

### Crear Pedido

**Request:**
```bash
curl -X POST http://localhost:8080/api/orders \
  -H "Content-Type: application/json" \
  -d '{
    "userId": 1,
    "items": [
      {"productId": 1, "cantidad": 2},
      {"productId": 2, "cantidad": 3}
    ]
  }'
```

**Response (201 Created):**
```json
{
  "id": 1,
  "userId": 1,
  "fechaCreacion": "2024-01-15T10:30:00",
  "total": 10.75,
  "status": "PENDING",
  "lineas": [
    {
      "id": 1,
      "productId": 1,
      "productName": "Pan de Campo",
      "cantidad": 2,
      "precioUnitario": 3.50,
      "subtotal": 7.00
    },
    {
      "id": 2,
      "productId": 2,
      "productName": "Croissant",
      "cantidad": 3,
      "precioUnitario": 1.25,
      "subtotal": 3.75
    }
  ]
}
```

### Confirmar Pedido (Actualizar Estado)

**Request:**
```bash
curl -X PUT http://localhost:8080/api/orders/1/status \
  -H "Content-Type: application/json" \
  -d '{"status": "CONFIRMED"}'
```

**Response (200 OK):**
```json
{
  "id": 1,
  "userId": 1,
  "fechaCreacion": "2024-01-15T10:30:00",
  "total": 10.75,
  "status": "CONFIRMED",
  "lineas": [...]
}
```

## ⚠️ Manejo de Errores

### Producto No Encontrado (404)

```json
{
  "timestamp": "2024-01-15T10:30:00",
  "status": 404,
  "error": "Not Found",
  "message": "Producto con id 99 no encontrado"
}
```

### Stock Insuficiente (409)

```json
{
  "timestamp": "2024-01-15T10:30:00",
  "status": 409,
  "error": "Conflict",
  "message": "Stock insuficiente para 'Pan de Campo': solicitado 100, disponible 50"
}
```

### Validación de Campos (400)

```json
{
  "timestamp": "2024-01-15T10:30:00",
  "status": 400,
  "error": "Validation Error",
  "errors": {
    "nombre": "El nombre es obligatorio",
    "precio": "El precio debe ser mayor que 0"
  }
}
```

## 📁 Estructura del Proyecto

```
src/
├── main/
│   ├── java/panaderia/
│   │   ├── PanaderiaApplication.java    # Clase principal
│   │   ├── config/                      # Configuraciones (OpenAPI)
│   │   ├── controllers/                 # Controladores REST
│   │   ├── dto/                         # Data Transfer Objects
│   │   ├── excepciones/                 # Excepciones personalizadas
│   │   ├── pedidos/                     # Entidades de pedidos
│   │   ├── productos/                   # Entidades de productos
│   │   ├── repositories/                # Repositorios JPA
│   │   └── servicios/                   # Servicios de negocio
│   └── resources/
│       ├── application.properties       # Configuración
│       ├── schema.sql                   # Esquema de BD
│       └── data.sql                     # Datos iniciales
└── test/
    └── java/panaderia/
        └── servicios/                   # Tests de servicios
```

## 🔧 Configuración

### H2 (Desarrollo - Por defecto)

```properties
spring.datasource.url=jdbc:h2:mem:panaderiadb
spring.datasource.driverClassName=org.h2.Driver
```

### MySQL (Producción)

Descomenta las siguientes líneas en `application.properties`:

```properties
spring.datasource.url=jdbc:mysql://localhost:3306/panaderia
spring.datasource.driverClassName=com.mysql.cj.jdbc.Driver
spring.datasource.username=root
spring.datasource.password=your_password
spring.jpa.database-platform=org.hibernate.dialect.MySQLDialect
```

## 📊 Estados de Pedido

| Estado | Descripción | Transiciones Válidas |
|--------|-------------|---------------------|
| PENDING | Pedido creado, pendiente de confirmación | CONFIRMED, CANCELED |
| CONFIRMED | Pedido confirmado, stock decrementado | SHIPPED, CANCELED |
| SHIPPED | Pedido enviado | DELIVERED, CANCELED |
| DELIVERED | Pedido entregado | (estado final) |
| CANCELED | Pedido cancelado | (estado final) |

## 🛡️ Validaciones

- **Productos:**
  - Nombre: obligatorio, no vacío
  - Precio: obligatorio, mayor que 0 (BigDecimal)
  - Stock: no puede ser negativo

- **Pedidos:**
  - Debe contener al menos un item
  - Cantidad de cada item debe ser >= 1
  - Se valida existencia de productos
  - Se valida stock suficiente antes de crear pedido
  - Al confirmar, se decrementa stock transaccionalmente
  - Al cancelar pedido confirmado/enviado, se restaura stock

## 🔒 Concurrencia

- Uso de `@Version` (Optimistic Locking) en entidades para prevenir race conditions
- Transacciones en operaciones críticas de stock

## 📦 Tecnologías

- **Java 17**
- **Spring Boot 3.2**
- **Spring Data JPA**
- **H2 Database** (desarrollo)
- **MySQL** (producción)
- **Lombok**
- **SpringDoc OpenAPI** (Swagger)
- **Maven**

---

## Legacy: Aplicación de Consola

La aplicación original de consola se encuentra en `src/com/panaderia/` para referencia histórica.
