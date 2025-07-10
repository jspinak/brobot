package io.github.jspinak.brobot.runner.errorhandling.enrichment;

import io.github.jspinak.brobot.runner.common.diagnostics.DiagnosticCapable;
import io.github.jspinak.brobot.runner.common.diagnostics.DiagnosticInfo;
import io.github.jspinak.brobot.runner.errorhandling.ErrorContext;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.lang.management.ManagementFactory;
import java.lang.management.MemoryMXBean;
import java.lang.management.ThreadMXBean;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicLong;

/**
 * Service responsible for enriching error context with system state information.
 * 
 * This service adds runtime information such as memory usage, CPU load,
 * thread count, and other system metrics to error contexts. It also handles
 * error categorization and ID generation.
 * 
 * Thread Safety: This class is thread-safe.
 * 
 * @since 1.0.0
 */
@Slf4j
@Service
public class ErrorEnrichmentService implements DiagnosticCapable {
    
    private final MemoryMXBean memoryMXBean = ManagementFactory.getMemoryMXBean();
    private final ThreadMXBean threadMXBean = ManagementFactory.getThreadMXBean();
    
    // Statistics
    private final AtomicLong totalEnrichments = new AtomicLong(0);
    private final Map<ErrorContext.ErrorCategory, AtomicLong> categoryCounts = new ConcurrentHashMap<>();
    
    // Diagnostic mode
    private final AtomicBoolean diagnosticMode = new AtomicBoolean(false);
    
    /**
     * Enrich an error context with system state information.
     * 
     * @param context the context to enrich
     * @return enriched context with system state
     */
    public ErrorContext enrichContext(ErrorContext context) {
        if (context == null) {
            throw new IllegalArgumentException("Context cannot be null");
        }
        
        totalEnrichments.incrementAndGet();
        
        // Capture current system state
        SystemState systemState = captureSystemState();
        
        // Build enriched context
        ErrorContext enriched = ErrorContext.builder()
                .errorId(context.getErrorId() != null ? context.getErrorId() : generateErrorId())
                .timestamp(context.getTimestamp())
                .operation(context.getOperation())
                .component(context.getComponent())
                .userId(context.getUserId())
                .sessionId(context.getSessionId())
                .additionalData(context.getAdditionalData())
                .category(context.getCategory())
                .severity(context.getSeverity())
                .recoverable(context.isRecoverable())
                .recoveryHint(context.getRecoveryHint())
                .memoryUsed(systemState.getMemoryUsed())
                .activeThreads(systemState.getActiveThreads())
                .cpuUsage(systemState.getCpuUsage())
                .build();
        
        // Track category
        categoryCounts.computeIfAbsent(enriched.getCategory(), k -> new AtomicLong())
                .incrementAndGet();
        
        if (diagnosticMode.get()) {
            log.info("[DIAGNOSTIC] Enriched context {} - Memory: {}MB, Threads: {}, CPU: {}%",
                    enriched.getErrorId(),
                    enriched.getMemoryUsed() / (1024 * 1024),
                    enriched.getActiveThreads(),
                    String.format("%.2f", enriched.getCpuUsage() * 100));
        }
        
        return enriched;
    }
    
    /**
     * Categorize an error based on its type and characteristics.
     * 
     * @param error the error to categorize
     * @return the error category
     */
    public ErrorContext.ErrorCategory categorizeError(Throwable error) {
        if (error == null) {
            return ErrorContext.ErrorCategory.UNKNOWN;
        }
        
        String errorName = error.getClass().getSimpleName().toLowerCase();
        String message = error.getMessage() != null ? error.getMessage().toLowerCase() : "";
        
        // Check error class name and message for categorization hints
        if (errorName.contains("file") || errorName.contains("io") || 
            message.contains("file") || message.contains("directory")) {
            return ErrorContext.ErrorCategory.FILE_IO;
        } else if (errorName.contains("network") || errorName.contains("connection") ||
                   message.contains("network") || message.contains("connection")) {
            return ErrorContext.ErrorCategory.NETWORK;
        } else if (errorName.contains("database") || errorName.contains("sql") ||
                   message.contains("database") || message.contains("sql")) {
            return ErrorContext.ErrorCategory.DATABASE;
        } else if (errorName.contains("validation") || errorName.contains("invalid") ||
                   message.contains("validation") || message.contains("invalid")) {
            return ErrorContext.ErrorCategory.VALIDATION;
        } else if (errorName.contains("auth") || errorName.contains("permission") ||
                   message.contains("auth") || message.contains("permission")) {
            return ErrorContext.ErrorCategory.AUTHORIZATION;
        } else if (errorName.contains("config") || errorName.contains("property") ||
                   message.contains("config") || message.contains("property")) {
            return ErrorContext.ErrorCategory.CONFIGURATION;
        }
        
        return ErrorContext.ErrorCategory.UNKNOWN;
    }
    
    /**
     * Capture current system state.
     * 
     * @return current system state
     */
    public SystemState captureSystemState() {
        Runtime runtime = Runtime.getRuntime();
        
        SystemState state = new SystemState();
        state.setMemoryUsed(runtime.totalMemory() - runtime.freeMemory());
        state.setMemoryTotal(runtime.totalMemory());
        state.setMemoryMax(runtime.maxMemory());
        state.setActiveThreads(threadMXBean.getThreadCount());
        state.setCpuUsage(getCpuUsage());
        state.setAvailableProcessors(runtime.availableProcessors());
        
        return state;
    }
    
    /**
     * Generate a unique error ID.
     * 
     * @return unique error ID
     */
    public String generateErrorId() {
        return "ERR-" + UUID.randomUUID().toString();
    }
    
    @Override
    public DiagnosticInfo getDiagnosticInfo() {
        Map<String, Object> states = new ConcurrentHashMap<>();
        states.put("totalEnrichments", totalEnrichments.get());
        
        // Current system state
        SystemState currentState = captureSystemState();
        states.put("system.memoryUsedMB", currentState.getMemoryUsed() / (1024 * 1024));
        states.put("system.memoryTotalMB", currentState.getMemoryTotal() / (1024 * 1024));
        states.put("system.memoryMaxMB", currentState.getMemoryMax() / (1024 * 1024));
        states.put("system.activeThreads", currentState.getActiveThreads());
        states.put("system.cpuUsage", currentState.getCpuUsage());
        states.put("system.availableProcessors", currentState.getAvailableProcessors());
        
        // Category statistics
        categoryCounts.forEach((category, count) -> {
            states.put("category." + category.name() + ".count", count.get());
        });
        
        return DiagnosticInfo.builder()
                .component("ErrorEnrichmentService")
                .states(states)
                .build();
    }
    
    @Override
    public boolean isDiagnosticModeEnabled() {
        return diagnosticMode.get();
    }
    
    @Override
    public void enableDiagnosticMode(boolean enabled) {
        diagnosticMode.set(enabled);
        log.info("Diagnostic mode {} for ErrorEnrichmentService", enabled ? "enabled" : "disabled");
    }
    
    private double getCpuUsage() {
        try {
            com.sun.management.OperatingSystemMXBean osBean = 
                (com.sun.management.OperatingSystemMXBean) ManagementFactory.getOperatingSystemMXBean();
            return osBean.getProcessCpuLoad();
        } catch (Exception e) {
            log.debug("Failed to get CPU usage", e);
            return -1;
        }
    }
    
    /**
     * System state information.
     */
    @Data
    public static class SystemState {
        private long memoryUsed;
        private long memoryTotal;
        private long memoryMax;
        private int activeThreads;
        private double cpuUsage;
        private int availableProcessors;
    }
}