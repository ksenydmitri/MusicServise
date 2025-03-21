package music.service.service;

import java.util.List;
import java.util.stream.Collectors;
import javax.transaction.Transactional;
import music.service.dto.*;
import music.service.model.Album;
import music.service.model.Track;
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

@Service
public class AlbumService {
    private static final Logger logger = LoggerFactory.getLogger(AlbumService.class);
    private final AlbumRepository albumRepository;
    private final UserRepository userRepository;
    private final CacheService cacheService;

    @Autowired
    public AlbumService(AlbumRepository albumRepository,
                        UserRepository userRepository, CacheService cacheService) {
        this.albumRepository = albumRepository;
        this.userRepository = userRepository;
        this.cacheService = cacheService;
    }

    @Transactional
    public Page<Album> getAllAlbums(String user, String title, int page, int size, String sortBy) {
        String cacheKey = buildAlbumsCacheKey(user, title, page, size, sortBy);

        if (cacheService.containsKey(cacheKey)) {
            logger.info("Cache hit for key: {}", cacheKey);
            return (Page<Album>) cacheService.get(cacheKey);
        }

        Pageable pageable = PageRequest.of(page, size, Sort.by(sortBy));
        Page<Album> albums = fetchAlbumsFromDatabase(user, title, pageable);
        cacheService.put(cacheKey, albums);
        return albums;
    }

    private Page<Album> fetchAlbumsFromDatabase(String user, String title, Pageable pageable) {
        if (user != null && title != null) {
            return albumRepository.findByUserUsernameAndTitleNative(user, title, pageable);
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
        return String.format("albums_%s_%s_page%d_size%d_sort%s",
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
                .map(Track::getTitle)
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
                .orElseThrow(() -> new RuntimeException("Album not found"));
        cacheService.put(cacheKey, album);
        return album;
    }

    @Transactional
    public AlbumResponse addAlbum(CreateAlbumRequest request) {
        User user = userRepository.findById(request.getUserId())
                .orElseThrow(() -> new RuntimeException("User not found"));

        Album album = new Album(request.getName());
        if (!album.getUsers().contains(user)) {
            album.getUsers().add(user);
        }
        Album savedAlbum = albumRepository.save(album);

        evictAllAlbumCaches();

        return mapToAlbumResponse(savedAlbum);
    }

    private void evictAllAlbumCaches() {
        cacheService.evictByPattern("albums_*");
        cacheService.evictByPattern("album_*");
    }

    @Transactional
    public AlbumResponse updateAlbum(Long albumId, UpdateAlbumRequest request) {
        Album album = albumRepository.findById(albumId)
                .orElseThrow(() -> new RuntimeException("Album not found"));

        if (request.getName() != null) {
            album.setTitle(request.getName());
        }
        if (request.getUserId() != null) {
            User user = userRepository.findById(request.getUserId())
                    .orElseThrow(() -> new RuntimeException("User not found"));
            if (!album.getUsers().contains(user)) {
                album.getUsers().add(user);
            }
        }
        Album savedAlbum = albumRepository.save(album);

        clearCacheForAlbum(albumId);

        return mapToAlbumResponse(savedAlbum);
    }

    @Transactional
    public void deleteAlbum(Long albumId) {
        Album album = getAlbumById(albumId);

        for (User user : album.getUsers()) {
            album.getUsers().remove(user);
        }
        albumRepository.deleteById(albumId);

        clearCacheForAlbum(albumId);
        cacheService.clear();
    }

    public void clearCacheForAlbum(Long albumId) {
        String cacheKey = "album_" + albumId;
        cacheService.evict(cacheKey);
    }
}