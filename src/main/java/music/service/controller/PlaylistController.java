package music.service.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import java.util.Optional;
import javax.validation.Valid;
import music.service.dto.CreatePlaylistRequest;
import music.service.dto.PlaylistResponse;
import music.service.dto.UpdatePlaylistRequest;
import music.service.model.Playlist;
import music.service.service.PlaylistService;
import org.springframework.data.domain.Page;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/playlists")
@Tag(name = "Playlist Management", description = "API для управления плейлистами")
public class PlaylistController {

    private final PlaylistService playlistService;

    public PlaylistController(PlaylistService playlistService) {
        this.playlistService = playlistService;
    }

    @GetMapping
    @Operation(
            summary = "Получить список плейлистов",
            description = "Возвращает пагинированный список плейлистов с возможностью фильтрации",
            responses = {
                @ApiResponse(responseCode = "200", description = "Успешное получение списка")
            }
    )
    public ResponseEntity<Page<PlaylistResponse>> getAllPlaylists(
            @Parameter(description = "Фильтр по пользователю")
            @RequestParam(required = false) String user,
            @Parameter(description = "Фильтр по названию")
            @RequestParam(required = false) String name,
            @Parameter(description = "Номер страницы")
            @RequestParam(required = false, defaultValue = "0") int page,
            @Parameter(description = "Размер страницы")
            @RequestParam(required = false, defaultValue = "10") int size,
            @Parameter(description = "Поле для сортировки")
            @RequestParam(required = false, defaultValue = "name") String sort
    ) {
        Page<Playlist> playlists = playlistService.getAllPlaylists(user, name, page, size, sort);
        Page<PlaylistResponse> responses = playlists.map(playlistService::mapToPlaylistResponse);
        return ResponseEntity.ok(responses);
    }

    @GetMapping("/{id}")
    @Operation(
            summary = "Получить плейлист по ID",
            responses = {
                @ApiResponse(responseCode = "200", description = "Плейлист найден"),
                @ApiResponse(responseCode = "404", description = "Плейлист не найден")
            }
    )
    public ResponseEntity<PlaylistResponse> getPlaylistById(
            @Parameter(description = "ID плейлиста", required = true) @PathVariable Long id) {
        Optional<Playlist> playlistOpt = playlistService.getPlaylistById(id);
        return playlistOpt.map(playlist -> ResponseEntity.ok(
                        playlistService.mapToPlaylistResponse(playlist)))
                .orElse(ResponseEntity.notFound().build());
    }

    @PostMapping
    @Operation(
            summary = "Создать новый плейлист",
            responses = {
                @ApiResponse(responseCode = "201", description = "Плейлист успешно создан"),
                @ApiResponse(responseCode = "400", description = "Некорректные данные")
            }
    )
    public ResponseEntity<PlaylistResponse> createPlaylist(
            @io.swagger.v3.oas.annotations.parameters.RequestBody(
                    description = "Данные для создания плейлиста",
                    required = true,
                    content = @Content(schema = @Schema(
                            implementation = CreatePlaylistRequest.class)))
            @Valid @RequestBody CreatePlaylistRequest request) {

        Playlist savedPlaylist = playlistService.savePlaylist(request);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(playlistService.mapToPlaylistResponse(savedPlaylist));
    }

    @PatchMapping("/{id}")
    @Operation(
            summary = "Обновить плейлист",
            responses = {
                @ApiResponse(responseCode = "200", description = "Плейлист обновлен"),
                @ApiResponse(responseCode = "404", description = "Плейлист не найден")
            }
    )
    public ResponseEntity<PlaylistResponse> patchPlaylist(
            @Parameter(description = "ID плейлиста", required = true) @PathVariable Long id,
            @RequestBody UpdatePlaylistRequest request) {
        PlaylistResponse response = playlistService.updatePlaylist(id, request);
        return ResponseEntity.ok(response);
    }

    @DeleteMapping("/{id}")
    @Operation(
            summary = "Удалить плейлист",
            responses = {
                @ApiResponse(responseCode = "204", description = "Плейлист удален"),
                @ApiResponse(responseCode = "404", description = "Плейлист не найден")
            }
    )
    public ResponseEntity<Void> deletePlaylist(
            @Parameter(description = "ID плейлиста", required = true) @PathVariable Long id) {
        playlistService.deletePlaylist(id);
        return ResponseEntity.noContent().build();
    }
}