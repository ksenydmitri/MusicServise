package music.service.service;

import com.google.api.services.drive.Drive;
import music.service.dto.*;
import music.service.exception.ResourceNotFoundException;
import music.service.exception.ValidationException;
import music.service.model.*;
import music.service.repositories.*;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.*;
import java.util.stream.Collectors;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class TrackServiceTest {

    @Mock
    private TrackRepository trackRepository;

    @Mock
    private AlbumRepository albumRepository;

    @Mock
    private UserRepository userRepository;

    @Mock
    private CacheService cacheService;

    @Mock
    private Drive googleDriveService;

    @Mock
    private MediaService mediaService;

    @Mock
    private PlaylistRepository playlistRepository;

    @Mock
    private AlbumService albumService;

    @InjectMocks
    private TrackService trackService;

    private Track testTrack;
    private Album testAlbum;
    private User testUser;
    private Playlist testPlaylist;
    private CreateTrackRequest createRequest;
    private UpdateTrackRequest updateRequest;
    private MultipartFile mockMediaFile;

    @BeforeEach
    void setUp() {
        testUser = new User();
        testUser.setId(1L);
        testUser.setUsername("testUser");

        testAlbum = new Album();
        testAlbum.setId(1L);
        testAlbum.setTitle("Test Album");

        testPlaylist = new Playlist();
        testPlaylist.setId(1L);
        testPlaylist.setName("Test Playlist");

        testTrack = new Track();
        testTrack.setId(1L);
        testTrack.setTitle("Test Track");
        testTrack.setDuration(180);
        testTrack.setGenre("Rock");
        testTrack.setAlbum(testAlbum);
        testTrack.setUsers(Set.of(testUser));
        testTrack.setPlaylists(Set.of(testPlaylist));
        testTrack.setMediaFileId("media123");

        createRequest = new CreateTrackRequest();
        createRequest.setTitle("New Track");
        createRequest.setDuration(200);
        createRequest.setGenre("Pop");
        createRequest.setAlbumId(1L);
        createRequest.setUserId(1L);

        updateRequest = new UpdateTrackRequest();
        updateRequest.setTitle("Updated Track");
        updateRequest.setDuration(220);
        updateRequest.setGenre("Jazz");
        updateRequest.setUserIds(List.of(1L));
        updateRequest.setPlaylistIds(List.of(1L));

        mockMediaFile = mock(MultipartFile.class);
        when(mockMediaFile.getContentType()).thenReturn("audio/mpeg");
        when(mockMediaFile.isEmpty()).thenReturn(false);
    }

    @Test
    void addTrackWithMedia_ShouldCreateTrack() throws IOException, ValidationException {
        // Arrange
        when(albumRepository.findById(1L)).thenReturn(Optional.of(testAlbum));
        when(userRepository.findById(1L)).thenReturn(Optional.of(testUser));
        when(mediaService.uploadMedia(mockMediaFile)).thenReturn("newMedia123");
        when(trackRepository.save(any(Track.class))).thenReturn(testTrack);

        // Act
        TrackResponse result = trackService.addTrackWithMedia(createRequest, mockMediaFile);

        // Assert
        assertEquals("Test Track", result.getTitle());
        verify(cacheService, times(1)).clear();
    }

    @Test
    void addTrackWithMedia_ShouldThrow_WhenInvalidMedia() {
        // Arrange
        when(mockMediaFile.getContentType()).thenReturn("image/jpeg");

        // Act & Assert
        assertThrows(ValidationException.class,
                () -> trackService.addTrackWithMedia(createRequest, mockMediaFile));
    }

    @Test
    void updateTrack_ShouldUpdateFields() {
        // Arrange
        when(trackRepository.findById(1L)).thenReturn(Optional.of(testTrack));
        when(userRepository.findById(1L)).thenReturn(Optional.of(testUser));
        when(playlistRepository.findById(1L)).thenReturn(Optional.of(testPlaylist));
        when(trackRepository.save(any(Track.class))).thenReturn(testTrack);

        // Act
        TrackResponse result = trackService.updateTrack(1L, updateRequest);

        // Assert
        assertEquals("Updated Track", testTrack.getTitle());
        assertEquals(220, testTrack.getDuration());
        assertEquals("Jazz", testTrack.getGenre());
        verify(cacheService, atLeastOnce()).evictByPattern("tracks:*");
    }

    @Test
    void updateTrack_ShouldThrow_WhenTrackNotFound() {
        // Arrange
        when(trackRepository.findById(1L)).thenReturn(Optional.empty());

        // Act & Assert
        assertThrows(ResourceNotFoundException.class,
                () -> trackService.updateTrack(1L, updateRequest));
    }

    @Test
    void deleteTrack_ShouldRemoveTrack() throws IOException {
        // Arrange
        when(trackRepository.findById(1L)).thenReturn(Optional.of(testTrack));

        // Act
        trackService.deleteTrack(1L);

        // Assert
        verify(trackRepository, times(1)).delete(testTrack);
        verify(googleDriveService, times(1)).files().delete("media123");
        verify(trackService, times(1)).evictAllTrackCaches();
        verify(albumService, times(1)).clearCacheForAlbum(1L);
    }

    @Test
    void getAllTracks_ShouldReturnFromCache() {
        // Arrange
        Page<Track> cachedPage = new PageImpl<>(List.of(testTrack));
        when(cacheService.containsKey(anyString())).thenReturn(true);
        when(cacheService.get(anyString())).thenReturn(cachedPage);

        // Act
        Page<Track> result = trackService.getAllTracks(null, null, null, null, null, PageRequest.of(0, 10));

        // Assert
        assertEquals(1, result.getContent().size());
        verifyNoInteractions(trackRepository);
    }

    @Test
    void getAllTracks_ShouldFetchFromDb_WhenNoCache() {
        // Arrange
        Page<Track> dbPage = new PageImpl<>(List.of(testTrack));
        when(trackRepository.findTracks(any(), any(), any(), any(), any(), any())).thenReturn(dbPage);

        // Act
        Page<Track> result = trackService.getAllTracks("testUser", "Test Album", "Test Track", "Rock", "Test Playlist", PageRequest.of(0, 10));

        // Assert
        assertEquals(1, result.getContent().size());
        verify(cacheService, times(1)).put(anyString(), eq(dbPage));
    }

    @Test
    void addTracksBulk_ShouldCreateMultipleTracks() throws ValidationException {
        // Arrange
        List<CreateTrackRequest> requests = List.of(createRequest, createRequest);
        List<MultipartFile> files = List.of(mockMediaFile, mockMediaFile);

        when(albumRepository.findAllById(any())).thenReturn(List.of(testAlbum));
        when(userRepository.findAllById(any())).thenReturn(List.of(testUser));
        when(trackRepository.saveAll(any())).thenReturn(List.of(testTrack, testTrack));

        // Act
        List<TrackResponse> results = trackService.addTracksBulk(requests, files);

        // Assert
        assertEquals(2, results.size());
        verify(cacheService, times(1)).clear();
    }

    @Test
    void addTracksBulk_ShouldThrow_WhenCountMismatch() {
        // Arrange
        List<CreateTrackRequest> requests = List.of(createRequest);
        List<MultipartFile> files = List.of(mockMediaFile, mockMediaFile);

        // Act & Assert
        assertThrows(ValidationException.class,
                () -> trackService.addTracksBulk(requests, files));
    }

    @Test
    void mapToTrackResponse_ShouldConvertCorrectly() {
        // Act
        TrackResponse response = trackService.mapToTrackResponse(testTrack);

        // Assert
        assertEquals(1L, response.getId());
        assertEquals("Test Track", response.getTitle());
        assertEquals(180, response.getDuration());
        assertEquals("Rock", response.getGenre());
        assertEquals(1, response.getUsernames().size());
        assertEquals("testUser", response.getUsernames().get(0));
    }

    @Test
    void validateTrackFile_ShouldPass_ForValidAudio() {
        // Arrange
        when(mockMediaFile.getContentType()).thenReturn("audio/mpeg");

        // Act & Assert (no exception)
        trackService.validateTrackFile(mockMediaFile);
    }

    @Test
    void validateTrackFile_ShouldThrow_ForInvalidType() {
        // Arrange
        when(mockMediaFile.getContentType()).thenReturn("image/jpeg");

        // Act & Assert
        assertThrows(ValidationException.class,
                () -> trackService.validateTrackFile(mockMediaFile));
    }
}