package music.service.controller;

import java.util.List;
import java.util.stream.Collectors;
import javax.validation.Valid;
import music.service.dto.AlbumResponse;
import music.service.dto.CreateAlbumRequest;
import music.service.model.Album;
import music.service.service.AlbumService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/albums")
public class AlbumController {

    private final AlbumService albumService;

    public AlbumController(AlbumService albumService) {
        this.albumService = albumService;
    }

    @GetMapping
    public ResponseEntity<List<AlbumResponse>> getAllAlbums() {
        List<Album> albums = albumService.getAllAlbums();
        List<AlbumResponse> responses = albums.stream()
                .map(albumService::mapToAlbumResponse)
                .collect(Collectors.toList());
        return ResponseEntity.ok(responses);
    }

    @GetMapping("/{id}")
    public ResponseEntity<AlbumResponse> getAlbumById(@PathVariable Long id) {
        Album album = albumService.getAlbumById(id);
        return ResponseEntity.ok(albumService.mapToAlbumResponse(album));
    }

    @PostMapping
    public ResponseEntity<AlbumResponse> addAlbum(
            @Valid @RequestBody CreateAlbumRequest request) {
        AlbumResponse response = albumService.addAlbum(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }
}
