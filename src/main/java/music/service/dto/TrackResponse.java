package music.service.dto;

import java.time.LocalDate;
import java.util.List;

import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@Builder
public class TrackResponse {
    private Long id;
    private String title;
    private int duration;
    private String genre;
    private LocalDate releaseDate;
    private AlbumResponse album;
    private List<String> usernames;
    private List<String> playlists;
    private String mediaFileId;
}
