package io.github.jspinak.brobot.test.metrics;

import com.sun.management.OperatingSystemMXBean;
import java.lang.management.ManagementFactory;
import java.lang.management.MemoryMXBean;
import java.lang.management.ThreadMXBean;
import java.time.Instant;
import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicLong;

/**
 * Monitors system resources during parallel test execution.
 * Helps identify memory leaks, thread issues, and resource bottlenecks.
 */
public class ParallelExecutionMonitor {
    
    private final ScheduledExecutorService scheduler = Executors.newScheduledThreadPool(2);
    private final BlockingQueue<ResourceSnapshot> snapshots = new LinkedBlockingQueue<>();
    private final AtomicBoolean monitoring = new AtomicBoolean(false);
    private final AtomicLong peakMemoryUsage = new AtomicLong(0);
    private final AtomicLong peakThreadCount = new AtomicLong(0);
    
    private final MemoryMXBean memoryBean = ManagementFactory.getMemoryMXBean();
    private final ThreadMXBean threadBean = ManagementFactory.getThreadMXBean();
    private final OperatingSystemMXBean osBean = (OperatingSystemMXBean) ManagementFactory.getOperatingSystemMXBean();
    
    private ScheduledFuture<?> monitoringTask;
    private Instant startTime;
    private Instant endTime;
    
    /**
     * Start monitoring system resources
     */
    public void startMonitoring() {
        if (monitoring.compareAndSet(false, true)) {
            startTime = Instant.now();
            
            // Take snapshots every 100ms
            monitoringTask = scheduler.scheduleAtFixedRate(
                this::captureSnapshot, 0, 100, TimeUnit.MILLISECONDS);
            
            // Log summary every 5 seconds
            scheduler.scheduleAtFixedRate(
                this::logSummary, 5, 5, TimeUnit.SECONDS);
            
            System.out.println("Started monitoring system resources for parallel test execution");
        }
    }
    
    /**
     * Stop monitoring and generate report
     */
    public void stopMonitoring() {
        if (monitoring.compareAndSet(true, false)) {
            endTime = Instant.now();
            
            if (monitoringTask != null) {
                monitoringTask.cancel(false);
            }
            
            generateFinalReport();
            scheduler.shutdown();
        }
    }
    
    private void captureSnapshot() {
        try {
            ResourceSnapshot snapshot = new ResourceSnapshot();
            snapshot.timestamp = Instant.now();
            
            // Memory metrics
            snapshot.heapUsed = memoryBean.getHeapMemoryUsage().getUsed();
            snapshot.heapMax = memoryBean.getHeapMemoryUsage().getMax();
            snapshot.nonHeapUsed = memoryBean.getNonHeapMemoryUsage().getUsed();
            
            // Thread metrics
            snapshot.threadCount = threadBean.getThreadCount();
            snapshot.peakThreadCount = threadBean.getPeakThreadCount();
            snapshot.daemonThreadCount = threadBean.getDaemonThreadCount();
            
            // CPU metrics
            snapshot.processCpuLoad = osBean.getProcessCpuLoad() * 100;
            snapshot.systemCpuLoad = osBean.getCpuLoad() * 100;
            
            // System metrics
            snapshot.freePhysicalMemory = osBean.getFreeMemorySize();
            snapshot.totalPhysicalMemory = osBean.getTotalMemorySize();
            
            snapshots.offer(snapshot);
            
            // Update peaks
            peakMemoryUsage.updateAndGet(peak -> Math.max(peak, snapshot.heapUsed));
            peakThreadCount.updateAndGet(peak -> Math.max(peak, snapshot.threadCount));
            
            // Detect issues
            detectResourceIssues(snapshot);
            
        } catch (Exception e) {
            System.err.println("Error capturing resource snapshot: " + e.getMessage());
        }
    }
    
    private void detectResourceIssues(ResourceSnapshot snapshot) {
        // Memory leak detection
        double heapUsagePercent = (snapshot.heapUsed * 100.0) / snapshot.heapMax;
        if (heapUsagePercent > 90) {
            System.err.println(String.format(
                "WARNING: High heap usage detected: %.1f%% (%.0f MB / %.0f MB)",
                heapUsagePercent,
                snapshot.heapUsed / (1024.0 * 1024),
                snapshot.heapMax / (1024.0 * 1024)
            ));
        }
        
        // Thread leak detection
        if (snapshot.threadCount > 500) {
            System.err.println(String.format(
                "WARNING: High thread count detected: %d threads (peak: %d)",
                snapshot.threadCount, snapshot.peakThreadCount
            ));
        }
        
        // CPU saturation detection
        if (snapshot.processCpuLoad > 90) {
            System.err.println(String.format(
                "WARNING: High CPU usage detected: %.1f%% process, %.1f%% system",
                snapshot.processCpuLoad, snapshot.systemCpuLoad
            ));
        }
    }
    
    private void logSummary() {
        if (!monitoring.get()) return;
        
        ResourceSnapshot latest = snapshots.poll();
        if (latest != null) {
            System.out.println(String.format(
                "Resource Monitor: Heap=%.0fMB/%.0fMB, Threads=%d, CPU=%.1f%%",
                latest.heapUsed / (1024.0 * 1024),
                latest.heapMax / (1024.0 * 1024),
                latest.threadCount,
                latest.processCpuLoad
            ));
        }
    }
    
    private void generateFinalReport() {
        System.out.println("\n" + "=".repeat(80));
        System.out.println("RESOURCE MONITORING REPORT");
        System.out.println("=".repeat(80));
        
        long duration = endTime.toEpochMilli() - startTime.toEpochMilli();
        System.out.println(String.format("Monitoring Duration: %.1fs", duration / 1000.0));
        
        // Memory statistics
        System.out.println("\nMEMORY USAGE:");
        System.out.println(String.format("  Peak Heap Usage:     %.0f MB", 
            peakMemoryUsage.get() / (1024.0 * 1024)));
        System.out.println(String.format("  Max Heap Available:  %.0f MB", 
            memoryBean.getHeapMemoryUsage().getMax() / (1024.0 * 1024)));
        System.out.println(String.format("  Peak Usage Percent:  %.1f%%", 
            (peakMemoryUsage.get() * 100.0) / memoryBean.getHeapMemoryUsage().getMax()));
        
        // Thread statistics
        System.out.println("\nTHREAD USAGE:");
        System.out.println(String.format("  Peak Thread Count:   %d", peakThreadCount.get()));
        System.out.println(String.format("  Current Threads:     %d", threadBean.getThreadCount()));
        System.out.println(String.format("  Daemon Threads:      %d", threadBean.getDaemonThreadCount()));
        
        // GC statistics
        System.out.println("\nGARBAGE COLLECTION:");
        ManagementFactory.getGarbageCollectorMXBeans().forEach(gc -> {
            System.out.println(String.format("  %s: %d collections, %.0fms total",
                gc.getName(), gc.getCollectionCount(), gc.getCollectionTime()));
        });
        
        // CPU statistics
        ResourceSnapshot lastSnapshot = snapshots.poll();
        if (lastSnapshot != null) {
            System.out.println("\nCPU USAGE (last reading):");
            System.out.println(String.format("  Process CPU:         %.1f%%", lastSnapshot.processCpuLoad));
            System.out.println(String.format("  System CPU:          %.1f%%", lastSnapshot.systemCpuLoad));
        }
        
        // Recommendations
        System.out.println("\nRECOMMENDATIONS:");
        if (peakMemoryUsage.get() > memoryBean.getHeapMemoryUsage().getMax() * 0.8) {
            System.out.println("  - Consider increasing heap size with -Xmx");
        }
        if (peakThreadCount.get() > 200) {
            System.out.println("  - High thread count detected, review parallel settings");
        }
        
        System.out.println("=".repeat(80));
    }
    
    /**
     * Get current resource usage for programmatic access
     */
    public ResourceUsage getCurrentUsage() {
        return new ResourceUsage(
            memoryBean.getHeapMemoryUsage().getUsed(),
            memoryBean.getHeapMemoryUsage().getMax(),
            threadBean.getThreadCount(),
            osBean.getProcessCpuLoad() * 100
        );
    }
    
    /**
     * Resource snapshot data class
     */
    private static class ResourceSnapshot {
        Instant timestamp;
        long heapUsed;
        long heapMax;
        long nonHeapUsed;
        int threadCount;
        int peakThreadCount;
        int daemonThreadCount;
        double processCpuLoad;
        double systemCpuLoad;
        long freePhysicalMemory;
        long totalPhysicalMemory;
    }
    
    /**
     * Public class for resource usage data
     */
    public static class ResourceUsage {
        public final long heapUsed;
        public final long heapMax;
        public final int threadCount;
        public final double cpuUsage;
        
        public ResourceUsage(long heapUsed, long heapMax, int threadCount, double cpuUsage) {
            this.heapUsed = heapUsed;
            this.heapMax = heapMax;
            this.threadCount = threadCount;
            this.cpuUsage = cpuUsage;
        }
        
        public double getHeapUsagePercent() {
            return (heapUsed * 100.0) / heapMax;
        }
    }
}