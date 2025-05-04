package edu.cit.spot.config;

import io.swagger.v3.oas.models.Components;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Contact;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.info.License;
import io.swagger.v3.oas.models.security.SecurityRequirement;
import io.swagger.v3.oas.models.security.SecurityScheme;
import io.swagger.v3.oas.models.servers.Server;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.Arrays;
import java.util.Collections;

/**
 * OpenAPI (Swagger) configuration for the SPOT system.
 * Provides documentation for all API endpoints.
 */
@Configuration
public class SwaggerConfig {

    /**
     * Configures the OpenAPI documentation for the SPOT system, including
     * authentication methods, servers, and contact information.
     * 
     * @return Configured OpenAPI instance
     */
    @Bean
    public OpenAPI spotOpenAPI() {
        final String securitySchemeName = "bearerAuth";
        
        return new OpenAPI()
            .info(new Info()
                .title("SPOT - School Presence and Orientation Tracker")
                .description("API Documentation for SPOT System - A comprehensive school attendance tracking system")
                .version("1.0")
                .contact(new Contact()
                    .name("SPOT Administration Team")
                    .email("admin@spot.edu")
                    .url("https://spot.edu"))
                .license(new License()
                    .name("Apache 2.0")
                    .url("https://www.apache.org/licenses/LICENSE-2.0")))
            .servers(Arrays.asList(
                new Server().url("https://spot-edu.me:8080").description("SPOT Development Server")
            ))
            // Add security scheme definition
            .components(new Components()
                .addSecuritySchemes(securitySchemeName, 
                    new SecurityScheme()
                        .name(securitySchemeName)
                        .type(SecurityScheme.Type.HTTP)
                        .scheme("bearer")
                        .bearerFormat("JWT")
                        .description(
                            "Enter JWT token in the format: Bearer eyJhbGciOiJIUzI1NiIsInR5cCI6Ik...")
                ))
            // Apply the security scheme globally
            .addSecurityItem(new SecurityRequirement().addList(securitySchemeName));
    }
}
