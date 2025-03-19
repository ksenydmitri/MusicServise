package music.service.service;

import java.util.List;
import java.util.stream.Collectors;
import javax.transaction.Transactional;

import music.service.dto.AlbumResponse;
import music.service.dto.CreateUserRequest;
import music.service.dto.UpdateUserRequest;
import music.service.dto.UserResponse;
import music.service.model.*;
import music.service.repositories.AlbumRepository;
import music.service.repositories.TrackRepository;
import music.service.repositories.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class UserService {
    private static final String USER_NOT_FOUND = "User not found";
    private final UserRepository userRepository;
    private final AlbumRepository albumRepository;
    private final TrackRepository trackRepository;

    @Autowired
    public UserService(UserRepository userRepository, AlbumService albumService,
                       AlbumRepository albumRepository, TrackRepository trackRepository) {
        this.userRepository = userRepository;
        this.albumRepository = albumRepository;
        this.trackRepository  = trackRepository;
    }

    public User createUser(CreateUserRequest request) {
        User user = new User();
        user.setUsername(request.getUsername());
        user.setPassword(request.getPassword());
        user.setEmail(request.getEmail());
        user.setRole(request.getRole());
        return userRepository.save(user);
    }

    public User getUserById(Long id) {
        return userRepository.findById(id)
                .orElseThrow(() -> new RuntimeException(USER_NOT_FOUND));
    }

    public List<User> getAllUsers() {
        return userRepository.findAll();
    }

    @Transactional
    public UserResponse updateUser(Long userId, UpdateUserRequest request) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException(USER_NOT_FOUND));
        if (request.getUsername() != null) {
            user.setUsername(request.getUsername());
        }
        if (request.getEmail() != null) {
            user.setEmail(request.getEmail());
        }
        if (request.getPassword() != null) {
            user.setPassword(request.getPassword());
        }
        if (request.getRole() != null) {
            user.setRole(request.getRole());
        }
        User updatedUser = userRepository.save(user);
        return mapToUserResponse(updatedUser);
    }

    public UserResponse mapToUserResponse(User user) {
        UserResponse response = new UserResponse();
        response.setId(user.getId());
        response.setUsername(user.getUsername());
        response.setEmail(user.getEmail());
        response.setRole(user.getRole());
        List<AlbumResponse> albumResponses = user.getAlbums().stream().map(album -> {
            AlbumResponse albumResponse = new AlbumResponse();
            albumResponse.setId(album.getId());
            albumResponse.setTitle(album.getTitle());
            albumResponse.setTracks(album.getTracks().stream()
                    .map(Track::getTitle)
                    .toList());
            albumResponse.setArtists(album.getUsers().stream()
                    .map(User::getUsername)
                    .toList());

            return albumResponse;
        }).toList();
        response.setAlbums(albumResponses);

        return response;
    }


    public UserResponse findByUsernameOrEmail(String query) {
        User user = userRepository.findByUsernameOrEmail(query, query)
                .orElseThrow(() -> new RuntimeException(USER_NOT_FOUND));
        return mapToUserResponse(user);
    }

    @Transactional
    public void removeUser(Long userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException(USER_NOT_FOUND));

        for (Album album : user.getAlbums()) {
            album.getUsers().remove(user);

            if (album.getUsers().isEmpty()) {
                albumRepository.delete(album);
            }
        }
        for (Track track : user.getTracks()) {
            track.getUsers().remove(user);

            if (track.getUsers().isEmpty()) {
                trackRepository.delete(track); // Удаляем трек, если у него больше нет пользователей
            }
        }
        userRepository.delete(user);
    }

}