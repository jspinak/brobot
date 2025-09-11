package io.github.jspinak.brobot.runner.ui.config.services;

import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.function.Consumer;
import javafx.application.Platform;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.geometry.Pos;
import javafx.scene.control.*;
import javafx.scene.layout.HBox;

import org.springframework.stereotype.Service;

import io.github.jspinak.brobot.runner.ui.components.EnhancedTable;
import io.github.jspinak.brobot.runner.ui.config.ConfigEntry;

import atlantafx.base.theme.Styles;
import lombok.Getter;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;

/**
 * Service for managing the configuration table UI. Handles table setup, column configuration, and
 * data binding.
 */
@Slf4j
@Service
public class ConfigTableService {

    private static final DateTimeFormatter DATE_FORMATTER =
            DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm");

    @Getter @Setter private TableConfiguration configuration;

    private Consumer<ConfigEntry> loadHandler;
    private Consumer<ConfigEntry> deleteHandler;
    private Consumer<ConfigEntry> selectionHandler;

    /** Configuration for table display. */
    @Getter
    @Setter
    public static class TableConfiguration {
        private boolean showActions;
        private boolean showPath;
        private boolean showTooltips;
        private boolean autoResize;

        // Column widths
        private int nameColumnMinWidth;
        private int nameColumnPrefWidth;
        private int nameColumnMaxWidth;
        private int projectColumnMinWidth;
        private int projectColumnPrefWidth;
        private int projectColumnMaxWidth;
        private int dateColumnMinWidth;
        private int dateColumnPrefWidth;
        private int dateColumnMaxWidth;
        private int pathColumnMinWidth;
        private int pathColumnPrefWidth;
        private int actionColumnMinWidth;
        private int actionColumnPrefWidth;
        private int actionColumnMaxWidth;

        public static TableConfigurationBuilder builder() {
            return new TableConfigurationBuilder();
        }

        public static class TableConfigurationBuilder {
            private boolean showActions = true;
            private boolean showPath = true;
            private boolean showTooltips = true;
            private boolean autoResize = true;

            private int nameColumnMinWidth = 100;
            private int nameColumnPrefWidth = 150;
            private int nameColumnMaxWidth = 250;
            private int projectColumnMinWidth = 100;
            private int projectColumnPrefWidth = 150;
            private int projectColumnMaxWidth = 250;
            private int dateColumnMinWidth = 120;
            private int dateColumnPrefWidth = 140;
            private int dateColumnMaxWidth = 160;
            private int pathColumnMinWidth = 150;
            private int pathColumnPrefWidth = 300;
            private int actionColumnMinWidth = 120;
            private int actionColumnPrefWidth = 140;
            private int actionColumnMaxWidth = 160;

            public TableConfigurationBuilder showActions(boolean show) {
                this.showActions = show;
                return this;
            }

            public TableConfigurationBuilder showPath(boolean show) {
                this.showPath = show;
                return this;
            }

            public TableConfigurationBuilder showTooltips(boolean show) {
                this.showTooltips = show;
                return this;
            }

            public TableConfigurationBuilder autoResize(boolean auto) {
                this.autoResize = auto;
                return this;
            }

            public TableConfigurationBuilder nameColumnWidths(int min, int pref, int max) {
                this.nameColumnMinWidth = min;
                this.nameColumnPrefWidth = pref;
                this.nameColumnMaxWidth = max;
                return this;
            }

            public TableConfigurationBuilder projectColumnWidths(int min, int pref, int max) {
                this.projectColumnMinWidth = min;
                this.projectColumnPrefWidth = pref;
                this.projectColumnMaxWidth = max;
                return this;
            }

            public TableConfigurationBuilder dateColumnWidths(int min, int pref, int max) {
                this.dateColumnMinWidth = min;
                this.dateColumnPrefWidth = pref;
                this.dateColumnMaxWidth = max;
                return this;
            }

            public TableConfigurationBuilder pathColumnWidths(int min, int pref) {
                this.pathColumnMinWidth = min;
                this.pathColumnPrefWidth = pref;
                return this;
            }

            public TableConfigurationBuilder actionColumnWidths(int min, int pref, int max) {
                this.actionColumnMinWidth = min;
                this.actionColumnPrefWidth = pref;
                this.actionColumnMaxWidth = max;
                return this;
            }

            public TableConfiguration build() {
                TableConfiguration config = new TableConfiguration();
                config.showActions = showActions;
                config.showPath = showPath;
                config.showTooltips = showTooltips;
                config.autoResize = autoResize;

                config.nameColumnMinWidth = nameColumnMinWidth;
                config.nameColumnPrefWidth = nameColumnPrefWidth;
                config.nameColumnMaxWidth = nameColumnMaxWidth;
                config.projectColumnMinWidth = projectColumnMinWidth;
                config.projectColumnPrefWidth = projectColumnPrefWidth;
                config.projectColumnMaxWidth = projectColumnMaxWidth;
                config.dateColumnMinWidth = dateColumnMinWidth;
                config.dateColumnPrefWidth = dateColumnPrefWidth;
                config.dateColumnMaxWidth = dateColumnMaxWidth;
                config.pathColumnMinWidth = pathColumnMinWidth;
                config.pathColumnPrefWidth = pathColumnPrefWidth;
                config.actionColumnMinWidth = actionColumnMinWidth;
                config.actionColumnPrefWidth = actionColumnPrefWidth;
                config.actionColumnMaxWidth = actionColumnMaxWidth;

                return config;
            }
        }
    }

    public ConfigTableService() {
        this.configuration = TableConfiguration.builder().build();
    }

    /**
     * Sets up the configuration table with all columns and handlers.
     *
     * @param table The EnhancedTable to configure
     */
    public void setupTable(EnhancedTable<ConfigEntry> table) {
        TableView<ConfigEntry> tableView = table.getTableView();

        // Configure table resize policy
        if (configuration.autoResize) {
            tableView.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY_FLEX_LAST_COLUMN);
        }

        // Clear existing columns
        tableView.getColumns().clear();

        // Add columns
        addNameColumn(tableView);
        addProjectColumn(tableView);
        addDateColumn(tableView);

        if (configuration.showPath) {
            addPathColumn(tableView);
        }

        if (configuration.showActions) {
            addActionColumn(tableView);
        }

        // Set selection handler
        tableView
                .getSelectionModel()
                .selectedItemProperty()
                .addListener(
                        (obs, oldVal, newVal) -> {
                            if (newVal != null && selectionHandler != null) {
                                selectionHandler.accept(newVal);
                            }
                        });
    }

    /**
     * Updates the table with new data.
     *
     * @param table The table to update
     * @param configs The configurations to display
     */
    public void updateTableData(EnhancedTable<ConfigEntry> table, List<ConfigEntry> configs) {
        Platform.runLater(
                () -> {
                    table.setItems(FXCollections.observableArrayList(configs));
                });
    }

    /**
     * Sets the handler for load actions.
     *
     * @param handler The load handler
     */
    public void setLoadHandler(Consumer<ConfigEntry> handler) {
        this.loadHandler = handler;
    }

    /**
     * Sets the handler for delete actions.
     *
     * @param handler The delete handler
     */
    public void setDeleteHandler(Consumer<ConfigEntry> handler) {
        this.deleteHandler = handler;
    }

    /**
     * Sets the handler for selection changes.
     *
     * @param handler The selection handler
     */
    public void setSelectionHandler(Consumer<ConfigEntry> handler) {
        this.selectionHandler = handler;
    }

    /**
     * Selects a specific configuration in the table.
     *
     * @param table The table
     * @param entry The entry to select
     */
    public void selectConfiguration(EnhancedTable<ConfigEntry> table, ConfigEntry entry) {
        Platform.runLater(
                () -> {
                    table.getTableView().getSelectionModel().select(entry);
                });
    }

    /**
     * Gets the currently selected configuration.
     *
     * @param table The table
     * @return The selected configuration or null
     */
    public ConfigEntry getSelectedConfiguration(EnhancedTable<ConfigEntry> table) {
        return table.getTableView().getSelectionModel().getSelectedItem();
    }

    private void addNameColumn(TableView<ConfigEntry> tableView) {
        TableColumn<ConfigEntry, String> nameColumn = new TableColumn<>("Name");
        nameColumn.setCellValueFactory(
                cellData -> new SimpleStringProperty(cellData.getValue().getName()));
        nameColumn.setMinWidth(configuration.nameColumnMinWidth);
        nameColumn.setPrefWidth(configuration.nameColumnPrefWidth);
        nameColumn.setMaxWidth(configuration.nameColumnMaxWidth);
        tableView.getColumns().add(nameColumn);
    }

    private void addProjectColumn(TableView<ConfigEntry> tableView) {
        TableColumn<ConfigEntry, String> projectColumn = new TableColumn<>("Project");
        projectColumn.setCellValueFactory(
                cellData -> new SimpleStringProperty(cellData.getValue().getProject()));
        projectColumn.setMinWidth(configuration.projectColumnMinWidth);
        projectColumn.setPrefWidth(configuration.projectColumnPrefWidth);
        projectColumn.setMaxWidth(configuration.projectColumnMaxWidth);
        tableView.getColumns().add(projectColumn);
    }

    private void addDateColumn(TableView<ConfigEntry> tableView) {
        TableColumn<ConfigEntry, String> lastModifiedColumn = new TableColumn<>("Last Modified");
        lastModifiedColumn.setCellValueFactory(
                cellData ->
                        new SimpleStringProperty(
                                cellData.getValue().getLastModified().format(DATE_FORMATTER)));
        lastModifiedColumn.setMinWidth(configuration.dateColumnMinWidth);
        lastModifiedColumn.setPrefWidth(configuration.dateColumnPrefWidth);
        lastModifiedColumn.setMaxWidth(configuration.dateColumnMaxWidth);
        tableView.getColumns().add(lastModifiedColumn);
    }

    private void addPathColumn(TableView<ConfigEntry> tableView) {
        TableColumn<ConfigEntry, String> pathColumn = new TableColumn<>("Path");
        pathColumn.setCellValueFactory(
                cellData ->
                        new SimpleStringProperty(
                                cellData.getValue().getProjectConfigPath().toString()));
        pathColumn.setMinWidth(configuration.pathColumnMinWidth);
        pathColumn.setPrefWidth(configuration.pathColumnPrefWidth);

        if (configuration.showTooltips) {
            // Setup custom cell factory for text truncation with tooltip
            pathColumn.setCellFactory(
                    column ->
                            new TableCell<ConfigEntry, String>() {
                                @Override
                                protected void updateItem(String path, boolean empty) {
                                    super.updateItem(path, empty);
                                    if (empty || path == null) {
                                        setText(null);
                                        setTooltip(null);
                                    } else {
                                        setText(path);
                                        setStyle("-fx-text-overrun: ellipsis;");
                                        // Add tooltip for full path
                                        Tooltip tooltip = new Tooltip(path);
                                        setTooltip(tooltip);
                                    }
                                }
                            });
        }

        tableView.getColumns().add(pathColumn);
    }

    private void addActionColumn(TableView<ConfigEntry> tableView) {
        TableColumn<ConfigEntry, Void> actionColumn = new TableColumn<>("Actions");
        actionColumn.setMinWidth(configuration.actionColumnMinWidth);
        actionColumn.setPrefWidth(configuration.actionColumnPrefWidth);
        actionColumn.setMaxWidth(configuration.actionColumnMaxWidth);

        actionColumn.setCellFactory(
                param ->
                        new TableCell<>() {
                            private final Button loadButton = new Button("Load");
                            private final Button deleteButton = new Button("Delete");
                            private final HBox buttonBox = new HBox(5, loadButton, deleteButton);

                            {
                                loadButton.getStyleClass().add(Styles.ACCENT);
                                loadButton.setOnAction(
                                        event -> {
                                            ConfigEntry entry =
                                                    getTableView().getItems().get(getIndex());
                                            if (loadHandler != null) {
                                                loadHandler.accept(entry);
                                            }
                                        });

                                deleteButton.getStyleClass().add(Styles.DANGER);
                                deleteButton.setOnAction(
                                        event -> {
                                            ConfigEntry entry =
                                                    getTableView().getItems().get(getIndex());
                                            if (deleteHandler != null) {
                                                deleteHandler.accept(entry);
                                            }
                                        });

                                buttonBox.setAlignment(Pos.CENTER);
                            }

                            @Override
                            protected void updateItem(Void item, boolean empty) {
                                super.updateItem(item, empty);
                                setGraphic(empty ? null : buttonBox);
                            }
                        });

        tableView.getColumns().add(actionColumn);
    }
}
