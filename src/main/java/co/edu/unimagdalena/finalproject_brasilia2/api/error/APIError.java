package co.edu.unimagdalena.finalproject_brasilia2.api.error;

import com.fasterxml.jackson.annotation.JsonFormat;
import org.springframework.http.HttpStatus;

import java.time.OffsetDateTime;
import java.util.List;

public record APIError(@JsonFormat(shape = JsonFormat.Shape.STRING) OffsetDateTime timestamp,
                       int status, String error, String message, String path,
                       List<FieldViolation> violations) {
    public static APIError of(HttpStatus status, String message, String path, List<FieldViolation> violations) {
        return new APIError(OffsetDateTime.now(), status.value(), status.getReasonPhrase(), message, path, violations);
    }

    public record FieldViolation(String field, String message){}
}
