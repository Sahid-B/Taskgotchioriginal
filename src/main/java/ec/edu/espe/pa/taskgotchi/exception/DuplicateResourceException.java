package ec.edu.espe.pa.taskgotchi.exception;

/**
 * Excepción lanzada cuando un recurso viola una restricción de unicidad (409 Conflict).
 */
public class DuplicateResourceException extends RuntimeException {
    public DuplicateResourceException(String message) {
        super(message);
    }
}
