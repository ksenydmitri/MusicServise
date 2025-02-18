package music.service;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;


@RestController
public class QueryController {

    @GetMapping(value = "/track", produces = "application/json")
    public ResponseEntity<String> getTrack(@RequestParam(value = "title", defaultValue = "Unknown") String title) {
        String jsonResponse = String.format("{\"title\": \"%s\", \"artist\": \"Sample Artist\"}", sanitizeInput(title));
        return ResponseEntity.ok(jsonResponse);
    }

    private String sanitizeInput(final String input) {
        return input.replaceAll("[^a-zA-Z0-9 ]", "");
    }
}