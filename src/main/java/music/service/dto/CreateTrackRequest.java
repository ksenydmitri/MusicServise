package music.service.dto;

import javax.validation.constraints.Email;
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;

import lombok.Builder;
import lombok.Getter;
import lombok.Setter;
import org.springframework.web.multipart.MultipartFile;

@Getter
@Setter
public class CreateTrackRequest {
    @NotBlank(message = "Title is required")
    private String title;

    @NotNull(message = "Duration is required")
    private int duration;

    @NotNull(message = "User ID is required")
    private String genre;

    @NotNull(message = "User ID is required")
    private Long albumId;

    @NotNull(message = "User ID is required")
    private Long userId;
}
