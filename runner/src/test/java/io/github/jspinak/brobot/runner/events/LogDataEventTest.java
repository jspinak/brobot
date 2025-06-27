package io.github.jspinak.brobot.runner.events;

import org.junit.jupiter.api.Test;

import io.github.jspinak.brobot.tools.logging.model.LogData;
import io.github.jspinak.brobot.tools.logging.model.LogEventType;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class LogDataEventTest {

    @Test
    void constructor_ShouldSetAllProperties() {
        // Arrange
        Object source = new Object();
        LogData logData = new LogData();
        logData.setType(LogEventType.ACTION);
        logData.setDescription("Test log entry");

        // Act
        LogEntryEvent event = new LogEntryEvent(
                BrobotEvent.EventType.LOG_MESSAGE, source, logData);

        // Assert
        assertEquals(BrobotEvent.EventType.LOG_MESSAGE, event.getEventType());
        assertSame(source, event.getSource());
        assertSame(logData, event.getLogEntry());
    }

    @Test
    void factoryMethod_Created_ShouldCreateCorrectEvent() {
        // Arrange
        Object source = new Object();
        LogData logData = mock(LogData.class);

        // Act
        LogEntryEvent event = LogEntryEvent.created(source, logData);

        // Assert
        assertEquals(BrobotEvent.EventType.LOG_MESSAGE, event.getEventType());
        assertSame(source, event.getSource());
        assertSame(logData, event.getLogEntry());
    }
}