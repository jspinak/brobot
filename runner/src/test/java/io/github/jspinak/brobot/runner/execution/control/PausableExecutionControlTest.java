package io.github.jspinak.brobot.runner.execution.control;

import static org.junit.jupiter.api.Assertions.*;

import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

/**
 * Unit tests for PausableExecutionControl.
 *
 * <p>Tests pause/resume/stop functionality and thread safety.
 */
@DisplayName("PausableExecutionControl Tests")
class PausableExecutionControlTest {

    private PausableExecutionControl control;
    private static final String EXECUTION_ID = "test-exec-123";

    @BeforeEach
    void setUp() {
        control = new PausableExecutionControl(EXECUTION_ID);
    }

    @Test
    @DisplayName("Should start in unpaused state")
    void shouldStartInUnpausedState() {
        assertFalse(control.isPaused());
        assertFalse(control.isStopRequested());
    }

    @Test
    @DisplayName("Should pause execution")
    void shouldPauseExecution() {
        // When
        control.pause();

        // Then
        assertTrue(control.isPaused());
        assertFalse(control.isStopRequested());
    }

    @Test
    @DisplayName("Should resume paused execution")
    void shouldResumePausedExecution() {
        // Given
        control.pause();
        assertTrue(control.isPaused());

        // When
        control.resume();

        // Then
        assertFalse(control.isPaused());
    }

    @Test
    @DisplayName("Should not pause when stop requested")
    void shouldNotPauseWhenStopRequested() {
        // Given
        control.stop();

        // When
        control.pause();

        // Then
        assertFalse(control.isPaused());
        assertTrue(control.isStopRequested());
    }

    @Test
    @DisplayName("Should stop execution")
    void shouldStopExecution() {
        // When
        control.stop();

        // Then
        assertTrue(control.isStopRequested());
    }

    @Test
    @DisplayName("Should resume when stopping paused execution")
    void shouldResumeWhenStoppingPausedExecution() {
        // Given
        control.pause();
        assertTrue(control.isPaused());

        // When
        control.stop();

        // Then
        assertFalse(control.isPaused());
        assertTrue(control.isStopRequested());
    }

    @Test
    @DisplayName("Should block on checkPaused when paused")
    void shouldBlockOnCheckPausedWhenPaused() throws InterruptedException {
        // Given
        control.pause();
        AtomicBoolean wasBlocked = new AtomicBoolean(false);
        CountDownLatch started = new CountDownLatch(1);
        CountDownLatch finished = new CountDownLatch(1);

        // When
        Thread checkThread =
                new Thread(
                        () -> {
                            try {
                                started.countDown();
                                control.checkPaused();
                                finished.countDown();
                            } catch (InterruptedException e) {
                                wasBlocked.set(true);
                            }
                        });

        checkThread.start();
        started.await();

        // Then - thread should be blocked
        assertFalse(finished.await(100, TimeUnit.MILLISECONDS));

        // When - resume
        control.resume();

        // Then - thread should unblock
        assertTrue(finished.await(1, TimeUnit.SECONDS));
        checkThread.join();
        assertFalse(wasBlocked.get());
    }

    @Test
    @DisplayName("Should throw InterruptedException when stop requested")
    void shouldThrowInterruptedExceptionWhenStopRequested() {
        // Given
        control.stop();

        // When/Then
        assertThrows(InterruptedException.class, () -> control.checkPaused());
    }

    @Test
    @DisplayName("Should throw InterruptedException when stop requested while paused")
    void shouldThrowInterruptedExceptionWhenStopRequestedWhilePaused() throws InterruptedException {
        // Given
        control.pause();
        AtomicBoolean gotException = new AtomicBoolean(false);
        CountDownLatch started = new CountDownLatch(1);
        CountDownLatch finished = new CountDownLatch(1);

        // When
        Thread checkThread =
                new Thread(
                        () -> {
                            try {
                                started.countDown();
                                control.checkPaused();
                            } catch (InterruptedException e) {
                                gotException.set(true);
                            } finally {
                                finished.countDown();
                            }
                        });

        checkThread.start();
        started.await();

        // Give thread time to block on pause
        Thread.sleep(50);

        // When - request stop
        control.stop();

        // Then
        assertTrue(finished.await(1, TimeUnit.SECONDS));
        assertTrue(gotException.get());
        checkThread.join();
    }

    @Test
    @DisplayName("Should handle multiple pause/resume cycles")
    void shouldHandleMultiplePauseResumeCycles() {
        for (int i = 0; i < 5; i++) {
            // Pause
            control.pause();
            assertTrue(control.isPaused());

            // Resume
            control.resume();
            assertFalse(control.isPaused());
        }
    }

    @Test
    @DisplayName("Should be idempotent for pause")
    void shouldBeIdempotentForPause() {
        // When - pause multiple times
        control.pause();
        control.pause();
        control.pause();

        // Then
        assertTrue(control.isPaused());

        // When - single resume
        control.resume();

        // Then - should be unpaused
        assertFalse(control.isPaused());
    }

    @Test
    @DisplayName("Should be idempotent for resume")
    void shouldBeIdempotentForResume() {
        // When - resume without pause
        control.resume();
        control.resume();

        // Then
        assertFalse(control.isPaused());
    }
}
