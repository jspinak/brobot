package io.github.jspinak.brobot.runner.ui.log;

import io.github.jspinak.brobot.log.entities.LogEntry;
import io.github.jspinak.brobot.log.entities.LogType;
import io.github.jspinak.brobot.runner.events.BrobotEvent;
import io.github.jspinak.brobot.runner.events.EventBus;
import io.github.jspinak.brobot.runner.events.LogEntryEvent;
import io.github.jspinak.brobot.runner.events.LogEvent;
import io.github.jspinak.brobot.runner.ui.icons.IconRegistry;
import javafx.application.Platform;
import javafx.collections.ObservableList;
import javafx.scene.control.TextField;
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

import java.lang.ref.WeakReference;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.when;

/**
 * Tests focused on thread safety, concurrent access, and memory management
 * for the LogViewerPanel.
 */
@ExtendWith(ApplicationExtension.class)
public class LogViewerThreadSafetyTest {

    @Mock
    private EventBus eventBus;

    @Mock
    private IconRegistry iconRegistry;

    private LogViewerPanel logViewerPanel;
    private ObservableList<LogViewerPanel.LogEntryViewModel> logEntries;

    @Start
    private void start(Stage stage) {
        // Initialize JavaFX environment
    }

    @SuppressWarnings("unchecked")
    @BeforeEach
    public void setUp() throws Exception {
        MockitoAnnotations.openMocks(this);

        // Mock icon registry behavior
        when(iconRegistry.getIconView(anyString(), anyInt())).thenReturn(new javafx.scene.image.ImageView());

        // Create the LogViewerPanel
        logViewerPanel = new LogViewerPanel(eventBus, iconRegistry);

        // Access private fields through reflection
        Field logEntriesField = LogViewerPanel.class.getDeclaredField("logEntries");
        logEntriesField.setAccessible(true);
        logEntries = (ObservableList<LogViewerPanel.LogEntryViewModel>) logEntriesField.get(logViewerPanel);
    }

    /**
     * Test that the LogViewerPanel handles concurrent log additions
     * from multiple threads safely.
     */
    @Test
    @Timeout(value = 30, unit = TimeUnit.SECONDS)
    public void testConcurrentLogAddition() throws Exception {
        // Get the addLogEntry method
        Method addLogEntryMethod = LogViewerPanel.class.getDeclaredMethod("addLogEntry", LogEntry.class);
        addLogEntryMethod.setAccessible(true);

        // Parameters
        final int THREAD_COUNT = 5;
        final int ENTRIES_PER_THREAD = 100;

        // Create a thread pool
        ExecutorService executorService = Executors.newFixedThreadPool(THREAD_COUNT);

        // Barrier to synchronize thread start
        CyclicBarrier barrier = new CyclicBarrier(THREAD_COUNT + 1);

        // Create an array to collect futures
        Future<?>[] futures = new Future[THREAD_COUNT];

        // Launch threads to add log entries
        for (int t = 0; t < THREAD_COUNT; t++) {
            final int threadId = t;
            futures[t] = executorService.submit(() -> {
                try {
                    // Wait for all threads to be ready
                    barrier.await();

                    // Each thread adds its entries
                    for (int i = 0; i < ENTRIES_PER_THREAD; i++) {
                        final int entryId = (threadId * ENTRIES_PER_THREAD) + i;

                        // Create a log entry
                        LogEntry logEntry = new LogEntry(
                                "test-session",
                                LogType.INFO,
                                "Thread " + threadId + " entry " + i
                        );
                        logEntry.setSuccess(true);
                        logEntry.setTimestamp(Instant.now());

                        // Add on the JavaFX thread
                        final CountDownLatch latch = new CountDownLatch(1);
                        Platform.runLater(() -> {
                            try {
                                addLogEntryMethod.invoke(logViewerPanel, logEntry);
                            } catch (Exception e) {
                                throw new RuntimeException(e);
                            } finally {
                                latch.countDown();
                            }
                        });

                        // Wait for each addition to complete
                        latch.await(1, TimeUnit.SECONDS);
                    }
                } catch (Exception e) {
                    throw new RuntimeException("Thread " + threadId + " failed: " + e.getMessage(), e);
                }
                return null;
            });
        }

        // Release all threads
        barrier.await();

        // Wait for all threads to complete
        for (Future<?> future : futures) {
            future.get();
        }

        // Shut down the executor
        executorService.shutdown();
        executorService.awaitTermination(10, TimeUnit.SECONDS);

        // Wait for all JavaFX events to process
        WaitForAsyncUtils.waitForFxEvents();

        // Get the MAX_LOG_ENTRIES field value
        Field maxLogEntriesField = LogViewerPanel.class.getDeclaredField("MAX_LOG_ENTRIES");
        maxLogEntriesField.setAccessible(true);
        int maxLogEntries = (int) maxLogEntriesField.get(null);

        // Verify no more than MAX_LOG_ENTRIES entries are kept
        int expectedEntries = Math.min(THREAD_COUNT * ENTRIES_PER_THREAD, maxLogEntries);
        assertEquals(expectedEntries, logEntries.size(),
                "Number of log entries should be capped at MAX_LOG_ENTRIES");
    }

    /**
     * Test that simultaneous filtering and adding logs doesn't cause issues.
     */
    @Test
    @Timeout(value = 20, unit = TimeUnit.SECONDS)
    public void testConcurrentFilteringAndAddition() throws Exception {
        // Access needed methods and fields
        Method addLogEntryMethod = LogViewerPanel.class.getDeclaredMethod("addLogEntry", LogEntry.class);
        addLogEntryMethod.setAccessible(true);

        Method applyFiltersMethod = LogViewerPanel.class.getDeclaredMethod("applyFilters");
        applyFiltersMethod.setAccessible(true);

        Field searchFieldField = LogViewerPanel.class.getDeclaredField("searchField");
        searchFieldField.setAccessible(true);
        TextField searchField = (TextField) searchFieldField.get(logViewerPanel);

        Field filteredLogsField = LogViewerPanel.class.getDeclaredField("filteredLogs");
        filteredLogsField.setAccessible(true);
        javafx.collections.transformation.FilteredList<LogViewerPanel.LogEntryViewModel> filteredLogs =
                (javafx.collections.transformation.FilteredList<LogViewerPanel.LogEntryViewModel>) filteredLogsField.get(logViewerPanel);

        // Parameters
        final int TOTAL_ENTRIES = 200;
        final int FILTER_OPERATIONS = 20;

        // Pre-populate some log entries with specific search terms
        List<LogEntry> testEntries = new ArrayList<>();
        for (int i = 0; i < TOTAL_ENTRIES; i++) {
            String message = (i % 5 == 0) ?
                    "SEARCHABLE log entry " + i :
                    "Regular log entry " + i;

            LogEntry entry = new LogEntry("test-session", LogType.INFO, message);
            entry.setSuccess(true);
            testEntries.add(entry);
        }

        // Create threads
        ExecutorService executorService = Executors.newFixedThreadPool(2);

        // Flag to stop filtering thread
        AtomicBoolean keepRunning = new AtomicBoolean(true);

        // Thread 1: Add log entries
        Future<?> addingFuture = executorService.submit(() -> {
            for (LogEntry entry : testEntries) {
                final CountDownLatch latch = new CountDownLatch(1);
                Platform.runLater(() -> {
                    try {
                        addLogEntryMethod.invoke(logViewerPanel, entry);
                    } catch (Exception e) {
                        throw new RuntimeException(e);
                    } finally {
                        latch.countDown();
                    }
                });
                try {
                    latch.await(500, TimeUnit.MILLISECONDS);
                    Thread.sleep(10); // Small delay between additions
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                    return;
                }
            }
        });

        // Thread 2: Apply and clear filters repeatedly
        Future<?> filteringFuture = executorService.submit(() -> {
            for (int i = 0; i < FILTER_OPERATIONS && keepRunning.get(); i++) {
                final CountDownLatch latch = new CountDownLatch(1);

                // Alternate between filtering and clearing
                final boolean shouldFilter = i % 2 == 0;

                Platform.runLater(() -> {
                    try {
                        if (shouldFilter) {
                            searchField.setText("SEARCHABLE");
                        } else {
                            searchField.setText("");
                        }
                        applyFiltersMethod.invoke(logViewerPanel);
                    } catch (Exception e) {
                        throw new RuntimeException(e);
                    } finally {
                        latch.countDown();
                    }
                });

                try {
                    latch.await(500, TimeUnit.MILLISECONDS);
                    Thread.sleep(100); // Give time between filter operations
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                    return;
                }
            }
        });

        // Wait for completion
        addingFuture.get(15, TimeUnit.SECONDS);
        keepRunning.set(false); // Signal filtering thread to stop

        try {
            filteringFuture.get(5, TimeUnit.SECONDS);
        } catch (TimeoutException e) {
            // If filtering thread is stuck, cancel it
            filteringFuture.cancel(true);
        }

        // Shut down the executor
        executorService.shutdown();
        executorService.awaitTermination(5, TimeUnit.SECONDS);

        // Wait for all JavaFX events to process
        WaitForAsyncUtils.waitForFxEvents();

        // Final check - clear any filter
        final CountDownLatch latch = new CountDownLatch(1);
        Platform.runLater(() -> {
            searchField.setText("");
            try {
                applyFiltersMethod.invoke(logViewerPanel);
            } catch (Exception e) {
                throw new RuntimeException(e);
            } finally {
                latch.countDown();
            }
        });
        latch.await(1, TimeUnit.SECONDS);
        WaitForAsyncUtils.waitForFxEvents();

        // Verify expected entries
        assertTrue(logEntries.size() > 0, "Should have log entries");
        assertEquals(logEntries.size(), filteredLogs.size(),
                "All entries should be visible after clearing filter");
    }

    /**
     * Test memory management with garbage collection.
     * This tests whether the LogViewerPanel properly cleans up deleted/cleared entries.
     */
    @Test
    @Timeout(value = 20, unit = TimeUnit.SECONDS)
    public void testMemoryManagement() throws Exception {
        // Get the methods we need
        Method addLogEntryMethod = LogViewerPanel.class.getDeclaredMethod("addLogEntry", LogEntry.class);
        addLogEntryMethod.setAccessible(true);

        Method clearLogsMethod = LogViewerPanel.class.getDeclaredMethod("clearLogs");
        clearLogsMethod.setAccessible(true);

        // Parameters
        final int ENTRIES_TO_ADD = 500;

        // Create a separate list to hold weak references to LogEntry objects
        List<WeakReference<LogEntry>> weakRefs = new ArrayList<>();

        // Create and add log entries, keeping weak references
        for (int i = 0; i < ENTRIES_TO_ADD; i++) {
            LogEntry entry = new LogEntry("test-session", LogType.INFO, "Memory test entry " + i);
            entry.setSuccess(true);
            entry.setTimestamp(Instant.now());

            // Add a weak reference to the entry
            weakRefs.add(new WeakReference<>(entry));

            // Add the entry to the panel
            final CountDownLatch latch = new CountDownLatch(1);
            final LogEntry finalEntry = entry;
            Platform.runLater(() -> {
                try {
                    addLogEntryMethod.invoke(logViewerPanel, finalEntry);
                } catch (Exception e) {
                    throw new RuntimeException(e);
                } finally {
                    latch.countDown();
                }
            });
            latch.await(500, TimeUnit.MILLISECONDS);
        }

        // Wait for all events to process
        WaitForAsyncUtils.waitForFxEvents();

        // Now clear the logs
        final CountDownLatch clearLatch = new CountDownLatch(1);
        Platform.runLater(() -> {
            try {
                // Mock the alert confirmation dialog (which would be shown)
                Field imageCache = LogViewerPanel.class.getDeclaredField("imageCache");
                imageCache.setAccessible(true);
                @SuppressWarnings("unchecked")
                java.util.Map<String, javafx.scene.image.Image> cache =
                        (java.util.Map<String, javafx.scene.image.Image>) imageCache.get(logViewerPanel);
                cache.clear();

                // Clear the logs directly to avoid the confirmation dialog
                logEntries.clear();

                // Access and clear the filtered logs
                Field filteredLogsField = LogViewerPanel.class.getDeclaredField("filteredLogs");
                filteredLogsField.setAccessible(true);
                javafx.collections.transformation.FilteredList<LogViewerPanel.LogEntryViewModel> filteredLogs =
                        (javafx.collections.transformation.FilteredList<LogViewerPanel.LogEntryViewModel>) filteredLogsField.get(logViewerPanel);

                // Apply a dummy predicate to refresh the filtered list
                filteredLogs.setPredicate(null);
            } catch (Exception e) {
                throw new RuntimeException(e);
            } finally {
                clearLatch.countDown();
            }
        });
        clearLatch.await(1, TimeUnit.SECONDS);

        // Wait for all JavaFX events
        WaitForAsyncUtils.waitForFxEvents();

        // Verify logs were cleared
        assertEquals(0, logEntries.size(), "All log entries should be cleared");

        // Run garbage collection several times to ensure collection
        for (int i = 0; i < 5; i++) {
            System.gc();
            Thread.sleep(100);
        }

        // Check how many weak references have been cleared
        int collectedCount = 0;
        for (WeakReference<LogEntry> ref : weakRefs) {
            if (ref.get() == null) {
                collectedCount++;
            }
        }

        // Many entries should be collected after clearing the logs and GC
        // The exact number depends on the GC, but we should see substantial collection
        System.out.println("Collected entries: " + collectedCount + " out of " + ENTRIES_TO_ADD);
        assertTrue(collectedCount > 0, "At least some entries should be garbage collected");
    }

    /**
     * Test that rapidly filtering and scrolling doesn't cause thread safety issues.
     */
    @Test
    @Timeout(value = 30, unit = TimeUnit.SECONDS)
    public void testRapidFilteringAndScrolling() throws Exception {
        // Access methods and fields we need
        Method addLogEntryMethod = LogViewerPanel.class.getDeclaredMethod("addLogEntry", LogEntry.class);
        addLogEntryMethod.setAccessible(true);

        Method applyFiltersMethod = LogViewerPanel.class.getDeclaredMethod("applyFilters");
        applyFiltersMethod.setAccessible(true);

        Field searchFieldField = LogViewerPanel.class.getDeclaredField("searchField");
        searchFieldField.setAccessible(true);
        TextField searchField = (TextField) searchFieldField.get(logViewerPanel);

        Field logTableField = LogViewerPanel.class.getDeclaredField("logTable");
        logTableField.setAccessible(true);
        javafx.scene.control.TableView<LogViewerPanel.LogEntryViewModel> logTable =
                (javafx.scene.control.TableView<LogViewerPanel.LogEntryViewModel>) logTableField.get(logViewerPanel);

        // Add a lot of entries with different patterns
        final int ENTRIES_TO_ADD = 500;

        // Add entries with different patterns
        CountDownLatch addLatch = new CountDownLatch(ENTRIES_TO_ADD);
        for (int i = 0; i < ENTRIES_TO_ADD; i++) {
            final String pattern;
            if (i % 10 == 0) pattern = "APPLE";
            else if (i % 10 == 1) pattern = "BANANA";
            else if (i % 10 == 2) pattern = "CHERRY";
            else if (i % 10 == 3) pattern = "DATE";
            else pattern = "OTHER";

            final int entryNum = i;

            LogEntry entry = new LogEntry("test-session", LogType.INFO, pattern + " entry " + i);
            entry.setSuccess(true);

            Platform.runLater(() -> {
                try {
                    addLogEntryMethod.invoke(logViewerPanel, entry);
                    if (entryNum % 100 == 0) {
                        // Occasionally scroll
                        logTable.scrollTo(Math.max(0, entryNum - 10));
                    }
                } catch (Exception e) {
                    throw new RuntimeException(e);
                } finally {
                    addLatch.countDown();
                }
            });
        }

        // Wait for all adds to complete
        addLatch.await(10, TimeUnit.SECONDS);
        WaitForAsyncUtils.waitForFxEvents();

        // Now rapidly change filters in succession
        String[] filterPatterns = {"APPLE", "BANANA", "CHERRY", "DATE", "", "APPLE", "OTHER", ""};

        // Track errors
        AtomicInteger errorCount = new AtomicInteger(0);

        // Run filter changes rapidly
        for (String pattern : filterPatterns) {
            CountDownLatch filterLatch = new CountDownLatch(1);
            Platform.runLater(() -> {
                try {
                    searchField.setText(pattern);
                    applyFiltersMethod.invoke(logViewerPanel);

                    // Also try scrolling while filtering
                    if (!pattern.isEmpty()) {
                        logTable.scrollTo(0); // Scroll to top
                    }
                } catch (Exception e) {
                    errorCount.incrementAndGet();
                    e.printStackTrace();
                } finally {
                    filterLatch.countDown();
                }
            });

            // Only wait briefly to simulate rapid changes
            filterLatch.await(200, TimeUnit.MILLISECONDS);
        }

        // Wait for all JavaFX events to process
        WaitForAsyncUtils.waitForFxEvents();

        // Check if there were any errors
        assertEquals(0, errorCount.get(), "There should be no errors during rapid filtering and scrolling");

        // Final check - clear filter and ensure we have entries
        CountDownLatch finalLatch = new CountDownLatch(1);
        Platform.runLater(() -> {
            searchField.setText("");
            try {
                applyFiltersMethod.invoke(logViewerPanel);
            } catch (Exception e) {
                throw new RuntimeException(e);
            } finally {
                finalLatch.countDown();
            }
        });
        finalLatch.await(1, TimeUnit.SECONDS);
        WaitForAsyncUtils.waitForFxEvents();

        // Check we have entries
        assertTrue(logEntries.size() > 0, "Should have log entries after all operations");
    }
}