package music.service.service;

import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;
import javax.transaction.Transactional;

import music.service.dto.*;
import music.service.model.Album;
import music.service.model.Track;
import music.service.model.User;
import music.service.repositories.*;
import music.service.utils.JwtUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

@Service
public class UserService {

    private static final String USER_NOT_FOUND = "User not found";

    private final UserRepository userRepository;
    private final AlbumRepository albumRepository;
    private final TrackRepository trackRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtUtil jwtUtil;
    private final AlbumService albumService;
    private final TrackService trackService;

    @Autowired
    public UserService(UserRepository userRepository,
                       AlbumRepository albumRepository,
                       TrackRepository trackRepository,
                       PasswordEncoder passwordEncoder,
                       JwtUtil jwtUtil, AlbumService albumService,
                       TrackService trackService) {
        this.userRepository = userRepository;
        this.albumRepository = albumRepository;
        this.trackRepository = trackRepository;
        this.passwordEncoder = passwordEncoder;
        this.jwtUtil = jwtUtil;
        this.albumService = albumService;
        this.trackService = trackService;
    }

    public User createAndEncodeUser(CreateUserRequest request) {
        User user = new User();
        user.setUsername(request.getUsername());
        user.setPassword(passwordEncoder.encode(request.getPassword()));
        user.setEmail(request.getEmail());
        user.setRole(request.getRole() == null ? "USER" : request.getRole());
        return userRepository.save(user);
    }

    public boolean usernameExists(String username) {
        return userRepository.existsByUsername(username);
    }

    public boolean emailExists(String email) {
        return userRepository.existsByEmail(email);
    }

    public String generateToken(String username) {
        return jwtUtil.generateToken(username);
    }

    public String extractUsernameFromToken(String token) {
        try {
            String cleanToken = token.replace("Bearer ", "");
            return jwtUtil.extractUsername(cleanToken);
        } catch (Exception e) {
            throw new RuntimeException("Failed to extract username from token", e);
        }
    }

    public boolean checkPassword(String rawPassword, String encodedPassword) {
        return passwordEncoder.matches(rawPassword, encodedPassword);
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
            user.setPassword(passwordEncoder.encode(request.getPassword()));
        }
        if (request.getRole() != null) {
            user.setRole(request.getRole());
        }

        List<Album> albums = albumRepository.findAllByUserId(userId);
        for (Album album : albums) {
            Set<User> artists = album.getUsers();
            for (User artist : artists) {
                if (artist.getId().equals(userId)) {
                    artist.setUsername(user.getUsername());
                    artist.setEmail(user.getEmail());
                }
            }
            albumRepository.save(album);
            albumService.evictAllAlbumCaches();
        }

        User updatedUser = userRepository.save(user);
        return mapToUserResponse(updatedUser);
    }


    public Optional<User> findByUsernameOrEmailForAuth(String query) {
        return userRepository.findByUsername(query)
                .or(() -> userRepository.findByEmail(query));
    }

    public UserResponse mapToUserResponse(User user) {
        UserResponse response = new UserResponse();
        response.setId(user.getId());
        response.setUsername(user.getUsername());
        response.setEmail(user.getEmail());
        response.setRole(user.getRole());
        response.setAlbumIds(user.getAlbums().stream()
                .map(Album::getId)
                .collect(Collectors.toList()));
        return response;
    }

    public UserResponse getUserDetailsByUsername(String username) {
        User user = userRepository.findByUsername(username)
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
                trackRepository.delete(track);
            }
        }
        trackService.evictAllTrackCaches();
        albumService.evictAllAlbumCaches();
        userRepository.delete(user);
    }
}
