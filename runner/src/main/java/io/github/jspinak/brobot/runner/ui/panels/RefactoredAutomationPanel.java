package io.github.jspinak.brobot.runner.ui.panels;

import io.github.jspinak.brobot.runner.automation.AutomationOrchestrator;
import io.github.jspinak.brobot.runner.config.BrobotRunnerProperties;
import io.github.jspinak.brobot.runner.events.EventBus;
import io.github.jspinak.brobot.runner.execution.ExecutionState;
import io.github.jspinak.brobot.runner.execution.ExecutionStatus;
import io.github.jspinak.brobot.runner.hotkeys.HotkeyManager;
import io.github.jspinak.brobot.runner.hotkeys.HotkeyManager.HotkeyAction;
import io.github.jspinak.brobot.runner.project.AutomationProject;
import io.github.jspinak.brobot.runner.project.AutomationProjectManager;
import io.github.jspinak.brobot.runner.project.TaskButton;
import io.github.jspinak.brobot.runner.ui.AutomationStatusPanel;
import io.github.jspinak.brobot.runner.ui.AutomationWindowController;
import io.github.jspinak.brobot.runner.ui.components.base.BasePanel;
import javafx.application.Platform;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Refactored automation panel using the new architecture.
 * Demonstrates proper use of BasePanel, LabelManager, and UIUpdateManager.
 */
@Slf4j
@Component
public class RefactoredAutomationPanel extends BasePanel {
    private static final DateTimeFormatter TIME_FORMATTER = DateTimeFormatter.ofPattern("HH:mm:ss");
    
    // Dependencies
    @Autowired private ApplicationContext context;
    @Autowired private AutomationProjectManager projectManager;
    @Autowired private BrobotRunnerProperties runnerProperties;
    @Autowired private AutomationOrchestrator automationOrchestrator;
    @Autowired private EventBus eventBus;
    @Autowired private HotkeyManager hotkeyManager;
    @Autowired private AutomationWindowController windowController;
    
    // UI Components
    private TextArea logArea;
    private FlowPane buttonPane;
    private ProgressBar progressBar;
    private AutomationStatusPanel statusPanel;
    
    // State tracking
    private final Set<String> renderedCategories = ConcurrentHashMap.newKeySet();
    private final Map<String, Button> renderedButtons = new ConcurrentHashMap<>();
    
    public RefactoredAutomationPanel() {
        super("RefactoredAutomationPanel");
    }
    
    @Override
    protected void doInitialize() {
        setupUI();
        setupHotkeys();
        setupEventListeners();
        
        // Schedule periodic status updates using UIUpdateManager
        schedulePeriodicUpdate(this::updateStatus, 1);
        
        // Initial button refresh
        doRefresh();
    }
    
    @Override
    protected void doRefresh() {
        refreshAutomationButtons();
    }
    
    @Override
    protected void doCleanup() {
        // Additional cleanup specific to this panel
        renderedCategories.clear();
        renderedButtons.clear();
    }
    
    private void setupUI() {
        setSpacing(20);
        setPadding(new Insets(20));
        getStyleClass().add("automation-panel");
        
        // Title using LabelManager
        Label titleLabel = labelManager.getOrCreateLabel(
            "automation_title", 
            "Automation Control",
            "title-label"
        );
        titleLabel.setStyle("-fx-font-size: 18; -fx-font-weight: bold;");
        
        // Status panel
        statusPanel = new AutomationStatusPanel(hotkeyManager);
        statusPanel.setPrefHeight(80);
        
        // Control section
        HBox controlSection = createControlSection();
        
        // Progress section
        VBox progressSection = createProgressSection();
        
        // Button pane
        buttonPane = new FlowPane();
        buttonPane.setHgap(15);
        buttonPane.setVgap(15);
        buttonPane.setPadding(new Insets(10));
        buttonPane.getStyleClass().add("button-pane");
        
        ScrollPane buttonScrollPane = new ScrollPane(buttonPane);
        buttonScrollPane.setFitToWidth(true);
        buttonScrollPane.setPrefHeight(200);
        
        // Log area
        logArea = new TextArea();
        logArea.setEditable(false);
        logArea.setWrapText(true);
        logArea.setPrefHeight(200);
        logArea.getStyleClass().add("automation-log");
        
        // Section labels
        Label actionsLabel = labelManager.getOrCreateLabel(
            "actions_section",
            "Available Actions",
            "section-label"
        );
        
        Label logLabel = labelManager.getOrCreateLabel(
            "log_section",
            "Execution Log",
            "section-label"
        );
        
        // Add all components
        getChildren().addAll(
            titleLabel,
            new Separator(),
            statusPanel,
            progressSection,
            controlSection,
            new Separator(),
            actionsLabel,
            buttonScrollPane,
            new Separator(),
            logLabel,
            logArea
        );
    }
    
    private HBox createControlSection() {
        HBox controlBox = new HBox(10);
        controlBox.setAlignment(Pos.CENTER_LEFT);
        
        Button refreshBtn = new Button("Refresh");
        refreshBtn.setOnAction(e -> doRefresh());
        
        Button pauseBtn = new Button("⏸ Pause");
        pauseBtn.setOnAction(e -> pauseAutomation());
        
        Button resumeBtn = new Button("▶ Resume");
        resumeBtn.setOnAction(e -> resumeAutomation());
        
        Button stopBtn = new Button("⏹ Stop");
        stopBtn.setOnAction(e -> stopAutomation());
        stopBtn.getStyleClass().add("danger");
        
        CheckBox autoMinimizeCheck = new CheckBox("Auto-minimize");
        autoMinimizeCheck.setSelected(windowController.isAutoMinimizeEnabled());
        autoMinimizeCheck.selectedProperty().addListener((obs, oldVal, newVal) -> 
            windowController.setAutoMinimizeEnabled(newVal)
        );
        
        controlBox.getChildren().addAll(
            refreshBtn, pauseBtn, resumeBtn, stopBtn,
            new Separator(javafx.geometry.Orientation.VERTICAL),
            autoMinimizeCheck
        );
        
        return controlBox;
    }
    
    private VBox createProgressSection() {
        VBox progressBox = new VBox(5);
        
        Label statusLabel = labelManager.getOrCreateLabel(
            "status_label",
            "Status: Ready",
            "status-label"
        );
        
        progressBar = new ProgressBar(0);
        progressBar.setPrefWidth(400);
        
        HBox progressContainer = new HBox(10);
        progressContainer.setAlignment(Pos.CENTER_LEFT);
        
        Label progressLabel = labelManager.getOrCreateLabel(
            "progress_label",
            "0%"
        );
        
        progressContainer.getChildren().addAll(progressBar, progressLabel);
        progressBox.getChildren().addAll(statusLabel, progressContainer);
        
        return progressBox;
    }
    
    private void setupHotkeys() {
        hotkeyManager.registerAction(HotkeyAction.PAUSE, this::pauseAutomation);
        hotkeyManager.registerAction(HotkeyAction.RESUME, this::resumeAutomation);
        hotkeyManager.registerAction(HotkeyAction.STOP, this::stopAutomation);
        hotkeyManager.registerAction(HotkeyAction.TOGGLE_PAUSE, this::togglePauseResume);
    }
    
    private void setupEventListeners() {
        // Set up automation callbacks
        if (automationOrchestrator != null) {
            automationOrchestrator.setLogCallback(this::addLogEntry);
        }
    }
    
    private void refreshAutomationButtons() {
        Platform.runLater(() -> {
            try {
                if (projectManager == null || projectManager.getCurrentProject() == null) {
                    clearButtons();
                    addLogEntry("No project loaded");
                    return;
                }
                
                AutomationProject project = projectManager.getCurrentProject();
                if (project.getAutomation() == null || project.getAutomation().getButtons() == null) {
                    clearButtons();
                    addLogEntry("No automation buttons defined");
                    return;
                }
                
                List<TaskButton> buttons = project.getAutomation().getButtons();
                if (buttons.isEmpty()) {
                    clearButtons();
                    addLogEntry("No automation buttons defined");
                    return;
                }
                
                addLogEntry("Found " + buttons.size() + " automation functions");
                updateButtonDisplay(buttons);
                
            } catch (Exception e) {
                log.error("Error refreshing automation buttons", e);
                addLogEntry("Error: " + e.getMessage());
            }
        });
    }
    
    private void updateButtonDisplay(List<TaskButton> buttons) {
        // Group buttons by category
        Map<String, List<TaskButton>> buttonsByCategory = new HashMap<>();
        for (TaskButton button : buttons) {
            String category = button.getCategory() != null ? button.getCategory() : "General";
            buttonsByCategory.computeIfAbsent(category, k -> new ArrayList<>()).add(button);
        }
        
        // Clear buttons that no longer exist
        Set<String> currentCategories = buttonsByCategory.keySet();
        renderedCategories.removeIf(cat -> !currentCategories.contains(cat));
        
        // Update button pane
        buttonPane.getChildren().clear();
        
        for (Map.Entry<String, List<TaskButton>> entry : buttonsByCategory.entrySet()) {
            VBox categoryBox = createCategoryBox(entry.getKey(), entry.getValue());
            buttonPane.getChildren().add(categoryBox);
        }
    }
    
    private VBox createCategoryBox(String category, List<TaskButton> buttons) {
        VBox categoryBox = new VBox(5);
        categoryBox.setPadding(new Insets(5));
        categoryBox.setStyle("-fx-border-color: lightgray; -fx-border-radius: 5;");
        
        // Use LabelManager for category label
        String labelId = "category_" + category.hashCode();
        Label categoryLabel = labelManager.getOrCreateLabel(labelId, category, "category-label");
        categoryLabel.setStyle("-fx-font-weight: bold;");
        
        categoryBox.getChildren().add(categoryLabel);
        
        for (TaskButton buttonDef : buttons) {
            Button button = createTaskButton(buttonDef);
            categoryBox.getChildren().add(button);
        }
        
        renderedCategories.add(category);
        return categoryBox;
    }
    
    private Button createTaskButton(TaskButton buttonDef) {
        String buttonId = "button_" + buttonDef.getId();
        
        Button button = renderedButtons.computeIfAbsent(buttonId, k -> {
            Button newButton = new Button(buttonDef.getLabel());
            newButton.setId(buttonId);
            
            if (buttonDef.getTooltip() != null) {
                newButton.setTooltip(new Tooltip(buttonDef.getTooltip()));
            }
            
            newButton.setOnAction(e -> executeAutomation(buttonDef));
            return newButton;
        });
        
        // Update button text in case it changed
        button.setText(buttonDef.getLabel());
        
        return button;
    }
    
    private void executeAutomation(TaskButton buttonDef) {
        addLogEntry("Starting: " + buttonDef.getLabel());
        automationOrchestrator.executeAutomation(buttonDef);
        
        if (windowController.isAutoMinimizeEnabled()) {
            windowController.minimizeForAutomation();
        }
    }
    
    private void pauseAutomation() {
        automationOrchestrator.pauseAutomation();
        updateStatusPanel(ExecutionState.PAUSED, "Paused");
        addLogEntry("Automation paused");
    }
    
    private void resumeAutomation() {
        automationOrchestrator.resumeAutomation();
        updateStatusPanel(ExecutionState.RUNNING, "Resumed");
        addLogEntry("Automation resumed");
    }
    
    private void stopAutomation() {
        automationOrchestrator.stopAllAutomation();
        updateStatusPanel(ExecutionState.STOPPED, "Stopped");
        windowController.restoreAfterAutomation();
        addLogEntry("Automation stopped");
    }
    
    private void togglePauseResume() {
        if (automationOrchestrator.isPaused()) {
            resumeAutomation();
        } else {
            pauseAutomation();
        }
    }
    
    private void updateStatus() {
        if (!isValid()) return;
        
        ExecutionStatus status = automationOrchestrator.getExecutionStatus();
        Platform.runLater(() -> {
            // Update status label
            labelManager.updateLabel("status_label", 
                "Status: " + status.getState().getDescription());
            
            // Update progress
            progressBar.setProgress(status.getProgress());
            labelManager.updateLabel("progress_label", 
                String.format("%.0f%%", status.getProgress() * 100));
            
            // Update status panel
            statusPanel.updateStatus(status);
        });
    }
    
    private void updateStatusPanel(ExecutionState state, String message) {
        ExecutionStatus status = new ExecutionStatus();
        status.setState(state);
        status.setCurrentOperation(message);
        statusPanel.updateStatus(status);
    }
    
    private void addLogEntry(String message) {
        Platform.runLater(() -> {
            String timestamp = TIME_FORMATTER.format(LocalDateTime.now());
            String entry = String.format("[%s] %s%n", timestamp, message);
            logArea.appendText(entry);
            
            // Auto-scroll to bottom
            logArea.setScrollTop(Double.MAX_VALUE);
        });
    }
    
    private void clearButtons() {
        buttonPane.getChildren().clear();
        renderedCategories.clear();
        renderedButtons.clear();
    }
}