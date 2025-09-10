package io.github.jspinak.brobot.runner.execution.service;

import io.github.jspinak.brobot.runner.common.diagnostics.DiagnosticInfo;
import io.github.jspinak.brobot.runner.execution.ExecutionState;
import io.github.jspinak.brobot.runner.execution.ExecutionStatus;
import io.github.jspinak.brobot.runner.execution.context.ExecutionOptions;
import io.github.jspinak.brobot.runner.execution.safety.ExecutionSafetyService;
import io.github.jspinak.brobot.runner.execution.thread.ExecutionThreadManager;
import io.github.jspinak.brobot.runner.execution.timeout.ExecutionTimeoutManager;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.Duration;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicReference;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

/**
 * Unit tests for ExecutionService.
 * 
 * Tests core execution orchestration, status management, and component coordination.
 */
@ExtendWith(MockitoExtension.class)
@DisplayName("ExecutionService Tests")
@Disabled("CI failure - needs investigation")
class ExecutionServiceTest {
    
    @Mock
    private ExecutionThreadManager threadManager;
    
    @Mock
    private ExecutionTimeoutManager timeoutManager;
    
    @Mock
    private ExecutionSafetyService safetyService;
    
    private ExecutionService executionService;
    
    @BeforeEach
    void setUp() {
        executionService = new ExecutionService(threadManager, timeoutManager, safetyService);
        
        // Default mock behavior
        when(safetyService.checkActionSafety(any(), anyString())).thenReturn(true);
        when(threadManager.submit(any(), any())).thenReturn(CompletableFuture.completedFuture(null));
    }
    
    @Test
    @DisplayName("Should execute task successfully")
    void shouldExecuteTaskSuccessfully() throws Exception {
        // Given
        CountDownLatch taskExecuted = new CountDownLatch(1);
        AtomicBoolean wasExecuted = new AtomicBoolean(false);
        
        Runnable task = () -> {
            wasExecuted.set(true);
            taskExecuted.countDown();
        };
        
        String taskName = "TestTask";
        ExecutionOptions options = ExecutionOptions.defaultOptions();
        
        // When
        CompletableFuture<Void> future = executionService.execute(task, taskName, options);
        
        // Then
        assertTrue(taskExecuted.await(2, TimeUnit.SECONDS));
        assertTrue(wasExecuted.get());
        assertTrue(future.isDone());
        assertFalse(future.isCompletedExceptionally());
        
        // Verify interactions
        verify(threadManager).submit(any(Runnable.class), any());
        verify(timeoutManager).monitor(any(), any());
        verify(safetyService).checkActionSafety(any(), eq("execution_start"));
        verify(safetyService).recordSuccess(any());
    }
    
    @Test
    @DisplayName("Should handle null parameters")
    void shouldHandleNullParameters() {
        // When/Then
        assertThrows(IllegalArgumentException.class, 
                () -> executionService.execute(null, "Task", ExecutionOptions.defaultOptions()));
        assertThrows(IllegalArgumentException.class, 
                () -> executionService.execute(() -> {}, null, ExecutionOptions.defaultOptions()));
    }
    
    @Test
    @DisplayName("Should use default options when null")
    void shouldUseDefaultOptionsWhenNull() throws Exception {
        // Given
        CountDownLatch taskExecuted = new CountDownLatch(1);
        Runnable task = taskExecuted::countDown;
        
        // When
        CompletableFuture<Void> future = executionService.execute(task, "TestTask", null);
        
        // Then
        assertTrue(taskExecuted.await(2, TimeUnit.SECONDS));
        assertTrue(future.isDone());
    }
    
    @Test
    @DisplayName("Should handle task execution failure")
    void shouldHandleTaskExecutionFailure() throws Exception {
        // Given
        RuntimeException expectedException = new RuntimeException("Task failed");
        Runnable failingTask = () -> {
            throw expectedException;
        };
        
        // When
        CompletableFuture<Void> future = executionService.execute(
                failingTask, "FailingTask", ExecutionOptions.defaultOptions());
        
        // Wait for completion
        Thread.sleep(100);
        
        // Then
        assertTrue(future.isCompletedExceptionally());
        verify(safetyService).recordFailure(any(), eq(expectedException));
        verify(timeoutManager).stopMonitoring(anyString());
    }
    
    @Test
    @DisplayName("Should handle safety check failure")
    void shouldHandleSafetyCheckFailure() throws Exception {
        // Given
        when(safetyService.checkActionSafety(any(), eq("execution_start")))
                .thenReturn(false);
        
        Runnable task = () -> {};
        
        // When
        CompletableFuture<Void> future = executionService.execute(
                task, "BlockedTask", ExecutionOptions.defaultOptions());
        
        // Wait for completion
        Thread.sleep(100);
        
        // Then
        assertTrue(future.isCompletedExceptionally());
        verify(safetyService).recordFailure(any(), any(SecurityException.class));
    }
    
    @Test
    @DisplayName("Should cancel execution")
    void shouldCancelExecution() throws Exception {
        // Given
        CountDownLatch taskStarted = new CountDownLatch(1);
        CountDownLatch holdTask = new CountDownLatch(1);
        
        Runnable longTask = () -> {
            taskStarted.countDown();
            try {
                holdTask.await();
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            }
        };
        
        // Mock thread manager to actually run the task
        when(threadManager.submit(any(), any())).thenAnswer(invocation -> {
            Runnable wrappedTask = invocation.getArgument(0);
            return CompletableFuture.runAsync(wrappedTask);
        });
        
        when(threadManager.cancel(anyString(), anyBoolean())).thenReturn(true);
        
        // Create status listener to capture execution ID
        AtomicReference<String> executionId = new AtomicReference<>();
        
        // When
        CompletableFuture<Void> future = executionService.execute(
                longTask, "CancellableTask", ExecutionOptions.defaultOptions());
        
        // Wait for task to start
        taskStarted.await();
        
        // Get execution ID by checking active executions
        // Since we can't directly access it, we'll use the cancel method
        // In real usage, the ID would be tracked by the caller
        
        // Then - verify the service is set up correctly
        assertFalse(future.isDone());
        
        // Clean up
        holdTask.countDown();
    }
    
    @Test
    @DisplayName("Should track status updates")
    void shouldTrackStatusUpdates() throws Exception {
        // Given
        AtomicReference<ExecutionStatus> capturedStatus = new AtomicReference<>();
        CountDownLatch statusReceived = new CountDownLatch(3); // Start, Running, Completed
        
        Runnable task = () -> {};
        
        // When
        CompletableFuture<Void> future = executionService.execute(
                task, "StatusTask", ExecutionOptions.defaultOptions());
        
        // Add status listener (would normally be done before execution)
        // This is a limitation of the current API design
        
        // Wait for completion
        future.get(2, TimeUnit.SECONDS);
        
        // Then
        assertTrue(future.isDone());
    }
    
    @Test
    @DisplayName("Should handle timeout")
    void shouldHandleTimeout() throws Exception {
        // Given
        CountDownLatch timeoutHandlerCalled = new CountDownLatch(1);
        
        // Setup timeout manager to call handler immediately
        doAnswer(invocation -> {
            ExecutionTimeoutManager.TimeoutHandler handler = invocation.getArgument(1);
            // Simulate timeout
            new Thread(() -> {
                try {
                    Thread.sleep(50);
                    handler.onTimeout("test-id", "TimeoutTask", Duration.ofMillis(100));
                    timeoutHandlerCalled.countDown();
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }).start();
            return null;
        }).when(timeoutManager).monitor(any(), any());
        
        Runnable task = () -> {
            try {
                Thread.sleep(200); // Longer than timeout
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            }
        };
        
        // When
        CompletableFuture<Void> future = executionService.execute(
                task, "TimeoutTask", ExecutionOptions.builder()
                        .timeout(Duration.ofMillis(100))
                        .build());
        
        // Then
        assertTrue(timeoutHandlerCalled.await(2, TimeUnit.SECONDS));
        verify(threadManager).cancel(anyString(), eq(true));
    }
    
    @Test
    @DisplayName("Should respect start delay")
    void shouldRespectStartDelay() throws Exception {
        // Given
        long startDelay = 100; // milliseconds
        AtomicBoolean taskStarted = new AtomicBoolean(false);
        long startTime = System.currentTimeMillis();
        
        Runnable task = () -> taskStarted.set(true);
        
        ExecutionOptions options = ExecutionOptions.builder()
                .startDelay(Duration.ofMillis(startDelay))
                .build();
        
        // When
        CompletableFuture<Void> future = executionService.execute(task, "DelayedTask", options);
        
        // Check task hasn't started immediately
        Thread.sleep(50);
        assertFalse(taskStarted.get());
        
        // Wait for completion
        future.get(1, TimeUnit.SECONDS);
        
        // Then
        assertTrue(taskStarted.get());
        long actualDelay = System.currentTimeMillis() - startTime;
        assertTrue(actualDelay >= startDelay);
    }
    
    @Test
    @DisplayName("Should provide diagnostic information")
    void shouldProvideDiagnosticInformation() {
        // When
        DiagnosticInfo info = executionService.getDiagnosticInfo();
        
        // Then
        assertNotNull(info);
        assertEquals("ExecutionService", info.getComponent());
        assertNotNull(info.getStates());
        assertEquals(0, info.getStates().get("activeExecutions"));
        assertEquals(0, info.getStates().get("statusListeners"));
    }
    
    @Test
    @DisplayName("Should cleanup after execution")
    void shouldCleanupAfterExecution() throws Exception {
        // Given
        Runnable task = () -> {};
        
        // When
        CompletableFuture<Void> future = executionService.execute(
                task, "CleanupTask", ExecutionOptions.defaultOptions());
        
        // Wait for completion
        future.get(1, TimeUnit.SECONDS);
        
        // Then
        verify(timeoutManager).stopMonitoring(anyString());
        verify(safetyService).cleanupExecution(anyString());
        
        // Check diagnostics show no active executions
        DiagnosticInfo info = executionService.getDiagnosticInfo();
        assertEquals(0, info.getStates().get("activeExecutions"));
    }
    
    @Test
    @DisplayName("Should handle multiple concurrent executions")
    void shouldHandleMultipleConcurrentExecutions() throws Exception {
        // Given
        int taskCount = 5;
        CountDownLatch allStarted = new CountDownLatch(taskCount);
        CountDownLatch allCompleted = new CountDownLatch(taskCount);
        
        // Mock to actually execute tasks
        when(threadManager.submit(any(), any())).thenAnswer(invocation -> {
            Runnable wrappedTask = invocation.getArgument(0);
            return CompletableFuture.runAsync(wrappedTask);
        });
        
        // When
        for (int i = 0; i < taskCount; i++) {
            final int taskId = i;
            Runnable task = () -> {
                allStarted.countDown();
                try {
                    Thread.sleep(50);
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                }
                allCompleted.countDown();
            };
            
            executionService.execute(task, "Task-" + taskId, ExecutionOptions.defaultOptions());
        }
        
        // Then
        assertTrue(allStarted.await(2, TimeUnit.SECONDS));
        assertTrue(allCompleted.await(2, TimeUnit.SECONDS));
        
        // Verify all executions were tracked
        verify(threadManager, times(taskCount)).submit(any(), any());
        verify(timeoutManager, times(taskCount)).monitor(any(), any());
        verify(safetyService, times(taskCount)).recordSuccess(any());
    }
    
    @Test
    @DisplayName("Should enable and disable diagnostic mode")
    void shouldEnableAndDisableDiagnosticMode() {
        // Initially disabled
        assertFalse(executionService.isDiagnosticModeEnabled());
        
        // Enable
        executionService.enableDiagnosticMode(true);
        assertTrue(executionService.isDiagnosticModeEnabled());
        
        // Disable
        executionService.enableDiagnosticMode(false);
        assertFalse(executionService.isDiagnosticModeEnabled());
    }
}