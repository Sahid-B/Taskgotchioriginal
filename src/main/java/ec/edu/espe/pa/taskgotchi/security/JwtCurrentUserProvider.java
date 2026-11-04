package ec.edu.espe.pa.taskgotchi.security;

import ec.edu.espe.pa.taskgotchi.exception.UnauthorizedException;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;

/**
 * Devuelve el ID del usuario dueño del token JWT de la petición actual.
 * Sustituye al antiguo DevCurrentUserProvider, que siempre devolvía el usuario 1:
 * ahora cada estudiante solo ve y modifica sus propios datos.
 */
@Component
public class JwtCurrentUserProvider implements CurrentUserProvider {

    @Override
    public Long getCurrentUserId() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication != null && authentication.getPrincipal() instanceof AuthenticatedUser user) {
            return user.id();
        }
        throw new UnauthorizedException("Debes iniciar sesión para acceder a este recurso.");
    }
}
