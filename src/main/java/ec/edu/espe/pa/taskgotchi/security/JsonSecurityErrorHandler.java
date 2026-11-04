package ec.edu.espe.pa.taskgotchi.security;

import ec.edu.espe.pa.taskgotchi.dto.ApiError;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.web.AuthenticationEntryPoint;
import org.springframework.security.web.access.AccessDeniedHandler;
import org.springframework.stereotype.Component;
import tools.jackson.databind.ObjectMapper;

import java.io.IOException;

/** Responde 401/403 con el mismo formato de error de seis campos que el resto de la API. */
@Component
public class JsonSecurityErrorHandler implements AuthenticationEntryPoint, AccessDeniedHandler {

    private final ObjectMapper objectMapper;

    public JsonSecurityErrorHandler(ObjectMapper objectMapper) {
        this.objectMapper = objectMapper;
    }

    @Override
    public void commence(HttpServletRequest request, HttpServletResponse response, AuthenticationException ex)
            throws IOException {
        Object detail = request.getAttribute(JwtAuthenticationFilter.ERROR_ATTRIBUTE);
        String message = detail != null ? detail.toString() : "Debes iniciar sesión para acceder a este recurso.";
        response.setHeader("WWW-Authenticate", "Bearer");
        write(response, request, HttpStatus.UNAUTHORIZED, message);
    }

    @Override
    public void handle(HttpServletRequest request, HttpServletResponse response, AccessDeniedException ex)
            throws IOException {
        write(response, request, HttpStatus.FORBIDDEN, "No tienes permiso para realizar esta acción.");
    }

    void write(HttpServletResponse response, HttpServletRequest request, HttpStatus status, String message)
            throws IOException {
        response.setStatus(status.value());
        response.setContentType(MediaType.APPLICATION_JSON_VALUE);
        response.setCharacterEncoding("UTF-8");
        ApiError error = new ApiError(status.value(), status.getReasonPhrase(), message, request.getRequestURI());
        objectMapper.writeValue(response.getOutputStream(), error);
    }
}
