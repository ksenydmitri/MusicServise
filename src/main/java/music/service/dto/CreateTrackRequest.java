package music.service.dto;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class CreateTrackRequest {
    @NotBlank(message = "Title is required")
    private String title;

    @NotNull(message = "Duration is required")
    private Long duration;

    @NotNull(message = "User ID is required")
    private Long albumId;

    @NotNull(message = "User ID is required")
    private Long userId;
}
