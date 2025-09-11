package io.github.jspinak.brobot.runner.ui.panels;

import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.concurrent.atomic.AtomicBoolean;
import javafx.application.Platform;
import javafx.scene.Node;
import javafx.scene.layout.FlowPane;
import javafx.scene.layout.VBox;

import jakarta.annotation.PostConstruct;
import jakarta.annotation.PreDestroy;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.stereotype.Component;

import io.github.jspinak.brobot.runner.automation.AutomationOrchestrator;
import io.github.jspinak.brobot.runner.config.BrobotRunnerProperties;
import io.github.jspinak.brobot.runner.events.EventBus;
import io.github.jspinak.brobot.runner.execution.ExecutionState;
import io.github.jspinak.brobot.runner.execution.ExecutionStatus;
import io.github.jspinak.brobot.runner.hotkeys.HotkeyManager;
import io.github.jspinak.brobot.runner.project.AutomationProject;
import io.github.jspinak.brobot.runner.project.AutomationProjectManager;
import io.github.jspinak.brobot.runner.project.TaskButton;
import io.github.jspinak.brobot.runner.ui.AutomationWindowController;
import io.github.jspinak.brobot.runner.ui.automation.services.*;

import lombok.extern.slf4j.Slf4j;

/**
 * Refactored unified automation panel using service-oriented architecture. Acts as a thin
 * orchestrator between specialized services.
 */
@Slf4j
@Component
public class RefactoredUnifiedAutomationPanel extends VBox {

    private static final DateTimeFormatter TIME_FORMATTER = DateTimeFormatter.ofPattern("HH:mm:ss");

    // Services
    private final RunnerStatusService statusService;
    private final ButtonCategoryService categoryService;
    private final HotkeyIntegrationService hotkeyService;
    private final UnifiedUIFactory uiFactory;
    private final AutomationLogService logService;

    // Dependencies
    private final ApplicationContext context;
    private final AutomationProjectManager projectManager;
    private final BrobotRunnerProperties runnerProperties;
    private final AutomationOrchestrator automationOrchestrator;
    private final EventBus eventBus;
    private final HotkeyManager hotkeyManager;
    private final AutomationWindowController windowController;

    // UI Components
    private UnifiedUIFactory.AssembledPanel assembledPanel;
    private final AtomicBoolean updateInProgress = new AtomicBoolean(false);

    @Autowired
    public RefactoredUnifiedAutomationPanel(
            RunnerStatusService statusService,
            ButtonCategoryService categoryService,
            HotkeyIntegrationService hotkeyService,
            UnifiedUIFactory uiFactory,
            AutomationLogService logService,
            ApplicationContext context,
            AutomationProjectManager projectManager,
            BrobotRunnerProperties runnerProperties,
            AutomationOrchestrator automationOrchestrator,
            EventBus eventBus,
            HotkeyManager hotkeyManager,
            AutomationWindowController windowController) {

        this.statusService = statusService;
        this.categoryService = categoryService;
        this.hotkeyService = hotkeyService;
        this.uiFactory = uiFactory;
        this.logService = logService;
        this.context = context;
        this.projectManager = projectManager;
        this.runnerProperties = runnerProperties;
        this.automationOrchestrator = automationOrchestrator;
        this.eventBus = eventBus;
        this.hotkeyManager = hotkeyManager;
        this.windowController = windowController;
    }

    @PostConstruct
    public void initialize() {
        log.info("Initializing Refactored Unified Automation Panel");

        // Set up services
        setupServices();

        // Create UI
        createUI();

        // Set up automation callbacks
        if (automationOrchestrator != null) {
            automationOrchestrator.setLogCallback(this::log);
        }

        // Start status monitoring
        statusService.startMonitoring();

        // Initial button refresh
        refreshAutomationButtons();
    }

    @PreDestroy
    public void cleanup() {
        log.info("Cleaning up Refactored Unified Automation Panel");
        statusService.stopMonitoring();
        hotkeyService.stopListening();
        logService.stop();
    }

    /** Sets up service configurations and listeners. */
    private void setupServices() {
        // Configure status service
        statusService.setConfiguration(
                RunnerStatusService.StatusConfiguration.builder()
                        .updateIntervalMs(500)
                        .notifyOnlyOnChange(true)
                        .build());

        statusService.addStatusListener(this::updateExecutionStatus);
        statusService.addStateChangeListener(this::handleStateChange);

        // Configure category service
        categoryService.setConfiguration(
                ButtonCategoryService.CategoryConfiguration.builder()
                        .defaultCategory("General")
                        .sortCategories(true)
                        .sortButtonsInCategory(true)
                        .build());

        categoryService.setButtonActionHandler(this::runAutomation);

        // Configure hotkey service
        hotkeyService.registerAutomationActions(
                this::pauseAutomation,
                this::resumeAutomation,
                this::stopAllAutomation,
                this::togglePauseResume);

        // Configure log service
        logService.setConfiguration(
                AutomationLogService.LogConfiguration.builder()
                        .timestampEnabled(true)
                        .batchingEnabled(true)
                        .maxLines(5000)
                        .build());
    }

    /** Creates the UI components. */
    private void createUI() {
        // Create individual components
        var titleLabel = uiFactory.createTitleLabel();
        var statusPanel = uiFactory.createEnhancedStatusPanel(hotkeyManager);
        var progressSection = uiFactory.createProgressSection();
        var controlBar = uiFactory.createControlBar();
        var buttonPane = uiFactory.createButtonPane();
        var logArea = uiFactory.createLogArea();

        // Set up control bar actions
        controlBar.getRefreshButton().setOnAction(e -> refreshAutomationButtons());
        controlBar.getPauseResumeButton().setOnAction(e -> togglePauseResume());
        controlBar.getStopAllButton().setOnAction(e -> stopAllAutomation());
        controlBar
                .getConfigureHotkeysButton()
                .setOnAction(e -> hotkeyService.showConfigurationDialog());
        controlBar.getAutoMinimizeCheckBox().setSelected(windowController.isAutoMinimizeEnabled());
        controlBar
                .getAutoMinimizeCheckBox()
                .selectedProperty()
                .addListener((obs, old, val) -> windowController.setAutoMinimizeEnabled(val));

        // Set log service text area
        logService.setLogTextArea(logArea);

        // Assemble the panel
        assembledPanel =
                uiFactory.assembleMainPanel(
                        titleLabel, statusPanel, progressSection, controlBar, buttonPane, logArea);

        // Add to this panel
        getChildren().add(assembledPanel.getMainContainer());
    }

    /** Refreshes automation buttons from the current project. */
    public void refreshAutomationButtons() {
        if (updateInProgress.compareAndSet(false, true)) {
            try {
                Platform.runLater(
                        () -> {
                            try {
                                List<TaskButton> buttons = loadProjectButtons();
                                updateButtonPane(buttons);
                            } finally {
                                updateInProgress.set(false);
                            }
                        });
            } catch (Exception e) {
                log.error("Error refreshing automation buttons", e);
                updateInProgress.set(false);
            }
        }
    }

    /** Loads buttons from the current project. */
    private List<TaskButton> loadProjectButtons() {
        if (projectManager == null || projectManager.getCurrentProject() == null) {
            log("No project loaded. Please load a configuration first.");
            return List.of();
        }

        AutomationProject project = projectManager.getCurrentProject();
        if (project.getAutomation() == null || project.getAutomation().getButtons() == null) {
            log("No automation buttons defined in the current project.");
            return List.of();
        }

        List<TaskButton> buttons = project.getAutomation().getButtons();
        log("Found " + buttons.size() + " automation functions.");
        return buttons;
    }

    /** Updates the button pane with new buttons. */
    private void updateButtonPane(List<TaskButton> buttons) {
        FlowPane flowPane = assembledPanel.getButtonPane().getFlowPane();
        ButtonCategoryService.CategoryUpdate update = categoryService.updateCategories(buttons);

        if (update.hasChanges()) {
            // Remove old nodes
            flowPane.getChildren().removeAll(update.getRemovedNodes());

            // Add new nodes
            flowPane.getChildren().addAll(update.getAddedNodes());

            // Reorder if needed
            if (!update.getOrderedCategories().isEmpty()) {
                flowPane.getChildren().clear();
                for (Node node : categoryService.getAllRenderedCategories()) {
                    flowPane.getChildren().add(node);
                }
            }
        }
    }

    /** Runs automation for a task button. */
    private void runAutomation(TaskButton buttonDef) {
        log("Starting automation: " + buttonDef.getLabel());
        automationOrchestrator.executeAutomation(buttonDef);

        // Auto-minimize if enabled
        if (windowController.isAutoMinimizeEnabled()) {
            windowController.minimizeForAutomation();
        }
    }

    /** Pauses automation. */
    private void pauseAutomation() {
        log.info("Pausing automation");
        automationOrchestrator.pauseAutomation();
        log("Automation paused");
    }

    /** Resumes automation. */
    private void resumeAutomation() {
        log.info("Resuming automation");
        automationOrchestrator.resumeAutomation();
        log("Automation resumed");
    }

    /** Stops all automation. */
    private void stopAllAutomation() {
        log.info("Stopping all automation");
        automationOrchestrator.stopAllAutomation();
        windowController.restoreAfterAutomation();
        log("All automation stopped");
    }

    /** Toggles pause/resume state. */
    private void togglePauseResume() {
        if (statusService.isPaused()) {
            resumeAutomation();
        } else if (statusService.isRunning()) {
            pauseAutomation();
        }
    }

    /** Updates execution status in UI. */
    private void updateExecutionStatus(ExecutionStatus status) {
        Platform.runLater(
                () -> {
                    // Update status label
                    var statusLabel = assembledPanel.getProgressSection().getStatusLabel();
                    statusLabel.setText("Status: " + status.getState().getDescription());

                    // Update progress bar
                    var progressBar = assembledPanel.getProgressSection().getProgressBar();
                    progressBar.setProgress(status.getProgress());

                    // Update enhanced status panel
                    assembledPanel.getStatusPanel().updateStatus(status);

                    // Update pause/resume button
                    updatePauseResumeButton(status);
                });
    }

    /** Handles state changes. */
    private void handleStateChange(ExecutionState oldState, ExecutionState newState) {
        // Restore window if automation completed
        if (newState == ExecutionState.COMPLETED
                || newState == ExecutionState.STOPPED
                || newState == ExecutionState.ERROR) {
            windowController.restoreAfterAutomation();
        }
    }

    /** Updates pause/resume button state. */
    private void updatePauseResumeButton(ExecutionStatus status) {
        var button = assembledPanel.getControlBar().getPauseResumeButton();
        ExecutionState state = status.getState();

        if (state == ExecutionState.RUNNING) {
            button.setText("Pause");
            button.setDisable(false);
        } else if (state == ExecutionState.PAUSED) {
            button.setText("Resume");
            button.setDisable(false);
        } else {
            button.setText("Pause");
            button.setDisable(!state.isActive());
        }
    }

    /** Logs a message. */
    public void log(String message) {
        logService.log(message);
    }

    /** Gets current statistics. */
    public String getStatistics() {
        return String.format(
                "Status: %s\nLog entries: %d",
                statusService.getStatusSummary(), logService.getStatistics().getTotalLogs());
    }
}
