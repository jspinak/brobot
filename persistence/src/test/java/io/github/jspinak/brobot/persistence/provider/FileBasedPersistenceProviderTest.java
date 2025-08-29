package io.github.jspinak.brobot.persistence.provider;

import io.github.jspinak.brobot.action.basic.find.PatternFindOptions;
import io.github.jspinak.brobot.model.action.ActionHistory;
import io.github.jspinak.brobot.model.action.ActionRecord;
import io.github.jspinak.brobot.persistence.config.PersistenceConfiguration;
import io.github.jspinak.brobot.test.BrobotTestBase;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Unit tests for FileBasedPersistenceProvider.
 */
class FileBasedPersistenceProviderTest extends BrobotTestBase {
    
    @TempDir
    Path tempDir;
    
    private FileBasedPersistenceProvider provider;
    private PersistenceConfiguration configuration;
    
    @BeforeEach
    @Override
    public void setupTest() {
        super.setupTest();
        configuration = PersistenceConfiguration.fileDefault();
        configuration.getFile().setBasePath(tempDir.toString());
        configuration.getFile().setPrettyPrint(false); // Faster for tests
        configuration.getPerformance().setAsyncRecording(false); // Sync for testing
        provider = new FileBasedPersistenceProvider(configuration);
    }
    
    @AfterEach
    void tearDown() {
        provider.shutdown();
    }
    
    @Test
    void testStartAndStopSession() throws Exception {
        String sessionId = provider.startSession("Test Session", "TestApp", "metadata");
        
        assertNotNull(sessionId);
        assertTrue(provider.isRecording());
        
        String stoppedId = provider.stopSession();
        assertEquals(sessionId, stoppedId);
        assertFalse(provider.isRecording());
        
        // Verify session directory was created
        Path sessionsDir = tempDir.resolve("sessions");
        assertTrue(Files.exists(sessionsDir));
        assertTrue(Files.list(sessionsDir).findAny().isPresent());
    }
    
    @Test
    void testRecordAction() {
        String sessionId = provider.startSession("Record Test", "App", null);
        
        ActionRecord record = new ActionRecord.Builder()
            .setActionConfig(new PatternFindOptions.Builder()
                .setStrategy(PatternFindOptions.Strategy.BEST)
                .build())
            .setActionSuccess(true)
            .setDuration(250)
            .setText("Test action")
            .build();
        
        provider.recordAction(record, null);
        provider.stopSession();
        
        // Export and verify
        ActionHistory history = provider.exportSession(sessionId);
        assertNotNull(history);
        assertEquals(1, history.getSnapshots().size());
        
        ActionRecord exported = history.getSnapshots().get(0);
        assertEquals(record.isActionSuccess(), exported.isActionSuccess());
        assertEquals(record.getDuration(), exported.getDuration());
        assertEquals(record.getText(), exported.getText());
    }
    
    @Test
    void testBuffering() throws IOException {
        configuration.getPerformance().setBufferSize(3);
        provider = new FileBasedPersistenceProvider(configuration);
        
        String sessionId = provider.startSession("Buffer Test", "App", null);
        
        // Add records up to buffer size - shouldn't flush yet
        for (int i = 0; i < 3; i++) {
            provider.recordAction(new ActionRecord.Builder()
                .setText("Record " + i)
                .setActionConfig(new PatternFindOptions.Builder().build())
                .build(), null);
        }
        
        // One more should trigger flush
        provider.recordAction(new ActionRecord.Builder()
            .setText("Record 3")
            .setActionConfig(new PatternFindOptions.Builder().build())
            .build(), null);
        
        provider.stopSession();
        
        // All records should be persisted
        ActionHistory history = provider.exportSession(sessionId);
        assertEquals(4, history.getSnapshots().size());
    }
    
    @Test
    void testJsonFormat() {
        configuration.getFile().setFormat(PersistenceConfiguration.FileFormat.JSON);
        provider = new FileBasedPersistenceProvider(configuration);
        
        String sessionId = provider.startSession("JSON Test", "App", null);
        provider.recordAction(new ActionRecord.Builder()
            .setActionSuccess(true)
            .setText("JSON record")
            .build(), null);
        provider.stopSession();
        
        ActionHistory history = provider.exportSession(sessionId);
        assertEquals(1, history.getSnapshots().size());
        assertEquals("JSON record", history.getSnapshots().get(0).getText());
    }
    
    @Test
    void testCsvFormat() {
        configuration.getFile().setFormat(PersistenceConfiguration.FileFormat.CSV);
        provider = new FileBasedPersistenceProvider(configuration);
        
        String sessionId = provider.startSession("CSV Test", "App", null);
        provider.recordAction(new ActionRecord.Builder()
            .setActionSuccess(true)
            .setDuration(100)
            .setText("CSV record")
            .build(), null);
        provider.stopSession();
        
        // CSV has limited field support, but basic fields should work
        ActionHistory history = provider.exportSession(sessionId);
        assertEquals(1, history.getSnapshots().size());
        
        ActionRecord record = history.getSnapshots().get(0);
        assertTrue(record.isActionSuccess());
        assertEquals(100, record.getDuration());
        assertEquals("CSV record", record.getText());
    }
    
    @Test
    void testCompression() {
        configuration.getFile().setCompressExports(true);
        configuration.getFile().setFormat(PersistenceConfiguration.FileFormat.JSON);
        provider = new FileBasedPersistenceProvider(configuration);
        
        String sessionId = provider.startSession("Compression Test", "App", null);
        
        // Add some records
        for (int i = 0; i < 5; i++) {
            provider.recordAction(new ActionRecord.Builder()
                .setText("Record " + i)
                .setDuration(i * 100)
                .build(), null);
        }
        
        provider.stopSession();
        
        // Should still be able to read compressed files
        ActionHistory history = provider.exportSession(sessionId);
        assertEquals(5, history.getSnapshots().size());
    }
    
    @Test
    void testImportSession() {
        ActionHistory toImport = new ActionHistory();
        for (int i = 0; i < 3; i++) {
            toImport.addSnapshot(new ActionRecord.Builder()
                .setActionSuccess(i % 2 == 0)
                .setText("Imported " + i)
                .build());
        }
        
        String importedId = provider.importSession(toImport, "Imported Session");
        assertNotNull(importedId);
        
        ActionHistory exported = provider.exportSession(importedId);
        assertEquals(3, exported.getSnapshots().size());
        assertEquals("Imported 0", exported.getSnapshots().get(0).getText());
    }
    
    @Test
    void testDeleteSession() throws Exception {
        String sessionId = provider.startSession("To Delete", "App", null);
        provider.recordAction(new ActionRecord.Builder().build(), null);
        provider.stopSession();
        
        assertTrue(provider.getAllSessions().contains(sessionId));
        
        // Get session path before deletion
        Path sessionsDir = tempDir.resolve("sessions");
        long dirCountBefore = Files.list(sessionsDir).count();
        
        provider.deleteSession(sessionId);
        
        assertFalse(provider.getAllSessions().contains(sessionId));
        assertNull(provider.exportSession(sessionId));
        
        // Verify directory was deleted
        long dirCountAfter = Files.list(sessionsDir).count();
        assertEquals(dirCountBefore - 1, dirCountAfter);
    }
    
    @Test
    void testMultipleSessions() {
        // Create multiple sessions
        String session1 = provider.startSession("Session 1", "App1", null);
        provider.recordAction(new ActionRecord.Builder().setText("S1R1").build(), null);
        provider.stopSession();
        
        String session2 = provider.startSession("Session 2", "App2", null);
        provider.recordAction(new ActionRecord.Builder().setText("S2R1").build(), null);
        provider.recordAction(new ActionRecord.Builder().setText("S2R2").build(), null);
        provider.stopSession();
        
        String session3 = provider.startSession("Session 3", "App3", null);
        provider.recordAction(new ActionRecord.Builder().setText("S3R1").build(), null);
        provider.stopSession();
        
        // Verify all sessions exist
        List<String> sessions = provider.getAllSessions();
        assertEquals(3, sessions.size());
        assertTrue(sessions.contains(session1));
        assertTrue(sessions.contains(session2));
        assertTrue(sessions.contains(session3));
        
        // Verify each session's content
        ActionHistory h1 = provider.exportSession(session1);
        assertEquals(1, h1.getSnapshots().size());
        assertEquals("S1R1", h1.getSnapshots().get(0).getText());
        
        ActionHistory h2 = provider.exportSession(session2);
        assertEquals(2, h2.getSnapshots().size());
        assertEquals("S2R1", h2.getSnapshots().get(0).getText());
        assertEquals("S2R2", h2.getSnapshots().get(1).getText());
    }
    
    @Test
    void testSessionMetadata() {
        String sessionId = provider.startSession("Meta Test", "TestApp", "Custom metadata");
        
        provider.recordAction(new ActionRecord.Builder()
            .setActionSuccess(true)
            .build(), null);
        provider.recordAction(new ActionRecord.Builder()
            .setActionSuccess(false)
            .build(), null);
        
        var metadata = provider.getSessionMetadata(sessionId);
        
        assertNotNull(metadata);
        assertEquals("Meta Test", metadata.getName());
        assertEquals("TestApp", metadata.getApplication());
        assertEquals("Custom metadata", metadata.getMetadata());
        assertEquals(2, metadata.getTotalActions());
        assertEquals(1, metadata.getSuccessfulActions());
        
        provider.stopSession();
    }
    
    @Test
    void testPersistenceAcrossInstances() {
        // Create session with first instance
        String sessionId;
        {
            FileBasedPersistenceProvider provider1 = new FileBasedPersistenceProvider(configuration);
            sessionId = provider1.startSession("Persistent", "App", null);
            provider1.recordAction(new ActionRecord.Builder()
                .setText("Persisted record")
                .setActionConfig(new PatternFindOptions.Builder().build())
                .build(), null);
            provider1.stopSession();
            provider1.shutdown();
        }
        
        // Load with second instance using the SAME configuration (same temp dir)
        {
            FileBasedPersistenceProvider provider2 = new FileBasedPersistenceProvider(configuration);
            
            assertTrue(provider2.getAllSessions().contains(sessionId));
            
            ActionHistory history = provider2.exportSession(sessionId);
            assertEquals(1, history.getSnapshots().size());
            assertEquals("Persisted record", history.getSnapshots().get(0).getText());
            
            provider2.shutdown();
        }
    }
}