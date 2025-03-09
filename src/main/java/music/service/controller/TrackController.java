package music.service.controller;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;
import javax.validation.Valid;
import music.service.dto.*;
import music.service.model.Track;
import music.service.service.TrackService;
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
    public ResponseEntity<List<TrackResponse>> getAllTracks() {
        List<Track> tracks = trackService.getAllTracks();
        List<TrackResponse> responses = tracks.stream()
                .map(trackService::mapToTrackResponse)
                .collect(Collectors.toList());
        return ResponseEntity.ok(responses);
    }

    @GetMapping("/{id}")
    public ResponseEntity<TrackResponse> getTrackById(@PathVariable Long id) {
        Optional<Track> trackOpt = trackService.getTrackById(id);
        return trackOpt.map(track -> ResponseEntity.ok(trackService.mapToTrackResponse(track)))
                .orElse(ResponseEntity.notFound().build());
    }

    @GetMapping("/genres")
    public ResponseEntity<List<TrackResponse>> getTracksByGenre(@RequestParam String genre) {
        List<Track> tracks = trackService.getByGenre(genre);
        List<TrackResponse> responses = tracks.stream()
                .map(trackService::mapToTrackResponse)
                .collect(Collectors.toList());
        return ResponseEntity.ok(responses);
    }

    @GetMapping("/albums")
    public ResponseEntity<List<TrackResponse>> getTracksByAlbum(@RequestParam String album) {
        List<Track> tracks = trackService.getByAlbum(album);
        List<TrackResponse> responses = tracks.stream()
                .map(trackService::mapToTrackResponse)
                .collect(Collectors.toList());
        return ResponseEntity.ok(responses);
    }

    @GetMapping("/playlists")
    public ResponseEntity<List<TrackResponse>> getTracksByPlaylist(@RequestParam String playlist) {
        List<Track> tracks = trackService.getByPlaylist(playlist);
        List<TrackResponse> responses = tracks.stream()
                .map(trackService::mapToTrackResponse)
                .collect(Collectors.toList());
        return ResponseEntity.ok(responses);
    }

    @GetMapping("/users")
    public ResponseEntity<List<TrackResponse>> getTracksByUser(@RequestParam String username) {
        List<Track> tracks = trackService.getByUser(username);
        List<TrackResponse> responses = tracks.stream()
                .map(trackService::mapToTrackResponse)
                .collect(Collectors.toList());
        return ResponseEntity.ok(responses);
    }

    @PostMapping
    public ResponseEntity<TrackResponse> addTrack(
            @Valid @RequestBody CreateTrackRequest request) {
        TrackResponse response = trackService.addTrack(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @PatchMapping("/{id}")
    public ResponseEntity<TrackResponse> patchTrack(
            @PathVariable Long id,
            @RequestBody UpdateTrackRequest request) {
        TrackResponse updatedTrack = trackService.updateTrack(id, request);
        return ResponseEntity.ok(updatedTrack);
    }

}
