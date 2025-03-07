package music.service.controller;

import java.util.List;
import java.util.stream.Collectors;
import javax.validation.Valid;
import music.service.dto.CreateUserRequest;
import music.service.dto.UpdateUserRequest;
import music.service.dto.UserResponse;
import music.service.model.User;
import music.service.service.UserService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/users")
public class UserController {

    private final UserService userService;

    public UserController(UserService userService) {
        this.userService = userService;
    }

    @GetMapping
    public ResponseEntity<List<UserResponse>> getAllUsers() {
        List<User> users = userService.getAllUsers();
        List<UserResponse> responses = users.stream()
                .map(userService::mapToUserResponse)
                .collect(Collectors.toList());
        return ResponseEntity.ok(responses);
    }

    @GetMapping("/{id}")
    public ResponseEntity<UserResponse> getUserById(@PathVariable Long id) {
        User user = userService.getUserById(id);
        return ResponseEntity.ok(userService.mapToUserResponse(user));
    }

    @PostMapping
    public ResponseEntity<UserResponse> createUser(@Valid @RequestBody CreateUserRequest request) {
        User savedUser = userService.createUser(request);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(userService.mapToUserResponse(savedUser));
    }

    @PutMapping("/{id}")
    public ResponseEntity<UserResponse> updateUserEmail(
            @PathVariable Long id,
            @Valid @RequestBody UpdateUserRequest request) {
        User updatedUser = userService.updateUserEmail(id, request.getEmail());
        return ResponseEntity.ok(userService.mapToUserResponse(updatedUser));
    }
}
