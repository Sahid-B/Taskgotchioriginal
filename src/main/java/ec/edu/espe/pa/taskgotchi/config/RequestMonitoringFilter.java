package ec.edu.espe.pa.taskgotchi.config;

import ec.edu.espe.pa.taskgotchi.security.AuthRateLimitFilter;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;

/**
 * Middleware de monitoreo: registra método, URL, código HTTP y tiempo de cada
 * petición de la API, incluidas las rechazadas antes de llegar al controlador
 * (404/405). Las peticiones lentas o con error 5xx se registran como WARN.
 * Se ejecuta justo después de {@link RequestIdFilter}, así cada línea lleva su ID.
 */
@Component
@Order(Ordered.HIGHEST_PRECEDENCE + 1)
public class RequestMonitoringFilter extends OncePerRequestFilter {

    private static final Logger log = LoggerFactory.getLogger(RequestMonitoringFilter.class);

    private final long slowRequestMs;

    public RequestMonitoringFilter(@Value("${taskgotchi.monitoring.slow-request-ms:1000}") long slowRequestMs) {
        this.slowRequestMs = slowRequestMs;
    }

    @Override
    protected boolean shouldNotFilter(HttpServletRequest request) {
        return !AuthRateLimitFilter.applicationPath(request).startsWith("/api/");
    }

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain chain)
            throws ServletException, IOException {
        long start = System.nanoTime();
        Throwable failure = null;
        try {
            chain.doFilter(request, response);
        } catch (IOException | ServletException | RuntimeException e) {
            failure = e;
            throw e;
        } finally {
            log(request, response.getStatus(), (System.nanoTime() - start) / 1_000_000, failure);
        }
    }

    private void log(HttpServletRequest request, int status, long elapsedMs, Throwable failure) {
        String target = request.getMethod() + " " + request.getRequestURI();
        if (status >= 500 || failure != null) {
            log.warn("{} -> {} ({} ms){}", target, status, elapsedMs,
                    failure != null ? " error=" + failure.getClass().getSimpleName() : "");
        } else if (elapsedMs >= slowRequestMs) {
            log.warn("{} -> {} ({} ms) petición lenta", target, status, elapsedMs);
        } else {
            log.info("{} -> {} ({} ms)", target, status, elapsedMs);
        }
    }
}
