package io.github.jspinak.brobot.runner.ui.automation.factories;

import java.util.HashMap;
import java.util.Map;
import java.util.function.Consumer;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.Tooltip;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.scene.shape.Circle;

import org.springframework.stereotype.Component;

import io.github.jspinak.brobot.runner.ui.automation.models.AutomationButtonConfig;

import atlantafx.base.theme.Styles;
import lombok.extern.slf4j.Slf4j;

/**
 * Factory for creating automation-related buttons with consistent styling and behavior. Follows the
 * Factory pattern to centralize button creation logic.
 */
@Slf4j
@Component
public class AutomationButtonFactory {

    // Style constants
    private static final String AUTOMATION_BUTTON_CLASS = "automation-button";
    private static final String CONTROL_BUTTON_CLASS = "control-button";
    private static final String CATEGORY_BOX_CLASS = "category-box";

    // Icon constants
    private static final String PLAY_ICON = "▶";
    private static final String PAUSE_ICON = "⏸";
    private static final String STOP_ICON = "⏹";
    private static final String RESUME_ICON = "⏵";

    /** Creates an automation button with the specified configuration. */
    public Button createAutomationButton(AutomationButtonConfig config) {
        Button button = new Button(config.getDisplayName());

        // Apply base styling
        button.getStyleClass().addAll(AUTOMATION_BUTTON_CLASS, Styles.BUTTON_OUTLINED);

        // Set width constraints
        double prefWidth = config.getPreferredWidth() > 0 ? config.getPreferredWidth() : 150;
        button.setMinWidth(100);
        button.setPrefWidth(prefWidth);
        button.setMaxWidth(250);
        button.setWrapText(true);

        // Add tooltip if description is provided
        if (config.getDescription() != null && !config.getDescription().isEmpty()) {
            Tooltip tooltip = new Tooltip(config.getDescription());
            tooltip.setWrapText(true);
            tooltip.setMaxWidth(300);
            button.setTooltip(tooltip);
        }

        // Set action handler
        if (config.getOnAction() != null) {
            button.setOnAction(
                    e -> {
                        log.debug("Automation button clicked: {}", config.getName());
                        config.getOnAction().accept(config.getName());
                    });
        }

        // Apply custom style class if provided
        if (config.getStyleClass() != null) {
            button.getStyleClass().add(config.getStyleClass());
        }

        // Add icon if specified
        if (config.getIcon() != null) {
            button.setText(config.getIcon() + " " + config.getDisplayName());
        }

        return button;
    }

    /** Creates a control button (Play, Pause, Stop, etc.). */
    public Button createControlButton(ControlButtonType type, Runnable action) {
        Button button = new Button();
        button.getStyleClass().addAll(CONTROL_BUTTON_CLASS, Styles.BUTTON_ICON);

        switch (type) {
            case PLAY:
                button.setText("Run");
                button.getStyleClass().add(Styles.SUCCESS);
                button.setTooltip(new Tooltip("Run Automation (Ctrl+R)"));
                break;

            case PAUSE:
                button.setText("Pause");
                button.getStyleClass().add(Styles.WARNING);
                button.setTooltip(new Tooltip("Pause Automation (Ctrl+P)"));
                break;

            case RESUME:
                button.setText("Resume");
                button.getStyleClass().add(Styles.SUCCESS);
                button.setTooltip(new Tooltip("Resume Automation"));
                break;

            case STOP:
                button.setText("Stop");
                button.getStyleClass().add(Styles.DANGER);
                button.setTooltip(new Tooltip("Stop All Automation (Ctrl+S)"));
                break;
        }

        button.setOnAction(
                e -> {
                    log.debug("Control button clicked: {}", type);
                    if (action != null) {
                        action.run();
                    }
                });

        return button;
    }

    /** Creates a category box for grouping automation buttons. */
    public VBox createCategoryBox(String categoryName, String description) {
        VBox categoryBox = new VBox(12);
        categoryBox.getStyleClass().add(CATEGORY_BOX_CLASS);
        categoryBox.setPadding(new Insets(16));
        categoryBox.getStyleClass().add(Styles.ELEVATED_1);
        categoryBox.setFillWidth(true);

        // Category header
        HBox header = new HBox(8);
        header.setAlignment(Pos.CENTER_LEFT);

        // Category indicator
        Circle indicator = new Circle(4);
        indicator.setFill(getCategoryColor(categoryName));

        // Category title
        Label titleLabel = new Label(categoryName);
        titleLabel.getStyleClass().addAll(Styles.TEXT_BOLD, Styles.TITLE_4);
        titleLabel.setWrapText(false);
        titleLabel.setMinHeight(24);

        header.getChildren().addAll(indicator, titleLabel);

        // Category description
        if (description != null && !description.isEmpty()) {
            Label descLabel = new Label(description);
            descLabel.getStyleClass().add(Styles.TEXT_MUTED);
            descLabel.setWrapText(true);
            descLabel.setPadding(new Insets(4, 0, 0, 0));
            categoryBox.getChildren().addAll(header, descLabel);
        } else {
            categoryBox.getChildren().add(header);
        }

        return categoryBox;
    }

    /** Creates a button with a loading state. */
    public Button createLoadingButton(String text) {
        Button button = new Button(text);
        button.getStyleClass().addAll(AUTOMATION_BUTTON_CLASS, Styles.BUTTON_OUTLINED);
        button.setDisable(true);

        // Add loading indicator
        button.setGraphic(createLoadingIndicator());

        return button;
    }

    /** Creates a toggle button for pause/resume functionality. */
    public Button createPauseResumeToggleButton(Consumer<Boolean> onToggle) {
        Button button = new Button("Pause");
        button.getStyleClass().addAll(CONTROL_BUTTON_CLASS, Styles.WARNING);
        button.setTooltip(new Tooltip("Pause/Resume Automation"));

        // Track state
        button.setUserData(false); // false = not paused, true = paused

        button.setOnAction(
                e -> {
                    boolean isPaused = !(boolean) button.getUserData();
                    button.setUserData(isPaused);

                    if (isPaused) {
                        button.setText("Resume");
                        button.getStyleClass().remove(Styles.WARNING);
                        button.getStyleClass().add(Styles.SUCCESS);
                    } else {
                        button.setText("Pause");
                        button.getStyleClass().remove(Styles.SUCCESS);
                        button.getStyleClass().add(Styles.WARNING);
                    }

                    if (onToggle != null) {
                        onToggle.accept(isPaused);
                    }
                });

        return button;
    }

    /** Updates a button to show running state. */
    public void setButtonRunningState(Button button, boolean isRunning) {
        if (isRunning) {
            button.getStyleClass().add("running");
            button.setDisable(true);

            // Add pulsing effect through style class
            button.getStyleClass().add(Styles.ACCENT);
        } else {
            button.getStyleClass().removeAll("running", Styles.ACCENT);
            button.setDisable(false);
        }
    }

    /** Updates a button to show error state. */
    public void setButtonErrorState(Button button, boolean hasError, String errorMessage) {
        if (hasError) {
            button.getStyleClass().add(Styles.DANGER);

            if (errorMessage != null) {
                Tooltip errorTooltip = new Tooltip("Error: " + errorMessage);
                errorTooltip.getStyleClass().add(Styles.DANGER);
                button.setTooltip(errorTooltip);
            }
        } else {
            button.getStyleClass().remove(Styles.DANGER);
            // Restore original tooltip if needed
        }
    }

    /** Creates a group of related buttons. */
    public HBox createButtonGroup(Button... buttons) {
        HBox group = new HBox(4);
        group.setAlignment(Pos.CENTER_LEFT);
        group.getChildren().addAll(buttons);

        // Apply button group styling
        for (int i = 0; i < buttons.length; i++) {
            Button button = buttons[i];
            button.getStyleClass().add("button-group-member");

            if (i == 0) {
                button.getStyleClass().add("first");
            }
            if (i == buttons.length - 1) {
                button.getStyleClass().add("last");
            }
        }

        return group;
    }

    /** Creates a loading indicator node. */
    private Circle createLoadingIndicator() {
        Circle circle = new Circle(6);
        circle.getStyleClass().add("loading-indicator");

        // Add spinning animation via style class
        circle.getStyleClass().add("spinner");

        return circle;
    }

    /** Gets a color for a category based on its name. */
    private Color getCategoryColor(String categoryName) {
        // Use a simple hash to generate consistent colors
        int hash = categoryName.hashCode();
        double hue = (hash & 0xFF) * 360.0 / 255.0;
        return Color.hsb(hue, 0.7, 0.8);
    }

    /** Control button types. */
    public enum ControlButtonType {
        PLAY,
        PAUSE,
        RESUME,
        STOP
    }

    /** Creates a batch of automation buttons from configurations. */
    public Map<String, Button> createAutomationButtons(
            Map<String, AutomationButtonConfig> configs) {
        Map<String, Button> buttons = new HashMap<>();

        for (Map.Entry<String, AutomationButtonConfig> entry : configs.entrySet()) {
            Button button = createAutomationButton(entry.getValue());
            buttons.put(entry.getKey(), button);
        }

        return buttons;
    }
}
