package io.github.jspinak.brobot.runner.session;

import lombok.Data;

import com.fasterxml.jackson.databind.JsonNode;

import io.github.jspinak.brobot.runner.config.BrobotRunnerProperties;
import io.github.jspinak.brobot.runner.events.EventBus;
import io.github.jspinak.brobot.runner.events.LogEvent;
import io.github.jspinak.brobot.runner.json.parsing.ConfigurationParser;
import io.github.jspinak.brobot.runner.json.parsing.exception.ConfigurationException;
import io.github.jspinak.brobot.runner.resources.ResourceManager;
import io.github.jspinak.brobot.model.transition.StateTransitionStore;
import io.github.jspinak.brobot.navigation.transition.StateTransitions;

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

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@MockitoSettings(strictness = Strictness.LENIENT)
@ExtendWith(MockitoExtension.class)
@Data
public class SessionManagerTest {

    @Mock
    private EventBus eventBus;

    @Mock
    private BrobotRunnerProperties properties;

    @Mock
    private ResourceManager resourceManager;

    @Mock
    private ConfigurationParser jsonParser;

    @Mock
    private StateTransitionStore stateTransitionsRepository;

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
        assertThat(session).isNotNull();
        assertThat(session.getId()).isNotNull();
        assertThat(session.getProjectName()).isEqualTo("TestProject");
        assertThat(session.getConfigPath()).isEqualTo("/test/config");
        assertThat(session.getImagePath()).isEqualTo("/test/images");
        assertThat(session.isActive()).isTrue();
        assertThat(session.getStartTime()).isNotNull();
        assertThat(session.getEndTime()).isNull();

        // Verify session is current
        assertThat(sessionManager.isSessionActive()).isTrue();
        assertThat(sessionManager.getCurrentSession()).isEqualTo(session);

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
        assertThat(sessionManager.isSessionActive()).isFalse();
        assertThat(sessionManager.getCurrentSession()).isNull();

        // Verify session was ended properly
        assertThat(session.isActive()).isFalse();
        assertThat(session.getEndTime()).isNotNull();

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
        assertThat(foundEndEvent).as("Expected a session ended event").isTrue();
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
        assertThat(sessionManager.getLastAutosaveTime()).isNotNull();

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
        assertThat(loadedSession).isPresent();
        assertThat(loadedSession.get().getId()).isEqualTo(sessionId);
        assertThat(loadedSession.get().getProjectName()).isEqualTo("LoadedProject");

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
        assertThat(summaries).hasSize(2);

        // Sessions should be sorted by startTime (newest first)
        assertThat(summaries.getFirst().getId()).isEqualTo("def");
        assertThat(summaries.getFirst().getProjectName()).isEqualTo("Project2");
        assertThat(summaries.getFirst().getStartTime()).isEqualTo(LocalDateTime.parse("2024-01-02T12:00:00"));
        assertThat(summaries.getFirst().getStatus()).isEqualTo("Completed");

        assertThat(summaries.get(1).getId()).isEqualTo("abc");
        assertThat(summaries.get(1).getProjectName()).isEqualTo("Project1");
        assertThat(summaries.get(1).getStartTime()).isEqualTo(LocalDateTime.parse("2024-01-01T12:00:00"));
        assertThat(summaries.get(1).getStatus()).isEqualTo("Active");
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
        assertThat(success).isTrue();

        // Verify current session
        assertThat(spySessionManager.isSessionActive()).isTrue();
        assertThat(spySessionManager.getCurrentSession().getId()).isEqualTo("restore-session");
        assertThat(spySessionManager.getCurrentSession().getProjectName()).isEqualTo("RestoredProject");

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
        assertThat(foundRestoreEvent).as("Expected a session restored event").isTrue();
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
        assertThat(success).isTrue();

        // Verify file was deleted
        assertThat(Files.exists(sessionFile)).isFalse();

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
        assertThat(foundDeleteEvent).as("Expected a session deleted event").isTrue();
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
        assertThat(sessionManager.isSessionActive()).isFalse();

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