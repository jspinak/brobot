package io.github.jspinak.brobot.runner.ui.log;

import io.github.jspinak.brobot.report.log.model.LogData;
import io.github.jspinak.brobot.runner.events.EventBus;
import io.github.jspinak.brobot.runner.persistence.LogQueryService;
import io.github.jspinak.brobot.runner.ui.icons.IconRegistry;
import lombok.Setter;

import java.io.File;

@Setter
public class TestableLogViewerPanel extends LogViewerPanel {
    /**
     * Creates a new LogViewerPanel.
     *
     * @param logQueryService The service to query logs
     * @param eventBus     The event bus for communication
     * @param iconRegistry The icon registry for icons
     */
    public TestableLogViewerPanel(LogQueryService logQueryService, EventBus eventBus, IconRegistry iconRegistry) {
        super(logQueryService, eventBus, iconRegistry);
    }

    // Make addLogEntry public for testing
    @Override
    public void addLogEntry(LogData logData) {
        super.addLogEntry(logData);
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

    public javafx.scene.control.TextField getSearchField() {
        try {
            java.lang.reflect.Field field = LogViewerPanel.class.getDeclaredField("searchField");
            field.setAccessible(true);
            return (javafx.scene.control.TextField) field.get(this);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }
}
