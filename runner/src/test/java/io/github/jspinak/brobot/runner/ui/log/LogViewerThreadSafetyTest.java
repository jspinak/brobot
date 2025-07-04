package io.github.jspinak.brobot.runner.ui.log;

import lombok.Data;

import io.github.jspinak.brobot.runner.events.EventBus;
import io.github.jspinak.brobot.runner.persistence.LogQueryService;
import io.github.jspinak.brobot.runner.ui.icons.IconRegistry;
import io.github.jspinak.brobot.tools.logging.model.LogData;
import io.github.jspinak.brobot.tools.logging.model.LogEventType;
import javafx.application.Platform;
import javafx.collections.ObservableList;
import javafx.scene.Scene;
import javafx.scene.layout.StackPane;
import javafx.stage.Stage;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.Timeout;
import org.junit.jupiter.api.extension.ExtendWith;
import io.github.jspinak.brobot.runner.testutil.JavaFXTestUtils;
import org.junit.jupiter.api.BeforeAll;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.mockito.junit.jupiter.MockitoExtension;
import org.testfx.util.WaitForAsyncUtils;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
@Data
public class LogViewerThreadSafetyTest {

    @Mock
    private EventBus eventBus;
    @Mock
    private IconRegistry iconRegistry;
    @Mock
    private LogQueryService logQueryService;

    private LogViewerPanel logViewerPanel;
    private StackPane rootPane;
    private Stage stage;

    @BeforeAll
    public static void initJavaFX() throws InterruptedException {
        JavaFXTestUtils.initJavaFX();
    }

    @BeforeEach
    public void setUp() throws Exception {
        JavaFXTestUtils.runOnFXThread(() -> {
            when(iconRegistry.getIconView(anyString(), anyInt())).thenReturn(new javafx.scene.image.ImageView());
            when(logQueryService.getRecentLogs(anyInt())).thenReturn(Collections.emptyList());

            logViewerPanel = new LogViewerPanel(logQueryService, eventBus, iconRegistry);
            rootPane = new StackPane();
            rootPane.getChildren().add(logViewerPanel);
            
            stage = new Stage();
            stage.setScene(new Scene(rootPane, 800, 600));
            stage.show();
        });
    }

    @AfterEach
    public void tearDown() throws Exception {
        final CountDownLatch latch = new CountDownLatch(1);
        Platform.runLater(() -> {
            try {
                if (logViewerPanel != null) {
                    logViewerPanel.close();
                }
                rootPane.getChildren().clear();
            } catch (Exception e) {
                fail("Failed to close LogViewerPanel", e);
            } finally {
                latch.countDown();
            }
        });
        assertTrue(latch.await(5, TimeUnit.SECONDS), "Panel cleanup timed out.");
    }

    /**
     * This test now verifies that the cleanup LOGIC is correct,
     * rather than testing the JVM's garbage collector.
     */
    @Test
    @Timeout(value = 20, unit = TimeUnit.SECONDS)
    public void testMemoryManagementOnRefresh() throws Exception {
        final int ENTRIES_TO_ADD = 500;

        // ARRANGE: Create initial data
        List<LogData> initialData = new ArrayList<>();
        for (int i = 0; i < ENTRIES_TO_ADD; i++) {
            initialData.add(new LogData("session", LogEventType.SYSTEM, "Entry " + i));
        }
        when(logQueryService.getRecentLogs(anyInt())).thenReturn(initialData);

        // ACT 1: Load initial data and verify it's there
        Platform.runLater(() -> logViewerPanel.refreshLogs());
        WaitForAsyncUtils.waitForFxEvents();

        ObservableList<LogViewerPanel.LogEntryViewModel> viewModels = getLogEntriesList(logViewerPanel);
        assertEquals(ENTRIES_TO_ADD, viewModels.size(), "Panel should be populated with initial data.");

        // ARRANGE 2: Configure service to return an empty list for the next refresh
        when(logQueryService.getRecentLogs(anyInt())).thenReturn(Collections.emptyList());

        // ACT 2: Refresh the panel, which should trigger the cleanup logic
        Platform.runLater(() -> logViewerPanel.refreshLogs());
        WaitForAsyncUtils.waitForFxEvents();

        // ASSERT: Instead of checking the GC, we verify that the panel's internal
        // state has been correctly cleared, which is what allows the GC to work.
        assertEquals(0, viewModels.size(), "The logEntries list should be empty after refresh.");

        // Verify the direct reference to the selected item is also cleared
        Field selectedLogEntryField = LogViewerPanel.class.getDeclaredField("selectedLogEntry");
        selectedLogEntryField.setAccessible(true);
        assertNull(selectedLogEntryField.get(logViewerPanel), "The selectedLogEntry field should be null after cleanup.");
    }

    // Helper to get the list from the panel instance
    private ObservableList<LogViewerPanel.LogEntryViewModel> getLogEntriesList(LogViewerPanel panel) throws Exception {
        Field logEntriesField = LogViewerPanel.class.getDeclaredField("logEntries");
        logEntriesField.setAccessible(true);
        return (ObservableList<LogViewerPanel.LogEntryViewModel>) logEntriesField.get(panel);
    }
}