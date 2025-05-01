package music.service.service;

import music.service.dto.*;
import music.service.exception.ResourceNotFoundException;
import music.service.model.Album;
import music.service.model.User;
import music.service.repositories.AlbumRepository;
import music.service.repositories.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.*;
import java.util.stream.Collectors;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class AlbumServiceTest {

    @Mock
    private AlbumRepository albumRepository;

    @Mock
    private UserRepository userRepository;

    @Mock
    private CacheService cacheService;

    @Mock
    private MediaService mediaService;

    @InjectMocks
    private AlbumService albumService;

    private User testUser;
    private Album testAlbum;
    private CreateAlbumRequest createRequest;
    private UpdateAlbumRequest updateRequest;

    @BeforeEach
    void setUp() {
        testUser = new User();
        testUser.setId(1L);
        testUser.setUsername("testUser");

        testAlbum = new Album();
        testAlbum.setId(1L);
        testAlbum.setTitle("Test Album");
        testAlbum.getUsers().add(testUser);

        createRequest = new CreateAlbumRequest();
        createRequest.setName("New Album");
        createRequest.setUserId(1L);

        updateRequest = new UpdateAlbumRequest();
        updateRequest.setName("Updated Album");
        updateRequest.setUserId(1L);
    }

    @Test
    void getAllAlbums_ShouldReturnFromCache_WhenCacheExists() {
        // Arrange
        String cacheKey = "albums_all_all_page0_size_10_sort_title";
        Page<Album> cachedPage = new PageImpl<>(Collections.singletonList(testAlbum));
        when(cacheService.containsKey(cacheKey)).thenReturn(true);
        when(cacheService.get(cacheKey)).thenReturn(cachedPage);

        // Act
        Page<Album> result = albumService.getAllAlbums(null, null, 0, 10, "title");

        // Assert
        assertEquals(1, result.getContent().size());
        assertEquals("Test Album", result.getContent().get(0).getTitle());
        verify(cacheService, times(1)).get(cacheKey);
        verifyNoInteractions(albumRepository);
    }

    @Test
    void getAllAlbums_ShouldFetchFromDbAndCache_WhenNoCache() {
        // Arrange
        String cacheKey = "albums_testUser_all_page0_size_10_sort_title";
        Pageable pageable = PageRequest.of(0, 10, Sort.by("title"));
        Page<Album> dbPage = new PageImpl<>(Collections.singletonList(testAlbum));

        when(cacheService.containsKey(cacheKey)).thenReturn(false);
        when(albumRepository.findByUserUsername("testUser", pageable)).thenReturn(dbPage);

        // Act
        Page<Album> result = albumService.getAllAlbums("testUser", null, 0, 10, "title");

        // Assert
        assertEquals(1, result.getContent().size());
        verify(albumRepository, times(1)).findByUserUsername("testUser", pageable);
        verify(cacheService, times(1)).put(cacheKey, dbPage);
    }

    @Test
    void getAlbumById_ShouldReturnFromCache() {
        // Arrange
        String cacheKey = "album_1";
        when(cacheService.containsKey(cacheKey)).thenReturn(true);
        when(cacheService.get(cacheKey)).thenReturn(testAlbum);

        // Act
        Album result = albumService.getAlbumById(1L);

        // Assert
        assertEquals("Test Album", result.getTitle());
        verifyNoInteractions(albumRepository);
    }

    @Test
    void getAlbumById_ShouldThrow_WhenNotFound() {
        // Arrange
        when(albumRepository.findById(1L)).thenReturn(Optional.empty());

        // Act & Assert
        assertThrows(ResourceNotFoundException.class, () -> albumService.getAlbumById(1L));
    }

    @Test
    void addAlbum_ShouldCreateNewAlbum() {
        // Arrange
        when(userRepository.findById(1L)).thenReturn(Optional.of(testUser));
        when(albumRepository.save(any(Album.class))).thenReturn(testAlbum);

        // Act
        AlbumResponse result = albumService.addAlbum(createRequest, null);

        // Assert
        assertEquals("Test Album", result.getTitle());
        verify(albumRepository, times(1)).save(any(Album.class));
        verify(cacheService, times(1)).evictByPattern("albums_*");
    }

    @Test
    void addAlbum_ShouldThrow_WhenInvalidRequest() {
        // Arrange
        CreateAlbumRequest invalidRequest = new CreateAlbumRequest();

        // Act & Assert
        assertThrows(IllegalArgumentException.class, () -> albumService.addAlbum(invalidRequest, null));
    }

    @Test
    void addAlbum_ShouldHandleCollaborators() {
        // Arrange
        User collaborator = new User();
        collaborator.setId(2L);
        collaborator.setUsername("collaborator");

        createRequest.setCollaborators(Collections.singletonList("collaborator"));
        when(userRepository.findById(1L)).thenReturn(Optional.of(testUser));
        when(userRepository.findByUsernameIn(anyList())).thenReturn(Collections.singletonList(collaborator));
        when(albumRepository.save(any(Album.class))).thenReturn(testAlbum);

        // Act
        AlbumResponse result = albumService.addAlbum(createRequest, null);

        // Assert
        assertEquals(2, result.getUserIds().size());
        verify(userRepository, times(1)).findByUsernameIn(anyList());
    }

    @Test
    void addAlbum_ShouldThrow_WhenCollaboratorNotFound() {
        // Arrange
        createRequest.setCollaborators(Collections.singletonList("unknown"));
        when(userRepository.findById(1L)).thenReturn(Optional.of(testUser));
        when(userRepository.findByUsernameIn(anyList())).thenReturn(Collections.emptyList());

        // Act & Assert
        assertThrows(ResourceNotFoundException.class, () -> albumService.addAlbum(createRequest, null));
    }

    @Test
    void updateAlbum_ShouldUpdateExistingAlbum() {
        // Arrange
        when(albumRepository.findById(1L)).thenReturn(Optional.of(testAlbum));
        when(userRepository.findById(1L)).thenReturn(Optional.of(testUser));
        when(albumRepository.save(any(Album.class))).thenReturn(testAlbum);

        // Act
        AlbumResponse result = albumService.updateAlbum(1L, updateRequest, null);

        // Assert
        assertEquals("Test Album", result.getTitle());
        verify(cacheService, times(1)).evict("album_1");
        verify(cacheService, times(1)).evictByPattern("albums_*");
    }

    @Test
    void updateAlbum_ShouldHandleCoverImage() {
        // Arrange
        MultipartFile mockFile = mock(MultipartFile.class);
        when(mockFile.isEmpty()).thenReturn(false);
        when(albumRepository.findById(1L)).thenReturn(Optional.of(testAlbum));
        when(mediaService.uploadMedia(mockFile)).thenReturn("newImageId");

        // Act
        albumService.updateAlbum(1L, updateRequest, mockFile);

        // Assert
        assertEquals("newImageId", testAlbum.getCoverImageId());
    }

    @Test
    void deleteAlbum_ShouldRemoveAlbum() {
        // Arrange
        when(albumRepository.findById(1L)).thenReturn(Optional.of(testAlbum));

        // Act
        albumService.deleteAlbum(1L);

        // Assert
        verify(albumRepository, times(1)).deleteById(1L);
        verify(cacheService, times(1)).evict("album_1");
    }

    @Test
    void mapToAlbumResponse_ShouldConvertCorrectly() {
        // Act
        AlbumResponse response = albumService.mapToAlbumResponse(testAlbum);

        // Assert
        assertEquals(1L, response.getId());
        assertEquals("Test Album", response.getTitle());
        assertEquals(1, response.getArtists().size());
        assertEquals("testUser", response.getArtists().get(0));
    }
}