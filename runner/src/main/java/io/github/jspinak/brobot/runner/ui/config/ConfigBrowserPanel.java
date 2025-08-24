package io.github.jspinak.brobot.runner.ui.config;

import io.github.jspinak.brobot.model.state.State;
import io.github.jspinak.brobot.model.state.StateImage;
import io.github.jspinak.brobot.navigation.service.StateService;
import io.github.jspinak.brobot.runner.events.EventBus;
import io.github.jspinak.brobot.runner.events.LogEvent;
import io.github.jspinak.brobot.runner.project.AutomationProject;
import io.github.jspinak.brobot.runner.project.AutomationProjectManager;
import javafx.application.Platform;
import javafx.geometry.Insets;
import javafx.scene.control.*;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.VBox;
import lombok.Getter;
import lombok.Setter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

/**
 * Panel for browsing configuration structure using a tree-based view.
 */
public class ConfigBrowserPanel extends BorderPane {
    private static final Logger logger = LoggerFactory.getLogger(ConfigBrowserPanel.class);

    private final EventBus eventBus;
    private final AutomationProjectManager projectManager;
    private final StateService allStatesService;

    private final TreeView<ConfigItem> configTree;
    private final TreeItem<ConfigItem> rootItem;

    private final TextArea detailsTextArea;
    private final ImageView imagePreview;
    private final VBox previewPanel;

    private ConfigEntry currentConfig;
    private final Map<String, TreeItem<ConfigItem>> stateItems = new HashMap<>();

    public ConfigBrowserPanel(EventBus eventBus, AutomationProjectManager projectManager,
                              StateService allStatesService) {
        this.eventBus = eventBus;
        this.projectManager = projectManager;
        this.allStatesService = allStatesService;

        // Create tree view
        rootItem = new TreeItem<>(new ConfigItem("Configuration", ConfigItemType.ROOT));
        rootItem.setExpanded(true);

        configTree = new TreeView<>(rootItem);
        configTree.setCellFactory(tv -> new ConfigTreeCell());
        configTree.getSelectionModel().selectedItemProperty().addListener(
                (obs, oldVal, newVal) -> handleItemSelection(newVal)
        );

        // Create details panel
        detailsTextArea = new TextArea();
        detailsTextArea.setEditable(false);
        detailsTextArea.setWrapText(true);

        // Create image preview
        imagePreview = new ImageView();
        imagePreview.setFitHeight(200);
        imagePreview.setFitWidth(200);
        imagePreview.setPreserveRatio(true);

        // Preview panel layout
        previewPanel = new VBox(10);
        previewPanel.setPadding(new Insets(10));
        previewPanel.getChildren().addAll(
                new Label("Preview"),
                imagePreview
        );
        previewPanel.setVisible(false);

        // Split pane for tree and details
        SplitPane splitPane = new SplitPane();

        VBox treeBox = new VBox(5);
        treeBox.getChildren().addAll(
                new Label("Configuration Structure"),
                configTree
        );

        VBox detailsBox = new VBox(5);
        detailsBox.getChildren().addAll(
                new Label("Details"),
                detailsTextArea
        );
        VBox.setVgrow(detailsTextArea, Priority.ALWAYS);

        // Add nodes to split pane
        splitPane.getItems().addAll(treeBox, detailsBox);
        splitPane.setDividerPositions(0.4);

        // Search field
        TextField searchField = new TextField();
        searchField.setPromptText("Search configuration...");
        searchField.textProperty().addListener((obs, oldVal, newVal) -> {
            if (newVal.trim().isEmpty()) {
                resetTreeExpansion(rootItem);
            } else {
                searchTree(rootItem, newVal.toLowerCase());
            }
        });

        // Toolbar
        HBox toolbar = new HBox(10);
        toolbar.setPadding(new Insets(5));
        toolbar.getChildren().addAll(
                new Label("Search:"),
                searchField
        );
        HBox.setHgrow(searchField, Priority.ALWAYS);

        // Add to layout
        setTop(toolbar);
        setCenter(splitPane);
        setRight(previewPanel);
    }

    /**
     * Sets the configuration to browse.
     *
     * @param config The configuration entry
     */
    public void setConfiguration(ConfigEntry config) {
        currentConfig = config;

        // Clear existing items
        rootItem.getChildren().clear();
        stateItems.clear();

        if (config == null) {
            return;
        }

        try {
            // Load configuration content
            populateConfigurationTree(config);

            // Expand the root item
            rootItem.setExpanded(true);

            // Log
            eventBus.publish(LogEvent.info(this,
                    "Configuration browser loaded: " + config.getName(), "Configuration"));

        } catch (Exception e) {
            logger.error("Error loading configuration for browser", e);
            showAlert(Alert.AlertType.ERROR,
                    "Load Error",
                    "Error loading configuration for browser",
                    e.getMessage());
        }
    }

    private void populateConfigurationTree(ConfigEntry config) throws IOException {
        // Set root node name
        rootItem.setValue(new ConfigItem(config.getName(), ConfigItemType.ROOT));

        // Add configuration files
        TreeItem<ConfigItem> filesItem = new TreeItem<>(new ConfigItem("Configuration Files", ConfigItemType.FOLDER));

        // Project config file
        TreeItem<ConfigItem> projectConfigItem = new TreeItem<>(
                new ConfigItem(config.getProjectConfigFileName(), ConfigItemType.PROJECT_CONFIG)
        );
        projectConfigItem.getValue().setData(config.getProjectConfigPath());

        // DSL config file
        TreeItem<ConfigItem> dslConfigItem = new TreeItem<>(
                new ConfigItem(config.getDslConfigFileName(), ConfigItemType.DSL_CONFIG)
        );
        dslConfigItem.getValue().setData(config.getDslConfigPath());

        filesItem.getChildren().addAll(projectConfigItem, dslConfigItem);

        // Add project structure if loaded
        AutomationProject project = projectManager.getCurrentProject();
        if (project != null) {
            // Add states
            TreeItem<ConfigItem> statesItem = new TreeItem<>(new ConfigItem("States", ConfigItemType.FOLDER));

            // Use allStatesService instead of project.getAllStates()
            for (State state : allStatesService.getAllStates()) {
                TreeItem<ConfigItem> stateItem = new TreeItem<>(
                        new ConfigItem(state.getName(), ConfigItemType.STATE)
                );
                stateItem.getValue().setData(state);

                // Add state images
                for (StateImage stateImage : state.getStateImages()) {
                    TreeItem<ConfigItem> imageItem = new TreeItem<>(
                            new ConfigItem(stateImage.getName(), ConfigItemType.STATE_IMAGE)
                    );
                    imageItem.getValue().setData(stateImage);
                    stateItem.getChildren().add(imageItem);
                }

                statesItem.getChildren().add(stateItem);
                stateItems.put(state.getName(), stateItem);
            }

            // Add state definitions
            TreeItem<ConfigItem> transitionsItem = new TreeItem<>(new ConfigItem("State Transitions", ConfigItemType.FOLDER));

            // Add automation buttons
            TreeItem<ConfigItem> automationItem = new TreeItem<>(new ConfigItem("Automation", ConfigItemType.FOLDER));
            if (project.getAutomation() != null && project.getAutomation().getButtons() != null) {
                for (io.github.jspinak.brobot.runner.project.TaskButton button : project.getAutomation().getButtons()) {
                    TreeItem<ConfigItem> buttonItem = new TreeItem<>(
                            new ConfigItem(button.getLabel(), ConfigItemType.AUTOMATION_BUTTON)
                    );
                    buttonItem.getValue().setData(button);
                    automationItem.getChildren().add(buttonItem);
                }
            }

            // Add metadata
            TreeItem<ConfigItem> metadataItem = new TreeItem<>(new ConfigItem("Metadata", ConfigItemType.FOLDER));

            metadataItem.getChildren().addAll(
                    new TreeItem<>(new ConfigItem("Project: " + project.getName(), ConfigItemType.METADATA)),
                    new TreeItem<>(new ConfigItem("Version: " + (project.getVersion() != null ? project.getVersion() : "Not specified"), ConfigItemType.METADATA)),
                    new TreeItem<>(new ConfigItem("Author: " + (project.getAuthor() != null ? project.getAuthor() : "Not specified"), ConfigItemType.METADATA))
            );

            rootItem.getChildren().addAll(filesItem, metadataItem, statesItem, transitionsItem, automationItem);
        } else {
            rootItem.getChildren().add(filesItem);
        }
    }

    private void handleItemSelection(TreeItem<ConfigItem> item) {
        if (item == null || item.getValue() == null) {
            detailsTextArea.clear();
            imagePreview.setImage(null);
            previewPanel.setVisible(false);
            return;
        }

        ConfigItem configItem = item.getValue();

        // Update details
        try {
            switch (configItem.getType()) {
                case PROJECT_CONFIG:
                case DSL_CONFIG:
                    if (configItem.getData() instanceof Path path) {
                        String content = Files.readString(path);
                        detailsTextArea.setText(content);
                    }
                    imagePreview.setImage(null);
                    previewPanel.setVisible(false);
                    break;

                case STATE:
                    if (configItem.getData() instanceof State state) {
                        String details = "State: " + state.getName() + "\n\n" +
                                "ID: " + state.getId() + "\n" +
                                "Images: " + state.getStateImages().size() + "\n";
                        // Add more state details as needed
                        detailsTextArea.setText(details);
                    }
                    imagePreview.setImage(null);
                    previewPanel.setVisible(false);
                    break;

                case STATE_IMAGE:
                    if (configItem.getData() instanceof StateImage stateImage) {
                        String details = "State Image: " + stateImage.getName() + "\n\n" +
                                "Type: " + stateImage.getObjectType() + "\n" +
                                "Path: " + stateImage.getPatterns().getFirst().getImgpath() + "\n";
                        // Add more image details as needed
                        detailsTextArea.setText(details);

                        // Try to load the image for preview
                        String imageName = stateImage.getPatterns().getFirst().getImgpath();
                        if (imageName != null && !imageName.isEmpty()) {
                            // This is simplified - in a real implementation you'd use your image loading utilities
                            Path imagePath = currentConfig.getImagePath().resolve(imageName + ".png");
                            if (Files.exists(imagePath)) {
                                try {
                                    Image image = new Image(imagePath.toUri().toString());
                                    imagePreview.setImage(image);
                                    previewPanel.setVisible(true);
                                } catch (Exception e) {
                                    logger.error("Error loading image preview", e);
                                    imagePreview.setImage(null);
                                    previewPanel.setVisible(false);
                                }
                            } else {
                                imagePreview.setImage(null);
                                previewPanel.setVisible(false);
                            }
                        } else {
                            imagePreview.setImage(null);
                            previewPanel.setVisible(false);
                        }
                    }
                    break;

                case AUTOMATION_BUTTON:
                    if (configItem.getData() instanceof io.github.jspinak.brobot.runner.project.TaskButton button) {
                        String details = "Button: " + button.getLabel() + "\n\n" +
                                "Function: " + button.getFunctionName() + "\n" +
                                "Category: " + (button.getCategory() != null ? button.getCategory() : "None") + "\n" +
                                "Confirmation Required: " + button.isConfirmationRequired() + "\n";
                        // Add more button details as needed
                        detailsTextArea.setText(details);
                    }
                    imagePreview.setImage(null);
                    previewPanel.setVisible(false);
                    break;

                default:
                    detailsTextArea.setText("Select an item to view details");
                    imagePreview.setImage(null);
                    previewPanel.setVisible(false);
            }
        } catch (Exception e) {
            logger.error("Error displaying item details", e);
            detailsTextArea.setText("Error displaying details: " + e.getMessage());
            imagePreview.setImage(null);
            previewPanel.setVisible(false);
        }
    }

    private boolean searchTree(TreeItem<ConfigItem> item, String searchText) {
        // Check if this item matches
        boolean matches = item.getValue().toString().toLowerCase().contains(searchText);

        // Check children recursively
        boolean childrenMatch = false;
        for (TreeItem<ConfigItem> child : item.getChildren()) {
            if (searchTree(child, searchText)) {
                childrenMatch = true;
            }
        }

        // Expand this item if it matches or any children match
        item.setExpanded(matches || childrenMatch);

        return matches || childrenMatch;
    }

    private void resetTreeExpansion(TreeItem<ConfigItem> item) {
        // Reset expansion state based on item type
        switch (item.getValue().getType()) {
            case ROOT:
            case FOLDER:
                item.setExpanded(true);
                break;
            default:
                item.setExpanded(false);
        }

        // Process children
        for (TreeItem<ConfigItem> child : item.getChildren()) {
            resetTreeExpansion(child);
        }
    }

    /**
     * Navigates to a specific state in the configuration browser.
     *
     * @param stateName The name of the state to navigate to
     * @return true if the state was found and selected, false otherwise
     */
    public boolean navigateToState(String stateName) {
        TreeItem<ConfigItem> stateItem = stateItems.get(stateName);
        if (stateItem != null) {
            // Expand parents
            TreeItem<ConfigItem> parent = stateItem.getParent();
            while (parent != null) {
                parent.setExpanded(true);
                parent = parent.getParent();
            }

            // Select the item
            configTree.getSelectionModel().select(stateItem);
            configTree.scrollTo(configTree.getSelectionModel().getSelectedIndex());
            return true;
        }
        return false;
    }

    private void showAlert(Alert.AlertType type, String title, String header, String content) {
        Platform.runLater(() -> {
            Alert alert = new Alert(type);
            alert.setTitle(title);
            alert.setHeaderText(header);
            alert.setContentText(content);
            alert.showAndWait();
        });
    }

    /**
     * Clears the configuration browser.
     */
    public void clear() {
        rootItem.getChildren().clear();
        stateItems.clear();
        detailsTextArea.clear();
        imagePreview.setImage(null);
        previewPanel.setVisible(false);
    }

    /**
     * Data class for config tree items.
     */
    @Getter
    public static class ConfigItem {
        private final String name;
        private final ConfigItemType type;
        @Setter
        private Object data;

        public ConfigItem(String name, ConfigItemType type) {
            this.name = name;
            this.type = type;
        }

        @Override
        public String toString() {
            return name;
        }
    }

    /**
     * Enum for configuration item types.
     */
    public enum ConfigItemType {
        ROOT,
        FOLDER,
        PROJECT_CONFIG,
        DSL_CONFIG,
        STATE,
        STATE_IMAGE,
        AUTOMATION_BUTTON,
        METADATA
    }

    /**
     * Custom tree cell for configuration items.
     */
    private static class ConfigTreeCell extends TreeCell<ConfigItem> {
        @Override
        protected void updateItem(ConfigItem item, boolean empty) {
            super.updateItem(item, empty);

            if (empty || item == null) {
                setText(null);
                setGraphic(null);
                return;
            }

            setText(item.getName());

            // Set icon based on item type
            String iconPath = switch (item.getType()) {
                case ROOT -> "/icons/16/home.png";
                case FOLDER -> "/icons/16/folder.png";
                case PROJECT_CONFIG, DSL_CONFIG -> "/icons/16/file.png";
                case STATE -> "/icons/16/state.png";
                case STATE_IMAGE -> "/icons/16/image.png";
                case AUTOMATION_BUTTON -> "/icons/16/button.png";
                case METADATA -> "/icons/16/info.png";
            };

            try {
                ImageView icon = new ImageView(
                        new Image(Objects.requireNonNull(getClass().getResourceAsStream(iconPath))));
                icon.setFitHeight(16);
                icon.setFitWidth(16);
                setGraphic(icon);
            } catch (Exception e) {
                setGraphic(null);
            }
        }
    }
}