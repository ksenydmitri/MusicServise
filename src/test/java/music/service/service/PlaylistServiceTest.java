package music.service.service;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

import java.util.*;

import music.service.dto.CreatePlaylistRequest;
import music.service.dto.PlaylistResponse;
import music.service.dto.UpdatePlaylistRequest;
import music.service.exception.ResourceNotFoundException;
import music.service.model.Playlist;
import music.service.model.Track;
import music.service.model.User;
import music.service.repositories.PlaylistRepository;
import music.service.repositories.TrackRepository;
import music.service.repositories.UserRepository;
import music.service.service.CacheService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;

@ExtendWith(MockitoExtension.class)
class PlaylistServiceTest {

    @Mock
    private PlaylistRepository playlistRepository;

    @Mock
    private TrackRepository trackRepository;

    @Mock
    private UserRepository userRepository;

    @Mock
    private CacheService cacheService;

    @InjectMocks
    private PlaylistService playlistService;

    private Playlist playlist;
    private User user;
    private Track track;

    @BeforeEach
    void setUp() {
        user = new User();
        user.setId(1L);
        user.setUsername("testUser");

        track = new Track();
        track.setId(1L);
        track.setTitle("Test Track");

        playlist = new Playlist();
        playlist.setId(1L);
        playlist.setName("Test Playlist");
        playlist.setUsers(new HashSet<>(Collections.singleton(user)));
        playlist.setTracks(new HashSet<>(Collections.singleton(track)));
    }

    @Test
    void getAllPlaylists_ShouldReturnPageOfPlaylists() {
        // Arrange
        Pageable pageable = PageRequest.of(0, 10, Sort.by("name"));
        Page<Playlist> expectedPage = new PageImpl<>(Collections.singletonList(playlist));

        when(cacheService.containsKey(anyString())).thenReturn(false);
        when(playlistRepository.findAll(pageable)).thenReturn(expectedPage);

        // Act
        Page<Playlist> result = playlistService.getAllPlaylists(null, null, 0, 10, "name");

        // Assert
        assertNotNull(result);
        assertEquals(1, result.getTotalElements());
        verify(cacheService).put(anyString(), eq(expectedPage));
    }

    @Test
    void getAllPlaylists_ShouldReturnFromCache() {
        // Arrange
        Page<Playlist> expectedPage = new PageImpl<>(Collections.singletonList(playlist));
        when(cacheService.containsKey(anyString())).thenReturn(true);
        when(cacheService.get(anyString())).thenReturn(expectedPage);

        // Act
        Page<Playlist> result = playlistService.getAllPlaylists(null, null, 0, 10, "name");

        // Assert
        assertNotNull(result);
        assertEquals(1, result.getTotalElements());
        verify(playlistRepository, never()).findAll(any(Pageable.class));
    }

    @Test
    void getPlaylistById_ShouldReturnPlaylist() {
        // Arrange
        when(playlistRepository.findById(1L)).thenReturn(Optional.of(playlist));

        // Act
        Optional<Playlist> result = playlistService.getPlaylistById(1L);

        // Assert
        assertTrue(result.isPresent());
        assertEquals("Test Playlist", result.get().getName());
    }

    @Test
    void savePlaylist_ShouldSaveAndClearCache() {
        // Arrange
        CreatePlaylistRequest request = new CreatePlaylistRequest();
        request.setName("New Playlist");

        when(playlistRepository.save(any(Playlist.class))).thenReturn(playlist);

        // Act
        Playlist result = playlistService.savePlaylist(request);

        // Assert
        assertNotNull(result);
        verify(cacheService).clear();
        verify(playlistRepository).save(any(Playlist.class));
    }

    @Test
    void deletePlaylist_ShouldDeleteAndClearCache() {
        // Arrange
        when(playlistRepository.findById(1L)).thenReturn(Optional.of(playlist));

        // Act
        playlistService.deletePlaylist(1L);

        // Assert
        verify(playlistRepository).delete(playlist);
        verify(cacheService).evictByPattern("playlist_*");
        verify(cacheService).evictByPattern("playlists_*");
    }


    @Test
    void deletePlaylist_ShouldThrowWhenNotFound() {
        // Arrange
        when(playlistRepository.findById(1L)).thenReturn(Optional.empty());

        // Act & Assert
        assertThrows(ResourceNotFoundException.class, () -> {
            playlistService.deletePlaylist(1L);
        });
    }

    @Test
    void mapToPlaylistResponse_ShouldMapCorrectly() {
        // Act
        PlaylistResponse response = playlistService.mapToPlaylistResponse(playlist);

        // Assert
        assertEquals(1L, response.getId());
        assertEquals("Test Playlist", response.getName());
        assertEquals(1, response.getUsers().size());
        assertEquals("testUser", response.getUsers().get(0));
        assertEquals(1, response.getTracks().size());
        assertEquals("Test Track", response.getTracks().get(0));
    }

    @Test
    void updatePlaylist_ShouldUpdateName() {
        // Arrange
        UpdatePlaylistRequest request = new UpdatePlaylistRequest();
        request.setName("Updated Name");

        when(playlistRepository.findById(1L)).thenReturn(Optional.of(playlist));
        when(playlistRepository.save(any(Playlist.class))).thenReturn(playlist);

        // Act
        PlaylistResponse result = playlistService.updatePlaylist(1L, request);

        // Assert
        assertEquals("Updated Name", result.getName());
        verify(cacheService).evictByPattern("playlist_*");
        verify(cacheService).evictByPattern("playlists_*");
    }

    @Test
    void updatePlaylist_ShouldAddUser() {
        // Arrange
        User newUser = new User();
        newUser.setId(2L);
        newUser.setUsername("newUser");

        UpdatePlaylistRequest request = new UpdatePlaylistRequest();
        request.setUserId(2L);

        when(playlistRepository.findById(1L)).thenReturn(Optional.of(playlist));
        when(userRepository.findById(2L)).thenReturn(Optional.of(newUser));
        when(playlistRepository.save(any(Playlist.class))).thenReturn(playlist);

        // Act
        PlaylistResponse result = playlistService.updatePlaylist(1L, request);

        // Assert
        assertEquals(2, result.getUsers().size());
        verify(cacheService).evictByPattern("playlist_*");
        verify(cacheService).evictByPattern("playlists_*");
    }

    @Test
    void updatePlaylist_ShouldAddTrack() {
        // Arrange
        Track newTrack = new Track();
        newTrack.setId(2L);
        newTrack.setTitle("New Track");

        UpdatePlaylistRequest request = new UpdatePlaylistRequest();
        request.setTrackId(2L);

        when(playlistRepository.findById(1L)).thenReturn(Optional.of(playlist));
        when(trackRepository.findById(2L)).thenReturn(Optional.of(newTrack));
        when(playlistRepository.save(any(Playlist.class))).thenReturn(playlist);

        // Act
        PlaylistResponse result = playlistService.updatePlaylist(1L, request);

        // Assert
        assertEquals(2, result.getTracks().size());
        verify(cacheService).evictByPattern("playlist_*");
        verify(cacheService).evictByPattern("playlists_*");
    }

    @Test
    void updatePlaylist_ShouldThrowWhenPlaylistNotFound() {
        // Arrange
        when(playlistRepository.findById(1L)).thenReturn(Optional.empty());

        // Act & Assert
        assertThrows(ResourceNotFoundException.class, () -> {
            playlistService.updatePlaylist(1L, new UpdatePlaylistRequest());
        });
    }

    @Test
    void updatePlaylist_ShouldThrowWhenUserNotFound() {
        // Arrange
        UpdatePlaylistRequest request = new UpdatePlaylistRequest();
        request.setUserId(99L);

        when(playlistRepository.findById(1L)).thenReturn(Optional.of(playlist));
        when(userRepository.findById(99L)).thenReturn(Optional.empty());

        // Act & Assert
        assertThrows(ResourceNotFoundException.class, () -> {
            playlistService.updatePlaylist(1L, request);
        });
    }

    @Test
    void updatePlaylist_ShouldThrowWhenTrackNotFound() {
        // Arrange
        UpdatePlaylistRequest request = new UpdatePlaylistRequest();
        request.setTrackId(99L);

        when(playlistRepository.findById(1L)).thenReturn(Optional.of(playlist));
        when(trackRepository.findById(99L)).thenReturn(Optional.empty());

        // Act & Assert
        assertThrows(ResourceNotFoundException.class, () -> {
            playlistService.updatePlaylist(1L, request);
        });
    }

    @Test
    void buildPlaylistsCacheKey_ShouldBuildCorrectKey() {
        // Act
        String key = playlistService.buildPlaylistsCacheKey("user1", "playlist1", 0, 10, "name");

        // Assert
        assertEquals("playlists_user1_playlist1_page0_size10_sortname", key);
    }

    @Test
    void buildPlaylistsCacheKey_ShouldHandleNullValues() {
        // Act
        String key = playlistService.buildPlaylistsCacheKey(null, null, 0, 10, "name");

        // Assert
        assertEquals("playlists_all_all_page0_size10_sortname", key);
    }

    @Test
    void updatePlaylistName_ShouldUpdateName_WhenNameIsNotNull() {
        // Arrange
        Playlist playlist = new Playlist();
        playlist.setName("Old Name");
        String newName = "New Name";

        // Act
        playlistService.updatePlaylistName(playlist, newName);

        // Assert
        assertEquals("New Name", playlist.getName());
    }

    @Test
    void updatePlaylistName_ShouldNotUpdateName_WhenNameIsNull() {
        // Arrange
        Playlist playlist = new Playlist();
        playlist.setName("Old Name");

        // Act
        playlistService.updatePlaylistName(playlist, null);

        // Assert
        assertEquals("Old Name", playlist.getName());
    }

    @Test
    void addUserToPlaylist_ShouldNotAddUser_WhenUserIdIsNull() {
        // Arrange
        Playlist playlist = new Playlist();

        // Act
        playlistService.addUserToPlaylist(playlist, null);

        // Assert
        assertTrue(playlist.getUsers().isEmpty());
    }

    @Test
    void addUserToPlaylist_ShouldAddUser_WhenUserIsValid() {
        // Arrange
        Playlist playlist = new Playlist();
        User user = new User();
        user.setId(1L);
        when(userRepository.findById(1L)).thenReturn(Optional.of(user));

        // Act
        playlistService.addUserToPlaylist(playlist, 1L);

        // Assert
        assertTrue(playlist.getUsers().contains(user));
    }

    @Test
    void addUserToPlaylist_ShouldNotDuplicateUser_WhenUserAlreadyExists() {
        // Arrange
        Playlist playlist = new Playlist();
        User user = new User();
        user.setId(1L);
        playlist.getUsers().add(user);
        when(userRepository.findById(1L)).thenReturn(Optional.of(user));

        // Act
        playlistService.addUserToPlaylist(playlist, 1L);

        // Assert
        assertEquals(1, playlist.getUsers().size());
    }

    @Test
    void addUserToPlaylist_ShouldThrowException_WhenUserNotFound() {
        // Arrange
        Playlist playlist = new Playlist();
        when(userRepository.findById(99L)).thenReturn(Optional.empty());

        // Act & Assert
        ResourceNotFoundException exception = assertThrows(ResourceNotFoundException.class,
                () -> playlistService.addUserToPlaylist(playlist, 99L));
        assertEquals("User not found", exception.getMessage());
    }


    @Test
    void addTrackToPlaylist_ShouldNotAddTrack_WhenTrackIdIsNull() {
        // Arrange
        Playlist playlist = new Playlist();

        // Act
        playlistService.addTrackToPlaylist(playlist, null);

        // Assert
        assertTrue(playlist.getTracks().isEmpty());
    }

    @Test
    void addTrackToPlaylist_ShouldAddTrack_WhenTrackIsValid() {
        // Arrange
        Playlist playlist = new Playlist();
        Track track = new Track();
        track.setId(1L);
        when(trackRepository.findById(1L)).thenReturn(Optional.of(track));

        // Act
        playlistService.addTrackToPlaylist(playlist, 1L);

        // Assert
        assertTrue(playlist.getTracks().contains(track));
    }

    @Test
    void addTrackToPlaylist_ShouldNotDuplicateTrack_WhenTrackAlreadyExists() {
        // Arrange
        Playlist playlist = new Playlist();
        Track track = new Track();
        track.setId(1L);
        playlist.getTracks().add(track);
        when(trackRepository.findById(1L)).thenReturn(Optional.of(track));

        // Act
        playlistService.addTrackToPlaylist(playlist, 1L);

        // Assert
        assertEquals(1, playlist.getTracks().size());
    }

    @Test
    void addTrackToPlaylist_ShouldThrowException_WhenTrackNotFound() {
        // Arrange
        Playlist playlist = new Playlist();
        when(trackRepository.findById(99L)).thenReturn(Optional.empty());

        // Act & Assert
        ResourceNotFoundException exception = assertThrows(ResourceNotFoundException.class,
                () -> playlistService.addTrackToPlaylist(playlist, 99L));
        assertEquals("Track not found", exception.getMessage());
    }

    @Test
    void fetchPlaylistsFromDB_ShouldCallNativeQuery_WhenUserAndNameAreProvided() {
        // Arrange
        Pageable pageable = PageRequest.of(0, 10);
        Page<Playlist> expectedPage = new PageImpl<>(Collections.singletonList(new Playlist()));
        when(playlistRepository.findByUserUsernameAndNameNative("testUser", "testName", pageable))
                .thenReturn(expectedPage);

        // Act
        Page<Playlist> result = playlistService.fetchPlaylistsFromDB("testUser", "testName", pageable);

        // Assert
        assertNotNull(result);
        assertEquals(1, result.getTotalElements());
        verify(playlistRepository).findByUserUsernameAndNameNative("testUser", "testName", pageable);
    }

    @Test
    void fetchPlaylistsFromDB_ShouldCallFindByUserUsername_WhenOnlyUserIsProvided() {
        // Arrange
        Pageable pageable = PageRequest.of(0, 10);
        Page<Playlist> expectedPage = new PageImpl<>(Collections.singletonList(new Playlist()));
        when(playlistRepository.findByUserUsername("testUser", pageable)).thenReturn(expectedPage);

        // Act
        Page<Playlist> result = playlistService.fetchPlaylistsFromDB("testUser", null, pageable);

        // Assert
        assertNotNull(result);
        assertEquals(1, result.getTotalElements());
        verify(playlistRepository).findByUserUsername("testUser", pageable);
    }

    @Test
    void fetchPlaylistsFromDB_ShouldCallFindAllByName_WhenOnlyNameIsProvided() {
        // Arrange
        Pageable pageable = PageRequest.of(0, 10);
        Page<Playlist> expectedPage = new PageImpl<>(Collections.singletonList(new Playlist()));
        when(playlistRepository.findAllByName("testName", pageable)).thenReturn(expectedPage);

        // Act
        Page<Playlist> result = playlistService.fetchPlaylistsFromDB(null, "testName", pageable);

        // Assert
        assertNotNull(result);
        assertEquals(1, result.getTotalElements());
        verify(playlistRepository).findAllByName("testName", pageable);
    }

    @Test
    void fetchPlaylistsFromDB_ShouldCallFindAll_WhenUserAndNameAreNull() {
        // Arrange
        Pageable pageable = PageRequest.of(0, 10);
        Page<Playlist> expectedPage = new PageImpl<>(Collections.singletonList(new Playlist()));
        when(playlistRepository.findAll(pageable)).thenReturn(expectedPage);

        // Act
        Page<Playlist> result = playlistService.fetchPlaylistsFromDB(null, null, pageable);

        // Assert
        assertNotNull(result);
        assertEquals(1, result.getTotalElements());
        verify(playlistRepository).findAll(pageable);
    }

}