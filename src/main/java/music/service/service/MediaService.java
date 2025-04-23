package music.service.service;

import com.google.api.services.drive.Drive;
import com.google.api.services.drive.model.File;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class MediaService {

    private final Drive googleDriveService;

    public MediaService(Drive googleDriveService) {
        this.googleDriveService = googleDriveService;
    }

    public String uploadMedia(MultipartFile file) {
        try {
            File fileMetadata = new File();
            fileMetadata.setName(file.getOriginalFilename());
            InputStream fileStream = file.getInputStream();

            File uploadedFile = googleDriveService.files().create(
                    fileMetadata,
                    new com.google.api.client.http.InputStreamContent(file.getContentType(), fileStream)
            ).setFields("id, webViewLink").execute();

            return uploadedFile.getWebViewLink();
        } catch (Exception e) {
            throw new RuntimeException("Ошибка загрузки файла", e);
        }
    }

    public List<FileInfo> listFiles() throws IOException {
        List<File> files = googleDriveService.files().list()
                .setQ("mimeType contains 'image/' or mimeType contains 'video/' or mimeType contains 'audio/'")
                .setFields("files(id, name, webViewLink, mimeType, size)")
                .execute()
                .getFiles();

        return files.stream()
                .map(file -> new FileInfo(
                        file.getId(),
                        file.getName(),
                        file.getWebViewLink(),
                        file.getMimeType(),
                        file.getSize()
                ))
                .collect(Collectors.toList());
    }

    public byte[] downloadFile(String fileId) throws IOException {
        ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
        googleDriveService.files().get(fileId).executeMediaAndDownloadTo(outputStream);
        return outputStream.toByteArray();
    }

    public FileInfo getFileInfo(String fileId) throws IOException {
        File file = googleDriveService.files().get(fileId).execute();
        return new FileInfo(file.getId(), file.getName(), file.getWebViewLink(), file.getMimeType(), file.getSize());
    }

    public List<FileInfo> searchFiles(String query) throws IOException {
        List<File> files = googleDriveService.files().list()
                .setQ("name contains '" + query + "' and (mimeType contains 'image/' or mimeType contains 'video/' or mimeType contains 'audio/')")
                .setFields("files(id, name, webViewLink, mimeType, size)")
                .execute()
                .getFiles();

        return files.stream()
                .map(file -> new FileInfo(
                        file.getId(),
                        file.getName(),
                        file.getWebViewLink(),
                        file.getMimeType(),
                        file.getSize()
                ))
                .collect(Collectors.toList());
    }

    // DTO класс для возврата информации о файлах
    public record FileInfo(String id, String name, String url, String mimeType, Long size) {}
}
