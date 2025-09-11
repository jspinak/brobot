package io.github.jspinak.brobot.persistence;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.UUID;

import org.junit.jupiter.api.*;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.junit.jupiter.params.provider.ValueSource;

import io.github.jspinak.brobot.model.action.ActionHistory;
import io.github.jspinak.brobot.model.action.ActionRecord;
import io.github.jspinak.brobot.model.state.StateObject;
import io.github.jspinak.brobot.test.BrobotTestBase;

/**
 * Comprehensive test suite for PersistenceProvider interface implementations. Tests session
 * management, action recording, and data import/export functionality.
 */
@DisplayName("PersistenceProvider Tests")
public class PersistenceProviderTest extends BrobotTestBase {

    private PersistenceProvider persistenceProvider;
    private TestPersistenceProvider testProvider;

    @BeforeEach
    @Override
    public void setupTest() {
        super.setupTest();
        testProvider = new TestPersistenceProvider();
        persistenceProvider = testProvider;
    }

    @Nested
    @DisplayName("Session Management")
    class SessionManagement {

        @Test
        @DisplayName("Should start new session with valid parameters")
        void shouldStartNewSession() {
            String sessionId =
                    persistenceProvider.startSession("TestSession", "TestApp", "metadata");

            assertNotNull(sessionId);
            assertTrue(persistenceProvider.isRecording());
            assertEquals(sessionId, persistenceProvider.getCurrentSessionId());
        }

        @Test
        @DisplayName("Should stop active session")
        void shouldStopActiveSession() {
            String startId = persistenceProvider.startSession("TestSession", "TestApp", null);

            String stopId = persistenceProvider.stopSession();

            assertEquals(startId, stopId);
            assertFalse(persistenceProvider.isRecording());
            assertNull(persistenceProvider.getCurrentSessionId());
        }

        @Test
        @DisplayName("Should handle stop when no active session")
        void shouldHandleStopWithNoSession() {
            String result = persistenceProvider.stopSession();

            assertNull(result);
            assertFalse(persistenceProvider.isRecording());
        }

        @Test
        @DisplayName("Should pause and resume recording")
        void shouldPauseAndResumeRecording() {
            persistenceProvider.startSession("Test", "App", null);
            assertTrue(persistenceProvider.isRecording());

            persistenceProvider.pauseRecording();
            assertFalse(persistenceProvider.isRecording());

            persistenceProvider.resumeRecording();
            assertTrue(persistenceProvider.isRecording());
        }

        @ParameterizedTest
        @CsvSource({
            "Session1, App1, metadata1",
            "Session2, App2, metadata2",
            "LongSessionName123456789, ApplicationWithLongName, {complex:metadata}"
        })
        @DisplayName("Should start sessions with various parameters")
        void shouldStartSessionsWithVariousParameters(String name, String app, String metadata) {
            String sessionId = persistenceProvider.startSession(name, app, metadata);

            assertNotNull(sessionId);

            PersistenceProvider.SessionMetadata sessionMetadata =
                    persistenceProvider.getSessionMetadata(sessionId);

            assertEquals(name, sessionMetadata.getName());
            assertEquals(app, sessionMetadata.getApplication());
            assertEquals(metadata, sessionMetadata.getMetadata());
        }
    }

    @Nested
    @DisplayName("Action Recording")
    class ActionRecording {

        @Test
        @DisplayName("Should record single action")
        void shouldRecordSingleAction() {
            String sessionId = persistenceProvider.startSession("Test", "App", null);

            ActionRecord record = createActionRecord("CLICK", true);
            StateObject stateObject = mock(StateObject.class);

            persistenceProvider.recordAction(record, stateObject);

            ActionHistory history = persistenceProvider.exportSession(sessionId);
            assertEquals(1, history.getSnapshots().size());
            assertEquals(record, history.getSnapshots().get(0));
        }

        @Test
        @DisplayName("Should record batch of actions")
        void shouldRecordBatchActions() {
            persistenceProvider.startSession("Test", "App", null);

            List<ActionRecord> records =
                    Arrays.asList(
                            createActionRecord("CLICK", true),
                            createActionRecord("TYPE", true),
                            createActionRecord("FIND", false),
                            createActionRecord("MOVE", true));

            persistenceProvider.recordBatch(records);

            ActionHistory history =
                    persistenceProvider.exportSession(persistenceProvider.getCurrentSessionId());
            assertEquals(4, history.getSnapshots().size());
        }

        @Test
        @DisplayName("Should not record when paused")
        void shouldNotRecordWhenPaused() {
            String sessionId = persistenceProvider.startSession("Test", "App", null);
            persistenceProvider.pauseRecording();

            ActionRecord record = createActionRecord("CLICK", true);
            persistenceProvider.recordAction(record, null);

            ActionHistory history = persistenceProvider.exportSession(sessionId);
            assertEquals(0, history.getSnapshots().size());
        }

        @Test
        @DisplayName("Should resume recording after pause")
        void shouldResumeRecordingAfterPause() {
            String sessionId = persistenceProvider.startSession("Test", "App", null);

            persistenceProvider.recordAction(createActionRecord("ACTION1", true), null);
            persistenceProvider.pauseRecording();
            persistenceProvider.recordAction(createActionRecord("ACTION2", true), null);
            persistenceProvider.resumeRecording();
            persistenceProvider.recordAction(createActionRecord("ACTION3", true), null);

            ActionHistory history = persistenceProvider.exportSession(sessionId);
            assertEquals(2, history.getSnapshots().size()); // ACTION1 and ACTION3
        }
    }

    @Nested
    @DisplayName("Session Import/Export")
    class SessionImportExport {

        @Test
        @DisplayName("Should export session as ActionHistory")
        void shouldExportSessionAsActionHistory() {
            String sessionId = persistenceProvider.startSession("Export Test", "App", null);

            persistenceProvider.recordAction(createActionRecord("CLICK", true), null);
            persistenceProvider.recordAction(createActionRecord("TYPE", false), null);
            persistenceProvider.stopSession();

            ActionHistory history = persistenceProvider.exportSession(sessionId);

            assertNotNull(history);
            assertEquals(2, history.getSnapshots().size());
        }

        @Test
        @DisplayName("Should import ActionHistory as new session")
        void shouldImportActionHistoryAsNewSession() {
            ActionHistory history = new ActionHistory();
            history.setSnapshots(
                    Arrays.asList(
                            createActionRecord("IMPORTED1", true),
                            createActionRecord("IMPORTED2", false)));

            String importedSessionId = persistenceProvider.importSession(history, "Imported");

            assertNotNull(importedSessionId);
            assertTrue(persistenceProvider.getAllSessions().contains(importedSessionId));

            ActionHistory exported = persistenceProvider.exportSession(importedSessionId);
            assertEquals(2, exported.getSnapshots().size());
        }

        @Test
        @DisplayName("Should handle empty history import")
        void shouldHandleEmptyHistoryImport() {
            ActionHistory emptyHistory = new ActionHistory();
            emptyHistory.setSnapshots(new ArrayList<>());

            String sessionId = persistenceProvider.importSession(emptyHistory, "Empty");

            assertNotNull(sessionId);
            ActionHistory exported = persistenceProvider.exportSession(sessionId);
            assertEquals(0, exported.getSnapshots().size());
        }
    }

    @Nested
    @DisplayName("Session Queries")
    class SessionQueries {

        @Test
        @DisplayName("Should get all sessions")
        void shouldGetAllSessions() {
            String session1 = persistenceProvider.startSession("Session1", "App", null);
            persistenceProvider.stopSession();

            String session2 = persistenceProvider.startSession("Session2", "App", null);
            persistenceProvider.stopSession();

            String session3 = persistenceProvider.startSession("Session3", "App", null);
            persistenceProvider.stopSession();

            List<String> allSessions = persistenceProvider.getAllSessions();

            assertEquals(3, allSessions.size());
            assertTrue(allSessions.containsAll(Arrays.asList(session1, session2, session3)));
        }

        @Test
        @DisplayName("Should delete session")
        void shouldDeleteSession() {
            String sessionId = persistenceProvider.startSession("ToDelete", "App", null);
            persistenceProvider.stopSession();

            assertTrue(persistenceProvider.getAllSessions().contains(sessionId));

            persistenceProvider.deleteSession(sessionId);

            assertFalse(persistenceProvider.getAllSessions().contains(sessionId));
            assertNull(persistenceProvider.getSessionMetadata(sessionId));
        }

        @Test
        @DisplayName("Should get session metadata")
        void shouldGetSessionMetadata() {
            String sessionId =
                    persistenceProvider.startSession(
                            "MetadataTest", "TestApplication", "test-metadata");

            // Record some actions
            persistenceProvider.recordAction(createActionRecord("ACTION1", true), null);
            persistenceProvider.recordAction(createActionRecord("ACTION2", false), null);
            persistenceProvider.recordAction(createActionRecord("ACTION3", true), null);

            persistenceProvider.stopSession();

            PersistenceProvider.SessionMetadata metadata =
                    persistenceProvider.getSessionMetadata(sessionId);

            assertNotNull(metadata);
            assertEquals("MetadataTest", metadata.getName());
            assertEquals("TestApplication", metadata.getApplication());
            assertEquals("test-metadata", metadata.getMetadata());
            assertEquals(3, metadata.getTotalActions());
            assertEquals(2, metadata.getSuccessfulActions());
            assertEquals(66.67, metadata.getSuccessRate(), 0.01);
        }
    }

    @Nested
    @DisplayName("Edge Cases")
    class EdgeCases {

        @Test
        @DisplayName("Should handle null parameters in start session")
        void shouldHandleNullParametersInStartSession() {
            String sessionId = persistenceProvider.startSession(null, null, null);

            assertNotNull(sessionId);
            assertTrue(persistenceProvider.isRecording());
        }

        @Test
        @DisplayName("Should handle recording without state object")
        void shouldHandleRecordingWithoutStateObject() {
            persistenceProvider.startSession("Test", "App", null);

            ActionRecord record = createActionRecord("ACTION", true);

            assertDoesNotThrow(() -> persistenceProvider.recordAction(record, null));
        }

        @Test
        @DisplayName("Should handle multiple consecutive starts")
        void shouldHandleMultipleConsecutiveStarts() {
            String session1 = persistenceProvider.startSession("Session1", "App", null);
            String session2 = persistenceProvider.startSession("Session2", "App", null);

            // Should stop first session and start new one
            assertNotEquals(session1, session2);
            assertEquals(session2, persistenceProvider.getCurrentSessionId());
        }

        @Test
        @DisplayName("Should handle export of non-existent session")
        void shouldHandleExportOfNonExistentSession() {
            ActionHistory history = persistenceProvider.exportSession("non-existent");

            assertNotNull(history);
            assertTrue(history.getSnapshots().isEmpty());
        }
    }

    @Nested
    @DisplayName("Performance and Scalability")
    class PerformanceScalability {

        @ParameterizedTest
        @ValueSource(ints = {10, 100, 1000})
        @DisplayName("Should handle large batch recordings")
        void shouldHandleLargeBatchRecordings(int batchSize) {
            persistenceProvider.startSession("Performance", "App", null);

            List<ActionRecord> largeBatch = new ArrayList<>();
            for (int i = 0; i < batchSize; i++) {
                largeBatch.add(createActionRecord("ACTION_" + i, i % 2 == 0));
            }

            long startTime = System.currentTimeMillis();
            persistenceProvider.recordBatch(largeBatch);
            long endTime = System.currentTimeMillis();

            ActionHistory history =
                    persistenceProvider.exportSession(persistenceProvider.getCurrentSessionId());

            assertEquals(batchSize, history.getSnapshots().size());

            // Performance assertion - should complete reasonably fast
            long duration = endTime - startTime;
            assertTrue(
                    duration < batchSize * 10, // Allow 10ms per record
                    "Recording " + batchSize + " records took " + duration + "ms");
        }

        @Test
        @DisplayName("Should handle many sessions")
        void shouldHandleManySession() {
            List<String> sessionIds = new ArrayList<>();

            for (int i = 0; i < 50; i++) {
                String sessionId = persistenceProvider.startSession("Session" + i, "App", null);
                persistenceProvider.recordAction(createActionRecord("ACTION", true), null);
                persistenceProvider.stopSession();
                sessionIds.add(sessionId);
            }

            List<String> allSessions = persistenceProvider.getAllSessions();
            assertEquals(50, allSessions.size());
            assertTrue(allSessions.containsAll(sessionIds));
        }
    }

    @Nested
    @DisplayName("SessionMetadata")
    class SessionMetadataTests {

        @Test
        @DisplayName("Should calculate success rate correctly")
        void shouldCalculateSuccessRateCorrectly() {
            PersistenceProvider.SessionMetadata metadata =
                    new PersistenceProvider.SessionMetadata();

            metadata.setTotalActions(10);
            metadata.setSuccessfulActions(7);
            assertEquals(70.0, metadata.getSuccessRate(), 0.01);

            metadata.setTotalActions(0);
            assertEquals(0.0, metadata.getSuccessRate(), 0.01);

            metadata.setTotalActions(3);
            metadata.setSuccessfulActions(3);
            assertEquals(100.0, metadata.getSuccessRate(), 0.01);
        }

        @Test
        @DisplayName("Should handle metadata creation")
        void shouldHandleMetadataCreation() {
            PersistenceProvider.SessionMetadata metadata =
                    new PersistenceProvider.SessionMetadata("id123", "TestSession", "TestApp");

            assertEquals("id123", metadata.getSessionId());
            assertEquals("TestSession", metadata.getName());
            assertEquals("TestApp", metadata.getApplication());
            assertNotNull(metadata.getStartTime());
            assertNull(metadata.getEndTime());
            assertEquals(0, metadata.getTotalActions());
            assertEquals(0, metadata.getSuccessfulActions());
        }

        @Test
        @DisplayName("Should set and get all metadata fields")
        void shouldSetAndGetAllMetadataFields() {
            PersistenceProvider.SessionMetadata metadata =
                    new PersistenceProvider.SessionMetadata();

            LocalDateTime now = LocalDateTime.now();

            metadata.setSessionId("test-id");
            metadata.setName("test-name");
            metadata.setApplication("test-app");
            metadata.setStartTime(now);
            metadata.setEndTime(now.plusHours(1));
            metadata.setTotalActions(100);
            metadata.setSuccessfulActions(95);
            metadata.setMetadata("test-metadata");

            assertEquals("test-id", metadata.getSessionId());
            assertEquals("test-name", metadata.getName());
            assertEquals("test-app", metadata.getApplication());
            assertEquals(now, metadata.getStartTime());
            assertEquals(now.plusHours(1), metadata.getEndTime());
            assertEquals(100, metadata.getTotalActions());
            assertEquals(95, metadata.getSuccessfulActions());
            assertEquals("test-metadata", metadata.getMetadata());
            assertEquals(95.0, metadata.getSuccessRate(), 0.01);
        }
    }

    // Helper method to create test ActionRecord
    private ActionRecord createActionRecord(String action, boolean success) {
        ActionRecord record = new ActionRecord();
        // ActionRecord doesn't have setAction method, action type is set via ActionConfig
        record.setActionSuccess(success);
        record.setTimeStamp(LocalDateTime.now());
        return record;
    }

    /** Test implementation of PersistenceProvider for testing. */
    private static class TestPersistenceProvider implements PersistenceProvider {
        private final List<SessionData> sessions = new ArrayList<>();
        private SessionData currentSession = null;
        private boolean isPaused = false;

        @Override
        public String startSession(String sessionName, String application, String metadata) {
            if (currentSession != null) {
                stopSession();
            }

            String sessionId = UUID.randomUUID().toString();
            currentSession = new SessionData(sessionId, sessionName, application, metadata);
            sessions.add(currentSession);
            isPaused = false;
            return sessionId;
        }

        @Override
        public String stopSession() {
            if (currentSession == null) {
                return null;
            }

            String sessionId = currentSession.sessionId;
            currentSession.metadata.setEndTime(LocalDateTime.now());
            currentSession = null;
            isPaused = false;
            return sessionId;
        }

        @Override
        public void pauseRecording() {
            isPaused = true;
        }

        @Override
        public void resumeRecording() {
            isPaused = false;
        }

        @Override
        public boolean isRecording() {
            return currentSession != null && !isPaused;
        }

        @Override
        public void recordAction(ActionRecord record, StateObject stateObject) {
            if (!isRecording()) {
                return;
            }

            currentSession.records.add(record);
            currentSession.metadata.setTotalActions(currentSession.metadata.getTotalActions() + 1);
            if (record.isActionSuccess()) {
                currentSession.metadata.setSuccessfulActions(
                        currentSession.metadata.getSuccessfulActions() + 1);
            }
        }

        @Override
        public void recordBatch(List<ActionRecord> records) {
            for (ActionRecord record : records) {
                recordAction(record, null);
            }
        }

        @Override
        public ActionHistory exportSession(String sessionId) {
            ActionHistory history = new ActionHistory();
            SessionData session = findSession(sessionId);
            if (session != null) {
                history.setSnapshots(new ArrayList<>(session.records));
            } else {
                history.setSnapshots(new ArrayList<>());
            }
            return history;
        }

        @Override
        public String importSession(ActionHistory history, String sessionName) {
            String sessionId = UUID.randomUUID().toString();
            SessionData session = new SessionData(sessionId, sessionName, "Imported", null);
            session.records.addAll(history.getSnapshots());
            session.metadata.setTotalActions(history.getSnapshots().size());
            session.metadata.setSuccessfulActions(
                    (int)
                            history.getSnapshots().stream()
                                    .filter(ActionRecord::isActionSuccess)
                                    .count());
            sessions.add(session);
            return sessionId;
        }

        @Override
        public List<String> getAllSessions() {
            return sessions.stream()
                    .map(s -> s.sessionId)
                    .collect(java.util.stream.Collectors.toList());
        }

        @Override
        public void deleteSession(String sessionId) {
            sessions.removeIf(s -> s.sessionId.equals(sessionId));
        }

        @Override
        public SessionMetadata getSessionMetadata(String sessionId) {
            SessionData session = findSession(sessionId);
            return session != null ? session.metadata : null;
        }

        @Override
        public String getCurrentSessionId() {
            return currentSession != null ? currentSession.sessionId : null;
        }

        private SessionData findSession(String sessionId) {
            return sessions.stream()
                    .filter(s -> s.sessionId.equals(sessionId))
                    .findFirst()
                    .orElse(null);
        }

        private static class SessionData {
            final String sessionId;
            final SessionMetadata metadata;
            final List<ActionRecord> records = new ArrayList<>();

            SessionData(String sessionId, String name, String app, String meta) {
                this.sessionId = sessionId;
                this.metadata = new SessionMetadata(sessionId, name, app);
                this.metadata.setMetadata(meta);
            }
        }
    }
}
