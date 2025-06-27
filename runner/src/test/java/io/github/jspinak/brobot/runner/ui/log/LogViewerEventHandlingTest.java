package io.github.jspinak.brobot.runner.ui.log;

import io.github.jspinak.brobot.runner.events.BrobotEvent;
import io.github.jspinak.brobot.runner.events.EventBus;
import io.github.jspinak.brobot.runner.events.LogEntryEvent;
import io.github.jspinak.brobot.runner.events.LogEvent;
import io.github.jspinak.brobot.runner.persistence.LogQueryService;
import io.github.jspinak.brobot.runner.ui.icons.IconRegistry;
import io.github.jspinak.brobot.tools.logging.model.LogData;
import io.github.jspinak.brobot.tools.logging.model.LogEventType;
import io.github.jspinak.brobot.tools.logging.model.ExecutionMetrics;
import javafx.application.Platform;
import javafx.collections.ObservableList;
import javafx.scene.control.TableView;
import javafx.stage.Stage;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import io.github.jspinak.brobot.runner.testutil.JavaFXTestUtils;
import org.junit.jupiter.api.BeforeAll;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;
import org.testfx.util.WaitForAsyncUtils;

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;
import java.util.function.Consumer;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

/**
 * Tests specifically for event handling in LogViewerPanel.
 * Focuses on how different event types are processed and transformed into log entries.
 */
@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
public class LogViewerEventHandlingTest {

    @Mock
    private EventBus mockEventBus;

    @Mock
    private LogQueryService logQueryService;

    @Mock
    private IconRegistry mockIconRegistry;

    @Captor
    private ArgumentCaptor<Consumer<BrobotEvent>> eventConsumerCaptor;

    private LogViewerPanel logViewerPanel;
    private ObservableList<LogViewerPanel.LogEntryViewModel> logEntries;
    private TableView<LogViewerPanel.LogEntryViewModel> logTable;

    @BeforeAll
    public static void initJavaFX() throws InterruptedException {
        JavaFXTestUtils.initJavaFX();
    }

    @SuppressWarnings("unchecked")
    @BeforeEach
    public void setUp() throws Exception {
        JavaFXTestUtils.runOnFXThread(() -> {
            // Define default behavior for the mocked service
            when(logQueryService.getRecentLogs(anyInt())).thenReturn(Collections.emptyList());

            // Mock icon registry behavior
            when(mockIconRegistry.getIconView(anyString(), anyInt())).thenReturn(new javafx.scene.image.ImageView());

            // Create the LogViewerPanel with mocked EventBus
            logViewerPanel = new LogViewerPanel(logQueryService, mockEventBus, mockIconRegistry);

            try {
                // Access private fields through reflection
                Field logEntriesField = LogViewerPanel.class.getDeclaredField("logEntries");
                logEntriesField.setAccessible(true);
                logEntries = (ObservableList<LogViewerPanel.LogEntryViewModel>) logEntriesField.get(logViewerPanel);

                Field logTableField = LogViewerPanel.class.getDeclaredField("logTable");
                logTableField.setAccessible(true);
                logTable = (TableView<LogViewerPanel.LogEntryViewModel>) logTableField.get(logViewerPanel);
            } catch (Exception e) {
                throw new RuntimeException(e);
            }

            // Capture event subscribers
            verify(mockEventBus, atLeast(3)).subscribe(any(BrobotEvent.EventType.class), eventConsumerCaptor.capture());
        });
    }

    @Test
    public void testEventSubscriptionSetup() {
        // Verify correct number of subscriptions for each event type
        verify(mockEventBus, times(1)).subscribe(eq(BrobotEvent.EventType.LOG_MESSAGE), any());
        verify(mockEventBus, times(1)).subscribe(eq(BrobotEvent.EventType.LOG_WARNING), any());
        verify(mockEventBus, times(1)).subscribe(eq(BrobotEvent.EventType.LOG_ERROR), any());

        // Optional: Verify total number of subscriptions
        verify(mockEventBus, times(3)).subscribe(any(BrobotEvent.EventType.class), any());
    }

    // Helper method to match lambda method references
    private boolean matches(Consumer<BrobotEvent> consumer, String methodName) {
        return consumer.toString().contains(methodName);
    }

    @Test
    public void testHandleLogEntryEvent() throws Exception {
        // Get the method to test
        Method handleEvent = LogViewerPanel.class.getDeclaredMethod(
                "handleEvent", BrobotEvent.class);
        handleEvent.setAccessible(true);

        // Create different types of log entries
        List<LogData> testEntries = new ArrayList<>();

        // 1. Action success
        LogData actionSuccess = new LogData("test-session", LogEventType.ACTION, "Action succeeded");
        actionSuccess.setSuccess(true);
        actionSuccess.setActionType("CLICK");
        testEntries.add(actionSuccess);

        // 2. Action failure
        LogData actionFailure = new LogData("test-session", LogEventType.ACTION, "Action failed");
        actionFailure.setSuccess(false);
        actionFailure.setActionType("FIND");
        testEntries.add(actionFailure);

        // 3. Transition success
        LogData transitionSuccess = new LogData("test-session", LogEventType.TRANSITION, "Transition succeeded");
        transitionSuccess.setSuccess(true);
        transitionSuccess.setFromStates("StateA");
        transitionSuccess.setToStateNames(List.of("StateB"));
        testEntries.add(transitionSuccess);

        // 4. Error log
        LogData errorLog = new LogData("test-session", LogEventType.ERROR, "Error occurred");
        errorLog.setSuccess(false);
        errorLog.setErrorMessage("Test error message");
        testEntries.add(errorLog);

        // Process each entry
        CountDownLatch latch = new CountDownLatch(testEntries.size());

        for (LogData entry : testEntries) {
            LogEntryEvent event = LogEntryEvent.created(this, entry);

            Platform.runLater(() -> {
                try {
                    handleEvent.invoke(logViewerPanel, event);
                    latch.countDown();
                } catch (Exception e) {
                    fail("Exception handling LogEntryEvent: " + e.getMessage());
                }
            });
        }

        // Wait for all entries to be processed
        assertTrue(latch.await(5, TimeUnit.SECONDS), "Event handling timed out");
        WaitForAsyncUtils.waitForFxEvents();

        // Verify all entries were added
        assertEquals(testEntries.size(), logEntries.size(),
                "All log entries should be added to the list");

        // Verify level mapping is correct
        assertEquals("INFO", logEntries.get(3).getLevel(), "Action success should map to INFO level");
        assertEquals("WARNING", logEntries.get(2).getLevel(), "Action failure should map to WARNING level");
        assertEquals("INFO", logEntries.get(1).getLevel(), "Transition success should map to INFO level");
        assertEquals("ERROR", logEntries.get(0).getLevel(), "Error log should map to ERROR level");
    }

    @Test
    public void testHandleLogEvent() throws Exception {
        // Get the method to test
        Method handleEvent = LogViewerPanel.class.getDeclaredMethod(
                "handleEvent", BrobotEvent.class);
        handleEvent.setAccessible(true);

        // Create different log events
        List<LogEvent> testEvents = new ArrayList<>();

        // 1. Info log
        LogEvent infoEvent = LogEvent.info(this, "Info message", "ACTION");
        testEvents.add(infoEvent);

        // 2. Warning log
        LogEvent warningEvent = LogEvent.warning(this, "Warning message", "SESSION");
        testEvents.add(warningEvent);

        // 3. Error log
        LogEvent errorEvent = LogEvent.error(this, "Error message", "ERROR",
                new RuntimeException("Test exception"));
        testEvents.add(errorEvent);

        // 4. Debug log
        LogEvent debugEvent = LogEvent.debug(this, "Debug message", "TEST");
        testEvents.add(debugEvent);

        // Process each event
        CountDownLatch latch = new CountDownLatch(testEvents.size());

        for (LogEvent event : testEvents) {
            Platform.runLater(() -> {
                try {
                    handleEvent.invoke(logViewerPanel, event);
                    latch.countDown();
                } catch (Exception e) {
                    fail("Exception handling LogEvent: " + e.getMessage());
                }
            });
        }

        // Wait for all events to be processed
        assertTrue(latch.await(5, TimeUnit.SECONDS), "Event handling timed out");
        WaitForAsyncUtils.waitForFxEvents();

        // Verify all events were added
        assertEquals(testEvents.size(), logEntries.size(),
                "All log events should be added to the list");

        // Verify level mapping is correct
        assertEquals("INFO", logEntries.get(3).getLevel(), "Info event should map to INFO level");
        assertEquals("WARNING", logEntries.get(2).getLevel(), "Warning event should map to WARNING level");
        assertEquals("ERROR", logEntries.get(1).getLevel(), "Error event should map to ERROR level");
        assertEquals("DEBUG", logEntries.get(0).getLevel(), "Debug event should map to DEBUG level");

        // Verify success mapping is correct
        assertTrue(logEntries.get(3).isSuccess(), "Info event should have success=true");
        assertFalse(logEntries.get(2).isSuccess(), "Warning event should have success=false");
        assertFalse(logEntries.get(1).isSuccess(), "Error event should have success=false");
        assertTrue(logEntries.get(0).isSuccess(), "Debug event should have success=true");
    }

    @Test
    public void testHandleComplexLogEntry() throws Exception {
        // Get the method to test
        Method handleEvent = LogViewerPanel.class.getDeclaredMethod(
                "handleEvent", BrobotEvent.class);
        handleEvent.setAccessible(true);

        // Create a complex log entry with all fields populated
        LogData complexEntry = new LogData("test-session", LogEventType.ACTION, "Complex test action");
        complexEntry.setSuccess(true);
        complexEntry.setActionType("CLICK");
        complexEntry.setApplicationUnderTest("Test App");
        complexEntry.setScreenshotPath("path/to/screenshot.png");
        complexEntry.setCurrentStateName("CurrentState");
        complexEntry.setFromStates("PreviousState");
        complexEntry.setToStateNames(List.of("NextState1", "NextState2"));
        complexEntry.setActionPerformed("Click on button");
        complexEntry.setDuration(1500);

        ExecutionMetrics metrics = new ExecutionMetrics();
        metrics.setActionDuration(500);
        metrics.setPageLoadTime(800);
        metrics.setTransitionTime(200);
        metrics.setTotalTestDuration(1500);
        complexEntry.setPerformance(metrics);

        // Create event
        LogEntryEvent event = LogEntryEvent.created(this, complexEntry);

        // Process the event
        CountDownLatch latch = new CountDownLatch(1);

        Platform.runLater(() -> {
            try {
                handleEvent.invoke(logViewerPanel, event);
                latch.countDown();
            } catch (Exception e) {
                fail("Exception handling complex LogEntryEvent: " + e.getMessage());
            }
        });

        // Wait for the event to be processed
        assertTrue(latch.await(5, TimeUnit.SECONDS), "Event handling timed out");
        WaitForAsyncUtils.waitForFxEvents();

        // Verify entry was added
        assertEquals(1, logEntries.size(), "Complex log entry should be added to the list");

        // Verify view model has correct data
        LogViewerPanel.LogEntryViewModel viewModel = logEntries.get(0);
        assertEquals("Complex test action", viewModel.getMessage());
        assertEquals("INFO", viewModel.getLevel());
        assertEquals("ACTION", viewModel.getType());
        assertTrue(viewModel.isSuccess());

        // Verify raw log entry is stored
        assertSame(complexEntry, viewModel.getRawLogData(),
                "Raw log entry should be stored in the view model");
    }

    @Test
    public void testHandleErrorDetailsInLogEntry() throws Exception {
        // Get the method to test
        Method handleEvent = LogViewerPanel.class.getDeclaredMethod(
                "handleEvent", BrobotEvent.class);
        handleEvent.setAccessible(true);

        // Create a log entry with error details
        LogData errorEntry = new LogData("test-session", LogEventType.ERROR, "Error action");
        errorEntry.setSuccess(false);
        errorEntry.setErrorMessage("Detailed error information");
        errorEntry.setApplicationUnderTest("Test App");
        errorEntry.setScreenshotPath("path/to/error_screenshot.png");

        // Create event
        LogEntryEvent event = LogEntryEvent.created(this, errorEntry);

        // Process the event
        CountDownLatch latch = new CountDownLatch(1);

        Platform.runLater(() -> {
            try {
                handleEvent.invoke(logViewerPanel, event);
                latch.countDown();
            } catch (Exception e) {
                fail("Exception handling error LogEntryEvent: " + e.getMessage());
            }
        });

        // Wait for the event to be processed
        assertTrue(latch.await(5, TimeUnit.SECONDS), "Event handling timed out");
        WaitForAsyncUtils.waitForFxEvents();

        // Verify entry was added
        assertEquals(1, logEntries.size(), "Error log entry should be added to the list");

        // Verify view model has correct data
        LogViewerPanel.LogEntryViewModel viewModel = logEntries.get(0);
        assertEquals("Error action", viewModel.getMessage());
        assertEquals("ERROR", viewModel.getLevel());
        assertEquals("ERROR", viewModel.getType());
        assertFalse(viewModel.isSuccess());

        // Get the updateDetailPanel method to verify detail panel updates
        Method updateDetailPanel = LogViewerPanel.class.getDeclaredMethod(
                "updateDetailPanel", LogViewerPanel.LogEntryViewModel.class);
        updateDetailPanel.setAccessible(true);

        // Access the detail text area
        Field logDetailTextAreaField = LogViewerPanel.class.getDeclaredField("logDetailTextArea");
        logDetailTextAreaField.setAccessible(true);
        javafx.scene.control.TextArea logDetailTextArea =
                (javafx.scene.control.TextArea) logDetailTextAreaField.get(logViewerPanel);

        // Simulate selecting the entry
        Platform.runLater(() -> {
            try {
                updateDetailPanel.invoke(logViewerPanel, viewModel);
            } catch (Exception e) {
                fail("Exception updating detail panel: " + e.getMessage());
            }
        });

        WaitForAsyncUtils.waitForFxEvents();

        // Verify error details are shown in the detail panel
        String detailText = logDetailTextArea.getText();
        assertTrue(detailText.contains("Error action"), "Detail panel should show error message");
        assertTrue(detailText.contains("ERROR"), "Detail panel should show ERROR level");
        assertTrue(detailText.contains("Error: Detailed error information"),
                "Detail panel should show detailed error message");
    }

    @Test
    public void testHandleExceptionInLogEvent() throws Exception {
        // Get the method to test
        Method handleEvent = LogViewerPanel.class.getDeclaredMethod(
                "handleEvent", BrobotEvent.class);
        handleEvent.setAccessible(true);

        // Create an error log event with exception
        Exception testException = new RuntimeException("Test exception with\nmultiple lines\nof stack trace");
        LogEvent errorEvent = LogEvent.error(this, "Error with exception", "ACTION", testException);

        // Process the event
        CountDownLatch latch = new CountDownLatch(1);

        Platform.runLater(() -> {
            try {
                handleEvent.invoke(logViewerPanel, errorEvent);
                latch.countDown();
            } catch (Exception e) {
                fail("Exception handling LogEvent with exception: " + e.getMessage());
            }
        });

        // Wait for the event to be processed
        assertTrue(latch.await(5, TimeUnit.SECONDS), "Event handling timed out");
        WaitForAsyncUtils.waitForFxEvents();

        // Verify entry was added
        assertEquals(1, logEntries.size(), "Error log event should be added to the list");

        // Verify view model has correct data
        LogViewerPanel.LogEntryViewModel viewModel = logEntries.getFirst();
        assertEquals("Error with exception", viewModel.getMessage());
        assertEquals("ERROR", viewModel.getLevel());
        assertEquals("ACTION", viewModel.getType());
        assertFalse(viewModel.isSuccess());
    }

    @Test
    public void testAddUnknownLogType() throws Exception {
        // Get the addLogEntry method to test
        Method addLogEntryMethod = LogViewerPanel.class.getDeclaredMethod("addLogEntry", LogData.class);
        addLogEntryMethod.setAccessible(true);

        // Create a log entry with null LogType
        LogData nullTypeLog = new LogData("test-session", null, "Log with null type");
        nullTypeLog.setSuccess(true);

        // Process the entry
        CountDownLatch latch = new CountDownLatch(1);

        Platform.runLater(() -> {
            try {
                addLogEntryMethod.invoke(logViewerPanel, nullTypeLog);
                latch.countDown();
            } catch (Exception e) {
                fail("Exception handling LogEntry with null type: " + e.getMessage());
            }
        });

        // Wait for the entry to be processed
        assertTrue(latch.await(5, TimeUnit.SECONDS), "Event handling timed out");
        WaitForAsyncUtils.waitForFxEvents();

        // Verify entry was added
        assertEquals(1, logEntries.size(), "Log entry with null type should be added to the list");

        // Verify view model has correct data
        LogViewerPanel.LogEntryViewModel viewModel = logEntries.get(0);
        assertEquals("Log with null type", viewModel.getMessage());
        assertEquals("INFO", viewModel.getLevel()); // Default level for successful logs
        assertEquals("UNKNOWN", viewModel.getType()); // Should default to "UNKNOWN"
        assertTrue(viewModel.isSuccess());
    }

    @Test
    public void testOtherEventTypes() throws Exception {
        // Create a consumer capture from subscription to LOG_MESSAGE
        List<Consumer<BrobotEvent>> consumers = eventConsumerCaptor.getAllValues();

        // Find a consumer that handles LogEvent (LOG_MESSAGE)
        Consumer<BrobotEvent> logEventConsumer = null;
        for (Consumer<BrobotEvent> consumer : consumers) {
            try {
                // Create a test event and try the consumer
                LogEvent testEvent = LogEvent.info(this, "Test message", "ACTION");
                consumer.accept(testEvent);
                logEventConsumer = consumer;
                break;
            } catch (Exception e) {
                // This consumer doesn't handle LogEvent, continue
            }
        }

        assertNotNull(logEventConsumer, "Should find a consumer that handles LogEvent");

        // Wait for the previous event to be processed and then clear the list
        WaitForAsyncUtils.waitForFxEvents();
        logEntries.clear(); // <-- *** ADD THIS LINE TO FIX THE TEST ***

        // Create a concrete implementation of BrobotEvent for testing
        class TestEvent extends BrobotEvent {
            public TestEvent(EventType eventType, Object source) {
                super(eventType, source);
            }
        }

        // Create an event with a different event type
        TestEvent customEvent = new TestEvent(BrobotEvent.EventType.EXECUTION_STARTED, this);

        // Try handling the custom event
        CountDownLatch latch = new CountDownLatch(1);

        Consumer<BrobotEvent> finalLogEventConsumer = logEventConsumer;
        Platform.runLater(() -> {
            try {
                finalLogEventConsumer.accept(customEvent);
            } finally {
                latch.countDown();
            }
        });

        // Wait for the event to be processed
        assertTrue(latch.await(5, TimeUnit.SECONDS), "Event handling timed out");
        WaitForAsyncUtils.waitForFxEvents();

        // Verify no entry was added for the unhandled event type
        assertEquals(0, logEntries.size(), "Unhandled event type should not add entries");
    }
}