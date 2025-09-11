package io.github.jspinak.brobot.runner.diagnostics.services;

import java.lang.management.ManagementFactory;
import java.lang.management.MemoryMXBean;
import java.lang.management.MemoryPoolMXBean;
import java.lang.management.MemoryUsage;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.springframework.stereotype.Service;

import io.github.jspinak.brobot.runner.common.diagnostics.DiagnosticCapable;
import io.github.jspinak.brobot.runner.common.diagnostics.DiagnosticInfo;
import io.github.jspinak.brobot.runner.diagnostics.MemoryInfo;
import io.github.jspinak.brobot.runner.diagnostics.MemoryPoolInfo;

import lombok.extern.slf4j.Slf4j;

/**
 * Service responsible for collecting memory diagnostic information. Provides detailed memory usage
 * statistics for heap, non-heap, and memory pools.
 */
@Slf4j
@Service
public class MemoryDiagnosticService implements DiagnosticCapable {

    private final MemoryMXBean memoryBean = ManagementFactory.getMemoryMXBean();
    private final List<MemoryPoolMXBean> memoryPoolBeans = ManagementFactory.getMemoryPoolMXBeans();

    /**
     * Collects comprehensive memory information including heap, non-heap, and pool usage.
     *
     * @return MemoryInfo containing all memory metrics
     */
    public MemoryInfo collectMemoryInfo() {
        log.debug("Collecting memory diagnostic information");

        try {
            MemoryUsage heapUsage = memoryBean.getHeapMemoryUsage();
            MemoryUsage nonHeapUsage = memoryBean.getNonHeapMemoryUsage();

            return MemoryInfo.builder()
                    .heapUsed(heapUsage.getUsed())
                    .heapMax(heapUsage.getMax())
                    .heapCommitted(heapUsage.getCommitted())
                    .nonHeapUsed(nonHeapUsage.getUsed())
                    .nonHeapMax(nonHeapUsage.getMax())
                    .nonHeapCommitted(nonHeapUsage.getCommitted())
                    .memoryPools(collectMemoryPools())
                    .build();
        } catch (Exception e) {
            log.error("Error collecting memory information", e);
            // Return empty memory info on error
            return MemoryInfo.builder()
                    .heapUsed(0)
                    .heapMax(0)
                    .heapCommitted(0)
                    .nonHeapUsed(0)
                    .nonHeapMax(0)
                    .nonHeapCommitted(0)
                    .memoryPools(new HashMap<>())
                    .build();
        }
    }

    /**
     * Collects information about all memory pools.
     *
     * @return Map of memory pool names to their information
     */
    public Map<String, MemoryPoolInfo> collectMemoryPools() {
        Map<String, MemoryPoolInfo> pools = new HashMap<>();

        try {
            for (MemoryPoolMXBean pool : memoryPoolBeans) {
                MemoryUsage usage = pool.getUsage();
                if (usage != null) {
                    pools.put(
                            pool.getName(),
                            MemoryPoolInfo.builder()
                                    .name(pool.getName())
                                    .type(pool.getType().toString())
                                    .used(usage.getUsed())
                                    .max(usage.getMax())
                                    .committed(usage.getCommitted())
                                    .build());
                }
            }
        } catch (Exception e) {
            log.error("Error collecting memory pool information", e);
        }

        return pools;
    }

    /**
     * Gets current heap memory usage percentage.
     *
     * @return Heap usage percentage (0-100)
     */
    public double getHeapUsagePercentage() {
        try {
            MemoryUsage heapUsage = memoryBean.getHeapMemoryUsage();
            if (heapUsage.getMax() > 0) {
                return (double) heapUsage.getUsed() / heapUsage.getMax() * 100;
            }
        } catch (Exception e) {
            log.error("Error calculating heap usage percentage", e);
        }
        return 0.0;
    }

    /**
     * Gets current non-heap memory usage percentage.
     *
     * @return Non-heap usage percentage (0-100)
     */
    public double getNonHeapUsagePercentage() {
        try {
            MemoryUsage nonHeapUsage = memoryBean.getNonHeapMemoryUsage();
            if (nonHeapUsage.getMax() > 0 && nonHeapUsage.getMax() != -1) {
                return (double) nonHeapUsage.getUsed() / nonHeapUsage.getMax() * 100;
            }
        } catch (Exception e) {
            log.error("Error calculating non-heap usage percentage", e);
        }
        return 0.0;
    }

    /** Forces garbage collection. */
    public void runGarbageCollection() {
        log.info("Running garbage collection");
        System.gc();
    }

    /**
     * Gets memory usage for a specific pool.
     *
     * @param poolName Name of the memory pool
     * @return MemoryPoolInfo or null if pool not found
     */
    public MemoryPoolInfo getMemoryPoolInfo(String poolName) {
        try {
            for (MemoryPoolMXBean pool : memoryPoolBeans) {
                if (pool.getName().equals(poolName)) {
                    MemoryUsage usage = pool.getUsage();
                    if (usage != null) {
                        return MemoryPoolInfo.builder()
                                .name(pool.getName())
                                .type(pool.getType().toString())
                                .used(usage.getUsed())
                                .max(usage.getMax())
                                .committed(usage.getCommitted())
                                .build();
                    }
                }
            }
        } catch (Exception e) {
            log.error("Error getting memory pool info for: " + poolName, e);
        }
        return null;
    }

    @Override
    public DiagnosticInfo getDiagnosticInfo() {
        Map<String, Object> states = new HashMap<>();

        try {
            MemoryUsage heapUsage = memoryBean.getHeapMemoryUsage();
            MemoryUsage nonHeapUsage = memoryBean.getNonHeapMemoryUsage();

            states.put("heap_used_mb", heapUsage.getUsed() / (1024 * 1024));
            states.put("heap_max_mb", heapUsage.getMax() / (1024 * 1024));
            states.put("heap_usage_percent", getHeapUsagePercentage());
            states.put("non_heap_used_mb", nonHeapUsage.getUsed() / (1024 * 1024));
            states.put("memory_pool_count", memoryPoolBeans.size());

            // Add pool summaries
            Map<String, Long> poolUsage = new HashMap<>();
            for (MemoryPoolMXBean pool : memoryPoolBeans) {
                MemoryUsage usage = pool.getUsage();
                if (usage != null) {
                    poolUsage.put(pool.getName(), usage.getUsed() / (1024 * 1024));
                }
            }
            states.put("pool_usage_mb", poolUsage);

            return DiagnosticInfo.builder()
                    .component("MemoryDiagnosticService")
                    .states(states)
                    .build();

        } catch (Exception e) {
            log.error("Error collecting diagnostic info", e);
            return DiagnosticInfo.error("MemoryDiagnosticService", e);
        }
    }
}
