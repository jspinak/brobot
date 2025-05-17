package io.github.jspinak.brobot.runner.events;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class ErrorEventTest {

    @Test
    void constructor_ShouldSetAllProperties() {
        // Arrange
        Object source = new Object();
        String errorMessage = "Test error message";
        Exception exception = new RuntimeException("Test exception");
        ErrorEvent.ErrorSeverity severity = ErrorEvent.ErrorSeverity.HIGH;
        String componentName = "TestComponent";

        // Act
        ErrorEvent event = new ErrorEvent(source, errorMessage, exception, severity, componentName);

        // Assert
        assertEquals(BrobotEvent.EventType.ERROR_OCCURRED, event.getEventType());
        assertSame(source, event.getSource());
        assertEquals(errorMessage, event.getErrorMessage());
        assertSame(exception, event.getException());
        assertEquals(severity, event.getSeverity());
        assertEquals(componentName, event.getComponentName());
    }

    @Test
    void factoryMethod_Low_ShouldCreateCorrectEvent() {
        // Arrange
        Object source = new Object();
        String errorMessage = "Low severity error";
        Exception exception = new RuntimeException("Test exception");
        String componentName = "TestComponent";

        // Act
        ErrorEvent event = ErrorEvent.low(source, errorMessage, exception, componentName);

        // Assert
        assertEquals(BrobotEvent.EventType.ERROR_OCCURRED, event.getEventType());
        assertSame(source, event.getSource());
        assertEquals(errorMessage, event.getErrorMessage());
        assertSame(exception, event.getException());
        assertEquals(ErrorEvent.ErrorSeverity.LOW, event.getSeverity());
        assertEquals(componentName, event.getComponentName());
    }

    @Test
    void factoryMethod_Medium_ShouldCreateCorrectEvent() {
        // Arrange
        Object source = new Object();
        String errorMessage = "Medium severity error";
        Exception exception = new RuntimeException("Test exception");
        String componentName = "TestComponent";

        // Act
        ErrorEvent event = ErrorEvent.medium(source, errorMessage, exception, componentName);

        // Assert
        assertEquals(BrobotEvent.EventType.ERROR_OCCURRED, event.getEventType());
        assertSame(source, event.getSource());
        assertEquals(errorMessage, event.getErrorMessage());
        assertSame(exception, event.getException());
        assertEquals(ErrorEvent.ErrorSeverity.MEDIUM, event.getSeverity());
        assertEquals(componentName, event.getComponentName());
    }

    @Test
    void factoryMethod_High_ShouldCreateCorrectEvent() {
        // Arrange
        Object source = new Object();
        String errorMessage = "High severity error";
        Exception exception = new RuntimeException("Test exception");
        String componentName = "TestComponent";

        // Act
        ErrorEvent event = ErrorEvent.high(source, errorMessage, exception, componentName);

        // Assert
        assertEquals(BrobotEvent.EventType.ERROR_OCCURRED, event.getEventType());
        assertSame(source, event.getSource());
        assertEquals(errorMessage, event.getErrorMessage());
        assertSame(exception, event.getException());
        assertEquals(ErrorEvent.ErrorSeverity.HIGH, event.getSeverity());
        assertEquals(componentName, event.getComponentName());
    }

    @Test
    void factoryMethod_Fatal_ShouldCreateCorrectEvent() {
        // Arrange
        Object source = new Object();
        String errorMessage = "Fatal error";
        Exception exception = new RuntimeException("Test exception");
        String componentName = "TestComponent";

        // Act
        ErrorEvent event = ErrorEvent.fatal(source, errorMessage, exception, componentName);

        // Assert
        assertEquals(BrobotEvent.EventType.ERROR_OCCURRED, event.getEventType());
        assertSame(source, event.getSource());
        assertEquals(errorMessage, event.getErrorMessage());
        assertSame(exception, event.getException());
        assertEquals(ErrorEvent.ErrorSeverity.FATAL, event.getSeverity());
        assertEquals(componentName, event.getComponentName());
    }

    @Test
    void severityLevels_ShouldHaveCorrectOrdinalOrder() {
        // Verify that severity levels are ordered by increasing severity
        assertTrue(ErrorEvent.ErrorSeverity.LOW.ordinal() < ErrorEvent.ErrorSeverity.MEDIUM.ordinal());
        assertTrue(ErrorEvent.ErrorSeverity.MEDIUM.ordinal() < ErrorEvent.ErrorSeverity.HIGH.ordinal());
        assertTrue(ErrorEvent.ErrorSeverity.HIGH.ordinal() < ErrorEvent.ErrorSeverity.FATAL.ordinal());
    }
}