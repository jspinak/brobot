package io.github.jspinak.brobot.runner.ui.config.services;

import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.*;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.VBox;

import org.springframework.stereotype.Service;

import io.github.jspinak.brobot.runner.ui.components.Card;
import io.github.jspinak.brobot.runner.ui.components.EnhancedTable;
import io.github.jspinak.brobot.runner.ui.config.ConfigEntry;
import io.github.jspinak.brobot.runner.ui.config.RefactoredConfigDetailsPanel;
import io.github.jspinak.brobot.runner.ui.management.LabelManager;
import io.github.jspinak.brobot.runner.ui.management.UIUpdateManager;

import atlantafx.base.theme.Styles;
import lombok.Getter;
import lombok.Setter;

/**
 * Factory service for creating UI components for the configuration selection panel. Centralizes UI
 * creation logic for consistent styling and behavior.
 */
@Service
public class ConfigSelectionUIFactory {

    @Getter @Setter private UIConfiguration configuration;

    /** Configuration for UI creation. */
    @Getter
    @Setter
    public static class UIConfiguration {
        private double panelPadding;
        private double panelSpacing;
        private double headerSpacing;
        private double splitPaneDividerPosition;
        private double recentConfigsMinWidth;
        private double detailsMinWidth;

        // Style classes
        private String primaryButtonClass;
        private String secondaryButtonClass;
        private String dangerButtonClass;
        private String cardElevation;

        public static UIConfigurationBuilder builder() {
            return new UIConfigurationBuilder();
        }

        public static class UIConfigurationBuilder {
            private double panelPadding = 15;
            private double panelSpacing = 15;
            private double headerSpacing = 10;
            private double splitPaneDividerPosition = 0.45;
            private double recentConfigsMinWidth = 300;
            private double detailsMinWidth = 400;

            private String primaryButtonClass = "button-primary";
            private String secondaryButtonClass = "";
            private String dangerButtonClass = Styles.DANGER;
            private String cardElevation = Styles.ELEVATED_1;

            public UIConfigurationBuilder panelPadding(double padding) {
                this.panelPadding = padding;
                return this;
            }

            public UIConfigurationBuilder panelSpacing(double spacing) {
                this.panelSpacing = spacing;
                return this;
            }

            public UIConfigurationBuilder headerSpacing(double spacing) {
                this.headerSpacing = spacing;
                return this;
            }

            public UIConfigurationBuilder splitPaneDividerPosition(double position) {
                this.splitPaneDividerPosition = position;
                return this;
            }

            public UIConfigurationBuilder recentConfigsMinWidth(double width) {
                this.recentConfigsMinWidth = width;
                return this;
            }

            public UIConfigurationBuilder detailsMinWidth(double width) {
                this.detailsMinWidth = width;
                return this;
            }

            public UIConfigurationBuilder primaryButtonClass(String styleClass) {
                this.primaryButtonClass = styleClass;
                return this;
            }

            public UIConfigurationBuilder secondaryButtonClass(String styleClass) {
                this.secondaryButtonClass = styleClass;
                return this;
            }

            public UIConfigurationBuilder dangerButtonClass(String styleClass) {
                this.dangerButtonClass = styleClass;
                return this;
            }

            public UIConfigurationBuilder cardElevation(String elevation) {
                this.cardElevation = elevation;
                return this;
            }

            public UIConfiguration build() {
                UIConfiguration config = new UIConfiguration();
                config.panelPadding = panelPadding;
                config.panelSpacing = panelSpacing;
                config.headerSpacing = headerSpacing;
                config.splitPaneDividerPosition = splitPaneDividerPosition;
                config.recentConfigsMinWidth = recentConfigsMinWidth;
                config.detailsMinWidth = detailsMinWidth;
                config.primaryButtonClass = primaryButtonClass;
                config.secondaryButtonClass = secondaryButtonClass;
                config.dangerButtonClass = dangerButtonClass;
                config.cardElevation = cardElevation;
                return config;
            }
        }
    }

    /** Container for header components. */
    @Getter
    public static class HeaderSection {
        private final Label titleLabel;
        private final Button importButton;
        private final Button browseButton;
        private final Button refreshButton;
        private final HBox headerActions;
        private final HBox container;

        private HeaderSection(
                Label titleLabel,
                Button importButton,
                Button browseButton,
                Button refreshButton,
                HBox headerActions,
                HBox container) {
            this.titleLabel = titleLabel;
            this.importButton = importButton;
            this.browseButton = browseButton;
            this.refreshButton = refreshButton;
            this.headerActions = headerActions;
            this.container = container;
        }
    }

    /** Container for split pane components. */
    @Getter
    public static class SplitPaneSection {
        private final SplitPane splitPane;
        private final Card recentConfigsCard;
        private final Card detailsCard;
        private final EnhancedTable<ConfigEntry> configTable;
        private final RefactoredConfigDetailsPanel detailsPanel;

        private SplitPaneSection(
                SplitPane splitPane,
                Card recentConfigsCard,
                Card detailsCard,
                EnhancedTable<ConfigEntry> configTable,
                RefactoredConfigDetailsPanel detailsPanel) {
            this.splitPane = splitPane;
            this.recentConfigsCard = recentConfigsCard;
            this.detailsCard = detailsCard;
            this.configTable = configTable;
            this.detailsPanel = detailsPanel;
        }
    }

    /** Container for all assembled UI components. */
    @Getter
    public static class AssembledUI {
        private final VBox mainPanel;
        private final HeaderSection header;
        private final SplitPaneSection splitPane;

        private AssembledUI(VBox mainPanel, HeaderSection header, SplitPaneSection splitPane) {
            this.mainPanel = mainPanel;
            this.header = header;
            this.splitPane = splitPane;
        }
    }

    public ConfigSelectionUIFactory() {
        this.configuration = UIConfiguration.builder().build();
    }

    /**
     * Creates the main panel container.
     *
     * @return The main VBox panel
     */
    public VBox createMainPanel() {
        VBox panel = new VBox();
        panel.getStyleClass().add("config-selection-panel");
        panel.setPadding(new Insets(configuration.panelPadding));
        panel.setSpacing(configuration.panelSpacing);
        return panel;
    }

    /**
     * Creates the header section with title and action buttons.
     *
     * @return The header section
     */
    public HeaderSection createHeaderSection() {
        Label titleLabel = new Label("Configuration Selection");
        titleLabel.getStyleClass().add("title-label");

        HBox headerActions = new HBox(configuration.headerSpacing);

        Button importButton = new Button("Import Configuration");
        importButton.getStyleClass().add(configuration.primaryButtonClass);

        Button browseButton = new Button("Browse Files");
        if (!configuration.secondaryButtonClass.isEmpty()) {
            browseButton.getStyleClass().add(configuration.secondaryButtonClass);
        }

        Button refreshButton = new Button("Refresh");
        if (!configuration.secondaryButtonClass.isEmpty()) {
            refreshButton.getStyleClass().add(configuration.secondaryButtonClass);
        }

        headerActions.getChildren().addAll(importButton, browseButton, refreshButton);
        headerActions.setAlignment(Pos.CENTER_RIGHT);

        HBox header = new HBox(titleLabel, headerActions);
        header.setAlignment(Pos.CENTER_LEFT);
        HBox.setHgrow(headerActions, Priority.ALWAYS);

        return new HeaderSection(
                titleLabel, importButton, browseButton, refreshButton, headerActions, header);
    }

    /**
     * Creates the split pane section with table and details.
     *
     * @param eventBus The event bus for the details panel
     * @return The split pane section
     */
    public SplitPaneSection createSplitPaneSection(
            io.github.jspinak.brobot.runner.events.EventBus eventBus) {
        // Create table
        EnhancedTable<ConfigEntry> configTable = new EnhancedTable<>();

        // Create details panel
        // TODO: Get LabelManager and UIUpdateManager from Spring context
        LabelManager labelManager = new LabelManager();
        UIUpdateManager uiUpdateManager = new UIUpdateManager();
        uiUpdateManager.initialize();
        RefactoredConfigDetailsPanel detailsPanel =
                new RefactoredConfigDetailsPanel(eventBus, labelManager, uiUpdateManager);

        // Create split pane
        SplitPane splitPane = new SplitPane();
        splitPane.getStyleClass().add("config-split-pane");

        // Wrap in cards
        Card recentConfigsCard = new Card("Recent Configurations");
        recentConfigsCard
                .getStyleClass()
                .addAll("recent-configurations-table", configuration.cardElevation);
        recentConfigsCard.setContent(configTable);
        recentConfigsCard.setMinWidth(configuration.recentConfigsMinWidth);

        Card detailsCard = new Card("Configuration Details");
        detailsCard
                .getStyleClass()
                .addAll("configuration-details-card", configuration.cardElevation);
        detailsCard.setContent(detailsPanel);
        detailsCard.setMinWidth(configuration.detailsMinWidth);

        splitPane.getItems().addAll(recentConfigsCard, detailsCard);
        splitPane.setDividerPositions(configuration.splitPaneDividerPosition);
        VBox.setVgrow(splitPane, Priority.ALWAYS);

        return new SplitPaneSection(
                splitPane, recentConfigsCard, detailsCard, configTable, detailsPanel);
    }

    /**
     * Assembles all UI components into the main panel.
     *
     * @param mainPanel The main panel
     * @param header The header section
     * @param splitPane The split pane section
     * @return The assembled UI
     */
    public AssembledUI assembleUI(
            VBox mainPanel, HeaderSection header, SplitPaneSection splitPane) {
        mainPanel.getChildren().clear();
        mainPanel.getChildren().addAll(header.getContainer(), splitPane.getSplitPane());

        return new AssembledUI(mainPanel, header, splitPane);
    }

    /**
     * Creates a standard alert dialog.
     *
     * @param type The alert type
     * @param title The alert title
     * @param header The header text
     * @param content The content text
     * @return The configured alert
     */
    public Alert createAlert(Alert.AlertType type, String title, String header, String content) {
        Alert alert = new Alert(type);
        alert.setTitle(title);
        alert.setHeaderText(header);
        alert.setContentText(content);
        return alert;
    }
}
