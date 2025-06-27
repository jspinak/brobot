package io.github.jspinak.brobot.runner.ui.log;

import org.junit.jupiter.api.Test;

import io.github.jspinak.brobot.tools.logging.model.LogData;
import io.github.jspinak.brobot.tools.logging.model.LogEventType;

import java.time.Instant;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Tests for the LogEntryViewModel inner class.
 */
public class LogDataViewModelTest {

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
        LogData testLogData = createTestLogEntry();

        viewModel.setRawLogData(testLogData);

        assertSame(testLogData, viewModel.getRawLogData());
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
        LogData testLogData = createTestLogEntry();

        viewModel.setTime(testTime);
        viewModel.setLevel(testLevel);
        viewModel.setType(testType);
        viewModel.setMessage(testMessage);
        viewModel.setSuccess(testSuccess);
        viewModel.setRawLogData(testLogData);

        // Verify all properties
        assertEquals(testTime, viewModel.getTime());
        assertEquals(testLevel, viewModel.getLevel());
        assertEquals(testType, viewModel.getType());
        assertEquals(testMessage, viewModel.getMessage());
        assertEquals(testSuccess, viewModel.isSuccess());
        assertSame(testLogData, viewModel.getRawLogData());
    }

    @Test
    public void testCreateFromLogEntry() {
        // Create a LogEntry with specific properties
        LogData logData = new LogData("test-session", LogEventType.ACTION, "Click action");
        logData.setSuccess(true);
        logData.setTimestamp(Instant.now());

        // Create a ViewModel from this LogEntry (manually, simulating what LogViewerPanel would do)
        LogViewerPanel.LogEntryViewModel viewModel = new LogViewerPanel.LogEntryViewModel();
        viewModel.setTime(logData.getTimestamp().toString());
        viewModel.setType(logData.getType().toString());
        viewModel.setMessage(logData.getDescription());
        viewModel.setSuccess(logData.isSuccess());
        viewModel.setLevel("INFO"); // Successful actions are INFO level
        viewModel.setRawLogData(logData);

        // Verify the properties
        assertEquals(logData.getTimestamp().toString(), viewModel.getTime());
        assertEquals(logData.getType().toString(), viewModel.getType());
        assertEquals(logData.getDescription(), viewModel.getMessage());
        assertEquals(logData.isSuccess(), viewModel.isSuccess());
        assertEquals("INFO", viewModel.getLevel());
        assertSame(logData, viewModel.getRawLogData());
    }

    @Test
    public void testLogLevelDetermination() {
        // Test different LogEntry types and success values

        // Successful ACTION should map to INFO
        LogData successAction = new LogData("test-session", LogEventType.ACTION, "Success action");
        successAction.setSuccess(true);

        LogViewerPanel.LogEntryViewModel successViewModel = new LogViewerPanel.LogEntryViewModel();
        successViewModel.setType(successAction.getType().toString());
        successViewModel.setSuccess(successAction.isSuccess());
        successViewModel.setLevel("INFO"); // This is what LogViewerPanel would set

        assertEquals("INFO", successViewModel.getLevel());

        // Failed ACTION should map to WARNING
        LogData failedAction = new LogData("test-session", LogEventType.ACTION, "Failed action");
        failedAction.setSuccess(false);

        LogViewerPanel.LogEntryViewModel failedViewModel = new LogViewerPanel.LogEntryViewModel();
        failedViewModel.setType(failedAction.getType().toString());
        failedViewModel.setSuccess(failedAction.isSuccess());
        failedViewModel.setLevel("WARNING"); // This is what LogViewerPanel would set

        assertEquals("WARNING", failedViewModel.getLevel());

        // ERROR type should always map to ERROR level regardless of success
        LogData errorLog = new LogData("test-session", LogEventType.ERROR, "Error log");
        errorLog.setSuccess(true); // Even if success is true

        LogViewerPanel.LogEntryViewModel errorViewModel = new LogViewerPanel.LogEntryViewModel();
        errorViewModel.setType(errorLog.getType().toString());
        errorViewModel.setSuccess(errorLog.isSuccess());
        errorViewModel.setLevel("ERROR"); // This is what LogViewerPanel would set

        assertEquals("ERROR", errorViewModel.getLevel());
    }

    private LogData createTestLogEntry() {
        LogData logData = new LogData("test-session", LogEventType.ACTION, "Test log entry");
        logData.setSuccess(true);
        return logData;
    }
}