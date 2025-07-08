package io.github.jspinak.brobot.runner.ui.automation.components;

import atlantafx.base.theme.Styles;
import io.github.jspinak.brobot.runner.ui.automation.models.AutomationStatus;
import io.github.jspinak.brobot.runner.ui.automation.services.AutomationStatusService;
import io.github.jspinak.brobot.runner.ui.components.base.BrobotCard;
import javafx.application.Platform;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.Label;
import javafx.scene.control.ProgressBar;
import javafx.scene.layout.*;
import javafx.scene.paint.Color;
import javafx.scene.shape.Circle;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

/**
 * UI component for displaying automation execution status.
 * Shows progress, current action, and timing information.
 */
@Slf4j
@Component
public class AutomationStatusPanel extends VBox {
    
    private final AutomationStatusService statusService;
    
    // Status indicators
    private Circle statusIndicator;
    private Label statusLabel;
    private Label automationLabel;
    private Label actionLabel;
    
    // Progress tracking
    private ProgressBar progressBar;
    private Label progressLabel;
    private Label completionLabel;
    
    // Timing
    private Label elapsedLabel;
    private Label remainingLabel;
    
    @Autowired
    public AutomationStatusPanel(AutomationStatusService statusService) {
        this.statusService = statusService;
        initialize();
        setupStatusListener();
    }
    
    private void initialize() {
        setSpacing(12);
        setPadding(new Insets(16));
        getStyleClass().add("automation-status-panel");
        
        // Create header with status indicator
        HBox header = createHeader();
        
        // Create main status card
        BrobotCard statusCard = createStatusCard();
        
        // Create progress card
        BrobotCard progressCard = createProgressCard();
        
        // Create timing card
        BrobotCard timingCard = createTimingCard();
        
        getChildren().addAll(header, statusCard, progressCard, timingCard);
    }
    
    /**
     * Creates the header with status indicator.
     */
    private HBox createHeader() {
        HBox header = new HBox(8);
        header.setAlignment(Pos.CENTER_LEFT);
        
        statusIndicator = new Circle(8);
        updateStatusIndicator("IDLE");
        
        Label title = new Label("Automation Status");
        title.getStyleClass().addAll(Styles.TITLE_3, Styles.TEXT_BOLD);
        
        header.getChildren().addAll(statusIndicator, title);
        return header;
    }
    
    /**
     * Creates the main status display card.
     */
    private BrobotCard createStatusCard() {
        BrobotCard card = new BrobotCard("Current Status");
        
        GridPane grid = new GridPane();
        grid.setHgap(12);
        grid.setVgap(8);
        
        // Status row
        Label statusLabelTitle = new Label("Status:");
        statusLabelTitle.getStyleClass().add(Styles.TEXT_MUTED);
        statusLabel = new Label("Idle");
        statusLabel.getStyleClass().add(Styles.TEXT_BOLD);
        
        // Automation row
        Label automationLabelTitle = new Label("Automation:");
        automationLabelTitle.getStyleClass().add(Styles.TEXT_MUTED);
        automationLabel = new Label("None");
        
        // Action row
        Label actionLabelTitle = new Label("Current Action:");
        actionLabelTitle.getStyleClass().add(Styles.TEXT_MUTED);
        actionLabel = new Label("-");
        actionLabel.setWrapText(true);
        
        // Add to grid
        grid.add(statusLabelTitle, 0, 0);
        grid.add(statusLabel, 1, 0);
        grid.add(automationLabelTitle, 0, 1);
        grid.add(automationLabel, 1, 1);
        grid.add(actionLabelTitle, 0, 2);
        grid.add(actionLabel, 1, 2);
        
        // Configure column constraints
        ColumnConstraints col1 = new ColumnConstraints();
        col1.setPrefWidth(100);
        ColumnConstraints col2 = new ColumnConstraints();
        col2.setHgrow(Priority.ALWAYS);
        grid.getColumnConstraints().addAll(col1, col2);
        
        card.addContent(grid);
        return card;
    }
    
    /**
     * Creates the progress display card.
     */
    private BrobotCard createProgressCard() {
        BrobotCard card = new BrobotCard("Progress");
        
        VBox content = new VBox(8);
        
        // Progress bar
        progressBar = new ProgressBar(0);
        progressBar.setPrefWidth(Double.MAX_VALUE);
        progressBar.getStyleClass().add(Styles.LARGE);
        
        // Progress labels
        HBox progressInfo = new HBox();
        progressInfo.setAlignment(Pos.CENTER);
        
        progressLabel = new Label("0%");
        progressLabel.getStyleClass().add(Styles.TEXT_BOLD);
        
        Region spacer = new Region();
        HBox.setHgrow(spacer, Priority.ALWAYS);
        
        completionLabel = new Label("0 / 0");
        completionLabel.getStyleClass().add(Styles.TEXT_MUTED);
        
        progressInfo.getChildren().addAll(progressLabel, spacer, completionLabel);
        
        content.getChildren().addAll(progressBar, progressInfo);
        card.addContent(content);
        return card;
    }
    
    /**
     * Creates the timing information card.
     */
    private BrobotCard createTimingCard() {
        BrobotCard card = new BrobotCard("Timing");
        
        GridPane grid = new GridPane();
        grid.setHgap(12);
        grid.setVgap(8);
        
        // Elapsed time
        Label elapsedTitle = new Label("Elapsed:");
        elapsedTitle.getStyleClass().add(Styles.TEXT_MUTED);
        elapsedLabel = new Label("00:00");
        
        // Remaining time
        Label remainingTitle = new Label("Estimated Remaining:");
        remainingTitle.getStyleClass().add(Styles.TEXT_MUTED);
        remainingLabel = new Label("--:--");
        
        grid.add(elapsedTitle, 0, 0);
        grid.add(elapsedLabel, 1, 0);
        grid.add(remainingTitle, 0, 1);
        grid.add(remainingLabel, 1, 1);
        
        // Configure column constraints
        ColumnConstraints col1 = new ColumnConstraints();
        col1.setPrefWidth(120);
        ColumnConstraints col2 = new ColumnConstraints();
        col2.setHgrow(Priority.ALWAYS);
        grid.getColumnConstraints().addAll(col1, col2);
        
        card.addContent(grid);
        return card;
    }
    
    /**
     * Sets up the status listener.
     */
    private void setupStatusListener() {
        statusService.addStatusListener(this::updateStatus);
    }
    
    /**
     * Updates the UI based on automation status.
     */
    private void updateStatus(AutomationStatus status) {
        Platform.runLater(() -> {
            // Update status indicator and label
            String state = status.getStateString();
            updateStatusIndicator(state);
            statusLabel.setText(state);
            
            // Update automation info
            automationLabel.setText(status.getCurrentAutomationName());
            actionLabel.setText(status.getCurrentAction() != null ? status.getCurrentAction() : "-");
            
            // Update progress
            progressBar.setProgress(status.getProgress());
            progressLabel.setText(status.getProgressPercentage());
            completionLabel.setText(status.getCompletionRatio());
            
            // Update timing
            elapsedLabel.setText(formatTime(status.getElapsedTime()));
            long remaining = status.getEstimatedTimeRemaining();
            remainingLabel.setText(remaining > 0 ? formatTime(remaining) : "--:--");
            
            // Show error state if needed
            if (status.isHasError()) {
                showError(status.getErrorMessage());
            }
        });
    }
    
    /**
     * Updates the status indicator color based on state.
     */
    private void updateStatusIndicator(String state) {
        statusIndicator.getStyleClass().removeAll("idle", "running", "paused", "error");
        
        switch (state) {
            case "IDLE":
                statusIndicator.setFill(Color.GRAY);
                statusIndicator.getStyleClass().add("idle");
                break;
            case "RUNNING":
                statusIndicator.setFill(Color.GREEN);
                statusIndicator.getStyleClass().add("running");
                break;
            case "PAUSED":
                statusIndicator.setFill(Color.ORANGE);
                statusIndicator.getStyleClass().add("paused");
                break;
            case "ERROR":
                statusIndicator.setFill(Color.RED);
                statusIndicator.getStyleClass().add("error");
                break;
        }
    }
    
    /**
     * Formats time in HH:MM:SS or MM:SS format.
     */
    private String formatTime(long millis) {
        if (millis <= 0) {
            return "00:00";
        }
        
        long seconds = millis / 1000;
        long minutes = seconds / 60;
        long hours = minutes / 60;
        
        if (hours > 0) {
            return String.format("%02d:%02d:%02d", hours, minutes % 60, seconds % 60);
        } else {
            return String.format("%02d:%02d", minutes, seconds % 60);
        }
    }
    
    /**
     * Shows an error message.
     */
    private void showError(String errorMessage) {
        if (errorMessage != null && !errorMessage.isEmpty()) {
            actionLabel.setText("ERROR: " + errorMessage);
            actionLabel.getStyleClass().add(Styles.DANGER);
        }
    }
    
    /**
     * Resets the display to idle state.
     */
    public void reset() {
        Platform.runLater(() -> {
            updateStatusIndicator("IDLE");
            statusLabel.setText("Idle");
            automationLabel.setText("None");
            actionLabel.setText("-");
            actionLabel.getStyleClass().remove(Styles.DANGER);
            
            progressBar.setProgress(0);
            progressLabel.setText("0%");
            completionLabel.setText("0 / 0");
            
            elapsedLabel.setText("00:00");
            remainingLabel.setText("--:--");
        });
    }
}