package music.service.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.ArraySchema;
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
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.List;
import java.util.Set;


@RestController
@RequestMapping("/tracks")
@Tag(name = "Track Controller", description = "API для работы с треками")
public class TrackController {

    private final TrackService trackService;

    public TrackController(TrackService trackService) {
        this.trackService = trackService;
    }

    @GetMapping
    @Operation(
            summary = "Получить список треков",
            description = "Возвращает список треков с возможностью фильтрации",
            responses = {
                    @ApiResponse(
                            responseCode = "200",
                            description = "Список треков успешно получен",
                            content = @Content(schema = @Schema(implementation = Page.class))
                    )
            }
    )
    public ResponseEntity<Page<TrackResponse>> getAllTracks(
            @Parameter(description = "Фильтр по пользователю")
            @RequestParam(required = false) String user,
            @Parameter(description = "Фильтр по названию трека")
            @RequestParam(required = false) String title,
            @Parameter(description = "Фильтр по альбому")
            @RequestParam(required = false) String album,
            @Parameter(description = "Фильтр по жанру")
            @RequestParam(required = false) String genre,
            @Parameter(description = "Фильтр по плейлисту")
            @RequestParam(required = false) String playlist,
            @Parameter(description = "Номер страницы")
            @RequestParam(defaultValue = "0") int page,
            @Parameter(description = "Размер страницы")
            @RequestParam(defaultValue = "10") int size) {
        Pageable pageable = PageRequest.of(page, size);
        Page<Track> tracks = trackService.getAllTracks(user, album, title, genre, playlist, pageable);
        Page<TrackResponse> responses = tracks.map(trackService::mapToTrackResponse);
        return ResponseEntity.ok(responses);
    }

    @PostMapping(consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    @Operation(
            summary = "Добавить новый трек",
            description = "Создает новый трек",
            responses = {
                    @ApiResponse(
                            responseCode = "201",
                            description = "Трек успешно создан",
                            content = @Content(schema = @Schema(implementation = TrackResponse.class))
                    ),
                    @ApiResponse(
                            responseCode = "400",
                            description = "Некорректные данные"
                    )
            }
    )
    public ResponseEntity<TrackResponse> addTrack(
            @RequestBody @Valid CreateTrackRequest request,
            @RequestPart MultipartFile mediaFile) {
        try {
            trackService.validateTrackFile(mediaFile);
            TrackResponse response = trackService.addTrackWithMedia(request,mediaFile);
            return ResponseEntity.status(HttpStatus.CREATED).body(response);
        } catch (ValidationException e) {
            throw new ValidationException(e.getMessage());
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    @PostMapping(value = "/bulk", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    @Operation(
            summary = "Добавить несколько треков",
            description = "Создает несколько треков за одну операцию",
            responses = {
                    @ApiResponse(
                            responseCode = "201",
                            description = "Треки успешно созданы",
                            content = @Content(array
                                    = @ArraySchema(schema =
                            @Schema(implementation = TrackResponse.class)))
                    ),
                    @ApiResponse(
                            responseCode = "400",
                            description = "Некорректные данные"
                    )
            }
    )
    public ResponseEntity<List<TrackResponse>> addTracksBulk(
            @ModelAttribute @Valid List<CreateTrackRequest> requests) {
        try {
            List<TrackResponse> responses = trackService.addTracksBulk(requests);
            return ResponseEntity.status(HttpStatus.CREATED).body(responses);
        } catch (ValidationException e) {
            throw new ValidationException(e.getMessage());
        }
    }

    @PatchMapping("/{id}")
    @Operation(
            summary = "Обновить трек",
            description = "Обновляет данные трека",
            responses = {
                    @ApiResponse(
                            responseCode = "200",
                            description = "Трек успешно обновлен",
                            content = @Content(schema = @Schema(implementation = TrackResponse.class))
                    ),
                    @ApiResponse(
                            responseCode = "404",
                            description = "Трек не найден"
                    )
            }
    )
    public ResponseEntity<TrackResponse> patchTrack(
            @PathVariable Long id,
            @RequestBody UpdateTrackRequest request) {
        TrackResponse updatedTrack = trackService.updateTrack(id, request);
        return ResponseEntity.ok(updatedTrack);
    }

    @DeleteMapping("/{id}")
    @Operation(
            summary = "Удалить трек",
            description = "Удаляет трек по идентификатору",
            responses = {
                    @ApiResponse(
                            responseCode = "204",
                            description = "Трек успешно удален"
                    ),
                    @ApiResponse(
                            responseCode = "404",
                            description = "Трек не найден"
                    )
            }
    )
    public ResponseEntity<Void> deleteTrack(@PathVariable Long id) {
        trackService.deleteTrack(id);
        return ResponseEntity.noContent().build();
    }

}