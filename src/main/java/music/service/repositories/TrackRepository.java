package music.service.repositories;

import music.service.model.Track;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface TrackRepository extends JpaRepository<Track, Long>,
        JpaSpecificationExecutor<Track> {

    @Query("SELECT DISTINCT t FROM Track t " +
            "LEFT JOIN t.album a " +
            "LEFT JOIN t.playlists p " +
            "LEFT JOIN t.users u " +
            "WHERE (:username IS NULL OR u.username = :username) " +
            "AND (:albumTitle IS NULL OR a.title = :albumTitle) " +
            "AND (:title IS NULL OR t.title LIKE %:title%) " +
            "AND (:genre IS NULL OR t.genre = :genre) " +
            "AND (:playlistName IS NULL OR p.name = :playlistName)")
    Page<Track> findTracks(@Param("username") String username,
                           @Param("albumTitle") String albumTitle,
                           @Param("title") String title,
                           @Param("genre") String genre,
                           @Param("playlistName") String playlistName,
                           Pageable pageable);

    @Query("SELECT t FROM Track t JOIN FETCH t.album WHERE t.id = :trackId")
    Optional<Track> findTrackWithAlbumById(@Param("trackId") Long trackId);

    @Query("SELECT t FROM Track t JOIN t.users a WHERE a.id = :userId")
    List<Track> findTracksByUserId(@Param("userId") Long userId);

}
