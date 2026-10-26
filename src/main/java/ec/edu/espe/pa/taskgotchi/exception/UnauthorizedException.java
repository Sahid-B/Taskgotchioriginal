package ec.edu.espe.pa.taskgotchi.exception;

/**
 * Excepcion lanzada cuando las credenciales de acceso no son validas (401 Unauthorized).
 */
public class UnauthorizedException extends RuntimeException {
    public UnauthorizedException(String message) {
        super(message);
    }
}
