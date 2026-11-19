package org.cybersaarthi.common.config;

import io.swagger.v3.oas.models.Components;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Contact;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.info.License;
import io.swagger.v3.oas.models.security.SecurityRequirement;
import io.swagger.v3.oas.models.security.SecurityScheme;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class OpenApiConfig {

    private static final String SECURITY_SCHEME_NAME = "BearerAuth";

    @Bean
    public OpenAPI cyberSaarthiOpenAPI() {
        return new OpenAPI()
            .info(new Info()
                .title("CyberSaarthi REST API")
                .description("Authoritative backend REST API for CyberSaarthi — Cybercrime Awareness and Incident Reporting Portal.")
                .version("1.0.0")
                .contact(new Contact()
                    .name("CyberSaarthi Development Team")
                    .url("https://github.com/Gauravkadam-web/CyberSaarthi"))
                .license(new License()
                    .name("MIT")
                    .url("https://opensource.org/licenses/MIT")))
            .addSecurityItem(new SecurityRequirement().addList(SECURITY_SCHEME_NAME))
            .components(new Components()
                .addSecuritySchemes(SECURITY_SCHEME_NAME, new SecurityScheme()
                    .name(SECURITY_SCHEME_NAME)
                    .type(SecurityScheme.Type.HTTP)
                    .scheme("bearer")
                    .bearerFormat("JWT")
                    .description("Provide valid Bearer JWT token issued by /api/v1/auth/login or /api/v1/auth/register")));
    }
}
