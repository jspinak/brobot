package io.github.jspinak.brobot.runner.ui.automation.services;

import javafx.geometry.Insets;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import javafx.scene.paint.Color;

import org.springframework.stereotype.Service;

import lombok.extern.slf4j.Slf4j;

/**
 * Factory service for creating UI components for the basic automation panel. Provides consistent
 * styling and layout.
 */
@Slf4j
@Service
public class BasicUIFactory {

    // Configuration
    private UIConfiguration configuration = UIConfiguration.builder().build();

    /** UI configuration settings. */
    public static class UIConfiguration {
        private int panelPadding = 20;
        private int panelSpacing = 10;
        private int controlBarSpacing = 10;
        private int statusBoxSpacing = 5;
        private Insets statusBoxPadding = new Insets(5);
        private int buttonPaneHgap = 10;
        private int buttonPaneVgap = 10;
        private Insets buttonPanePadding = new Insets(10);
        private int buttonScrollHeight = 200;
        private int logAreaHeight = 300;
        private String titleFontSize = "16";
        private String titleFontWeight = "bold";
        private String statusFontWeight = "bold";

        public static UIConfigurationBuilder builder() {
            return new UIConfigurationBuilder();
        }

        public static class UIConfigurationBuilder {
            private UIConfiguration config = new UIConfiguration();

            public UIConfigurationBuilder panelPadding(int padding) {
                config.panelPadding = padding;
                return this;
            }

            public UIConfigurationBuilder panelSpacing(int spacing) {
                config.panelSpacing = spacing;
                return this;
            }

            public UIConfigurationBuilder controlBarSpacing(int spacing) {
                config.controlBarSpacing = spacing;
                return this;
            }

            public UIConfigurationBuilder statusBoxSpacing(int spacing) {
                config.statusBoxSpacing = spacing;
                return this;
            }

            public UIConfigurationBuilder buttonScrollHeight(int height) {
                config.buttonScrollHeight = height;
                return this;
            }

            public UIConfigurationBuilder logAreaHeight(int height) {
                config.logAreaHeight = height;
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

    /** Creates the main panel container. */
    public VBox createMainPanel() {
        VBox panel = new VBox();
        panel.setPadding(new Insets(configuration.panelPadding));
        panel.setSpacing(configuration.panelSpacing);
        panel.getStyleClass().add("automation-panel");
        return panel;
    }

    /** Creates the title label. */
    public Label createTitleLabel() {
        Label titleLabel = new Label("Automation Control");
        titleLabel.getStyleClass().add("title-label");
        titleLabel.setStyle(
                String.format(
                        "-fx-font-size: %s; -fx-font-weight: %s;",
                        configuration.titleFontSize, configuration.titleFontWeight));
        return titleLabel;
    }

    /** Creates the control bar section. */
    public ControlBarSection createControlBar() {
        Button refreshButton = new Button("Refresh Automation Buttons");
        refreshButton.setId("refreshAutomationButtons");

        Button pauseResumeButton = new Button("Pause Execution");
        pauseResumeButton.setId("pauseResumeExecution");
        pauseResumeButton.setDisable(true);

        Button stopAllButton = new Button("Stop All Automation");
        stopAllButton.setId("stopAllAutomation");

        HBox controlBar = new HBox(configuration.controlBarSpacing);
        controlBar.getStyleClass().add("control-bar");
        controlBar.getChildren().addAll(refreshButton, pauseResumeButton, stopAllButton);

        return new ControlBarSection(controlBar, refreshButton, pauseResumeButton, stopAllButton);
    }

    /** Creates the status section. */
    public StatusSection createStatusSection() {
        Label statusLabel = new Label("Status: Ready");
        statusLabel.getStyleClass().add("status-label");
        statusLabel.setStyle("-fx-font-weight: " + configuration.statusFontWeight + ";");

        ProgressBar progressBar = new ProgressBar(0);
        progressBar.setPrefWidth(Double.MAX_VALUE);

        VBox statusBox = new VBox(configuration.statusBoxSpacing);
        statusBox.getStyleClass().add("content-section");
        statusBox.setPadding(configuration.statusBoxPadding);
        statusBox.setBorder(createBorder());
        statusBox.getChildren().addAll(statusLabel, progressBar);

        return new StatusSection(statusBox, statusLabel, progressBar);
    }

    /** Creates the button pane section. */
    public ButtonPaneSection createButtonPane() {
        FlowPane buttonPane = new FlowPane();
        buttonPane.getStyleClass().add("button-pane");
        buttonPane.setPadding(configuration.buttonPanePadding);
        buttonPane.setHgap(configuration.buttonPaneHgap);
        buttonPane.setVgap(configuration.buttonPaneVgap);
        buttonPane.setBorder(createBorder());

        ScrollPane buttonScrollPane = new ScrollPane(buttonPane);
        buttonScrollPane.setFitToWidth(true);
        buttonScrollPane.setPrefHeight(configuration.buttonScrollHeight);

        return new ButtonPaneSection(buttonPane, buttonScrollPane);
    }

    /** Creates the log area. */
    public TextArea createLogArea() {
        TextArea logArea = new TextArea();
        logArea.setEditable(false);
        logArea.setWrapText(true);
        logArea.setPrefHeight(configuration.logAreaHeight);
        return logArea;
    }

    /** Creates a section label. */
    public Label createSectionLabel(String text) {
        Label label = new Label(text);
        label.getStyleClass().add("section-label");
        return label;
    }

    /** Creates a separator. */
    public Separator createSeparator() {
        return new Separator();
    }

    /** Creates a standard border. */
    private Border createBorder() {
        return new Border(
                new BorderStroke(
                        Color.LIGHTGRAY,
                        BorderStrokeStyle.SOLID,
                        new CornerRadii(5),
                        BorderWidths.DEFAULT));
    }

    /** Assembles the complete UI. */
    public AssembledUI assembleUI(
            VBox mainPanel,
            Label titleLabel,
            StatusSection statusSection,
            ControlBarSection controlBar,
            ButtonPaneSection buttonPane,
            TextArea logArea) {

        mainPanel
                .getChildren()
                .addAll(
                        titleLabel,
                        createSeparator(),
                        statusSection.getContainer(),
                        controlBar.getContainer(),
                        createSectionLabel("Available Automation Functions:"),
                        buttonPane.getScrollPane(),
                        createSectionLabel("Automation Log:"),
                        logArea);

        return new AssembledUI(
                mainPanel, titleLabel, statusSection, controlBar, buttonPane, logArea);
    }

    // Component containers

    public static class ControlBarSection {
        private final HBox container;
        private final Button refreshButton;
        private final Button pauseResumeButton;
        private final Button stopAllButton;

        public ControlBarSection(
                HBox container,
                Button refreshButton,
                Button pauseResumeButton,
                Button stopAllButton) {
            this.container = container;
            this.refreshButton = refreshButton;
            this.pauseResumeButton = pauseResumeButton;
            this.stopAllButton = stopAllButton;
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

    public static class ButtonPaneSection {
        private final FlowPane buttonPane;
        private final ScrollPane scrollPane;

        public ButtonPaneSection(FlowPane buttonPane, ScrollPane scrollPane) {
            this.buttonPane = buttonPane;
            this.scrollPane = scrollPane;
        }

        public FlowPane getButtonPane() {
            return buttonPane;
        }

        public ScrollPane getScrollPane() {
            return scrollPane;
        }
    }

    public static class AssembledUI {
        private final VBox mainPanel;
        private final Label titleLabel;
        private final StatusSection statusSection;
        private final ControlBarSection controlBar;
        private final ButtonPaneSection buttonPane;
        private final TextArea logArea;

        public AssembledUI(
                VBox mainPanel,
                Label titleLabel,
                StatusSection statusSection,
                ControlBarSection controlBar,
                ButtonPaneSection buttonPane,
                TextArea logArea) {
            this.mainPanel = mainPanel;
            this.titleLabel = titleLabel;
            this.statusSection = statusSection;
            this.controlBar = controlBar;
            this.buttonPane = buttonPane;
            this.logArea = logArea;
        }

        public VBox getMainPanel() {
            return mainPanel;
        }

        public Label getTitleLabel() {
            return titleLabel;
        }

        public StatusSection getStatusSection() {
            return statusSection;
        }

        public ControlBarSection getControlBar() {
            return controlBar;
        }

        public ButtonPaneSection getButtonPane() {
            return buttonPane;
        }

        public TextArea getLogArea() {
            return logArea;
        }
    }
}
