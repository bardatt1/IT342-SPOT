package edu.cit.spot.config;

import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.ExampleObject;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

// Using fully qualified names to avoid name collisions

/**
 * Global API response annotations for SPOT system controllers.
 * This helps standardize the error documentation across all endpoints.
 */
public final class GlobalApiResponseConfig {

    /**
     * Standard API responses to be applied to all controller methods
     */
    @Target({ElementType.METHOD, ElementType.TYPE})
    @Retention(RetentionPolicy.RUNTIME)
    @ApiResponses(value = {
        @io.swagger.v3.oas.annotations.responses.ApiResponse(
            responseCode = "400", 
            description = "Bad Request - Invalid input data or parameters",
            content = @Content(
                mediaType = "application/json",
                schema = @Schema(implementation = edu.cit.spot.dto.ApiResponse.class),
                examples = @ExampleObject(
                    value = "{\n" +
                            "  \"result\": \"ERROR\",\n" +
                            "  \"message\": \"Invalid input parameters: Student ID must be a positive number\",\n" +
                            "  \"data\": null\n" +
                            "}"
                )
            )
        ),
        @io.swagger.v3.oas.annotations.responses.ApiResponse(
            responseCode = "401", 
            description = "Unauthorized - Authentication required or failed",
            content = @Content(
                mediaType = "application/json",
                schema = @Schema(implementation = edu.cit.spot.dto.ApiResponse.class),
                examples = @ExampleObject(
                    value = "{\n" +
                            "  \"result\": \"ERROR\",\n" +
                            "  \"message\": \"Authentication required. Please login or provide a valid JWT token\",\n" +
                            "  \"data\": null\n" +
                            "}"
                )
            )
        ),
        @io.swagger.v3.oas.annotations.responses.ApiResponse(
            responseCode = "403", 
            description = "Forbidden - Insufficient permissions",
            content = @Content(
                mediaType = "application/json",
                schema = @Schema(implementation = edu.cit.spot.dto.ApiResponse.class),
                examples = @ExampleObject(
                    value = "{\n" +
                            "  \"result\": \"ERROR\",\n" +
                            "  \"message\": \"You do not have permission to access this section's attendance data\",\n" +
                            "  \"data\": null\n" +
                            "}"
                )
            )
        ),
        @io.swagger.v3.oas.annotations.responses.ApiResponse(
            responseCode = "404", 
            description = "Not Found - Resource not found",
            content = @Content(
                mediaType = "application/json", 
                schema = @Schema(implementation = edu.cit.spot.dto.ApiResponse.class),
                examples = @ExampleObject(
                    value = "{\n" +
                            "  \"result\": \"ERROR\",\n" +
                            "  \"message\": \"Student with ID 12345 not found in the system\",\n" +
                            "  \"data\": null\n" +
                            "}"
                )
            )
        ),
        @io.swagger.v3.oas.annotations.responses.ApiResponse(
            responseCode = "409", 
            description = "Conflict - Resource conflict or duplicate entry",
            content = @Content(
                mediaType = "application/json",
                schema = @Schema(implementation = edu.cit.spot.dto.ApiResponse.class),
                examples = @ExampleObject(
                    value = "{\n" +
                            "  \"result\": \"ERROR\",\n" +
                            "  \"message\": \"Student is already enrolled in this section\",\n" +
                            "  \"data\": null\n" +
                            "}"
                )
            )
        ),
        @io.swagger.v3.oas.annotations.responses.ApiResponse(
            responseCode = "500", 
            description = "Internal Server Error - Unexpected server error",
            content = @Content(
                mediaType = "application/json",
                schema = @Schema(implementation = edu.cit.spot.dto.ApiResponse.class),
                examples = @ExampleObject(
                    value = "{\n" +
                            "  \"result\": \"ERROR\",\n" +
                            "  \"message\": \"An unexpected error occurred while processing your request\",\n" +
                            "  \"data\": null\n" +
                            "}"
                )
            )
        )
    })
    public @interface StandardApiResponses {}
}
