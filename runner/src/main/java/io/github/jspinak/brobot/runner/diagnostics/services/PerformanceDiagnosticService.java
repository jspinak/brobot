package io.github.jspinak.brobot.runner.diagnostics.services;

import java.lang.management.*;
import java.util.*;

import org.springframework.stereotype.Service;

import io.github.jspinak.brobot.runner.common.diagnostics.DiagnosticCapable;
import io.github.jspinak.brobot.runner.common.diagnostics.DiagnosticInfo;
import io.github.jspinak.brobot.runner.diagnostics.GCInfo;
import io.github.jspinak.brobot.runner.diagnostics.PerformanceMetrics;
import io.github.jspinak.brobot.runner.performance.PerformanceProfiler;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

/**
 * Service responsible for collecting performance diagnostic information. Provides garbage
 * collection metrics, CPU usage, and performance profiling data.
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class PerformanceDiagnosticService implements DiagnosticCapable {

    private final PerformanceProfiler performanceProfiler;
    private final List<GarbageCollectorMXBean> gcBeans =
            ManagementFactory.getGarbageCollectorMXBeans();
    private final OperatingSystemMXBean osBean = ManagementFactory.getOperatingSystemMXBean();
    private final RuntimeMXBean runtimeBean = ManagementFactory.getRuntimeMXBean();

    /**
     * Collects comprehensive performance metrics including GC and CPU usage.
     *
     * @return PerformanceMetrics containing all performance data
     */
    public PerformanceMetrics collectPerformanceMetrics() {
        log.debug("Collecting performance diagnostic information");

        try {
            // Collect GC metrics
            long gcTotalCount = 0;
            long gcTotalTime = 0;
            Map<String, GCInfo> gcDetails = new HashMap<>();

            for (GarbageCollectorMXBean gc : gcBeans) {
                long count = gc.getCollectionCount();
                long time = gc.getCollectionTime();

                if (count >= 0) {
                    gcTotalCount += count;
                }
                if (time >= 0) {
                    gcTotalTime += time;
                }

                gcDetails.put(
                        gc.getName(),
                        GCInfo.builder()
                                .name(gc.getName())
                                .collectionCount(count)
                                .collectionTime(time)
                                .build());
            }

            // Get performance report from profiler
            String performanceReport =
                    performanceProfiler != null
                            ? performanceProfiler.generateReport().toString()
                            : "Performance profiler not available";

            return PerformanceMetrics.builder()
                    .performanceReport(performanceReport)
                    .gcTotalCount(gcTotalCount)
                    .gcTotalTime(gcTotalTime)
                    .gcDetails(gcDetails)
                    .cpuUsage(getCpuUsage())
                    .build();

        } catch (Exception e) {
            log.error("Error collecting performance metrics", e);
            // Return empty metrics on error
            return PerformanceMetrics.builder()
                    .performanceReport("Error collecting performance metrics: " + e.getMessage())
                    .gcTotalCount(0)
                    .gcTotalTime(0)
                    .gcDetails(new HashMap<>())
                    .cpuUsage(0.0)
                    .build();
        }
    }

    /**
     * Gets current CPU usage percentage.
     *
     * @return CPU usage percentage (0-100)
     */
    public double getCpuUsage() {
        try {
            // Try to get process CPU usage first
            if (osBean instanceof com.sun.management.OperatingSystemMXBean) {
                com.sun.management.OperatingSystemMXBean sunOsBean =
                        (com.sun.management.OperatingSystemMXBean) osBean;
                double cpuUsage = sunOsBean.getProcessCpuLoad() * 100;
                if (cpuUsage >= 0) {
                    return cpuUsage;
                }
            }

            // Fallback to system load average
            double loadAverage = osBean.getSystemLoadAverage();
            if (loadAverage >= 0) {
                int processors = osBean.getAvailableProcessors();
                return Math.min(100, (loadAverage / processors) * 100);
            }
        } catch (Exception e) {
            log.error("Error getting CPU usage", e);
        }

        return 0.0;
    }

    /**
     * Gets garbage collection statistics for a specific collector.
     *
     * @param collectorName Name of the garbage collector
     * @return GCInfo or null if not found
     */
    public GCInfo getGCInfo(String collectorName) {
        try {
            for (GarbageCollectorMXBean gc : gcBeans) {
                if (gc.getName().equals(collectorName)) {
                    return GCInfo.builder()
                            .name(gc.getName())
                            .collectionCount(gc.getCollectionCount())
                            .collectionTime(gc.getCollectionTime())
                            .build();
                }
            }
        } catch (Exception e) {
            log.error("Error getting GC info for: " + collectorName, e);
        }
        return null;
    }

    /**
     * Gets average GC pause time in milliseconds.
     *
     * @return Average GC pause time
     */
    public double getAverageGCPauseTime() {
        try {
            long totalCount = 0;
            long totalTime = 0;

            for (GarbageCollectorMXBean gc : gcBeans) {
                long count = gc.getCollectionCount();
                long time = gc.getCollectionTime();

                if (count > 0 && time >= 0) {
                    totalCount += count;
                    totalTime += time;
                }
            }

            if (totalCount > 0) {
                return (double) totalTime / totalCount;
            }
        } catch (Exception e) {
            log.error("Error calculating average GC pause time", e);
        }
        return 0.0;
    }

    /**
     * Gets GC overhead percentage (time spent in GC vs total runtime).
     *
     * @return GC overhead percentage
     */
    public double getGCOverheadPercentage() {
        try {
            long uptime = runtimeBean.getUptime();
            if (uptime > 0) {
                long gcTime = 0;
                for (GarbageCollectorMXBean gc : gcBeans) {
                    long time = gc.getCollectionTime();
                    if (time >= 0) {
                        gcTime += time;
                    }
                }
                return (double) gcTime / uptime * 100;
            }
        } catch (Exception e) {
            log.error("Error calculating GC overhead", e);
        }
        return 0.0;
    }

    /**
     * Checks if system is experiencing GC pressure.
     *
     * @return true if GC overhead is above 5%
     */
    public boolean isUnderGCPressure() {
        return getGCOverheadPercentage() > 5.0;
    }

    /**
     * Gets JIT compilation metrics.
     *
     * @return Map of JIT compilation statistics
     */
    public Map<String, Object> getJITCompilationStats() {
        Map<String, Object> stats = new HashMap<>();

        try {
            CompilationMXBean compilationBean = ManagementFactory.getCompilationMXBean();
            if (compilationBean != null && compilationBean.isCompilationTimeMonitoringSupported()) {
                stats.put("compiler_name", compilationBean.getName());
                stats.put("total_compilation_time_ms", compilationBean.getTotalCompilationTime());
            }
        } catch (Exception e) {
            log.error("Error getting JIT compilation stats", e);
        }

        return stats;
    }

    /**
     * Gets class loading statistics.
     *
     * @return Map of class loading metrics
     */
    public Map<String, Object> getClassLoadingStats() {
        Map<String, Object> stats = new HashMap<>();

        try {
            ClassLoadingMXBean classLoadingBean = ManagementFactory.getClassLoadingMXBean();
            stats.put("loaded_class_count", classLoadingBean.getLoadedClassCount());
            stats.put("total_loaded_class_count", classLoadingBean.getTotalLoadedClassCount());
            stats.put("unloaded_class_count", classLoadingBean.getUnloadedClassCount());
        } catch (Exception e) {
            log.error("Error getting class loading stats", e);
        }

        return stats;
    }

    @Override
    public DiagnosticInfo getDiagnosticInfo() {
        Map<String, Object> states = new HashMap<>();

        try {
            // GC metrics
            long gcTotalCount = 0;
            long gcTotalTime = 0;
            List<Map<String, Object>> gcInfo = new ArrayList<>();

            for (GarbageCollectorMXBean gc : gcBeans) {
                Map<String, Object> collector = new HashMap<>();
                collector.put("name", gc.getName());
                collector.put("count", gc.getCollectionCount());
                collector.put("time_ms", gc.getCollectionTime());
                gcInfo.add(collector);

                if (gc.getCollectionCount() >= 0) {
                    gcTotalCount += gc.getCollectionCount();
                }
                if (gc.getCollectionTime() >= 0) {
                    gcTotalTime += gc.getCollectionTime();
                }
            }

            states.put("gc_collectors", gcInfo);
            states.put("gc_total_count", gcTotalCount);
            states.put("gc_total_time_ms", gcTotalTime);
            states.put("gc_overhead_percent", getGCOverheadPercentage());
            states.put("gc_avg_pause_ms", getAverageGCPauseTime());
            states.put("under_gc_pressure", isUnderGCPressure());

            // CPU and system metrics
            states.put("cpu_usage_percent", getCpuUsage());
            states.put("available_processors", osBean.getAvailableProcessors());
            states.put("system_load_average", osBean.getSystemLoadAverage());

            // JIT and class loading
            states.putAll(getJITCompilationStats());
            states.putAll(getClassLoadingStats());

            return DiagnosticInfo.builder()
                    .component("PerformanceDiagnosticService")
                    .states(states)
                    .build();

        } catch (Exception e) {
            log.error("Error collecting diagnostic info", e);
            return DiagnosticInfo.error("PerformanceDiagnosticService", e);
        }
    }
}
