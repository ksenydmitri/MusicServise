package music.service.dto;

import lombok.Getter;
import lombok.Setter;

import java.util.List;

@Getter
@Setter
public class UpdateTrackRequest {
    private String title;
    private int duration;
    private List<Long> userIds;
    private List<Long> playlistIds;
    private String genre;
}
