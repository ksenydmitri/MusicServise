package music.service.service;

import music.service.dto.*;
import music.service.exception.ResourceNotFoundException;
import music.service.model.*;
import music.service.repositories.*;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.*;

import java.util.*;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

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

    private Playlist testPlaylist;
    private User testUser;
    private Track testTrack;
    private CreatePlaylistRequest createRequest;
    private UpdatePlaylistRequest updateRequest;

    @BeforeEach
    void setUp() {
        testUser = new User();
        testUser.setId(1L);
        testUser.setUsername("testUser");

        testTrack = new Track();
        testTrack.setId(1L);
        testTrack.setTitle("Test Track");

        testPlaylist = new Playlist();
        testPlaylist.setId(1L);
        testPlaylist.setName("Test Playlist");
        testPlaylist.getUsers().add(testUser);
        testPlaylist.getTracks().add(testTrack);

        createRequest = new CreatePlaylistRequest();
        createRequest.setName("New Playlist");

        updateRequest = new UpdatePlaylistRequest();
        updateRequest.setName("Updated Playlist");
        updateRequest.setUserId(2L);
        updateRequest.setTrackId(2L);
    }

    @Test
    void getAllPlaylists_ShouldReturnFromCache() {
        // Arrange
        Page<Playlist> cachedPage = new PageImpl<>(List.of(testPlaylist));
        when(cacheService.containsKey(anyString())).thenReturn(true);
        when(cacheService.get(anyString())).thenReturn(cachedPage);

        // Act
        Page<Playlist> result = playlistService.getAllPlaylists(null, null, 0, 10, "name");

        // Assert
        assertEquals(1, result.getContent().size());
        verifyNoInteractions(playlistRepository);
    }

    @Test
    void getAllPlaylists_ShouldFetchFromDb_WhenNoCache() {
        // Arrange
        Page<Playlist> dbPage = new PageImpl<>(List.of(testPlaylist));
        Pageable pageable = PageRequest.of(0, 10, Sort.by("name"));
        when(playlistRepository.findAll(pageable)).thenReturn(dbPage);

        // Act
        Page<Playlist> result = playlistService.getAllPlaylists(null, null, 0, 10, "name");

        // Assert
        assertEquals(1, result.getContent().size());
        verify(cacheService, times(1)).put(anyString(), eq(dbPage));
    }

    @Test
    void getPlaylistById_ShouldReturnPlaylist() {
        // Arrange
        when(playlistRepository.findById(1L)).thenReturn(Optional.of(testPlaylist));

        // Act
        Optional<Playlist> result = playlistService.getPlaylistById(1L);

        // Assert
        assertTrue(result.isPresent());
        assertEquals("Test Playlist", result.get().getName());
    }

    @Test
    void savePlaylist_ShouldCreateNewPlaylist() {
        // Arrange
        when(playlistRepository.save(any(Playlist.class))).thenReturn(testPlaylist);

        // Act
        Playlist result = playlistService.savePlaylist(createRequest);

        // Assert
        assertEquals("Test Playlist", result.getName());
        verify(cacheService, times(1)).clear();
        verify(playlistService, times(1)).evictAllPlaylistCaches();
    }

    @Test
    void deletePlaylist_ShouldRemovePlaylist() {
        // Arrange
        when(playlistRepository.findById(1L)).thenReturn(Optional.of(testPlaylist));

        // Act
        playlistService.deletePlaylist(1L);

        // Assert
        verify(playlistRepository, times(1)).delete(testPlaylist);
        verify(playlistService, times(1)).evictAllPlaylistCaches();
    }

    @Test
    void deletePlaylist_ShouldThrow_WhenNotFound() {
        // Arrange
        when(playlistRepository.findById(1L)).thenReturn(Optional.empty());

        // Act & Assert
        assertThrows(ResourceNotFoundException.class,
                () -> playlistService.deletePlaylist(1L));
    }

    @Test
    void mapToPlaylistResponse_ShouldConvertCorrectly() {
        // Act
        PlaylistResponse response = playlistService.mapToPlaylistResponse(testPlaylist);

        // Assert
        assertEquals(1L, response.getId());
        assertEquals("Test Playlist", response.getName());
        assertEquals(1, response.getUsers().size());
        assertEquals("testUser", response.getUsers().get(0));
        assertEquals(1, response.getTracks().size());
        assertEquals("Test Track", response.getTracks().get(0));
    }

    @Test
    void updatePlaylist_ShouldUpdateAllFields() {
        // Arrange
        User newUser = new User();
        newUser.setId(2L);
        newUser.setUsername("newUser");

        Track newTrack = new Track();
        newTrack.setId(2L);
        newTrack.setTitle("New Track");

        when(playlistRepository.findById(1L)).thenReturn(Optional.of(testPlaylist));
        when(userRepository.findById(2L)).thenReturn(Optional.of(newUser));
        when(trackRepository.findById(2L)).thenReturn(Optional.of(newTrack));
        when(playlistRepository.save(any(Playlist.class))).thenReturn(testPlaylist);

        // Act
        PlaylistResponse result = playlistService.updatePlaylist(1L, updateRequest);

        // Assert
        assertEquals("Updated Playlist", testPlaylist.getName());
        assertEquals(2, testPlaylist.getUsers().size());
        assertEquals(2, testPlaylist.getTracks().size());
        verify(playlistService, times(1)).evictAllPlaylistCaches();
    }

    @Test
    void updatePlaylist_ShouldUpdateOnlyName() {
        // Arrange
        UpdatePlaylistRequest partialRequest = new UpdatePlaylistRequest();
        partialRequest.setName("Partial Update");

        when(playlistRepository.findById(1L)).thenReturn(Optional.of(testPlaylist));
        when(playlistRepository.save(any(Playlist.class))).thenReturn(testPlaylist);

        // Act
        PlaylistResponse result = playlistService.updatePlaylist(1L, partialRequest);

        // Assert
        assertEquals("Partial Update", testPlaylist.getName());
        assertEquals(1, testPlaylist.getUsers().size()); // users not changed
        assertEquals(1, testPlaylist.getTracks().size()); // tracks not changed
    }

    @Test
    void updatePlaylist_ShouldThrow_WhenUserNotFound() {
        // Arrange
        when(playlistRepository.findById(1L)).thenReturn(Optional.of(testPlaylist));
        when(userRepository.findById(2L)).thenReturn(Optional.empty());

        // Act & Assert
        assertThrows(ResourceNotFoundException.class,
                () -> playlistService.updatePlaylist(1L, updateRequest));
    }

    @Test
    void updatePlaylist_ShouldThrow_WhenTrackNotFound() {
        // Arrange
        when(playlistRepository.findById(1L)).thenReturn(Optional.of(testPlaylist));
        when(userRepository.findById(2L)).thenReturn(Optional.of(new User()));
        when(trackRepository.findById(2L)).thenReturn(Optional.empty());

        // Act & Assert
        assertThrows(ResourceNotFoundException.class,
                () -> playlistService.updatePlaylist(1L, updateRequest));
    }

    @Test
    void fetchPlaylistsFromDB_ShouldFilterByUserAndName() {
        // Arrange
        Pageable pageable = PageRequest.of(0, 10);
        Page<Playlist> expectedPage = new PageImpl<>(List.of(testPlaylist));
        when(playlistRepository.findByUserUsernameAndNameNative("user", "name", pageable))
                .thenReturn(expectedPage);

        // Act
        Page<Playlist> result = playlistService.fetchPlaylistsFromDB("user", "name", pageable);

        // Assert
        assertEquals(1, result.getContent().size());
        verify(playlistRepository, times(1))
                .findByUserUsernameAndNameNative("user", "name", pageable);
    }

    @Test
    void buildPlaylistsCacheKey_ShouldIncludeAllParams() {
        // Act
        String key = playlistService.buildPlaylistsCacheKey("user", "name", 1, 20, "name");

        // Assert
        assertEquals("playlists_user_name_page1_size20_sortname", key);
    }
}