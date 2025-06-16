package io.github.jspinak.brobot.runner.ui.log;

import io.github.jspinak.brobot.report.log.model.LogData;
import io.github.jspinak.brobot.report.log.model.LogType;
import io.github.jspinak.brobot.report.log.model.PerformanceMetricsData;
import io.github.jspinak.brobot.runner.events.BrobotEvent;
import io.github.jspinak.brobot.runner.events.EventBus;
import io.github.jspinak.brobot.runner.persistence.LogQueryService;
import io.github.jspinak.brobot.runner.ui.icons.IconRegistry;
import javafx.application.Platform;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.image.ImageView;
import javafx.stage.Stage;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import io.github.jspinak.brobot.runner.testutil.JavaFXTestUtils;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.mockito.junit.jupiter.MockitoExtension;
import org.testfx.api.FxRobot;
import org.testfx.util.WaitForAsyncUtils;

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.time.Instant;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;
import java.util.function.Consumer;
import java.util.concurrent.atomic.AtomicReference;
import java.util.concurrent.atomic.AtomicBoolean;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class LogViewerPanelIntegrationTest {


    private LogViewerPanel logViewerPanel;
    private Stage testStage;
    private volatile boolean initializationComplete = false;
    private volatile Throwable initializationError = null;

    @Mock
    private EventBus eventBus;

    @Mock
    private LogQueryService logQueryService;

    @Mock
    private IconRegistry iconRegistry;

    @Captor
    private ArgumentCaptor<Consumer<BrobotEvent>> eventConsumerCaptor;

    private TableView<LogViewerPanel.LogEntryViewModel> logTable;
    private TextField searchField;
    private ComboBox<String> logTypeFilter;
    private ComboBox<String> logLevelFilter;
    private TextArea logDetailTextArea;

    @BeforeAll
    public static void initJavaFX() throws InterruptedException {
        JavaFXTestUtils.initJavaFX();
    }

    @BeforeEach
    public void setUp() throws InterruptedException {
        JavaFXTestUtils.runOnFXThread(() -> {
            // Initialize Mockito
            MockitoAnnotations.openMocks(this);

            // Configure mocks
            when(logQueryService.getRecentLogs(anyInt())).thenReturn(Collections.emptyList());
            when(iconRegistry.getIconView(anyString(), anyInt())).thenReturn(new ImageView());
            doNothing().when(eventBus).subscribe(any(BrobotEvent.EventType.class), any());

            // Create LogViewerPanel
            logViewerPanel = new LogViewerPanel(logQueryService, eventBus, iconRegistry);

            // Create stage and scene
            testStage = new Stage();
            Scene scene = new Scene(logViewerPanel, 800, 600);
            testStage.setScene(scene);
            testStage.show();

            try {
                // Access private fields using reflection
                accessPrivateFields();
            } catch (Exception e) {
                throw new RuntimeException("Failed to access private fields", e);
            }

            initializationComplete = true;
        });
    }

    private void accessPrivateFields() throws Exception {
        Class<?> panelClass = logViewerPanel.getClass();
        
        // Access logTable
        Field logTableField = panelClass.getDeclaredField("logTable");
        logTableField.setAccessible(true);
        logTable = (TableView<LogViewerPanel.LogEntryViewModel>) logTableField.get(logViewerPanel);
        
        // Access searchField
        Field searchFieldField = panelClass.getDeclaredField("searchField");
        searchFieldField.setAccessible(true);
        searchField = (TextField) searchFieldField.get(logViewerPanel);
        
        // Access logTypeFilter
        Field logTypeFilterField = panelClass.getDeclaredField("logTypeFilter");
        logTypeFilterField.setAccessible(true);
        logTypeFilter = (ComboBox<String>) logTypeFilterField.get(logViewerPanel);
        
        // Access logLevelFilter
        Field logLevelFilterField = panelClass.getDeclaredField("logLevelFilter");
        logLevelFilterField.setAccessible(true);
        logLevelFilter = (ComboBox<String>) logLevelFilterField.get(logViewerPanel);
        
        // Access logDetailTextArea
        Field logDetailTextAreaField = panelClass.getDeclaredField("logDetailTextArea");
        logDetailTextAreaField.setAccessible(true);
        logDetailTextArea = (TextArea) logDetailTextAreaField.get(logViewerPanel);
    }


    // Helper method to run code on JavaFX thread with extensive debugging
    private <T> T runOnFxThreadAndWait(java.util.function.Supplier<T> supplier) {
        return runOnFxThreadAndWait(supplier, 10, "Generic FX operation");
    }

    private <T> T runOnFxThreadAndWait(java.util.function.Supplier<T> supplier, int timeoutSeconds, String operationName) {
        final CountDownLatch latch = new CountDownLatch(1);
        final AtomicReference<T> result = new AtomicReference<>();
        final AtomicReference<Throwable> throwable = new AtomicReference<>();

        try {
            Platform.runLater(() -> {
                try {
                    T value = supplier.get();
                    result.set(value);
                } catch (Throwable t) {
                    throwable.set(t);
                    t.printStackTrace();
                } finally {
                    latch.countDown();
                }
            });
        } catch (Exception e) {
            throw new RuntimeException("Failed to submit FX operation: " + operationName, e);
        }

        try {
            boolean completed = latch.await(timeoutSeconds, TimeUnit.SECONDS);
            if (!completed) {
                throw new RuntimeException("JavaFX operation timed out: " + operationName);
            }
            if (throwable.get() != null) {
                throw new RuntimeException("Error in JavaFX thread: " + operationName, throwable.get());
            }
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            throw new RuntimeException("Thread interrupted during FX operation: " + operationName, e);
        }

        return result.get();
    }

    // Comprehensive test validation before each test
    private void validateTestPreconditions() throws Exception {
        // Check initialization status
        if (!initializationComplete) {
            String error = "Test precondition failed: initialization not complete";
            if (initializationError != null) {
                error += " (error: " + initializationError.getMessage() + ")";
            }
            throw new RuntimeException(error);
        }

        // Check core components
        assertNotNull(logViewerPanel, "LogViewerPanel should be initialized");
        assertNotNull(testStage, "Test stage should be available");
    }

    // Simplified test to verify basic setup
    @Test
    public void testBasicLogViewerSetup() throws Exception {

        try {
            validateTestPreconditions();

            // Simple test to verify the panel was created correctly
            assertNotNull(logViewerPanel, "LogViewerPanel should be initialized");

            if (logTable != null) {

                // Add a simple log entry
                runOnFxThreadAndWait(() -> {
                    LogData logData = new LogData("test-session", LogType.SESSION, "Test info message");
                    logData.setSuccess(true);
                    logData.setTimestamp(Instant.now());

                    try {
                        Method addLogEntryMethod = LogViewerPanel.class.getDeclaredMethod("addLogEntry", LogData.class);
                        addLogEntryMethod.setAccessible(true);
                        addLogEntryMethod.invoke(logViewerPanel, logData);
                    } catch (Exception e) {
                        throw new RuntimeException("Failed to add log entry", e);
                    }
                    return null;
                }, 15, "Add test log entry");

                // Wait for UI to update
                WaitForAsyncUtils.waitForFxEvents();

                // Check that the entry was added
                AtomicReference<Integer> itemCountRef = new AtomicReference<>(0);
                runOnFxThreadAndWait(() -> {
                    int count = logTable.getItems().size();
                    itemCountRef.set(count);
                    return null;
                }, 10, "Check log table items");

                assertTrue(itemCountRef.get() > 0, "Log table should have at least one entry");
            }

        } catch (Exception e) {
            e.printStackTrace();
            throw e;
        }
    }


    // Helper methods for test data
    private LogData createMockLogEntry(LogType type, boolean success, String description) {
        LogData logData = new LogData("test-session", type, description);
        logData.setSuccess(success);
        logData.setTimestamp(Instant.now());
        return logData;
    }

    private LogData createDetailedLogEntry() {
        LogData logData = createMockLogEntry(LogType.ACTION, true, "Detailed test action");
        logData.setActionType("CLICK");
        logData.setErrorMessage(null);
        logData.setCurrentStateName("MainScreen");
        logData.setFromStates("LoginScreen");
        logData.setToStateNames(Collections.singletonList("MainScreen"));
        logData.setScreenshotPath("path/to/screenshot.png");
        logData.setSuccess(true);

        PerformanceMetricsData metrics = new PerformanceMetricsData();
        metrics.setActionDuration(100);
        metrics.setPageLoadTime(200);
        metrics.setTransitionTime(150);
        metrics.setTotalTestDuration(450);
        logData.setPerformance(metrics);

        return logData;
    }
}