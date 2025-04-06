package music.service.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import javax.validation.Valid;
import music.service.dto.*;
import music.service.exception.ValidationException;
import music.service.model.Track;
import music.service.service.TrackService;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;


@RestController
@RequestMapping("/tracks")
@Tag(name = "Track Controller", description = "API для работы с треками") // Описание контроллера
public class TrackController {

    private final TrackService trackService;

    public TrackController(TrackService trackService) {
        this.trackService = trackService;
    }

    @GetMapping
    @Operation(
            summary = "Получить список треков",
            description = "Возвращает список треков с возможностью фильтрации по пользователю,"
                    + " названию, альбому, жанру и плейлисту.",
            responses = {
                @ApiResponse(
                            responseCode = "200",
                            description = "Список треков успешно получен",
                            content = @Content(schema = @Schema(implementation = Page.class))
                    )
            }
    )
    public ResponseEntity<Page<TrackResponse>> getAllTracks(
            @Parameter(description = "Фильтр по пользователю", example = "user123")
            @RequestParam(required = false) String user,

            @Parameter(description = "Фильтр по названию трека", example = "My Song")
            @RequestParam(required = false) String title,

            @Parameter(description = "Фильтр по альбому", example = "My Album")
            @RequestParam(required = false) String album,

            @Parameter(description = "Фильтр по жанру", example = "Rock")
            @RequestParam(required = false) String genre,

            @Parameter(description = "Фильтр по плейлисту", example = "My Playlist")
            @RequestParam(required = false) String playlist,

            @Parameter(description = "Номер страницы", example = "0")
            @RequestParam(required = false, defaultValue = "0") int page,

            @Parameter(description = "Размер страницы", example = "10")
            @RequestParam(required = false, defaultValue = "10") int size) {
        Pageable pageable = PageRequest.of(page, size);
        Page<Track> tracks = trackService.getAllTracks(
                user, album, title, genre, playlist, pageable);
        Page<TrackResponse> responses = tracks.map(trackService::mapToTrackResponse);
        return ResponseEntity.ok(responses);
    }

    @PostMapping
    @Operation(
            summary = "Добавить новый трек",
            description = "Создает новый трек на основе переданных данных.",
            responses = {
                @ApiResponse(
                            responseCode = "201",
                            description = "Трек успешно создан",
                            content = @Content(schema = @Schema(
                                    implementation = TrackResponse.class))
                    ),
                @ApiResponse(
                            responseCode = "400",
                            description = "Некорректные данные",
                            content = @Content
                    )
            }
    )
    public ResponseEntity<TrackResponse> addTrack(
            @Parameter(description = "Данные для создания трека", required = true)
            @RequestBody @Valid CreateTrackRequest request) {
        try {
            TrackResponse response = trackService.addTrack(request);
            return ResponseEntity.status(HttpStatus.CREATED).body(response);
        } catch (ValidationException e) {
            throw new ValidationException(e.getMessage());
        }
    }

    @PatchMapping("/{id}")
    @Operation(
            summary = "Обновить трек",
            description = "Обновляет данные трека по его идентификатору.",
            responses = {
                @ApiResponse(
                            responseCode = "200",
                            description = "Трек успешно обновлен",
                            content = @Content(
                                    schema = @Schema(implementation = TrackResponse.class))
                    ),
                @ApiResponse(
                            responseCode = "404",
                            description = "Трек не найден",
                            content = @Content
                    )
            }
    )
    public ResponseEntity<TrackResponse> patchTrack(
            @Parameter(description = "Идентификатор трека", example = "1", required = true)
            @PathVariable Long id,

            @Parameter(description = "Данные для обновления трека", required = true)
            @RequestBody UpdateTrackRequest request) {
        TrackResponse updatedTrack = trackService.updateTrack(id, request);
        return ResponseEntity.ok(updatedTrack);
    }

    @DeleteMapping("/{id}")
    @Operation(
            summary = "Удалить трек",
            description = "Удаляет трек по его идентификатору.",
            responses = {
                @ApiResponse(
                            responseCode = "204",
                            description = "Трек успешно удален",
                            content = @Content
                    ),
                @ApiResponse(
                            responseCode = "404",
                            description = "Трек не найден",
                            content = @Content
                    )
            }
    )
    public ResponseEntity<Void> deleteTrack(
            @Parameter(description = "Идентификатор трека", example = "1", required = true)
            @PathVariable Long id) {
        trackService.deleteTrack(id);
        return ResponseEntity.noContent().build();
    }
}