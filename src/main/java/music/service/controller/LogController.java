package music.service.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import music.service.service.LogService;
import org.springframework.core.io.Resource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.io.IOException;

@RestController
@Tag(name = "Log Controller", description = "API для работы с логами и учёта посещений")
@RequestMapping("/logs")
public class LogController {

    private final LogService logService;

    public LogController(LogService logService) {
        this.logService = logService;

    }

    @PostMapping
    @Operation(
            summary = "Создать лог-файл",
            description = "Асинхронно создаёт лог-файл по указанной дате и возвращает ID задачи.",
            responses = {
                    @ApiResponse(
                            responseCode = "202",
                            description = "Задача принята",
                            content = @Content(schema = @Schema(implementation = String.class))
                    )
            }
    )
    public ResponseEntity<String> createLogFile(@RequestParam String date) {
        String taskId = logService.startLogGeneration(date).join();
        return ResponseEntity.accepted().body(taskId);
    }

    @GetMapping("/{taskId}/status")
    @Operation(
            summary = "Получить статус задачи",
            description = "Возвращает статус задачи (PROCESSING, COMPLETED, FAILED).",
            responses = {
                    @ApiResponse(responseCode = "200", description = "Статус найден"),
                    @ApiResponse(responseCode = "404", description = "ID задачи не найден")
            }
    )
    public ResponseEntity<String> getTaskStatus(
            @Parameter(description = "ID задачи", example = "TASK-123456")
            @PathVariable String taskId) {
        String status = logService.getTaskStatus(taskId);
        if (status.equals("NOT_FOUND")) {
            return ResponseEntity.notFound().build();
        }
        return ResponseEntity.ok(status);
    }

    @GetMapping("/{taskId}/file")
    @Operation(
            summary = "Скачать лог-файл по ID задачи",
            description = "Возвращает лог-файл, если задача завершена.",
            responses = {
                    @ApiResponse(responseCode = "200", description = "Файл найден"),
                    @ApiResponse(responseCode = "404", description = "Файл не найден или задача не завершена")
            }
    )
    public ResponseEntity<Resource> getLogFile(
            @Parameter(description = "ID задачи", example = "TASK-123456") @PathVariable String taskId) {
        Resource resource = logService.getLogFile(taskId);
        if (resource == null) {
            return ResponseEntity.notFound().build();
        }

        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"" + resource.getFilename() + "\"")
                .contentType(MediaType.APPLICATION_OCTET_STREAM)
                .body(resource);
    }
}