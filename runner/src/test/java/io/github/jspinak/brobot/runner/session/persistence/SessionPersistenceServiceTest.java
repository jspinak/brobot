package io.github.jspinak.brobot.runner.session.persistence;

import io.github.jspinak.brobot.runner.common.diagnostics.DiagnosticInfo;
import io.github.jspinak.brobot.runner.session.Session;
import io.github.jspinak.brobot.runner.session.SessionEvent;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import org.springframework.test.util.ReflectionTestUtils;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.LocalDateTime;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.*;

class SessionPersistenceServiceTest {

    private SessionPersistenceService persistenceService;
    
    @TempDir
    Path tempDir;

    @BeforeEach
    void setUp() {
        persistenceService = new SessionPersistenceService();
        // Use reflection to set the storage path to our temp directory
        ReflectionTestUtils.setField(persistenceService, "sessionStoragePathConfig", tempDir.toString());
        persistenceService.initialize();
    }

    @Test
    void testSaveSession_CreatesJsonFile() throws IOException {
        // Given
        Session session = createTestSession();

        // When
        Path savedPath = persistenceService.saveSession(session);

        // Then
        assertThat(savedPath).exists();
        assertThat(savedPath.toString()).endsWith(".json");
        String content = Files.readString(savedPath);
        assertThat(content).contains(session.getId());
        assertThat(content).contains("Test Project");
    }

    @Test
    void testSaveSession_WithNullSession_ThrowsException() {
        assertThatThrownBy(() -> persistenceService.saveSession(null))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("Session and session ID must not be null");
    }

    @Test
    void testLoadSession_ReturnsSessionFromDisk() throws IOException {
        // Given
        Session originalSession = createTestSession();
        persistenceService.saveSession(originalSession);

        // When
        Optional<Session> loadedSession = persistenceService.loadSession(originalSession.getId());

        // Then
        assertThat(loadedSession).isPresent();
        assertThat(loadedSession.get().getId()).isEqualTo(originalSession.getId());
        assertThat(loadedSession.get().getProjectName()).isEqualTo(originalSession.getProjectName());
        assertThat(loadedSession.get().getConfigPath()).isEqualTo(originalSession.getConfigPath());
    }

    @Test
    void testLoadSession_NonExistentSession_ReturnsEmpty() {
        // When
        Optional<Session> result = persistenceService.loadSession("non-existent-id");

        // Then
        assertThat(result).isEmpty();
    }

    @Test
    void testLoadSession_WithNullId_ReturnsEmpty() {
        // When
        Optional<Session> result = persistenceService.loadSession(null);

        // Then
        assertThat(result).isEmpty();
    }

    @Test
    void testDeleteSession_RemovesFile() throws IOException {
        // Given
        Session session = createTestSession();
        Path savedPath = persistenceService.saveSession(session);
        assertThat(savedPath).exists();

        // When
        boolean deleted = persistenceService.deleteSession(session.getId());

        // Then
        assertThat(deleted).isTrue();
        assertThat(savedPath).doesNotExist();
    }

    @Test
    void testDeleteSession_NonExistentSession_ReturnsFalse() {
        // When
        boolean result = persistenceService.deleteSession("non-existent-id");

        // Then
        assertThat(result).isFalse();
    }

    @Test
    void testSessionExists_ReturnsTrueForSavedSession() throws IOException {
        // Given
        Session session = createTestSession();
        persistenceService.saveSession(session);

        // When/Then
        assertThat(persistenceService.sessionExists(session.getId())).isTrue();
        assertThat(persistenceService.sessionExists("non-existent-id")).isFalse();
    }

    @Test
    void testBackupSession_CreatesBackupCopy() throws IOException {
        // Given
        Session session = createTestSession();
        persistenceService.saveSession(session);

        // When
        Optional<Path> backupPath = persistenceService.backupSession(session.getId());

        // Then
        assertThat(backupPath).isPresent();
        assertThat(backupPath.get()).exists();
        assertThat(backupPath.get().toString()).contains("backup");
        
        // Verify backup content
        String backupContent = Files.readString(backupPath.get());
        assertThat(backupContent).contains("backup");
    }

    @Test
    void testBackupSession_NonExistentSession_ReturnsEmpty() {
        // When
        Optional<Path> result = persistenceService.backupSession("non-existent-id");

        // Then
        assertThat(result).isEmpty();
    }

    @Test
    void testGetSessionPath_ReturnsCorrectPath() {
        // When
        Path path = persistenceService.getSessionPath("test-session-id");

        // Then
        assertThat(path.toString()).endsWith("test-session-id.json");
        assertThat(path.getParent()).isEqualTo(tempDir);
    }

    @Test
    void testSaveAndLoadComplexSession() throws IOException {
        // Given
        Session session = createTestSession();
        session.addEvent(new SessionEvent("TEST_EVENT", "Test event occurred"));
        session.addEvent(new SessionEvent("ANOTHER_EVENT", "Another test event", "With details"));
        session.addStateData("testKey", "testValue");
        session.addStateData("numberKey", 42);

        // When
        persistenceService.saveSession(session);
        Optional<Session> loaded = persistenceService.loadSession(session.getId());

        // Then
        assertThat(loaded).isPresent();
        Session loadedSession = loaded.get();
        assertThat(loadedSession.getEvents()).hasSize(2);
        assertThat(loadedSession.getStateData()).containsEntry("testKey", "testValue");
        assertThat(loadedSession.getStateData()).containsEntry("numberKey", 42);
    }

    @Test
    void testConcurrentSaveOperations() throws InterruptedException {
        // Given
        int threadCount = 5;
        Thread[] threads = new Thread[threadCount];

        // When
        for (int i = 0; i < threadCount; i++) {
            final int index = i;
            threads[i] = new Thread(() -> {
                try {
                    Session session = createTestSession();
                    session.setId("concurrent-session-" + index);
                    persistenceService.saveSession(session);
                } catch (IOException e) {
                    fail("Failed to save session: " + e.getMessage());
                }
            });
            threads[i].start();
        }

        // Wait for all threads to complete
        for (Thread thread : threads) {
            thread.join();
        }

        // Then - verify all sessions were saved
        for (int i = 0; i < threadCount; i++) {
            assertThat(persistenceService.sessionExists("concurrent-session-" + i)).isTrue();
        }
    }

    @Test
    void testDiagnosticInfo() throws IOException {
        // Given
        Session session1 = createTestSession();
        Session session2 = createTestSession();
        session2.setId(UUID.randomUUID().toString());
        
        persistenceService.saveSession(session1);
        persistenceService.saveSession(session2);
        persistenceService.loadSession(session1.getId());
        persistenceService.deleteSession(session2.getId());

        // When
        DiagnosticInfo diagnosticInfo = persistenceService.getDiagnosticInfo();

        // Then
        assertThat(diagnosticInfo.getComponent()).isEqualTo("SessionPersistenceService");
        assertThat(diagnosticInfo.getStates()).containsKeys(
                "storagePath", "sessionsSaved", "sessionsLoaded", 
                "sessionsDeleted", "totalStorageSizeMB", "totalSessionFiles"
        );
        assertThat((Integer) diagnosticInfo.getStates().get("sessionsSaved")).isGreaterThanOrEqualTo(2);
        assertThat((Integer) diagnosticInfo.getStates().get("sessionsLoaded")).isGreaterThanOrEqualTo(1);
        assertThat((Integer) diagnosticInfo.getStates().get("sessionsDeleted")).isGreaterThanOrEqualTo(1);
    }

    @Test
    void testDiagnosticMode() throws IOException {
        // Given
        persistenceService.enableDiagnosticMode(true);
        Session session = createTestSession();

        // When - save with diagnostic mode enabled
        persistenceService.saveSession(session);

        // Then
        assertThat(persistenceService.isDiagnosticModeEnabled()).isTrue();

        // When
        persistenceService.enableDiagnosticMode(false);

        // Then
        assertThat(persistenceService.isDiagnosticModeEnabled()).isFalse();
    }

    private Session createTestSession() {
        Session session = new Session();
        session.setId(UUID.randomUUID().toString());
        session.setProjectName("Test Project");
        session.setConfigPath("/test/config.json");
        session.setImagePath("/test/images");
        session.setStartTime(LocalDateTime.now());
        session.setActive(true);
        return session;
    }
}