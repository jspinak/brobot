package io.github.jspinak.brobot.runner.ui;

import io.github.jspinak.brobot.runner.automation.AutomationOrchestrator;
import io.github.jspinak.brobot.runner.config.BrobotRunnerProperties;
import io.github.jspinak.brobot.runner.events.EventBus;
import io.github.jspinak.brobot.runner.project.TaskButton;
import io.github.jspinak.brobot.runner.ui.automation.services.*;
import javafx.application.Platform;
import javafx.scene.layout.VBox;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.stereotype.Component;

import jakarta.annotation.PostConstruct;
import jakarta.annotation.PreDestroy;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Optional;

/**
 * Refactored JavaFX UI component for automation control using service-oriented architecture.
 * Acts as a thin orchestrator between specialized services.
 * 
 * @deprecated Use {@link io.github.jspinak.brobot.runner.ui.panels.UnifiedAutomationPanel} instead.
 *             This class will be removed in a future version.
 */
@Deprecated
@Slf4j
@Component
@Getter
public class RefactoredBasicAutomationPanel extends VBox {
    
    private static RefactoredBasicAutomationPanel INSTANCE;
    private static final DateTimeFormatter TIME_FORMATTER = DateTimeFormatter.ofPattern("HH:mm:ss");
    
    // Services
    private final AutomationButtonService buttonService;
    private final BasicAutomationControlService controlService;
    private final StatusMonitoringService statusService;
    private final BasicUIFactory uiFactory;
    
    // Dependencies (needed for compatibility)
    private final ApplicationContext context;
    private final BrobotRunnerProperties runnerProperties;
    private final EventBus eventBus;
    
    // UI Components
    private BasicUIFactory.AssembledUI assembledUI;
    
    @Autowired
    public RefactoredBasicAutomationPanel(
            AutomationButtonService buttonService,
            BasicAutomationControlService controlService,
            StatusMonitoringService statusService,
            BasicUIFactory uiFactory,
            ApplicationContext context,
            BrobotRunnerProperties runnerProperties,
            AutomationOrchestrator automationOrchestrator,
            EventBus eventBus) {
        
        this.buttonService = buttonService;
        this.controlService = controlService;
        this.statusService = statusService;
        this.uiFactory = uiFactory;
        this.context = context;
        this.runnerProperties = runnerProperties;
        this.eventBus = eventBus;
        
        // Set the singleton instance
        INSTANCE = this;
    }
    
    @PostConstruct
    public void initialize() {
        log.info("Initializing Refactored Basic Automation Panel");
        
        // Configure services
        configureServices();
        
        // Set up UI
        setupUI();
        
        // Set up automation callbacks
        if (controlService != null) {
            controlService.setLogHandler(this::log);
        }
        
        // Start status monitoring
        statusService.startMonitoring();
        
        log("Automation panel initialized. Load a configuration to see available automation functions.");
    }
    
    @PreDestroy
    public void cleanup() {
        log.info("Cleaning up Refactored Basic Automation Panel");
        statusService.stopMonitoring();
    }
    
    /**
     * Configures services.
     */
    private void configureServices() {
        // Configure button service
        buttonService.setConfiguration(
            AutomationButtonService.ButtonConfiguration.builder()
                .groupByCategory(true)
                .categorySpacing(5)
                .defaultCategory("General")
                .build()
        );
        
        buttonService.setButtonActionHandler(controlService::runAutomation);
        
        // Configure control service
        controlService.setConfiguration(
            BasicAutomationControlService.ControlConfiguration.builder()
                .confirmationEnabled(true)
                .publishEvents(true)
                .build()
        );
        
        controlService.setLogHandler(this::log);
        
        // Configure status monitoring
        statusService.setConfiguration(
            StatusMonitoringService.MonitoringConfiguration.builder()
                .updateIntervalMs(500)
                .threadName("AutomationStatusUpdater")
                .daemon(true)
                .build()
        );
        
        statusService.addStatusListener(this::updateUIFromStatus);
        
        // Configure UI factory
        uiFactory.setConfiguration(
            BasicUIFactory.UIConfiguration.builder()
                .panelPadding(20)
                .panelSpacing(10)
                .build()
        );
    }
    
    /**
     * Sets up the UI.
     */
    private void setupUI() {
        // Create main panel
        VBox mainPanel = uiFactory.createMainPanel();
        
        // Create components
        var titleLabel = uiFactory.createTitleLabel();
        var statusSection = uiFactory.createStatusSection();
        var controlBar = uiFactory.createControlBar();
        var buttonPane = uiFactory.createButtonPane();
        var logArea = uiFactory.createLogArea();
        
        // Set up control actions
        controlBar.getRefreshButton().setOnAction(e -> refreshAutomationButtons());
        controlBar.getPauseResumeButton().setOnAction(e -> controlService.togglePauseResume());
        controlBar.getStopAllButton().setOnAction(e -> controlService.stopAllAutomation());
        
        // Assemble UI
        assembledUI = uiFactory.assembleUI(
            mainPanel, titleLabel, statusSection, controlBar, buttonPane, logArea
        );
        
        // Add to this panel
        getChildren().add(mainPanel);
    }
    
    /**
     * Updates UI from status update.
     */
    private void updateUIFromStatus(StatusMonitoringService.StatusUpdate update) {
        assembledUI.getStatusSection().getStatusLabel().setText(update.getStatusMessage());
        assembledUI.getStatusSection().getProgressBar().setProgress(update.getProgress());
        
        var pauseResumeButton = assembledUI.getControlBar().getPauseResumeButton();
        pauseResumeButton.setText(update.getPauseResumeText());
        pauseResumeButton.setDisable(!update.isPauseResumeEnabled());
    }
    
    /**
     * Refreshes the automation buttons based on the currently loaded project.
     */
    public void refreshAutomationButtons() {
        AutomationButtonService.ButtonLoadResult result = buttonService.loadProjectButtons();
        
        log(result.getMessage());
        
        if (result.isSuccess()) {
            buttonService.populateButtonPane(
                assembledUI.getButtonPane().getButtonPane(),
                result.getButtons()
            );
        }
    }
    
    /**
     * Adds a log entry to the log area.
     */
    public void log(String message) {
        String timestamp = LocalDateTime.now().format(TIME_FORMATTER);
        String logEntry = "[" + timestamp + "] " + message;
        
        Platform.runLater(() -> {
            var logArea = assembledUI.getLogArea();
            logArea.appendText(logEntry + "\n");
            // Auto-scroll to bottom
            logArea.positionCaret(logArea.getText().length());
        });
    }
    
    /**
     * Updates the status message display.
     */
    public void setStatusMessage(String message) {
        Platform.runLater(() -> {
            assembledUI.getStatusSection().getStatusLabel().setText("Status: " + message);
        });
    }
    
    /**
     * Updates the progress bar value.
     */
    public void setProgressValue(double value) {
        Platform.runLater(() -> {
            assembledUI.getStatusSection().getProgressBar().setProgress(value);
        });
    }
    
    /**
     * Updates the pause/resume button state.
     */
    public void updatePauseResumeButton(boolean paused) {
        Platform.runLater(() -> {
            var button = assembledUI.getControlBar().getPauseResumeButton();
            button.setText(paused ? "Resume Execution" : "Pause Execution");
        });
    }
    
    /**
     * Updates all button states based on whether automation is running.
     */
    public void updateButtonStates(boolean running) {
        Platform.runLater(() -> {
            // Update control buttons
            assembledUI.getControlBar().getPauseResumeButton().setDisable(!running);
            assembledUI.getControlBar().getRefreshButton().setDisable(running);
            assembledUI.getControlBar().getStopAllButton().setDisable(!running);
            
            // Update automation buttons
            buttonService.updateButtonStates(
                assembledUI.getButtonPane().getButtonPane(),
                running
            );
        });
    }
    
    /**
     * Gets the singleton instance of the AutomationPanel.
     */
    public static Optional<RefactoredBasicAutomationPanel> getInstance() {
        return Optional.ofNullable(INSTANCE);
    }
    
    /**
     * Sets the singleton instance of the AutomationPanel.
     */
    public static void setInstance(RefactoredBasicAutomationPanel instance) {
        INSTANCE = instance;
    }
    
    /**
     * Gets current statistics.
     */
    public String getStatistics() {
        return String.format(
            "Status: %s\nProgress: %.1f%%",
            controlService.getStatusMessage(),
            controlService.getProgress() * 100
        );
    }
}