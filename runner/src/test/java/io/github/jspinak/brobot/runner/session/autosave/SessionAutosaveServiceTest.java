package io.github.jspinak.brobot.runner.session.autosave;

import io.github.jspinak.brobot.runner.common.diagnostics.DiagnosticInfo;
import io.github.jspinak.brobot.runner.session.Session;
import io.github.jspinak.brobot.runner.session.context.SessionContext;
import io.github.jspinak.brobot.runner.session.context.SessionOptions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.AfterEach;

import java.time.Duration;
import java.time.LocalDateTime;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.Consumer;

import static org.assertj.core.api.Assertions.*;
import static org.awaitility.Awaitility.*;

class SessionAutosaveServiceTest {

    private SessionAutosaveService autosaveService;

    @BeforeEach
    void setUp() {
        autosaveService = new SessionAutosaveService();
    }

    @AfterEach
    void tearDown() {
        autosaveService.shutdown();
    }

    @Test
    void testEnableAutosave_SchedulesPeriodicSave() throws InterruptedException {
        // Given
        CountDownLatch saveLatch = new CountDownLatch(2); // Expect at least 2 saves
        AtomicInteger saveCount = new AtomicInteger(0);
        
        SessionContext context = SessionContext.builder()
                .sessionId("test-session-1")
                .sessionName("Test Session")
                .projectName("Test Project")
                .configPath("/config.json")
                .imagePath("/images")
                .options(SessionOptions.builder()
                        .autosaveEnabled(true)
                        .autosaveInterval(Duration.ofMillis(100)) // Quick interval for testing
                        .build())
                .build();

        Consumer<Session> saveHandler = session -> {
            saveCount.incrementAndGet();
            saveLatch.countDown();
        };

        // When
        autosaveService.enableAutosave(context, saveHandler);

        // Then
        assertThat(saveLatch.await(1, TimeUnit.SECONDS)).isTrue();
        assertThat(saveCount.get()).isGreaterThanOrEqualTo(2);
        
        SessionAutosaveService.AutosaveStatus status = autosaveService.getStatus(context.getSessionId());
        assertThat(status).isNotNull();
        assertThat(status.isEnabled()).isTrue();
        assertThat(status.getSaveCount()).isGreaterThanOrEqualTo(2);
    }

    @Test
    void testEnableAutosave_WithDisabledOption_DoesNotSchedule() {
        // Given
        SessionContext context = SessionContext.builder()
                .sessionId("test-session-2")
                .sessionName("Test Session")
                .projectName("Test Project")
                .configPath("/config.json")
                .imagePath("/images")
                .options(SessionOptions.builder()
                        .autosaveEnabled(false)
                        .build())
                .build();

        Consumer<Session> saveHandler = session -> fail("Should not be called");

        // When
        autosaveService.enableAutosave(context, saveHandler);

        // Then
        SessionAutosaveService.AutosaveStatus status = autosaveService.getStatus(context.getSessionId());
        assertThat(status).isNull();
        assertThat(autosaveService.getAllAutosaveStatuses()).isEmpty();
    }

    @Test
    void testEnableAutosave_WithNullInputs_ThrowsException() {
        // Given
        SessionContext context = SessionContext.builder()
                .sessionId("test-session")
                .options(SessionOptions.defaultOptions())
                .build();

        // When/Then
        assertThatThrownBy(() -> autosaveService.enableAutosave(null, session -> {}))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("Context and saveHandler must not be null");

        assertThatThrownBy(() -> autosaveService.enableAutosave(context, null))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("Context and saveHandler must not be null");
    }

    @Test
    void testDisableAutosave_StopsScheduledSaves() throws InterruptedException {
        // Given
        AtomicInteger saveCount = new AtomicInteger(0);
        SessionContext context = SessionContext.builder()
                .sessionId("test-session-3")
                .sessionName("Test Session")
                .projectName("Test Project")
                .configPath("/config.json")
                .imagePath("/images")
                .options(SessionOptions.builder()
                        .autosaveEnabled(true)
                        .autosaveInterval(Duration.ofMillis(100))
                        .build())
                .build();

        autosaveService.enableAutosave(context, session -> saveCount.incrementAndGet());
        
        // Wait for at least one save
        await().atMost(Duration.ofSeconds(1))
                .until(() -> saveCount.get() > 0);
        
        int countBeforeDisable = saveCount.get();

        // When
        autosaveService.disableAutosave(context.getSessionId());

        // Then - wait and verify no more saves occur
        Thread.sleep(300);
        assertThat(saveCount.get()).isEqualTo(countBeforeDisable);
        
        SessionAutosaveService.AutosaveStatus status = autosaveService.getStatus(context.getSessionId());
        assertThat(status).isNotNull();
        assertThat(status.isEnabled()).isFalse();
    }

    @Test
    void testTriggerAutosave_ManuallyInvokesHandler() {
        // Given
        AtomicInteger saveCount = new AtomicInteger(0);
        SessionContext context = SessionContext.builder()
                .sessionId("test-session-4")
                .sessionName("Test Session")
                .projectName("Test Project")
                .configPath("/config.json")
                .imagePath("/images")
                .options(SessionOptions.builder()
                        .autosaveEnabled(true)
                        .autosaveInterval(Duration.ofHours(1)) // Long interval
                        .build())
                .build();

        autosaveService.enableAutosave(context, session -> saveCount.incrementAndGet());

        // When
        boolean triggered = autosaveService.triggerAutosave(context.getSessionId(), 
                session -> saveCount.incrementAndGet());

        // Then
        assertThat(triggered).isTrue();
        assertThat(saveCount.get()).isEqualTo(1); // Only manual trigger
    }

    @Test
    void testTriggerAutosave_NonExistentSession_ReturnsFalse() {
        // When
        boolean triggered = autosaveService.triggerAutosave("non-existent", session -> {});

        // Then
        assertThat(triggered).isFalse();
    }

    @Test
    void testGetAllAutosaveStatuses() {
        // Given
        for (int i = 0; i < 3; i++) {
            SessionContext context = SessionContext.builder()
                    .sessionId("session-" + i)
                    .sessionName("Session " + i)
                    .projectName("Project " + i)
                    .configPath("/config" + i + ".json")
                    .imagePath("/images" + i)
                    .options(SessionOptions.builder()
                            .autosaveEnabled(true)
                            .autosaveInterval(Duration.ofMinutes(5))
                            .build())
                    .build();
            autosaveService.enableAutosave(context, session -> {});
        }

        // When
        Map<String, SessionAutosaveService.AutosaveStatus> statuses = autosaveService.getAllAutosaveStatuses();

        // Then
        assertThat(statuses).hasSize(3);
        assertThat(statuses).containsKeys("session-0", "session-1", "session-2");
        statuses.values().forEach(status -> {
            assertThat(status.isEnabled()).isTrue();
            assertThat(status.getInterval()).isEqualTo(Duration.ofMinutes(5));
        });
    }

    @Test
    void testAutosaveErrorHandling() throws InterruptedException {
        // Given
        CountDownLatch errorLatch = new CountDownLatch(1);
        AtomicInteger successCount = new AtomicInteger(0);
        
        SessionContext context = SessionContext.builder()
                .sessionId("test-session-error")
                .sessionName("Test Session")
                .projectName("Test Project")
                .configPath("/config.json")
                .imagePath("/images")
                .options(SessionOptions.builder()
                        .autosaveEnabled(true)
                        .autosaveInterval(Duration.ofMillis(100))
                        .build())
                .build();

        Consumer<Session> saveHandler = session -> {
            if (successCount.getAndIncrement() == 0) {
                // First call succeeds
                return;
            }
            // Second call throws exception
            errorLatch.countDown();
            throw new RuntimeException("Simulated save error");
        };

        // When
        autosaveService.enableAutosave(context, saveHandler);

        // Then
        assertThat(errorLatch.await(1, TimeUnit.SECONDS)).isTrue();
        
        SessionAutosaveService.AutosaveStatus status = autosaveService.getStatus(context.getSessionId());
        assertThat(status).isNotNull();
        assertThat(status.getSaveCount()).isGreaterThanOrEqualTo(1);
        assertThat(status.getFailureCount()).isGreaterThanOrEqualTo(1);
    }

    @Test
    void testShutdown_CancelsAllTasks() {
        // Given
        for (int i = 0; i < 3; i++) {
            SessionContext context = SessionContext.builder()
                    .sessionId("shutdown-session-" + i)
                    .sessionName("Session " + i)
                    .projectName("Project " + i)
                    .configPath("/config.json")
                    .imagePath("/images")
                    .options(SessionOptions.builder()
                            .autosaveEnabled(true)
                            .autosaveInterval(Duration.ofSeconds(1))
                            .build())
                    .build();
            autosaveService.enableAutosave(context, session -> {});
        }

        assertThat(autosaveService.getAllAutosaveStatuses()).hasSize(3);

        // When
        autosaveService.shutdown();

        // Then - all tasks should be cancelled
        autosaveService.getAllAutosaveStatuses().values().forEach(status -> {
            assertThat(status.isEnabled()).isFalse();
        });
    }

    @Test
    void testDiagnosticInfo() {
        // Given
        SessionContext context = SessionContext.builder()
                .sessionId("diag-session")
                .sessionName("Diagnostic Session")
                .projectName("Diagnostic Project")
                .configPath("/config.json")
                .imagePath("/images")
                .options(SessionOptions.builder()
                        .autosaveEnabled(true)
                        .autosaveInterval(Duration.ofMinutes(5))
                        .build())
                .build();
        
        autosaveService.enableAutosave(context, session -> {});
        autosaveService.triggerAutosave(context.getSessionId(), session -> {});

        // When
        DiagnosticInfo diagnosticInfo = autosaveService.getDiagnosticInfo();

        // Then
        assertThat(diagnosticInfo.getComponent()).isEqualTo("SessionAutosaveService");
        assertThat(diagnosticInfo.getStates()).containsKeys(
                "activeSessions", "totalAutosaves", "failedAutosaves", "successRate"
        );
        assertThat(diagnosticInfo.getStates().get("activeSessions")).isEqualTo(1);
        assertThat((Integer) diagnosticInfo.getStates().get("totalAutosaves")).isGreaterThanOrEqualTo(1);
        assertThat((Double) diagnosticInfo.getStates().get("successRate")).isEqualTo(100.0);
    }

    @Test
    void testDiagnosticMode() {
        // Given
        assertThat(autosaveService.isDiagnosticModeEnabled()).isFalse();

        // When
        autosaveService.enableDiagnosticMode(true);

        // Then
        assertThat(autosaveService.isDiagnosticModeEnabled()).isTrue();

        // When
        autosaveService.enableDiagnosticMode(false);

        // Then
        assertThat(autosaveService.isDiagnosticModeEnabled()).isFalse();
    }

    @Test
    void testAutoSaveStatus_RecordsMetrics() {
        // Given
        SessionAutosaveService.AutosaveStatus status = 
                new SessionAutosaveService.AutosaveStatus("test-session", Duration.ofMinutes(5));

        // When
        status.recordSuccessfulSave();
        status.recordSuccessfulSave();
        status.recordFailedSave();

        // Then
        assertThat(status.getSessionId()).isEqualTo("test-session");
        assertThat(status.getInterval()).isEqualTo(Duration.ofMinutes(5));
        assertThat(status.getSaveCount()).isEqualTo(2);
        assertThat(status.getFailureCount()).isEqualTo(1);
        assertThat(status.getLastSaveTime()).isNotNull();
        assertThat(status.getLastSaveTime()).isAfter(LocalDateTime.now().minusSeconds(1));
    }
}