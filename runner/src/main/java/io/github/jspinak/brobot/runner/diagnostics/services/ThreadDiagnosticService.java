package io.github.jspinak.brobot.runner.diagnostics.services;

import java.lang.management.ManagementFactory;
import java.lang.management.ThreadInfo;
import java.lang.management.ThreadMXBean;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

import org.springframework.stereotype.Service;

import io.github.jspinak.brobot.runner.common.diagnostics.DiagnosticCapable;
import io.github.jspinak.brobot.runner.common.diagnostics.DiagnosticInfo;
import io.github.jspinak.brobot.runner.diagnostics.ThreadDiagnosticInfo;

import lombok.extern.slf4j.Slf4j;

/**
 * Service responsible for collecting thread diagnostic information. Provides thread count, deadlock
 * detection, and thread state analysis.
 */
@Slf4j
@Service
public class ThreadDiagnosticService implements DiagnosticCapable {

    private final ThreadMXBean threadBean = ManagementFactory.getThreadMXBean();
    private final Map<Long, ThreadInfo> lastThreadSnapshot = new ConcurrentHashMap<>();

    /**
     * Collects comprehensive thread information including counts and states.
     *
     * @return ThreadDiagnosticInfo containing all thread metrics
     */
    public ThreadDiagnosticInfo collectThreadInfo() {
        log.debug("Collecting thread diagnostic information");

        try {
            long[] deadlockedThreads = threadBean.findDeadlockedThreads();
            Map<Thread.State, Integer> threadStates = collectThreadStates();

            return ThreadDiagnosticInfo.builder()
                    .threadCount(threadBean.getThreadCount())
                    .peakThreadCount(threadBean.getPeakThreadCount())
                    .daemonThreadCount(threadBean.getDaemonThreadCount())
                    .totalStartedThreadCount(threadBean.getTotalStartedThreadCount())
                    .deadlockedThreads(deadlockedThreads != null ? deadlockedThreads.length : 0)
                    .threadStates(threadStates)
                    .build();
        } catch (Exception e) {
            log.error("Error collecting thread information", e);
            // Return empty thread info on error
            return ThreadDiagnosticInfo.builder()
                    .threadCount(0)
                    .peakThreadCount(0)
                    .daemonThreadCount(0)
                    .totalStartedThreadCount(0)
                    .deadlockedThreads(0)
                    .threadStates(new HashMap<>())
                    .build();
        }
    }

    /**
     * Collects thread states distribution.
     *
     * @return Map of thread states to their counts
     */
    private Map<Thread.State, Integer> collectThreadStates() {
        Map<Thread.State, Integer> threadStates = new HashMap<>();

        try {
            for (ThreadInfo info : threadBean.dumpAllThreads(false, false)) {
                if (info != null) {
                    Thread.State state = info.getThreadState();
                    threadStates.merge(state, 1, Integer::sum);
                }
            }
        } catch (Exception e) {
            log.error("Error collecting thread states", e);
        }

        return threadStates;
    }

    /**
     * Detects deadlocked threads.
     *
     * @return List of deadlocked thread information, empty if none found
     */
    public List<ThreadInfo> detectDeadlockedThreads() {
        List<ThreadInfo> deadlocked = new ArrayList<>();

        try {
            long[] deadlockedThreadIds = threadBean.findDeadlockedThreads();
            if (deadlockedThreadIds != null && deadlockedThreadIds.length > 0) {
                ThreadInfo[] infos = threadBean.getThreadInfo(deadlockedThreadIds, true, true);
                for (ThreadInfo info : infos) {
                    if (info != null) {
                        deadlocked.add(info);
                    }
                }
            }
        } catch (Exception e) {
            log.error("Error detecting deadlocked threads", e);
        }

        return deadlocked;
    }

    /**
     * Gets detailed information about a specific thread.
     *
     * @param threadId The thread ID
     * @return ThreadInfo or null if not found
     */
    public ThreadInfo getThreadInfo(long threadId) {
        try {
            ThreadInfo[] infos = threadBean.getThreadInfo(new long[] {threadId}, true, true);
            return infos != null && infos.length > 0 ? infos[0] : null;
        } catch (Exception e) {
            log.error("Error getting thread info for ID: " + threadId, e);
            return null;
        }
    }

    /**
     * Gets threads consuming high CPU.
     *
     * @param threshold CPU usage threshold percentage (0-100)
     * @return List of high CPU threads
     */
    public List<ThreadInfo> getHighCpuThreads(double threshold) {
        List<ThreadInfo> highCpuThreads = new ArrayList<>();

        if (!threadBean.isThreadCpuTimeSupported() || !threadBean.isThreadCpuTimeEnabled()) {
            log.warn("Thread CPU time monitoring not supported or enabled");
            return highCpuThreads;
        }

        try {
            Map<Long, Long> cpuTimes = new HashMap<>();
            long totalCpuTime = 0;

            // Collect CPU times
            for (long threadId : threadBean.getAllThreadIds()) {
                long cpuTime = threadBean.getThreadCpuTime(threadId);
                if (cpuTime > 0) {
                    cpuTimes.put(threadId, cpuTime);
                    totalCpuTime += cpuTime;
                }
            }

            // Find high CPU threads
            if (totalCpuTime > 0) {
                for (Map.Entry<Long, Long> entry : cpuTimes.entrySet()) {
                    double cpuPercent = (double) entry.getValue() / totalCpuTime * 100;
                    if (cpuPercent >= threshold) {
                        ThreadInfo info = threadBean.getThreadInfo(entry.getKey());
                        if (info != null) {
                            highCpuThreads.add(info);
                        }
                    }
                }
            }
        } catch (Exception e) {
            log.error("Error finding high CPU threads", e);
        }

        return highCpuThreads;
    }

    /** Resets peak thread count. */
    public void resetPeakThreadCount() {
        try {
            threadBean.resetPeakThreadCount();
            log.info("Peak thread count reset");
        } catch (Exception e) {
            log.error("Error resetting peak thread count", e);
        }
    }

    /** Takes a snapshot of all threads for comparison. */
    public void takeThreadSnapshot() {
        try {
            lastThreadSnapshot.clear();
            ThreadInfo[] threads = threadBean.dumpAllThreads(true, true);
            for (ThreadInfo info : threads) {
                if (info != null) {
                    lastThreadSnapshot.put(info.getThreadId(), info);
                }
            }
            log.debug("Thread snapshot taken with {} threads", lastThreadSnapshot.size());
        } catch (Exception e) {
            log.error("Error taking thread snapshot", e);
        }
    }

    /**
     * Gets new threads since last snapshot.
     *
     * @return List of new thread information
     */
    public List<ThreadInfo> getNewThreadsSinceSnapshot() {
        List<ThreadInfo> newThreads = new ArrayList<>();

        try {
            ThreadInfo[] currentThreads = threadBean.dumpAllThreads(false, false);
            for (ThreadInfo info : currentThreads) {
                if (info != null && !lastThreadSnapshot.containsKey(info.getThreadId())) {
                    newThreads.add(info);
                }
            }
        } catch (Exception e) {
            log.error("Error finding new threads", e);
        }

        return newThreads;
    }

    @Override
    public DiagnosticInfo getDiagnosticInfo() {
        Map<String, Object> states = new HashMap<>();

        try {
            states.put("thread_count", threadBean.getThreadCount());
            states.put("peak_thread_count", threadBean.getPeakThreadCount());
            states.put("daemon_thread_count", threadBean.getDaemonThreadCount());
            states.put("total_started_threads", threadBean.getTotalStartedThreadCount());

            // Check for deadlocks
            long[] deadlocked = threadBean.findDeadlockedThreads();
            states.put("deadlocked_threads", deadlocked != null ? deadlocked.length : 0);

            // Thread states
            Map<Thread.State, Integer> threadStates = collectThreadStates();
            states.put("thread_states", threadStates);

            // CPU time support
            states.put("cpu_time_supported", threadBean.isThreadCpuTimeSupported());
            states.put("cpu_time_enabled", threadBean.isThreadCpuTimeEnabled());

            return DiagnosticInfo.builder()
                    .component("ThreadDiagnosticService")
                    .states(states)
                    .build();

        } catch (Exception e) {
            log.error("Error collecting diagnostic info", e);
            return DiagnosticInfo.error("ThreadDiagnosticService", e);
        }
    }
}
