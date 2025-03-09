package music.service.repositories;

import music.service.model.Track;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import javax.validation.constraints.NotBlank;
import java.util.List;
import java.util.Optional;

@Repository
public interface TrackRepository extends JpaRepository<Track, Long> {
    List<Track> findByGenre(String name);

    List<Track> findByAlbumTitle(String title);

    List<Track> findByUsersUsername(String username);

    List<Track> findByPlaylistsName(String name);

    Optional<Track> findByTitle(@NotBlank(message = "Title is required") String title);
}
