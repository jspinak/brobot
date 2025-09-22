package io.github.jspinak.brobot.control;

import java.util.HashSet;
import java.util.Set;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.atomic.AtomicBoolean;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import io.github.jspinak.brobot.logging.BrobotLogger;
import io.github.jspinak.brobot.logging.LogCategory;
import io.github.jspinak.brobot.logging.LogLevel;

/**
 * Manages execution pausing and resuming for debugging purposes. Allows setting breakpoints and
 * pausing execution at specific points.
 */
@Component
public class ExecutionPauseController {

    @Autowired(required = false)
    private BrobotLogger logger;

    @Autowired(required = false)
    private ExecutionController executionController;

    private final AtomicBoolean pauseEnabled = new AtomicBoolean(false);
    private final AtomicBoolean isPaused = new AtomicBoolean(false);
    private CountDownLatch pauseLatch;
    private final Set<String> pausePoints = new HashSet<>();
    private final AtomicBoolean globalPause = new AtomicBoolean(false);

    /** Check if execution should be paused at this point */
    public boolean shouldPauseExecution() {
        // Check if execution controller wants to stop
        if (executionController != null && executionController.isStopped()) {
            return false; // Don't pause if stopped
        }

        return pauseEnabled.get() && (globalPause.get() || !pausePoints.isEmpty());
    }

    /** Check if execution should pause at a specific point */
    public boolean shouldPauseAt(String pausePointId) {
        if (!pauseEnabled.get()) {
            return false;
        }

        return globalPause.get() || pausePoints.contains(pausePointId);
    }

    /** Wait for resume signal */
    public void waitForResume() {
        if (!shouldPauseExecution()) {
            return;
        }

        isPaused.set(true);
        pauseLatch = new CountDownLatch(1);

        if (logger != null) {
            logger.log(LogCategory.EXECUTION, LogLevel.INFO, "=== EXECUTION PAUSED ===");
            logger.log(LogCategory.EXECUTION, LogLevel.INFO, "Waiting for resume signal...");
            logger.log(LogCategory.EXECUTION, LogLevel.INFO, "Call resume() to continue execution");
        }

        try {
            pauseLatch.await();
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            if (logger != null) {
                logger.log(
                        LogCategory.EXECUTION,
                        LogLevel.WARN,
                        "Pause interrupted: " + e.getMessage());
            }
        } finally {
            isPaused.set(false);
            if (logger != null) {
                logger.log(LogCategory.EXECUTION, LogLevel.INFO, "=== EXECUTION RESUMED ===");
            }
        }
    }

    /** Wait for resume at a specific pause point */
    public void waitForResumeAt(String pausePointId) {
        if (!shouldPauseAt(pausePointId)) {
            return;
        }

        if (logger != null) {
            logger.log(
                    LogCategory.EXECUTION, LogLevel.INFO, "Paused at breakpoint: " + pausePointId);
        }
        waitForResume();

        // Remove one-time pause points after use
        if (!globalPause.get()) {
            pausePoints.remove(pausePointId);
        }
    }

    /** Set a pause point (breakpoint) */
    public void setPausePoint(String pausePointId) {
        pausePoints.add(pausePointId);
        if (logger != null) {
            logger.log(LogCategory.EXECUTION, LogLevel.INFO, "Pause point set: " + pausePointId);
        }
    }

    /** Remove a pause point */
    public void removePausePoint(String pausePointId) {
        pausePoints.remove(pausePointId);
        if (logger != null) {
            logger.log(
                    LogCategory.EXECUTION, LogLevel.INFO, "Pause point removed: " + pausePointId);
        }
    }

    /** Clear all pause points */
    public void clearPausePoints() {
        pausePoints.clear();
        if (logger != null) {
            logger.log(LogCategory.EXECUTION, LogLevel.INFO, "All pause points cleared");
        }
    }

    /** Resume execution if paused */
    public void resume() {
        if (isPaused.get() && pauseLatch != null) {
            pauseLatch.countDown();
        }
    }

    /** Enable or disable pause functionality */
    public void setPauseEnabled(boolean enabled) {
        pauseEnabled.set(enabled);
        if (logger != null) {
            logger.log(
                    LogCategory.EXECUTION,
                    LogLevel.INFO,
                    "Pause functionality " + (enabled ? "enabled" : "disabled"));
        }

        if (!enabled && isPaused.get()) {
            resume(); // Auto-resume if disabling while paused
        }
    }

    /** Set global pause (pause at every opportunity) */
    public void setGlobalPause(boolean enabled) {
        globalPause.set(enabled);
        if (logger != null) {
            logger.log(
                    LogCategory.EXECUTION,
                    LogLevel.INFO,
                    "Global pause " + (enabled ? "enabled" : "disabled"));
        }
    }

    /** Check if currently paused */
    public boolean isPaused() {
        return isPaused.get();
    }

    /** Check if pause functionality is enabled */
    public boolean isPauseEnabled() {
        return pauseEnabled.get();
    }

    /** Get all current pause points */
    public Set<String> getPausePoints() {
        return new HashSet<>(pausePoints);
    }

    /** Integration hook for ExecutionController */
    public void integrateWithExecutionController() {
        if (executionController != null) {
            // Add methods to ExecutionController if needed
            if (logger != null) {
                logger.log(
                        LogCategory.EXECUTION,
                        LogLevel.INFO,
                        "ExecutionPauseController integrated with ExecutionController");
            }
        }
    }
}
