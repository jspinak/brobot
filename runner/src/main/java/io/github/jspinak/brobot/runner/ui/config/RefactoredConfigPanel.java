package io.github.jspinak.brobot.runner.ui.config;

import javafx.scene.control.*;
import javafx.scene.layout.*;

import io.github.jspinak.brobot.runner.config.ApplicationConfig;
import io.github.jspinak.brobot.runner.config.BrobotRunnerProperties;
import io.github.jspinak.brobot.runner.events.EventBus;
import io.github.jspinak.brobot.runner.init.BrobotLibraryInitializer;
import io.github.jspinak.brobot.runner.ui.components.base.AtlantaCard;

import lombok.extern.slf4j.Slf4j;

/**
 * Refactored configuration panel that follows single responsibility principle. This panel is just a
 * container that delegates to specialized sub-components.
 */
@Slf4j
public class RefactoredConfigPanel extends VBox {

    private final EventBus eventBus;
    private final BrobotRunnerProperties runnerProperties;
    private final BrobotLibraryInitializer libraryInitializer;
    private final ApplicationConfig appConfig;

    // Sub-components with single responsibilities
    private final ConfigActionBar actionBar;
    private final ConfigListPanel listPanel;
    private final ConfigDetailsView detailsView;

    public RefactoredConfigPanel(
            EventBus eventBus,
            BrobotRunnerProperties runnerProperties,
            BrobotLibraryInitializer libraryInitializer,
            ApplicationConfig appConfig) {
        this.eventBus = eventBus;
        this.runnerProperties = runnerProperties;
        this.libraryInitializer = libraryInitializer;
        this.appConfig = appConfig;

        // Initialize sub-components
        this.actionBar = new ConfigActionBar(eventBus);
        this.listPanel = new ConfigListPanel(eventBus);
        this.detailsView = new ConfigDetailsView(eventBus);

        // Setup layout
        setupLayout();

        // Wire up component interactions
        setupInteractions();

        // Apply styling
        getStyleClass().add("refactored-config-panel");
    }

    private void setupLayout() {
        // Main content area with action bar and split layout
        VBox mainContent = new VBox();
        mainContent.getStyleClass().add("main-content");

        // Create split layout for list and details
        HBox splitLayout = createSplitLayout();

        // Add components to main content
        mainContent.getChildren().addAll(actionBar, splitLayout);

        // Add main content to this panel
        getChildren().add(mainContent);
        VBox.setVgrow(mainContent, Priority.ALWAYS);
    }

    private HBox createSplitLayout() {
        HBox splitLayout = new HBox(24);
        splitLayout.getStyleClass().add("split-layout");

        // Left: Configuration list
        AtlantaCard listCard = new AtlantaCard("Recent Configurations");
        listCard.setContent(listPanel);
        listCard.setMinWidth(600);
        listCard.setExpand(true);

        // Right: Configuration details
        AtlantaCard detailsCard = new AtlantaCard("Configuration Details");
        detailsCard.setContent(detailsView);
        detailsCard.setMinWidth(500);
        detailsCard.setExpand(true);

        splitLayout.getChildren().addAll(listCard, detailsCard);
        HBox.setHgrow(listCard, Priority.ALWAYS);
        HBox.setHgrow(detailsCard, Priority.ALWAYS);

        return splitLayout;
    }

    private void setupInteractions() {
        // When a configuration is selected in the list, update the details view
        listPanel
                .selectedConfigProperty()
                .addListener(
                        (obs, oldVal, newVal) -> {
                            if (newVal != null) {
                                detailsView.showConfiguration(newVal);
                            } else {
                                detailsView.clear();
                            }
                        });

        // Handle action bar events
        actionBar.setOnNewConfiguration(() -> createNewConfiguration());
        actionBar.setOnImport(() -> importConfiguration());
        actionBar.setOnRefresh(() -> listPanel.refresh());
    }

    private void createNewConfiguration() {
        log.info("Creating new configuration");
        // Implementation for creating new configuration
    }

    private void importConfiguration() {
        log.info("Importing configuration");
        // Implementation for importing configuration
    }
}
