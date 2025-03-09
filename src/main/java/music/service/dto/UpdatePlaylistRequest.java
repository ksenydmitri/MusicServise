package music.service.dto;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class UpdatePlaylistRequest {
    private String name;
    private Long userId;
    private Long trackId;
}
