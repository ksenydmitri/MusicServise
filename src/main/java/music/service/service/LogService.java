package music.service.service;

import org.springframework.core.io.Resource;
import org.springframework.core.io.UrlResource;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import java.io.*;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ConcurrentHashMap;

@Service
public class LogService {

    private static final String LOG_DIRECTORY = "logs/";
    private static final String SOURCE_LOG_FILE = "logs/application.log";
    private final Map<String, String> taskStatus = new ConcurrentHashMap<>();
    private final Map<String, Resource> taskFiles = new ConcurrentHashMap<>();

    @Async
    public CompletableFuture<String> startLogGeneration(String date) {
        String taskId = generateTaskId();
        taskStatus.put(taskId, "PROCESSING");
        CompletableFuture.runAsync(() -> {
            try {
                Thread.sleep(20000);
                Path logFilePath = filterLogsByDate(date);
                taskFiles.put(taskId, new UrlResource(logFilePath.toUri()));
                taskStatus.put(taskId, "COMPLETED");
            } catch (Exception e) {
                taskStatus.put(taskId, "FAILED");
                e.printStackTrace();
            }
        });
        return CompletableFuture.completedFuture(taskId);
    }

    private Path filterLogsByDate(String date) throws IOException {

        Path logDir = Paths.get(LOG_DIRECTORY);
        if (!Files.exists(logDir)) {
            Files.createDirectories(logDir);
        }

        String outputFileName = String.format("application_%s.log", date);
        Path outputPath = logDir.resolve(outputFileName);

        Path sourcePath = Paths.get(SOURCE_LOG_FILE);
        if (!Files.exists(sourcePath)) {
            throw new FileNotFoundException("Source log file not found: " + sourcePath.toAbsolutePath());
        }

        try (BufferedReader reader = Files.newBufferedReader(sourcePath);
             BufferedWriter writer = Files.newBufferedWriter(outputPath)) {

            String line;
            while ((line = reader.readLine()) != null) {
                if (line.startsWith(date)) {
                    writer.write(line);
                    writer.newLine();
                }
            }
        }

        return outputPath;
    }

    public String getTaskStatus(String taskId) {
        return taskStatus.getOrDefault(taskId, "NOT_FOUND");
    }

    public Resource getLogFile(String taskId) {
        return taskFiles.getOrDefault(taskId, null);
    }

    private String generateTaskId() {
        return "TASK-" + System.currentTimeMillis();
    }
}