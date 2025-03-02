package music.service.controller;

import music.service.dto.PlaylistDTO;
import music.service.model.Playlist;
import music.service.model.Track;
import music.service.service.PlaylistService;
import music.service.service.TrackService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.HashSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;

@RestController
@RequestMapping("/playlists")
public class PlaylistController {

    private final PlaylistService playlistService;
    private final TrackService trackService;

    @Autowired
    public PlaylistController(PlaylistService playlistService, TrackService trackService) {
        this.playlistService = playlistService;
        this.trackService = trackService;
    }

    // Get all playlists
    @GetMapping
    public ResponseEntity<List<Playlist>> getAllPlaylists() {
        List<Playlist> playlists = playlistService.getAllPlaylists();
        return ResponseEntity.ok(playlists);
    }

    // Get playlist by ID
    @GetMapping("/{id}")
    public ResponseEntity<Playlist> getPlaylistById(@PathVariable Long id) {
        Optional<Playlist> playlist = playlistService.getPlaylistById(id);
        return playlist.map(ResponseEntity::ok).orElse(ResponseEntity.notFound().build());
    }

    // Create new playlist
    @PostMapping
    public ResponseEntity<Playlist> createPlaylist(@RequestBody PlaylistDTO playlistDTO) {
        // Преобразование PlaylistDTO в Playlist
        Set<Track> tracks = new HashSet<>();
        for (Long trackId : playlistDTO.getTrackIds()) {
            Optional<Track> track = trackService.getTrackById(trackId);
            track.ifPresent(tracks::add);
        }
        Playlist playlist = new Playlist(playlistDTO.getName());
        playlist.setTracks(tracks);

        Playlist savedPlaylist = playlistService.savePlaylist(playlist);
        return ResponseEntity.ok(savedPlaylist);
    }

    // Delete playlist
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deletePlaylist(@PathVariable Long id) {
        playlistService.deletePlaylist(id);
        return ResponseEntity.noContent().build();
    }
}
