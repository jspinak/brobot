package io.github.jspinak.brobot.runner.cache;

import lombok.Data;

import io.github.jspinak.brobot.runner.events.EventBus;
import io.github.jspinak.brobot.runner.events.LogEvent;
import io.github.jspinak.brobot.runner.resources.ResourceManager;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Map;
import java.util.function.Function;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@Data
public class LRUCacheTest {

    @Mock
    private EventBus eventBus;

    @Mock
    private ResourceManager resourceManager;

    private LRUCache<String, String> cache;
    private final int MAX_SIZE = 3;

    @BeforeEach
    public void setup() {
        cache = new LRUCache<>(eventBus, "TestCache", MAX_SIZE, resourceManager);
        verify(resourceManager).registerResource(any(AutoCloseable.class), anyString());
    }

    @Test
    public void testPutAndGet() {
        // Put items in cache
        cache.put("key1", "value1");
        cache.put("key2", "value2");

        // Verify items are retrievable
        assertEquals("value1", cache.get("key1"));
        assertEquals("value2", cache.get("key2"));

        // Verify null handling
        assertNull(cache.get("nonexistent"));

        // Verify size
        assertEquals(2, cache.size());
    }

    @Test
    public void testComputeIfAbsent() {
        // Use computeIfAbsent to get or compute a value
        String value1 = cache.computeIfAbsent("key1", k -> "computed_" + k);

        // Verify value was computed and cached
        assertEquals("computed_key1", value1);
        assertEquals("computed_key1", cache.get("key1"));

        // Verify subsequent call returns cached value and doesn't recompute
        Function<String, String> supplier = spy(new Function<String, String>() {
            @Override
            public String apply(String s) {
                return "new_" + s;
            }
        });

        String value2 = cache.computeIfAbsent("key1", supplier);

        // Value should be the cached one, not recomputed
        assertEquals("computed_key1", value2);
        verify(supplier, never()).apply(anyString());

        // Verify computing a new value
        String value3 = cache.computeIfAbsent("key2", supplier);
        assertEquals("new_key2", value3);
        verify(supplier).apply("key2");
    }

    @Test
    public void testEviction() {
        // Fill cache to capacity
        cache.put("key1", "value1");
        cache.put("key2", "value2");
        cache.put("key3", "value3");

        // Verify all items are in cache
        assertEquals(3, cache.size());
        assertNotNull(cache.get("key1"));
        assertNotNull(cache.get("key2"));
        assertNotNull(cache.get("key3"));

        // Access key1 to make it most recently used
        cache.get("key1");

        // Add a new item to trigger eviction
        cache.put("key4", "value4");

        // Size should still be MAX_SIZE
        assertEquals(MAX_SIZE, cache.size());

        // key2 should be evicted (least recently used)
        assertNotNull(cache.get("key1")); // Recently accessed
        assertNull(cache.get("key2"));    // Should be evicted
        assertNotNull(cache.get("key3"));
        assertNotNull(cache.get("key4")); // Most recently added
    }

    @Test
    public void testInvalidate() {
        // Add items to cache
        cache.put("key1", "value1");
        cache.put("key2", "value2");

        // Invalidate one item
        cache.invalidate("key1");

        // Verify item was removed
        assertNull(cache.get("key1"));
        assertEquals("value2", cache.get("key2"));
        assertEquals(1, cache.size());
    }

    @Test
    public void testInvalidateAll() {
        // Add items to cache
        cache.put("key1", "value1");
        cache.put("key2", "value2");
        cache.put("key3", "value3");

        // Invalidate all items
        cache.invalidateAll();

        // Verify cache is empty
        assertEquals(0, cache.size());
        assertNull(cache.get("key1"));
        assertNull(cache.get("key2"));
        assertNull(cache.get("key3"));
    }

    @Test
    public void testGetStats() {
        // Add items and perform operations to generate stats
        cache.put("key1", "value1");
        cache.put("key2", "value2");

        // Generate hits
        cache.get("key1");
        cache.get("key1");
        cache.get("key2");

        // Generate misses
        cache.get("nonexistent1");
        cache.get("nonexistent2");

        // Get stats
        Map<String, Long> stats = cache.getStats();

        // Verify stats are correct
        assertEquals(3, stats.get("hits").longValue());
        assertEquals(2, stats.get("misses").longValue());
        assertEquals(2, stats.get("puts").longValue());
        assertEquals(2, stats.get("size").longValue());
        assertEquals(MAX_SIZE, stats.get("maxSize").longValue());
        assertEquals(60, stats.get("hitRatio").longValue()); // 3/(3+2) = 60%
    }

    @Test
    public void testClose() throws Exception {
        // Add items to cache
        cache.put("key1", "value1");
        cache.put("key2", "value2");

        // Close the cache
        cache.close();

        // Verify cache is empty
        assertEquals(0, cache.size());

        // Verify event was published
        verify(eventBus, atLeastOnce()).publish(any(LogEvent.class));
    }
}