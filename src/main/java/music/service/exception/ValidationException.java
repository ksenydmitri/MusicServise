package music.service.exception;

/**
 * Исключение, которое выбрасывается при нарушении правил валидации.
 */
public class ValidationException extends RuntimeException {

    /**
     * Конструктор с сообщением об ошибке.
     *
     * @param message Сообщение, описывающее ошибку валидации.
     */
    public ValidationException(String message) {
        super(message);
    }

    /**
     * Конструктор с сообщением об ошибке и причиной исключения.
     *
     * @param message Сообщение, описывающее ошибку валидации.
     * @param cause   Причина исключения.
     */
    public ValidationException(String message, Throwable cause) {
        super(message, cause);
    }
}