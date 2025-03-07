package music.service.controller;


import java.util.List;
import java.util.stream.Collectors;
import javax.validation.Valid;
import music.service.dto.CreateGenreRequest;
import music.service.dto.GenreResponse;
import music.service.model.Genre;
import music.service.service.GenreService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/genres")
public class GenreController {

    private final GenreService genreService;

    public GenreController(GenreService genreService) {
        this.genreService = genreService;
    }

    @GetMapping
    public ResponseEntity<List<GenreResponse>> getAllGenres() {
        List<Genre> genres = genreService.getAllGenres();
        List<GenreResponse> responses = genres.stream()
                .map(genreService::mapToGenreResponse)
                .collect(Collectors.toList());
        return ResponseEntity.ok(responses);
    }

    @PostMapping
    public ResponseEntity<GenreResponse> createGenre(
            @Valid @RequestBody CreateGenreRequest request) {
        Genre genre = new Genre();
        genre.setName(request.getName());
        Genre savedGenre = genreService.saveGenre(genre);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(genreService.mapToGenreResponse(savedGenre));
    }
}
