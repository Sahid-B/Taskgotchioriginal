package ec.edu.espe.pa.taskgotchi.dto;

import com.fasterxml.jackson.annotation.JsonInclude;

import java.time.LocalDateTime;
import java.util.Collections;
import java.util.List;

/**
 * Estructura de error uniforme de 6 claves para todas las respuestas de error de la API:
 * timestamp, status, error, message, path, errors.
 */
@JsonInclude(JsonInclude.Include.ALWAYS)
public record ApiError(
        LocalDateTime timestamp,
        int status,
        String error,
        String message,
        String path,
        List<String> errors
) {
    public ApiError(int status, String error, String message, String path) {
        this(LocalDateTime.now(), status, error, message, path, Collections.emptyList());
    }

    public ApiError(int status, String error, String message, String path, List<String> errors) {
        this(LocalDateTime.now(), status, error, message, path, errors != null ? errors : Collections.emptyList());
    }
}
