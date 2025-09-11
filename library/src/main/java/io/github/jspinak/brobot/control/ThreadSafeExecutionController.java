package io.github.jspinak.brobot.control;

import org.springframework.stereotype.Component;

import lombok.extern.slf4j.Slf4j;

/**
 * Thread-safe implementation of ExecutionController.
 *
 * <p>This class provides a robust, thread-safe mechanism for controlling execution flow in
 * multi-threaded automation scenarios. It uses proper synchronization to ensure state transitions
 * are atomic and pause/resume operations work correctly across threads.
 *
 * <p>Key implementation details:
 *
 * <ul>
 *   <li>Uses volatile state for visibility across threads
 *   <li>Synchronizes state transitions to ensure atomicity
 *   <li>Implements wait/notify pattern for pause/resume
 *   <li>Provides clear logging for debugging execution flow
 * </ul>
 *
 * <p>Usage pattern:
 *
 * <pre>{@code
 * // In execution loop
 * while (processing) {
 *     controller.checkPausePoint(); // Will block if paused, throw if stopped
 *     // Do work here
 * }
 * }</pre>
 */
@Slf4j
@Component
public class ThreadSafeExecutionController implements ExecutionController {

    private volatile ExecutionState state = ExecutionState.IDLE;
    private final Object stateLock = new Object();

    @Override
    public void start() {
        synchronized (stateLock) {
            if (!state.canStart()) {
                throw new IllegalStateException(
                        String.format(
                                "Cannot start from state: %s. Execution must be IDLE or STOPPED.",
                                state));
            }
            log.debug("Starting execution from state: {}", state);
            state = ExecutionState.RUNNING;
            stateLock.notifyAll(); // Wake any waiting threads
        }
    }

    @Override
    public void pause() {
        synchronized (stateLock) {
            if (!state.canPause()) {
                log.warn("Cannot pause from state: {}. Ignoring pause request.", state);
                return;
            }
            log.info("Pausing execution");
            state = ExecutionState.PAUSED;
        }
    }

    @Override
    public void resume() {
        synchronized (stateLock) {
            if (!state.canResume()) {
                throw new IllegalStateException(
                        String.format(
                                "Cannot resume from state: %s. Execution must be PAUSED.", state));
            }
            log.info("Resuming execution");
            state = ExecutionState.RUNNING;
            stateLock.notifyAll(); // Wake all threads waiting at pause points
        }
    }

    @Override
    public void stop() {
        synchronized (stateLock) {
            if (state.isTerminated()) {
                log.debug("Already stopped or stopping. Current state: {}", state);
                return;
            }
            log.info("Stopping execution from state: {}", state);
            state = ExecutionState.STOPPING;
            stateLock.notifyAll(); // Wake any paused threads so they can exit
        }
    }

    @Override
    public boolean isPaused() {
        return state == ExecutionState.PAUSED;
    }

    @Override
    public boolean isStopped() {
        return state.isTerminated();
    }

    @Override
    public boolean isRunning() {
        return state == ExecutionState.RUNNING;
    }

    @Override
    public ExecutionState getState() {
        return state;
    }

    @Override
    public void checkPausePoint() throws ExecutionStoppedException, InterruptedException {
        synchronized (stateLock) {
            // Check for stop first
            if (state == ExecutionState.STOPPING || state == ExecutionState.STOPPED) {
                state = ExecutionState.STOPPED; // Ensure we're in final stopped state
                throw new ExecutionStoppedException("Execution stopped at pause point");
            }

            // Wait while paused
            while (state == ExecutionState.PAUSED) {
                log.debug("Thread {} waiting at pause point", Thread.currentThread().getName());
                try {
                    stateLock.wait(); // Release lock and wait for notify
                } catch (InterruptedException e) {
                    // Restore interrupt flag and exit
                    Thread.currentThread().interrupt();
                    throw new InterruptedException("Thread interrupted while paused");
                }

                // After waking, check if we should stop
                if (state == ExecutionState.STOPPING || state == ExecutionState.STOPPED) {
                    state = ExecutionState.STOPPED;
                    throw new ExecutionStoppedException("Execution stopped after pause");
                }
            }
        }
    }

    @Override
    public void reset() {
        synchronized (stateLock) {
            log.debug("Resetting execution controller from state: {}", state);
            state = ExecutionState.IDLE;
            stateLock.notifyAll(); // Wake any waiting threads
        }
    }

    /**
     * Gets a string representation of the current state. Useful for debugging and logging.
     *
     * @return String representation of the controller state
     */
    @Override
    public String toString() {
        return String.format("ExecutionController[state=%s]", state);
    }
}
