package ec.edu.espe.pa.taskgotchi.aspect;

import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Pointcut;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;

/**
 * Aspecto de rendimiento: envuelve (@Around) todos los métodos públicos de la capa
 * de servicio y mide su duración. Los que superan el umbral se registran como WARN;
 * el resto, en DEBUG. Las excepciones se registran y se vuelven a lanzar sin cambios,
 * para que GlobalExceptionHandler siga generando la respuesta HTTP.
 */
@Aspect
@Component
@Order(Ordered.HIGHEST_PRECEDENCE)
public class PerformanceAspect {

    private static final Logger log = LoggerFactory.getLogger(PerformanceAspect.class);

    private final long slowMethodMs;

    public PerformanceAspect(@Value("${taskgotchi.monitoring.slow-method-ms:500}") long slowMethodMs) {
        this.slowMethodMs = slowMethodMs;
    }

    @Pointcut("within(ec.edu.espe.pa.taskgotchi.service..*) && execution(public * *(..))")
    void serviceLayer() {
    }

    @Around("serviceLayer()")
    public Object measure(ProceedingJoinPoint joinPoint) throws Throwable {
        long start = System.nanoTime();
        String method = joinPoint.getSignature().getDeclaringType().getSimpleName() + "." + joinPoint.getSignature().getName();
        try {
            return joinPoint.proceed();
        } catch (Throwable ex) {
            log.debug("{} lanzó {}", method, ex.getClass().getSimpleName());
            throw ex;
        } finally {
            long elapsedMs = (System.nanoTime() - start) / 1_000_000;
            if (elapsedMs >= slowMethodMs) {
                log.warn("Método lento: {} tardó {} ms (umbral {} ms)", method, elapsedMs, slowMethodMs);
            } else if (log.isDebugEnabled()) {
                log.debug("{} tardó {} ms", method, elapsedMs);
            }
        }
    }
}
