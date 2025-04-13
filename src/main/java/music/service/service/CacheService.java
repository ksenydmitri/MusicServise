package music.service.service;

import java.util.Set;

public interface CacheService {
    Object get(String key);
    void put(String key, Object value);
    void clear();
    void evict(String key);
    boolean containsKey(String key);
    void evictByPattern(String pattern);
    Set<String> getCachedKeys();
    void update(String key, Object value);

}