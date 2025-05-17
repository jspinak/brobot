package io.github.jspinak.brobot.runner.events;

import io.github.jspinak.brobot.runner.execution.ExecutionState;
import io.github.jspinak.brobot.runner.execution.ExecutionStatus;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class ExecutionStatusEventTest {

    @Test
    void constructor_ShouldSetAllProperties() {
        // Arrange
        Object source = new Object();
        ExecutionStatus status = new ExecutionStatus();
        status.setState(ExecutionState.RUNNING);
        status.setProgress(0.5);
        String message = "Test message";

        // Act
        ExecutionStatusEvent event = new ExecutionStatusEvent(
                BrobotEvent.EventType.EXECUTION_STARTED, source, status, message);

        // Assert
        assertEquals(BrobotEvent.EventType.EXECUTION_STARTED, event.getEventType());
        assertSame(source, event.getSource());
        assertSame(status, event.getStatus());
        assertEquals(message, event.getMessage());
        assertEquals(ExecutionState.RUNNING, event.getState());
        assertEquals(0.5, event.getProgress());
    }

    @Test
    void factoryMethod_Started_ShouldCreateCorrectEvent() {
        // Arrange
        Object source = new Object();
        ExecutionStatus status = new ExecutionStatus();
        String message = "Execution started";

        // Act
        ExecutionStatusEvent event = ExecutionStatusEvent.started(source, status, message);

        // Assert
        assertEquals(BrobotEvent.EventType.EXECUTION_STARTED, event.getEventType());
        assertSame(source, event.getSource());
        assertSame(status, event.getStatus());
        assertEquals(message, event.getMessage());
    }

    @Test
    void factoryMethod_Progress_ShouldCreateCorrectEvent() {
        // Arrange
        Object source = new Object();
        ExecutionStatus status = new ExecutionStatus();
        String message = "Execution in progress";

        // Act
        ExecutionStatusEvent event = ExecutionStatusEvent.progress(source, status, message);

        // Assert
        assertEquals(BrobotEvent.EventType.EXECUTION_PROGRESS, event.getEventType());
        assertSame(source, event.getSource());
        assertSame(status, event.getStatus());
        assertEquals(message, event.getMessage());
    }

    @Test
    void factoryMethod_Completed_ShouldCreateCorrectEvent() {
        // Arrange
        Object source = new Object();
        ExecutionStatus status = new ExecutionStatus();
        String message = "Execution completed";

        // Act
        ExecutionStatusEvent event = ExecutionStatusEvent.completed(source, status, message);

        // Assert
        assertEquals(BrobotEvent.EventType.EXECUTION_COMPLETED, event.getEventType());
        assertSame(source, event.getSource());
        assertSame(status, event.getStatus());
        assertEquals(message, event.getMessage());
    }

    @Test
    void factoryMethod_Failed_ShouldCreateCorrectEvent() {
        // Arrange
        Object source = new Object();
        ExecutionStatus status = new ExecutionStatus();
        String message = "Execution failed";

        // Act
        ExecutionStatusEvent event = ExecutionStatusEvent.failed(source, status, message);

        // Assert
        assertEquals(BrobotEvent.EventType.EXECUTION_FAILED, event.getEventType());
        assertSame(source, event.getSource());
        assertSame(status, event.getStatus());
        assertEquals(message, event.getMessage());
    }

    @Test
    void factoryMethod_Paused_ShouldCreateCorrectEvent() {
        // Arrange
        Object source = new Object();
        ExecutionStatus status = new ExecutionStatus();
        String message = "Execution paused";

        // Act
        ExecutionStatusEvent event = ExecutionStatusEvent.paused(source, status, message);

        // Assert
        assertEquals(BrobotEvent.EventType.EXECUTION_PAUSED, event.getEventType());
        assertSame(source, event.getSource());
        assertSame(status, event.getStatus());
        assertEquals(message, event.getMessage());
    }

    @Test
    void factoryMethod_Resumed_ShouldCreateCorrectEvent() {
        // Arrange
        Object source = new Object();
        ExecutionStatus status = new ExecutionStatus();
        String message = "Execution resumed";

        // Act
        ExecutionStatusEvent event = ExecutionStatusEvent.resumed(source, status, message);

        // Assert
        assertEquals(BrobotEvent.EventType.EXECUTION_RESUMED, event.getEventType());
        assertSame(source, event.getSource());
        assertSame(status, event.getStatus());
        assertEquals(message, event.getMessage());
    }

    @Test
    void factoryMethod_Stopped_ShouldCreateCorrectEvent() {
        // Arrange
        Object source = new Object();
        ExecutionStatus status = new ExecutionStatus();
        String message = "Execution stopped";

        // Act
        ExecutionStatusEvent event = ExecutionStatusEvent.stopped(source, status, message);

        // Assert
        assertEquals(BrobotEvent.EventType.EXECUTION_STOPPED, event.getEventType());
        assertSame(source, event.getSource());
        assertSame(status, event.getStatus());
        assertEquals(message, event.getMessage());
    }
}