package ec.edu.espe.pa.taskgotchi.config;

import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

/**
 * Cadena de middlewares de la API:
 * RequestIdFilter → RequestMonitoringFilter → (Spring Security) → AuditInterceptor → controlador.
 */
@Configuration
@RequiredArgsConstructor
public class WebMvcConfig implements WebMvcConfigurer {

    private final AuditInterceptor auditInterceptor;

    @Override
    public void addInterceptors(InterceptorRegistry registry) {
        // Auditoría solo de los recursos del jugador; el login y el registro quedan
        // fuera para no dejar rastro de intentos de credenciales en los logs.
        registry.addInterceptor(auditInterceptor)
                .addPathPatterns("/api/v1/tasks/**", "/api/v1/bosses/**", "/api/v1/habits/**",
                        "/api/v1/pet/**", "/api/v1/categories/**", "/api/v1/users/**");
    }
}
