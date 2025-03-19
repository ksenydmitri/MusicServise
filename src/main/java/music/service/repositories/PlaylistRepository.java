package music.service.repositories;

import java.util.List;
import music.service.model.Playlist;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;


@Repository
public interface PlaylistRepository extends JpaRepository<Playlist, Long> {
    List<Playlist> findAllByName(String title);

    @Query("SELECT a FROM Playlist a JOIN a.users u WHERE u.username = :username")
    List<Playlist> findByUserUsername(@Param("username") String username);

    @Query(value = "SELECT a.* FROM playlists a "
            + "JOIN public.users_playlists au ON a.id = au.playlist_id "
            + "JOIN users u ON au.user_id = u.id "
            + "WHERE u.username = :username AND a.name = :name", nativeQuery = true)
    List<Playlist> findByUserUsernameAndNameNative(
            @Param("username") String username,
            @Param("name") String name
    );
}
