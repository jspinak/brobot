package io.github.jspinak.brobot.runner.ui.config;

import java.io.IOException;
import javafx.application.Platform;
import javafx.scene.control.*;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import io.github.jspinak.brobot.navigation.service.StateService;
import io.github.jspinak.brobot.runner.events.EventBus;
import io.github.jspinak.brobot.runner.project.AutomationProjectManager;
import io.github.jspinak.brobot.runner.ui.config.services.*;

import lombok.extern.slf4j.Slf4j;

/**
 * Refactored panel for browsing configuration structure using a tree-based view. Delegates
 * responsibilities to specialized services.
 */
@Slf4j
@Component
public class RefactoredConfigBrowserPanel extends BorderPane {

    private final EventBus eventBus;
    private final AutomationProjectManager projectManager;
    private final StateService allStatesService;

    // Services
    private final TreeManagementService treeManagementService;
    private final SelectionService selectionService;
    private final ImagePreviewService imagePreviewService;
    private final SearchService searchService;
    private final ConfigurationLoaderService configurationLoaderService;
    private final ConfigBrowserUIFactory uiFactory;
    private final NavigationService navigationService;

    // UI Components
    private final TreeView<ConfigBrowserPanel.ConfigItem> configTree;
    private final TreeItem<ConfigBrowserPanel.ConfigItem> rootItem;
    private final TextArea detailsTextArea;
    private final ImageView imagePreview;
    private final VBox previewPanel;
    private final TextField searchField;

    private ConfigEntry currentConfig;

    @Autowired
    public RefactoredConfigBrowserPanel(
            EventBus eventBus,
            AutomationProjectManager projectManager,
            StateService allStatesService,
            TreeManagementService treeManagementService,
            SelectionService selectionService,
            ImagePreviewService imagePreviewService,
            SearchService searchService,
            ConfigurationLoaderService configurationLoaderService,
            ConfigBrowserUIFactory uiFactory,
            NavigationService navigationService) {

        this.eventBus = eventBus;
        this.projectManager = projectManager;
        this.allStatesService = allStatesService;
        this.treeManagementService = treeManagementService;
        this.selectionService = selectionService;
        this.imagePreviewService = imagePreviewService;
        this.searchService = searchService;
        this.configurationLoaderService = configurationLoaderService;
        this.uiFactory = uiFactory;
        this.navigationService = navigationService;

        // Create UI components using factory
        rootItem = treeManagementService.createRootItem("Configuration");
        configTree = uiFactory.createTreeView(rootItem);
        detailsTextArea = uiFactory.createDetailsTextArea();
        imagePreview = uiFactory.createImagePreview();
        previewPanel = uiFactory.createPreviewPanel(imagePreview);
        searchField = uiFactory.createSearchField();

        // Setup event handlers
        setupEventHandlers();

        // Build UI layout
        buildUI();
    }

    /** Sets up event handlers for UI components. */
    private void setupEventHandlers() {
        // Tree selection handler
        configTree
                .getSelectionModel()
                .selectedItemProperty()
                .addListener((obs, oldVal, newVal) -> handleItemSelection(newVal));

        // Search field handler
        searchField
                .textProperty()
                .addListener(
                        (obs, oldVal, newVal) -> {
                            if (newVal.trim().isEmpty()) {
                                searchService.resetTreeExpansion(rootItem);
                            } else {
                                SearchService.SearchResult result =
                                        searchService.searchTree(rootItem, newVal);
                                if (result.isFound()) {
                                    searchService.highlightFirstMatch(result, configTree);
                                }
                            }
                        });
    }

    /** Builds the UI layout. */
    private void buildUI() {
        // Create tree and details boxes
        VBox treeBox = uiFactory.createTreeBox(configTree);
        VBox detailsBox = uiFactory.createDetailsBox(detailsTextArea);

        // Create split pane
        SplitPane splitPane = uiFactory.createSplitPane(treeBox, detailsBox);

        // Create toolbar
        HBox toolbar = uiFactory.createToolbar(searchField);

        // Add to layout
        setTop(toolbar);
        setCenter(splitPane);
        setRight(previewPanel);
    }

    /**
     * Handles selection of a tree item.
     *
     * @param item The selected tree item
     */
    private void handleItemSelection(TreeItem<ConfigBrowserPanel.ConfigItem> item) {
        // Update details text
        String details = selectionService.processSelection(item);
        detailsTextArea.setText(details);

        // Handle image preview
        String imageName = selectionService.getImageNameForPreview(item);
        if (imageName != null && currentConfig != null) {
            Image image = imagePreviewService.loadImagePreview(imageName, currentConfig);
            imagePreview.setImage(image);
            previewPanel.setVisible(image != null);
        } else {
            imagePreview.setImage(null);
            previewPanel.setVisible(false);
        }
    }

    /**
     * Sets the configuration to browse.
     *
     * @param config The configuration entry
     */
    public void setConfiguration(ConfigEntry config) {
        currentConfig = config;

        try {
            configurationLoaderService.loadConfiguration(config, rootItem);
        } catch (IOException e) {
            log.error("Error loading configuration for browser", e);
            showAlert(
                    Alert.AlertType.ERROR,
                    "Load Error",
                    "Error loading configuration for browser",
                    e.getMessage());
        }
    }

    /**
     * Navigates to a specific state in the configuration browser.
     *
     * @param stateName The name of the state to navigate to
     * @return true if the state was found and selected, false otherwise
     */
    public boolean navigateToState(String stateName) {
        return navigationService.navigateToState(stateName, configTree);
    }

    /** Clears the configuration browser. */
    public void clear() {
        treeManagementService.clearTree(rootItem);
        detailsTextArea.clear();
        imagePreview.setImage(null);
        previewPanel.setVisible(false);
        imagePreviewService.clearCache();
        currentConfig = null;
    }

    /**
     * Shows an alert dialog.
     *
     * @param type The alert type
     * @param title The alert title
     * @param header The alert header
     * @param content The alert content
     */
    private void showAlert(Alert.AlertType type, String title, String header, String content) {
        Platform.runLater(
                () -> {
                    Alert alert = new Alert(type);
                    alert.setTitle(title);
                    alert.setHeaderText(header);
                    alert.setContentText(content);
                    alert.showAndWait();
                });
    }
}
