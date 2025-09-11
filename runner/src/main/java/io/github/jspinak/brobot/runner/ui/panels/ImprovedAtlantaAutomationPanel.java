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
import javafx.scene.layout.*;

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
import io.github.jspinak.brobot.runner.ui.components.base.AtlantaBasePanel;
import io.github.jspinak.brobot.runner.ui.components.base.AtlantaCard;

import lombok.extern.slf4j.Slf4j;

/**
 * Improved automation panel with better spacing and layout. Extends AtlantaBasePanel for consistent
 * styling.
 */
@Slf4j
@Component
public class ImprovedAtlantaAutomationPanel extends AtlantaBasePanel {

    private static final DateTimeFormatter TIME_FORMATTER = DateTimeFormatter.ofPattern("HH:mm:ss");

    // Dependencies
    private final ApplicationContext context;
    private final AutomationProjectManager projectManager;
    private final BrobotRunnerProperties runnerProperties;
    private final AutomationOrchestrator automationOrchestrator;
    private final EventBus eventBus;
    private final HotkeyManager hotkeyManager;
    private final AutomationWindowController windowController;

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
    private CheckBox autoScrollCheck;

    // State tracking
    private final Map<String, Button> taskButtons = new ConcurrentHashMap<>();
    private volatile boolean isRunning = false;
    private volatile boolean isPaused = false;

    @Autowired
    public ImprovedAtlantaAutomationPanel(
            ApplicationContext context,
            AutomationProjectManager projectManager,
            BrobotRunnerProperties runnerProperties,
            AutomationOrchestrator automationOrchestrator,
            EventBus eventBus,
            HotkeyManager hotkeyManager,
            AutomationWindowController windowController) {

        super("Automation Control");

        this.context = context;
        this.projectManager = projectManager;
        this.runnerProperties = runnerProperties;
        this.automationOrchestrator = automationOrchestrator;
        this.eventBus = eventBus;
        this.hotkeyManager = hotkeyManager;
        this.windowController = windowController;

        getStyleClass().add("automation-panel");

        initialize();
    }

    private void initialize() {
        // Set up control bar
        setupControlBar();

        // Add main content
        addContent(createMainContent());

        // Load initial project
        refreshProjectTasks();

        // Register hotkeys
        registerHotkeys();
    }

    /** Sets up the control bar with improved button grouping. */
    private void setupControlBar() {
        // Project info
        projectLabel = new Label("No project loaded");
        projectLabel.getStyleClass().add("project-label");
        projectLabel.setMinWidth(200);

        HBox projectInfo = createButtonGroup(new Label("Project:"), projectLabel);

        // Execution controls
        startButton = new Button("Start");
        startButton.getStyleClass().addAll("button", "primary", "control-button");
        startButton.setOnAction(e -> startAutomation());

        pauseButton = new Button("Pause");
        pauseButton.getStyleClass().addAll("button", "secondary", "control-button");
        pauseButton.setOnAction(e -> togglePause());
        pauseButton.setDisable(true);

        stopButton = new Button("Stop");
        stopButton.getStyleClass().addAll("button", "danger", "control-button");
        stopButton.setOnAction(e -> stopAutomation());
        stopButton.setDisable(true);

        HBox executionControls = createButtonGroup(startButton, pauseButton, stopButton);

        // Window and hotkey controls
        windowControlButton = new Button("Window");
        windowControlButton.getStyleClass().addAll("button", "secondary", "control-button");
        windowControlButton.setOnAction(e -> showWindowControl());

        Button hotkeyButton = new Button("Hotkeys");
        hotkeyButton.getStyleClass().addAll("button", "secondary", "control-button");
        hotkeyButton.setOnAction(e -> showHotkeySettings());

        HBox windowControls = createButtonGroup(windowControlButton, hotkeyButton);

        // Status section
        VBox statusBox = new VBox(4);
        statusBox.setAlignment(Pos.CENTER_LEFT);

        statusLabel = new Label("Ready");
        statusLabel.getStyleClass().add("status-label");

        progressBar = new ProgressBar(0);
        progressBar.setPrefWidth(200);
        progressBar.getStyleClass().add("progress-bar");

        statusBox.getChildren().addAll(statusLabel, progressBar);

        // Add all groups to action bar with proper spacing
        addToActionBar(
                projectInfo,
                createSeparator(),
                executionControls,
                createSeparator(),
                windowControls,
                createSpacer(),
                statusBox);
    }

    /** Creates the main content area with improved responsive layout. */
    private Region createMainContent() {
        // Left: Task buttons
        AtlantaCard tasksCard = new AtlantaCard("Automation Tasks");

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
        HBox logControls = new HBox(12);
        logControls.setAlignment(Pos.CENTER_RIGHT);

        autoScrollCheck = new CheckBox("Auto-scroll");
        autoScrollCheck.setSelected(true);
        autoScrollCheck.getStyleClass().add("auto-scroll-check");

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

        // Use responsive split layout
        return createResponsiveSplitLayout(tasksCard, logCard);
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

                        // Show empty state
                        Label emptyLabel = new Label("No project loaded");
                        emptyLabel.getStyleClass().add("empty-state-title");

                        Label instructionLabel = new Label("Please configure a project first");
                        instructionLabel.getStyleClass().add("empty-state-text");

                        VBox emptyState = new VBox(8, emptyLabel, instructionLabel);
                        emptyState.setAlignment(Pos.CENTER);
                        emptyState.setPadding(new Insets(40));

                        taskButtonsPane.getChildren().add(emptyState);
                        logToUI("No project loaded. Please configure a project first.");
                        return;
                    }

                    projectLabel.setText(currentProject.getName());

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
                        categoryBox.setPadding(new Insets(8));

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
                        Label noTasksLabel = new Label("No tasks defined");
                        noTasksLabel.getStyleClass().add("empty-state-title");

                        Label helpLabel =
                                new Label("Add task buttons to your project configuration");
                        helpLabel.getStyleClass().add("empty-state-text");

                        VBox emptyState = new VBox(8, noTasksLabel, helpLabel);
                        emptyState.setAlignment(Pos.CENTER);
                        emptyState.setPadding(new Insets(40));

                        taskButtonsPane.getChildren().add(emptyState);
                    }

                    logToUI(
                            "Loaded project: "
                                    + currentProject.getName()
                                    + " with "
                                    + taskButtons.size()
                                    + " tasks");
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
            // Apply custom style classes if specified
            if (styling.getCustomClass() != null) {
                button.getStyleClass().add(styling.getCustomClass());
            }

            // For theme consistency, avoid inline styles
            // Colors should be handled via CSS classes
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
            logToUI("Cannot start task - automation already running");
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
                logToUI("Executing function: " + functionName);
                updateExecutionState(ExecutionState.RUNNING);

                // TODO: Implement actual task execution
                logToUI("Task execution started: " + functionName);

                // Simulate completion
                Platform.runLater(
                        () -> {
                            try {
                                Thread.sleep(1000);
                            } catch (InterruptedException e) {
                                Thread.currentThread().interrupt();
                            }
                            updateExecutionState(ExecutionState.STOPPED);
                            logToUI("Task completed: " + functionName);
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

        try {
            // TODO: Implement actual automation start
            logToUI("Automation started");
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
            pauseButton.setText("Resume");
            updateExecutionState(ExecutionState.PAUSED);
            automationOrchestrator.pauseAutomation();
        } else {
            logToUI("Resuming automation...");
            pauseButton.setText("Pause");
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
                            pauseButton.setText("Pause");
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
        logToUI("Opening window control...");
        // TODO: Implement window control dialog
    }

    /** Shows hotkey settings dialog. */
    private void showHotkeySettings() {
        logToUI("Opening hotkey settings...");
        // TODO: Implement hotkey settings dialog
    }

    /** Registers global hotkeys. */
    private void registerHotkeys() {
        // TODO: Implement hotkey registration when HotkeyManager API is available
        log.info("Hotkey registration pending implementation");
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
