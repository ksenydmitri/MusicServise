package music.service.service;

import java.util.List;
import music.service.dto.CreateGenreRequest;
import music.service.dto.GenreResponse;
import music.service.model.Genre;
import music.service.repositories.GenreRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class GenreService {

    private final GenreRepository genreRepository;

    @Autowired
    public GenreService(GenreRepository genreRepository) {
        this.genreRepository = genreRepository;
    }

    public List<Genre> getAllGenres() {
        return genreRepository.findAll();
    }

    public Genre saveGenre(Genre genre) {
        return genreRepository.save(genre);
    }

    public GenreResponse mapToGenreResponse(Genre genre) {
        GenreResponse response = new GenreResponse();
        response.setId(genre.getId());
        response.setName(genre.getName());
        return response;
    }

    public Genre mapToGenre(CreateGenreRequest request) {
        Genre genre = new Genre();
        genre.setName(request.getName());
        return genre;
    }
}
