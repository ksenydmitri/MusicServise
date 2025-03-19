package music.service.repositories;

import music.service.model.Playlist;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

@Repository
public interface PlaylistRepository extends JpaRepository<Playlist, Long> {
    // Метод для поиска по названию с пагинацией
    Page<Playlist> findAllByName(String name, Pageable pageable);

    // Метод для поиска по имени пользователя с пагинацией
    @Query("SELECT p FROM Playlist p JOIN p.users u WHERE u.username = :username")
    Page<Playlist> findByUserUsername(@Param("username") String username, Pageable pageable);

    // Нативный запрос для поиска по имени пользователя и названию с пагинацией
    @Query(
            value = "SELECT p.* FROM playlists p " +
                    "JOIN public.users_playlists up ON p.id = up.playlist_id " +
                    "JOIN users u ON up.user_id = u.id " +
                    "WHERE u.username = :username AND p.name = :name",
            countQuery = "SELECT COUNT(*) FROM playlists p " +
                    "JOIN public.users_playlists up ON p.id = up.playlist_id " +
                    "JOIN users u ON up.user_id = u.id " +
                    "WHERE u.username = :username AND p.name = :name",
            nativeQuery = true
    )
    Page<Playlist> findByUserUsernameAndNameNative(
            @Param("username") String username,
            @Param("name") String name,
            Pageable pageable
    );
}