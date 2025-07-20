package io.github.jspinak.brobot.runner.diagnostics;

import io.github.jspinak.brobot.runner.diagnostics.services.*;
import io.github.jspinak.brobot.runner.errorhandling.ErrorHandler;
import io.github.jspinak.brobot.runner.common.diagnostics.DiagnosticCapable;
import io.github.jspinak.brobot.runner.common.diagnostics.DiagnosticInfo;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import jakarta.annotation.PostConstruct;
import java.io.IOException;
import java.nio.file.Path;
import java.time.LocalDateTime;
import java.util.*;

/**
 * Comprehensive diagnostic tool for troubleshooting application issues.
 * Acts as an orchestrator for various diagnostic services.
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class DiagnosticTool implements DiagnosticCapable {

    private final SystemInfoCollectorService systemInfoService;
    private final MemoryDiagnosticService memoryService;
    private final ThreadDiagnosticService threadService;
    private final PerformanceDiagnosticService performanceService;
    private final HealthCheckService healthCheckService;
    private final DiagnosticReportService reportService;
    private final ErrorHandler errorHandler;
    
    @PostConstruct
    public void initialize() {
        log.info("Diagnostic tool initialized");
    }
    
    /**
     * Run a complete system diagnostic and return the report.
     * 
     * @return Complete diagnostic report
     */
    public DiagnosticReport runDiagnostics() {
        log.info("Running comprehensive diagnostics...");
        
        try {
            DiagnosticReport report = DiagnosticReport.builder()
                .timestamp(LocalDateTime.now())
                .systemInfo(systemInfoService.collectSystemInfo())
                .memoryInfo(memoryService.collectMemoryInfo())
                .threadInfo(threadService.collectThreadInfo())
                .performanceMetrics(performanceService.collectPerformanceMetrics())
                .errorStatistics(errorHandler.getStatistics())
                .healthCheckResult(healthCheckService.performHealthCheck())
                .environmentVariables(collectEnvironmentVariables())
                .systemProperties(collectSystemProperties())
                .build();
                
            log.info("Diagnostics complete");
            return report;
            
        } catch (Exception e) {
            log.error("Error running diagnostics", e);
            throw new RuntimeException("Failed to run diagnostics", e);
        }
    }
    
    /**
     * Run a quick health check.
     * 
     * @return Health check result
     */
    public HealthCheckResult performHealthCheck() {
        log.debug("Performing quick health check");
        return healthCheckService.performHealthCheck();
    }
    
    /**
     * Export diagnostic report to file.
     * 
     * @param report The diagnostic report
     * @param outputDir Directory to save the report
     * @return Path to the saved report
     * @throws IOException If unable to save the report
     */
    public Path exportDiagnostics(DiagnosticReport report, Path outputDir) throws IOException {
        log.info("Exporting diagnostics to: {}", outputDir);
        return reportService.saveReport(report, outputDir);
    }
    
    /**
     * Export current diagnostics to file.
     * 
     * @param outputDir Directory to save the report
     * @return Path to the saved report
     * @throws IOException If unable to save the report
     */
    public Path exportCurrentDiagnostics(Path outputDir) throws IOException {
        DiagnosticReport report = runDiagnostics();
        return exportDiagnostics(report, outputDir);
    }
    
    /**
     * Get a summary of the current system state.
     * 
     * @return Summary string
     */
    public String getSystemSummary() {
        try {
            DiagnosticReport report = runDiagnostics();
            return reportService.generateSummary(report);
        } catch (Exception e) {
            log.error("Error generating system summary", e);
            return "Error generating summary: " + e.getMessage();
        }
    }
    
    /**
     * Get formatted diagnostic report as string.
     * 
     * @return Formatted report string
     */
    public String getFormattedReport() {
        try {
            DiagnosticReport report = runDiagnostics();
            return reportService.formatReport(report);
        } catch (Exception e) {
            log.error("Error formatting report", e);
            return "Error formatting report: " + e.getMessage();
        }
    }
    
    /**
     * Check if the system is healthy.
     * 
     * @return true if all health checks pass
     */
    public boolean isSystemHealthy() {
        HealthCheckResult result = performHealthCheck();
        return result.overallStatus() == HealthStatus.HEALTHY;
    }
    
    /**
     * Get specific component health.
     * 
     * @param component Component name
     * @return Health check item for the component
     */
    public HealthCheckItem getComponentHealth(String component) {
        return healthCheckService.getComponentHealth(component);
    }
    
    /**
     * Force garbage collection.
     */
    public void forceGarbageCollection() {
        log.info("Forcing garbage collection");
        memoryService.runGarbageCollection();
    }
    
    /**
     * Get current memory usage percentage.
     * 
     * @return Heap usage percentage
     */
    public double getMemoryUsagePercentage() {
        return memoryService.getHeapUsagePercentage();
    }
    
    /**
     * Get current thread count.
     * 
     * @return Active thread count
     */
    public int getThreadCount() {
        ThreadDiagnosticInfo info = threadService.collectThreadInfo();
        return info.threadCount();
    }
    
    /**
     * Check for deadlocked threads.
     * 
     * @return true if deadlocks detected
     */
    public boolean hasDeadlockedThreads() {
        ThreadDiagnosticInfo info = threadService.collectThreadInfo();
        return info.deadlockedThreads() > 0;
    }
    
    /**
     * Get GC overhead percentage.
     * 
     * @return GC overhead as percentage of runtime
     */
    public double getGCOverheadPercentage() {
        return performanceService.getGCOverheadPercentage();
    }
    
    private Map<String, String> collectEnvironmentVariables() {
        Map<String, String> filtered = new HashMap<>();
        Map<String, String> env = System.getenv();
        
        // Only include relevant environment variables
        String[] relevantVars = {
            "JAVA_HOME", "JAVA_OPTS", "PATH", "USER", "HOME",
            "BROBOT_HOME", "BROBOT_CONFIG", "LOG_LEVEL"
        };
        
        for (String var : relevantVars) {
            String value = env.get(var);
            if (value != null) {
                filtered.put(var, value);
            }
        }
        
        return filtered;
    }
    
    private Map<String, String> collectSystemProperties() {
        Map<String, String> filtered = new HashMap<>();
        Properties props = System.getProperties();
        
        // Only include relevant system properties
        String[] relevantProps = {
            "java.version", "java.vendor", "java.home",
            "java.runtime.name", "java.runtime.version",
            "java.vm.name", "java.vm.vendor", "java.vm.version",
            "os.name", "os.arch", "os.version",
            "user.name", "user.home", "user.dir",
            "file.encoding", "java.class.path"
        };
        
        for (String prop : relevantProps) {
            String value = props.getProperty(prop);
            if (value != null) {
                filtered.put(prop, value);
            }
        }
        
        return filtered;
    }
    
    @Override
    public DiagnosticInfo getDiagnosticInfo() {
        Map<String, Object> states = new HashMap<>();
        
        try {
            // Basic system state
            states.put("memory_usage_percent", getMemoryUsagePercentage());
            states.put("thread_count", getThreadCount());
            states.put("has_deadlocks", hasDeadlockedThreads());
            states.put("gc_overhead_percent", getGCOverheadPercentage());
            states.put("system_healthy", isSystemHealthy());
            
            // Service statuses
            states.put("services_available", Map.of(
                "systemInfo", systemInfoService != null,
                "memory", memoryService != null,
                "thread", threadService != null,
                "performance", performanceService != null,
                "health", healthCheckService != null,
                "report", reportService != null
            ));
            
            return DiagnosticInfo.builder()
                .component("DiagnosticTool")
                .states(states)
                .build();
                
        } catch (Exception e) {
            log.error("Error collecting diagnostic info", e);
            return DiagnosticInfo.error("DiagnosticTool", e);
        }
    }
}