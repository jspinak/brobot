package io.github.jspinak.brobot.runner.diagnostics.services;

import io.github.jspinak.brobot.runner.diagnostics.HealthCheckItem;
import io.github.jspinak.brobot.runner.diagnostics.HealthCheckResult;
import io.github.jspinak.brobot.runner.diagnostics.HealthStatus;
import io.github.jspinak.brobot.runner.errorhandling.ErrorHandler;
import io.github.jspinak.brobot.runner.common.diagnostics.DiagnosticCapable;
import io.github.jspinak.brobot.runner.common.diagnostics.DiagnosticInfo;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.io.File;
import java.lang.management.ManagementFactory;
import java.lang.management.MemoryMXBean;
import java.lang.management.MemoryUsage;
import java.lang.management.ThreadMXBean;
import java.util.*;

/**
 * Service responsible for performing health checks on various system components.
 * Provides comprehensive health status assessment.
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class HealthCheckService implements DiagnosticCapable {
    
    private final MemoryDiagnosticService memoryDiagnosticService;
    private final ThreadDiagnosticService threadDiagnosticService;
    private final PerformanceDiagnosticService performanceDiagnosticService;
    private final ErrorHandler errorHandler;
    
    private final MemoryMXBean memoryBean = ManagementFactory.getMemoryMXBean();
    private final ThreadMXBean threadBean = ManagementFactory.getThreadMXBean();
    
    // Thresholds
    private static final double MEMORY_WARNING_THRESHOLD = 80.0;
    private static final double MEMORY_CRITICAL_THRESHOLD = 90.0;
    private static final int THREAD_WARNING_THRESHOLD = 500;
    private static final int THREAD_CRITICAL_THRESHOLD = 1000;
    private static final double GC_WARNING_THRESHOLD = 5.0;
    private static final double GC_CRITICAL_THRESHOLD = 10.0;
    private static final double ERROR_RATE_WARNING_THRESHOLD = 5.0;
    private static final double ERROR_RATE_CRITICAL_THRESHOLD = 10.0;
    private static final double DISK_SPACE_WARNING_THRESHOLD = 10.0;
    private static final double DISK_SPACE_CRITICAL_THRESHOLD = 5.0;
    
    /**
     * Performs a comprehensive health check on all system components.
     * 
     * @return HealthCheckResult with overall status and individual check results
     */
    public HealthCheckResult performHealthCheck() {
        log.debug("Performing comprehensive health check");
        
        List<HealthCheckItem> checks = new ArrayList<>();
        
        try {
            // Perform individual health checks
            checks.add(checkMemoryHealth());
            checks.add(checkThreadHealth());
            checks.add(checkGCHealth());
            checks.add(checkErrorRate());
            checks.add(checkDiskSpace());
            
            // Add custom checks
            checks.addAll(performCustomChecks());
            
            // Determine overall status
            HealthStatus overallStatus = determineOverallStatus(checks);
            
            return new HealthCheckResult(overallStatus, checks);
            
        } catch (Exception e) {
            log.error("Error performing health check", e);
            checks.add(new HealthCheckItem("Health Check", HealthStatus.WARNING, 
                "Error during health check: " + e.getMessage()));
            return new HealthCheckResult(HealthStatus.WARNING, checks);
        }
    }
    
    /**
     * Checks memory health status.
     * 
     * @return HealthCheckItem for memory status
     */
    public HealthCheckItem checkMemoryHealth() {
        try {
            double heapUsage = memoryDiagnosticService.getHeapUsagePercentage();
            
            HealthStatus status;
            String message;
            
            if (heapUsage >= MEMORY_CRITICAL_THRESHOLD) {
                status = HealthStatus.CRITICAL;
                message = String.format("Critical memory usage: %.1f%%", heapUsage);
            } else if (heapUsage >= MEMORY_WARNING_THRESHOLD) {
                status = HealthStatus.WARNING;
                message = String.format("High memory usage: %.1f%%", heapUsage);
            } else {
                status = HealthStatus.HEALTHY;
                message = String.format("Memory usage normal: %.1f%%", heapUsage);
            }
            
            return new HealthCheckItem("Memory", status, message);
            
        } catch (Exception e) {
            log.error("Error checking memory health", e);
            return new HealthCheckItem("Memory", HealthStatus.WARNING, 
                "Unable to check memory: " + e.getMessage());
        }
    }
    
    /**
     * Checks thread health status.
     * 
     * @return HealthCheckItem for thread status
     */
    public HealthCheckItem checkThreadHealth() {
        try {
            int threadCount = threadBean.getThreadCount();
            long[] deadlocked = threadBean.findDeadlockedThreads();
            
            HealthStatus status;
            String message;
            
            if (deadlocked != null && deadlocked.length > 0) {
                status = HealthStatus.CRITICAL;
                message = String.format("Deadlock detected! %d threads deadlocked", deadlocked.length);
            } else if (threadCount >= THREAD_CRITICAL_THRESHOLD) {
                status = HealthStatus.CRITICAL;
                message = String.format("Critical thread count: %d", threadCount);
            } else if (threadCount >= THREAD_WARNING_THRESHOLD) {
                status = HealthStatus.WARNING;
                message = String.format("High thread count: %d", threadCount);
            } else {
                status = HealthStatus.HEALTHY;
                message = String.format("Thread count normal: %d", threadCount);
            }
            
            return new HealthCheckItem("Threads", status, message);
            
        } catch (Exception e) {
            log.error("Error checking thread health", e);
            return new HealthCheckItem("Threads", HealthStatus.WARNING, 
                "Unable to check threads: " + e.getMessage());
        }
    }
    
    /**
     * Checks garbage collection health.
     * 
     * @return HealthCheckItem for GC status
     */
    public HealthCheckItem checkGCHealth() {
        try {
            double gcOverhead = performanceDiagnosticService.getGCOverheadPercentage();
            
            HealthStatus status;
            String message;
            
            if (gcOverhead >= GC_CRITICAL_THRESHOLD) {
                status = HealthStatus.CRITICAL;
                message = String.format("Critical GC overhead: %.1f%%", gcOverhead);
            } else if (gcOverhead >= GC_WARNING_THRESHOLD) {
                status = HealthStatus.WARNING;
                message = String.format("High GC overhead: %.1f%%", gcOverhead);
            } else {
                status = HealthStatus.HEALTHY;
                message = String.format("GC overhead normal: %.1f%%", gcOverhead);
            }
            
            return new HealthCheckItem("Garbage Collection", status, message);
            
        } catch (Exception e) {
            log.error("Error checking GC health", e);
            return new HealthCheckItem("Garbage Collection", HealthStatus.WARNING, 
                "Unable to check GC: " + e.getMessage());
        }
    }
    
    /**
     * Checks error rate health.
     * 
     * @return HealthCheckItem for error rate status
     */
    public HealthCheckItem checkErrorRate() {
        try {
            // Get error statistics from error handler
            var statistics = errorHandler.getStatistics();
            long totalErrors = statistics.totalErrors();
            
            // Calculate error rate based on runtime (errors per minute)
            long uptimeMinutes = ManagementFactory.getRuntimeMXBean().getUptime() / 60000;
            double errorRate = uptimeMinutes > 0 ? (double) totalErrors / uptimeMinutes : 0.0;
            
            HealthStatus status;
            String message;
            
            if (errorRate >= ERROR_RATE_CRITICAL_THRESHOLD) {
                status = HealthStatus.CRITICAL;
                message = String.format("Critical error rate: %.1f errors/min", errorRate);
            } else if (errorRate >= ERROR_RATE_WARNING_THRESHOLD) {
                status = HealthStatus.WARNING;
                message = String.format("High error rate: %.1f errors/min", errorRate);
            } else {
                status = HealthStatus.HEALTHY;
                message = String.format("Error rate normal: %.1f errors/min", errorRate);
            }
            
            return new HealthCheckItem("Error Rate", status, message);
            
        } catch (Exception e) {
            log.error("Error checking error rate", e);
            return new HealthCheckItem("Error Rate", HealthStatus.WARNING, 
                "Unable to check error rate: " + e.getMessage());
        }
    }
    
    /**
     * Checks disk space health.
     * 
     * @return HealthCheckItem for disk space status
     */
    public HealthCheckItem checkDiskSpace() {
        try {
            File root = new File("/");
            long totalSpace = root.getTotalSpace();
            long freeSpace = root.getFreeSpace();
            
            if (totalSpace == 0) {
                return new HealthCheckItem("Disk Space", HealthStatus.WARNING, 
                    "Unable to determine disk space");
            }
            
            double freePercent = (double) freeSpace / totalSpace * 100;
            
            HealthStatus status;
            String message;
            
            if (freePercent < DISK_SPACE_CRITICAL_THRESHOLD) {
                status = HealthStatus.CRITICAL;
                message = String.format("Critical: Only %.1f%% disk space free", freePercent);
            } else if (freePercent < DISK_SPACE_WARNING_THRESHOLD) {
                status = HealthStatus.WARNING;
                message = String.format("Low disk space: %.1f%% free", freePercent);
            } else {
                status = HealthStatus.HEALTHY;
                message = String.format("Disk space adequate: %.1f%% free", freePercent);
            }
            
            return new HealthCheckItem("Disk Space", status, message);
            
        } catch (Exception e) {
            log.error("Error checking disk space", e);
            return new HealthCheckItem("Disk Space", HealthStatus.WARNING,
                "Unable to check disk space: " + e.getMessage());
        }
    }
    
    /**
     * Performs custom application-specific health checks.
     * 
     * @return List of custom health check items
     */
    private List<HealthCheckItem> performCustomChecks() {
        List<HealthCheckItem> customChecks = new ArrayList<>();
        
        // Add any custom health checks here
        // For example: database connectivity, external service availability, etc.
        
        return customChecks;
    }
    
    /**
     * Determines overall health status based on individual check results.
     * 
     * @param checks List of health check items
     * @return Overall health status
     */
    private HealthStatus determineOverallStatus(List<HealthCheckItem> checks) {
        boolean hasCritical = false;
        boolean hasWarning = false;
        
        for (HealthCheckItem check : checks) {
            if (check.status() == HealthStatus.CRITICAL) {
                hasCritical = true;
            } else if (check.status() == HealthStatus.WARNING) {
                hasWarning = true;
            }
        }
        
        if (hasCritical) {
            return HealthStatus.CRITICAL;
        } else if (hasWarning) {
            return HealthStatus.WARNING;
        } else {
            return HealthStatus.HEALTHY;
        }
    }
    
    /**
     * Gets health status for a specific component.
     * 
     * @param component Component name
     * @return HealthCheckItem for the component or null if not found
     */
    public HealthCheckItem getComponentHealth(String component) {
        switch (component.toLowerCase()) {
            case "memory":
                return checkMemoryHealth();
            case "threads":
                return checkThreadHealth();
            case "gc":
            case "garbage collection":
                return checkGCHealth();
            case "errors":
            case "error rate":
                return checkErrorRate();
            case "disk":
            case "disk space":
                return checkDiskSpace();
            default:
                return null;
        }
    }
    
    @Override
    public DiagnosticInfo getDiagnosticInfo() {
        Map<String, Object> states = new HashMap<>();
        
        try {
            HealthCheckResult result = performHealthCheck();
            
            states.put("overall_status", result.overallStatus().toString());
            states.put("check_count", result.checks().size());
            
            // Count by status
            Map<HealthStatus, Integer> statusCounts = new HashMap<>();
            for (HealthCheckItem check : result.checks()) {
                statusCounts.merge(check.status(), 1, Integer::sum);
            }
            states.put("status_counts", statusCounts);
            
            // Individual check results
            List<Map<String, String>> checkResults = new ArrayList<>();
            for (HealthCheckItem check : result.checks()) {
                Map<String, String> checkInfo = new HashMap<>();
                checkInfo.put("component", check.component());
                checkInfo.put("status", check.status().toString());
                checkInfo.put("message", check.message());
                checkResults.add(checkInfo);
            }
            states.put("checks", checkResults);
            
            return DiagnosticInfo.builder()
                .component("HealthCheckService")
                .states(states)
                .build();
                
        } catch (Exception e) {
            log.error("Error collecting diagnostic info", e);
            return DiagnosticInfo.error("HealthCheckService", e);
        }
    }
}