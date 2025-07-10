package io.github.jspinak.brobot.runner.ui.services.logs;

import javafx.scene.control.Alert;
import javafx.stage.FileChooser;
import javafx.stage.Window;
import lombok.extern.slf4j.Slf4j;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;

/**
 * Controller for log export functionality.
 * Handles file selection and export formats.
 */
@Slf4j
public class LogExportController {
    
    private static final DateTimeFormatter FILE_DATE_FORMAT = DateTimeFormatter.ofPattern("yyyyMMdd_HHmmss");
    
    /**
     * Shows the export dialog and exports logs if a file is selected.
     */
    public void exportLogs(List<LogEntryViewModel> logs, Window ownerWindow) {
        File file = showSaveDialog(ownerWindow);
        if (file != null) {
            try {
                if (file.getName().endsWith(".csv")) {
                    exportAsCSV(logs, file);
                } else {
                    exportAsText(logs, file);
                }
                showExportSuccess(file);
            } catch (IOException e) {
                showExportError(e);
            }
        }
    }
    
    /**
     * Shows the file save dialog.
     */
    private File showSaveDialog(Window ownerWindow) {
        FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle("Export Logs");
        fileChooser.getExtensionFilters().addAll(
            new FileChooser.ExtensionFilter("Text Files", "*.txt"),
            new FileChooser.ExtensionFilter("CSV Files", "*.csv"),
            new FileChooser.ExtensionFilter("All Files", "*.*")
        );
        
        String timestamp = LocalDateTime.now().format(FILE_DATE_FORMAT);
        fileChooser.setInitialFileName("logs_" + timestamp);
        
        return fileChooser.showSaveDialog(ownerWindow);
    }
    
    /**
     * Exports logs as plain text.
     */
    private void exportAsText(List<LogEntryViewModel> logs, File file) throws IOException {
        try (PrintWriter writer = new PrintWriter(new FileWriter(file))) {
            writer.println("=== Brobot Log Export ===");
            writer.println("Exported at: " + LocalDateTime.now());
            writer.println("Total entries: " + logs.size());
            writer.println();
            
            for (LogEntryViewModel entry : logs) {
                writer.println("=".repeat(80));
                writer.println("Time: " + entry.getFullTime());
                writer.println("Level: " + entry.getLevel());
                writer.println("Type: " + entry.getType());
                writer.println("Message: " + entry.getMessage());
                writer.println();
            }
        }
        
        log.info("Logs exported to text file: {}", file.getAbsolutePath());
    }
    
    /**
     * Exports logs as CSV.
     */
    private void exportAsCSV(List<LogEntryViewModel> logs, File file) throws IOException {
        try (PrintWriter writer = new PrintWriter(new FileWriter(file))) {
            // Write header
            writer.println("Time,Level,Type,Message");
            
            // Write data
            for (LogEntryViewModel entry : logs) {
                writer.printf("\"%s\",\"%s\",\"%s\",\"%s\"%n",
                    entry.getFullTime(),
                    entry.getLevel(),
                    entry.getType(),
                    escapeCsvValue(entry.getMessage())
                );
            }
        }
        
        log.info("Logs exported to CSV file: {}", file.getAbsolutePath());
    }
    
    /**
     * Escapes CSV values by replacing quotes with double quotes.
     */
    private String escapeCsvValue(String value) {
        if (value == null) {
            return "";
        }
        return value.replace("\"", "\"\"");
    }
    
    /**
     * Shows success alert.
     */
    private void showExportSuccess(File file) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle("Export Successful");
        alert.setHeaderText("Logs exported successfully");
        alert.setContentText("File saved to:\n" + file.getAbsolutePath());
        alert.showAndWait();
    }
    
    /**
     * Shows error alert.
     */
    private void showExportError(Exception e) {
        log.error("Failed to export logs", e);
        
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setTitle("Export Failed");
        alert.setHeaderText("Failed to export logs");
        alert.setContentText("Error: " + e.getMessage());
        alert.showAndWait();
    }
}