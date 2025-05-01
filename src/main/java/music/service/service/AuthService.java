package music.service.service;

import music.service.dto.*;
import music.service.model.User;
import org.springframework.stereotype.Service;

@Service
public class AuthService {

    private final UserService userService;

    public AuthService(UserService userService) {
        this.userService = userService;
    }

    public AuthResponse register(CreateUserRequest request) {
        if (userService.usernameExists(request.getUsername())) {
            throw new RuntimeException("Username is already taken");
        }

        if (userService.emailExists(request.getEmail())) {
            throw new RuntimeException("Email is already registered");
        }

        User user = userService.createAndEncodeUser(request);

        String token = userService.generateToken(user.getUsername());

        AuthResponse response = new AuthResponse();
        response.setToken(token);
        response.setUsername(user.getUsername());
        response.setUser(userService.mapToUserResponse(user));
        return response;
    }

    public AuthResponse login(AuthRequest request) {
        User user = userService.findByUsernameOrEmailForAuth(request.getUsername())
                .orElseThrow(() -> new RuntimeException("User not found"));

        if (!userService.checkPassword(request.getPassword(), user.getPassword())) {
            throw new RuntimeException("Invalid password");
        }

        String token = userService.generateToken(user.getUsername());
        AuthResponse authResponse = new AuthResponse();
        authResponse.setToken(token);
        authResponse.setUsername(user.getUsername());
        authResponse.setUser(userService.mapToUserResponse(user));
        return authResponse;
    }

    public String getCurrentUser(String token) {
        return userService.extractUsernameFromToken(token);
    }

    public UserResponse getUserDetailsByUsername(String username) {
        return userService.getUserDetailsByUsername(username);
    }

    public AuthResponse updateUser(String currentUsername, UpdateUserRequest request) {
        User user = userService.findByUsernameOrEmailForAuth(currentUsername)
                .orElseThrow(() -> new RuntimeException("User not found"));
        if (request.getUsername() != null && !request.getUsername().equals(currentUsername)) {
            if (userService.usernameExists(request.getUsername())) {
                throw new RuntimeException("Username is already taken");
            }
        }

        UserResponse updatedUser = userService.updateUser(user.getId(), request);
        String newToken = null;
        if (request.getUsername() != null && !request.getUsername().equals(currentUsername)) {
            newToken = userService.generateToken(request.getUsername());
        }

        AuthResponse response = new AuthResponse();
        response.setUser(updatedUser);
        response.setUsername(user.getUsername());
        response.setToken(newToken);
        return response;
    }
}
