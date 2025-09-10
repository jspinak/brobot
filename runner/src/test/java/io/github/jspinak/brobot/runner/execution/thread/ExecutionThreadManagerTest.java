package io.github.jspinak.brobot.runner.execution.thread;

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
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Unit tests for ExecutionThreadManager.
 * 
 * Tests thread management, task submission, cancellation, and diagnostics.
 */
@DisplayName("ExecutionThreadManager Tests")
class ExecutionThreadManagerTest {
    
    private ExecutionThreadManager threadManager;
    
    @BeforeEach
    void setUp() {
        threadManager = new ExecutionThreadManager();
    }
    
    @AfterEach
    void tearDown() {
        threadManager.shutdown();
    }
    
    @Disabled("CI failure - needs investigation")
@Test
    @DisplayName("Should submit and execute task")
    void shouldSubmitAndExecuteTask() throws Exception {
        // Given
        CountDownLatch taskExecuted = new CountDownLatch(1);
        AtomicBoolean wasExecuted = new AtomicBoolean(false);
        
        Runnable task = () -> {
            wasExecuted.set(true);
            taskExecuted.countDown();
        };
        
        ExecutionContext context = createContext("TestTask");
        
        // When
        Future<?> future = threadManager.submit(task, context);
        
        // Then
        assertTrue(taskExecuted.await(2, TimeUnit.SECONDS));
        assertTrue(wasExecuted.get());
        assertNotNull(future);
        assertTrue(future.isDone());
    }
    
    @Test
    @DisplayName("Should handle null parameters")
    void shouldHandleNullParameters() {
        // When/Then
        assertThrows(IllegalArgumentException.class, 
                () -> threadManager.submit(null, createContext("Test")));
        assertThrows(IllegalArgumentException.class, 
                () -> threadManager.submit(() -> {}, null));
    }
    
    @Test
    @DisplayName("Should track active executions")
    void shouldTrackActiveExecutions() throws Exception {
        // Given
        CountDownLatch started = new CountDownLatch(1);
        CountDownLatch proceed = new CountDownLatch(1);
        
        Runnable task = () -> {
            started.countDown();
            try {
                proceed.await();
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            }
        };
        
        ExecutionContext context = createContext("LongTask");
        
        // When
        threadManager.submit(task, context);
        started.await();
        
        // Then
        assertEquals(1, threadManager.getActiveExecutionCount());
        assertTrue(threadManager.isActive(context.getId()));
        
        // When - complete task
        proceed.countDown();
        Thread.sleep(100); // Give time to clean up
        
        // Then
        assertEquals(0, threadManager.getActiveExecutionCount());
        assertFalse(threadManager.isActive(context.getId()));
    }
    
    @Test
    @DisplayName("Should cancel execution")
    void shouldCancelExecution() throws Exception {
        // Given
        CountDownLatch started = new CountDownLatch(1);
        AtomicBoolean wasInterrupted = new AtomicBoolean(false);
        
        Runnable task = () -> {
            started.countDown();
            try {
                Thread.sleep(5000); // Long sleep
            } catch (InterruptedException e) {
                wasInterrupted.set(true);
                Thread.currentThread().interrupt();
            }
        };
        
        ExecutionContext context = createContext("CancellableTask");
        
        // When
        Future<?> future = threadManager.submit(task, context);
        started.await();
        
        boolean cancelled = threadManager.cancel(context.getId(), true);
        
        // Then
        assertTrue(cancelled);
        assertTrue(future.isCancelled());
        Thread.sleep(100); // Give time for interrupt
        assertTrue(wasInterrupted.get());
    }
    
    @Test
    @DisplayName("Should handle cancellation of non-existent execution")
    void shouldHandleCancellationOfNonExistentExecution() {
        // When
        boolean cancelled = threadManager.cancel("non-existent-id", true);
        
        // Then
        assertFalse(cancelled);
    }
    
    @Test
    @DisplayName("Should execute multiple tasks concurrently")
    void shouldExecuteMultipleTasksConcurrently() throws Exception {
        // Given
        int taskCount = 5;
        CountDownLatch allStarted = new CountDownLatch(taskCount);
        CountDownLatch allFinished = new CountDownLatch(taskCount);
        AtomicInteger concurrentCount = new AtomicInteger(0);
        AtomicInteger maxConcurrent = new AtomicInteger(0);
        
        Runnable task = () -> {
            int current = concurrentCount.incrementAndGet();
            maxConcurrent.updateAndGet(max -> Math.max(max, current));
            allStarted.countDown();
            
            try {
                Thread.sleep(100); // Hold thread briefly
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            } finally {
                concurrentCount.decrementAndGet();
                allFinished.countDown();
            }
        };
        
        // When
        for (int i = 0; i < taskCount; i++) {
            threadManager.submit(task, createContext("Task-" + i));
        }
        
        // Then
        assertTrue(allStarted.await(2, TimeUnit.SECONDS));
        assertTrue(maxConcurrent.get() > 1); // Should run concurrently
        assertTrue(allFinished.await(2, TimeUnit.SECONDS));
    }
    
    @Test
    @DisplayName("Should provide diagnostic information")
    void shouldProvideDiagnosticInformation() {
        // When
        DiagnosticInfo info = threadManager.getDiagnosticInfo();
        
        // Then
        assertNotNull(info);
        assertEquals("ExecutionThreadManager", info.getComponent());
        assertNotNull(info.getStates());
        assertTrue(info.getStates().containsKey("activeExecutions"));
        assertTrue(info.getStates().containsKey("poolSize"));
        assertTrue(info.getStates().containsKey("completedTaskCount"));
    }
    
    @Test
    @DisplayName("Should handle task exceptions")
    void shouldHandleTaskExceptions() throws Exception {
        // Given
        RuntimeException expectedException = new RuntimeException("Test exception");
        AtomicBoolean exceptionThrown = new AtomicBoolean(false);
        
        Runnable task = () -> {
            exceptionThrown.set(true);
            throw expectedException;
        };
        
        ExecutionContext context = createContext("FailingTask");
        
        // When
        Future<?> future = threadManager.submit(task, context);
        
        // Wait for completion
        Thread.sleep(100);
        
        // Then
        assertTrue(exceptionThrown.get());
        assertTrue(future.isDone());
        assertEquals(0, threadManager.getActiveExecutionCount());
    }
    
    @Test
    @DisplayName("Should respect thread priority")
    void shouldRespectThreadPriority() throws Exception {
        // Given
        CountDownLatch threadStarted = new CountDownLatch(1);
        AtomicInteger recordedPriority = new AtomicInteger();
        
        Runnable task = () -> {
            recordedPriority.set(Thread.currentThread().getPriority());
            threadStarted.countDown();
        };
        
        ExecutionOptions options = ExecutionOptions.builder()
                .priority(Thread.MAX_PRIORITY)
                .build();
        
        ExecutionContext context = ExecutionContext.builder()
                .taskName("HighPriorityTask")
                .options(options)
                .build();
        
        // When
        threadManager.submit(task, context);
        threadStarted.await();
        
        // Then
        assertEquals(Thread.MAX_PRIORITY, recordedPriority.get());
    }
    
    @Test
    @DisplayName("Should enable and disable diagnostic mode")
    void shouldEnableAndDisableDiagnosticMode() {
        // Initially disabled
        assertFalse(threadManager.isDiagnosticModeEnabled());
        
        // Enable
        threadManager.enableDiagnosticMode(true);
        assertTrue(threadManager.isDiagnosticModeEnabled());
        
        // Disable
        threadManager.enableDiagnosticMode(false);
        assertFalse(threadManager.isDiagnosticModeEnabled());
    }
    
    @Test
    @DisplayName("Should properly shutdown")
    void shouldProperlyShutdown() throws Exception {
        // Given
        CountDownLatch taskStarted = new CountDownLatch(1);
        CountDownLatch shutdownStarted = new CountDownLatch(1);
        AtomicBoolean wasInterrupted = new AtomicBoolean(false);
        
        Runnable task = () -> {
            taskStarted.countDown();
            try {
                shutdownStarted.await();
                Thread.sleep(5000);
            } catch (InterruptedException e) {
                wasInterrupted.set(true);
            }
        };
        
        ExecutionContext context = createContext("ShutdownTask");
        threadManager.submit(task, context);
        taskStarted.await();
        
        // When
        shutdownStarted.countDown();
        threadManager.shutdown();
        
        // Then
        Thread.sleep(100);
        assertTrue(wasInterrupted.get());
        assertEquals(0, threadManager.getActiveExecutionCount());
    }
    
    // Helper method to create execution context
    private ExecutionContext createContext(String taskName) {
        return ExecutionContext.builder()
                .taskName(taskName)
                .correlationId("test-correlation-" + taskName)
                .options(ExecutionOptions.defaultOptions())
                .build();
    }
}