package music.service.controller;

import java.util.ArrayList;
import java.util.List;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class PathController {

    private final List<Track> tracks = new ArrayList<>();

    public PathController() {
        tracks.add(new Track("1", "Sample Title 1", "Sample Artist 1"));
        tracks.add(new Track("2", "Sample Title 2", "Sample Artist 2"));
        tracks.add(new Track("3", "Sample Title 3", "Sample Artist 3"));
    }

    @GetMapping(value = "/track/{id}", produces = "application/json")
    public ResponseEntity<Track> getTrackById(@PathVariable("id") String id) {
        for (Track track : tracks) {
            if (track.getId().equals(sanitizeInput(id))) {
                return ResponseEntity.ok(track);
            }
        }
        return ResponseEntity.notFound().build();
    }

    private String sanitizeInput(final String input) {
        return input.replaceAll("[^a-zA-Z0-9 ]", "");
    }

    public static class Track {
        private final String id;
        private final String title;
        private final String artist;

        public Track(String id, String title, String artist) {
            this.id = id;
            this.title = title;
            this.artist = artist;
        }

        public String getId() {
            return id;
        }

        public String getTitle() {
            return title;
        }

        public String getArtist() {
            return artist;
        }
    }
}
