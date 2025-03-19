package music.service.service;

import java.util.List;
import java.util.stream.Collectors;
import javax.transaction.Transactional;
import music.service.dto.*;
import music.service.model.*;
import music.service.repositories.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

@Service
public class TrackService {
    private static final Logger logger = LoggerFactory.getLogger(TrackService.class);
    private final TrackRepository trackRepository;
    private final AlbumRepository albumRepository;
    private final UserRepository userRepository;
    private final PlaylistRepository playlistRepository;
    private final CacheService cacheService;

    @Autowired
    public TrackService(TrackRepository trackRepository, AlbumRepository albumRepository,
                        UserRepository userRepository, PlaylistRepository playlistRepository,
                        CacheService cacheService) {
        this.trackRepository = trackRepository;
        this.albumRepository = albumRepository;
        this.userRepository = userRepository;
        this.playlistRepository = playlistRepository;
        this.cacheService = cacheService;
    }

    @Transactional
    public List<Track> getAllTracks(String username, String albumTitle, String title,
                                    String genre, String playlistName) {
        String cacheKey = buildTracksCacheKey(username, albumTitle, title, genre, playlistName);

        if (cacheService.containsKey(cacheKey)) {
            logger.debug("Cache hit for key: {}", cacheKey);
            return (List<Track>) cacheService.get(cacheKey);
        }

        List<Track> tracks = fetchTracksFromDatabase(
                username, albumTitle, title, genre, playlistName);
        cacheService.put(cacheKey, tracks);
        return tracks;
    }

    private List<Track> fetchTracksFromDatabase(String username, String albumTitle,
                                                String title, String genre, String playlistName) {
        Specification<Track> specification = Specification.where(null);

        if (username != null && !username.isEmpty()) {
            specification = specification.and((root, query, criteriaBuilder) ->
                    criteriaBuilder.equal(root.join("users").get("username"), username));
        }

        if (albumTitle != null && !albumTitle.isEmpty()) {
            specification = specification.and((root, query, criteriaBuilder) ->
                    criteriaBuilder.equal(root.join("album").get("title"), albumTitle));
        }

        if (title != null && !title.isEmpty()) {
            specification = specification.and((root, query, criteriaBuilder) ->
                    criteriaBuilder.equal(root.get("title"), title));
        }

        if (genre != null && !genre.isEmpty()) {
            specification = specification.and((root, query, criteriaBuilder) ->
                    criteriaBuilder.equal(root.get("genre"), genre));
        }

        if (playlistName != null) {
            specification = specification.and((root, query, criteriaBuilder) ->
                    criteriaBuilder.equal(root.join("playlists").get("id"), playlistName));
        }

        return trackRepository.findAll(specification);
    }


    @Transactional
    public TrackResponse addTrackWithFile(CreateTrackRequest request, MultipartFile file) {

        Album album = albumRepository.findById(request.getAlbumId())
                .orElseThrow(() -> new RuntimeException("Album not found"));
        Track track = new Track(request.getTitle(), request.getDuration());
        track.setGenre(request.getGenre());
        track.setAlbum(album);

        User user = userRepository.findById(request.getUserId())
                .orElseThrow(() -> new RuntimeException("User not found"));

        if (!track.getUsers().contains(user)) {
            track.getUsers().add(user);
        }
        album.getTracks().add(track);
        Track savedTrack = trackRepository.save(track);

        return mapToTrackResponse(savedTrack);
    }

    @Transactional
    public TrackResponse updateTrack(Long trackId, UpdateTrackRequest request) {
        validateInput(trackId, request);

        Track track = trackRepository.findById(trackId)
                .orElseThrow(() -> new RuntimeException("Track not found"));

        updateTrackTitle(track, request.getTitle());
        updateTrackGenre(track, request.getGenre());
        updateTrackDuration(track, request.getDuration());
        addPlaylistToTrack(track, request.getPlaylistId());
        addUserToTrack(track, request.getUserId());

        Track updatedTrack = trackRepository.save(track);

        String cacheKey = buildTracksCacheKey(
                track.getUsers().isEmpty() ? null : track.getUsers().iterator().next().getUsername(),
                track.getAlbum() != null ? track.getAlbum().getTitle() : null,
                track.getTitle(), track.getGenre(), null
        );

        cacheService.evict(cacheKey);

        return mapToTrackResponse(updatedTrack);
    }

    private void validateInput(Long trackId, UpdateTrackRequest request) {
        if (trackId == null) {
            throw new IllegalArgumentException("Track ID must not be null");
        }
        if (request == null) {
            throw new IllegalArgumentException("Request body must not be null");
        }
    }

    private void updateTrackTitle(Track track, String title) {
        if (title != null && !title.isEmpty()) {
            track.setTitle(title);
        } else if (title != null) {
            throw new IllegalArgumentException("Track title must not be empty");
        }
    }

    private void updateTrackGenre(Track track, String genre) {
        if (genre != null && !genre.isEmpty()) {
            track.setGenre(genre);
        } else if (genre != null) {
            throw new IllegalArgumentException("Track genre must not be empty");
        }
    }

    private void updateTrackDuration(Track track, int duration) {
        if (duration > 0) {
            track.setDuration(duration);
        }
    }

    private void addPlaylistToTrack(Track track, Long playlistId) {
        if (playlistId != null) {
            Playlist playlist = playlistRepository.findById(playlistId)
                    .orElseThrow(() -> new RuntimeException("Playlist not found"));
            if (!track.getPlaylists().contains(playlist)) {
                track.getPlaylists().add(playlist);
            }
        }
    }

    private void addUserToTrack(Track track, Long userId) {
        if (userId != null) {
            User user = userRepository.findById(userId)
                    .orElseThrow(() -> new RuntimeException("User not found"));
            if (!track.getUsers().contains(user)) {
                track.getUsers().add(user);
            }
        }
    }

    @Transactional
    public void deleteTrack(Long trackId) {
        Track track = trackRepository.findById(trackId)
                .orElseThrow(() -> new RuntimeException("Track not found"));

        for (Playlist playlist : track.getPlaylists()) {
            playlist.getTracks().remove(track);
        }

        for (User user : track.getUsers()) {
            user.getTracks().remove(track);
        }

        trackRepository.delete(track);
    }


    public TrackResponse mapToTrackResponse(Track track) {
        TrackResponse response = new TrackResponse();
        response.setId(track.getId());
        response.setTitle(track.getTitle());
        response.setDuration(track.getDuration());
        response.setAlbumTitle(track.getAlbum() != null ? track.getAlbum().getTitle() : null);
        response.setUsernames(track.getUsers().stream()
                .map(User::getUsername)
                .collect(Collectors.toList()));
        response.setReleaseDate(track.getReleaseDate());
        response.setPlaylists(track.getPlaylists().stream()
                .map(Playlist::getName)
                .collect(Collectors.toList()));
        response.setGenre(track.getGenre());
        return response;
    }

    private String buildTracksCacheKey(String user, String albumTitle, String title,
                                       String genre, String playlistName) {
        return String.format("albums_%s_%s_%s_%s_%s",
                user != null ? user : "all",
                albumTitle != null ? albumTitle : "all",
                title != null ? title : "all",
                genre != null ? genre : "all",
                playlistName != null ? playlistName : "all"
        );
    }

}

