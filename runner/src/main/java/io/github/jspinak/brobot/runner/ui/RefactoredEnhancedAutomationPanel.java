package io.github.jspinak.brobot.runner.ui;

import io.github.jspinak.brobot.runner.automation.AutomationOrchestrator;
import io.github.jspinak.brobot.runner.config.BrobotRunnerProperties;
import io.github.jspinak.brobot.runner.events.EventBus;
import io.github.jspinak.brobot.runner.hotkeys.HotkeyManager;
import io.github.jspinak.brobot.runner.project.AutomationProjectManager;
import io.github.jspinak.brobot.runner.project.TaskButton;
import io.github.jspinak.brobot.runner.ui.enhanced.services.*;
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
 * Refactored enhanced JavaFX UI component for automation control with hotkey support.
 * Uses service-oriented architecture to separate concerns.
 * 
 * @deprecated Use {@link io.github.jspinak.brobot.runner.ui.panels.UnifiedAutomationPanel} instead.
 *             This class will be removed in a future version.
 */
@Deprecated
@Slf4j
@Component
@Getter
public class RefactoredEnhancedAutomationPanel extends VBox {
    
    private static RefactoredEnhancedAutomationPanel INSTANCE;
    private static final DateTimeFormatter TIME_FORMATTER = DateTimeFormatter.ofPattern("HH:mm:ss");
    
    // Services
    private final EnhancedHotkeyService hotkeyService;
    private final EnhancedWindowService windowService;
    private final EnhancedStatusMonitoringService statusService;
    private final EnhancedButtonService buttonService;
    private final EnhancedExecutionService executionService;
    private final EnhancedUIFactory uiFactory;
    
    // Dependencies
    private final ApplicationContext context;
    private final AutomationProjectManager projectManager;
    private final BrobotRunnerProperties runnerProperties;
    private final AutomationOrchestrator automationOrchestrator;
    private final EventBus eventBus;
    private final HotkeyManager hotkeyManager;
    private final AutomationWindowController windowController;
    
    // UI Components
    private EnhancedUIFactory.AssembledUI assembledUI;
    
    @Autowired
    public RefactoredEnhancedAutomationPanel(
            EnhancedHotkeyService hotkeyService,
            EnhancedWindowService windowService,
            EnhancedStatusMonitoringService statusService,
            EnhancedButtonService buttonService,
            EnhancedExecutionService executionService,
            EnhancedUIFactory uiFactory,
            ApplicationContext context,
            AutomationProjectManager projectManager,
            BrobotRunnerProperties runnerProperties,
            AutomationOrchestrator automationOrchestrator,
            EventBus eventBus,
            HotkeyManager hotkeyManager,
            AutomationWindowController windowController) {
        
        this.hotkeyService = hotkeyService;
        this.windowService = windowService;
        this.statusService = statusService;
        this.buttonService = buttonService;
        this.executionService = executionService;
        this.uiFactory = uiFactory;
        this.context = context;
        this.projectManager = projectManager;
        this.runnerProperties = runnerProperties;
        this.automationOrchestrator = automationOrchestrator;
        this.eventBus = eventBus;
        this.hotkeyManager = hotkeyManager;
        this.windowController = windowController;
        
        INSTANCE = this;
    }
    
    @PostConstruct
    public void initialize() {
        log.info("Initializing Refactored Enhanced Automation Panel");
        
        // Configure services
        configureServices();
        
        // Set up UI
        setupUI();
        
        // Set up hotkeys
        setupHotkeys();
        
        log("Enhanced automation panel initialized. Configure hotkeys and load a project to begin.");
    }
    
    @PreDestroy
    public void cleanup() {
        log.info("Cleaning up Refactored Enhanced Automation Panel");
        statusService.stopMonitoring();
    }
    
    /**
     * Configures all services with appropriate settings and handlers.
     */
    private void configureServices() {
        // Configure hotkey service
        hotkeyService.setConfiguration(
            EnhancedHotkeyService.HotkeyConfiguration.builder()
                .autoRegisterWithScene(true)
                .showConfigDialog(true)
                .logHotkeyActions(true)
                .build()
        );
        hotkeyService.setLogHandler(this::log);
        
        // Configure window service
        windowService.setConfiguration(
            EnhancedWindowService.WindowConfiguration.builder()
                .autoMinimizeEnabled(windowController.isAutoMinimizeEnabled())
                .restoreAfterAutomation(true)
                .logWindowActions(true)
                .build()
        );
        windowService.setLogHandler(this::log);
        
        // Configure status monitoring
        statusService.setConfiguration(
            EnhancedStatusMonitoringService.MonitoringConfiguration.builder()
                .updateIntervalMs(100)
                .threadName("EnhancedStatusUpdater")
                .daemon(true)
                .autoStart(true)
                .build()
        );
        
        statusService.addStatusListener(update -> {
            // Update status panel
            if (assembledUI != null && assembledUI.getStatusPanel() != null) {
                assembledUI.getStatusPanel().updateStatus(update.getStatus());
            }
            
            // Update pause/resume button
            if (assembledUI != null && assembledUI.getControlBar() != null) {
                var button = assembledUI.getControlBar().getPauseResumeButton();
                button.setText(update.getPauseResumeText());
                button.setDisable(!update.isPauseResumeEnabled());
            }
            
            // Update button states
            if (assembledUI != null && assembledUI.getButtonPane() != null) {
                buttonService.updateButtonStates(assembledUI.getButtonPane(), update.isButtonsDisabled());
            }
            
            // Update settings
            if (assembledUI != null && assembledUI.getSettingsBar() != null) {
                assembledUI.getSettingsBar().getConfigureHotkeysButton().setDisable(update.isButtonsDisabled());
            }
        });
        
        // Configure button service
        buttonService.setConfiguration(
            EnhancedButtonService.ButtonConfiguration.builder()
                .groupByCategory(true)
                .defaultCategory("General")
                .categorySpacing(5)
                .build()
        );
        
        buttonService.setButtonActionHandler(this::runAutomation);
        buttonService.setLogHandler(this::log);
        
        // Configure execution service
        executionService.setConfiguration(
            EnhancedExecutionService.ExecutionConfiguration.builder()
                .confirmationEnabled(true)
                .publishEvents(true)
                .checkConcurrency(true)
                .build()
        );
        
        executionService.setLogHandler(this::log);
        
        // Configure UI factory
        uiFactory.setConfiguration(
            EnhancedUIFactory.UIConfiguration.builder()
                .panelPadding(20)
                .panelSpacing(10)
                .build()
        );
    }
    
    /**
     * Sets up the UI components and layout.
     */
    private void setupUI() {
        // Create main panel
        VBox mainPanel = uiFactory.createMainPanel();
        
        // Create components
        var statusPanel = uiFactory.createStatusPanel(hotkeyManager);
        var controlBar = uiFactory.createControlBar();
        var settingsBar = uiFactory.createSettingsBar();
        var buttonPane = uiFactory.createButtonPane();
        var buttonScrollPane = uiFactory.createButtonScrollPane(buttonPane);
        var logArea = uiFactory.createLogArea();
        
        // Set up control actions
        controlBar.getRefreshButton().setOnAction(e -> refreshAutomationButtons());
        controlBar.getPauseResumeButton().setOnAction(e -> executionService.togglePauseResume());
        controlBar.getStopButton().setOnAction(e -> 
            executionService.stopAllAutomation(() -> windowService.restoreAfterAutomation())
        );
        
        // Set up settings actions
        settingsBar.getConfigureHotkeysButton().setOnAction(e -> showHotkeyConfigDialog());
        settingsBar.getAutoMinimizeCheckbox().setSelected(windowService.isAutoMinimizeEnabled());
        settingsBar.getAutoMinimizeCheckbox().selectedProperty().addListener((obs, oldVal, newVal) -> {
            windowService.setAutoMinimizeEnabled(newVal);
        });
        
        // Assemble UI
        assembledUI = uiFactory.assembleUI(
            mainPanel, statusPanel, controlBar, settingsBar, buttonPane, buttonScrollPane, logArea
        );
        
        // Add to this panel
        getChildren().add(mainPanel);
    }
    
    /**
     * Sets up hotkeys.
     */
    private void setupHotkeys() {
        hotkeyService.registerHotkeyActions(
            () -> executionService.pauseAutomation(),
            () -> executionService.resumeAutomation(),
            () -> executionService.stopAllAutomation(() -> windowService.restoreAfterAutomation()),
            () -> executionService.togglePauseResume()
        );
        
        // Register with scene when available
        sceneProperty().addListener((obs, oldScene, newScene) -> {
            hotkeyService.registerWithScene(newScene);
        });
    }
    
    /**
     * Shows the hotkey configuration dialog.
     */
    private void showHotkeyConfigDialog() {
        hotkeyService.showConfigurationDialog(assembledUI.getStatusPanel());
    }
    
    /**
     * Refreshes the automation buttons.
     */
    public void refreshAutomationButtons() {
        var result = buttonService.loadProjectButtons();
        
        log(result.getMessage());
        
        if (result.isSuccess()) {
            buttonService.populateButtonPane(assembledUI.getButtonPane(), result.getButtons());
        }
    }
    
    /**
     * Runs an automation task.
     * @param buttonDef The task button definition
     */
    private void runAutomation(TaskButton buttonDef) {
        executionService.runAutomation(buttonDef, () -> windowService.minimizeForAutomation());
    }
    
    /**
     * Logs a message to the log area.
     * @param message The message to log
     */
    public void log(String message) {
        String timestamp = LocalDateTime.now().format(TIME_FORMATTER);
        String logEntry = "[" + timestamp + "] " + message;
        
        Platform.runLater(() -> {
            var logArea = assembledUI.getLogArea();
            logArea.appendText(logEntry + "\n");
            logArea.positionCaret(logArea.getText().length());
        });
    }
    
    /**
     * Gets the singleton instance.
     * @return Optional containing the instance
     */
    public static Optional<RefactoredEnhancedAutomationPanel> getInstance() {
        return Optional.ofNullable(INSTANCE);
    }
    
    /**
     * Sets the singleton instance.
     * @param instance The instance to set
     */
    public static void setInstance(RefactoredEnhancedAutomationPanel instance) {
        INSTANCE = instance;
    }
}