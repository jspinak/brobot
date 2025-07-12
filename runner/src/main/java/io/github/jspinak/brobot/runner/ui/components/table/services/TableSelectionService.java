package io.github.jspinak.brobot.runner.ui.components.table.services;

import javafx.beans.property.ObjectProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.collections.ObservableList;
import javafx.scene.control.SelectionMode;
import javafx.scene.control.TableView;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;

/**
 * Service for managing table selection operations.
 *
 * @param <T> The type of items in the table
 */
@Service
public class TableSelectionService<T> {
    
    private final ObjectProperty<SelectionMode> selectionMode = new SimpleObjectProperty<>(SelectionMode.SINGLE);
    private TableView<T> tableView;
    private final List<Consumer<T>> selectionListeners = new ArrayList<>();
    private final List<Consumer<List<T>>> multiSelectionListeners = new ArrayList<>();
    
    /**
     * Initializes the selection service with the table view.
     *
     * @param tableView The table view
     */
    public void initialize(TableView<T> tableView) {
        this.tableView = tableView;
        
        // Setup selection mode binding
        selectionMode.addListener((obs, oldVal, newVal) -> {
            if (newVal != null) {
                tableView.getSelectionModel().setSelectionMode(newVal);
            }
        });
        
        // Initialize with current selection mode
        tableView.getSelectionModel().setSelectionMode(selectionMode.get());
        
        // Setup selection listeners
        tableView.getSelectionModel().selectedItemProperty().addListener((obs, oldVal, newVal) -> {
            if (newVal != null) {
                notifySelectionListeners(newVal);
            }
        });
        
        tableView.getSelectionModel().getSelectedItems().addListener(
            (javafx.collections.ListChangeListener<T>) change -> {
                notifyMultiSelectionListeners(new ArrayList<>(change.getList()));
            }
        );
    }
    
    /**
     * Sets the selection mode for the table.
     *
     * @param mode The selection mode
     */
    public void setSelectionMode(SelectionMode mode) {
        selectionMode.set(mode);
    }
    
    /**
     * Gets the selection mode for the table.
     *
     * @return The selection mode
     */
    public SelectionMode getSelectionMode() {
        return selectionMode.get();
    }
    
    /**
     * Gets the selection mode property.
     *
     * @return The selection mode property
     */
    public ObjectProperty<SelectionMode> selectionModeProperty() {
        return selectionMode;
    }
    
    /**
     * Gets the selected item.
     *
     * @return The selected item, or null if none is selected
     */
    public T getSelectedItem() {
        return tableView != null ? tableView.getSelectionModel().getSelectedItem() : null;
    }
    
    /**
     * Gets the list of selected items.
     *
     * @return The list of selected items
     */
    public ObservableList<T> getSelectedItems() {
        return tableView != null ? tableView.getSelectionModel().getSelectedItems() : null;
    }
    
    /**
     * Selects an item in the table.
     *
     * @param item The item to select
     */
    public void select(T item) {
        if (tableView != null) {
            tableView.getSelectionModel().select(item);
        }
    }
    
    /**
     * Selects an item by index.
     *
     * @param index The index of the item to select
     */
    public void select(int index) {
        if (tableView != null) {
            tableView.getSelectionModel().select(index);
        }
    }
    
    /**
     * Selects multiple items.
     *
     * @param items The items to select
     */
    public void selectAll(List<T> items) {
        if (tableView != null && selectionMode.get() == SelectionMode.MULTIPLE) {
            tableView.getSelectionModel().clearSelection();
            for (T item : items) {
                tableView.getSelectionModel().select(item);
            }
        }
    }
    
    /**
     * Clears the selection.
     */
    public void clearSelection() {
        if (tableView != null) {
            tableView.getSelectionModel().clearSelection();
        }
    }
    
    /**
     * Selects all items in the table.
     */
    public void selectAll() {
        if (tableView != null && selectionMode.get() == SelectionMode.MULTIPLE) {
            tableView.getSelectionModel().selectAll();
        }
    }
    
    /**
     * Checks if an item is selected.
     *
     * @param item The item to check
     * @return True if selected
     */
    public boolean isSelected(T item) {
        return tableView != null && tableView.getSelectionModel().isSelected(
            tableView.getItems().indexOf(item));
    }
    
    /**
     * Gets the selected index.
     *
     * @return The selected index, or -1 if none
     */
    public int getSelectedIndex() {
        return tableView != null ? tableView.getSelectionModel().getSelectedIndex() : -1;
    }
    
    /**
     * Gets the selected indices.
     *
     * @return The list of selected indices
     */
    public ObservableList<Integer> getSelectedIndices() {
        return tableView != null ? tableView.getSelectionModel().getSelectedIndices() : null;
    }
    
    /**
     * Adds a selection listener.
     *
     * @param listener The listener to add
     */
    public void addSelectionListener(Consumer<T> listener) {
        selectionListeners.add(listener);
    }
    
    /**
     * Removes a selection listener.
     *
     * @param listener The listener to remove
     */
    public void removeSelectionListener(Consumer<T> listener) {
        selectionListeners.remove(listener);
    }
    
    /**
     * Adds a multi-selection listener.
     *
     * @param listener The listener to add
     */
    public void addMultiSelectionListener(Consumer<List<T>> listener) {
        multiSelectionListeners.add(listener);
    }
    
    /**
     * Removes a multi-selection listener.
     *
     * @param listener The listener to remove
     */
    public void removeMultiSelectionListener(Consumer<List<T>> listener) {
        multiSelectionListeners.remove(listener);
    }
    
    /**
     * Notifies selection listeners.
     *
     * @param item The selected item
     */
    private void notifySelectionListeners(T item) {
        for (Consumer<T> listener : selectionListeners) {
            listener.accept(item);
        }
    }
    
    /**
     * Notifies multi-selection listeners.
     *
     * @param items The selected items
     */
    private void notifyMultiSelectionListeners(List<T> items) {
        for (Consumer<List<T>> listener : multiSelectionListeners) {
            listener.accept(items);
        }
    }
    
    /**
     * Focuses on the selected item.
     */
    public void focusSelected() {
        if (tableView != null) {
            int index = tableView.getSelectionModel().getSelectedIndex();
            if (index >= 0) {
                tableView.getFocusModel().focus(index);
                tableView.scrollTo(index);
            }
        }
    }
}