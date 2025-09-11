package io.github.jspinak.brobot.runner.performance;

import java.io.*;
import java.nio.ByteBuffer;
import java.nio.channels.AsynchronousFileChannel;
import java.nio.channels.CompletionHandler;
import java.nio.charset.StandardCharsets;
import java.nio.file.*;
import java.util.*;
import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicLong;
import java.util.function.Consumer;
import java.util.zip.GZIPInputStream;
import java.util.zip.GZIPOutputStream;

import jakarta.annotation.PostConstruct;

import org.springframework.stereotype.Component;

import com.fasterxml.jackson.core.JsonFactory;
import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.core.JsonToken;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;

import lombok.Data;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Component
@RequiredArgsConstructor
@Data
public class OptimizedFileLoader {

    private final PerformanceProfiler profiler;
    private final MemoryOptimizer memoryOptimizer;

    // Jackson configuration for optimal performance
    private final ObjectMapper objectMapper =
            new ObjectMapper()
                    .configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false)
                    .configure(JsonParser.Feature.ALLOW_COMMENTS, true)
                    .configure(JsonParser.Feature.ALLOW_UNQUOTED_FIELD_NAMES, true);

    private final JsonFactory jsonFactory = new JsonFactory();

    // Thread pool for async file operations
    private final ExecutorService fileExecutor =
            Executors.newFixedThreadPool(
                    Math.max(2, Runtime.getRuntime().availableProcessors() / 2),
                    r -> {
                        Thread t = new Thread(r, "file-loader");
                        t.setDaemon(true);
                        return t;
                    });

    // File cache with compression
    private final Map<Path, CachedFile> fileCache = new ConcurrentHashMap<>();
    private final long maxCacheSize = 50 * 1024 * 1024; // 50MB
    private final AtomicLong currentCacheSize = new AtomicLong();

    @PostConstruct
    public void initialize() {
        log.info("Initialized optimized file loader");

        // Register with memory optimizer
        memoryOptimizer.registerReleasable(new FileCacheReleasable());
    }

    /** Load JSON file asynchronously with optimal performance. */
    public <T> CompletableFuture<T> loadJsonAsync(Path path, Class<T> type) {
        return CompletableFuture.supplyAsync(
                () -> {
                    try {
                        return loadJson(path, type);
                    } catch (IOException e) {
                        throw new CompletionException(e);
                    }
                },
                fileExecutor);
    }

    /** Load JSON file with caching and compression. */
    public <T> T loadJson(Path path, Class<T> type) throws IOException {
        try (var timer = profiler.startOperation("json-load-" + path.getFileName())) {
            // Check cache first
            CachedFile cached = fileCache.get(path);
            if (cached != null && cached.isValid(path)) {
                log.debug("Loading {} from cache", path);
                return deserializeFromCache(cached, type);
            }

            // Load and parse file
            T result = loadJsonOptimized(path, type);

            // Cache if beneficial
            cacheFileIfBeneficial(path, result);

            return result;
        }
    }

    /** Stream large JSON arrays efficiently. */
    public <T> void streamJsonArray(Path path, Class<T> elementType, Consumer<T> consumer)
            throws IOException {
        try (JsonParser parser = jsonFactory.createParser(path.toFile())) {

            // Move to array start
            while (parser.nextToken() != JsonToken.START_ARRAY) {
                // Skip to array
            }

            // Process each element
            while (parser.nextToken() != JsonToken.END_ARRAY) {
                T element = objectMapper.readValue(parser, elementType);
                consumer.accept(element);
            }
        }
    }

    /** Load file content asynchronously using NIO.2. */
    public CompletableFuture<String> loadTextAsync(Path path) {
        CompletableFuture<String> future = new CompletableFuture<>();

        try {
            AsynchronousFileChannel channel =
                    AsynchronousFileChannel.open(path, StandardOpenOption.READ);

            long size = Files.size(path);
            ByteBuffer buffer = ByteBuffer.allocate((int) size);

            channel.read(
                    buffer,
                    0,
                    buffer,
                    new CompletionHandler<Integer, ByteBuffer>() {
                        @Override
                        public void completed(Integer result, ByteBuffer attachment) {
                            try {
                                channel.close();
                                attachment.flip();
                                String content =
                                        StandardCharsets.UTF_8.decode(attachment).toString();
                                future.complete(content);
                            } catch (Exception e) {
                                future.completeExceptionally(e);
                            }
                        }

                        @Override
                        public void failed(Throwable exc, ByteBuffer attachment) {
                            try {
                                channel.close();
                            } catch (IOException e) {
                                // Ignore
                            }
                            future.completeExceptionally(exc);
                        }
                    });

        } catch (Exception e) {
            future.completeExceptionally(e);
        }

        return future;
    }

    /** Save JSON with compression for large files. */
    public <T> CompletableFuture<Void> saveJsonAsync(Path path, T object, boolean compress) {
        return CompletableFuture.runAsync(
                () -> {
                    try {
                        saveJson(path, object, compress);
                    } catch (IOException e) {
                        throw new CompletionException(e);
                    }
                },
                fileExecutor);
    }

    public <T> void saveJson(Path path, T object, boolean compress) throws IOException {
        try (var timer = profiler.startOperation("json-save-" + path.getFileName())) {
            Files.createDirectories(path.getParent());

            if (compress || shouldCompress(object)) {
                try (GZIPOutputStream gzipOut = new GZIPOutputStream(Files.newOutputStream(path))) {
                    objectMapper.writeValue(gzipOut, object);
                }
                log.debug("Saved compressed JSON to {}", path);
            } else {
                objectMapper.writeValue(path.toFile(), object);
                log.debug("Saved JSON to {}", path);
            }

            // Invalidate cache
            fileCache.remove(path);
        }
    }

    /** Batch load multiple files concurrently. */
    public <T> CompletableFuture<Map<Path, T>> loadMultipleAsync(List<Path> paths, Class<T> type) {

        Map<Path, CompletableFuture<T>> futures = new HashMap<>();

        for (Path path : paths) {
            futures.put(path, loadJsonAsync(path, type));
        }

        return CompletableFuture.allOf(futures.values().toArray(CompletableFuture[]::new))
                .thenApply(
                        v -> {
                            Map<Path, T> results = new HashMap<>();
                            futures.forEach(
                                    (path, future) -> {
                                        try {
                                            results.put(path, future.join());
                                        } catch (Exception e) {
                                            log.error("Failed to load {}", path, e);
                                        }
                                    });
                            return results;
                        });
    }

    private <T> T loadJsonOptimized(Path path, Class<T> type) throws IOException {
        // Check if file is compressed
        if (path.toString().endsWith(".gz") || isGzipped(path)) {
            try (GZIPInputStream gzipIn = new GZIPInputStream(Files.newInputStream(path))) {
                return objectMapper.readValue(gzipIn, type);
            }
        } else {
            // Use streaming for large files
            long fileSize = Files.size(path);
            if (fileSize > 1024 * 1024) { // 1MB
                return loadJsonStreaming(path, type);
            } else {
                return objectMapper.readValue(path.toFile(), type);
            }
        }
    }

    private <T> T loadJsonStreaming(Path path, Class<T> type) throws IOException {
        try (BufferedReader reader = Files.newBufferedReader(path);
                JsonParser parser = jsonFactory.createParser(reader)) {
            return objectMapper.readValue(parser, type);
        }
    }

    private boolean isGzipped(Path path) throws IOException {
        try (InputStream is = Files.newInputStream(path)) {
            byte[] signature = new byte[2];
            int read = is.read(signature);
            return read == 2 && signature[0] == (byte) 0x1f && signature[1] == (byte) 0x8b;
        }
    }

    private <T> void cacheFileIfBeneficial(Path path, T object) {
        try {
            // Estimate object size
            byte[] serialized = objectMapper.writeValueAsBytes(object);
            long size = serialized.length;

            // Cache if small enough and frequently accessed
            if (size < maxCacheSize / 10) { // Max 10% of cache for single file
                // Compress if beneficial
                byte[] data = size > 10240 ? compress(serialized) : serialized;

                CachedFile cached =
                        new CachedFile(
                                path,
                                data,
                                Files.getLastModifiedTime(path).toMillis(),
                                size != data.length);

                // Evict old entries if needed
                ensureCacheSpace(data.length);

                fileCache.put(path, cached);
                currentCacheSize.addAndGet(data.length);

                log.debug("Cached {} ({} bytes)", path, data.length);
            }
        } catch (Exception e) {
            log.debug("Failed to cache file {}", path, e);
        }
    }

    private void ensureCacheSpace(long requiredSpace) {
        while (currentCacheSize.get() + requiredSpace > maxCacheSize && !fileCache.isEmpty()) {
            // Remove oldest entry
            Map.Entry<Path, CachedFile> oldest =
                    fileCache.entrySet().stream()
                            .min(Comparator.comparingLong(e -> e.getValue().lastAccess))
                            .orElse(null);

            if (oldest != null) {
                fileCache.remove(oldest.getKey());
                currentCacheSize.addAndGet(-oldest.getValue().data.length);
            }
        }
    }

    private <T> T deserializeFromCache(CachedFile cached, Class<T> type) throws IOException {
        cached.lastAccess = System.currentTimeMillis();

        byte[] data = cached.compressed ? decompress(cached.data) : cached.data;
        return objectMapper.readValue(data, type);
    }

    private byte[] compress(byte[] data) throws IOException {
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        try (GZIPOutputStream gzipOut = new GZIPOutputStream(baos)) {
            gzipOut.write(data);
        }
        return baos.toByteArray();
    }

    private byte[] decompress(byte[] compressed) throws IOException {
        try (ByteArrayInputStream bais = new ByteArrayInputStream(compressed);
                GZIPInputStream gzipIn = new GZIPInputStream(bais);
                ByteArrayOutputStream baos = new ByteArrayOutputStream()) {

            byte[] buffer = new byte[4096];
            int n;
            while ((n = gzipIn.read(buffer)) > 0) {
                baos.write(buffer, 0, n);
            }
            return baos.toByteArray();
        }
    }

    private boolean shouldCompress(Object object) {
        // Estimate size and compress if large
        try {
            return objectMapper.writeValueAsBytes(object).length > 10240; // 10KB
        } catch (Exception e) {
            return false;
        }
    }

    private static class CachedFile {
        final Path path;
        final byte[] data;
        final long fileModified;
        final boolean compressed;
        volatile long lastAccess;

        CachedFile(Path path, byte[] data, long fileModified, boolean compressed) {
            this.path = path;
            this.data = data;
            this.fileModified = fileModified;
            this.compressed = compressed;
            this.lastAccess = System.currentTimeMillis();
        }

        boolean isValid(Path currentPath) {
            try {
                return Files.getLastModifiedTime(currentPath).toMillis() == fileModified;
            } catch (IOException e) {
                return false;
            }
        }
    }

    private class FileCacheReleasable implements MemoryOptimizer.MemoryReleasable {
        @Override
        public long releaseMemory() {
            long released = currentCacheSize.get();
            fileCache.clear();
            currentCacheSize.set(0);
            log.info("Released {} bytes from file cache", released);
            return released;
        }

        @Override
        public MemoryOptimizer.MemoryPriority getMemoryPriority() {
            return MemoryOptimizer.MemoryPriority.LOW;
        }
    }
}
