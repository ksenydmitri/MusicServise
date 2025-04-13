package music.service.service;

import static org.junit.jupiter.api.Assertions.*;

import music.service.config.CacheConfig;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Set;

@ExtendWith(MockitoExtension.class)
class CacheConfigTest {

    private CacheConfig cacheConfig;

    @BeforeEach
    void setUp() {
        cacheConfig = new CacheConfig();
    }

    @Test
    void testPutAndGet() {
        cacheConfig.put("key1", "value1");
        cacheConfig.put("key2", "value2");

        assertEquals("value1", cacheConfig.get("key1"));
        assertEquals("value2", cacheConfig.get("key2"));
    }

    @Test
    void testContainsKey() {
        cacheConfig.put("key1", "value1");
        assertTrue(cacheConfig.containsKey("key1"));
        assertFalse(cacheConfig.containsKey("key2"));
    }

    @Test
    void testClear() {
        cacheConfig.put("key1", "value1");
        cacheConfig.put("key2", "value2");

        cacheConfig.clear();

        assertFalse(cacheConfig.containsKey("key1"));
        assertFalse(cacheConfig.containsKey("key2"));
        assertTrue(cacheConfig.getCachedKeys().isEmpty());
    }

    @Test
    void testEvict() {
        cacheConfig.put("key1", "value1");
        cacheConfig.put("key2", "value2");

        cacheConfig.evict("key1");

        assertFalse(cacheConfig.containsKey("key1"));
        assertTrue(cacheConfig.containsKey("key2"));
    }

    @Test
    void testEvictNonexistentKey() {
        cacheConfig.put("key1", "value1");
        cacheConfig.evict("nonexistentKey");

        assertTrue(cacheConfig.containsKey("key1"));
    }

    @Test
    void testEvictionPolicy() {
        for (int i = 0; i < 101; i++) {
            cacheConfig.put("key" + i, "value" + i);
        }

        assertFalse(cacheConfig.containsKey("key0")); // Первый элемент должен быть удалён
        assertTrue(cacheConfig.containsKey("key100")); // Последний элемент должен быть в кэше
    }

    @Test
    void testEvictByPattern() {
        cacheConfig.put("test1", "value1");
        cacheConfig.put("test2", "value2");
        cacheConfig.put("sample1", "value3");

        cacheConfig.evictByPattern("test*");

        assertFalse(cacheConfig.containsKey("test1"));
        assertFalse(cacheConfig.containsKey("test2"));
        assertTrue(cacheConfig.containsKey("sample1"));
    }

    @Test
    void testGetCachedKeys() {
        cacheConfig.put("key1", "value1");
        cacheConfig.put("key2", "value2");

        Set<String> keys = cacheConfig.getCachedKeys();

        assertTrue(keys.contains("key1"));
        assertTrue(keys.contains("key2"));
        assertEquals(2, keys.size());
    }

    @Test
    void testUpdate() {
        cacheConfig.put("key1", "value1");
        cacheConfig.update("key1", "newValue");

        assertEquals("newValue", cacheConfig.get("key1"));
    }

    @Test
    void testRemoveEldestEntry() {
        for (int i = 0; i <= 100; i++) {
            cacheConfig.put("key" + i, "value" + i);
        }

        assertFalse(cacheConfig.containsKey("key0")); // Старый элемент удалён
        assertTrue(cacheConfig.containsKey("key100")); // Новый элемент добавлен
        assertEquals(100, cacheConfig.getCachedKeys().size()); // Размер не превышает MAX_CACHE_SIZE
    }
}