package music.service.controller;

import music.service.dto.AuthRequest;
import music.service.dto.AuthResponse;
import music.service.dto.CreateUserRequest;
import music.service.model.User;
import music.service.repositories.UserRepository;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/auth")
public class AuthController {
    private final UserRepository userRepository;

    public AuthController(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    @PostMapping("/register")
    public ResponseEntity<?> register(@RequestBody CreateUserRequest request) {
        // Проверка на существующего пользователя
        if (userRepository.existsByUsername(request.getUsername())) {
            return ResponseEntity.badRequest().body("Username is taken");
        }

        User user = new User();
        user.setUsername(request.getUsername());
        user.setPassword(request.getPassword()); // Без хэширования!
        user.setEmail(request.getEmail());
        userRepository.save(user);

        return ResponseEntity.ok("User registered");
    }

    @PostMapping("/login")
    public ResponseEntity<?> login(@RequestBody AuthRequest request) {
        User user = userRepository.findByUsername(request.getUsername())
                .orElseThrow(() -> new RuntimeException("User not found"));

        // Проверка пароля (без хэширования)
        if (!user.getPassword().equals(request.getPassword())) {
            return ResponseEntity.badRequest().body("Invalid password");
        }


        AuthResponse response = new AuthResponse();
        response.setToken(request.getUsername());
        response.setUsername(user.getUsername());

        return ResponseEntity.ok(response);
    }
}
