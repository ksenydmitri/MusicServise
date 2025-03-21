package music.service.exception;

/**
 * Исключение, которое выбрасывается, когда запрашиваемый ресурс не найден.
 */
public class ResourceNotFoundException extends RuntimeException {

    /**
     * Конструктор с сообщением об ошибке.
     *
     * @param message Сообщение, описывающее, какой ресурс не найден.
     */
    public ResourceNotFoundException(String message) {
        super(message);
    }

    /**
     * Конструктор с сообщением об ошибке и причиной исключения.
     *
     * @param message Сообщение, описывающее, какой ресурс не найден.
     * @param cause   Причина исключения.
     */
    public ResourceNotFoundException(String message, Throwable cause) {
        super(message, cause);
    }
}