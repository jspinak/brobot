package io.github.jspinak.brobot.runner.session;

import static org.assertj.core.api.Assertions.*;
import static org.awaitility.Awaitility.*;
import static org.mockito.Mockito.*;

import java.nio.file.Path;
import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.test.util.ReflectionTestUtils;

import io.github.jspinak.brobot.model.transition.StateTransitionStore;
import io.github.jspinak.brobot.navigation.transition.StateTransitions;
import io.github.jspinak.brobot.runner.common.diagnostics.DiagnosticInfo;
import io.github.jspinak.brobot.runner.events.EventBus;
import io.github.jspinak.brobot.runner.resources.ResourceManager;
import io.github.jspinak.brobot.runner.session.autosave.SessionAutosaveService;
import io.github.jspinak.brobot.runner.session.discovery.SessionDiscoveryService;
import io.github.jspinak.brobot.runner.session.lifecycle.SessionLifecycleService;
import io.github.jspinak.brobot.runner.session.persistence.SessionPersistenceService;
import io.github.jspinak.brobot.runner.session.state.SessionStateService;

class SessionManagerIntegrationTest {

    private SessionManager sessionManager;

    @Mock private EventBus eventBus;

    @Mock private ResourceManager resourceManager;

    @Mock private StateTransitionStore stateTransitionStore;

    private SessionLifecycleService lifecycleService;
    private SessionPersistenceService persistenceService;
    private SessionStateService stateService;
    private SessionAutosaveService autosaveService;
    private SessionDiscoveryService discoveryService;

    @TempDir Path tempDir;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);

        // Create real service instances
        lifecycleService = new SessionLifecycleService();

        persistenceService = new SessionPersistenceService();
        ReflectionTestUtils.setField(
                persistenceService, "sessionStoragePathConfig", tempDir.toString());
        persistenceService.initialize();

        stateService = new SessionStateService(stateTransitionStore);

        autosaveService = new SessionAutosaveService();

        discoveryService = new SessionDiscoveryService();
        ReflectionTestUtils.setField(
                discoveryService, "sessionStoragePathConfig", tempDir.toString());
        discoveryService.initialize();

        // Create SessionManager with real services
        sessionManager =
                new SessionManager(
                        eventBus,
                        resourceManager,
                        lifecycleService,
                        persistenceService,
                        stateService,
                        autosaveService,
                        discoveryService);

        sessionManager.initialize();
    }

    @AfterEach
    void tearDown() {
        sessionManager.close();
    }

    @Test
    void testCompleteSessionLifecycle() {
        // Given
        String projectName = "Integration Test Project";
        String configPath = "/test/config.json";
        String imagePath = "/test/images";

        // When - Start new session
        Session session = sessionManager.startNewSession(projectName, configPath, imagePath);

        // Then
        assertThat(session).isNotNull();
        assertThat(session.getId()).isNotNull();
        assertThat(session.getProjectName()).isEqualTo(projectName);
        assertThat(session.isActive()).isTrue();
        assertThat(sessionManager.isSessionActive()).isTrue();
        assertThat(sessionManager.getCurrentSession()).isEqualTo(session);

        // Verify event was published
        verify(eventBus, atLeastOnce()).publish(any());

        // When - End session
        sessionManager.endCurrentSession();

        // Then
        assertThat(sessionManager.isSessionActive()).isFalse();
        assertThat(sessionManager.getCurrentSession()).isNull();
        assertThat(session.isActive()).isFalse();
        assertThat(session.getEndTime()).isNotNull();
    }

    @Test
    void testSessionPersistenceAndLoading() {
        // Given
        Session originalSession =
                sessionManager.startNewSession("Persistence Test", "/config.json", "/images");
        String sessionId = originalSession.getId();

        // Add some events to the session
        originalSession.addEvent(new SessionEvent("TEST_EVENT", "Test event"));
        sessionManager.saveSession(originalSession);

        sessionManager.endCurrentSession();

        // When - Load the session
        Session loadedSession = sessionManager.loadSession(sessionId).orElse(null);

        // Then
        assertThat(loadedSession).isNotNull();
        assertThat(loadedSession.getId()).isEqualTo(sessionId);
        assertThat(loadedSession.getProjectName()).isEqualTo("Persistence Test");
        assertThat(loadedSession.getEvents()).hasSizeGreaterThanOrEqualTo(1);
    }

    @Test
    void testSessionRestoration() {
        // Given
        Session originalSession =
                sessionManager.startNewSession("Restore Test", "/config.json", "/images");
        String sessionId = originalSession.getId();

        // Mock some state transitions
        List<StateTransitions> mockTransitions =
                Arrays.asList(mock(StateTransitions.class), mock(StateTransitions.class));
        when(stateTransitionStore.getAllStateTransitionsAsCopy()).thenReturn(mockTransitions);

        // Save current state
        sessionManager.saveSession(originalSession);
        sessionManager.endCurrentSession();

        // When - Restore the session
        boolean restored = sessionManager.restoreSession(sessionId);

        // Then
        assertThat(restored).isTrue();
        assertThat(sessionManager.isSessionActive()).isTrue();
        assertThat(sessionManager.getCurrentSession()).isNotNull();
        assertThat(sessionManager.getCurrentSession().getId()).isEqualTo(sessionId);

        // Verify state was restored
        verify(stateTransitionStore).emptyRepos();
        verify(stateTransitionStore, times(2)).add(any(StateTransitions.class));
    }

    @Test
    void testAutosaveIntegration() throws InterruptedException {
        // Given
        CountDownLatch autosaveLatch = new CountDownLatch(2);

        // Create a custom autosave service that counts saves
        SessionAutosaveService customAutosaveService =
                new SessionAutosaveService() {
                    @Override
                    public void enableAutosave(
                            io.github.jspinak.brobot.runner.session.context.SessionContext context,
                            java.util.function.Consumer<Session> saveHandler) {
                        super.enableAutosave(
                                context,
                                session -> {
                                    saveHandler.accept(session);
                                    autosaveLatch.countDown();
                                });
                    }
                };

        // Create SessionManager with custom autosave service
        SessionManager autoSaveManager =
                new SessionManager(
                        eventBus,
                        resourceManager,
                        lifecycleService,
                        persistenceService,
                        stateService,
                        customAutosaveService,
                        discoveryService);

        // Start session with quick autosave interval
        Session session =
                autoSaveManager.startNewSession("Autosave Test", "/config.json", "/images");

        // Then - Wait for autosaves
        assertThat(autosaveLatch.await(3, TimeUnit.SECONDS)).isTrue();

        // Verify autosave time is updated
        LocalDateTime lastAutosave = autoSaveManager.getLastAutosaveTime();
        assertThat(lastAutosave).isNotNull();

        autoSaveManager.close();
    }

    @Test
    void testSessionDiscovery() {
        // Given - Create multiple sessions
        Session session1 = sessionManager.startNewSession("Project Alpha", "/alpha.json", "/alpha");
        sessionManager.endCurrentSession();

        Session session2 = sessionManager.startNewSession("Project Beta", "/beta.json", "/beta");
        sessionManager.endCurrentSession();

        Session session3 =
                sessionManager.startNewSession("Project Alpha", "/alpha2.json", "/alpha");

        // When - Get all sessions
        List<SessionSummary> allSessions = sessionManager.getAllSessionSummaries();

        // Then
        assertThat(allSessions).hasSize(3);
        assertThat(allSessions)
                .extracting(SessionSummary::getProjectName)
                .containsExactlyInAnyOrder("Project Alpha", "Project Beta", "Project Alpha");
    }

    @Test
    void testDeleteSession() {
        // Given
        Session session = sessionManager.startNewSession("To Delete", "/delete.json", "/delete");
        String sessionId = session.getId();
        sessionManager.endCurrentSession();

        // Verify session exists
        assertThat(sessionManager.loadSession(sessionId)).isPresent();

        // When
        boolean deleted = sessionManager.deleteSession(sessionId);

        // Then
        assertThat(deleted).isTrue();
        assertThat(sessionManager.loadSession(sessionId)).isEmpty();
    }

    @Test
    void testCannotDeleteActiveSession() {
        // Given
        Session activeSession = sessionManager.startNewSession("Active", "/active.json", "/active");

        // When
        boolean deleted = sessionManager.deleteSession(activeSession.getId());

        // Then
        assertThat(deleted).isFalse();
        assertThat(sessionManager.isSessionActive()).isTrue();
    }

    @Test
    void testConcurrentSessionOperations() throws InterruptedException {
        // Given
        int threadCount = 5;
        CountDownLatch startLatch = new CountDownLatch(threadCount);
        CountDownLatch endLatch = new CountDownLatch(threadCount);
        Set<String> sessionIds = new HashSet<>();

        // When - Create sessions concurrently
        for (int i = 0; i < threadCount; i++) {
            final int index = i;
            new Thread(
                            () -> {
                                try {
                                    Session session =
                                            sessionManager.startNewSession(
                                                    "Concurrent " + index,
                                                    "/concurrent" + index + ".json",
                                                    "/concurrent" + index);
                                    synchronized (sessionIds) {
                                        sessionIds.add(session.getId());
                                    }
                                    startLatch.countDown();

                                    // Do some work
                                    Thread.sleep(100);

                                    sessionManager.endCurrentSession();
                                    endLatch.countDown();
                                } catch (Exception e) {
                                    e.printStackTrace();
                                }
                            })
                    .start();
        }

        // Then
        assertThat(startLatch.await(5, TimeUnit.SECONDS)).isTrue();
        assertThat(endLatch.await(5, TimeUnit.SECONDS)).isTrue();

        // Only one session should be active at the end
        assertThat(sessionManager.isSessionActive()).isFalse();

        // All sessions should have been created
        assertThat(sessionIds).hasSize(threadCount);
    }

    @Test
    void testDiagnosticMode() {
        // Given
        sessionManager.startNewSession("Diagnostic Test", "/diag.json", "/diag");

        // When
        sessionManager.enableDiagnosticMode(true);
        DiagnosticInfo diagnosticInfo = sessionManager.getDiagnosticInfo();

        // Then
        assertThat(sessionManager.isDiagnosticModeEnabled()).isTrue();
        assertThat(diagnosticInfo.getComponent()).isEqualTo("SessionManager");
        assertThat(diagnosticInfo.getStates())
                .containsKeys(
                        "lifecycle",
                        "persistence",
                        "state",
                        "autosave",
                        "discovery",
                        "activeSession",
                        "currentSessionId");
        assertThat((Boolean) diagnosticInfo.getStates().get("activeSession")).isTrue();

        // Verify all services have diagnostic mode enabled
        assertThat(lifecycleService.isDiagnosticModeEnabled()).isTrue();
        assertThat(persistenceService.isDiagnosticModeEnabled()).isTrue();
        assertThat(stateService.isDiagnosticModeEnabled()).isTrue();
        assertThat(autosaveService.isDiagnosticModeEnabled()).isTrue();
        assertThat(discoveryService.isDiagnosticModeEnabled()).isTrue();
    }

    @Test
    void testStateCaptureDuringAutosave() {
        // Given
        List<StateTransitions> mockTransitions =
                Arrays.asList(mock(StateTransitions.class), mock(StateTransitions.class));
        when(stateTransitionStore.getAllStateTransitionsAsCopy()).thenReturn(mockTransitions);

        Session session =
                sessionManager.startNewSession("State Capture Test", "/state.json", "/state");

        // When - Trigger manual autosave
        sessionManager.autosaveCurrentSession();

        // Then
        verify(stateTransitionStore, atLeastOnce()).getAllStateTransitionsAsCopy();
        assertThat(session.getStateTransitions()).hasSize(2);
    }

    @Test
    void testCompleteWorkflow_CreateSaveRestoreDelete() {
        // Step 1: Create and save session
        Session originalSession =
                sessionManager.startNewSession("Complete Workflow", "/workflow.json", "/workflow");
        String sessionId = originalSession.getId();

        originalSession.addEvent(new SessionEvent("WORKFLOW_START", "Starting workflow"));
        sessionManager.saveSession(originalSession);

        // Step 2: End session
        sessionManager.endCurrentSession();
        assertThat(sessionManager.isSessionActive()).isFalse();

        // Step 3: Verify in discovery
        List<SessionSummary> sessions = sessionManager.getAllSessionSummaries();
        assertThat(sessions).anyMatch(s -> s.getId().equals(sessionId));

        // Step 4: Restore session
        boolean restored = sessionManager.restoreSession(sessionId);
        assertThat(restored).isTrue();
        assertThat(sessionManager.getCurrentSession().getId()).isEqualTo(sessionId);

        // Step 5: End restored session
        sessionManager.endCurrentSession();

        // Step 6: Delete session
        boolean deleted = sessionManager.deleteSession(sessionId);
        assertThat(deleted).isTrue();

        // Step 7: Verify deletion
        assertThat(sessionManager.loadSession(sessionId)).isEmpty();
        sessions = sessionManager.getAllSessionSummaries();
        assertThat(sessions).noneMatch(s -> s.getId().equals(sessionId));
    }
}
