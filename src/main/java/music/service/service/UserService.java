package music.service.service;

import java.util.List;
import java.util.stream.Collectors;
import javax.transaction.Transactional;
import music.service.dto.CreateUserRequest;
import music.service.dto.UpdateUserRequest;
import music.service.dto.UserResponse;
import music.service.model.Album;
import music.service.model.Track;
import music.service.model.User;
import music.service.repositories.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class UserService {
    private static final String USER_NOT_FOUND = "User not found";
    private final UserRepository userRepository;

    @Autowired
    public UserService(UserRepository userRepository) {
        this.userRepository = userRepository;
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

    @Transactional
    public void deleteUser(Long userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException(USER_NOT_FOUND));
        userRepository.delete(user);
    }

    public UserResponse mapToUserResponse(User user) {
        UserResponse response = new UserResponse();
        response.setId(user.getId());
        response.setUsername(user.getUsername());
        response.setEmail(user.getEmail());
        response.setRole(user.getRole());
        response.setAlbums(user.getAlbums().stream()
                .map(Album::getTitle)
                .collect(Collectors.toList()));
        response.setTracks(user.getTracks().stream()
                .map(Track::getTitle)
                .collect(Collectors.toList()));
        return response;
    }

    public UserResponse findByUsernameOrEmail(String query) {
        User user = userRepository.findByUsernameOrEmail(query, query)
                .orElseThrow(() -> new RuntimeException(USER_NOT_FOUND));
        return mapToUserResponse(user);
    }

}
