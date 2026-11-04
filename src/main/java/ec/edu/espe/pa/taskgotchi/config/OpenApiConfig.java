package ec.edu.espe.pa.taskgotchi.config;

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

    private static final String BEARER = "bearerAuth";

    @Bean
    public OpenAPI taskgotchiOpenAPI() {
        return new OpenAPI()
                .info(new Info()
                        .title("Taskgotchi REST API")
                        .description("Plataforma web para estudiantes que administra tareas y hábitos mientras cuidan una mascota virtual. "
                                + "Obtén un token en /api/v1/auth/login o /api/v1/auth/register y pulsa Authorize.")
                        .version("v1.0.0")
                        .contact(new Contact().name("Taskgotchi Team")))
                .components(new Components().addSecuritySchemes(BEARER, new SecurityScheme()
                        .type(SecurityScheme.Type.HTTP).scheme("bearer").bearerFormat("JWT")))
                .addSecurityItem(new SecurityRequirement().addList(BEARER));
    }
}
