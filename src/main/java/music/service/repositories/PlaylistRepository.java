package music.service.repositories;


import java.util.List;

import music.service.model.Playlist;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface PlaylistRepository extends JpaRepository<Playlist, Long> {
    List<Playlist> findPlaylistsByName(String name);

    List<Playlist> findByUsers(String users);

    List<Playlist> findByTracks(String tracks);
}
