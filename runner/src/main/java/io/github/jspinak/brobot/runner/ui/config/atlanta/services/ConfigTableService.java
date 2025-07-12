package io.github.jspinak.brobot.runner.ui.config.atlanta.services;

import io.github.jspinak.brobot.runner.ui.config.AtlantaConfigPanel.ConfigEntry;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.collections.transformation.FilteredList;
import javafx.collections.transformation.SortedList;
import javafx.scene.control.*;
import javafx.scene.layout.HBox;
import org.springframework.stereotype.Service;

import java.time.format.DateTimeFormatter;
import java.util.function.BiConsumer;
import java.util.function.Consumer;

/**
 * Service for managing the configurations table.
 */
@Service("atlantaConfigTableService")
public class ConfigTableService {
    
    private static final DateTimeFormatter DATE_FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm");
    private ObservableList<ConfigEntry> masterData = FXCollections.observableArrayList();
    private FilteredList<ConfigEntry> filteredData;
    private Consumer<ConfigEntry> loadHandler;
    private Consumer<ConfigEntry> deleteHandler;
    
    /**
     * Creates and configures the configurations table.
     *
     * @return The configured table view
     */
    public TableView<ConfigEntry> createConfigurationsTable() {
        TableView<ConfigEntry> configTable = new TableView<>();
        configTable.getStyleClass().add("table");
        configTable.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY);
        
        // Set up filtering
        filteredData = new FilteredList<>(masterData, p -> true);
        SortedList<ConfigEntry> sortedData = new SortedList<>(filteredData);
        sortedData.comparatorProperty().bind(configTable.comparatorProperty());
        configTable.setItems(sortedData);
        
        // Create columns
        configTable.getColumns().addAll(
            createNameColumn(),
            createProjectColumn(),
            createModifiedColumn(),
            createPathColumn(),
            createActionsColumn()
        );
        
        // Add placeholder
        Label placeholder = new Label("No configurations found");
        placeholder.getStyleClass().addAll("empty-state-title");
        configTable.setPlaceholder(placeholder);
        
        return configTable;
    }
    
    /**
     * Creates the name column.
     */
    private TableColumn<ConfigEntry, String> createNameColumn() {
        TableColumn<ConfigEntry, String> nameCol = new TableColumn<>("Name");
        nameCol.setCellValueFactory(data -> new SimpleStringProperty(data.getValue().getName()));
        nameCol.setMinWidth(150);
        return nameCol;
    }
    
    /**
     * Creates the project column.
     */
    private TableColumn<ConfigEntry, String> createProjectColumn() {
        TableColumn<ConfigEntry, String> projectCol = new TableColumn<>("Project");
        projectCol.setCellValueFactory(data -> new SimpleStringProperty(data.getValue().getProject()));
        projectCol.setMinWidth(150);
        return projectCol;
    }
    
    /**
     * Creates the modified date column.
     */
    private TableColumn<ConfigEntry, String> createModifiedColumn() {
        TableColumn<ConfigEntry, String> modifiedCol = new TableColumn<>("Last Modified");
        modifiedCol.setCellValueFactory(data -> 
            new SimpleStringProperty(data.getValue().getLastModified().format(DATE_FORMATTER)));
        modifiedCol.setMinWidth(150);
        return modifiedCol;
    }
    
    /**
     * Creates the path column.
     */
    private TableColumn<ConfigEntry, String> createPathColumn() {
        TableColumn<ConfigEntry, String> pathCol = new TableColumn<>("Path");
        pathCol.setCellValueFactory(data -> new SimpleStringProperty(data.getValue().getPath()));
        pathCol.setMinWidth(200);
        return pathCol;
    }
    
    /**
     * Creates the actions column with buttons.
     */
    private TableColumn<ConfigEntry, Void> createActionsColumn() {
        TableColumn<ConfigEntry, Void> actionsCol = new TableColumn<>("Actions");
        actionsCol.setMinWidth(150);
        actionsCol.setCellFactory(col -> new ActionButtonCell());
        return actionsCol;
    }
    
    /**
     * Custom cell for action buttons.
     */
    private class ActionButtonCell extends TableCell<ConfigEntry, Void> {
        private final Button loadBtn = new Button("Load");
        private final Button deleteBtn = new Button("Delete");
        private final HBox buttonBox;
        
        public ActionButtonCell() {
            loadBtn.getStyleClass().addAll("button", "small", "accent");
            deleteBtn.getStyleClass().addAll("button", "small", "danger");
            
            loadBtn.setOnAction(e -> {
                ConfigEntry entry = getTableView().getItems().get(getIndex());
                if (loadHandler != null) {
                    loadHandler.accept(entry);
                }
            });
            
            deleteBtn.setOnAction(e -> {
                ConfigEntry entry = getTableView().getItems().get(getIndex());
                if (deleteHandler != null) {
                    deleteHandler.accept(entry);
                }
            });
            
            buttonBox = new HBox(8, loadBtn, deleteBtn);
            buttonBox.getStyleClass().add("action-buttons");
        }
        
        @Override
        protected void updateItem(Void item, boolean empty) {
            super.updateItem(item, empty);
            if (empty) {
                setGraphic(null);
            } else {
                setGraphic(buttonBox);
            }
        }
    }
    
    /**
     * Sets the load handler for configuration entries.
     *
     * @param handler The load handler
     */
    public void setLoadHandler(Consumer<ConfigEntry> handler) {
        this.loadHandler = handler;
    }
    
    /**
     * Sets the delete handler for configuration entries.
     *
     * @param handler The delete handler
     */
    public void setDeleteHandler(Consumer<ConfigEntry> handler) {
        this.deleteHandler = handler;
    }
    
    /**
     * Updates the table data.
     *
     * @param data The new data
     */
    public void updateData(ObservableList<ConfigEntry> data) {
        masterData.setAll(data);
    }
    
    /**
     * Adds a configuration entry.
     *
     * @param entry The entry to add
     */
    public void addEntry(ConfigEntry entry) {
        masterData.add(entry);
    }
    
    /**
     * Removes a configuration entry.
     *
     * @param entry The entry to remove
     */
    public void removeEntry(ConfigEntry entry) {
        masterData.remove(entry);
    }
    
    /**
     * Clears all entries.
     */
    public void clearEntries() {
        masterData.clear();
    }
    
    /**
     * Gets the current data.
     *
     * @return The observable list of entries
     */
    public ObservableList<ConfigEntry> getData() {
        return masterData;
    }
    
    /**
     * Applies a filter to the table.
     *
     * @param searchText The search text
     */
    public void applyFilter(String searchText) {
        if (searchText == null || searchText.trim().isEmpty()) {
            filteredData.setPredicate(p -> true);
        } else {
            String lowerCaseFilter = searchText.toLowerCase();
            filteredData.setPredicate(entry -> 
                entry.getName().toLowerCase().contains(lowerCaseFilter) ||
                entry.getProject().toLowerCase().contains(lowerCaseFilter) ||
                entry.getPath().toLowerCase().contains(lowerCaseFilter)
            );
        }
    }
    
    /**
     * Sets the items per page (for pagination if needed).
     *
     * @param itemsPerPage The number of items per page
     */
    public void setItemsPerPage(int itemsPerPage) {
        // This could be implemented for pagination
        // For now, just a placeholder
    }
}