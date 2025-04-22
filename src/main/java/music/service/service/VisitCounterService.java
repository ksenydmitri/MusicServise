package music.service.service;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import org.springframework.stereotype.Service;

@Service
public class VisitCounterService {

    private final Map<String, Integer> visitCounts = new ConcurrentHashMap<>();

    public synchronized void incrementVisitCount(String url) {
        visitCounts.compute(url, (key, value) -> value == null ? 1 : value + 1);
    }


    public synchronized int getVisitCount(String url) {
        return visitCounts.getOrDefault(url, 0);
    }

    public synchronized int getTotalVisitCount() {
        return visitCounts.values().stream().mapToInt(Integer::intValue).sum();
    }

}
