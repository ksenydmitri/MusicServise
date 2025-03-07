package music.service.dto;

import lombok.Getter;
import lombok.Setter;

import java.util.List;

@Getter
@Setter
public class AlbumResponse {
    private Long id;
    private String title;
    private List<String> artists;
    private List<String> tracks;
}
