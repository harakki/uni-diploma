package dev.harakki.comics.shared.api;

import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.ExampleObject;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import org.springframework.http.ProblemDetail;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Retention(RetentionPolicy.RUNTIME)
@Target({ElementType.TYPE, ElementType.METHOD})
@ApiResponses({
        @ApiResponse(
                responseCode = "400",
                description = "Bad Request",
                content = @Content(
                        mediaType = "application/problem+json",
                        schema = @Schema(implementation = ProblemDetail.class),
                        examples = @ExampleObject(
                                name = "Bad Request",
                                value = """
                                        {
                                          "title": "Bad Request",
                                          "status": 400,
                                          "detail": "Validation failed for 2 field(s).",
                                          "instance": "/api/v1/titles"
                                          "errors": {
                                            "name": "must not be blank",
                                            "releaseYear": "must be a valid year in the past or present"
                                          }
                                        }
                                        """
                        )
                )
        ),
        @ApiResponse(
                responseCode = "404",
                description = "Resource Not Found",
                content = @Content(
                        mediaType = "application/problem+json",
                        schema = @Schema(implementation = ProblemDetail.class),
                        examples = @ExampleObject(
                                name = "Resource Not Found",
                                value = """
                                        {
                                          "title": "Resource Not Found",
                                          "status": 404,
                                          "detail": "Title with id 019b9d1e-bc3a-70f3-8520-36e8d82dc9e0 not found",
                                          "instance": "/api/v1/titles/019b9d1e-bc3a-70f3-8520-36e8d82dc9e0"
                                        }
                                        """
                        )

                )
        ),
        @ApiResponse(
                responseCode = "409",
                description = "Conflict",
                content = @Content(
                        mediaType = "application/problem+json",
                        schema = @Schema(implementation = ProblemDetail.class),
                        examples = @ExampleObject(
                                name = "Conflict",
                                value = """
                                        {
                                          "title": "Resource Conflict",
                                          "status": 409,
                                          "detail": "Title with name 'Chainsaw Man' already exists",
                                          "instance": "/api/v1/titles"
                                        }
                                        """
                        )

                )
        ),
        @ApiResponse(
                responseCode = "500",
                description = "Internal Server Error",
                content = @Content(
                        mediaType = "application/problem+json",
                        schema = @Schema(implementation = ProblemDetail.class),
                        examples = @ExampleObject(
                                name = "Internal Server Error",
                                value = """
                                        {
                                          "title": "Internal Server Error",
                                          "status": 500,
                                          "detail": "An unexpected error occurred.",
                                          "instance": "/api/v1/titles"
                                        }
                                        """
                        )
                )
        )
})
public @interface ApiProblemResponses {
}
