package com.example.camunda_client.workflow.config;

import io.swagger.v3.oas.models.Components;
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
    OpenAPI workflowOpenApi() {
        return new OpenAPI()
                .info(new Info()
                        .title("Spring Boot 4 Workflow Client API")
                        .version("0.0.1")
                        .description("Workflow abstraction API with Camunda 7 REST implementation.")
                        .contact(new Contact().name("Workflow Platform Team"))
                        .license(new License().name("Internal")))
                .servers(List.of(new Server().url("http://localhost:8081").description("Local server")))
                .components(new Components());
    }
}
