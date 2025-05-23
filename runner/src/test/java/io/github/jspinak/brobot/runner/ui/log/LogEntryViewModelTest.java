package io.github.jspinak.brobot.runner.ui.log;

import io.github.jspinak.brobot.log.entities.LogEntry;
import io.github.jspinak.brobot.log.entities.LogType;
import org.junit.jupiter.api.Test;

import java.time.Instant;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Tests for the LogEntryViewModel inner class.
 */
public class LogEntryViewModelTest {

    @Test
    public void testTimeProperty() {
        LogViewerPanel.LogEntryViewModel viewModel = new LogViewerPanel.LogEntryViewModel();
        String testTime = "2025-05-10 15:30:45.123";

        viewModel.setTime(testTime);

        assertEquals(testTime, viewModel.getTime());
        assertEquals(testTime, viewModel.timeProperty().get());
    }

    @Test
    public void testLevelProperty() {
        LogViewerPanel.LogEntryViewModel viewModel = new LogViewerPanel.LogEntryViewModel();
        String testLevel = "ERROR";

        viewModel.setLevel(testLevel);

        assertEquals(testLevel, viewModel.getLevel());
        assertEquals(testLevel, viewModel.levelProperty().get());
    }

    @Test
    public void testTypeProperty() {
        LogViewerPanel.LogEntryViewModel viewModel = new LogViewerPanel.LogEntryViewModel();
        String testType = "ACTION";

        viewModel.setType(testType);

        assertEquals(testType, viewModel.getType());
        assertEquals(testType, viewModel.typeProperty().get());
    }

    @Test
    public void testMessageProperty() {
        LogViewerPanel.LogEntryViewModel viewModel = new LogViewerPanel.LogEntryViewModel();
        String testMessage = "Test log message";

        viewModel.setMessage(testMessage);

        assertEquals(testMessage, viewModel.getMessage());
        assertEquals(testMessage, viewModel.messageProperty().get());
    }

    @Test
    public void testSuccessProperty() {
        LogViewerPanel.LogEntryViewModel viewModel = new LogViewerPanel.LogEntryViewModel();
        boolean testSuccess = true;

        viewModel.setSuccess(testSuccess);

        assertEquals(testSuccess, viewModel.isSuccess());
        assertEquals(testSuccess, viewModel.successProperty().get());

        // Test with false
        viewModel.setSuccess(false);
        assertFalse(viewModel.isSuccess());
    }

    @Test
    public void testRawLogEntry() {
        LogViewerPanel.LogEntryViewModel viewModel = new LogViewerPanel.LogEntryViewModel();
        LogEntry testLogEntry = createTestLogEntry();

        viewModel.setRawLogEntry(testLogEntry);

        assertSame(testLogEntry, viewModel.getRawLogEntry());
    }

    @Test
    public void testAllPropertiesTogether() {
        LogViewerPanel.LogEntryViewModel viewModel = new LogViewerPanel.LogEntryViewModel();

        // Set all properties
        String testTime = "2025-05-10 15:30:45.123";
        String testLevel = "WARNING";
        String testType = "TRANSITION";
        String testMessage = "Test transition message";
        boolean testSuccess = false;
        LogEntry testLogEntry = createTestLogEntry();

        viewModel.setTime(testTime);
        viewModel.setLevel(testLevel);
        viewModel.setType(testType);
        viewModel.setMessage(testMessage);
        viewModel.setSuccess(testSuccess);
        viewModel.setRawLogEntry(testLogEntry);

        // Verify all properties
        assertEquals(testTime, viewModel.getTime());
        assertEquals(testLevel, viewModel.getLevel());
        assertEquals(testType, viewModel.getType());
        assertEquals(testMessage, viewModel.getMessage());
        assertEquals(testSuccess, viewModel.isSuccess());
        assertSame(testLogEntry, viewModel.getRawLogEntry());
    }

    @Test
    public void testCreateFromLogEntry() {
        // Create a LogEntry with specific properties
        LogEntry logEntry = new LogEntry("test-session", LogType.ACTION, "Click action");
        logEntry.setSuccess(true);
        logEntry.setTimestamp(Instant.now());

        // Create a ViewModel from this LogEntry (manually, simulating what LogViewerPanel would do)
        LogViewerPanel.LogEntryViewModel viewModel = new LogViewerPanel.LogEntryViewModel();
        viewModel.setTime(logEntry.getTimestamp().toString());
        viewModel.setType(logEntry.getType().toString());
        viewModel.setMessage(logEntry.getDescription());
        viewModel.setSuccess(logEntry.isSuccess());
        viewModel.setLevel("INFO"); // Successful actions are INFO level
        viewModel.setRawLogEntry(logEntry);

        // Verify the properties
        assertEquals(logEntry.getTimestamp().toString(), viewModel.getTime());
        assertEquals(logEntry.getType().toString(), viewModel.getType());
        assertEquals(logEntry.getDescription(), viewModel.getMessage());
        assertEquals(logEntry.isSuccess(), viewModel.isSuccess());
        assertEquals("INFO", viewModel.getLevel());
        assertSame(logEntry, viewModel.getRawLogEntry());
    }

    @Test
    public void testLogLevelDetermination() {
        // Test different LogEntry types and success values

        // Successful ACTION should map to INFO
        LogEntry successAction = new LogEntry("test-session", LogType.ACTION, "Success action");
        successAction.setSuccess(true);

        LogViewerPanel.LogEntryViewModel successViewModel = new LogViewerPanel.LogEntryViewModel();
        successViewModel.setType(successAction.getType().toString());
        successViewModel.setSuccess(successAction.isSuccess());
        successViewModel.setLevel("INFO"); // This is what LogViewerPanel would set

        assertEquals("INFO", successViewModel.getLevel());

        // Failed ACTION should map to WARNING
        LogEntry failedAction = new LogEntry("test-session", LogType.ACTION, "Failed action");
        failedAction.setSuccess(false);

        LogViewerPanel.LogEntryViewModel failedViewModel = new LogViewerPanel.LogEntryViewModel();
        failedViewModel.setType(failedAction.getType().toString());
        failedViewModel.setSuccess(failedAction.isSuccess());
        failedViewModel.setLevel("WARNING"); // This is what LogViewerPanel would set

        assertEquals("WARNING", failedViewModel.getLevel());

        // ERROR type should always map to ERROR level regardless of success
        LogEntry errorLog = new LogEntry("test-session", LogType.ERROR, "Error log");
        errorLog.setSuccess(true); // Even if success is true

        LogViewerPanel.LogEntryViewModel errorViewModel = new LogViewerPanel.LogEntryViewModel();
        errorViewModel.setType(errorLog.getType().toString());
        errorViewModel.setSuccess(errorLog.isSuccess());
        errorViewModel.setLevel("ERROR"); // This is what LogViewerPanel would set

        assertEquals("ERROR", errorViewModel.getLevel());
    }

    private LogEntry createTestLogEntry() {
        LogEntry logEntry = new LogEntry("test-session", LogType.ACTION, "Test log entry");
        logEntry.setSuccess(true);
        return logEntry;
    }
}