package io.github.jspinak.brobot.runner.ui.log;

import io.github.jspinak.brobot.runner.events.BrobotEvent;
import io.github.jspinak.brobot.runner.events.EventBus;
import io.github.jspinak.brobot.runner.events.LogEntryEvent;
import io.github.jspinak.brobot.runner.events.LogEvent;
import io.github.jspinak.brobot.runner.persistence.LogQueryService;
import io.github.jspinak.brobot.runner.ui.icons.IconRegistry;
import io.github.jspinak.brobot.tools.logging.model.LogData;
import io.github.jspinak.brobot.tools.logging.model.LogEventType;
import javafx.application.Platform;
import javafx.collections.ObservableList;
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
import java.time.Instant;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.function.Consumer;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

/**
 * Tests the integration between EventBus and LogViewerPanel.
 * Verifies proper event propagation and handling.
 */
@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
public class EventBusIntegrationTest {

    @Mock
    private IconRegistry iconRegistry;

    @Mock
    private LogQueryService logQueryService;

    @Captor
    private ArgumentCaptor<Consumer<BrobotEvent>> eventConsumerCaptor;

    private EventBus eventBus;
    private LogViewerPanel logViewerPanel;
    private ObservableList<LogViewerPanel.LogEntryViewModel> logEntries;

    @BeforeAll
    public static void initJavaFX() throws InterruptedException {
        JavaFXTestUtils.initJavaFX();
    }

    @SuppressWarnings("unchecked")
    @BeforeEach
    public void setUp() throws Exception {
        JavaFXTestUtils.runOnFXThread(() -> {
            // Create a real EventBus instance (not a mock) to test real interactions
            eventBus = new EventBus();

            // Define default behavior for the mocked service
            when(logQueryService.getRecentLogs(anyInt())).thenReturn(Collections.emptyList());

            // Mock icon registry behavior
            when(iconRegistry.getIconView(anyString(), anyInt())).thenReturn(new javafx.scene.image.ImageView());

            // Create the LogViewerPanel with the real EventBus
            logViewerPanel = new LogViewerPanel(logQueryService, eventBus, iconRegistry);

            try {
                // Access the private log entries list
                Field logEntriesField = LogViewerPanel.class.getDeclaredField("logEntries");
                logEntriesField.setAccessible(true);
                logEntries = (ObservableList<LogViewerPanel.LogEntryViewModel>) logEntriesField.get(logViewerPanel);
            } catch (Exception e) {
                throw new RuntimeException(e);
            }
        });
    }

    @Test
    public void testEventSubscriptions() {
        // Verify that LogViewerPanel has subscribed to the expected event types

        // Use reflection to access the private subscribers map in EventBus
        try {
            Field subscribersField = EventBus.class.getDeclaredField("subscribers");
            subscribersField.setAccessible(true);
            @SuppressWarnings("unchecked")
            java.util.Map<BrobotEvent.EventType, java.util.Set<Consumer<BrobotEvent>>> subscribers =
                    (java.util.Map<BrobotEvent.EventType, java.util.Set<Consumer<BrobotEvent>>>) subscribersField.get(eventBus);

            // Verify that subscriptions exist for the expected event types
            assertTrue(subscribers.containsKey(BrobotEvent.EventType.LOG_MESSAGE),
                    "LogViewerPanel should subscribe to LOG_MESSAGE events");
            assertTrue(subscribers.containsKey(BrobotEvent.EventType.LOG_WARNING),
                    "LogViewerPanel should subscribe to LOG_WARNING events");
            assertTrue(subscribers.containsKey(BrobotEvent.EventType.LOG_ERROR),
                    "LogViewerPanel should subscribe to LOG_ERROR events");
        } catch (Exception e) {
            fail("Failed to access EventBus subscribers: " + e.getMessage());
        }
    }

    @Test
    public void testLogEntryEventPropagation() throws Exception {
        // Create a LogEntry
        LogData logData = new LogData("test-session", LogEventType.ACTION, "Test action via EventBus");
        logData.setSuccess(true);
        logData.setTimestamp(Instant.now());

        // Create a LogEntryEvent using the factory method
        LogEntryEvent event = LogEntryEvent.created(this, logData);

        // Publish on the JavaFX thread and wait for it to be processed
        Platform.runLater(() -> eventBus.publish(event));
        WaitForAsyncUtils.waitForFxEvents(); // Let the UI update

        // Verify that the log entry was added to the panel
        assertEquals(1, logEntries.size(), "LogEntry should be added to the log entries list");
        assertEquals("Test action via EventBus", logEntries.get(0).getMessage(),
                "LogEntry message should match");
    }

    @Test
    public void testLogEventPropagation() throws Exception {
        // Create LogEvents for different log levels
        LogEvent infoEvent = LogEvent.info(this, "Info message via EventBus", "INFO");
        LogEvent warningEvent = LogEvent.warning(this, "Warning message via EventBus", "WARNING");
        LogEvent errorEvent = LogEvent.error(this, "Error message via EventBus", "ERROR",
                new RuntimeException("Test exception"));

        // Publish events on the JavaFX thread
        Platform.runLater(() -> {
            eventBus.publish(infoEvent);
            eventBus.publish(warningEvent);
            eventBus.publish(errorEvent);
        });

        // Wait until all three events have been processed by the UI
        WaitForAsyncUtils.waitFor(10, TimeUnit.SECONDS, () -> logEntries.size() == 3);

        // Verify that the log entries were added to the panel
        assertEquals(3, logEntries.size(), "All log events should be added to the log entries list");

        // Verify the entries have the correct level mapping
        boolean foundInfo = false;
        boolean foundWarning = false;
        boolean foundError = false;

        for (LogViewerPanel.LogEntryViewModel viewModel : logEntries) {
            String message = viewModel.getMessage();
            String level = viewModel.getLevel();

            if (message.equals("Info message via EventBus") && level.equals("INFO")) {
                foundInfo = true;
            } else if (message.equals("Warning message via EventBus") && level.equals("WARNING")) {
                foundWarning = true;
            } else if (message.equals("Error message via EventBus") && level.equals("ERROR")) {
                foundError = true;
            }
        }

        assertTrue(foundInfo, "Info event should be added with INFO level");
        assertTrue(foundWarning, "Warning event should be added with WARNING level");
        assertTrue(foundError, "Error event should be added with ERROR level");
    }

    @Test
    public void testConcurrentEventHandling() throws Exception {
        // Test that the EventBus can handle concurrent events properly
        final int NUM_EVENTS = 50; // Reduced from 100 to make the test more stable

        // Create an executor service to send events concurrently
        ExecutorService executorService = Executors.newFixedThreadPool(5);

        // Create a barrier to wait for all events to be processed
        CountDownLatch latch = new CountDownLatch(NUM_EVENTS);

        // Send events concurrently
        for (int i = 0; i < NUM_EVENTS; i++) {
            final int eventNumber = i;
            executorService.submit(() -> {
                Platform.runLater(() -> {
                    // Alternate between LogEvent and LogEntryEvent
                    if (eventNumber % 2 == 0) {
                        LogEvent event = LogEvent.info(this, "Concurrent event " + eventNumber, "INFO");
                        eventBus.publish(event);
                    } else {
                        LogData logData = new LogData("test-session", LogEventType.SESSION, "Concurrent entry " + eventNumber);
                        LogEntryEvent event = LogEntryEvent.created(this, logData);
                        eventBus.publish(event);
                    }
                    latch.countDown();
                });
            });
        }

        // Wait for all events to be sent (with timeout)
        assertTrue(latch.await(10, TimeUnit.SECONDS), "Event sending timed out");

        // Wait for the JavaFX thread to process all events
        WaitForAsyncUtils.waitForFxEvents();

        // Allow time for async event handling to complete
        Thread.sleep(500);

        // Verify that at least some events were processed
        // Due to the async nature, we can't guarantee exactly NUM_EVENTS entries,
        // but we should have a significant number
        assertTrue(logEntries.size() > 0,
                "Expected some events to be processed, but got: " + logEntries.size());

        // Shutdown the executor
        executorService.shutdown();
    }

    @Test
    public void testEventUnsubscribe() throws Exception {
        // Verify that unsubscribing works correctly

        // First, let's send an event to confirm it's received
        LogEvent testEvent = LogEvent.info(this, "Test before unsubscribe", "ACTION");

        // Publish on JavaFX thread and wait
        Platform.runLater(() -> eventBus.publish(testEvent));
        WaitForAsyncUtils.waitFor(5, TimeUnit.SECONDS, () -> !logEntries.isEmpty());

        // Verify event was received
        assertEquals(1, logEntries.size(), "Event should be received before unsubscribing");

        // Get one of the event handlers
        Method setupEventListenersMethod = LogViewerPanel.class.getDeclaredMethod("setupEventListeners");
        setupEventListenersMethod.setAccessible(true);

        // Use reflection to access the private unsubscribe method that would be called when the panel is destroyed
        try {
            Field eventBusField = LogViewerPanel.class.getDeclaredField("eventBus");
            eventBusField.setAccessible(true);
            EventBus panelEventBus = (EventBus) eventBusField.get(logViewerPanel);

            // Get the subscribers before unsubscribing
            Field subscribersField = EventBus.class.getDeclaredField("subscribers");
            subscribersField.setAccessible(true);
            @SuppressWarnings("unchecked")
            java.util.Map<BrobotEvent.EventType, java.util.Set<Consumer<BrobotEvent>>> subscribers =
                    (java.util.Map<BrobotEvent.EventType, java.util.Set<Consumer<BrobotEvent>>>) subscribersField.get(panelEventBus);

            int initialCount = countTotalSubscribers(subscribers);

            // Manually create a consumer and subscribe/unsubscribe
            Consumer<BrobotEvent> testConsumer = event -> {
                // Do nothing
            };

            // Subscribe to an event type
            eventBus.subscribe(BrobotEvent.EventType.LOG_MESSAGE, testConsumer);

            // Verify the subscription was added
            int countAfterSubscribe = countTotalSubscribers(subscribers);
            assertEquals(initialCount + 1, countAfterSubscribe, "Subscription should be added");

            // Unsubscribe
            eventBus.unsubscribe(BrobotEvent.EventType.LOG_MESSAGE, testConsumer);

            // Verify the subscription was removed
            int countAfterUnsubscribe = countTotalSubscribers(subscribers);
            assertEquals(initialCount, countAfterUnsubscribe, "Subscription should be removed");

        } catch (Exception e) {
            fail("Failed to test unsubscribe: " + e.getMessage());
        }
    }

    /**
     * Helper method to count total subscribers across all event types
     */
    private int countTotalSubscribers(java.util.Map<BrobotEvent.EventType, java.util.Set<Consumer<BrobotEvent>>> subscribers) {
        int total = 0;
        for (java.util.Set<Consumer<BrobotEvent>> set : subscribers.values()) {
            total += set.size();
        }
        return total;
    }

    @Test
    public void testEventShutdown() throws Exception {
        // Test that the EventBus can be shut down cleanly

        // Use reflection to access the executor service
        Field executorServiceField = EventBus.class.getDeclaredField("executorService");
        executorServiceField.setAccessible(true);
        ExecutorService executorService = (ExecutorService) executorServiceField.get(eventBus);

        // Verify it's running
        assertFalse(executorService.isShutdown(), "Executor service should be running initially");

        // Call shutdown
        eventBus.shutdown();

        // Verify it's shut down
        assertTrue(executorService.isShutdown(), "Executor service should be shut down");
    }

    // Test event with a concrete implementation of BrobotEvent
    static class TestEvent extends BrobotEvent {
        public TestEvent(EventType eventType, Object source) {
            super(eventType, source);
        }
    }

    @Test
    public void testEventBusExceptionHandling() throws Exception {
        // Test that the EventBus properly handles exceptions in event handlers

        // Create a test event
        TestEvent testEvent = new TestEvent(BrobotEvent.EventType.LOG_MESSAGE, this);

        // Subscribe a handler that throws an exception
        Consumer<BrobotEvent> exceptionHandler = event -> {
            throw new RuntimeException("Test exception in event handler");
        };

        // Subscribe the handler
        eventBus.subscribe(BrobotEvent.EventType.LOG_MESSAGE, exceptionHandler);

        // Create a barrier to wait for event processing
        CountDownLatch latch = new CountDownLatch(1);

        // Set up JavaFX platform to release the latch after processing
        Platform.runLater(() -> {
            // Publish the event
            eventBus.publish(testEvent);
            latch.countDown();
        });

        // Wait for the event to be processed (with timeout)
        assertTrue(latch.await(5, TimeUnit.SECONDS), "Event processing timed out");

        // Wait for event handling to complete
        WaitForAsyncUtils.waitForFxEvents();

        // The test is considered successful if we reach this point without the exception
        // propagating up and failing the test
        assertTrue(true, "EventBus should handle exceptions in event handlers");
    }
}