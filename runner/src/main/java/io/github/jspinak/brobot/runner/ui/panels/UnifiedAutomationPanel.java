package io.github.jspinak.brobot.runner.ui.panels;

import io.github.jspinak.brobot.runner.automation.AutomationOrchestrator;
import io.github.jspinak.brobot.runner.config.BrobotRunnerProperties;
import io.github.jspinak.brobot.runner.events.EventBus;
import io.github.jspinak.brobot.runner.execution.ExecutionState;
import io.github.jspinak.brobot.runner.execution.ExecutionStatus;
import io.github.jspinak.brobot.runner.hotkeys.HotkeyManager;
import io.github.jspinak.brobot.runner.hotkeys.HotkeyManager.HotkeyAction;
// import io.github.jspinak.brobot.runner.hotkeys.HotkeyConfigDialog; // TODO: Create this dialog
import io.github.jspinak.brobot.runner.project.AutomationProject;
import io.github.jspinak.brobot.runner.project.AutomationProjectManager;
import io.github.jspinak.brobot.runner.project.TaskButton;
import io.github.jspinak.brobot.runner.ui.AutomationStatusPanel;
import io.github.jspinak.brobot.runner.ui.AutomationWindowController;
import javafx.application.Platform;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import javafx.scene.paint.Color;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Unified AutomationPanel that merges functionality from both
 * AutomationPanel and EnhancedAutomationPanel without using singleton pattern.
 * 
 * Key improvements:
 * - No singleton pattern (proper Spring DI)
 * - Prevents label duplication
 * - Includes hotkey support
 * - Window control features
 * - Thread-safe UI updates
 */
@Slf4j
@Component
@Getter
@Setter(AccessLevel.PRIVATE)
public class UnifiedAutomationPanel extends VBox {
    private static final DateTimeFormatter TIME_FORMATTER = DateTimeFormatter.ofPattern("HH:mm:ss");
    
    // Core dependencies
    private final ApplicationContext context;
    private final AutomationProjectManager projectManager;
    private final BrobotRunnerProperties runnerProperties;
    private final AutomationOrchestrator automationOrchestrator;
    private final EventBus eventBus;
    
    // Enhanced features
    private final HotkeyManager hotkeyManager;
    private final AutomationWindowController windowController;
    
    // UI Components
    private final TextArea logArea;
    private final FlowPane buttonPane;
    private Label statusLabel;
    private ProgressBar progressBar;
    private Button pauseResumeButton;
    private AutomationStatusPanel enhancedStatusPanel;
    
    // State tracking to prevent duplicates
    private final Map<String, Node> renderedCategories = new ConcurrentHashMap<>();
    private final Map<String, Button> renderedButtons = new ConcurrentHashMap<>();
    private volatile boolean updateInProgress = false;
    
    @Autowired
    public UnifiedAutomationPanel(
            ApplicationContext context, 
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
        
        initialize();
    }
    
    private void initialize() {
        setupUI();
        setupHotkeys();
        setupEventListeners();
        
        // Start status update thread
        if (automationOrchestrator != null) {
            automationOrchestrator.setLogCallback(this::log);
            startStatusUpdateThread();
        }
        
        // Initial button refresh
        refreshAutomationButtons();
    }
    
    private void setupUI() {
        setPadding(new Insets(20));
        setSpacing(10);
        getStyleClass().add("automation-panel");
        
        // Title
        Label titleLabel = new Label("Automation Control");
        titleLabel.getStyleClass().add("title-label");
        titleLabel.setStyle("-fx-font-size: 18; -fx-font-weight: bold;");
        
        // Enhanced Status Panel
        enhancedStatusPanel = new AutomationStatusPanel(hotkeyManager);
        enhancedStatusPanel.setPrefHeight(80);
        
        // Control buttons section
        HBox controlSection = createControlSection();
        
        // Progress section
        VBox progressSection = createProgressSection();
        
        // Button pane setup
        buttonPane.getStyleClass().add("button-pane");
        buttonPane.setPadding(new Insets(10));
        buttonPane.setHgap(10);
        buttonPane.setVgap(10);
        buttonPane.setBorder(new Border(new BorderStroke(
                Color.LIGHTGRAY, BorderStrokeStyle.SOLID, new CornerRadii(5), BorderWidths.DEFAULT
        )));
        
        ScrollPane buttonScrollPane = new ScrollPane(buttonPane);
        buttonScrollPane.setFitToWidth(true);
        buttonScrollPane.setPrefHeight(200);
        
        // Log area setup
        setupLogArea();
        
        // Add all components
        getChildren().addAll(
                titleLabel,
                new Separator(),
                enhancedStatusPanel,
                progressSection,
                controlSection,
                createSectionLabel("Available Automation Functions:"),
                buttonScrollPane,
                createSectionLabel("Automation Log:"),
                logArea
        );
    }
    
    private HBox createControlSection() {
        HBox controlBar = new HBox(10);
        controlBar.getStyleClass().add("control-bar");
        controlBar.setAlignment(Pos.CENTER_LEFT);
        
        Button refreshButton = new Button("Refresh");
        refreshButton.setId("refreshAutomationButtons");
        refreshButton.setOnAction(e -> refreshAutomationButtons());
        
        pauseResumeButton = new Button("Pause");
        pauseResumeButton.setId("pauseResumeExecution");
        pauseResumeButton.setOnAction(e -> togglePauseResume());
        pauseResumeButton.setDisable(true);
        
        Button stopAllButton = new Button("Stop All");
        stopAllButton.setId("stopAllAutomation");
        stopAllButton.setOnAction(e -> stopAllAutomation());
        stopAllButton.getStyleClass().add("danger");
        
        Separator separator = new Separator(javafx.geometry.Orientation.VERTICAL);
        
        Button configureHotkeysBtn = new Button("⌨ Hotkeys");
        configureHotkeysBtn.setOnAction(e -> showHotkeyConfiguration());
        
        CheckBox autoMinimizeCheck = new CheckBox("Auto-minimize");
        autoMinimizeCheck.setSelected(windowController.isAutoMinimizeEnabled());
        autoMinimizeCheck.selectedProperty().addListener((obs, oldVal, newVal) -> 
            windowController.setAutoMinimizeEnabled(newVal)
        );
        
        Label hotkeyInfo = new Label("(Ctrl+P: Pause, Ctrl+R: Resume, Ctrl+S: Stop)");
        hotkeyInfo.setStyle("-fx-text-fill: -color-fg-muted; -fx-font-size: 11px;");
        
        controlBar.getChildren().addAll(
            refreshButton, pauseResumeButton, stopAllButton,
            separator,
            configureHotkeysBtn, autoMinimizeCheck,
            hotkeyInfo
        );
        
        return controlBar;
    }
    
    private VBox createProgressSection() {
        VBox progressBox = new VBox(5);
        progressBox.getStyleClass().add("content-section");
        progressBox.setPadding(new Insets(5));
        progressBox.setBorder(new Border(new BorderStroke(
                Color.LIGHTGRAY, BorderStrokeStyle.SOLID, new CornerRadii(5), BorderWidths.DEFAULT
        )));
        
        statusLabel = new Label("Status: Ready");
        statusLabel.getStyleClass().add("status-label");
        statusLabel.setStyle("-fx-font-weight: bold;");
        
        progressBar = new ProgressBar(0);
        progressBar.setPrefWidth(Double.MAX_VALUE);
        
        progressBox.getChildren().addAll(statusLabel, progressBar);
        
        return progressBox;
    }
    
    private void setupLogArea() {
        logArea.setEditable(false);
        logArea.setWrapText(true);
        logArea.setPrefHeight(300);
        logArea.getStyleClass().add("automation-log");
    }
    
    private void setupHotkeys() {
        // Register hotkey actions
        hotkeyManager.registerAction(HotkeyAction.PAUSE, this::pauseAutomation);
        hotkeyManager.registerAction(HotkeyAction.RESUME, this::resumeAutomation);
        hotkeyManager.registerAction(HotkeyAction.STOP, this::stopAllAutomation);
        hotkeyManager.registerAction(HotkeyAction.TOGGLE_PAUSE, this::togglePauseResume);
        
        // Start listening for hotkeys
        // hotkeyManager.startListening(); // TODO: Implement in HotkeyManager
    }
    
    private void setupEventListeners() {
        // Subscribe to execution status events
        // Subscribe to execution status events
        // TODO: Fix EventBus subscription when ExecutionStatusEvent is properly implemented
    }
    
    /**
     * Refreshes the automation buttons based on the currently loaded project.
     * Prevents duplicate categories and buttons.
     */
    public void refreshAutomationButtons() {
        if (updateInProgress) {
            log.debug("Update already in progress, skipping refresh");
            return;
        }
        
        Platform.runLater(() -> {
            updateInProgress = true;
            try {
                if (projectManager == null || projectManager.getCurrentProject() == null) {
                    buttonPane.getChildren().clear();
                    renderedCategories.clear();
                    renderedButtons.clear();
                    log("No project loaded. Please load a configuration first.");
                    return;
                }
                
                AutomationProject project = projectManager.getCurrentProject();
                if (project.getAutomation() == null || project.getAutomation().getButtons() == null) {
                    buttonPane.getChildren().clear();
                    renderedCategories.clear();
                    renderedButtons.clear();
                    log("No automation buttons defined in the current project.");
                    return;
                }
                
                List<TaskButton> buttons = project.getAutomation().getButtons();
                if (buttons.isEmpty()) {
                    buttonPane.getChildren().clear();
                    renderedCategories.clear();
                    renderedButtons.clear();
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
                
                // Update UI only for changed categories
                Set<String> currentCategories = new HashSet<>(buttonsByCategory.keySet());
                Set<String> existingCategories = new HashSet<>(renderedCategories.keySet());
                
                // Remove categories that no longer exist
                existingCategories.stream()
                    .filter(cat -> !currentCategories.contains(cat))
                    .forEach(cat -> {
                        Node categoryNode = renderedCategories.remove(cat);
                        if (categoryNode != null) {
                            buttonPane.getChildren().remove(categoryNode);
                        }
                    });
                
                // Add or update categories
                for (Map.Entry<String, List<TaskButton>> entry : buttonsByCategory.entrySet()) {
                    String category = entry.getKey();
                    
                    if (!renderedCategories.containsKey(category)) {
                        VBox categoryBox = createCategoryBox(category, entry.getValue());
                        renderedCategories.put(category, categoryBox);
                        buttonPane.getChildren().add(categoryBox);
                    } else {
                        // Update existing category if needed
                        updateCategoryBox(category, entry.getValue());
                    }
                }
                
            } finally {
                updateInProgress = false;
            }
        });
    }
    
    private VBox createCategoryBox(String category, List<TaskButton> buttons) {
        VBox categoryBox = new VBox(5);
        categoryBox.setId("category_" + category.hashCode());
        categoryBox.setPadding(new Insets(5));
        categoryBox.setStyle("-fx-border-color: lightgray; -fx-border-radius: 5;");
        categoryBox.getStyleClass().add("category-box");
        
        Label categoryLabel = new Label(category);
        categoryLabel.setId("label_" + category.hashCode());
        categoryLabel.getStyleClass().add("category-label");
        categoryLabel.setStyle("-fx-font-weight: bold;");
        categoryBox.getChildren().add(categoryLabel);
        
        for (TaskButton buttonDef : buttons) {
            Button uiButton = createAutomationButton(buttonDef);
            String buttonKey = category + "_" + buttonDef.getId();
            renderedButtons.put(buttonKey, uiButton);
            categoryBox.getChildren().add(uiButton);
        }
        
        return categoryBox;
    }
    
    private void updateCategoryBox(String category, List<TaskButton> buttons) {
        // This method would update an existing category box if the buttons changed
        // For now, we'll skip complex diffing logic
    }
    
    private Button createAutomationButton(TaskButton buttonDef) {
        Button uiButton = new Button(buttonDef.getLabel());
        uiButton.setId("button_" + buttonDef.getId());
        
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
        uiButton.setOnAction(e -> {
            runAutomation(buttonDef);
            // Auto-minimize if enabled
            if (windowController.isAutoMinimizeEnabled()) {
                windowController.minimizeForAutomation();
            }
        });
        
        return uiButton;
    }
    
    private void runAutomation(TaskButton buttonDef) {
        log("Starting automation: " + buttonDef.getLabel());
        automationOrchestrator.executeAutomation(buttonDef);
    }
    
    private void pauseAutomation() {
        log.info("Pausing automation");
        automationOrchestrator.pauseAutomation();
        ExecutionStatus status = new ExecutionStatus();
        status.setState(ExecutionState.PAUSED);
        enhancedStatusPanel.updateStatus(status);
        log("Automation paused");
    }
    
    private void resumeAutomation() {
        log.info("Resuming automation");
        automationOrchestrator.resumeAutomation();
        ExecutionStatus status = new ExecutionStatus();
        status.setState(ExecutionState.RUNNING);
        status.setCurrentOperation("Resumed");
        enhancedStatusPanel.updateStatus(status);
        log("Automation resumed");
    }
    
    private void stopAllAutomation() {
        log.info("Stopping all automation");
        automationOrchestrator.stopAllAutomation();
        ExecutionStatus status = new ExecutionStatus();
        status.setState(ExecutionState.STOPPED);
        enhancedStatusPanel.updateStatus(status);
        windowController.restoreAfterAutomation();
        log("All automation stopped");
    }
    
    private void togglePauseResume() {
        ExecutionState state = automationOrchestrator.getExecutionStatus().getState();
        
        if (state == ExecutionState.RUNNING) {
            pauseAutomation();
        } else if (state == ExecutionState.PAUSED) {
            resumeAutomation();
        }
    }
    
    private void showHotkeyConfiguration() {
        // TODO: Implement HotkeyConfigDialog
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle("Hotkey Configuration");
        alert.setHeaderText("Hotkey Configuration");
        alert.setContentText("Hotkey configuration dialog not yet implemented.\n\nDefault hotkeys:\n" +
                            "Ctrl+P - Pause\n" +
                            "Ctrl+R - Resume\n" +
                            "Ctrl+S - Stop\n" +
                            "Ctrl+Space - Toggle Pause/Resume");
        alert.showAndWait();
    }
    
    private void updateExecutionStatus(ExecutionStatus status) {
        if (updateInProgress) {
            return;
        }
        
        updateInProgress = true;
        try {
            // Update status label
            statusLabel.setText("Status: " + status.getState().getDescription());
            
            // Update progress bar
            progressBar.setProgress(status.getProgress());
            
            // Update enhanced status panel
            enhancedStatusPanel.updateStatus(status);
            
            // Update pause/resume button
            updatePauseResumeButton(status);
            
            // Restore window if automation completed
            if (status.getState() == ExecutionState.COMPLETED || 
                status.getState() == ExecutionState.STOPPED ||
                status.getState() == ExecutionState.ERROR) {
                windowController.restoreAfterAutomation();
            }
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
    
    private void startStatusUpdateThread() {
        Thread statusThread = new Thread(() -> {
            while (!Thread.currentThread().isInterrupted()) {
                try {
                    ExecutionStatus status = automationOrchestrator.getExecutionStatus();
                    Platform.runLater(() -> updateExecutionStatus(status));
                    Thread.sleep(500); // Update every 500ms
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                    break;
                } catch (Exception e) {
                    log.error("Error in status update thread", e);
                }
            }
        });
        statusThread.setDaemon(true);
        statusThread.setName("AutomationPanel-StatusUpdate");
        statusThread.start();
    }
    
    public void log(String message) {
        Platform.runLater(() -> {
            String timestamp = TIME_FORMATTER.format(LocalDateTime.now());
            String logEntry = String.format("[%s] %s%n", timestamp, message);
            logArea.appendText(logEntry);
            
            // Auto-scroll to bottom
            logArea.setScrollTop(Double.MAX_VALUE);
        });
    }
    
    private Label createSectionLabel(String text) {
        Label label = new Label(text);
        label.getStyleClass().add("section-label");
        return label;
    }
    
    /**
     * Cleanup method to be called when the panel is being destroyed.
     */
    public void cleanup() {
        // hotkeyManager.stopListening(); // TODO: Implement in HotkeyManager
        renderedCategories.clear();
        renderedButtons.clear();
    }
}