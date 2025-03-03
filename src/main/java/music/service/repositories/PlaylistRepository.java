package music.service.repositories;


import java.util.Collection;
import java.util.List;
import music.service.model.Playlist;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface PlaylistRepository extends JpaRepository<Playlist, Long> {
    List<Playlist> findPlaylistsByNameIn(Collection<String> names);
}
