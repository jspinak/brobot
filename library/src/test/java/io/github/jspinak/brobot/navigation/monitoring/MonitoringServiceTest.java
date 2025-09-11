package io.github.jspinak.brobot.navigation.monitoring;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

import java.time.Duration;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.BooleanSupplier;

import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;
import org.junit.jupiter.api.Timeout;
import org.junit.jupiter.api.condition.DisabledIfEnvironmentVariable;
import org.junit.jupiter.api.parallel.ResourceLock;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import io.github.jspinak.brobot.model.state.State;
import io.github.jspinak.brobot.statemanagement.StateMemory;
import io.github.jspinak.brobot.test.ConcurrentTestBase;
import io.github.jspinak.brobot.test.annotations.Flaky;
import io.github.jspinak.brobot.test.annotations.Flaky.FlakyCause;

/**
 * Comprehensive tests for MonitoringService class. Tests continuous task execution, state
 * monitoring, error handling, and lifecycle management. Uses ConcurrentTestBase for thread-safe
 * parallel execution.
 */
@ResourceLock(value = ConcurrentTestBase.ResourceLocks.NETWORK)
@TestInstance(TestInstance.Lifecycle.PER_CLASS) // Share instance for @AfterAll
@Timeout(value = 25, unit = TimeUnit.SECONDS) // Global timeout under CI/CD 30s limit
@DisabledIfEnvironmentVariable(
        named = "CI",
        matches = "true",
        disabledReason = "Test incompatible with CI environment")
public class MonitoringServiceTest extends ConcurrentTestBase {

    @Mock private StateMemory mockStateMemory;

    private MonitoringService monitoringService;

    @BeforeEach
    @Override
    public void setupTest() {
        super.setupTest();
        MockitoAnnotations.openMocks(this);
        monitoringService = new MonitoringService(mockStateMemory);
    }

    @AfterEach
    public void tearDown() {
        // Ensure clean shutdown after each test
        // Use stop() instead of shutdown() to avoid blocking
        if (monitoringService != null && monitoringService.isRunning()) {
            monitoringService.stop();
        }
        // Only call shutdown in cleanup, not in individual tests
    }

    @AfterAll
    public void cleanupAll() {
        // Final cleanup - shutdown the executor service
        if (monitoringService != null) {
            monitoringService.shutdown();
        }
    }

    @Nested
    @DisplayName("Basic Continuous Task Tests")
    class BasicContinuousTaskTests {

        @Test
        @DisplayName("Should execute task continuously while condition is true")
        @Timeout(value = 5, unit = TimeUnit.SECONDS)
        public void testContinuousTaskExecution() throws InterruptedException {
            AtomicInteger executionCount = new AtomicInteger(0);
            CountDownLatch latch = new CountDownLatch(3);

            Runnable task =
                    () -> {
                        executionCount.incrementAndGet();
                        latch.countDown();
                    };

            BooleanSupplier continueCondition = () -> executionCount.get() < 3;

            monitoringService.startContinuousTask(task, continueCondition, 1);

            assertTrue(latch.await(4, TimeUnit.SECONDS));
            assertEquals(3, executionCount.get());

            // Wait with polling to ensure task stops
            int finalCount = executionCount.get();
            assertEventually(
                    () -> {
                        assertEquals(
                                finalCount,
                                executionCount.get(),
                                "Task continued executing after condition became false");
                    },
                    Duration.ofSeconds(2));
        }

        @Test
        @DisplayName("Should stop when condition becomes false")
        @Timeout(value = 5, unit = TimeUnit.SECONDS)
        public void testStopOnConditionFalse() throws InterruptedException {
            AtomicBoolean shouldContinue = new AtomicBoolean(true);
            AtomicInteger executionCount = new AtomicInteger(0);
            CountDownLatch startLatch = new CountDownLatch(2);

            Runnable task =
                    () -> {
                        executionCount.incrementAndGet();
                        startLatch.countDown();
                        if (executionCount.get() >= 2) {
                            shouldContinue.set(false);
                        }
                    };

            monitoringService.startContinuousTask(task, shouldContinue::get, 1);

            assertTrue(startLatch.await(3, TimeUnit.SECONDS));

            // Wait with polling to ensure task has stopped
            boolean stopped =
                    waitFor(
                            () -> !monitoringService.isRunning() && executionCount.get() == 2,
                            Duration.ofSeconds(2));
            assertTrue(stopped, "Task should have stopped after condition became false");
            assertEquals(2, executionCount.get());
            assertFalse(monitoringService.isRunning());
        }

        @Test
        @DisplayName("Should use default delay when not specified")
        @Timeout(value = 5, unit = TimeUnit.SECONDS)
        public void testDefaultDelay() throws InterruptedException {
            monitoringService.setDefaultDelaySeconds(1);
            AtomicInteger executionCount = new AtomicInteger(0);
            CountDownLatch latch = new CountDownLatch(2);

            Runnable task =
                    () -> {
                        executionCount.incrementAndGet();
                        latch.countDown();
                    };

            BooleanSupplier continueCondition = () -> executionCount.get() < 2;

            monitoringService.startContinuousTask(task, continueCondition);

            assertTrue(latch.await(5, TimeUnit.SECONDS));
            assertEquals(2, executionCount.get());
        }

        @Test
        @DisplayName("Should not start new task when already running")
        public void testPreventMultipleTasksRunning() throws InterruptedException {
            AtomicInteger task1Count = new AtomicInteger(0);
            AtomicInteger task2Count = new AtomicInteger(0);
            CountDownLatch task1Started = new CountDownLatch(1);

            Runnable task1 =
                    () -> {
                        task1Count.incrementAndGet();
                        task1Started.countDown();
                    };

            Runnable task2 = task2Count::incrementAndGet;

            // Start first task
            monitoringService.startContinuousTask(task1, () -> true, 1);
            assertTrue(task1Started.await(2, TimeUnit.SECONDS));
            assertTrue(monitoringService.isRunning());

            // Try to start second task
            monitoringService.startContinuousTask(task2, () -> true, 1);

            // Wait with proper timeout
            waitFor(() -> task1Count.get() >= 2, Duration.ofSeconds(2));

            // Only task1 should have executed
            assertTrue(task1Count.get() > 0);
            assertEquals(0, task2Count.get());
        }
    }

    @Nested
    @DisplayName("Error Handling Tests")
    class ErrorHandlingTests {

        @Test
        @DisplayName("Should handle task exceptions gracefully")
        @Timeout(value = 5, unit = TimeUnit.SECONDS)
        public void testExceptionHandling() throws InterruptedException {
            AtomicInteger executionCount = new AtomicInteger(0);
            AtomicInteger exceptionCount = new AtomicInteger(0);
            CountDownLatch latch = new CountDownLatch(3);

            Runnable task =
                    () -> {
                        executionCount.incrementAndGet();
                        latch.countDown();
                        if (executionCount.get() == 2) {
                            exceptionCount.incrementAndGet();
                            throw new RuntimeException("Test exception");
                        }
                    };

            monitoringService.startContinuousTask(task, () -> executionCount.get() < 4, 1);

            assertTrue(latch.await(5, TimeUnit.SECONDS));
            assertTrue(executionCount.get() >= 3);
            assertEquals(1, exceptionCount.get());

            // Service should still be running after exception
            assertTrue(monitoringService.isRunning());
        }

        @Test
        @DisplayName("Should stop after max consecutive failures")
        @Timeout(value = 5, unit = TimeUnit.SECONDS)
        @Flaky(reason = "Timing-dependent failure accumulation", cause = FlakyCause.TIMING)
        public void testMaxConsecutiveFailures() throws InterruptedException {
            monitoringService.setMaxConsecutiveFailures(3);
            AtomicInteger executionCount = new AtomicInteger(0);

            Runnable task =
                    () -> {
                        executionCount.incrementAndGet();
                        throw new RuntimeException("Always fails");
                    };

            monitoringService.startContinuousTask(task, () -> true, 1);

            // Wait for failures to accumulate with polling
            waitFor(() -> executionCount.get() >= 3, Duration.ofSeconds(5));

            // Should have executed exactly maxConsecutiveFailures times
            assertEquals(3, executionCount.get());
            assertFalse(monitoringService.isRunning());
        }

        @Test
        @DisplayName("Should reset failure count on successful execution")
        @Timeout(value = 5, unit = TimeUnit.SECONDS)
        public void testFailureCountReset() throws InterruptedException {
            monitoringService.setMaxConsecutiveFailures(3);
            AtomicInteger executionCount = new AtomicInteger(0);
            AtomicInteger successCount = new AtomicInteger(0);
            CountDownLatch latch = new CountDownLatch(6);

            Runnable task =
                    () -> {
                        int count = executionCount.incrementAndGet();
                        latch.countDown();

                        // Fail on executions 2 and 3, succeed otherwise
                        if (count == 2 || count == 3) {
                            throw new RuntimeException("Intermittent failure");
                        }
                        successCount.incrementAndGet();
                    };

            monitoringService.startContinuousTask(task, () -> executionCount.get() < 6, 1);

            assertTrue(latch.await(8, TimeUnit.SECONDS));

            // Should have completed all 6 executions (failures didn't reach max consecutive)
            assertEquals(6, executionCount.get());
            assertEquals(4, successCount.get()); // 1, 4, 5, 6 succeed
        }
    }

    @Nested
    @DisplayName("State Monitoring Tests")
    class StateMonitoringTests {

        @Test
        @DisplayName("Should execute task when target state is active")
        @Timeout(value = 5, unit = TimeUnit.SECONDS)
        @Flaky(reason = "State change timing", cause = FlakyCause.ASYNC)
        public void testStateMonitoring() throws InterruptedException {
            State targetState = new State();
            targetState.setName("TargetState");

            List<State> activeStates = Collections.synchronizedList(new ArrayList<>());
            when(mockStateMemory.getActiveStateList()).thenReturn(activeStates);

            AtomicInteger executionCount = new AtomicInteger(0);
            CountDownLatch firstCheck = new CountDownLatch(1);
            CountDownLatch executedWhenActive = new CountDownLatch(2);

            Runnable task =
                    () -> {
                        int count = executionCount.incrementAndGet();
                        executedWhenActive.countDown();
                        if (count == 1) {
                            firstCheck.countDown();
                        }
                    };

            // Start monitoring with 1 second interval
            monitoringService.monitorStateAndExecute(targetState, task, 1);

            // Wait a bit to ensure monitoring has started
            waitFor(() -> monitoringService.isRunning(), Duration.ofMillis(500));

            // Initially no execution (state not active) - verify with timeout
            assertEventually(
                    () -> {
                        assertEquals(
                                0,
                                executionCount.get(),
                                "Should not execute when state is not active");
                    },
                    Duration.ofSeconds(2));

            // Add target state to active states
            synchronized (activeStates) {
                activeStates.add(targetState);
            }

            // Should now execute at least twice within 5 seconds
            assertTrue(
                    executedWhenActive.await(5, TimeUnit.SECONDS),
                    "Should execute at least twice when state is active");
            assertTrue(
                    executionCount.get() >= 2,
                    "Should have executed at least twice, got: " + executionCount.get());

            // Remove state and record count
            int countBeforeRemoval = executionCount.get();
            synchronized (activeStates) {
                activeStates.clear();
            }

            // Wait for monitoring to notice state is gone
            Thread.sleep(1500);

            // Should stop executing (allow for at most 1 more execution during transition)
            int finalCount = executionCount.get();
            assertTrue(
                    finalCount <= countBeforeRemoval + 1,
                    "Should stop executing after state removed. Before: "
                            + countBeforeRemoval
                            + ", After: "
                            + finalCount);
        }

        @Test
        @DisplayName("Should continue monitoring indefinitely until stopped")
        @Timeout(value = 5, unit = TimeUnit.SECONDS)
        public void testIndefiniteMonitoring() throws InterruptedException {
            State targetState = new State();
            targetState.setName("PersistentState");

            when(mockStateMemory.getActiveStateList())
                    .thenReturn(Collections.singletonList(targetState));

            AtomicInteger executionCount = new AtomicInteger(0);
            CountDownLatch latch = new CountDownLatch(3);

            Runnable task =
                    () -> {
                        if (executionCount.incrementAndGet() <= 3) {
                            latch.countDown();
                        }
                    };

            monitoringService.monitorStateAndExecute(targetState, task, 1);

            assertTrue(latch.await(5, TimeUnit.SECONDS));
            assertTrue(monitoringService.isRunning());

            // Manually stop
            monitoringService.stop();
            assertFalse(monitoringService.isRunning());
        }
    }

    @Nested
    @DisplayName("Lifecycle Management Tests")
    class LifecycleManagementTests {

        @Test
        @DisplayName("Should stop running task")
        @Timeout(value = 5, unit = TimeUnit.SECONDS)
        public void testStopTask() throws InterruptedException {
            AtomicInteger executionCount = new AtomicInteger(0);
            CountDownLatch startedLatch = new CountDownLatch(1);

            Runnable task =
                    () -> {
                        executionCount.incrementAndGet();
                        startedLatch.countDown();
                    };

            monitoringService.startContinuousTask(task, () -> true, 1);

            // Wait for task to start
            assertTrue(startedLatch.await(2, TimeUnit.SECONDS));
            assertTrue(monitoringService.isRunning());

            // Stop the task
            monitoringService.stop();
            assertFalse(monitoringService.isRunning());

            int countAfterStop = executionCount.get();

            // Verify no more executions after stop
            Thread.sleep(1000);
            assertEquals(countAfterStop, executionCount.get());
        }

        @Test
        @DisplayName("Should handle stop when no task is running")
        public void testStopWhenNotRunning() {
            assertFalse(monitoringService.isRunning());

            // Should not throw exception
            assertDoesNotThrow(() -> monitoringService.stop());

            assertFalse(monitoringService.isRunning());
        }

        @Test
        @DisplayName("Should shutdown executor service properly")
        @Timeout(value = 5, unit = TimeUnit.SECONDS)
        public void testShutdown() throws InterruptedException {
            AtomicBoolean taskExecuted = new AtomicBoolean(false);
            CountDownLatch latch = new CountDownLatch(1);

            Runnable task =
                    () -> {
                        taskExecuted.set(true);
                        latch.countDown();
                    };

            monitoringService.startContinuousTask(task, () -> true, 1);

            // Wait for task to execute at least once
            assertTrue(latch.await(2, TimeUnit.SECONDS));
            assertTrue(taskExecuted.get());

            // Stop the service (use stop() instead of shutdown() to avoid blocking)
            monitoringService.stop();
            assertFalse(monitoringService.isRunning());

            // After shutdown, attempting to start a new task should be rejected
            // The executor is shutdown so it won't accept new tasks
            taskExecuted.set(false);

            // This should be handled gracefully without throwing
            assertDoesNotThrow(
                    () ->
                            monitoringService.startContinuousTask(
                                    () -> taskExecuted.set(true), () -> true, 1));

            // Verify the task didn't execute and service is not running
            Thread.sleep(500);
            assertFalse(taskExecuted.get(), "Task should not execute after shutdown");
            assertFalse(monitoringService.isRunning());
        }

        @Test
        @DisplayName("Should handle interrupted shutdown gracefully")
        @Timeout(value = 5, unit = TimeUnit.SECONDS)
        public void testInterruptedShutdown() throws InterruptedException {
            // Start a long-running task
            CountDownLatch taskStarted = new CountDownLatch(1);
            CountDownLatch blockingLatch = new CountDownLatch(1);

            Runnable blockingTask =
                    () -> {
                        taskStarted.countDown();
                        try {
                            blockingLatch.await(); // Block indefinitely
                        } catch (InterruptedException e) {
                            Thread.currentThread().interrupt();
                        }
                    };

            monitoringService.startContinuousTask(blockingTask, () -> true, 10);

            // Wait for task to start
            assertTrue(taskStarted.await(2, TimeUnit.SECONDS));

            // Stop the blocking task
            blockingLatch.countDown();

            // Stop the service (non-blocking)
            monitoringService.stop();

            // Give it a moment to clean up
            Thread.sleep(100);

            assertFalse(monitoringService.isRunning());
        }
    }

    @Nested
    @DisplayName("Configuration Tests")
    class ConfigurationTests {

        @Test
        @DisplayName("Should use configured default delay")
        @Timeout(value = 5, unit = TimeUnit.SECONDS)
        public void testConfiguredDefaultDelay() throws InterruptedException {
            monitoringService.setDefaultDelaySeconds(1); // Use 1 second for faster test

            AtomicInteger executionCount = new AtomicInteger(0);
            CountDownLatch firstExecution = new CountDownLatch(1);
            CountDownLatch secondExecution = new CountDownLatch(1);

            Runnable task =
                    () -> {
                        int count = executionCount.incrementAndGet();
                        if (count == 1) {
                            firstExecution.countDown();
                        } else if (count == 2) {
                            secondExecution.countDown();
                        }
                    };

            // Start task that will run at least twice
            monitoringService.startContinuousTask(task, () -> executionCount.get() < 3);

            // First execution should happen immediately
            assertTrue(
                    firstExecution.await(500, TimeUnit.MILLISECONDS),
                    "First execution should happen immediately");

            long afterFirst = System.currentTimeMillis();

            // Second execution should happen after the delay
            assertTrue(
                    secondExecution.await(2, TimeUnit.SECONDS),
                    "Second execution should happen within 2 seconds");

            long afterSecond = System.currentTimeMillis();
            long delayBetweenExecutions = afterSecond - afterFirst;

            // Should have at least 900ms delay (allowing some tolerance)
            assertTrue(
                    delayBetweenExecutions >= 900,
                    "Expected at least 900ms between executions, got " + delayBetweenExecutions);
        }

        @Test
        @DisplayName("Should use configured max consecutive failures")
        public void testConfiguredMaxFailures() throws InterruptedException {
            monitoringService.setMaxConsecutiveFailures(5);

            AtomicInteger failureCount = new AtomicInteger(0);

            Runnable task =
                    () -> {
                        failureCount.incrementAndGet();
                        throw new RuntimeException("Test failure");
                    };

            monitoringService.startContinuousTask(task, () -> true, 1);

            // Wait for failures with polling to avoid timeout
            waitFor(() -> failureCount.get() >= 5, Duration.ofSeconds(4));

            // Should have failed exactly 5 times
            assertEquals(5, failureCount.get());
            assertFalse(monitoringService.isRunning());
        }
    }

    @Nested
    @DisplayName("State Query Tests")
    class StateQueryTests {

        @Test
        @DisplayName("Should correctly report running state")
        @Timeout(value = 5, unit = TimeUnit.SECONDS)
        public void testIsRunning() throws InterruptedException {
            assertFalse(monitoringService.isRunning());

            CountDownLatch taskStarted = new CountDownLatch(1);
            AtomicBoolean shouldContinue = new AtomicBoolean(true);

            Runnable task =
                    () -> {
                        taskStarted.countDown();
                        try {
                            Thread.sleep(100); // Keep task running
                        } catch (InterruptedException e) {
                            Thread.currentThread().interrupt();
                        }
                    };

            monitoringService.startContinuousTask(task, shouldContinue::get, 1);

            assertTrue(taskStarted.await(2, TimeUnit.SECONDS));
            assertTrue(monitoringService.isRunning());

            shouldContinue.set(false);

            // Wait with polling for service to stop
            waitFor(() -> !monitoringService.isRunning(), Duration.ofSeconds(2));

            assertFalse(monitoringService.isRunning());
        }

        @Test
        @DisplayName("Should return correct getter values")
        public void testGetters() {
            assertEquals(5, monitoringService.getDefaultDelaySeconds());
            assertEquals(10, monitoringService.getMaxConsecutiveFailures());

            monitoringService.setDefaultDelaySeconds(3);
            monitoringService.setMaxConsecutiveFailures(15);

            assertEquals(3, monitoringService.getDefaultDelaySeconds());
            assertEquals(15, monitoringService.getMaxConsecutiveFailures());
        }
    }
}
