package io.github.jspinak.brobot.runner.session;

import com.fasterxml.jackson.databind.JsonNode;
import io.github.jspinak.brobot.json.parsing.JsonParser;
import io.github.jspinak.brobot.json.parsing.exception.ConfigurationException;
import io.github.jspinak.brobot.manageStates.StateTransitions;
import io.github.jspinak.brobot.runner.config.BrobotRunnerProperties;
import io.github.jspinak.brobot.runner.events.EventBus;
import io.github.jspinak.brobot.runner.events.LogEvent;
import io.github.jspinak.brobot.runner.resources.ResourceManager;
import io.github.jspinak.brobot.services.StateTransitionsRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.api.io.TempDir;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@MockitoSettings(strictness = Strictness.LENIENT)
@ExtendWith(MockitoExtension.class)
public class SessionManagerTest {

    @Mock
    private EventBus eventBus;

    @Mock
    private BrobotRunnerProperties properties;

    @Mock
    private ResourceManager resourceManager;

    @Mock
    private JsonParser jsonParser;

    @Mock
    private StateTransitionsRepository stateTransitionsRepository;

    @TempDir
    Path tempDir;

    private SessionManager sessionManager;
    private Path sessionsPath;

    @BeforeEach
    public void setup() throws IOException {
        // Configure paths
        sessionsPath = tempDir.resolve("sessions");
        Files.createDirectories(sessionsPath);

        when(properties.getLogPath()).thenReturn(tempDir.toString());

        // Create session manager
        sessionManager = new SessionManager(
                eventBus, properties, resourceManager, jsonParser, stateTransitionsRepository);

        // Initialize the manager
        sessionManager.initialize();

        // Verify resources are registered
        verify(resourceManager).registerResource(eq(sessionManager), anyString());
    }

    @Test
    public void testStartNewSession() throws ConfigurationException {
        // Mock json parser
        when(jsonParser.toPrettyJsonSafe(any(Session.class))).thenReturn("{}");

        // Start a new session
        Session session = sessionManager.startNewSession("TestProject", "/test/config", "/test/images");

        // Verify session properties
        assertNotNull(session);
        assertNotNull(session.getId());
        assertEquals("TestProject", session.getProjectName());
        assertEquals("/test/config", session.getConfigPath());
        assertEquals("/test/images", session.getImagePath());
        assertTrue(session.isActive());
        assertNotNull(session.getStartTime());
        assertNull(session.getEndTime());

        // Verify session is current
        assertTrue(sessionManager.isSessionActive());
        assertEquals(session, sessionManager.getCurrentSession());

        // Verify events published
        verify(eventBus, atLeastOnce()).publish(any(LogEvent.class));
    }

    @Test
    public void testEndCurrentSession() throws ConfigurationException {
        // Mock json parser
        when(jsonParser.toPrettyJsonSafe(any(Session.class))).thenReturn("{}");

        // Start and then end a session
        Session session = sessionManager.startNewSession("TestProject", "/test/config", "/test/images");
        sessionManager.endCurrentSession();

        // Verify session is not active
        assertFalse(sessionManager.isSessionActive());
        assertNull(sessionManager.getCurrentSession());

        // Verify session was ended properly
        assertFalse(session.isActive());
        assertNotNull(session.getEndTime());

        // Verify events published
        ArgumentCaptor<LogEvent> eventCaptor = ArgumentCaptor.forClass(LogEvent.class);
        verify(eventBus, atLeastOnce()).publish(eventCaptor.capture());

        boolean foundEndEvent = false;
        for (LogEvent event : eventCaptor.getAllValues()) {
            if (event.getMessage().contains("Session ended")) {
                foundEndEvent = true;
                break;
            }
        }
        assertTrue(foundEndEvent, "Expected a session ended event");
    }

    @Test
    public void testAutosaveCurrentSession() throws ConfigurationException {
        // Mock json parser
        when(jsonParser.toPrettyJsonSafe(any(Session.class))).thenReturn("{}");

        // Start a session
        Session session = sessionManager.startNewSession("TestProject", "/test/config", "/test/images");

        // Clear invocations to track only the autosave events
        clearInvocations(eventBus, jsonParser);

        // Trigger autosave
        sessionManager.autosaveCurrentSession();

        // Verify session was saved
        verify(jsonParser).toPrettyJsonSafe(eq(session));

        // Verify lastAutosaveTime was updated
        assertNotNull(sessionManager.getLastAutosaveTime());

        // Verify events published
        verify(eventBus, atLeastOnce()).publish(any(LogEvent.class));
    }

    @Test
    public void testLoadSession() throws ConfigurationException, IOException {
        // Create a test session file
        String sessionId = "test-session";
        Path sessionFile = sessionsPath.resolve("session_" + sessionId + "_20240101_120000.json");
        Files.writeString(sessionFile, "{\"id\":\"" + sessionId + "\"}");

        // Mock json parser
        Session mockSession = new Session();
        mockSession.setId(sessionId);
        mockSession.setProjectName("LoadedProject");
        when(jsonParser.convertJson(anyString(), eq(Session.class))).thenReturn(mockSession);

        // Load the session
        Optional<Session> loadedSession = sessionManager.loadSession(sessionId);

        // Verify session was loaded
        assertTrue(loadedSession.isPresent());
        assertEquals(sessionId, loadedSession.get().getId());
        assertEquals("LoadedProject", loadedSession.get().getProjectName());

        // Verify events published
        verify(eventBus, atLeastOnce()).publish(any(LogEvent.class));
    }

    @Test
    public void testGetAllSessionSummaries() throws IOException, ConfigurationException {
        // Create test session files
        Path sessionFile1 = sessionsPath.resolve("session_abc_20240101_120000.json");
        Path sessionFile2 = sessionsPath.resolve("session_def_20240102_120000.json");

        Files.writeString(sessionFile1, "{\"id\":\"abc\",\"projectName\":\"Project1\"}");
        Files.writeString(sessionFile2, "{\"id\":\"def\",\"projectName\":\"Project2\"}");

        // Mock json parser
        JsonNode node1 = mock(JsonNode.class);
        JsonNode node2 = mock(JsonNode.class);

        when(jsonParser.parseJson(contains("abc"))).thenReturn(node1);
        when(jsonParser.parseJson(contains("def"))).thenReturn(node2);

        // Mock node values
        mockJsonNodeValue(node1, "projectName", "Project1");
        mockJsonNodeValue(node1, "startTime", "2024-01-01T12:00:00");
        mockJsonNodeValue(node1, "active", "true");
        mockJsonNodeValue(node1, "endTime", null);

        mockJsonNodeValue(node2, "projectName", "Project2");
        mockJsonNodeValue(node2, "startTime", "2024-01-02T12:00:00");
        mockJsonNodeValue(node2, "active", "false");
        mockJsonNodeValue(node2, "endTime", "2024-01-02T13:00:00");

        // Get session summaries
        List<SessionSummary> summaries = sessionManager.getAllSessionSummaries();

        // Verify summaries
        assertEquals(2, summaries.size());

        // Sessions should be sorted by startTime (newest first)
        assertEquals("def", summaries.getFirst().getId());
        assertEquals("Project2", summaries.getFirst().getProjectName());
        assertEquals(LocalDateTime.parse("2024-01-02T12:00:00"), summaries.getFirst().getStartTime());
        assertEquals("Completed", summaries.getFirst().getStatus());

        assertEquals("abc", summaries.get(1).getId());
        assertEquals("Project1", summaries.get(1).getProjectName());
        assertEquals(LocalDateTime.parse("2024-01-01T12:00:00"), summaries.get(1).getStartTime());
        assertEquals("Active", summaries.get(1).getStatus());
    }

    @Test
    public void testRestoreSession() throws ConfigurationException, IOException {
        // Mock json parser for saving
        when(jsonParser.toPrettyJsonSafe(any(Session.class))).thenReturn("{}");

        // Mock loadSession to return a test session
        Session mockSession = new Session();
        mockSession.setId("restore-session");
        mockSession.setProjectName("RestoredProject");
        mockSession.setConfigPath("/restored/config");
        mockSession.setImagePath("/restored/images");
        mockSession.setActive(true);

        List<StateTransitions> mockTransitions = Arrays.asList(
                mock(StateTransitions.class),
                mock(StateTransitions.class)
        );
        mockSession.setStateTransitions(mockTransitions);

        SessionManager spySessionManager = spy(sessionManager);
        doReturn(Optional.of(mockSession)).when(spySessionManager).loadSession("restore-session");

        // Restore the session
        boolean success = spySessionManager.restoreSession("restore-session");

        // Verify success
        assertTrue(success);

        // Verify current session
        assertTrue(spySessionManager.isSessionActive());
        assertEquals("restore-session", spySessionManager.getCurrentSession().getId());
        assertEquals("RestoredProject", spySessionManager.getCurrentSession().getProjectName());

        // Verify state transitions were restored
        verify(stateTransitionsRepository).emptyRepos();
        verify(stateTransitionsRepository, times(2)).add(any(StateTransitions.class));

        // Verify events published
        ArgumentCaptor<LogEvent> eventCaptor = ArgumentCaptor.forClass(LogEvent.class);
        verify(eventBus, atLeastOnce()).publish(eventCaptor.capture());

        boolean foundRestoreEvent = false;
        for (LogEvent event : eventCaptor.getAllValues()) {
            if (event.getMessage().contains("Session restored")) {
                foundRestoreEvent = true;
                break;
            }
        }
        assertTrue(foundRestoreEvent, "Expected a session restored event");
    }

    @Test
    public void testDeleteSession() throws IOException {
        // Create a test session file
        String sessionId = "delete-session";
        Path sessionFile = sessionsPath.resolve("session_" + sessionId + "_20240101_120000.json");
        Files.writeString(sessionFile, "{\"id\":\"" + sessionId + "\"}");

        // Delete the session
        boolean success = sessionManager.deleteSession(sessionId);

        // Verify success
        assertTrue(success);

        // Verify file was deleted
        assertFalse(Files.exists(sessionFile));

        // Verify events published
        ArgumentCaptor<LogEvent> eventCaptor = ArgumentCaptor.forClass(LogEvent.class);
        verify(eventBus, atLeastOnce()).publish(eventCaptor.capture());

        boolean foundDeleteEvent = false;
        for (LogEvent event : eventCaptor.getAllValues()) {
            if (event.getMessage().contains("Session deleted")) {
                foundDeleteEvent = true;
                break;
            }
        }
        assertTrue(foundDeleteEvent, "Expected a session deleted event");
    }

    @Test
    public void testClose() throws ConfigurationException {
        // Mock json parser
        when(jsonParser.toPrettyJsonSafe(any(Session.class))).thenReturn("{}");

        // Start a session
        sessionManager.startNewSession("TestProject", "/test/config", "/test/images");

        // Clear invocations
        clearInvocations(eventBus, jsonParser);

        // Close the manager
        sessionManager.close();

        // Verify session was saved (endCurrentSession was called)
        verify(jsonParser).toPrettyJsonSafe(any(Session.class));

        // Verify no active session
        assertFalse(sessionManager.isSessionActive());

        // Verify events published
        verify(eventBus, atLeastOnce()).publish(any(LogEvent.class));
    }

    // Helper method to mock JSON node values
    private void mockJsonNodeValue(JsonNode node, String field, String value) {
        JsonNode fieldNode = mock(JsonNode.class);
        when(node.has(field)).thenReturn(true);
        when(node.get(field)).thenReturn(fieldNode);
        when(fieldNode.isNull()).thenReturn(false);
        when(fieldNode.asText()).thenReturn(value);
        if (field.equals("active")) {
            when(fieldNode.asBoolean()).thenReturn(Boolean.parseBoolean(value));
        }
    }
}