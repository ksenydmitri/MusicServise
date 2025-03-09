package music.service.dto;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class CreateAlbumRequest {
    @NotBlank(message = "Title is required")
    private String name;

    @NotNull(message = "User ID is required")
    private Long userId;

    private Long trackId;


}
