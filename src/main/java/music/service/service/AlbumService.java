package music.service.service;

import java.util.List;
import java.util.stream.Collectors;

import music.service.dto.AlbumResponse;
import music.service.dto.CreateAlbumRequest;
import music.service.dto.CreateTrackRequest;
import music.service.dto.TrackResponse;
import music.service.model.Album;
import music.service.model.Track;
import music.service.model.User;
import music.service.repositories.AlbumRepository;
import music.service.repositories.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import javax.transaction.Transactional;

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

    public Album getAlbumByTitle(String title) {
        return albumRepository.findByTitle(title);
    }

    public Album addAlbumToUser(Long userId, Album album) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("User not found"));
        album.getUsers().add(user);
        Album savedAlbum = albumRepository.save(album);
        user.getAlbums().add(savedAlbum);
        userRepository.save(user);
        return savedAlbum;
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

        Album album = new Album(request.getTitle());
        if (!album.getUsers().contains(user)) {
            album.getUsers().add(user);
        }
        Album savedAlbum = albumRepository.save(album);

        return mapToAlbumResponse(savedAlbum);
    }

}
