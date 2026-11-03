package com.poultry.smartfarm.config;

import io.swagger.v3.oas.models.Components;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Contact;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.security.SecurityRequirement;
import io.swagger.v3.oas.models.security.SecurityScheme;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class OpenApiConfig {

    @Bean
    public OpenAPI smartPoultryOpenAPI() {
        final String basicSchemeName = "basicAuth";
        return new OpenAPI()
                .info(new Info()
                        .title("Smart Indian Poultry Farm Management System API")
                        .version("1.0.0 - Phase 1")
                        .description("REST API for Indian Poultry Farming, Breeds, Farms, Poultry Houses, RBAC & Multilingual Management.")
                        .contact(new Contact().name("Smart Indian Poultry Team")))
                .addSecurityItem(new SecurityRequirement().addList(basicSchemeName))
                .components(new Components()
                        .addSecuritySchemes(basicSchemeName,
                                new SecurityScheme()
                                        .name(basicSchemeName)
                                        .type(SecurityScheme.Type.HTTP)
                                        .scheme("basic")));
    }
}
