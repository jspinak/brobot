package io.github.jspinak.brobot.runner.events;

import io.github.jspinak.brobot.log.entities.LogEntry;
import io.github.jspinak.brobot.log.entities.LogType;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class LogEntryEventTest {

    @Test
    void constructor_ShouldSetAllProperties() {
        // Arrange
        Object source = new Object();
        LogEntry logEntry = new LogEntry();
        logEntry.setType(LogType.ACTION);
        logEntry.setDescription("Test log entry");

        // Act
        LogEntryEvent event = new LogEntryEvent(
                BrobotEvent.EventType.LOG_MESSAGE, source, logEntry);

        // Assert
        assertEquals(BrobotEvent.EventType.LOG_MESSAGE, event.getEventType());
        assertSame(source, event.getSource());
        assertSame(logEntry, event.getLogEntry());
    }

    @Test
    void factoryMethod_Created_ShouldCreateCorrectEvent() {
        // Arrange
        Object source = new Object();
        LogEntry logEntry = mock(LogEntry.class);

        // Act
        LogEntryEvent event = LogEntryEvent.created(source, logEntry);

        // Assert
        assertEquals(BrobotEvent.EventType.LOG_MESSAGE, event.getEventType());
        assertSame(source, event.getSource());
        assertSame(logEntry, event.getLogEntry());
    }
}