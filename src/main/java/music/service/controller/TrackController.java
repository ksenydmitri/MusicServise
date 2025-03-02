package music.service.controller;

import music.service.dto.TrackDTO;
import music.service.model.Track;
import music.service.service.TrackService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Optional;

@RestController
@RequestMapping("/tracks")
public class TrackController {

    private final TrackService trackService;

    @Autowired
    public TrackController(TrackService trackService) {
        this.trackService = trackService;
    }

    // Get all tracks
    @GetMapping
    public ResponseEntity<List<Track>> getAllTracks() {
        List<Track> tracks = trackService.getAllTracks();
        return ResponseEntity.ok(tracks);
    }

    // Get track by ID
    @GetMapping("/{id}")
    public ResponseEntity<Track> getTrackById(@PathVariable Long id) {
        Optional<Track> track = trackService.getTrackById(id);
        return track.map(ResponseEntity::ok).orElse(ResponseEntity.notFound().build());
    }

    // Create new track
    @PostMapping
    public ResponseEntity<Track> createTrack(@RequestBody TrackDTO trackDTO) {
        // Преобразование TrackDTO в Track
        Track track = new Track(
                trackDTO.getTitle(),
                trackDTO.getArtist(),
                trackDTO.getGenre(),
                trackDTO.getDuration(),
                trackDTO.getReleaseDate()
        );

        Track savedTrack = trackService.saveTrack(track);
        return ResponseEntity.ok(savedTrack);
    }

    // Update track
    @PutMapping("/{id}")
    public ResponseEntity<Track> updateTrack(@PathVariable Long id, @RequestBody TrackDTO trackDetails) {
        Optional<Track> optionalTrack = trackService.getTrackById(id);
        if (!optionalTrack.isPresent()) {
            return ResponseEntity.notFound().build();
        }
        Track track = optionalTrack.get();
        track.setTitle(trackDetails.getTitle());
        track.setArtist(trackDetails.getArtist());
        track.setGenre(trackDetails.getGenre());
        track.setDuration(trackDetails.getDuration());
        track.setReleaseDate(trackDetails.getReleaseDate());

        Track updatedTrack = trackService.saveTrack(track);
        return ResponseEntity.ok(updatedTrack);
    }

    // Delete track
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteTrack(@PathVariable Long id) {
        trackService.deleteTrack(id);
        return ResponseEntity.noContent().build();
    }
}
