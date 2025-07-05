package io.github.jspinak.brobot.runner.ui;

import lombok.Getter;
import lombok.Setter;
import lombok.AccessLevel;

import io.github.jspinak.brobot.runner.automation.AutomationOrchestrator;
import io.github.jspinak.brobot.runner.config.BrobotRunnerProperties;
import io.github.jspinak.brobot.runner.events.EventBus;
import io.github.jspinak.brobot.runner.events.ExecutionStatusEvent;
import io.github.jspinak.brobot.runner.execution.ExecutionState;
import io.github.jspinak.brobot.runner.execution.ExecutionStatus;
import io.github.jspinak.brobot.runner.project.RunnerInterface;
import io.github.jspinak.brobot.runner.project.TaskButton;
import io.github.jspinak.brobot.runner.project.AutomationProject;
import io.github.jspinak.brobot.runner.project.AutomationProjectManager;
import javafx.application.Platform;
import javafx.geometry.Insets;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import javafx.scene.paint.Color;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.ApplicationContext;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;

/**
 * JavaFX UI component for automation control.
 */
@Getter
@Setter(AccessLevel.PRIVATE)
public class AutomationPanel extends VBox {
    private static AutomationPanel INSTANCE;

    private static final Logger logger = LoggerFactory.getLogger(AutomationPanel.class);
    private static final DateTimeFormatter TIME_FORMATTER = DateTimeFormatter.ofPattern("HH:mm:ss");

    private final ApplicationContext context;
    private final AutomationProjectManager projectManager;
    private final BrobotRunnerProperties runnerProperties;
    private final AutomationOrchestrator automationOrchestrator;
    private final EventBus eventBus;

    private final TextArea logArea;
    private final FlowPane buttonPane;
    private Label statusLabel;
    private ProgressBar progressBar;
    private javafx.scene.control.Button pauseResumeButton;
    private volatile boolean updateInProgress = false;

    /**
     * Constructor with Spring dependencies
     */
    public AutomationPanel(ApplicationContext context, AutomationProjectManager projectManager,
                           BrobotRunnerProperties runnerProperties, AutomationOrchestrator automationOrchestrator,
                           EventBus eventBus) {
        this.context = context;
        this.projectManager = projectManager;
        this.runnerProperties = runnerProperties;
        this.automationOrchestrator = automationOrchestrator;
        this.eventBus = eventBus;
        this.logArea = new TextArea();
        this.buttonPane = new FlowPane();

        setupBasicUI();
        setupLogArea();

        // Register for automation events
        if (automationOrchestrator != null) {
            automationOrchestrator.setLogCallback(this::log);

            // Start a UI update thread for status
            startStatusUpdateThread();
        }

        // Set the singleton instance
        INSTANCE = this;
    }

    private void setupBasicUI() {
        setPadding(new Insets(20));
        setSpacing(10);

        Label titleLabel = new Label("Automation Control");
        titleLabel.setStyle("-fx-font-size: 16; -fx-font-weight: bold;");

        javafx.scene.control.Button refreshButton = new javafx.scene.control.Button("Refresh Automation Buttons");
        refreshButton.setId("refreshAutomationButtons");
        refreshButton.setOnAction(e -> refreshAutomationButtons());

        javafx.scene.control.Button stopAllButton = new javafx.scene.control.Button("Stop All Automation");
        stopAllButton.setId("stopAllAutomation");
        stopAllButton.setOnAction(e -> stopAllAutomation());

        pauseResumeButton = new javafx.scene.control.Button("Pause Execution");
        pauseResumeButton.setId("pauseResumeExecution");
        pauseResumeButton.setOnAction(e -> togglePauseResume());
        pauseResumeButton.setDisable(true);

        HBox controlBar = new HBox(10, refreshButton, pauseResumeButton, stopAllButton);

        // Setup status area
        statusLabel = new Label("Status: Ready");
        statusLabel.setStyle("-fx-font-weight: bold;");

        progressBar = new ProgressBar(0);
        progressBar.setPrefWidth(Double.MAX_VALUE);

        VBox statusBox = new VBox(5, statusLabel, progressBar);
        statusBox.setPadding(new Insets(5));
        statusBox.setBorder(new Border(new BorderStroke(
                Color.LIGHTGRAY, BorderStrokeStyle.SOLID, new CornerRadii(5), BorderWidths.DEFAULT
        )));

        // Setup button pane
        buttonPane.setPadding(new Insets(10));
        buttonPane.setHgap(10);
        buttonPane.setVgap(10);
        buttonPane.setBorder(new Border(new BorderStroke(
                Color.LIGHTGRAY, BorderStrokeStyle.SOLID, new CornerRadii(5), BorderWidths.DEFAULT
        )));

        ScrollPane buttonScrollPane = new ScrollPane(buttonPane);
        buttonScrollPane.setFitToWidth(true);
        buttonScrollPane.setPrefHeight(200);

        getChildren().addAll(
                titleLabel,
                new Separator(),
                statusBox,
                controlBar,
                new Label("Available Automation Functions:"),
                buttonScrollPane,
                new Label("Automation Log:")
        );
    }

    private void setupLogArea() {
        logArea.setEditable(false);
        logArea.setWrapText(true);
        logArea.setPrefHeight(300);

        getChildren().add(logArea);

        log("Automation panel initialized. Load a configuration to see available automation functions.");
    }

    /**
     * Starts a background thread to update the UI with the latest status
     */
    private void startStatusUpdateThread() {
        Thread statusThread = new Thread(() -> {
            while (true) {
                try {
                    // Check execution status and update UI
                    if (automationOrchestrator != null) {
                        updateExecutionStatusUI();
                    }

                    // Sleep for a short interval
                    Thread.sleep(500);
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                    break;
                } catch (Exception e) {
                    logger.error("Error in status update thread", e);
                }
            }
        });
        statusThread.setDaemon(true);
        statusThread.setName("AutomationStatusUpdater");
        statusThread.start();
    }

    /**
     * Updates the UI elements with the current execution status
     */
    private void updateExecutionStatusUI() {
        if (updateInProgress) return;

        updateInProgress = true;
        try {
            ExecutionStatus status = automationOrchestrator.getExecutionStatus();
            if (status == null) return; // **FIX**: Add null check for status

            Platform.runLater(() -> {
                // Update status label
                statusLabel.setText("Status: " + status.getStatusMessage());

                // Update progress bar
                progressBar.setProgress(status.getProgress());

                // Update pause/resume button state
                updatePauseResumeButton(status);
            });
        } finally {
            updateInProgress = false;
        }
    }

    /**
     * Updates the pause/resume button based on execution state
     */
    private void updatePauseResumeButton(ExecutionStatus status) {
        ExecutionState state = status.getState();

        if (state == ExecutionState.RUNNING) {
            pauseResumeButton.setText("Pause Execution");
            pauseResumeButton.setDisable(false);
        } else if (state == ExecutionState.PAUSED) {
            pauseResumeButton.setText("Resume Execution");
            pauseResumeButton.setDisable(false);
        } else {
            pauseResumeButton.setText("Pause Execution");
            pauseResumeButton.setDisable(!state.isActive());
        }
    }

    /**
     * Toggles between pause and resume based on current state
     */
    private void togglePauseResume() {
        ExecutionState state = automationOrchestrator.getExecutionStatus().getState();

        if (state == ExecutionState.RUNNING) {
            pauseAutomation();
        } else if (state == ExecutionState.PAUSED) {
            resumeAutomation();
        }
    }

    /**
     * Refreshes the automation buttons based on the currently loaded project
     */
    public void refreshAutomationButtons() {
        buttonPane.getChildren().clear();

        if (projectManager == null || projectManager.getActiveProject() == null) {
            log("No project loaded. Please load a configuration first.");
            return;
        }

        AutomationProject project = projectManager.getActiveProject();
        if (project.getAutomation() == null || project.getAutomation().getButtons() == null) {
            log("No automation buttons defined in the current project.");
            return;
        }

        List<TaskButton> buttons = project.getAutomation().getButtons();
        if (buttons.isEmpty()) {
            log("No automation buttons defined in the current project.");
            return;
        }

        log("Found " + buttons.size() + " automation functions.");

        // Group buttons by category if available
        Map<String, List<TaskButton>> buttonsByCategory = new HashMap<>();

        for (TaskButton buttonDef : buttons) {
            String category = buttonDef.getCategory() != null ? buttonDef.getCategory() : "General";
            buttonsByCategory.computeIfAbsent(category, k -> new ArrayList<>()).add(buttonDef);
        }

        // Create button sections by category
        for (Map.Entry<String, List<TaskButton>> entry : buttonsByCategory.entrySet()) {
            VBox categoryBox = new VBox(5);
            categoryBox.setPadding(new Insets(5));
            categoryBox.setStyle("-fx-border-color: lightgray; -fx-border-radius: 5;");

            Label categoryLabel = new Label(entry.getKey());
            categoryLabel.setStyle("-fx-font-weight: bold;");
            categoryBox.getChildren().add(categoryLabel);

            for (TaskButton buttonDef : entry.getValue()) {
                javafx.scene.control.Button uiButton = createAutomationButton(buttonDef);
                categoryBox.getChildren().add(uiButton);
            }

            buttonPane.getChildren().add(categoryBox);
        }
    }

    /**
     * Creates a JavaFX button from a button definition
     */
    private javafx.scene.control.Button createAutomationButton(TaskButton buttonDef) {
        javafx.scene.control.Button uiButton = new javafx.scene.control.Button(buttonDef.getLabel());

        // Apply styling if defined
        if (buttonDef.getStyling() != null) {
            TaskButton.ButtonStyling styling = buttonDef.getStyling();
            StringBuilder styleString = new StringBuilder();

            if (styling.getBackgroundColor() != null) {
                styleString.append("-fx-background-color: ").append(styling.getBackgroundColor()).append("; ");
            }

            if (styling.getTextColor() != null) {
                styleString.append("-fx-text-fill: ").append(styling.getTextColor()).append("; ");
            }

            if (styling.getSize() != null) {
                switch (styling.getSize().toLowerCase()) {
                    case "small":
                        styleString.append("-fx-font-size: 10px; ");
                        break;
                    case "large":
                        styleString.append("-fx-font-size: 14px; ");
                        break;
                    default:
                        styleString.append("-fx-font-size: 12px; ");
                }
            }

            if (styling.getCustomClass() != null) {
                uiButton.getStyleClass().add(styling.getCustomClass());
            }

            uiButton.setStyle(styleString.toString());
        }

        // Set tooltip if defined
        if (buttonDef.getTooltip() != null) {
            uiButton.setTooltip(new Tooltip(buttonDef.getTooltip()));
        }

        // Set action
        uiButton.setOnAction(e -> runAutomation(buttonDef));

        return uiButton;
    }

    /**
     * Runs the automation function associated with the button
     */
    private void runAutomation(TaskButton buttonDef) {
        // Check if another automation is already running
        if (automationOrchestrator.getExecutionStatus().getState().isActive()) {
            log("Another automation task is already running. Please wait or stop it first.");
            return;
        }

        // Add confirmation dialog if required
        if (buttonDef.isConfirmationRequired()) {
            Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
            alert.setTitle("Confirm Automation");
            alert.setHeaderText("Run " + buttonDef.getLabel() + "?");
            alert.setContentText(buttonDef.getConfirmationMessage() != null ?
                    buttonDef.getConfirmationMessage() :
                    "Are you sure you want to run this automation?");

            Optional<ButtonType> result = alert.showAndWait();
            if (result.isEmpty() || result.get() != ButtonType.OK) {
                return;
            }
        }

        log("Starting automation: " + buttonDef.getLabel());
        eventBus.publish(ExecutionStatusEvent.started(this,
                automationOrchestrator.getExecutionStatus(),
                "Starting automation: " + buttonDef.getLabel()));
        automationOrchestrator.executeAutomation(buttonDef);
    }

    /**
     * Stops all running automation
     */
    private void stopAllAutomation() {
        if (automationOrchestrator.getExecutionStatus() != null && !automationOrchestrator.getExecutionStatus().getState().isActive()) {
            log("No automation is currently running.");
            return;
        }

        log("Stopping all automation...");
        eventBus.publish(ExecutionStatusEvent.stopped(this,
                automationOrchestrator.getExecutionStatus(),
                "Stopping all automation"));
        automationOrchestrator.stopAllAutomation();
    }

    /**
     * Pauses the current automation
     */
    private void pauseAutomation() {
        if (!automationOrchestrator.getExecutionStatus().getState().isActive() ||
                automationOrchestrator.getExecutionStatus().getState() == ExecutionState.PAUSED) {
            return;
        }

        log("Pausing automation...");
        automationOrchestrator.pauseAutomation();
    }

    /**
     * Resumes the paused automation
     */
    private void resumeAutomation() {
        if (automationOrchestrator.getExecutionStatus().getState() != ExecutionState.PAUSED) {
            return;
        }

        log("Resuming automation...");
        automationOrchestrator.resumeAutomation();
    }

    /**
     * Adds a log entry to the log area
     */
    public void log(String message) {
        String timestamp = LocalDateTime.now().format(TIME_FORMATTER);
        String logEntry = "[" + timestamp + "] " + message;

        Platform.runLater(() -> {
            logArea.appendText(logEntry + "\n");
            // Auto-scroll to bottom
            logArea.positionCaret(logArea.getText().length());
        });
    }

    /**
     * Gets the singleton instance of the AutomationPanel.
     */
    public static Optional<io.github.jspinak.brobot.runner.ui.AutomationPanel> getInstance() {
        return Optional.ofNullable((io.github.jspinak.brobot.runner.ui.AutomationPanel) INSTANCE);
    }

    /**
     * Sets the singleton instance of the AutomationPanel.
     */
    public static void setInstance(io.github.jspinak.brobot.runner.ui.AutomationPanel instance) {
        INSTANCE = instance;
    }

    /**
     * Updates the status message display.
     */
    public void setStatusMessage(String message) {
        Platform.runLater(() -> {
            statusLabel.setText("Status: " + message);
        });
    }

    /**
     * Updates the progress bar value.
     */
    public void setProgressValue(double value) {
        Platform.runLater(() -> {
            progressBar.setProgress(value);
        });
    }

    /**
     * Updates the pause/resume button state.
     */
    public void updatePauseResumeButton(boolean paused) {
        Platform.runLater(() -> {
            if (paused) {
                pauseResumeButton.setText("Resume Execution");
            } else {
                pauseResumeButton.setText("Pause Execution");
            }
        });
    }

    /**
     * Updates all button states based on whether automation is running.
     */
    public void updateButtonStates(boolean running) {
        Platform.runLater(() -> {
            // Update pause/resume button
            pauseResumeButton.setDisable(!running);
            pauseResumeButton.setText("Pause Execution");

            // If you have a refresh button
            javafx.scene.control.Button refreshButton = (javafx.scene.control.Button)
                    lookup("#refreshAutomationButtons");
            if (refreshButton != null) {
                refreshButton.setDisable(running);
            }

            // If you have a stop button
            javafx.scene.control.Button stopButton = (javafx.scene.control.Button)
                    lookup("#stopAllAutomation");
            if (stopButton != null) {
                stopButton.setDisable(!running);
            }

            // Disable all automation function buttons if running
            for (javafx.scene.Node node : buttonPane.getChildren()) {
                if (node instanceof VBox categoryBox) {
                    // This is a category box
                    for (javafx.scene.Node button : categoryBox.getChildren()) {
                        if (button instanceof javafx.scene.control.Button) {
                            // Skip category label and control buttons
                            if (button != refreshButton && button != stopButton && button != pauseResumeButton) {
                                button.setDisable(running);
                            }
                        }
                    }
                }
            }
        });
    }
}