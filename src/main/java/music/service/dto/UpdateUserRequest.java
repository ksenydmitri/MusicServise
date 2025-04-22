package music.service.dto;

import javax.validation.constraints.Email;
import javax.validation.constraints.Size;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class UpdateUserRequest {
    @Size(min = 3, max = 50, message = "Username must be between 3 and 50 characters")
    private String username;

    @Size(min = 6, max = 50, message = "Password must be between 6 and 50 characters")
    private String password;

    @Email(message = "Invalid email format")
    private String email;

    private String role;
}
