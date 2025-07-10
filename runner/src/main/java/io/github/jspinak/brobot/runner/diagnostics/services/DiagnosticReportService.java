package io.github.jspinak.brobot.runner.diagnostics.services;

import io.github.jspinak.brobot.runner.diagnostics.*;
import io.github.jspinak.brobot.runner.errorhandling.ErrorStatistics;
import io.github.jspinak.brobot.runner.common.diagnostics.DiagnosticCapable;
import io.github.jspinak.brobot.runner.common.diagnostics.DiagnosticInfo;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.HashMap;
import java.util.Map;

/**
 * Service responsible for generating, formatting, and saving diagnostic reports.
 * Handles report generation, formatting, and persistence.
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class DiagnosticReportService implements DiagnosticCapable {
    
    private final DateTimeFormatter FILE_DATE_FORMAT = DateTimeFormatter.ofPattern("yyyy-MM-dd_HH-mm-ss");
    private final DateTimeFormatter REPORT_DATE_FORMAT = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
    
    /**
     * Formats a complete diagnostic report into a human-readable string.
     * 
     * @param report The diagnostic report to format
     * @return Formatted report string
     */
    public String formatReport(DiagnosticReport report) {
        StringBuilder sb = new StringBuilder();
        
        sb.append("=== BROBOT RUNNER DIAGNOSTIC REPORT ===\n");
        sb.append("Generated: ").append(report.timestamp().format(REPORT_DATE_FORMAT)).append("\n\n");
        
        // System Info
        formatSystemInfo(sb, report.systemInfo());
        
        // Memory Info
        formatMemoryInfo(sb, report.memoryInfo());
        
        // Thread Info
        formatThreadInfo(sb, report.threadInfo());
        
        // Performance Metrics
        formatPerformanceMetrics(sb, report.performanceMetrics());
        
        // Error Statistics
        formatErrorStatistics(sb, report.errorStatistics());
        
        // Health Check
        if (report.healthCheckResult() != null) {
            formatHealthCheck(sb, report.healthCheckResult());
        }
        
        // Environment Variables (selected)
        formatEnvironmentVariables(sb, report.environmentVariables());
        
        // System Properties (selected)
        formatSystemProperties(sb, report.systemProperties());
        
        return sb.toString();
    }
    
    /**
     * Saves a diagnostic report to file.
     * 
     * @param report The report to save
     * @param directory Directory to save the report in
     * @return Path to the saved report file
     * @throws IOException If unable to save the report
     */
    public Path saveReport(DiagnosticReport report, Path directory) throws IOException {
        String filename = String.format("diagnostic-report-%s.txt", 
            report.timestamp().format(FILE_DATE_FORMAT));
        Path filePath = directory.resolve(filename);
        
        log.info("Saving diagnostic report to: {}", filePath);
        
        // Ensure directory exists
        Files.createDirectories(directory);
        
        // Write report
        String formattedReport = formatReport(report);
        Files.writeString(filePath, formattedReport, 
            StandardOpenOption.CREATE, StandardOpenOption.TRUNCATE_EXISTING);
        
        return filePath;
    }
    
    /**
     * Generates a summary of the diagnostic report.
     * 
     * @param report The diagnostic report
     * @return Summary string
     */
    public String generateSummary(DiagnosticReport report) {
        StringBuilder summary = new StringBuilder();
        
        summary.append("Diagnostic Summary:\n");
        summary.append("  Timestamp: ").append(report.timestamp().format(REPORT_DATE_FORMAT)).append("\n");
        summary.append("  System: ").append(report.systemInfo().osName())
               .append(" ").append(report.systemInfo().osVersion()).append("\n");
        summary.append("  Java: ").append(report.systemInfo().javaVersion()).append("\n");
        summary.append("  Memory: ").append(formatBytes(report.memoryInfo().heapUsed()))
               .append(" / ").append(formatBytes(report.memoryInfo().heapMax())).append("\n");
        summary.append("  Threads: ").append(report.threadInfo().threadCount()).append("\n");
        summary.append("  Total Errors: ").append(report.errorStatistics().totalErrors()).append("\n");
        
        if (report.healthCheckResult() != null) {
            summary.append("  Health Status: ")
                   .append(report.healthCheckResult().overallStatus()).append("\n");
        }
        
        return summary.toString();
    }
    
    private void formatSystemInfo(StringBuilder sb, SystemInfo sys) {
        sb.append("SYSTEM INFORMATION\n");
        sb.append("==================\n");
        sb.append("OS: ").append(sys.osName()).append(" ").append(sys.osVersion()).append("\n");
        sb.append("Architecture: ").append(sys.osArch()).append("\n");
        sb.append("Java: ").append(sys.javaVersion()).append(" (").append(sys.javaVendor()).append(")\n");
        sb.append("JVM: ").append(sys.jvmName()).append(" ").append(sys.jvmVersion()).append("\n");
        sb.append("Processors: ").append(sys.availableProcessors()).append("\n");
        sb.append("System Load: ").append(String.format("%.2f", sys.systemLoadAverage())).append("\n");
        sb.append("Uptime: ").append(formatUptime(sys.uptime())).append("\n\n");
    }
    
    private void formatMemoryInfo(StringBuilder sb, MemoryInfo mem) {
        sb.append("MEMORY INFORMATION\n");
        sb.append("==================\n");
        sb.append("Heap Memory:\n");
        sb.append("  Used: ").append(formatBytes(mem.heapUsed())).append("\n");
        sb.append("  Max: ").append(formatBytes(mem.heapMax())).append("\n");
        sb.append("  Committed: ").append(formatBytes(mem.heapCommitted())).append("\n");
        sb.append("Non-Heap Memory:\n");
        sb.append("  Used: ").append(formatBytes(mem.nonHeapUsed())).append("\n");
        sb.append("  Max: ").append(formatBytes(mem.nonHeapMax())).append("\n");
        sb.append("  Committed: ").append(formatBytes(mem.nonHeapCommitted())).append("\n\n");
    }
    
    private void formatThreadInfo(StringBuilder sb, ThreadDiagnosticInfo threads) {
        sb.append("THREAD INFORMATION\n");
        sb.append("==================\n");
        sb.append("Current Threads: ").append(threads.threadCount()).append("\n");
        sb.append("Peak Threads: ").append(threads.peakThreadCount()).append("\n");
        sb.append("Daemon Threads: ").append(threads.daemonThreadCount()).append("\n");
        sb.append("Total Started: ").append(threads.totalStartedThreadCount()).append("\n");
        sb.append("Deadlocked: ").append(threads.deadlockedThreads()).append("\n\n");
    }
    
    private void formatPerformanceMetrics(StringBuilder sb, PerformanceMetrics metrics) {
        sb.append("PERFORMANCE METRICS\n");
        sb.append("===================\n");
        sb.append(metrics.performanceReport()).append("\n");
    }
    
    private void formatErrorStatistics(StringBuilder sb, ErrorStatistics stats) {
        sb.append("ERROR STATISTICS\n");
        sb.append("================\n");
        sb.append(stats.getSummary()).append("\n");
    }
    
    private void formatHealthCheck(StringBuilder sb, HealthCheckResult health) {
        sb.append("HEALTH CHECK\n");
        sb.append("============\n");
        sb.append("Overall Status: ").append(health.overallStatus()).append("\n");
        sb.append("Individual Checks:\n");
        
        for (HealthCheckItem check : health.checks()) {
            sb.append("  ").append(check.component()).append(": ")
              .append(check.status()).append(" - ")
              .append(check.message()).append("\n");
        }
        sb.append("\n");
    }
    
    private void formatEnvironmentVariables(StringBuilder sb, Map<String, String> envVars) {
        sb.append("ENVIRONMENT VARIABLES (Selected)\n");
        sb.append("================================\n");
        
        // Only show relevant environment variables
        String[] relevantVars = {"JAVA_HOME", "JAVA_OPTS", "PATH", "USER", "HOME"};
        for (String var : relevantVars) {
            String value = envVars.get(var);
            if (value != null) {
                sb.append(var).append(": ").append(value).append("\n");
            }
        }
        sb.append("\n");
    }
    
    private void formatSystemProperties(StringBuilder sb, Map<String, String> sysProps) {
        sb.append("SYSTEM PROPERTIES (Selected)\n");
        sb.append("============================\n");
        
        // Only show relevant system properties
        String[] relevantProps = {
            "java.runtime.version", "java.vendor", "java.vm.name",
            "os.name", "os.version", "user.dir", "user.home"
        };
        
        for (String prop : relevantProps) {
            String value = sysProps.get(prop);
            if (value != null) {
                sb.append(prop).append(": ").append(value).append("\n");
            }
        }
    }
    
    private String formatUptime(long uptimeMs) {
        long seconds = uptimeMs / 1000;
        long minutes = seconds / 60;
        long hours = minutes / 60;
        long days = hours / 24;
        
        return String.format("%d days, %d hours, %d minutes", 
            days, hours % 24, minutes % 60);
    }
    
    private String formatBytes(long bytes) {
        if (bytes < 1024) return bytes + " B";
        if (bytes < 1024 * 1024) return String.format("%.1f KB", bytes / 1024.0);
        if (bytes < 1024 * 1024 * 1024) return String.format("%.1f MB", bytes / (1024.0 * 1024));
        return String.format("%.1f GB", bytes / (1024.0 * 1024 * 1024));
    }
    
    @Override
    public DiagnosticInfo getDiagnosticInfo() {
        Map<String, Object> states = new HashMap<>();
        
        try {
            states.put("report_date_format", REPORT_DATE_FORMAT.toString());
            states.put("file_date_format", FILE_DATE_FORMAT.toString());
            states.put("service_status", "operational");
            
            return DiagnosticInfo.builder()
                .component("DiagnosticReportService")
                .states(states)
                .build();
                
        } catch (Exception e) {
            log.error("Error collecting diagnostic info", e);
            return DiagnosticInfo.error("DiagnosticReportService", e);
        }
    }
}