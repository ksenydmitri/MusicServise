package music.service.service;

import org.springframework.core.io.Resource;
import org.springframework.core.io.UrlResource;
import org.springframework.stereotype.Service;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.zip.GZIPInputStream;

@Service
public class LogService {

    private static final String LOG_DIRECTORY = "logs/";

    /**
     * Возвращает Resource для файла логов за указанную дату.
     *
     * @param date Дата в формате yyyy-MM-dd.
     * @return Resource для файла логов.
     * @throws IOException Если произошла ошибка при работе с файлом.
     */
    public Resource getLogFileByDate(String date, int index, boolean uncompress) throws IOException {
        LocalDate logDate = LocalDate.parse(date, DateTimeFormatter.ISO_DATE);
        String logFileName = String.format("application.log.%s.%d.gz", logDate, index);
        Path path = Paths.get(LOG_DIRECTORY + logFileName);
        File file = path.toFile();

        if (!file.exists()) {
            throw new IOException("File not found: " + path.toAbsolutePath());
        }

        if (uncompress) {
            String uncompressedFileName = String.format("application.log.%s.%d.log", logDate, index);
            File uncompressedFile = new File(LOG_DIRECTORY + uncompressedFileName);
            uncompressGzipFile(file, uncompressedFile);
            return new UrlResource(uncompressedFile.toURI());
        } else {
            return new UrlResource(path.toUri());
        }
    }

    /**
     * Распаковывает .gz файл.
     *
     * @param gzipFile   Сжатый файл.
     * @param outputFile Выходной файл.
     * @throws IOException Если произошла ошибка при распаковке.
     */
    private void uncompressGzipFile(File gzipFile, File outputFile) throws IOException {
        try (GZIPInputStream gzipInputStream = new GZIPInputStream(new FileInputStream(gzipFile));
             FileOutputStream fileOutputStream = new FileOutputStream(outputFile)) {
            byte[] buffer = new byte[1024];
            int len;
            while ((len = gzipInputStream.read(buffer)) > 0) {
                fileOutputStream.write(buffer, 0, len);
            }
        }
    }

    /**
     * Удаляет временный файл.
     *
     * @param file Файл для удаления.
     */
    public void deleteTempFile(File file) {
        if (file.exists()) {
            file.delete();
        }
    }
}