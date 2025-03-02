package music.service.controller;

import music.service.model.Track;
import music.service.service.MusicService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class TrackController {

    private final MusicService musicService;

    @Autowired
    public TrackController(MusicService musicService) {
        this.musicService = musicService;
    }

    @GetMapping(value = "/track", produces = "application/json")
    public ResponseEntity<String> getTrack(@RequestParam(value = "title",
            defaultValue = "Unknown") String title) {
        String sanitizedTitle = musicService.sanitizeInput(title);
        String jsonResponse = String.format("{\"title\": \"%s\", \"artist\": \"Sample Artist\"}",
                sanitizedTitle);
        return ResponseEntity.ok(jsonResponse);
    }

    @GetMapping(value = "/track/{id}", produces = "application/json")
    public ResponseEntity<Track> getTrackById(@PathVariable("id") String id) {
        Track track = musicService.getTrackById(id);
        if (track != null) {
            return ResponseEntity.ok(track);
        } else {
            return ResponseEntity.notFound().build();
        }
    }
}
