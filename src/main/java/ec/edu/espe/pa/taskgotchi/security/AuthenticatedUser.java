package ec.edu.espe.pa.taskgotchi.security;

/** Usuario autenticado que viaja en el SecurityContext tras validar el JWT. */
public record AuthenticatedUser(Long id, String username) {
}
