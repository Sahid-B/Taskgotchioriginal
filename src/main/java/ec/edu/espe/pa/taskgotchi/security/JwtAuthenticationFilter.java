package ec.edu.espe.pa.taskgotchi.security;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.http.HttpHeaders;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.AuthorityUtils;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.oauth2.jwt.JwtException;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;

/**
 * Middleware de autenticación: lee "Authorization: Bearer &lt;token&gt;", valida el JWT y
 * coloca al usuario en el SecurityContext. Si el token falta o no es válido no
 * responde por sí mismo: deja la petición sin autenticar y Spring Security la
 * bloquea con 401 en las rutas protegidas.
 * Se registra dentro de la cadena de Spring Security (ver SecurityConfig), no como
 * filtro de servlet independiente.
 */
public class JwtAuthenticationFilter extends OncePerRequestFilter {

    public static final String ERROR_ATTRIBUTE = JwtAuthenticationFilter.class.getName() + ".error";
    private static final String BEARER = "Bearer ";

    private final JwtService jwtService;

    public JwtAuthenticationFilter(JwtService jwtService) {
        this.jwtService = jwtService;
    }

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain chain)
            throws ServletException, IOException {
        String header = request.getHeader(HttpHeaders.AUTHORIZATION);
        if (header != null && header.regionMatches(true, 0, BEARER, 0, BEARER.length())) {
            try {
                AuthenticatedUser user = jwtService.parse(header.substring(BEARER.length()).trim());
                var authentication = new UsernamePasswordAuthenticationToken(user, null, AuthorityUtils.NO_AUTHORITIES);
                SecurityContextHolder.getContext().setAuthentication(authentication);
            } catch (JwtException e) {
                SecurityContextHolder.clearContext();
                request.setAttribute(ERROR_ATTRIBUTE, "Token inválido o expirado. Inicia sesión de nuevo.");
            }
        }
        chain.doFilter(request, response);
    }
}
