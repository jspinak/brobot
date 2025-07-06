package io.github.jspinak.brobot.runner.events;

import lombok.Data;

import io.github.jspinak.brobot.runner.execution.ExecutionState;
import io.github.jspinak.brobot.runner.execution.ExecutionStatus;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.function.Consumer;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@Data
class ExecutionEventPublisherTest {

    @Mock
    private EventBus eventBus;

    private ExecutionEventPublisher publisher;
    private ExecutionStatus status;

    @BeforeEach
    void setUp() {
        publisher = new ExecutionEventPublisher(eventBus);
        status = new ExecutionStatus();
    }

    @Test
    void getStatusConsumer_ShouldReturnConsumer() {
        // Act
        Consumer<ExecutionStatus> consumer = publisher.getStatusConsumer();

        // Assert
        assertNotNull(consumer);
    }

    @Test
    void onStatusUpdate_WithStateChange_ShouldPublishStateChangeEvent() {
        // Arrange
        status.setState(ExecutionState.RUNNING);

        // Act
        publisher.onStatusUpdate(status);

        // Assert
        verify(eventBus, times(2)).publish(any(ExecutionStatusEvent.class));
    }

    @Test
    void onStatusUpdate_WithNoStateChange_ShouldNotPublishStateChangeEvent() {
        // Arrange
        status.setState(ExecutionState.RUNNING);

        // First update to set the state
        publisher.onStatusUpdate(status);
        reset(eventBus);

        // Act - update with the same state
        publisher.onStatusUpdate(status);

        // Assert - no new state change event
        verify(eventBus, never()).publish(argThat(event ->
                event instanceof ExecutionStatusEvent &&
                        ((ExecutionStatusEvent) event).getEventType() == BrobotEvent.EventType.EXECUTION_STARTED));
    }

    @Test
    void onStatusUpdate_WithProgressChange_ShouldPublishProgressEvent() {
        // Arrange
        status.setState(ExecutionState.RUNNING);
        status.setProgress(0.0);

        // First update to set the initial state
        publisher.onStatusUpdate(status);
        reset(eventBus);

        // Act - update with significant progress change
        status.setProgress(0.1);
        publisher.onStatusUpdate(status);

        // Assert
        verify(eventBus, times(1)).publish(argThat(event ->
                event instanceof ExecutionStatusEvent &&
                        ((ExecutionStatusEvent) event).getEventType() == BrobotEvent.EventType.EXECUTION_PROGRESS));
    }

    @Test
    void onStatusUpdate_WithSmallProgressChange_ShouldNotPublishProgressEvent() {
        // Arrange
        status.setState(ExecutionState.RUNNING);
        status.setProgress(0.0);

        // First update to set the initial state
        publisher.onStatusUpdate(status);
        reset(eventBus);

        // Act - update with insignificant progress change
        status.setProgress(0.01); // Less than the 0.05 threshold
        publisher.onStatusUpdate(status);

        // Assert
        verify(eventBus, never()).publish(argThat(event ->
                event instanceof ExecutionStatusEvent &&
                        ((ExecutionStatusEvent) event).getEventType() == BrobotEvent.EventType.EXECUTION_PROGRESS));
    }

    @Test
    void onStatusUpdate_WithErrorState_ShouldPublishErrorEvent() {
        // Arrange
        status.setState(ExecutionState.ERROR);
        Exception exception = new RuntimeException("Test exception");
        status.setError(exception);

        // Act
        publisher.onStatusUpdate(status);

        // Assert - should publish both execution failed and error events
        verify(eventBus, times(1)).publish(argThat(event ->
                event instanceof ExecutionStatusEvent &&
                        ((ExecutionStatusEvent) event).getEventType() == BrobotEvent.EventType.EXECUTION_FAILED));

        verify(eventBus, times(1)).publish(argThat(event ->
                event instanceof ErrorEvent &&
                        ((ErrorEvent) event).getEventType() == BrobotEvent.EventType.ERROR_OCCURRED));
    }

    @Test
    void publishExecutionEvent_ShouldDelegateToEventBus() {
        // Arrange
        ExecutionStatusEvent event = ExecutionStatusEvent.started(this, status, "Test event");

        // Act
        publisher.publishExecutionEvent(event);

        // Assert
        verify(eventBus, times(1)).publish(event);
    }

    @Test
    void onStatusUpdate_StateComplete_ShouldPublishCompleteEvent() {
        // Arrange
        status.setState(ExecutionState.COMPLETED);

        // Act
        publisher.onStatusUpdate(status);

        // Assert
        ArgumentCaptor<BrobotEvent> eventCaptor = ArgumentCaptor.forClass(BrobotEvent.class);
        verify(eventBus, times(2)).publish(eventCaptor.capture());

        boolean hasCompletedEvent = eventCaptor.getAllValues().stream()
                .anyMatch(event -> event instanceof ExecutionStatusEvent &&
                        event.getEventType() == BrobotEvent.EventType.EXECUTION_COMPLETED);
        assertTrue(hasCompletedEvent, "Should publish EXECUTION_COMPLETED event");
    }

    @Test
    void onStatusUpdate_StatePaused_ShouldPublishPausedEvent() {
        // Arrange
        status.setState(ExecutionState.PAUSED);

        // Act
        publisher.onStatusUpdate(status);

        // Assert
        ArgumentCaptor<BrobotEvent> eventCaptor = ArgumentCaptor.forClass(BrobotEvent.class);
        verify(eventBus, times(2)).publish(eventCaptor.capture());

        boolean hasCompletedEvent = eventCaptor.getAllValues().stream()
                .anyMatch(event -> event instanceof ExecutionStatusEvent &&
                        event.getEventType() == BrobotEvent.EventType.EXECUTION_PAUSED);
        assertTrue(hasCompletedEvent, "Should publish EXECUTION_PAUSED event");
    }

    @Test
    void onStatusUpdate_StateStopped_ShouldPublishStoppedEvent() {
        // Arrange
        status.setState(ExecutionState.STOPPED);

        // Act
        publisher.onStatusUpdate(status);

        // Assert
        ArgumentCaptor<BrobotEvent> eventCaptor = ArgumentCaptor.forClass(BrobotEvent.class);
        verify(eventBus, times(2)).publish(eventCaptor.capture());

        boolean hasCompletedEvent = eventCaptor.getAllValues().stream()
                .anyMatch(event -> event instanceof ExecutionStatusEvent &&
                        event.getEventType() == BrobotEvent.EventType.EXECUTION_STOPPED);
        assertTrue(hasCompletedEvent, "Should publish EXECUTION_STOPPED event");
    }
}