package io.github.jspinak.brobot.runner.ui;

import java.time.Duration;
import java.time.format.DateTimeFormatter;
import java.util.Map;
import javafx.application.Platform;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.Label;
import javafx.scene.control.ProgressBar;
import javafx.scene.control.Separator;
import javafx.scene.layout.*;
import javafx.scene.paint.Color;
import javafx.scene.shape.Circle;

import io.github.jspinak.brobot.runner.execution.ExecutionState;
import io.github.jspinak.brobot.runner.execution.ExecutionStatus;
import io.github.jspinak.brobot.runner.hotkeys.HotkeyManager;
import io.github.jspinak.brobot.runner.ui.components.base.BrobotCard;

import atlantafx.base.theme.Styles;

/**
 * Enhanced status panel that displays automation state with visual indicators and hotkey
 * information.
 */
public class AutomationStatusPanel extends BrobotCard {
    private static final DateTimeFormatter TIME_FORMATTER = DateTimeFormatter.ofPattern("HH:mm:ss");

    private final Label stateLabel;
    private final Label currentOperationLabel;
    private final Label durationLabel;
    private final ProgressBar progressBar;
    private final Circle statusIndicator;
    private final VBox hotkeyInfoBox;
    private final HotkeyManager hotkeyManager;

    // Colors for different states
    private static final Color COLOR_IDLE = Color.GRAY;
    private static final Color COLOR_RUNNING = Color.GREEN;
    private static final Color COLOR_PAUSED = Color.ORANGE;
    private static final Color COLOR_STOPPED = Color.RED;
    private static final Color COLOR_ERROR = Color.DARKRED;
    private static final Color COLOR_COMPLETED = Color.DARKGREEN;

    public AutomationStatusPanel(HotkeyManager hotkeyManager) {
        super("Automation Status");
        this.hotkeyManager = hotkeyManager;
        getStyleClass().add("automation-status-panel");

        VBox contentBox = new VBox(10);
        contentBox.setPadding(new Insets(16));

        // Main status display
        HBox statusRow = new HBox(15);
        statusRow.setAlignment(Pos.CENTER_LEFT);

        // Status indicator circle
        statusIndicator = new Circle(15);
        statusIndicator.setFill(COLOR_IDLE);
        statusIndicator.setStroke(Color.BLACK);
        statusIndicator.setStrokeWidth(2);

        // State label
        stateLabel = new Label("IDLE");
        stateLabel.getStyleClass().addAll(Styles.TITLE_2, Styles.TEXT_BOLD);

        // Duration label
        durationLabel = new Label("00:00:00");
        durationLabel.getStyleClass().addAll(Styles.TITLE_3, "monospace");
        durationLabel.setStyle("-fx-font-family: monospace;");

        statusRow.getChildren().addAll(statusIndicator, stateLabel, createSpacer(), durationLabel);

        // Current operation label
        currentOperationLabel = new Label("Ready");
        currentOperationLabel.getStyleClass().add(Styles.TEXT_MUTED);
        currentOperationLabel.setWrapText(true);

        // Progress bar
        progressBar = new ProgressBar(0);
        progressBar.setPrefHeight(20);
        progressBar.setMaxWidth(Double.MAX_VALUE);
        HBox.setHgrow(progressBar, Priority.ALWAYS);

        // Hotkey info
        hotkeyInfoBox = createHotkeyInfoBox();

        // Add all components to content box
        contentBox
                .getChildren()
                .addAll(
                        statusRow,
                        currentOperationLabel,
                        progressBar,
                        new Separator(),
                        hotkeyInfoBox);

        // Add content box to card
        addContent(contentBox);
    }

    private Region createSpacer() {
        Region spacer = new Region();
        HBox.setHgrow(spacer, Priority.ALWAYS);
        return spacer;
    }

    private VBox createHotkeyInfoBox() {
        VBox box = new VBox(5);
        box.setPadding(new Insets(10, 0, 0, 0));

        Label title = new Label("Hotkeys:");
        title.getStyleClass().addAll(Styles.TITLE_4, Styles.TEXT_BOLD);
        box.getChildren().add(title);

        updateHotkeyDisplay();

        return box;
    }

    /** Updates the hotkey display with current mappings */
    public void updateHotkeyDisplay() {
        Platform.runLater(
                () -> {
                    // Clear existing hotkey labels (except title)
                    if (hotkeyInfoBox.getChildren().size() > 1) {
                        hotkeyInfoBox
                                .getChildren()
                                .subList(1, hotkeyInfoBox.getChildren().size())
                                .clear();
                    }

                    // Add current hotkey mappings
                    Map<HotkeyManager.HotkeyAction, javafx.scene.input.KeyCombination> hotkeys =
                            hotkeyManager.getAllHotkeys();

                    for (Map.Entry<HotkeyManager.HotkeyAction, javafx.scene.input.KeyCombination>
                            entry : hotkeys.entrySet()) {
                        HotkeyManager.HotkeyAction action = entry.getKey();
                        String keyCombo = hotkeyManager.getHotkeyDisplayString(action);

                        HBox hotkeyRow = new HBox(10);
                        hotkeyRow.setAlignment(Pos.CENTER_LEFT);

                        Label actionLabel = new Label(action.getDisplayName() + ":");
                        actionLabel.setMinWidth(150);
                        actionLabel.setStyle("-fx-font-size: 12px;");

                        Label keyLabel = new Label(keyCombo);
                        keyLabel.setStyle(
                                "-fx-font-size: 12px; -fx-font-family: monospace; -fx-font-weight:"
                                        + " bold;");

                        hotkeyRow.getChildren().addAll(actionLabel, keyLabel);
                        hotkeyInfoBox.getChildren().add(hotkeyRow);
                    }
                });
    }

    /** Updates the status display based on execution status */
    public void updateStatus(ExecutionStatus status) {
        Platform.runLater(
                () -> {
                    ExecutionState state = status.getState();

                    // Update state label and color
                    stateLabel.setText(state.name());
                    statusIndicator.setFill(getColorForState(state));

                    // Animate the indicator for running state
                    if (state == ExecutionState.RUNNING) {
                        statusIndicator.setOpacity(0.8);
                        // Could add pulse animation here
                    } else {
                        statusIndicator.setOpacity(1.0);
                    }

                    // Update operation label
                    String operation = status.getCurrentOperation();
                    if (operation != null && !operation.isEmpty()) {
                        currentOperationLabel.setText(operation);
                    } else {
                        currentOperationLabel.setText(getDefaultMessageForState(state));
                    }

                    // Update progress
                    progressBar.setProgress(status.getProgress());

                    // Update duration
                    Duration duration = status.getDuration();
                    if (duration != null) {
                        long hours = duration.toHours();
                        long minutes = duration.toMinutesPart();
                        long seconds = duration.toSecondsPart();
                        durationLabel.setText(
                                String.format("%02d:%02d:%02d", hours, minutes, seconds));
                    }

                    // Style the progress bar based on state
                    updateProgressBarStyle(state);
                });
    }

    private Color getColorForState(ExecutionState state) {
        switch (state) {
            case IDLE:
                return COLOR_IDLE;
            case STARTING:
            case RUNNING:
                return COLOR_RUNNING;
            case PAUSED:
                return COLOR_PAUSED;
            case STOPPING:
            case STOPPED:
                return COLOR_STOPPED;
            case ERROR:
            case FAILED:
            case TIMEOUT:
                return COLOR_ERROR;
            case COMPLETED:
                return COLOR_COMPLETED;
            default:
                return COLOR_IDLE;
        }
    }

    private String getDefaultMessageForState(ExecutionState state) {
        switch (state) {
            case IDLE:
                return "Ready to start automation";
            case STARTING:
                return "Starting automation...";
            case RUNNING:
                return "Automation in progress...";
            case PAUSED:
                return "Automation paused - Press hotkey to resume";
            case STOPPING:
                return "Stopping automation...";
            case STOPPED:
                return "Automation stopped by user";
            case COMPLETED:
                return "Automation completed successfully";
            case ERROR:
                return "Automation encountered an error";
            case FAILED:
                return "Automation failed";
            case TIMEOUT:
                return "Automation timed out";
            default:
                return state.name();
        }
    }

    private void updateProgressBarStyle(ExecutionState state) {
        progressBar
                .getStyleClass()
                .removeAll("progress-bar-success", "progress-bar-warning", "progress-bar-error");

        switch (state) {
            case RUNNING:
            case COMPLETED:
                progressBar.getStyleClass().add("progress-bar-success");
                break;
            case PAUSED:
                progressBar.getStyleClass().add("progress-bar-warning");
                break;
            case ERROR:
            case FAILED:
            case TIMEOUT:
                progressBar.getStyleClass().add("progress-bar-error");
                break;
        }
    }

    /** Resets the display to initial state */
    public void reset() {
        Platform.runLater(
                () -> {
                    stateLabel.setText("IDLE");
                    statusIndicator.setFill(COLOR_IDLE);
                    currentOperationLabel.setText("Ready");
                    progressBar.setProgress(0);
                    durationLabel.setText("00:00:00");
                });
    }
}
