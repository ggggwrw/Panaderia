# Panadería API REST

API REST para el Sistema de Gestión de Panadería desarrollada con Spring Boot.

## Características

- ✅ CRUD completo de productos
- ✅ Gestión de pedidos con validación de stock
- ✅ Manejo de estados de pedido (PENDING, CONFIRMED, SHIPPED, DELIVERED, CANCELED)
- ✅ Cálculo automático de totales
- ✅ Validaciones de negocio
- ✅ Manejo global de excepciones
- ✅ Documentación OpenAPI/Swagger
- ✅ Base de datos H2 para desarrollo/testing
- ✅ Soporte para MySQL en producción

## Requisitos

- Java 17 o superior
- Maven 3.6 o superior

## Instalación y Ejecución

### 1. Clonar el repositorio

```bash
git clone https://github.com/ggggwrw/Panaderia.git
cd Panaderia
```

### 2. Compilar el proyecto

```bash
mvn clean install
```

### 3. Ejecutar la aplicación

```bash
mvn spring-boot:run
```

La aplicación estará disponible en: `http://localhost:8080`

### 4. Acceder a la documentación

- Swagger UI: http://localhost:8080/swagger-ui.html
- OpenAPI JSON: http://localhost:8080/v3/api-docs
- Consola H2: http://localhost:8080/h2-console (JDBC URL: `jdbc:h2:mem:panaderiadb`)

## Ejecutar Tests

```bash
mvn test
```

## Endpoints de la API

### Productos

| Método | Endpoint | Descripción |
|--------|----------|-------------|
| GET | `/api/products` | Listar todos los productos |
| GET | `/api/products?search={term}` | Buscar productos por nombre o categoría |
| GET | `/api/products/{id}` | Obtener detalle de un producto |
| POST | `/api/products` | Crear un nuevo producto |
| PUT | `/api/products/{id}` | Actualizar un producto |
| DELETE | `/api/products/{id}` | Eliminar un producto |

### Pedidos

| Método | Endpoint | Descripción |
|--------|----------|-------------|
| POST | `/api/orders` | Crear un nuevo pedido |
| GET | `/api/orders` | Listar todos los pedidos |
| GET | `/api/orders/{id}` | Obtener detalle de un pedido |
| GET | `/api/users/{userId}/orders` | Obtener historial de pedidos de un usuario |
| PUT | `/api/orders/{id}/status` | Actualizar estado del pedido |

## Ejemplos de Uso

### Crear un Producto

```bash
curl -X POST http://localhost:8080/api/products \
  -H "Content-Type: application/json" \
  -d '{
    "nombre": "Pan de Leche",
    "descripcion": "Pan suave con leche",
    "precio": 2.50,
    "categoria": "Panes",
    "stock": 100
  }'
```

**Respuesta (201 Created):**
```json
{
  "id": 11,
  "nombre": "Pan de Leche",
  "descripcion": "Pan suave con leche",
  "precio": 2.50,
  "categoria": "Panes",
  "imageUrl": null,
  "stock": 100,
  "version": 0
}
```

### Listar Productos

```bash
curl http://localhost:8080/api/products
```

### Buscar Productos

```bash
curl "http://localhost:8080/api/products?search=pan"
```

### Crear un Pedido

```bash
curl -X POST http://localhost:8080/api/orders \
  -H "Content-Type: application/json" \
  -d '{
    "userId": 1,
    "items": [
      {"productId": 1, "cantidad": 2},
      {"productId": 2, "cantidad": 5}
    ]
  }'
```

**Respuesta (201 Created):**
```json
{
  "id": 1,
  "userId": 1,
  "fechaCreacion": "2024-01-15T10:30:00",
  "total": 13.25,
  "status": "PENDING",
  "lineas": [
    {
      "id": 1,
      "productId": 1,
      "productName": "Pan de Campo",
      "cantidad": 2,
      "precioUnitario": 3.50
    },
    {
      "id": 2,
      "productId": 2,
      "productName": "Croissant",
      "cantidad": 5,
      "precioUnitario": 1.25
    }
  ]
}
```

### Confirmar un Pedido

```bash
curl -X PUT http://localhost:8080/api/orders/1/status \
  -H "Content-Type: application/json" \
  -d '{"status": "CONFIRMED"}'
```

### Cancelar un Pedido

```bash
curl -X PUT http://localhost:8080/api/orders/1/status \
  -H "Content-Type: application/json" \
  -d '{"status": "CANCELED"}'
```

## Respuestas de Error

### Producto No Encontrado (404)

```json
{
  "status": 404,
  "error": "Not Found",
  "message": "Producto no encontrado con id: '999'",
  "timestamp": "2024-01-15T10:30:00"
}
```

### Stock Insuficiente (409)

```json
{
  "status": 409,
  "error": "Stock Insuficiente",
  "message": "Stock insuficiente para producto 'Pan de Campo' (ID: 1). Solicitado: 100, Disponible: 50",
  "timestamp": "2024-01-15T10:30:00",
  "details": {
    "productId": 1,
    "productName": "Pan de Campo",
    "requestedQuantity": 100,
    "availableStock": 50
  }
}
```

### Error de Validación (400)

```json
{
  "status": 400,
  "error": "Validation Error",
  "message": "Error de validación en los campos proporcionados",
  "timestamp": "2024-01-15T10:30:00",
  "validationErrors": {
    "nombre": "El nombre es obligatorio",
    "precio": "El precio debe ser mayor que 0"
  }
}
```

## Estructura del Proyecto

```
src/
├── main/
│   ├── java/com/techlab/panaderia/
│   │   ├── PanaderiaApplication.java    # Aplicación principal
│   │   ├── config/                       # Configuraciones (OpenAPI)
│   │   ├── controllers/                  # Controladores REST
│   │   ├── excepciones/                  # Excepciones personalizadas
│   │   ├── pedidos/                      # Entidades y repos de pedidos
│   │   ├── productos/                    # Entidades y repos de productos
│   │   └── servicios/                    # Servicios de negocio
│   └── resources/
│       ├── application.properties        # Configuración de la aplicación
│       ├── schema.sql                    # Esquema de base de datos
│       └── data.sql                      # Datos de ejemplo
└── test/
    └── java/com/techlab/panaderia/
        └── servicios/                    # Tests de servicios
```

## Configuración para MySQL (Producción)

Para usar MySQL en producción, editar `application.properties`:

```properties
# Comentar configuración de H2
# spring.datasource.url=jdbc:h2:mem:panaderiadb
# ...

# Descomentar configuración de MySQL
spring.datasource.url=jdbc:mysql://localhost:3306/panaderia?useSSL=false&serverTimezone=UTC
spring.datasource.driverClassName=com.mysql.cj.jdbc.Driver
spring.datasource.username=root
spring.datasource.password=your_password
spring.jpa.database-platform=org.hibernate.dialect.MySQLDialect
spring.jpa.hibernate.ddl-auto=update
```

## Estados de Pedido

| Estado | Descripción | Transiciones Válidas |
|--------|-------------|----------------------|
| PENDING | Pedido creado, pendiente de confirmación | CONFIRMED, CANCELED |
| CONFIRMED | Pedido confirmado, stock descontado | SHIPPED, CANCELED* |
| SHIPPED | Pedido enviado | DELIVERED |
| DELIVERED | Pedido entregado | - |
| CANCELED | Pedido cancelado | - |

*Al cancelar un pedido CONFIRMED, se restaura el stock.

## Tecnologías Utilizadas

- Spring Boot 3.2.0
- Spring Data JPA
- H2 Database
- MySQL Connector
- Jakarta Validation
- SpringDoc OpenAPI
- JUnit 5

---

## Código Antiguo (Referencia)

El código original de la aplicación de consola se encuentra en `src/com/panaderia/` y sirve como referencia histórica del proyecto inicial.
