package io.github.jspinak.brobot.runner.ui.config.services;

import java.util.Objects;
import javafx.geometry.Insets;
import javafx.scene.control.*;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.*;

import org.springframework.stereotype.Service;

import io.github.jspinak.brobot.runner.ui.config.ConfigBrowserPanel;

/** Factory service for creating UI components for the configuration browser. */
@Service
public class ConfigBrowserUIFactory {

    /** Configuration for UI component creation. */
    public static class UIConfiguration {
        private int imagePreviewHeight = 200;
        private int imagePreviewWidth = 200;
        private double splitPaneDividerPosition = 0.4;
        private Insets previewPanelPadding = new Insets(10);
        private Insets toolbarPadding = new Insets(5);
        private int previewPanelSpacing = 10;
        private int treeBoxSpacing = 5;
        private int detailsBoxSpacing = 5;
        private int toolbarSpacing = 10;

        // Builder pattern
        public static Builder builder() {
            return new Builder();
        }

        public static class Builder {
            private final UIConfiguration config = new UIConfiguration();

            public Builder imagePreviewHeight(int height) {
                config.imagePreviewHeight = height;
                return this;
            }

            public Builder imagePreviewWidth(int width) {
                config.imagePreviewWidth = width;
                return this;
            }

            public Builder splitPaneDividerPosition(double position) {
                config.splitPaneDividerPosition = position;
                return this;
            }

            public Builder previewPanelPadding(Insets padding) {
                config.previewPanelPadding = padding;
                return this;
            }

            public Builder toolbarPadding(Insets padding) {
                config.toolbarPadding = padding;
                return this;
            }

            public UIConfiguration build() {
                return config;
            }
        }

        // Getters
        public int getImagePreviewHeight() {
            return imagePreviewHeight;
        }

        public int getImagePreviewWidth() {
            return imagePreviewWidth;
        }

        public double getSplitPaneDividerPosition() {
            return splitPaneDividerPosition;
        }

        public Insets getPreviewPanelPadding() {
            return previewPanelPadding;
        }

        public Insets getToolbarPadding() {
            return toolbarPadding;
        }

        public int getPreviewPanelSpacing() {
            return previewPanelSpacing;
        }

        public int getTreeBoxSpacing() {
            return treeBoxSpacing;
        }

        public int getDetailsBoxSpacing() {
            return detailsBoxSpacing;
        }

        public int getToolbarSpacing() {
            return toolbarSpacing;
        }
    }

    private UIConfiguration configuration = new UIConfiguration();

    /**
     * Sets the UI configuration.
     *
     * @param configuration The configuration to use
     */
    public void setConfiguration(UIConfiguration configuration) {
        this.configuration = configuration;
    }

    /**
     * Creates the tree view for configuration browsing.
     *
     * @param rootItem The root tree item
     * @return The configured tree view
     */
    public TreeView<ConfigBrowserPanel.ConfigItem> createTreeView(
            TreeItem<ConfigBrowserPanel.ConfigItem> rootItem) {
        TreeView<ConfigBrowserPanel.ConfigItem> treeView = new TreeView<>(rootItem);
        treeView.setCellFactory(tv -> new ConfigTreeCell());
        return treeView;
    }

    /**
     * Creates the details text area.
     *
     * @return The configured text area
     */
    public TextArea createDetailsTextArea() {
        TextArea detailsTextArea = new TextArea();
        detailsTextArea.setEditable(false);
        detailsTextArea.setWrapText(true);
        return detailsTextArea;
    }

    /**
     * Creates the image preview component.
     *
     * @return The configured image view
     */
    public ImageView createImagePreview() {
        ImageView imagePreview = new ImageView();
        imagePreview.setFitHeight(configuration.getImagePreviewHeight());
        imagePreview.setFitWidth(configuration.getImagePreviewWidth());
        imagePreview.setPreserveRatio(true);
        return imagePreview;
    }

    /**
     * Creates the preview panel.
     *
     * @param imagePreview The image preview component
     * @return The configured preview panel
     */
    public VBox createPreviewPanel(ImageView imagePreview) {
        VBox previewPanel = new VBox(configuration.getPreviewPanelSpacing());
        previewPanel.setPadding(configuration.getPreviewPanelPadding());
        previewPanel.getChildren().addAll(new Label("Preview"), imagePreview);
        previewPanel.setVisible(false);
        return previewPanel;
    }

    /**
     * Creates the tree box containing the tree view.
     *
     * @param configTree The tree view
     * @return The configured tree box
     */
    public VBox createTreeBox(TreeView<ConfigBrowserPanel.ConfigItem> configTree) {
        VBox treeBox = new VBox(configuration.getTreeBoxSpacing());
        treeBox.getChildren().addAll(new Label("Configuration Structure"), configTree);
        return treeBox;
    }

    /**
     * Creates the details box containing the text area.
     *
     * @param detailsTextArea The details text area
     * @return The configured details box
     */
    public VBox createDetailsBox(TextArea detailsTextArea) {
        VBox detailsBox = new VBox(configuration.getDetailsBoxSpacing());
        detailsBox.getChildren().addAll(new Label("Details"), detailsTextArea);
        VBox.setVgrow(detailsTextArea, Priority.ALWAYS);
        return detailsBox;
    }

    /**
     * Creates the split pane for tree and details.
     *
     * @param treeBox The tree box
     * @param detailsBox The details box
     * @return The configured split pane
     */
    public SplitPane createSplitPane(VBox treeBox, VBox detailsBox) {
        SplitPane splitPane = new SplitPane();
        splitPane.getItems().addAll(treeBox, detailsBox);
        splitPane.setDividerPositions(configuration.getSplitPaneDividerPosition());
        return splitPane;
    }

    /**
     * Creates the search field.
     *
     * @return The configured search field
     */
    public TextField createSearchField() {
        TextField searchField = new TextField();
        searchField.setPromptText("Search configuration...");
        return searchField;
    }

    /**
     * Creates the toolbar.
     *
     * @param searchField The search field
     * @return The configured toolbar
     */
    public HBox createToolbar(TextField searchField) {
        HBox toolbar = new HBox(configuration.getToolbarSpacing());
        toolbar.setPadding(configuration.getToolbarPadding());
        toolbar.getChildren().addAll(new Label("Search:"), searchField);
        HBox.setHgrow(searchField, Priority.ALWAYS);
        return toolbar;
    }

    /** Custom tree cell for configuration items. */
    private static class ConfigTreeCell extends TreeCell<ConfigBrowserPanel.ConfigItem> {
        @Override
        protected void updateItem(ConfigBrowserPanel.ConfigItem item, boolean empty) {
            super.updateItem(item, empty);

            if (empty || item == null) {
                setText(null);
                setGraphic(null);
                return;
            }

            setText(item.getName());

            // Set icon based on item type
            String iconPath =
                    switch (item.getType()) {
                        case ROOT -> "/icons/16/home.png";
                        case FOLDER -> "/icons/16/folder.png";
                        case PROJECT_CONFIG, DSL_CONFIG -> "/icons/16/file.png";
                        case STATE -> "/icons/16/state.png";
                        case STATE_IMAGE -> "/icons/16/image.png";
                        case AUTOMATION_BUTTON -> "/icons/16/button.png";
                        case METADATA -> "/icons/16/info.png";
                    };

            try {
                ImageView icon =
                        new ImageView(
                                new Image(
                                        Objects.requireNonNull(
                                                getClass().getResourceAsStream(iconPath))));
                icon.setFitHeight(16);
                icon.setFitWidth(16);
                setGraphic(icon);
            } catch (Exception e) {
                setGraphic(null);
            }
        }
    }
}
