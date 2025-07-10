package io.github.jspinak.brobot.runner.performance.thread;

import io.github.jspinak.brobot.runner.common.diagnostics.DiagnosticCapable;
import io.github.jspinak.brobot.runner.common.diagnostics.DiagnosticInfo;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import jakarta.annotation.PostConstruct;
import java.lang.management.ManagementFactory;
import java.lang.management.ThreadInfo;
import java.lang.management.ThreadMXBean;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicLong;
import java.util.stream.Collectors;

/**
 * Service responsible for monitoring thread health and collecting metrics.
 * 
 * This service provides comprehensive thread monitoring capabilities including
 * thread count tracking, contention detection, deadlock detection, and
 * performance metrics collection.
 * 
 * Thread Safety: This class is thread-safe.
 * 
 * @since 1.0.0
 */
@Slf4j
@Service
public class ThreadMonitoringService implements DiagnosticCapable {
    
    private final ThreadMXBean threadBean = ManagementFactory.getThreadMXBean();
    private final ThreadPoolManagementService poolManagement;
    
    // Metrics
    private final AtomicLong totalThreadsCreated = new AtomicLong();
    private final AtomicLong totalContentionEvents = new AtomicLong();
    private final AtomicLong totalDeadlocksDetected = new AtomicLong();
    private volatile int peakThreadCount = 0;
    
    // Contention tracking
    private final Map<Long, ThreadContentionInfo> contentionMap = new ConcurrentHashMap<>();
    
    // Diagnostic mode
    private final AtomicBoolean diagnosticMode = new AtomicBoolean(false);
    
    @Autowired
    public ThreadMonitoringService(ThreadPoolManagementService poolManagement) {
        this.poolManagement = poolManagement;
    }
    
    @PostConstruct
    public void initialize() {
        // Enable thread contention monitoring if supported
        if (threadBean.isThreadContentionMonitoringSupported()) {
            threadBean.setThreadContentionMonitoringEnabled(true);
            log.info("Thread contention monitoring enabled");
        } else {
            log.warn("Thread contention monitoring not supported on this JVM");
        }
        
        // Initialize peak thread count
        peakThreadCount = threadBean.getPeakThreadCount();
    }
    
    /**
     * Get current thread statistics.
     * 
     * @return thread statistics
     */
    public ThreadStatistics getCurrentStatistics() {
        int currentThreads = threadBean.getThreadCount();
        int daemonThreads = threadBean.getDaemonThreadCount();
        long totalStarted = threadBean.getTotalStartedThreadCount();
        
        // Update metrics
        totalThreadsCreated.set(totalStarted);
        if (currentThreads > peakThreadCount) {
            peakThreadCount = currentThreads;
        }
        
        // Get pool statistics
        Map<String, ThreadPoolHealth> poolHealth = poolManagement.getAllPoolHealth();
        
        return new ThreadStatistics(
            currentThreads,
            daemonThreads,
            peakThreadCount,
            totalStarted,
            poolHealth,
            getSystemThreadGroups()
        );
    }
    
    /**
     * Detect thread contention.
     * 
     * @return list of contention information
     */
    public List<ThreadContentionInfo> detectContention() {
        if (!threadBean.isThreadContentionMonitoringEnabled()) {
            return Collections.emptyList();
        }
        
        List<ThreadContentionInfo> contentions = new ArrayList<>();
        ThreadInfo[] threads = threadBean.getThreadInfo(
            threadBean.getAllThreadIds(), Integer.MAX_VALUE);
        
        for (ThreadInfo thread : threads) {
            if (thread != null && thread.getBlockedCount() > 0) {
                long threadId = thread.getThreadId();
                
                ThreadContentionInfo existing = contentionMap.get(threadId);
                long currentBlocked = thread.getBlockedCount();
                long currentWaited = thread.getWaitedCount();
                
                if (existing != null) {
                    // Check if contention increased
                    if (currentBlocked > existing.getBlockedCount() ||
                        currentWaited > existing.getWaitedCount()) {
                        
                        totalContentionEvents.incrementAndGet();
                        
                        ThreadContentionInfo updated = new ThreadContentionInfo(
                            thread.getThreadName(),
                            threadId,
                            currentBlocked,
                            currentWaited,
                            thread.getBlockedTime(),
                            thread.getWaitedTime(),
                            thread.getLockName(),
                            thread.getLockOwnerName()
                        );
                        
                        contentionMap.put(threadId, updated);
                        contentions.add(updated);
                    }
                } else {
                    // New thread with contention
                    ThreadContentionInfo info = new ThreadContentionInfo(
                        thread.getThreadName(),
                        threadId,
                        currentBlocked,
                        currentWaited,
                        thread.getBlockedTime(),
                        thread.getWaitedTime(),
                        thread.getLockName(),
                        thread.getLockOwnerName()
                    );
                    
                    contentionMap.put(threadId, info);
                    if (currentBlocked > 10 || currentWaited > 10) { // Threshold
                        contentions.add(info);
                    }
                }
            }
        }
        
        if (diagnosticMode.get() && !contentions.isEmpty()) {
            log.info("[DIAGNOSTIC] Detected {} threads with contention", contentions.size());
        }
        
        return contentions;
    }
    
    /**
     * Detect deadlocked threads.
     * 
     * @return list of deadlocked thread info
     */
    public List<ThreadInfo> detectDeadlocks() {
        long[] deadlockedThreadIds = threadBean.findDeadlockedThreads();
        
        if (deadlockedThreadIds == null || deadlockedThreadIds.length == 0) {
            return Collections.emptyList();
        }
        
        totalDeadlocksDetected.addAndGet(deadlockedThreadIds.length);
        
        ThreadInfo[] deadlockedThreads = threadBean.getThreadInfo(deadlockedThreadIds);
        List<ThreadInfo> result = Arrays.stream(deadlockedThreads)
                .filter(Objects::nonNull)
                .collect(Collectors.toList());
        
        log.error("Detected {} deadlocked threads!", result.size());
        for (ThreadInfo thread : result) {
            log.error("Deadlocked thread: {} (ID: {})", 
                    thread.getThreadName(), thread.getThreadId());
        }
        
        return result;
    }
    
    /**
     * Get detailed information about specific threads.
     * 
     * @param threadIds thread IDs to get info for
     * @return map of thread ID to thread info
     */
    public Map<Long, ThreadInfo> getThreadDetails(long... threadIds) {
        ThreadInfo[] threads = threadBean.getThreadInfo(threadIds, Integer.MAX_VALUE);
        
        Map<Long, ThreadInfo> result = new HashMap<>();
        for (ThreadInfo thread : threads) {
            if (thread != null) {
                result.put(thread.getThreadId(), thread);
            }
        }
        
        return result;
    }
    
    /**
     * Get all thread details.
     * 
     * @return map of all threads
     */
    public Map<Long, ThreadInfo> getAllThreadDetails() {
        return getThreadDetails(threadBean.getAllThreadIds());
    }
    
    /**
     * Enable or disable thread contention monitoring.
     * 
     * @param enable true to enable
     */
    public void enableContentionMonitoring(boolean enable) {
        if (threadBean.isThreadContentionMonitoringSupported()) {
            threadBean.setThreadContentionMonitoringEnabled(enable);
            log.info("Thread contention monitoring {}", enable ? "enabled" : "disabled");
        }
    }
    
    /**
     * Reset thread peak count.
     */
    public void resetPeakThreadCount() {
        threadBean.resetPeakThreadCount();
        peakThreadCount = threadBean.getThreadCount();
        log.info("Reset peak thread count to {}", peakThreadCount);
    }
    
    /**
     * Monitor threads periodically and log warnings.
     */
    @Scheduled(fixedDelay = 30000) // Every 30 seconds
    public void monitorThreadHealth() {
        ThreadStatistics stats = getCurrentStatistics();
        
        // Check for high thread count
        if (stats.getCurrentThreadCount() > 200) {
            log.warn("High thread count detected: {} threads", stats.getCurrentThreadCount());
        }
        
        // Check for deadlocks
        List<ThreadInfo> deadlocks = detectDeadlocks();
        if (!deadlocks.isEmpty()) {
            log.error("DEADLOCK DETECTED! {} threads are deadlocked", deadlocks.size());
        }
        
        // Check for thread leaks
        if (stats.getTotalStartedThreads() - stats.getCurrentThreadCount() > 1000) {
            log.warn("Possible thread leak detected. Total started: {}, Current: {}",
                    stats.getTotalStartedThreads(), stats.getCurrentThreadCount());
        }
        
        if (diagnosticMode.get()) {
            log.info("[DIAGNOSTIC] Thread health check - Current: {}, Peak: {}, Daemon: {}",
                    stats.getCurrentThreadCount(), stats.getPeakThreadCount(), 
                    stats.getDaemonThreadCount());
        }
    }
    
    @Override
    public DiagnosticInfo getDiagnosticInfo() {
        ThreadStatistics stats = getCurrentStatistics();
        
        Map<String, Object> states = new HashMap<>();
        states.put("currentThreads", stats.getCurrentThreadCount());
        states.put("daemonThreads", stats.getDaemonThreadCount());
        states.put("peakThreads", stats.getPeakThreadCount());
        states.put("totalStartedThreads", stats.getTotalStartedThreads());
        states.put("totalContentionEvents", totalContentionEvents.get());
        states.put("totalDeadlocksDetected", totalDeadlocksDetected.get());
        states.put("contentionMonitoringEnabled", threadBean.isThreadContentionMonitoringEnabled());
        
        // Add thread group breakdown
        stats.getThreadGroups().forEach((group, count) -> {
            states.put("threadGroup." + group, count);
        });
        
        return DiagnosticInfo.builder()
                .component("ThreadMonitoringService")
                .states(states)
                .build();
    }
    
    @Override
    public boolean isDiagnosticModeEnabled() {
        return diagnosticMode.get();
    }
    
    @Override
    public void enableDiagnosticMode(boolean enabled) {
        diagnosticMode.set(enabled);
        log.info("Diagnostic mode {} for ThreadMonitoringService", 
                enabled ? "enabled" : "disabled");
    }
    
    private Map<String, Integer> getSystemThreadGroups() {
        Map<String, Integer> groups = new HashMap<>();
        Map<Thread, StackTraceElement[]> allThreads = Thread.getAllStackTraces();
        
        for (Thread thread : allThreads.keySet()) {
            String groupName = thread.getThreadGroup() != null ? 
                thread.getThreadGroup().getName() : "unknown";
            groups.merge(groupName, 1, Integer::sum);
        }
        
        return groups;
    }
    
    /**
     * Thread statistics snapshot.
     */
    @Data
    public static class ThreadStatistics {
        private final int currentThreadCount;
        private final int daemonThreadCount;
        private final int peakThreadCount;
        private final long totalStartedThreads;
        private final Map<String, ThreadPoolHealth> poolHealth;
        private final Map<String, Integer> threadGroups;
        
        public int getTotalPoolThreads() {
            return poolHealth.values().stream()
                    .mapToInt(ThreadPoolHealth::poolSize)
                    .sum();
        }
        
        public int getTotalActivePoolThreads() {
            return poolHealth.values().stream()
                    .mapToInt(ThreadPoolHealth::activeThreads)
                    .sum();
        }
    }
    
    /**
     * Thread contention information.
     */
    @Data
    public static class ThreadContentionInfo {
        private final String threadName;
        private final long threadId;
        private final long blockedCount;
        private final long waitedCount;
        private final long blockedTime;
        private final long waitedTime;
        private final String lockName;
        private final String lockOwnerName;
        
        public boolean isHighContention() {
            return blockedCount > 100 || waitedCount > 100 ||
                   blockedTime > 1000 || waitedTime > 1000;
        }
    }
}