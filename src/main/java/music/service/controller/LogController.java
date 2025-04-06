package music.service.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import java.io.IOException;
import music.service.service.LogService;
import org.springframework.core.io.Resource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@Tag(name = "Log Controller", description = "API для работы с логами") // Описание контроллера
public class LogController {

    private final LogService logService;

    public LogController(LogService logService) {
        this.logService = logService;
    }

    @GetMapping("/logs")
    @Operation(
            summary = "Скачать лог-файл",
            description = "Возвращает лог-файл за указанную дату и индекс. "
                    + "Файл может быть сжат (gz) или распакован.",
            responses = {
                @ApiResponse(
                            responseCode = "200",
                            description = "Файл успешно найден и возвращен",
                            content = @Content(schema = @Schema(implementation = Resource.class))
                    ),
                @ApiResponse(
                            responseCode = "404",
                            description = "Файл не найден",
                            content = @Content
                    )
            }
    )
    public ResponseEntity<Resource> downloadLogFile(
            @Parameter(description = "Дата логов в формате yyyy-MM-dd",
                    example = "2023-10-01", required = true)
            @RequestParam String date,

            @Parameter(description = "Индекс файла (например, 0, 1, 2)", example = "0")
            @RequestParam(defaultValue = "0") int index,

            @Parameter(description = "Распаковать файл (true/false)", example = "false")
            @RequestParam(defaultValue = "false") boolean uncompress) throws IOException {
        try {
            Resource resource = logService.getLogFileByDate(date, index, uncompress);

            String contentType = uncompress ? MediaType.TEXT_PLAIN_VALUE : "application/gzip";
            String filename = resource.getFilename();

            return ResponseEntity.ok()
                    .contentType(MediaType.parseMediaType(contentType))
                    .header(HttpHeaders.CONTENT_DISPOSITION,
                            "attachment; filename=\"" + filename + "\"")
                    .body(resource);
        } catch (IOException e) {
            return ResponseEntity.notFound().build(); // 404, если файл не найден
        }
    }
}