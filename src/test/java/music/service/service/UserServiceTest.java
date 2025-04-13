package music.service.service;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

import java.util.*;

import music.service.dto.CreateUserRequest;
import music.service.dto.UpdateUserRequest;
import music.service.dto.UserResponse;
import music.service.model.*;
import music.service.repositories.AlbumRepository;
import music.service.repositories.TrackRepository;
import music.service.repositories.UserRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class UserServiceTest {

    @Mock private UserRepository userRepository;
    @Mock private AlbumRepository albumRepository;
    @Mock private TrackRepository trackRepository;

    @InjectMocks private UserService userService;

    @Test
    void createUser_ShouldSaveUser() {
        // Arrange
        CreateUserRequest request = new CreateUserRequest();
        request.setUsername("test_user");
        request.setPassword("password");
        request.setEmail("test@example.com");
        request.setRole("USER");

        User user = new User();
        user.setUsername(request.getUsername());
        user.setPassword(request.getPassword());
        user.setEmail(request.getEmail());
        user.setRole(request.getRole());

        when(userRepository.save(any())).thenAnswer(invocation -> invocation.getArgument(0));

        // Act
        User createdUser = userService.createUser(request);

        // Assert
        assertNotNull(createdUser);
        assertEquals("test_user", createdUser.getUsername());
        assertEquals("password", createdUser.getPassword());
        assertEquals("test@example.com", createdUser.getEmail());
        assertEquals("USER", createdUser.getRole());
        verify(userRepository).save(any());
    }

    @Test
    void getUserById_ShouldReturnUser_WhenExists() {
        // Arrange
        Long userId = 1L;
        User user = new User();
        user.setId(userId);
        user.setUsername("test_user");

        when(userRepository.findById(userId)).thenReturn(Optional.of(user));

        // Act
        User foundUser = userService.getUserById(userId);

        // Assert
        assertNotNull(foundUser);
        assertEquals(userId, foundUser.getId());
        assertEquals("test_user", foundUser.getUsername());
    }

    @Test
    void getUserById_ShouldThrowException_WhenNotFound() {
        // Arrange
        Long userId = 1L;
        when(userRepository.findById(userId)).thenReturn(Optional.empty());

        // Act & Assert
        RuntimeException exception = assertThrows(
                RuntimeException.class,
                () -> userService.getUserById(userId)
        );
        assertEquals("User not found", exception.getMessage());
    }

    @Test
    void getAllUsers_ShouldReturnAllUsers() {
        // Arrange
        User user1 = new User();
        user1.setUsername("user1");
        User user2 = new User();
        user2.setUsername("user2");

        when(userRepository.findAll()).thenReturn(List.of(user1, user2));

        // Act
        List<User> users = userService.getAllUsers();

        // Assert
        assertEquals(2, users.size());
        assertEquals("user1", users.get(0).getUsername());
        assertEquals("user2", users.get(1).getUsername());
    }

    @Test
    void updateUser_ShouldUpdateUserDetails() {
        // Arrange
        Long userId = 1L;
        User user = new User();
        user.setId(userId);
        user.setUsername("old_user");

        UpdateUserRequest request = new UpdateUserRequest();
        request.setUsername("updated_user");
        request.setEmail("updated@example.com");
        request.setPassword("new_password");
        request.setRole("ADMIN");

        when(userRepository.findById(userId)).thenReturn(Optional.of(user));
        when(userRepository.save(any())).thenAnswer(invocation -> invocation.getArgument(0));

        // Act
        UserResponse response = userService.updateUser(userId, request);

        // Assert
        assertEquals("updated_user", response.getUsername());
        assertEquals("updated@example.com", response.getEmail());
        assertEquals("new_password", user.getPassword());
        assertEquals("ADMIN", response.getRole());
        verify(userRepository).save(user);
    }

    @Test
    void findByUsernameOrEmail_ShouldReturnUser_WhenExists() {
        // Arrange
        String query = "test_user";
        User user = new User();
        user.setUsername(query);
        user.setEmail("test@example.com");

        when(userRepository.findByUsernameOrEmail(query, query)).thenReturn(Optional.of(user));

        // Act
        UserResponse response = userService.findByUsernameOrEmail(query);

        // Assert
        assertNotNull(response);
        assertEquals(query, response.getUsername());
        assertEquals("test@example.com", response.getEmail());
    }

    @Test
    void findByUsernameOrEmail_ShouldThrowException_WhenNotFound() {
        // Arrange
        String query = "non_existing_user";
        when(userRepository.findByUsernameOrEmail(query, query)).thenReturn(Optional.empty());

        // Act & Assert
        RuntimeException exception = assertThrows(
                RuntimeException.class,
                () -> userService.findByUsernameOrEmail(query)
        );
        assertEquals("User not found", exception.getMessage());
    }

    @Test
    void removeUser_ShouldDeleteUserAndAssociatedEntities() {
        // Arrange
        Long userId = 1L;

        User user = new User();
        user.setId(userId);

        Album album = new Album();
        album.setUsers(new HashSet<>(Set.of(user))); // Используем HashSet

        Track track = new Track();
        track.setUsers(new HashSet<>(Set.of(user))); // Используем HashSet

        user.setAlbums(new HashSet<>(Set.of(album))); // Используем HashSet
        user.setTracks(new HashSet<>(Set.of(track))); // Используем HashSet

        when(userRepository.findById(userId)).thenReturn(Optional.of(user));

        // Act
        userService.removeUser(userId);

        // Assert
        verify(albumRepository).delete(album);
        verify(trackRepository).delete(track);
        verify(userRepository).delete(user);
    }



}
