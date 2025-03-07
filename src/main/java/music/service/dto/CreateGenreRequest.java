package music.service.dto;

import javax.validation.constraints.NotBlank;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class CreateGenreRequest {
    @NotBlank(message = "Name is required")
    private String name;
}
