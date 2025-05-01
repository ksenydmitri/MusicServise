package music.service.service;

import music.service.dto.*;
import music.service.exception.ResourceNotFoundException;
import music.service.model.Album;
import music.service.model.Track;
import music.service.model.User;
import music.service.repositories.*;
import music.service.utils.JwtUtil;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.*;
import java.util.stream.Collectors;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class UserServiceTest {

    @Mock
    private UserRepository userRepository;

    @Mock
    private AlbumRepository albumRepository;

    @Mock
    private TrackRepository trackRepository;

    @Mock
    private PasswordEncoder passwordEncoder;

    @Mock
    private JwtUtil jwtUtil;

    @Mock
    private AlbumService albumService;

    @Mock
    private TrackService trackService;

    @InjectMocks
    private UserService userService;

    private User testUser;
    private CreateUserRequest createRequest;
    private UpdateUserRequest updateRequest;
    private Album testAlbum;
    private Track testTrack;

    @BeforeEach
    void setUp() {
        testUser = new User();
        testUser.setId(1L);
        testUser.setUsername("testUser");
        testUser.setEmail("test@example.com");
        testUser.setPassword("encodedPassword");
        testUser.setRole("USER");

        testAlbum = new Album();
        testAlbum.setId(1L);
        testAlbum.setTitle("Test Album");
        testAlbum.getUsers().add(testUser);

        testTrack = new Track();
        testTrack.setId(1L);
        testTrack.setTitle("Test Track");
        testTrack.getUsers().add(testUser);

        testUser.getAlbums().add(testAlbum);
        testUser.getTracks().add(testTrack);

        createRequest = new CreateUserRequest();
        createRequest.setUsername("newUser");
        createRequest.setPassword("password");
        createRequest.setEmail("new@example.com");

        updateRequest = new UpdateUserRequest();
        updateRequest.setUsername("updatedUser");
        updateRequest.setPassword("newPassword");
        updateRequest.setEmail("updated@example.com");
        updateRequest.setRole("ADMIN");
    }

    @Test
    void createAndEncodeUser_ShouldCreateNewUser() {
        // Arrange
        when(passwordEncoder.encode("password")).thenReturn("encodedPassword");
        when(userRepository.save(any(User.class))).thenReturn(testUser);

        // Act
        User result = userService.createAndEncodeUser(createRequest);

        // Assert
        assertEquals("testUser", result.getUsername());
        assertEquals("encodedPassword", result.getPassword());
        verify(passwordEncoder, times(1)).encode("password");
        verify(userRepository, times(1)).save(any(User.class));
    }

    @Test
    void usernameExists_ShouldReturnTrue_WhenUsernameExists() {
        // Arrange
        when(userRepository.existsByUsername("testUser")).thenReturn(true);

        // Act & Assert
        assertTrue(userService.usernameExists("testUser"));
    }

    @Test
    void emailExists_ShouldReturnTrue_WhenEmailExists() {
        // Arrange
        when(userRepository.existsByEmail("test@example.com")).thenReturn(true);

        // Act & Assert
        assertTrue(userService.emailExists("test@example.com"));
    }

    @Test
    void generateToken_ShouldReturnToken() {
        // Arrange
        when(jwtUtil.generateToken("testUser")).thenReturn("testToken");

        // Act
        String token = userService.generateToken("testUser");

        // Assert
        assertEquals("testToken", token);
    }

    @Test
    void extractUsernameFromToken_ShouldReturnUsername() {
        // Arrange
        when(jwtUtil.extractUsername("cleanToken")).thenReturn("testUser");

        // Act
        String username = userService.extractUsernameFromToken("Bearer cleanToken");

        // Assert
        assertEquals("testUser", username);
    }

    @Test
    void extractUsernameFromToken_ShouldThrow_WhenInvalidToken() {
        // Arrange
        when(jwtUtil.extractUsername(anyString())).thenThrow(new RuntimeException("Invalid token"));

        // Act & Assert
        assertThrows(RuntimeException.class, () -> userService.extractUsernameFromToken("Bearer invalid"));
    }

    @Test
    void checkPassword_ShouldReturnTrue_WhenMatches() {
        // Arrange
        when(passwordEncoder.matches("raw", "encoded")).thenReturn(true);

        // Act & Assert
        assertTrue(userService.checkPassword("raw", "encoded"));
    }

    @Test
    void getUserById_ShouldReturnUser() {
        // Arrange
        when(userRepository.findById(1L)).thenReturn(Optional.of(testUser));

        // Act
        User result = userService.getUserById(1L);

        // Assert
        assertEquals("testUser", result.getUsername());
    }

    @Test
    void getUserById_ShouldThrow_WhenNotFound() {
        // Arrange
        when(userRepository.findById(1L)).thenReturn(Optional.empty());

        // Act & Assert
        assertThrows(RuntimeException.class, () -> userService.getUserById(1L));
    }

    @Test
    void getAllUsers_ShouldReturnAllUsers() {
        // Arrange
        when(userRepository.findAll()).thenReturn(List.of(testUser));

        // Act
        List<User> result = userService.getAllUsers();

        // Assert
        assertEquals(1, result.size());
        assertEquals("testUser", result.get(0).getUsername());
    }

    @Test
    void updateUser_ShouldUpdateAllFields() {
        // Arrange
        when(userRepository.findById(1L)).thenReturn(Optional.of(testUser));
        when(passwordEncoder.encode("newPassword")).thenReturn("newEncodedPassword");
        when(userRepository.save(any(User.class))).thenReturn(testUser);
        when(albumRepository.findAllByUserId(1L)).thenReturn(List.of(testAlbum));

        // Act
        UserResponse result = userService.updateUser(1L, updateRequest);

        // Assert
        assertEquals("testUser", result.getUsername()); // username changed in mock setup
        verify(passwordEncoder, times(1)).encode("newPassword");
        verify(albumService, atLeastOnce()).evictAllAlbumCaches();
    }

    @Test
    void updateUser_ShouldUpdateOnlyProvidedFields() {
        // Arrange
        UpdateUserRequest partialRequest = new UpdateUserRequest();
        partialRequest.setEmail("partial@example.com");

        when(userRepository.findById(1L)).thenReturn(Optional.of(testUser));
        when(userRepository.save(any(User.class))).thenReturn(testUser);
        when(albumRepository.findAllByUserId(1L)).thenReturn(List.of(testAlbum));

        // Act
        UserResponse result = userService.updateUser(1L, partialRequest);

        // Assert
        assertEquals("partial@example.com", testUser.getEmail());
        assertEquals("USER", testUser.getRole()); // role not changed
        verify(passwordEncoder, never()).encode(anyString());
    }

    @Test
    void findByUsernameOrEmailForAuth_ShouldFindByUsername() {
        // Arrange
        when(userRepository.findByUsername("testUser")).thenReturn(Optional.of(testUser));

        // Act
        Optional<User> result = userService.findByUsernameOrEmailForAuth("testUser");

        // Assert
        assertTrue(result.isPresent());
        assertEquals("testUser", result.get().getUsername());
        verify(userRepository, never()).findByEmail(anyString());
    }

    @Test
    void findByUsernameOrEmailForAuth_ShouldFindByEmail() {
        // Arrange
        when(userRepository.findByUsername("test@example.com")).thenReturn(Optional.empty());
        when(userRepository.findByEmail("test@example.com")).thenReturn(Optional.of(testUser));

        // Act
        Optional<User> result = userService.findByUsernameOrEmailForAuth("test@example.com");

        // Assert
        assertTrue(result.isPresent());
        assertEquals("testUser", result.get().getUsername());
    }

    @Test
    void mapToUserResponse_ShouldConvertCorrectly() {
        // Act
        UserResponse response = userService.mapToUserResponse(testUser);

        // Assert
        assertEquals(1L, response.getId());
        assertEquals("testUser", response.getUsername());
        assertEquals(1, response.getAlbumIds().size());
        assertEquals(1L, response.getAlbumIds().get(0));
    }

    @Test
    void getUserDetailsByUsername_ShouldReturnUserResponse() {
        // Arrange
        when(userRepository.findByUsername("testUser")).thenReturn(Optional.of(testUser));

        // Act
        UserResponse result = userService.getUserDetailsByUsername("testUser");

        // Assert
        assertEquals("testUser", result.getUsername());
        assertEquals("test@example.com", result.getEmail());
    }

    @Test
    void getUserDetailsByUsername_ShouldThrow_WhenNotFound() {
        // Arrange
        when(userRepository.findByUsername("unknown")).thenReturn(Optional.empty());

        // Act & Assert
        assertThrows(RuntimeException.class, () -> userService.getUserDetailsByUsername("unknown"));
    }

    @Test
    void removeUser_ShouldDeleteUserAndCleanup() {
        // Arrange
        when(userRepository.findById(1L)).thenReturn(Optional.of(testUser));

        // Act
        userService.removeUser(1L);

        // Assert
        verify(userRepository, times(1)).delete(testUser);
        verify(albumService, times(1)).evictAllAlbumCaches();
        verify(trackService, times(1)).evictAllTrackCaches();
    }

    @Test
    void removeUser_ShouldDeleteOrphanedAlbums() {
        // Arrange
        Album orphanedAlbum = new Album();
        orphanedAlbum.setId(2L);
        orphanedAlbum.getUsers().add(testUser);
        testUser.getAlbums().add(orphanedAlbum);

        when(userRepository.findById(1L)).thenReturn(Optional.of(testUser));

        // Act
        userService.removeUser(1L);

        // Assert
        verify(albumRepository, times(1)).delete(orphanedAlbum);
    }

    @Test
    void removeUser_ShouldDeleteOrphanedTracks() {
        // Arrange
        Track orphanedTrack = new Track();
        orphanedTrack.setId(2L);
        orphanedTrack.getUsers().add(testUser);
        testUser.getTracks().add(orphanedTrack);

        when(userRepository.findById(1L)).thenReturn(Optional.of(testUser));

        // Act
        userService.removeUser(1L);

        // Assert
        verify(trackRepository, times(1)).delete(orphanedTrack);
    }
}