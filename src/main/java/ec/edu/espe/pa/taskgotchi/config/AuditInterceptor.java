package ec.edu.espe.pa.taskgotchi.config;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpHeaders;
import org.springframework.stereotype.Component;
import org.springframework.web.method.HandlerMethod;
import org.springframework.web.servlet.HandlerInterceptor;
import org.springframework.web.servlet.HandlerMapping;

import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.TreeMap;
import java.util.regex.Pattern;

/**
 * Middleware de auditoría: deja constancia de cada acción que modifica datos
 * (crear, editar, eliminar, completar misiones o subtareas de jefes, cuidar a la
 * mascota) con el endpoint, el ID del recurso y el resultado. Nunca registra el
 * cuerpo de la petición, para no guardar datos personales ni credenciales.
 */
@Component
public class AuditInterceptor implements HandlerInterceptor {

    private static final Logger audit = LoggerFactory.getLogger("AUDIT");
    private static final Set<String> MUTATING_METHODS = Set.of("POST", "PUT", "PATCH", "DELETE");
    private static final Pattern CREATED_ID = Pattern.compile("[A-Za-z0-9-]{1,64}");

    @Override
    public void afterCompletion(HttpServletRequest request, HttpServletResponse response, Object handler, Exception ex) {
        describe(request, response.getStatus(), response.getHeader(HttpHeaders.LOCATION), handler, ex)
                .ifPresent(audit::info);
    }

    /** Devuelve la línea de auditoría, o vacío si la petición no debe auditarse. */
    @SuppressWarnings("unchecked")
    Optional<String> describe(HttpServletRequest request, int status, String location, Object handler, Exception ex) {
        if (!MUTATING_METHODS.contains(request.getMethod()) || !(handler instanceof HandlerMethod method)) {
            return Optional.empty();
        }
        Object vars = request.getAttribute(HandlerMapping.URI_TEMPLATE_VARIABLES_ATTRIBUTE);
        Map<String, String> ids = new TreeMap<>(vars instanceof Map<?, ?> map ? (Map<String, String>) map : Map.of());
        boolean success = ex == null && status >= 200 && status < 300;

        // En una creación (201) el ID nuevo solo viaja en la cabecera Location.
        if (success && status == 201 && location != null) {
            String createdId = location.substring(location.lastIndexOf('/') + 1);
            if (CREATED_ID.matcher(createdId).matches()) {
                ids.put("createdId", createdId);
            }
        }

        return Optional.of(String.format("action=%s.%s method=%s path=%s ids=%s status=%d result=%s",
                method.getBeanType().getSimpleName().replace("Controller", ""),
                method.getMethod().getName(),
                request.getMethod(),
                request.getRequestURI(),
                ids,
                status,
                success ? "OK" : "FAILED"));
    }
}
