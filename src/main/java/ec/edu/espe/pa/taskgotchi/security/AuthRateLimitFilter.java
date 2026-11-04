package ec.edu.espe.pa.taskgotchi.security;

import ec.edu.espe.pa.taskgotchi.dto.ApiError;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;
import tools.jackson.databind.ObjectMapper;

import java.io.IOException;
import java.time.Clock;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicLong;

/**
 * Middleware de bloqueo por fuerza bruta: limita los intentos de login y registro
 * por dirección IP dentro de una ventana de tiempo. Al superarlo responde 429 Too
 * Many Requests con la cabecera Retry-After, sin llegar al controlador.
 * <ul>
 *   <li>Usa la IP de la conexión (no X-Forwarded-For) para que no se pueda falsear.</li>
 *   <li>Se ejecuta dentro de la cadena de Spring Security, después de CORS, para que
 *       el navegador pueda leer el 429 (ver SecurityConfig).</li>
 *   <li>Memoria acotada: como máximo {@code maxTrackedClients} IPs; si se llena con
 *       ventanas activas, las IPs nuevas se rechazan hasta que expiren (falla cerrado).</li>
 * </ul>
 */
@Component
public class AuthRateLimitFilter extends OncePerRequestFilter {

    private static final Logger log = LoggerFactory.getLogger(AuthRateLimitFilter.class);
    static final String AUTH_PATH = "/api/v1/auth/";

    private final int maxAttempts;
    private final long windowMs;
    private final int maxTrackedClients;
    private final ObjectMapper objectMapper;
    private final Clock clock;
    private final Map<String, Window> windows = new ConcurrentHashMap<>();
    private final AtomicLong lastPurge = new AtomicLong();

    @Autowired
    public AuthRateLimitFilter(@Value("${taskgotchi.security.auth-rate-limit.max-attempts:10}") int maxAttempts,
                               @Value("${taskgotchi.security.auth-rate-limit.window-seconds:60}") long windowSeconds,
                               @Value("${taskgotchi.security.auth-rate-limit.max-tracked-clients:10000}") int maxTrackedClients,
                               ObjectMapper objectMapper) {
        this(maxAttempts, windowSeconds, maxTrackedClients, objectMapper, Clock.systemUTC());
    }

    AuthRateLimitFilter(int maxAttempts, long windowSeconds, int maxTrackedClients, ObjectMapper objectMapper, Clock clock) {
        this.maxAttempts = maxAttempts;
        this.windowMs = windowSeconds * 1000;
        this.maxTrackedClients = maxTrackedClients;
        this.objectMapper = objectMapper;
        this.clock = clock;
    }

    private record Window(long startedAt, AtomicInteger attempts) { }

    /** Ruta relativa a la aplicación: funciona igual con o sin server.servlet.context-path. */
    public static String applicationPath(HttpServletRequest request) {
        String uri = request.getRequestURI();
        String context = request.getContextPath();
        return context != null && !context.isEmpty() && uri.startsWith(context) ? uri.substring(context.length()) : uri;
    }

    @Override
    protected boolean shouldNotFilter(HttpServletRequest request) {
        return !"POST".equals(request.getMethod()) || !applicationPath(request).startsWith(AUTH_PATH);
    }

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain chain)
            throws ServletException, IOException {
        long now = clock.millis();
        String client = request.getRemoteAddr();
        purgeExpired(now);

        if (!windows.containsKey(client) && windows.size() >= maxTrackedClients) {
            reject(request, response, Math.max(1, windowMs / 1000), client);
            return;
        }
        Window window = windows.compute(client, (ip, current) ->
                current == null || now - current.startedAt() >= windowMs ? new Window(now, new AtomicInteger()) : current);

        if (window.attempts().incrementAndGet() > maxAttempts) {
            reject(request, response, Math.max(1, (window.startedAt() + windowMs - now + 999) / 1000), client);
            return;
        }
        chain.doFilter(request, response);
    }

    /** Limpia ventanas caducadas como mucho una vez por ventana, no en cada petición. */
    private void purgeExpired(long now) {
        long previous = lastPurge.get();
        if (now - previous >= windowMs && lastPurge.compareAndSet(previous, now)) {
            windows.values().removeIf(w -> now - w.startedAt() >= windowMs);
        }
    }

    private void reject(HttpServletRequest request, HttpServletResponse response, long retryAfter, String client)
            throws IOException {
        log.warn("Bloqueado por exceso de intentos: {} {} desde {}", request.getMethod(), applicationPath(request), client);
        response.setStatus(HttpStatus.TOO_MANY_REQUESTS.value());
        response.setHeader("Retry-After", String.valueOf(retryAfter));
        response.setContentType(MediaType.APPLICATION_JSON_VALUE);
        response.setCharacterEncoding("UTF-8");
        objectMapper.writeValue(response.getOutputStream(), new ApiError(429, HttpStatus.TOO_MANY_REQUESTS.getReasonPhrase(),
                "Demasiados intentos. Espera " + retryAfter + " segundos e inténtalo de nuevo.", request.getRequestURI()));
    }

    int trackedClients() {
        return windows.size();
    }
}
