package io.github.jspinak.brobot.runner.ui.icons.services;

import javafx.scene.image.Image;
import javafx.scene.image.WritableImage;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.DisplayName;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Unit tests for IconCacheService.
 */
class IconCacheServiceTest {
    
    private IconCacheService cacheService;
    
    @BeforeEach
    void setUp() {
        cacheService = new IconCacheService();
    }
    
    @Test
    @DisplayName("Should cache and retrieve icons")
    void testBasicCaching() {
        // Given
        String key = "test_icon_24";
        Image icon = new WritableImage(24, 24);
        
        // When
        cacheService.put(key, icon);
        Image retrieved = cacheService.get(key);
        
        // Then
        assertNotNull(retrieved);
        assertEquals(icon, retrieved);
        assertTrue(cacheService.contains(key));
        assertEquals(1, cacheService.size());
    }
    
    @Test
    @DisplayName("Should return null for non-existent keys")
    void testCacheMiss() {
        // When
        Image result = cacheService.get("non_existent");
        
        // Then
        assertNull(result);
        assertFalse(cacheService.contains("non_existent"));
    }
    
    @Test
    @DisplayName("Should track cache statistics")
    void testCacheStatistics() {
        // Given
        cacheService.setConfiguration(
            IconCacheService.CacheConfiguration.builder()
                .enableStatistics(true)
                .build()
        );
        
        String key = "stats_test_24";
        Image icon = new WritableImage(24, 24);
        cacheService.put(key, icon);
        
        // When
        cacheService.get(key); // Hit
        cacheService.get(key); // Hit
        cacheService.get("missing"); // Miss
        
        // Then
        IconCacheService.CacheStatistics stats = cacheService.getStatistics();
        assertEquals(1, stats.getSize());
        assertEquals(2, stats.getHits());
        assertEquals(1, stats.getMisses());
        assertEquals(2.0 / 3.0, stats.getHitRate(), 0.001);
    }
    
    @Test
    @DisplayName("Should evict least used items when over capacity")
    void testEviction() {
        // Given
        cacheService.setConfiguration(
            IconCacheService.CacheConfiguration.builder()
                .maxSize(5)
                .evictionThreshold(2)
                .enableStatistics(true)
                .build()
        );
        
        // When - Add items up to eviction threshold
        for (int i = 0; i < 7; i++) {
            cacheService.put("icon_" + i, new WritableImage(16, 16));
            // Access some items to increase their usage count
            if (i >= 5) {
                cacheService.get("icon_" + i);
            }
        }
        
        // Then - Should have evicted 2 least used items
        assertEquals(5, cacheService.size());
        // Items 5 and 6 should still be there (they were accessed)
        assertNotNull(cacheService.get("icon_5"));
        assertNotNull(cacheService.get("icon_6"));
        // Early items might have been evicted
        assertTrue(
            cacheService.get("icon_0") == null || 
            cacheService.get("icon_1") == null
        );
    }
    
    @Test
    @DisplayName("Should handle null icon gracefully")
    void testNullIcon() {
        // When
        cacheService.put("null_icon", null);
        
        // Then
        assertEquals(0, cacheService.size());
        assertNull(cacheService.get("null_icon"));
    }
    
    @Test
    @DisplayName("Should clear cache completely")
    void testClearCache() {
        // Given
        for (int i = 0; i < 10; i++) {
            cacheService.put("icon_" + i, new WritableImage(16, 16));
        }
        assertEquals(10, cacheService.size());
        
        // When
        cacheService.clear();
        
        // Then
        assertEquals(0, cacheService.size());
        assertEquals(0, cacheService.getStatistics().getHits());
        assertEquals(0, cacheService.getStatistics().getMisses());
    }
    
    @Test
    @DisplayName("Should remove specific items from cache")
    void testRemove() {
        // Given
        String key = "removable_icon";
        Image icon = new WritableImage(32, 32);
        cacheService.put(key, icon);
        
        // When
        Image removed = cacheService.remove(key);
        
        // Then
        assertEquals(icon, removed);
        assertNull(cacheService.get(key));
        assertEquals(0, cacheService.size());
    }
    
    @Test
    @DisplayName("Should generate proper cache keys")
    void testKeyGeneration() {
        // When
        String key1 = IconCacheService.generateKey("Settings", 24);
        String key2 = IconCacheService.generateKey("SETTINGS", 24);
        String key3 = IconCacheService.generateKey("settings", 32);
        
        // Then
        assertEquals("settings_24", key1);
        assertEquals("settings_24", key2); // Case insensitive
        assertEquals("settings_32", key3);
    }
    
    @Test
    @DisplayName("Should be thread-safe")
    void testThreadSafety() throws InterruptedException {
        // Given
        int threadCount = 10;
        int operationsPerThread = 100;
        ExecutorService executor = Executors.newFixedThreadPool(threadCount);
        CountDownLatch latch = new CountDownLatch(threadCount);
        List<Exception> exceptions = new ArrayList<>();
        
        // When
        for (int t = 0; t < threadCount; t++) {
            final int threadId = t;
            executor.submit(() -> {
                try {
                    for (int i = 0; i < operationsPerThread; i++) {
                        String key = "thread_" + threadId + "_icon_" + i;
                        cacheService.put(key, new WritableImage(16, 16));
                        cacheService.get(key);
                        if (i % 10 == 0) {
                            cacheService.remove(key);
                        }
                    }
                } catch (Exception e) {
                    exceptions.add(e);
                } finally {
                    latch.countDown();
                }
            });
        }
        
        // Then
        assertTrue(latch.await(5, TimeUnit.SECONDS));
        assertTrue(exceptions.isEmpty());
        executor.shutdown();
    }
    
    @Test
    @DisplayName("Should format statistics correctly")
    void testStatisticsToString() {
        // Given
        IconCacheService.CacheStatistics stats = 
            new IconCacheService.CacheStatistics(10, 75, 25, 0.75);
        
        // When
        String result = stats.toString();
        
        // Then
        assertEquals("CacheStatistics[size=10, hits=75, misses=25, hitRate=75.00%]", result);
    }
}