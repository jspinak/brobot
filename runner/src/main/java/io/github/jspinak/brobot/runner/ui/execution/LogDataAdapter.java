package io.github.jspinak.brobot.runner.ui.execution;

import io.github.jspinak.brobot.tools.logging.model.LogData;
import io.github.jspinak.brobot.tools.logging.model.ExecutionMetrics;
import java.util.HashMap;
import java.util.Map;

/**
 * Adapter class to provide compatibility between LogData and ExecutionDashboardPanel expectations.
 */
public class LogDataAdapter {
    
    /**
     * Adapter for ExecutionMetrics to provide expected methods.
     */
    public static class ExecutionMetricsAdapter {
        private final ExecutionMetrics metrics;
        
        public ExecutionMetricsAdapter(ExecutionMetrics metrics) {
            this.metrics = metrics;
        }
        
        public Long getTotalDuration() {
            return metrics != null ? metrics.getTotalTestDuration() : null;
        }
        
        public Long getMatchDuration() {
            return metrics != null ? metrics.getActionDuration() : null;
        }
        
        public ExecutionMetrics getOriginalMetrics() {
            return metrics;
        }
    }
    
    private final LogData logData;
    private final Map<String, String> details = new HashMap<>();
    
    public LogDataAdapter(LogData logData) {
        this.logData = logData;
        initializeDetails();
    }
    
    private void initializeDetails() {
        // Extract details from various LogData fields
        if (logData.getCurrentStateName() != null) {
            details.put("state", logData.getCurrentStateName());
        }
        if (logData.getFromStates() != null) {
            details.put("fromState", logData.getFromStates());
        }
        if (logData.getToStateNames() != null && !logData.getToStateNames().isEmpty()) {
            details.put("toState", logData.getToStateNames().get(0));
        }
        if (logData.getDuration() > 0) {
            details.put("duration", String.valueOf(logData.getDuration()));
        }
        if (logData.getDescription() != null) {
            details.put("details", logData.getDescription());
        }
        if (logData.getActionPerformed() != null) {
            details.put("target", logData.getActionPerformed());
        }
        if (logData.getErrorMessage() != null) {
            details.put("errorMessage", logData.getErrorMessage());
            details.put("errorType", "ERROR");
        }
    }
    
    public String getAction() {
        return logData.getActionType();
    }
    
    public Map<String, String> getDetails() {
        return details;
    }
    
    public String getResult() {
        return logData.isSuccess() ? "SUCCESS" : "FAILURE";
    }
    
    public String getMessage() {
        return logData.getDescription();
    }
    
    public ExecutionMetricsAdapter getPerformanceMetrics() {
        return new ExecutionMetricsAdapter(logData.getPerformance());
    }
    
    public LogData getOriginalLogData() {
        return logData;
    }
}