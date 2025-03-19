package music.service.repositories;

import music.service.model.Album;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface AlbumRepository extends JpaRepository<Album, Long> {
    List<Album> findAllByTitle(String title);

    @Query("SELECT a FROM Album a JOIN a.users u WHERE u.username = :username")
    List<Album> findByUserUsername(@Param("username") String username);

    @Query(value = "SELECT a.* FROM albums a "
            + "JOIN public.users_albums au ON a.id = au.album_id "
            + "JOIN users u ON au.user_id = u.id "
            + "WHERE u.username = :username AND a.title = :title", nativeQuery = true)
    List<Album> findByUserUsernameAndTitleNative(
            @Param("username") String username,
            @Param("title") String title
    );

}
