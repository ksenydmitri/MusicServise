package music.service.service;

import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.function.Function;
import java.util.stream.Collectors;
import javax.transaction.Transactional;
import music.service.config.CacheConfig;
import music.service.dto.*;
import music.service.exception.ResourceNotFoundException;
import music.service.exception.ValidationException;
import music.service.model.*;
import music.service.repositories.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;

@Service
public class TrackService {
    private static final int DEFAULT_PAGE = 0;
    private static final int DEFAULT_SIZE = 1;

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
    public Page<Track> getAllTracks(String username, String albumTitle, String title,
                                    String genre, String playlistName, Pageable pageable) {
        String cacheKey = buildTracksCacheKey(
                username, albumTitle,
                title, genre, playlistName,
                pageable.getPageNumber(),
                pageable.getPageSize());

        if (cacheService.containsKey(cacheKey)) {
            logger.debug("Cache hit for key: {}", cacheKey);
            return (Page<Track>) cacheService.get(cacheKey);
        }

        Page<Track> tracks = fetchTracksFromDatabase(
                username, albumTitle, title, genre, playlistName, pageable);
        cacheService.put(cacheKey, tracks);
        return tracks;
    }

    private Page<Track> fetchTracksFromDatabase(String username, String albumTitle,
                                                String title, String genre,
                                                String playlistName,
                                                Pageable pageable) {
        Specification<Track> specification = Specification.where(null);

        if (username != null && !username.isEmpty()) {
            specification = specification.and(
                    (root, query, criteriaBuilder) ->
                    criteriaBuilder.equal(root.join("users").get("username"), username));
        }

        if (albumTitle != null && !albumTitle.isEmpty()) {
            specification = specification.and(
                    (root, query, criteriaBuilder) ->
                    criteriaBuilder.equal(root.join("album").get("title"), albumTitle));
        }

        if (title != null && !title.isEmpty()) {
            specification = specification.and(
                    (root, query, criteriaBuilder) ->
                    criteriaBuilder.equal(root.get("title"), title));
        }

        if (genre != null && !genre.isEmpty()) {
            specification = specification.and((root, query, criteriaBuilder) ->
                    criteriaBuilder.equal(root.get("genre"), genre));
        }

        if (playlistName != null && !playlistName.isEmpty()) {
            specification = specification.and((root, query, criteriaBuilder) ->
                    criteriaBuilder.equal(root.join("playlists").get("name"), playlistName));
        }

        return trackRepository.findAll(specification, pageable);
    }

    @Transactional
    public TrackResponse addTrack(CreateTrackRequest request) throws ValidationException {
        if (request == null) {
            throw new ValidationException("Request must not be null");
        }
        if (request.getTitle() == null || request.getTitle().isEmpty()) {
            throw new ValidationException("Track title must not be empty");
        }
        if (request.getDuration() <= 0) {
            throw new ValidationException("Track duration must be positive");
        }

        Album album = albumRepository.findById(request.getAlbumId())
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Album not found with ID: " + request.getAlbumId()));

        Track track = new Track(request.getTitle(), request.getDuration());
        track.setGenre(request.getGenre());
        track.setAlbum(album);

        User user = userRepository.findById(request.getUserId())
                .orElseThrow(() -> new ResourceNotFoundException(
                        "User not found with ID: " + request.getUserId()));

        if (!track.getUsers().contains(user)) {
            track.getUsers().add(user);
        }
        album.getTracks().add(track);
        Track savedTrack = trackRepository.save(track);
        cacheService.clear();

        logger.info("Track added successfully with ID: {}", savedTrack.getId());
        return mapToTrackResponse(savedTrack);
    }

    @Transactional
    public TrackResponse updateTrack(Long trackId, UpdateTrackRequest request) {
        validateInput(trackId, request);

        Track track = trackRepository.findById(trackId)
                .orElseThrow(() -> new ResourceNotFoundException("Track not found with ID: " + trackId));

        updateTrackTitle(track, request.getTitle());
        updateTrackGenre(track, request.getGenre());
        updateTrackDuration(track, request.getDuration());

        if (request.getPlaylistId() != null) {
            addPlaylistToTrack(track, request.getPlaylistId());
        }
        if (request.getUserId() != null) {
            addUserToTrack(track, request.getUserId());
        }

        Track updatedTrack = trackRepository.save(track);

        String cacheKey = buildTracksCacheKey(
                track.getUsers() != null && !track.getUsers().isEmpty()
                        ? track.getUsers().iterator().next().getUsername()
                        : null,
                track.getAlbum() != null
                        ? track.getAlbum().getTitle()
                        : null,
                track.getTitle(),
                track.getGenre(),
                null,
                DEFAULT_PAGE, DEFAULT_SIZE
        );
        cacheService.evict(cacheKey);
        logger.info("Track updated successfully with ID: {}", updatedTrack.getId());
        return mapToTrackResponse(updatedTrack);
    }

    void validateInput(Long trackId, UpdateTrackRequest request) {
        if (trackId == null) {
            throw new IllegalArgumentException("Track ID must not be null");
        }
        if (request == null) {
            throw new IllegalArgumentException("Request body must not be null");
        }
    }

    void updateTrackTitle(Track track, String title) {
        if (title != null && !title.isEmpty()) {
            track.setTitle(title);
        } else if (title != null) {
            throw new IllegalArgumentException("Track title must not be empty");
        }
    }

    void updateTrackGenre(Track track, String genre) {
        if (genre != null && !genre.isEmpty()) {
            track.setGenre(genre);
        } else if (genre != null) {
            throw new IllegalArgumentException("Track genre must not be empty");
        }
    }

    void updateTrackDuration(Track track, int duration) {
        if (duration > 0) {
            track.setDuration(duration);
        }
    }

    void addPlaylistToTrack(Track track, Long playlistId) {
        if (playlistId != null) {
            Playlist playlist = playlistRepository.findById(playlistId)
                    .orElseThrow(() -> new ResourceNotFoundException(
                            "Playlist not found with ID: " + playlistId));
            if (!track.getPlaylists().contains(playlist)) {
                track.getPlaylists().add(playlist);
            }
        }
    }

    void addUserToTrack(Track track, Long userId) {
        if (userId != null) {
            User user = userRepository.findById(userId)
                    .orElseThrow(() -> new ResourceNotFoundException(
                            "User not found with ID: " + userId));
            if (!track.getUsers().contains(user)) {
                track.getUsers().add(user);
            }
        }
    }

    @Transactional
    public void deleteTrack(Long trackId) {
        Track track = trackRepository.findById(trackId)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Track not found with ID: " + trackId));

        Album album = track.getAlbum();

        if (track.getPlaylists() != null) {
            for (Playlist playlist : track.getPlaylists()) {
                playlist.getTracks().remove(track);
            }
        }

        if (track.getUsers() != null) {
            for (User user : track.getUsers()) {
                user.getTracks().remove(track);
            }
        }

        evictAllTrackCaches();

        trackRepository.delete(track);
        logger.info("Track deleted successfully with ID: {}", trackId);
    }

    public TrackResponse mapToTrackResponse(Track track) {
        TrackResponse response = new TrackResponse();
        response.setId(track.getId());
        response.setTitle(track.getTitle());
        response.setDuration(track.getDuration());
        response.setAlbumTitle(track.getAlbum() != null ? track.getAlbum().getTitle() : null);
        response.setUsernames(track.getUsers() != null
                ? track.getUsers().stream().map(User::getUsername).collect(Collectors.toList())
                : List.of());
        response.setReleaseDate(track.getReleaseDate());
        response.setPlaylists(track.getPlaylists() != null
                ? track.getPlaylists().stream().map(Playlist::getName).collect(Collectors.toList())
                : List.of());
        response.setGenre(track.getGenre());
        return response;
    }

    private String buildTracksCacheKey(String user, String albumTitle, String title,
                                       String genre, String playlistName, int page, int size) {
        return String.format("tracks_%s_%s_%s_%s_%s_%d_%d",
                user != null ? user : "all",
                albumTitle != null ? albumTitle : "all",
                title != null ? title : "all",
                genre != null ? genre : "all",
                playlistName != null ? playlistName : "all",
                page,
                size
        );
    }

    private void evictAllTrackCaches() {
        cacheService.evictByPattern("tracks_*");
        cacheService.evictByPattern("track_*");
    }

    @Transactional
    public List<TrackResponse> addTracksBulk(List<CreateTrackRequest> requests) {
        validateBulkRequest(requests);

        Map<Long, Album> albums = fetchRequiredAlbums(requests);
        Map<Long, User> users = fetchRequiredUsers(requests);

        List<Track> tracks = createTracksFromRequests(requests, albums, users);
        List<Track> savedTracks = saveAllTracks(tracks);

        cacheService.clear();
        logBulkOperation(savedTracks.size());

        return mapToTrackResponses(savedTracks);
    }

    private void validateBulkRequest(List<CreateTrackRequest> requests) {
        if (requests == null || requests.isEmpty()) {
            throw new ValidationException("Request list must not be null or empty");
        }

        requests.forEach(this::validateSingleRequest);
    }

    void validateSingleRequest(CreateTrackRequest request) {
        if (request == null) {
            throw new ValidationException("Request in list must not be null");
        }
        if (request.getTitle() == null || request.getTitle().isEmpty()) {
            throw new ValidationException("Track title must not be empty");
        }
        if (request.getDuration() <= 0) {
            throw new ValidationException("Track duration must be positive");
        }
    }

    private Map<Long, Album> fetchRequiredAlbums(List<CreateTrackRequest> requests) {
        Set<Long> albumIds = extractAlbumIds(requests);
        List<Album> foundAlbums = albumRepository.findAllById(albumIds);

        return foundAlbums.stream()
                .collect(Collectors.toMap(Album::getId, Function.identity()));
    }

    private Set<Long> extractAlbumIds(List<CreateTrackRequest> requests) {
        return requests.stream()
                .map(CreateTrackRequest::getAlbumId)
                .collect(Collectors.toSet());
    }

    private Map<Long, User> fetchRequiredUsers(List<CreateTrackRequest> requests) {
        Set<Long> userIds = extractUserIds(requests);
        List<User> foundUsers = userRepository.findAllById(userIds);

        return foundUsers.stream()
                .collect(Collectors.toMap(User::getId, Function.identity()));
    }

    private Set<Long> extractUserIds(List<CreateTrackRequest> requests) {
        return requests.stream()
                .map(CreateTrackRequest::getUserId)
                .collect(Collectors.toSet());
    }

    private List<Track> createTracksFromRequests(List<CreateTrackRequest> requests,
                                                 Map<Long, Album> albums,
                                                 Map<Long, User> users) {
        return requests.stream()
                .map(request -> createSingleTrack(request, albums, users))
                .collect(Collectors.toList());
    }

    private Track createSingleTrack(CreateTrackRequest request,
                                    Map<Long, Album> albums,
                                    Map<Long, User> users) {
        Album album = getAlbumOrThrow(request.getAlbumId(), albums);
        User user = getUserOrThrow(request.getUserId(), users);

        Track track = new Track(request.getTitle(), request.getDuration());
        track.setGenre(request.getGenre());
        track.setAlbum(album);
        track.getUsers().add(user);
        album.getTracks().add(track);

        return track;
    }

    private Album getAlbumOrThrow(Long albumId, Map<Long, Album> albums) {
        Album album = albums.get(albumId);
        if (album == null) {
            throw new ResourceNotFoundException("Album not found with ID: " + albumId);
        }
        return album;
    }

    private User getUserOrThrow(Long userId, Map<Long, User> users) {
        User user = users.get(userId);
        if (user == null) {
            throw new ResourceNotFoundException("User not found with ID: " + userId);
        }
        return user;
    }

    private List<Track> saveAllTracks(List<Track> tracks) {
        return trackRepository.saveAll(tracks);
    }


    private void logBulkOperation(int tracksCount) {
        logger.info("Added {} tracks in bulk operation", tracksCount);
    }

    private List<TrackResponse> mapToTrackResponses(List<Track> tracks) {
        return tracks.stream()
                .map(this::mapToTrackResponse)
                .collect(Collectors.toList());
    }
}