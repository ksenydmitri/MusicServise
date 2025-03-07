package music.service.dto;

import lombok.Getter;
import lombok.Setter;

import java.time.LocalDate;
import java.util.List;

@Getter
@Setter
public class TrackResponse {
    private Integer id;
    private String title;
    private Long duration;
    private LocalDate releaseDate;
    private String albumTitle;
    private List<String> usernames;
}
