package co.edu.unimagdalena.finalproject_brasilia2.api.error;

import co.edu.unimagdalena.finalproject_brasilia2.exceptions.NotFoundException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.ConstraintViolationException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.context.request.ServletWebRequest;
import org.springframework.web.context.request.WebRequest;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;

import javax.naming.AuthenticationException;
import java.nio.file.AccessDeniedException;
import java.util.List;

/**
 * Global exception handler for consistent API error responses
 * Handles all exceptions thrown by controllers and services
 *
 * @author AFGamero
 * @since 2025-11-20
 */
@Slf4j
@RestControllerAdvice
public class GlobalExceptionHandler {

    /**
     * Extract clean request path from WebRequest
     * Uses pattern matching for cleaner code
     */
    private String getPath(WebRequest req) {
        return req instanceof ServletWebRequest swr
                ? swr.getRequest().getRequestURI()
                : req.getDescription(false).replace("uri=", "");
    }

    /**
     * Extract client IP address for security logging
     */
    private String getClientIp(WebRequest req) {
        if (req instanceof ServletWebRequest swr) {
            HttpServletRequest request = swr.getRequest();
            String ip = request.getHeader("X-Forwarded-For");
            if (ip == null || ip.isEmpty()) {
                ip = request.getRemoteAddr();
            }
            return ip;
        }
        return "unknown";
    }

    /**
     * Handle NotFoundException (404 Not Found)
     * When a requested resource doesn't exist
     */
    @ExceptionHandler(NotFoundException.class)
    public ResponseEntity<ApiError> handleNotFound(NotFoundException ex, WebRequest req) {
        log.warn("Resource not found: {} | Path: {}", ex.getMessage(), getPath(req));
        var body = ApiError.of(HttpStatus.NOT_FOUND, ex.getMessage(), getPath(req));
        return ResponseEntity.status(HttpStatus.NOT_FOUND).body(body);
    }

    /**
     * Handle validation errors (400 Bad Request)
     * When @Valid annotation fails on request body
     * Provides field-level error details
     */
    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ApiError> handleValidation(MethodArgumentNotValidException ex, WebRequest req) {
        var violations = ex.getBindingResult().getFieldErrors()
                .stream()
                .map(fe -> new ApiError.FieldViolation(fe.getField(), fe.getDefaultMessage()))
                .toList();

        log.warn("Validation failed with {} violations on path: {}", violations.size(), getPath(req));

        var body = ApiError.of(
                HttpStatus.BAD_REQUEST,
                "There are invalid fields in your request",
                getPath(req),
                violations
        );
        return ResponseEntity.badRequest().body(body);
    }

    /**
     * Handle constraint violations (400 Bad Request)
     * When Bean Validation constraints are violated at service/repository level
     */
    @ExceptionHandler(ConstraintViolationException.class)
    public ResponseEntity<ApiError> handleConstraint(ConstraintViolationException ex, WebRequest req) {
        var violations = ex.getConstraintViolations().stream()
                .map(cv -> new ApiError.FieldViolation(cv.getPropertyPath().toString(), cv.getMessage()))
                .toList();

        log.warn("Constraint violation detected with {} violations on path: {}", violations.size(), getPath(req));

        var body = ApiError.of(
                HttpStatus.BAD_REQUEST,
                "Constraint violation detected",
                getPath(req),
                violations
        );
        return ResponseEntity.badRequest().body(body);
    }

    /**
     * Handle IllegalArgumentException (422 Unprocessable Entity)
     * When request parameters are semantically invalid
     * Examples: invalid date range, stop order, passenger type, etc.
     */
    @ExceptionHandler(IllegalArgumentException.class)
    public ResponseEntity<ApiError> handleIllegalArg(IllegalArgumentException ex, WebRequest req) {
        log.warn("Invalid argument: {} | Path: {}", ex.getMessage(), getPath(req));

        var body = ApiError.of(
                HttpStatus.UNPROCESSABLE_ENTITY,
                ex.getMessage(),
                getPath(req)
        );
        return ResponseEntity.status(HttpStatus.UNPROCESSABLE_ENTITY).body(body);
    }

    /**
     * Handle IllegalStateException (409 Conflict)
     * When business logic validation fails
     * Examples: seat already occupied, trip already departed, etc.
     */
    @ExceptionHandler(IllegalStateException.class)
    public ResponseEntity<ApiError> handleIllegalState(IllegalStateException ex, WebRequest req) {
        log.warn("Business logic conflict: {} | Path: {}", ex.getMessage(), getPath(req));

        var body = ApiError.of(HttpStatus.CONFLICT, ex.getMessage(), getPath(req));
        return ResponseEntity.status(HttpStatus.CONFLICT).body(body);
    }

    /**
     * Handle type mismatch (400 Bad Request)
     * When path variable or request param has wrong type
     * Example: /api/trips/abc when expecting Long
     */
    @ExceptionHandler(MethodArgumentTypeMismatchException.class)
    public ResponseEntity<ApiError> handleTypeMismatch(MethodArgumentTypeMismatchException ex, WebRequest req) {
        String message = String.format(
                "Parameter '%s' with value '%s' could not be converted to type '%s'",
                ex.getName(),
                ex.getValue(),
                ex.getRequiredType() != null ? ex.getRequiredType().getSimpleName() : "unknown"
        );

        log.warn("Type mismatch: {} | Path: {}", message, getPath(req));

        var body = ApiError.of(HttpStatus.BAD_REQUEST, message, getPath(req));
        return ResponseEntity.badRequest().body(body);
    }

    /**
     * Handle database constraint violations (409 Conflict)
     * When trying to insert duplicate data or violate FK constraints
     * Examples: duplicate email, duplicate license plate, foreign key violations
     */
    @ExceptionHandler(DataIntegrityViolationException.class)
    public ResponseEntity<ApiError> handleDataIntegrityViolation(DataIntegrityViolationException ex, WebRequest req) {
        String message = "Database constraint violation";

        // Extract more specific message from root cause
        if (ex.getRootCause() != null && ex.getRootCause().getMessage() != null) {
            String root = ex.getRootCause().getMessage();
            if (root.contains("duplicate key") || root.contains("Duplicate entry")) {
                message = "A record with this unique value already exists";
            } else if (root.contains("foreign key constraint")) {
                message = "Cannot perform operation: related records exist";
            }
        }

        log.error("Data integrity violation: {} | Path: {} | Root: {}",
                message, getPath(req), ex.getRootCause() != null ? ex.getRootCause().getMessage() : "N/A");

        var body = ApiError.of(HttpStatus.CONFLICT, message, getPath(req));
        return ResponseEntity.status(HttpStatus.CONFLICT).body(body);
    }

    /**
     * Handle authentication errors (401 Unauthorized)
     * When user is not authenticated or credentials are invalid
     */
    @ExceptionHandler(javax.naming.AuthenticationException.class)
    public ResponseEntity<ApiError> handleAuthentication(javax.naming.AuthenticationException ex, WebRequest req) {
        log.warn("Unauthorized access attempt from IP: {} | Path: {} | Reason: {}",
                getClientIp(req), getPath(req), ex.getMessage());

        var body = ApiError.of(
                HttpStatus.UNAUTHORIZED,
                "Authentication required. Please provide valid credentials.",
                getPath(req)
        );
        return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(body);
    }

    /**
     * Handle access denied errors (403 Forbidden)
     * When authenticated user doesn't have required permissions
     */
    @ExceptionHandler(java.nio.file.AccessDeniedException.class)
    public ResponseEntity<ApiError> handleAccessDenied(java.nio.file.AccessDeniedException ex, WebRequest req) {
        log.warn("Access denied from IP: {} | Path: {} | Reason: {}",
                getClientIp(req), getPath(req), ex.getMessage());

        var body = ApiError.of(
                HttpStatus.FORBIDDEN,
                "You don't have permission to access this resource",
                getPath(req)
        );
        return ResponseEntity.status(HttpStatus.FORBIDDEN).body(body);
    }

    /**
     * Handle generic exceptions (500 Internal Server Error)
     * Catch-all for unexpected errors
     * Should be investigated and fixed immediately
     */
    @ExceptionHandler(Exception.class)
    public ResponseEntity<ApiError> handleGeneric(Exception ex, WebRequest req) {
        log.error("Unexpected error occurred | Path: {} | Exception: {}",
                getPath(req), ex.getClass().getSimpleName(), ex);

        var body = ApiError.of(
                HttpStatus.INTERNAL_SERVER_ERROR,
                "An unexpected error occurred. Please contact support if the problem persists.",
                getPath(req)
        );
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(body);
    }
}