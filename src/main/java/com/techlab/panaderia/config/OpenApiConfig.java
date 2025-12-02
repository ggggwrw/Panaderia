package com.techlab.panaderia.config;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Contact;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.info.License;
import io.swagger.v3.oas.models.servers.Server;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.List;

@Configuration
public class OpenApiConfig {
    
    @Bean
    public OpenAPI customOpenAPI() {
        return new OpenAPI()
                .info(new Info()
                        .title("Panadería API")
                        .version("1.0.0")
                        .description("API REST para el Sistema de Gestión de Panadería. " +
                                "Permite la gestión de productos y pedidos con validación de stock, " +
                                "manejo de estados de pedido y cálculo automático de totales.")
                        .contact(new Contact()
                                .name("TechLab")
                                .email("soporte@techlab.com"))
                        .license(new License()
                                .name("MIT License")
                                .url("https://opensource.org/licenses/MIT")))
                .servers(List.of(
                        new Server().url("http://localhost:8080").description("Servidor de desarrollo")
                ));
    }
}
