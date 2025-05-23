package io.github.jspinak.brobot.runner.ui.log;

import io.github.jspinak.brobot.log.entities.LogEntry;
import io.github.jspinak.brobot.runner.events.EventBus;
import io.github.jspinak.brobot.runner.ui.icons.IconRegistry;
import javafx.stage.FileChooser;
import lombok.Setter;

import java.io.File;

@Setter
public class TestableLogViewerPanel extends LogViewerPanel {
    /**
     * Creates a new LogViewerPanel.
     *
     * @param eventBus     The event bus for communication
     * @param iconRegistry The icon registry for icons
     */
    public TestableLogViewerPanel(EventBus eventBus, IconRegistry iconRegistry) {
        super(eventBus, iconRegistry);
    }

    // Make addLogEntry public for testing
    @Override
    public void addLogEntry(LogEntry logEntry) {
        super.addLogEntry(logEntry);
    }

    // Override exportLogs to avoid UI dialogs
    @Override
    protected void exportLogs() {
        if (exportFile != null) {
            if (exportFile.getName().toLowerCase().endsWith(".csv")) {
                exportLogsAsCSV(exportFile);
            } else {
                exportLogsAsText(exportFile);
            }
        }
    }

    // File to export to when exportLogs is called
    private File exportFile;

    // Prevent UI dialogs
    @Override
    void showExportSuccessMessage(String filePath) {
        System.out.println("Export successful: " + filePath);
    }

    @Override
    void showExportErrorMessage(String errorMessage) {
        System.err.println("Export failed: " + errorMessage);
    }
}
