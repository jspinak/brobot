package io.github.jspinak.brobot.aspects.data;

import java.io.*;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicLong;
import java.util.zip.GZIPOutputStream;

import jakarta.annotation.PostConstruct;
import jakarta.annotation.PreDestroy;

import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.reflect.MethodSignature;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Component;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;

import io.github.jspinak.brobot.action.ActionResult;
import io.github.jspinak.brobot.action.ObjectCollection;
import io.github.jspinak.brobot.aspects.annotations.CollectData;
import io.github.jspinak.brobot.logging.BrobotLogger;
import io.github.jspinak.brobot.logging.LogCategory;
import io.github.jspinak.brobot.logging.LogLevel;
import io.github.jspinak.brobot.model.state.StateObject;

import lombok.Data;
import lombok.extern.slf4j.Slf4j;

/**
 * Aspect that automatically collects datasets for machine learning training.
 *
 * <p>This aspect intercepts methods annotated with @CollectData and captures: - Input parameters
 * and their features - Output results and success indicators - Screenshots before/after execution -
 * Timing and performance data - Execution context and metadata
 *
 * <p>The collected data is stored in a structured format suitable for training machine learning
 * models to improve automation accuracy.
 */
@Aspect
@Component
@Slf4j
@ConditionalOnProperty(
        prefix = "brobot.aspects.dataset",
        name = "enabled",
        havingValue = "true",
        matchIfMissing = false)
public class DatasetCollectionAspect {

    private final BrobotLogger brobotLogger;

    @Value("${brobot.aspects.dataset.output-dir:./ml-datasets}")
    private String outputDir;

    @Value("${brobot.aspects.dataset.batch-size:100}")
    private int batchSize;

    @Value("${brobot.aspects.dataset.max-queue-size:1000}")
    private int maxQueueSize;

    // Data collection infrastructure
    private final BlockingQueue<DataSample> dataQueue = new LinkedBlockingQueue<>();
    private final ConcurrentHashMap<String, AtomicInteger> categoryCounts =
            new ConcurrentHashMap<>();
    private final ConcurrentHashMap<String, AtomicLong> categorySizes = new ConcurrentHashMap<>();

    // Background processing
    private final ExecutorService processingExecutor = Executors.newFixedThreadPool(2);
    private final ScheduledExecutorService flushScheduler = Executors.newScheduledThreadPool(1);

    // JSON mapper for serialization
    private final ObjectMapper objectMapper =
            new ObjectMapper()
                    .enable(SerializationFeature.INDENT_OUTPUT)
                    .disable(SerializationFeature.FAIL_ON_EMPTY_BEANS);

    // Random for sampling
    private final Random random = new Random();

    @Autowired
    public DatasetCollectionAspect(BrobotLogger brobotLogger) {
        this.brobotLogger = brobotLogger;
    }

    @PostConstruct
    public void init() {
        log.info("Dataset Collection Aspect initialized with output directory: {}", outputDir);

        // Create output directory
        try {
            Files.createDirectories(Paths.get(outputDir));
        } catch (IOException e) {
            log.error("Failed to create dataset directory", e);
        }

        // Start background processors
        processingExecutor.submit(this::processDataQueue);
        flushScheduler.scheduleAtFixedRate(this::flushPendingData, 30, 30, TimeUnit.SECONDS);
    }

    @PreDestroy
    public void shutdown() {
        log.info("Shutting down Dataset Collection Aspect");
        flushPendingData();
        processingExecutor.shutdown();
        flushScheduler.shutdown();

        try {
            if (!processingExecutor.awaitTermination(10, TimeUnit.SECONDS)) {
                processingExecutor.shutdownNow();
            }
        } catch (InterruptedException e) {
            processingExecutor.shutdownNow();
        }
    }

    /** Intercept methods annotated with @CollectData */
    @Around("@annotation(collectData)")
    public Object collectDataset(ProceedingJoinPoint joinPoint, CollectData collectData)
            throws Throwable {
        // Check sampling rate
        if (collectData.samplingRate() < 1.0 && random.nextDouble() > collectData.samplingRate()) {
            return joinPoint.proceed();
        }

        // Check max samples limit
        String category = collectData.category();
        AtomicInteger count = categoryCounts.computeIfAbsent(category, k -> new AtomicInteger());
        if (collectData.maxSamples() > 0 && count.get() >= collectData.maxSamples()) {
            return joinPoint.proceed();
        }

        // Create data sample
        DataSample sample = new DataSample();
        sample.setId(UUID.randomUUID().toString());
        sample.setCategory(category);
        sample.setTimestamp(Instant.now());
        sample.setMethodName(joinPoint.getSignature().getName());

        // Capture pre-execution state
        capturePreExecutionData(joinPoint, collectData, sample);

        long startTime = System.currentTimeMillis();
        boolean success = false;
        Object result = null;
        Throwable error = null;

        try {
            // Execute method
            result = joinPoint.proceed();
            success = determineSuccess(result);

            // Capture post-execution state
            capturePostExecutionData(result, collectData, sample);

            return result;

        } catch (Throwable e) {
            error = e;
            throw e;

        } finally {
            // Finalize sample
            long duration = System.currentTimeMillis() - startTime;
            sample.setSuccess(success);
            sample.setDuration(duration);
            if (error != null) {
                sample.setError(error.getClass().getSimpleName() + ": " + error.getMessage());
            }

            // Apply labels
            if (collectData.labels().length > 0) {
                sample.setLabels(Arrays.asList(collectData.labels()));
            }

            // Queue for processing if it matches criteria
            if (!collectData.onlySuccess() || success) {
                queueDataSample(sample, collectData);
            }
        }
    }

    /** Capture pre-execution data */
    private void capturePreExecutionData(
            ProceedingJoinPoint joinPoint, CollectData collectData, DataSample sample) {
        Object[] args = joinPoint.getArgs();
        MethodSignature signature = (MethodSignature) joinPoint.getSignature();
        String[] paramNames = signature.getParameterNames();

        // Extract features from parameters
        Map<String, Object> features = new HashMap<>();

        for (int i = 0; i < args.length; i++) {
            Object arg = args[i];
            String paramName =
                    paramNames != null && i < paramNames.length ? paramNames[i] : "param" + i;

            if (arg instanceof ObjectCollection) {
                features.putAll(extractObjectCollectionFeatures((ObjectCollection) arg));
            } else if (arg instanceof StateObject) {
                features.putAll(extractStateObjectFeatures((StateObject) arg, paramName));
            } else if (arg != null && arg.getClass().getSimpleName().equals("Region")) {
                // Region features would be extracted here
            } else if (shouldIncludeFeature(paramName, collectData.features())) {
                features.put(paramName, sanitizeValue(arg));
            }
        }

        sample.setInputFeatures(features);

        // Capture screenshot if enabled
        if (collectData.captureScreenshots()) {
            sample.setPreScreenshot(captureScreenshot("pre_" + sample.getId()));
        }
    }

    /** Capture post-execution data */
    private void capturePostExecutionData(
            Object result, CollectData collectData, DataSample sample) {
        Map<String, Object> outputFeatures = new HashMap<>();

        if (result instanceof ActionResult) {
            ActionResult actionResult = (ActionResult) result;
            outputFeatures.put("success", actionResult.isSuccess());
            outputFeatures.put(
                    "matchCount",
                    actionResult.getMatchList() != null ? actionResult.getMatchList().size() : 0);
            outputFeatures.put("duration", actionResult.getDuration());

            // Extract match details if requested
            if (shouldIncludeFeature("matches", collectData.features())
                    && actionResult.getMatchList() != null) {
                List<Map<String, Object>> matches = new ArrayList<>();
                actionResult
                        .getMatchList()
                        .forEach(
                                match -> {
                                    Map<String, Object> matchData = new HashMap<>();
                                    matchData.put("score", match.getScore());
                                    // Match region features would be extracted here
                                    matchData.put("region", "region_data");
                                    matches.add(matchData);
                                });
                outputFeatures.put("matches", matches);
            }
        } else if (result != null) {
            outputFeatures.put("result", sanitizeValue(result));
        }

        sample.setOutputFeatures(outputFeatures);

        // Capture screenshot if enabled
        if (collectData.captureScreenshots()) {
            sample.setPostScreenshot(captureScreenshot("post_" + sample.getId()));
        }
    }

    /** Extract features from ObjectCollection */
    private Map<String, Object> extractObjectCollectionFeatures(ObjectCollection collection) {
        Map<String, Object> features = new HashMap<>();

        // Count different object types
        // Object count would be extracted from collection
        features.put("objectCount", "unknown");

        // Add more specific counts and features as needed
        // This is simplified - real implementation would extract more features

        return features;
    }

    /** Extract features from StateObject */
    private Map<String, Object> extractStateObjectFeatures(StateObject stateObject, String prefix) {
        Map<String, Object> features = new HashMap<>();

        features.put(prefix + "_name", stateObject.getName());
        // Search region would be extracted here
        features.put(prefix + "_searchRegion", "search_region_data");

        return features;
    }

    // Region feature extraction removed - Region class not available

    /** Check if feature should be included */
    private boolean shouldIncludeFeature(String featureName, String[] requestedFeatures) {
        if (requestedFeatures.length == 0) {
            return true; // Include all features
        }

        for (String requested : requestedFeatures) {
            if (requested.equals(featureName) || featureName.startsWith(requested + "_")) {
                return true;
            }
        }

        return false;
    }

    /** Sanitize value for storage */
    private Object sanitizeValue(Object value) {
        if (value == null) {
            return null;
        }

        // Handle primitive types and strings
        if (value instanceof Number || value instanceof Boolean || value instanceof String) {
            return value;
        }

        // Convert complex objects to string representation
        return value.toString();
    }

    /** Capture screenshot (placeholder) */
    private String captureScreenshot(String name) {
        // TODO: Implement actual screenshot capture when ScreenCapture is available
        return null;
    }

    /** Determine if execution was successful */
    private boolean determineSuccess(Object result) {
        if (result instanceof ActionResult) {
            return ((ActionResult) result).isSuccess();
        }
        // For other results, assume success if not null
        return result != null;
    }

    /** Queue data sample for processing */
    private void queueDataSample(DataSample sample, CollectData config) {
        if (dataQueue.size() >= maxQueueSize) {
            log.warn("Dataset queue full, dropping sample");
            return;
        }

        sample.setConfig(config);

        if (!dataQueue.offer(sample)) {
            log.warn("Failed to queue data sample");
        } else {
            categoryCounts.get(sample.getCategory()).incrementAndGet();
            logDataCollection(sample);
        }
    }

    /** Background data queue processor */
    private void processDataQueue() {
        List<DataSample> batch = new ArrayList<>();

        while (!Thread.currentThread().isInterrupted()) {
            try {
                // Collect batch
                DataSample sample = dataQueue.poll(1, TimeUnit.SECONDS);
                if (sample != null) {
                    batch.add(sample);
                }

                // Process batch when full or timeout
                if (batch.size() >= batchSize || (!batch.isEmpty() && sample == null)) {
                    processBatch(batch);
                    batch.clear();
                }

            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                break;
            } catch (Exception e) {
                log.error("Error processing data queue", e);
            }
        }
    }

    /** Process batch of samples */
    private void processBatch(List<DataSample> batch) {
        if (batch.isEmpty()) {
            return;
        }

        // Group by category
        Map<String, List<DataSample>> byCategory = new HashMap<>();
        for (DataSample sample : batch) {
            byCategory.computeIfAbsent(sample.getCategory(), k -> new ArrayList<>()).add(sample);
        }

        // Write each category
        byCategory.forEach(this::writeCategoryData);
    }

    /** Write data for a category */
    private void writeCategoryData(String category, List<DataSample> samples) {
        try {
            // Create category directory
            Path categoryDir = Paths.get(outputDir, category);
            Files.createDirectories(categoryDir);

            // Generate filename with timestamp
            String timestamp =
                    LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMdd_HHmmss"));
            String filename =
                    String.format(
                            "data_%s_%s.json",
                            timestamp, UUID.randomUUID().toString().substring(0, 8));

            // Determine if we should compress
            boolean compress = samples.get(0).getConfig().compress();
            if (compress) {
                filename += ".gz";
            }

            Path outputFile = categoryDir.resolve(filename);

            // Write data
            try (OutputStream os = Files.newOutputStream(outputFile);
                    OutputStream out = compress ? new GZIPOutputStream(os) : os;
                    OutputStreamWriter writer = new OutputStreamWriter(out)) {

                objectMapper.writeValue(writer, samples);

                // Update statistics
                long fileSize = Files.size(outputFile);
                categorySizes.computeIfAbsent(category, k -> new AtomicLong()).addAndGet(fileSize);

                log.info("Wrote {} samples to {} ({} bytes)", samples.size(), outputFile, fileSize);
            }

        } catch (IOException e) {
            log.error("Failed to write dataset for category {}", category, e);
        }
    }

    /** Flush pending data */
    private void flushPendingData() {
        List<DataSample> pending = new ArrayList<>();
        dataQueue.drainTo(pending);

        if (!pending.isEmpty()) {
            processBatch(pending);
            log.info("Flushed {} pending samples", pending.size());
        }
    }

    /** Log data collection event */
    private void logDataCollection(DataSample sample) {
        brobotLogger
                .builder(LogCategory.SYSTEM)
                .level(LogLevel.DEBUG)
                .action("DATASET_COLLECTED", sample.getCategory())
                .context("category", sample.getCategory())
                .context("sampleId", sample.getId())
                .context("method", sample.getMethodName())
                .context("success", sample.isSuccess())
                .message("Dataset sample collected")
                .log();
    }

    /** Get collection statistics */
    public Map<String, DatasetStats> getStatistics() {
        Map<String, DatasetStats> stats = new HashMap<>();

        categoryCounts.forEach(
                (category, count) -> {
                    DatasetStats categoryStats = new DatasetStats();
                    categoryStats.setCategory(category);
                    categoryStats.setSampleCount(count.get());
                    categoryStats.setTotalSize(
                            categorySizes.getOrDefault(category, new AtomicLong()).get());
                    stats.put(category, categoryStats);
                });

        return stats;
    }

    /** Inner class for data samples */
    @Data
    private static class DataSample {
        private String id;
        private String category;
        private Instant timestamp;
        private String methodName;
        private Map<String, Object> inputFeatures;
        private Map<String, Object> outputFeatures;
        private boolean success;
        private long duration;
        private String error;
        private List<String> labels;
        private String preScreenshot;
        private String postScreenshot;
        private transient CollectData config;
    }

    /** Dataset statistics */
    @Data
    public static class DatasetStats {
        private String category;
        private int sampleCount;
        private long totalSize;

        public double getAverageSampleSize() {
            return sampleCount > 0 ? (double) totalSize / sampleCount : 0;
        }
    }
}
