package io.github.jspinak.brobot.runner.cache;

import lombok.Data;

import io.github.jspinak.brobot.model.element.Pattern;
import io.github.jspinak.brobot.action.ActionResult;
import io.github.jspinak.brobot.model.state.State;
import io.github.jspinak.brobot.model.state.StateImage;
import io.github.jspinak.brobot.runner.events.EventBus;
import io.github.jspinak.brobot.runner.resources.ResourceManager;
import jakarta.annotation.PostConstruct;
import jakarta.annotation.PreDestroy;
import org.springframework.stereotype.Component;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Factory and manager for different cache types in the application
 */
@Component
@Data
public class CacheManager {
    private final EventBus eventBus;
    private final ResourceManager resourceManager;

    // Cache instances for different object types
    private final Map<String, LRUCache<?, ?>> caches = new ConcurrentHashMap<>();
    private LRUCache<String, State> stateCache;
    private LRUCache<String, StateImage> stateImageCache;
    private LRUCache<String, Pattern> patternCache;
    private LRUCache<String, ActionResult> matchesCache;

    public CacheManager(EventBus eventBus, ResourceManager resourceManager) {
        this.eventBus = eventBus;
        this.resourceManager = resourceManager;
    }

    @PostConstruct
    public void initialize() {
        // Initialize standard caches
        stateCache = createCache("State", 50);
        stateImageCache = createCache("StateImage", 100);
        patternCache = createCache("Pattern", 200);
        matchesCache = createCache("Matches", 20);

        // Register caches
        registerCache("state", stateCache);
        registerCache("stateImage", stateImageCache);
        registerCache("pattern", patternCache);
        registerCache("matches", matchesCache);
    }

    /**
     * Creates a new LRU cache with the specified name and size
     * 
     * @param name The cache name
     * @param maxSize Maximum number of entries
     * @return New LRU cache instance
     * @throws IllegalArgumentException if name is null or maxSize is invalid
     */
    public <K, V> LRUCache<K, V> createCache(String name, int maxSize) {
        if (name == null || name.trim().isEmpty()) {
            throw new IllegalArgumentException("Cache name cannot be null or empty");
        }
        if (maxSize <= 0) {
            throw new IllegalArgumentException("Cache max size must be positive");
        }
        return new LRUCache<>(eventBus, name, maxSize, resourceManager);
    }

    /**
     * Registers a cache for management
     * 
     * @param key The cache key
     * @param cache The cache instance
     * @throws IllegalArgumentException if key or cache is null
     */
    public <K, V> void registerCache(String key, LRUCache<K, V> cache) {
        if (key == null || key.trim().isEmpty()) {
            throw new IllegalArgumentException("Cache key cannot be null or empty");
        }
        if (cache == null) {
            throw new IllegalArgumentException("Cache cannot be null");
        }
        caches.put(key, cache);
    }

    /**
     * Gets the state cache
     */
    public LRUCache<String, State> getStateCache() {
        return stateCache;
    }

    /**
     * Gets the state image cache
     */
    public LRUCache<String, StateImage> getStateImageCache() {
        return stateImageCache;
    }

    /**
     * Gets the pattern cache
     */
    public LRUCache<String, Pattern> getPatternCache() {
        return patternCache;
    }

    /**
     * Gets the matches cache
     */
    public LRUCache<String, ActionResult> getMatchesCache() {
        return matchesCache;
    }

    /**
     * Clears all caches
     */
    public void clearAllCaches() {
        caches.values().stream()
            .filter(cache -> cache != null)
            .forEach(LRUCache::invalidateAll);
    }

    /**
     * Gets statistics for all caches
     * 
     * @return Map of cache names to their statistics
     */
    public Map<String, Map<String, Long>> getAllCacheStats() {
        Map<String, Map<String, Long>> stats = new ConcurrentHashMap<>();
        caches.forEach((name, cache) -> {
            if (cache != null) {
                stats.put(name, cache.getStats());
            }
        });
        return stats;
    }

    @PreDestroy
    public void shutdown() {
        clearAllCaches();
    }
}