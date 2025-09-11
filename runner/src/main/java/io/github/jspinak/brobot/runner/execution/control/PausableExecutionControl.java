package io.github.jspinak.brobot.runner.execution.control;

import java.util.concurrent.atomic.AtomicBoolean;

import lombok.extern.slf4j.Slf4j;

/**
 * Implementation of ExecutionControl providing pause/resume/stop functionality.
 *
 * <p>Thread Safety: This class is thread-safe.
 *
 * @since 1.0.0
 */
@Slf4j
public class PausableExecutionControl implements ExecutionControl {

    private final Object pauseLock = new Object();
    private final AtomicBoolean paused = new AtomicBoolean(false);
    private final AtomicBoolean stopRequested = new AtomicBoolean(false);
    private final String executionId;

    public PausableExecutionControl(String executionId) {
        this.executionId = executionId;
    }

    @Override
    public void pause() {
        synchronized (pauseLock) {
            if (!paused.get() && !stopRequested.get()) {
                paused.set(true);
                log.debug("Execution {} paused", executionId);
            }
        }
    }

    @Override
    public void resume() {
        synchronized (pauseLock) {
            if (paused.get()) {
                paused.set(false);
                pauseLock.notifyAll();
                log.debug("Execution {} resumed", executionId);
            }
        }
    }

    @Override
    public void stop() {
        stopRequested.set(true);

        // Resume if paused to allow stop to proceed
        synchronized (pauseLock) {
            if (paused.get()) {
                paused.set(false);
                pauseLock.notifyAll();
            }
        }

        log.debug("Stop requested for execution {}", executionId);
    }

    @Override
    public boolean isPaused() {
        return paused.get();
    }

    @Override
    public boolean isStopRequested() {
        return stopRequested.get();
    }

    @Override
    public void checkPaused() throws InterruptedException {
        if (stopRequested.get()) {
            throw new InterruptedException("Stop requested");
        }

        synchronized (pauseLock) {
            while (paused.get() && !stopRequested.get()) {
                pauseLock.wait();
            }

            // Check stop again after waking
            if (stopRequested.get()) {
                throw new InterruptedException("Stop requested while paused");
            }
        }
    }
}
