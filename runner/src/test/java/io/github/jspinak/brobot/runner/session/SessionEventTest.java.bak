package io.github.jspinak.brobot.runner.session;

import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;

import static org.junit.jupiter.api.Assertions.*;

public class SessionEventTest {

    @Test
    public void testDefaultConstructor() {
        SessionEvent event = new SessionEvent();

        assertNull(event.getType());
        assertNull(event.getDescription());
        assertNull(event.getDetails());
        assertNotNull(event.getTimestamp());

        // Timestamp should be close to now
        LocalDateTime now = LocalDateTime.now();
        assertTrue(event.getTimestamp().isAfter(now.minusSeconds(1)));
        assertTrue(event.getTimestamp().isBefore(now.plusSeconds(1)));
    }

    @Test
    public void testTypeAndDescriptionConstructor() {
        SessionEvent event = new SessionEvent("TEST", "Test description");

        assertEquals("TEST", event.getType());
        assertEquals("Test description", event.getDescription());
        assertNull(event.getDetails());
        assertNotNull(event.getTimestamp());
    }

    @Test
    public void testFullConstructor() {
        SessionEvent event = new SessionEvent("TEST", "Test description", "Test details");

        assertEquals("TEST", event.getType());
        assertEquals("Test description", event.getDescription());
        assertEquals("Test details", event.getDetails());
        assertNotNull(event.getTimestamp());
    }

    @Test
    public void testSetters() {
        SessionEvent event = new SessionEvent();
        LocalDateTime timestamp = LocalDateTime.now().minusHours(1);

        event.setType("UPDATED");
        event.setDescription("Updated description");
        event.setDetails("Updated details");
        event.setTimestamp(timestamp);

        assertEquals("UPDATED", event.getType());
        assertEquals("Updated description", event.getDescription());
        assertEquals("Updated details", event.getDetails());
        assertEquals(timestamp, event.getTimestamp());
    }
}