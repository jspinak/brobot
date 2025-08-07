package io.github.jspinak.brobot.tools.migration;

import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.io.FileWriter;
import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicLong;
import java.util.stream.Collectors;

/**
 * Tracks usage of deprecated ActionHistory methods to help guide migration efforts.
 * 
 * <p>This component collects metrics on deprecated API usage, helping to:
 * <ul>
 *   <li>Identify which deprecated methods are still being used</li>
 *   <li>Track migration progress over time</li>
 *   <li>Generate reports for decision making</li>
 *   <li>Alert when deprecated usage exceeds thresholds</li>
 * </ul>
 * 
 * <p>Metrics are collected in-memory and periodically written to disk for analysis.
 * 
 * @since 1.2.0
 */
@Component
@Slf4j
@Getter
public class DeprecationMetrics {
    
    private static final DeprecationMetrics INSTANCE = new DeprecationMetrics();
    
    /**
     * Tracks usage count for each deprecated method.
     */
    private final Map<String, AtomicLong> methodUsageCount = new ConcurrentHashMap<>();
    
    /**
     * Tracks unique call sites (stack traces) for each deprecated method.
     */
    private final Map<String, Map<String, AtomicLong>> callSiteUsage = new ConcurrentHashMap<>();
    
    /**
     * Tracks first usage time for each deprecated method.
     */
    private final Map<String, LocalDateTime> firstUsageTime = new ConcurrentHashMap<>();
    
    /**
     * Tracks last usage time for each deprecated method.
     */
    private final Map<String, LocalDateTime> lastUsageTime = new ConcurrentHashMap<>();
    
    /**
     * Total number of deprecated API calls.
     */
    private final AtomicLong totalDeprecatedCalls = new AtomicLong(0);
    
    /**
     * Threshold for warning about excessive deprecated usage.
     */
    private static final long WARNING_THRESHOLD = 1000;
    
    /**
     * Get singleton instance for static access.
     */
    public static DeprecationMetrics getInstance() {
        return INSTANCE;
    }
    
    /**
     * Log usage of a deprecated method.
     * 
     * @param methodName the deprecated method name
     * @param parameterType the parameter type (e.g., "ActionOptions.Action")
     */
    public static void log(String methodName, String parameterType) {
        getInstance().logUsage(methodName, parameterType);
    }
    
    /**
     * Log usage of a deprecated method with stack trace analysis.
     * 
     * @param methodName the deprecated method name
     * @param parameterType the parameter type
     */
    public void logUsage(String methodName, String parameterType) {
        String fullMethodName = methodName + "(" + parameterType + ")";
        
        // Increment usage count
        methodUsageCount.computeIfAbsent(fullMethodName, k -> new AtomicLong(0))
            .incrementAndGet();
        
        // Track timing
        LocalDateTime now = LocalDateTime.now();
        firstUsageTime.putIfAbsent(fullMethodName, now);
        lastUsageTime.put(fullMethodName, now);
        
        // Track call site
        String callSite = getCallSite();
        callSiteUsage.computeIfAbsent(fullMethodName, k -> new ConcurrentHashMap<>())
            .computeIfAbsent(callSite, k -> new AtomicLong(0))
            .incrementAndGet();
        
        // Increment total
        long total = totalDeprecatedCalls.incrementAndGet();
        
        // Log warning if threshold exceeded
        if (total % WARNING_THRESHOLD == 0) {
            log.warn("Deprecated API usage has reached {} calls. Consider migrating to modern API.", total);
            log.warn("Most used deprecated method: {}", getMostUsedMethod());
        }
        
        // Log debug for individual usage
        log.debug("Deprecated method called: {} from {}", fullMethodName, callSite);
    }
    
    /**
     * Get the call site from the stack trace.
     * 
     * @return formatted call site string
     */
    private String getCallSite() {
        StackTraceElement[] stack = Thread.currentThread().getStackTrace();
        
        // Skip this class and the deprecated method itself
        for (int i = 3; i < stack.length; i++) {
            StackTraceElement element = stack[i];
            String className = element.getClassName();
            
            // Skip framework classes
            if (!className.startsWith("io.github.jspinak.brobot.model.action") &&
                !className.startsWith("java.") &&
                !className.startsWith("sun.")) {
                
                return String.format("%s.%s(%s:%d)",
                    element.getClassName(),
                    element.getMethodName(),
                    element.getFileName(),
                    element.getLineNumber());
            }
        }
        
        return "Unknown";
    }
    
    /**
     * Get the most frequently used deprecated method.
     * 
     * @return method name and usage count
     */
    public String getMostUsedMethod() {
        return methodUsageCount.entrySet().stream()
            .max(Map.Entry.comparingByValue((a, b) -> Long.compare(a.get(), b.get())))
            .map(e -> e.getKey() + " (" + e.getValue().get() + " calls)")
            .orElse("None");
    }
    
    /**
     * Generate a detailed usage report.
     * 
     * @return formatted report string
     */
    public String generateReport() {
        StringBuilder report = new StringBuilder();
        report.append("=== Deprecation Usage Report ===\n");
        report.append("Generated: ").append(LocalDateTime.now()).append("\n");
        report.append("Total deprecated calls: ").append(totalDeprecatedCalls.get()).append("\n\n");
        
        // Sort by usage count
        methodUsageCount.entrySet().stream()
            .sorted(Map.Entry.<String, AtomicLong>comparingByValue(
                (a, b) -> Long.compare(b.get(), a.get())))
            .forEach(entry -> {
                String method = entry.getKey();
                long count = entry.getValue().get();
                
                report.append("Method: ").append(method).append("\n");
                report.append("  Usage count: ").append(count).append("\n");
                report.append("  First used: ").append(firstUsageTime.get(method)).append("\n");
                report.append("  Last used: ").append(lastUsageTime.get(method)).append("\n");
                
                // Top call sites
                Map<String, AtomicLong> sites = callSiteUsage.get(method);
                if (sites != null && !sites.isEmpty()) {
                    report.append("  Top call sites:\n");
                    sites.entrySet().stream()
                        .sorted(Map.Entry.<String, AtomicLong>comparingByValue(
                            (a, b) -> Long.compare(b.get(), a.get())))
                        .limit(3)
                        .forEach(site -> {
                            report.append("    - ").append(site.getKey())
                                  .append(" (").append(site.getValue().get()).append(" calls)\n");
                        });
                }
                report.append("\n");
            });
        
        // Migration recommendations
        report.append("=== Migration Recommendations ===\n");
        if (totalDeprecatedCalls.get() > WARNING_THRESHOLD * 10) {
            report.append("CRITICAL: Very high deprecated API usage detected.\n");
            report.append("Immediate migration recommended for: ").append(getMostUsedMethod()).append("\n");
        } else if (totalDeprecatedCalls.get() > WARNING_THRESHOLD) {
            report.append("WARNING: Significant deprecated API usage detected.\n");
            report.append("Consider planning migration for frequently used methods.\n");
        } else {
            report.append("INFO: Low deprecated API usage. Migration can be scheduled normally.\n");
        }
        
        return report.toString();
    }
    
    /**
     * Write report to file.
     * 
     * @param directory directory to write report to
     * @return path to written report
     * @throws IOException if write fails
     */
    public Path writeReport(Path directory) throws IOException {
        String timestamp = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMdd_HHmmss"));
        Path reportPath = directory.resolve("deprecation_report_" + timestamp + ".txt");
        
        try (FileWriter writer = new FileWriter(reportPath.toFile())) {
            writer.write(generateReport());
        }
        
        log.info("Deprecation report written to: {}", reportPath);
        return reportPath;
    }
    
    /**
     * Scheduled task to periodically log metrics summary.
     * Runs every hour in production.
     */
    @Scheduled(fixedDelay = 3600000) // 1 hour
    public void logMetricsSummary() {
        if (totalDeprecatedCalls.get() > 0) {
            log.info("Deprecation metrics summary: {} total calls, {} unique methods",
                    totalDeprecatedCalls.get(), methodUsageCount.size());
            log.info("Most used deprecated method: {}", getMostUsedMethod());
            
            // Write report if usage is high
            if (totalDeprecatedCalls.get() > WARNING_THRESHOLD) {
                try {
                    Path reportDir = Paths.get(System.getProperty("user.home"), ".brobot", "reports");
                    reportDir.toFile().mkdirs();
                    writeReport(reportDir);
                } catch (IOException e) {
                    log.error("Failed to write deprecation report: {}", e.getMessage());
                }
            }
        }
    }
    
    /**
     * Reset all metrics (useful for testing).
     */
    public void reset() {
        methodUsageCount.clear();
        callSiteUsage.clear();
        firstUsageTime.clear();
        lastUsageTime.clear();
        totalDeprecatedCalls.set(0);
        log.info("Deprecation metrics reset");
    }
    
    /**
     * Get migration progress percentage based on modern vs deprecated API usage.
     * 
     * @param modernApiCalls number of modern API calls
     * @return percentage of modern API usage (0-100)
     */
    public double getMigrationProgress(long modernApiCalls) {
        long deprecatedCalls = totalDeprecatedCalls.get();
        if (deprecatedCalls == 0 && modernApiCalls == 0) {
            return 100.0; // No usage means fully migrated
        }
        if (deprecatedCalls == 0) {
            return 100.0; // Only modern API used
        }
        long total = deprecatedCalls + modernApiCalls;
        return (double) modernApiCalls / total * 100;
    }
    
    /**
     * Check if a specific method should be removed based on usage.
     * 
     * @param methodName the method to check
     * @param daysSinceLastUse days since the method was last used
     * @return true if method can be safely removed
     */
    public boolean canRemoveMethod(String methodName, long daysSinceLastUse) {
        AtomicLong usage = methodUsageCount.get(methodName);
        if (usage == null || usage.get() == 0) {
            return true; // Never used, safe to remove
        }
        
        LocalDateTime lastUse = lastUsageTime.get(methodName);
        if (lastUse == null) {
            return true; // No recorded usage
        }
        
        long daysSince = java.time.Duration.between(lastUse, LocalDateTime.now()).toDays();
        return daysSince > daysSinceLastUse && usage.get() < 100; // Low usage and not recent
    }
}