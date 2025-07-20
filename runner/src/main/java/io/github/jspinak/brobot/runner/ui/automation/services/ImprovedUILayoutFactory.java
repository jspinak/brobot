package io.github.jspinak.brobot.runner.ui.automation.services;

import io.github.jspinak.brobot.runner.ui.components.base.AtlantaCard;
import javafx.geometry.Insets;
import javafx.geometry.Orientation;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

/**
 * Factory service for creating UI components for the improved automation panel.
 * Provides consistent styling using AtlantaFX components.
 */
@Slf4j
@Service
public class ImprovedUILayoutFactory {
    
    // Configuration
    private LayoutConfiguration configuration = LayoutConfiguration.builder().build();
    
    /**
     * Layout configuration settings.
     */
    public static class LayoutConfiguration {
        private double splitPanePosition = 0.4;
        private int controlBarSpacing = 10;
        private int statusBoxSpacing = 4;
        private int logControlSpacing = 12;
        private int taskPaneHgap = 12;
        private int taskPaneVgap = 12;
        private Insets taskPanePadding = new Insets(8);
        private String primaryButtonClass = "primary";
        private String secondaryButtonClass = "secondary";
        private String dangerButtonClass = "danger";
        
        public static LayoutConfigurationBuilder builder() {
            return new LayoutConfigurationBuilder();
        }
        
        public static class LayoutConfigurationBuilder {
            private LayoutConfiguration config = new LayoutConfiguration();
            
            public LayoutConfigurationBuilder splitPanePosition(double position) {
                config.splitPanePosition = position;
                return this;
            }
            
            public LayoutConfigurationBuilder controlBarSpacing(int spacing) {
                config.controlBarSpacing = spacing;
                return this;
            }
            
            public LayoutConfigurationBuilder statusBoxSpacing(int spacing) {
                config.statusBoxSpacing = spacing;
                return this;
            }
            
            public LayoutConfigurationBuilder taskPaneHgap(int gap) {
                config.taskPaneHgap = gap;
                return this;
            }
            
            public LayoutConfigurationBuilder taskPaneVgap(int gap) {
                config.taskPaneVgap = gap;
                return this;
            }
            
            public LayoutConfigurationBuilder taskPanePadding(Insets padding) {
                config.taskPanePadding = padding;
                return this;
            }
            
            public LayoutConfiguration build() {
                return config;
            }
        }
    }
    
    /**
     * Sets the configuration.
     */
    public void setConfiguration(LayoutConfiguration configuration) {
        this.configuration = configuration;
    }
    
    /**
     * Creates a button group with consistent spacing.
     */
    public HBox createButtonGroup(Node... nodes) {
        HBox group = new HBox(configuration.controlBarSpacing);
        group.setAlignment(Pos.CENTER_LEFT);
        group.getChildren().addAll(nodes);
        return group;
    }
    
    /**
     * Creates a separator for the action bar.
     */
    public Separator createSeparator() {
        return new Separator(Orientation.VERTICAL);
    }
    
    /**
     * Creates a spacer region.
     */
    public Region createSpacer() {
        Region spacer = new Region();
        HBox.setHgrow(spacer, Priority.ALWAYS);
        return spacer;
    }
    
    /**
     * Creates project info section.
     */
    public ProjectInfoSection createProjectInfo() {
        Label projectTextLabel = new Label("Project:");
        
        Label projectLabel = new Label("No project loaded");
        projectLabel.getStyleClass().add("project-label");
        projectLabel.setMinWidth(200);
        
        HBox projectInfo = createButtonGroup(projectTextLabel, projectLabel);
        
        return new ProjectInfoSection(projectInfo, projectLabel);
    }
    
    /**
     * Creates execution control buttons.
     */
    public ExecutionControls createExecutionControls() {
        Button startButton = new Button("Start");
        startButton.getStyleClass().addAll("button", configuration.primaryButtonClass, "control-button");
        
        Button pauseButton = new Button("Pause");
        pauseButton.getStyleClass().addAll("button", configuration.secondaryButtonClass, "control-button");
        pauseButton.setDisable(true);
        
        Button stopButton = new Button("Stop");
        stopButton.getStyleClass().addAll("button", configuration.dangerButtonClass, "control-button");
        stopButton.setDisable(true);
        
        HBox controls = createButtonGroup(startButton, pauseButton, stopButton);
        
        return new ExecutionControls(controls, startButton, pauseButton, stopButton);
    }
    
    /**
     * Creates window control section.
     */
    public WindowControls createWindowControls() {
        Button windowControlButton = new Button("Window");
        windowControlButton.getStyleClass().addAll("button", configuration.secondaryButtonClass, "control-button");
        
        Button hotkeyButton = new Button("Hotkeys");
        hotkeyButton.getStyleClass().addAll("button", configuration.secondaryButtonClass, "control-button");
        
        HBox controls = createButtonGroup(windowControlButton, hotkeyButton);
        
        return new WindowControls(controls, windowControlButton, hotkeyButton);
    }
    
    /**
     * Creates status section.
     */
    public StatusSection createStatusSection() {
        VBox statusBox = new VBox(configuration.statusBoxSpacing);
        statusBox.setAlignment(Pos.CENTER_LEFT);
        
        Label statusLabel = new Label("Ready");
        statusLabel.getStyleClass().add("status-label");
        
        ProgressBar progressBar = new ProgressBar(0);
        progressBar.setPrefWidth(200);
        progressBar.getStyleClass().add("progress-bar");
        
        statusBox.getChildren().addAll(statusLabel, progressBar);
        
        return new StatusSection(statusBox, statusLabel, progressBar);
    }
    
    /**
     * Creates task panel with scroll.
     */
    public TaskPanel createTaskPanel() {
        AtlantaCard tasksCard = new AtlantaCard("Automation Tasks");
        
        ScrollPane taskScroll = new ScrollPane();
        taskScroll.setFitToWidth(true);
        taskScroll.setHbarPolicy(ScrollPane.ScrollBarPolicy.NEVER);
        
        FlowPane taskButtonsPane = new FlowPane();
        taskButtonsPane.setHgap(configuration.taskPaneHgap);
        taskButtonsPane.setVgap(configuration.taskPaneVgap);
        taskButtonsPane.setPadding(configuration.taskPanePadding);
        taskButtonsPane.getStyleClass().add("task-buttons-pane");
        
        taskScroll.setContent(taskButtonsPane);
        tasksCard.setContent(taskScroll);
        
        return new TaskPanel(tasksCard, taskScroll, taskButtonsPane);
    }
    
    /**
     * Creates log panel.
     */
    public LogPanel createLogPanel() {
        AtlantaCard logCard = new AtlantaCard("Execution Log");
        logCard.setExpand(true);
        
        VBox logContent = new VBox(8);
        
        // Log controls
        HBox logControls = new HBox(configuration.logControlSpacing);
        logControls.setAlignment(Pos.CENTER_RIGHT);
        
        CheckBox autoScrollCheck = new CheckBox("Auto-scroll");
        autoScrollCheck.setSelected(true);
        autoScrollCheck.getStyleClass().add("auto-scroll-check");
        
        Button clearLogButton = new Button("Clear");
        clearLogButton.getStyleClass().addAll("button", configuration.secondaryButtonClass, "small");
        
        logControls.getChildren().addAll(autoScrollCheck, clearLogButton);
        
        // Log area
        TextArea logArea = new TextArea();
        logArea.setEditable(false);
        logArea.getStyleClass().add("log-area");
        logArea.setWrapText(true);
        VBox.setVgrow(logArea, Priority.ALWAYS);
        
        logContent.getChildren().addAll(logControls, logArea);
        VBox.setVgrow(logArea, Priority.ALWAYS);
        
        logCard.setContent(logContent);
        
        // Set up auto-scroll behavior
        logArea.textProperty().addListener((obs, oldText, newText) -> {
            if (autoScrollCheck.isSelected()) {
                logArea.setScrollTop(Double.MAX_VALUE);
            }
        });
        
        return new LogPanel(logCard, logContent, logArea, autoScrollCheck, clearLogButton);
    }
    
    /**
     * Creates responsive split layout.
     */
    public SplitPane createResponsiveSplitLayout(Region left, Region right) {
        SplitPane splitPane = new SplitPane();
        splitPane.getItems().addAll(left, right);
        splitPane.setDividerPositions(configuration.splitPanePosition);
        
        // Ensure minimum sizes
        SplitPane.setResizableWithParent(left, Boolean.FALSE);
        left.setMinWidth(300);
        right.setMinWidth(400);
        
        return splitPane;
    }
    
    // Component containers
    
    public static class ProjectInfoSection {
        private final HBox container;
        private final Label projectLabel;
        
        public ProjectInfoSection(HBox container, Label projectLabel) {
            this.container = container;
            this.projectLabel = projectLabel;
        }
        
        public HBox getContainer() { return container; }
        public Label getProjectLabel() { return projectLabel; }
    }
    
    public static class ExecutionControls {
        private final HBox container;
        private final Button startButton;
        private final Button pauseButton;
        private final Button stopButton;
        
        public ExecutionControls(HBox container, Button startButton, Button pauseButton, Button stopButton) {
            this.container = container;
            this.startButton = startButton;
            this.pauseButton = pauseButton;
            this.stopButton = stopButton;
        }
        
        public HBox getContainer() { return container; }
        public Button getStartButton() { return startButton; }
        public Button getPauseButton() { return pauseButton; }
        public Button getStopButton() { return stopButton; }
    }
    
    public static class WindowControls {
        private final HBox container;
        private final Button windowControlButton;
        private final Button hotkeyButton;
        
        public WindowControls(HBox container, Button windowControlButton, Button hotkeyButton) {
            this.container = container;
            this.windowControlButton = windowControlButton;
            this.hotkeyButton = hotkeyButton;
        }
        
        public HBox getContainer() { return container; }
        public Button getWindowControlButton() { return windowControlButton; }
        public Button getHotkeyButton() { return hotkeyButton; }
    }
    
    public static class StatusSection {
        private final VBox container;
        private final Label statusLabel;
        private final ProgressBar progressBar;
        
        public StatusSection(VBox container, Label statusLabel, ProgressBar progressBar) {
            this.container = container;
            this.statusLabel = statusLabel;
            this.progressBar = progressBar;
        }
        
        public VBox getContainer() { return container; }
        public Label getStatusLabel() { return statusLabel; }
        public ProgressBar getProgressBar() { return progressBar; }
    }
    
    public static class TaskPanel {
        private final AtlantaCard card;
        private final ScrollPane scrollPane;
        private final FlowPane taskButtonsPane;
        
        public TaskPanel(AtlantaCard card, ScrollPane scrollPane, FlowPane taskButtonsPane) {
            this.card = card;
            this.scrollPane = scrollPane;
            this.taskButtonsPane = taskButtonsPane;
        }
        
        public AtlantaCard getCard() { return card; }
        public ScrollPane getScrollPane() { return scrollPane; }
        public FlowPane getTaskButtonsPane() { return taskButtonsPane; }
    }
    
    public static class LogPanel {
        private final AtlantaCard card;
        private final VBox content;
        private final TextArea logArea;
        private final CheckBox autoScrollCheck;
        private final Button clearButton;
        
        public LogPanel(AtlantaCard card, VBox content, TextArea logArea, 
                       CheckBox autoScrollCheck, Button clearButton) {
            this.card = card;
            this.content = content;
            this.logArea = logArea;
            this.autoScrollCheck = autoScrollCheck;
            this.clearButton = clearButton;
        }
        
        public AtlantaCard getCard() { return card; }
        public VBox getContent() { return content; }
        public TextArea getLogArea() { return logArea; }
        public CheckBox getAutoScrollCheck() { return autoScrollCheck; }
        public Button getClearButton() { return clearButton; }
    }
}