package music.service.dto;

import java.util.List;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class AlbumResponse {
    private Long id;
    private String title;
    private List<String> artists;
    private List<String> tracks;
}
