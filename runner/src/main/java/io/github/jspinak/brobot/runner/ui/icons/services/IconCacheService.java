package io.github.jspinak.brobot.runner.ui.icons.services;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicLong;
import javafx.scene.image.Image;

import org.springframework.stereotype.Service;

import lombok.extern.slf4j.Slf4j;

/** Service for managing icon caching. Provides thread-safe cache operations with statistics. */
@Slf4j
@Service
public class IconCacheService {

    private final Map<String, Image> iconCache = new ConcurrentHashMap<>();
    private final Map<String, Long> accessCount = new ConcurrentHashMap<>();
    private final AtomicLong cacheHits = new AtomicLong(0);
    private final AtomicLong cacheMisses = new AtomicLong(0);

    /** Cache configuration. */
    public static class CacheConfiguration {
        private int maxSize = 1000;
        private boolean enableStatistics = true;
        private long evictionThreshold = 500; // Evict when this many items over max

        public static CacheConfigurationBuilder builder() {
            return new CacheConfigurationBuilder();
        }

        public static class CacheConfigurationBuilder {
            private CacheConfiguration config = new CacheConfiguration();

            public CacheConfigurationBuilder maxSize(int size) {
                config.maxSize = size;
                return this;
            }

            public CacheConfigurationBuilder enableStatistics(boolean enable) {
                config.enableStatistics = enable;
                return this;
            }

            public CacheConfigurationBuilder evictionThreshold(long threshold) {
                config.evictionThreshold = threshold;
                return this;
            }

            public CacheConfiguration build() {
                return config;
            }
        }
    }

    private CacheConfiguration configuration = CacheConfiguration.builder().build();

    /** Sets the cache configuration. */
    public void setConfiguration(CacheConfiguration configuration) {
        this.configuration = configuration;
        log.info("Icon cache configured with max size: {}", configuration.maxSize);
    }

    /** Gets an icon from cache. */
    public Image get(String key) {
        Image icon = iconCache.get(key);

        if (configuration.enableStatistics) {
            if (icon != null) {
                cacheHits.incrementAndGet();
                accessCount.merge(key, 1L, Long::sum);
            } else {
                cacheMisses.incrementAndGet();
            }
        }

        return icon;
    }

    /** Puts an icon in cache. */
    public void put(String key, Image icon) {
        if (icon == null) {
            log.warn("Attempted to cache null icon for key: {}", key);
            return;
        }

        // Check if we need to evict
        if (iconCache.size() >= configuration.maxSize + configuration.evictionThreshold) {
            evictLeastUsed();
        }

        iconCache.put(key, icon);
        if (configuration.enableStatistics) {
            accessCount.put(key, 0L);
        }

        log.trace("Cached icon: {}", key);
    }

    /** Checks if a key exists in cache. */
    public boolean contains(String key) {
        return iconCache.containsKey(key);
    }

    /** Removes an icon from cache. */
    public Image remove(String key) {
        accessCount.remove(key);
        return iconCache.remove(key);
    }

    /** Clears the entire cache. */
    public void clear() {
        iconCache.clear();
        accessCount.clear();
        cacheHits.set(0);
        cacheMisses.set(0);
        log.info("Icon cache cleared");
    }

    /** Gets the current cache size. */
    public int size() {
        return iconCache.size();
    }

    /** Gets cache statistics. */
    public CacheStatistics getStatistics() {
        return new CacheStatistics(
                iconCache.size(), cacheHits.get(), cacheMisses.get(), calculateHitRate());
    }

    /** Evicts least recently used items. */
    private void evictLeastUsed() {
        int toEvict = (int) configuration.evictionThreshold;

        accessCount.entrySet().stream()
                .sorted(Map.Entry.comparingByValue())
                .limit(toEvict)
                .map(Map.Entry::getKey)
                .forEach(
                        key -> {
                            iconCache.remove(key);
                            accessCount.remove(key);
                            log.trace("Evicted icon from cache: {}", key);
                        });

        log.debug("Evicted {} icons from cache", toEvict);
    }

    /** Calculates the cache hit rate. */
    private double calculateHitRate() {
        long hits = cacheHits.get();
        long misses = cacheMisses.get();
        long total = hits + misses;

        if (total == 0) {
            return 0.0;
        }

        return (double) hits / total;
    }

    /** Cache statistics. */
    public static class CacheStatistics {
        private final int size;
        private final long hits;
        private final long misses;
        private final double hitRate;

        public CacheStatistics(int size, long hits, long misses, double hitRate) {
            this.size = size;
            this.hits = hits;
            this.misses = misses;
            this.hitRate = hitRate;
        }

        public int getSize() {
            return size;
        }

        public long getHits() {
            return hits;
        }

        public long getMisses() {
            return misses;
        }

        public double getHitRate() {
            return hitRate;
        }

        @Override
        public String toString() {
            return String.format(
                    "CacheStatistics[size=%d, hits=%d, misses=%d, hitRate=%.2f%%]",
                    size, hits, misses, hitRate * 100);
        }
    }

    /** Generates a cache key for an icon. */
    public static String generateKey(String iconName, int size) {
        return iconName.toLowerCase() + "_" + size;
    }
}
