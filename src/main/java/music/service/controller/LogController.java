package music.service.controller;

import music.service.service.LogService;
import org.springframework.core.io.Resource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.io.File;
import java.io.IOException;

@RestController
public class LogController {

    private final LogService logService;

    public LogController(LogService logService) {
        this.logService = logService;
    }

    /**
     * Скачивает файл логов за указанную дату и индекс.
     *
     * @param date       Дата в формате yyyy-MM-dd.
     * @param index      Индекс файла (например, 0, 1, 2).
     * @param uncompress Если true, возвращает распакованный файл.
     * @return ResponseEntity с файлом логов.
     */
    @GetMapping("/logs")
    public ResponseEntity<Resource> downloadLogFile(
            @RequestParam String date,
            @RequestParam(defaultValue = "0") int index,
            @RequestParam(defaultValue = "false") boolean uncompress) throws IOException {
        try {
            Resource resource = logService.getLogFileByDate(date, index, uncompress);

            // Определяем MIME-тип и имя файла
            String contentType = uncompress ? MediaType.TEXT_PLAIN_VALUE : "application/gzip";
            String filename = resource.getFilename();

            // Возвращаем файл
            return ResponseEntity.ok()
                    .contentType(MediaType.parseMediaType(contentType))
                    .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"" + filename + "\"")
                    .body(resource);
        } catch (IOException e) {
            return ResponseEntity.notFound().build(); // 404, если файл не найден
        }
    }
}