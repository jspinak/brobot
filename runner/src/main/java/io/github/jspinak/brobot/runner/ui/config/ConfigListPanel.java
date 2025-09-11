package io.github.jspinak.brobot.runner.ui.config;

import java.time.format.DateTimeFormatter;
import javafx.beans.property.ObjectProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.geometry.Pos;
import javafx.scene.control.*;
import javafx.scene.layout.*;

import io.github.jspinak.brobot.runner.events.EventBus;
import io.github.jspinak.brobot.runner.ui.config.model.ConfigEntry;

import lombok.extern.slf4j.Slf4j;

/**
 * Configuration list panel component. Single responsibility: Displays and manages the list of
 * configurations.
 */
@Slf4j
public class ConfigListPanel extends VBox {

    private static final DateTimeFormatter DATE_FORMATTER =
            DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm");

    private final EventBus eventBus;
    private final TableView<ConfigEntry> configTable;
    private final ObservableList<ConfigEntry> configData;
    private final ObjectProperty<ConfigEntry> selectedConfig;

    // Search and pagination
    private TextField searchField;
    private ComboBox<Integer> itemsPerPage;

    public ConfigListPanel(EventBus eventBus) {
        this.eventBus = eventBus;
        this.configData = FXCollections.observableArrayList();
        this.selectedConfig = new SimpleObjectProperty<>();

        // Setup styling
        getStyleClass().add("config-list-panel");
        setSpacing(16);

        // Create search bar
        HBox searchBar = createSearchBar();

        // Create table
        configTable = createConfigTable();

        // Bind selection
        configTable
                .getSelectionModel()
                .selectedItemProperty()
                .addListener(
                        (obs, oldVal, newVal) -> {
                            selectedConfig.set(newVal);
                        });

        // Add components
        getChildren().addAll(searchBar, configTable);
        VBox.setVgrow(configTable, Priority.ALWAYS);

        // Load initial data
        loadConfigurations();
    }

    private HBox createSearchBar() {
        HBox searchBar = new HBox(12);
        searchBar.getStyleClass().add("search-bar");
        searchBar.setAlignment(Pos.CENTER_LEFT);

        searchField = new TextField();
        searchField.setPromptText("Search configurations...");
        searchField.setPrefWidth(300);
        searchField
                .textProperty()
                .addListener((obs, oldVal, newVal) -> filterConfigurations(newVal));
        HBox.setHgrow(searchField, Priority.ALWAYS);

        Label itemsLabel = new Label("Items per page:");

        itemsPerPage = new ComboBox<>();
        itemsPerPage.getItems().addAll(25, 50, 100);
        itemsPerPage.setValue(25);
        itemsPerPage.valueProperty().addListener((obs, oldVal, newVal) -> updatePagination());

        searchBar.getChildren().addAll(searchField, itemsLabel, itemsPerPage);

        return searchBar;
    }

    private TableView<ConfigEntry> createConfigTable() {
        TableView<ConfigEntry> table = new TableView<>();
        table.setItems(configData);
        table.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY);

        // Name column
        TableColumn<ConfigEntry, String> nameCol = new TableColumn<>("Name");
        nameCol.setCellValueFactory(data -> new SimpleStringProperty(data.getValue().getName()));
        nameCol.setPrefWidth(200);

        // Project column
        TableColumn<ConfigEntry, String> projectCol = new TableColumn<>("Project");
        projectCol.setCellValueFactory(
                data -> new SimpleStringProperty(data.getValue().getProject()));
        projectCol.setPrefWidth(150);

        // Last Modified column
        TableColumn<ConfigEntry, String> modifiedCol = new TableColumn<>("Last Modif...");
        modifiedCol.setCellValueFactory(
                data ->
                        new SimpleStringProperty(
                                data.getValue().getLastModified().format(DATE_FORMATTER)));
        modifiedCol.setPrefWidth(140);

        // Path column
        TableColumn<ConfigEntry, String> pathCol = new TableColumn<>("Path");
        pathCol.setCellValueFactory(data -> new SimpleStringProperty(data.getValue().getPath()));
        pathCol.setPrefWidth(200);

        // Actions column
        TableColumn<ConfigEntry, Void> actionsCol = new TableColumn<>("Actions");
        actionsCol.setCellFactory(col -> new ConfigActionCell());
        actionsCol.setPrefWidth(120);

        table.getColumns().addAll(nameCol, projectCol, modifiedCol, pathCol, actionsCol);

        // Show placeholder when empty
        table.setPlaceholder(new Label("No configurations found"));

        return table;
    }

    private void loadConfigurations() {
        log.debug("Loading configurations");
        // Clear existing data
        configData.clear();

        // TODO: Load actual configurations from storage
        // For now, just show the placeholder
    }

    private void filterConfigurations(String searchText) {
        if (searchText == null || searchText.isEmpty()) {
            configTable.setItems(configData);
        } else {
            String lowerSearch = searchText.toLowerCase();
            ObservableList<ConfigEntry> filtered =
                    configData.filtered(
                            config ->
                                    config.getName().toLowerCase().contains(lowerSearch)
                                            || config.getProject()
                                                    .toLowerCase()
                                                    .contains(lowerSearch)
                                            || config.getPath()
                                                    .toLowerCase()
                                                    .contains(lowerSearch));
            configTable.setItems(filtered);
        }
    }

    private void updatePagination() {
        // TODO: Implement pagination logic
        log.debug("Updating pagination with {} items per page", itemsPerPage.getValue());
    }

    public void refresh() {
        loadConfigurations();
    }

    public ObjectProperty<ConfigEntry> selectedConfigProperty() {
        return selectedConfig;
    }

    /** Custom table cell for action buttons. */
    private class ConfigActionCell extends TableCell<ConfigEntry, Void> {
        private final Button loadBtn = new Button("Load");
        private final Button deleteBtn = new Button("Delete");
        private final HBox container = new HBox(4);

        public ConfigActionCell() {
            loadBtn.getStyleClass().addAll("button", "small", "accent");
            deleteBtn.getStyleClass().addAll("button", "small", "danger");

            container.getChildren().addAll(loadBtn, deleteBtn);
            container.setAlignment(Pos.CENTER);

            loadBtn.setOnAction(
                    e -> {
                        ConfigEntry entry = getTableView().getItems().get(getIndex());
                        loadConfiguration(entry);
                    });

            deleteBtn.setOnAction(
                    e -> {
                        ConfigEntry entry = getTableView().getItems().get(getIndex());
                        deleteConfiguration(entry);
                    });
        }

        @Override
        protected void updateItem(Void item, boolean empty) {
            super.updateItem(item, empty);

            if (empty) {
                setGraphic(null);
            } else {
                setGraphic(container);
            }
        }
    }

    private void loadConfiguration(ConfigEntry config) {
        log.info("Loading configuration: {}", config.getName());
        // TODO: Implement configuration loading
    }

    private void deleteConfiguration(ConfigEntry config) {
        log.info("Deleting configuration: {}", config.getName());
        // TODO: Implement configuration deletion
    }
}
