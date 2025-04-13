package music.service.service;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Optional;

import music.service.config.CacheConfig;
import music.service.dto.CreateAlbumRequest;
import music.service.dto.AlbumResponse;
import music.service.dto.UpdateAlbumRequest;
import music.service.exception.ResourceNotFoundException;
import music.service.model.Album;
import music.service.model.Track;
import music.service.model.User;
import music.service.repositories.AlbumRepository;
import music.service.repositories.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;

@ExtendWith(MockitoExtension.class)
class AlbumServiceTest {

    @Mock
    private AlbumRepository albumRepository;

    @Mock
    private UserRepository userRepository;

    @Mock
    private CacheService cacheService;

    @InjectMocks
    private AlbumService albumService;

    private Album album;
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

        album = new Album();
        album.setId(1L);
        album.setTitle("Test Album");
        album.setUsers(new HashSet<>(Collections.singleton(user)));
        album.setTracks(new HashSet<>(Collections.singleton(track)));
    }

    @Test
    void getAllAlbums_ShouldReturnFromCache() {
        // Arrange
        String cacheKey = "albums_all_all_page0_size_10_sort_title";
        Page<Album> expectedPage = new PageImpl<>(Collections.singletonList(album));

        when(cacheService.containsKey(cacheKey)).thenReturn(true);
        when(cacheService.get(cacheKey)).thenReturn(expectedPage);

        // Act
        Page<Album> result = albumService.getAllAlbums(null, null, 0, 10, "title");

        // Assert
        assertNotNull(result);
        assertEquals(1, result.getTotalElements());
        verify(albumRepository, never()).findAll(any(Pageable.class));
    }

    @Test
    void getAllAlbums_ShouldFetchFromDatabase() {
        // Arrange
        String cacheKey = "albums_testUser_Test_page0_size_10_sort_title";
        Pageable pageable = PageRequest.of(0, 10, Sort.by("title"));
        Page<Album> expectedPage = new PageImpl<>(Collections.singletonList(album));

        when(cacheService.containsKey(cacheKey)).thenReturn(false);
        when(albumRepository.findByUserUsernameAndTitleNative("testUser", "Test", pageable))
                .thenReturn(expectedPage);

        // Act
        Page<Album> result = albumService.getAllAlbums("testUser", "Test", 0, 10, "title");

        // Assert
        assertNotNull(result);
        assertEquals(1, result.getTotalElements());
        verify(cacheService).put(cacheKey, expectedPage);
    }

    /*@Test
    void getAllAlbums_ShouldHandleEmptyParameters() {
        // Arrange
        String cacheKey = "albums_all_all_page0_size_10_sort_title";
        Page<Album> expectedPage = Page.empty(); // Ожидаем пустую страницу

        when(cacheService.containsKey(cacheKey)).thenReturn(false); // Ключ отсутствует в кеше
        when(albumRepository.findByUserUsernameAndTitleNative(null, null, PageRequest.of(0, 10, Sort.by("title"))))
                .thenReturn(Page.empty()); // Мокируем возвращение пустой страницы

        // Act
        Page<Album> result = albumService.getAllAlbums(null, null, 0, 10, "title");

        // Assert
        assertNotNull(result); // Проверяем, что результат не null
        assertEquals(0, result.getTotalElements()); // Ожидаем 0 элементов
        verify(cacheService).put(cacheKey, expectedPage); // Проверяем вызов кеширования
    }*/

    @Test
    void getAlbumById_ShouldReturnFromCache() {
        // Arrange
        String cacheKey = "album_1";

        when(cacheService.containsKey(cacheKey)).thenReturn(true);
        when(cacheService.get(cacheKey)).thenReturn(album);

        // Act
        Album result = albumService.getAlbumById(1L);

        // Assert
        assertEquals("Test Album", result.getTitle());
        verify(albumRepository, never()).findById(anyLong());
    }

    @Test
    void getAlbumById_ShouldFetchFromDB_WhenNoCache() {
        // Arrange
        Long albumId = 1L;
        String cacheKey = "album_" + albumId;
        Album dbAlbum = new Album();
        dbAlbum.setId(albumId);

        when(cacheService.containsKey(cacheKey)).thenReturn(false);
        when(albumRepository.findById(albumId)).thenReturn(Optional.of(dbAlbum));

        // Act
        Album result = albumService.getAlbumById(albumId);

        // Assert
        assertNotNull(result);
        assertEquals(dbAlbum, result);
        verify(cacheService).put(cacheKey, dbAlbum);
    }

    @Test
    void getAlbumById_ShouldHandleCacheMissButFoundInDB() {
        // Arrange
        Long albumId = 1L;
        String cacheKey = "album_" + albumId;

        Album dbAlbum = new Album();
        dbAlbum.setId(albumId);

        when(cacheService.containsKey(cacheKey)).thenReturn(false);
        when(albumRepository.findById(albumId)).thenReturn(Optional.of(dbAlbum));

        // Act
        Album result = albumService.getAlbumById(albumId);

        // Assert
        assertNotNull(result);
        assertEquals(dbAlbum, result);
        verify(cacheService).put(cacheKey, dbAlbum);
    }


    @Test
    void getAlbumById_ShouldThrowException_WhenAlbumNotFound() {
        // Arrange
        Long albumId = 1L;
        when(cacheService.containsKey("album_" + albumId)).thenReturn(false);
        when(albumRepository.findById(albumId)).thenReturn(Optional.empty());

        // Act & Assert
        assertThrows(ResourceNotFoundException.class, () -> albumService.getAlbumById(albumId));
    }

    @Test
    void addAlbum_ShouldSaveAlbumAndEvictCache() {
        // Arrange
        Long userId = 1L;
        User user = new User();
        user.setId(userId);

        CreateAlbumRequest request = new CreateAlbumRequest();
        request.setUserId(userId);
        request.setName("Test Album");

        Album album = new Album(request.getName());
        when(userRepository.findById(userId)).thenReturn(Optional.of(user));
        when(albumRepository.save(any())).thenReturn(album);

        // Act
        AlbumResponse response = albumService.addAlbum(request);

        // Assert
        assertNotNull(response);
        assertEquals("Test Album", response.getTitle());
        verify(cacheService, atLeastOnce()).evictByPattern("albums_*");
    }

    @Test
    void addAlbum_ShouldThrowException_WhenUserNotFound() {
        // Arrange
        Long userId = 1L;

        CreateAlbumRequest request = new CreateAlbumRequest();
        request.setUserId(userId);
        request.setName("Test Album");

        when(userRepository.findById(userId)).thenReturn(Optional.empty());

        // Act & Assert
        assertThrows(ResourceNotFoundException.class, () -> albumService.addAlbum(request));
    }

    @Test
    void addAlbum_ShouldThrowValidationException_WhenRequestIsInvalid() {
        // Arrange
        CreateAlbumRequest request = new CreateAlbumRequest(); // Без обязательных полей

        // Act & Assert
        assertThrows(IllegalArgumentException.class, () -> albumService.addAlbum(request));
    }


    @Test
    void updateAlbum_ShouldUpdateAlbumAndEvictCache() {
        // Arrange
        Long albumId = 1L;
        Long userId = 1L;
        Album album = new Album();
        album.setId(albumId);

        User user = new User();
        user.setId(userId);

        UpdateAlbumRequest request = new UpdateAlbumRequest();
        request.setName("Updated Album");
        request.setUserId(userId);

        when(albumRepository.findById(albumId)).thenReturn(Optional.of(album));
        when(userRepository.findById(userId)).thenReturn(Optional.of(user));
        when(albumRepository.save(any())).thenReturn(album);

        // Act
        AlbumResponse response = albumService.updateAlbum(albumId, request);

        // Assert
        assertNotNull(response);
        assertEquals("Updated Album", response.getTitle());
        verify(cacheService).evict("album_" + albumId);
        verify(cacheService, atLeastOnce()).evictByPattern("albums_*");
    }

    @Test
    void updateAlbum_ShouldThrowException_WhenAlbumNotFound() {
        // Arrange
        Long albumId = 1L;
        Long userId = 1L;

        UpdateAlbumRequest request = new UpdateAlbumRequest();
        request.setName("Updated Album");
        request.setUserId(userId);

        when(albumRepository.findById(albumId)).thenReturn(Optional.empty());

        // Act & Assert
        assertThrows(ResourceNotFoundException.class, () -> albumService.updateAlbum(albumId, request));
    }

    @Test
    void updateAlbum_ShouldThrowException_WhenUserNotFound() {
        // Arrange
        Long albumId = 1L;
        Long userId = 1L;
        Album album = new Album();
        album.setId(albumId);

        UpdateAlbumRequest request = new UpdateAlbumRequest();
        request.setName("Updated Album");
        request.setUserId(userId);

        when(albumRepository.findById(albumId)).thenReturn(Optional.of(album));
        when(userRepository.findById(userId)).thenReturn(Optional.empty());

        // Act & Assert
        assertThrows(ResourceNotFoundException.class, () -> albumService.updateAlbum(albumId, request));
    }

    @Test
    void updateAlbum_ShouldHandleOnlyAlbumUpdate_NoUserChange() {
        // Arrange
        Long albumId = 1L;

        Album album = new Album();
        album.setId(albumId);

        UpdateAlbumRequest request = new UpdateAlbumRequest();
        request.setName("Updated Album");
        request.setUserId(null); // Пользователь не изменяется

        when(albumRepository.findById(albumId)).thenReturn(Optional.of(album));
        when(albumRepository.save(any())).thenReturn(album);

        // Act
        AlbumResponse response = albumService.updateAlbum(albumId, request);

        // Assert
        assertNotNull(response);
        assertEquals("Updated Album", response.getTitle());
        verify(cacheService).evict("album_" + albumId);
    }

    @Test
    void deleteAlbum_ShouldRemoveAlbumAndEvictCache() {
        // Arrange
        Long albumId = 1L;
        Album album = new Album();
        album.setId(albumId);

        when(albumRepository.findById(albumId)).thenReturn(Optional.of(album));

        // Act
        albumService.deleteAlbum(albumId);

        // Assert
        verify(albumRepository).deleteById(albumId);
        verify(cacheService).evict("album_" + albumId);
    }

    @Test
    void deleteAlbum_ShouldThrowException_WhenAlbumNotFound() {
        // Arrange
        Long albumId = 1L;

        when(albumRepository.findById(albumId)).thenReturn(Optional.empty());

        // Act & Assert
        assertThrows(ResourceNotFoundException.class, () -> albumService.deleteAlbum(albumId));
    }

    /*@Test
    void getAllAlbums_ShouldHandleEmptyCacheKey() {
        // Arrange
        String cacheKey = "albums_all_all_page0_size_10_sort_title"; // Генерируемый ключ
        when(cacheService.containsKey(cacheKey)).thenReturn(false); // Настройка мокирования

        // Act & Assert
        assertThrows(IllegalArgumentException.class, () -> albumService.getAllAlbums("all", "all", 0, 10, "title"));
    }*/

    @Test
    void fetchAlbumsFromDatabase_ShouldCallNativeQuery_WhenUserAndTitleAreProvided() {
        // Arrange
        Pageable pageable = PageRequest.of(0, 10);
        Page<Album> expectedPage = new PageImpl<>(Collections.singletonList(new Album()));
        when(albumRepository.findByUserUsernameAndTitleNative("testUser", "testTitle", pageable))
                .thenReturn(expectedPage);

        // Act
        Page<Album> result = albumService.fetchAlbumsFromDatabase("testUser", "testTitle", pageable);

        // Assert
        assertNotNull(result);
        assertEquals(1, result.getTotalElements());
    }

    @Test
    void fetchAlbumsFromDatabase_ShouldCallFindByUserUsername_WhenOnlyUserIsProvided() {
        // Arrange
        Pageable pageable = PageRequest.of(0, 10);
        Page<Album> expectedPage = new PageImpl<>(Collections.singletonList(new Album()));
        when(albumRepository.findByUserUsername("testUser", pageable)).thenReturn(expectedPage);

        // Act
        Page<Album> result = albumService.fetchAlbumsFromDatabase("testUser", null, pageable);

        // Assert
        assertNotNull(result);
        assertEquals(1, result.getTotalElements());
    }

    @Test
    void fetchAlbumsFromDatabase_ShouldCallFindAllByTitle_WhenOnlyTitleIsProvided() {
        // Arrange
        Pageable pageable = PageRequest.of(0, 10);
        Page<Album> expectedPage = new PageImpl<>(Collections.singletonList(new Album()));
        when(albumRepository.findAllByTitle("testTitle", pageable)).thenReturn(expectedPage);

        // Act
        Page<Album> result = albumService.fetchAlbumsFromDatabase(null, "testTitle", pageable);

        // Assert
        assertNotNull(result);
        assertEquals(1, result.getTotalElements());
    }

    @Test
    void fetchAlbumsFromDatabase_ShouldCallFindAll_WhenUserAndTitleAreNull() {
        // Arrange
        Pageable pageable = PageRequest.of(0, 10);
        Page<Album> expectedPage = new PageImpl<>(Collections.singletonList(new Album()));
        when(albumRepository.findAll(pageable)).thenReturn(expectedPage);

        // Act
        Page<Album> result = albumService.fetchAlbumsFromDatabase(null, null, pageable);

        // Assert
        assertNotNull(result);
        assertEquals(1, result.getTotalElements());
    }


}
