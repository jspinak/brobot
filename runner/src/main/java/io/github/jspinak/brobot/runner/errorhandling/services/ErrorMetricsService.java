package io.github.jspinak.brobot.runner.errorhandling.services;

import io.github.jspinak.brobot.runner.errorhandling.ErrorContext;
import io.github.jspinak.brobot.runner.errorhandling.ErrorHistory;
import io.github.jspinak.brobot.runner.errorhandling.ErrorStatistics;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicLong;
import java.util.stream.Collectors;

/**
 * Service responsible for tracking error metrics and statistics.
 * Provides insights into error patterns and frequency.
 */
@Slf4j
@Service
public class ErrorMetricsService {
    
    private final AtomicLong totalErrors = new AtomicLong();
    private final Map<ErrorContext.ErrorCategory, AtomicLong> errorsByCategory = new ConcurrentHashMap<>();
    private final Map<ErrorContext.ErrorSeverity, AtomicLong> errorsBySeverity = new ConcurrentHashMap<>();
    private final Map<String, AtomicLong> errorsByComponent = new ConcurrentHashMap<>();
    private final Map<String, AtomicLong> errorsByType = new ConcurrentHashMap<>();
    
    @Getter
    private final ErrorHistory errorHistory = new ErrorHistory();
    
    // Time-based metrics
    private final Map<String, List<LocalDateTime>> errorTimestamps = new ConcurrentHashMap<>();
    private static final int MAX_TIMESTAMPS_PER_ERROR = 100;
    
    /**
     * Records an error in the metrics system.
     */
    public void recordError(Throwable error, ErrorContext context) {
        // Increment total count
        totalErrors.incrementAndGet();
        
        // Count by category
        errorsByCategory.computeIfAbsent(context.getCategory(), k -> new AtomicLong())
            .incrementAndGet();
        
        // Count by severity
        errorsBySeverity.computeIfAbsent(context.getSeverity(), k -> new AtomicLong())
            .incrementAndGet();
        
        // Count by component
        if (context.getComponent() != null) {
            errorsByComponent.computeIfAbsent(context.getComponent(), k -> new AtomicLong())
                .incrementAndGet();
        }
        
        // Count by error type
        String errorType = error.getClass().getSimpleName();
        errorsByType.computeIfAbsent(errorType, k -> new AtomicLong())
            .incrementAndGet();
        
        // Record timestamp for rate calculation
        recordTimestamp(errorType);
        
        // Record in history
        errorHistory.record(error, context);
        
        log.trace("Recorded error: {} - Category: {}, Severity: {}", 
                 errorType, context.getCategory(), context.getSeverity());
    }
    
    /**
     * Gets comprehensive error statistics.
     */
    public ErrorStatistics getStatistics() {
        Map<ErrorContext.ErrorCategory, Long> categoryStats = new HashMap<>();
        errorsByCategory.forEach((category, count) -> 
            categoryStats.put(category, count.get())
        );
        
        return new ErrorStatistics(
            totalErrors.get(),
            categoryStats,
            errorHistory.getRecentErrors(10),
            errorHistory.getMostFrequentErrors(5)
        );
    }
    
    /**
     * Gets detailed metrics including severity and component breakdowns.
     */
    public DetailedMetrics getDetailedMetrics() {
        return DetailedMetrics.builder()
            .totalErrors(totalErrors.get())
            .errorsByCategory(getCountMap(errorsByCategory))
            .errorsBySeverity(getCountMap(errorsBySeverity))
            .errorsByComponent(getTopComponents(10))
            .errorsByType(getTopErrorTypes(10))
            .errorRate(calculateErrorRate())
            .build();
    }
    
    /**
     * Gets error count for a specific category.
     */
    public long getErrorCountByCategory(ErrorContext.ErrorCategory category) {
        AtomicLong count = errorsByCategory.get(category);
        return count != null ? count.get() : 0;
    }
    
    /**
     * Gets error count for a specific severity.
     */
    public long getErrorCountBySeverity(ErrorContext.ErrorSeverity severity) {
        AtomicLong count = errorsBySeverity.get(severity);
        return count != null ? count.get() : 0;
    }
    
    /**
     * Gets error count for a specific component.
     */
    public long getErrorCountByComponent(String component) {
        AtomicLong count = errorsByComponent.get(component);
        return count != null ? count.get() : 0;
    }
    
    /**
     * Calculates the error rate (errors per minute) over the last hour.
     */
    public double calculateErrorRate() {
        LocalDateTime oneHourAgo = LocalDateTime.now().minusHours(1);
        
        long recentErrors = errorTimestamps.values().stream()
            .flatMap(List::stream)
            .filter(timestamp -> timestamp.isAfter(oneHourAgo))
            .count();
        
        return recentErrors / 60.0; // errors per minute
    }
    
    /**
     * Clears all metrics and history.
     */
    public void clearMetrics() {
        totalErrors.set(0);
        errorsByCategory.clear();
        errorsBySeverity.clear();
        errorsByComponent.clear();
        errorsByType.clear();
        errorTimestamps.clear();
        errorHistory.clear();
        
        log.info("Error metrics cleared");
    }
    
    /**
     * Gets metrics for a specific time period.
     */
    public PeriodMetrics getMetricsForPeriod(LocalDateTime start, LocalDateTime end) {
        long periodErrors = errorTimestamps.values().stream()
            .flatMap(List::stream)
            .filter(timestamp -> timestamp.isAfter(start) && timestamp.isBefore(end))
            .count();
        
        return new PeriodMetrics(start, end, periodErrors, calculateRateForPeriod(periodErrors, start, end));
    }
    
    private void recordTimestamp(String errorType) {
        errorTimestamps.compute(errorType, (key, timestamps) -> {
            if (timestamps == null) {
                timestamps = new ArrayList<>();
            }
            
            timestamps.add(LocalDateTime.now());
            
            // Keep only recent timestamps to prevent memory issues
            if (timestamps.size() > MAX_TIMESTAMPS_PER_ERROR) {
                timestamps = new ArrayList<>(
                    timestamps.subList(timestamps.size() - MAX_TIMESTAMPS_PER_ERROR, timestamps.size())
                );
            }
            
            return timestamps;
        });
    }
    
    private <T> Map<T, Long> getCountMap(Map<T, AtomicLong> source) {
        Map<T, Long> result = new HashMap<>();
        source.forEach((key, value) -> result.put(key, value.get()));
        return result;
    }
    
    private Map<String, Long> getTopComponents(int limit) {
        return errorsByComponent.entrySet().stream()
            .sorted(Map.Entry.<String, AtomicLong>comparingByValue((a, b) -> Long.compare(b.get(), a.get())))
            .limit(limit)
            .collect(Collectors.toMap(
                Map.Entry::getKey,
                e -> e.getValue().get(),
                (e1, e2) -> e1,
                LinkedHashMap::new
            ));
    }
    
    private Map<String, Long> getTopErrorTypes(int limit) {
        return errorsByType.entrySet().stream()
            .sorted(Map.Entry.<String, AtomicLong>comparingByValue((a, b) -> Long.compare(b.get(), a.get())))
            .limit(limit)
            .collect(Collectors.toMap(
                Map.Entry::getKey,
                e -> e.getValue().get(),
                (e1, e2) -> e1,
                LinkedHashMap::new
            ));
    }
    
    private double calculateRateForPeriod(long errors, LocalDateTime start, LocalDateTime end) {
        long minutes = java.time.Duration.between(start, end).toMinutes();
        return minutes > 0 ? errors / (double) minutes : 0;
    }
    
    /**
     * Detailed metrics including all breakdown categories.
     */
    @lombok.Builder
    @lombok.Data
    public static class DetailedMetrics {
        private final long totalErrors;
        private final Map<ErrorContext.ErrorCategory, Long> errorsByCategory;
        private final Map<ErrorContext.ErrorSeverity, Long> errorsBySeverity;
        private final Map<String, Long> errorsByComponent;
        private final Map<String, Long> errorsByType;
        private final double errorRate;
    }
    
    /**
     * Metrics for a specific time period.
     */
    @lombok.AllArgsConstructor
    @lombok.Data
    public static class PeriodMetrics {
        private final LocalDateTime startTime;
        private final LocalDateTime endTime;
        private final long errorCount;
        private final double errorRate;
    }
}