package io.github.jspinak.brobot.runner.ui.panels;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import javafx.application.Platform;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.*;
import javafx.scene.image.ImageView;
import javafx.scene.layout.*;
import javafx.scene.paint.Color;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.stereotype.Component;

import io.github.jspinak.brobot.runner.automation.AutomationOrchestrator;
import io.github.jspinak.brobot.runner.config.BrobotRunnerProperties;
import io.github.jspinak.brobot.runner.events.EventBus;
import io.github.jspinak.brobot.runner.execution.ExecutionState;
import io.github.jspinak.brobot.runner.hotkeys.HotkeyManager;
import io.github.jspinak.brobot.runner.project.AutomationProject;
import io.github.jspinak.brobot.runner.project.AutomationProjectManager;
import io.github.jspinak.brobot.runner.project.RunnerInterface;
import io.github.jspinak.brobot.runner.project.TaskButton;
import io.github.jspinak.brobot.runner.ui.AutomationWindowController;
import io.github.jspinak.brobot.runner.ui.components.base.AtlantaCard;
import io.github.jspinak.brobot.runner.ui.icons.IconRegistry;

import lombok.extern.slf4j.Slf4j;

/**
 * Modern automation panel with AtlantaFX styling. Provides a clean, card-based interface for
 * running automations.
 */
@Slf4j
@Component
public class AtlantaAutomationPanel extends VBox {

    private static final DateTimeFormatter TIME_FORMATTER = DateTimeFormatter.ofPattern("HH:mm:ss");

    // Dependencies
    private final ApplicationContext context;
    private final AutomationProjectManager projectManager;
    private final BrobotRunnerProperties runnerProperties;
    private final AutomationOrchestrator automationOrchestrator;
    private final EventBus eventBus;
    private final HotkeyManager hotkeyManager;
    private final AutomationWindowController windowController;
    private final IconRegistry iconRegistry;

    // UI Components
    private TextArea logArea;
    private FlowPane taskButtonsPane;
    private Label statusLabel;
    private Label projectLabel;
    private ProgressBar progressBar;
    private Button startButton;
    private Button pauseButton;
    private Button stopButton;
    private Button windowControlButton;

    // State tracking
    private final Map<String, Button> taskButtons = new ConcurrentHashMap<>();
    private volatile boolean isRunning = false;
    private volatile boolean isPaused = false;

    @Autowired
    public AtlantaAutomationPanel(
            ApplicationContext context,
            AutomationProjectManager projectManager,
            BrobotRunnerProperties runnerProperties,
            AutomationOrchestrator automationOrchestrator,
            EventBus eventBus,
            HotkeyManager hotkeyManager,
            AutomationWindowController windowController,
            IconRegistry iconRegistry) {

        this.context = context;
        this.projectManager = projectManager;
        this.runnerProperties = runnerProperties;
        this.automationOrchestrator = automationOrchestrator;
        this.eventBus = eventBus;
        this.hotkeyManager = hotkeyManager;
        this.windowController = windowController;
        this.iconRegistry = iconRegistry;

        getStyleClass().add("automation-panel");

        initialize();
    }

    private void initialize() {
        // Create main content
        VBox mainContent = new VBox();
        mainContent.getChildren().addAll(createControlBar(), createMainContent());

        getChildren().add(mainContent);
        VBox.setVgrow(mainContent, Priority.ALWAYS);

        // Load initial project
        refreshProjectTasks();

        // Register hotkeys
        registerHotkeys();
    }

    /** Creates the control bar with execution controls. */
    private HBox createControlBar() {
        HBox controlBar = new HBox(8);
        controlBar.getStyleClass().add("action-bar");
        controlBar.setAlignment(Pos.CENTER_LEFT);

        // Project info
        projectLabel = new Label("No project loaded");
        projectLabel.getStyleClass().add("project-label");

        // Execution controls
        ImageView playIcon = iconRegistry.getIconView("play", 16);
        startButton = new Button("Start");
        if (playIcon != null) startButton.setGraphic(playIcon);
        startButton.getStyleClass().addAll("button", "primary");
        startButton.setOnAction(e -> startAutomation());

        ImageView pauseIcon = iconRegistry.getIconView("pause", 16);
        pauseButton = new Button("Pause");
        if (pauseIcon != null) pauseButton.setGraphic(pauseIcon);
        pauseButton.getStyleClass().addAll("button", "secondary");
        pauseButton.setOnAction(e -> togglePause());
        pauseButton.setDisable(true);

        ImageView stopIcon = iconRegistry.getIconView("stop", 16);
        stopButton = new Button("Stop");
        if (stopIcon != null) stopButton.setGraphic(stopIcon);
        stopButton.getStyleClass().addAll("button", "danger");
        stopButton.setOnAction(e -> stopAutomation());
        stopButton.setDisable(true);

        // Window control
        ImageView windowIcon = iconRegistry.getIconView("window", 16);
        windowControlButton = new Button("Window");
        if (windowIcon != null) windowControlButton.setGraphic(windowIcon);
        windowControlButton.getStyleClass().addAll("button", "secondary");
        windowControlButton.setOnAction(e -> showWindowControl());

        // Hotkey button
        ImageView settingsIcon = iconRegistry.getIconView("settings", 16);
        Button hotkeyButton = new Button("Hotkeys");
        if (settingsIcon != null) hotkeyButton.setGraphic(settingsIcon);
        hotkeyButton.getStyleClass().addAll("button", "secondary");
        hotkeyButton.setOnAction(e -> showHotkeySettings());

        Region spacer = new Region();
        HBox.setHgrow(spacer, Priority.ALWAYS);

        // Progress and status
        VBox statusBox = new VBox(4);
        statusBox.setAlignment(Pos.CENTER_RIGHT);

        statusLabel = new Label("Ready");
        statusLabel.getStyleClass().add("status-label");

        progressBar = new ProgressBar(0);
        progressBar.setPrefWidth(200);
        progressBar.getStyleClass().add("progress-bar");

        statusBox.getChildren().addAll(statusLabel, progressBar);

        controlBar
                .getChildren()
                .addAll(
                        projectLabel,
                        new Separator(javafx.geometry.Orientation.VERTICAL),
                        startButton,
                        pauseButton,
                        stopButton,
                        new Separator(javafx.geometry.Orientation.VERTICAL),
                        windowControlButton,
                        hotkeyButton,
                        spacer,
                        statusBox);

        return controlBar;
    }

    /** Creates the main content area with task buttons and log. */
    private HBox createMainContent() {
        HBox content = new HBox(24);
        content.getStyleClass().add("split-layout");

        // Left: Task buttons
        AtlantaCard tasksCard = new AtlantaCard("Automation Tasks");
        tasksCard.setMinWidth(400);
        tasksCard.setPrefWidth(500);

        ScrollPane taskScroll = new ScrollPane();
        taskScroll.setFitToWidth(true);
        taskScroll.setHbarPolicy(ScrollPane.ScrollBarPolicy.NEVER);

        taskButtonsPane = new FlowPane();
        taskButtonsPane.setHgap(12);
        taskButtonsPane.setVgap(12);
        taskButtonsPane.setPadding(new Insets(8));
        taskButtonsPane.getStyleClass().add("task-buttons-pane");

        taskScroll.setContent(taskButtonsPane);
        tasksCard.setContent(taskScroll);

        // Right: Execution log
        AtlantaCard logCard = new AtlantaCard("Execution Log");
        logCard.setExpand(true);

        VBox logContent = new VBox(8);

        // Log controls
        HBox logControls = new HBox(8);
        logControls.setAlignment(Pos.CENTER_RIGHT);

        CheckBox autoScrollCheck = new CheckBox("Auto-scroll");
        autoScrollCheck.setSelected(true);

        Button clearLogButton = new Button("Clear");
        clearLogButton.getStyleClass().addAll("button", "secondary", "small");
        clearLogButton.setOnAction(e -> logArea.clear());

        logControls.getChildren().addAll(autoScrollCheck, clearLogButton);

        // Log area
        logArea = new TextArea();
        logArea.setEditable(false);
        logArea.getStyleClass().add("log-area");
        logArea.setWrapText(true);
        VBox.setVgrow(logArea, Priority.ALWAYS);

        // Auto-scroll behavior
        logArea.textProperty()
                .addListener(
                        (obs, oldText, newText) -> {
                            if (autoScrollCheck.isSelected()) {
                                logArea.setScrollTop(Double.MAX_VALUE);
                            }
                        });

        logContent.getChildren().addAll(logControls, logArea);
        VBox.setVgrow(logArea, Priority.ALWAYS);

        logCard.setContent(logContent);

        content.getChildren().addAll(tasksCard, logCard);
        HBox.setHgrow(logCard, Priority.ALWAYS);

        return content;
    }

    /** Refreshes the task buttons from the current project. */
    private void refreshProjectTasks() {
        Platform.runLater(
                () -> {
                    taskButtonsPane.getChildren().clear();
                    taskButtons.clear();

                    AutomationProject currentProject = projectManager.getCurrentProject();
                    if (currentProject == null) {
                        projectLabel.setText("No project loaded");
                        logArea.appendText(
                                "No project loaded. Please configure a project first.\n");
                        return;
                    }

                    projectLabel.setText("Project: " + currentProject.getName());

                    // Group tasks by category
                    Map<String, List<TaskButton>> tasksByCategory = new HashMap<>();
                    RunnerInterface automation = currentProject.getAutomation();
                    if (automation != null && automation.getButtons() != null) {
                        for (TaskButton taskButton : automation.getButtons()) {
                            String category =
                                    taskButton.getCategory() != null
                                            ? taskButton.getCategory()
                                            : "General";
                            tasksByCategory
                                    .computeIfAbsent(category, k -> new ArrayList<>())
                                    .add(taskButton);
                        }
                    }

                    // Create UI for each category
                    for (Map.Entry<String, List<TaskButton>> entry : tasksByCategory.entrySet()) {
                        VBox categoryBox = new VBox(8);
                        categoryBox.getStyleClass().add("task-category");

                        Label categoryLabel = new Label(entry.getKey());
                        categoryLabel.getStyleClass().add("category-label");

                        FlowPane categoryButtons = new FlowPane();
                        categoryButtons.setHgap(8);
                        categoryButtons.setVgap(8);

                        for (TaskButton taskButton : entry.getValue()) {
                            Button button = createTaskButton(taskButton);
                            categoryButtons.getChildren().add(button);
                            String buttonKey =
                                    taskButton.getLabel() != null
                                            ? taskButton.getLabel()
                                            : taskButton.getId();
                            taskButtons.put(buttonKey, button);
                        }

                        categoryBox.getChildren().addAll(categoryLabel, categoryButtons);
                        taskButtonsPane.getChildren().add(categoryBox);
                    }

                    if (taskButtons.isEmpty()) {
                        Label noTasksLabel = new Label("No tasks defined in the current project");
                        noTasksLabel.getStyleClass().add("empty-state-title");
                        taskButtonsPane.getChildren().add(noTasksLabel);
                    }

                    logArea.appendText(
                            "Loaded project: "
                                    + currentProject.getName()
                                    + " with "
                                    + taskButtons.size()
                                    + " tasks\n");
                });
    }

    /** Creates a styled task button. */
    private Button createTaskButton(TaskButton taskButton) {
        String buttonText =
                taskButton.getLabel() != null ? taskButton.getLabel() : taskButton.getId();
        Button button = new Button(buttonText);
        button.getStyleClass().addAll("button", "task-button");

        // Apply styling if specified
        TaskButton.ButtonStyling styling = taskButton.getStyling();
        if (styling != null) {
            StringBuilder style = new StringBuilder();

            if (styling.getBackgroundColor() != null && !styling.getBackgroundColor().isEmpty()) {
                try {
                    Color color = Color.web(styling.getBackgroundColor());
                    String rgb =
                            String.format(
                                    "rgb(%d,%d,%d)",
                                    (int) (color.getRed() * 255),
                                    (int) (color.getGreen() * 255),
                                    (int) (color.getBlue() * 255));
                    style.append("-fx-background-color: ").append(rgb).append(";");
                } catch (Exception e) {
                    log.warn(
                            "Invalid background color for task button: {}",
                            styling.getBackgroundColor());
                }
            }

            if (styling.getTextColor() != null && !styling.getTextColor().isEmpty()) {
                style.append(" -fx-text-fill: ").append(styling.getTextColor()).append(";");
            }

            if (style.length() > 0) {
                button.setStyle(style.toString());
            }

            if (styling.getCustomClass() != null) {
                button.getStyleClass().add(styling.getCustomClass());
            }
        }

        // Set tooltip if available
        if (taskButton.getTooltip() != null && !taskButton.getTooltip().isEmpty()) {
            Tooltip tooltip = new Tooltip(taskButton.getTooltip());
            button.setTooltip(tooltip);
        }

        button.setOnAction(e -> executeTask(taskButton));

        return button;
    }

    /** Executes a task. */
    private void executeTask(TaskButton taskButton) {
        if (isRunning) {
            logArea.appendText("Cannot start task - automation already running\n");
            return;
        }

        String taskName =
                taskButton.getLabel() != null ? taskButton.getLabel() : taskButton.getId();
        logToUI("Executing task: " + taskName);

        // Check for confirmation
        if (taskButton.isConfirmationRequired()) {
            String message =
                    taskButton.getConfirmationMessage() != null
                            ? taskButton.getConfirmationMessage()
                            : "Are you sure you want to execute " + taskName + "?";

            Alert confirm = new Alert(Alert.AlertType.CONFIRMATION);
            confirm.setTitle("Confirm Execution");
            confirm.setHeaderText("Task Confirmation");
            confirm.setContentText(message);

            confirm.showAndWait()
                    .ifPresent(
                            response -> {
                                if (response == ButtonType.OK) {
                                    runTask(taskButton);
                                }
                            });
        } else {
            runTask(taskButton);
        }
    }

    /** Actually runs the task. */
    private void runTask(TaskButton taskButton) {
        String functionName = taskButton.getFunctionName();
        if (functionName != null && !functionName.isEmpty()) {
            try {
                // Execute using the function name
                // The automationOrchestrator would need to be updated to support this
                logToUI("Executing function: " + functionName);
                updateExecutionState(ExecutionState.RUNNING);

                // For now, just log that we would execute it
                logToUI(
                        "Would execute function: "
                                + functionName
                                + " with parameters: "
                                + taskButton.getParametersAsMap());

                // Reset state after a delay
                Platform.runLater(
                        () -> {
                            try {
                                Thread.sleep(1000);
                            } catch (InterruptedException e) {
                                Thread.currentThread().interrupt();
                            }
                            updateExecutionState(ExecutionState.STOPPED);
                        });
            } catch (Exception e) {
                logToUI("Error executing task: " + e.getMessage());
                log.error("Failed to execute task", e);
                updateExecutionState(ExecutionState.STOPPED);
            }
        } else {
            String taskName =
                    taskButton.getLabel() != null ? taskButton.getLabel() : taskButton.getId();
            logToUI("No function defined for task: " + taskName);
        }
    }

    /** Starts automation execution. */
    private void startAutomation() {
        if (isRunning) {
            return;
        }

        logToUI("Starting automation...");
        updateExecutionState(ExecutionState.RUNNING);

        // Start main automation
        try {
            // The automationOrchestrator would need a start method or we use execute
            logToUI("Starting main automation sequence");
            updateExecutionState(ExecutionState.RUNNING);

            // For now, just simulate starting
            Platform.runLater(
                    () -> {
                        try {
                            Thread.sleep(2000);
                        } catch (InterruptedException e) {
                            Thread.currentThread().interrupt();
                        }
                        updateExecutionState(ExecutionState.STOPPED);
                        logToUI("Automation completed");
                    });
        } catch (Exception e) {
            logToUI("Error starting automation: " + e.getMessage());
            updateExecutionState(ExecutionState.STOPPED);
        }
    }

    /** Toggles pause state. */
    private void togglePause() {
        if (!isRunning) {
            return;
        }

        isPaused = !isPaused;
        if (isPaused) {
            logToUI("Pausing automation...");
            pauseButton.setText("▶ Resume");
            updateExecutionState(ExecutionState.PAUSED);
            automationOrchestrator.pauseAutomation();
        } else {
            logToUI("Resuming automation...");
            pauseButton.setText("⏸ Pause");
            updateExecutionState(ExecutionState.RUNNING);
            automationOrchestrator.resumeAutomation();
        }
    }

    /** Stops automation execution. */
    private void stopAutomation() {
        if (!isRunning) {
            return;
        }

        logToUI("Stopping automation...");
        updateExecutionState(ExecutionState.STOPPED);
        automationOrchestrator.stopAllAutomation();
    }

    /** Updates the execution state and UI. */
    private void updateExecutionState(ExecutionState state) {
        Platform.runLater(
                () -> {
                    switch (state) {
                        case RUNNING:
                            isRunning = true;
                            isPaused = false;
                            startButton.setDisable(true);
                            pauseButton.setDisable(false);
                            stopButton.setDisable(false);
                            statusLabel.setText("Running");
                            statusLabel
                                    .getStyleClass()
                                    .removeAll("status-paused", "status-stopped");
                            statusLabel.getStyleClass().add("status-running");
                            break;

                        case PAUSED:
                            isPaused = true;
                            statusLabel.setText("Paused");
                            statusLabel
                                    .getStyleClass()
                                    .removeAll("status-running", "status-stopped");
                            statusLabel.getStyleClass().add("status-paused");
                            break;

                        case STOPPED:
                            isRunning = false;
                            isPaused = false;
                            startButton.setDisable(false);
                            pauseButton.setDisable(true);
                            stopButton.setDisable(true);
                            pauseButton.setText("⏸ Pause");
                            statusLabel.setText("Stopped");
                            statusLabel
                                    .getStyleClass()
                                    .removeAll("status-running", "status-paused");
                            statusLabel.getStyleClass().add("status-stopped");
                            progressBar.setProgress(0);
                            break;
                    }
                });
    }

    /** Shows window control dialog. */
    private void showWindowControl() {
        // Implementation would show window control options
        logToUI("Window control feature - coming soon");
    }

    /** Shows hotkey settings dialog. */
    private void showHotkeySettings() {
        // Implementation would show hotkey configuration
        logToUI("Hotkey settings - coming soon");
    }

    /** Registers global hotkeys. */
    private void registerHotkeys() {
        // TODO: Implement hotkey registration when HotkeyManager API is available
        // For now, hotkeys are disabled
    }

    /** Logs a message to the UI. */
    private void logToUI(String message) {
        Platform.runLater(
                () -> {
                    String timestamp = LocalDateTime.now().format(TIME_FORMATTER);
                    logArea.appendText(String.format("[%s] %s\n", timestamp, message));
                });
    }

    /** Updates the progress bar. */
    public void updateProgress(double progress) {
        Platform.runLater(() -> progressBar.setProgress(progress));
    }
}
