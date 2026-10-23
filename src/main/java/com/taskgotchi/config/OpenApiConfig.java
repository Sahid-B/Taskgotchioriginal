package com.taskgotchi.config;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.info.Contact;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class OpenApiConfig {

    @Bean
    public OpenAPI taskgotchiOpenAPI() {
        return new OpenAPI()
                .info(new Info()
                        .title("Taskgotchi REST API")
                        .description("Plataforma web para estudiantes que administra tareas y hábitos mientras cuidan una mascota virtual.")
                        .version("v1.0.0")
                        .contact(new Contact().name("Taskgotchi Team")));
    }
}
