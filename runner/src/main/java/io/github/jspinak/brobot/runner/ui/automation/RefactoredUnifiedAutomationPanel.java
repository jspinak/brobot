package io.github.jspinak.brobot.runner.ui.automation;

import atlantafx.base.theme.Styles;
import io.github.jspinak.brobot.runner.events.EventBus;
import io.github.jspinak.brobot.runner.project.AutomationProject;
import io.github.jspinak.brobot.runner.project.AutomationProjectManager;
import io.github.jspinak.brobot.runner.ui.automation.components.AutomationControlPanel;
import io.github.jspinak.brobot.runner.ui.automation.components.AutomationStatusPanel;
import io.github.jspinak.brobot.runner.ui.automation.factories.AutomationButtonFactory;
import io.github.jspinak.brobot.runner.ui.automation.models.AutomationButtonConfig;
import io.github.jspinak.brobot.runner.ui.automation.services.AutomationControlService;
import io.github.jspinak.brobot.runner.ui.automation.services.AutomationStatusService;
import io.github.jspinak.brobot.runner.ui.components.base.BrobotPanel;
import javafx.application.Platform;
import javafx.geometry.Insets;
import javafx.geometry.Orientation;
import javafx.geometry.Pos;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import jakarta.annotation.PostConstruct;
import jakarta.annotation.PreDestroy;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * Refactored unified panel for automation management.
 * Uses separated services and components following Single Responsibility Principle.
 */
@Slf4j
@Component
public class RefactoredUnifiedAutomationPanel extends BrobotPanel {
    
    private final AutomationProjectManager projectManager;
    private final AutomationButtonFactory buttonFactory;
    private final AutomationControlService controlService;
    private final AutomationStatusService statusService;
    private final EventBus eventBus;
    
    // UI Components
    private final AutomationControlPanel controlPanel;
    private final AutomationStatusPanel statusPanel;
    
    // Automation buttons
    private VBox automationButtonsContainer;
    private Map<String, Button> automationButtons = new HashMap<>();
    
    // Project selector
    private ComboBox<String> projectSelector;
    private Button refreshButton;
    
    @Autowired
    public RefactoredUnifiedAutomationPanel(
            AutomationProjectManager projectManager,
            AutomationButtonFactory buttonFactory,
            AutomationControlService controlService,
            AutomationStatusService statusService,
            AutomationControlPanel controlPanel,
            AutomationStatusPanel statusPanel,
            EventBus eventBus) {
        
        super();
        this.projectManager = projectManager;
        this.buttonFactory = buttonFactory;
        this.controlService = controlService;
        this.statusService = statusService;
        this.controlPanel = controlPanel;
        this.statusPanel = statusPanel;
        this.eventBus = eventBus;
    }
    
    @PostConstruct
    public void postConstruct() {
        // Re-initialize with dependencies available
        initialize();
        
        // Start monitoring automation status
        statusService.startMonitoring();
        
        // Set up event listeners
        setupEventListeners();
        
        // Load initial project if available
        loadCurrentProject();
    }
    
    @PreDestroy
    public void preDestroy() {
        // Stop monitoring when panel is destroyed
        statusService.stopMonitoring();
    }
    
    @Override
    protected void initialize() {
        // Skip initialization if dependencies not ready
        if (projectManager == null || buttonFactory == null || controlService == null || 
            statusService == null || controlPanel == null || statusPanel == null) {
            return;
        }
        
        // Create main layout
        BorderPane mainLayout = new BorderPane();
        mainLayout.setPadding(new Insets(16));
        
        // Top: Project selector
        HBox topBar = createTopBar();
        mainLayout.setTop(topBar);
        BorderPane.setMargin(topBar, new Insets(0, 0, 16, 0));
        
        // Center: Split pane with automations and status
        SplitPane splitPane = createSplitPane();
        mainLayout.setCenter(splitPane);
        
        getChildren().add(mainLayout);
    }
    
    /**
     * Creates the top bar with project selector.
     */
    private HBox createTopBar() {
        HBox topBar = new HBox(16);
        topBar.setAlignment(Pos.CENTER_LEFT);
        topBar.setPadding(new Insets(0, 0, 8, 0));
        
        // Project selector
        Label projectLabel = new Label("Project:");
        projectLabel.getStyleClass().add(Styles.TEXT_BOLD);
        
        projectSelector = new ComboBox<>();
        projectSelector.setPrefWidth(200);
        projectSelector.setPromptText("Select a project");
        
        // Populate projects
        updateProjectList();
        
        // Project change handler
        projectSelector.setOnAction(e -> {
            String selectedProject = projectSelector.getValue();
            if (selectedProject != null) {
                loadProject(selectedProject);
            }
        });
        
        // Refresh button
        refreshButton = new Button("Refresh");
        refreshButton.getStyleClass().add(Styles.BUTTON_ICON);
        refreshButton.setOnAction(e -> refreshProjects());
        
        // Spacer
        Region spacer = new Region();
        HBox.setHgrow(spacer, Priority.ALWAYS);
        
        // Control panel on the right
        controlPanel.setMinHeight(80);
        controlPanel.setPrefHeight(Region.USE_COMPUTED_SIZE);
        controlPanel.setMaxHeight(120);
        
        topBar.getChildren().addAll(
            projectLabel, projectSelector, refreshButton,
            spacer, controlPanel
        );
        
        return topBar;
    }
    
    /**
     * Creates the split pane with automations list and status.
     */
    private SplitPane createSplitPane() {
        SplitPane splitPane = new SplitPane();
        splitPane.setOrientation(Orientation.HORIZONTAL);
        
        // Left: Automations list
        ScrollPane automationsScroll = createAutomationsPanel();
        
        // Right: Status panel
        ScrollPane statusScroll = new ScrollPane(statusPanel);
        statusScroll.setFitToWidth(true);
        statusScroll.setFitToHeight(true);
        
        splitPane.getItems().addAll(automationsScroll, statusScroll);
        splitPane.setDividerPositions(0.6);
        
        return splitPane;
    }
    
    /**
     * Creates the automations panel.
     */
    private ScrollPane createAutomationsPanel() {
        automationButtonsContainer = new VBox(16);
        automationButtonsContainer.setPadding(new Insets(16));
        automationButtonsContainer.setAlignment(Pos.TOP_LEFT);
        automationButtonsContainer.setFillWidth(true);
        
        // Add placeholder
        Label placeholder = new Label("No automations loaded");
        placeholder.getStyleClass().add(Styles.TEXT_MUTED);
        placeholder.setWrapText(true);
        automationButtonsContainer.getChildren().add(placeholder);
        
        ScrollPane scrollPane = new ScrollPane(automationButtonsContainer);
        scrollPane.setFitToWidth(true);
        scrollPane.setHbarPolicy(ScrollPane.ScrollBarPolicy.NEVER);
        
        return scrollPane;
    }
    
    /**
     * Updates the project list in the selector.
     */
    private void updateProjectList() {
        List<String> projects = projectManager.getAvailableProjects();
        projectSelector.getItems().clear();
        projectSelector.getItems().addAll(projects);
        
        // Select current project if any
        AutomationProject currentProject = projectManager.getCurrentProject();
        if (currentProject != null) {
            projectSelector.setValue(currentProject.getName());
        }
    }
    
    /**
     * Refreshes the project list.
     */
    private void refreshProjects() {
        log.info("Refreshing projects list");
        refreshButton.setDisable(true);
        
        Platform.runLater(() -> {
            try {
                projectManager.refreshProjects();
                updateProjectList();
                showInfo("Projects refreshed successfully");
            } catch (Exception e) {
                log.error("Error refreshing projects", e);
                showError("Failed to refresh projects: " + e.getMessage());
            } finally {
                refreshButton.setDisable(false);
            }
        });
    }
    
    /**
     * Loads a project by name.
     */
    private void loadProject(String projectName) {
        log.info("Loading project: {}", projectName);
        
        try {
            projectManager.loadProject(projectName);
            loadCurrentProject();
            showInfo("Project loaded: " + projectName);
        } catch (Exception e) {
            log.error("Error loading project", e);
            showError("Failed to load project: " + e.getMessage());
        }
    }
    
    /**
     * Loads the current project's automations.
     */
    private void loadCurrentProject() {
        AutomationProject project = projectManager.getCurrentProject();
        if (project == null) {
            log.debug("No project loaded");
            return;
        }
        
        // Clear existing buttons
        automationButtonsContainer.getChildren().clear();
        automationButtons.clear();
        
        // Group automations by category
        Map<String, List<String>> categorizedAutomations = categorizeAutomations(project.getAutomationNames());
        
        // Create buttons for each category
        for (Map.Entry<String, List<String>> entry : categorizedAutomations.entrySet()) {
            String category = entry.getKey();
            List<String> automations = entry.getValue();
            
            if (!automations.isEmpty()) {
                // Create category box
                VBox categoryBox = buttonFactory.createCategoryBox(
                    category, 
                    "Contains " + automations.size() + " automation(s)"
                );
                
                // Create buttons for automations
                FlowPane buttonFlow = new FlowPane();
                buttonFlow.setHgap(12);
                buttonFlow.setVgap(12);
                buttonFlow.setPadding(new Insets(12, 0, 0, 0));
                buttonFlow.setPrefWrapLength(600);  // Limit width to force wrapping
                
                for (String automation : automations) {
                    AutomationButtonConfig config = createButtonConfig(automation);
                    Button button = buttonFactory.createAutomationButton(config);
                    automationButtons.put(automation, button);
                    buttonFlow.getChildren().add(button);
                }
                
                categoryBox.getChildren().add(buttonFlow);
                automationButtonsContainer.getChildren().add(categoryBox);
            }
        }
        
        // Add "Run All" button if there are multiple automations
        if (project.getAutomationNames().size() > 1) {
            addRunAllButton();
        }
        
        // Update control panel
        controlPanel.setPlayAction(this::runSelectedAutomation);
    }
    
    /**
     * Categorizes automations based on their names.
     */
    private Map<String, List<String>> categorizeAutomations(List<String> automations) {
        return automations.stream()
            .collect(Collectors.groupingBy(this::extractCategory));
    }
    
    /**
     * Extracts category from automation name.
     */
    private String extractCategory(String automationName) {
        // Simple categorization based on name patterns
        if (automationName.toLowerCase().contains("test")) {
            return "Tests";
        } else if (automationName.toLowerCase().contains("setup")) {
            return "Setup";
        } else if (automationName.toLowerCase().contains("demo")) {
            return "Demos";
        } else {
            return "General";
        }
    }
    
    /**
     * Creates button configuration for an automation.
     */
    private AutomationButtonConfig createButtonConfig(String automationName) {
        return AutomationButtonConfig.builder()
            .name(automationName)
            .displayName(formatAutomationName(automationName))
            .description("Click to run " + automationName)
            .category(extractCategory(automationName))
            .onAction(this::runAutomation)
            .enabled(!controlService.isRunning())
            .build();
    }
    
    /**
     * Formats automation name for display.
     */
    private String formatAutomationName(String name) {
        // Convert camelCase or snake_case to readable format
        return name.replaceAll("([a-z])([A-Z])", "$1 $2")
                  .replaceAll("_", " ")
                  .trim();
    }
    
    /**
     * Adds a "Run All" button.
     */
    private void addRunAllButton() {
        Separator separator = new Separator();
        separator.setPadding(new Insets(8, 0, 8, 0));
        
        Button runAllButton = new Button("Run All Automations");
        runAllButton.getStyleClass().addAll(Styles.BUTTON_OUTLINED, Styles.ACCENT);
        runAllButton.setPrefWidth(200);
        runAllButton.setOnAction(e -> runAllAutomations());
        
        automationButtonsContainer.getChildren().addAll(separator, runAllButton);
    }
    
    /**
     * Runs a specific automation.
     */
    private void runAutomation(String automationName) {
        if (controlService.isRunning()) {
            showWarning("An automation is already running");
            return;
        }
        
        log.info("Running automation: {}", automationName);
        
        // Update button states
        updateButtonStates(true);
        
        // Execute automation
        controlService.executeAutomation(automationName)
            .thenAccept(result -> {
                Platform.runLater(() -> {
                    updateButtonStates(false);
                    
                    if (result.isSuccess()) {
                        showInfo("Automation completed successfully: " + automationName);
                    } else {
                        showError("Automation failed: " + result.getError());
                    }
                });
            });
    }
    
    /**
     * Runs the selected automation from control panel.
     */
    private void runSelectedAutomation() {
        // For now, run the first automation
        // In a full implementation, there would be a selection mechanism
        List<String> automations = new ArrayList<>(automationButtons.keySet());
        if (!automations.isEmpty()) {
            runAutomation(automations.get(0));
        }
    }
    
    /**
     * Runs all automations sequentially.
     */
    private void runAllAutomations() {
        if (controlService.isRunning()) {
            showWarning("An automation is already running");
            return;
        }
        
        log.info("Running all automations");
        
        updateButtonStates(true);
        
        controlService.runAllAutomations()
            .thenAccept(result -> {
                Platform.runLater(() -> {
                    updateButtonStates(false);
                    
                    if (result.isSuccess()) {
                        showInfo("All automations completed successfully");
                    } else {
                        showError("Automation sequence failed: " + result.getError());
                    }
                });
            });
    }
    
    /**
     * Updates button enabled states based on running status.
     */
    private void updateButtonStates(boolean isRunning) {
        for (Button button : automationButtons.values()) {
            button.setDisable(isRunning);
        }
        
        // Update project selector
        projectSelector.setDisable(isRunning);
        refreshButton.setDisable(isRunning);
    }
    
    /**
     * Sets up event listeners.
     */
    private void setupEventListeners() {
        // Listen for execution events
        controlService.addExecutionListener(event -> {
            Platform.runLater(() -> {
                switch (event.getType()) {
                    case STARTED:
                        log.debug("Automation started: {}", event.getAutomationName());
                        highlightRunningButton(event.getAutomationName());
                        break;
                    case COMPLETED:
                        log.debug("Automation completed: {}", event.getAutomationName());
                        resetButtonHighlight(event.getAutomationName());
                        break;
                    case FAILED:
                        log.debug("Automation failed: {}", event.getAutomationName());
                        showButtonError(event.getAutomationName());
                        break;
                }
            });
        });
    }
    
    /**
     * Highlights the button for a running automation.
     */
    private void highlightRunningButton(String automationName) {
        Button button = automationButtons.get(automationName);
        if (button != null) {
            buttonFactory.setButtonRunningState(button, true);
        }
    }
    
    /**
     * Resets button highlight after completion.
     */
    private void resetButtonHighlight(String automationName) {
        Button button = automationButtons.get(automationName);
        if (button != null) {
            buttonFactory.setButtonRunningState(button, false);
        }
    }
    
    /**
     * Shows error state on button.
     */
    private void showButtonError(String automationName) {
        Button button = automationButtons.get(automationName);
        if (button != null) {
            buttonFactory.setButtonRunningState(button, false);
            buttonFactory.setButtonErrorState(button, true, "Automation failed");
        }
    }
    
    /**
     * Shows an info message.
     */
    private void showInfo(String message) {
        log.info(message);
        // In a full implementation, this would show a notification
    }
    
    /**
     * Shows a warning message.
     */
    private void showWarning(String message) {
        log.warn(message);
        // In a full implementation, this would show a notification
    }
    
    /**
     * Shows an error message.
     */
    private void showError(String message) {
        log.error(message);
        // In a full implementation, this would show a notification
    }
}