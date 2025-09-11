package io.github.jspinak.brobot.runner.ui.automation.components;

import javafx.application.Platform;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Region;
import javafx.scene.layout.VBox;

import jakarta.annotation.PostConstruct;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import io.github.jspinak.brobot.runner.ui.automation.factories.AutomationButtonFactory;
import io.github.jspinak.brobot.runner.ui.automation.factories.AutomationButtonFactory.ControlButtonType;
import io.github.jspinak.brobot.runner.ui.automation.services.AutomationControlService;
import io.github.jspinak.brobot.runner.ui.automation.services.AutomationStatusService;
import io.github.jspinak.brobot.runner.ui.components.base.BrobotPanel;

import atlantafx.base.theme.Styles;
import lombok.extern.slf4j.Slf4j;

/**
 * UI component for automation execution controls. Provides play, pause, resume, and stop
 * functionality.
 */
@Slf4j
@Component
public class AutomationControlPanel extends BrobotPanel {

    private final AutomationButtonFactory buttonFactory;
    private final AutomationControlService controlService;
    private final AutomationStatusService statusService;

    // Control buttons
    private Button playButton;
    private Button pauseResumeButton;
    private Button stopButton;

    // Status labels
    private Label statusLabel;
    private Label automationNameLabel;
    private Label elapsedTimeLabel;

    @Autowired
    public AutomationControlPanel(
            AutomationButtonFactory buttonFactory,
            AutomationControlService controlService,
            AutomationStatusService statusService) {

        super();
        this.buttonFactory = buttonFactory;
        this.controlService = controlService;
        this.statusService = statusService;
    }

    @PostConstruct
    private void postInit() {
        // Re-initialize with dependencies available
        initialize();
        setupStatusListener();
    }

    @Override
    protected void initialize() {
        // Skip initialization if dependencies not ready
        if (buttonFactory == null || controlService == null || statusService == null) {
            return;
        }
        VBox content = new VBox(16);
        content.setPadding(new Insets(16));
        content.setMinHeight(100);
        content.setPrefHeight(Region.USE_COMPUTED_SIZE);
        content.setMaxHeight(Region.USE_COMPUTED_SIZE);

        // Create control buttons section
        HBox controlSection = createControlSection();

        // Create status section
        VBox statusSection = createStatusSection();

        content.getChildren().addAll(controlSection, statusSection);
        getChildren().add(content);
    }

    /** Creates the control buttons section. */
    private HBox createControlSection() {
        HBox controls = new HBox(8);
        controls.setAlignment(Pos.CENTER_LEFT);

        // Create buttons
        playButton = buttonFactory.createControlButton(ControlButtonType.PLAY, this::handlePlay);
        pauseResumeButton = buttonFactory.createPauseResumeToggleButton(this::handlePauseResume);
        stopButton = buttonFactory.createControlButton(ControlButtonType.STOP, this::handleStop);

        // Initially disable pause and stop
        pauseResumeButton.setDisable(true);
        stopButton.setDisable(true);

        controls.getChildren().addAll(playButton, pauseResumeButton, stopButton);

        return controls;
    }

    /** Creates the status display section. */
    private VBox createStatusSection() {
        VBox status = new VBox(12);
        status.setPadding(new Insets(8, 0, 0, 0));

        // Status label
        statusLabel = new Label("Ready");
        statusLabel.getStyleClass().addAll(Styles.TEXT_BOLD, Styles.TITLE_4);

        // Automation name label
        automationNameLabel = new Label("No automation running");
        automationNameLabel.getStyleClass().add(Styles.TEXT_MUTED);
        automationNameLabel.setWrapText(true);
        automationNameLabel.setMaxWidth(300);

        // Elapsed time label
        elapsedTimeLabel = new Label("00:00");
        elapsedTimeLabel.getStyleClass().add(Styles.TEXT_SMALL);

        status.getChildren().addAll(statusLabel, automationNameLabel, elapsedTimeLabel);

        return status;
    }

    /** Sets up the status listener to update UI. */
    private void setupStatusListener() {
        statusService.addStatusListener(
                status -> {
                    Platform.runLater(() -> updateUI(status));
                });
    }

    /** Updates UI based on automation status. */
    private void updateUI(
            io.github.jspinak.brobot.runner.ui.automation.models.AutomationStatus status) {
        // Update status label
        statusLabel.setText(status.getStateString());

        // Update status label style
        statusLabel.getStyleClass().removeAll(Styles.SUCCESS, Styles.WARNING, Styles.DANGER);
        switch (status.getStateString()) {
            case "RUNNING":
                statusLabel.getStyleClass().add(Styles.SUCCESS);
                break;
            case "PAUSED":
                statusLabel.getStyleClass().add(Styles.WARNING);
                break;
            case "ERROR":
                statusLabel.getStyleClass().add(Styles.DANGER);
                break;
        }

        // Update automation name
        if (status.isRunning()) {
            String automationText = "Running: " + status.getCurrentAutomationName();
            if (status.getCurrentAction() != null) {
                automationText = automationText + "\n" + status.getCurrentAction();
            }
            automationNameLabel.setText(automationText);
        } else {
            automationNameLabel.setText("No automation running");
        }

        // Update elapsed time
        elapsedTimeLabel.setText(formatElapsedTime(status.getElapsedTime()));

        // Update button states
        updateButtonStates(status);

        // Show error if present
        if (status.isHasError() && status.getErrorMessage() != null) {
            showError(status.getErrorMessage());
        }
    }

    /** Updates button enabled/disabled states based on status. */
    private void updateButtonStates(
            io.github.jspinak.brobot.runner.ui.automation.models.AutomationStatus status) {
        boolean isRunning = status.isRunning();
        boolean isPaused = status.isPaused();

        playButton.setDisable(isRunning);
        pauseResumeButton.setDisable(!isRunning);
        stopButton.setDisable(!isRunning);

        // Update pause/resume button state
        if (isPaused) {
            pauseResumeButton.setText("⏵ Resume");
            pauseResumeButton.getStyleClass().remove(Styles.WARNING);
            pauseResumeButton.getStyleClass().add(Styles.SUCCESS);
        } else if (isRunning) {
            pauseResumeButton.setText("⏸ Pause");
            pauseResumeButton.getStyleClass().remove(Styles.SUCCESS);
            pauseResumeButton.getStyleClass().add(Styles.WARNING);
        }
    }

    /** Handles play button click. */
    private void handlePlay() {
        log.debug("Play button clicked");
        // The actual automation execution is triggered from the main panel
        // This just updates the UI state
    }

    /** Handles pause/resume toggle. */
    private void handlePauseResume(boolean isPaused) {
        if (isPaused) {
            controlService.pauseAutomation();
        } else {
            controlService.resumeAutomation();
        }
    }

    /** Handles stop button click. */
    private void handleStop() {
        controlService.stopAllAutomations();
    }

    /** Sets the play button action handler. */
    public void setPlayAction(Runnable action) {
        playButton.setOnAction(e -> action.run());
    }

    /** Formats elapsed time in HH:MM:SS or MM:SS format. */
    private String formatElapsedTime(long elapsedMillis) {
        if (elapsedMillis <= 0) {
            return "00:00";
        }

        long seconds = elapsedMillis / 1000;
        long minutes = seconds / 60;
        long hours = minutes / 60;

        if (hours > 0) {
            return String.format("%02d:%02d:%02d", hours, minutes % 60, seconds % 60);
        } else {
            return String.format("%02d:%02d", minutes, seconds % 60);
        }
    }

    /** Shows an error message in the UI. */
    private void showError(String errorMessage) {
        Label errorLabel = new Label("Error: " + errorMessage);
        errorLabel.getStyleClass().addAll(Styles.TEXT_SMALL, Styles.DANGER);
        errorLabel.setWrapText(true);

        VBox content = (VBox) getChildren().get(0);
        // Remove any existing error labels
        content.getChildren()
                .removeIf(node -> node.getUserData() != null && node.getUserData().equals("error"));

        errorLabel.setUserData("error");
        content.getChildren().add(errorLabel);
    }

    /** Clears any error messages. */
    public void clearError() {
        VBox content = (VBox) getChildren().get(0);
        content.getChildren()
                .removeIf(node -> node.getUserData() != null && node.getUserData().equals("error"));
    }

    /** Enables or disables all controls. */
    public void setControlsEnabled(boolean enabled) {
        playButton.setDisable(!enabled || controlService.isRunning());
        pauseResumeButton.setDisable(!enabled || !controlService.isRunning());
        stopButton.setDisable(!enabled || !controlService.isRunning());
    }
}
