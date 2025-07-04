package io.github.jspinak.brobot.control;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.Timeout;

import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;

import static org.junit.jupiter.api.Assertions.*;

class ThreadSafeExecutionControllerTest {

    private ThreadSafeExecutionController controller;

    @BeforeEach
    void setUp() {
        controller = new ThreadSafeExecutionController();
    }

    @Test
    void testInitialState() {
        assertEquals(ExecutionState.IDLE, controller.getState());
        assertFalse(controller.isRunning());
        assertFalse(controller.isPaused());
        assertFalse(controller.isStopped());
    }

    @Test
    void testStartTransition() {
        controller.start();
        assertEquals(ExecutionState.RUNNING, controller.getState());
        assertTrue(controller.isRunning());
        assertFalse(controller.isPaused());
        assertFalse(controller.isStopped());
    }

    @Test
    void testStartFromInvalidState() {
        controller.start();
        controller.pause();
        
        assertThrows(IllegalStateException.class, () -> controller.start(),
                "Should not be able to start from PAUSED state");
    }

    @Test
    void testPauseTransition() {
        controller.start();
        controller.pause();
        
        assertEquals(ExecutionState.PAUSED, controller.getState());
        assertFalse(controller.isRunning());
        assertTrue(controller.isPaused());
        assertFalse(controller.isStopped());
    }

    @Test
    void testPauseFromInvalidState() {
        // Pausing from IDLE should be ignored (not throw)
        controller.pause();
        assertEquals(ExecutionState.IDLE, controller.getState());
    }

    @Test
    void testResumeTransition() {
        controller.start();
        controller.pause();
        controller.resume();
        
        assertEquals(ExecutionState.RUNNING, controller.getState());
        assertTrue(controller.isRunning());
        assertFalse(controller.isPaused());
        assertFalse(controller.isStopped());
    }

    @Test
    void testResumeFromInvalidState() {
        controller.start();
        
        assertThrows(IllegalStateException.class, () -> controller.resume(),
                "Should not be able to resume from RUNNING state");
    }

    @Test
    void testStopTransition() {
        controller.start();
        controller.stop();
        
        assertEquals(ExecutionState.STOPPING, controller.getState());
        assertFalse(controller.isRunning());
        assertFalse(controller.isPaused());
        assertTrue(controller.isStopped());
    }

    @Test
    void testStopFromPaused() {
        controller.start();
        controller.pause();
        controller.stop();
        
        assertEquals(ExecutionState.STOPPING, controller.getState());
        assertTrue(controller.isStopped());
    }

    @Test
    void testStopIdempotent() {
        controller.start();
        controller.stop();
        controller.stop(); // Should not throw
        
        assertEquals(ExecutionState.STOPPING, controller.getState());
    }

    @Test
    void testReset() {
        controller.start();
        controller.pause();
        controller.reset();
        
        assertEquals(ExecutionState.IDLE, controller.getState());
        assertFalse(controller.isRunning());
        assertFalse(controller.isPaused());
        assertFalse(controller.isStopped());
    }

    @Test
    @Timeout(5)
    void testCheckPausePointWhenRunning() throws Exception {
        controller.start();
        
        // Should not block when running
        assertDoesNotThrow(() -> controller.checkPausePoint());
    }

    @Test
    @Timeout(5)
    void testCheckPausePointWhenStopped() {
        controller.start();
        controller.stop();
        
        assertThrows(ExecutionStoppedException.class, () -> controller.checkPausePoint());
    }

    @Test
    @Timeout(5)
    void testCheckPausePointBlocksWhenPaused() throws Exception {
        controller.start();
        controller.pause(); // Pause before thread starts
        
        AtomicBoolean wasBlocked = new AtomicBoolean(false);
        CountDownLatch threadAtPausePoint = new CountDownLatch(1);
        CountDownLatch resumeLatch = new CountDownLatch(1);
        
        // Thread that will be paused
        Thread workerThread = new Thread(() -> {
            try {
                threadAtPausePoint.countDown();
                controller.checkPausePoint(); // Should block here since already paused
                wasBlocked.set(true);
                resumeLatch.countDown();
            } catch (Exception e) {
                fail("Unexpected exception: " + e);
            }
        });
        
        workerThread.start();
        threadAtPausePoint.await(); // Wait for thread to reach pause point
        Thread.sleep(100); // Give thread time to be blocked
        
        // Verify thread is blocked
        assertFalse(wasBlocked.get(), "Thread should be blocked at pause point");
        
        // Resume and verify thread continues
        controller.resume();
        assertTrue(resumeLatch.await(2, TimeUnit.SECONDS), "Thread should continue after resume");
        assertTrue(wasBlocked.get(), "Thread should have continued past pause point");
        
        workerThread.join();
    }

    @Test
    @Timeout(5)
    void testCheckPausePointStopsDuringPause() throws Exception {
        controller.start();
        controller.pause();
        
        AtomicBoolean stoppedCorrectly = new AtomicBoolean(false);
        CountDownLatch threadStarted = new CountDownLatch(1);
        
        Thread workerThread = new Thread(() -> {
            try {
                threadStarted.countDown();
                controller.checkPausePoint();
                fail("Should have thrown ExecutionStoppedException");
            } catch (ExecutionStoppedException e) {
                stoppedCorrectly.set(true);
            } catch (Exception e) {
                fail("Wrong exception type: " + e);
            }
        });
        
        workerThread.start();
        threadStarted.await();
        Thread.sleep(100); // Ensure thread is waiting at pause point
        
        // Stop while paused
        controller.stop();
        workerThread.join(2000);
        
        assertTrue(stoppedCorrectly.get(), "Thread should have received stop signal");
    }

    @Test
    @Timeout(5)
    void testConcurrentPauseResumeOperations() throws Exception {
        controller.start();
        
        int numThreads = 10;
        CountDownLatch startLatch = new CountDownLatch(numThreads);
        CountDownLatch endLatch = new CountDownLatch(numThreads);
        AtomicInteger successCount = new AtomicInteger(0);
        
        // Create multiple threads that check pause points
        ExecutorService executor = Executors.newFixedThreadPool(numThreads);
        
        for (int i = 0; i < numThreads; i++) {
            executor.submit(() -> {
                try {
                    startLatch.countDown();
                    startLatch.await(); // All threads start together
                    
                    for (int j = 0; j < 10; j++) {
                        controller.checkPausePoint();
                        Thread.sleep(10);
                    }
                    
                    successCount.incrementAndGet();
                } catch (ExecutionStoppedException e) {
                    // Expected when stopped
                } catch (Exception e) {
                    e.printStackTrace();
                } finally {
                    endLatch.countDown();
                }
            });
        }
        
        // Start all threads
        startLatch.await();
        
        // Perform pause/resume cycles
        for (int i = 0; i < 3; i++) {
            Thread.sleep(50);
            controller.pause();
            Thread.sleep(50);
            controller.resume();
        }
        
        // Let threads finish
        Thread.sleep(100);
        controller.stop();
        
        assertTrue(endLatch.await(5, TimeUnit.SECONDS), "All threads should complete");
        executor.shutdown();
    }

    @Test
    void testInterruptHandling() throws Exception {
        controller.start();
        controller.pause();
        
        AtomicBoolean wasInterrupted = new AtomicBoolean(false);
        
        Thread workerThread = new Thread(() -> {
            try {
                controller.checkPausePoint();
            } catch (InterruptedException e) {
                wasInterrupted.set(true);
                // Verify interrupt flag is preserved
                assertTrue(Thread.currentThread().isInterrupted());
            } catch (Exception e) {
                fail("Unexpected exception: " + e);
            }
        });
        
        workerThread.start();
        Thread.sleep(100); // Let thread reach pause point
        workerThread.interrupt();
        workerThread.join(1000);
        
        assertTrue(wasInterrupted.get(), "Thread should have been interrupted");
    }

    @Test
    void testStateTransitionsThreadSafety() throws Exception {
        int iterations = 100;
        AtomicInteger errors = new AtomicInteger(0);
        
        Runnable stateChanger = () -> {
            for (int i = 0; i < iterations; i++) {
                try {
                    // Each thread gets its own controller instance to avoid conflicts
                    ThreadSafeExecutionController localController = new ThreadSafeExecutionController();
                    localController.start();
                    localController.pause();
                    localController.resume();
                    localController.stop();
                    localController.reset();
                } catch (Exception e) {
                    errors.incrementAndGet();
                    e.printStackTrace();
                }
            }
        };
        
        Thread t1 = new Thread(stateChanger);
        Thread t2 = new Thread(stateChanger);
        
        t1.start();
        t2.start();
        
        t1.join();
        t2.join();
        
        assertEquals(0, errors.get(), "No errors should occur during concurrent state changes");
    }

    @Test
    void testToString() {
        assertEquals("ExecutionController[state=IDLE]", controller.toString());
        
        controller.start();
        assertEquals("ExecutionController[state=RUNNING]", controller.toString());
    }
}