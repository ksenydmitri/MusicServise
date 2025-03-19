package music.service.controller;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;
import javax.validation.Valid;
import music.service.dto.*;
import music.service.model.Track;
import music.service.service.TrackService;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

@RestController
@RequestMapping("/tracks")
public class TrackController {

    private final TrackService trackService;

    public TrackController(TrackService trackService) {
        this.trackService = trackService;
    }

    @GetMapping
    public ResponseEntity<List<TrackResponse>> getAllTracks(
            @RequestParam(required = false) String user,
            @RequestParam(required = false) String title,
            @RequestParam(required = false) String album,
            @RequestParam(required = false) String genre,
            @RequestParam(required = false) String playlist
    ) {
        List<Track> tracks = trackService.getAllTracks(user, album, title, genre, playlist);
        List<TrackResponse> responses = tracks.stream()
                .map(trackService::mapToTrackResponse)
                .collect(Collectors.toList());
        return ResponseEntity.ok(responses);
    }

    @PostMapping(consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<TrackResponse> addTrack(
            @RequestPart("trackData") @Valid CreateTrackRequest request,
            @RequestPart("file") MultipartFile file) {
        try {
            TrackResponse response = trackService.addTrackWithFile(request, file);
            return ResponseEntity.status(HttpStatus.CREATED).body(response);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(null); // Или верните сообщение об ошибке
        }
    }

    @PatchMapping("/{id}")
    public ResponseEntity<TrackResponse> patchTrack(
            @PathVariable Long id,
            @RequestBody UpdateTrackRequest request) {
        TrackResponse updatedTrack = trackService.updateTrack(id, request);
        return ResponseEntity.ok(updatedTrack);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteTrack(@PathVariable Long id) {
        trackService.deleteTrack(id);
        return ResponseEntity.noContent().build();
    }
}
