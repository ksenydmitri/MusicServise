package music.service.dto;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class UpdateTrackRequest {
    private String title;
    private int duration;
    private Long userId;
    private Long playlistId;
    private String genre;
}
