package ec.edu.espe.pa.taskgotchi.config;

import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

@Configuration
@RequiredArgsConstructor
public class WebMvcConfig implements WebMvcConfigurer {

    private final RequestMonitoringInterceptor requestMonitoringInterceptor;
    private final AuditInterceptor auditInterceptor;

    @Override
    public void addInterceptors(InterceptorRegistry registry) {
        // TODO: En la siguiente unidad, afinar los patrones de URL para interceptar métricas y auditoría
        registry.addInterceptor(requestMonitoringInterceptor)
                .addPathPatterns("/api/v1/**");

        registry.addInterceptor(auditInterceptor)
                .addPathPatterns("/api/v1/**");
    }
}
