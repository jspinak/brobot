package io.github.jspinak.brobot.runner.errorhandling.history;

import io.github.jspinak.brobot.runner.common.diagnostics.DiagnosticCapable;
import io.github.jspinak.brobot.runner.common.diagnostics.DiagnosticInfo;
import io.github.jspinak.brobot.runner.errorhandling.ErrorContext;
import io.github.jspinak.brobot.runner.errorhandling.ErrorHistory;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicBoolean;

/**
 * Service responsible for maintaining error history and providing historical analysis.
 * 
 * This service tracks error occurrences, frequencies, and patterns over time,
 * enabling analysis and reporting of error trends.
 * 
 * Thread Safety: This class is thread-safe.
 * 
 * @since 1.0.0
 */
@Slf4j
@Service
public class ErrorHistoryService implements DiagnosticCapable {
    
    private final ErrorHistory errorHistory;
    
    @Value("${error.history.max-size:1000}")
    private int maxHistorySize;
    
    @Value("${error.history.retention-hours:24}")
    private int retentionHours;
    
    // Diagnostic mode
    private final AtomicBoolean diagnosticMode = new AtomicBoolean(false);
    
    public ErrorHistoryService() {
        this.errorHistory = new ErrorHistory();
    }
    
    /**
     * Record an error occurrence in history.
     * 
     * @param error the error that occurred
     * @param context the error context
     */
    public void record(Throwable error, ErrorContext context) {
        if (error == null || context == null) {
            log.warn("Attempted to record null error or context");
            return;
        }
        
        errorHistory.record(error, context);
        
        if (diagnosticMode.get()) {
            log.info("[DIAGNOSTIC] Recorded error {} - Type: {}, Category: {}, Severity: {}",
                    context.getErrorId(),
                    error.getClass().getSimpleName(),
                    context.getCategory(),
                    context.getSeverity());
        }
    }
    
    /**
     * Get recent error records.
     * 
     * @param count number of recent errors to retrieve
     * @return list of recent error records
     */
    public List<ErrorHistory.ErrorRecord> getRecentErrors(int count) {
        return errorHistory.getRecentErrors(count);
    }
    
    /**
     * Get errors by category.
     * 
     * @param category the error category
     * @return list of errors in that category
     */
    public List<ErrorHistory.ErrorRecord> getErrorsByCategory(ErrorContext.ErrorCategory category) {
        return errorHistory.getErrorsByCategory(category);
    }
    
    /**
     * Get most frequent errors.
     * 
     * @param count number of top errors to retrieve
     * @return list of error frequencies
     */
    public List<ErrorHistory.ErrorFrequency> getMostFrequentErrors(int count) {
        return errorHistory.getMostFrequentErrors(count);
    }
    
    /**
     * Get errors within a time range.
     * 
     * @param start start time (inclusive)
     * @param end end time (exclusive)
     * @return list of errors in the time range
     */
    public List<ErrorHistory.ErrorRecord> getErrorsInRange(Instant start, Instant end) {
        if (start == null || end == null) {
            throw new IllegalArgumentException("Start and end times must not be null");
        }
        
        if (start.isAfter(end)) {
            throw new IllegalArgumentException("Start time must be before end time");
        }
        
        return errorHistory.getErrorsInRange(start, end);
    }
    
    /**
     * Get comprehensive error history statistics.
     * 
     * @return error history statistics
     */
    public ErrorHistory.ErrorHistoryStatistics getStatistics() {
        return errorHistory.getStatistics();
    }
    
    /**
     * Clear all error history.
     */
    public void clear() {
        errorHistory.clear();
        log.info("Error history cleared");
        
        if (diagnosticMode.get()) {
            log.info("[DIAGNOSTIC] Error history cleared - all records removed");
        }
    }
    
    /**
     * Get error trends over time.
     * 
     * @param intervalMinutes time interval in minutes for grouping
     * @return map of time intervals to error counts
     */
    public Map<Instant, Long> getErrorTrends(int intervalMinutes) {
        Map<Instant, Long> trends = new ConcurrentHashMap<>();
        List<ErrorHistory.ErrorRecord> allErrors = errorHistory.getRecentErrors(Integer.MAX_VALUE);
        
        for (ErrorHistory.ErrorRecord record : allErrors) {
            // Round timestamp to interval
            long epochMinutes = record.getTimestamp().getEpochSecond() / 60;
            long intervalStart = (epochMinutes / intervalMinutes) * intervalMinutes * 60;
            Instant intervalKey = Instant.ofEpochSecond(intervalStart);
            
            trends.merge(intervalKey, 1L, Long::sum);
        }
        
        return trends;
    }
    
    @Override
    public DiagnosticInfo getDiagnosticInfo() {
        ErrorHistory.ErrorHistoryStatistics stats = errorHistory.getStatistics();
        
        Map<String, Object> states = new ConcurrentHashMap<>();
        states.put("totalErrors", stats.totalErrors());
        states.put("maxHistorySize", maxHistorySize);
        states.put("retentionHours", retentionHours);
        
        // Category breakdown
        stats.errorsByCategory().forEach((category, count) -> {
            states.put("category." + category.name() + ".count", count);
        });
        
        // Severity breakdown
        stats.errorsBySeverity().forEach((severity, count) -> {
            states.put("severity." + severity.name() + ".count", count);
        });
        
        // Most frequent errors
        List<ErrorHistory.ErrorFrequency> topErrors = stats.mostFrequent();
        states.put("topErrorsCount", topErrors.size());
        for (int i = 0; i < topErrors.size(); i++) {
            ErrorHistory.ErrorFrequency freq = topErrors.get(i);
            states.put("topError." + i + ".key", freq.errorKey());
            states.put("topError." + i + ".count", freq.count());
        }
        
        return DiagnosticInfo.builder()
                .component("ErrorHistoryService")
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
        log.info("Diagnostic mode {} for ErrorHistoryService", enabled ? "enabled" : "disabled");
    }
}