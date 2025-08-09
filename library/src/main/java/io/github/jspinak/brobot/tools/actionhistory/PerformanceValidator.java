package io.github.jspinak.brobot.tools.actionhistory;

import io.github.jspinak.brobot.model.action.ActionHistory;
import io.github.jspinak.brobot.model.action.ActionRecord;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.stream.Collectors;

/**
 * Validates performance characteristics using ActionHistory data.
 * 
 * <p>This class analyzes ActionHistory records to validate performance expectations,
 * detect anomalies, and ensure automation reliability. It provides various validation
 * methods to check timing, success rates, and consistency.</p>
 * 
 * <p>Features:
 * <ul>
 *   <li>Performance threshold validation</li>
 *   <li>Anomaly detection</li>
 *   <li>Success rate analysis</li>
 *   <li>Trend detection</li>
 *   <li>Comparative analysis between histories</li>
 * </ul>
 * 
 * @since 1.2.0
 */
@Component
@RequiredArgsConstructor
@Slf4j
public class PerformanceValidator {
    
    /**
     * Default performance thresholds.
     */
    public static class DefaultThresholds {
        public static final double MIN_SUCCESS_RATE = 80.0;
        public static final long MAX_AVERAGE_DURATION_MS = 2000;
        public static final long MAX_DURATION_MS = 5000;
        public static final double MAX_DURATION_VARIANCE = 0.5;
        public static final int MIN_SAMPLES = 10;
    }
    
    /**
     * Validates overall performance against default thresholds.
     * 
     * @param history the ActionHistory to validate
     * @return validation result with details
     */
    public ValidationResult validate(ActionHistory history) {
        return validate(history, ValidationConfig.getDefault());
    }
    
    /**
     * Validates performance with custom configuration.
     * 
     * @param history the ActionHistory to validate
     * @param config the validation configuration
     * @return validation result with details
     */
    public ValidationResult validate(ActionHistory history, ValidationConfig config) {
        ValidationResult result = new ValidationResult();
        
        if (history.getSnapshots().isEmpty()) {
            result.addError("No snapshots in history");
            return result;
        }
        
        // Check minimum samples
        if (history.getSnapshots().size() < config.minSamples) {
            result.addWarning(String.format("Insufficient samples: %d < %d", 
                history.getSnapshots().size(), config.minSamples));
        }
        
        // Validate success rate
        validateSuccessRate(history, config, result);
        
        // Validate duration metrics
        validateDuration(history, config, result);
        
        // Check for anomalies
        detectAnomalies(history, config, result);
        
        // Check for trends
        detectTrends(history, config, result);
        
        return result;
    }
    
    private void validateSuccessRate(ActionHistory history, ValidationConfig config, 
                                    ValidationResult result) {
        long successCount = history.getSnapshots().stream()
            .filter(ActionRecord::isActionSuccess)
            .count();
        
        double successRate = (double) successCount / history.getSnapshots().size() * 100;
        result.setSuccessRate(successRate);
        
        if (successRate < config.minSuccessRate) {
            result.addError(String.format("Success rate %.1f%% below threshold %.1f%%", 
                successRate, config.minSuccessRate));
        }
    }
    
    private void validateDuration(ActionHistory history, ValidationConfig config,
                                 ValidationResult result) {
        DoubleSummaryStatistics stats = history.getSnapshots().stream()
            .mapToDouble(ActionRecord::getDuration)
            .summaryStatistics();
        
        result.setAverageDuration(stats.getAverage());
        result.setMaxDuration(stats.getMax());
        result.setMinDuration(stats.getMin());
        
        if (stats.getAverage() > config.maxAverageDurationMs) {
            result.addError(String.format("Average duration %.0fms exceeds threshold %dms",
                stats.getAverage(), config.maxAverageDurationMs));
        }
        
        if (stats.getMax() > config.maxDurationMs) {
            result.addWarning(String.format("Max duration %.0fms exceeds threshold %dms",
                stats.getMax(), config.maxDurationMs));
        }
        
        // Calculate variance
        if (stats.getCount() > 1) {
            double mean = stats.getAverage();
            double variance = history.getSnapshots().stream()
                .mapToDouble(r -> Math.pow(r.getDuration() - mean, 2))
                .average().orElse(0);
            
            double stdDev = Math.sqrt(variance);
            double coefficientOfVariation = stdDev / mean;
            
            result.setDurationVariance(coefficientOfVariation);
            
            if (coefficientOfVariation > config.maxDurationVariance) {
                result.addWarning(String.format("Duration variance %.2f exceeds threshold %.2f",
                    coefficientOfVariation, config.maxDurationVariance));
            }
        }
    }
    
    private void detectAnomalies(ActionHistory history, ValidationConfig config,
                                ValidationResult result) {
        if (history.getSnapshots().size() < 3) return;
        
        // Detect duration anomalies using IQR method
        List<Double> durations = history.getSnapshots().stream()
            .map(ActionRecord::getDuration)
            .sorted()
            .collect(Collectors.toList());
        
        int q1Index = durations.size() / 4;
        int q3Index = 3 * durations.size() / 4;
        
        double q1 = durations.get(q1Index);
        double q3 = durations.get(q3Index);
        double iqr = q3 - q1;
        
        double lowerBound = q1 - 1.5 * iqr;
        double upperBound = q3 + 1.5 * iqr;
        
        List<ActionRecord> anomalies = history.getSnapshots().stream()
            .filter(r -> r.getDuration() < lowerBound || r.getDuration() > upperBound)
            .collect(Collectors.toList());
        
        if (!anomalies.isEmpty()) {
            result.setAnomalyCount(anomalies.size());
            result.addWarning(String.format("Detected %d duration anomalies (outliers)", 
                anomalies.size()));
            
            for (ActionRecord anomaly : anomalies) {
                String timestamp = anomaly.getTimeStamp() != null ? 
                    anomaly.getTimeStamp().format(DateTimeFormatter.ISO_LOCAL_DATE_TIME) : "unknown";
                result.addAnomaly(String.format("Anomaly at %s: duration %.0fms", 
                    timestamp, anomaly.getDuration()));
            }
        }
        
        // Detect consecutive failures
        int maxConsecutiveFailures = 0;
        int currentFailureStreak = 0;
        
        for (ActionRecord record : history.getSnapshots()) {
            if (!record.isActionSuccess()) {
                currentFailureStreak++;
                maxConsecutiveFailures = Math.max(maxConsecutiveFailures, currentFailureStreak);
            } else {
                currentFailureStreak = 0;
            }
        }
        
        if (maxConsecutiveFailures > config.maxConsecutiveFailures) {
            result.addError(String.format("Detected %d consecutive failures (max allowed: %d)",
                maxConsecutiveFailures, config.maxConsecutiveFailures));
        }
    }
    
    private void detectTrends(ActionHistory history, ValidationConfig config,
                            ValidationResult result) {
        if (history.getSnapshots().size() < 10) return;
        
        // Analyze duration trend
        List<ActionRecord> records = history.getSnapshots();
        int windowSize = Math.min(5, records.size() / 3);
        
        double earlyAvg = records.subList(0, windowSize).stream()
            .mapToDouble(ActionRecord::getDuration)
            .average().orElse(0);
        
        double lateAvg = records.subList(records.size() - windowSize, records.size()).stream()
            .mapToDouble(ActionRecord::getDuration)
            .average().orElse(0);
        
        double percentChange = ((lateAvg - earlyAvg) / earlyAvg) * 100;
        
        if (Math.abs(percentChange) > 20) {
            String trend = percentChange > 0 ? "degradation" : "improvement";
            result.addInfo(String.format("Performance %s detected: %.1f%% change in duration",
                trend, Math.abs(percentChange)));
        }
        
        // Analyze success rate trend
        long earlySuccess = records.subList(0, windowSize).stream()
            .filter(ActionRecord::isActionSuccess)
            .count();
        
        long lateSuccess = records.subList(records.size() - windowSize, records.size()).stream()
            .filter(ActionRecord::isActionSuccess)
            .count();
        
        double earlySuccessRate = (double) earlySuccess / windowSize * 100;
        double lateSuccessRate = (double) lateSuccess / windowSize * 100;
        
        if (Math.abs(lateSuccessRate - earlySuccessRate) > 10) {
            String trend = lateSuccessRate > earlySuccessRate ? "improving" : "degrading";
            result.addInfo(String.format("Success rate %s: %.1f%% -> %.1f%%",
                trend, earlySuccessRate, lateSuccessRate));
        }
    }
    
    /**
     * Compare two ActionHistory instances for performance differences.
     * 
     * @param baseline the baseline history
     * @param current the current history to compare
     * @return comparison result
     */
    public ComparisonResult compare(ActionHistory baseline, ActionHistory current) {
        ComparisonResult result = new ComparisonResult();
        
        // Calculate metrics for both
        DoubleSummaryStatistics baselineStats = baseline.getSnapshots().stream()
            .mapToDouble(ActionRecord::getDuration)
            .summaryStatistics();
        
        DoubleSummaryStatistics currentStats = current.getSnapshots().stream()
            .mapToDouble(ActionRecord::getDuration)
            .summaryStatistics();
        
        // Success rates
        double baselineSuccess = baseline.getSnapshots().stream()
            .filter(ActionRecord::isActionSuccess)
            .count() / (double) baseline.getSnapshots().size() * 100;
        
        double currentSuccess = current.getSnapshots().stream()
            .filter(ActionRecord::isActionSuccess)
            .count() / (double) current.getSnapshots().size() * 100;
        
        // Set comparison values
        result.setBaselineAvgDuration(baselineStats.getAverage());
        result.setCurrentAvgDuration(currentStats.getAverage());
        result.setDurationChange((currentStats.getAverage() - baselineStats.getAverage()) 
            / baselineStats.getAverage() * 100);
        
        result.setBaselineSuccessRate(baselineSuccess);
        result.setCurrentSuccessRate(currentSuccess);
        result.setSuccessRateChange(currentSuccess - baselineSuccess);
        
        // Determine if regression
        if (currentStats.getAverage() > baselineStats.getAverage() * 1.2) {
            result.setRegression(true);
            result.addIssue("Performance regression: duration increased by " +
                String.format("%.1f%%", result.getDurationChange()));
        }
        
        if (currentSuccess < baselineSuccess - 5) {
            result.setRegression(true);
            result.addIssue("Success rate regression: decreased by " +
                String.format("%.1f%%", Math.abs(result.getSuccessRateChange())));
        }
        
        return result;
    }
    
    /**
     * Validation configuration.
     */
    public static class ValidationConfig {
        public double minSuccessRate = DefaultThresholds.MIN_SUCCESS_RATE;
        public long maxAverageDurationMs = DefaultThresholds.MAX_AVERAGE_DURATION_MS;
        public long maxDurationMs = DefaultThresholds.MAX_DURATION_MS;
        public double maxDurationVariance = DefaultThresholds.MAX_DURATION_VARIANCE;
        public int minSamples = DefaultThresholds.MIN_SAMPLES;
        public int maxConsecutiveFailures = 3;
        
        public static ValidationConfig getDefault() {
            return new ValidationConfig();
        }
        
        public ValidationConfig withMinSuccessRate(double rate) {
            this.minSuccessRate = rate;
            return this;
        }
        
        public ValidationConfig withMaxAverageDuration(long ms) {
            this.maxAverageDurationMs = ms;
            return this;
        }
        
        public ValidationConfig withMaxDuration(long ms) {
            this.maxDurationMs = ms;
            return this;
        }
    }
    
    /**
     * Validation result containing all findings.
     */
    public static class ValidationResult {
        private boolean valid = true;
        private List<String> errors = new ArrayList<>();
        private List<String> warnings = new ArrayList<>();
        private List<String> info = new ArrayList<>();
        private List<String> anomalies = new ArrayList<>();
        
        private double successRate;
        private double averageDuration;
        private double maxDuration;
        private double minDuration;
        private double durationVariance;
        private int anomalyCount;
        
        public void addError(String error) {
            errors.add(error);
            valid = false;
        }
        
        public void addWarning(String warning) {
            warnings.add(warning);
        }
        
        public void addInfo(String information) {
            info.add(information);
        }
        
        public void addAnomaly(String anomaly) {
            anomalies.add(anomaly);
        }
        
        // Getters and setters
        public boolean isValid() { return valid && errors.isEmpty(); }
        public List<String> getErrors() { return errors; }
        public List<String> getWarnings() { return warnings; }
        public List<String> getInfo() { return info; }
        public List<String> getAnomalies() { return anomalies; }
        
        public double getSuccessRate() { return successRate; }
        public void setSuccessRate(double rate) { this.successRate = rate; }
        
        public double getAverageDuration() { return averageDuration; }
        public void setAverageDuration(double duration) { this.averageDuration = duration; }
        
        public double getMaxDuration() { return maxDuration; }
        public void setMaxDuration(double duration) { this.maxDuration = duration; }
        
        public double getMinDuration() { return minDuration; }
        public void setMinDuration(double duration) { this.minDuration = duration; }
        
        public double getDurationVariance() { return durationVariance; }
        public void setDurationVariance(double variance) { this.durationVariance = variance; }
        
        public int getAnomalyCount() { return anomalyCount; }
        public void setAnomalyCount(int count) { this.anomalyCount = count; }
        
        @Override
        public String toString() {
            StringBuilder sb = new StringBuilder();
            sb.append("ValidationResult{\n");
            sb.append("  valid=").append(isValid()).append(",\n");
            sb.append("  successRate=").append(String.format("%.1f%%", successRate)).append(",\n");
            sb.append("  avgDuration=").append(String.format("%.0fms", averageDuration)).append(",\n");
            if (!errors.isEmpty()) {
                sb.append("  errors=").append(errors).append(",\n");
            }
            if (!warnings.isEmpty()) {
                sb.append("  warnings=").append(warnings).append(",\n");
            }
            if (!anomalies.isEmpty()) {
                sb.append("  anomalies=").append(anomalies.size()).append(" detected,\n");
            }
            sb.append("}");
            return sb.toString();
        }
    }
    
    /**
     * Result of comparing two ActionHistory instances.
     */
    public static class ComparisonResult {
        private double baselineAvgDuration;
        private double currentAvgDuration;
        private double durationChange;
        
        private double baselineSuccessRate;
        private double currentSuccessRate;
        private double successRateChange;
        
        private boolean regression = false;
        private List<String> issues = new ArrayList<>();
        
        public void addIssue(String issue) {
            issues.add(issue);
        }
        
        // Getters and setters
        public double getBaselineAvgDuration() { return baselineAvgDuration; }
        public void setBaselineAvgDuration(double duration) { this.baselineAvgDuration = duration; }
        
        public double getCurrentAvgDuration() { return currentAvgDuration; }
        public void setCurrentAvgDuration(double duration) { this.currentAvgDuration = duration; }
        
        public double getDurationChange() { return durationChange; }
        public void setDurationChange(double change) { this.durationChange = change; }
        
        public double getBaselineSuccessRate() { return baselineSuccessRate; }
        public void setBaselineSuccessRate(double rate) { this.baselineSuccessRate = rate; }
        
        public double getCurrentSuccessRate() { return currentSuccessRate; }
        public void setCurrentSuccessRate(double rate) { this.currentSuccessRate = rate; }
        
        public double getSuccessRateChange() { return successRateChange; }
        public void setSuccessRateChange(double change) { this.successRateChange = change; }
        
        public boolean isRegression() { return regression; }
        public void setRegression(boolean regression) { this.regression = regression; }
        
        public List<String> getIssues() { return issues; }
        
        @Override
        public String toString() {
            StringBuilder sb = new StringBuilder();
            sb.append("ComparisonResult{\n");
            sb.append("  Duration: ").append(String.format("%.0fms -> %.0fms (%.1f%% change)\n",
                baselineAvgDuration, currentAvgDuration, durationChange));
            sb.append("  Success: ").append(String.format("%.1f%% -> %.1f%% (%.1f%% change)\n",
                baselineSuccessRate, currentSuccessRate, successRateChange));
            sb.append("  Regression: ").append(regression);
            if (!issues.isEmpty()) {
                sb.append("\n  Issues: ").append(issues);
            }
            sb.append("\n}");
            return sb.toString();
        }
    }
}