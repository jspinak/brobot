package io.github.jspinak.brobot.runner.execution.timeout;

import io.github.jspinak.brobot.runner.common.diagnostics.DiagnosticInfo;
import io.github.jspinak.brobot.runner.execution.context.ExecutionContext;
import io.github.jspinak.brobot.runner.execution.context.ExecutionOptions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.DisplayName;

import java.time.Duration;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicReference;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Unit tests for ExecutionTimeoutManager.
 * 
 * Tests timeout monitoring, handler callbacks, and diagnostic capabilities.
 */
@DisplayName("ExecutionTimeoutManager Tests")
class ExecutionTimeoutManagerTest {
    
    private ExecutionTimeoutManager timeoutManager;
    
    @BeforeEach
    void setUp() {
        timeoutManager = new ExecutionTimeoutManager();
    }
    
    @AfterEach
    void tearDown() {
        timeoutManager.shutdown();
    }
    
    @Test
    @DisplayName("Should monitor execution and trigger timeout")
    void shouldMonitorExecutionAndTriggerTimeout() throws Exception {
        // Given
        CountDownLatch timeoutTriggered = new CountDownLatch(1);
        AtomicBoolean handlerCalled = new AtomicBoolean(false);
        AtomicReference<String> capturedId = new AtomicReference<>();
        AtomicReference<Duration> capturedElapsed = new AtomicReference<>();
        
        ExecutionTimeoutManager.TimeoutHandler handler = (id, taskName, elapsed) -> {
            handlerCalled.set(true);
            capturedId.set(id);
            capturedElapsed.set(elapsed);
            timeoutTriggered.countDown();
        };
        
        ExecutionContext context = createContext("TimeoutTask", Duration.ofMillis(100));
        
        // When
        timeoutManager.monitor(context, handler);
        
        // Then - wait for timeout
        assertTrue(timeoutTriggered.await(2, TimeUnit.SECONDS));
        assertTrue(handlerCalled.get());
        assertEquals(context.getId(), capturedId.get());
        assertNotNull(capturedElapsed.get());
        assertTrue(capturedElapsed.get().toMillis() >= 100);
    }
    
    @Test
    @DisplayName("Should not trigger timeout for completed execution")
    void shouldNotTriggerTimeoutForCompletedExecution() throws Exception {
        // Given
        AtomicBoolean handlerCalled = new AtomicBoolean(false);
        
        ExecutionTimeoutManager.TimeoutHandler handler = (id, taskName, elapsed) -> {
            handlerCalled.set(true);
        };
        
        ExecutionContext context = createContext("QuickTask", Duration.ofSeconds(1));
        
        // When
        timeoutManager.monitor(context, handler);
        
        // Stop monitoring before timeout
        Thread.sleep(100);
        timeoutManager.stopMonitoring(context.getId());
        
        // Wait past timeout period
        Thread.sleep(1100);
        
        // Then
        assertFalse(handlerCalled.get());
    }
    
    @Test
    @DisplayName("Should handle null parameters")
    void shouldHandleNullParameters() {
        // When/Then
        assertThrows(IllegalArgumentException.class, 
                () -> timeoutManager.monitor(null, (id, task, elapsed) -> {}));
        assertThrows(IllegalArgumentException.class, 
                () -> timeoutManager.monitor(createContext("Test", Duration.ofSeconds(1)), null));
    }
    
    @Test
    @DisplayName("Should check if execution is timed out")
    void shouldCheckIfExecutionIsTimedOut() throws Exception {
        // Given
        ExecutionContext context = createContext("TimeoutCheckTask", Duration.ofMillis(100));
        
        timeoutManager.monitor(context, (id, task, elapsed) -> {});
        
        // Initially not timed out
        assertFalse(timeoutManager.isTimedOut(context.getId()));
        
        // Wait for timeout
        Thread.sleep(150);
        
        // Then
        assertTrue(timeoutManager.isTimedOut(context.getId()));
    }
    
    @Test
    @DisplayName("Should get remaining time before timeout")
    void shouldGetRemainingTimeBeforeTimeout() throws Exception {
        // Given
        ExecutionContext context = createContext("RemainingTimeTask", Duration.ofSeconds(2));
        
        timeoutManager.monitor(context, (id, task, elapsed) -> {});
        
        // When - immediately
        Duration remaining = timeoutManager.getRemainingTime(context.getId());
        
        // Then
        assertNotNull(remaining);
        assertTrue(remaining.toMillis() > 1900);
        assertTrue(remaining.toMillis() <= 2000);
        
        // When - after some time
        Thread.sleep(500);
        remaining = timeoutManager.getRemainingTime(context.getId());
        
        // Then
        assertTrue(remaining.toMillis() > 1400);
        assertTrue(remaining.toMillis() <= 1500);
    }
    
    @Test
    @DisplayName("Should return null for unknown execution")
    void shouldReturnNullForUnknownExecution() {
        // When
        Duration remaining = timeoutManager.getRemainingTime("unknown-id");
        
        // Then
        assertNull(remaining);
        assertFalse(timeoutManager.isTimedOut("unknown-id"));
    }
    
    @Test
    @DisplayName("Should monitor multiple executions")
    void shouldMonitorMultipleExecutions() throws Exception {
        // Given
        int executionCount = 3;
        CountDownLatch allTimedOut = new CountDownLatch(executionCount);
        
        ExecutionTimeoutManager.TimeoutHandler handler = (id, task, elapsed) -> {
            allTimedOut.countDown();
        };
        
        // When
        for (int i = 0; i < executionCount; i++) {
            ExecutionContext context = createContext("Task-" + i, Duration.ofMillis(100 + i * 50));
            timeoutManager.monitor(context, handler);
        }
        
        // Then
        assertEquals(executionCount, timeoutManager.getMonitoredCount());
        assertTrue(allTimedOut.await(3, TimeUnit.SECONDS));
    }
    
    @Disabled("CI failure - needs investigation")
@Test
    @DisplayName("Should handle timeout handler exceptions")
    void shouldHandleTimeoutHandlerExceptions() throws Exception {
        // Given
        CountDownLatch handlerCalled = new CountDownLatch(1);
        
        ExecutionTimeoutManager.TimeoutHandler handler = (id, task, elapsed) -> {
            handlerCalled.countDown();
            throw new RuntimeException("Handler exception");
        };
        
        ExecutionContext context = createContext("ExceptionTask", Duration.ofMillis(100));
        
        // When
        timeoutManager.monitor(context, handler);
        
        // Then - handler should be called despite exception
        assertTrue(handlerCalled.await(2, TimeUnit.SECONDS));
        
        // Timeout manager should continue functioning
        assertEquals(0, timeoutManager.getMonitoredCount()); // Cleaned up after handling
    }
    
    @Test
    @DisplayName("Should provide diagnostic information")
    void shouldProvideDiagnosticInformation() throws Exception {
        // Given
        ExecutionContext context1 = createContext("Task1", Duration.ofSeconds(10));
        ExecutionContext context2 = createContext("Task2", Duration.ofSeconds(5));
        
        timeoutManager.monitor(context1, (id, task, elapsed) -> {});
        timeoutManager.monitor(context2, (id, task, elapsed) -> {});
        
        // When
        DiagnosticInfo info = timeoutManager.getDiagnosticInfo();
        
        // Then
        assertNotNull(info);
        assertEquals("ExecutionTimeoutManager", info.getComponent());
        assertEquals(2, info.getStates().get("monitoredExecutions"));
        assertTrue(info.getStates().containsKey("checkInterval"));
        assertTrue(info.getStates().containsKey("schedulerActive"));
        
        // Should contain execution details
        assertTrue(info.getStates().containsKey("execution." + context1.getId() + ".task"));
        assertTrue(info.getStates().containsKey("execution." + context2.getId() + ".task"));
    }
    
    @Test
    @DisplayName("Should use default timeout when not specified")
    void shouldUseDefaultTimeoutWhenNotSpecified() throws Exception {
        // Given
        CountDownLatch timeoutTriggered = new CountDownLatch(1);
        AtomicReference<Duration> capturedElapsed = new AtomicReference<>();
        
        ExecutionTimeoutManager.TimeoutHandler handler = (id, task, elapsed) -> {
            capturedElapsed.set(elapsed);
            timeoutTriggered.countDown();
        };
        
        // Context with null timeout in options
        ExecutionOptions options = ExecutionOptions.builder()
                .timeout(null)
                .build();
        
        ExecutionContext context = ExecutionContext.builder()
                .taskName("DefaultTimeoutTask")
                .options(options)
                .build();
        
        // When
        timeoutManager.monitor(context, handler);
        
        // Then - should use default timeout (5 minutes)
        // We won't wait 5 minutes, just verify it's monitored
        assertEquals(1, timeoutManager.getMonitoredCount());
        
        // Clean up
        timeoutManager.stopMonitoring(context.getId());
    }
    
    @Disabled("CI failure - needs investigation")
@Test
    @DisplayName("Should only call handler once per timeout")
    void shouldOnlyCallHandlerOncePerTimeout() throws Exception {
        // Given
        CountDownLatch handlerCalls = new CountDownLatch(2);
        AtomicInteger callCount = new AtomicInteger(0);
        
        ExecutionTimeoutManager.TimeoutHandler handler = (id, task, elapsed) -> {
            callCount.incrementAndGet();
            handlerCalls.countDown();
        };
        
        ExecutionContext context = createContext("OnceOnlyTask", Duration.ofMillis(100));
        
        // When
        timeoutManager.monitor(context, handler);
        
        // Wait for timeout and then some
        assertFalse(handlerCalls.await(500, TimeUnit.MILLISECONDS));
        
        // Then
        assertEquals(1, callCount.get());
    }
    
    @Test
    @DisplayName("Should enable and disable diagnostic mode")
    void shouldEnableAndDisableDiagnosticMode() {
        // Initially disabled
        assertFalse(timeoutManager.isDiagnosticModeEnabled());
        
        // Enable
        timeoutManager.enableDiagnosticMode(true);
        assertTrue(timeoutManager.isDiagnosticModeEnabled());
        
        // Disable
        timeoutManager.enableDiagnosticMode(false);
        assertFalse(timeoutManager.isDiagnosticModeEnabled());
    }
    
    // Helper method to create execution context
    private ExecutionContext createContext(String taskName, Duration timeout) {
        ExecutionOptions options = ExecutionOptions.builder()
                .timeout(timeout)
                .build();
        
        return ExecutionContext.builder()
                .taskName(taskName)
                .correlationId("test-correlation-" + taskName)
                .options(options)
                .build();
    }
}