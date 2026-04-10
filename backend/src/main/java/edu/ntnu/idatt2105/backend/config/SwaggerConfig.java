package edu.ntnu.idatt2105.backend.config;

import io.swagger.v3.oas.models.Components;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.security.SecurityRequirement;
import io.swagger.v3.oas.models.security.SecurityScheme;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * Configures the OpenAPI (Swagger) documentation for the IK Compliance API.
 *
 * <p>Registers a global Bearer JWT security scheme so that the Swagger UI
 * includes an "Authorize" button for entering tokens during manual testing.
 */
@Configuration
public class SwaggerConfig {

  /**
   * Builds the {@link OpenAPI} bean with API metadata and a Bearer JWT security scheme.
   *
   * @return the customized {@link OpenAPI} configuration
   */
  @Bean
  public OpenAPI customOpenAPI() {
    return new OpenAPI()
        .info(new Info()
            .title("IK Compliance API")
            .version("0.1.0")
            .description("Internal control system")
        )
        .addSecurityItem(new SecurityRequirement().addList("bearerAuth"))
        .components(new Components()
            .addSecuritySchemes("bearerAuth", new SecurityScheme()
                .name("bearerAuth")
                .type(SecurityScheme.Type.HTTP)
                .scheme("bearer")
                .bearerFormat("JWT")));
  }
}
