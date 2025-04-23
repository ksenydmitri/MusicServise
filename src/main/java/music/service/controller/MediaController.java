package music.service.controller;

import music.service.service.MediaService;
import music.service.service.MediaService.FileInfo;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.List;

@RestController
@RequestMapping("/media")
public class MediaController {

    private final MediaService mediaService;

    public MediaController(MediaService mediaService) {
        this.mediaService = mediaService;
    }

    @PostMapping("/upload")
    public ResponseEntity<String> uploadMedia(@RequestParam("file") MultipartFile file) {
        String webViewLink = mediaService.uploadMedia(file);
        return ResponseEntity.ok()
                .body("Файл загружен: " + webViewLink);
    }

    @GetMapping("/files")
    public ResponseEntity<List<FileInfo>> listFiles() throws IOException {
        return ResponseEntity.ok(mediaService.listFiles());
    }

    @GetMapping("/download/{fileId}")
    public ResponseEntity<byte[]> downloadFile(@PathVariable String fileId) throws IOException {
        byte[] fileContent = mediaService.downloadFile(fileId);
        MediaService.FileInfo fileInfo = mediaService.getFileInfo(fileId);

        return ResponseEntity.ok()
                .contentType(MediaType.parseMediaType(fileInfo.mimeType()))
                .header("Content-Disposition", "attachment; filename=\"" + fileInfo.name() + "\"")
                .body(fileContent);
    }

    @GetMapping("/search")
    public ResponseEntity<List<FileInfo>> searchFiles(@RequestParam String query) throws IOException {
        return ResponseEntity.ok(mediaService.searchFiles(query));
    }
}
