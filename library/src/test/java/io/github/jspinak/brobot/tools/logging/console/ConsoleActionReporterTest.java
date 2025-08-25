package io.github.jspinak.brobot.tools.logging.console;

import io.github.jspinak.brobot.action.ActionType;
import io.github.jspinak.brobot.action.basic.find.PatternFindOptions;
import io.github.jspinak.brobot.action.basic.click.ClickOptions;
import io.github.jspinak.brobot.action.basic.type.TypeOptions;
import io.github.jspinak.brobot.logging.unified.BrobotLogger;
import io.github.jspinak.brobot.logging.unified.LogBuilder;
import io.github.jspinak.brobot.model.match.Match;
import io.github.jspinak.brobot.model.element.Region;
import io.github.jspinak.brobot.test.BrobotTestBase;
import io.github.jspinak.brobot.tools.logging.model.LogData;
import io.github.jspinak.brobot.tools.logging.model.ExecutionMetrics;
import io.github.jspinak.brobot.tools.logging.model.LogEventType;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;

import java.io.ByteArrayOutputStream;
import java.io.PrintStream;
import java.time.Duration;
import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class ConsoleActionReporterTest extends BrobotTestBase {
    
    private ConsoleActionReporter reporter;
    
    @Mock
    private BrobotLogger brobotLogger;
    
    @Mock
    private LogBuilder logBuilder;
    
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
        
        reporter = new ConsoleActionReporter(config, brobotLogger);
    }
    
    @AfterEach
    public void tearDown() {
        System.setOut(originalOut);
    }
    
    @Test
    public void testReportAction_FindSuccess() {
        // Arrange
        config.setReportFind(true);
        LogData logData = createFindLogData(true, 0.5);
        
        // Act
        reporter.reportAction(logData);
        
        // Assert
        String output = outputStream.toString();
        assertTrue(output.contains("✓"));
        assertTrue(output.contains("FIND"));
        assertTrue(output.contains("testImage"));
        assertTrue(output.contains("0.95"));
        assertTrue(output.contains("500ms"));
        
        verify(brobotLogger).log();
        verify(logBuilder).action("FIND");
    }
    
    @Test
    public void testReportAction_FindFailed() {
        // Arrange
        config.setReportFind(true);
        LogData logData = createFindLogData(false, 2.0);
        
        // Act
        reporter.reportAction(logData);
        
        // Assert
        String output = outputStream.toString();
        assertTrue(output.contains("✗"));
        assertTrue(output.contains("FIND"));
        assertTrue(output.contains("testImage"));
        assertTrue(output.contains("2.0s"));
        assertFalse(output.contains("confidence")); // No confidence for failed finds
    }
    
    @Test
    public void testReportAction_ClickSuccess() {
        // Arrange
        config.setReportClick(true);
        LogData logData = createClickLogData(true, 0.3);
        
        // Act
        reporter.reportAction(logData);
        
        // Assert
        String output = outputStream.toString();
        assertTrue(output.contains("✓"));
        assertTrue(output.contains("CLICK"));
        assertTrue(output.contains("clickTarget"));
        assertTrue(output.contains("100,100"));
    }
    
    @Test
    public void testReportAction_TypeSuccess() {
        // Arrange
        config.setReportType(true);
        LogData logData = createTypeLogData("Hello World", 1.0);
        
        // Act
        reporter.reportAction(logData);
        
        // Assert
        String output = outputStream.toString();
        assertTrue(output.contains("✓"));
        assertTrue(output.contains("TYPE"));
        assertTrue(output.contains("Hello World"));
        assertTrue(output.contains("1.0s"));
    }
    
    @Test
    public void testReportAction_PerformanceWarning() {
        // Arrange
        config.setReportFind(true);
        config.setPerformanceWarnThreshold(1000); // 1 second
        LogData logData = createFindLogData(true, 1.5); // 1.5 seconds
        
        // Act
        reporter.reportAction(logData);
        
        // Assert
        String output = outputStream.toString();
        assertTrue(output.contains("⚠")); // Warning icon
        assertTrue(output.contains("1.5s"));
        assertTrue(output.contains("SLOW"));
    }
    
    @Test
    public void testReportAction_PerformanceError() {
        // Arrange
        config.setReportFind(true);
        config.setPerformanceErrorThreshold(3000); // 3 seconds
        LogData logData = createFindLogData(true, 3.5); // 3.5 seconds
        
        // Act
        reporter.reportAction(logData);
        
        // Assert
        String output = outputStream.toString();
        assertTrue(output.contains("⚠")); // Warning icon for very slow
        assertTrue(output.contains("3.5s"));
    }
    
    @Test
    public void testReportAction_Disabled() {
        // Arrange
        config.setEnabled(false);
        LogData logData = createFindLogData(true, 0.5);
        
        // Act
        reporter.reportAction(logData);
        
        // Assert
        String output = outputStream.toString();
        assertTrue(output.isEmpty());
        verify(brobotLogger, never()).log();
    }
    
    @Test
    public void testReportAction_FilteredActionType() {
        // Arrange
        config.setReportFind(false); // Disable find reporting
        LogData logData = createFindLogData(true, 0.5);
        
        // Act
        reporter.reportAction(logData);
        
        // Assert
        String output = outputStream.toString();
        assertTrue(output.isEmpty());
    }
    
    @Test
    public void testReportTransition() {
        // Arrange
        String fromState = "Login";
        String toState = "Dashboard";
        boolean success = true;
        Duration duration = Duration.ofSeconds(2);
        
        // Act
        reporter.reportTransition(fromState, toState, success, duration);
        
        // Assert
        String output = outputStream.toString();
        assertTrue(output.contains("→"));
        assertTrue(output.contains("Login"));
        assertTrue(output.contains("Dashboard"));
        assertTrue(output.contains("2.0s"));
        
        verify(brobotLogger).log();
        verify(logBuilder).action("TRANSITION");
    }
    
    @Test
    public void testReportTransition_Failed() {
        // Arrange
        String fromState = "Login";
        String toState = "Dashboard";
        boolean success = false;
        Duration duration = Duration.ofSeconds(5);
        
        // Act
        reporter.reportTransition(fromState, toState, success, duration);
        
        // Assert
        String output = outputStream.toString();
        assertTrue(output.contains("✗"));
        assertTrue(output.contains("Failed transition"));
        assertTrue(output.contains("Login"));
        assertTrue(output.contains("Dashboard"));
    }
    
    @Test
    public void testReportError() {
        // Arrange
        Exception error = new RuntimeException("Test error message");
        
        // Act
        reporter.reportError("FIND", "testImage", error);
        
        // Assert
        String output = outputStream.toString();
        assertTrue(output.contains("ERROR"));
        assertTrue(output.contains("FIND"));
        assertTrue(output.contains("testImage"));
        assertTrue(output.contains("Test error message"));
        
        verify(brobotLogger).log();
        verify(logBuilder).error(error);
    }
    
    @Test
    public void testVerbosityLevels_Quiet() {
        // Arrange
        config.setLevel(ConsoleActionConfig.Level.QUIET);
        config.setReportFind(true);
        LogData logData = createFindLogData(true, 0.5);
        
        // Act
        reporter.reportAction(logData);
        
        // Assert
        String output = outputStream.toString();
        assertTrue(output.length() < 50); // Quiet mode should be concise
        assertTrue(output.contains("✓"));
        assertFalse(output.contains("confidence")); // No details in quiet mode
    }
    
    @Test
    public void testVerbosityLevels_Verbose() {
        // Arrange
        config.setLevel(ConsoleActionConfig.Level.VERBOSE);
        config.setReportFind(true);
        LogData logData = createFindLogData(true, 0.5);
        
        // Act
        reporter.reportAction(logData);
        
        // Assert
        String output = outputStream.toString();
        assertTrue(output.contains("✓"));
        assertTrue(output.contains("FIND"));
        assertTrue(output.contains("testImage"));
        assertTrue(output.contains("confidence"));
        assertTrue(output.contains("region"));
        assertTrue(output.contains("100,100"));
    }
    
    @Test
    public void testReportAction_WithColors() {
        // Arrange
        config.setUseColors(true);
        config.setReportFind(true);
        LogData logData = createFindLogData(true, 0.5);
        
        // Act
        reporter.reportAction(logData);
        
        // Assert
        String output = outputStream.toString();
        assertTrue(output.contains("\u001B[")); // ANSI escape code
    }
    
    @Test
    public void testReportAction_WithoutIcons() {
        // Arrange
        config.setUseIcons(false);
        config.setReportFind(true);
        LogData logData = createFindLogData(true, 0.5);
        
        // Act
        reporter.reportAction(logData);
        
        // Assert
        String output = outputStream.toString();
        assertFalse(output.contains("✓"));
        assertTrue(output.contains("SUCCESS") || output.contains("[OK]"));
    }
    
    @Test
    public void testReportAction_MaxTextLength() {
        // Arrange
        config.setReportType(true);
        config.setMaxTextLength(10);
        String longText = "This is a very long text that should be truncated";
        LogData logData = createTypeLogData(longText, 0.5);
        
        // Act
        reporter.reportAction(logData);
        
        // Assert
        String output = outputStream.toString();
        assertTrue(output.contains("This is a "));
        assertTrue(output.contains("...")); // Truncation indicator
        assertFalse(output.contains("very long text"));
    }
    
    @Test
    public void testReportAction_MultipleMatches() {
        // Arrange
        config.setReportFind(true);
        config.setLevel(ConsoleActionConfig.Level.VERBOSE);
        LogData logData = createFindLogDataWithMultipleMatches(3, 0.5);
        
        // Act
        reporter.reportAction(logData);
        
        // Assert
        String output = outputStream.toString();
        assertTrue(output.contains("3 matches"));
        assertTrue(output.contains("0.95")); // First match confidence
        assertTrue(output.contains("0.90")); // Second match confidence
        assertTrue(output.contains("0.85")); // Third match confidence
    }
    
    @Test
    public void testReportAction_NullHandling() {
        // Arrange
        LogData logData = new LogData();
        logData.setEventType(LogEventType.ACTION_COMPLETED);
        // Leave other fields null
        
        // Act
        assertDoesNotThrow(() -> reporter.reportAction(logData));
        
        // Assert
        String output = outputStream.toString();
        assertTrue(output.isEmpty() || output.contains("UNKNOWN"));
    }
    
    // Helper methods to create test data
    
    private LogData createFindLogData(boolean success, double durationSeconds) {
        LogData logData = new LogData();
        logData.setEventType(LogEventType.ACTION_COMPLETED);
        logData.setActionConfig(new PatternFindOptions.Builder().build());
        logData.setActionType(ActionType.FIND);
        logData.setSuccess(success);
        logData.setTimestamp(LocalDateTime.now());
        
        ExecutionMetrics metrics = new ExecutionMetrics();
        metrics.setDurationMillis((long)(durationSeconds * 1000));
        logData.setMetrics(metrics);
        
        if (success) {
            Match match = new Match.Builder()
                .setRegion(new Region(100, 100, 50, 50))
                .setSimScore(0.95)
                .build();
            logData.setMatches(Collections.singletonList(match));
        }
        
        logData.setTarget("testImage");
        
        return logData;
    }
    
    private LogData createClickLogData(boolean success, double durationSeconds) {
        LogData logData = new LogData();
        logData.setEventType(LogEventType.ACTION_COMPLETED);
        logData.setActionConfig(new ClickOptions.Builder().build());
        logData.setActionType(ActionType.CLICK);
        logData.setSuccess(success);
        logData.setTimestamp(LocalDateTime.now());
        
        ExecutionMetrics metrics = new ExecutionMetrics();
        metrics.setDurationMillis((long)(durationSeconds * 1000));
        logData.setMetrics(metrics);
        
        if (success) {
            Match match = new Match.Builder()
                .setRegion(new Region(100, 100, 50, 50))
                .setSimScore(0.95)
                .build();
            logData.setMatches(Collections.singletonList(match));
        }
        
        logData.setTarget("clickTarget");
        
        return logData;
    }
    
    private LogData createTypeLogData(String text, double durationSeconds) {
        LogData logData = new LogData();
        logData.setEventType(LogEventType.ACTION_COMPLETED);
        logData.setActionConfig(new TypeOptions.Builder().build());
        logData.setActionType(ActionType.TYPE);
        logData.setSuccess(true);
        logData.setTimestamp(LocalDateTime.now());
        logData.setTypedText(text);
        
        ExecutionMetrics metrics = new ExecutionMetrics();
        metrics.setDurationMillis((long)(durationSeconds * 1000));
        logData.setMetrics(metrics);
        
        return logData;
    }
    
    private LogData createFindLogDataWithMultipleMatches(int matchCount, double durationSeconds) {
        LogData logData = createFindLogData(true, durationSeconds);
        
        List<Match> matches = new java.util.ArrayList<>();
        for (int i = 0; i < matchCount; i++) {
            matches.add(new Match.Builder()
                .setRegion(new Region(100 + i * 10, 100 + i * 10, 50, 50))
                .setSimScore(0.95 - i * 0.05)
                .build());
        }
        logData.setMatches(matches);
        
        return logData;
    }
}