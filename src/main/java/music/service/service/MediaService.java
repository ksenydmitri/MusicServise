package music.service.service;

import com.google.api.services.drive.Drive;
import com.google.api.services.drive.model.File;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
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

            return uploadedFile.getId();
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

    public ResponseEntity<byte[]> streamFile(String fileId, String rangeHeader) throws IOException {
        byte[] fileContent = downloadFile(fileId);
        FileInfo fileInfo = getFileInfo(fileId);

        if (rangeHeader == null) {
            return ResponseEntity.ok()
                    .contentType(MediaType.parseMediaType(fileInfo.mimeType()))
                    .header("Accept-Ranges", "bytes")
                    .body(fileContent);
        }

        String[] ranges = rangeHeader.replace("bytes=", "").split("-");
        int start = Integer.parseInt(ranges[0]);
        int end = ranges.length > 1 ? Integer.parseInt(ranges[1]) : fileContent.length - 1;
        int chunkSize = end - start + 1;

        byte[] partialContent = new byte[chunkSize];
        System.arraycopy(fileContent, start, partialContent, 0, chunkSize);

        return ResponseEntity.status(206)
                .contentType(MediaType.parseMediaType(fileInfo.mimeType()))
                .header("Content-Range", "bytes " + start + "-" + end + "/" + fileContent.length)
                .header("Accept-Ranges", "bytes")
                .body(partialContent);
    }

    public void deleteFile(String fileId) {
        try {
            googleDriveService.files().delete(fileId).execute();
        } catch (IOException e) {
            System.err.println("Ошибка при удалении файла: " + e.getMessage());
            throw new RuntimeException("Не удалось удалить файл: " + fileId, e);
        }
    }

    public record FileInfo(
            String id,
            String name,
            String url,
            String mimeType,
            Long size) {}
}
