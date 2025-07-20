package io.github.jspinak.brobot.runner.ui.automation.services;

import io.github.jspinak.brobot.runner.automation.AutomationOrchestrator;
import io.github.jspinak.brobot.runner.events.EventBus;
import io.github.jspinak.brobot.runner.events.ExecutionStatusEvent;
import io.github.jspinak.brobot.runner.execution.ExecutionState;
import io.github.jspinak.brobot.runner.execution.ExecutionStatus;
import io.github.jspinak.brobot.runner.project.TaskButton;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class BasicAutomationControlServiceTest {
    
    @Mock
    private AutomationOrchestrator automationOrchestrator;
    
    @Mock
    private EventBus eventBus;
    
    @Mock
    private ExecutionStatus executionStatus;
    
    private BasicAutomationControlService service;
    
    @BeforeEach
    void setUp() {
        service = new BasicAutomationControlService(automationOrchestrator, eventBus);
        // Initialize JavaFX toolkit if needed
        try {
            javafx.application.Platform.startup(() -> {});
        } catch (IllegalStateException e) {
            // Already initialized
        }
    }
    
    @Test
    void testGetExecutionStatus() {
        // Given
        when(automationOrchestrator.getExecutionStatus()).thenReturn(executionStatus);
        
        // When
        ExecutionStatus result = service.getExecutionStatus();
        
        // Then
        assertEquals(executionStatus, result);
    }
    
    @Test
    void testGetExecutionStateWhenStatusNull() {
        // Given
        when(automationOrchestrator.getExecutionStatus()).thenReturn(null);
        
        // When
        ExecutionState state = service.getExecutionState();
        
        // Then
        assertEquals(ExecutionState.IDLE, state);
    }
    
    @Test
    void testGetExecutionState() {
        // Given
        when(automationOrchestrator.getExecutionStatus()).thenReturn(executionStatus);
        when(executionStatus.getState()).thenReturn(ExecutionState.RUNNING);
        
        // When
        ExecutionState state = service.getExecutionState();
        
        // Then
        assertEquals(ExecutionState.RUNNING, state);
    }
    
    @Test
    void testIsAutomationActive() {
        // Given - Active state
        when(automationOrchestrator.getExecutionStatus()).thenReturn(executionStatus);
        when(executionStatus.getState()).thenReturn(ExecutionState.RUNNING);
        when(executionStatus.getState().isActive()).thenReturn(true);
        
        // When/Then
        assertTrue(service.isAutomationActive());
        
        // Given - Inactive state
        when(executionStatus.getState()).thenReturn(ExecutionState.IDLE);
        when(executionStatus.getState().isActive()).thenReturn(false);
        
        // When/Then
        assertFalse(service.isAutomationActive());
    }
    
    @Test
    void testIsAutomationPaused() {
        // Given - Paused
        when(automationOrchestrator.getExecutionStatus()).thenReturn(executionStatus);
        when(executionStatus.getState()).thenReturn(ExecutionState.PAUSED);
        
        // When/Then
        assertTrue(service.isAutomationPaused());
        
        // Given - Not paused
        when(executionStatus.getState()).thenReturn(ExecutionState.RUNNING);
        
        // When/Then
        assertFalse(service.isAutomationPaused());
    }
    
    @Test
    void testRunAutomationWhenAlreadyActive() {
        // Given
        List<String> logs = new ArrayList<>();
        service.setLogHandler(logs::add);
        
        when(automationOrchestrator.getExecutionStatus()).thenReturn(executionStatus);
        when(executionStatus.getState()).thenReturn(ExecutionState.RUNNING);
        when(executionStatus.getState().isActive()).thenReturn(true);
        
        TaskButton button = createTaskButton("test", "Test Button");
        
        // When
        service.runAutomation(button);
        
        // Then
        verify(automationOrchestrator, never()).executeAutomation(any());
        assertEquals(1, logs.size());
        assertTrue(logs.get(0).contains("already running"));
    }
    
    @Test
    void testRunAutomationWithoutConfirmation() {
        // Given
        service.setConfiguration(
            BasicAutomationControlService.ControlConfiguration.builder()
                .confirmationEnabled(false)
                .build()
        );
        
        when(automationOrchestrator.getExecutionStatus()).thenReturn(executionStatus);
        when(executionStatus.getState()).thenReturn(ExecutionState.IDLE);
        when(executionStatus.getState().isActive()).thenReturn(false);
        
        TaskButton button = createTaskButton("test", "Test Button");
        
        // When
        service.runAutomation(button);
        
        // Then
        verify(automationOrchestrator).executeAutomation(button);
    }
    
    @Test
    void testRunAutomationWithEventPublishing() {
        // Given
        service.setConfiguration(
            BasicAutomationControlService.ControlConfiguration.builder()
                .confirmationEnabled(false)
                .publishEvents(true)
                .build()
        );
        
        when(automationOrchestrator.getExecutionStatus()).thenReturn(executionStatus);
        when(executionStatus.getState()).thenReturn(ExecutionState.IDLE);
        when(executionStatus.getState().isActive()).thenReturn(false);
        
        TaskButton button = createTaskButton("test", "Test Button");
        
        // When
        service.runAutomation(button);
        
        // Then
        ArgumentCaptor<ExecutionStatusEvent> eventCaptor = ArgumentCaptor.forClass(ExecutionStatusEvent.class);
        verify(eventBus).publish(eventCaptor.capture());
        
        ExecutionStatusEvent event = eventCaptor.getValue();
        assertEquals(service, event.getSource());
        assertTrue(event.getMessage().contains("Starting automation: Test Button"));
    }
    
    @Test
    void testTogglePauseResume() {
        // Given - Running state
        when(automationOrchestrator.getExecutionStatus()).thenReturn(executionStatus);
        when(executionStatus.getState()).thenReturn(ExecutionState.RUNNING);
        
        // When
        service.togglePauseResume();
        
        // Then
        verify(automationOrchestrator).pauseAutomation();
        
        // Given - Paused state
        when(executionStatus.getState()).thenReturn(ExecutionState.PAUSED);
        
        // When
        service.togglePauseResume();
        
        // Then
        verify(automationOrchestrator).resumeAutomation();
    }
    
    @Test
    void testPauseAutomation() {
        // Given - Active and not paused
        when(automationOrchestrator.getExecutionStatus()).thenReturn(executionStatus);
        when(executionStatus.getState()).thenReturn(ExecutionState.RUNNING);
        when(executionStatus.getState().isActive()).thenReturn(true);
        
        // When
        service.pauseAutomation();
        
        // Then
        verify(automationOrchestrator).pauseAutomation();
    }
    
    @Test
    void testPauseAutomationWhenNotActive() {
        // Given
        when(automationOrchestrator.getExecutionStatus()).thenReturn(executionStatus);
        when(executionStatus.getState()).thenReturn(ExecutionState.IDLE);
        when(executionStatus.getState().isActive()).thenReturn(false);
        
        // When
        service.pauseAutomation();
        
        // Then
        verify(automationOrchestrator, never()).pauseAutomation();
    }
    
    @Test
    void testResumeAutomation() {
        // Given
        when(automationOrchestrator.getExecutionStatus()).thenReturn(executionStatus);
        when(executionStatus.getState()).thenReturn(ExecutionState.PAUSED);
        
        // When
        service.resumeAutomation();
        
        // Then
        verify(automationOrchestrator).resumeAutomation();
    }
    
    @Test
    void testResumeAutomationWhenNotPaused() {
        // Given
        when(automationOrchestrator.getExecutionStatus()).thenReturn(executionStatus);
        when(executionStatus.getState()).thenReturn(ExecutionState.RUNNING);
        
        // When
        service.resumeAutomation();
        
        // Then
        verify(automationOrchestrator, never()).resumeAutomation();
    }
    
    @Test
    void testStopAllAutomation() {
        // Given
        when(automationOrchestrator.getExecutionStatus()).thenReturn(executionStatus);
        when(executionStatus.getState()).thenReturn(ExecutionState.RUNNING);
        when(executionStatus.getState().isActive()).thenReturn(true);
        
        // When
        service.stopAllAutomation();
        
        // Then
        verify(automationOrchestrator).stopAllAutomation();
    }
    
    @Test
    void testStopAllAutomationWhenNotActive() {
        // Given
        List<String> logs = new ArrayList<>();
        service.setLogHandler(logs::add);
        
        when(automationOrchestrator.getExecutionStatus()).thenReturn(executionStatus);
        when(executionStatus.getState()).thenReturn(ExecutionState.IDLE);
        when(executionStatus.getState().isActive()).thenReturn(false);
        
        // When
        service.stopAllAutomation();
        
        // Then
        verify(automationOrchestrator, never()).stopAllAutomation();
        assertEquals(1, logs.size());
        assertTrue(logs.get(0).contains("No automation is currently running"));
    }
    
    @Test
    void testGetPauseResumeButtonText() {
        // Given - Paused
        when(automationOrchestrator.getExecutionStatus()).thenReturn(executionStatus);
        when(executionStatus.getState()).thenReturn(ExecutionState.PAUSED);
        
        // When/Then
        assertEquals("Resume Execution", service.getPauseResumeButtonText());
        
        // Given - Not paused
        when(executionStatus.getState()).thenReturn(ExecutionState.RUNNING);
        
        // When/Then
        assertEquals("Pause Execution", service.getPauseResumeButtonText());
    }
    
    @Test
    void testIsPauseResumeEnabled() {
        // Given - Running
        when(automationOrchestrator.getExecutionStatus()).thenReturn(executionStatus);
        when(executionStatus.getState()).thenReturn(ExecutionState.RUNNING);
        
        // When/Then
        assertTrue(service.isPauseResumeEnabled());
        
        // Given - Paused
        when(executionStatus.getState()).thenReturn(ExecutionState.PAUSED);
        
        // When/Then
        assertTrue(service.isPauseResumeEnabled());
        
        // Given - Idle
        when(executionStatus.getState()).thenReturn(ExecutionState.IDLE);
        
        // When/Then
        assertFalse(service.isPauseResumeEnabled());
    }
    
    @Test
    void testGetStatusMessage() {
        // Given - With current operation
        when(automationOrchestrator.getExecutionStatus()).thenReturn(executionStatus);
        when(executionStatus.getCurrentOperation()).thenReturn("Processing item 5");
        
        // When/Then
        assertEquals("Processing item 5", service.getStatusMessage());
        
        // Given - Without current operation
        when(executionStatus.getCurrentOperation()).thenReturn(null);
        when(executionStatus.getState()).thenReturn(ExecutionState.RUNNING);
        when(executionStatus.getState().getDescription()).thenReturn("Running");
        
        // When/Then
        assertEquals("Running", service.getStatusMessage());
        
        // Given - Null status
        when(automationOrchestrator.getExecutionStatus()).thenReturn(null);
        
        // When/Then
        assertEquals("Ready", service.getStatusMessage());
    }
    
    @Test
    void testGetProgress() {
        // Given
        when(automationOrchestrator.getExecutionStatus()).thenReturn(executionStatus);
        when(executionStatus.getProgress()).thenReturn(0.75);
        
        // When/Then
        assertEquals(0.75, service.getProgress());
        
        // Given - Null status
        when(automationOrchestrator.getExecutionStatus()).thenReturn(null);
        
        // When/Then
        assertEquals(0.0, service.getProgress());
    }
    
    @Test
    void testConfiguration() {
        // Given
        BasicAutomationControlService.ControlConfiguration config = 
            BasicAutomationControlService.ControlConfiguration.builder()
                .confirmationEnabled(true)
                .confirmationTitle("Custom Title")
                .defaultConfirmationMessage("Custom Message")
                .publishEvents(false)
                .build();
        
        service.setConfiguration(config);
        
        // When - Run without events
        when(automationOrchestrator.getExecutionStatus()).thenReturn(executionStatus);
        when(executionStatus.getState()).thenReturn(ExecutionState.IDLE);
        when(executionStatus.getState().isActive()).thenReturn(false);
        
        TaskButton button = createTaskButton("test", "Test");
        service.runAutomation(button);
        
        // Then - No events published
        verify(eventBus, never()).publish(any());
    }
    
    @Test
    void testLogHandler() {
        // Given
        List<String> logs = new ArrayList<>();
        Consumer<String> logHandler = logs::add;
        service.setLogHandler(logHandler);
        
        // When
        when(automationOrchestrator.getExecutionStatus()).thenReturn(executionStatus);
        when(executionStatus.getState()).thenReturn(ExecutionState.IDLE);
        when(executionStatus.getState().isActive()).thenReturn(false);
        
        service.stopAllAutomation();
        
        // Then
        assertFalse(logs.isEmpty());
        assertTrue(logs.stream().anyMatch(log -> log.contains("No automation")));
    }
    
    private TaskButton createTaskButton(String id, String label) {
        TaskButton button = new TaskButton();
        button.setId(id);
        button.setLabel(label);
        return button;
    }
}