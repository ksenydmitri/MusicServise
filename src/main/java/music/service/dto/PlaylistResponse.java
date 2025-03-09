package music.service.dto;

import lombok.Getter;
import lombok.Setter;
import music.service.model.Track;
import music.service.model.User;

import java.util.List;
import java.util.Set;

@Getter
@Setter
public class PlaylistResponse {
    private Long id;
    private String name;
    private List<String> tracks;
    private List<String> users;
}
