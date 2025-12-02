package panaderia.config;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Contact;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.info.License;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class OpenApiConfig {
    
    @Bean
    public OpenAPI customOpenAPI() {
        return new OpenAPI()
            .info(new Info()
                .title("Panaderia API")
                .version("1.0.0")
                .description("REST API para el sistema de gestión de panadería. " +
                           "Permite gestionar productos, pedidos e inventario.")
                .contact(new Contact()
                    .name("Panaderia Team")
                    .email("soporte@panaderia.com"))
                .license(new License()
                    .name("MIT License")
                    .url("https://opensource.org/licenses/MIT")));
    }
}
