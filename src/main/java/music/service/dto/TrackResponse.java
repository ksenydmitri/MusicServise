package music.service.dto;

import lombok.Getter;
import lombok.Setter;

import java.time.LocalDate;
import java.util.List;

@Getter
@Setter
public class TrackResponse {
    private Long id;
    private String title;
    private int duration;
    private String genre;
    private LocalDate releaseDate;
    private String albumTitle;
    private List<String> usernames;
    private List<String> playlists;
}
