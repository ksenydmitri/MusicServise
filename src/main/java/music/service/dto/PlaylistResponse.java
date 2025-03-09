package music.service.dto;

import java.util.List;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class PlaylistResponse {
    private Long id;
    private String name;
    private List<String> tracks;
    private List<String> users;
}
