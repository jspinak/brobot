package io.github.jspinak.brobot.runner.ui.components.table.services;

import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.*;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.Region;
import javafx.scene.layout.VBox;

import org.springframework.stereotype.Service;

import atlantafx.base.theme.Styles;

/** Factory service for creating UI components for the enhanced table. */
@Service
public class EnhancedTableUIFactory {

    /** Configuration for UI component creation. */
    public static class UIConfiguration {
        private String searchPrompt = "Search...";
        private int toolbarSpacing = 10;
        private Insets toolbarPadding = new Insets(5, 10, 5, 10);
        private String tablePlaceholder = "No data available";
        private int defaultPageSize = 25;

        // Builder pattern
        public static Builder builder() {
            return new Builder();
        }

        public static class Builder {
            private final UIConfiguration config = new UIConfiguration();

            public Builder searchPrompt(String prompt) {
                config.searchPrompt = prompt;
                return this;
            }

            public Builder toolbarSpacing(int spacing) {
                config.toolbarSpacing = spacing;
                return this;
            }

            public Builder toolbarPadding(Insets padding) {
                config.toolbarPadding = padding;
                return this;
            }

            public Builder tablePlaceholder(String placeholder) {
                config.tablePlaceholder = placeholder;
                return this;
            }

            public Builder defaultPageSize(int pageSize) {
                config.defaultPageSize = pageSize;
                return this;
            }

            public UIConfiguration build() {
                return config;
            }
        }

        // Getters
        public String getSearchPrompt() {
            return searchPrompt;
        }

        public int getToolbarSpacing() {
            return toolbarSpacing;
        }

        public Insets getToolbarPadding() {
            return toolbarPadding;
        }

        public String getTablePlaceholder() {
            return tablePlaceholder;
        }

        public int getDefaultPageSize() {
            return defaultPageSize;
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
     * Creates the main container for the enhanced table.
     *
     * @return The main VBox container
     */
    public VBox createMainContainer() {
        VBox container = new VBox();
        return container;
    }

    /**
     * Creates a table view with default styling.
     *
     * @param <T> The type of items in the table
     * @return The styled table view
     */
    public <T> TableView<T> createTableView() {
        TableView<T> tableView = new TableView<>();
        tableView.getStyleClass().addAll(Styles.STRIPED, Styles.BORDERED);
        tableView.setPlaceholder(new Label(configuration.getTablePlaceholder()));
        VBox.setVgrow(tableView, Priority.ALWAYS);
        return tableView;
    }

    /**
     * Creates the search field.
     *
     * @return The search field
     */
    public TextField createSearchField() {
        TextField searchField = new TextField();
        searchField.setPromptText(configuration.getSearchPrompt());
        return searchField;
    }

    /**
     * Creates the page size selector.
     *
     * @return The page size combo box
     */
    public ComboBox<Integer> createPageSizeSelector() {
        ComboBox<Integer> pageSizeSelector = new ComboBox<>();
        pageSizeSelector.getItems().addAll(10, 25, 50, 100);
        pageSizeSelector.setValue(configuration.getDefaultPageSize());
        return pageSizeSelector;
    }

    /**
     * Creates the pagination control.
     *
     * @return The pagination control
     */
    public Pagination createPagination() {
        Pagination pagination = new Pagination();
        pagination.setPageCount(1);
        return pagination;
    }

    /**
     * Creates the toolbar.
     *
     * @param searchField The search field
     * @param pageSizeSelector The page size selector
     * @return The toolbar HBox
     */
    public HBox createToolbar(TextField searchField, ComboBox<Integer> pageSizeSelector) {
        HBox toolbar = new HBox(configuration.getToolbarSpacing());
        toolbar.setAlignment(Pos.CENTER_LEFT);
        toolbar.setPadding(configuration.getToolbarPadding());

        Label searchLabel = new Label("Search:");
        toolbar.getChildren().addAll(searchLabel, searchField);

        // Add spacer
        Region spacer = createSpacer();
        toolbar.getChildren().add(spacer);

        // Add page size selector
        Label pageSizeLabel = new Label("Items per page:");
        toolbar.getChildren().addAll(pageSizeLabel, pageSizeSelector);

        return toolbar;
    }

    /**
     * Creates the pagination box.
     *
     * @param pagination The pagination control
     * @return The pagination container
     */
    public HBox createPaginationBox(Pagination pagination) {
        HBox paginationBox = new HBox(pagination);
        paginationBox.setAlignment(Pos.CENTER);
        return paginationBox;
    }

    /**
     * Creates a spacer region.
     *
     * @return The spacer region
     */
    public Region createSpacer() {
        Region spacer = new Region();
        HBox.setHgrow(spacer, Priority.ALWAYS);
        return spacer;
    }

    /**
     * Creates a label with specific style.
     *
     * @param text The label text
     * @param styleClasses The style classes
     * @return The styled label
     */
    public Label createLabel(String text, String... styleClasses) {
        Label label = new Label(text);
        if (styleClasses != null && styleClasses.length > 0) {
            label.getStyleClass().addAll(styleClasses);
        }
        return label;
    }

    /** Creates additional toolbar components. */
    public static class ToolbarComponents {
        private final TextField searchField;
        private final ComboBox<Integer> pageSizeSelector;
        private final HBox toolbar;

        public ToolbarComponents(
                TextField searchField, ComboBox<Integer> pageSizeSelector, HBox toolbar) {
            this.searchField = searchField;
            this.pageSizeSelector = pageSizeSelector;
            this.toolbar = toolbar;
        }

        public TextField getSearchField() {
            return searchField;
        }

        public ComboBox<Integer> getPageSizeSelector() {
            return pageSizeSelector;
        }

        public HBox getToolbar() {
            return toolbar;
        }
    }

    /**
     * Creates all toolbar components at once.
     *
     * @return The toolbar components
     */
    public ToolbarComponents createToolbarComponents() {
        TextField searchField = createSearchField();
        ComboBox<Integer> pageSizeSelector = createPageSizeSelector();
        HBox toolbar = createToolbar(searchField, pageSizeSelector);

        return new ToolbarComponents(searchField, pageSizeSelector, toolbar);
    }

    /**
     * Assembles the complete table UI.
     *
     * @param <T> The type of items in the table
     * @param container The main container
     * @param toolbar The toolbar
     * @param tableView The table view
     * @param paginationBox The pagination box
     * @return The assembled container
     */
    public <T> VBox assembleTableUI(
            VBox container, HBox toolbar, TableView<T> tableView, HBox paginationBox) {
        container.getChildren().addAll(toolbar, tableView, paginationBox);
        return container;
    }
}
