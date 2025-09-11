package io.github.jspinak.brobot.runner.ui.enhanced.services;

import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import javafx.scene.paint.Color;

import org.springframework.stereotype.Service;

import io.github.jspinak.brobot.runner.hotkeys.HotkeyManager;
import io.github.jspinak.brobot.runner.ui.AutomationStatusPanel;

import lombok.Getter;
import lombok.Setter;

/**
 * Factory service for creating UI components for the enhanced automation panel. Centralizes UI
 * creation and styling.
 */
@Service
public class EnhancedUIFactory {

    @Getter @Setter private UIConfiguration configuration;

    /** Configuration for UI creation. */
    @Getter
    @Setter
    public static class UIConfiguration {
        private double panelPadding;
        private double panelSpacing;
        private double controlBarSpacing;
        private double settingsBarSpacing;
        private double buttonPanePadding;
        private double buttonPaneHgap;
        private double buttonPaneVgap;
        private double logAreaHeight;

        // Style classes
        private String panelStyleClass;
        private String controlBarStyleClass;
        private String buttonPaneStyleClass;
        private String sectionLabelStyleClass;
        private String categoryLabelStyleClass;
        private String dangerButtonStyleClass;

        // Border styles
        private BorderStroke buttonPaneBorderStroke;

        public static UIConfigurationBuilder builder() {
            return new UIConfigurationBuilder();
        }

        public static class UIConfigurationBuilder {
            private double panelPadding = 20;
            private double panelSpacing = 10;
            private double controlBarSpacing = 10;
            private double settingsBarSpacing = 15;
            private double buttonPanePadding = 10;
            private double buttonPaneHgap = 10;
            private double buttonPaneVgap = 10;
            private double logAreaHeight = 200;

            private String panelStyleClass = "enhanced-automation-panel";
            private String controlBarStyleClass = "control-bar";
            private String buttonPaneStyleClass = "button-pane";
            private String sectionLabelStyleClass = "section-label";
            private String categoryLabelStyleClass = "category-label";
            private String dangerButtonStyleClass = "button-danger";

            private BorderStroke buttonPaneBorderStroke =
                    new BorderStroke(
                            Color.LIGHTGRAY,
                            BorderStrokeStyle.SOLID,
                            new CornerRadii(5),
                            BorderWidths.DEFAULT);

            public UIConfigurationBuilder panelPadding(double padding) {
                this.panelPadding = padding;
                return this;
            }

            public UIConfigurationBuilder panelSpacing(double spacing) {
                this.panelSpacing = spacing;
                return this;
            }

            public UIConfigurationBuilder controlBarSpacing(double spacing) {
                this.controlBarSpacing = spacing;
                return this;
            }

            public UIConfigurationBuilder settingsBarSpacing(double spacing) {
                this.settingsBarSpacing = spacing;
                return this;
            }

            public UIConfigurationBuilder buttonPanePadding(double padding) {
                this.buttonPanePadding = padding;
                return this;
            }

            public UIConfigurationBuilder buttonPaneHgap(double gap) {
                this.buttonPaneHgap = gap;
                return this;
            }

            public UIConfigurationBuilder buttonPaneVgap(double gap) {
                this.buttonPaneVgap = gap;
                return this;
            }

            public UIConfigurationBuilder logAreaHeight(double height) {
                this.logAreaHeight = height;
                return this;
            }

            public UIConfigurationBuilder panelStyleClass(String styleClass) {
                this.panelStyleClass = styleClass;
                return this;
            }

            public UIConfigurationBuilder controlBarStyleClass(String styleClass) {
                this.controlBarStyleClass = styleClass;
                return this;
            }

            public UIConfigurationBuilder buttonPaneStyleClass(String styleClass) {
                this.buttonPaneStyleClass = styleClass;
                return this;
            }

            public UIConfigurationBuilder sectionLabelStyleClass(String styleClass) {
                this.sectionLabelStyleClass = styleClass;
                return this;
            }

            public UIConfigurationBuilder categoryLabelStyleClass(String styleClass) {
                this.categoryLabelStyleClass = styleClass;
                return this;
            }

            public UIConfigurationBuilder dangerButtonStyleClass(String styleClass) {
                this.dangerButtonStyleClass = styleClass;
                return this;
            }

            public UIConfigurationBuilder buttonPaneBorderStroke(BorderStroke stroke) {
                this.buttonPaneBorderStroke = stroke;
                return this;
            }

            public UIConfiguration build() {
                UIConfiguration config = new UIConfiguration();
                config.panelPadding = panelPadding;
                config.panelSpacing = panelSpacing;
                config.controlBarSpacing = controlBarSpacing;
                config.settingsBarSpacing = settingsBarSpacing;
                config.buttonPanePadding = buttonPanePadding;
                config.buttonPaneHgap = buttonPaneHgap;
                config.buttonPaneVgap = buttonPaneVgap;
                config.logAreaHeight = logAreaHeight;
                config.panelStyleClass = panelStyleClass;
                config.controlBarStyleClass = controlBarStyleClass;
                config.buttonPaneStyleClass = buttonPaneStyleClass;
                config.sectionLabelStyleClass = sectionLabelStyleClass;
                config.categoryLabelStyleClass = categoryLabelStyleClass;
                config.dangerButtonStyleClass = dangerButtonStyleClass;
                config.buttonPaneBorderStroke = buttonPaneBorderStroke;
                return config;
            }
        }
    }

    /** Container for control bar components. */
    @Getter
    public static class ControlBarSection {
        private final HBox container;
        private final Button refreshButton;
        private final Button pauseResumeButton;
        private final Button stopButton;

        private ControlBarSection(
                HBox container, Button refreshButton, Button pauseResumeButton, Button stopButton) {
            this.container = container;
            this.refreshButton = refreshButton;
            this.pauseResumeButton = pauseResumeButton;
            this.stopButton = stopButton;
        }
    }

    /** Container for settings bar components. */
    @Getter
    public static class SettingsBarSection {
        private final HBox container;
        private final Button configureHotkeysButton;
        private final CheckBox autoMinimizeCheckbox;

        private SettingsBarSection(
                HBox container, Button configureHotkeysButton, CheckBox autoMinimizeCheckbox) {
            this.container = container;
            this.configureHotkeysButton = configureHotkeysButton;
            this.autoMinimizeCheckbox = autoMinimizeCheckbox;
        }
    }

    /** Container for all assembled UI components. */
    @Getter
    public static class AssembledUI {
        private final VBox mainPanel;
        private final AutomationStatusPanel statusPanel;
        private final ControlBarSection controlBar;
        private final SettingsBarSection settingsBar;
        private final FlowPane buttonPane;
        private final ScrollPane buttonScrollPane;
        private final TextArea logArea;

        private AssembledUI(
                VBox mainPanel,
                AutomationStatusPanel statusPanel,
                ControlBarSection controlBar,
                SettingsBarSection settingsBar,
                FlowPane buttonPane,
                ScrollPane buttonScrollPane,
                TextArea logArea) {
            this.mainPanel = mainPanel;
            this.statusPanel = statusPanel;
            this.controlBar = controlBar;
            this.settingsBar = settingsBar;
            this.buttonPane = buttonPane;
            this.buttonScrollPane = buttonScrollPane;
            this.logArea = logArea;
        }
    }

    public EnhancedUIFactory() {
        this.configuration = UIConfiguration.builder().build();
    }

    /**
     * Creates the main panel container.
     *
     * @return The main panel
     */
    public VBox createMainPanel() {
        VBox panel = new VBox();
        panel.setPadding(new Insets(configuration.panelPadding));
        panel.setSpacing(configuration.panelSpacing);
        panel.getStyleClass().add(configuration.panelStyleClass);
        return panel;
    }

    /**
     * Creates the status panel.
     *
     * @param hotkeyManager The hotkey manager
     * @return The status panel
     */
    public AutomationStatusPanel createStatusPanel(HotkeyManager hotkeyManager) {
        return new AutomationStatusPanel(hotkeyManager);
    }

    /**
     * Creates the control bar section.
     *
     * @return The control bar section
     */
    public ControlBarSection createControlBar() {
        HBox controlBar = new HBox(configuration.controlBarSpacing);
        controlBar.getStyleClass().add(configuration.controlBarStyleClass);

        Button refreshButton = new Button("Refresh Functions");
        refreshButton.setId("refreshAutomationButtons");

        Button pauseResumeButton = new Button("Pause");
        pauseResumeButton.setId("pauseResumeExecution");
        pauseResumeButton.setDisable(true);

        Button stopButton = new Button("Stop All");
        stopButton.setId("stopAllAutomation");
        stopButton.getStyleClass().add(configuration.dangerButtonStyleClass);

        controlBar.getChildren().addAll(refreshButton, pauseResumeButton, stopButton);

        return new ControlBarSection(controlBar, refreshButton, pauseResumeButton, stopButton);
    }

    /**
     * Creates the settings bar section.
     *
     * @return The settings bar section
     */
    public SettingsBarSection createSettingsBar() {
        HBox settingsBar = new HBox(configuration.settingsBarSpacing);
        settingsBar.setAlignment(Pos.CENTER_LEFT);

        Button configureHotkeysButton = new Button("Configure Hotkeys");
        CheckBox autoMinimizeCheckbox = new CheckBox("Auto-minimize on start");

        settingsBar.getChildren().addAll(configureHotkeysButton, autoMinimizeCheckbox);

        return new SettingsBarSection(settingsBar, configureHotkeysButton, autoMinimizeCheckbox);
    }

    /**
     * Creates the button pane.
     *
     * @return The button pane
     */
    public FlowPane createButtonPane() {
        FlowPane buttonPane = new FlowPane();
        buttonPane.getStyleClass().add(configuration.buttonPaneStyleClass);
        buttonPane.setPadding(new Insets(configuration.buttonPanePadding));
        buttonPane.setHgap(configuration.buttonPaneHgap);
        buttonPane.setVgap(configuration.buttonPaneVgap);

        if (configuration.buttonPaneBorderStroke != null) {
            buttonPane.setBorder(new Border(configuration.buttonPaneBorderStroke));
        }

        return buttonPane;
    }

    /**
     * Creates the button scroll pane.
     *
     * @param buttonPane The button pane to wrap
     * @return The scroll pane
     */
    public ScrollPane createButtonScrollPane(FlowPane buttonPane) {
        ScrollPane scrollPane = new ScrollPane(buttonPane);
        scrollPane.setFitToWidth(true);
        scrollPane.setPrefHeight(150);
        return scrollPane;
    }

    /**
     * Creates the log area.
     *
     * @return The log area
     */
    public TextArea createLogArea() {
        TextArea logArea = new TextArea();
        logArea.setEditable(false);
        logArea.setWrapText(true);
        logArea.setPrefHeight(configuration.logAreaHeight);
        return logArea;
    }

    /**
     * Creates a section label.
     *
     * @param text The label text
     * @return The label
     */
    public Label createSectionLabel(String text) {
        Label label = new Label(text);
        label.getStyleClass().add(configuration.sectionLabelStyleClass);
        return label;
    }

    /**
     * Creates a separator.
     *
     * @return The separator
     */
    public Separator createSeparator() {
        return new Separator();
    }

    /**
     * Assembles all UI components.
     *
     * @param mainPanel The main panel
     * @param statusPanel The status panel
     * @param controlBar The control bar
     * @param settingsBar The settings bar
     * @param buttonPane The button pane
     * @param buttonScrollPane The button scroll pane
     * @param logArea The log area
     * @return The assembled UI
     */
    public AssembledUI assembleUI(
            VBox mainPanel,
            AutomationStatusPanel statusPanel,
            ControlBarSection controlBar,
            SettingsBarSection settingsBar,
            FlowPane buttonPane,
            ScrollPane buttonScrollPane,
            TextArea logArea) {

        mainPanel.getChildren().clear();
        mainPanel
                .getChildren()
                .addAll(
                        statusPanel,
                        createSeparator(),
                        controlBar.getContainer(),
                        settingsBar.getContainer(),
                        createSeparator(),
                        createSectionLabel("Automation Functions:"),
                        buttonScrollPane,
                        createSectionLabel("Log:"),
                        logArea);

        return new AssembledUI(
                mainPanel,
                statusPanel,
                controlBar,
                settingsBar,
                buttonPane,
                buttonScrollPane,
                logArea);
    }
}
