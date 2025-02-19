package music.service.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class QueryController {

    private final MusicService musicService;

    @Autowired
    public QueryController(MusicService musicService) {
        this.musicService = musicService;
    }

    @GetMapping(value = "/track", produces = "application/json")
    public ResponseEntity<String> getTrack(@RequestParam(value = "title", defaultValue = "Unknown") String title) {
        String sanitizedTitle = musicService.sanitizeInput(title);
        String jsonResponse = String.format("{\"title\": \"%s\", \"artist\": \"Sample Artist\"}", sanitizedTitle);
        return ResponseEntity.ok(jsonResponse);
    }
}
