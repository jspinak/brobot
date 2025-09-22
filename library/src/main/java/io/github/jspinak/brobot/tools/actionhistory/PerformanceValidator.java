package io.github.jspinak.brobot.tools.actionhistory;

import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.stream.Collectors;

import org.springframework.stereotype.Component;

import io.github.jspinak.brobot.model.action.ActionHistory;
import io.github.jspinak.brobot.model.action.ActionRecord;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

/**
 * Validates performance characteristics using ActionHistory data.
 *
 * <p>This class analyzes ActionHistory records to validate performance expectations, detect
 * anomalies, and ensure automation reliability. It provides various validation methods to check
 * timing, success rates, and consistency.
 *
 * <p>Features:
 *
 * <ul>
 *   <li>Performance threshold validation
 *   <li>Anomaly detection
 *   <li>Success rate analysis
 *   <li>Trend detection
 *   <li>Comparative analysis between histories
 * </ul>
 *
 * @since 1.1.0
 */
@Component
@RequiredArgsConstructor
@Slf4j
public class PerformanceValidator {

    /** Default performance thresholds. */
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
            result.addWarning(
                    String.format(
                            "Insufficient samples: %d < %d",
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

    private void validateSuccessRate(
            ActionHistory history, ValidationConfig config, ValidationResult result) {
        long successCount =
                history.getSnapshots().stream().filter(ActionRecord::isActionSuccess).count();

        double successRate = (double) successCount / history.getSnapshots().size() * 100;
        result.setSuccessRate(successRate);

        if (successRate < config.minSuccessRate) {
            result.addError(
                    String.format(
                            "Success rate %.1f%% below threshold %.1f%%",
                            successRate, config.minSuccessRate));
        }
    }

    private void validateDuration(
            ActionHistory history, ValidationConfig config, ValidationResult result) {
        DoubleSummaryStatistics stats =
                history.getSnapshots().stream()
                        .mapToDouble(ActionRecord::getDuration)
                        .summaryStatistics();

        result.setAverageDuration(stats.getAverage());
        result.setMaxDuration(stats.getMax());
        result.setMinDuration(stats.getMin());

        if (stats.getAverage() > config.maxAverageDurationMs) {
            result.addError(
                    String.format(
                            "Average duration %.0fms exceeds threshold %dms",
                            stats.getAverage(), config.maxAverageDurationMs));
        }

        if (stats.getMax() > config.maxDurationMs) {
            result.addWarning(
                    String.format(
                            "Max duration %.0fms exceeds threshold %dms",
                            stats.getMax(), config.maxDurationMs));
        }

        // Calculate variance
        if (stats.getCount() > 1) {
            double mean = stats.getAverage();
            double variance =
                    history.getSnapshots().stream()
                            .mapToDouble(r -> Math.pow(r.getDuration() - mean, 2))
                            .average()
                            .orElse(0);

            double stdDev = Math.sqrt(variance);
            double coefficientOfVariation = stdDev / mean;

            result.setDurationVariance(coefficientOfVariation);

            if (coefficientOfVariation > config.maxDurationVariance) {
                result.addWarning(
                        String.format(
                                "Duration variance %.2f exceeds threshold %.2f",
                                coefficientOfVariation, config.maxDurationVariance));
            }
        }
    }

    private void detectAnomalies(
            ActionHistory history, ValidationConfig config, ValidationResult result) {
        if (history.getSnapshots().size() < 3) return;

        // Detect duration anomalies using IQR method
        List<Double> durations =
                history.getSnapshots().stream()
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

        List<ActionRecord> anomalies =
                history.getSnapshots().stream()
                        .filter(r -> r.getDuration() < lowerBound || r.getDuration() > upperBound)
                        .collect(Collectors.toList());

        if (!anomalies.isEmpty()) {
            result.setAnomalyCount(anomalies.size());
            result.addWarning(
                    String.format("Detected %d duration anomalies (outliers)", anomalies.size()));

            for (ActionRecord anomaly : anomalies) {
                String timestamp =
                        anomaly.getTimeStamp() != null
                                ? anomaly.getTimeStamp()
                                        .format(DateTimeFormatter.ISO_LOCAL_DATE_TIME)
                                : "unknown";
                result.addAnomaly(
                        String.format(
                                "Anomaly at %s: duration %.0fms",
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
            result.addError(
                    String.format(
                            "Detected %d consecutive failures (max allowed: %d)",
                            maxConsecutiveFailures, config.maxConsecutiveFailures));
        }
    }

    private void detectTrends(
            ActionHistory history, ValidationConfig config, ValidationResult result) {
        if (history.getSnapshots().size() < 10) return;

        // Analyze duration trend
        List<ActionRecord> records = history.getSnapshots();
        int windowSize = Math.min(5, records.size() / 3);

        double earlyAvg =
                records.subList(0, windowSize).stream()
                        .mapToDouble(ActionRecord::getDuration)
                        .average()
                        .orElse(0);

        double lateAvg =
                records.subList(records.size() - windowSize, records.size()).stream()
                        .mapToDouble(ActionRecord::getDuration)
                        .average()
                        .orElse(0);

        double percentChange = ((lateAvg - earlyAvg) / earlyAvg) * 100;

        if (Math.abs(percentChange) > 20) {
            String trend = percentChange > 0 ? "degradation" : "improvement";
            result.addInfo(
                    String.format(
                            "Performance %s detected: %.1f%% change in duration",
                            trend, Math.abs(percentChange)));
        }

        // Analyze success rate trend
        long earlySuccess =
                records.subList(0, windowSize).stream()
                        .filter(ActionRecord::isActionSuccess)
                        .count();

        long lateSuccess =
                records.subList(records.size() - windowSize, records.size()).stream()
                        .filter(ActionRecord::isActionSuccess)
                        .count();

        double earlySuccessRate = (double) earlySuccess / windowSize * 100;
        double lateSuccessRate = (double) lateSuccess / windowSize * 100;

        if (Math.abs(lateSuccessRate - earlySuccessRate) > 10) {
            String trend = lateSuccessRate > earlySuccessRate ? "improving" : "degrading";
            result.addInfo(
                    String.format(
                            "Success rate %s: %.1f%% -> %.1f%%",
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
        DoubleSummaryStatistics baselineStats =
                baseline.getSnapshots().stream()
                        .mapToDouble(ActionRecord::getDuration)
                        .summaryStatistics();

        DoubleSummaryStatistics currentStats =
                current.getSnapshots().stream()
                        .mapToDouble(ActionRecord::getDuration)
                        .summaryStatistics();

        // Success rates
        double baselineSuccess =
                baseline.getSnapshots().stream().filter(ActionRecord::isActionSuccess).count()
                        / (double) baseline.getSnapshots().size()
                        * 100;

        double currentSuccess =
                current.getSnapshots().stream().filter(ActionRecord::isActionSuccess).count()
                        / (double) current.getSnapshots().size()
                        * 100;

        // Set comparison values
        result.setBaselineAvgDuration(baselineStats.getAverage());
        result.setCurrentAvgDuration(currentStats.getAverage());
        result.setDurationChange(
                (currentStats.getAverage() - baselineStats.getAverage())
                        / baselineStats.getAverage()
                        * 100);

        result.setBaselineSuccessRate(baselineSuccess);
        result.setCurrentSuccessRate(currentSuccess);
        result.setSuccessRateChange(currentSuccess - baselineSuccess);

        // Determine if regression
        if (currentStats.getAverage() > baselineStats.getAverage() * 1.2) {
            result.setRegression(true);
            result.addIssue(
                    "Performance regression: duration increased by "
                            + String.format("%.1f%%", result.getDurationChange()));
        }

        if (currentSuccess < baselineSuccess - 5) {
            result.setRegression(true);
            result.addIssue(
                    "Success rate regression: decreased by "
                            + String.format("%.1f%%", Math.abs(result.getSuccessRateChange())));
        }

        return result;
    }

    /**
     * Configuration for performance validation.
     * Provides fluent API for customizing validation thresholds and parameters.
     */
    public static class ValidationConfig {
        public double minSuccessRate = DefaultThresholds.MIN_SUCCESS_RATE;
        public long maxAverageDurationMs = DefaultThresholds.MAX_AVERAGE_DURATION_MS;
        public long maxDurationMs = DefaultThresholds.MAX_DURATION_MS;
        public double maxDurationVariance = DefaultThresholds.MAX_DURATION_VARIANCE;
        public int minSamples = DefaultThresholds.MIN_SAMPLES;
        public int maxConsecutiveFailures = 3;

        /**
         * Creates a validation config with default thresholds.
         *
         * @return new ValidationConfig with default values
         */
        public static ValidationConfig getDefault() {
            return new ValidationConfig();
        }

        /**
         * Sets the minimum acceptable success rate percentage.
         *
         * @param rate minimum success rate (0-100)
         * @return this config for method chaining
         */
        public ValidationConfig withMinSuccessRate(double rate) {
            this.minSuccessRate = rate;
            return this;
        }

        /**
         * Sets the maximum acceptable average duration in milliseconds.
         *
         * @param ms maximum average duration in milliseconds
         * @return this config for method chaining
         */
        public ValidationConfig withMaxAverageDuration(long ms) {
            this.maxAverageDurationMs = ms;
            return this;
        }

        /**
         * Sets the maximum acceptable single operation duration.
         *
         * @param ms maximum duration in milliseconds
         * @return this config for method chaining
         */
        public ValidationConfig withMaxDuration(long ms) {
            this.maxDurationMs = ms;
            return this;
        }

        /**
         * Sets the minimum number of samples required for reliable validation.
         *
         * @param samples minimum sample count
         * @return this config for method chaining
         */
        public ValidationConfig withMinSamples(int samples) {
            this.minSamples = samples;
            return this;
        }

        /**
         * Sets the maximum allowed consecutive failures.
         *
         * @param failures maximum consecutive failure count
         * @return this config for method chaining
         */
        public ValidationConfig withMaxConsecutiveFailures(int failures) {
            this.maxConsecutiveFailures = failures;
            return this;
        }

        /**
         * Sets the maximum acceptable duration variance (coefficient of variation).
         *
         * @param variance maximum variance (0.0-1.0)
         * @return this config for method chaining
         */
        public ValidationConfig withMaxDurationVariance(double variance) {
            this.maxDurationVariance = variance;
            return this;
        }
    }

    /**
     * Result of performance validation containing metrics and findings.
     * Includes errors, warnings, anomalies, and performance metrics.
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

        /**
         * Adds an error to the validation result and marks it as invalid.
         *
         * @param error error message to add
         */
        public void addError(String error) {
            errors.add(error);
            valid = false;
        }

        /**
         * Adds a warning message to the validation result.
         *
         * @param warning warning message to add
         */
        public void addWarning(String warning) {
            warnings.add(warning);
        }

        /**
         * Adds an informational message to the validation result.
         *
         * @param information info message to add
         */
        public void addInfo(String information) {
            info.add(information);
        }

        /**
         * Records a detected performance anomaly.
         *
         * @param anomaly anomaly description to add
         */
        public void addAnomaly(String anomaly) {
            anomalies.add(anomaly);
        }

        // Getters and setters
        /**
         * Checks if the validation passed all checks.
         *
         * @return true if valid with no errors, false otherwise
         */
        public boolean isValid() {
            return valid && errors.isEmpty();
        }

        /**
         * Returns all validation errors.
         *
         * @return list of error messages
         */
        public List<String> getErrors() {
            return errors;
        }

        /**
         * Returns all validation warnings.
         *
         * @return list of warning messages
         */
        public List<String> getWarnings() {
            return warnings;
        }

        /**
         * Returns all informational messages.
         *
         * @return list of info messages
         */
        public List<String> getInfo() {
            return info;
        }

        /**
         * Returns all detected anomalies.
         *
         * @return list of anomaly descriptions
         */
        public List<String> getAnomalies() {
            return anomalies;
        }

        /**
         * Returns the calculated success rate percentage.
         *
         * @return success rate (0-100)
         */
        public double getSuccessRate() {
            return successRate;
        }

        /**
         * Sets the calculated success rate.
         *
         * @param rate success rate percentage (0-100)
         */
        public void setSuccessRate(double rate) {
            this.successRate = rate;
        }

        /**
         * Returns the average duration of all operations.
         *
         * @return average duration in milliseconds
         */
        public double getAverageDuration() {
            return averageDuration;
        }

        /**
         * Sets the average duration metric.
         *
         * @param duration average duration in milliseconds
         */
        public void setAverageDuration(double duration) {
            this.averageDuration = duration;
        }

        /**
         * Returns the maximum duration observed.
         *
         * @return maximum duration in milliseconds
         */
        public double getMaxDuration() {
            return maxDuration;
        }

        /**
         * Sets the maximum duration metric.
         *
         * @param duration maximum duration in milliseconds
         */
        public void setMaxDuration(double duration) {
            this.maxDuration = duration;
        }

        /**
         * Returns the minimum duration observed.
         *
         * @return minimum duration in milliseconds
         */
        public double getMinDuration() {
            return minDuration;
        }

        /**
         * Sets the minimum duration metric.
         *
         * @param duration minimum duration in milliseconds
         */
        public void setMinDuration(double duration) {
            this.minDuration = duration;
        }

        /**
         * Returns the coefficient of variation for duration.
         *
         * @return duration variance (0.0-1.0)
         */
        public double getDurationVariance() {
            return durationVariance;
        }

        /**
         * Sets the duration variance metric.
         *
         * @param variance coefficient of variation (0.0-1.0)
         */
        public void setDurationVariance(double variance) {
            this.durationVariance = variance;
        }

        /**
         * Returns the number of anomalies detected.
         *
         * @return anomaly count
         */
        public int getAnomalyCount() {
            return anomalyCount;
        }

        /**
         * Sets the anomaly count.
         *
         * @param count number of anomalies
         */
        public void setAnomalyCount(int count) {
            this.anomalyCount = count;
        }

        @Override
        public String toString() {
            StringBuilder sb = new StringBuilder();
            sb.append("ValidationResult{\n");
            sb.append("  valid=").append(isValid()).append(",\n");
            sb.append("  successRate=").append(String.format("%.1f%%", successRate)).append(",\n");
            sb.append("  avgDuration=")
                    .append(String.format("%.0fms", averageDuration))
                    .append(",\n");
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

    /** Result of comparing two ActionHistory instances. */
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
        public double getBaselineAvgDuration() {
            return baselineAvgDuration;
        }

        public void setBaselineAvgDuration(double duration) {
            this.baselineAvgDuration = duration;
        }

        public double getCurrentAvgDuration() {
            return currentAvgDuration;
        }

        public void setCurrentAvgDuration(double duration) {
            this.currentAvgDuration = duration;
        }

        public double getDurationChange() {
            return durationChange;
        }

        public void setDurationChange(double change) {
            this.durationChange = change;
        }

        public double getBaselineSuccessRate() {
            return baselineSuccessRate;
        }

        public void setBaselineSuccessRate(double rate) {
            this.baselineSuccessRate = rate;
        }

        public double getCurrentSuccessRate() {
            return currentSuccessRate;
        }

        public void setCurrentSuccessRate(double rate) {
            this.currentSuccessRate = rate;
        }

        public double getSuccessRateChange() {
            return successRateChange;
        }

        public void setSuccessRateChange(double change) {
            this.successRateChange = change;
        }

        public boolean isRegression() {
            return regression;
        }

        public void setRegression(boolean regression) {
            this.regression = regression;
        }

        public List<String> getIssues() {
            return issues;
        }

        @Override
        public String toString() {
            StringBuilder sb = new StringBuilder();
            sb.append("ComparisonResult{\n");
            sb.append("  Duration: ")
                    .append(
                            String.format(
                                    "%.0fms -> %.0fms (%.1f%% change)\n",
                                    baselineAvgDuration, currentAvgDuration, durationChange));
            sb.append("  Success: ")
                    .append(
                            String.format(
                                    "%.1f%% -> %.1f%% (%.1f%% change)\n",
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
