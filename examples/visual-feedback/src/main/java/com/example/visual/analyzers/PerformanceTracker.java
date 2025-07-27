package com.example.visual.analyzers;

import io.github.jspinak.brobot.action.ActionConfig;
import io.github.jspinak.brobot.action.ActionResult;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.time.Duration;
import java.time.Instant;
import java.time.LocalDateTime;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicLong;
import java.util.stream.Collectors;

/**
 * Performance tracking and monitoring component.
 * Collects, analyzes, and reports on automation performance metrics
 * including execution times, resource usage, and trend analysis.
 */
@Component
@Slf4j
public class PerformanceTracker {
    
    private final Map<String, PerformanceMetrics> actionMetrics = new ConcurrentHashMap<>();
    private final List<PerformanceSnapshot> snapshots = Collections.synchronizedList(new ArrayList<>());
    private final AtomicLong totalActions = new AtomicLong(0);
    private final AtomicLong successfulActions = new AtomicLong(0);
    
    /**
     * Records performance data for an action execution
     */
    public void trackAction(ActionConfig config, ActionResult result) {
        totalActions.incrementAndGet();
        
        if (result.isSuccess()) {
            successfulActions.incrementAndGet();
        }
        
        String actionType = config.getClass().getSimpleName();
        
        // Update action-specific metrics
        actionMetrics.computeIfAbsent(actionType, k -> new PerformanceMetrics())
                .addExecution(result);
        
        // Create performance snapshot
        PerformanceSnapshot snapshot = PerformanceSnapshot.builder()
            .timestamp(Instant.now())
            .actionType(actionType)
            .executionTime(result.getDuration())
            .success(result.isSuccess())
            .confidence(result.getBestMatch() != null ? result.getBestMatch().getScore() : 0.0)
            .memoryUsage(getCurrentMemoryUsage())
            .cpuUsage(getCurrentCpuUsage())
            .build();
        
        snapshots.add(snapshot);
        
        // Cleanup old snapshots (keep last 1000)
        if (snapshots.size() > 1000) {
            snapshots.removeAll(snapshots.subList(0, snapshots.size() - 1000));
        }
        
        // Log performance if it's concerning
        checkPerformanceThresholds(result, actionType);
    }
    
    /**
     * Generates a comprehensive performance report
     */
    public String generateReport(Duration timeWindow) {
        Instant cutoff = Instant.now().minus(timeWindow);
        
        List<PerformanceSnapshot> recentSnapshots = snapshots.stream()
            .filter(s -> s.getTimestamp().isAfter(cutoff))
            .collect(Collectors.toList());
        
        if (recentSnapshots.isEmpty()) {
            return "No performance data available for the specified time window.";
        }
        
        StringBuilder report = new StringBuilder();
        report.append("=== Performance Report ===\n");
        report.append("Time Window: ").append(formatDuration(timeWindow)).append("\n");
        report.append("Generated: ").append(LocalDateTime.now()).append("\n\n");
        
        // Overall statistics
        appendOverallStats(report, recentSnapshots);
        
        // Action type breakdown
        appendActionTypeBreakdown(report, recentSnapshots);
        
        // Performance trends
        appendPerformanceTrends(report, recentSnapshots);
        
        // Resource utilization
        appendResourceUtilization(report, recentSnapshots);
        
        // Recommendations
        appendRecommendations(report, recentSnapshots);
        
        return report.toString();
    }
    
    /**
     * Gets current performance statistics
     */
    public PerformanceStats getCurrentStats() {
        Instant now = Instant.now();
        Instant oneHourAgo = now.minus(Duration.ofHours(1));
        
        List<PerformanceSnapshot> recentSnapshots = snapshots.stream()
            .filter(s -> s.getTimestamp().isAfter(oneHourAgo))
            .collect(Collectors.toList());
        
        double successRate = totalActions.get() > 0 
            ? (double) successfulActions.get() / totalActions.get() 
            : 0.0;
        
        double avgExecutionTime = recentSnapshots.stream()
            .mapToLong(s -> s.getExecutionTime().toMillis())
            .average()
            .orElse(0.0);
        
        double avgConfidence = recentSnapshots.stream()
            .filter(s -> s.getConfidence() > 0)
            .mapToDouble(PerformanceSnapshot::getConfidence)
            .average()
            .orElse(0.0);
        
        return PerformanceStats.builder()
            .totalActions(totalActions.get())
            .successfulActions(successfulActions.get())
            .successRate(successRate)
            .averageExecutionTime(Duration.ofMillis((long) avgExecutionTime))
            .averageConfidence(avgConfidence)
            .currentMemoryUsage(getCurrentMemoryUsage())
            .currentCpuUsage(getCurrentCpuUsage())
            .build();
    }
    
    /**
     * Detects performance anomalies in recent executions
     */
    public List<PerformanceAnomaly> detectAnomalies() {
        List<PerformanceAnomaly> anomalies = new ArrayList<>();
        
        // Check for sudden performance degradation
        if (detectPerformanceDegradation()) {
            anomalies.add(new PerformanceAnomaly(
                "PERFORMANCE_DEGRADATION",
                "Recent actions are significantly slower than baseline",
                "Consider checking system resources or optimizing patterns"
            ));
        }
        
        // Check for memory leaks
        if (detectMemoryLeak()) {
            anomalies.add(new PerformanceAnomaly(
                "MEMORY_LEAK",
                "Memory usage is consistently increasing",
                "Review action implementations for resource cleanup"
            ));
        }
        
        // Check for high failure rate
        if (detectHighFailureRate()) {
            anomalies.add(new PerformanceAnomaly(
                "HIGH_FAILURE_RATE",
                "Action failure rate is above normal threshold",
                "Review target patterns and system stability"
            ));
        }
        
        return anomalies;
    }
    
    /**
     * Gets performance trends over time
     */
    public Map<String, Object> getPerformanceTrends() {
        Map<String, Object> trends = new HashMap<>();
        
        // Group snapshots by hour for trend analysis
        Map<Integer, List<PerformanceSnapshot>> hourlyData = snapshots.stream()
            .collect(Collectors.groupingBy(s -> s.getTimestamp().atZone(
                java.time.ZoneId.systemDefault()).getHour()));
        
        // Calculate hourly averages
        Map<Integer, Double> hourlyExecutionTimes = hourlyData.entrySet().stream()
            .collect(Collectors.toMap(
                Map.Entry::getKey,
                entry -> entry.getValue().stream()
                    .mapToLong(s -> s.getExecutionTime().toMillis())
                    .average()
                    .orElse(0.0)
            ));
        
        Map<Integer, Double> hourlySuccessRates = hourlyData.entrySet().stream()
            .collect(Collectors.toMap(
                Map.Entry::getKey,
                entry -> entry.getValue().stream()
                    .mapToDouble(s -> s.isSuccess() ? 1.0 : 0.0)
                    .average()
                    .orElse(0.0)
            ));
        
        trends.put("hourlyExecutionTimes", hourlyExecutionTimes);
        trends.put("hourlySuccessRates", hourlySuccessRates);
        trends.put("totalDataPoints", snapshots.size());
        
        return trends;
    }
    
    /**
     * Helper methods for report generation
     */
    private void appendOverallStats(StringBuilder report, List<PerformanceSnapshot> snapshots) {
        report.append("--- Overall Statistics ---\n");
        
        long totalExecutions = snapshots.size();
        long successfulExecutions = snapshots.stream()
            .mapToLong(s -> s.isSuccess() ? 1 : 0)
            .sum();
        
        double successRate = totalExecutions > 0 
            ? (double) successfulExecutions / totalExecutions * 100 
            : 0.0;
        
        double avgExecutionTime = snapshots.stream()
            .mapToLong(s -> s.getExecutionTime().toMillis())
            .average()
            .orElse(0.0);
        
        double avgConfidence = snapshots.stream()
            .filter(s -> s.getConfidence() > 0)
            .mapToDouble(PerformanceSnapshot::getConfidence)
            .average()
            .orElse(0.0);
        
        report.append(String.format("Total Executions: %d\n", totalExecutions));
        report.append(String.format("Successful: %d (%.1f%%)\n", successfulExecutions, successRate));
        report.append(String.format("Average Execution Time: %.0f ms\n", avgExecutionTime));
        report.append(String.format("Average Confidence: %.1f%%\n", avgConfidence * 100));
        report.append("\n");
    }
    
    private void appendActionTypeBreakdown(StringBuilder report, List<PerformanceSnapshot> snapshots) {
        report.append("--- Action Type Breakdown ---\n");
        
        Map<String, List<PerformanceSnapshot>> byType = snapshots.stream()
            .collect(Collectors.groupingBy(PerformanceSnapshot::getActionType));
        
        byType.forEach((type, typeSnapshots) -> {
            double avgTime = typeSnapshots.stream()
                .mapToLong(s -> s.getExecutionTime().toMillis())
                .average()
                .orElse(0.0);
            
            long successCount = typeSnapshots.stream()
                .mapToLong(s -> s.isSuccess() ? 1 : 0)
                .sum();
            
            double successRate = (double) successCount / typeSnapshots.size() * 100;
            
            report.append(String.format("%s: %d executions, %.0f ms avg, %.1f%% success\n",
                type, typeSnapshots.size(), avgTime, successRate));
        });
        
        report.append("\n");
    }
    
    private void appendPerformanceTrends(StringBuilder report, List<PerformanceSnapshot> snapshots) {
        report.append("--- Performance Trends ---\n");
        
        if (snapshots.size() < 10) {
            report.append("Insufficient data for trend analysis\n\n");
            return;
        }
        
        // Split into first and second half for comparison
        int midpoint = snapshots.size() / 2;
        List<PerformanceSnapshot> firstHalf = snapshots.subList(0, midpoint);
        List<PerformanceSnapshot> secondHalf = snapshots.subList(midpoint, snapshots.size());
        
        double firstHalfAvgTime = firstHalf.stream()
            .mapToLong(s -> s.getExecutionTime().toMillis())
            .average()
            .orElse(0.0);
        
        double secondHalfAvgTime = secondHalf.stream()
            .mapToLong(s -> s.getExecutionTime().toMillis())
            .average()
            .orElse(0.0);
        
        double timeChange = ((secondHalfAvgTime - firstHalfAvgTime) / firstHalfAvgTime) * 100;
        
        report.append(String.format("Execution Time Trend: %.1f%% %s\n", 
            Math.abs(timeChange), timeChange > 0 ? "increase" : "decrease"));
        
        report.append("\n");
    }
    
    private void appendResourceUtilization(StringBuilder report, List<PerformanceSnapshot> snapshots) {
        report.append("--- Resource Utilization ---\n");
        
        double avgMemory = snapshots.stream()
            .filter(s -> s.getMemoryUsage() > 0)
            .mapToDouble(PerformanceSnapshot::getMemoryUsage)
            .average()
            .orElse(0.0);
        
        double maxMemory = snapshots.stream()
            .mapToDouble(PerformanceSnapshot::getMemoryUsage)
            .max()
            .orElse(0.0);
        
        double avgCpu = snapshots.stream()
            .filter(s -> s.getCpuUsage() > 0)
            .mapToDouble(PerformanceSnapshot::getCpuUsage)
            .average()
            .orElse(0.0);
        
        report.append(String.format("Average Memory Usage: %.1f MB\n", avgMemory));
        report.append(String.format("Peak Memory Usage: %.1f MB\n", maxMemory));
        report.append(String.format("Average CPU Usage: %.1f%%\n", avgCpu));
        report.append("\n");
    }
    
    private void appendRecommendations(StringBuilder report, List<PerformanceSnapshot> snapshots) {
        report.append("--- Recommendations ---\n");
        
        List<String> recommendations = generatePerformanceRecommendations(snapshots);
        
        if (recommendations.isEmpty()) {
            report.append("No specific recommendations - performance appears normal\n");
        } else {
            recommendations.forEach(rec -> report.append("• ").append(rec).append("\n"));
        }
        
        report.append("\n");
    }
    
    private List<String> generatePerformanceRecommendations(List<PerformanceSnapshot> snapshots) {
        List<String> recommendations = new ArrayList<>();
        
        double avgExecutionTime = snapshots.stream()
            .mapToLong(s -> s.getExecutionTime().toMillis())
            .average()
            .orElse(0.0);
        
        if (avgExecutionTime > 3000) {
            recommendations.add("Consider optimizing search patterns or reducing search areas");
        }
        
        double avgConfidence = snapshots.stream()
            .filter(s -> s.getConfidence() > 0)
            .mapToDouble(PerformanceSnapshot::getConfidence)
            .average()
            .orElse(0.0);
        
        if (avgConfidence < 0.8) {
            recommendations.add("Review and update pattern images for better matching accuracy");
        }
        
        long failureCount = snapshots.stream()
            .mapToLong(s -> s.isSuccess() ? 0 : 1)
            .sum();
        
        double failureRate = (double) failureCount / snapshots.size();
        if (failureRate > 0.1) {
            recommendations.add("High failure rate detected - review target accessibility and timing");
        }
        
        return recommendations;
    }
    
    private void checkPerformanceThresholds(ActionResult result, String actionType) {
        Duration executionTime = result.getDuration();
        
        if (executionTime.toMillis() > 5000) {
            log.warn("Slow action detected: {} took {}ms", actionType, executionTime.toMillis());
        }
        
        if (!result.isSuccess()) {
            log.warn("Action failed: {} - {}", actionType, result.getErrorMessage());
        }
        
        if (result.getBestMatch() != null && result.getBestMatch().getScore() < 0.7) {
            log.warn("Low confidence match: {} - {}%", actionType, 
                result.getBestMatch().getScore() * 100);
        }
    }
    
    private boolean detectPerformanceDegradation() {
        if (snapshots.size() < 20) return false;
        
        // Compare recent 10 with earlier 10
        List<PerformanceSnapshot> recent = snapshots.subList(
            snapshots.size() - 10, snapshots.size());
        List<PerformanceSnapshot> earlier = snapshots.subList(
            Math.max(0, snapshots.size() - 30), snapshots.size() - 20);
        
        if (earlier.isEmpty()) return false;
        
        double recentAvg = recent.stream()
            .mapToLong(s -> s.getExecutionTime().toMillis())
            .average()
            .orElse(0.0);
        
        double earlierAvg = earlier.stream()
            .mapToLong(s -> s.getExecutionTime().toMillis())
            .average()
            .orElse(0.0);
        
        return recentAvg > earlierAvg * 1.5; // 50% slower
    }
    
    private boolean detectMemoryLeak() {
        if (snapshots.size() < 10) return false;
        
        // Check if memory usage is consistently increasing
        List<Double> recentMemory = snapshots.stream()
            .skip(snapshots.size() - 10)
            .mapToDouble(PerformanceSnapshot::getMemoryUsage)
            .boxed()
            .collect(Collectors.toList());
        
        // Simple trend detection
        double first = recentMemory.get(0);
        double last = recentMemory.get(recentMemory.size() - 1);
        
        return last > first * 1.3; // 30% increase
    }
    
    private boolean detectHighFailureRate() {
        if (snapshots.size() < 10) return false;
        
        long recentFailures = snapshots.stream()
            .skip(snapshots.size() - 10)
            .mapToLong(s -> s.isSuccess() ? 0 : 1)
            .sum();
        
        return recentFailures > 3; // More than 30% failure rate
    }
    
    private double getCurrentMemoryUsage() {
        Runtime runtime = Runtime.getRuntime();
        return (runtime.totalMemory() - runtime.freeMemory()) / (1024.0 * 1024.0); // MB
    }
    
    private double getCurrentCpuUsage() {
        // Simplified CPU usage - in real implementation would use more sophisticated method
        return Math.random() * 50 + 10; // Mock 10-60% usage
    }
    
    private String formatDuration(Duration duration) {
        long hours = duration.toHours();
        long minutes = duration.toMinutesPart();
        return String.format("%dh %dm", hours, minutes);
    }
    
    // Data classes
    public static class PerformanceMetrics {
        private final List<Duration> executionTimes = new ArrayList<>();
        private final AtomicLong successCount = new AtomicLong(0);
        private final AtomicLong totalCount = new AtomicLong(0);
        
        public void addExecution(ActionResult result) {
            executionTimes.add(result.getDuration());
            totalCount.incrementAndGet();
            if (result.isSuccess()) {
                successCount.incrementAndGet();
            }
        }
        
        public double getAverageExecutionTime() {
            return executionTimes.stream()
                .mapToLong(Duration::toMillis)
                .average()
                .orElse(0.0);
        }
        
        public double getSuccessRate() {
            long total = totalCount.get();
            return total > 0 ? (double) successCount.get() / total : 0.0;
        }
    }
    
    public static class PerformanceSnapshot {
        private final Instant timestamp;
        private final String actionType;
        private final Duration executionTime;
        private final boolean success;
        private final double confidence;
        private final double memoryUsage;
        private final double cpuUsage;
        
        private PerformanceSnapshot(Instant timestamp, String actionType, Duration executionTime,
                boolean success, double confidence, double memoryUsage, double cpuUsage) {
            this.timestamp = timestamp;
            this.actionType = actionType;
            this.executionTime = executionTime;
            this.success = success;
            this.confidence = confidence;
            this.memoryUsage = memoryUsage;
            this.cpuUsage = cpuUsage;
        }
        
        public static PerformanceSnapshotBuilder builder() {
            return new PerformanceSnapshotBuilder();
        }
        
        // Getters
        public Instant getTimestamp() { return timestamp; }
        public String getActionType() { return actionType; }
        public Duration getExecutionTime() { return executionTime; }
        public boolean isSuccess() { return success; }
        public double getConfidence() { return confidence; }
        public double getMemoryUsage() { return memoryUsage; }
        public double getCpuUsage() { return cpuUsage; }
        
        public static class PerformanceSnapshotBuilder {
            private Instant timestamp;
            private String actionType;
            private Duration executionTime;
            private boolean success;
            private double confidence;
            private double memoryUsage;
            private double cpuUsage;
            
            public PerformanceSnapshotBuilder timestamp(Instant timestamp) {
                this.timestamp = timestamp;
                return this;
            }
            
            public PerformanceSnapshotBuilder actionType(String actionType) {
                this.actionType = actionType;
                return this;
            }
            
            public PerformanceSnapshotBuilder executionTime(Duration executionTime) {
                this.executionTime = executionTime;
                return this;
            }
            
            public PerformanceSnapshotBuilder success(boolean success) {
                this.success = success;
                return this;
            }
            
            public PerformanceSnapshotBuilder confidence(double confidence) {
                this.confidence = confidence;
                return this;
            }
            
            public PerformanceSnapshotBuilder memoryUsage(double memoryUsage) {
                this.memoryUsage = memoryUsage;
                return this;
            }
            
            public PerformanceSnapshotBuilder cpuUsage(double cpuUsage) {
                this.cpuUsage = cpuUsage;
                return this;
            }
            
            public PerformanceSnapshot build() {
                return new PerformanceSnapshot(timestamp, actionType, executionTime,
                    success, confidence, memoryUsage, cpuUsage);
            }
        }
    }
    
    public static class PerformanceStats {
        public static PerformanceStatsBuilder builder() {
            return new PerformanceStatsBuilder();
        }
        
        public static class PerformanceStatsBuilder {
            public PerformanceStatsBuilder totalActions(long total) { return this; }
            public PerformanceStatsBuilder successfulActions(long successful) { return this; }
            public PerformanceStatsBuilder successRate(double rate) { return this; }
            public PerformanceStatsBuilder averageExecutionTime(Duration time) { return this; }
            public PerformanceStatsBuilder averageConfidence(double confidence) { return this; }
            public PerformanceStatsBuilder currentMemoryUsage(double memory) { return this; }
            public PerformanceStatsBuilder currentCpuUsage(double cpu) { return this; }
            public PerformanceStats build() { return new PerformanceStats(); }
        }
    }
    
    public static class PerformanceAnomaly {
        private final String type;
        private final String description;
        private final String recommendation;
        
        public PerformanceAnomaly(String type, String description, String recommendation) {
            this.type = type;
            this.description = description;
            this.recommendation = recommendation;
        }
        
        public String getType() { return type; }
        public String getDescription() { return description; }
        public String getRecommendation() { return recommendation; }
    }
}