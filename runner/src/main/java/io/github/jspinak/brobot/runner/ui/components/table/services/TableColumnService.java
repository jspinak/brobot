package io.github.jspinak.brobot.runner.ui.components.table.services;

import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;
import javafx.scene.control.TableCell;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.util.Callback;
import org.springframework.stereotype.Service;

import java.util.Comparator;
import java.util.HashMap;
import java.util.Map;

/**
 * Service for managing table columns.
 *
 * @param <T> The type of items in the table
 */
@Service
public class TableColumnService<T> {
    
    private final Map<TableColumn<T, ?>, StringProperty> columnTitles = new HashMap<>();
    
    /**
     * Adds a string column to the table.
     *
     * @param tableView The table view
     * @param title The column title
     * @param propertyName The property name to bind to
     * @return The created column
     */
    public TableColumn<T, String> addStringColumn(TableView<T> tableView, String title, String propertyName) {
        TableColumn<T, String> column = new TableColumn<>();
        
        // Create a property for the title to allow dynamic updates
        StringProperty titleProperty = new SimpleStringProperty(title);
        column.textProperty().bind(titleProperty);
        columnTitles.put(column, titleProperty);
        
        // Set the cell value factory
        column.setCellValueFactory(new PropertyValueFactory<>(propertyName));
        
        // Add the column to the table
        tableView.getColumns().add(column);
        
        return column;
    }
    
    /**
     * Adds a column with a custom cell value factory.
     *
     * @param <S> The type of the column data
     * @param tableView The table view
     * @param title The column title
     * @param cellValueFactory The cell value factory for the column
     * @return The created column
     */
    public <S> TableColumn<T, S> addColumn(TableView<T> tableView, String title, 
            Callback<TableColumn.CellDataFeatures<T, S>, javafx.beans.value.ObservableValue<S>> cellValueFactory) {
        TableColumn<T, S> column = new TableColumn<>();
        
        // Create a property for the title to allow dynamic updates
        StringProperty titleProperty = new SimpleStringProperty(title);
        column.textProperty().bind(titleProperty);
        columnTitles.put(column, titleProperty);
        
        // Set the cell value factory
        column.setCellValueFactory(cellValueFactory);
        
        // Add the column to the table
        tableView.getColumns().add(column);
        
        return column;
    }
    
    /**
     * Adds a column with custom cell value factory and cell factory.
     *
     * @param <S> The type of the column data
     * @param tableView The table view
     * @param title The column title
     * @param valueFactory The cell value factory
     * @param cellFactory The cell factory
     * @return The created column
     */
    public <S> TableColumn<T, S> addColumn(TableView<T> tableView, String title,
            Callback<TableColumn.CellDataFeatures<T, S>, javafx.beans.value.ObservableValue<S>> valueFactory,
            Callback<TableColumn<T, S>, TableCell<T, S>> cellFactory) {
        TableColumn<T, S> column = addColumn(tableView, title, valueFactory);
        column.setCellFactory(cellFactory);
        return column;
    }
    
    /**
     * Sets the title of a column.
     *
     * @param column The column
     * @param title The new title
     */
    public void setColumnTitle(TableColumn<T, ?> column, String title) {
        StringProperty titleProperty = columnTitles.get(column);
        if (titleProperty != null) {
            titleProperty.set(title);
        }
    }
    
    /**
     * Removes a column from the table.
     *
     * @param tableView The table view
     * @param column The column to remove
     */
    public void removeColumn(TableView<T> tableView, TableColumn<T, ?> column) {
        tableView.getColumns().remove(column);
        columnTitles.remove(column);
    }
    
    /**
     * Clears all columns from the table.
     *
     * @param tableView The table view
     */
    public void clearColumns(TableView<T> tableView) {
        tableView.getColumns().clear();
        columnTitles.clear();
    }
    
    /**
     * Sets a comparator for a specific column.
     *
     * @param <S> The type of the column data
     * @param column The column
     * @param comparator The comparator
     */
    public <S> void setColumnComparator(TableColumn<T, S> column, Comparator<S> comparator) {
        column.setComparator(comparator);
    }
    
    /**
     * Sets the preferred width for a column.
     *
     * @param column The column
     * @param width The preferred width
     */
    public void setColumnWidth(TableColumn<T, ?> column, double width) {
        column.setPrefWidth(width);
    }
    
    /**
     * Sets the minimum width for a column.
     *
     * @param column The column
     * @param width The minimum width
     */
    public void setColumnMinWidth(TableColumn<T, ?> column, double width) {
        column.setMinWidth(width);
    }
    
    /**
     * Sets the maximum width for a column.
     *
     * @param column The column
     * @param width The maximum width
     */
    public void setColumnMaxWidth(TableColumn<T, ?> column, double width) {
        column.setMaxWidth(width);
    }
    
    /**
     * Sets whether a column is resizable.
     *
     * @param column The column
     * @param resizable True if resizable
     */
    public void setColumnResizable(TableColumn<T, ?> column, boolean resizable) {
        column.setResizable(resizable);
    }
    
    /**
     * Gets the current title of a column.
     *
     * @param column The column
     * @return The title, or null if not managed
     */
    public String getColumnTitle(TableColumn<T, ?> column) {
        StringProperty titleProperty = columnTitles.get(column);
        return titleProperty != null ? titleProperty.get() : null;
    }
}