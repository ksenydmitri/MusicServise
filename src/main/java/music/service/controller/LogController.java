package music.service.controller;

import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import org.springframework.core.io.Resource;
import org.springframework.core.io.UrlResource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/logs")
public class LogController {

    @GetMapping("/download")
    public ResponseEntity<Resource> downloadLogFile(@RequestParam String date) {
        try {
            LocalDate logDate = LocalDate.parse(date, DateTimeFormatter.ISO_DATE);
            String logFileName = "application-" + logDate + ".log";
            Path logFilePath = Paths.get(logFileName);

            if (!logFilePath.toFile().exists()) {
                return ResponseEntity.notFound().build();
            }

            Resource resource = new UrlResource(logFilePath.toUri());
            return ResponseEntity.ok()
                    .contentType(MediaType.TEXT_PLAIN)
                    .header(HttpHeaders.CONTENT_DISPOSITION,
                            "attachment; filename=\"" + resource.getFilename() + "\"")
                    .body(resource);
        } catch (Exception e) {
            return ResponseEntity.internalServerError().build();
        }
    }
}