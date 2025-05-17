package io.github.jspinak.brobot.runner.events;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class LogEventTest {

    @Test
    void constructor_ShouldSetAllProperties() {
        // Arrange
        Object source = new Object();
        String message = "Test log message";
        LogEvent.LogLevel level = LogEvent.LogLevel.INFO;
        String category = "Test";
        Exception exception = new RuntimeException("Test exception");

        // Act
        LogEvent event = new LogEvent(
                BrobotEvent.EventType.LOG_MESSAGE, source, message, level, category, exception);

        // Assert
        assertEquals(BrobotEvent.EventType.LOG_MESSAGE, event.getEventType());
        assertSame(source, event.getSource());
        assertEquals(message, event.getMessage());
        assertEquals(level, event.getLevel());
        assertEquals(category, event.getCategory());
        assertSame(exception, event.getException());
    }

    @Test
    void factoryMethod_Debug_ShouldCreateCorrectEvent() {
        // Arrange
        Object source = new Object();
        String message = "Debug message";
        String category = "Test";

        // Act
        LogEvent event = LogEvent.debug(source, message, category);

        // Assert
        assertEquals(BrobotEvent.EventType.LOG_MESSAGE, event.getEventType());
        assertSame(source, event.getSource());
        assertEquals(message, event.getMessage());
        assertEquals(LogEvent.LogLevel.DEBUG, event.getLevel());
        assertEquals(category, event.getCategory());
        assertNull(event.getException());
    }

    @Test
    void factoryMethod_Info_ShouldCreateCorrectEvent() {
        // Arrange
        Object source = new Object();
        String message = "Info message";
        String category = "Test";

        // Act
        LogEvent event = LogEvent.info(source, message, category);

        // Assert
        assertEquals(BrobotEvent.EventType.LOG_MESSAGE, event.getEventType());
        assertSame(source, event.getSource());
        assertEquals(message, event.getMessage());
        assertEquals(LogEvent.LogLevel.INFO, event.getLevel());
        assertEquals(category, event.getCategory());
        assertNull(event.getException());
    }

    @Test
    void factoryMethod_Warning_ShouldCreateCorrectEvent() {
        // Arrange
        Object source = new Object();
        String message = "Warning message";
        String category = "Test";

        // Act
        LogEvent event = LogEvent.warning(source, message, category);

        // Assert
        assertEquals(BrobotEvent.EventType.LOG_WARNING, event.getEventType());
        assertSame(source, event.getSource());
        assertEquals(message, event.getMessage());
        assertEquals(LogEvent.LogLevel.WARNING, event.getLevel());
        assertEquals(category, event.getCategory());
        assertNull(event.getException());
    }

    @Test
    void factoryMethod_Error_ShouldCreateCorrectEvent() {
        // Arrange
        Object source = new Object();
        String message = "Error message";
        String category = "Test";
        Exception exception = new RuntimeException("Test exception");

        // Act
        LogEvent event = LogEvent.error(source, message, category, exception);

        // Assert
        assertEquals(BrobotEvent.EventType.LOG_ERROR, event.getEventType());
        assertSame(source, event.getSource());
        assertEquals(message, event.getMessage());
        assertEquals(LogEvent.LogLevel.ERROR, event.getLevel());
        assertEquals(category, event.getCategory());
        assertSame(exception, event.getException());
    }

    @Test
    void factoryMethod_Critical_ShouldCreateCorrectEvent() {
        // Arrange
        Object source = new Object();
        String message = "Critical message";
        String category = "Test";
        Exception exception = new RuntimeException("Test exception");

        // Act
        LogEvent event = LogEvent.critical(source, message, category, exception);

        // Assert
        assertEquals(BrobotEvent.EventType.LOG_ERROR, event.getEventType());
        assertSame(source, event.getSource());
        assertEquals(message, event.getMessage());
        assertEquals(LogEvent.LogLevel.CRITICAL, event.getLevel());
        assertEquals(category, event.getCategory());
        assertSame(exception, event.getException());
    }

    @Test
    void logLevels_ShouldHaveCorrectOrdinalOrder() {
        // Verify that log levels are ordered by increasing severity
        assertTrue(LogEvent.LogLevel.DEBUG.ordinal() < LogEvent.LogLevel.INFO.ordinal());
        assertTrue(LogEvent.LogLevel.INFO.ordinal() < LogEvent.LogLevel.WARNING.ordinal());
        assertTrue(LogEvent.LogLevel.WARNING.ordinal() < LogEvent.LogLevel.ERROR.ordinal());
        assertTrue(LogEvent.LogLevel.ERROR.ordinal() < LogEvent.LogLevel.CRITICAL.ordinal());
    }
}