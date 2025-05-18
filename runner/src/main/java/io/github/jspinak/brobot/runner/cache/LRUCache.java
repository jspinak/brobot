package io.github.jspinak.brobot.runner.cache;

import io.github.jspinak.brobot.runner.events.EventBus;
import io.github.jspinak.brobot.runner.events.LogEvent;
import io.github.jspinak.brobot.runner.resources.ResourceManager;

import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.concurrent.atomic.AtomicLong;
import java.util.concurrent.locks.ReadWriteLock;
import java.util.concurrent.locks.ReentrantReadWriteLock;

/**
 * A generic cache implementation using LRU (Least Recently Used) policy
 */
public class LRUCache<K, V> implements AutoCloseable {
    private final EventBus eventBus;
    private final String cacheName;
    private final ResourceManager resourceManager;
    private final int maxSize;
    private final ReadWriteLock lock = new ReentrantReadWriteLock();

    // Stats
    private final AtomicLong hits = new AtomicLong(0);
    private final AtomicLong misses = new AtomicLong(0);
    private final AtomicLong puts = new AtomicLong(0);

    // The cache is implemented using a LinkedHashMap with access-order
    private final Map<K, V> cache;

    public LRUCache(EventBus eventBus, String cacheName, int maxSize, ResourceManager resourceManager) {
        this.eventBus = eventBus;
        this.cacheName = cacheName;
        this.maxSize = maxSize;
        this.resourceManager = resourceManager;

        // Create LRU cache using LinkedHashMap with access order
        this.cache = Collections.synchronizedMap(new LinkedHashMap<K, V>(maxSize + 1, 0.75f, true) {
            @Override
            protected boolean removeEldestEntry(Map.Entry<K, V> eldest) {
                boolean shouldRemove = size() > maxSize;
                if (shouldRemove) {
                    eventBus.publish(LogEvent.debug(LRUCache.this,
                            cacheName + " cache evicting entry: " + eldest.getKey(), "Cache"));
                }
                return shouldRemove;
            }
        });

        resourceManager.registerResource(this, cacheName + " Cache");
    }

    /**
     * Gets a value from the cache
     * @param key The key to look up
     * @return The value, or null if not found
     */
    public V get(K key) {
        if (key == null) return null;

        lock.readLock().lock();
        try {
            V value = cache.get(key);
            if (value != null) {
                hits.incrementAndGet();
                eventBus.publish(LogEvent.debug(this,
                        cacheName + " cache hit for: " + key, "Cache"));
            } else {
                misses.incrementAndGet();
                eventBus.publish(LogEvent.debug(this,
                        cacheName + " cache miss for: " + key, "Cache"));
            }
            return value;
        } finally {
            lock.readLock().unlock();
        }
    }

    /**
     * Stores a value in the cache
     * @param key The key
     * @param value The value
     */
    public void put(K key, V value) {
        if (key == null || value == null) return;

        lock.writeLock().lock();
        try {
            cache.put(key, value);
            puts.incrementAndGet();
            eventBus.publish(LogEvent.debug(this,
                    cacheName + " cache stored: " + key, "Cache"));
        } finally {
            lock.writeLock().unlock();
        }
    }

    /**
     * Gets a value if present, otherwise computes and stores it
     * @param key The key
     * @param supplier Function to compute the value if not present
     * @return The value
     */
    public V computeIfAbsent(K key, java.util.function.Function<K, V> supplier) {
        if (key == null) return null;

        V value = get(key);
        if (value != null) {
            return value;
        }

        // Not in cache, compute new value
        lock.writeLock().lock();
        try {
            // Check again in case another thread added it
            value = cache.get(key);
            if (value != null) {
                hits.incrementAndGet();
                return value;
            }

            // Compute and cache the value
            value = supplier.apply(key);
            if (value != null) {
                cache.put(key, value);
                puts.incrementAndGet();
                eventBus.publish(LogEvent.debug(this,
                        cacheName + " cache computed and stored: " + key, "Cache"));
            }
            return value;
        } finally {
            lock.writeLock().unlock();
        }
    }

    /**
     * Removes a value from the cache
     * @param key The key to remove
     */
    public void invalidate(K key) {
        if (key == null) return;

        lock.writeLock().lock();
        try {
            V removed = cache.remove(key);
            if (removed != null) {
                eventBus.publish(LogEvent.debug(this,
                        cacheName + " cache invalidated: " + key, "Cache"));
            }
        } finally {
            lock.writeLock().unlock();
        }
    }

    /**
     * Clears the entire cache
     */
    public void invalidateAll() {
        lock.writeLock().lock();
        try {
            int size = cache.size();
            cache.clear();
            eventBus.publish(LogEvent.info(this,
                    cacheName + " cache completely invalidated (" + size + " entries)", "Cache"));
        } finally {
            lock.writeLock().unlock();
        }
    }

    /**
     * Gets the number of entries in the cache
     */
    public int size() {
        lock.readLock().lock();
        try {
            return cache.size();
        } finally {
            lock.readLock().unlock();
        }
    }

    /**
     * Gets the cache statistics
     */
    public Map<String, Long> getStats() {
        Map<String, Long> stats = new HashMap<>();
        stats.put("hits", hits.get());
        stats.put("misses", misses.get());
        stats.put("puts", puts.get());
        stats.put("size", (long) size());
        stats.put("maxSize", (long) maxSize);
        stats.put("hitRatio", calculateHitRatio());
        return stats;
    }

    private long calculateHitRatio() {
        long totalRequests = hits.get() + misses.get();
        if (totalRequests == 0) return 0;
        return (hits.get() * 100) / totalRequests;
    }

    @Override
    public void close() {
        invalidateAll();
        eventBus.publish(LogEvent.info(this,
                cacheName + " cache closed (hits: " + hits.get() +
                        ", misses: " + misses.get() +
                        ", hit ratio: " + calculateHitRatio() + "%)",
                "Resources"));
    }
}