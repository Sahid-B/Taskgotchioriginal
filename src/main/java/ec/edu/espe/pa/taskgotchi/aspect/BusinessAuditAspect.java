package ec.edu.espe.pa.taskgotchi.aspect;

import ec.edu.espe.pa.taskgotchi.security.AuthenticatedUser;
import org.aspectj.lang.JoinPoint;
import org.aspectj.lang.annotation.AfterReturning;
import org.aspectj.lang.annotation.AfterThrowing;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.reflect.MethodSignature;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;

import java.lang.reflect.Method;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * Aspecto de auditoría de negocio. Se aplica a los métodos anotados con
 * {@link Auditable} y registra en el logger "BUSINESS":
 * evento, usuario, IDs recibidos y un resumen del resultado (ID, estado, etapa de
 * la mascota, XP...). A diferencia del AuditInterceptor, que ve peticiones HTTP,
 * este aspecto ve la lógica: también captura recompensas internas como derrotar a
 * un jefe o la evolución de la mascota, que no tienen endpoint propio.
 * Solo registra argumentos numéricos (IDs), nunca cuerpos de petición.
 */
@Aspect
@Component
public class BusinessAuditAspect {

    private static final Logger log = LoggerFactory.getLogger("BUSINESS");
    private static final List<String> SUMMARY_FIELDS = List.of("id", "status", "stage", "xp", "coins", "currentStreak", "currentHp");

    @AfterReturning(pointcut = "@annotation(auditable)", returning = "result")
    public void afterSuccess(JoinPoint joinPoint, Auditable auditable, Object result) {
        log.info("event={} user={} args={} result={}", auditable.value(), currentUser(), ids(joinPoint), summarize(result));
    }

    @AfterThrowing(pointcut = "@annotation(auditable)", throwing = "ex")
    public void afterFailure(JoinPoint joinPoint, Auditable auditable, Throwable ex) {
        log.info("event={} user={} args={} result=FAILED reason={}", auditable.value(), currentUser(), ids(joinPoint),
                ex.getClass().getSimpleName());
    }

    static String currentUser() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        return auth != null && auth.getPrincipal() instanceof AuthenticatedUser user ? String.valueOf(user.id()) : "anonymous";
    }

    /** Solo los parámetros numéricos (IDs, XP, monedas) con su nombre. */
    static Map<String, Object> ids(JoinPoint joinPoint) {
        Map<String, Object> ids = new LinkedHashMap<>();
        String[] names = ((MethodSignature) joinPoint.getSignature()).getParameterNames();
        Object[] args = joinPoint.getArgs();
        for (int i = 0; i < args.length; i++) {
            if (args[i] instanceof Number) {
                ids.put(names != null ? names[i] : "arg" + i, args[i]);
            }
        }
        return ids;
    }

    /** Extrae algunos campos conocidos del resultado (record o entidad), si existen. */
    static Object summarize(Object result) {
        if (result == null) {
            return "OK";
        }
        Map<String, Object> summary = new LinkedHashMap<>();
        for (String field : SUMMARY_FIELDS) {
            Object value = read(result, field);
            if (value != null) {
                summary.put(field, value);
            }
        }
        return summary.isEmpty() ? result.getClass().getSimpleName() : summary;
    }

    private static Object read(Object target, String field) {
        String getter = "get" + Character.toUpperCase(field.charAt(0)) + field.substring(1);
        for (String name : new String[]{field, getter}) {
            try {
                Method method = target.getClass().getMethod(name);
                return method.getParameterCount() == 0 ? method.invoke(target) : null;
            } catch (ReflectiveOperationException | RuntimeException ignored) {
                // El campo no existe en este tipo de resultado.
            }
        }
        return null;
    }
}
