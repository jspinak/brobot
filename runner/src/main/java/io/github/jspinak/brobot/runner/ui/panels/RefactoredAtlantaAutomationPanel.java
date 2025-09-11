package io.github.jspinak.brobot.runner.ui.panels;

import java.util.List;
import java.util.Map;
import javafx.application.Platform;
import javafx.scene.control.*;
import javafx.scene.layout.*;

import jakarta.annotation.PostConstruct;
import jakarta.annotation.PreDestroy;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.stereotype.Component;

import io.github.jspinak.brobot.runner.automation.AutomationOrchestrator;
import io.github.jspinak.brobot.runner.config.BrobotRunnerProperties;
import io.github.jspinak.brobot.runner.events.EventBus;
import io.github.jspinak.brobot.runner.execution.ExecutionState;
import io.github.jspinak.brobot.runner.hotkeys.HotkeyManager;
import io.github.jspinak.brobot.runner.project.AutomationProjectManager;
import io.github.jspinak.brobot.runner.project.TaskButton;
import io.github.jspinak.brobot.runner.ui.AutomationWindowController;
import io.github.jspinak.brobot.runner.ui.automation.services.*;
import io.github.jspinak.brobot.runner.ui.components.base.AtlantaCard;
import io.github.jspinak.brobot.runner.ui.icons.IconRegistry;

import lombok.extern.slf4j.Slf4j;

/**
 * Refactored automation panel using service-oriented architecture. Acts as a thin orchestrator
 * between services.
 */
@Slf4j
@Component
public class RefactoredAtlantaAutomationPanel extends VBox {

    // Services
    private final AutomationExecutionService executionService;
    private final AutomationUIFactory uiFactory;
    private final TaskButtonService taskButtonService;
    private final AutomationLogService logService;

    // Dependencies
    private final ApplicationContext context;
    private final AutomationProjectManager projectManager;
    private final BrobotRunnerProperties runnerProperties;
    private final EventBus eventBus;
    private final HotkeyManager hotkeyManager;
    private final AutomationWindowController windowController;

    // UI Components
    private Label projectLabel;
    private AutomationUIFactory.StatusBox statusBox;
    private AutomationUIFactory.ExecutionControls executionControls;
    private FlowPane taskButtonsPane;
    private TextArea logArea;

    @Autowired
    public RefactoredAtlantaAutomationPanel(
            AutomationExecutionService executionService,
            AutomationUIFactory uiFactory,
            TaskButtonService taskButtonService,
            AutomationLogService logService,
            ApplicationContext context,
            AutomationProjectManager projectManager,
            BrobotRunnerProperties runnerProperties,
            AutomationOrchestrator automationOrchestrator,
            EventBus eventBus,
            HotkeyManager hotkeyManager,
            AutomationWindowController windowController,
            IconRegistry iconRegistry) {

        this.executionService = executionService;
        this.uiFactory = uiFactory;
        this.taskButtonService = taskButtonService;
        this.logService = logService;
        this.context = context;
        this.projectManager = projectManager;
        this.runnerProperties = runnerProperties;
        this.eventBus = eventBus;
        this.hotkeyManager = hotkeyManager;
        this.windowController = windowController;

        getStyleClass().add("automation-panel");
    }

    @PostConstruct
    public void initialize() {
        // Set up service listeners
        setupServiceListeners();

        // Create UI
        createUI();

        // Load initial project
        refreshProjectTasks();

        // Register hotkeys
        registerHotkeys();

        log.info("Refactored Atlanta Automation Panel initialized");
    }

    @PreDestroy
    public void cleanup() {
        logService.stop();
    }

    /** Sets up service listeners. */
    private void setupServiceListeners() {
        // Execution service listeners
        executionService.setStateListener(this::handleExecutionStateChange);
        executionService.setProgressListener(this::updateProgress);
        executionService.setLogListener(message -> logService.log(message));

        // Task button service listeners
        taskButtonService.setTaskLoadListener(this::handleTasksLoaded);
        taskButtonService.setTaskActionListener(this::executeTask);

        // Log service setup
        logService.setConfiguration(
                AutomationLogService.LogConfiguration.builder()
                        .timestampEnabled(true)
                        .batchingEnabled(true)
                        .maxLines(5000)
                        .build());
    }

    /** Creates the UI. */
    private void createUI() {
        // Create main components
        projectLabel = uiFactory.createProjectLabel();
        executionControls = uiFactory.createExecutionControls();
        statusBox = uiFactory.createStatusBox();

        // Window control buttons
        Button windowControlButton = uiFactory.createWindowControlButton();
        windowControlButton.setOnAction(e -> showWindowControl());

        Button hotkeyButton = uiFactory.createHotkeyButton();
        hotkeyButton.setOnAction(e -> showHotkeySettings());

        // Create control bar
        HBox controlBar =
                uiFactory.createControlBar(
                        projectLabel,
                        executionControls,
                        windowControlButton,
                        hotkeyButton,
                        statusBox.getContainer());

        // Set button actions
        executionControls.getStartButton().setOnAction(e -> startAutomation());
        executionControls.getPauseButton().setOnAction(e -> togglePause());
        executionControls.getStopButton().setOnAction(e -> stopAutomation());

        // Create task card
        AtlantaCard tasksCard = uiFactory.createTaskCard();
        ScrollPane taskScroll = new ScrollPane();
        taskScroll.setFitToWidth(true);
        taskScroll.setHbarPolicy(ScrollPane.ScrollBarPolicy.NEVER);

        taskButtonsPane = uiFactory.createTaskButtonsPane();
        taskScroll.setContent(taskButtonsPane);
        tasksCard.setContent(taskScroll);

        // Create log card
        AutomationUIFactory.LogCard logCard = uiFactory.createLogCard();
        logArea = logCard.getLogArea();
        logService.setLogTextArea(logArea);
        logService.setAutoScroll(logCard.getAutoScrollCheck().isSelected());

        logCard.getAutoScrollCheck()
                .selectedProperty()
                .addListener((obs, old, selected) -> logService.setAutoScroll(selected));

        // Create main content layout
        HBox mainContent = uiFactory.createMainContentLayout(tasksCard, logCard.getCard());

        // Add all to panel
        getChildren().addAll(controlBar, mainContent);
        VBox.setVgrow(mainContent, Priority.ALWAYS);
    }

    /** Refreshes project tasks. */
    private void refreshProjectTasks() {
        Platform.runLater(
                () -> {
                    TaskButtonService.ProjectTasks projectTasks =
                            taskButtonService.loadProjectTasks();

                    taskButtonsPane.getChildren().clear();

                    if (!projectTasks.hasProject()) {
                        projectLabel.setText("No project loaded");
                        Label emptyLabel =
                                uiFactory.createEmptyStateLabel(
                                        "No project loaded. Please configure a project first.");
                        taskButtonsPane.getChildren().add(emptyLabel);
                        return;
                    }

                    projectLabel.setText("Project: " + projectTasks.getProjectName());

                    // Create task buttons by category
                    Map<String, List<TaskButton>> tasksByCategory =
                            projectTasks.getTasksByCategory();
                    for (Map.Entry<String, List<TaskButton>> entry : tasksByCategory.entrySet()) {
                        VBox categoryBox = uiFactory.createTaskCategoryBox(entry.getKey());
                        FlowPane categoryButtons = (FlowPane) categoryBox.getChildren().get(1);

                        for (TaskButton taskButton : entry.getValue()) {
                            Button button =
                                    uiFactory.createTaskButton(
                                            taskButton,
                                            task -> taskButtonService.requestTaskExecution(task));
                            categoryButtons.getChildren().add(button);
                        }

                        taskButtonsPane.getChildren().add(categoryBox);
                    }

                    if (projectTasks.getTotalTasks() == 0) {
                        Label noTasksLabel =
                                uiFactory.createEmptyStateLabel(
                                        "No tasks defined in the current project");
                        taskButtonsPane.getChildren().add(noTasksLabel);
                    }
                });
    }

    /** Handles tasks loaded event. */
    private void handleTasksLoaded(String projectName, int taskCount) {
        String message =
                projectName != null
                        ? String.format("Loaded project: %s with %d tasks", projectName, taskCount)
                        : "No project loaded";
        logService.log(message);
    }

    /** Executes a task. */
    private void executeTask(TaskButton taskButton) {
        executionService.executeTask(taskButton);
    }

    /** Starts automation. */
    private void startAutomation() {
        executionService.startAutomation();
    }

    /** Toggles pause state. */
    private void togglePause() {
        if (executionService.isPaused()) {
            executionService.resumeAutomation();
        } else {
            executionService.pauseAutomation();
        }
    }

    /** Stops automation. */
    private void stopAutomation() {
        executionService.stopAutomation();
    }

    /** Handles execution state changes. */
    private void handleExecutionStateChange(ExecutionState state) {
        Platform.runLater(
                () -> {
                    switch (state) {
                        case RUNNING:
                        case STARTING:
                            executionControls.getStartButton().setDisable(true);
                            executionControls.getPauseButton().setDisable(false);
                            executionControls.getStopButton().setDisable(false);
                            statusBox.getStatusLabel().setText("Running");
                            updateStatusStyle("status-running");
                            break;

                        case PAUSED:
                            executionControls.getPauseButton().setText("Resume");
                            statusBox.getStatusLabel().setText("Paused");
                            updateStatusStyle("status-paused");
                            break;

                        case STOPPED:
                        case STOPPING:
                        case COMPLETED:
                        case FAILED:
                        case ERROR:
                        case TIMEOUT:
                        case IDLE:
                            executionControls.getStartButton().setDisable(false);
                            executionControls.getPauseButton().setDisable(true);
                            executionControls.getStopButton().setDisable(true);
                            executionControls.getPauseButton().setText("Pause");
                            statusBox.getStatusLabel().setText("Stopped");
                            updateStatusStyle("status-stopped");
                            statusBox.getProgressBar().setProgress(0);
                            break;
                    }
                });
    }

    /** Updates status label style. */
    private void updateStatusStyle(String newStyle) {
        Label statusLabel = statusBox.getStatusLabel();
        statusLabel.getStyleClass().removeAll("status-running", "status-paused", "status-stopped");
        statusLabel.getStyleClass().add(newStyle);
    }

    /** Updates progress. */
    private void updateProgress(double progress) {
        Platform.runLater(() -> statusBox.getProgressBar().setProgress(progress));
    }

    /** Shows window control dialog. */
    private void showWindowControl() {
        logService.log("Window control feature - coming soon");
    }

    /** Shows hotkey settings dialog. */
    private void showHotkeySettings() {
        logService.log("Hotkey settings - coming soon");
    }

    /** Registers hotkeys. */
    private void registerHotkeys() {
        // TODO: Implement when HotkeyManager API is available
        log.debug("Hotkey registration pending - API not available");
    }

    /** Reloads the current project tasks. */
    public void reloadProject() {
        refreshProjectTasks();
    }

    /** Gets execution statistics. */
    public String getStatistics() {
        AutomationLogService.LogStatistics logStats = logService.getStatistics();
        TaskButtonService.TaskStatistics taskStats = taskButtonService.getStatistics();

        return String.format(
                "Logs: %d (Errors: %d, Warnings: %d)\nTasks: %d in %d categories",
                logStats.getTotalLogs(),
                logStats.getErrors(),
                logStats.getWarnings(),
                taskStats.getTotalTasks(),
                taskStats.getTotalCategories());
    }
}
