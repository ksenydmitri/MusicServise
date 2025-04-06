package music.service.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
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
@Tag(name = "User Management", description = "API для управления пользователями")
public class UserController {

    private final UserService userService;

    public UserController(UserService userService) {
        this.userService = userService;
    }

    @GetMapping
    @Operation(
            summary = "Получить всех пользователей",
            responses = @ApiResponse(responseCode = "200", description = "Список пользователей")
    )
    public ResponseEntity<List<UserResponse>> getAllUsers() {
        List<User> users = userService.getAllUsers();
        List<UserResponse> responses = users.stream()
                .map(userService::mapToUserResponse)
                .collect(Collectors.toList());
        return ResponseEntity.ok(responses);
    }

    @GetMapping("/{id}")
    @Operation(
            summary = "Получить пользователя по ID",
            responses = {
                @ApiResponse(responseCode = "200", description = "Пользователь найден"),
                @ApiResponse(responseCode = "404", description = "Пользователь не найден")
            }
    )
    public ResponseEntity<UserResponse> getUserById(
            @Parameter(description = "ID пользователя", required = true) @PathVariable Long id) {
        User user = userService.getUserById(id);
        return ResponseEntity.ok(userService.mapToUserResponse(user));
    }

    @PostMapping
    @Operation(
            summary = "Создать нового пользователя",
            responses = {
                @ApiResponse(responseCode = "201", description = "Пользователь создан"),
                @ApiResponse(responseCode = "400", description = "Некорректные данные")
            }
    )
    public ResponseEntity<UserResponse> createUser(
            @io.swagger.v3.oas.annotations.parameters.RequestBody(
                    description = "Данные пользователя",
                    required = true,
                    content = @Content(schema = @Schema(implementation = CreateUserRequest.class)))
            @Valid @RequestBody CreateUserRequest request) {
        User savedUser = userService.createUser(request);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(userService.mapToUserResponse(savedUser));
    }

    @GetMapping("/search")
    @Operation(
            summary = "Поиск пользователя",
            description = "Поиск по имени пользователя или email",
            responses = {
                @ApiResponse(responseCode = "200", description = "Пользователь найден"),
                @ApiResponse(responseCode = "404", description = "Пользователь не найден")
            }
    )
    public ResponseEntity<UserResponse> findByUsernameOrEmail(
            @Parameter(description = "Поисковый запрос",
                    required = true) @RequestParam String query) {
        UserResponse user = userService.findByUsernameOrEmail(query);
        return ResponseEntity.ok(user);
    }

    @PatchMapping("/{id}")
    @Operation(
            summary = "Обновить пользователя",
            responses = {
                @ApiResponse(responseCode = "200", description = "Пользователь обновлен"),
                @ApiResponse(responseCode = "404", description = "Пользователь не найден")
            }
    )
    public ResponseEntity<UserResponse> patchUser(
            @Parameter(description = "ID пользователя", required = true) @PathVariable Long id,
            @RequestBody UpdateUserRequest request) {
        UserResponse updatedUser = userService.updateUser(id, request);
        return ResponseEntity.ok(updatedUser);
    }

    @DeleteMapping("/{id}")
    @Operation(
            summary = "Удалить пользователя",
            responses = {
                @ApiResponse(responseCode = "204", description = "Пользователь удален"),
                @ApiResponse(responseCode = "404", description = "Пользователь не найден")
            }
    )
    public ResponseEntity<Void> deleteUser(
            @Parameter(description = "ID пользователя", required = true) @PathVariable Long id) {
        userService.removeUser(id);
        return ResponseEntity.noContent().build();
    }
}