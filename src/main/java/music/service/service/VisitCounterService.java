package music.service.service;

import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import org.springframework.stereotype.Service;

@Service
public class VisitCounterService {

    private final Map<String, Integer> visitCounts = new ConcurrentHashMap<>();
    private final Map<String, Set<String>> uniqueVisits = new ConcurrentHashMap<>();

    /**
     * Увеличить счетчик посещений для указанного URL.
     */
    public void incrementVisitCount(String url) {
        visitCounts.compute(url, (key, value) -> value == null ? 1 : value + 1);
    }

    /**
     * Зарегистрировать уникального посетителя для указанного URL.
     */
    public void registerUniqueVisit(String url, String userIp) {
        uniqueVisits.computeIfAbsent(url, k -> ConcurrentHashMap.newKeySet()).add(userIp);
    }

    /**
     * Получить текущее количество посещений для указанного URL.
     */
    public int getVisitCount(String url) {
        return visitCounts.getOrDefault(url, 0);
    }

    /**
     * Получить общее количество посещений по всем URL.
     */
    public int getTotalVisitCount() {
        return visitCounts.values().stream().mapToInt(Integer::intValue).sum();
    }

    /**
     * Получить количество уникальных посетителей для указанного URL.
     */
    public int getUniqueVisitCount(String url) {
        return uniqueVisits.getOrDefault(url, ConcurrentHashMap.newKeySet()).size();
    }

    /**
     * Получить копию всех данных о посещениях.
     */
    public Map<String, Integer> getAllVisitCounts() {
        return new ConcurrentHashMap<>(visitCounts);
    }

    /**
     * Получить копию всех данных об уникальных посещениях.
     */
    public Map<String, Set<String>> getAllUniqueVisits() {
        return new ConcurrentHashMap<>(uniqueVisits);
    }

}
