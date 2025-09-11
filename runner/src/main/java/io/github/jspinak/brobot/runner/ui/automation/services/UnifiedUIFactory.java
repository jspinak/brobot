package io.github.jspinak.brobot.runner.ui.automation.services;

import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import javafx.scene.paint.Color;

import org.springframework.stereotype.Service;

import io.github.jspinak.brobot.runner.ui.AutomationStatusPanel;

import lombok.extern.slf4j.Slf4j;

/**
 * Factory service for creating UI components for the unified automation panel. Centralizes UI
 * creation logic with consistent styling.
 */
@Slf4j
@Service
public class UnifiedUIFactory {

    // Configuration
    private UIConfiguration configuration = UIConfiguration.builder().build();

    /** UI configuration settings. */
    public static class UIConfiguration {
        private String titleText = "Automation Control";
        private int titleFontSize = 18;
        private int controlBarSpacing = 10;
        private int mainSpacing = 10;
        private int sectionPadding = 20;
        private int logAreaHeight = 300;
        private int buttonScrollHeight = 200;
        private String dangerStyleClass = "danger";

        public static UIConfigurationBuilder builder() {
            return new UIConfigurationBuilder();
        }

        public static class UIConfigurationBuilder {
            private UIConfiguration config = new UIConfiguration();

            public UIConfigurationBuilder titleText(String text) {
                config.titleText = text;
                return this;
            }

            public UIConfigurationBuilder titleFontSize(int size) {
                config.titleFontSize = size;
                return this;
            }

            public UIConfigurationBuilder controlBarSpacing(int spacing) {
                config.controlBarSpacing = spacing;
                return this;
            }

            public UIConfigurationBuilder mainSpacing(int spacing) {
                config.mainSpacing = spacing;
                return this;
            }

            public UIConfigurationBuilder sectionPadding(int padding) {
                config.sectionPadding = padding;
                return this;
            }

            public UIConfigurationBuilder logAreaHeight(int height) {
                config.logAreaHeight = height;
                return this;
            }

            public UIConfigurationBuilder buttonScrollHeight(int height) {
                config.buttonScrollHeight = height;
                return this;
            }

            public UIConfiguration build() {
                return config;
            }
        }
    }

    /** Sets the configuration. */
    public void setConfiguration(UIConfiguration configuration) {
        this.configuration = configuration;
    }

    /** Creates the main title label. */
    public Label createTitleLabel() {
        Label titleLabel = new Label(configuration.titleText);
        titleLabel.getStyleClass().add("title-label");
        titleLabel.setStyle(
                String.format(
                        "-fx-font-size: %d; -fx-font-weight: bold;", configuration.titleFontSize));
        return titleLabel;
    }

    /** Creates the control bar with buttons. */
    public ControlBar createControlBar() {
        HBox controlBar = new HBox(configuration.controlBarSpacing);
        controlBar.getStyleClass().add("control-bar");
        controlBar.setAlignment(Pos.CENTER_LEFT);

        // Control buttons
        Button refreshButton = new Button("Refresh");
        refreshButton.setId("refreshAutomationButtons");

        Button pauseResumeButton = new Button("Pause");
        pauseResumeButton.setId("pauseResumeExecution");
        pauseResumeButton.setDisable(true);

        Button stopAllButton = new Button("Stop All");
        stopAllButton.setId("stopAllAutomation");
        stopAllButton.getStyleClass().add(configuration.dangerStyleClass);

        // Separator
        Separator separator = new Separator(javafx.geometry.Orientation.VERTICAL);

        // Hotkey button
        Button configureHotkeysBtn = new Button("⌨ Hotkeys");

        // Auto-minimize checkbox
        CheckBox autoMinimizeCheck = new CheckBox("Auto-minimize");

        // Hotkey info label
        Label hotkeyInfo = new Label("(Ctrl+P: Pause, Ctrl+R: Resume, Ctrl+S: Stop)");
        hotkeyInfo.setStyle("-fx-text-fill: -color-fg-muted; -fx-font-size: 11px;");

        controlBar
                .getChildren()
                .addAll(
                        refreshButton,
                        pauseResumeButton,
                        stopAllButton,
                        separator,
                        configureHotkeysBtn,
                        autoMinimizeCheck,
                        hotkeyInfo);

        return new ControlBar(
                controlBar,
                refreshButton,
                pauseResumeButton,
                stopAllButton,
                configureHotkeysBtn,
                autoMinimizeCheck);
    }

    /** Control bar component container. */
    public static class ControlBar {
        private final HBox container;
        private final Button refreshButton;
        private final Button pauseResumeButton;
        private final Button stopAllButton;
        private final Button configureHotkeysButton;
        private final CheckBox autoMinimizeCheckBox;

        public ControlBar(
                HBox container,
                Button refreshButton,
                Button pauseResumeButton,
                Button stopAllButton,
                Button configureHotkeysButton,
                CheckBox autoMinimizeCheckBox) {
            this.container = container;
            this.refreshButton = refreshButton;
            this.pauseResumeButton = pauseResumeButton;
            this.stopAllButton = stopAllButton;
            this.configureHotkeysButton = configureHotkeysButton;
            this.autoMinimizeCheckBox = autoMinimizeCheckBox;
        }

        public HBox getContainer() {
            return container;
        }

        public Button getRefreshButton() {
            return refreshButton;
        }

        public Button getPauseResumeButton() {
            return pauseResumeButton;
        }

        public Button getStopAllButton() {
            return stopAllButton;
        }

        public Button getConfigureHotkeysButton() {
            return configureHotkeysButton;
        }

        public CheckBox getAutoMinimizeCheckBox() {
            return autoMinimizeCheckBox;
        }
    }

    /** Creates the progress section. */
    public ProgressSection createProgressSection() {
        VBox progressBox = new VBox(5);
        progressBox.getStyleClass().add("content-section");
        progressBox.setPadding(new Insets(5));
        progressBox.setBorder(
                new Border(
                        new BorderStroke(
                                Color.LIGHTGRAY,
                                BorderStrokeStyle.SOLID,
                                new CornerRadii(5),
                                BorderWidths.DEFAULT)));

        Label statusLabel = new Label("Status: Ready");
        statusLabel.getStyleClass().add("status-label");
        statusLabel.setStyle("-fx-font-weight: bold;");

        ProgressBar progressBar = new ProgressBar(0);
        progressBar.setPrefWidth(Double.MAX_VALUE);

        progressBox.getChildren().addAll(statusLabel, progressBar);

        return new ProgressSection(progressBox, statusLabel, progressBar);
    }

    /** Progress section container. */
    public static class ProgressSection {
        private final VBox container;
        private final Label statusLabel;
        private final ProgressBar progressBar;

        public ProgressSection(VBox container, Label statusLabel, ProgressBar progressBar) {
            this.container = container;
            this.statusLabel = statusLabel;
            this.progressBar = progressBar;
        }

        public VBox getContainer() {
            return container;
        }

        public Label getStatusLabel() {
            return statusLabel;
        }

        public ProgressBar getProgressBar() {
            return progressBar;
        }
    }

    /** Creates the button pane for automation functions. */
    public ButtonPane createButtonPane() {
        FlowPane buttonPane = new FlowPane();
        buttonPane.getStyleClass().add("button-pane");
        buttonPane.setPadding(new Insets(10));
        buttonPane.setHgap(10);
        buttonPane.setVgap(10);
        buttonPane.setBorder(
                new Border(
                        new BorderStroke(
                                Color.LIGHTGRAY,
                                BorderStrokeStyle.SOLID,
                                new CornerRadii(5),
                                BorderWidths.DEFAULT)));

        ScrollPane buttonScrollPane = new ScrollPane(buttonPane);
        buttonScrollPane.setFitToWidth(true);
        buttonScrollPane.setPrefHeight(configuration.buttonScrollHeight);

        return new ButtonPane(buttonPane, buttonScrollPane);
    }

    /** Button pane container. */
    public static class ButtonPane {
        private final FlowPane flowPane;
        private final ScrollPane scrollPane;

        public ButtonPane(FlowPane flowPane, ScrollPane scrollPane) {
            this.flowPane = flowPane;
            this.scrollPane = scrollPane;
        }

        public FlowPane getFlowPane() {
            return flowPane;
        }

        public ScrollPane getScrollPane() {
            return scrollPane;
        }
    }

    /** Creates the log area. */
    public TextArea createLogArea() {
        TextArea logArea = new TextArea();
        logArea.setEditable(false);
        logArea.setWrapText(true);
        logArea.setPrefHeight(configuration.logAreaHeight);
        logArea.getStyleClass().add("automation-log");
        return logArea;
    }

    /** Creates a section label. */
    public Label createSectionLabel(String text) {
        Label label = new Label(text);
        label.getStyleClass().add("section-label");
        return label;
    }

    /** Creates the main layout container. */
    public VBox createMainContainer() {
        VBox container = new VBox();
        container.setPadding(new Insets(configuration.sectionPadding));
        container.setSpacing(configuration.mainSpacing);
        container.getStyleClass().add("automation-panel");
        return container;
    }

    /** Creates a separator. */
    public Separator createSeparator() {
        return new Separator();
    }

    /** Creates the enhanced status panel. */
    public AutomationStatusPanel createEnhancedStatusPanel(
            io.github.jspinak.brobot.runner.hotkeys.HotkeyManager hotkeyManager) {
        AutomationStatusPanel panel = new AutomationStatusPanel(hotkeyManager);
        panel.setPrefHeight(80);
        return panel;
    }

    /** Assembles all components into the main panel. */
    public AssembledPanel assembleMainPanel(
            Label titleLabel,
            AutomationStatusPanel statusPanel,
            ProgressSection progressSection,
            ControlBar controlBar,
            ButtonPane buttonPane,
            TextArea logArea) {

        VBox mainContainer = createMainContainer();

        mainContainer
                .getChildren()
                .addAll(
                        titleLabel,
                        createSeparator(),
                        statusPanel,
                        progressSection.getContainer(),
                        controlBar.getContainer(),
                        createSectionLabel("Available Automation Functions:"),
                        buttonPane.getScrollPane(),
                        createSectionLabel("Automation Log:"),
                        logArea);

        return new AssembledPanel(
                mainContainer,
                titleLabel,
                statusPanel,
                progressSection,
                controlBar,
                buttonPane,
                logArea);
    }

    /** Container for all assembled UI components. */
    public static class AssembledPanel {
        private final VBox mainContainer;
        private final Label titleLabel;
        private final AutomationStatusPanel statusPanel;
        private final ProgressSection progressSection;
        private final ControlBar controlBar;
        private final ButtonPane buttonPane;
        private final TextArea logArea;

        public AssembledPanel(
                VBox mainContainer,
                Label titleLabel,
                AutomationStatusPanel statusPanel,
                ProgressSection progressSection,
                ControlBar controlBar,
                ButtonPane buttonPane,
                TextArea logArea) {
            this.mainContainer = mainContainer;
            this.titleLabel = titleLabel;
            this.statusPanel = statusPanel;
            this.progressSection = progressSection;
            this.controlBar = controlBar;
            this.buttonPane = buttonPane;
            this.logArea = logArea;
        }

        public VBox getMainContainer() {
            return mainContainer;
        }

        public Label getTitleLabel() {
            return titleLabel;
        }

        public AutomationStatusPanel getStatusPanel() {
            return statusPanel;
        }

        public ProgressSection getProgressSection() {
            return progressSection;
        }

        public ControlBar getControlBar() {
            return controlBar;
        }

        public ButtonPane getButtonPane() {
            return buttonPane;
        }

        public TextArea getLogArea() {
            return logArea;
        }
    }
}
