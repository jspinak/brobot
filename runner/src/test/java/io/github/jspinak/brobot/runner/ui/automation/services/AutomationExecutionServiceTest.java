package io.github.jspinak.brobot.runner.ui.automation.services;

import io.github.jspinak.brobot.runner.automation.AutomationOrchestrator;
import io.github.jspinak.brobot.runner.execution.ExecutionState;
import io.github.jspinak.brobot.runner.project.TaskButton;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.DisplayName;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicReference;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

/**
 * Unit tests for AutomationExecutionService.
 */
class AutomationExecutionServiceTest {
    
    @Mock
    private AutomationOrchestrator automationOrchestrator;
    
    private AutomationExecutionService executionService;
    
    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
        executionService = new AutomationExecutionService(automationOrchestrator);
    }
    
    @Test
    @DisplayName("Should start automation successfully")
    void testStartAutomation() throws Exception {
        // Given
        AtomicReference<ExecutionState> capturedState = new AtomicReference<>();
        CountDownLatch stateLatch = new CountDownLatch(2); // RUNNING and STOPPED
        
        executionService.setStateListener(state -> {
            capturedState.set(state);
            stateLatch.countDown();
        });
        
        // When
        CompletableFuture<Void> future = executionService.startAutomation();
        
        // Then
        assertTrue(executionService.isRunning());
        assertEquals(ExecutionState.RUNNING, executionService.getCurrentState());
        
        // Wait for completion
        future.get(2, TimeUnit.SECONDS);
        stateLatch.await(2, TimeUnit.SECONDS);
        
        assertFalse(executionService.isRunning());
        assertEquals(ExecutionState.STOPPED, capturedState.get());
    }
    
    @Test
    @DisplayName("Should not start automation when already running")
    void testStartAutomationWhenRunning() {
        // Given
        executionService.startAutomation();
        assertTrue(executionService.isRunning());
        
        // When
        CompletableFuture<Void> secondStart = executionService.startAutomation();
        
        // Then
        assertNotNull(secondStart);
        assertTrue(secondStart.isDone());
    }
    
    @Test
    @DisplayName("Should execute task successfully")
    void testExecuteTask() throws Exception {
        // Given
        TaskButton taskButton = createTestTaskButton("test-task", "Test Task");
        
        CountDownLatch logLatch = new CountDownLatch(3); // Multiple log messages
        executionService.setLogListener(message -> logLatch.countDown());
        
        // When
        CompletableFuture<Void> future = executionService.executeTask(taskButton);
        
        // Then
        assertTrue(executionService.isRunning());
        
        // Wait for completion
        future.get(3, TimeUnit.SECONDS);
        assertTrue(logLatch.await(3, TimeUnit.SECONDS));
        
        assertFalse(executionService.isRunning());
    }
    
    @Test
    @DisplayName("Should handle task without function name")
    void testExecuteTaskWithoutFunction() throws Exception {
        // Given
        TaskButton taskButton = new TaskButton();
        taskButton.setId("no-function");
        taskButton.setLabel("No Function Task");
        // No function name set
        
        CountDownLatch logLatch = new CountDownLatch(2);
        executionService.setLogListener(message -> {
            if (message.contains("No function defined")) {
                logLatch.countDown();
            }
            logLatch.countDown();
        });
        
        // When
        CompletableFuture<Void> future = executionService.executeTask(taskButton);
        
        // Then
        future.get(2, TimeUnit.SECONDS);
        assertTrue(logLatch.await(2, TimeUnit.SECONDS));
    }
    
    @Test
    @DisplayName("Should pause and resume automation")
    void testPauseResume() throws Exception {
        // Given
        CountDownLatch pauseLatch = new CountDownLatch(1);
        CountDownLatch resumeLatch = new CountDownLatch(1);
        
        executionService.setStateListener(state -> {
            if (state == ExecutionState.PAUSED) pauseLatch.countDown();
            if (state == ExecutionState.RUNNING && pauseLatch.getCount() == 0) {
                resumeLatch.countDown();
            }
        });
        
        // Start automation
        executionService.startAutomation();
        assertTrue(executionService.isRunning());
        
        // When - Pause
        executionService.pauseAutomation();
        
        // Then
        assertTrue(pauseLatch.await(1, TimeUnit.SECONDS));
        assertTrue(executionService.isPaused());
        assertEquals(ExecutionState.PAUSED, executionService.getCurrentState());
        verify(automationOrchestrator).pauseAutomation();
        
        // When - Resume
        executionService.resumeAutomation();
        
        // Then
        assertTrue(resumeLatch.await(1, TimeUnit.SECONDS));
        assertFalse(executionService.isPaused());
        assertEquals(ExecutionState.RUNNING, executionService.getCurrentState());
        verify(automationOrchestrator).resumeAutomation();
    }
    
    @Test
    @DisplayName("Should stop automation")
    void testStopAutomation() throws Exception {
        // Given
        CountDownLatch stopLatch = new CountDownLatch(1);
        executionService.setStateListener(state -> {
            if (state == ExecutionState.STOPPED) stopLatch.countDown();
        });
        
        executionService.startAutomation();
        assertTrue(executionService.isRunning());
        
        // When
        executionService.stopAutomation();
        
        // Then
        assertTrue(stopLatch.await(1, TimeUnit.SECONDS));
        assertFalse(executionService.isRunning());
        assertFalse(executionService.isPaused());
        assertEquals(ExecutionState.STOPPED, executionService.getCurrentState());
        verify(automationOrchestrator).stopAllAutomation();
    }
    
    @Test
    @DisplayName("Should handle concurrent task execution prevention")
    void testPreventConcurrentExecution() {
        // Given
        executionService.startAutomation();
        TaskButton taskButton = createTestTaskButton("concurrent-test", "Concurrent Test");
        
        // When
        CompletableFuture<Void> taskFuture = executionService.executeTask(taskButton);
        
        // Then
        assertNotNull(taskFuture);
        assertTrue(taskFuture.isDone()); // Should complete immediately without executing
    }
    
    @Test
    @DisplayName("Should track progress updates")
    void testProgressUpdates() throws Exception {
        // Given
        CountDownLatch progressLatch = new CountDownLatch(5); // Expect at least 5 updates
        executionService.setProgressListener(progress -> {
            assertTrue(progress >= 0 && progress <= 1.0);
            progressLatch.countDown();
        });
        
        // When
        CompletableFuture<Void> future = executionService.startAutomation();
        
        // Then
        assertTrue(progressLatch.await(10, TimeUnit.SECONDS));
        future.get(10, TimeUnit.SECONDS);
    }
    
    @Test
    @DisplayName("Should handle exceptions during execution")
    void testExceptionHandling() throws Exception {
        // Given
        doThrow(new RuntimeException("Test exception")).when(automationOrchestrator).pauseAutomation();
        
        CountDownLatch errorLogLatch = new CountDownLatch(1);
        executionService.setLogListener(message -> {
            if (message.contains("Error pausing")) {
                errorLogLatch.countDown();
            }
        });
        
        executionService.startAutomation();
        
        // When
        executionService.pauseAutomation();
        
        // Then
        assertTrue(errorLogLatch.await(1, TimeUnit.SECONDS));
        // Should still be marked as paused despite error
        assertTrue(executionService.isPaused());
    }
    
    /**
     * Creates a test task button.
     */
    private TaskButton createTestTaskButton(String id, String label) {
        TaskButton taskButton = new TaskButton();
        taskButton.setId(id);
        taskButton.setLabel(label);
        taskButton.setFunctionName("testFunction");
        taskButton.setCategory("Test");
        return taskButton;
    }
}