package dev.harakki.comics.shared.config;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.info.Contact;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class OpenApiConfig {

    @Bean
    public OpenAPI customOpenAPI() {
        return new OpenAPI()
                .info(new Info()
                        .title("Comics API")
                        .version("1.0.0")
                        .description("RESTful API for managing comics service.")
                        .contact(new Contact().name("harakki").url("https://github.com/harakki"))
                );
    }

}
