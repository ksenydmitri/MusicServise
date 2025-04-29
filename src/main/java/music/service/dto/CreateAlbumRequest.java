package music.service.dto;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;

import java.util.List;

@Getter
@Setter
public class CreateAlbumRequest {
    @NotBlank(message = "Name is required")
    private String name;

    @NotNull(message = "User ID is required")
    private Long userId;

    private List<String> Collaborators;

}
