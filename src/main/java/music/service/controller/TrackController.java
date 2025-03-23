package music.service.controller;

import javax.validation.Valid;
import music.service.dto.*;
import music.service.exception.ValidationException;
import music.service.model.Track;
import music.service.service.TrackService;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/tracks")
public class TrackController {

    private final TrackService trackService;

    public TrackController(TrackService trackService) {
        this.trackService = trackService;
    }


    @GetMapping
    public ResponseEntity<Page<TrackResponse>> getAllTracks(
            @RequestParam(required = false) String user,
            @RequestParam(required = false) String title,
            @RequestParam(required = false) String album,
            @RequestParam(required = false) String genre,
            @RequestParam(required = false) String playlist,
            @RequestParam(required = false, defaultValue = "0") int page,
            @RequestParam(required = false, defaultValue = "10") int size) {
        Pageable pageable = PageRequest.of(page, size);
        Page<Track> tracks = trackService.getAllTracks(
                user, album, title, genre, playlist, pageable);
        Page<TrackResponse> responses = tracks.map(trackService::mapToTrackResponse);
        return ResponseEntity.ok(responses);
    }

    @PostMapping
    public ResponseEntity<TrackResponse> addTrack(
            @RequestBody @Valid CreateTrackRequest request) {
        try {
            TrackResponse response = trackService.addTrack(request);
            return ResponseEntity.status(HttpStatus.CREATED).body(response);
        } catch (ValidationException e) {
            throw new ValidationException(e.getMessage());
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
