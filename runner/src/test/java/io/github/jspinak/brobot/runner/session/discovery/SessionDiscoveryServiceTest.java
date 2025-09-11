package io.github.jspinak.brobot.runner.session.discovery;

import static org.assertj.core.api.Assertions.*;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import org.springframework.test.util.ReflectionTestUtils;

import com.fasterxml.jackson.databind.ObjectMapper;

import io.github.jspinak.brobot.runner.common.diagnostics.DiagnosticInfo;
import io.github.jspinak.brobot.runner.session.Session;
import io.github.jspinak.brobot.runner.session.SessionSummary;

class SessionDiscoveryServiceTest {

    private SessionDiscoveryService discoveryService;
    private ObjectMapper objectMapper;

    @TempDir Path tempDir;

    @BeforeEach
    void setUp() {
        discoveryService = new SessionDiscoveryService();
        objectMapper = new ObjectMapper();
        objectMapper.findAndRegisterModules();

        // Use reflection to set the storage path to our temp directory
        ReflectionTestUtils.setField(
                discoveryService, "sessionStoragePathConfig", tempDir.toString());
        discoveryService.initialize();
    }

    @Test
    void testListAvailableSessions_EmptyDirectory_ReturnsEmptyList() {
        // When
        List<SessionSummary> sessions = discoveryService.listAvailableSessions();

        // Then
        assertThat(sessions).isEmpty();
    }

    @Test
    void testListAvailableSessions_WithSessions_ReturnsSortedList() throws IOException {
        // Given
        createSessionFile("session1", "Project A", LocalDateTime.now().minusDays(2));
        createSessionFile("session2", "Project B", LocalDateTime.now().minusDays(1));
        createSessionFile("session3", "Project C", LocalDateTime.now());

        // When
        List<SessionSummary> sessions = discoveryService.listAvailableSessions();

        // Then
        assertThat(sessions).hasSize(3);
        assertThat(sessions.get(0).getProjectName()).isEqualTo("Project C"); // Most recent first
        assertThat(sessions.get(1).getProjectName()).isEqualTo("Project B");
        assertThat(sessions.get(2).getProjectName()).isEqualTo("Project A");
    }

    @Test
    void testFindSession_ExistingSession_ReturnsSummary() throws IOException {
        // Given
        String sessionId = "test-session-id";
        createSessionFile(sessionId, "Test Project", LocalDateTime.now());

        // When
        Optional<SessionSummary> summary = discoveryService.findSession(sessionId);

        // Then
        assertThat(summary).isPresent();
        assertThat(summary.get().getId()).isEqualTo(sessionId);
        assertThat(summary.get().getProjectName()).isEqualTo("Test Project");
    }

    @Test
    void testFindSession_NonExistentSession_ReturnsEmpty() {
        // When
        Optional<SessionSummary> summary = discoveryService.findSession("non-existent");

        // Then
        assertThat(summary).isEmpty();
    }

    @Test
    void testFindSession_NullId_ReturnsEmpty() {
        // When
        Optional<SessionSummary> summary = discoveryService.findSession(null);

        // Then
        assertThat(summary).isEmpty();
    }

    @Test
    void testFindSessionsByDate() throws IOException {
        // Given
        LocalDate today = LocalDate.now();
        LocalDate yesterday = today.minusDays(1);

        createSessionFile("session1", "Today's Project 1", today.atStartOfDay());
        createSessionFile("session2", "Today's Project 2", today.atTime(10, 30));
        createSessionFile("session3", "Yesterday's Project", yesterday.atTime(14, 0));

        // When
        List<SessionSummary> todaySessions = discoveryService.findSessionsByDate(today);
        List<SessionSummary> yesterdaySessions = discoveryService.findSessionsByDate(yesterday);

        // Then
        assertThat(todaySessions).hasSize(2);
        assertThat(todaySessions)
                .extracting(SessionSummary::getProjectName)
                .containsExactlyInAnyOrder("Today's Project 1", "Today's Project 2");

        assertThat(yesterdaySessions).hasSize(1);
        assertThat(yesterdaySessions.get(0).getProjectName()).isEqualTo("Yesterday's Project");
    }

    @Test
    void testFindSessionsByDate_NullDate_ReturnsEmpty() {
        // When
        List<SessionSummary> sessions = discoveryService.findSessionsByDate(null);

        // Then
        assertThat(sessions).isEmpty();
    }

    @Test
    void testFindSessionsByProject() throws IOException {
        // Given
        createSessionFile("session1", "Project Alpha", LocalDateTime.now());
        createSessionFile("session2", "Project Beta", LocalDateTime.now());
        createSessionFile("session3", "Project Alpha", LocalDateTime.now());

        // When
        List<SessionSummary> alphaSessions =
                discoveryService.findSessionsByProject("Project Alpha");
        List<SessionSummary> betaSessions = discoveryService.findSessionsByProject("Project Beta");

        // Then
        assertThat(alphaSessions).hasSize(2);
        assertThat(betaSessions).hasSize(1);
        assertThat(betaSessions.get(0).getId()).isEqualTo("session2");
    }

    @Test
    void testFindSessionsByProject_EmptyProject_ReturnsEmpty() {
        // When
        List<SessionSummary> sessions = discoveryService.findSessionsByProject("");

        // Then
        assertThat(sessions).isEmpty();
    }

    @Test
    void testSearchSessions_ByKeyword() throws IOException {
        // Given
        createSessionFile("session1", "Alpha Project", LocalDateTime.now(), "Alpha Session");
        createSessionFile("session2", "Beta Project", LocalDateTime.now(), "Beta Session");
        createSessionFile("session3", "Gamma Project", LocalDateTime.now(), "Alpha Test");

        // When
        List<SessionSummary> alphaResults = discoveryService.searchSessions("alpha");
        List<SessionSummary> betaResults = discoveryService.searchSessions("BETA");

        // Then
        assertThat(alphaResults).hasSize(2); // Matches in both project name and session name
        assertThat(betaResults).hasSize(1);
    }

    @Test
    void testSearchSessions_EmptyKeyword_ReturnsAll() throws IOException {
        // Given
        createSessionFile("session1", "Project 1", LocalDateTime.now());
        createSessionFile("session2", "Project 2", LocalDateTime.now());

        // When
        List<SessionSummary> results = discoveryService.searchSessions("");

        // Then
        assertThat(results).hasSize(2);
    }

    @Test
    void testGetRecentSessions() throws IOException {
        // Given
        for (int i = 0; i < 10; i++) {
            createSessionFile("session" + i, "Project " + i, LocalDateTime.now().minusHours(i));
        }

        // When
        List<SessionSummary> recent5 = discoveryService.getRecentSessions(5);
        List<SessionSummary> recent3 = discoveryService.getRecentSessions(3);

        // Then
        assertThat(recent5).hasSize(5);
        assertThat(recent3).hasSize(3);
        assertThat(recent3.get(0).getProjectName()).isEqualTo("Project 0"); // Most recent
    }

    @Test
    void testCacheInvalidation() throws IOException {
        // Given - create initial sessions
        createSessionFile("session1", "Project 1", LocalDateTime.now());

        // First load to populate cache
        List<SessionSummary> initial = discoveryService.listAvailableSessions();
        assertThat(initial).hasSize(1);

        // Add new session
        createSessionFile("session2", "Project 2", LocalDateTime.now());

        // When - immediately query (should use cache)
        List<SessionSummary> cached = discoveryService.listAvailableSessions();

        // Then - should still show cached result
        assertThat(cached).hasSize(1);

        // When - force cache invalidation by setting cache time to past
        ReflectionTestUtils.setField(
                discoveryService, "cacheLastUpdated", LocalDateTime.now().minusMinutes(10));

        List<SessionSummary> refreshed = discoveryService.listAvailableSessions();

        // Then - should show updated results
        assertThat(refreshed).hasSize(2);
    }

    @Test
    void testSessionSummaryGeneration() throws IOException {
        // Given
        String sessionId = "test-session";
        Session session = new Session();
        session.setId(sessionId);
        session.setProjectName("Test Project");
        session.setConfigPath("/config/test.json");
        session.setStartTime(LocalDateTime.now().minusHours(2));
        session.setEndTime(LocalDateTime.now());
        session.setActive(false);

        Path sessionFile = tempDir.resolve(sessionId + ".json");
        objectMapper.writeValue(sessionFile.toFile(), session);

        // When
        Optional<SessionSummary> summary = discoveryService.findSession(sessionId);

        // Then
        assertThat(summary).isPresent();
        SessionSummary s = summary.get();
        assertThat(s.getId()).isEqualTo(sessionId);
        assertThat(s.getProjectName()).isEqualTo("Test Project");
        assertThat(s.getConfigPath()).isEqualTo("/config/test.json");
        assertThat(s.getSessionName()).isNotNull(); // Generated if not present
        assertThat(s.getFileSize()).isGreaterThan(0);
        assertThat(s.getActive()).isFalse();
    }

    @Test
    void testDiagnosticInfo() throws IOException {
        // Given
        createSessionFile("session1", "Project 1", LocalDateTime.now());
        createSessionFile("session2", "Project 2", LocalDateTime.now());
        discoveryService.listAvailableSessions(); // Populate cache

        // When
        DiagnosticInfo diagnosticInfo = discoveryService.getDiagnosticInfo();

        // Then
        assertThat(diagnosticInfo.getComponent()).isEqualTo("SessionDiscoveryService");
        assertThat(diagnosticInfo.getStates())
                .containsKeys(
                        "storagePath",
                        "cachedSessions",
                        "cacheLastUpdated",
                        "cacheValid",
                        "totalSessionFiles");
        assertThat(diagnosticInfo.getStates().get("cachedSessions")).isEqualTo(2);
        assertThat(diagnosticInfo.getStates().get("totalSessionFiles")).isEqualTo(2L);
        assertThat((Boolean) diagnosticInfo.getStates().get("cacheValid")).isTrue();
    }

    @Test
    void testDiagnosticMode() {
        // Given
        assertThat(discoveryService.isDiagnosticModeEnabled()).isFalse();

        // When
        discoveryService.enableDiagnosticMode(true);

        // Then
        assertThat(discoveryService.isDiagnosticModeEnabled()).isTrue();

        // When
        discoveryService.enableDiagnosticMode(false);

        // Then
        assertThat(discoveryService.isDiagnosticModeEnabled()).isFalse();
    }

    @Test
    void testHandleCorruptedSessionFile() throws IOException {
        // Given - create a corrupted JSON file
        Path corruptedFile = tempDir.resolve("corrupted-session.json");
        Files.writeString(corruptedFile, "{ invalid json content");

        // When - should handle gracefully
        List<SessionSummary> sessions = discoveryService.listAvailableSessions();

        // Then
        assertThat(sessions).isEmpty(); // Corrupted file is skipped
    }

    private void createSessionFile(String sessionId, String projectName, LocalDateTime startTime)
            throws IOException {
        createSessionFile(sessionId, projectName, startTime, null);
    }

    private void createSessionFile(
            String sessionId, String projectName, LocalDateTime startTime, String sessionName)
            throws IOException {
        Session session = new Session();
        session.setId(sessionId);
        session.setProjectName(projectName);
        session.setStartTime(startTime);
        session.setActive(true);
        session.setConfigPath("/config/" + sessionId + ".json");

        if (sessionName != null) {
            ReflectionTestUtils.setField(session, "sessionName", sessionName);
        }

        Path sessionFile = tempDir.resolve(sessionId + ".json");
        objectMapper.writeValue(sessionFile.toFile(), session);
    }
}
