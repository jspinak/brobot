package io.github.jspinak.brobot.runner.ui;

import io.github.jspinak.brobot.runner.event.ActionRecordedEvent;
import io.github.jspinak.brobot.runner.event.RecordingStartedEvent;
import io.github.jspinak.brobot.runner.event.RecordingStoppedEvent;
import io.github.jspinak.brobot.persistence.PersistenceProvider.SessionMetadata;
import io.github.jspinak.brobot.runner.persistence.entity.RecordingSessionEntity;
import io.github.jspinak.brobot.runner.service.ActionHistoryExportService;
import io.github.jspinak.brobot.runner.service.PersistenceAdapterService;
import javafx.application.Platform;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.beans.property.SimpleIntegerProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import javafx.scene.paint.Color;
import javafx.scene.shape.Circle;
import javafx.stage.FileChooser;
import lombok.extern.slf4j.Slf4j;
import net.rgielen.fxweaver.core.FxmlView;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;

import java.io.File;
import java.io.IOException;
import java.time.Duration;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Optional;
import java.util.Timer;
import java.util.TimerTask;

/**
 * JavaFX panel for controlling action recording.
 * Provides start/stop controls, session management, and export functionality.
 */
@Component
@FxmlView
@Slf4j
public class RecordingControlPanel extends VBox {
    
    @Autowired
    private PersistenceAdapterService recordingService;
    
    @Autowired
    private ActionHistoryExportService exportService;
    
    // UI Components
    private ToggleButton recordButton;
    private Label sessionLabel;
    private Label statusLabel;
    private Label actionCountLabel;
    private Label successRateLabel;
    private ProgressBar successRateBar;
    private Button exportButton;
    private Button pauseButton;
    private Circle recordingIndicator;
    private Label durationLabel;
    
    // Properties for data binding
    private final SimpleBooleanProperty recording = new SimpleBooleanProperty(false);
    private final SimpleIntegerProperty actionCount = new SimpleIntegerProperty(0);
    private final SimpleIntegerProperty successCount = new SimpleIntegerProperty(0);
    private final SimpleStringProperty sessionName = new SimpleStringProperty("");
    
    // Timer for duration updates
    private Timer durationTimer;
    private LocalDateTime recordingStartTime;
    
    public RecordingControlPanel() {
        initialize();
    }
    
    private void initialize() {
        setPadding(new Insets(10));
        setSpacing(10);
        setStyle("-fx-background-color: #f0f0f0; -fx-border-color: #cccccc; -fx-border-radius: 5;");
        
        // Title
        Label titleLabel = new Label("Action Recording");
        titleLabel.setStyle("-fx-font-size: 16px; -fx-font-weight: bold;");
        
        // Recording controls
        HBox controlBox = new HBox(10);
        controlBox.setAlignment(Pos.CENTER_LEFT);
        
        recordButton = new ToggleButton("Record");
        recordButton.setStyle("-fx-base: #ff4444;");
        recordButton.setOnAction(e -> toggleRecording());
        
        pauseButton = new Button("Pause");
        pauseButton.setDisable(true);
        pauseButton.setOnAction(e -> pauseRecording());
        
        recordingIndicator = new Circle(8);
        recordingIndicator.setFill(Color.GRAY);
        
        controlBox.getChildren().addAll(recordButton, pauseButton, recordingIndicator);
        
        // Session info
        VBox sessionBox = new VBox(5);
        sessionLabel = new Label("No active session");
        sessionLabel.setStyle("-fx-font-weight: bold;");
        
        durationLabel = new Label("Duration: 00:00:00");
        
        sessionBox.getChildren().addAll(sessionLabel, durationLabel);
        
        // Statistics
        GridPane statsGrid = new GridPane();
        statsGrid.setHgap(10);
        statsGrid.setVgap(5);
        
        statsGrid.add(new Label("Actions:"), 0, 0);
        actionCountLabel = new Label("0");
        statsGrid.add(actionCountLabel, 1, 0);
        
        statsGrid.add(new Label("Success Rate:"), 0, 1);
        successRateLabel = new Label("0%");
        statsGrid.add(successRateLabel, 1, 1);
        
        successRateBar = new ProgressBar(0);
        successRateBar.setPrefWidth(150);
        statsGrid.add(successRateBar, 0, 2, 2, 1);
        
        // Export controls
        HBox exportBox = new HBox(10);
        exportBox.setAlignment(Pos.CENTER);
        
        exportButton = new Button("Export Session");
        exportButton.setDisable(true);
        exportButton.setOnAction(e -> exportCurrentSession());
        
        Button importButton = new Button("Import");
        importButton.setOnAction(e -> importSession());
        
        exportBox.getChildren().addAll(exportButton, importButton);
        
        // Status
        statusLabel = new Label("Ready");
        statusLabel.setStyle("-fx-text-fill: #666666;");
        
        // Add all components
        getChildren().addAll(
            titleLabel,
            new Separator(),
            controlBox,
            sessionBox,
            new Separator(),
            statsGrid,
            new Separator(),
            exportBox,
            statusLabel
        );
        
        // Bind properties
        recordButton.selectedProperty().bindBidirectional(recording);
        recording.addListener((obs, old, isRecording) -> {
            updateRecordingIndicator(isRecording);
            pauseButton.setDisable(!isRecording);
        });
    }
    
    private void toggleRecording() {
        if (recordButton.isSelected()) {
            startRecording();
        } else {
            stopRecording();
        }
    }
    
    private void startRecording() {
        // Show dialog for session details
        Dialog<SessionInfo> dialog = new Dialog<>();
        dialog.setTitle("Start Recording Session");
        dialog.setHeaderText("Enter session details");
        
        // Create form
        GridPane grid = new GridPane();
        grid.setHgap(10);
        grid.setVgap(10);
        grid.setPadding(new Insets(20, 150, 10, 10));
        
        TextField nameField = new TextField();
        nameField.setPromptText("Session name");
        nameField.setText("Session " + LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm")));
        
        TextField appField = new TextField();
        appField.setPromptText("Application name");
        appField.setText("Brobot Automation");
        
        TextArea descField = new TextArea();
        descField.setPromptText("Description (optional)");
        descField.setPrefRowCount(3);
        
        grid.add(new Label("Name:"), 0, 0);
        grid.add(nameField, 1, 0);
        grid.add(new Label("Application:"), 0, 1);
        grid.add(appField, 1, 1);
        grid.add(new Label("Description:"), 0, 2);
        grid.add(descField, 1, 2);
        
        dialog.getDialogPane().setContent(grid);
        
        // Add buttons
        ButtonType startButtonType = new ButtonType("Start", ButtonBar.ButtonData.OK_DONE);
        dialog.getDialogPane().getButtonTypes().addAll(startButtonType, ButtonType.CANCEL);
        
        // Convert result
        dialog.setResultConverter(dialogButton -> {
            if (dialogButton == startButtonType) {
                return new SessionInfo(nameField.getText(), appField.getText(), descField.getText());
            }
            return null;
        });
        
        Optional<SessionInfo> result = dialog.showAndWait();
        
        result.ifPresentOrElse(
            info -> {
                String sessionId = recordingService.startRecording(
                    info.name, info.application, info.description
                );
                
                sessionName.set(info.name);
                sessionLabel.setText("Session: " + info.name);
                statusLabel.setText("Recording started");
                exportButton.setDisable(false);
                
                // Start duration timer
                recordingStartTime = LocalDateTime.now();
                startDurationTimer();
                
                log.info("Started recording session: {}", info.name);
            },
            () -> {
                // User cancelled, reset toggle
                recordButton.setSelected(false);
            }
        );
    }
    
    private void stopRecording() {
        String sessionId = recordingService.stopRecording();
        
        if (sessionId != null) {
            SessionMetadata metadata = recordingService.getSessionMetadata(sessionId);
            statusLabel.setText("Recording stopped - " + metadata.getTotalActions() + " actions recorded");
            
            // Stop duration timer
            stopDurationTimer();
            
            // Show summary
            Alert alert = new Alert(Alert.AlertType.INFORMATION);
            alert.setTitle("Recording Complete");
            alert.setHeaderText("Session: " + metadata.getName());
            alert.setContentText(String.format(
                "Total Actions: %d\nSuccessful: %d\nSuccess Rate: %.1f%%",
                metadata.getTotalActions(),
                metadata.getSuccessfulActions(),
                metadata.getSuccessRate()
            ));
            alert.showAndWait();
            
            log.info("Stopped recording session: {}", metadata.getName());
        }
        
        // Reset UI
        sessionLabel.setText("No active session");
        durationLabel.setText("Duration: 00:00:00");
        actionCount.set(0);
        successCount.set(0);
        updateStatistics();
    }
    
    private void pauseRecording() {
        if (pauseButton.getText().equals("Pause")) {
            recordingService.pauseRecording();
            pauseButton.setText("Resume");
            statusLabel.setText("Recording paused");
            stopDurationTimer();
        } else {
            recordingService.resumeRecording();
            pauseButton.setText("Pause");
            statusLabel.setText("Recording resumed");
            startDurationTimer();
        }
    }
    
    private void exportCurrentSession() {
        String currentSessionId = recordingService.getCurrentSessionId();
        if (currentSessionId != null) {
            SessionMetadata metadata = recordingService.getSessionMetadata(currentSessionId);
            FileChooser fileChooser = new FileChooser();
            fileChooser.setTitle("Export Session");
            fileChooser.setInitialFileName(metadata.getName() + "_" + 
                LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMdd_HHmmss")) + ".json");
            fileChooser.getExtensionFilters().addAll(
                new FileChooser.ExtensionFilter("JSON Files", "*.json"),
                new FileChooser.ExtensionFilter("CSV Files", "*.csv"),
                new FileChooser.ExtensionFilter("Compressed JSON", "*.zip")
            );
            
            File file = fileChooser.showSaveDialog(getScene().getWindow());
            
            if (file != null) {
                try {
                    ActionHistoryExportService.ExportFormat format = 
                        determineFormat(file.getName());
                    exportService.exportSessionToFile(Long.parseLong(currentSessionId), file, format);
                    
                    statusLabel.setText("Exported to: " + file.getName());
                    
                    Alert alert = new Alert(Alert.AlertType.INFORMATION);
                    alert.setTitle("Export Complete");
                    alert.setHeaderText("Session exported successfully");
                    alert.setContentText("File: " + file.getAbsolutePath());
                    alert.showAndWait();
                    
                } catch (IOException e) {
                    log.error("Failed to export session", e);
                    showError("Export failed: " + e.getMessage());
                }
            }
        }
    }
    
    private void importSession() {
        FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle("Import Session");
        fileChooser.getExtensionFilters().addAll(
            new FileChooser.ExtensionFilter("All Supported", "*.json", "*.csv", "*.zip"),
            new FileChooser.ExtensionFilter("JSON Files", "*.json"),
            new FileChooser.ExtensionFilter("CSV Files", "*.csv"),
            new FileChooser.ExtensionFilter("Compressed Files", "*.zip")
        );
        
        File file = fileChooser.showOpenDialog(getScene().getWindow());
        
        if (file != null) {
            try {
                String sessionName = "Imported_" + LocalDateTime.now()
                    .format(DateTimeFormatter.ofPattern("yyyyMMdd_HHmmss"));
                
                RecordingSessionEntity session = exportService.importToSession(file, sessionName);
                
                statusLabel.setText("Imported session: " + session.getName());
                
                Alert alert = new Alert(Alert.AlertType.INFORMATION);
                alert.setTitle("Import Complete");
                alert.setHeaderText("Session imported successfully");
                alert.setContentText(String.format(
                    "Session: %s\nActions: %d",
                    session.getName(),
                    session.getTotalActions()
                ));
                alert.showAndWait();
                
            } catch (IOException e) {
                log.error("Failed to import session", e);
                showError("Import failed: " + e.getMessage());
            }
        }
    }
    
    // Event listeners
    
    @EventListener
    public void onRecordingStarted(RecordingStartedEvent event) {
        Platform.runLater(() -> {
            recording.set(true);
            updateRecordingIndicator(true);
        });
    }
    
    @EventListener
    public void onRecordingStopped(RecordingStoppedEvent event) {
        Platform.runLater(() -> {
            recording.set(false);
            updateRecordingIndicator(false);
        });
    }
    
    @EventListener
    public void onActionRecorded(ActionRecordedEvent event) {
        Platform.runLater(() -> {
            actionCount.set(actionCount.get() + 1);
            if (event.wasSuccessful()) {
                successCount.set(successCount.get() + 1);
            }
            updateStatistics();
        });
    }
    
    // Helper methods
    
    private void updateStatistics() {
        actionCountLabel.setText(String.valueOf(actionCount.get()));
        
        if (actionCount.get() > 0) {
            double rate = (double) successCount.get() / actionCount.get();
            successRateLabel.setText(String.format("%.1f%%", rate * 100));
            successRateBar.setProgress(rate);
        } else {
            successRateLabel.setText("0%");
            successRateBar.setProgress(0);
        }
    }
    
    private void updateRecordingIndicator(boolean isRecording) {
        if (isRecording) {
            recordingIndicator.setFill(Color.RED);
            // Add pulsing animation
            recordingIndicator.setOpacity(1.0);
        } else {
            recordingIndicator.setFill(Color.GRAY);
            recordingIndicator.setOpacity(0.5);
        }
    }
    
    private void startDurationTimer() {
        durationTimer = new Timer(true);
        durationTimer.scheduleAtFixedRate(new TimerTask() {
            @Override
            public void run() {
                Platform.runLater(() -> {
                    if (recordingStartTime != null) {
                        Duration duration = Duration.between(recordingStartTime, LocalDateTime.now());
                        durationLabel.setText("Duration: " + formatDuration(duration));
                    }
                });
            }
        }, 0, 1000);
    }
    
    private void stopDurationTimer() {
        if (durationTimer != null) {
            durationTimer.cancel();
            durationTimer = null;
        }
    }
    
    private String formatDuration(Duration duration) {
        long hours = duration.toHours();
        long minutes = duration.toMinutesPart();
        long seconds = duration.toSecondsPart();
        return String.format("%02d:%02d:%02d", hours, minutes, seconds);
    }
    
    private ActionHistoryExportService.ExportFormat determineFormat(String filename) {
        if (filename.endsWith(".csv")) {
            return ActionHistoryExportService.ExportFormat.CSV;
        } else if (filename.endsWith(".zip")) {
            return ActionHistoryExportService.ExportFormat.JSON_COMPRESSED;
        } else {
            return ActionHistoryExportService.ExportFormat.JSON;
        }
    }
    
    private void showError(String message) {
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setTitle("Error");
        alert.setHeaderText("An error occurred");
        alert.setContentText(message);
        alert.showAndWait();
    }
    
    // Helper class for session info
    private static class SessionInfo {
        final String name;
        final String application;
        final String description;
        
        SessionInfo(String name, String application, String description) {
            this.name = name;
            this.application = application;
            this.description = description;
        }
    }
}