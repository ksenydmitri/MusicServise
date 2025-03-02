package music.service.service;

import java.util.ArrayList;
import java.util.List;
import music.service.model.Track;
import org.springframework.stereotype.Service;


@Service
public class MusicService {

    private final List<Track> tracks = new ArrayList<>();

    public MusicService() {
        tracks.add(new Track("1", "Sample Title 1", "Sample Artist 1"));
        tracks.add(new Track("2", "Sample Title 2", "Sample Artist 2"));
        tracks.add(new Track("3", "Sample Title 3", "Sample Artist 3"));
    }

    public Track getTrackById(String id) {
        for (Track track : tracks) {
            if (track.getId().equals(id)) {
                return track;
            }
        }
        return null;
    }

    public String sanitizeInput(final String input) {
        return input.replaceAll("[^a-zA-Z0-9 ]", "");
    }
}
