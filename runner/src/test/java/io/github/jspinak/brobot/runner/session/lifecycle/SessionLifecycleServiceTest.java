package io.github.jspinak.brobot.runner.session.lifecycle;

import static org.assertj.core.api.Assertions.*;

import java.time.LocalDateTime;
import java.util.Optional;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import io.github.jspinak.brobot.runner.common.diagnostics.DiagnosticInfo;
import io.github.jspinak.brobot.runner.session.Session;
import io.github.jspinak.brobot.runner.session.context.SessionContext;
import io.github.jspinak.brobot.runner.session.context.SessionOptions;

class SessionLifecycleServiceTest {

    private SessionLifecycleService lifecycleService;

    @BeforeEach
    void setUp() {
        lifecycleService = new SessionLifecycleService();
    }

    @Test
    void testStartSession_CreatesNewSession() {
        // Given
        SessionContext context =
                SessionContext.builder()
                        .sessionName("Test Session")
                        .projectName("Test Project")
                        .configPath("/config/test.json")
                        .imagePath("/images")
                        .options(SessionOptions.defaultOptions())
                        .build();

        // When
        Session session = lifecycleService.startSession(context);

        // Then
        assertThat(session).isNotNull();
        assertThat(session.getId()).isEqualTo(context.getSessionId());
        assertThat(session.getProjectName()).isEqualTo("Test Project");
        assertThat(session.getConfigPath()).isEqualTo("/config/test.json");
        assertThat(session.getImagePath()).isEqualTo("/images");
        assertThat(session.isActive()).isTrue();
        assertThat(session.getStartTime()).isNotNull();
    }

    @Test
    void testStartSession_FromLegacyMethod() {
        // When
        SessionContext context =
                lifecycleService.startSession(
                        "Test Session",
                        "Test Project",
                        "/config/test.json",
                        "/images",
                        SessionOptions.defaultOptions());

        // Then
        assertThat(context).isNotNull();
        assertThat(context.getSessionName()).isEqualTo("Test Session");
        assertThat(context.getProjectName()).isEqualTo("Test Project");
        assertThat(lifecycleService.isSessionActive(context.getSessionId())).isTrue();
    }

    @Test
    void testEndSession_MarksSessionInactive() {
        // Given
        SessionContext context =
                SessionContext.builder()
                        .sessionName("Test Session")
                        .projectName("Test Project")
                        .configPath("/config/test.json")
                        .imagePath("/images")
                        .options(SessionOptions.defaultOptions())
                        .build();
        Session session = lifecycleService.startSession(context);

        // When
        lifecycleService.endSession(session.getId());

        // Then
        assertThat(lifecycleService.isSessionActive(session.getId())).isFalse();
        assertThat(session.isActive()).isFalse();
        assertThat(session.getEndTime()).isNotNull();
    }

    @Test
    void testEndSession_NonExistentSession_HandlesGracefully() {
        // When/Then - should not throw
        assertThatCode(() -> lifecycleService.endSession("non-existent-id"))
                .doesNotThrowAnyException();
    }

    @Test
    void testIsSessionActive_ReturnsTrueForActiveSession() {
        // Given
        SessionContext context =
                SessionContext.builder()
                        .sessionName("Test Session")
                        .projectName("Test Project")
                        .configPath("/config/test.json")
                        .imagePath("/images")
                        .options(SessionOptions.defaultOptions())
                        .build();
        Session session = lifecycleService.startSession(context);

        // When/Then
        assertThat(lifecycleService.isSessionActive(session.getId())).isTrue();
        assertThat(lifecycleService.isSessionActive()).isTrue();
    }

    @Test
    void testGetCurrentSession_ReturnsLatestActiveSession() {
        // Given
        SessionContext context1 =
                SessionContext.builder()
                        .sessionName("Session 1")
                        .projectName("Project 1")
                        .configPath("/config1.json")
                        .imagePath("/images1")
                        .options(SessionOptions.defaultOptions())
                        .build();

        SessionContext context2 =
                SessionContext.builder()
                        .sessionName("Session 2")
                        .projectName("Project 2")
                        .configPath("/config2.json")
                        .imagePath("/images2")
                        .options(SessionOptions.defaultOptions())
                        .build();

        lifecycleService.startSession(context1);
        Session session2 = lifecycleService.startSession(context2);

        // When
        Optional<Session> currentSession = lifecycleService.getCurrentSession();

        // Then
        assertThat(currentSession).isPresent();
        assertThat(currentSession.get().getId()).isEqualTo(session2.getId());
    }

    @Test
    void testActivateSession_ReplacesCurrentSession() {
        // Given
        SessionContext context =
                SessionContext.builder()
                        .sessionName("Original Session")
                        .projectName("Original Project")
                        .configPath("/original.json")
                        .imagePath("/original")
                        .options(SessionOptions.defaultOptions())
                        .build();
        lifecycleService.startSession(context);

        Session restoredSession = new Session();
        restoredSession.setId("restored-session-id");
        restoredSession.setProjectName("Restored Project");
        restoredSession.setConfigPath("/restored.json");
        restoredSession.setImagePath("/restored");
        restoredSession.setStartTime(LocalDateTime.now().minusHours(1));

        // When
        lifecycleService.activateSession(restoredSession);

        // Then
        assertThat(lifecycleService.getCurrentSession()).isPresent();
        assertThat(lifecycleService.getCurrentSession().get().getId())
                .isEqualTo("restored-session-id");
        assertThat(restoredSession.isActive()).isTrue();
    }

    @Test
    void testActivateSession_WithNullSession_ThrowsException() {
        assertThatThrownBy(() -> lifecycleService.activateSession(null))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("Session and session ID must not be null");
    }

    @Test
    void testStateTransitions() {
        // Given
        SessionContext context =
                SessionContext.builder()
                        .sessionName("Test Session")
                        .projectName("Test Project")
                        .configPath("/config.json")
                        .imagePath("/images")
                        .options(SessionOptions.defaultOptions())
                        .build();
        Session session = lifecycleService.startSession(context);

        // When - Valid transition
        SessionLifecycleService.SessionTransition pauseResult =
                lifecycleService.transitionTo(
                        session.getId(), SessionLifecycleService.SessionState.PAUSED);

        // Then
        assertThat(pauseResult.success()).isTrue();
        assertThat(lifecycleService.getSessionState(session.getId()))
                .isEqualTo(SessionLifecycleService.SessionState.PAUSED);

        // When - Invalid transition
        SessionLifecycleService.SessionTransition invalidResult =
                lifecycleService.transitionTo(
                        session.getId(), SessionLifecycleService.SessionState.ACTIVE);

        // Then
        assertThat(invalidResult.success()).isFalse();
        assertThat(invalidResult.message()).contains("Invalid transition");
    }

    @Test
    void testConcurrentSessionCreation() throws InterruptedException {
        // Given
        int threadCount = 10;
        ExecutorService executor = Executors.newFixedThreadPool(threadCount);
        CountDownLatch latch = new CountDownLatch(threadCount);

        // When
        for (int i = 0; i < threadCount; i++) {
            final int index = i;
            executor.submit(
                    () -> {
                        try {
                            SessionContext context =
                                    SessionContext.builder()
                                            .sessionName("Session " + index)
                                            .projectName("Project " + index)
                                            .configPath("/config" + index + ".json")
                                            .imagePath("/images" + index)
                                            .options(SessionOptions.defaultOptions())
                                            .build();
                            lifecycleService.startSession(context);
                        } finally {
                            latch.countDown();
                        }
                    });
        }

        // Then
        assertThat(latch.await(5, TimeUnit.SECONDS)).isTrue();
        assertThat(lifecycleService.getActiveSessions()).hasSize(threadCount);

        executor.shutdown();
    }

    @Test
    void testDiagnosticInfo() {
        // Given
        SessionContext context =
                SessionContext.builder()
                        .sessionName("Test Session")
                        .projectName("Test Project")
                        .configPath("/config.json")
                        .imagePath("/images")
                        .options(SessionOptions.defaultOptions())
                        .build();
        lifecycleService.startSession(context);

        // When
        DiagnosticInfo diagnosticInfo = lifecycleService.getDiagnosticInfo();

        // Then
        assertThat(diagnosticInfo.getComponent()).isEqualTo("SessionLifecycleService");
        assertThat(diagnosticInfo.getStates())
                .containsKeys("activeSessions", "totalSessionsStarted", "totalSessionsEnded");
        assertThat(diagnosticInfo.getStates().get("activeSessions")).isEqualTo(1);
    }

    @Test
    void testDiagnosticMode() {
        // Given/When
        lifecycleService.enableDiagnosticMode(true);

        // Then
        assertThat(lifecycleService.isDiagnosticModeEnabled()).isTrue();

        // When
        lifecycleService.enableDiagnosticMode(false);

        // Then
        assertThat(lifecycleService.isDiagnosticModeEnabled()).isFalse();
    }
}
