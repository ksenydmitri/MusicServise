package music.service.service;

import java.io.IOException;
import java.io.InputStream;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;
import javax.transaction.Transactional;
import com.google.api.client.http.InputStreamContent;
import com.google.api.services.drive.Drive;
import com.google.api.services.drive.model.File;
import music.service.dto.*;
import music.service.exception.ResourceNotFoundException;
import music.service.exception.ValidationException;
import music.service.model.*;
import music.service.repositories.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

@Service
public class TrackService {
    private static final Logger logger = LoggerFactory.getLogger(TrackService.class);

    private final TrackRepository trackRepository;
    private final AlbumRepository albumRepository;
    private final UserRepository userRepository;
    private final CacheService cacheService;
    private final Drive googleDriveService;
    private final MediaService mediaService;

    private static final int DEFAULT_PAGE = 0;
    private static final int DEFAULT_SIZE = 1;
    private static final String TRACKS_CACHE_PREFIX = "tracks";
    private static final String TRACK_CACHE_PREFIX = "track";
    private final PlaylistRepository playlistRepository;

    @Autowired
    public TrackService(TrackRepository trackRepository,
                        AlbumRepository albumRepository,
                        UserRepository userRepository,
                        CacheService cacheService,
                        Drive googleDriveService, MediaService mediaService, PlaylistRepository playlistRepository) {
        this.trackRepository = trackRepository;
        this.albumRepository = albumRepository;
        this.userRepository = userRepository;
        this.cacheService = cacheService;
        this.googleDriveService = googleDriveService;
        this.mediaService = mediaService;
        this.playlistRepository = playlistRepository;
    }


    @Transactional
    public TrackResponse addTrackWithMedia(CreateTrackRequest request, MultipartFile mediaFile)
            throws ValidationException, IOException {

        validateTrackRequest(request);
        validateMediaFile(mediaFile);

        File uploadedFile = uploadMediaToDrive(mediaFile, request.getTitle());
        Track track = createTrackFromRequest(request);
        Track savedTrack = saveTrackWithRelations(track, request.getUserId());

        return buildTrackResponse(savedTrack, uploadedFile);
    }

    @Transactional
    public TrackResponse updateTrack(Long trackId, UpdateTrackRequest request) {
        validateUpdateInput(trackId, request);

        Track track = getTrackById(trackId);
        updateTrackFields(track, request);
        Track updatedTrack = trackRepository.save(track);

        evictTrackCache(track);
        return mapToTrackResponse(updatedTrack);
    }

    @Transactional
    public void deleteTrack(Long trackId) {
        Track track = getTrackById(trackId);
        deleteMediaFileFromDrive(track.getMediaFileId());
        removeTrackRelations(track);
        trackRepository.delete(track);

        evictAllTrackCaches();
    }

    @Transactional
    public Page<Track> getAllTracks(String username, String albumTitle, String title,
                                    String genre, String playlistName, Pageable pageable) {

        String cacheKey = buildTracksCacheKey(username, albumTitle, title, genre, playlistName, pageable);

        if (cacheService.containsKey(cacheKey)) {
            logger.debug("Cache hit for key: {}", cacheKey);
            return (Page<Track>) cacheService.get(cacheKey);
        }

        Page<Track> tracks = fetchFilteredTracks(username, albumTitle, title, genre, playlistName, pageable);
        cacheService.put(cacheKey, tracks);
        return tracks;
    }

    @Transactional
    public List<TrackResponse> addTracksBulk(List<CreateTrackRequest> requests) {
        validateBulkRequest(requests);

        Map<Long, Album> albums = fetchAlbumsForRequests(requests);
        Map<Long, User> users = fetchUsersForRequests(requests);

        List<Track> tracks = createTracksFromRequests(requests, albums, users);
        List<Track> savedTracks = trackRepository.saveAll(tracks);

        cacheService.clear();
        logger.info("Added {} tracks in bulk operation", savedTracks.size());

        return mapToTrackResponses(savedTracks);
    }



    private Track createTrackFromRequest(CreateTrackRequest request) {
        validateTrackFile(request.getMediaFile());
        Album album = getAlbumById(request.getAlbumId());

        Track track = new Track(request.getTitle(), request.getDuration());
        track.setGenre(request.getGenre());
        track.setAlbum(album);
        String mediaFileId = mediaService.uploadMedia(request.getMediaFile());
        track.setMediaFileId(mediaFileId);

        return track;
    }

    private List<Track> createTracksFromRequests(List<CreateTrackRequest> requests,
                                                 Map<Long, Album> albums,
                                                 Map<Long, User> users) {
        return requests.stream()
                .map(request -> {
                    String mediaFileId = mediaService.uploadMedia(request.getMediaFile());
                    Track track = createTrackFromRequest(request);
                    Album album = albums.get(request.getAlbumId());
                    track.setAlbum(album);
                    User user = users.get(request.getUserId());
                    track.setUsers(Set.of(user));

                    return track;
                })
                .collect(Collectors.toList());
    }


    private Track saveTrackWithRelations(Track track, Long userId) {
        User user = getUserById(userId);
        track.getUsers().add(user);
        track.getAlbum().getTracks().add(track);

        Track savedTrack = trackRepository.save(track);
        cacheService.clear();

        logger.info("Track added successfully with ID: {}", savedTrack.getId());
        return savedTrack;
    }

    private File uploadMediaToDrive(MultipartFile mediaFile, String title) throws IOException {
        File fileMetadata = new File();
        fileMetadata.setName(title + "_" + System.currentTimeMillis());

        try (InputStream fileStream = mediaFile.getInputStream()) {
            return googleDriveService.files().create(
                    fileMetadata,
                    new InputStreamContent(mediaFile.getContentType(), fileStream)
            ).setFields("id, webViewLink").execute();
        }
    }

    private void updateTrackFields(Track track, UpdateTrackRequest request) {

        if (request.getTitle() != null && !request.getTitle().isEmpty()) {
            track.setTitle(request.getTitle());
        }
        if (request.getDuration() > 0) {
            track.setDuration(request.getDuration());
        }

        if (request.getGenre() != null && !request.getGenre().isEmpty()) {
            track.setGenre(request.getGenre());
        }

        if (request.getUserIds() != null && !request.getUserIds().isEmpty()) {
            Set<User> users = request.getUserIds().stream()
                    .map(this::getUserById)
                    .collect(Collectors.toSet());
            track.setUsers(users);
        }

        if (request.getPlaylistIds() != null && !request.getPlaylistIds().isEmpty()) {
            Set<Playlist> playlists = request.getPlaylistIds().stream()
                    .map(this::getPlaylistById)
                    .collect(Collectors.toSet());
            track.setPlaylists(playlists);
        }
    }


    private void deleteMediaFileFromDrive(String mediaFileId) {
        if (mediaFileId != null) {
            try {
                googleDriveService.files().delete(mediaFileId).execute();
            } catch (IOException e) {
                logger.warn("Could not delete media file {}: {}", mediaFileId, e.getMessage());
            }
        }
    }

    private void removeTrackRelations(Track track) {
        track.getUsers().clear();
        track.getPlaylists().clear();
    }

    private void validateTrackRequest(CreateTrackRequest request) {
        if (request == null) {
            throw new ValidationException("Request must not be null");
        }
        if (request.getTitle() == null || request.getTitle().isEmpty()) {
            throw new ValidationException("Track title must not be empty");
        }
        if (request.getDuration() <= 0) {
            throw new ValidationException("Track duration must be positive");
        }
    }

    private void validateMediaFile(MultipartFile mediaFile) {
        if (mediaFile == null || mediaFile.isEmpty()) {
            throw new ValidationException("Media file must be provided");
        }
        String contentType = mediaFile.getContentType();
        if (!isSupportedMediaType(contentType)) {
            throw new ValidationException("Unsupported media type: " + contentType);
        }
    }

    private void validateBulkRequest(List<CreateTrackRequest> requests) {
        if (requests == null || requests.isEmpty()) {
            throw new ValidationException("Requests must not be null or empty");
        }
        requests.forEach(this::validateTrackRequest); // Использование существующего метода validateTrackRequest
    }

    private boolean isSupportedMediaType(String contentType) {
        return contentType != null && (
                contentType.startsWith("audio/") ||
                        contentType.equals("application/octet-stream") // для некоторых аудиофайлов
        );
    }

    private void validateUpdateInput(Long trackId, UpdateTrackRequest request) {
        if (trackId == null) {
            throw new IllegalArgumentException("Track ID must not be null");
        }
        if (request == null) {
            throw new IllegalArgumentException("Request body must not be null");
        }
    }

    private String buildTracksCacheKey(String username, String albumTitle, String title,
                                       String genre, String playlistName, Pageable pageable) {
        return String.format("%s_%s_%s_%s_%s_%d_%d",
                TRACKS_CACHE_PREFIX,
                username != null ? username : "all",
                albumTitle != null ? albumTitle : "all",
                title != null ? title : "all",
                genre != null ? genre : "all",
                playlistName != null ? playlistName : "all",
                pageable.getPageNumber(),
                pageable.getPageSize()
        );
    }

    private void evictTrackCache(Track track) {
        String cacheKey = buildTracksCacheKey(
                track.getUsers().isEmpty() ? null : track.getUsers().iterator().next().getUsername(),
                track.getAlbum() != null ? track.getAlbum().getTitle() : null,
                track.getTitle(),
                track.getGenre(),
                null,
                PageRequest.of(DEFAULT_PAGE, DEFAULT_SIZE)
        );
        cacheService.evict(cacheKey);
    }

    private void evictAllTrackCaches() {
        cacheService.evictByPattern(TRACKS_CACHE_PREFIX + "_*");
        cacheService.evictByPattern(TRACK_CACHE_PREFIX + "_*");
    }

    private TrackResponse buildTrackResponse(Track track, File mediaFile) {
        TrackResponse response = mapToTrackResponse(track);
        response.setMediaFileId(mediaFile.getId());
        response.setMediaUrl(mediaFile.getWebViewLink());
        return response;
    }

    public TrackResponse mapToTrackResponse(Track track) {
        return TrackResponse.builder()
                .id(track.getId())
                .title(track.getTitle())
                .duration(track.getDuration())
                .albumTitle(track.getAlbum() != null ? track.getAlbum().getTitle() : null)
                .usernames(track.getUsers().stream().map(User::getUsername).collect(Collectors.toList()))
                .releaseDate(track.getReleaseDate())
                .playlists(track.getPlaylists().stream().map(Playlist::getName).collect(Collectors.toList()))
                .genre(track.getGenre())
                .build();
    }

    public List<TrackResponse> mapToTrackResponses(List<Track> tracks) {
        return tracks.stream()
                .map(this::mapToTrackResponse)
                .collect(Collectors.toList());
    }

    private Track getTrackById(Long trackId) {
        return trackRepository.findById(trackId)
                .orElseThrow(() -> new ResourceNotFoundException("Track not found with ID: " + trackId));
    }

    private Album getAlbumById(Long albumId) {
        return albumRepository.findById(albumId)
                .orElseThrow(() -> new ResourceNotFoundException("Album not found with ID: " + albumId));
    }

    private User getUserById(Long userId) {
        return userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User not found with ID: " + userId));
    }

    private Playlist getPlaylistById(Long playlistId) {
        return playlistRepository.findById(playlistId)
                .orElseThrow(() -> new ResourceNotFoundException("Playlist not found with ID: " + playlistId));
    }

    private Map<Long, Album> fetchAlbumsForRequests(List<CreateTrackRequest> requests) {
        List<Long> albumIds = requests.stream()
                .map(CreateTrackRequest::getAlbumId)
                .filter(Objects::nonNull)
                .distinct()
                .collect(Collectors.toList());
        List<Album> albums = albumRepository.findAllById(albumIds);
        return albums.stream()
                .collect(Collectors.toMap(Album::getId, album -> album));
    }


    private Page<Track> fetchFilteredTracks(String username, String albumTitle, String title,
                                            String genre, String playlistName, Pageable pageable) {
        return trackRepository.findTracks(playlistName,username,title,genre,albumTitle,pageable);
    }

    private Map<Long, User> fetchUsersForRequests(List<CreateTrackRequest> requests) {
        List<Long> userIds = requests.stream()
                .map(CreateTrackRequest::getUserId)
                .filter(Objects::nonNull)
                .distinct()
                .collect(Collectors.toList());
        List<User> users = userRepository.findAllById(userIds);
        return users.stream()
                .collect(Collectors.toMap(User::getId, user -> user));
    }

    public void validateTrackFile(MultipartFile mediaFile) {
        Set<String> allowedContentTypes = Set.of(
                "audio/mpeg",    // MP3
                "audio/wav",     // WAV
                "audio/x-wav",   // WAV (альтернативный)
                "audio/aac",     // AAC
                "audio/flac"     // FLAC
        );
        if (!allowedContentTypes.contains(mediaFile.getContentType())) {
            throw new ValidationException("Недопустимый тип файла. " +
                    "Разрешены только аудиофайлы (MP3, WAV, AAC, FLAC)");
        }
    }


}