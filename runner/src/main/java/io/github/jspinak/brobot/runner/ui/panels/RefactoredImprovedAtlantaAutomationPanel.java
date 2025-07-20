package io.github.jspinak.brobot.runner.ui.panels;

import io.github.jspinak.brobot.runner.config.BrobotRunnerProperties;
import io.github.jspinak.brobot.runner.events.EventBus;
import io.github.jspinak.brobot.runner.execution.ExecutionState;
import io.github.jspinak.brobot.runner.hotkeys.HotkeyManager;
import io.github.jspinak.brobot.runner.project.TaskButton;
import io.github.jspinak.brobot.runner.ui.AutomationWindowController;
import io.github.jspinak.brobot.runner.ui.automation.services.*;
import io.github.jspinak.brobot.runner.ui.components.base.AtlantaBasePanel;
import javafx.application.Platform;
import javafx.scene.Node;
import javafx.scene.layout.Region;
import javafx.scene.control.Alert;
import javafx.scene.control.SplitPane;
import javafx.scene.layout.FlowPane;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.stereotype.Component;

import jakarta.annotation.PostConstruct;
import jakarta.annotation.PreDestroy;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;

/**
 * Refactored improved automation panel using service-oriented architecture.
 * Acts as a thin orchestrator between specialized services.
 */
@Slf4j
@Component
public class RefactoredImprovedAtlantaAutomationPanel extends AtlantaBasePanel {
    
    private static final DateTimeFormatter TIME_FORMATTER = DateTimeFormatter.ofPattern("HH:mm:ss");
    
    // Services
    private final TaskManagementService taskService;
    private final ImprovedExecutionService executionService;
    private final ImprovedUILayoutFactory uiFactory;
    
    // Dependencies
    private final ApplicationContext context;
    private final BrobotRunnerProperties runnerProperties;
    private final EventBus eventBus;
    private final HotkeyManager hotkeyManager;
    private final AutomationWindowController windowController;
    
    // UI Components
    private ImprovedUILayoutFactory.ProjectInfoSection projectInfo;
    private ImprovedUILayoutFactory.ExecutionControls executionControls;
    private ImprovedUILayoutFactory.WindowControls windowControls;
    private ImprovedUILayoutFactory.StatusSection statusSection;
    private ImprovedUILayoutFactory.TaskPanel taskPanel;
    private ImprovedUILayoutFactory.LogPanel logPanel;
    
    @Autowired
    public RefactoredImprovedAtlantaAutomationPanel(
            TaskManagementService taskService,
            ImprovedExecutionService executionService,
            ImprovedUILayoutFactory uiFactory,
            ApplicationContext context,
            BrobotRunnerProperties runnerProperties,
            EventBus eventBus,
            HotkeyManager hotkeyManager,
            AutomationWindowController windowController) {
        
        super("Automation Control");
        
        this.taskService = taskService;
        this.executionService = executionService;
        this.uiFactory = uiFactory;
        this.context = context;
        this.runnerProperties = runnerProperties;
        this.eventBus = eventBus;
        this.hotkeyManager = hotkeyManager;
        this.windowController = windowController;
        
        getStyleClass().add("automation-panel");
    }
    
    @PostConstruct
    public void initialize() {
        log.info("Initializing Refactored Improved Atlanta Automation Panel");
        
        // Configure services
        configureServices();
        
        // Set up UI
        setupControlBar();
        addContent(createMainContent());
        
        // Load initial project
        refreshProjectTasks();
        
        // Register hotkeys
        registerHotkeys();
    }
    
    @PreDestroy
    public void cleanup() {
        log.info("Cleaning up Refactored Improved Atlanta Automation Panel");
        // Cleanup if needed
    }
    
    /**
     * Configures services.
     */
    private void configureServices() {
        // Configure task service
        taskService.setConfiguration(
            TaskManagementService.TaskConfiguration.builder()
                .defaultCategory("General")
                .groupByCategory(true)
                .categorySpacing(8)
                .buttonSpacing(8)
                .build()
        );
        
        taskService.setTaskExecutionHandler(this::executeTask);
        
        // Configure execution service
        executionService.setConfiguration(
            ImprovedExecutionService.ExecutionConfiguration.builder()
                .confirmationEnabled(true)
                .autoLogExecution(true)
                .build()
        );
        
        executionService.setLogHandler(this::logToUI);
        executionService.addStateListener(this::handleStateChange);
        
        // Configure UI factory
        uiFactory.setConfiguration(
            ImprovedUILayoutFactory.LayoutConfiguration.builder()
                .splitPanePosition(0.4)
                .controlBarSpacing(10)
                .build()
        );
    }
    
    /**
     * Sets up the control bar.
     */
    private void setupControlBar() {
        // Create sections
        projectInfo = uiFactory.createProjectInfo();
        executionControls = uiFactory.createExecutionControls();
        windowControls = uiFactory.createWindowControls();
        statusSection = uiFactory.createStatusSection();
        
        // Set up actions
        executionControls.getStartButton().setOnAction(e -> startAutomation());
        executionControls.getPauseButton().setOnAction(e -> executionService.togglePause());
        executionControls.getStopButton().setOnAction(e -> executionService.stopAutomation());
        
        windowControls.getWindowControlButton().setOnAction(e -> showWindowControl());
        windowControls.getHotkeyButton().setOnAction(e -> showHotkeySettings());
        
        // Add to action bar
        addToActionBar(
            projectInfo.getContainer(),
            uiFactory.createSeparator(),
            executionControls.getContainer(),
            uiFactory.createSeparator(),
            windowControls.getContainer(),
            uiFactory.createSpacer(),
            statusSection.getContainer()
        );
    }
    
    /**
     * Creates the main content area.
     */
    private Region createMainContent() {
        // Create panels
        taskPanel = uiFactory.createTaskPanel();
        logPanel = uiFactory.createLogPanel();
        
        // Set up log panel actions
        logPanel.getClearButton().setOnAction(e -> logPanel.getLogArea().clear());
        
        // Create split layout
        return uiFactory.createResponsiveSplitLayout(
            taskPanel.getCard(),
            logPanel.getCard()
        );
    }
    
    /**
     * Refreshes project tasks.
     */
    private void refreshProjectTasks() {
        Platform.runLater(() -> {
            // Load tasks
            TaskManagementService.ProjectTasksResult result = taskService.loadProjectTasks();
            
            // Update project label
            projectInfo.getProjectLabel().setText(result.getProjectName() != null ? 
                result.getProjectName() : "No project loaded");
            
            // Clear current task display
            FlowPane taskPane = taskPanel.getTaskButtonsPane();
            taskPane.getChildren().clear();
            
            // Create task UI
            if (!result.hasProject()) {
                Node emptyState = taskService.createEmptyState(
                    "No project loaded",
                    "Please configure a project first"
                );
                taskPane.getChildren().add(emptyState);
                logToUI("No project loaded. Please configure a project first.");
            } else if (!result.hasTasks()) {
                Node emptyState = taskService.createEmptyState();
                taskPane.getChildren().add(emptyState);
                logToUI("Loaded project: " + result.getProjectName() + " (no tasks defined)");
            } else {
                List<Node> taskNodes = taskService.createTaskUI(result.getTasks());
                taskPane.getChildren().addAll(taskNodes);
                logToUI("Loaded project: " + result.getProjectName() + 
                       " with " + taskService.getTaskCount() + " tasks");
            }
        });
    }
    
    /**
     * Executes a task.
     */
    private void executeTask(TaskButton taskButton) {
        executionService.executeTask(taskButton);
    }
    
    /**
     * Starts automation.
     */
    private void startAutomation() {
        executionService.startAutomation();
    }
    
    /**
     * Handles state changes.
     */
    private void handleStateChange(ExecutionState state, boolean isRunning, boolean isPaused) {
        Platform.runLater(() -> {
            // Update buttons
            executionControls.getStartButton().setDisable(isRunning);
            executionControls.getPauseButton().setDisable(!isRunning);
            executionControls.getStopButton().setDisable(!isRunning);
            
            // Update pause button text
            executionControls.getPauseButton().setText(isPaused ? "Resume" : "Pause");
            
            // Update status
            String statusText = getStatusText(state);
            statusSection.getStatusLabel().setText(statusText);
            
            // Update status style
            statusSection.getStatusLabel().getStyleClass().removeAll(
                "status-running", "status-paused", "status-stopped"
            );
            statusSection.getStatusLabel().getStyleClass().add(getStatusStyle(state));
            
            // Update progress
            if (!isRunning) {
                statusSection.getProgressBar().setProgress(0);
            }
        });
    }
    
    /**
     * Gets status text for state.
     */
    private String getStatusText(ExecutionState state) {
        switch (state) {
            case RUNNING: return "Running";
            case PAUSED: return "Paused";
            case STOPPED: return "Stopped";
            case IDLE: return "Ready";
            case COMPLETED: return "Completed";
            case FAILED: return "Failed";
            case ERROR: return "Error";
            default: return state.toString();
        }
    }
    
    /**
     * Gets status style class for state.
     */
    private String getStatusStyle(ExecutionState state) {
        switch (state) {
            case RUNNING:
            case STARTING:
                return "status-running";
            case PAUSED:
                return "status-paused";
            case STOPPED:
            case IDLE:
            case COMPLETED:
            case FAILED:
            case ERROR:
            default:
                return "status-stopped";
        }
    }
    
    /**
     * Shows window control dialog.
     */
    private void showWindowControl() {
        logToUI("Opening window control...");
        // TODO: Implement window control dialog
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle("Window Control");
        alert.setHeaderText("Window Control");
        alert.setContentText("Window control functionality coming soon.");
        alert.showAndWait();
    }
    
    /**
     * Shows hotkey settings dialog.
     */
    private void showHotkeySettings() {
        logToUI("Opening hotkey settings...");
        // TODO: Implement hotkey settings dialog
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle("Hotkey Settings");
        alert.setHeaderText("Keyboard Shortcuts");
        alert.setContentText("Hotkey configuration coming soon.");
        alert.showAndWait();
    }
    
    /**
     * Registers hotkeys.
     */
    private void registerHotkeys() {
        // TODO: Implement hotkey registration when HotkeyManager API is available
        log.info("Hotkey registration pending implementation");
    }
    
    /**
     * Logs a message to the UI.
     */
    private void logToUI(String message) {
        Platform.runLater(() -> {
            String timestamp = LocalDateTime.now().format(TIME_FORMATTER);
            logPanel.getLogArea().appendText(String.format("[%s] %s\n", timestamp, message));
        });
    }
    
    /**
     * Updates the progress bar.
     */
    public void updateProgress(double progress) {
        Platform.runLater(() -> statusSection.getProgressBar().setProgress(progress));
    }
    
    /**
     * Gets current statistics.
     */
    public String getStatistics() {
        ImprovedExecutionService.ExecutionStateSummary summary = executionService.getStateSummary();
        return String.format(
            "Status: %s\nProject: %s\nTasks: %d",
            summary.getStatusText(),
            taskService.getCurrentProjectName(),
            taskService.getTaskCount()
        );
    }
}