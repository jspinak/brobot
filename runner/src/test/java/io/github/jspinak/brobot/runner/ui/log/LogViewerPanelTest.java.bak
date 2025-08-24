package io.github.jspinak.brobot.runner.ui.log;

import lombok.Data;

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
import javafx.scene.control.TableView;
import javafx.stage.Stage;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import io.github.jspinak.brobot.runner.testutil.JavaFXTestUtils;
import org.junit.jupiter.api.BeforeAll;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;
import org.testfx.util.WaitForAsyncUtils;

import java.lang.reflect.Field;
import java.time.Instant;
import java.util.Collections;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;
import java.util.function.Consumer;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
@Data
public class LogViewerPanelTest {

    private LogViewerPanel logViewerPanel;

    @Mock
    private EventBus eventBus;

    @Mock
    private IconRegistry iconRegistry;

    @Mock
    private LogQueryService logQueryService;

    private TableView<LogViewerPanel.LogEntryViewModel> logTable;
    private ObservableList<LogViewerPanel.LogEntryViewModel> logEntries;

    @BeforeAll
    public static void initJavaFX() throws InterruptedException {
        JavaFXTestUtils.initJavaFX();
    }

    @BeforeEach
    public void setUp() throws Exception {
        JavaFXTestUtils.runOnFXThread(() -> {
            // Mock dependencies
            when(iconRegistry.getIconView(anyString(), anyInt())).thenReturn(new javafx.scene.image.ImageView());
            when(logQueryService.getRecentLogs(anyInt())).thenReturn(Collections.emptyList());

            // Create the panel. This will subscribe to the mocked EventBus.
            logViewerPanel = new LogViewerPanel(logQueryService, eventBus, iconRegistry);

            try {
                // Access private fields for verification
                Field logTableField = LogViewerPanel.class.getDeclaredField("logTable");
                logTableField.setAccessible(true);
                logTable = (TableView<LogViewerPanel.LogEntryViewModel>) logTableField.get(logViewerPanel);

                Field logEntriesField = LogViewerPanel.class.getDeclaredField("logEntries");
                logEntriesField.setAccessible(true);
                logEntries = (ObservableList<LogViewerPanel.LogEntryViewModel>) logEntriesField.get(logViewerPanel);
            } catch (Exception e) {
                throw new RuntimeException(e);
            }
        });
    }

    @Test
    public void testInitialization() {
        assertNotNull(logViewerPanel);
        // Verify that the panel subscribed to events on the bus
        verify(eventBus, times(3)).subscribe(any(BrobotEvent.EventType.class), any());
    }

    @Test
    public void testHandleLogEventViaBus() throws TimeoutException {
        // --- This is the new, correct way to test event handling ---
        // 1. Capture the handler that the panel subscribed with
        var handlerCaptor = ArgumentCaptor.forClass(Consumer.class);
        verify(eventBus, times(3)).subscribe(any(BrobotEvent.EventType.class), handlerCaptor.capture());

        // All subscriptions use the same handler, so we just need one
        Consumer<BrobotEvent> handler = handlerCaptor.getAllValues().get(0);

        // 2. Create events to test
        LogEvent infoEvent = LogEvent.info(this, "Info message", "ACTION");
        LogEvent warningEvent = LogEvent.warning(this, "Warning message", "SYSTEM");
        LogData errorLogData = new LogData("session", LogEventType.ERROR, "Error from LogData");
        LogEntryEvent errorEntryEvent = LogEntryEvent.created(this, errorLogData);

        // 3. Invoke the captured handler on the JavaFX thread, simulating the EventBus
        Platform.runLater(() -> {
            handler.accept(infoEvent);
            handler.accept(warningEvent);
            handler.accept(errorEntryEvent);
        });

        // 4. Wait for the UI to process the events
        WaitForAsyncUtils.waitFor(5, TimeUnit.SECONDS, () -> logEntries.size() == 3);

        // 5. Assert the results
        assertEquals(3, logEntries.size());
        // Note: logs are added to the front, so the order is reversed
        assertEquals("Error from LogData", logEntries.get(0).getMessage());
        assertEquals("ERROR", logEntries.get(0).getLevel());

        assertEquals("Warning message", logEntries.get(1).getMessage());
        assertEquals("WARNING", logEntries.get(1).getLevel());

        assertEquals("Info message", logEntries.get(2).getMessage());
        assertEquals("INFO", logEntries.get(2).getLevel());
    }
}