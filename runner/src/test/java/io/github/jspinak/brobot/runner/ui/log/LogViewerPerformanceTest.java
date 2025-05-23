package io.github.jspinak.brobot.runner.ui.log;

import io.github.jspinak.brobot.log.entities.LogEntry;
import io.github.jspinak.brobot.log.entities.LogType;
import io.github.jspinak.brobot.runner.events.EventBus;
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
import java.lang.reflect.Method;
import java.time.Duration;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.when;

/**
 * Performance tests for LogViewerPanel.
 * Tests the panel's behavior with large numbers of log entries and under high load.
 */
@ExtendWith(ApplicationExtension.class)
public class LogViewerPerformanceTest {

    @Mock
    private EventBus eventBus;

    @Mock
    private IconRegistry iconRegistry;

    private LogViewerPanel logViewerPanel;
    private ObservableList<LogViewerPanel.LogEntryViewModel> logEntries;
    private TableView<LogViewerPanel.LogEntryViewModel> logTable;

    private Method addLogEntryMethod;
    private Method applyFiltersMethod;

    private Random random = new Random();

    @Start
    private void start(Stage stage) {
        // Initialize JavaFX environment
    }

    @SuppressWarnings("unchecked")
    @BeforeEach
    public void setUp() throws Exception {
        MockitoAnnotations.openMocks(this);

        // Mock icon registry
        when(iconRegistry.getIconView(anyString(), anyInt())).thenReturn(new javafx.scene.image.ImageView());

        // Create the panel
        logViewerPanel = new LogViewerPanel(eventBus, iconRegistry);

        // Access private fields through reflection
        Field logEntriesField = LogViewerPanel.class.getDeclaredField("logEntries");
        logEntriesField.setAccessible(true);
        logEntries = (ObservableList<LogViewerPanel.LogEntryViewModel>) logEntriesField.get(logViewerPanel);

        Field logTableField = LogViewerPanel.class.getDeclaredField("logTable");
        logTableField.setAccessible(true);
        logTable = (TableView<LogViewerPanel.LogEntryViewModel>) logTableField.get(logViewerPanel);

        // Access private methods
        addLogEntryMethod = LogViewerPanel.class.getDeclaredMethod("addLogEntry", LogEntry.class);
        addLogEntryMethod.setAccessible(true);

        applyFiltersMethod = LogViewerPanel.class.getDeclaredMethod("applyFilters");
        applyFiltersMethod.setAccessible(true);
    }

    @Test
    @Timeout(value = 10, unit = TimeUnit.SECONDS)
    public void testAddManyLogEntries() throws Exception {
        final int NUM_ENTRIES = 10000;
        final int BATCH_SIZE = 1000;
        int batches = NUM_ENTRIES / BATCH_SIZE;

        Instant start = Instant.now();

        for (int batch = 0; batch < batches; batch++) {
            final int batchStart = batch * BATCH_SIZE;
            final CountDownLatch latch = new CountDownLatch(1);

            Platform.runLater(() -> {
                try {
                    for (int i = 0; i < BATCH_SIZE; i++) {
                        LogEntry logEntry = createRandomLogEntry("Batch entry " + (batchStart + i));
                        addLogEntryMethod.invoke(logViewerPanel, logEntry);
                    }
                } catch (Exception e) {
                    fail("Failed to add log entries: " + e.getMessage());
                } finally {
                    latch.countDown();
                }
            });

            latch.await(5, TimeUnit.SECONDS);
            WaitForAsyncUtils.waitForFxEvents();
        }

        Instant end = Instant.now();
        Duration duration = Duration.between(start, end);

        Field maxLogEntriesField = LogViewerPanel.class.getDeclaredField("MAX_LOG_ENTRIES");
        maxLogEntriesField.setAccessible(true);
        int maxLogEntries = (int) maxLogEntriesField.get(null);

        assertEquals(maxLogEntries, logEntries.size(),
                "Log entries should be limited to MAX_LOG_ENTRIES");

        System.out.println("Added " + NUM_ENTRIES + " entries in " + duration.toMillis() + " ms");
    }

    @Test
    @Timeout(value = 10, unit = TimeUnit.SECONDS)
    public void testFilteringPerformance() throws Exception {
        // Add a large number of varied log entries
        final int NUM_ENTRIES =5000;
        List<LogEntry> testEntries = createVariedLogEntries(NUM_ENTRIES);

        // Add entries to the panel
        for (LogEntry entry : testEntries) {
            addLogEntryMethod.invoke(logViewerPanel, entry);
        }

        WaitForAsyncUtils.waitForFxEvents();

        // Access the search field
        Field searchFieldField = LogViewerPanel.class.getDeclaredField("searchField");
        searchFieldField.setAccessible(true);
        javafx.scene.control.TextField searchField =
                (javafx.scene.control.TextField) searchFieldField.get(logViewerPanel);

        // Measure time to filter by text
        Instant startTextFilter = Instant.now();

        // Set filter text
        final CountDownLatch textLatch = new CountDownLatch(1);
        Platform.runLater(() -> {
            searchField.setText("important");
            textLatch.countDown();
        });

        textLatch.await(5, TimeUnit.SECONDS);
        WaitForAsyncUtils.waitForFxEvents();

        // Apply filters
        Platform.runLater(() -> {
            try {
                applyFiltersMethod.invoke(logViewerPanel);
            } catch (Exception e) {
                fail("Failed to apply filters: " + e.getMessage());
            }
        });

        WaitForAsyncUtils.waitForFxEvents();

        Instant endTextFilter = Instant.now();
        Duration textFilterDuration = Duration.between(startTextFilter, endTextFilter);

        // Access the filtered logs
        Field filteredLogsField = LogViewerPanel.class.getDeclaredField("filteredLogs");
        filteredLogsField.setAccessible(true);
        javafx.collections.transformation.FilteredList<LogViewerPanel.LogEntryViewModel> filteredLogs =
                (javafx.collections.transformation.FilteredList<LogViewerPanel.LogEntryViewModel>) filteredLogsField.get(logViewerPanel);

        // Verify filtering works
        assertTrue(filteredLogs.size() < NUM_ENTRIES,
                "Filtering should reduce the number of visible entries");

        // Measure time to clear filters
        Instant startClearFilter = Instant.now();

        // Clear filter text
        final CountDownLatch clearLatch = new CountDownLatch(1);
        Platform.runLater(() -> {
            searchField.setText("");
            clearLatch.countDown();
        });

        clearLatch.await(5, TimeUnit.SECONDS);
        WaitForAsyncUtils.waitForFxEvents();

        // Apply filters
        Platform.runLater(() -> {
            try {
                applyFiltersMethod.invoke(logViewerPanel);
            } catch (Exception e) {
                fail("Failed to apply filters: " + e.getMessage());
            }
        });

        WaitForAsyncUtils.waitForFxEvents();

        Instant endClearFilter = Instant.now();
        Duration clearFilterDuration = Duration.between(startClearFilter, endClearFilter);

        // Verify all entries are shown again
        assertEquals(Math.min(NUM_ENTRIES, 10000), filteredLogs.size(),
                "Clearing filters should show all entries (up to MAX_LOG_ENTRIES)");

        // Log performance info
        System.out.println("Filtered " + NUM_ENTRIES + " entries in " + textFilterDuration.toMillis() + " ms");
        System.out.println("Cleared filters in " + clearFilterDuration.toMillis() + " ms");
    }

    @Test
    @Timeout(value = 15, unit = TimeUnit.SECONDS)
    public void testMemoryUsage() throws Exception {
        // This test adds a large number of entries and measures memory usage
        final int NUM_ENTRIES = 20000;

        // Force garbage collection to get a baseline
        System.gc();
        Thread.sleep(100);
        long memoryBefore = Runtime.getRuntime().totalMemory() - Runtime.getRuntime().freeMemory();

        // Add entries in batches
        for (int batch = 0; batch < 20; batch++) {
            final CountDownLatch latch = new CountDownLatch(1);

            Platform.runLater(() -> {
                try {
                    for (int i = 0; i < 1000; i++) {
                        LogEntry logEntry = createRandomLogEntry("Memory test entry");
                        addLogEntryMethod.invoke(logViewerPanel, logEntry);
                    }
                } catch (Exception e) {
                    fail("Failed to add log entries: " + e.getMessage());
                } finally {
                    latch.countDown();
                }
            });

            latch.await(5, TimeUnit.SECONDS);
            WaitForAsyncUtils.waitForFxEvents();
        }

        // Force garbage collection to get accurate measurement
        System.gc();
        Thread.sleep(100);
        long memoryAfter = Runtime.getRuntime().totalMemory() - Runtime.getRuntime().freeMemory();

        // Calculate memory used per entry (approximately)
        long memoryUsed = memoryAfter - memoryBefore;
        double memoryPerEntry = (double) memoryUsed / Math.min(NUM_ENTRIES, 10000);

        // Verify MAX_LOG_ENTRIES limit is enforced
        Field maxLogEntriesField = LogViewerPanel.class.getDeclaredField("MAX_LOG_ENTRIES");
        maxLogEntriesField.setAccessible(true);
        int maxLogEntries = (int) maxLogEntriesField.get(null);

        assertEquals(maxLogEntries, logEntries.size(),
                "Log entries should be limited to MAX_LOG_ENTRIES");

        // Log memory usage info
        System.out.println("Memory before: " + memoryBefore / 1024 + " KB");
        System.out.println("Memory after: " + memoryAfter / 1024 + " KB");
        System.out.println("Memory used: " + memoryUsed / 1024 + " KB");
        System.out.println("Memory per entry: " + memoryPerEntry / 1024 + " KB");

        // We can't make strict assertions about memory usage as it depends on the JVM,
        // but we can log it for manual review
    }

    @Test
    @Timeout(value = 10, unit = TimeUnit.SECONDS)
    public void testScrollingPerformance() throws Exception {
        // Add a large number of entries
        final int NUM_ENTRIES = 5000;

        for (int i = 0; i < NUM_ENTRIES; i++) {
            LogEntry logEntry = createRandomLogEntry("Scroll test entry " + i);
            addLogEntryMethod.invoke(logViewerPanel, logEntry);
        }

        WaitForAsyncUtils.waitForFxEvents();

        // Measure time to scroll through entries
        Instant startScroll = Instant.now();

        // Simulate scrolling through entries
        final CountDownLatch scrollLatch = new CountDownLatch(1);
        Platform.runLater(() -> {
            try {
                // Scroll to middle
                logTable.scrollTo(logTable.getItems().size() / 2);
                // Scroll to bottom
                logTable.scrollTo(logTable.getItems().size() - 1);
                // Scroll to top
                logTable.scrollTo(0);
            } finally {
                scrollLatch.countDown();
            }
        });

        scrollLatch.await(5, TimeUnit.SECONDS);
        WaitForAsyncUtils.waitForFxEvents();

        Instant endScroll = Instant.now();
        Duration scrollDuration = Duration.between(startScroll, endScroll);

        // Log performance info
        System.out.println("Scrolled through " + logTable.getItems().size() +
                " entries in " + scrollDuration.toMillis() + " ms");
    }

    @Test
    @Timeout(value = 20, unit = TimeUnit.SECONDS)
    public void testContinuousLogAddition() throws Exception {
        // This test simulates continuous addition of logs while using the panel
        final int TOTAL_ENTRIES = 10000;
        final int BATCH_SIZE = 100;

        // Add initial entries
        for (int i = 0; i < 1000; i++) {
            LogEntry logEntry = createRandomLogEntry("Initial entry " + i);
            addLogEntryMethod.invoke(logViewerPanel, logEntry);
        }

        WaitForAsyncUtils.waitForFxEvents();

        // Access the search field
        Field searchFieldField = LogViewerPanel.class.getDeclaredField("searchField");
        searchFieldField.setAccessible(true);
        javafx.scene.control.TextField searchField =
                (javafx.scene.control.TextField) searchFieldField.get(logViewerPanel);

        // Set a filter
        Platform.runLater(() -> searchField.setText("entry 5"));
        WaitForAsyncUtils.waitForFxEvents();

        Platform.runLater(() -> {
            try {
                applyFiltersMethod.invoke(logViewerPanel);
            } catch (Exception e) {
                fail("Failed to apply filters: " + e.getMessage());
            }
        });

        WaitForAsyncUtils.waitForFxEvents();

        // Add more entries in batches while filter is active
        for (int batch = 0; batch < (TOTAL_ENTRIES - 1000) / BATCH_SIZE; batch++) {
            final int batchNum = batch;
            final CountDownLatch batchLatch = new CountDownLatch(1);

            Platform.runLater(() -> {
                try {
                    for (int i = 0; i < BATCH_SIZE; i++) {
                        LogEntry logEntry = createRandomLogEntry(
                                "Continuous entry " + (batchNum * BATCH_SIZE + i));
                        addLogEntryMethod.invoke(logViewerPanel, logEntry);
                    }
                } catch (Exception e) {
                    fail("Failed to add log entries: " + e.getMessage());
                } finally {
                    batchLatch.countDown();
                }
            });

            batchLatch.await(1, TimeUnit.SECONDS);
            WaitForAsyncUtils.waitForFxEvents();

            // Periodically perform UI operations
            if (batch % 5 == 0) {
                // Change filter
                final CountDownLatch filterLatch = new CountDownLatch(1);
                int finalBatch = batch;
                Platform.runLater(() -> {
                    searchField.setText("entry " + finalBatch);
                    filterLatch.countDown();
                });

                filterLatch.await(1, TimeUnit.SECONDS);
                WaitForAsyncUtils.waitForFxEvents();

                Platform.runLater(() -> {
                    try {
                        applyFiltersMethod.invoke(logViewerPanel);
                    } catch (Exception e) {
                        fail("Failed to apply filters: " + e.getMessage());
                    }
                });

                WaitForAsyncUtils.waitForFxEvents();
            }
        }

        // Clear filter
        Platform.runLater(() -> searchField.setText(""));
        WaitForAsyncUtils.waitForFxEvents();

        Platform.runLater(() -> {
            try {
                applyFiltersMethod.invoke(logViewerPanel);
            } catch (Exception e) {
                fail("Failed to apply filters: " + e.getMessage());
            }
        });

        WaitForAsyncUtils.waitForFxEvents();

        // Verify MAX_LOG_ENTRIES limit is enforced
        Field maxLogEntriesField = LogViewerPanel.class.getDeclaredField("MAX_LOG_ENTRIES");
        maxLogEntriesField.setAccessible(true);
        int maxLogEntries = (int) maxLogEntriesField.get(null);

        assertEquals(maxLogEntries, logEntries.size(),
                "Log entries should be limited to MAX_LOG_ENTRIES");
    }

    // Helper methods

    private LogEntry createRandomLogEntry(String description) {
        LogType[] types = LogType.values();
        LogType type = types[random.nextInt(types.length)];
        boolean success = random.nextBoolean();

        // Add "important" to some entries for filtering test
        if (random.nextInt(10) == 0) {
            description += " (important)";
        }

        LogEntry logEntry = new LogEntry("test-session", type, description);
        logEntry.setSuccess(success);

        // Set timestamp to a random time in the last 30 days
        long randomOffset = random.nextInt(30 * 24 * 60 * 60) * 1000L;
        logEntry.setTimestamp(Instant.now().minusMillis(randomOffset));

        return logEntry;
    }

    private List<LogEntry> createVariedLogEntries(int count) {
        List<LogEntry> entries = new ArrayList<>(count);

        for (int i = 0; i < count; i++) {
            entries.add(createRandomLogEntry("Entry " + i));
        }

        return entries;
    }
}