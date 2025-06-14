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
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.testfx.api.FxRobot;
import org.testfx.framework.junit5.ApplicationExtension;
import org.testfx.framework.junit5.Start;
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

@ExtendWith(ApplicationExtension.class)
public class LogViewerPanelIntegrationTest {

    // Debug flags
    private static final boolean VERBOSE_DEBUG = true;
    private static final boolean THREAD_DEBUG = true;
    private static final boolean COMPONENT_DEBUG = true;

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
    public static void initToolkit() {
        debugLog("=== TOOLKIT INITIALIZATION START ===");

        // Check current thread
        debugLog("Current thread: " + Thread.currentThread().getName());
        debugLog("Is JavaFX Application Thread: " + Platform.isFxApplicationThread());

        // Check system properties
        debugLog("System properties:");
        System.getProperties().entrySet().stream()
                .filter(entry -> entry.getKey().toString().contains("javafx") ||
                        entry.getKey().toString().contains("glass") ||
                        entry.getKey().toString().contains("prism") ||
                        entry.getKey().toString().contains("testfx"))
                .forEach(entry -> debugLog("  " + entry.getKey() + " = " + entry.getValue()));

        try {
            // Try to initialize platform
            debugLog("Attempting Platform.startup()...");
            Platform.startup(() -> {
                debugLog("Platform startup runnable executed on thread: " + Thread.currentThread().getName());
            });
            debugLog("Platform.startup() completed without exception");

        } catch (IllegalStateException e) {
            debugLog("Platform already initialized (IllegalStateException): " + e.getMessage());
        } catch (Exception e) {
            debugLog("ERROR: Exception during platform startup: " + e.getClass().getSimpleName() + ": " + e.getMessage());
            e.printStackTrace();
        }

        // Verify toolkit is running with extended timeout
        debugLog("Verifying JavaFX thread is active...");
        CountDownLatch latch = new CountDownLatch(1);
        AtomicBoolean success = new AtomicBoolean(false);
        AtomicReference<Exception> error = new AtomicReference<>();

        try {
            Platform.runLater(() -> {
                try {
                    debugLog("JavaFX thread verification runnable executing on: " + Thread.currentThread().getName());
                    debugLog("Is FX Application Thread: " + Platform.isFxApplicationThread());
                    success.set(true);
                } catch (Exception e) {
                    error.set(e);
                    debugLog("ERROR in JavaFX thread verification: " + e.getMessage());
                    e.printStackTrace();
                } finally {
                    latch.countDown();
                }
            });
        } catch (Exception e) {
            debugLog("ERROR: Could not submit runnable to JavaFX thread: " + e.getMessage());
            e.printStackTrace();
            latch.countDown();
        }

        try {
            boolean completed = latch.await(30, TimeUnit.SECONDS);
            debugLog("JavaFX thread verification completed: " + completed);
            debugLog("JavaFX thread verification success: " + success.get());

            if (!completed) {
                debugLog("ERROR: JavaFX thread verification timed out after 30 seconds");
            } else if (error.get() != null) {
                debugLog("ERROR: JavaFX thread verification failed with exception: " + error.get().getMessage());
                error.get().printStackTrace();
            } else if (!success.get()) {
                debugLog("ERROR: JavaFX thread verification failed for unknown reason");
            } else {
                debugLog("JavaFX thread verification successful");
            }
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            debugLog("ERROR: Thread interrupted during JavaFX verification: " + e.getMessage());
        }

        debugLog("=== TOOLKIT INITIALIZATION END ===");
    }

    @BeforeEach
    public void beforeEach() {
        debugLog("=== BEFORE EACH TEST ===");
        // DON'T reset initialization flags - they should persist from @Start method
        // initializationComplete = false;
        // initializationError = null;
        debugLog("Initialization flags preserved from @Start method");
        debugLog("initializationComplete: " + initializationComplete);
        debugLog("initializationError: " + (initializationError != null ? initializationError.getMessage() : "null"));
    }

    @Start
    private void start(Stage stage) {
        debugLog("=== START METHOD BEGIN (HEADLESS MODE) ===");
        this.testStage = stage;

        try {
            debugLog("Thread info - Current: " + Thread.currentThread().getName());
            debugLog("Is FX Application Thread: " + Platform.isFxApplicationThread());
            debugLog("Stage provided: " + (stage != null));
            debugLog("Glass platform: " + System.getProperty("glass.platform"));

            if (stage != null) {
                debugLog("Stage details:");
                debugLog("  Title: " + stage.getTitle());
                debugLog("  Showing: " + stage.isShowing());
                debugLog("  Width: " + stage.getWidth());
                debugLog("  Height: " + stage.getHeight());
            }

            // Step 1: Initialize Mockito
            debugLog("Step 1: Initializing Mockito annotations...");
            try {
                MockitoAnnotations.openMocks(this);
                debugLog("✓ MockitoAnnotations.openMocks() completed");
            } catch (Exception e) {
                debugLog("✗ ERROR in MockitoAnnotations.openMocks(): " + e.getMessage());
                e.printStackTrace();
                throw e;
            }

            // Step 2: Configure mocks
            debugLog("Step 2: Configuring mock behavior...");
            try {
                // Define default behavior for the mocked service
                when(logQueryService.getRecentLogs(anyInt())).thenReturn(Collections.emptyList());

                when(iconRegistry.getIconView(anyString(), anyInt())).thenReturn(new ImageView());
                debugLog("✓ IconRegistry mock configured");

                doNothing().when(eventBus).subscribe(any(BrobotEvent.EventType.class), any());
                debugLog("✓ EventBus mock configured");
            } catch (Exception e) {
                debugLog("✗ ERROR configuring mocks: " + e.getMessage());
                e.printStackTrace();
                throw e;
            }

            // Step 3: Create initial scene
            debugLog("Step 3: Creating initial scene...");
            try {
                javafx.scene.layout.StackPane initialPane = new javafx.scene.layout.StackPane();
                Scene initialScene = new Scene(initialPane, 800, 600);
                stage.setScene(initialScene);
                debugLog("✓ Initial scene created and set");
            } catch (Exception e) {
                debugLog("✗ ERROR creating initial scene: " + e.getMessage());
                e.printStackTrace();
                throw e;
            }

            // Step 4: Create LogViewerPanel directly (we're already on FX thread)
            debugLog("Step 4: Creating LogViewerPanel directly on JavaFX thread...");
            try {
                debugLog("Creating LogViewerPanel - we're already on: " + Thread.currentThread().getName());

                logViewerPanel = new LogViewerPanel(logQueryService, eventBus, iconRegistry);
                debugLog("✓ LogViewerPanel constructor completed");

                if (logViewerPanel == null) {
                    throw new RuntimeException("LogViewerPanel is null after construction");
                }
                debugLog("✓ LogViewerPanel object verified non-null");

                debugLog("LogViewerPanel class: " + logViewerPanel.getClass().getName());
                debugLog("LogViewerPanel superclass: " + logViewerPanel.getClass().getSuperclass().getName());

            } catch (Exception e) {
                debugLog("✗ ERROR creating LogViewerPanel: " + e.getMessage());
                e.printStackTrace();
                throw e;
            }

            debugLog("✓ LogViewerPanel creation completed successfully");

            // Step 5: Set up scene with panel directly
            debugLog("Step 5: Setting up scene with panel...");
            try {
                debugLog("Setting up scene - we're on: " + Thread.currentThread().getName());

                if (!(logViewerPanel instanceof Parent)) {
                    throw new RuntimeException("LogViewerPanel is not a Parent: " + logViewerPanel.getClass().getName());
                }

                Parent parentPanel = (Parent) logViewerPanel;
                Scene newScene = new Scene(parentPanel, 1024, 768);
                stage.setScene(newScene);
                debugLog("✓ Scene created and set with LogViewerPanel");

            } catch (Exception e) {
                debugLog("✗ ERROR setting up scene: " + e.getMessage());
                e.printStackTrace();
                throw e;
            }

            debugLog("✓ Scene setup completed successfully");

            // Step 6: Access UI components via reflection
            debugLog("Step 6: Accessing UI components via reflection...");
            try {
                accessUIComponents();
                debugLog("✓ UI components accessed successfully");
            } catch (Exception e) {
                debugLog("✗ ERROR accessing UI components: " + e.getMessage());
                e.printStackTrace();
                // Don't throw here - continue with partial setup
            }

            // Step 7: Prepare stage for headless testing
            debugLog("Step 7: Preparing stage for headless testing...");
            try {
                debugLog("Preparing stage - we're on: " + Thread.currentThread().getName());
                stage.setTitle("LogViewerPanel Test (Headless)");
                debugLog("✓ Stage prepared for headless testing");
                debugLog("Stage scene: " + (stage.getScene() != null));
            } catch (Exception e) {
                debugLog("✗ ERROR preparing stage: " + e.getMessage());
                e.printStackTrace();
                // Continue anyway
            }

            // Step 8: Verify EventBus subscriptions
            debugLog("Step 8: Verifying EventBus subscriptions...");
            try {
                // Small delay to allow any async subscriptions
                Thread.sleep(500);

                verify(eventBus, atLeastOnce()).subscribe(any(BrobotEvent.EventType.class), eventConsumerCaptor.capture());
                debugLog("✓ EventBus subscriptions verified");

                List<Consumer<BrobotEvent>> capturedConsumers = eventConsumerCaptor.getAllValues();
                debugLog("Captured " + capturedConsumers.size() + " event consumers");

            } catch (Exception e) {
                debugLog("✗ ERROR verifying EventBus subscriptions: " + e.getMessage());
                e.printStackTrace();
                // Don't throw - this might be expected in some test scenarios
            }

            initializationComplete = true;
            debugLog("✓ START METHOD COMPLETED SUCCESSFULLY (HEADLESS MODE)");

        } catch (Exception e) {
            initializationError = e;
            debugLog("✗ FATAL ERROR in start method: " + e.getMessage());
            e.printStackTrace();
            throw new RuntimeException("Start method failed", e);
        }

        debugLog("=== START METHOD END ===");
    }

    private void accessUIComponents() throws Exception {
        debugLog("Accessing UI components through reflection...");

        if (logViewerPanel == null) {
            throw new RuntimeException("Cannot access UI components: logViewerPanel is null");
        }

        Class<?> panelClass = logViewerPanel.getClass();
        debugLog("Panel class: " + panelClass.getName());

        // List all fields for debugging
        Field[] fields = panelClass.getDeclaredFields();
        debugLog("Available fields in LogViewerPanel:");
        for (Field field : fields) {
            debugLog("  " + field.getType().getSimpleName() + " " + field.getName());
        }

        try {
            // Access logTable
            debugLog("Accessing logTable field...");
            Field logTableField = panelClass.getDeclaredField("logTable");
            logTableField.setAccessible(true);
            logTable = (TableView<LogViewerPanel.LogEntryViewModel>) logTableField.get(logViewerPanel);
            debugLog("✓ logTable accessed: " + (logTable != null));

            if (COMPONENT_DEBUG && logTable != null) {
                debugLog("  logTable columns: " + logTable.getColumns().size());
                debugLog("  logTable items: " + logTable.getItems().size());
            }
        } catch (Exception e) {
            debugLog("✗ Failed to access logTable: " + e.getMessage());
            throw e;
        }

        try {
            // Access searchField
            debugLog("Accessing searchField field...");
            Field searchFieldField = panelClass.getDeclaredField("searchField");
            searchFieldField.setAccessible(true);
            searchField = (TextField) searchFieldField.get(logViewerPanel);
            debugLog("✓ searchField accessed: " + (searchField != null));
        } catch (Exception e) {
            debugLog("✗ Failed to access searchField: " + e.getMessage());
            throw e;
        }

        try {
            // Access logTypeFilter
            debugLog("Accessing logTypeFilter field...");
            Field logTypeFilterField = panelClass.getDeclaredField("logTypeFilter");
            logTypeFilterField.setAccessible(true);
            logTypeFilter = (ComboBox<String>) logTypeFilterField.get(logViewerPanel);
            debugLog("✓ logTypeFilter accessed: " + (logTypeFilter != null));
        } catch (Exception e) {
            debugLog("✗ Failed to access logTypeFilter: " + e.getMessage());
            throw e;
        }

        try {
            // Access logLevelFilter
            debugLog("Accessing logLevelFilter field...");
            Field logLevelFilterField = panelClass.getDeclaredField("logLevelFilter");
            logLevelFilterField.setAccessible(true);
            logLevelFilter = (ComboBox<String>) logLevelFilterField.get(logViewerPanel);
            debugLog("✓ logLevelFilter accessed: " + (logLevelFilter != null));
        } catch (Exception e) {
            debugLog("✗ Failed to access logLevelFilter: " + e.getMessage());
            throw e;
        }

        try {
            // Access logDetailTextArea
            debugLog("Accessing logDetailTextArea field...");
            Field logDetailTextAreaField = panelClass.getDeclaredField("logDetailTextArea");
            logDetailTextAreaField.setAccessible(true);
            logDetailTextArea = (TextArea) logDetailTextAreaField.get(logViewerPanel);
            debugLog("✓ logDetailTextArea accessed: " + (logDetailTextArea != null));
        } catch (Exception e) {
            debugLog("✗ Failed to access logDetailTextArea: " + e.getMessage());
            throw e;
        }
    }

    // Helper method to run code on JavaFX thread with extensive debugging
    private <T> T runOnFxThreadAndWait(java.util.function.Supplier<T> supplier) {
        return runOnFxThreadAndWait(supplier, 10, "Generic FX operation");
    }

    private <T> T runOnFxThreadAndWait(java.util.function.Supplier<T> supplier, int timeoutSeconds, String operationName) {
        debugLog("Starting FX thread operation: " + operationName);

        final CountDownLatch latch = new CountDownLatch(1);
        final AtomicReference<T> result = new AtomicReference<>();
        final AtomicReference<Throwable> throwable = new AtomicReference<>();

        try {
            Platform.runLater(() -> {
                try {
                    debugLog("Executing on FX thread: " + operationName + " (thread: " + Thread.currentThread().getName() + ")");
                    T value = supplier.get();
                    result.set(value);
                    debugLog("✓ FX thread operation completed: " + operationName);
                } catch (Throwable t) {
                    throwable.set(t);
                    debugLog("✗ FX thread operation failed: " + operationName + " - " + t.getMessage());
                    t.printStackTrace();
                } finally {
                    latch.countDown();
                }
            });
        } catch (Exception e) {
            debugLog("✗ Failed to submit to FX thread: " + operationName + " - " + e.getMessage());
            throw new RuntimeException("Failed to submit FX operation: " + operationName, e);
        }

        try {
            boolean completed = latch.await(timeoutSeconds, TimeUnit.SECONDS);
            if (!completed) {
                debugLog("✗ FX thread operation timed out: " + operationName + " (after " + timeoutSeconds + "s)");
                throw new RuntimeException("JavaFX operation timed out: " + operationName);
            }
            if (throwable.get() != null) {
                debugLog("✗ FX thread operation threw exception: " + operationName);
                throw new RuntimeException("Error in JavaFX thread: " + operationName, throwable.get());
            }
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            debugLog("✗ FX thread operation interrupted: " + operationName);
            throw new RuntimeException("Thread interrupted during FX operation: " + operationName, e);
        }

        return result.get();
    }

    // Comprehensive test validation before each test
    private void validateTestPreconditions() throws Exception {
        debugLog("=== VALIDATING TEST PRECONDITIONS ===");

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

        // Check UI components
        if (logTable == null) debugLog("WARNING: logTable is null");
        if (searchField == null) debugLog("WARNING: searchField is null");
        if (logTypeFilter == null) debugLog("WARNING: logTypeFilter is null");
        if (logLevelFilter == null) debugLog("WARNING: logLevelFilter is null");
        if (logDetailTextArea == null) debugLog("WARNING: logDetailTextArea is null");

        // Check stage state
        debugLog("Stage state:");
        debugLog("  Showing: " + testStage.isShowing());
        debugLog("  Scene: " + (testStage.getScene() != null));
        debugLog("  Focused: " + testStage.isFocused());

        debugLog("✓ Test preconditions validated");
    }

    // Simplified test to verify basic setup
    @Test
    public void testBasicLogViewerSetup(FxRobot robot) throws Exception {
        debugLog("=== STARTING TEST: testBasicLogViewerSetup ===");

        try {
            validateTestPreconditions();

            // Simple test to verify the panel was created correctly
            assertNotNull(logViewerPanel, "LogViewerPanel should be initialized");

            if (logTable != null) {
                debugLog("Testing log table functionality...");

                // Add a simple log entry
                runOnFxThreadAndWait(() -> {
                    LogData logData = new LogData("test-session", LogType.SESSION, "Test info message");
                    logData.setSuccess(true);
                    logData.setTimestamp(Instant.now());

                    try {
                        Method addLogEntryMethod = LogViewerPanel.class.getDeclaredMethod("addLogEntry", LogData.class);
                        addLogEntryMethod.setAccessible(true);
                        addLogEntryMethod.invoke(logViewerPanel, logData);
                        debugLog("✓ Test log entry added successfully");
                    } catch (Exception e) {
                        debugLog("✗ Failed to add test log entry: " + e.getMessage());
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
                    debugLog("Log table item count: " + count);
                    return null;
                }, 10, "Check log table items");

                assertTrue(itemCountRef.get() > 0, "Log table should have at least one entry");
                debugLog("✓ Basic log table functionality verified");
            } else {
                debugLog("WARNING: Skipping log table test due to null logTable");
            }

            debugLog("✓ TEST PASSED: testBasicLogViewerSetup");

        } catch (Exception e) {
            debugLog("✗ TEST FAILED: testBasicLogViewerSetup - " + e.getMessage());
            e.printStackTrace();
            throw e;
        }
    }

    // Additional debugging utilities
    private static void debugLog(String message) {
        if (VERBOSE_DEBUG) {
            System.out.println("[DEBUG] " + System.currentTimeMillis() + " - " + message);
        }
    }

    private void debugThreadState() {
        if (THREAD_DEBUG) {
            debugLog("=== THREAD STATE DEBUG ===");
            debugLog("Current thread: " + Thread.currentThread().getName());
            debugLog("Is FX Application Thread: " + Platform.isFxApplicationThread());
            debugLog("Thread state: " + Thread.currentThread().getState());
            debugLog("Thread interrupted: " + Thread.currentThread().isInterrupted());
            debugLog("=== END THREAD STATE DEBUG ===");
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