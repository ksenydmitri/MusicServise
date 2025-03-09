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
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class AlbumService {
    private final AlbumRepository albumRepository;
    private final UserRepository userRepository;

    @Autowired
    public AlbumService(AlbumRepository albumRepository, UserRepository userRepository) {
        this.albumRepository = albumRepository;
        this.userRepository = userRepository;
    }

    public List<Album> getAllAlbums() {
        return albumRepository.findAll();
    }

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

    public Album getAlbumById(Long id) {
        return albumRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Album not found"));
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

        return mapToAlbumResponse(savedAlbum);
    }

    @Transactional
    public AlbumResponse updateAlbum (Long albumId, UpdateAlbumRequest request) {
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
        return mapToAlbumResponse(savedAlbum);
    }

    @Transactional
    public void deleteAlbum(Long albumId) {

        Album album = getAlbumById(albumId);

        for (User user : album.getUsers()) {
            album.getUsers().remove(user);
        }
        albumRepository.deleteById(albumId);
    }

}
