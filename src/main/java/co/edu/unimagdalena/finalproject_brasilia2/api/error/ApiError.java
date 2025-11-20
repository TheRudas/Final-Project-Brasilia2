package co.edu.unimagdalena.finalproject_brasilia2.api.error;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonInclude;
import org.springframework.http.HttpStatus;

import java.time.OffsetDateTime;
import java.util.List;

/**
 * Unified API error response using Java records
 * Immutable and clean structure for all error responses
 *
 * @author AFGamero
 * @since 2025-11-20
 */
@JsonInclude(JsonInclude.Include.NON_NULL)
public record ApiError(
        @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd'T'HH:mm:ss.SSSXXX")
        OffsetDateTime timestamp,
        int status,
        String error,
        String message,
        String path,
        List<FieldViolation> violations
) {
    /**
     * Factory method for errors WITH field violations (validation errors)
     *
     * @param status HTTP status code
     * @param message Error message
     * @param path Request path
     * @param violations List of field-level validation errors
     * @return ApiError instance
     */
    public static ApiError of(HttpStatus status, String message, String path, List<FieldViolation> violations) {
        return new ApiError(
                OffsetDateTime.now(),
                status.value(),
                status.getReasonPhrase(),
                message,
                path,
                violations.isEmpty() ? null : violations
        );
    }

    /**
     * Factory method for errors WITHOUT field violations (simple errors)
     *
     * @param status HTTP status code
     * @param message Error message
     * @param path Request path
     * @return ApiError instance
     */
    public static ApiError of(HttpStatus status, String message, String path) {
        return new ApiError(
                OffsetDateTime.now(),
                status.value(),
                status.getReasonPhrase(),
                message,
                path,
                null
        );
    }

    /**
     * Field-level validation error
     *
     * @param field Field name that failed validation
     * @param message Validation error message
     */
    public record FieldViolation(String field, String message) {}
}