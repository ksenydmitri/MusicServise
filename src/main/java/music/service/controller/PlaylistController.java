package music.service.controller;

import java.util.*;
import java.util.stream.Collectors;
import javax.validation.Valid;
import music.service.dto.*;
import music.service.model.Playlist;
import music.service.service.PlaylistService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/playlists")
public class PlaylistController {

    private final PlaylistService playlistService;

    public PlaylistController(PlaylistService playlistService) {
        this.playlistService = playlistService;
    }

    @GetMapping
    public ResponseEntity<List<PlaylistResponse>> getAllPlaylists(
            @RequestParam(required = false) String user,
            @RequestParam(required = false) String name
    ) {
        List<Playlist> playlists = playlistService.getAllPlaylists(user, name);
        List<PlaylistResponse> responses = playlists.stream()
                .map(playlistService::mapToPlaylistResponse)
                .collect(Collectors.toList());
        return ResponseEntity.ok(responses);
    }

    @GetMapping("/{id}")
    public ResponseEntity<PlaylistResponse> getPlaylistById(@PathVariable Long id) {
        Optional<Playlist> playlistOpt = playlistService.getPlaylistById(id);
        return playlistOpt.map(playlist -> ResponseEntity.ok(
                playlistService.mapToPlaylistResponse(playlist)))
                .orElse(ResponseEntity.notFound().build());
    }

    @PostMapping
    public ResponseEntity<PlaylistResponse> createPlaylist(
            @Valid @RequestBody CreatePlaylistRequest request) {
        Playlist playlist = new Playlist();
        playlist.setName(request.getName());
        Playlist savedPlaylist = playlistService.savePlaylist(request);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(playlistService.mapToPlaylistResponse(savedPlaylist));
    }

    @PatchMapping("/{id}")
    public ResponseEntity<PlaylistResponse> patchPlaylist(
            @PathVariable Long id,
            @RequestBody UpdatePlaylistRequest request) {
        PlaylistResponse response = playlistService.updatePlaylist(id, request);
        return ResponseEntity.ok(response);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deletePlaylist(@PathVariable Long id) {
        playlistService.deletePlaylist(id);
        return ResponseEntity.noContent().build();
    }
}
