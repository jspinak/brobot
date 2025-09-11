package io.github.jspinak.brobot.runner.service;

import static org.junit.jupiter.api.Assertions.*;

import java.util.List;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.TestPropertySource;
import org.springframework.transaction.annotation.Transactional;

import io.github.jspinak.brobot.action.basic.find.PatternFindOptions;
import io.github.jspinak.brobot.model.action.ActionHistory;
import io.github.jspinak.brobot.model.action.ActionRecord;
import io.github.jspinak.brobot.model.match.Match;
import io.github.jspinak.brobot.runner.persistence.entity.RecordingSessionEntity;
import io.github.jspinak.brobot.runner.persistence.repository.ActionRecordRepository;
import io.github.jspinak.brobot.runner.persistence.repository.RecordingSessionRepository;

/** Integration tests for ActionRecordingService. */
@SpringBootTest
@TestPropertySource(
        properties = {
            "brobot.runner.recording.enabled=true",
            "brobot.runner.recording.database.async-recording=false", // Sync for testing
            "spring.datasource.url=jdbc:h2:mem:testdb"
        })
@Transactional
class ActionRecordingServiceTest {

    @Autowired private ActionRecordingService recordingService;

    @Autowired private RecordingSessionRepository sessionRepository;

    @Autowired private ActionRecordRepository recordRepository;

    @BeforeEach
    void setUp() {
        // Clean up any existing data
        recordRepository.deleteAll();
        sessionRepository.deleteAll();
    }

    @Test
    void testStartAndStopRecording() {
        // Start recording
        RecordingSessionEntity session =
                recordingService.startRecording("Test Session", "Test App", "Test description");

        assertNotNull(session);
        assertEquals("Test Session", session.getName());
        assertEquals("Test App", session.getApplication());
        assertTrue(recordingService.isRecording());

        // Stop recording
        RecordingSessionEntity stoppedSession = recordingService.stopRecording();

        assertNotNull(stoppedSession);
        assertFalse(recordingService.isRecording());
        assertEquals(RecordingSessionEntity.SessionStatus.COMPLETED, stoppedSession.getStatus());
    }

    @Test
    void testRecordAction() {
        // Start recording
        RecordingSessionEntity session =
                recordingService.startRecording("Test Session", "Test App", null);

        // Create test ActionRecord
        ActionRecord record =
                new ActionRecord.Builder()
                        .setActionConfig(
                                new PatternFindOptions.Builder()
                                        .setStrategy(PatternFindOptions.Strategy.BEST)
                                        .setSimilarity(0.85)
                                        .build())
                        .setActionSuccess(true)
                        .setDuration(250)
                        .addMatch(
                                new Match.Builder()
                                        .setRegion(100, 200, 50, 30)
                                        .setSimScore(0.92)
                                        .build())
                        .build();

        // Record the action (synchronously for testing)
        recordingService.recordActionSync(record, null);

        // Verify it was recorded
        List<RecordingSessionEntity> sessions = sessionRepository.findAll();
        assertEquals(1, sessions.size());

        RecordingSessionEntity savedSession = sessions.get(0);
        assertEquals(1, savedSession.getTotalActions());
        assertEquals(1, savedSession.getSuccessfulActions());
        assertEquals(0, savedSession.getFailedActions());

        // Stop recording
        recordingService.stopRecording();
    }

    @Test
    void testExportSession() {
        // Start recording and add some records
        RecordingSessionEntity session =
                recordingService.startRecording("Export Test", "Test App", null);

        // Add multiple records
        for (int i = 0; i < 5; i++) {
            ActionRecord record =
                    new ActionRecord.Builder()
                            .setActionConfig(new PatternFindOptions.Builder().build())
                            .setActionSuccess(i % 2 == 0) // Alternate success/failure
                            .setDuration(100 + i * 50)
                            .build();

            recordingService.recordActionSync(record, null);
        }

        recordingService.stopRecording();

        // Export the session
        ActionHistory exported = recordingService.exportSession(session.getId());

        assertNotNull(exported);
        assertEquals(5, exported.getSnapshots().size());
        assertEquals(5, exported.getTimesSearched());
        assertEquals(3, exported.getTimesFound()); // 3 successful (0, 2, 4)
    }

    @Test
    void testImportSession() {
        // Create ActionHistory to import
        ActionHistory history = new ActionHistory();

        for (int i = 0; i < 3; i++) {
            ActionRecord record =
                    new ActionRecord.Builder()
                            .setActionConfig(new PatternFindOptions.Builder().build())
                            .setActionSuccess(true)
                            .setDuration(200)
                            .build();
            history.addSnapshot(record);
        }

        // Import as new session
        RecordingSessionEntity imported =
                recordingService.importSession(history, "Imported Session", "Test App");

        assertNotNull(imported);
        assertEquals("Imported Session", imported.getName());
        assertEquals(3, imported.getTotalActions());
        assertEquals(3, imported.getSuccessfulActions());
        assertTrue(imported.isImported());
        assertEquals(RecordingSessionEntity.SessionStatus.COMPLETED, imported.getStatus());
    }

    @Test
    void testPauseAndResumeRecording() {
        // Start recording
        recordingService.startRecording("Pause Test", "Test App", null);
        assertTrue(recordingService.isRecording());

        // Pause
        recordingService.pauseRecording();
        assertFalse(recordingService.isRecording());

        // Resume
        recordingService.resumeRecording();
        assertTrue(recordingService.isRecording());

        // Stop
        recordingService.stopRecording();
        assertFalse(recordingService.isRecording());
    }

    @Test
    void testBatchRecording() {
        // Start recording
        recordingService.startRecording("Batch Test", "Test App", null);

        // Create batch of records
        List<ActionRecord> batch =
                List.of(
                        new ActionRecord.Builder()
                                .setActionConfig(new PatternFindOptions.Builder().build())
                                .setActionSuccess(true)
                                .build(),
                        new ActionRecord.Builder()
                                .setActionConfig(new PatternFindOptions.Builder().build())
                                .setActionSuccess(false)
                                .build(),
                        new ActionRecord.Builder()
                                .setActionConfig(new PatternFindOptions.Builder().build())
                                .setActionSuccess(true)
                                .build());

        // Record batch
        recordingService.recordBatch(batch);

        // Stop and verify
        RecordingSessionEntity session = recordingService.stopRecording();

        assertEquals(3, session.getTotalActions());
        assertEquals(2, session.getSuccessfulActions());
        assertEquals(1, session.getFailedActions());
    }
}
