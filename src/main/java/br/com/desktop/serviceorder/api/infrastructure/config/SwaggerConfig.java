package br.com.desktop.serviceorder.api.infrastructure.config;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Info;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class SwaggerConfig {

    @Bean
    public OpenAPI serviceOrderOpenApi() {
        return new OpenAPI()
                .info(new Info()
                        .title("Service Order API")
                        .description("API de Gestão de Ordens de Serviço")
                        .version("v1"));
    }
}
