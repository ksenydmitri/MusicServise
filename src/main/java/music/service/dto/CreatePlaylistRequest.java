package music.service.dto;

import lombok.Getter;
import lombok.Setter;

import javax.validation.constraints.NotBlank;

@Getter
@Setter
public class CreatePlaylistRequest {
    @NotBlank(message = "Name is required")
    private String name;
}
