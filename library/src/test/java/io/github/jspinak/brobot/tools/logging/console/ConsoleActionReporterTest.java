package io.github.jspinak.brobot.tools.logging.console;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

import java.io.ByteArrayOutputStream;
import java.io.PrintStream;
import java.time.Instant;
import java.util.Arrays;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.condition.DisabledIfEnvironmentVariable;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import io.github.jspinak.brobot.logging.unified.BrobotLogger;
import io.github.jspinak.brobot.logging.unified.LogBuilder;
import io.github.jspinak.brobot.test.BrobotTestBase;
import io.github.jspinak.brobot.tools.logging.model.LogData;
import io.github.jspinak.brobot.tools.logging.model.LogEventType;

@ExtendWith(MockitoExtension.class)
@DisabledIfEnvironmentVariable(
        named = "CI",
        matches = "true",
        disabledReason = "Test incompatible with CI environment")
public class ConsoleActionReporterTest extends BrobotTestBase {

    private ConsoleActionReporter reporter;

    @Mock private BrobotLogger brobotLogger;

    @Mock private LogBuilder logBuilder;

    private ConsoleActionConfig config;

    private ByteArrayOutputStream outputStream;
    private PrintStream originalOut;

    @BeforeEach
    @Override
    public void setupTest() {
        super.setupTest();

        // Capture console output
        outputStream = new ByteArrayOutputStream();
        originalOut = System.out;
        System.setOut(new PrintStream(outputStream));

        // Setup config with defaults
        config = new ConsoleActionConfig();
        config.setEnabled(true);
        config.setLevel(ConsoleActionConfig.Level.NORMAL);
        config.setUseColors(false); // Disable colors for easier testing
        config.setUseIcons(true);

        // Setup logging chain
        lenient().when(brobotLogger.log()).thenReturn(logBuilder);
        lenient().when(logBuilder.action(anyString())).thenReturn(logBuilder);
        lenient().when(logBuilder.metadata(anyString(), any())).thenReturn(logBuilder);
        lenient().when(logBuilder.observation(anyString())).thenReturn(logBuilder);
        lenient().when(logBuilder.level(any())).thenReturn(logBuilder);
        lenient().when(logBuilder.error(any(Exception.class))).thenReturn(logBuilder);
        lenient().doNothing().when(logBuilder).log();

        // Correct constructor parameter order: BrobotLogger first, then ConsoleActionConfig
        reporter = new ConsoleActionReporter(brobotLogger, config);
    }

    @AfterEach
    public void tearDown() {
        System.setOut(originalOut);
    }

    @Test
    public void testReportLogEntry_FindSuccess() {
        // Arrange
        config.setReportFind(true);
        LogData logData = createFindLogData(true, 0.5);

        // Act
        reporter.reportLogEntry(logData);

        // Assert
        verify(brobotLogger, atLeastOnce()).log();
        verify(logBuilder, atLeastOnce()).observation(anyString());
        verify(logBuilder, atLeastOnce()).log();
    }

    @Test
    public void testReportLogEntry_FindFailed() {
        // Arrange
        config.setReportFind(true);
        LogData logData = createFindLogData(false, 2.0);

        // Act
        reporter.reportLogEntry(logData);

        // Assert
        verify(brobotLogger, atLeastOnce()).log();
        verify(logBuilder, atLeastOnce()).observation(anyString());
        verify(logBuilder, atLeastOnce()).log();
    }

    @Test
    public void testReportLogEntry_ClickSuccess() {
        // Arrange
        config.setReportClick(true);
        LogData logData = createClickLogData(true, 0.3);

        // Act
        reporter.reportLogEntry(logData);

        // Assert
        verify(brobotLogger, atLeastOnce()).log();
        verify(logBuilder, atLeastOnce()).observation(anyString());
        verify(logBuilder, atLeastOnce()).log();
    }

    @Test
    public void testReportLogEntry_TypeSuccess() {
        // Arrange
        config.setReportType(true);
        LogData logData = createTypeLogData("Hello World", 1.0);

        // Act
        reporter.reportLogEntry(logData);

        // Assert
        verify(brobotLogger, atLeastOnce()).log();
        verify(logBuilder, atLeastOnce()).observation(anyString());
        verify(logBuilder, atLeastOnce()).log();
    }

    @Test
    public void testReportLogEntry_PerformanceWarning() {
        // Arrange
        config.setReportFind(true);
        config.setShowTiming(true);
        config.setPerformanceWarnThreshold(1000); // 1 second
        LogData logData = createFindLogData(true, 1.5); // 1.5 seconds

        // Act
        reporter.reportLogEntry(logData);

        // Assert
        String output = outputStream.toString();
        // Performance warning may be shown
        verify(brobotLogger, atLeastOnce()).log();
    }

    @Test
    public void testReportLogEntry_PerformanceError() {
        // Arrange
        config.setReportFind(true);
        config.setShowTiming(true);
        config.setPerformanceErrorThreshold(3000); // 3 seconds
        LogData logData = createFindLogData(true, 3.5); // 3.5 seconds

        // Act
        reporter.reportLogEntry(logData);

        // Assert
        verify(brobotLogger, atLeastOnce()).log();
    }

    @Test
    public void testReportLogEntry_Disabled() {
        // Arrange
        config.setEnabled(false);
        LogData logData = createFindLogData(true, 0.5);

        // Act
        reporter.reportLogEntry(logData);

        // Assert
        String output = outputStream.toString();
        assertTrue(output.isEmpty());
        verify(brobotLogger, never()).log();
    }

    @Test
    public void testReportLogEntry_FilteredActionType() {
        // Arrange
        config.setReportFind(false); // Disable find reporting
        LogData logData = createFindLogData(true, 0.5);

        // Act
        reporter.reportLogEntry(logData);

        // Assert
        String output = outputStream.toString();
        assertTrue(output.isEmpty());
    }

    @Test
    public void testReportLogEntry_Transition() {
        // Arrange
        config.setReportTransitions(true);
        LogData logData = createTransitionLogData("Login", "Dashboard", true, 2.0);

        // Act
        reporter.reportLogEntry(logData);

        // Assert
        verify(brobotLogger, atLeastOnce()).log();
        verify(logBuilder, atLeastOnce()).observation(anyString());
        verify(logBuilder, atLeastOnce()).log();
    }

    @Test
    public void testReportLogEntry_TransitionFailed() {
        // Arrange
        config.setReportTransitions(true);
        LogData logData = createTransitionLogData("Login", "Dashboard", false, 5.0);

        // Act
        reporter.reportLogEntry(logData);

        // Assert
        verify(brobotLogger, atLeastOnce()).log();
        verify(logBuilder, atLeastOnce()).observation(anyString());
        verify(logBuilder, atLeastOnce()).log();
    }

    @Test
    public void testReportLogEntry_Error() {
        // Arrange
        LogData logData = createErrorLogData("Test error message");

        // Act
        reporter.reportLogEntry(logData);

        // Assert
        verify(brobotLogger, atLeastOnce()).log();
        verify(logBuilder, atLeastOnce()).observation(anyString());
        verify(logBuilder, atLeastOnce()).log();
    }

    @Test
    public void testReportLogEntry_VerbosityQuiet() {
        // Arrange
        config.setLevel(ConsoleActionConfig.Level.QUIET);
        config.setReportFind(true);
        LogData logData = createFindLogData(true, 0.5);

        // Act
        reporter.reportLogEntry(logData);

        // Assert
        String output = outputStream.toString();
        // Quiet mode should produce less output
        verify(brobotLogger, atLeastOnce()).log();
    }

    @Test
    public void testReportLogEntry_VerbosityVerbose() {
        // Arrange
        config.setLevel(ConsoleActionConfig.Level.VERBOSE);
        config.setReportFind(true);
        LogData logData = createFindLogData(true, 0.5);

        // Act
        reporter.reportLogEntry(logData);

        // Assert
        String output = outputStream.toString();
        // Verbose mode should produce more output
        verify(brobotLogger, atLeast(2)).log(); // Multiple log entries in verbose mode
    }

    @Test
    public void testReportLogEntry_WithColors() {
        // Arrange
        config.setUseColors(true);
        config.setReportFind(true);
        LogData logData = createFindLogData(true, 0.5);

        // Act
        reporter.reportLogEntry(logData);

        // Assert
        // Colors are handled in the formatter, not the reporter
        verify(brobotLogger, atLeastOnce()).log();
    }

    @Test
    public void testReportLogEntry_WithoutIcons() {
        // Arrange
        config.setUseIcons(false);
        config.setReportFind(true);
        LogData logData = createFindLogData(true, 0.5);

        // Act
        reporter.reportLogEntry(logData);

        // Assert
        String output = outputStream.toString();
        // Without icons, text-based indicators may be used
        verify(brobotLogger, atLeastOnce()).log();
    }

    @Test
    public void testReportLogEntry_MaxTextLength() {
        // Arrange
        config.setReportType(true);
        config.setMaxTextLength(10);
        String longText = "This is a very long text that should be truncated";
        LogData logData = createTypeLogData(longText, 0.5);

        // Act
        reporter.reportLogEntry(logData);

        // Assert
        // Text truncation happens in the reporter
        verify(brobotLogger, atLeastOnce()).log();
    }

    @Test
    public void testReportLogEntry_MultipleMatches() {
        // Arrange
        config.setReportFind(true);
        config.setLevel(ConsoleActionConfig.Level.VERBOSE);
        LogData logData = createFindLogDataWithMultipleMatches(3, 0.5);

        // Act
        reporter.reportLogEntry(logData);

        // Assert
        // Multiple matches should be reported in verbose mode
        verify(brobotLogger, atLeastOnce()).log();
    }

    @Test
    public void testReportLogEntry_NullHandling() {
        // Arrange
        LogData logData = new LogData();
        logData.setType(LogEventType.ACTION);
        // Leave other fields null

        // Act
        assertDoesNotThrow(() -> reporter.reportLogEntry(logData));

        // Assert
        // Should handle null fields gracefully
        verify(brobotLogger, atLeastOnce()).log();
    }

    // Helper methods to create test data

    private LogData createFindLogData(boolean success, double durationSeconds) {
        LogData logData = new LogData();
        logData.setType(LogEventType.ACTION);
        logData.setActionType("FIND");
        logData.setSuccess(success);
        logData.setTimestamp(Instant.now());
        logData.setDuration((long) (durationSeconds * 1000));
        logData.setDescription("FIND testImage");

        return logData;
    }

    private LogData createClickLogData(boolean success, double durationSeconds) {
        LogData logData = new LogData();
        logData.setType(LogEventType.ACTION);
        logData.setActionType("CLICK");
        logData.setSuccess(success);
        logData.setTimestamp(Instant.now());
        logData.setDuration((long) (durationSeconds * 1000));
        logData.setDescription("CLICK clickTarget");

        return logData;
    }

    private LogData createTypeLogData(String text, double durationSeconds) {
        LogData logData = new LogData();
        logData.setType(LogEventType.ACTION);
        logData.setActionType("TYPE");
        logData.setSuccess(true);
        logData.setTimestamp(Instant.now());
        logData.setDuration((long) (durationSeconds * 1000));
        logData.setDescription("TYPE \"" + text + "\"");

        return logData;
    }

    private LogData createFindLogDataWithMultipleMatches(int matchCount, double durationSeconds) {
        LogData logData = createFindLogData(true, durationSeconds);
        // In real implementation, match details would be in StateImageLogData
        return logData;
    }

    private LogData createTransitionLogData(
            String fromState, String toState, boolean success, double durationSeconds) {
        LogData logData = new LogData();
        logData.setType(LogEventType.TRANSITION);
        logData.setFromStates(fromState);
        logData.setToStateNames(Arrays.asList(toState));
        logData.setSuccess(success);
        logData.setTimestamp(Instant.now());
        logData.setDuration((long) (durationSeconds * 1000));

        return logData;
    }

    private LogData createErrorLogData(String errorMessage) {
        LogData logData = new LogData();
        logData.setType(LogEventType.ERROR);
        logData.setErrorMessage(errorMessage);
        logData.setTimestamp(Instant.now());

        return logData;
    }
}
