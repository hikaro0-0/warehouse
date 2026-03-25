package com.hikaro.warehouse.config;

import io.swagger.v3.oas.annotations.OpenAPIDefinition;
import io.swagger.v3.oas.annotations.info.Contact;
import io.swagger.v3.oas.annotations.info.Info;
import io.swagger.v3.oas.annotations.info.License;
import io.swagger.v3.oas.annotations.servers.Server;
import org.springframework.context.annotation.Configuration;

@Configuration
@OpenAPIDefinition(
        info = @Info(
                title = "Warehouse Accounting REST API",
                version = "v1",
                description = "API for managing warehouses, suppliers, categories, products, shipments, and transaction demos.",
                contact = @Contact(name = "Hikaro"),
                license = @License(name = "Internal use")
        ),
        servers = @Server(url = "http://localhost:8080", description = "Local environment")
)
public class OpenApiConfig {
}
