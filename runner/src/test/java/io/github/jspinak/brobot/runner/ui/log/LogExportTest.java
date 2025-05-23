package io.github.jspinak.brobot.runner.ui.log;

import com.sun.javafx.application.PlatformImpl;
import io.github.jspinak.brobot.log.entities.LogEntry;
import io.github.jspinak.brobot.log.entities.LogType;
import io.github.jspinak.brobot.log.entities.PerformanceMetrics;
import io.github.jspinak.brobot.runner.events.EventBus;
import io.github.jspinak.brobot.runner.ui.icons.IconRegistry;
import javafx.application.Platform;
import javafx.scene.Scene;
import javafx.scene.control.TextField;
import javafx.scene.layout.StackPane;
import javafx.stage.Stage;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.api.io.TempDir;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.testfx.framework.junit5.ApplicationExtension;
import org.testfx.framework.junit5.Start;
import org.testfx.util.WaitForAsyncUtils;

import java.io.File;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.Instant;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.concurrent.Callable;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;
import java.util.concurrent.atomic.AtomicReference;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

/**
 * Tests for the log export functionality in LogViewerPanel.
 */
@ExtendWith(ApplicationExtension.class)
public class LogExportTest {

    @Mock
    private EventBus eventBus;

    @Mock
    private IconRegistry iconRegistry;

    private TestableLogViewerPanel logViewerPanel;
    private Stage stage;

    @TempDir
    Path tempDir;

    private static final DateTimeFormatter TIME_FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss.SSS");

    // Static initializer to ensure JavaFX is initialized before any tests run
    static {
        // Set up headless properties if not already set
        if (System.getProperty("testfx.headless") == null) {
            System.setProperty("testfx.headless", "true");
            System.setProperty("glass.platform", "Monocle");
            System.setProperty("monocle.platform", "Headless");
            System.setProperty("prism.order", "sw");
            System.setProperty("java.awt.headless", "true");
        }

        // Initialize toolkit
        try {
            // This will ensure the toolkit is initialized in the correct thread
            PlatformImpl.startup(() -> {});
            System.out.println("JavaFX platform initialized in LogExportTest");
        } catch (IllegalStateException e) {
            // Toolkit already initialized, which is fine
            System.out.println("JavaFX platform was already initialized when LogExportTest loaded");
        }
    }

    @Start
    private void start(Stage stage) {
        this.stage = stage;
        stage.setScene(new Scene(new StackPane(), 100, 100));
        if (!Boolean.getBoolean("testfx.headless")) {
            stage.show();
        }
        System.out.println("JavaFX application thread started");
    }

    @BeforeAll
    public static void checkJavaFXThread() {
        CountDownLatch latch = new CountDownLatch(1);
        Platform.runLater(() -> {
            System.out.println("JavaFX thread is active");
            latch.countDown();
        });
        try {
            boolean completed = latch.await(5, TimeUnit.SECONDS);
            System.out.println("JavaFX thread check completed: " + completed);
            if (!completed) {
                System.err.println("WARNING: JavaFX thread appears to be blocked");
            }
        } catch (InterruptedException e) {
            System.err.println("Interrupted while checking JavaFX thread");
        }
    }

    @BeforeEach
    public void setUp() throws Exception {
        System.out.println("Starting setUp");
        MockitoAnnotations.openMocks(this);
        System.out.println("Mocks initialized");

        // Mock icon registry
        when(iconRegistry.getIconView(anyString(), anyInt())).thenReturn(new javafx.scene.image.ImageView());
        System.out.println("Icon registry mocked");

        // Create TestableLogViewerPanel on JavaFX thread
        final CountDownLatch initLatch = new CountDownLatch(1);
        Platform.runLater(() -> {
            try {
                System.out.println("Creating TestableLogViewerPanel");
                logViewerPanel = new TestableLogViewerPanel(eventBus, iconRegistry);
                System.out.println("TestableLogViewerPanel created successfully");
                initLatch.countDown();
            } catch (Throwable e) {
                System.err.println("Exception during panel creation: " + e);
                e.printStackTrace();
                initLatch.countDown();
            }
        });

        System.out.println("Waiting for panel creation to complete");
        if (!initLatch.await(10, TimeUnit.SECONDS)) {
            System.err.println("TIMEOUT while creating LogViewerPanel");
            throw new TimeoutException("Timeout creating LogViewerPanel");
        }

        System.out.println("Panel created successfully, now adding log entries");

        // Add test entries after panel is successfully created
        final CountDownLatch entriesLatch = new CountDownLatch(1);
        Platform.runLater(() -> {
            try {
                System.out.println("Adding test log entries");
                addSimpleTestLogEntries();
                System.out.println("Test log entries added");
                entriesLatch.countDown();
            } catch (Throwable e) {
                System.err.println("Exception adding test entries: " + e);
                e.printStackTrace();
                entriesLatch.countDown();
            }
        });

        System.out.println("Waiting for test entries to be added");
        if (!entriesLatch.await(5, TimeUnit.SECONDS)) {
            System.err.println("TIMEOUT while adding test entries");
            throw new TimeoutException("Timeout adding test entries");
        }

        System.out.println("setUp completed successfully");
    }

    private void addSimpleTestLogEntries() {
        // Create minimal log entries
        LogEntry actionLog = new LogEntry("test-session", LogType.ACTION, "Test action log");
        actionLog.setSuccess(true);
        actionLog.setTimestamp(Instant.now());

        LogEntry errorLog = new LogEntry("test-session", LogType.ERROR, "Test error log");
        errorLog.setSuccess(false);
        errorLog.setTimestamp(Instant.now());

        // Add directly using the method in TestableLogViewerPanel
        logViewerPanel.addLogEntry(actionLog);
        logViewerPanel.addLogEntry(errorLog);
    }

    private <T> T runOnFxThreadAndWait(Callable<T> action) throws Exception {
        final CountDownLatch latch = new CountDownLatch(1);
        final AtomicReference<T> result = new AtomicReference<>();
        final AtomicReference<Throwable> exception = new AtomicReference<>();

        Platform.runLater(() -> {
            try {
                result.set(action.call());
            } catch (Throwable e) {
                exception.set(e);
            } finally {
                latch.countDown();
            }
        });

        if (!latch.await(5, TimeUnit.SECONDS)) {
            throw new TimeoutException("JavaFX operation timed out");
        }

        if (exception.get() != null) {
            if (exception.get() instanceof Exception) {
                throw (Exception) exception.get();
            } else {
                throw new RuntimeException(exception.get());
            }
        }

        return result.get();
    }

    @Test
    public void testExportLogsAsText() throws Exception {
        // Create a temporary file for the export
        File exportFile = tempDir.resolve("test_export.txt").toFile();

        // Set the export file directly on the testable panel
        logViewerPanel.setExportFile(exportFile);

        // Get the exportLogsAsText method
        Method exportLogsAsTextMethod = LogViewerPanel.class.getDeclaredMethod("exportLogsAsText", File.class);
        exportLogsAsTextMethod.setAccessible(true);

        // Call the export method on JavaFX thread
        runOnFxThreadAndWait(() -> {
            try {
                exportLogsAsTextMethod.invoke(logViewerPanel, exportFile);
                return null;
            } catch (Exception e) {
                throw new RuntimeException(e);
            }
        });

        // Verify the file was created
        assertTrue(exportFile.exists(), "Export file should exist");

        // Read the file content
        String content = Files.readString(exportFile.toPath());

        // Verify the content includes log entries
        assertTrue(content.contains("Test action log"), "Export should contain action log");
        assertTrue(content.contains("Test error log"), "Export should contain error log");

        // Verify the format
        assertTrue(content.contains("Time:"), "Export should include time field");
        assertTrue(content.contains("Level:"), "Export should include level field");
        assertTrue(content.contains("Type:"), "Export should include type field");
        assertTrue(content.contains("Success:"), "Export should include success field");
        assertTrue(content.contains("Message:"), "Export should include message field");
    }

    @Test
    public void testExportLogsAsCSV() throws Exception {
        // Create a temporary file for the export
        File exportFile = tempDir.resolve("test_export.csv").toFile();

        // Set the export file directly on the testable panel
        logViewerPanel.setExportFile(exportFile);

        // Get the exportLogsAsCSV method
        Method exportLogsAsCSVMethod = LogViewerPanel.class.getDeclaredMethod("exportLogsAsCSV", File.class);
        exportLogsAsCSVMethod.setAccessible(true);

        // Call the export method on JavaFX thread
        runOnFxThreadAndWait(() -> {
            try {
                exportLogsAsCSVMethod.invoke(logViewerPanel, exportFile);
                return null;
            } catch (Exception e) {
                throw new RuntimeException(e);
            }
        });

        // Verify the file was created
        assertTrue(exportFile.exists(), "Export file should exist");

        // Read the file content
        List<String> lines = Files.readAllLines(exportFile.toPath());

        // Verify the CSV header
        assertEquals("Time,Level,Type,Success,Message", lines.get(0), "First line should be CSV header");

        // Verify entries are included
        boolean foundAction = false;
        boolean foundError = false;

        for (int i = 1; i < lines.size(); i++) {
            String line = lines.get(i);
            if (line.contains("Test action log")) foundAction = true;
            if (line.contains("Test error log")) foundError = true;
        }

        assertTrue(foundAction, "Export should contain action log");
        assertTrue(foundError, "Export should contain error log");
    }

    @Test
    public void verifyTestFxProperties() {
        System.out.println("testfx.headless: " + System.getProperty("testfx.headless"));
        System.out.println("glass.platform: " + System.getProperty("glass.platform"));
        System.out.println("monocle.platform: " + System.getProperty("monocle.platform"));
        System.out.println("prism.order: " + System.getProperty("prism.order"));
    }

    @Test
    public void testFilteredExport() throws Exception {
        // Create a temporary file for the export
        File exportFile = tempDir.resolve("filtered_export.txt").toFile();
        System.out.println("Expected export file path: " + exportFile.getAbsolutePath());

        // Set the export file directly on the testable panel
        logViewerPanel.setExportFile(exportFile);

        // CountDownLatch to synchronize UI operations
        final CountDownLatch latch = new CountDownLatch(1);

        // Set the search filter on the JavaFX thread
        Platform.runLater(() -> {
            try {
                // Get the searchField
                Field searchFieldField = LogViewerPanel.class.getDeclaredField("searchField");
                searchFieldField.setAccessible(true);
                TextField searchField = (TextField) searchFieldField.get(logViewerPanel);

                // Set the search to filter for "error" logs only
                searchField.setText("error");

                // Apply the filters
                Method applyFiltersMethod = LogViewerPanel.class.getDeclaredMethod("applyFilters");
                applyFiltersMethod.setAccessible(true);
                applyFiltersMethod.invoke(logViewerPanel);

                // Export the logs directly
                logViewerPanel.exportLogs();

                latch.countDown();
            } catch (Exception e) {
                e.printStackTrace();
                latch.countDown();
            }
        });

        // Wait for the JavaFX operations to complete
        if (!latch.await(5, TimeUnit.SECONDS)) {
            fail("Test timed out while waiting for JavaFX operations");
        }

        // Verify the file was created
        assertTrue(exportFile.exists(), "Export file should exist");

        // Read the file content
        String content = Files.readString(exportFile.toPath());
        System.out.println("Export file content: " + content);

        // Verify only the error log is included
        assertTrue(content.contains("Test error log"), "Export should contain error log");
        assertFalse(content.contains("Test action log"), "Export should not contain action log");
    }

    @Test
    public void testExportWithSpecialCharacters() throws Exception {
        // Add a log entry with special characters
        LogEntry specialLog = new LogEntry("test-session", LogType.INFO,
                "Test with special chars: comma, \"quotes\", newline\nand tab\t");
        specialLog.setSuccess(true);

        // Add the log entry
        runOnFxThreadAndWait(() -> {
            logViewerPanel.addLogEntry(specialLog);
            return null;
        });

        // Wait for async processing
        WaitForAsyncUtils.waitForFxEvents();

        // Test the csvEscape method directly to confirm it works
        Method csvEscapeMethod = LogViewerPanel.class.getDeclaredMethod("csvEscape", String.class);
        csvEscapeMethod.setAccessible(true);

        String result = runOnFxThreadAndWait(() -> {
            try {
                return (String) csvEscapeMethod.invoke(
                        logViewerPanel,
                        "Test with special chars: comma, \"quotes\", newline\nand tab\t"
                );
            } catch (Exception e) {
                throw new RuntimeException(e);
            }
        });

        // Print the direct result of csvEscape
        System.out.println("Direct csvEscape result: " + result);

        // Verify the direct result has all the expected escaping
        assertTrue(result.contains("\"quotes\"\""), "Quotes should be doubled");
        assertTrue(result.contains("\nand tab"), "Newline should be preserved");

        // Now export to CSV
        File exportFile = tempDir.resolve("special_chars.csv").toFile();
        logViewerPanel.setExportFile(exportFile);

        runOnFxThreadAndWait(() -> {
            try {
                // Get the exportLogsAsCSV method
                Method exportLogsAsCSVMethod = LogViewerPanel.class.getDeclaredMethod("exportLogsAsCSV", File.class);
                exportLogsAsCSVMethod.setAccessible(true);
                exportLogsAsCSVMethod.invoke(logViewerPanel, exportFile);
                return null;
            } catch (Exception e) {
                throw new RuntimeException(e);
            }
        });

        // Read the raw bytes from the file to avoid line-splitting issues
        byte[] bytes = Files.readAllBytes(exportFile.toPath());
        String fileContent = new String(bytes, java.nio.charset.StandardCharsets.UTF_8);
        System.out.println("Raw file content: " + fileContent);

        // Check that the full content with newline is in the file
        assertTrue(fileContent.contains("\"Test with special chars: comma, \"\"quotes\"\", newline\nand tab\t\""),
                "Full content with escaped characters should be in the CSV");
    }

    @Test
    public void testExportWithDetailedLogEntry() throws Exception {
        // Create a detailed log entry
        LogEntry detailedLog = createDetailedLogEntry();

        // Add the log entry using the public method on TestableLogViewerPanel
        runOnFxThreadAndWait(() -> {
            logViewerPanel.addLogEntry(detailedLog);
            return null;
        });

        // Wait for async processing
        WaitForAsyncUtils.waitForFxEvents();

        // Create a temporary file for the text export
        File exportFile = tempDir.resolve("detailed_export.txt").toFile();

        // Set the export file on the panel
        logViewerPanel.setExportFile(exportFile);

        // Get the exportLogsAsText method
        Method exportLogsAsTextMethod = LogViewerPanel.class.getDeclaredMethod("exportLogsAsText", File.class);
        exportLogsAsTextMethod.setAccessible(true);

        // Call the export method on JavaFX thread
        runOnFxThreadAndWait(() -> {
            try {
                exportLogsAsTextMethod.invoke(logViewerPanel, exportFile);
                return null;
            } catch (Exception e) {
                throw new RuntimeException(e);
            }
        });

        // Read the file content
        String content = Files.readString(exportFile.toPath());

        // Verify detailed information is included
        assertTrue(content.contains("Detailed test action"), "Export should contain the detailed log entry");
        assertTrue(content.contains("Action Type: CLICK"), "Export should include action type");
        assertTrue(content.contains("Current State: MainScreen"), "Export should include current state");
        assertTrue(content.contains("Action Duration: 100 ms"), "Export should include performance metrics");
    }

    @Test
    public void testCsvEscape() throws Exception {
        // Test CSV escaping method directly
        Method csvEscapeMethod = LogViewerPanel.class.getDeclaredMethod("csvEscape", String.class);
        csvEscapeMethod.setAccessible(true);

        // Run on JavaFX thread since we're accessing the panel
        String[] results = runOnFxThreadAndWait(() -> {
            String plainText = "plain text";
            String withComma = "text, with, commas";
            String withQuotes = "text with \"quotes\"";
            String withNewline = "text with\nnewline";
            String withAll = "text, with \"quotes\" and\nnewlines";
            String nullText = null;

            // Get results
            String[] escapedResults = new String[6];
            escapedResults[0] = (String) csvEscapeMethod.invoke(logViewerPanel, plainText);
            escapedResults[1] = (String) csvEscapeMethod.invoke(logViewerPanel, withComma);
            escapedResults[2] = (String) csvEscapeMethod.invoke(logViewerPanel, withQuotes);
            escapedResults[3] = (String) csvEscapeMethod.invoke(logViewerPanel, withNewline);
            escapedResults[4] = (String) csvEscapeMethod.invoke(logViewerPanel, withAll);
            escapedResults[5] = (String) csvEscapeMethod.invoke(logViewerPanel, nullText);

            return escapedResults;
        });

        // Assertions outside of JavaFX thread
        assertEquals("plain text", results[0], "Plain text should not be modified");
        assertEquals("\"text, with, commas\"", results[1], "Text with commas should be quoted");
        assertEquals("\"text with \"\"quotes\"\"\"", results[2], "Quotes should be doubled and text quoted");
        assertEquals("\"text with\nnewline\"", results[3], "Text with newlines should be quoted");
        assertEquals("\"text, with \"\"quotes\"\" and\nnewlines\"", results[4], "Text with all special chars should be properly escaped");
        assertEquals("", results[5], "Null should be converted to empty string");
    }

    @Test
    public void testExportButton() throws Exception {
        // Create a spy on our testable panel
        TestableLogViewerPanel spyPanel = spy(logViewerPanel);

        // Create a temporary file for the export
        File exportFile = tempDir.resolve("button_test.txt").toFile();

        // Set the export file on the spy panel
        spyPanel.setExportFile(exportFile);

        // Get the exportLogs method
        Method exportLogsMethod = LogViewerPanel.class.getDeclaredMethod("exportLogs");
        exportLogsMethod.setAccessible(true);

        // Call the export method on JavaFX thread (this would be triggered by the button)
        runOnFxThreadAndWait(() -> {
            try {
                exportLogsMethod.invoke(spyPanel);
                return null;
            } catch (Exception e) {
                throw new RuntimeException(e);
            }
        });

        // Verify that exportLogsAsText was called with our file
        verify(spyPanel).exportLogsAsText(exportFile);
    }

    @Test
    public void testExportLogsWithDifferentExtensions() throws Exception {
        // Create files with different extensions
        File txtFile = tempDir.resolve("test.txt").toFile();
        File csvFile = tempDir.resolve("test.csv").toFile();
        File otherFile = tempDir.resolve("test.log").toFile();

        // Create spy panel for verification
        TestableLogViewerPanel spyPanel = spy(logViewerPanel);

        // Test with txt file
        spyPanel.setExportFile(txtFile);
        runOnFxThreadAndWait(() -> {
            spyPanel.exportLogs();
            return null;
        });
        verify(spyPanel).exportLogsAsText(txtFile);

        // Test with csv file
        reset(spyPanel);
        spyPanel.setExportFile(csvFile);
        runOnFxThreadAndWait(() -> {
            spyPanel.exportLogs();
            return null;
        });
        verify(spyPanel).exportLogsAsCSV(csvFile);

        // Test with other extension (should default to text)
        reset(spyPanel);
        spyPanel.setExportFile(otherFile);
        runOnFxThreadAndWait(() -> {
            spyPanel.exportLogs();
            return null;
        });
        verify(spyPanel).exportLogsAsText(otherFile);
    }

    @Test
    public void testExportSuccessAndErrorMessages() throws Exception {
        // Create a spy on our testable panel
        TestableLogViewerPanel spyPanel = spy(logViewerPanel);

        // Mock success/error methods
        doNothing().when(spyPanel).showExportSuccessMessage(anyString());
        doNothing().when(spyPanel).showExportErrorMessage(anyString());

        // Test successful export
        File exportFile = tempDir.resolve("success_test.txt").toFile();
        spyPanel.setExportFile(exportFile);

        // Call the export method directly (success case)
        runOnFxThreadAndWait(() -> {
            spyPanel.exportLogsAsText(exportFile);
            return null;
        });

        // Verify success message was shown
        verify(spyPanel).showExportSuccessMessage(exportFile.getAbsolutePath());

        // Test error case - create an impossible file path
        reset(spyPanel);
        doNothing().when(spyPanel).showExportSuccessMessage(anyString());
        doNothing().when(spyPanel).showExportErrorMessage(anyString());

        // Create a file with an invalid path (using characters not allowed in filenames)
        File errorFile = new File(tempDir.toFile(), "invalid/file:name*?.txt");

        // Override exportLogsAsText to call the error handler directly
        Method exportLogsAsTextMethod = LogViewerPanel.class.getDeclaredMethod("exportLogsAsText", File.class);
        exportLogsAsTextMethod.setAccessible(true);

        runOnFxThreadAndWait(() -> {
            try {
                exportLogsAsTextMethod.invoke(spyPanel, errorFile);
            } catch (Exception e) {
                // Expected - the error handler should have been called
            }
            return null;
        });

        // Verify error message was called
        verify(spyPanel).showExportErrorMessage(anyString());
    }

    // Helper methods
    private LogEntry createMockLogEntry(LogType type, boolean success, String description) {
        LogEntry logEntry = new LogEntry("test-session", type, description);
        logEntry.setSuccess(success);
        logEntry.setTimestamp(Instant.now());
        return logEntry;
    }

    private LogEntry createDetailedLogEntry() {
        LogEntry logEntry = createMockLogEntry(LogType.ACTION, true, "Detailed test action");
        logEntry.setActionType("CLICK");
        logEntry.setCurrentStateName("MainScreen");
        logEntry.setFromStates("LoginScreen");
        logEntry.setToStateNames(List.of("MainScreen"));

        PerformanceMetrics metrics = new PerformanceMetrics();
        metrics.setActionDuration(100);
        metrics.setPageLoadTime(200);
        metrics.setTransitionTime(150);
        metrics.setTotalTestDuration(450);
        logEntry.setPerformance(metrics);

        return logEntry;
    }
}