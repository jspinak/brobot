package io.github.jspinak.brobot.runner.errorhandling;

import lombok.Getter;

import java.time.Instant;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentLinkedDeque;
import java.util.concurrent.atomic.AtomicLong;
import java.util.stream.Collectors;

/**
 * Maintains a history of errors for analysis and reporting.
 */
public class ErrorHistory {
    
    private static final int MAX_HISTORY_SIZE = 1000;
    private static final long RETENTION_PERIOD_MS = 24 * 60 * 60 * 1000; // 24 hours
    
    private final Deque<ErrorRecord> errorRecords = new ConcurrentLinkedDeque<>();
    private final Map<String, AtomicLong> errorFrequency = new ConcurrentHashMap<>();
    private final Map<ErrorContext.ErrorCategory, List<ErrorRecord>> errorsByCategory = 
        new ConcurrentHashMap<>();
    
    /**
     * Record an error in the history.
     */
    public void record(Throwable error, ErrorContext context) {
        ErrorRecord record = new ErrorRecord(error, context);
        
        // Add to main history
        errorRecords.addFirst(record);
        
        // Update frequency count
        String errorKey = error.getClass().getName() + ":" + error.getMessage();
        errorFrequency.computeIfAbsent(errorKey, k -> new AtomicLong()).incrementAndGet();
        
        // Add to category index
        errorsByCategory.computeIfAbsent(context.getCategory(), k -> new ArrayList<>())
            .add(record);
        
        // Cleanup old records
        cleanupOldRecords();
    }
    
    /**
     * Get recent errors.
     */
    public List<ErrorRecord> getRecentErrors(int count) {
        return errorRecords.stream()
            .limit(count)
            .collect(Collectors.toList());
    }
    
    /**
     * Get errors by category.
     */
    public List<ErrorRecord> getErrorsByCategory(ErrorContext.ErrorCategory category) {
        return errorsByCategory.getOrDefault(category, Collections.emptyList());
    }
    
    /**
     * Get most frequent errors.
     */
    public List<ErrorFrequency> getMostFrequentErrors(int count) {
        return errorFrequency.entrySet().stream()
            .map(entry -> new ErrorFrequency(entry.getKey(), entry.getValue().get()))
            .sorted((a, b) -> Long.compare(b.count(), a.count()))
            .limit(count)
            .collect(Collectors.toList());
    }
    
    /**
     * Get errors within a time range.
     */
    public List<ErrorRecord> getErrorsInRange(Instant start, Instant end) {
        return errorRecords.stream()
            .filter(record -> record.getTimestamp().isAfter(start) && 
                            record.getTimestamp().isBefore(end))
            .collect(Collectors.toList());
    }
    
    /**
     * Clear all error history.
     */
    public void clear() {
        errorRecords.clear();
        errorFrequency.clear();
        errorsByCategory.clear();
    }
    
    /**
     * Get statistics about error history.
     */
    public ErrorHistoryStatistics getStatistics() {
        Map<ErrorContext.ErrorCategory, Long> categoryCount = new HashMap<>();
        Map<ErrorContext.ErrorSeverity, Long> severityCount = new HashMap<>();
        
        for (ErrorRecord record : errorRecords) {
            categoryCount.merge(record.getContext().getCategory(), 1L, Long::sum);
            severityCount.merge(record.getContext().getSeverity(), 1L, Long::sum);
        }
        
        return new ErrorHistoryStatistics(
            errorRecords.size(),
            categoryCount,
            severityCount,
            getMostFrequentErrors(5)
        );
    }
    
    private void cleanupOldRecords() {
        // Remove records older than retention period
        Instant cutoff = Instant.now().minusMillis(RETENTION_PERIOD_MS);
        errorRecords.removeIf(record -> record.getTimestamp().isBefore(cutoff));
        
        // Limit total size
        while (errorRecords.size() > MAX_HISTORY_SIZE) {
            errorRecords.removeLast();
        }
        
        // Clean up category index
        errorsByCategory.values().forEach(list -> 
            list.removeIf(record -> record.getTimestamp().isBefore(cutoff))
        );
    }
    
    /**
     * Record of a single error occurrence.
     */
    @Getter
    public static class ErrorRecord {
        private final String errorId;
        private final Instant timestamp;
        private final String errorType;
        private final String message;
        private final ErrorContext context;
        private final List<String> stackTrace;
        
        ErrorRecord(Throwable error, ErrorContext context) {
            this.errorId = context.getErrorId();
            this.timestamp = context.getTimestamp();
            this.errorType = error.getClass().getName();
            this.message = error.getMessage();
            this.context = context;
            this.stackTrace = extractStackTrace(error);
        }
        
        private List<String> extractStackTrace(Throwable error) {
            List<String> trace = new ArrayList<>();
            StackTraceElement[] elements = error.getStackTrace();
            
            // Limit to first 10 elements
            for (int i = 0; i < Math.min(10, elements.length); i++) {
                trace.add(elements[i].toString());
            }
            
            return trace;
        }
    }
    
    /**
     * Error frequency information.
     */
    public record ErrorFrequency(String errorKey, long count) {}
    
    /**
     * Statistics about error history.
     */
    public record ErrorHistoryStatistics(
        long totalErrors,
        Map<ErrorContext.ErrorCategory, Long> errorsByCategory,
        Map<ErrorContext.ErrorSeverity, Long> errorsBySeverity,
        List<ErrorFrequency> mostFrequent
    ) {}
}