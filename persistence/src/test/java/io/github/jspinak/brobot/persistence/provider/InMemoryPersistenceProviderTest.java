package io.github.jspinak.brobot.persistence.provider;

import static org.junit.jupiter.api.Assertions.*;

import java.util.List;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import io.github.jspinak.brobot.action.basic.find.PatternFindOptions;
import io.github.jspinak.brobot.model.action.ActionHistory;
import io.github.jspinak.brobot.model.action.ActionRecord;
import io.github.jspinak.brobot.model.match.Match;
import io.github.jspinak.brobot.persistence.config.PersistenceConfiguration;
import io.github.jspinak.brobot.test.BrobotTestBase;

/** Unit tests for InMemoryPersistenceProvider. */
class InMemoryPersistenceProviderTest extends BrobotTestBase {

    private InMemoryPersistenceProvider provider;
    private PersistenceConfiguration configuration;

    @BeforeEach
    @Override
    public void setupTest() {
        super.setupTest();
        configuration = PersistenceConfiguration.memoryDefault();
        configuration.getMemory().setMaxSessions(5);
        configuration.getMemory().setMaxRecordsPerSession(10);
        provider = new InMemoryPersistenceProvider(configuration);
    }

    @Test
    void testStartAndStopSession() {
        // Start session
        String sessionId = provider.startSession("Test Session", "TestApp", "Test metadata");

        assertNotNull(sessionId);
        assertTrue(provider.isRecording());
        assertEquals(sessionId, provider.getCurrentSessionId());

        // Stop session
        String stoppedId = provider.stopSession();

        assertEquals(sessionId, stoppedId);
        assertFalse(provider.isRecording());
        assertNull(provider.getCurrentSessionId());
    }

    @Test
    void testPauseAndResume() {
        provider.startSession("Test", "App", null);

        assertTrue(provider.isRecording());

        provider.pauseRecording();
        assertFalse(provider.isRecording());

        provider.resumeRecording();
        assertTrue(provider.isRecording());

        provider.stopSession();
    }

    @Test
    void testRecordAction() {
        String sessionId = provider.startSession("Test", "App", null);

        ActionRecord record =
                new ActionRecord.Builder()
                        .setActionConfig(new PatternFindOptions.Builder().build())
                        .setActionSuccess(true)
                        .setDuration(100)
                        .addMatch(
                                new Match.Builder()
                                        .setRegion(10, 20, 30, 40)
                                        .setSimScore(0.95)
                                        .build())
                        .build();

        provider.recordAction(record, null);

        provider.stopSession();

        // Export and verify
        ActionHistory history = provider.exportSession(sessionId);
        assertNotNull(history);
        assertEquals(1, history.getSnapshots().size());
        assertEquals(record, history.getSnapshots().get(0));
    }

    @Test
    void testRecordBatch() {
        String sessionId = provider.startSession("Batch Test", "App", null);

        List<ActionRecord> batch =
                List.of(
                        new ActionRecord.Builder().setActionSuccess(true).setDuration(100).build(),
                        new ActionRecord.Builder().setActionSuccess(false).setDuration(200).build(),
                        new ActionRecord.Builder().setActionSuccess(true).setDuration(150).build());

        provider.recordBatch(batch);
        provider.stopSession();

        ActionHistory history = provider.exportSession(sessionId);
        assertEquals(3, history.getSnapshots().size());
    }

    @Test
    void testSessionLimit() {
        // Create more sessions than the limit
        for (int i = 0; i < 7; i++) {
            provider.startSession("Session " + i, "App", null);
            provider.stopSession();
        }

        // Should only have the last 5 sessions
        List<String> sessions = provider.getAllSessions();
        assertEquals(5, sessions.size());
    }

    @Test
    void testRecordLimit() {
        String sessionId = provider.startSession("Record Limit Test", "App", null);

        // Add more records than the limit
        for (int i = 0; i < 15; i++) {
            ActionRecord record =
                    new ActionRecord.Builder()
                            .setActionSuccess(true)
                            .setDuration(i * 10)
                            .setText("Record " + i)
                            .build();
            provider.recordAction(record, null);
        }

        provider.stopSession();

        // Should only have the last 10 records
        ActionHistory history = provider.exportSession(sessionId);
        assertEquals(10, history.getSnapshots().size());
        // Verify we have the last 10 records (5-14)
        assertEquals("Record 5", history.getSnapshots().get(0).getText());
        assertEquals("Record 14", history.getSnapshots().get(9).getText());
    }

    @Test
    void testImportSession() {
        // Create history to import
        ActionHistory history = new ActionHistory();
        for (int i = 0; i < 3; i++) {
            history.addSnapshot(
                    new ActionRecord.Builder()
                            .setActionSuccess(true)
                            .setDuration(100 * i)
                            .setText("Imported " + i)
                            .build());
        }

        // Import
        String importedId = provider.importSession(history, "Imported Session");

        assertNotNull(importedId);

        // Verify imported session
        ActionHistory exported = provider.exportSession(importedId);
        assertEquals(3, exported.getSnapshots().size());
        assertEquals("Imported 0", exported.getSnapshots().get(0).getText());
    }

    @Test
    void testDeleteSession() {
        String sessionId = provider.startSession("To Delete", "App", null);
        provider.recordAction(new ActionRecord.Builder().build(), null);
        provider.stopSession();

        assertTrue(provider.getAllSessions().contains(sessionId));

        provider.deleteSession(sessionId);

        assertFalse(provider.getAllSessions().contains(sessionId));
        assertNull(provider.exportSession(sessionId));
    }

    @Test
    void testGetSessionMetadata() {
        String sessionId = provider.startSession("Meta Test", "TestApp", "Test description");

        provider.recordAction(new ActionRecord.Builder().setActionSuccess(true).build(), null);
        provider.recordAction(new ActionRecord.Builder().setActionSuccess(false).build(), null);

        provider.stopSession();

        var metadata = provider.getSessionMetadata(sessionId);

        assertNotNull(metadata);
        assertEquals("Meta Test", metadata.getName());
        assertEquals("TestApp", metadata.getApplication());
        assertEquals("Test description", metadata.getMetadata());
        assertEquals(2, metadata.getTotalActions());
        assertEquals(1, metadata.getSuccessfulActions());
        assertEquals(50.0, metadata.getSuccessRate(), 0.01);
    }

    @Test
    void testMemoryStatistics() {
        // Create sessions with records
        for (int i = 0; i < 3; i++) {
            provider.startSession("Session " + i, "App", null);
            for (int j = 0; j < 5; j++) {
                provider.recordAction(new ActionRecord.Builder().build(), null);
            }
            provider.stopSession();
        }

        InMemoryPersistenceProvider.MemoryStatistics stats = provider.getMemoryStatistics();

        assertEquals(3, stats.totalSessions);
        assertEquals(15, stats.totalRecords);
        assertTrue(stats.estimatedMemoryBytes > 0);
    }

    @Test
    void testClearAll() {
        // Create some sessions
        provider.startSession("Session 1", "App", null);
        provider.recordAction(new ActionRecord.Builder().build(), null);
        provider.stopSession();

        provider.startSession("Session 2", "App", null);
        provider.recordAction(new ActionRecord.Builder().build(), null);
        provider.stopSession();

        assertEquals(2, provider.getAllSessions().size());

        // Clear all
        provider.clearAll();

        assertEquals(0, provider.getAllSessions().size());
    }

    @Test
    void testNoRecordingWhenNotActive() {
        ActionRecord record = new ActionRecord.Builder().build();

        // Should not record when not recording
        provider.recordAction(record, null);

        // Start and pause - should not record
        String sessionId = provider.startSession("Test", "App", null);
        provider.pauseRecording();
        provider.recordAction(record, null);

        provider.resumeRecording();
        provider.stopSession();

        // Should have no records
        ActionHistory history = provider.exportSession(sessionId);
        assertEquals(0, history.getSnapshots().size());
    }
}
