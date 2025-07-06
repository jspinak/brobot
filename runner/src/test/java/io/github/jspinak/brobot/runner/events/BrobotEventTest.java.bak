package io.github.jspinak.brobot.runner.events;

import org.junit.jupiter.api.Test;

import java.time.Instant;

import static org.junit.jupiter.api.Assertions.*;

class BrobotEventTest {

    /**
     * Test implementation of BrobotEvent for testing
     */
    private static class TestEvent extends BrobotEvent {
        public TestEvent(EventType eventType, Object source) {
            super(eventType, source);
        }
    }

    @Test
    void constructor_ShouldSetTimestampAndProperties() {
        // Arrange
        Object source = new Object();
        BrobotEvent.EventType eventType = BrobotEvent.EventType.LOG_MESSAGE;
        Instant before = Instant.now();

        // Act
        TestEvent event = new TestEvent(eventType, source);
        Instant after = Instant.now();

        // Assert
        assertNotNull(event.getTimestamp());
        assertTrue(event.getTimestamp().compareTo(before) >= 0, "Timestamp should be after or equal to the before time");
        assertTrue(event.getTimestamp().compareTo(after) <= 0, "Timestamp should be before or equal to the after time");
        assertEquals(eventType, event.getEventType());
        assertSame(source, event.getSource());
    }

    @Test
    void eventTypesExist() {
        // Assert that important event types are defined
        assertNotNull(BrobotEvent.EventType.EXECUTION_STARTED);
        assertNotNull(BrobotEvent.EventType.EXECUTION_COMPLETED);
        assertNotNull(BrobotEvent.EventType.EXECUTION_FAILED);
        assertNotNull(BrobotEvent.EventType.ERROR_OCCURRED);
        assertNotNull(BrobotEvent.EventType.LOG_MESSAGE);
        assertNotNull(BrobotEvent.EventType.CONFIG_LOADED);
    }
}