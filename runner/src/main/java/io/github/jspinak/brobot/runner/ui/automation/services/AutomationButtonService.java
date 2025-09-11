package io.github.jspinak.brobot.runner.ui.automation.services;

import java.util.*;
import java.util.function.Consumer;
import javafx.geometry.Insets;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.Tooltip;
import javafx.scene.layout.FlowPane;
import javafx.scene.layout.VBox;

import org.springframework.stereotype.Service;

import io.github.jspinak.brobot.runner.project.AutomationProject;
import io.github.jspinak.brobot.runner.project.AutomationProjectManager;
import io.github.jspinak.brobot.runner.project.TaskButton;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

/**
 * Service for managing automation button creation and organization. Handles button creation,
 * styling, and category organization.
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class AutomationButtonService {

    private final AutomationProjectManager projectManager;

    // Configuration
    private ButtonConfiguration configuration = ButtonConfiguration.builder().build();

    // Button action handler
    private Consumer<TaskButton> buttonActionHandler;

    /** Button service configuration. */
    public static class ButtonConfiguration {
        private String defaultCategory = "General";
        private boolean groupByCategory = true;
        private int categorySpacing = 5;
        private Insets categoryPadding = new Insets(5);
        private String categoryStyle = "-fx-border-color: lightgray; -fx-border-radius: 5;";
        private String categoryLabelStyle = "-fx-font-weight: bold;";
        private String defaultButtonFontSize = "12px";

        public static ButtonConfigurationBuilder builder() {
            return new ButtonConfigurationBuilder();
        }

        public static class ButtonConfigurationBuilder {
            private ButtonConfiguration config = new ButtonConfiguration();

            public ButtonConfigurationBuilder defaultCategory(String category) {
                config.defaultCategory = category;
                return this;
            }

            public ButtonConfigurationBuilder groupByCategory(boolean group) {
                config.groupByCategory = group;
                return this;
            }

            public ButtonConfigurationBuilder categorySpacing(int spacing) {
                config.categorySpacing = spacing;
                return this;
            }

            public ButtonConfigurationBuilder categoryPadding(Insets padding) {
                config.categoryPadding = padding;
                return this;
            }

            public ButtonConfigurationBuilder categoryStyle(String style) {
                config.categoryStyle = style;
                return this;
            }

            public ButtonConfigurationBuilder categoryLabelStyle(String style) {
                config.categoryLabelStyle = style;
                return this;
            }

            public ButtonConfigurationBuilder defaultButtonFontSize(String size) {
                config.defaultButtonFontSize = size;
                return this;
            }

            public ButtonConfiguration build() {
                return config;
            }
        }
    }

    /** Sets the configuration. */
    public void setConfiguration(ButtonConfiguration configuration) {
        this.configuration = configuration;
    }

    /** Sets the button action handler. */
    public void setButtonActionHandler(Consumer<TaskButton> handler) {
        this.buttonActionHandler = handler;
    }

    /** Loads and organizes buttons from the current project. */
    public ButtonLoadResult loadProjectButtons() {
        if (projectManager == null || projectManager.getCurrentProject() == null) {
            return new ButtonLoadResult(
                    false, 0, "No project loaded. Please load a configuration first.");
        }

        AutomationProject project = projectManager.getCurrentProject();
        if (project.getAutomation() == null || project.getAutomation().getButtons() == null) {
            return new ButtonLoadResult(
                    false, 0, "No automation buttons defined in the current project.");
        }

        List<TaskButton> buttons = project.getAutomation().getButtons();
        if (buttons.isEmpty()) {
            return new ButtonLoadResult(
                    false, 0, "No automation buttons defined in the current project.");
        }

        return new ButtonLoadResult(
                true,
                buttons.size(),
                "Found " + buttons.size() + " automation functions.",
                buttons);
    }

    /** Creates UI components for buttons. */
    public void populateButtonPane(FlowPane buttonPane, List<TaskButton> buttons) {
        buttonPane.getChildren().clear();

        if (configuration.groupByCategory) {
            Map<String, List<TaskButton>> buttonsByCategory = organizeByCategory(buttons);

            for (Map.Entry<String, List<TaskButton>> entry : buttonsByCategory.entrySet()) {
                VBox categoryBox = createCategoryBox(entry.getKey(), entry.getValue());
                buttonPane.getChildren().add(categoryBox);
            }
        } else {
            // Create buttons without category grouping
            for (TaskButton buttonDef : buttons) {
                Button uiButton = createAutomationButton(buttonDef);
                buttonPane.getChildren().add(uiButton);
            }
        }
    }

    /** Organizes buttons by category. */
    private Map<String, List<TaskButton>> organizeByCategory(List<TaskButton> buttons) {
        Map<String, List<TaskButton>> buttonsByCategory = new LinkedHashMap<>();

        for (TaskButton buttonDef : buttons) {
            String category =
                    buttonDef.getCategory() != null
                            ? buttonDef.getCategory()
                            : configuration.defaultCategory;
            buttonsByCategory.computeIfAbsent(category, k -> new ArrayList<>()).add(buttonDef);
        }

        return buttonsByCategory;
    }

    /** Creates a category box with buttons. */
    private VBox createCategoryBox(String category, List<TaskButton> buttons) {
        VBox categoryBox = new VBox(configuration.categorySpacing);
        categoryBox.setPadding(configuration.categoryPadding);
        categoryBox.setStyle(configuration.categoryStyle);

        Label categoryLabel = new Label(category);
        categoryLabel.getStyleClass().add("category-label");
        categoryLabel.setStyle(configuration.categoryLabelStyle);
        categoryBox.getChildren().add(categoryLabel);

        for (TaskButton buttonDef : buttons) {
            Button uiButton = createAutomationButton(buttonDef);
            categoryBox.getChildren().add(uiButton);
        }

        return categoryBox;
    }

    /** Creates a JavaFX button from a button definition. */
    private Button createAutomationButton(TaskButton buttonDef) {
        Button uiButton = new Button(buttonDef.getLabel());

        // Apply styling if defined
        if (buttonDef.getStyling() != null) {
            applyButtonStyling(uiButton, buttonDef.getStyling());
        }

        // Set tooltip if defined
        if (buttonDef.getTooltip() != null && !buttonDef.getTooltip().isEmpty()) {
            uiButton.setTooltip(new Tooltip(buttonDef.getTooltip()));
        }

        // Set action
        if (buttonActionHandler != null) {
            uiButton.setOnAction(e -> buttonActionHandler.accept(buttonDef));
        }

        return uiButton;
    }

    /** Applies styling to a button. */
    private void applyButtonStyling(Button button, TaskButton.ButtonStyling styling) {
        StringBuilder styleString = new StringBuilder();

        if (styling.getBackgroundColor() != null && !styling.getBackgroundColor().isEmpty()) {
            styleString
                    .append("-fx-background-color: ")
                    .append(styling.getBackgroundColor())
                    .append("; ");
        }

        if (styling.getTextColor() != null && !styling.getTextColor().isEmpty()) {
            styleString.append("-fx-text-fill: ").append(styling.getTextColor()).append("; ");
        }

        String fontSize = configuration.defaultButtonFontSize;
        if (styling.getSize() != null && !styling.getSize().isEmpty()) {
            switch (styling.getSize().toLowerCase()) {
                case "small":
                    fontSize = "10px";
                    break;
                case "large":
                    fontSize = "14px";
                    break;
                default:
                    fontSize = "12px";
            }
        }
        styleString.append("-fx-font-size: ").append(fontSize).append("; ");

        if (styling.getCustomClass() != null && !styling.getCustomClass().isEmpty()) {
            button.getStyleClass().add(styling.getCustomClass());
        }

        if (styleString.length() > 0) {
            button.setStyle(styleString.toString());
        }
    }

    /** Updates button states based on automation running status. */
    public void updateButtonStates(FlowPane buttonPane, boolean running) {
        for (javafx.scene.Node node : buttonPane.getChildren()) {
            if (node instanceof VBox) {
                // This is a category box
                VBox categoryBox = (VBox) node;
                updateCategoryButtons(categoryBox, running);
            } else if (node instanceof Button) {
                // Direct button (no category)
                node.setDisable(running);
            }
        }
    }

    /** Updates buttons within a category. */
    private void updateCategoryButtons(VBox categoryBox, boolean running) {
        for (javafx.scene.Node child : categoryBox.getChildren()) {
            if (child instanceof Button) {
                child.setDisable(running);
            }
        }
    }

    /** Result of loading project buttons. */
    public static class ButtonLoadResult {
        private final boolean success;
        private final int buttonCount;
        private final String message;
        private final List<TaskButton> buttons;

        public ButtonLoadResult(boolean success, int buttonCount, String message) {
            this(success, buttonCount, message, Collections.emptyList());
        }

        public ButtonLoadResult(
                boolean success, int buttonCount, String message, List<TaskButton> buttons) {
            this.success = success;
            this.buttonCount = buttonCount;
            this.message = message;
            this.buttons = buttons;
        }

        public boolean isSuccess() {
            return success;
        }

        public int getButtonCount() {
            return buttonCount;
        }

        public String getMessage() {
            return message;
        }

        public List<TaskButton> getButtons() {
            return buttons;
        }
    }
}
