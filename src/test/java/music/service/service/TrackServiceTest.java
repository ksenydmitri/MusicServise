package music.service.service;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

import java.util.*;
import java.util.function.Function;
import java.util.stream.Collectors;

import music.service.config.CacheConfig;
import music.service.dto.CreateTrackRequest;
import music.service.dto.TrackResponse;
import music.service.dto.UpdateTrackRequest;
import music.service.exception.ResourceNotFoundException;
import music.service.exception.ValidationException;
import music.service.model.*;
import music.service.repositories.*;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Spy;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.*;
import org.springframework.data.jpa.domain.Specification;

@ExtendWith(MockitoExtension.class)
class TrackServiceTest {

    @Mock private TrackRepository trackRepository;
    @Mock private AlbumRepository albumRepository;
    @Mock private UserRepository userRepository;
    @Mock private PlaylistRepository playlistRepository;
    @Mock private CacheService cacheService;

    @InjectMocks private TrackService trackService;

    @Test
    void getAllTracks_ShouldReturnCachedResult_WhenCacheExists() {
        // Arrange
        String cacheKey = "tracks_user_album_title_genre_playlist_0_1";
        Page<Track> cachedPage = new PageImpl<>(List.of(new Track()));
        when(cacheService.containsKey(cacheKey)).thenReturn(true);
        when(cacheService.get(cacheKey)).thenReturn(cachedPage);

        Pageable pageable = PageRequest.of(0, 1);

        // Act
        Page<Track> result = trackService.getAllTracks("user", "album", "title", "genre", "playlist", pageable);

        // Assert
        assertNotNull(result);
        assertEquals(cachedPage, result);
        verify(trackRepository, never()).findAll(any(Specification.class), eq(pageable));
    }

    @Test
    void getAllTracks_ShouldFetchFromDatabase_WhenCacheMiss() {
        // Arrange
        String cacheKey = "tracks_user_album_title_genre_playlist_0_1";
        Page<Track> dbPage = new PageImpl<>(List.of(new Track()));
        Pageable pageable = PageRequest.of(0, 1);

        when(cacheService.containsKey(cacheKey)).thenReturn(false);
        when(trackRepository.findAll(any(Specification.class), eq(pageable))).thenReturn(dbPage);

        // Act
        Page<Track> result = trackService.getAllTracks("user", "album", "title", "genre", "playlist", pageable);

        // Assert
        assertNotNull(result);
        assertEquals(dbPage, result);
        verify(cacheService).put(cacheKey, dbPage);
    }


    @Test
    void addTrack_ShouldSaveTrackAndClearCache() {
        // Arrange
        Long albumId = 1L;
        Long userId = 2L;

        CreateTrackRequest request = new CreateTrackRequest();
        request.setAlbumId(albumId);
        request.setUserId(userId);
        request.setTitle("Track Title");
        request.setDuration(300);

        Album album = new Album();
        album.setId(albumId);

        User user = new User();
        user.setId(userId);

        when(albumRepository.findById(albumId)).thenReturn(Optional.of(album));
        when(userRepository.findById(userId)).thenReturn(Optional.of(user));
        when(trackRepository.save(any())).thenAnswer(invocation -> invocation.getArgument(0));

        // Act
        TrackResponse response = trackService.addTrack(request);

        // Assert
        assertNotNull(response);
        assertEquals("Track Title", response.getTitle());
        assertEquals(300, response.getDuration());
        verify(cacheService).clear();
    }

    @Test
    void addTrack_ShouldThrowValidationException_WhenRequestIsInvalid() {
        // Arrange
        CreateTrackRequest request = new CreateTrackRequest(); // Нет обязательных полей

        // Act & Assert
        assertThrows(ValidationException.class, () -> trackService.addTrack(request));
    }

    @Test
    void updateTrack_ShouldUpdateTrackAndEvictCache() {
        // Arrange
        Long trackId = 1L;
        UpdateTrackRequest request = new UpdateTrackRequest();
        request.setTitle("Updated Title");
        request.setDuration(250);

        Track track = new Track();
        track.setId(trackId);
        track.setTitle("Original Title");

        when(trackRepository.findById(trackId)).thenReturn(Optional.of(track));
        when(trackRepository.save(any())).thenAnswer(invocation -> invocation.getArgument(0));

        // Act
        TrackResponse response = trackService.updateTrack(trackId, request);

        // Assert
        assertNotNull(response);
        assertEquals("Updated Title", response.getTitle());
        verify(cacheService).evict(anyString());
    }

    @Test
    void updateTrack_ShouldThrowResourceNotFoundException_WhenTrackNotFound() {
        // Arrange
        Long trackId = 1L;
        UpdateTrackRequest request = new UpdateTrackRequest();
        request.setTitle("Updated Title");

        when(trackRepository.findById(trackId)).thenReturn(Optional.empty());

        // Act & Assert
        assertThrows(ResourceNotFoundException.class, () -> trackService.updateTrack(trackId, request));
    }


    @Test
    void deleteTrack_ShouldRemoveTrackAndEvictCache() {
        // Arrange
        Long trackId = 1L;

        Track track = new Track();
        track.setId(trackId);

        when(trackRepository.findById(trackId)).thenReturn(Optional.of(track));

        // Act
        trackService.deleteTrack(trackId);

        // Assert
        verify(trackRepository).delete(track);
        verify(cacheService).evictByPattern("tracks_*");
    }

    @Test
    void deleteTrack_ShouldThrowResourceNotFoundException_WhenTrackNotFound() {
        // Arrange
        Long trackId = 1L;

        when(trackRepository.findById(trackId)).thenReturn(Optional.empty());

        // Act & Assert
        assertThrows(ResourceNotFoundException.class, () -> trackService.deleteTrack(trackId));
    }


    @Test
    void addTracksBulk_ShouldSaveTracksAndClearCache() {
        // Arrange
        CreateTrackRequest request1 = new CreateTrackRequest();
        request1.setAlbumId(1L);
        request1.setUserId(2L);
        request1.setTitle("Track 1");
        request1.setDuration(200);

        CreateTrackRequest request2 = new CreateTrackRequest();
        request2.setAlbumId(1L);
        request2.setUserId(3L);
        request2.setTitle("Track 2");
        request2.setDuration(300);

        List<CreateTrackRequest> requests = List.of(request1, request2);

        Album album = new Album();
        album.setId(1L);

        User user1 = new User();
        user1.setId(2L);

        User user2 = new User();
        user2.setId(3L);

        when(albumRepository.findAllById(Set.of(1L))).thenReturn(List.of(album));
        when(userRepository.findAllById(Set.of(2L, 3L))).thenReturn(List.of(user1, user2));
        when(trackRepository.saveAll(any())).thenAnswer(invocation -> invocation.getArgument(0));

        // Act
        List<TrackResponse> responses = trackService.addTracksBulk(requests);

        // Assert
        assertEquals(2, responses.size());
        verify(cacheService).clear();
    }

    @Test
    void addTracksBulk_ShouldThrowException_WhenAlbumsNotFound() {
        // Arrange
        CreateTrackRequest request1 = new CreateTrackRequest();
        request1.setAlbumId(1L);
        request1.setUserId(2L);
        request1.setTitle("Track 1");
        request1.setDuration(200);

        List<CreateTrackRequest> requests = List.of(request1);

        when(albumRepository.findAllById(Set.of(1L))).thenReturn(Collections.emptyList());

        // Act & Assert
        assertThrows(ResourceNotFoundException.class, () -> trackService.addTracksBulk(requests));
    }

    @Test
    void getAllTracks_ShouldHandleEmptyParameters() {
        // Arrange
        String cacheKey = "tracks_all_all_all_all_all_0_1";
        Page<Track> dbPage = new PageImpl<>(List.of(new Track()));
        Pageable pageable = PageRequest.of(0, 1);

        when(cacheService.containsKey(cacheKey)).thenReturn(false);
        when(trackRepository.findAll(any(Specification.class), eq(pageable))).thenReturn(dbPage);

        // Act
        Page<Track> result = trackService.getAllTracks(null, null, null, null, null, pageable);

        // Assert
        assertNotNull(result);
        assertEquals(dbPage, result);
        verify(cacheService).put(cacheKey, dbPage);
    }

    @Test
    void getAllTracks_ShouldFetchDataWithPartiallyFilledParameters() {
        // Arrange
        String cacheKey = "tracks_user_album_all_genre_all_0_1";
        Page<Track> dbPage = new PageImpl<>(List.of(new Track()));
        Pageable pageable = PageRequest.of(0, 1);

        when(cacheService.containsKey(cacheKey)).thenReturn(false);
        when(trackRepository.findAll(any(Specification.class), eq(pageable))).thenReturn(dbPage);

        // Act
        Page<Track> result = trackService.getAllTracks("user", "album", null, "genre", null, pageable);

        // Assert
        assertNotNull(result);
        assertEquals(dbPage, result);
        verify(cacheService).put(cacheKey, dbPage);
    }

    @Test
    void addTrack_ShouldThrowResourceNotFoundException_WhenAlbumNotProvided() {
        // Arrange
        CreateTrackRequest request = new CreateTrackRequest();
        request.setAlbumId(99L);
        request.setUserId(2L);
        request.setTitle("Track Title");
        request.setDuration(200);

        when(albumRepository.findById(request.getAlbumId())).thenReturn(Optional.empty());

        // Act & Assert
        assertThrows(ResourceNotFoundException.class, () -> trackService.addTrack(request));
    }

    @Test
    void addTrack_ShouldThrowResourceNotFoundException_WhenUserNotProvided() {
        // Arrange
        CreateTrackRequest request = new CreateTrackRequest();
        request.setAlbumId(1L);
        request.setUserId(99L);
        request.setTitle("Track Title");
        request.setDuration(200);

        Album album = new Album();
        album.setId(1L);
        when(albumRepository.findById(request.getAlbumId())).thenReturn(Optional.of(album));
        when(userRepository.findById(request.getUserId())).thenReturn(Optional.empty());

        // Act & Assert
        assertThrows(ResourceNotFoundException.class, () -> trackService.addTrack(request));
    }

    @Test
    void updateTrack_ShouldUpdateOnlyTitle() {
        // Arrange
        Long trackId = 1L;
        UpdateTrackRequest request = new UpdateTrackRequest();
        request.setTitle("Updated Title");
        request.setDuration(0); // Не меняем длительность

        Track track = new Track();
        track.setId(trackId);
        track.setTitle("Original Title");

        when(trackRepository.findById(trackId)).thenReturn(Optional.of(track));
        when(trackRepository.save(any())).thenAnswer(invocation -> invocation.getArgument(0));

        // Act
        TrackResponse response = trackService.updateTrack(trackId, request);

        // Assert
        assertNotNull(response);
        assertEquals("Updated Title", response.getTitle());
        verify(cacheService).evict(anyString());
    }

    @Test
    void deleteTrack_ShouldHandleTrackWithoutAlbum() {
        // Arrange
        Long trackId = 1L;

        Track track = new Track();
        track.setId(trackId);

        when(trackRepository.findById(trackId)).thenReturn(Optional.of(track));

        // Act
        trackService.deleteTrack(trackId);

        // Assert
        verify(trackRepository).delete(track);
        verify(cacheService).evictByPattern("tracks_*");
    }

    @Test
    void addTracksBulk_ShouldThrowException_WhenUsersNotFound() {
        // Arrange
        CreateTrackRequest request1 = new CreateTrackRequest();
        request1.setAlbumId(1L);
        request1.setUserId(99L); // Пользователь не существует
        request1.setTitle("Track Title");
        request1.setDuration(200);

        List<CreateTrackRequest> requests = List.of(request1);

        Album album = new Album();
        album.setId(1L);
        when(albumRepository.findAllById(Set.of(1L))).thenReturn(List.of(album));
        when(userRepository.findAllById(Set.of(99L))).thenReturn(Collections.emptyList());

        // Act & Assert
        assertThrows(ResourceNotFoundException.class, () -> trackService.addTracksBulk(requests));
    }


    @Test
    void validateInput_ShouldThrowException_WhenRequestIsNull() {
        // Arrange
        Long trackId = 1L;
        UpdateTrackRequest request = null;

        // Act & Assert
        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class,
                () ->trackService.validateInput(trackId, request));
        assertEquals("Request body must not be null", exception.getMessage());
    }

    @Test
    void validateInput_ShouldThrowException_WhenTrackIdIsNull() {
        // Arrange
        Long trackId = null;
        UpdateTrackRequest request = new UpdateTrackRequest();

        // Act & Assert
        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class,
                () -> trackService.validateInput(trackId, request));
        assertEquals("Track ID must not be null", exception.getMessage());
    }

    @Test
    void validateInput_ShouldNotThrowException_WhenInputIsValid() {
        // Arrange
        Long trackId = 1L;
        UpdateTrackRequest request = new UpdateTrackRequest();

        // Act & Assert
        assertDoesNotThrow(() -> trackService.validateInput(trackId, request));
    }

    @Test
    void updateTrackTitle_ShouldUpdateTitle_WhenTitleIsValid() {
        // Arrange
        Track track = new Track();
        track.setTitle("Old Title");
        String newTitle = "New Title";

        // Act
        trackService.updateTrackTitle(track, newTitle);

        // Assert
        assertEquals("New Title", track.getTitle());
    }

    @Test
    void updateTrackTitle_ShouldThrowException_WhenTitleIsEmpty() {
        // Arrange
        Track track = new Track();
        track.setTitle("Old Title");
        String emptyTitle = "";

        // Act & Assert
        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class,
                () -> trackService.updateTrackTitle(track, emptyTitle));
        assertEquals("Track title must not be empty", exception.getMessage());
    }

    @Test
    void updateTrackTitle_ShouldNotUpdate_WhenTitleIsNull() {
        // Arrange
        Track track = new Track();
        track.setTitle("Old Title");

        // Act
        trackService.updateTrackTitle(track, null);

        // Assert
        assertEquals("Old Title", track.getTitle());
    }


    @Test
    void updateTrackGenre_ShouldUpdateGenre_WhenGenreIsValid() {
        // Arrange
        Track track = new Track();
        track.setGenre("Old Genre");
        String newGenre = "New Genre";

        // Act
        trackService.updateTrackGenre(track, newGenre);

        // Assert
        assertEquals("New Genre", track.getGenre());
    }

    @Test
    void updateTrackGenre_ShouldThrowException_WhenGenreIsEmpty() {
        // Arrange
        Track track = new Track();
        track.setGenre("Old Genre");
        String emptyGenre = "";

        // Act & Assert
        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class,
                () -> trackService.updateTrackGenre(track, emptyGenre));
        assertEquals("Track genre must not be empty", exception.getMessage());
    }

    @Test
    void updateTrackGenre_ShouldNotUpdate_WhenGenreIsNull() {
        // Arrange
        Track track = new Track();
        track.setGenre("Old Genre");

        // Act
        trackService.updateTrackGenre(track, null);

        // Assert
        assertEquals("Old Genre", track.getGenre());
    }

    @Test
    void updateTrackDuration_ShouldUpdateDuration_WhenDurationIsValid() {
        // Arrange
        Track track = new Track();
        track.setDuration(200);
        int newDuration = 300;

        // Act
        trackService.updateTrackDuration(track, newDuration);

        // Assert
        assertEquals(300, track.getDuration());
    }

    @Test
    void updateTrackDuration_ShouldNotUpdate_WhenDurationIsInvalid() {
        // Arrange
        Track track = new Track();
        track.setDuration(200);
        int invalidDuration = -1;

        // Act
        trackService.updateTrackDuration(track, invalidDuration);

        // Assert
        assertEquals(200, track.getDuration());
    }


    @Test
    void addPlaylistToTrack_ShouldAddPlaylist_WhenPlaylistIsValid() {
        // Arrange
        Track track = new Track();
        Playlist playlist = new Playlist();
        playlist.setId(1L);
        when(playlistRepository.findById(1L)).thenReturn(Optional.of(playlist));

        // Act
        trackService.addPlaylistToTrack(track, 1L);

        // Assert
        assertTrue(track.getPlaylists().contains(playlist));
    }

    @Test
    void addPlaylistToTrack_ShouldNotAddPlaylist_WhenAlreadyExists() {
        // Arrange
        Track track = new Track();
        Playlist playlist = new Playlist();
        playlist.setId(1L);
        track.getPlaylists().add(playlist);
        when(playlistRepository.findById(1L)).thenReturn(Optional.of(playlist));

        // Act
        trackService.addPlaylistToTrack(track, 1L);

        // Assert
        assertEquals(1, track.getPlaylists().size());
    }

    @Test
    void addPlaylistToTrack_ShouldNotAddPlaylist_WhenPlaylistIdIsNull() {
        // Arrange
        Track track = new Track();

        // Act
        trackService.addPlaylistToTrack(track, null);

        // Assert
        assertTrue(track.getPlaylists().isEmpty());
    }

    @Test
    void addPlaylistToTrack_ShouldThrowException_WhenPlaylistNotFound() {
        // Arrange
        Track track = new Track();
        when(playlistRepository.findById(99L)).thenReturn(Optional.empty());

        // Act & Assert
        ResourceNotFoundException exception = assertThrows(ResourceNotFoundException.class,
                () -> trackService.addPlaylistToTrack(track, 99L));
        assertEquals("Playlist not found with ID: 99", exception.getMessage());
    }

    @Test
    void addUserToTrack_ShouldNotAddUser_WhenUserIdIsNull() {
        // Arrange
        Track track = new Track();

        // Act
        trackService.addUserToTrack(track, null);

        // Assert
        assertTrue(track.getUsers().isEmpty());
    }

    @Test
    void addUserToTrack_ShouldAddUser_WhenUserIsValid() {
        // Arrange
        Track track = new Track();
        User user = new User();
        user.setId(1L);
        when(userRepository.findById(1L)).thenReturn(Optional.of(user));

        // Act
        trackService.addUserToTrack(track, 1L);

        // Assert
        assertTrue(track.getUsers().contains(user));
    }

    @Test
    void addUserToTrack_ShouldNotDuplicateUser_WhenUserAlreadyExists() {
        // Arrange
        Track track = new Track();
        User user = new User();
        user.setId(1L);
        track.getUsers().add(user);
        when(userRepository.findById(1L)).thenReturn(Optional.of(user));

        // Act
        trackService.addUserToTrack(track, 1L);

        // Assert
        assertEquals(1, track.getUsers().size());
    }

    @Test
    void addUserToTrack_ShouldThrowException_WhenUserNotFound() {
        // Arrange
        Track track = new Track();
        when(userRepository.findById(99L)).thenReturn(Optional.empty());

        // Act & Assert
        ResourceNotFoundException exception = assertThrows(ResourceNotFoundException.class,
                () -> trackService.addUserToTrack(track, 99L));
        assertEquals("User not found with ID: 99", exception.getMessage());
    }

    @Test
    void validateSingleRequest_ShouldThrowException_WhenRequestIsNull() {
        // Arrange
        CreateTrackRequest request = null;

        // Act & Assert
        ValidationException exception = assertThrows(ValidationException.class,
                () -> trackService.validateSingleRequest(request));
        assertEquals("Request in list must not be null", exception.getMessage());
    }

    @Test
    void validateSingleRequest_ShouldThrowException_WhenTitleIsNull() {
        // Arrange
        CreateTrackRequest request = new CreateTrackRequest();
        request.setDuration(300); // Указываем другие корректные параметры

        // Act & Assert
        ValidationException exception = assertThrows(ValidationException.class,
                () -> trackService.validateSingleRequest(request));
        assertEquals("Track title must not be empty", exception.getMessage());
    }

    @Test
    void validateSingleRequest_ShouldThrowException_WhenTitleIsEmpty() {
        // Arrange
        CreateTrackRequest request = new CreateTrackRequest();
        request.setTitle(""); // Пустой заголовок
        request.setDuration(300);

        // Act & Assert
        ValidationException exception = assertThrows(ValidationException.class,
                () -> trackService.validateSingleRequest(request));
        assertEquals("Track title must not be empty", exception.getMessage());
    }

    @Test
    void validateSingleRequest_ShouldThrowException_WhenDurationIsInvalid() {
        // Arrange
        CreateTrackRequest request = new CreateTrackRequest();
        request.setTitle("Valid Title");
        request.setDuration(-10); // Некорректная длительность

        // Act & Assert
        ValidationException exception = assertThrows(ValidationException.class,
                () -> trackService.validateSingleRequest(request));
        assertEquals("Track duration must be positive", exception.getMessage());
    }

    @Test
    void validateSingleRequest_ShouldNotThrowException_WhenRequestIsValid() {
        // Arrange
        CreateTrackRequest request = new CreateTrackRequest();
        request.setTitle("Valid Title");
        request.setDuration(300);

        // Act & Assert
        assertDoesNotThrow(() -> trackService.validateSingleRequest(request));
    }


}
