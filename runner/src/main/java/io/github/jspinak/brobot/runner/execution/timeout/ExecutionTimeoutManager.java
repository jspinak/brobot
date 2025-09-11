package io.github.jspinak.brobot.runner.execution.timeout;

import java.time.Duration;
import java.time.Instant;
import java.util.Map;
import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicBoolean;

import jakarta.annotation.PreDestroy;

import org.springframework.stereotype.Component;

import io.github.jspinak.brobot.runner.common.diagnostics.DiagnosticCapable;
import io.github.jspinak.brobot.runner.common.diagnostics.DiagnosticInfo;
import io.github.jspinak.brobot.runner.execution.context.ExecutionContext;

import lombok.extern.slf4j.Slf4j;

/**
 * Manages execution timeouts and ensures tasks don't run indefinitely.
 *
 * <p>This class monitors running executions and triggers timeout actions when executions exceed
 * their configured time limits.
 *
 * <p>Thread Safety: This class is thread-safe.
 *
 * @since 1.0.0
 */
@Slf4j
@Component
public class ExecutionTimeoutManager implements DiagnosticCapable {

    // Configuration constants
    private static final Duration CHECK_INTERVAL = Duration.ofSeconds(1);
    private static final Duration DEFAULT_TIMEOUT = Duration.ofMinutes(5);

    // Scheduled executor for timeout checks
    private final ScheduledExecutorService scheduler;

    // Tracks monitored executions
    private final Map<String, TimeoutMonitor> monitors = new ConcurrentHashMap<>();

    // Timeout handlers
    private final Map<String, TimeoutHandler> handlers = new ConcurrentHashMap<>();

    // Diagnostic mode flag
    private final AtomicBoolean diagnosticMode = new AtomicBoolean(false);

    // Global timeout checking task
    private ScheduledFuture<?> checkTask;

    public ExecutionTimeoutManager() {
        this.scheduler =
                Executors.newScheduledThreadPool(
                        1,
                        r -> {
                            Thread thread = new Thread(r, "brobot-timeout-monitor");
                            thread.setDaemon(true);
                            return thread;
                        });

        // Start periodic timeout checking
        startTimeoutChecking();

        log.info("ExecutionTimeoutManager initialized with check interval: {}", CHECK_INTERVAL);
    }

    /**
     * Monitors an execution for timeout.
     *
     * @param context execution context containing timeout configuration
     * @param handler callback to invoke on timeout
     */
    public void monitor(ExecutionContext context, TimeoutHandler handler) {
        if (context == null || handler == null) {
            throw new IllegalArgumentException("Context and handler must not be null");
        }

        Duration timeout =
                context.getOptions() != null && context.getOptions().getTimeout() != null
                        ? context.getOptions().getTimeout()
                        : DEFAULT_TIMEOUT;

        TimeoutMonitor monitor =
                new TimeoutMonitor(
                        context.getId(),
                        context.getTaskName(),
                        context.getStartTime(),
                        timeout,
                        context.getCorrelationId());

        monitors.put(context.getId(), monitor);
        handlers.put(context.getId(), handler);

        log.debug("Monitoring execution {} for timeout after {}", context.getTaskName(), timeout);

        if (diagnosticMode.get()) {
            log.info(
                    "[DIAGNOSTIC] Timeout monitor added - ID: {}, Task: {}, Timeout: {}",
                    context.getId(),
                    context.getTaskName(),
                    timeout);
        }
    }

    /**
     * Stops monitoring an execution.
     *
     * @param executionId the execution ID
     */
    public void stopMonitoring(String executionId) {
        TimeoutMonitor monitor = monitors.remove(executionId);
        handlers.remove(executionId);

        if (monitor != null) {
            log.debug("Stopped monitoring execution {}", monitor.getTaskName());
        }
    }

    /**
     * Checks if an execution has timed out.
     *
     * @param executionId the execution ID
     * @return true if the execution has exceeded its timeout
     */
    public boolean isTimedOut(String executionId) {
        TimeoutMonitor monitor = monitors.get(executionId);
        return monitor != null && monitor.isTimedOut();
    }

    /**
     * Gets the remaining time before timeout.
     *
     * @param executionId the execution ID
     * @return remaining duration or null if not monitored
     */
    public Duration getRemainingTime(String executionId) {
        TimeoutMonitor monitor = monitors.get(executionId);
        return monitor != null ? monitor.getRemainingTime() : null;
    }

    /** Gets the number of monitored executions. */
    public int getMonitoredCount() {
        return monitors.size();
    }

    @Override
    public DiagnosticInfo getDiagnosticInfo() {
        Map<String, Object> states = new ConcurrentHashMap<>();
        states.put("monitoredExecutions", monitors.size());
        states.put("checkInterval", CHECK_INTERVAL.toSeconds() + "s");
        states.put("schedulerActive", !scheduler.isShutdown());

        // Add timeout status for each monitored execution
        monitors.forEach(
                (id, monitor) -> {
                    states.put("execution." + id + ".task", monitor.getTaskName());
                    states.put(
                            "execution." + id + ".elapsed",
                            monitor.getElapsedTime().toSeconds() + "s");
                    states.put(
                            "execution." + id + ".remaining",
                            monitor.getRemainingTime().toSeconds() + "s");
                    states.put("execution." + id + ".timedOut", monitor.isTimedOut());
                });

        return DiagnosticInfo.builder().component("ExecutionTimeoutManager").states(states).build();
    }

    @Override
    public boolean isDiagnosticModeEnabled() {
        return diagnosticMode.get();
    }

    @Override
    public void enableDiagnosticMode(boolean enabled) {
        diagnosticMode.set(enabled);
        log.info("Diagnostic mode {}", enabled ? "enabled" : "disabled");
    }

    @PreDestroy
    public void shutdown() {
        log.info("Shutting down ExecutionTimeoutManager...");

        // Cancel check task
        if (checkTask != null) {
            checkTask.cancel(false);
        }

        // Clear monitors
        monitors.clear();
        handlers.clear();

        // Shutdown scheduler
        scheduler.shutdown();
        try {
            if (!scheduler.awaitTermination(5, TimeUnit.SECONDS)) {
                scheduler.shutdownNow();
            }
        } catch (InterruptedException e) {
            scheduler.shutdownNow();
            Thread.currentThread().interrupt();
        }

        log.info("ExecutionTimeoutManager shutdown complete");
    }

    /** Starts the periodic timeout checking task. */
    private void startTimeoutChecking() {
        checkTask =
                scheduler.scheduleWithFixedDelay(
                        this::checkTimeouts,
                        CHECK_INTERVAL.toMillis(),
                        CHECK_INTERVAL.toMillis(),
                        TimeUnit.MILLISECONDS);
    }

    /** Checks all monitored executions for timeouts. */
    private void checkTimeouts() {
        try {
            monitors.forEach(
                    (id, monitor) -> {
                        if (monitor.isTimedOut() && !monitor.isHandled()) {
                            handleTimeout(id, monitor);
                        }
                    });
        } catch (Exception e) {
            log.error("Error checking timeouts", e);
        }
    }

    /** Handles a timeout for a specific execution. */
    private void handleTimeout(String executionId, TimeoutMonitor monitor) {
        monitor.markHandled();

        log.warn(
                "Execution {} timed out after {}", monitor.getTaskName(), monitor.getElapsedTime());

        TimeoutHandler handler = handlers.get(executionId);
        if (handler != null) {
            try {
                handler.onTimeout(executionId, monitor.getTaskName(), monitor.getElapsedTime());
            } catch (Exception e) {
                log.error("Error handling timeout for execution {}", executionId, e);
            }
        }

        if (diagnosticMode.get()) {
            log.info(
                    "[DIAGNOSTIC] Timeout handled - ID: {}, Task: {}, Elapsed: {}",
                    executionId,
                    monitor.getTaskName(),
                    monitor.getElapsedTime());
        }
    }

    /** Internal class representing a timeout monitor for an execution. */
    private static class TimeoutMonitor {
        private final String executionId;
        private final String taskName;
        private final Instant startTime;
        private final Duration timeout;
        private final String correlationId;
        private final AtomicBoolean handled = new AtomicBoolean(false);

        public TimeoutMonitor(
                String executionId,
                String taskName,
                Instant startTime,
                Duration timeout,
                String correlationId) {
            this.executionId = executionId;
            this.taskName = taskName;
            this.startTime = startTime;
            this.timeout = timeout;
            this.correlationId = correlationId;
        }

        public boolean isTimedOut() {
            return getElapsedTime().compareTo(timeout) > 0;
        }

        public Duration getElapsedTime() {
            return Duration.between(startTime, Instant.now());
        }

        public Duration getRemainingTime() {
            Duration elapsed = getElapsedTime();
            return elapsed.compareTo(timeout) < 0 ? timeout.minus(elapsed) : Duration.ZERO;
        }

        public boolean isHandled() {
            return handled.get();
        }

        public void markHandled() {
            handled.set(true);
        }

        public String getTaskName() {
            return taskName;
        }

        public String getCorrelationId() {
            return correlationId;
        }
    }

    /** Interface for handling timeout events. */
    public interface TimeoutHandler {
        /**
         * Called when an execution times out.
         *
         * @param executionId the execution ID
         * @param taskName the task name
         * @param elapsed time elapsed before timeout
         */
        void onTimeout(String executionId, String taskName, Duration elapsed);
    }
}
