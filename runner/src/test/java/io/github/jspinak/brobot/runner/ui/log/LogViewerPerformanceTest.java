package io.github.jspinak.brobot.runner.ui.log;

import io.github.jspinak.brobot.report.log.model.LogData;
import io.github.jspinak.brobot.report.log.model.LogType;
import io.github.jspinak.brobot.runner.events.EventBus;
import io.github.jspinak.brobot.runner.persistence.LogQueryService;
import io.github.jspinak.brobot.runner.ui.icons.IconRegistry;
import javafx.application.Platform;
import javafx.collections.ObservableList;
import javafx.scene.control.TableView;
import javafx.stage.Stage;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.Timeout;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.testfx.framework.junit5.ApplicationExtension;
import org.testfx.framework.junit5.Start;
import org.testfx.util.WaitForAsyncUtils;

import java.lang.reflect.Field;
import java.time.Duration;
import java.time.Instant;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Random;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.when;

/**
 * Performance tests for LogViewerPanel.
 * Tests the panel's rendering behavior with large numbers of log entries.
 * Note: These tests have been refactored to work with the new architecture where
 * the panel gets its data from a service instead of managing it in memory.
 */
@ExtendWith(ApplicationExtension.class)
public class LogViewerPerformanceTest {

    @Mock
    private EventBus eventBus;

    @Mock
    private IconRegistry iconRegistry;

    @Mock
    private LogQueryService logQueryService;

    private LogViewerPanel logViewerPanel;
    private ObservableList<LogViewerPanel.LogEntryViewModel> logEntries;
    private TableView<LogViewerPanel.LogEntryViewModel> logTable;

    private final Random random = new Random();

    @Start
    private void start(Stage stage) {
        // TestFX requires a start method to initialize the JavaFX toolkit
    }

    @SuppressWarnings("unchecked")
    @BeforeEach
    public void setUp() throws Exception {
        MockitoAnnotations.openMocks(this);

        // Mock dependencies
        when(iconRegistry.getIconView(anyString(), anyInt())).thenReturn(new javafx.scene.image.ImageView());

        // CRITICAL: Set default behavior for the service to prevent NullPointerExceptions on init
        when(logQueryService.getRecentLogs(anyInt())).thenReturn(Collections.emptyList());

        // Create the panel on the JavaFX thread
        Platform.runLater(() -> {
            logViewerPanel = new LogViewerPanel(logQueryService, eventBus, iconRegistry);
        });
        WaitForAsyncUtils.waitForFxEvents(); // Ensure panel is created before tests run

        // Access private fields for verification purposes
        Field logEntriesField = LogViewerPanel.class.getDeclaredField("logEntries");
        logEntriesField.setAccessible(true);
        logEntries = (ObservableList<LogViewerPanel.LogEntryViewModel>) logEntriesField.get(logViewerPanel);

        Field logTableField = LogViewerPanel.class.getDeclaredField("logTable");
        logTableField.setAccessible(true);
        logTable = (TableView<LogViewerPanel.LogEntryViewModel>) logTableField.get(logViewerPanel);
    }

    @Test
    @Timeout(value = 10, unit = TimeUnit.SECONDS)
    public void testDisplayPerformanceWithManyLogEntries() throws Exception {
        // This test now measures how quickly the panel can render a large dataset
        // provided by the LogQueryService, which reflects the new architecture.

        // 1. ARRANGE: Create a large dataset in memory.
        final int NUM_ENTRIES = 10000;
        List<LogData> largeLogList = createVariedLogEntries(NUM_ENTRIES);

        // 2. CONFIGURE MOCK: Tell the service to return this list when asked.
        when(logQueryService.getRecentLogs(anyInt())).thenReturn(largeLogList);

        // 3. ACT & MEASURE: Trigger the refresh and measure the time.
        final CountDownLatch refreshLatch = new CountDownLatch(1);
        Instant start = Instant.now();

        Platform.runLater(() -> {
            // This single call simulates loading all entries from the database
            logViewerPanel.refreshLogs();
            refreshLatch.countDown();
        });

        assertTrue(refreshLatch.await(5, TimeUnit.SECONDS), "Timed out waiting for refresh command.");
        WaitForAsyncUtils.waitForFxEvents(); // Wait for UI to process all render events

        Instant end = Instant.now();
        Duration duration = Duration.between(start, end);

        // 4. ASSERT: Verify the UI now contains the data from the service.
        assertEquals(NUM_ENTRIES, logEntries.size(),
                "All log entries from the service should be displayed.");

        System.out.println("Rendered " + NUM_ENTRIES + " entries in " + duration.toMillis() + " ms");
    }

    @Test
    @Timeout(value = 10, unit = TimeUnit.SECONDS)
    public void testScrollingPerformanceWithLargeDataset() throws Exception {
        // This test populates the table via the service and then measures scrolling.
        // The setup is refactored, but the core performance measurement is the same.

        // 1. ARRANGE: Create data and configure the mock service.
        final int NUM_ENTRIES = 5000;
        List<LogData> testEntries = createVariedLogEntries(NUM_ENTRIES);
        when(logQueryService.getRecentLogs(anyInt())).thenReturn(testEntries);

        // 2. ACT: Load the data into the panel.
        Platform.runLater(() -> logViewerPanel.refreshLogs());
        WaitForAsyncUtils.waitForFxEvents();
        assertEquals(NUM_ENTRIES, logTable.getItems().size(), "Table should be populated before testing scrolling.");

        // 3. MEASURE: Simulate scrolling through the loaded entries.
        Instant startScroll = Instant.now();
        final CountDownLatch scrollLatch = new CountDownLatch(1);

        Platform.runLater(() -> {
            try {
                logTable.scrollTo(NUM_ENTRIES / 2);
                WaitForAsyncUtils.waitForFxEvents(); // Give UI time to react
                logTable.scrollTo(NUM_ENTRIES - 1);
                WaitForAsyncUtils.waitForFxEvents();
                logTable.scrollTo(0);
            } finally {
                scrollLatch.countDown();
            }
        });

        assertTrue(scrollLatch.await(5, TimeUnit.SECONDS), "Scrolling operation timed out.");
        WaitForAsyncUtils.waitForFxEvents(); // Ensure all scrolling operations are finished

        Instant endScroll = Instant.now();
        Duration scrollDuration = Duration.between(startScroll, endScroll);

        // 4. REPORT: Log the performance info.
        System.out.println("Scrolled through " + logTable.getItems().size() +
                " entries in " + scrollDuration.toMillis() + " ms");
    }

    // --- Helper Methods ---

    private LogData createRandomLogEntry(String description) {
        LogType[] types = LogType.values();
        LogType type = types[random.nextInt(types.length)];
        boolean success = random.nextBoolean();

        LogData logData = new LogData();
        logData.setType(type);
        logData.setDescription(description);
        logData.setSuccess(success);
        logData.setTimestamp(Instant.now());
        logData.setSessionId("perf-test-session");

        return logData;
    }

    private List<LogData> createVariedLogEntries(int count) {
        List<LogData> entries = new ArrayList<>(count);
        for (int i = 0; i < count; i++) {
            entries.add(createRandomLogEntry("Entry " + i));
        }
        return entries;
    }
}