package com.senai.backend.config;

import io.swagger.v3.oas.annotations.OpenAPIDefinition;
import io.swagger.v3.oas.annotations.info.Contact;
import io.swagger.v3.oas.annotations.info.Info;
import org.springframework.context.annotation.Configuration;

@Configuration
@OpenAPIDefinition(info = @Info(
        title = "Passe — API de Gestão da Liga",
        version = "v1",
        description = "API REST para jogadores, clubes, mercado, escalações, partidas e classificação.",
        contact = @Contact(name = "Projeto Passe")
))
public class OpenApiConfig {
}
