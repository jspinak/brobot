package io.github.jspinak.brobot.runner.ui.automation.services;

import io.github.jspinak.brobot.runner.automation.AutomationOrchestrator;
import io.github.jspinak.brobot.runner.execution.ExecutionState;
import io.github.jspinak.brobot.runner.project.TaskButton;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicReference;
import java.util.function.Consumer;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ImprovedExecutionServiceTest {
    
    @Mock
    private AutomationOrchestrator automationOrchestrator;
    
    private ImprovedExecutionService service;
    
    @BeforeEach
    void setUp() {
        service = new ImprovedExecutionService(automationOrchestrator);
        // Initialize JavaFX toolkit if needed
        try {
            javafx.application.Platform.startup(() -> {});
        } catch (IllegalStateException e) {
            // Already initialized
        }
    }
    
    @Test
    void testInitialState() {
        // Then
        assertFalse(service.isRunning());
        assertFalse(service.isPaused());
        
        ImprovedExecutionService.ExecutionStateSummary summary = service.getStateSummary();
        assertFalse(summary.isRunning());
        assertFalse(summary.isPaused());
        assertTrue(summary.isStopped());
        assertEquals("Stopped", summary.getStatusText());
    }
    
    @Test
    void testExecuteTaskWhenAlreadyRunning() throws Exception {
        // Given
        TaskButton task = createTaskButton("test", "Test Task");
        
        // Simulate running state
        service.startAutomation().get(1, TimeUnit.SECONDS);
        assertTrue(service.isRunning());
        
        // When
        CompletableFuture<Boolean> result = service.executeTask(task);
        
        // Then
        assertFalse(result.get(1, TimeUnit.SECONDS));
    }
    
    @Test
    void testExecuteTaskWithoutConfirmation() throws Exception {
        // Given
        List<String> logMessages = new ArrayList<>();
        service.setLogHandler(logMessages::add);
        
        service.setConfiguration(
            ImprovedExecutionService.ExecutionConfiguration.builder()
                .confirmationEnabled(false)
                .build()
        );
        
        TaskButton task = createTaskButton("test", "Test Task");
        task.setFunctionName("testFunction");
        
        // When
        CompletableFuture<Boolean> result = service.executeTask(task);
        
        // Then
        assertTrue(result.get(2, TimeUnit.SECONDS));
        assertTrue(logMessages.stream().anyMatch(msg -> msg.contains("Executing task: Test Task")));
        assertTrue(logMessages.stream().anyMatch(msg -> msg.contains("Task completed: Test Task")));
    }
    
    @Test
    void testExecuteTaskWithNoFunction() throws Exception {
        // Given
        List<String> logMessages = new ArrayList<>();
        service.setLogHandler(logMessages::add);
        
        TaskButton task = createTaskButton("test", "Test Task");
        // No function name set
        
        // When
        CompletableFuture<Boolean> result = service.executeTask(task);
        
        // Then
        assertFalse(result.get(1, TimeUnit.SECONDS));
        assertTrue(logMessages.stream().anyMatch(msg -> msg.contains("No function defined")));
    }
    
    @Test
    void testStartAutomation() throws Exception {
        // Given
        List<String> logMessages = new ArrayList<>();
        service.setLogHandler(logMessages::add);
        
        // When
        CompletableFuture<Boolean> result = service.startAutomation();
        
        // Then
        assertTrue(result.get(1, TimeUnit.SECONDS));
        assertTrue(service.isRunning());
        assertFalse(service.isPaused());
        assertTrue(logMessages.stream().anyMatch(msg -> msg.contains("Starting automation")));
        assertTrue(logMessages.stream().anyMatch(msg -> msg.contains("Automation started")));
    }
    
    @Test
    void testStartAutomationWhenAlreadyRunning() throws Exception {
        // Given
        service.startAutomation().get(1, TimeUnit.SECONDS);
        assertTrue(service.isRunning());
        
        // When
        CompletableFuture<Boolean> result = service.startAutomation();
        
        // Then
        assertFalse(result.get(1, TimeUnit.SECONDS));
    }
    
    @Test
    void testTogglePause() throws Exception {
        // Given - Start automation first
        service.startAutomation().get(1, TimeUnit.SECONDS);
        assertTrue(service.isRunning());
        assertFalse(service.isPaused());
        
        // When - First toggle (pause)
        service.togglePause();
        
        // Then
        assertTrue(service.isRunning());
        assertTrue(service.isPaused());
        verify(automationOrchestrator).pauseAutomation();
        
        // When - Second toggle (resume)
        service.togglePause();
        
        // Then
        assertTrue(service.isRunning());
        assertFalse(service.isPaused());
        verify(automationOrchestrator).resumeAutomation();
    }
    
    @Test
    void testTogglePauseWhenNotRunning() {
        // Given
        assertFalse(service.isRunning());
        
        // When
        service.togglePause();
        
        // Then
        assertFalse(service.isPaused());
        verify(automationOrchestrator, never()).pauseAutomation();
    }
    
    @Test
    void testStopAutomation() throws Exception {
        // Given - Start automation first
        service.startAutomation().get(1, TimeUnit.SECONDS);
        assertTrue(service.isRunning());
        
        // When
        service.stopAutomation();
        
        // Then
        assertFalse(service.isRunning());
        assertFalse(service.isPaused());
        verify(automationOrchestrator).stopAllAutomation();
    }
    
    @Test
    void testStopAutomationWhenNotRunning() {
        // Given
        assertFalse(service.isRunning());
        
        // When
        service.stopAutomation();
        
        // Then
        verify(automationOrchestrator, never()).stopAllAutomation();
    }
    
    @Test
    void testStateListener() throws Exception {
        // Given
        AtomicReference<ExecutionState> lastState = new AtomicReference<>();
        AtomicBoolean lastRunning = new AtomicBoolean();
        AtomicBoolean lastPaused = new AtomicBoolean();
        
        ImprovedExecutionService.ExecutionStateListener listener = (state, running, paused) -> {
            lastState.set(state);
            lastRunning.set(running);
            lastPaused.set(paused);
        };
        
        service.addStateListener(listener);
        
        // When - Start
        service.startAutomation().get(1, TimeUnit.SECONDS);
        Thread.sleep(100); // Allow Platform.runLater to execute
        
        // Then
        assertEquals(ExecutionState.RUNNING, lastState.get());
        assertTrue(lastRunning.get());
        assertFalse(lastPaused.get());
        
        // When - Pause
        service.togglePause();
        Thread.sleep(100); // Allow Platform.runLater to execute
        
        // Then
        assertEquals(ExecutionState.PAUSED, lastState.get());
        assertTrue(lastRunning.get());
        assertTrue(lastPaused.get());
        
        // When - Stop
        service.stopAutomation();
        Thread.sleep(100); // Allow Platform.runLater to execute
        
        // Then
        assertEquals(ExecutionState.STOPPED, lastState.get());
        assertFalse(lastRunning.get());
        assertFalse(lastPaused.get());
    }
    
    @Test
    void testRemoveStateListener() throws Exception {
        // Given
        AtomicReference<ExecutionState> lastState = new AtomicReference<>();
        
        ImprovedExecutionService.ExecutionStateListener listener = (state, running, paused) -> {
            lastState.set(state);
        };
        
        service.addStateListener(listener);
        
        // Verify listener works
        service.startAutomation().get(1, TimeUnit.SECONDS);
        Thread.sleep(100);
        assertEquals(ExecutionState.RUNNING, lastState.get());
        
        // When - Remove listener
        service.removeStateListener(listener);
        lastState.set(null);
        
        service.stopAutomation();
        Thread.sleep(100);
        
        // Then - Listener should not be called
        assertNull(lastState.get());
    }
    
    @Test
    void testLogHandler() {
        // Given
        List<String> logs = new ArrayList<>();
        Consumer<String> logHandler = logs::add;
        service.setLogHandler(logHandler);
        
        // When
        service.stopAutomation(); // This logs even when not running
        
        // Then
        assertFalse(logs.isEmpty());
        assertTrue(logs.stream().anyMatch(msg -> msg.contains("Automation not running")));
    }
    
    @Test
    void testConfiguration() throws Exception {
        // Given
        ImprovedExecutionService.ExecutionConfiguration config =
            ImprovedExecutionService.ExecutionConfiguration.builder()
                .confirmationEnabled(false)
                .autoLogExecution(true)
                .defaultConfirmationTitle("Custom Title")
                .defaultConfirmationHeader("Custom Header")
                .build();
        
        service.setConfiguration(config);
        
        TaskButton task = createTaskButton("test", "Test");
        task.setFunctionName("test");
        
        // When
        CompletableFuture<Boolean> result = service.executeTask(task);
        
        // Then - Should not show confirmation
        assertTrue(result.get(2, TimeUnit.SECONDS));
    }
    
    @Test
    void testExecutionStateSummary() throws Exception {
        // Given - Initial state
        ImprovedExecutionService.ExecutionStateSummary summary = service.getStateSummary();
        assertFalse(summary.isRunning());
        assertFalse(summary.isPaused());
        assertTrue(summary.isStopped());
        assertEquals("Stopped", summary.getStatusText());
        
        // When - Running
        service.startAutomation().get(1, TimeUnit.SECONDS);
        summary = service.getStateSummary();
        
        // Then
        assertTrue(summary.isRunning());
        assertFalse(summary.isPaused());
        assertFalse(summary.isStopped());
        assertEquals("Running", summary.getStatusText());
        
        // When - Paused
        service.togglePause();
        Thread.sleep(100);
        summary = service.getStateSummary();
        
        // Then
        assertTrue(summary.isRunning());
        assertTrue(summary.isPaused());
        assertFalse(summary.isStopped());
        assertEquals("Paused", summary.getStatusText());
    }
    
    @Test
    void testTaskWithIdButNoLabel() throws Exception {
        // Given
        List<String> logs = new ArrayList<>();
        service.setLogHandler(logs::add);
        
        service.setConfiguration(
            ImprovedExecutionService.ExecutionConfiguration.builder()
                .confirmationEnabled(false)
                .build()
        );
        
        TaskButton task = new TaskButton();
        task.setId("task-id-123");
        task.setLabel(null); // No label
        task.setFunctionName("testFunction");
        
        // When
        CompletableFuture<Boolean> result = service.executeTask(task);
        
        // Then
        assertTrue(result.get(2, TimeUnit.SECONDS));
        assertTrue(logs.stream().anyMatch(msg -> msg.contains("task-id-123")));
    }
    
    private TaskButton createTaskButton(String id, String label) {
        TaskButton button = new TaskButton();
        button.setId(id);
        button.setLabel(label);
        return button;
    }
}