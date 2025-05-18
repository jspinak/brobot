package io.github.jspinak.brobot.runner.session;

import io.github.jspinak.brobot.manageStates.StateTransitions;
import org.junit.jupiter.api.Test;

import java.time.Duration;
import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.mock;

public class SessionTest {

    @Test
    public void testAddEvent() {
        Session session = new Session();

        // Add events
        session.addEvent(new SessionEvent("TYPE1", "Description 1"));
        session.addEvent(new SessionEvent("TYPE2", "Description 2", "Details"));

        // Verify events were added
        List<SessionEvent> events = session.getEvents();
        assertEquals(2, events.size());
        assertEquals("TYPE1", events.get(0).getType());
        assertEquals("Description 1", events.get(0).getDescription());
        assertEquals("TYPE2", events.get(1).getType());
        assertEquals("Details", events.get(1).getDetails());
    }

    @Test
    public void testAddStateData() {
        Session session = new Session();

        // Add state data
        session.addStateData("key1", "value1");
        session.addStateData("key2", 123);

        // Verify data was added
        assertEquals("value1", session.getStateData().get("key1"));
        assertEquals(123, session.getStateData().get("key2"));
    }

    @Test
    public void testGetDuration_WithStartAndEndTime() {
        Session session = new Session();
        session.setStartTime(LocalDateTime.of(2024, 1, 1, 10, 0, 0));
        session.setEndTime(LocalDateTime.of(2024, 1, 1, 11, 30, 0));

        Duration duration = session.getDuration();
        assertEquals(Duration.ofMinutes(90), duration);
    }

    @Test
    public void testGetDuration_WithStartTimeOnly() {
        Session session = new Session();
        session.setStartTime(LocalDateTime.now().minusHours(1));

        Duration duration = session.getDuration();

        // Duration should be approximately 1 hour
        assertTrue(duration.toMinutes() >= 59 && duration.toMinutes() <= 61);
    }

    @Test
    public void testGetDuration_WithNoStartTime() {
        Session session = new Session();

        Duration duration = session.getDuration();
        assertEquals(Duration.ZERO, duration);
    }

    @Test
    public void testSettersAndGetters() {
        Session session = new Session();
        String id = "test-session";
        LocalDateTime startTime = LocalDateTime.of(2024, 1, 1, 10, 0, 0);
        LocalDateTime endTime = LocalDateTime.of(2024, 1, 1, 11, 0, 0);
        String projectName = "Test Project";
        String configPath = "/test/config";
        String imagePath = "/test/images";
        boolean active = true;

        // Mock state transitions
        List<StateTransitions> stateTransitions = Arrays.asList(
                mock(StateTransitions.class),
                mock(StateTransitions.class)
        );

        // Set properties
        session.setId(id);
        session.setStartTime(startTime);
        session.setEndTime(endTime);
        session.setProjectName(projectName);
        session.setConfigPath(configPath);
        session.setImagePath(imagePath);
        session.setActive(active);
        session.setStateTransitions(stateTransitions);
        session.setActiveStateIds(new HashSet<>(Arrays.asList(1L, 2L)));

        // Verify properties
        assertEquals(id, session.getId());
        assertEquals(startTime, session.getStartTime());
        assertEquals(endTime, session.getEndTime());
        assertEquals(projectName, session.getProjectName());
        assertEquals(configPath, session.getConfigPath());
        assertEquals(imagePath, session.getImagePath());
        assertTrue(session.isActive());
        assertEquals(stateTransitions, session.getStateTransitions());
        assertEquals(2, session.getActiveStateIds().size());
        assertTrue(session.getActiveStateIds().contains(1L));
        assertTrue(session.getActiveStateIds().contains(2L));
    }
}