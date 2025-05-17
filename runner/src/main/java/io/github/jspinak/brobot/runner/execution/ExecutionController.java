package io.github.jspinak.brobot.runner.execution;

import io.github.jspinak.brobot.datatypes.project.Button;
import lombok.Getter;
import lombok.Setter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import java.time.Duration;
import java.time.Instant;
import java.util.concurrent.*;
import java.util.function.Consumer;
import java.util.function.Supplier;

/**
 * Main controller for managing execution of automation tasks.
 * Provides lifecycle control (start/pause/resume/stop), thread management,
 * status tracking, and timeout mechanisms.
 */
@Component
public class ExecutionController {
    private static final Logger logger = LoggerFactory.getLogger(ExecutionController.class);

    private final ExecutorService executorService;
    private final ScheduledExecutorService timeoutExecutor;
    private final ExecutionStatusManager statusManager;
    private final SafetyManager safetyManager;

    /**
     * -- GETTER --
     *  Get the current execution status
     */
    @Getter
    private final ExecutionStatus status = new ExecutionStatus();
    private Future<?> currentTask;
    private ScheduledFuture<?> timeoutTask;
    private final Object pauseLock = new Object();
    private volatile boolean paused = false;
    private volatile boolean requestStop = false;

    /**
     * Set a callback for logging
     * -- SETTER --
     *  Set a callback for logging
     */
    @Setter
    private Consumer<String> logCallback;

    public ExecutionController() {
        this.executorService = Executors.newSingleThreadExecutor(r -> {
            Thread thread = new Thread(r, "Brobot-Automation-Thread");
            thread.setDaemon(true); // Make thread daemon so it doesn't prevent JVM shutdown
            return thread;
        });
        this.timeoutExecutor = Executors.newSingleThreadScheduledExecutor(r -> {
            Thread thread = new Thread(r, "Brobot-Timeout-Thread");
            thread.setDaemon(true);
            return thread;
        });
        this.statusManager = new ExecutionStatusManager(this.status);
        this.safetyManager = new SafetyManager();
    }

    /**
     * Sets a consumer that will be notified of status changes.
     *
     * @param statusConsumer The consumer to be notified when status changes
     */
    public void setStatusConsumer(Consumer<ExecutionStatus> statusConsumer) {
        statusManager.setStatusConsumer(statusConsumer);
    }

    /**
     * Executes an automation function based on a Button definition
     *
     * @param button Button definition containing execution parameters
     * @param automationTask The actual task to execute
     * @param timeoutMillis Timeout in milliseconds, or 0 for no timeout
     * @param statusConsumer Optional consumer for status updates
     */
    public void executeAutomation(Button button, Runnable automationTask, long timeoutMillis,
                                  Consumer<ExecutionStatus> statusConsumer) {
        Duration timeout = timeoutMillis > 0 ? Duration.ofMillis(timeoutMillis) : null;

        Supplier<Void> task = () -> {
            try {
                log("Executing automation function: " + button.getFunctionName());
                automationTask.run();
                log("Function completed: " + button.getFunctionName());
                return null;
            } catch (Exception e) {
                log("Error executing function: " + e.getMessage());
                logger.error("Error executing automation", e);
                throw e;
            }
        };

        startExecution(task, timeout, statusConsumer);
    }

    /**
     * Starts execution of an automation task with a specified timeout
     *
     * @param task The task to execute
     * @param timeout Maximum allowed execution time
     * @param statusConsumer Optional consumer for receiving status updates
     * @return A future representing the running task
     */
    public <T> Future<T> startExecution(Supplier<T> task, Duration timeout, Consumer<ExecutionStatus> statusConsumer) {
        if (isRunning()) {
            throw new IllegalStateException("An automation task is already running");
        }

        // Reset state
        requestStop = false;
        paused = false;

        // Initialize status
        statusManager.reset();
        if (statusConsumer != null) {
            statusManager.setStatusConsumer(statusConsumer);
        }
        statusManager.updateState(ExecutionState.STARTING);
        statusManager.updateStartTime(Instant.now());

        // Log execution start
        log("Starting automation execution");

        // Wrap the task to handle execution state
        currentTask = executorService.submit(() -> {
            T result = null;
            try {
                statusManager.updateState(ExecutionState.RUNNING);

                // Execute the task with pause/stop capabilities
                result = executeWithControls(task);

                if (requestStop) {
                    statusManager.updateState(ExecutionState.STOPPED);
                    log("Execution was stopped by user request");
                } else if (status.getState() == ExecutionState.TIMEOUT) {
                    log("Execution ended due to timeout");
                } else {
                    statusManager.updateState(ExecutionState.COMPLETED);
                    log("Execution completed successfully");
                }
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                statusManager.updateState(ExecutionState.STOPPED);
                log("Execution was interrupted: " + e.getMessage());
            } catch (Exception e) {
                statusManager.updateState(ExecutionState.ERROR);
                statusManager.setError(e);
                log("Execution failed with error: " + e.getMessage());
                logger.error("Execution error", e);
            } finally {
                statusManager.updateEndTime(Instant.now());
                if (timeoutTask != null) {
                    timeoutTask.cancel(false);
                }
            }

            return result;
        });

        // Set up timeout if specified
        if (timeout != null && !timeout.isZero() && !timeout.isNegative()) {
            timeoutTask = timeoutExecutor.schedule(() -> {
                if (isRunning()) {
                    log("Execution timed out after " + timeout);

                    statusManager.updateState(ExecutionState.TIMEOUT);
                    currentTask.cancel(true);
                }
            }, timeout.toMillis(), TimeUnit.MILLISECONDS);
        }

        return new CastingFuture<>(currentTask);
    }

    /**
     * Executes a task with pause/resume and stop capabilities
     */
    private <T> T executeWithControls(Supplier<T> task) throws InterruptedException {
        // Run the task with periodic checks for pause/stop
        while (!Thread.currentThread().isInterrupted()) {
            // Check if stop requested
            if (requestStop) {
                throw new InterruptedException("Stop requested");
            }

            // Check if paused
            checkPaused();

            // Safety check before proceeding
            safetyManager.performSafetyCheck();

            // Run the actual task
            return task.get();
        }

        throw new InterruptedException("Thread interrupted");
    }

    /**
     * Pauses the current execution if running
     */
    public void pauseExecution() {
        if (!isRunning()) {
            return;
        }

        synchronized (pauseLock) {
            paused = true;
            statusManager.updateState(ExecutionState.PAUSED);
            log("Execution paused");
        }
    }

    /**
     * Resumes the execution if paused
     */
    public void resumeExecution() {
        if (!isPaused()) {
            return;
        }

        synchronized (pauseLock) {
            paused = false;
            statusManager.updateState(ExecutionState.RUNNING);
            pauseLock.notifyAll(); // Wake up waiting thread

            log("Execution resumed");
        }
    }

    /**
     * Stops the current execution if running
     */
    public void stopExecution() {
        if (!isRunning() && !isPaused()) {
            return;
        }

        // First resume if paused to allow the task to continue and check for stop flag
        if (isPaused()) {
            synchronized (pauseLock) {
                paused = false;
                pauseLock.notifyAll();
            }
        }

        // Set stop flag
        requestStop = true;

        // If task doesn't respond to stop flag in 2 seconds, force interrupt
        CompletableFuture.delayedExecutor(2, TimeUnit.SECONDS).execute(() -> {
            if (currentTask != null && !currentTask.isDone()) {
                currentTask.cancel(true);
                log("Execution force stopped after timeout");
            }
        });

        statusManager.updateState(ExecutionState.STOPPING);
        log("Stop requested for current execution");
    }

    /**
     * Checks if execution should remain paused and waits if so
     */
    private void checkPaused() throws InterruptedException {
        if (paused) {
            synchronized (pauseLock) {
                while (paused) {
                    pauseLock.wait();

                    // Check for stop request immediately after waking up
                    if (requestStop) {
                        throw new InterruptedException("Stop requested while paused");
                    }
                }
            }
        }
    }

    /**
     * Checks if an automation task is currently running
     */
    public boolean isRunning() {
        ExecutionState state = status.getState();
        return state == ExecutionState.STARTING || state == ExecutionState.RUNNING ||
                state == ExecutionState.PAUSED || state == ExecutionState.STOPPING;
    }

    /**
     * Checks if execution is paused
     */
    public boolean isPaused() {
        return paused && status.getState() == ExecutionState.PAUSED;
    }

    /**
     * Shut down the controller and release resources
     */
    public void shutdown() {
        try {
            // Stop any running execution first
            if (isRunning()) {
                stopExecution();
            }

            // Shut down executors
            executorService.shutdown();
            timeoutExecutor.shutdown();

            // Wait a bit for tasks to complete
            if (!executorService.awaitTermination(5, TimeUnit.SECONDS)) {
                executorService.shutdownNow();
            }
            if (!timeoutExecutor.awaitTermination(2, TimeUnit.SECONDS)) {
                timeoutExecutor.shutdownNow();
            }
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            executorService.shutdownNow();
            timeoutExecutor.shutdownNow();
        }
    }

    /**
     * Log a message to the configured log callback and the logger
     */
    private void log(String message) {
        logger.info(message);
        if (logCallback != null) {
            logCallback.accept(message);
        }
    }

    /**
     * Helper class to cast Future results correctly
     */
    private static class CastingFuture<T> implements Future<T> {
        private final Future<?> future;

        private CastingFuture(Future<?> future) {
            this.future = future;
        }

        @Override
        public boolean cancel(boolean mayInterruptIfRunning) {
            return future.cancel(mayInterruptIfRunning);
        }

        @Override
        public boolean isCancelled() {
            return future.isCancelled();
        }

        @Override
        public boolean isDone() {
            return future.isDone();
        }

        @SuppressWarnings("unchecked")
        @Override
        public T get() throws InterruptedException, ExecutionException {
            return (T) future.get();
        }

        @SuppressWarnings("unchecked")
        @Override
        public T get(long timeout, TimeUnit unit) throws InterruptedException, ExecutionException, TimeoutException {
            return (T) future.get(timeout, unit);
        }
    }
}