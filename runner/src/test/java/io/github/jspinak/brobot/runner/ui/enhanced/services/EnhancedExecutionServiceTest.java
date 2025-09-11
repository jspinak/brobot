package io.github.jspinak.brobot.runner.ui.enhanced.services;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicReference;
import javafx.application.Platform;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import io.github.jspinak.brobot.runner.automation.AutomationOrchestrator;
import io.github.jspinak.brobot.runner.events.EventBus;
import io.github.jspinak.brobot.runner.events.ExecutionStatusEvent;
import io.github.jspinak.brobot.runner.execution.ExecutionState;
import io.github.jspinak.brobot.runner.execution.ExecutionStatus;
import io.github.jspinak.brobot.runner.project.TaskButton;

@ExtendWith(MockitoExtension.class)
class EnhancedExecutionServiceTest {

    @Mock private AutomationOrchestrator automationOrchestrator;

    @Mock private EventBus eventBus;

    @Mock private ExecutionStatus executionStatus;

    private EnhancedExecutionService service;

    @BeforeEach
    void setUp() {
        service = new EnhancedExecutionService(automationOrchestrator, eventBus);

        // Initialize JavaFX toolkit if needed
        try {
            Platform.startup(() -> {});
        } catch (IllegalStateException e) {
            // Already initialized
        }
    }

    @Test
    void testRunAutomationWhenAlreadyActive() {
        // Given
        AtomicReference<String> logMessage = new AtomicReference<>();
        service.setLogHandler(logMessage::set);

        when(automationOrchestrator.getExecutionStatus()).thenReturn(executionStatus);
        when(executionStatus.getState()).thenReturn(ExecutionState.RUNNING);
        when(executionStatus.getState().isActive()).thenReturn(true);

        TaskButton button = createTaskButton("test", "Test Button");

        // When
        boolean started = service.runAutomation(button, null);

        // Then
        assertFalse(started);
        verify(automationOrchestrator, never()).executeAutomation(any());
        assertTrue(logMessage.get().contains("already running"));
    }

    @Test
    void testRunAutomationWithoutConfirmation() {
        // Given
        service.setConfiguration(
                EnhancedExecutionService.ExecutionConfiguration.builder()
                        .confirmationEnabled(false)
                        .build());

        when(automationOrchestrator.getExecutionStatus()).thenReturn(executionStatus);
        when(executionStatus.getState()).thenReturn(ExecutionState.IDLE);
        when(executionStatus.getState().isActive()).thenReturn(false);

        TaskButton button = createTaskButton("test", "Test Button");
        AtomicBoolean preExecutionCalled = new AtomicBoolean(false);

        // When
        boolean started = service.runAutomation(button, () -> preExecutionCalled.set(true));

        // Then
        assertTrue(started);
        assertTrue(preExecutionCalled.get());
        verify(automationOrchestrator).executeAutomation(button);
    }

    @Test
    void testRunAutomationWithEventPublishing() {
        // Given
        service.setConfiguration(
                EnhancedExecutionService.ExecutionConfiguration.builder()
                        .confirmationEnabled(false)
                        .publishEvents(true)
                        .build());

        when(automationOrchestrator.getExecutionStatus()).thenReturn(executionStatus);
        when(executionStatus.getState()).thenReturn(ExecutionState.IDLE);
        when(executionStatus.getState().isActive()).thenReturn(false);

        TaskButton button = createTaskButton("test", "Test Button");

        // When
        service.runAutomation(button, null);

        // Then
        ArgumentCaptor<ExecutionStatusEvent> eventCaptor =
                ArgumentCaptor.forClass(ExecutionStatusEvent.class);
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

        AtomicReference<String> logMessage = new AtomicReference<>();
        service.setLogHandler(logMessage::set);

        // When
        service.pauseAutomation();

        // Then
        verify(automationOrchestrator).pauseAutomation();
        assertTrue(logMessage.get().contains("Pausing automation"));
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

        AtomicReference<String> logMessage = new AtomicReference<>();
        service.setLogHandler(logMessage::set);

        // When
        service.resumeAutomation();

        // Then
        verify(automationOrchestrator).resumeAutomation();
        assertTrue(logMessage.get().contains("Resuming automation"));
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

        AtomicBoolean postStopCalled = new AtomicBoolean(false);

        // When
        service.stopAllAutomation(() -> postStopCalled.set(true));

        // Then
        verify(automationOrchestrator).stopAllAutomation();
        assertTrue(postStopCalled.get());
    }

    @Test
    void testStopAllAutomationWhenNotActive() {
        // Given
        AtomicReference<String> logMessage = new AtomicReference<>();
        service.setLogHandler(logMessage::set);

        when(automationOrchestrator.getExecutionStatus()).thenReturn(executionStatus);
        when(executionStatus.getState()).thenReturn(ExecutionState.IDLE);
        when(executionStatus.getState().isActive()).thenReturn(false);

        // When
        service.stopAllAutomation(null);

        // Then
        verify(automationOrchestrator, never()).stopAllAutomation();
        assertTrue(logMessage.get().contains("No automation is currently running"));
    }

    @Test
    void testGetExecutionStatus() {
        // Given
        when(automationOrchestrator.getExecutionStatus()).thenReturn(executionStatus);

        // When
        ExecutionStatus status = service.getExecutionStatus();

        // Then
        assertEquals(executionStatus, status);
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
    void testGetExecutionStateWhenStatusNull() {
        // Given
        when(automationOrchestrator.getExecutionStatus()).thenReturn(null);

        // When
        ExecutionState state = service.getExecutionState();

        // Then
        assertEquals(ExecutionState.IDLE, state);
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
    void testConfiguration() {
        // Given
        EnhancedExecutionService.ExecutionConfiguration config =
                EnhancedExecutionService.ExecutionConfiguration.builder()
                        .confirmationEnabled(false)
                        .confirmationTitle("Custom Title")
                        .defaultConfirmationMessage("Custom Message")
                        .publishEvents(false)
                        .checkConcurrency(false)
                        .build();

        service.setConfiguration(config);

        // When - Run without events or concurrency check
        when(automationOrchestrator.getExecutionStatus()).thenReturn(executionStatus);
        when(executionStatus.getState()).thenReturn(ExecutionState.RUNNING);
        when(executionStatus.getState().isActive()).thenReturn(true);

        TaskButton button = createTaskButton("test", "Test");
        service.runAutomation(button, null);

        // Then - Should execute despite running state (no concurrency check)
        verify(automationOrchestrator).executeAutomation(button);
        verify(eventBus, never()).publish(any()); // No events
    }

    private TaskButton createTaskButton(String id, String label) {
        TaskButton button = new TaskButton();
        button.setId(id);
        button.setLabel(label);
        return button;
    }
}
