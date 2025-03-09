package music.service.dto;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class UpdateAlbumRequest {
    private String name;
    private Long userId;
    private Long trackId;
}
