package music.service.exception;

import lombok.Getter;
import lombok.Setter;
import java.util.Map;

@Getter
@Setter
public class ErrorResponse {
    private int statusCode;
    private String message;
    private Map<String, String> validationErrors;

    public ErrorResponse(int statusCode, String message, Map<String, String> validationErrors) {
        this.statusCode = statusCode;
        this.message = message;
        this.validationErrors = validationErrors;
    }

    public ErrorResponse(int statusCode, String message) {
        this.statusCode = statusCode;
        this.message = message;
    }

}