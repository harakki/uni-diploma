package dev.harakki.comics.shared.config;

import io.swagger.v3.oas.models.Components;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Contact;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.media.Content;
import io.swagger.v3.oas.models.media.MediaType;
import io.swagger.v3.oas.models.media.Schema;
import io.swagger.v3.oas.models.responses.ApiResponse;
import io.swagger.v3.oas.models.security.SecurityRequirement;
import io.swagger.v3.oas.models.security.SecurityScheme;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.ProblemDetail;

import java.util.Map;

@Configuration
public class OpenApiConfig {

    @Bean
    public OpenAPI customOpenAPI() {
        return new OpenAPI()
                .info(apiInfo())
                .components(new Components()
                        .securitySchemes(Map.of(
                                "bearer-jwt", new SecurityScheme()
                                        .type(SecurityScheme.Type.HTTP)
                                        .scheme("bearer")
                                        .bearerFormat("JWT")
                                        .description("JWT token obtained from Keycloak")
                        ))
                        .responses(commonResponses())
                        .schemas(Map.of(
                                "ProblemDetail", problemDetailSchema()
                        ))
                )
                .addSecurityItem(new SecurityRequirement().addList("bearer-jwt"));
    }

    private Info apiInfo() {
        return new Info()
                .title("Comics API")
                .version("1.0.0")
                .description("RESTful API for managing comics service.")
                .contact(new Contact()
                        .name("harakki")
                        .url("https://github.com/harakki"));
    }

    private Map<String, ApiResponse> commonResponses() {
        return Map.of(
                "BadRequest", new ApiResponse()
                        .description("Validation error or malformed request")
                        .content(problemContent()),
                "NotFound", new ApiResponse()
                        .description("Resource not found")
                        .content(problemContent()),
                "Conflict", new ApiResponse()
                        .description("Resource conflict (e.g., duplicate)")
                        .content(problemContent()),
                "Unauthorized", new ApiResponse()
                        .description("Missing or invalid authentication token"),
                "Forbidden", new ApiResponse()
                        .description("Insufficient permissions"),
                "InternalServerError", new ApiResponse()
                        .description("Unexpected server error")
                        .content(problemContent())
        );
    }

    private Content problemContent() {
        return new Content().addMediaType(
                "application/problem+json",
                new MediaType().schema(new Schema<>().$ref("#/components/schemas/ProblemDetail"))
        );
    }

    @SuppressWarnings("unchecked")
    private Schema<ProblemDetail> problemDetailSchema() {
        return new Schema<ProblemDetail>()
                .type("object")
                .description("RFC 9457 Problem Details")
                .addProperty("type", new Schema<>().type("string").format("uri").description("URI reference identifying the problem type"))
                .addProperty("title", new Schema<>().type("string").description("Short human-readable summary"))
                .addProperty("status", new Schema<>().type("integer").format("int32").description("HTTP status code"))
                .addProperty("detail", new Schema<>().type("string").description("Human-readable explanation"))
                .addProperty("instance", new Schema<>().type("string").format("uri").description("URI reference identifying the specific occurrence"));
    }

}
