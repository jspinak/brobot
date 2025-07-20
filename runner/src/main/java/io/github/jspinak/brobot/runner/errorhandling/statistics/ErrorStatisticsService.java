package io.github.jspinak.brobot.runner.errorhandling.statistics;

import io.github.jspinak.brobot.runner.common.diagnostics.DiagnosticCapable;
import io.github.jspinak.brobot.runner.common.diagnostics.DiagnosticInfo;
import io.github.jspinak.brobot.runner.errorhandling.ErrorContext;
import io.github.jspinak.brobot.runner.errorhandling.ErrorResult;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicLong;
import java.util.stream.Collectors;

/**
 * Service responsible for collecting and analyzing error statistics.
 * 
 * This service tracks error rates, success rates, mean time between failures,
 * and other metrics that help monitor application health and error trends.
 * 
 * Thread Safety: This class is thread-safe.
 * 
 * @since 1.0.0
 */
@Slf4j
@Service
public class ErrorStatisticsService implements DiagnosticCapable {
    
    // Basic counters
    private final AtomicLong totalErrors = new AtomicLong(0);
    private final AtomicLong totalOperations = new AtomicLong(0);
    private final AtomicLong successfulOperations = new AtomicLong(0);
    private final AtomicLong failedOperations = new AtomicLong(0);
    private final AtomicLong recoveredOperations = new AtomicLong(0);
    
    // Time-based metrics
    private volatile Instant lastErrorTime = null;
    private volatile Instant firstErrorTime = null;
    private final Map<String, OperationMetrics> operationMetrics = new ConcurrentHashMap<>();
    
    // Error rate tracking
    private final Map<Instant, ErrorRateSnapshot> errorRateHistory = new ConcurrentHashMap<>();
    private volatile ErrorRateSnapshot currentSnapshot = new ErrorRateSnapshot();
    
    // Diagnostic mode
    private final AtomicBoolean diagnosticMode = new AtomicBoolean(false);
    
    /**
     * Record an operation attempt.
     * 
     * @param operation the operation name
     */
    public void recordOperationStart(String operation) {
        if (operation == null) {
            return;
        }
        
        totalOperations.incrementAndGet();
        operationMetrics.computeIfAbsent(operation, k -> new OperationMetrics())
                .recordStart();
        
        if (diagnosticMode.get()) {
            log.info("[DIAGNOSTIC] Operation started: {}", operation);
        }
    }
    
    /**
     * Record an operation result.
     * 
     * @param operation the operation name
     * @param result the operation result
     */
    public void recordOperationResult(String operation, ErrorResult result) {
        if (operation == null || result == null) {
            return;
        }
        
        OperationMetrics metrics = operationMetrics.get(operation);
        if (metrics == null) {
            log.warn("Recording result for untracked operation: {}", operation);
            metrics = operationMetrics.computeIfAbsent(operation, k -> new OperationMetrics());
        }
        
        metrics.recordComplete(result.isSuccess());
        
        if (result.isSuccess()) {
            successfulOperations.incrementAndGet();
        } else {
            failedOperations.incrementAndGet();
            totalErrors.incrementAndGet();
            
            // Update error timing
            Instant now = Instant.now();
            lastErrorTime = now;
            if (firstErrorTime == null) {
                firstErrorTime = now;
            }
            
            // Update current snapshot
            currentSnapshot.recordError();
            
            if (result.isRecoverable() && result.getRecoveryAction() != null) {
                recoveredOperations.incrementAndGet();
            }
        }
        
        if (diagnosticMode.get()) {
            log.info("[DIAGNOSTIC] Operation completed: {} - Success: {}, Recoverable: {}",
                    operation, result.isSuccess(), 
                    result.isRecoverable());
        }
    }
    
    /**
     * Record an error occurrence.
     * 
     * @param error the error
     * @param context the error context
     */
    public void recordError(Throwable error, ErrorContext context) {
        if (error == null || context == null) {
            return;
        }
        
        totalErrors.incrementAndGet();
        
        // Update error timing
        Instant now = Instant.now();
        lastErrorTime = now;
        if (firstErrorTime == null) {
            firstErrorTime = now;
        }
        
        // Update current snapshot
        currentSnapshot.recordError();
        
        // Track by operation if provided
        if (context.getOperation() != null) {
            operationMetrics.computeIfAbsent(context.getOperation(), k -> new OperationMetrics())
                    .recordError();
        }
        
        if (diagnosticMode.get()) {
            log.info("[DIAGNOSTIC] Error recorded: {} - Operation: {}, Category: {}",
                    error.getClass().getSimpleName(), context.getOperation(), context.getCategory());
        }
    }
    
    /**
     * Get current error rate (errors per minute).
     * 
     * @return current error rate
     */
    public double getCurrentErrorRate() {
        return currentSnapshot.getErrorRate();
    }
    
    /**
     * Get overall success rate.
     * 
     * @return success rate as percentage (0-100)
     */
    public double getOverallSuccessRate() {
        long total = totalOperations.get();
        if (total == 0) {
            return 100.0;
        }
        return (successfulOperations.get() * 100.0) / total;
    }
    
    /**
     * Get mean time between failures.
     * 
     * @return MTBF in seconds, or -1 if not enough data
     */
    public long getMeanTimeBetweenFailures() {
        if (firstErrorTime == null || lastErrorTime == null || totalErrors.get() < 2) {
            return -1;
        }
        
        long timeDiff = ChronoUnit.SECONDS.between(firstErrorTime, lastErrorTime);
        return timeDiff / (totalErrors.get() - 1);
    }
    
    /**
     * Get statistics for a specific operation.
     * 
     * @param operation the operation name
     * @return operation statistics
     */
    public OperationStatistics getOperationStatistics(String operation) {
        OperationMetrics metrics = operationMetrics.get(operation);
        if (metrics == null) {
            return OperationStatistics.empty(operation);
        }
        
        return metrics.toStatistics(operation);
    }
    
    /**
     * Get top operations by error count.
     * 
     * @param count number of operations to return
     * @return list of operations with highest error counts
     */
    public List<OperationStatistics> getTopErrorOperations(int count) {
        return operationMetrics.entrySet().stream()
                .map(entry -> entry.getValue().toStatistics(entry.getKey()))
                .sorted((a, b) -> Long.compare(b.errorCount(), a.errorCount()))
                .limit(count)
                .collect(Collectors.toList());
    }
    
    /**
     * Get error rate history.
     * 
     * @param hours number of hours to look back
     * @return map of timestamps to error rate snapshots
     */
    public Map<Instant, ErrorRateSnapshot> getErrorRateHistory(int hours) {
        Instant cutoff = Instant.now().minus(hours, ChronoUnit.HOURS);
        return errorRateHistory.entrySet().stream()
                .filter(entry -> entry.getKey().isAfter(cutoff))
                .collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue));
    }
    
    /**
     * Reset all statistics.
     */
    public void reset() {
        totalErrors.set(0);
        totalOperations.set(0);
        successfulOperations.set(0);
        failedOperations.set(0);
        recoveredOperations.set(0);
        lastErrorTime = null;
        firstErrorTime = null;
        operationMetrics.clear();
        errorRateHistory.clear();
        currentSnapshot = new ErrorRateSnapshot();
        
        log.info("Error statistics reset");
    }
    
    /**
     * Periodic task to snapshot error rates.
     */
    @Scheduled(fixedDelay = 60000) // Every minute
    public void snapshotErrorRate() {
        errorRateHistory.put(Instant.now(), currentSnapshot);
        currentSnapshot = new ErrorRateSnapshot();
        
        // Clean up old snapshots (keep 24 hours)
        Instant cutoff = Instant.now().minus(24, ChronoUnit.HOURS);
        errorRateHistory.entrySet().removeIf(entry -> entry.getKey().isBefore(cutoff));
    }
    
    @Override
    public DiagnosticInfo getDiagnosticInfo() {
        Map<String, Object> states = new ConcurrentHashMap<>();
        
        // Basic metrics
        states.put("totalErrors", totalErrors.get());
        states.put("totalOperations", totalOperations.get());
        states.put("successfulOperations", successfulOperations.get());
        states.put("failedOperations", failedOperations.get());
        states.put("recoveredOperations", recoveredOperations.get());
        
        // Calculated metrics
        states.put("successRate", getOverallSuccessRate());
        states.put("errorRate", getCurrentErrorRate());
        states.put("mtbf", getMeanTimeBetweenFailures());
        
        // Time metrics
        if (lastErrorTime != null) {
            states.put("lastErrorTime", lastErrorTime.toString());
            states.put("timeSinceLastError", ChronoUnit.SECONDS.between(lastErrorTime, Instant.now()));
        }
        
        // Top error operations
        List<OperationStatistics> topErrors = getTopErrorOperations(5);
        states.put("topErrorOperationsCount", topErrors.size());
        for (int i = 0; i < topErrors.size(); i++) {
            OperationStatistics stats = topErrors.get(i);
            states.put("topError." + i + ".operation", stats.operation());
            states.put("topError." + i + ".errorCount", stats.errorCount());
            states.put("topError." + i + ".successRate", stats.successRate());
        }
        
        return DiagnosticInfo.builder()
                .component("ErrorStatisticsService")
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
        log.info("Diagnostic mode {} for ErrorStatisticsService", enabled ? "enabled" : "disabled");
    }
    
    /**
     * Metrics for a specific operation.
     */
    @Data
    private static class OperationMetrics {
        private final AtomicLong totalAttempts = new AtomicLong(0);
        private final AtomicLong successCount = new AtomicLong(0);
        private final AtomicLong failureCount = new AtomicLong(0);
        private final AtomicLong errorCount = new AtomicLong(0);
        private final AtomicLong inProgress = new AtomicLong(0);
        private volatile Instant lastAttempt;
        private volatile Instant lastSuccess;
        private volatile Instant lastFailure;
        
        void recordStart() {
            totalAttempts.incrementAndGet();
            inProgress.incrementAndGet();
            lastAttempt = Instant.now();
        }
        
        void recordComplete(boolean success) {
            inProgress.decrementAndGet();
            if (success) {
                successCount.incrementAndGet();
                lastSuccess = Instant.now();
            } else {
                failureCount.incrementAndGet();
                lastFailure = Instant.now();
            }
        }
        
        void recordError() {
            errorCount.incrementAndGet();
        }
        
        OperationStatistics toStatistics(String operation) {
            long total = totalAttempts.get();
            double successRate = total > 0 ? (successCount.get() * 100.0) / total : 0;
            
            return new OperationStatistics(
                operation,
                total,
                successCount.get(),
                failureCount.get(),
                errorCount.get(),
                successRate,
                lastAttempt,
                lastSuccess,
                lastFailure
            );
        }
    }
    
    /**
     * Snapshot of error rate at a point in time.
     */
    @Data
    public static class ErrorRateSnapshot {
        private final Instant timestamp = Instant.now();
        private final AtomicLong errorCount = new AtomicLong(0);
        
        void recordError() {
            errorCount.incrementAndGet();
        }
        
        double getErrorRate() {
            long elapsed = ChronoUnit.SECONDS.between(timestamp, Instant.now());
            if (elapsed == 0) {
                return 0;
            }
            return (errorCount.get() * 60.0) / elapsed; // Errors per minute
        }
    }
    
    /**
     * Statistics for a specific operation.
     */
    public record OperationStatistics(
        String operation,
        long totalAttempts,
        long successCount,
        long failureCount,
        long errorCount,
        double successRate,
        Instant lastAttempt,
        Instant lastSuccess,
        Instant lastFailure
    ) {
        static OperationStatistics empty(String operation) {
            return new OperationStatistics(operation, 0, 0, 0, 0, 0, null, null, null);
        }
    }
}