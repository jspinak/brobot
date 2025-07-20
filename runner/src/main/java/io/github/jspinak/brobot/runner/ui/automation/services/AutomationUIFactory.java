package io.github.jspinak.brobot.runner.ui.automation.services;

import io.github.jspinak.brobot.runner.project.TaskButton;
import io.github.jspinak.brobot.runner.ui.components.base.AtlantaCard;
import io.github.jspinak.brobot.runner.ui.icons.IconRegistry;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.*;
import javafx.scene.image.ImageView;
import javafx.scene.layout.*;
import javafx.scene.paint.Color;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.function.Consumer;

/**
 * Factory service for creating automation UI components.
 * Centralizes UI creation logic with consistent styling.
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class AutomationUIFactory {
    
    private final IconRegistry iconRegistry;
    
    /**
     * Configuration for UI components.
     */
    public static class UIConfiguration {
        private String primaryButtonClass = "primary";
        private String secondaryButtonClass = "secondary";
        private String dangerButtonClass = "danger";
        private int iconSize = 16;
        private int controlBarSpacing = 8;
        private int mainContentSpacing = 24;
        private int taskButtonSpacing = 12;
        
        public static UIConfigurationBuilder builder() {
            return new UIConfigurationBuilder();
        }
        
        public static class UIConfigurationBuilder {
            private UIConfiguration config = new UIConfiguration();
            
            public UIConfigurationBuilder primaryButtonClass(String className) {
                config.primaryButtonClass = className;
                return this;
            }
            
            public UIConfigurationBuilder secondaryButtonClass(String className) {
                config.secondaryButtonClass = className;
                return this;
            }
            
            public UIConfigurationBuilder dangerButtonClass(String className) {
                config.dangerButtonClass = className;
                return this;
            }
            
            public UIConfigurationBuilder iconSize(int size) {
                config.iconSize = size;
                return this;
            }
            
            public UIConfiguration build() {
                return config;
            }
        }
    }
    
    private UIConfiguration configuration = UIConfiguration.builder().build();
    
    /**
     * Sets the UI configuration.
     */
    public void setConfiguration(UIConfiguration configuration) {
        this.configuration = configuration;
    }
    
    /**
     * Creates an icon button.
     */
    public Button createIconButton(String text, String iconName, String... styleClasses) {
        Button button = new Button(text);
        
        ImageView icon = iconRegistry.getIconView(iconName, configuration.iconSize);
        if (icon != null) {
            button.setGraphic(icon);
        }
        
        button.getStyleClass().add("button");
        button.getStyleClass().addAll(styleClasses);
        
        return button;
    }
    
    /**
     * Creates the execution control buttons.
     */
    public ExecutionControls createExecutionControls() {
        Button startButton = createIconButton("Start", "play", configuration.primaryButtonClass);
        Button pauseButton = createIconButton("Pause", "pause", configuration.secondaryButtonClass);
        Button stopButton = createIconButton("Stop", "stop", configuration.dangerButtonClass);
        
        pauseButton.setDisable(true);
        stopButton.setDisable(true);
        
        return new ExecutionControls(startButton, pauseButton, stopButton);
    }
    
    /**
     * Data class for execution controls.
     */
    public static class ExecutionControls {
        private final Button startButton;
        private final Button pauseButton;
        private final Button stopButton;
        
        public ExecutionControls(Button startButton, Button pauseButton, Button stopButton) {
            this.startButton = startButton;
            this.pauseButton = pauseButton;
            this.stopButton = stopButton;
        }
        
        public Button getStartButton() { return startButton; }
        public Button getPauseButton() { return pauseButton; }
        public Button getStopButton() { return stopButton; }
    }
    
    /**
     * Creates a control bar.
     */
    public HBox createControlBar(
            Label projectLabel,
            ExecutionControls controls,
            Button windowControlButton,
            Button hotkeyButton,
            VBox statusBox) {
        
        HBox controlBar = new HBox(configuration.controlBarSpacing);
        controlBar.getStyleClass().add("action-bar");
        controlBar.setAlignment(Pos.CENTER_LEFT);
        
        Region spacer = new Region();
        HBox.setHgrow(spacer, Priority.ALWAYS);
        
        controlBar.getChildren().addAll(
            projectLabel,
            new Separator(javafx.geometry.Orientation.VERTICAL),
            controls.getStartButton(), controls.getPauseButton(), controls.getStopButton(),
            new Separator(javafx.geometry.Orientation.VERTICAL),
            windowControlButton, hotkeyButton,
            spacer,
            statusBox
        );
        
        return controlBar;
    }
    
    /**
     * Creates the project label.
     */
    public Label createProjectLabel() {
        Label label = new Label("No project loaded");
        label.getStyleClass().add("project-label");
        return label;
    }
    
    /**
     * Creates the status box with label and progress bar.
     */
    public StatusBox createStatusBox() {
        VBox statusBox = new VBox(4);
        statusBox.setAlignment(Pos.CENTER_RIGHT);
        
        Label statusLabel = new Label("Ready");
        statusLabel.getStyleClass().add("status-label");
        
        ProgressBar progressBar = new ProgressBar(0);
        progressBar.setPrefWidth(200);
        progressBar.getStyleClass().add("progress-bar");
        
        statusBox.getChildren().addAll(statusLabel, progressBar);
        
        return new StatusBox(statusBox, statusLabel, progressBar);
    }
    
    /**
     * Data class for status box components.
     */
    public static class StatusBox {
        private final VBox container;
        private final Label statusLabel;
        private final ProgressBar progressBar;
        
        public StatusBox(VBox container, Label statusLabel, ProgressBar progressBar) {
            this.container = container;
            this.statusLabel = statusLabel;
            this.progressBar = progressBar;
        }
        
        public VBox getContainer() { return container; }
        public Label getStatusLabel() { return statusLabel; }
        public ProgressBar getProgressBar() { return progressBar; }
    }
    
    /**
     * Creates a task card.
     */
    public AtlantaCard createTaskCard() {
        AtlantaCard card = new AtlantaCard("Automation Tasks");
        card.setMinWidth(400);
        card.setPrefWidth(500);
        return card;
    }
    
    /**
     * Creates the task buttons pane.
     */
    public FlowPane createTaskButtonsPane() {
        FlowPane pane = new FlowPane();
        pane.setHgap(configuration.taskButtonSpacing);
        pane.setVgap(configuration.taskButtonSpacing);
        pane.setPadding(new Insets(8));
        pane.getStyleClass().add("task-buttons-pane");
        return pane;
    }
    
    /**
     * Creates a log card with controls.
     */
    public LogCard createLogCard() {
        AtlantaCard card = new AtlantaCard("Execution Log");
        card.setExpand(true);
        
        VBox logContent = new VBox(8);
        
        // Log controls
        HBox logControls = new HBox(8);
        logControls.setAlignment(Pos.CENTER_RIGHT);
        
        CheckBox autoScrollCheck = new CheckBox("Auto-scroll");
        autoScrollCheck.setSelected(true);
        
        Button clearLogButton = new Button("Clear");
        clearLogButton.getStyleClass().addAll("button", "secondary", "small");
        
        logControls.getChildren().addAll(autoScrollCheck, clearLogButton);
        
        // Log area
        TextArea logArea = new TextArea();
        logArea.setEditable(false);
        logArea.getStyleClass().add("log-area");
        logArea.setWrapText(true);
        VBox.setVgrow(logArea, Priority.ALWAYS);
        
        // Auto-scroll behavior
        logArea.textProperty().addListener((obs, oldText, newText) -> {
            if (autoScrollCheck.isSelected()) {
                logArea.setScrollTop(Double.MAX_VALUE);
            }
        });
        
        clearLogButton.setOnAction(e -> logArea.clear());
        
        logContent.getChildren().addAll(logControls, logArea);
        VBox.setVgrow(logArea, Priority.ALWAYS);
        
        card.setContent(logContent);
        
        return new LogCard(card, logArea, autoScrollCheck);
    }
    
    /**
     * Data class for log card components.
     */
    public static class LogCard {
        private final AtlantaCard card;
        private final TextArea logArea;
        private final CheckBox autoScrollCheck;
        
        public LogCard(AtlantaCard card, TextArea logArea, CheckBox autoScrollCheck) {
            this.card = card;
            this.logArea = logArea;
            this.autoScrollCheck = autoScrollCheck;
        }
        
        public AtlantaCard getCard() { return card; }
        public TextArea getLogArea() { return logArea; }
        public CheckBox getAutoScrollCheck() { return autoScrollCheck; }
    }
    
    /**
     * Creates a task button from TaskButton definition.
     */
    public Button createTaskButton(TaskButton taskButton, Consumer<TaskButton> onAction) {
        String buttonText = taskButton.getLabel() != null ? taskButton.getLabel() : taskButton.getId();
        Button button = new Button(buttonText);
        button.getStyleClass().addAll("button", "task-button");
        
        // Apply custom styling
        applyTaskButtonStyling(button, taskButton.getStyling());
        
        // Set tooltip
        if (taskButton.getTooltip() != null && !taskButton.getTooltip().isEmpty()) {
            button.setTooltip(new Tooltip(taskButton.getTooltip()));
        }
        
        // Set action
        button.setOnAction(e -> onAction.accept(taskButton));
        
        return button;
    }
    
    /**
     * Applies custom styling to a task button.
     */
    private void applyTaskButtonStyling(Button button, TaskButton.ButtonStyling styling) {
        if (styling == null) {
            return;
        }
        
        StringBuilder style = new StringBuilder();
        
        // Background color
        if (styling.getBackgroundColor() != null && !styling.getBackgroundColor().isEmpty()) {
            try {
                Color color = Color.web(styling.getBackgroundColor());
                String rgb = String.format("rgb(%d,%d,%d)", 
                    (int)(color.getRed() * 255),
                    (int)(color.getGreen() * 255),
                    (int)(color.getBlue() * 255));
                style.append("-fx-background-color: ").append(rgb).append(";");
            } catch (Exception e) {
                log.warn("Invalid background color: {}", styling.getBackgroundColor());
            }
        }
        
        // Text color
        if (styling.getTextColor() != null && !styling.getTextColor().isEmpty()) {
            style.append(" -fx-text-fill: ").append(styling.getTextColor()).append(";");
        }
        
        // Apply inline styles
        if (style.length() > 0) {
            button.setStyle(style.toString());
        }
        
        // Custom CSS class
        if (styling.getCustomClass() != null) {
            button.getStyleClass().add(styling.getCustomClass());
        }
    }
    
    /**
     * Creates a task category box.
     */
    public VBox createTaskCategoryBox(String categoryName) {
        VBox categoryBox = new VBox(8);
        categoryBox.getStyleClass().add("task-category");
        
        Label categoryLabel = new Label(categoryName);
        categoryLabel.getStyleClass().add("category-label");
        
        FlowPane categoryButtons = new FlowPane();
        categoryButtons.setHgap(8);
        categoryButtons.setVgap(8);
        
        categoryBox.getChildren().addAll(categoryLabel, categoryButtons);
        
        return categoryBox;
    }
    
    /**
     * Creates an empty state message.
     */
    public Label createEmptyStateLabel(String message) {
        Label label = new Label(message);
        label.getStyleClass().add("empty-state-title");
        return label;
    }
    
    /**
     * Creates the main content layout.
     */
    public HBox createMainContentLayout(AtlantaCard tasksCard, AtlantaCard logCard) {
        HBox content = new HBox(configuration.mainContentSpacing);
        content.getStyleClass().add("split-layout");
        
        content.getChildren().addAll(tasksCard, logCard);
        HBox.setHgrow(logCard, Priority.ALWAYS);
        
        return content;
    }
    
    /**
     * Creates additional control buttons.
     */
    public Button createWindowControlButton() {
        return createIconButton("Window", "window", configuration.secondaryButtonClass);
    }
    
    public Button createHotkeyButton() {
        return createIconButton("Hotkeys", "settings", configuration.secondaryButtonClass);
    }
}