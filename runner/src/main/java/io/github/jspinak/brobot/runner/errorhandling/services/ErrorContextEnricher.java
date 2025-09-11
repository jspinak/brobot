package io.github.jspinak.brobot.runner.errorhandling.services;

import java.lang.management.ManagementFactory;
import java.lang.management.MemoryMXBean;
import java.lang.management.ThreadMXBean;
import java.net.InetAddress;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;

import org.springframework.stereotype.Service;

import io.github.jspinak.brobot.runner.errorhandling.ErrorContext;

import lombok.extern.slf4j.Slf4j;

/**
 * Service responsible for enriching error context with system state information. Adds runtime
 * metrics, system properties, and environmental data to error contexts.
 */
@Slf4j
@Service
public class ErrorContextEnricher {

    private final Runtime runtime = Runtime.getRuntime();
    private final MemoryMXBean memoryMXBean = ManagementFactory.getMemoryMXBean();
    private final ThreadMXBean threadMXBean = ManagementFactory.getThreadMXBean();

    /** Enriches an error context with current system state. */
    public ErrorContext enrich(ErrorContext context) {
        if (context == null) {
            return null;
        }

        try {
            return ErrorContext.builder()
                    // Copy existing context
                    .errorId(context.getErrorId())
                    .timestamp(context.getTimestamp())
                    .operation(context.getOperation())
                    .component(context.getComponent())
                    .userId(context.getUserId())
                    .sessionId(context.getSessionId())
                    .additionalData(
                            mergeAdditionalData(context.getAdditionalData(), collectSystemData()))
                    .category(context.getCategory())
                    .severity(context.getSeverity())
                    .recoverable(context.isRecoverable())
                    .recoveryHint(context.getRecoveryHint())
                    // Add system state
                    .memoryUsed(getUsedMemory())
                    .activeThreads(threadMXBean.getThreadCount())
                    .cpuUsage(getCpuUsage())
                    .build();
        } catch (Exception e) {
            log.error("Failed to enrich error context", e);
            return context; // Return original context if enrichment fails
        }
    }

    /** Creates a minimal error context with system enrichment. */
    public ErrorContext createEnrichedContext(
            String operation, ErrorContext.ErrorCategory category) {
        return enrich(ErrorContext.minimal(operation, category));
    }

    /** Collects current system data. */
    private Map<String, Object> collectSystemData() {
        Map<String, Object> systemData = new HashMap<>();

        try {
            // Memory information
            systemData.put("heap.used", getUsedHeapMemory());
            systemData.put("heap.max", runtime.maxMemory());
            systemData.put("memory.free", runtime.freeMemory());

            // Thread information
            systemData.put("threads.active", threadMXBean.getThreadCount());
            systemData.put("threads.peak", threadMXBean.getPeakThreadCount());
            systemData.put("threads.daemon", threadMXBean.getDaemonThreadCount());

            // System properties
            systemData.put("os.name", System.getProperty("os.name"));
            systemData.put("os.version", System.getProperty("os.version"));
            systemData.put("java.version", System.getProperty("java.version"));
            systemData.put("java.vendor", System.getProperty("java.vendor"));

            // Host information
            try {
                InetAddress localhost = InetAddress.getLocalHost();
                systemData.put("host.name", localhost.getHostName());
                systemData.put("host.address", localhost.getHostAddress());
            } catch (Exception e) {
                log.trace("Failed to get host information", e);
            }

            // Timestamp
            systemData.put("enriched.at", LocalDateTime.now().toString());

        } catch (Exception e) {
            log.warn("Error collecting system data", e);
        }

        return systemData;
    }

    /** Gets the current CPU usage. */
    private double getCpuUsage() {
        try {
            com.sun.management.OperatingSystemMXBean osBean =
                    (com.sun.management.OperatingSystemMXBean)
                            ManagementFactory.getOperatingSystemMXBean();
            return osBean.getProcessCpuLoad();
        } catch (Exception e) {
            log.trace("Failed to get CPU usage", e);
            return -1;
        }
    }

    /** Gets the used memory in bytes. */
    private long getUsedMemory() {
        return runtime.totalMemory() - runtime.freeMemory();
    }

    /** Gets the used heap memory in bytes. */
    private long getUsedHeapMemory() {
        return memoryMXBean.getHeapMemoryUsage().getUsed();
    }

    /** Merges existing additional data with new system data. */
    private Map<String, Object> mergeAdditionalData(
            Map<String, Object> existing, Map<String, Object> systemData) {
        Map<String, Object> merged = new HashMap<>();

        // Add existing data first
        if (existing != null) {
            merged.putAll(existing);
        }

        // Add system data under "system" key to avoid conflicts
        merged.put("system", systemData);

        return merged;
    }

    /** Categorizes an error based on its type. */
    public ErrorContext.ErrorCategory categorizeError(Throwable error) {
        if (error == null) {
            return ErrorContext.ErrorCategory.UNKNOWN;
        }

        String className = error.getClass().getSimpleName().toLowerCase();
        String message = error.getMessage() != null ? error.getMessage().toLowerCase() : "";

        // Check class name patterns
        if (className.contains("file")
                || className.contains("io")
                || message.contains("file")
                || message.contains("directory")) {
            return ErrorContext.ErrorCategory.FILE_IO;
        } else if (className.contains("network")
                || className.contains("connection")
                || className.contains("socket")
                || message.contains("connection")) {
            return ErrorContext.ErrorCategory.NETWORK;
        } else if (className.contains("database")
                || className.contains("sql")
                || className.contains("jdbc")
                || message.contains("database")) {
            return ErrorContext.ErrorCategory.DATABASE;
        } else if (className.contains("validation")
                || className.contains("invalid")
                || className.contains("illegal")
                || message.contains("invalid")) {
            return ErrorContext.ErrorCategory.VALIDATION;
        } else if (className.contains("auth")
                || className.contains("permission")
                || className.contains("access")
                || message.contains("denied")) {
            return ErrorContext.ErrorCategory.AUTHORIZATION;
        } else if (className.contains("config")
                || className.contains("property")
                || message.contains("configuration")) {
            return ErrorContext.ErrorCategory.CONFIGURATION;
        } else if (className.contains("timeout") || message.contains("timeout")) {
            return ErrorContext.ErrorCategory.NETWORK; // Timeouts are usually network-related
        }

        return ErrorContext.ErrorCategory.UNKNOWN;
    }

    /** Determines severity based on error type and context. */
    public ErrorContext.ErrorSeverity determineSeverity(Throwable error, ErrorContext context) {
        // Critical errors
        if (error instanceof OutOfMemoryError
                || error instanceof StackOverflowError
                || error instanceof ThreadDeath) {
            return ErrorContext.ErrorSeverity.CRITICAL;
        }

        // High severity based on category
        if (context.getCategory() == ErrorContext.ErrorCategory.DATABASE
                || context.getCategory() == ErrorContext.ErrorCategory.AUTHORIZATION) {
            return ErrorContext.ErrorSeverity.HIGH;
        }

        // Medium severity for I/O and network
        if (context.getCategory() == ErrorContext.ErrorCategory.FILE_IO
                || context.getCategory() == ErrorContext.ErrorCategory.NETWORK) {
            return ErrorContext.ErrorSeverity.MEDIUM;
        }

        // Default to LOW for everything else
        return ErrorContext.ErrorSeverity.LOW;
    }
}
