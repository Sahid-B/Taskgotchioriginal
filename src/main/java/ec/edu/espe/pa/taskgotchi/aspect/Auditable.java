package ec.edu.espe.pa.taskgotchi.aspect;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Marca un método de servicio como evento de negocio. {@link BusinessAuditAspect}
 * registra quién lo ejecutó, con qué IDs y cómo terminó, sin tocar el código del método.
 */
@Documented
@Target(ElementType.METHOD)
@Retention(RetentionPolicy.RUNTIME)
public @interface Auditable {

    /** Nombre del evento, p. ej. TASK_COMPLETED. */
    String value();
}
