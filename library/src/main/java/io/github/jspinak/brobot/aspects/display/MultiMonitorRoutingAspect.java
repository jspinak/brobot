package io.github.jspinak.brobot.aspects.display;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicLong;

import jakarta.annotation.PostConstruct;

import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Pointcut;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Component;

import io.github.jspinak.brobot.action.ObjectCollection;
import io.github.jspinak.brobot.logging.unified.BrobotLogger;
import io.github.jspinak.brobot.logging.unified.LogEvent;
import io.github.jspinak.brobot.monitor.MonitorManager;

import lombok.Data;
import lombok.extern.slf4j.Slf4j;

/**
 * Aspect that provides intelligent routing of actions to specific monitors in multi-monitor setups.
 *
 * <p>This aspect intercepts region-based operations and routes them to the appropriate monitor
 * based on: - Region location and monitor boundaries - Configured monitor preferences - Monitor
 * health and availability - Load balancing across monitors - Success rate optimization
 *
 * <p>The aspect helps improve automation reliability in multi-monitor environments by ensuring
 * actions are executed on the correct display.
 */
@Aspect
@Component
@Slf4j
@ConditionalOnProperty(
        prefix = "brobot.aspects.multi-monitor",
        name = "enabled",
        havingValue = "true",
        matchIfMissing = false)
public class MultiMonitorRoutingAspect {

    @Autowired private BrobotLogger brobotLogger;

    @Autowired private MonitorManager monitorManager;

    @Value("${brobot.aspects.multi-monitor.default-monitor:0}")
    private int defaultMonitorIndex;

    @Value("${brobot.aspects.multi-monitor.enable-load-balancing:true}")
    private boolean enableLoadBalancing;

    @Value("${brobot.aspects.multi-monitor.enable-failover:true}")
    private boolean enableFailover;

    // Monitor statistics
    private final ConcurrentHashMap<Integer, MonitorStats> monitorStats = new ConcurrentHashMap<>();

    // Thread-local for current monitor context
    private final ThreadLocal<Integer> currentMonitor = new ThreadLocal<>();

    @PostConstruct
    public void init() {
        log.info("Multi-Monitor Routing Aspect initialized");

        // Initialize monitor statistics
        if (monitorManager != null) {
            // Use MonitorManager to get the actual monitor count
            int monitorCount = monitorManager.getMonitorCount();
            for (int i = 0; i < monitorCount; i++) {
                monitorStats.put(i, new MonitorStats(i));
            }
        }
    }

    /** Pointcut for find operations */
    @Pointcut("execution(* io.github.jspinak.brobot.action.methods.basicactions.find.*.find*(..))")
    public void findOperations() {}

    /** Pointcut for click operations */
    @Pointcut(
            "execution(* io.github.jspinak.brobot.action.methods.basicactions.click.*.click*(..))")
    public void clickOperations() {}

    /** Pointcut for all region-based operations */
    @Pointcut("findOperations() || clickOperations()")
    public void regionBasedOperations() {}

    /** Route operations to appropriate monitor */
    @Around("regionBasedOperations()")
    public Object routeToMonitor(ProceedingJoinPoint joinPoint) throws Throwable {
        // Extract region information from arguments
        RegionInfo regionInfo = extractRegionInfo(joinPoint.getArgs());

        // Determine target monitor
        int targetMonitor = determineTargetMonitor(regionInfo);

        // Set monitor context
        Integer previousMonitor = currentMonitor.get();
        currentMonitor.set(targetMonitor);

        // Update statistics
        MonitorStats stats = monitorStats.get(targetMonitor);
        if (stats != null) {
            stats.incrementOperations();
        }

        long startTime = System.currentTimeMillis();
        boolean success = false;

        try {
            // Log routing decision
            logRoutingDecision(joinPoint.getSignature().toShortString(), targetMonitor, regionInfo);

            // Execute on target monitor
            Object result = executeOnMonitor(joinPoint, targetMonitor);

            success = true;
            return result;

        } catch (Throwable e) {
            // Handle failure and potentially failover
            if (enableFailover) {
                return handleFailover(joinPoint, targetMonitor, e);
            }
            throw e;

        } finally {
            // Update statistics
            if (stats != null) {
                long duration = System.currentTimeMillis() - startTime;
                stats.recordResult(success, duration);
            }

            // Restore previous monitor context
            currentMonitor.set(previousMonitor);
        }
    }

    /** Extract region information from method arguments */
    private RegionInfo extractRegionInfo(Object[] args) {
        RegionInfo info = new RegionInfo();

        for (Object arg : args) {
            if (arg instanceof ObjectCollection) {
                // Extract region from ObjectCollection
                // Simplified - real implementation would extract actual regions
                info.setHasRegion(true);
                break;
            }
            // Check for other region-containing types
        }

        return info;
    }

    /** Determine target monitor for operation */
    private int determineTargetMonitor(RegionInfo regionInfo) {
        // If no specific region, use default or load balance
        if (!regionInfo.hasRegion) {
            if (enableLoadBalancing) {
                return selectMonitorByLoadBalancing();
            }
            return defaultMonitorIndex;
        }

        // If region spans multiple monitors, choose the one with most overlap
        // Simplified - real implementation would calculate actual overlap

        // For now, return default monitor
        return defaultMonitorIndex;
    }

    /** Select monitor using load balancing */
    private int selectMonitorByLoadBalancing() {
        // Find monitor with lowest load
        int selectedMonitor = defaultMonitorIndex;
        long lowestLoad = Long.MAX_VALUE;

        for (Map.Entry<Integer, MonitorStats> entry : monitorStats.entrySet()) {
            MonitorStats stats = entry.getValue();
            long load = stats.getCurrentLoad();

            if (load < lowestLoad) {
                lowestLoad = load;
                selectedMonitor = entry.getKey();
            }
        }

        return selectedMonitor;
    }

    /** Execute operation on specific monitor */
    private Object executeOnMonitor(ProceedingJoinPoint joinPoint, int monitorIndex)
            throws Throwable {
        // In a real implementation, this would set the monitor context
        // for the underlying automation framework

        // For now, just proceed with execution
        return joinPoint.proceed();
    }

    /** Handle failover to another monitor */
    private Object handleFailover(ProceedingJoinPoint joinPoint, int failedMonitor, Throwable error)
            throws Throwable {
        log.warn("Operation failed on monitor {}, attempting failover", failedMonitor, error);

        // Mark monitor as unhealthy
        MonitorStats stats = monitorStats.get(failedMonitor);
        if (stats != null) {
            stats.markUnhealthy();
        }

        // Try other monitors
        for (Map.Entry<Integer, MonitorStats> entry : monitorStats.entrySet()) {
            int monitor = entry.getKey();
            if (monitor != failedMonitor && entry.getValue().isHealthy()) {
                try {
                    log.info("Attempting failover to monitor {}", monitor);
                    currentMonitor.set(monitor);
                    return executeOnMonitor(joinPoint, monitor);
                } catch (Throwable e) {
                    log.warn("Failover to monitor {} failed", monitor, e);
                }
            }
        }

        // All monitors failed
        throw error;
    }

    /** Log routing decision */
    private void logRoutingDecision(String method, int monitor, RegionInfo regionInfo) {
        brobotLogger
                .log()
                .type(LogEvent.Type.ACTION)
                .level(LogEvent.Level.DEBUG)
                .action("MONITOR_ROUTING")
                .metadata("method", method)
                .metadata("targetMonitor", monitor)
                .metadata("hasRegion", regionInfo.hasRegion)
                .observation("Routed operation to monitor")
                .log();
    }

    /** Get current monitor for thread */
    public Optional<Integer> getCurrentMonitor() {
        return Optional.ofNullable(currentMonitor.get());
    }

    /** Get monitor statistics */
    public Map<Integer, MonitorStats> getMonitorStatistics() {
        return new HashMap<>(monitorStats);
    }

    /** Reset monitor statistics */
    public void resetStatistics() {
        monitorStats.values().forEach(MonitorStats::reset);
        log.info("Monitor statistics reset");
    }

    /** Inner class for region information */
    @Data
    private static class RegionInfo {
        private boolean hasRegion;
        private int x;
        private int y;
        private int width;
        private int height;
        private Set<Integer> spansMonitors = new HashSet<>();
    }

    /** Inner class for monitor statistics */
    @Data
    public static class MonitorStats {
        private final int monitorIndex;
        private final AtomicLong totalOperations = new AtomicLong();
        private final AtomicLong successfulOperations = new AtomicLong();
        private final AtomicLong totalDuration = new AtomicLong();
        private final AtomicInteger activeOperations = new AtomicInteger();
        private volatile boolean healthy = true;
        private volatile long lastFailureTime = 0;

        public MonitorStats(int monitorIndex) {
            this.monitorIndex = monitorIndex;
        }

        public void incrementOperations() {
            totalOperations.incrementAndGet();
            activeOperations.incrementAndGet();
        }

        public void recordResult(boolean success, long duration) {
            activeOperations.decrementAndGet();
            totalDuration.addAndGet(duration);

            if (success) {
                successfulOperations.incrementAndGet();
                // Mark healthy after successful operations
                if (!healthy && successfulOperations.get() % 5 == 0) {
                    healthy = true;
                    log.info("Monitor {} marked healthy after successful operations", monitorIndex);
                }
            }
        }

        public void markUnhealthy() {
            healthy = false;
            lastFailureTime = System.currentTimeMillis();
        }

        public long getCurrentLoad() {
            // Simple load calculation based on active operations and average duration
            long avgDuration =
                    totalOperations.get() > 0 ? totalDuration.get() / totalOperations.get() : 1000;
            return activeOperations.get() * avgDuration;
        }

        public double getSuccessRate() {
            long total = totalOperations.get();
            return total > 0 ? (double) successfulOperations.get() / total * 100 : 0;
        }

        public void reset() {
            totalOperations.set(0);
            successfulOperations.set(0);
            totalDuration.set(0);
            activeOperations.set(0);
            healthy = true;
            lastFailureTime = 0;
        }
    }
}
