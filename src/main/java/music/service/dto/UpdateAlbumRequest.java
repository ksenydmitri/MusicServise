package music.service.dto;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class UpdateAlbumRequest {
    private String name;
    public Long userId;
    public Long trackId;
}
