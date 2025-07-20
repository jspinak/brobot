package io.github.jspinak.brobot.runner.ui.enhanced.services;

import io.github.jspinak.brobot.runner.project.AutomationProject;
import io.github.jspinak.brobot.runner.project.AutomationProjectManager;
import io.github.jspinak.brobot.runner.project.RunnerInterface;
import io.github.jspinak.brobot.runner.project.TaskButton;
import javafx.geometry.Insets;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.Tooltip;
import javafx.scene.layout.*;
import javafx.scene.paint.Color;
import lombok.Getter;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.function.Consumer;

/**
 * Service for managing automation buttons in the enhanced panel.
 * Handles button loading, creation, styling, and organization by category.
 */
@Slf4j
@Service
public class EnhancedButtonService {
    
    private final AutomationProjectManager projectManager;
    
    @Getter
    @Setter
    private ButtonConfiguration configuration;
    
    private Consumer<TaskButton> buttonActionHandler;
    private Consumer<String> logHandler;
    
    /**
     * Result of loading buttons from project.
     */
    @Getter
    public static class ButtonLoadResult {
        private final boolean success;
        private final int buttonCount;
        private final String message;
        private final List<TaskButton> buttons;
        
        private ButtonLoadResult(boolean success, int buttonCount, String message, List<TaskButton> buttons) {
            this.success = success;
            this.buttonCount = buttonCount;
            this.message = message;
            this.buttons = buttons;
        }
        
        public static ButtonLoadResult success(List<TaskButton> buttons) {
            return new ButtonLoadResult(true, buttons.size(), 
                "Found " + buttons.size() + " automation functions.", buttons);
        }
        
        public static ButtonLoadResult failure(String message) {
            return new ButtonLoadResult(false, 0, message, Collections.emptyList());
        }
    }
    
    /**
     * Configuration for button display and behavior.
     */
    @Getter
    @Setter
    public static class ButtonConfiguration {
        private boolean groupByCategory;
        private String defaultCategory;
        private double categorySpacing;
        private Insets categoryPadding;
        private String categoryStyle;
        private String categoryLabelStyle;
        private String defaultButtonFontSize;
        private BorderStroke categoryBorderStroke;
        
        public static ButtonConfigurationBuilder builder() {
            return new ButtonConfigurationBuilder();
        }
        
        public static class ButtonConfigurationBuilder {
            private boolean groupByCategory = true;
            private String defaultCategory = "General";
            private double categorySpacing = 5;
            private Insets categoryPadding = new Insets(5);
            private String categoryStyle = "-fx-border-color: lightgray; -fx-border-radius: 5;";
            private String categoryLabelStyle = "";
            private String defaultButtonFontSize = "12px";
            private BorderStroke categoryBorderStroke = new BorderStroke(
                Color.LIGHTGRAY, BorderStrokeStyle.SOLID, new CornerRadii(5), BorderWidths.DEFAULT
            );
            
            public ButtonConfigurationBuilder groupByCategory(boolean group) {
                this.groupByCategory = group;
                return this;
            }
            
            public ButtonConfigurationBuilder defaultCategory(String category) {
                this.defaultCategory = category;
                return this;
            }
            
            public ButtonConfigurationBuilder categorySpacing(double spacing) {
                this.categorySpacing = spacing;
                return this;
            }
            
            public ButtonConfigurationBuilder categoryPadding(Insets padding) {
                this.categoryPadding = padding;
                return this;
            }
            
            public ButtonConfigurationBuilder categoryStyle(String style) {
                this.categoryStyle = style;
                return this;
            }
            
            public ButtonConfigurationBuilder categoryLabelStyle(String style) {
                this.categoryLabelStyle = style;
                return this;
            }
            
            public ButtonConfigurationBuilder defaultButtonFontSize(String fontSize) {
                this.defaultButtonFontSize = fontSize;
                return this;
            }
            
            public ButtonConfigurationBuilder categoryBorderStroke(BorderStroke stroke) {
                this.categoryBorderStroke = stroke;
                return this;
            }
            
            public ButtonConfiguration build() {
                ButtonConfiguration config = new ButtonConfiguration();
                config.groupByCategory = groupByCategory;
                config.defaultCategory = defaultCategory;
                config.categorySpacing = categorySpacing;
                config.categoryPadding = categoryPadding;
                config.categoryStyle = categoryStyle;
                config.categoryLabelStyle = categoryLabelStyle;
                config.defaultButtonFontSize = defaultButtonFontSize;
                config.categoryBorderStroke = categoryBorderStroke;
                return config;
            }
        }
    }
    
    @Autowired
    public EnhancedButtonService(AutomationProjectManager projectManager) {
        this.projectManager = projectManager;
        this.configuration = ButtonConfiguration.builder().build();
    }
    
    /**
     * Loads buttons from the active project.
     * @return The button load result
     */
    public ButtonLoadResult loadProjectButtons() {
        if (projectManager == null || projectManager.getCurrentProject() == null) {
            return ButtonLoadResult.failure("No project loaded. Please load a configuration first.");
        }
        
        AutomationProject project = projectManager.getCurrentProject();
        RunnerInterface automation = project.getAutomation();
        
        if (automation == null || automation.getButtons() == null || automation.getButtons().isEmpty()) {
            return ButtonLoadResult.failure("No automation buttons defined in the current project.");
        }
        
        List<TaskButton> buttons = new ArrayList<>(automation.getButtons());
        log("Loaded " + buttons.size() + " buttons from project");
        return ButtonLoadResult.success(buttons);
    }
    
    /**
     * Populates the button pane with loaded buttons.
     * @param buttonPane The pane to populate
     * @param buttons The buttons to add
     */
    public void populateButtonPane(FlowPane buttonPane, List<TaskButton> buttons) {
        buttonPane.getChildren().clear();
        
        if (configuration.groupByCategory) {
            Map<String, List<TaskButton>> buttonsByCategory = groupByCategory(buttons);
            
            for (Map.Entry<String, List<TaskButton>> entry : buttonsByCategory.entrySet()) {
                VBox categoryBox = createCategoryBox(entry.getKey(), entry.getValue());
                buttonPane.getChildren().add(categoryBox);
            }
        } else {
            for (TaskButton buttonDef : buttons) {
                Button uiButton = createAutomationButton(buttonDef);
                buttonPane.getChildren().add(uiButton);
            }
        }
    }
    
    /**
     * Updates button states based on running status.
     * @param buttonPane The button pane
     * @param disabled true to disable buttons
     */
    public void updateButtonStates(FlowPane buttonPane, boolean disabled) {
        for (javafx.scene.Node node : buttonPane.getChildren()) {
            if (node instanceof VBox categoryBox) {
                for (javafx.scene.Node child : categoryBox.getChildren()) {
                    if (child instanceof Button) {
                        child.setDisable(disabled);
                    }
                }
            } else if (node instanceof Button) {
                node.setDisable(disabled);
            }
        }
    }
    
    /**
     * Sets the handler for button actions.
     * @param handler The action handler
     */
    public void setButtonActionHandler(Consumer<TaskButton> handler) {
        this.buttonActionHandler = handler;
    }
    
    /**
     * Sets the log handler.
     * @param handler The log handler
     */
    public void setLogHandler(Consumer<String> handler) {
        this.logHandler = handler;
    }
    
    private void log(String message) {
        log.debug(message);
        if (logHandler != null) {
            logHandler.accept(message);
        }
    }
    
    private Map<String, List<TaskButton>> groupByCategory(List<TaskButton> buttons) {
        Map<String, List<TaskButton>> buttonsByCategory = new LinkedHashMap<>();
        
        for (TaskButton buttonDef : buttons) {
            String category = buttonDef.getCategory() != null ? 
                buttonDef.getCategory() : configuration.defaultCategory;
            buttonsByCategory.computeIfAbsent(category, k -> new ArrayList<>()).add(buttonDef);
        }
        
        return buttonsByCategory;
    }
    
    private VBox createCategoryBox(String category, List<TaskButton> buttons) {
        VBox categoryBox = new VBox(configuration.categorySpacing);
        categoryBox.setPadding(configuration.categoryPadding);
        categoryBox.setStyle(configuration.categoryStyle);
        
        if (configuration.categoryBorderStroke != null) {
            categoryBox.setBorder(new Border(configuration.categoryBorderStroke));
        }
        
        Label categoryLabel = new Label(category);
        categoryLabel.getStyleClass().add("category-label");
        if (!configuration.categoryLabelStyle.isEmpty()) {
            categoryLabel.setStyle(configuration.categoryLabelStyle);
        }
        categoryBox.getChildren().add(categoryLabel);
        
        for (TaskButton buttonDef : buttons) {
            Button uiButton = createAutomationButton(buttonDef);
            categoryBox.getChildren().add(uiButton);
        }
        
        return categoryBox;
    }
    
    private Button createAutomationButton(TaskButton buttonDef) {
        Button uiButton = new Button(buttonDef.getLabel());
        
        // Apply styling if defined
        if (buttonDef.getStyling() != null) {
            String styleString = buildButtonStyle(buttonDef.getStyling());
            uiButton.setStyle(styleString);
            
            if (buttonDef.getStyling().getCustomClass() != null) {
                uiButton.getStyleClass().add(buttonDef.getStyling().getCustomClass());
            }
        }
        
        // Set tooltip if defined
        if (buttonDef.getTooltip() != null) {
            uiButton.setTooltip(new Tooltip(buttonDef.getTooltip()));
        }
        
        // Set action
        if (buttonActionHandler != null) {
            uiButton.setOnAction(e -> buttonActionHandler.accept(buttonDef));
        }
        
        return uiButton;
    }
    
    private String buildButtonStyle(TaskButton.ButtonStyling styling) {
        StringBuilder styleString = new StringBuilder();
        
        if (styling.getBackgroundColor() != null) {
            styleString.append("-fx-background-color: ").append(styling.getBackgroundColor()).append("; ");
        }
        
        if (styling.getTextColor() != null) {
            styleString.append("-fx-text-fill: ").append(styling.getTextColor()).append("; ");
        }
        
        if (styling.getSize() != null) {
            String fontSize = mapButtonSize(styling.getSize());
            styleString.append("-fx-font-size: ").append(fontSize).append("; ");
        }
        
        return styleString.toString();
    }
    
    private String mapButtonSize(String size) {
        switch (size.toLowerCase()) {
            case "small":
                return "10px";
            case "large":
                return "14px";
            default:
                return configuration.defaultButtonFontSize;
        }
    }
}