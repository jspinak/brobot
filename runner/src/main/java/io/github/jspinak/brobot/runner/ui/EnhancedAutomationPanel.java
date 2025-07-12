package io.github.jspinak.brobot.runner.ui;

import io.github.jspinak.brobot.runner.automation.AutomationOrchestrator;
import io.github.jspinak.brobot.runner.config.BrobotRunnerProperties;
import io.github.jspinak.brobot.runner.events.EventBus;
import io.github.jspinak.brobot.runner.events.ExecutionStatusEvent;
import io.github.jspinak.brobot.runner.execution.ExecutionState;
import io.github.jspinak.brobot.runner.execution.ExecutionStatus;
import io.github.jspinak.brobot.runner.hotkeys.HotkeyManager;
import io.github.jspinak.brobot.runner.hotkeys.HotkeyManager.HotkeyAction;
import io.github.jspinak.brobot.runner.project.AutomationProject;
import io.github.jspinak.brobot.runner.project.AutomationProjectManager;
import io.github.jspinak.brobot.runner.project.TaskButton;
import javafx.application.Platform;
import javafx.geometry.Insets;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import javafx.scene.paint.Color;
import lombok.Getter;
import lombok.Setter;
import lombok.AccessLevel;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.ApplicationContext;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;

/**
 * Enhanced JavaFX UI component for automation control with hotkey support.
 * 
 * @deprecated Use {@link io.github.jspinak.brobot.runner.ui.panels.UnifiedAutomationPanel} instead.
 *             This class will be removed in a future version.
 */
@Deprecated
@Getter
@Setter(AccessLevel.PRIVATE)
public class EnhancedAutomationPanel extends VBox {
    private static EnhancedAutomationPanel INSTANCE;
    
    private static final Logger logger = LoggerFactory.getLogger(EnhancedAutomationPanel.class);
    private static final DateTimeFormatter TIME_FORMATTER = DateTimeFormatter.ofPattern("HH:mm:ss");
    
    private final ApplicationContext context;
    private final AutomationProjectManager projectManager;
    private final BrobotRunnerProperties runnerProperties;
    private final AutomationOrchestrator automationOrchestrator;
    private final EventBus eventBus;
    private final HotkeyManager hotkeyManager;
    private final AutomationWindowController windowController;
    
    private final TextArea logArea;
    private final FlowPane buttonPane;
    private AutomationStatusPanel statusPanel;
    private Button pauseResumeButton;
    private Button configureHotkeysButton;
    private CheckBox autoMinimizeCheckbox;
    
    private volatile boolean updateInProgress = false;
    
    /**
     * Constructor with Spring dependencies
     */
    public EnhancedAutomationPanel(ApplicationContext context, 
                                   AutomationProjectManager projectManager,
                                   BrobotRunnerProperties runnerProperties, 
                                   AutomationOrchestrator automationOrchestrator,
                                   EventBus eventBus,
                                   HotkeyManager hotkeyManager,
                                   AutomationWindowController windowController) {
        this.context = context;
        this.projectManager = projectManager;
        this.runnerProperties = runnerProperties;
        this.automationOrchestrator = automationOrchestrator;
        this.eventBus = eventBus;
        this.hotkeyManager = hotkeyManager;
        this.windowController = windowController;
        this.logArea = new TextArea();
        this.buttonPane = new FlowPane();
        
        setupUI();
        setupHotkeys();
        
        // Start status update thread
        if (automationOrchestrator != null) {
            startStatusUpdateThread();
        }
        
        INSTANCE = this;
    }
    
    private void setupUI() {
        setPadding(new Insets(20));
        setSpacing(10);
        getStyleClass().add("enhanced-automation-panel");
        
        // Status panel at the top
        statusPanel = new AutomationStatusPanel(hotkeyManager);
        
        // Control buttons
        HBox controlBar = createControlBar();
        
        // Settings bar
        HBox settingsBar = createSettingsBar();
        
        // Button pane for user-defined automation buttons
        setupButtonPane();
        
        ScrollPane buttonScrollPane = new ScrollPane(buttonPane);
        buttonScrollPane.setFitToWidth(true);
        buttonScrollPane.setPrefHeight(150);
        
        // Log area
        setupLogArea();
        
        // Layout
        getChildren().addAll(
            statusPanel,
            new Separator(),
            controlBar,
            settingsBar,
            new Separator(),
            createSectionLabel("Automation Functions:"),
            buttonScrollPane,
            createSectionLabel("Log:"),
            logArea
        );
    }
    
    private HBox createControlBar() {
        HBox controlBar = new HBox(10);
        controlBar.getStyleClass().add("control-bar");
        
        Button refreshButton = new Button("Refresh Functions");
        refreshButton.setId("refreshAutomationButtons");
        refreshButton.setOnAction(e -> refreshAutomationButtons());
        
        pauseResumeButton = new Button("Pause");
        pauseResumeButton.setId("pauseResumeExecution");
        pauseResumeButton.setOnAction(e -> togglePauseResume());
        pauseResumeButton.setDisable(true);
        
        Button stopButton = new Button("Stop All");
        stopButton.setId("stopAllAutomation");
        stopButton.setOnAction(e -> stopAllAutomation());
        stopButton.getStyleClass().add("button-danger");
        
        controlBar.getChildren().addAll(refreshButton, pauseResumeButton, stopButton);
        
        return controlBar;
    }
    
    private HBox createSettingsBar() {
        HBox settingsBar = new HBox(15);
        settingsBar.setAlignment(javafx.geometry.Pos.CENTER_LEFT);
        
        configureHotkeysButton = new Button("Configure Hotkeys");
        configureHotkeysButton.setOnAction(e -> showHotkeyConfigDialog());
        
        autoMinimizeCheckbox = new CheckBox("Auto-minimize on start");
        autoMinimizeCheckbox.setSelected(windowController.isAutoMinimizeEnabled());
        autoMinimizeCheckbox.selectedProperty().addListener((obs, oldVal, newVal) -> {
            windowController.setAutoMinimizeEnabled(newVal);
        });
        
        settingsBar.getChildren().addAll(configureHotkeysButton, autoMinimizeCheckbox);
        
        return settingsBar;
    }
    
    private void setupButtonPane() {
        buttonPane.getStyleClass().add("button-pane");
        buttonPane.setPadding(new Insets(10));
        buttonPane.setHgap(10);
        buttonPane.setVgap(10);
        buttonPane.setBorder(new Border(new BorderStroke(
            Color.LIGHTGRAY, BorderStrokeStyle.SOLID, new CornerRadii(5), BorderWidths.DEFAULT
        )));
    }
    
    private void setupLogArea() {
        logArea.setEditable(false);
        logArea.setWrapText(true);
        logArea.setPrefHeight(200);
        
        log("Enhanced automation panel initialized. Configure hotkeys and load a project to begin.");
    }
    
    private void setupHotkeys() {
        // Register hotkey actions
        hotkeyManager.registerAction(HotkeyAction.PAUSE, this::pauseAutomation);
        hotkeyManager.registerAction(HotkeyAction.RESUME, this::resumeAutomation);
        hotkeyManager.registerAction(HotkeyAction.STOP, this::stopAllAutomation);
        hotkeyManager.registerAction(HotkeyAction.TOGGLE_PAUSE, this::togglePauseResume);
        
        // Register with scene when available
        sceneProperty().addListener((obs, oldScene, newScene) -> {
            if (newScene != null) {
                hotkeyManager.registerWithScene(newScene);
                log("Hotkeys registered. Use " + hotkeyManager.getHotkeyDisplayString(HotkeyAction.TOGGLE_PAUSE) + 
                    " to pause/resume automation.");
            }
        });
    }
    
    private void showHotkeyConfigDialog() {
        HotkeyConfigDialog dialog = new HotkeyConfigDialog(hotkeyManager);
        dialog.showAndWait();
        
        if (dialog.isSaved()) {
            statusPanel.updateHotkeyDisplay();
            log("Hotkey configuration updated");
        }
    }
    
    private Label createSectionLabel(String text) {
        Label label = new Label(text);
        label.getStyleClass().add("section-label");
        return label;
    }
    
    /**
     * Starts a background thread to update the UI with execution status
     */
    private void startStatusUpdateThread() {
        Thread statusThread = new Thread(() -> {
            while (true) {
                try {
                    if (automationOrchestrator != null) {
                        updateExecutionStatusUI();
                    }
                    Thread.sleep(100); // Update 10 times per second
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
    
    private void updateExecutionStatusUI() {
        if (updateInProgress) return;
        
        updateInProgress = true;
        try {
            ExecutionStatus status = automationOrchestrator.getExecutionStatus();
            if (status == null) return;
            
            Platform.runLater(() -> {
                // Update status panel
                statusPanel.updateStatus(status);
                
                // Update pause/resume button
                updatePauseResumeButton(status);
                
                // Update button states
                updateButtonStates(status.getState().isActive());
            });
        } finally {
            updateInProgress = false;
        }
    }
    
    private void updatePauseResumeButton(ExecutionStatus status) {
        ExecutionState state = status.getState();
        
        if (state == ExecutionState.RUNNING) {
            pauseResumeButton.setText("Pause");
            pauseResumeButton.setDisable(false);
        } else if (state == ExecutionState.PAUSED) {
            pauseResumeButton.setText("Resume");
            pauseResumeButton.setDisable(false);
        } else {
            pauseResumeButton.setText("Pause");
            pauseResumeButton.setDisable(!state.isActive());
        }
    }
    
    public void refreshAutomationButtons() {
        buttonPane.getChildren().clear();
        
        if (projectManager == null || projectManager.getCurrentProject() == null) {
            log("No project loaded. Please load a configuration first.");
            return;
        }
        
        AutomationProject project = projectManager.getCurrentProject();
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
        
        // Group buttons by category
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
            categoryLabel.getStyleClass().add("category-label");
            categoryBox.getChildren().add(categoryLabel);
            
            for (TaskButton buttonDef : entry.getValue()) {
                Button uiButton = createAutomationButton(buttonDef);
                categoryBox.getChildren().add(uiButton);
            }
            
            buttonPane.getChildren().add(categoryBox);
        }
    }
    
    private Button createAutomationButton(TaskButton buttonDef) {
        Button uiButton = new Button(buttonDef.getLabel());
        
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
    
    private void runAutomation(TaskButton buttonDef) {
        if (automationOrchestrator.getExecutionStatus().getState().isActive()) {
            log("Another automation task is already running. Please wait or stop it first.");
            return;
        }
        
        // Confirmation dialog if required
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
        
        // Minimize window if enabled
        windowController.minimizeForAutomation();
        
        eventBus.publish(ExecutionStatusEvent.started(this,
            automationOrchestrator.getExecutionStatus(),
            "Starting automation: " + buttonDef.getLabel()));
        
        automationOrchestrator.executeAutomation(buttonDef);
    }
    
    private void togglePauseResume() {
        ExecutionState state = automationOrchestrator.getExecutionStatus().getState();
        
        if (state == ExecutionState.RUNNING) {
            pauseAutomation();
        } else if (state == ExecutionState.PAUSED) {
            resumeAutomation();
        }
    }
    
    private void pauseAutomation() {
        if (!automationOrchestrator.getExecutionStatus().getState().isActive() ||
            automationOrchestrator.getExecutionStatus().getState() == ExecutionState.PAUSED) {
            return;
        }
        
        log("Pausing automation...");
        automationOrchestrator.pauseAutomation();
    }
    
    private void resumeAutomation() {
        if (automationOrchestrator.getExecutionStatus().getState() != ExecutionState.PAUSED) {
            return;
        }
        
        log("Resuming automation...");
        automationOrchestrator.resumeAutomation();
    }
    
    private void stopAllAutomation() {
        ExecutionStatus status = automationOrchestrator.getExecutionStatus();
        if (status == null || !status.getState().isActive()) {
            log("No automation is currently running.");
            return;
        }
        
        log("Stopping all automation...");
        
        eventBus.publish(ExecutionStatusEvent.stopped(this,
            status,
            "Stopping all automation"));
        
        automationOrchestrator.stopAllAutomation();
        
        // Restore window if it was minimized
        windowController.restoreAfterAutomation();
    }
    
    private void updateButtonStates(boolean running) {
        Platform.runLater(() -> {
            // Disable automation function buttons when running
            for (javafx.scene.Node node : buttonPane.getChildren()) {
                if (node instanceof VBox categoryBox) {
                    for (javafx.scene.Node child : categoryBox.getChildren()) {
                        if (child instanceof Button && child != pauseResumeButton) {
                            child.setDisable(running);
                        }
                    }
                }
            }
            
            // Disable settings during execution
            configureHotkeysButton.setDisable(running);
        });
    }
    
    public void log(String message) {
        String timestamp = LocalDateTime.now().format(TIME_FORMATTER);
        String logEntry = "[" + timestamp + "] " + message;
        
        Platform.runLater(() -> {
            logArea.appendText(logEntry + "\n");
            logArea.positionCaret(logArea.getText().length());
        });
    }
    
    public static Optional<EnhancedAutomationPanel> getInstance() {
        return Optional.ofNullable(INSTANCE);
    }
    
    public static void setInstance(EnhancedAutomationPanel instance) {
        INSTANCE = instance;
    }
}