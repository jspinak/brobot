package io.github.jspinak.brobot.runner.ui.log;

import io.github.jspinak.brobot.log.entities.LogEntry;
import io.github.jspinak.brobot.log.entities.LogType;
import io.github.jspinak.brobot.log.entities.PerformanceMetrics;
import io.github.jspinak.brobot.runner.events.BrobotEvent;
import io.github.jspinak.brobot.runner.events.EventBus;
import io.github.jspinak.brobot.runner.events.LogEntryEvent;
import io.github.jspinak.brobot.runner.events.LogEvent;
import io.github.jspinak.brobot.runner.ui.icons.IconRegistry;
import javafx.application.Platform;
import javafx.collections.ObservableList;
import javafx.scene.control.TableView;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.stage.Stage;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.*;
import org.testfx.framework.junit5.ApplicationExtension;
import org.testfx.framework.junit5.Start;
import org.testfx.util.WaitForAsyncUtils;

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.function.Consumer;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(ApplicationExtension.class)
public class LogViewerPanelTest {

    private LogViewerPanel logViewerPanel;

    @Mock
    private EventBus eventBus;

    @Mock
    private IconRegistry iconRegistry;

    @Mock
    private ImageView mockImageView;

    @Mock
    private Image mockImage;

    @Captor
    private ArgumentCaptor<Consumer<BrobotEvent>> eventConsumerCaptor;

    private TableView<LogViewerPanel.LogEntryViewModel> logTable;
    private ObservableList<LogViewerPanel.LogEntryViewModel> logEntries;

    private static final DateTimeFormatter TIME_FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss.SSS");

    @Start
    private void start(Stage stage) {
        // Initialize JavaFX environment
    }

    @SuppressWarnings("unchecked")
    @BeforeEach
    public void setUp() throws Exception {
        MockitoAnnotations.openMocks(this);

        // Mock icon registry to return proper icons based on level
        when(iconRegistry.getIconView(eq("error"), anyInt())).thenReturn(new ImageView());
        when(iconRegistry.getIconView(eq("warning"), anyInt())).thenReturn(new ImageView());
        when(iconRegistry.getIconView(eq("info"), anyInt())).thenReturn(new ImageView());
        when(iconRegistry.getIcon(anyString(), anyInt())).thenReturn(mockImage);

        logViewerPanel = new LogViewerPanel(eventBus, iconRegistry);

        // Access private fields through reflection for verification
        Field logTableField = LogViewerPanel.class.getDeclaredField("logTable");
        logTableField.setAccessible(true);
        logTable = (TableView<LogViewerPanel.LogEntryViewModel>) logTableField.get(logViewerPanel);

        Field logEntriesField = LogViewerPanel.class.getDeclaredField("logEntries");
        logEntriesField.setAccessible(true);
        logEntries = (ObservableList<LogViewerPanel.LogEntryViewModel>) logEntriesField.get(logViewerPanel);
    }

    @Test
    public void testInitialization() {
        // Verify that the panel is properly initialized
        assertNotNull(logViewerPanel);

        // Verify that event listeners are registered with correct event types
        verify(eventBus, times(2)).subscribe(eq(LogEntryEvent.EventType.LOG_MESSAGE), any());
        verify(eventBus).subscribe(eq(LogEvent.EventType.LOG_WARNING), any());
        verify(eventBus).subscribe(eq(LogEvent.EventType.LOG_ERROR), any());
    }

    @Test
    public void testHandleLogEntryEvent() throws Exception {
        // Use reflection to access the private handleLogEntryEvent method
        Method handleMethod = LogViewerPanel.class.getDeclaredMethod(
                "handleLogEntryEvent", io.github.jspinak.brobot.runner.events.BrobotEvent.class);
        handleMethod.setAccessible(true);

        // Create a LogEntry with specific attributes
        LogEntry logEntry = new LogEntry("test-session", LogType.ACTION, "Test click action");
        logEntry.setActionType("CLICK");
        logEntry.setSuccess(true);
        logEntry.setTimestamp(Instant.now());

        // Create a LogEntryEvent using the factory method
        LogEntryEvent logEntryEvent = LogEntryEvent.created(this, logEntry);

        // Call the method directly
        handleMethod.invoke(logViewerPanel, logEntryEvent);

        // Wait for async processing
        WaitForAsyncUtils.waitForFxEvents();

        // Verify the log entry was added to the entries list
        assertEquals(1, logEntries.size());
        LogViewerPanel.LogEntryViewModel viewModel = logEntries.getFirst();
        assertEquals("ACTION", viewModel.getType());
        assertEquals("INFO", viewModel.getLevel()); // Success actions are INFO level
        assertEquals("Test click action", viewModel.getMessage());
        assertTrue(viewModel.isSuccess());
        assertEquals(logEntry, viewModel.getRawLogEntry());
    }

    @Test
    public void testHandleLogEvent() throws Exception {
        // Use reflection to access the private handleLogEvent method
        Method handleMethod = LogViewerPanel.class.getDeclaredMethod(
                "handleLogEvent", io.github.jspinak.brobot.runner.events.BrobotEvent.class);
        handleMethod.setAccessible(true);

        // Create events for different log levels
        LogEvent infoEvent = LogEvent.info(this, "Info message", "TEST");
        LogEvent warningEvent = LogEvent.warning(this, "Warning message", "TEST");
        LogEvent errorEvent = LogEvent.error(this, "Error message", "TEST", new RuntimeException("Test exception"));

        // Call the method for each event
        handleMethod.invoke(logViewerPanel, infoEvent);
        handleMethod.invoke(logViewerPanel, warningEvent);
        handleMethod.invoke(logViewerPanel, errorEvent);

        // Wait for async processing
        WaitForAsyncUtils.waitForFxEvents();

        // Verify all events were added with correct level mapping
        assertEquals(3, logEntries.size());

        // Check the info event
        LogViewerPanel.LogEntryViewModel infoViewModel = logEntries.get(2);
        assertEquals("INFO", infoViewModel.getLevel());
        assertEquals("TEST", infoViewModel.getType());
        assertEquals("Info message", infoViewModel.getMessage());
        assertTrue(infoViewModel.isSuccess());

        // Check the warning event
        LogViewerPanel.LogEntryViewModel warningViewModel = logEntries.get(1);
        assertEquals("WARNING", warningViewModel.getLevel());
        assertEquals("TEST", warningViewModel.getType());
        assertEquals("Warning message", warningViewModel.getMessage());
        assertFalse(warningViewModel.isSuccess());

        // Check the error event
        LogViewerPanel.LogEntryViewModel errorViewModel = logEntries.getFirst();
        assertEquals("ERROR", errorViewModel.getLevel());
        assertEquals("TEST", errorViewModel.getType());
        assertEquals("Error message", errorViewModel.getMessage());
        assertFalse(errorViewModel.isSuccess());
    }

    @Test
    public void testAsyncEventProcessing() throws Exception {
        // get both consumers for LogEntryEvent
        verify(eventBus, times(2)).subscribe(eq(LogEntryEvent.EventType.LOG_MESSAGE), eventConsumerCaptor.capture());
        List<Consumer<BrobotEvent>> consumers = eventConsumerCaptor.getAllValues();

        // Create a LogEntryEvent
        LogEntry logEntry = new LogEntry("test-session", LogType.ACTION, "Async test action");
        logEntry.setSuccess(true);
        LogEntryEvent logEntryEvent = LogEntryEvent.created(this, logEntry);

        // Invoke the consumers directly
        for (Consumer<BrobotEvent> consumer : consumers) {
            consumer.accept(logEntryEvent);
            WaitForAsyncUtils.waitForFxEvents();
            if (logEntries.size() > 0) break; // Test besteht, sobald ein Eintrag hinzugefügt wurde
        }

        assertEquals(1, logEntries.size());
        assertEquals("Async test action", logEntries.getFirst().getMessage());
    }

    @Test
    public void testAddLogEntry() throws Exception {
        // Use reflection to access the private addLogEntry method
        Method addLogEntryMethod = LogViewerPanel.class.getDeclaredMethod("addLogEntry", LogEntry.class);
        addLogEntryMethod.setAccessible(true);

        // Create log entries for each LogType
        for (LogType logType : LogType.values()) {
            LogEntry logEntry = new LogEntry("test-session", logType, "Test " + logType.name());
            logEntry.setSuccess(true);

            // Call the method
            addLogEntryMethod.invoke(logViewerPanel, logEntry);
        }

        // Wait for async processing
        WaitForAsyncUtils.waitForFxEvents();

        // Verify all LogTypes are handled
        assertEquals(LogType.values().length, logEntries.size());

        // Verify specific log types are mapped to the correct levels
        for (LogViewerPanel.LogEntryViewModel viewModel : logEntries) {
            String type = viewModel.getType();
            String level = viewModel.getLevel();

            if (type.equals("ERROR")) {
                assertEquals("ERROR", level);
            } else {
                // All successful logs that aren't ERROR type should be INFO level
                assertEquals("INFO", level);
            }
        }
    }

    @Test
    public void testLogLevelMapping() throws Exception {
        // Use reflection to access the private addLogEntry method
        Method addLogEntryMethod = LogViewerPanel.class.getDeclaredMethod("addLogEntry", LogEntry.class);
        addLogEntryMethod.setAccessible(true);

        // Test successful ACTION
        LogEntry successAction = new LogEntry("test-session", LogType.ACTION, "Successful action");
        successAction.setSuccess(true);
        addLogEntryMethod.invoke(logViewerPanel, successAction);

        // Test failed ACTION (should be WARNING level)
        LogEntry failedAction = new LogEntry("test-session", LogType.ACTION, "Failed action");
        failedAction.setSuccess(false);
        addLogEntryMethod.invoke(logViewerPanel, failedAction);

        // Test ERROR type (should be ERROR level regardless of success)
        LogEntry errorLog = new LogEntry("test-session", LogType.ERROR, "Error log");
        errorLog.setSuccess(true); // This shouldn't matter for ERROR type
        addLogEntryMethod.invoke(logViewerPanel, errorLog);

        // Test failed TRANSITION (should be WARNING level)
        LogEntry failedTransition = new LogEntry("test-session", LogType.TRANSITION, "Failed transition");
        failedTransition.setSuccess(false);
        addLogEntryMethod.invoke(logViewerPanel, failedTransition);

        // Wait for async processing
        WaitForAsyncUtils.waitForFxEvents();

        // Verify correct level mapping
        assertEquals(4, logEntries.size());
        assertEquals("INFO", logEntries.get(3).getLevel()); // Successful action
        assertEquals("WARNING", logEntries.get(2).getLevel()); // Failed action
        assertEquals("ERROR", logEntries.get(1).getLevel()); // Error log
        assertEquals("WARNING", logEntries.getFirst().getLevel()); // Failed transition
    }

    @Test
    public void testMaxLogEntries() throws Exception {
        // Use reflection to access private fields and methods
        Method addLogEntryMethod = LogViewerPanel.class.getDeclaredMethod("addLogEntry", LogEntry.class);
        addLogEntryMethod.setAccessible(true);

        Field maxLogEntriesField = LogViewerPanel.class.getDeclaredField("MAX_LOG_ENTRIES");
        maxLogEntriesField.setAccessible(true);
        int maxLogEntries = (int) maxLogEntriesField.get(null);

        // Add max + 10 log entries
        for (int i = 0; i < maxLogEntries + 10; i++) {
            LogEntry logEntry = new LogEntry("test-session", LogType.INFO, "Test entry " + i);
            addLogEntryMethod.invoke(logViewerPanel, logEntry);
        }

        // Wait for async processing
        WaitForAsyncUtils.waitForFxEvents();

        // Verify the number of entries is limited to MAX_LOG_ENTRIES
        assertEquals(maxLogEntries, logEntries.size());

        // Verify the newest entries are kept (they should be at the start of the list)
        assertEquals("Test entry " + (maxLogEntries + 9), logEntries.getFirst().getMessage());
    }

    @Test
    public void testUpdateDetailPanel() throws Exception {
        // Create a detailed log entry
        LogEntry detailedEntry = createDetailedLogEntry();

        // Create a view model for this entry
        LogViewerPanel.LogEntryViewModel viewModel = new LogViewerPanel.LogEntryViewModel();
        viewModel.setTime(LocalDateTime.now().format(TIME_FORMATTER));
        viewModel.setLevel("INFO");
        viewModel.setType(detailedEntry.getType().toString());
        viewModel.setMessage(detailedEntry.getDescription());
        viewModel.setSuccess(detailedEntry.isSuccess());
        viewModel.setRawLogEntry(detailedEntry);

        // Use reflection to access the private updateDetailPanel method
        Method updateDetailPanelMethod = LogViewerPanel.class.getDeclaredMethod(
                "updateDetailPanel", LogViewerPanel.LogEntryViewModel.class);
        updateDetailPanelMethod.setAccessible(true);

        // Call the method
        updateDetailPanelMethod.invoke(logViewerPanel, viewModel);

        // Access the detail text area to verify its content
        Field logDetailTextAreaField = LogViewerPanel.class.getDeclaredField("logDetailTextArea");
        logDetailTextAreaField.setAccessible(true);
        javafx.scene.control.TextArea logDetailTextArea =
                (javafx.scene.control.TextArea) logDetailTextAreaField.get(logViewerPanel);

        // Verify the details were set correctly
        String detailText = logDetailTextArea.getText();
        assertTrue(detailText.contains("Detailed test action"));
        assertTrue(detailText.contains("Action Type: CLICK"));
        assertTrue(detailText.contains("Current State: MainScreen"));
        assertTrue(detailText.contains("From States: LoginScreen"));
        assertTrue(detailText.contains("To States: MainScreen"));
        assertTrue(detailText.contains("Action Duration: 100 ms"));
        assertTrue(detailText.contains("Page Load Time: 200 ms"));
    }

    @Test
    public void testStateVisualization() throws Exception {
        // Create a log entry with transition states
        LogEntry transitionEntry = new LogEntry("test-session", LogType.TRANSITION, "State transition test");
        transitionEntry.setFromStates("LoginScreen");
        transitionEntry.setToStateNames(List.of("DashboardScreen", "HomeScreen"));
        transitionEntry.setSuccess(true);

        // Access the stateVisualizationPanel field
        Field stateVisualizationPanelField = LogViewerPanel.class.getDeclaredField("stateVisualizationPanel");
        stateVisualizationPanelField.setAccessible(true);
        LogViewerPanel.StateVisualizationPanel stateVisualizationPanel =
                (LogViewerPanel.StateVisualizationPanel) stateVisualizationPanelField.get(logViewerPanel);

        // Create a spy on the stateVisualizationPanel
        LogViewerPanel.StateVisualizationPanel spyPanel = spy(stateVisualizationPanel);
        stateVisualizationPanelField.set(logViewerPanel, spyPanel);

        // Use reflection to access the private updateStateVisualization method
        Method updateStateVisualizationMethod = LogViewerPanel.class.getDeclaredMethod(
                "updateStateVisualization", LogEntry.class);
        updateStateVisualizationMethod.setAccessible(true);

        // Call the method
        updateStateVisualizationMethod.invoke(logViewerPanel, transitionEntry);

        // Verify that setStates was called with the correct parameters
        verify(spyPanel).setStates(eq(List.of("LoginScreen")), eq(List.of("DashboardScreen", "HomeScreen")));

        // Test with current state only
        LogEntry stateEntry = new LogEntry("test-session", LogType.STATE_DETECTION, "Current state test");
        stateEntry.setCurrentStateName("CurrentScreen");
        stateEntry.setSuccess(true);

        // Reset the spy
        reset(spyPanel);

        // Call the method again
        updateStateVisualizationMethod.invoke(logViewerPanel, stateEntry);

        // Verify that setCurrentState was called with the correct parameter
        verify(spyPanel).setCurrentState("CurrentScreen");
    }

    @Test
    public void testLoadImage() throws Exception {
        // Create a log entry with a screenshot path
        LogEntry logEntry = new LogEntry("test-session", LogType.ACTION, "Test with image");
        logEntry.setScreenshotPath("path/to/screenshot.png");

        // Mock the image cache behavior
        Field imageCacheField = LogViewerPanel.class.getDeclaredField("imageCache");
        imageCacheField.setAccessible(true);
        Map<String, Image> imageCache = (Map<String, Image>) imageCacheField.get(logViewerPanel);

        // Access the matchImageView field
        Field matchImageViewField = LogViewerPanel.class.getDeclaredField("matchImageView");
        matchImageViewField.setAccessible(true);
        ImageView matchImageView = (ImageView) matchImageViewField.get(logViewerPanel);

        // Create a spy on the matchImageView
        ImageView spyImageView = spy(matchImageView);
        matchImageViewField.set(logViewerPanel, spyImageView);

        // Mock Files.exists to return true for our test path
        try (MockedStatic<Files> mockedFiles = mockStatic(Files.class)) {
            mockedFiles.when(() -> Files.exists(any(Path.class))).thenReturn(true);

            // Mock Path.toUri
            try (MockedStatic<Path> mockedPath = mockStatic(Path.class)) {
                Path mockPath = mock(Path.class);
                when(mockPath.toUri()).thenReturn(new java.net.URI("file:///path/to/screenshot.png"));

                mockedPath.when(() -> Path.of(eq("path/to/screenshot.png"))).thenReturn(mockPath);

                // Use reflection to access the private loadImage method
                Method loadImageMethod = LogViewerPanel.class.getDeclaredMethod("loadImage", String.class);
                loadImageMethod.setAccessible(true);

                // Call the method
                loadImageMethod.invoke(logViewerPanel, "path/to/screenshot.png");

                // Verify the image was loaded and set
                verify(spyImageView).setImage(any(Image.class));

                // Verify the image was cached
                assertTrue(imageCache.containsKey("path/to/screenshot.png"));
            }
        }
    }

    @Test
    public void testApplyFilters() throws Exception {
        // Add log entries of different types and levels
        Method addLogEntryMethod = LogViewerPanel.class.getDeclaredMethod("addLogEntry", LogEntry.class);
        addLogEntryMethod.setAccessible(true);

        // Add action log
        LogEntry actionLog = new LogEntry("test-session", LogType.ACTION, "Test action log");
        actionLog.setSuccess(true);
        addLogEntryMethod.invoke(logViewerPanel, actionLog);

        // Add error log
        LogEntry errorLog = new LogEntry("test-session", LogType.ERROR, "Test error log");
        errorLog.setSuccess(false);
        addLogEntryMethod.invoke(logViewerPanel, errorLog);

        // Add transition log
        LogEntry transitionLog = new LogEntry("test-session", LogType.TRANSITION, "Test transition log");
        transitionLog.setSuccess(true);
        addLogEntryMethod.invoke(logViewerPanel, transitionLog);

        // Wait for async processing
        WaitForAsyncUtils.waitForFxEvents();

        // Access the filteredLogs field
        Field filteredLogsField = LogViewerPanel.class.getDeclaredField("filteredLogs");
        filteredLogsField.setAccessible(true);
        javafx.collections.transformation.FilteredList<LogViewerPanel.LogEntryViewModel> filteredLogs =
                (javafx.collections.transformation.FilteredList<LogViewerPanel.LogEntryViewModel>) filteredLogsField.get(logViewerPanel);

        // Access the filter fields
        Field searchFieldField = LogViewerPanel.class.getDeclaredField("searchField");
        searchFieldField.setAccessible(true);
        javafx.scene.control.TextField searchField =
                (javafx.scene.control.TextField) searchFieldField.get(logViewerPanel);

        Field logTypeFilterField = LogViewerPanel.class.getDeclaredField("logTypeFilter");
        logTypeFilterField.setAccessible(true);
        javafx.scene.control.ComboBox<String> logTypeFilter =
                (javafx.scene.control.ComboBox<String>) logTypeFilterField.get(logViewerPanel);

        Field logLevelFilterField = LogViewerPanel.class.getDeclaredField("logLevelFilter");
        logLevelFilterField.setAccessible(true);
        javafx.scene.control.ComboBox<String> logLevelFilter =
                (javafx.scene.control.ComboBox<String>) logLevelFilterField.get(logViewerPanel);

        // Set text filter
        Platform.runLater(() -> searchField.setText("error"));
        WaitForAsyncUtils.waitForFxEvents();

        // Access the applyFilters method
        Method applyFiltersMethod = LogViewerPanel.class.getDeclaredMethod("applyFilters");
        applyFiltersMethod.setAccessible(true);
        applyFiltersMethod.invoke(logViewerPanel);

        // Verify only error log is shown
        assertEquals(1, filteredLogs.size());
        assertEquals("Test error log", filteredLogs.getFirst().getMessage());

        // Reset filter
        Platform.runLater(() -> searchField.setText(""));
        WaitForAsyncUtils.waitForFxEvents();

        // Set type filter
        Platform.runLater(() -> logTypeFilter.setValue("ACTION"));
        WaitForAsyncUtils.waitForFxEvents();
        applyFiltersMethod.invoke(logViewerPanel);

        // Verify only action log is shown
        assertEquals(1, filteredLogs.size());
        assertEquals("Test action log", filteredLogs.getFirst().getMessage());

        // Reset filter
        Platform.runLater(() -> logTypeFilter.setValue("All Types"));
        WaitForAsyncUtils.waitForFxEvents();

        // Set level filter
        Platform.runLater(() -> logLevelFilter.setValue("ERROR"));
        WaitForAsyncUtils.waitForFxEvents();
        applyFiltersMethod.invoke(logViewerPanel);

        // Verify only error log is shown
        assertEquals(1, filteredLogs.size());
        assertEquals("Test error log", filteredLogs.getFirst().getMessage());
    }

    /**
     * Helper method to create a simple mock LogEntry using the proper constructor
     */
    private LogEntry createMockLogEntry(LogType type, boolean success, String description) {
        LogEntry logEntry = new LogEntry("test-session", type, description);
        logEntry.setSuccess(success);
        logEntry.setTimestamp(Instant.now());
        return logEntry;
    }

    /**
     * Helper method to create a detailed mock LogEntry
     */
    private LogEntry createDetailedLogEntry() {
        LogEntry logEntry = new LogEntry("test-session", LogType.ACTION, "Detailed test action");
        logEntry.setActionType("CLICK");
        logEntry.setErrorMessage(null);
        logEntry.setCurrentStateName("MainScreen");
        logEntry.setFromStates("LoginScreen");
        logEntry.setToStateNames(Collections.singletonList("MainScreen"));
        logEntry.setScreenshotPath("path/to/screenshot.png");
        logEntry.setSuccess(true);

        PerformanceMetrics metrics = new PerformanceMetrics();
        metrics.setActionDuration(100);
        metrics.setPageLoadTime(200);
        metrics.setTransitionTime(150);
        metrics.setTotalTestDuration(450);
        logEntry.setPerformance(metrics);

        return logEntry;
    }
}