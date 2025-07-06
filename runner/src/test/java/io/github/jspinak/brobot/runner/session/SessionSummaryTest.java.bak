package io.github.jspinak.brobot.runner.session;

import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;

import static org.junit.jupiter.api.Assertions.*;

public class SessionSummaryTest {

    @Test
    public void testGetFormattedDuration_WithStartAndEndTime() {
        SessionSummary summary = new SessionSummary();
        summary.setStartTime(LocalDateTime.of(2024, 1, 1, 10, 0, 0));
        summary.setEndTime(LocalDateTime.of(2024, 1, 1, 11, 30, 45));

        assertEquals("1:30:45", summary.getFormattedDuration());
    }

    @Test
    public void testGetFormattedDuration_WithStartTimeOnly() {
        SessionSummary summary = new SessionSummary();
        // Set start time to 1 hour ago
        summary.setStartTime(LocalDateTime.now().minusHours(1).minusMinutes(15).minusSeconds(30));

        String duration = summary.getFormattedDuration();

        // Should be approximately 1:15:30
        assertTrue(duration.startsWith("1:"));
        String[] parts = duration.split(":");
        assertEquals(3, parts.length);
        assertEquals("1", parts[0]);
    }

    @Test
    public void testGetFormattedDuration_WithNoStartTime() {
        SessionSummary summary = new SessionSummary();

        assertEquals("Unknown", summary.getFormattedDuration());
    }

    @Test
    public void testGetStatus_Active() {
        SessionSummary summary = new SessionSummary();
        summary.setActive(true);

        assertEquals("Active", summary.getStatus());
    }

    @Test
    public void testGetStatus_Completed() {
        SessionSummary summary = new SessionSummary();
        summary.setActive(false);
        summary.setEndTime(LocalDateTime.now());

        assertEquals("Completed", summary.getStatus());
    }

    @Test
    public void testGetStatus_Unknown() {
        SessionSummary summary = new SessionSummary();
        summary.setActive(false);
        summary.setEndTime(null);

        assertEquals("Unknown", summary.getStatus());
    }

    @Test
    public void testGettersAndSetters() {
        SessionSummary summary = new SessionSummary();
        String id = "test-session";
        String projectName = "Test Project";
        LocalDateTime startTime = LocalDateTime.of(2024, 1, 1, 10, 0, 0);
        LocalDateTime endTime = LocalDateTime.of(2024, 1, 1, 11, 0, 0);
        LocalDateTime lastSaved = LocalDateTime.of(2024, 1, 1, 10, 30, 0);

        summary.setId(id);
        summary.setProjectName(projectName);
        summary.setStartTime(startTime);
        summary.setEndTime(endTime);
        summary.setLastSaved(lastSaved);
        summary.setActive(false);

        assertEquals(id, summary.getId());
        assertEquals(projectName, summary.getProjectName());
        assertEquals(startTime, summary.getStartTime());
        assertEquals(endTime, summary.getEndTime());
        assertEquals(lastSaved, summary.getLastSaved());
        assertFalse(summary.getActive());
    }
}