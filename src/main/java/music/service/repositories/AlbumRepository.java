package music.service.repositories;

import music.service.model.Album;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface AlbumRepository extends JpaRepository<Album, Long> {

    Page<Album> findAllByTitle(String title, Pageable pageable);

    @Query("SELECT a FROM Album a JOIN a.users u WHERE u.username = :username")
    Page<Album> findByUserUsername(@Param("username") String username, Pageable pageable);

    @Query(
            value = "SELECT a.* FROM albums a "
                    + "JOIN public.users_albums au ON a.id = au.album_id "
                    + "JOIN users u ON au.user_id = u.id "
                    + "WHERE u.username = :username AND a.title = :title",
            countQuery = "SELECT COUNT(*) FROM albums a "
                    + "JOIN public.users_albums au ON a.id = au.album_id "
                    + "JOIN users u ON au.user_id = u.id "
                    + "WHERE u.username = :username AND a.title = :title",
            nativeQuery = true
    )
    Page<Album> findByUserUsernameAndTitleNative(
            @Param("username") String username,
            @Param("title") String title,
            Pageable pageable
    );

    @Query("SELECT a FROM Album a JOIN a.users u WHERE u.id = :userId")
    List<Album> findAllByUserId(@Param("userId") Long userId);
}