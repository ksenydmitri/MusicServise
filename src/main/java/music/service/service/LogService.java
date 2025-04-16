package music.service.service;

import org.springframework.core.io.Resource;
import org.springframework.core.io.UrlResource;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

import java.io.*;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.zip.GZIPInputStream;

@Service
public class LogService {

    private static final String LOG_DIRECTORY = "logs/";
    private final Map<String, String> taskStatus = new ConcurrentHashMap<>();
    private final Map<String, Resource> taskFiles = new ConcurrentHashMap<>();

    /**
     * Асинхронно запускает генерацию лог-файла.
     *
     * @param date Дата логов в формате yyyy-MM-dd.
     * @return Уникальный ID задачи.
     */
    @Async
    public String startLogGeneration(String date) {
        String taskId = generateTaskId();
        taskStatus.put(taskId, "PROCESSING");

        // Выполнение в отдельном потоке
        new Thread(() -> {
            try {
                // Эмуляция долгой работы
                Thread.sleep(5000);

                // Генерация лог-файла (эмуляция)
                String logFileName = String.format("application.log.%s.gz", date);
                Path path = Paths.get(LOG_DIRECTORY + logFileName);
                File file = path.toFile();

                if (!file.exists()) {
                    // Создаем фиктивный файл, если его нет
                    file.createNewFile();
                    try (FileWriter writer = new FileWriter(file)) {
                        writer.write("Sample log data for " + date);
                    }
                }

                taskFiles.put(taskId, new UrlResource(file.toURI()));
                taskStatus.put(taskId, "COMPLETED");
            } catch (Exception e) {
                taskStatus.put(taskId, "FAILED");
            }
        }).start();

        return taskId;
    }

    /**
     * Возвращает статус выполнения задачи по её ID.
     *
     * @param taskId Уникальный идентификатор задачи.
     * @return Статус задачи (PROCESSING, COMPLETED, FAILED, NOT_FOUND).
     */
    public String getTaskStatus(String taskId) {
        return taskStatus.getOrDefault(taskId, "NOT_FOUND");
    }

    /**
     * Возвращает лог-файл по ID задачи.
     *
     * @param taskId Уникальный идентификатор задачи.
     * @return Resource для файла логов.
     * @throws IOException Если файл не найден.
     */
    public Resource getLogFile(String taskId) throws IOException {
        if (!taskFiles.containsKey(taskId)) {
            return null;
        }
        return taskFiles.get(taskId);
    }

    /**
     * Возвращает Resource для файла логов за указанную дату.
     *
     * @param date       Дата в формате yyyy-MM-dd.
     * @param index      Индекс файла.
     * @param uncompress Нужно ли распаковать файл.
     * @return Resource для файла логов.
     * @throws IOException Если произошла ошибка при работе с файлом.
     */
    public Resource getLogFileByDate(String date, int index, boolean uncompress)
            throws IOException {
        LocalDate logDate = LocalDate.parse(date, DateTimeFormatter.ISO_DATE);
        String logFileName = String.format("application.log.%s.%d.gz", logDate, index);
        Path path = Paths.get(LOG_DIRECTORY + logFileName);
        File file = path.toFile();

        if (!file.exists()) {
            throw new IOException("File not found: " + path.toAbsolutePath());
        }

        if (uncompress) {
            String uncompressedFileName = String.format(
                    "application.log.%s.%d.log", logDate, index);
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
     * Генерирует уникальный идентификатор задачи.
     *
     * @return Уникальный ID задачи.
     */
    private String generateTaskId() {
        return "TASK-" + System.currentTimeMillis();
    }
}
