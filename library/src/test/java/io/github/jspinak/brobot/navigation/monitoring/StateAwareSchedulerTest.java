package io.github.jspinak.brobot.navigation.monitoring;

import io.github.jspinak.brobot.config.logging.LoggingVerbosityConfig;
import io.github.jspinak.brobot.statemanagement.StateDetector;
import io.github.jspinak.brobot.statemanagement.StateMemory;
import io.github.jspinak.brobot.test.ConcurrentTestBase;
import io.github.jspinak.brobot.test.annotations.Flaky;
import io.github.jspinak.brobot.test.annotations.Flaky.FlakyCause;
import io.github.jspinak.brobot.test.utils.ConcurrentTestHelper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Timeout;
import org.junit.jupiter.api.parallel.ResourceLock;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import java.time.Duration;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

/**
 * Comprehensive test suite for StateAwareScheduler.
 * Tests state-aware scheduling capabilities with various configurations.
 * Uses ConcurrentTestBase for thread-safe parallel execution.
 */
@DisplayName("StateAwareScheduler Tests")
@ResourceLock(value = ConcurrentTestBase.ResourceLocks.NETWORK)
class StateAwareSchedulerTest extends ConcurrentTestBase {
    
    @Mock
    private StateDetector stateDetector;
    
    @Mock
    private StateMemory stateMemory;
    
    @Mock
    private LoggingVerbosityConfig verbosityConfig;
    
    private StateAwareScheduler scheduler;
    private ScheduledExecutorService executorService;
    
    @BeforeEach
    @Override
    public void setupTest() {
        super.setupTest();
        MockitoAnnotations.openMocks(this);
        
        scheduler = new StateAwareScheduler(stateDetector, stateMemory);
        // Use reflection to inject the optional verbosityConfig
        try {
            var field = StateAwareScheduler.class.getDeclaredField("verbosityConfig");
            field.setAccessible(true);
            field.set(scheduler, verbosityConfig);
        } catch (Exception e) {
            // Ignore if field injection fails
        }
        
        executorService = Executors.newScheduledThreadPool(2);
    }
    
    @Nested
    @DisplayName("StateCheckConfiguration Tests")
    class StateCheckConfigurationTests {
        
        @Test
        @DisplayName("Should build configuration with default values")
        void testDefaultConfiguration() {
            var config = new StateAwareScheduler.StateCheckConfiguration.Builder()
                    .build();
            
            assertNotNull(config);
            assertTrue(config.getRequiredStates().isEmpty());
            assertTrue(config.isRebuildOnMismatch());
            assertFalse(config.isSkipIfStatesMissing());
            assertEquals(StateAwareScheduler.StateCheckConfiguration.CheckMode.CHECK_INACTIVE_ONLY, 
                        config.getCheckMode());
            assertEquals(-1, config.getMaxIterations());
        }
        
        @Test
        @DisplayName("Should build configuration with custom values")
        void testCustomConfiguration() {
            List<String> states = Arrays.asList("State1", "State2", "State3");
            
            var config = new StateAwareScheduler.StateCheckConfiguration.Builder()
                    .withRequiredStates(states)
                    .withRebuildOnMismatch(false)
                    .withSkipIfStatesMissing(true)
                    .withCheckMode(StateAwareScheduler.StateCheckConfiguration.CheckMode.CHECK_ALL)
                    .withMaxIterations(10)
                    .build();
            
            assertEquals(states, config.getRequiredStates());
            assertFalse(config.isRebuildOnMismatch());
            assertTrue(config.isSkipIfStatesMissing());
            assertEquals(StateAwareScheduler.StateCheckConfiguration.CheckMode.CHECK_ALL, 
                        config.getCheckMode());
            assertEquals(10, config.getMaxIterations());
        }
        
        @Test
        @DisplayName("Should handle CheckMode enum values")
        void testCheckModeEnum() {
            var checkAll = StateAwareScheduler.StateCheckConfiguration.CheckMode.CHECK_ALL;
            var checkInactive = StateAwareScheduler.StateCheckConfiguration.CheckMode.CHECK_INACTIVE_ONLY;
            
            assertNotNull(checkAll);
            assertNotNull(checkInactive);
            assertNotEquals(checkAll, checkInactive);
        }
    }
    
    @Nested
    @DisplayName("Scheduling with State Check Tests")
    class SchedulingTests {
        
        @Test
        @DisplayName("Should schedule task with fixed rate")
        void testScheduleWithFixedRate() throws InterruptedException {
            // Arrange
            AtomicInteger counter = new AtomicInteger(0);
            CountDownLatch executionLatch = new CountDownLatch(2); // Wait for at least 2 executions
            Runnable task = () -> {
                counter.incrementAndGet();
                if (counter.get() <= 2) {
                    executionLatch.countDown();
                }
            };
            
            var config = new StateAwareScheduler.StateCheckConfiguration.Builder()
                    .withRequiredStates(List.of("TestState"))
                    .withMaxIterations(3)
                    .build();
            
            // Mock state checking to pass
            Set<Long> activeStates = new HashSet<>(Arrays.asList(1L, 2L));
            when(stateMemory.getActiveStates()).thenReturn(activeStates);
            
            // Act
            ScheduledFuture<?> future = scheduler.scheduleWithStateCheck(
                    executorService, task, config, 0, 50, TimeUnit.MILLISECONDS);
            
            // Wait for executions with timeout
            boolean executed = executionLatch.await(1, TimeUnit.SECONDS);
            
            // Assert
            assertTrue(executed, "Task should have executed at least twice");
            assertTrue(counter.get() >= 2, "Counter should be at least 2, was: " + counter.get());
            future.cancel(true);
        }
        
        @Test
        @DisplayName("Should schedule task with fixed delay")
        void testScheduleWithFixedDelay() throws InterruptedException {
            // Arrange
            AtomicInteger counter = new AtomicInteger(0);
            CountDownLatch executionLatch = new CountDownLatch(2); // Wait for at least 2 executions
            Runnable task = () -> {
                counter.incrementAndGet();
                executionLatch.countDown();
            };
            
            var config = new StateAwareScheduler.StateCheckConfiguration.Builder()
                    .withRequiredStates(List.of("TestState"))
                    .withMaxIterations(3)
                    .build();
            
            // Mock state checking to pass
            Set<Long> activeStates = new HashSet<>(Arrays.asList(1L, 2L));
            when(stateMemory.getActiveStates()).thenReturn(activeStates);
            
            // Act
            ScheduledFuture<?> future = scheduler.scheduleWithFixedDelayAndStateCheck(
                    executorService, task, config, 0, 50, TimeUnit.MILLISECONDS);
            
            // Wait for executions with timeout to prevent hanging
            boolean executed = executionLatch.await(1, TimeUnit.SECONDS);
            
            // Assert
            assertTrue(executed, "Task should have executed at least twice");
            assertTrue(counter.get() >= 2, "Counter should be at least 2, was: " + counter.get());
            future.cancel(true);
        }
        
        @Test
        @DisplayName("Should respect max iterations limit")
        void testMaxIterationsLimit() throws InterruptedException {
            // Arrange
            AtomicInteger counter = new AtomicInteger(0);
            CountDownLatch latch = new CountDownLatch(2);
            Runnable task = () -> {
                counter.incrementAndGet();
                latch.countDown();
            };
            
            var config = new StateAwareScheduler.StateCheckConfiguration.Builder()
                    .withMaxIterations(2)
                    .build();
            
            when(stateMemory.getActiveStates()).thenReturn(new HashSet<>());
            
            // Act
            ScheduledFuture<?> future = scheduler.scheduleWithStateCheck(
                    executorService, task, config, 0, 10, TimeUnit.MILLISECONDS);
            
            // Wait for completion
            boolean completed = latch.await(500, TimeUnit.MILLISECONDS);
            Thread.sleep(100); // Extra wait to ensure no more executions
            
            // Assert
            assertTrue(completed);
            assertEquals(2, counter.get());
            assertTrue(future.isCancelled() || future.isDone());
        }
        
        @Test
        @DisplayName("Should handle state check failures with skip option")
        void testStateCheckFailureWithSkip() throws InterruptedException {
            // Arrange
            AtomicBoolean taskExecuted = new AtomicBoolean(false);
            Runnable task = () -> taskExecuted.set(true);
            
            var config = new StateAwareScheduler.StateCheckConfiguration.Builder()
                    .withRequiredStates(List.of("MissingState"))
                    .withSkipIfStatesMissing(true)
                    .withMaxIterations(1)
                    .build();
            
            // Mock state checking to fail
            doThrow(new IllegalStateException("State not found"))
                .when(stateDetector).checkForActiveStates();
            
            // Act
            ScheduledFuture<?> future = scheduler.scheduleWithStateCheck(
                    executorService, task, config, 0, 50, TimeUnit.MILLISECONDS);
            
            Thread.sleep(100);
            
            // Assert - task should be skipped due to state check failure
            assertFalse(taskExecuted.get());
            future.cancel(true);
        }
        
        @Test
        @DisplayName("Should handle state check failures without skip option")
        void testStateCheckFailureWithoutSkip() throws InterruptedException {
            // Arrange
            AtomicBoolean taskExecuted = new AtomicBoolean(false);
            Runnable task = () -> taskExecuted.set(true);
            
            var config = new StateAwareScheduler.StateCheckConfiguration.Builder()
                    .withRequiredStates(List.of("MissingState"))
                    .withSkipIfStatesMissing(false)
                    .withMaxIterations(1)
                    .build();
            
            // Mock state checking to fail - return empty list of active states
            when(stateMemory.getActiveStateNames()).thenReturn(new ArrayList<>());
            when(stateDetector.findState("MissingState")).thenReturn(false);
            
            // Act
            ScheduledFuture<?> future = scheduler.scheduleWithStateCheck(
                    executorService, task, config, 0, 50, TimeUnit.MILLISECONDS);
            
            Thread.sleep(100);
            
            // Assert - task should still execute (state check logs error but continues)
            assertTrue(taskExecuted.get());
            future.cancel(true);
        }
    }
    
    @Nested
    @DisplayName("Logging Configuration Tests")
    class LoggingTests {
        
        @Test
        @DisplayName("Should log in verbose mode")
        void testVerboseLogging() throws InterruptedException {
            // Arrange
            when(verbosityConfig.isVerboseMode()).thenReturn(true);
            when(verbosityConfig.isNormalMode()).thenReturn(false);
            
            AtomicInteger counter = new AtomicInteger(0);
            Runnable task = counter::incrementAndGet;
            
            var config = new StateAwareScheduler.StateCheckConfiguration.Builder()
                    .withMaxIterations(3)
                    .build();
            
            when(stateMemory.getActiveStates()).thenReturn(new HashSet<>());
            
            // Act
            ScheduledFuture<?> future = scheduler.scheduleWithStateCheck(
                    executorService, task, config, 0, 20, TimeUnit.MILLISECONDS);
            
            Thread.sleep(150);
            future.cancel(true);
            
            // Assert
            verify(verbosityConfig, atLeastOnce()).isVerboseMode();
        }
        
        @Test
        @DisplayName("Should log in normal mode")
        void testNormalLogging() throws InterruptedException {
            // Arrange
            when(verbosityConfig.isVerboseMode()).thenReturn(false);
            when(verbosityConfig.isNormalMode()).thenReturn(true);
            
            AtomicInteger counter = new AtomicInteger(0);
            Runnable task = counter::incrementAndGet;
            
            var config = new StateAwareScheduler.StateCheckConfiguration.Builder()
                    .withMaxIterations(5)
                    .build();
            
            when(stateMemory.getActiveStates()).thenReturn(new HashSet<>());
            
            // Act
            ScheduledFuture<?> future = scheduler.scheduleWithStateCheck(
                    executorService, task, config, 0, 20, TimeUnit.MILLISECONDS);
            
            Thread.sleep(200);
            future.cancel(true);
            
            // Assert
            verify(verbosityConfig, atLeastOnce()).isNormalMode();
        }
        
        @Test
        @DisplayName("Should handle missing verbosity config")
        void testMissingVerbosityConfig() throws InterruptedException {
            // Arrange - create scheduler without verbosity config
            scheduler = new StateAwareScheduler(stateDetector, stateMemory);
            
            AtomicInteger counter = new AtomicInteger(0);
            Runnable task = counter::incrementAndGet;
            
            var config = new StateAwareScheduler.StateCheckConfiguration.Builder()
                    .withMaxIterations(2)
                    .build();
            
            when(stateMemory.getActiveStates()).thenReturn(new HashSet<>());
            
            // Act
            ScheduledFuture<?> future = scheduler.scheduleWithStateCheck(
                    executorService, task, config, 0, 50, TimeUnit.MILLISECONDS);
            
            Thread.sleep(150);
            
            // Assert - should work without verbosity config
            assertTrue(counter.get() > 0);
            future.cancel(true);
        }
    }
    
    @Nested
    @DisplayName("Exception Handling Tests")
    class ExceptionHandlingTests {
        
        @Test
        @DisplayName("Should handle task exceptions gracefully")
        void testTaskExceptionHandling() throws InterruptedException {
            // Arrange
            AtomicInteger counter = new AtomicInteger(0);
            Runnable task = () -> {
                counter.incrementAndGet();
                if (counter.get() == 1) {
                    throw new RuntimeException("Task error");
                }
            };
            
            var config = new StateAwareScheduler.StateCheckConfiguration.Builder()
                    .withMaxIterations(3)
                    .build();
            
            when(stateMemory.getActiveStates()).thenReturn(new HashSet<>());
            
            // Act
            ScheduledFuture<?> future = scheduler.scheduleWithStateCheck(
                    executorService, task, config, 0, 30, TimeUnit.MILLISECONDS);
            
            Thread.sleep(150);
            
            // Assert - should continue executing despite exception
            assertTrue(counter.get() >= 2);
            future.cancel(true);
        }
        
        @Test
        @DisplayName("Should stop on max iterations exception")
        void testMaxIterationsException() throws InterruptedException {
            // Arrange
            AtomicInteger counter = new AtomicInteger(0);
            CountDownLatch latch = new CountDownLatch(1);
            
            Runnable task = () -> {
                counter.incrementAndGet();
                if (counter.get() == 1) {
                    latch.countDown();
                }
            };
            
            var config = new StateAwareScheduler.StateCheckConfiguration.Builder()
                    .withMaxIterations(1)
                    .build();
            
            when(stateMemory.getActiveStates()).thenReturn(new HashSet<>());
            
            // Act
            ScheduledFuture<?> future = scheduler.scheduleWithStateCheck(
                    executorService, task, config, 0, 20, TimeUnit.MILLISECONDS);
            
            boolean completed = latch.await(200, TimeUnit.MILLISECONDS);
            Thread.sleep(100); // Wait to ensure no more executions
            
            // Assert
            assertTrue(completed);
            assertEquals(1, counter.get()); // Should not exceed max iterations
        }
    }
    
    @AfterEach
    void tearDown() {
        // Use ConcurrentTestHelper for proper executor shutdown
        if (executorService != null) {
            ConcurrentTestHelper.shutdownExecutor(executorService, Duration.ofSeconds(2));
        }
        
        // Also ensure scheduler is properly stopped
        if (scheduler != null) {
            // scheduler.shutdown(); // Method doesn't exist
        }
    }
}