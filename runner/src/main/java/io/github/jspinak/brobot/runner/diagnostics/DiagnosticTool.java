package io.github.jspinak.brobot.runner.diagnostics;

import io.github.jspinak.brobot.runner.errorhandling.ErrorHandler;
import io.github.jspinak.brobot.runner.errorhandling.ErrorStatistics;
import io.github.jspinak.brobot.runner.performance.PerformanceProfiler;
import io.github.jspinak.brobot.runner.performance.ThreadManagementOptimizer;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import jakarta.annotation.PostConstruct;
import java.io.*;
import java.lang.management.*;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.stream.Collectors;

/**
 * Comprehensive diagnostic tool for troubleshooting application issues.
 * Collects system information, performance metrics, and error data.
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class DiagnosticTool {

    private final PerformanceProfiler performanceProfiler;
    private final ErrorHandler errorHandler;
    private final ThreadManagementOptimizer threadOptimizer;
    
    private final RuntimeMXBean runtimeBean = ManagementFactory.getRuntimeMXBean();
    private final OperatingSystemMXBean osBean = ManagementFactory.getOperatingSystemMXBean();
    private final MemoryMXBean memoryBean = ManagementFactory.getMemoryMXBean();
    private final ThreadMXBean threadBean = ManagementFactory.getThreadMXBean();
    private final List<GarbageCollectorMXBean> gcBeans = ManagementFactory.getGarbageCollectorMXBeans();
    
    @PostConstruct
    public void initialize() {
        log.info("Diagnostic tool initialized");
    }
    
    /**
     * Run a complete system diagnostic and return the report.
     */
    public DiagnosticReport runDiagnostics() {
        log.info("Running system diagnostics...");
        
        DiagnosticReport report = DiagnosticReport.builder()
            .timestamp(LocalDateTime.now())
            .systemInfo(collectSystemInfo())
            .memoryInfo(collectMemoryInfo())
            .threadInfo(collectThreadInfo())
            .performanceMetrics(collectPerformanceMetrics())
            .errorStatistics(collectErrorStatistics())
            .environmentVariables(collectEnvironmentVariables())
            .systemProperties(collectSystemProperties())
            .build();
            
        log.info("Diagnostics complete");
        return report;
    }
    
    /**
     * Run a quick health check.
     */
    public HealthCheckResult performHealthCheck() {
        List<HealthCheckItem> checks = new ArrayList<>();
        
        // Memory check
        checks.add(checkMemoryHealth());
        
        // Thread check
        checks.add(checkThreadHealth());
        
        // GC check
        checks.add(checkGCHealth());
        
        // Error rate check
        checks.add(checkErrorRate());
        
        // Disk space check
        checks.add(checkDiskSpace());
        
        HealthStatus overallStatus = checks.stream()
            .map(HealthCheckItem::status)
            .max(Comparator.naturalOrder())
            .orElse(HealthStatus.HEALTHY);
            
        return new HealthCheckResult(overallStatus, checks);
    }
    
    /**
     * Export diagnostic report to file.
     */
    public Path exportDiagnostics(DiagnosticReport report, Path outputDir) throws IOException {
        Files.createDirectories(outputDir);
        
        String timestamp = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMdd_HHmmss"));
        Path reportFile = outputDir.resolve("diagnostics_" + timestamp + ".txt");
        
        try (BufferedWriter writer = Files.newBufferedWriter(reportFile)) {
            writer.write(formatReport(report));
        }
        
        log.info("Diagnostic report exported to: {}", reportFile);
        return reportFile;
    }
    
    private SystemInfo collectSystemInfo() {
        return SystemInfo.builder()
            .osName(System.getProperty("os.name"))
            .osVersion(System.getProperty("os.version"))
            .osArch(System.getProperty("os.arch"))
            .javaVersion(System.getProperty("java.version"))
            .javaVendor(System.getProperty("java.vendor"))
            .jvmName(runtimeBean.getVmName())
            .jvmVersion(runtimeBean.getVmVersion())
            .availableProcessors(osBean.getAvailableProcessors())
            .systemLoadAverage(osBean.getSystemLoadAverage())
            .uptime(runtimeBean.getUptime())
            .startTime(new Date(runtimeBean.getStartTime()))
            .build();
    }
    
    private MemoryInfo collectMemoryInfo() {
        MemoryUsage heapUsage = memoryBean.getHeapMemoryUsage();
        MemoryUsage nonHeapUsage = memoryBean.getNonHeapMemoryUsage();
        
        Runtime runtime = Runtime.getRuntime();
        
        return MemoryInfo.builder()
            .heapUsed(heapUsage.getUsed())
            .heapMax(heapUsage.getMax())
            .heapCommitted(heapUsage.getCommitted())
            .nonHeapUsed(nonHeapUsage.getUsed())
            .nonHeapMax(nonHeapUsage.getMax())
            .nonHeapCommitted(nonHeapUsage.getCommitted())
            .freeMemory(runtime.freeMemory())
            .totalMemory(runtime.totalMemory())
            .maxMemory(runtime.maxMemory())
            .memoryPools(collectMemoryPools())
            .build();
    }
    
    private Map<String, MemoryPoolInfo> collectMemoryPools() {
        Map<String, MemoryPoolInfo> pools = new HashMap<>();
        
        for (MemoryPoolMXBean pool : ManagementFactory.getMemoryPoolMXBeans()) {
            MemoryUsage usage = pool.getUsage();
            pools.put(pool.getName(), MemoryPoolInfo.builder()
                .name(pool.getName())
                .type(pool.getType().name())
                .used(usage.getUsed())
                .max(usage.getMax())
                .committed(usage.getCommitted())
                .build()
            );
        }
        
        return pools;
    }
    
    private ThreadInfo collectThreadInfo() {
        long[] deadlockedThreads = threadBean.findDeadlockedThreads();
        
        return ThreadInfo.builder()
            .threadCount(threadBean.getThreadCount())
            .peakThreadCount(threadBean.getPeakThreadCount())
            .daemonThreadCount(threadBean.getDaemonThreadCount())
            .totalStartedThreadCount(threadBean.getTotalStartedThreadCount())
            .deadlockedThreads(deadlockedThreads != null ? deadlockedThreads.length : 0)
            .threadStates(collectThreadStates())
            .build();
    }
    
    private Map<Thread.State, Integer> collectThreadStates() {
        Map<Thread.State, Integer> states = new EnumMap<>(Thread.State.class);
        
        for (java.lang.management.ThreadInfo info : threadBean.dumpAllThreads(false, false)) {
            if (info != null) {
                states.merge(info.getThreadState(), 1, Integer::sum);
            }
        }
        
        return states;
    }
    
    private PerformanceMetrics collectPerformanceMetrics() {
        PerformanceProfiler.PerformanceReport perfReport = performanceProfiler.generateReport();
        
        // GC metrics
        long totalGcCount = 0;
        long totalGcTime = 0;
        Map<String, GCInfo> gcInfo = new HashMap<>();
        
        for (GarbageCollectorMXBean gc : gcBeans) {
            totalGcCount += gc.getCollectionCount();
            totalGcTime += gc.getCollectionTime();
            
            gcInfo.put(gc.getName(), GCInfo.builder()
                .name(gc.getName())
                .collectionCount(gc.getCollectionCount())
                .collectionTime(gc.getCollectionTime())
                .build()
            );
        }
        
        return PerformanceMetrics.builder()
            .performanceReport(perfReport.toString())
            .gcTotalCount(totalGcCount)
            .gcTotalTime(totalGcTime)
            .gcDetails(gcInfo)
            .cpuUsage(getCpuUsage())
            .build();
    }
    
    private ErrorStatistics collectErrorStatistics() {
        return errorHandler.getStatistics();
    }
    
    private Map<String, String> collectEnvironmentVariables() {
        return new HashMap<>(System.getenv());
    }
    
    private Map<String, String> collectSystemProperties() {
        Properties props = System.getProperties();
        Map<String, String> propsMap = new HashMap<>();
        
        props.forEach((key, value) -> propsMap.put(key.toString(), value.toString()));
        
        return propsMap;
    }
    
    private double getCpuUsage() {
        if (osBean instanceof com.sun.management.OperatingSystemMXBean) {
            return ((com.sun.management.OperatingSystemMXBean) osBean).getProcessCpuLoad() * 100;
        }
        return -1;
    }
    
    private HealthCheckItem checkMemoryHealth() {
        MemoryUsage heapUsage = memoryBean.getHeapMemoryUsage();
        double usagePercent = (double) heapUsage.getUsed() / heapUsage.getMax() * 100;
        
        HealthStatus status;
        String message;
        
        if (usagePercent > 90) {
            status = HealthStatus.CRITICAL;
            message = String.format("Critical memory usage: %.1f%%", usagePercent);
        } else if (usagePercent > 80) {
            status = HealthStatus.WARNING;
            message = String.format("High memory usage: %.1f%%", usagePercent);
        } else {
            status = HealthStatus.HEALTHY;
            message = String.format("Memory usage normal: %.1f%%", usagePercent);
        }
        
        return new HealthCheckItem("Memory", status, message);
    }
    
    private HealthCheckItem checkThreadHealth() {
        long[] deadlocked = threadBean.findDeadlockedThreads();
        int threadCount = threadBean.getThreadCount();
        
        if (deadlocked != null && deadlocked.length > 0) {
            return new HealthCheckItem("Threads", HealthStatus.CRITICAL, 
                "Deadlocked threads detected: " + deadlocked.length);
        } else if (threadCount > 500) {
            return new HealthCheckItem("Threads", HealthStatus.WARNING,
                "High thread count: " + threadCount);
        } else {
            return new HealthCheckItem("Threads", HealthStatus.HEALTHY,
                "Thread count normal: " + threadCount);
        }
    }
    
    private HealthCheckItem checkGCHealth() {
        long totalGcTime = gcBeans.stream()
            .mapToLong(GarbageCollectorMXBean::getCollectionTime)
            .sum();
            
        long uptime = runtimeBean.getUptime();
        double gcOverhead = (double) totalGcTime / uptime * 100;
        
        HealthStatus status;
        String message;
        
        if (gcOverhead > 10) {
            status = HealthStatus.CRITICAL;
            message = String.format("High GC overhead: %.2f%%", gcOverhead);
        } else if (gcOverhead > 5) {
            status = HealthStatus.WARNING;
            message = String.format("Moderate GC overhead: %.2f%%", gcOverhead);
        } else {
            status = HealthStatus.HEALTHY;
            message = String.format("GC overhead normal: %.2f%%", gcOverhead);
        }
        
        return new HealthCheckItem("Garbage Collection", status, message);
    }
    
    private HealthCheckItem checkErrorRate() {
        ErrorStatistics stats = errorHandler.getStatistics();
        long totalErrors = stats.totalErrors();
        long uptime = runtimeBean.getUptime() / 1000; // Convert to seconds
        
        if (uptime == 0) uptime = 1; // Avoid division by zero
        
        double errorsPerMinute = (double) totalErrors / uptime * 60;
        
        HealthStatus status;
        String message;
        
        if (errorsPerMinute > 10) {
            status = HealthStatus.CRITICAL;
            message = String.format("High error rate: %.1f errors/min", errorsPerMinute);
        } else if (errorsPerMinute > 5) {
            status = HealthStatus.WARNING;
            message = String.format("Elevated error rate: %.1f errors/min", errorsPerMinute);
        } else {
            status = HealthStatus.HEALTHY;
            message = String.format("Error rate normal: %.1f errors/min", errorsPerMinute);
        }
        
        return new HealthCheckItem("Error Rate", status, message);
    }
    
    private HealthCheckItem checkDiskSpace() {
        try {
            File root = new File("/");
            long freeSpace = root.getFreeSpace();
            long totalSpace = root.getTotalSpace();
            
            if (totalSpace == 0) {
                return new HealthCheckItem("Disk Space", HealthStatus.WARNING, 
                    "Unable to determine disk space");
            }
            
            double freePercent = (double) freeSpace / totalSpace * 100;
            
            HealthStatus status;
            String message;
            
            if (freePercent < 5) {
                status = HealthStatus.CRITICAL;
                message = String.format("Critical: Only %.1f%% disk space free", freePercent);
            } else if (freePercent < 10) {
                status = HealthStatus.WARNING;
                message = String.format("Low disk space: %.1f%% free", freePercent);
            } else {
                status = HealthStatus.HEALTHY;
                message = String.format("Disk space adequate: %.1f%% free", freePercent);
            }
            
            return new HealthCheckItem("Disk Space", status, message);
            
        } catch (Exception e) {
            return new HealthCheckItem("Disk Space", HealthStatus.WARNING,
                "Unable to check disk space: " + e.getMessage());
        }
    }
    
    private String formatReport(DiagnosticReport report) {
        StringBuilder sb = new StringBuilder();
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
        
        sb.append("=== BROBOT RUNNER DIAGNOSTIC REPORT ===\n");
        sb.append("Generated: ").append(report.timestamp().format(formatter)).append("\n\n");
        
        // System Info
        sb.append("SYSTEM INFORMATION\n");
        sb.append("==================\n");
        SystemInfo sys = report.systemInfo();
        sb.append("OS: ").append(sys.osName()).append(" ").append(sys.osVersion()).append("\n");
        sb.append("Architecture: ").append(sys.osArch()).append("\n");
        sb.append("Java: ").append(sys.javaVersion()).append(" (").append(sys.javaVendor()).append(")\n");
        sb.append("JVM: ").append(sys.jvmName()).append(" ").append(sys.jvmVersion()).append("\n");
        sb.append("Processors: ").append(sys.availableProcessors()).append("\n");
        sb.append("System Load: ").append(String.format("%.2f", sys.systemLoadAverage())).append("\n");
        sb.append("Uptime: ").append(formatUptime(sys.uptime())).append("\n\n");
        
        // Memory Info
        sb.append("MEMORY INFORMATION\n");
        sb.append("==================\n");
        MemoryInfo mem = report.memoryInfo();
        sb.append("Heap Memory:\n");
        sb.append("  Used: ").append(formatBytes(mem.heapUsed())).append("\n");
        sb.append("  Max: ").append(formatBytes(mem.heapMax())).append("\n");
        sb.append("  Committed: ").append(formatBytes(mem.heapCommitted())).append("\n");
        sb.append("Non-Heap Memory:\n");
        sb.append("  Used: ").append(formatBytes(mem.nonHeapUsed())).append("\n");
        sb.append("  Max: ").append(formatBytes(mem.nonHeapMax())).append("\n");
        sb.append("  Committed: ").append(formatBytes(mem.nonHeapCommitted())).append("\n\n");
        
        // Thread Info
        sb.append("THREAD INFORMATION\n");
        sb.append("==================\n");
        ThreadInfo threads = report.threadInfo();
        sb.append("Current Threads: ").append(threads.threadCount()).append("\n");
        sb.append("Peak Threads: ").append(threads.peakThreadCount()).append("\n");
        sb.append("Daemon Threads: ").append(threads.daemonThreadCount()).append("\n");
        sb.append("Total Started: ").append(threads.totalStartedThreadCount()).append("\n");
        sb.append("Deadlocked: ").append(threads.deadlockedThreads()).append("\n\n");
        
        // Performance Metrics
        sb.append("PERFORMANCE METRICS\n");
        sb.append("===================\n");
        sb.append(report.performanceMetrics().performanceReport()).append("\n");
        
        // Error Statistics
        sb.append("ERROR STATISTICS\n");
        sb.append("================\n");
        sb.append(report.errorStatistics().getSummary()).append("\n");
        
        return sb.toString();
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
}