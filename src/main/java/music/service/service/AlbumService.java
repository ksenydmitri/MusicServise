package music.service.service;

import java.util.List;
import java.util.stream.Collectors;
import javax.transaction.Transactional;
import music.service.dto.*;
import music.service.exception.ResourceNotFoundException;
import music.service.model.Album;
import music.service.model.User;
import music.service.repositories.AlbumRepository;
import music.service.repositories.UserRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

@Service
public class AlbumService {
    private static final Logger logger = LoggerFactory.getLogger(
            AlbumService.class);
    private final AlbumRepository albumRepository;
    private final UserRepository userRepository;
    private final CacheService cacheService;
    private final MediaService mediaService;

    @Autowired
    public AlbumService(AlbumRepository albumRepository,
                        UserRepository userRepository,
                        CacheService cacheService,
                        MediaService mediaService) {
        this.albumRepository = albumRepository;
        this.userRepository = userRepository;
        this.cacheService = cacheService;
        this.mediaService = mediaService;
    }

    @Transactional
    public Page<Album> getAllAlbums(String user, String title, int page, int size, String sortBy) {
        String cacheKey = buildAlbumsCacheKey(user, title, page, size, sortBy);
        logger.debug("Generated cache key: {}", cacheKey);

        if (cacheService.containsKey(cacheKey)) {
            logger.info("Cache hit for key: {}", cacheKey);
            Object cachedResult = cacheService.get(cacheKey);
            logger.debug("Cached result: {}", cachedResult);
            return (Page<Album>) cachedResult;
        }

        Pageable pageable = PageRequest.of(page, size, Sort.by(sortBy));
        Page<Album> albums = fetchAlbumsFromDatabase(user, title, pageable);
        logger.debug("Fetched from database: {}", albums);

        cacheService.put(cacheKey, albums);
        return albums;
    }

    Page<Album> fetchAlbumsFromDatabase(String user,
                                        String title,
                                        Pageable pageable) {
        if (user != null && title != null) {
            return albumRepository.findByUserUsernameAndTitleNative(
                    user, title, pageable);
        } else if (user != null) {
            return albumRepository.findByUserUsername(user, pageable);
        } else if (title != null) {
            return albumRepository.findAllByTitle(title, pageable);
        } else {
            return albumRepository.findAll(pageable);
        }
    }

    private String buildAlbumsCacheKey(
            String user, String title, int page, int size, String sortBy) {
        return String.format("albums_%s_%s_page%d_size_%d_sort_%s",
                user != null ? user : "all",
                title != null ? title : "all",
                page, size, sortBy
        );
    }

    @Transactional
    public AlbumResponse mapToAlbumResponse(Album album) {
        AlbumResponse response = new AlbumResponse();
        response.setId(album.getId());
        response.setTitle(album.getTitle());
        response.setArtists(album.getUsers().stream()
                .map(User::getUsername)
                .collect(Collectors.toList()));
        response.setTracks(album.getTracks().stream()
                .map(track -> TrackResponse.builder()
                        .id(track.getId())
                        .title(track.getTitle())
                        .duration(track.getDuration())
                        .genre(track.getGenre())
                        .releaseDate(track.getReleaseDate())
                        .build())
                .collect(Collectors.toList()));
        response.setCoverImageId(album.getCoverImageId());
        response.setUserIds(album.getUsers().stream()
                .map(User::getId)
                .collect(Collectors.toList()));
        return response;
    }



    @Transactional
    public Album getAlbumById(Long id) {
        String cacheKey = "album_" + id;

        if (cacheService.containsKey(cacheKey)) {
            return (Album) cacheService.get(cacheKey);
        }

        Album album = albumRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Album not found"));

        cacheService.put(cacheKey, album);
        return album;
    }


    @Transactional
    public AlbumResponse addAlbum(CreateAlbumRequest request,
                                  MultipartFile coverFile) {
        if (request == null || request.getName() == null ||
                request.getName().isEmpty() || request.getUserId() == null) {
            throw new IllegalArgumentException("Invalid album creation request");
        }

        User owner = userRepository.findById(request.getUserId())
                .orElseThrow(() -> new ResourceNotFoundException(
                        "User not found with ID: " + request.getUserId()));

        Album album = new Album();
        album.setTitle(request.getName());
        album.getUsers().add(owner);

        if (request.getCollaborators() != null && !request.getCollaborators().isEmpty()) {
            List<User> collaborators = userRepository.findByUsernameIn(
                    request.getCollaborators());

            if (collaborators.size() != request.getCollaborators().size()) {
                List<String> foundUsernames = collaborators.stream()
                        .map(User::getUsername)
                        .collect(Collectors.toList());

                List<String> notFound = request.getCollaborators().stream()
                        .filter(username -> !foundUsernames.contains(username))
                        .collect(Collectors.toList());

                throw new ResourceNotFoundException("Users not found: " +
                        String.join(", ", notFound));
            }

            album.getUsers().addAll(collaborators);
        }
        if (coverFile != null && !coverFile.isEmpty()) {
            String coverImageId = mediaService.uploadMedia(coverFile);
            album.setCoverImageId(coverImageId);
        }

        Album savedAlbum = albumRepository.save(album);
        cacheService.evictByPattern("albums_*");
        return mapToAlbumResponse(savedAlbum);
    }

    public void evictAllAlbumCaches() {
        cacheService.evictByPattern("albums_*");
        cacheService.evictByPattern("album_*");
    }

    @Transactional
    public AlbumResponse updateAlbum(Long albumId,
                                     UpdateAlbumRequest request,
                                     MultipartFile coverFile) {
        Album album = albumRepository.findById(albumId)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Album not found"));

        if (request.getName() != null) {
            album.setTitle(request.getName());
        }

        if (request.getUserId() != null) {
            User user = userRepository.findById(request.getUserId())
                    .orElseThrow(() -> new ResourceNotFoundException(
                            "User not found"));
            if (!album.getUsers().contains(user)) {
                album.getUsers().add(user);
            }
        }
        if (coverFile != null && !coverFile.isEmpty()) {
            String newCoverImageId = mediaService.uploadMedia(coverFile);
            album.setCoverImageId(newCoverImageId);
        }
        Album savedAlbum = albumRepository.save(album);
        clearCacheForAlbum(albumId);
        evictAllAlbumCaches();
        return mapToAlbumResponse(savedAlbum);
    }


    @Transactional
    public void deleteAlbum(Long albumId) {
        Album album = getAlbumById(albumId);

        for (User user : album.getUsers()) {
            album.getUsers().remove(user);
        }

        if (album.getCoverImageId() != null) {
            mediaService.deleteFile(album.getCoverImageId());
        }
        albumRepository.deleteById(albumId);
        clearCacheForAlbum(albumId);
    }

    public void clearCacheForAlbum(Long albumId) {
        cacheService.evict("album_" + albumId);
        cacheService.evictByPattern("album_*");
    }
}