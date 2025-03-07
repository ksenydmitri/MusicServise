package music.service.dto;

import lombok.Getter;
import lombok.Setter;

import javax.validation.constraints.Email;
import javax.validation.constraints.Size;

@Getter
@Setter
public class UpdateUserRequest {
    @Size(min = 3, max = 50, message = "Username must be between 3 and 50 characters")
    private String username; // Необязательное поле

    @Size(min = 6, max = 50, message = "Password must be between 6 and 50 characters")
    private String password; // Необязательное поле

    @Email(message = "Invalid email format")
    private String email; // Необязательное поле

    private String role; // Необязательное поле
}
