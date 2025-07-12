package io.github.jspinak.brobot.runner.ui.components.table.services;

import javafx.collections.transformation.FilteredList;
import javafx.collections.transformation.SortedList;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import org.springframework.stereotype.Service;

import java.util.Comparator;

/**
 * Service for managing table sorting operations.
 *
 * @param <T> The type of items in the table
 */
@Service
public class TableSortingService<T> {
    
    private SortedList<T> sortedItems;
    private TableView<T> tableView;
    
    /**
     * Initializes sorting with the filtered list.
     *
     * @param filteredItems The filtered list
     * @return The sorted list
     */
    public SortedList<T> initializeSorting(FilteredList<T> filteredItems) {
        sortedItems = new SortedList<>(filteredItems);
        return sortedItems;
    }
    
    /**
     * Binds the sorted list to the table view.
     *
     * @param tableView The table view
     */
    public void bindToTableView(TableView<T> tableView) {
        this.tableView = tableView;
        if (sortedItems != null) {
            sortedItems.comparatorProperty().bind(tableView.comparatorProperty());
        }
    }
    
    /**
     * Sets the sort order for the table.
     *
     * @param <S> The type of the column data
     * @param column The column to sort by
     * @param ascending True for ascending order, false for descending
     */
    public <S> void setSortOrder(TableColumn<T, S> column, boolean ascending) {
        if (tableView == null) {
            return;
        }
        
        tableView.getSortOrder().clear();
        column.setSortType(ascending ? TableColumn.SortType.ASCENDING : TableColumn.SortType.DESCENDING);
        tableView.getSortOrder().add(column);
        tableView.sort();
    }
    
    /**
     * Adds a column to the sort order.
     *
     * @param <S> The type of the column data
     * @param column The column to add
     * @param ascending True for ascending order, false for descending
     */
    public <S> void addToSortOrder(TableColumn<T, S> column, boolean ascending) {
        if (tableView == null) {
            return;
        }
        
        column.setSortType(ascending ? TableColumn.SortType.ASCENDING : TableColumn.SortType.DESCENDING);
        if (!tableView.getSortOrder().contains(column)) {
            tableView.getSortOrder().add(column);
        }
        tableView.sort();
    }
    
    /**
     * Removes a column from the sort order.
     *
     * @param column The column to remove
     */
    public void removeFromSortOrder(TableColumn<T, ?> column) {
        if (tableView == null) {
            return;
        }
        
        tableView.getSortOrder().remove(column);
        tableView.sort();
    }
    
    /**
     * Clears the sort order.
     */
    public void clearSortOrder() {
        if (tableView == null) {
            return;
        }
        
        tableView.getSortOrder().clear();
    }
    
    /**
     * Toggles the sort order of a column.
     *
     * @param column The column to toggle
     */
    public void toggleSortOrder(TableColumn<T, ?> column) {
        if (tableView == null) {
            return;
        }
        
        if (tableView.getSortOrder().contains(column)) {
            // Toggle between ascending and descending
            TableColumn.SortType currentType = column.getSortType();
            column.setSortType(currentType == TableColumn.SortType.ASCENDING ? 
                TableColumn.SortType.DESCENDING : TableColumn.SortType.ASCENDING);
            tableView.sort();
        } else {
            // Add to sort order as ascending
            setSortOrder(column, true);
        }
    }
    
    /**
     * Sets a custom comparator for the sorted list.
     *
     * @param comparator The comparator
     */
    public void setCustomComparator(Comparator<T> comparator) {
        if (sortedItems != null) {
            sortedItems.setComparator(comparator);
        }
    }
    
    /**
     * Gets the current sort order.
     *
     * @return The list of columns in the sort order
     */
    public javafx.collections.ObservableList<TableColumn<T, ?>> getSortOrder() {
        return tableView != null ? tableView.getSortOrder() : null;
    }
    
    /**
     * Checks if a column is in the sort order.
     *
     * @param column The column to check
     * @return True if the column is in the sort order
     */
    public boolean isInSortOrder(TableColumn<T, ?> column) {
        return tableView != null && tableView.getSortOrder().contains(column);
    }
    
    /**
     * Gets the sort type for a column.
     *
     * @param column The column
     * @return The sort type, or null if not in sort order
     */
    public TableColumn.SortType getSortType(TableColumn<T, ?> column) {
        if (tableView != null && tableView.getSortOrder().contains(column)) {
            return column.getSortType();
        }
        return null;
    }
    
    /**
     * Forces a re-sort of the table.
     */
    public void resort() {
        if (tableView != null) {
            tableView.sort();
        }
    }
    
    /**
     * Gets the sorted items.
     *
     * @return The sorted list
     */
    public SortedList<T> getSortedItems() {
        return sortedItems;
    }
}