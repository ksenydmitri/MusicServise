package music.service.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import javax.validation.Valid;
import music.service.dto.AlbumResponse;
import music.service.dto.CreateAlbumRequest;
import music.service.dto.UpdateAlbumRequest;
import music.service.model.Album;
import music.service.service.AlbumService;
import org.springframework.data.domain.Page;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/albums")
@Tag(name = "Album Management", description = "API для управления альбомами")
public class AlbumController {

    private final AlbumService albumService;

    public AlbumController(AlbumService albumService) {
        this.albumService = albumService;
    }

    @GetMapping
    @Operation(
            summary = "Получить список альбомов",
            description = "Возвращает пагинированный список альбомов с возможностью фильтрации",
            responses = @ApiResponse(
                    responseCode = "200", description = "Успешное получение списка")
    )
    public ResponseEntity<Page<AlbumResponse>> getAllAlbums(
            @Parameter(description = "Фильтр по пользователю")
            @RequestParam(required = false) String user,
            @Parameter(description = "Фильтр по названию")
            @RequestParam(required = false) String title,
            @Parameter(description = "Номер страницы")
            @RequestParam(required = false, defaultValue = "0") int page,
            @Parameter(description = "Размер страницы")
            @RequestParam(required = false, defaultValue = "10") int size,
            @Parameter(description = "Поле для сортировки")
            @RequestParam(required = false, defaultValue = "title") String sort
    ) {
        Page<Album> albums = albumService.getAllAlbums(user, title, page, size, sort);
        Page<AlbumResponse> responses = albums.map(albumService::mapToAlbumResponse);
        return ResponseEntity.ok(responses);
    }

    @GetMapping("/{id}")
    @Operation(
            summary = "Получить альбом по ID",
            responses = {
                @ApiResponse(responseCode = "200", description = "Альбом найден"),
                @ApiResponse(responseCode = "404", description = "Альбом не найден")
            }
    )
    public ResponseEntity<AlbumResponse> getAlbumById(
            @Parameter(description = "ID альбома", required = true) @PathVariable Long id) {
        Album album = albumService.getAlbumById(id);
        return ResponseEntity.ok(albumService.mapToAlbumResponse(album));
    }

    @PostMapping
    @Operation(
            summary = "Создать новый альбом",
            responses = {
                @ApiResponse(responseCode = "201", description = "Альбом создан"),
                @ApiResponse(responseCode = "400", description = "Некорректные данные")
            }
    )
    public ResponseEntity<AlbumResponse> addAlbum(
            @io.swagger.v3.oas.annotations.parameters.RequestBody(
                    description = "Данные для создания альбома",
                    required = true,
                    content = @Content(schema = @Schema(implementation = CreateAlbumRequest.class)))
            @Valid @RequestBody CreateAlbumRequest request) {
        AlbumResponse response = albumService.addAlbum(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @PatchMapping("/{id}")
    @Operation(
            summary = "Обновить альбом",
            responses = {
                @ApiResponse(responseCode = "200", description = "Альбом обновлен"),
                @ApiResponse(responseCode = "404", description = "Альбом не найден")
            }
    )
    public ResponseEntity<AlbumResponse> patchAlbum(
            @Parameter(description = "ID альбома", required = true) @PathVariable Long id,
            @RequestBody UpdateAlbumRequest request) {
        AlbumResponse response = albumService.updateAlbum(id, request);
        return ResponseEntity.ok(response);
    }

    @DeleteMapping("/{id}")
    @Operation(
            summary = "Удалить альбом",
            responses = {
                @ApiResponse(responseCode = "204", description = "Альбом удален"),
                @ApiResponse(responseCode = "404", description = "Альбом не найден")
            }
    )
    public ResponseEntity<Void> deleteAlbum(
            @Parameter(description = "ID альбома", required = true) @PathVariable Long id) {
        albumService.deleteAlbum(id);
        return ResponseEntity.noContent().build();
    }
}