package io.github.jspinak.brobot.runner.ui.components;

import javafx.beans.property.ObjectProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.collections.transformation.FilteredList;
import javafx.collections.transformation.SortedList;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.VBox;
import javafx.util.Callback;
import lombok.Getter;
import atlantafx.base.theme.Styles;

import java.util.Comparator;
import java.util.HashMap;
import java.util.Map;
import java.util.function.Function;
import java.util.function.Predicate;

/**
 * An enhanced table component with filtering, sorting, and pagination.
 *
 * @param <T> The type of items in the table
 */
public class EnhancedTable<T> extends VBox {

    // The underlying TableView
    @Getter
    private final TableView<T> tableView;

    // Data and filtered/sorted wrappers
    @Getter
    private final ObservableList<T> items = FXCollections.observableArrayList();
    private final FilteredList<T> filteredItems = new FilteredList<>(items);
    private final SortedList<T> sortedItems = new SortedList<>(filteredItems);

    // Search field
    private final TextField searchField;

    // Pagination
    private final Pagination pagination;
    private final ComboBox<Integer> pageSizeSelector;
    private final ObjectProperty<Integer> pageSize = new SimpleObjectProperty<>(25);
    private final ObjectProperty<Function<T, String>> searchProvider = new SimpleObjectProperty<>();

    // Selection mode
    private final ObjectProperty<SelectionMode> selectionMode = new SimpleObjectProperty<>(SelectionMode.SINGLE);

    // Column configuration helpers
    private final Map<TableColumn<T, ?>, StringProperty> columnTitles = new HashMap<>();

    /**
     * Creates a new EnhancedTable.
     */
    public EnhancedTable() {
        // Create the table view
        tableView = new TableView<>();
        tableView.getStyleClass().addAll(Styles.STRIPED, Styles.BORDERED);
        tableView.setPlaceholder(new Label("No data available"));

        // Setup search field
        searchField = new TextField();
        searchField.setPromptText("Search...");
        searchField.textProperty().addListener((obs, oldVal, newVal) -> {
            updateFilter();
            goToFirstPage();
        });

        // Setup page size selector
        pageSizeSelector = new ComboBox<>();
        pageSizeSelector.getItems().addAll(10, 25, 50, 100);
        pageSizeSelector.setValue(25);
        pageSizeSelector.valueProperty().bindBidirectional(pageSize);
        pageSizeSelector.valueProperty().addListener((obs, oldVal, newVal) -> updatePagination());

        // Setup pagination
        pagination = new Pagination();
        pagination.setPageCount(1);
        pagination.currentPageIndexProperty().addListener((obs, oldVal, newVal) -> updateTableItems());

        // Setup layouts
        HBox toolBar = new HBox(10);
        toolBar.setAlignment(Pos.CENTER_LEFT);
        toolBar.setPadding(new Insets(5, 10, 5, 10));

        Label searchLabel = new Label("Search:");
        toolBar.getChildren().addAll(searchLabel, searchField);

        // Add spacer to push pagination controls to the right
        javafx.scene.layout.Region spacer = new javafx.scene.layout.Region();
        HBox.setHgrow(spacer, Priority.ALWAYS);
        toolBar.getChildren().add(spacer);

        // Add page size selector
        Label pageSizeLabel = new Label("Items per page:");
        toolBar.getChildren().addAll(pageSizeLabel, pageSizeSelector);

        HBox paginationBox = new HBox(pagination);
        paginationBox.setAlignment(Pos.CENTER);

        VBox.setVgrow(tableView, Priority.ALWAYS);
        getChildren().addAll(toolBar, tableView, paginationBox);

        // Bind sorted items to the table view
        sortedItems.comparatorProperty().bind(tableView.comparatorProperty());

        // Setup selection mode binding
        selectionMode.addListener((obs, oldVal, newVal) -> {
            if (newVal != null) {
                tableView.getSelectionModel().setSelectionMode(newVal);
            }
        });

        // Initialize with current selection mode
        tableView.getSelectionModel().setSelectionMode(selectionMode.get());

        // Setup initial data
        updatePagination();
    }

    /**
     * Sets the items in the table.
     *
     * @param items The items to display
     */
    public void setItems(ObservableList<T> items) {
        this.items.clear();
        if (items != null) {
            this.items.addAll(items);
        }
        updatePagination();
    }

    /**
     * Sets the search provider function that extracts searchable text from an item.
     *
     * @param searchProvider The search provider function
     */
    public void setSearchProvider(Function<T, String> searchProvider) {
        this.searchProvider.set(searchProvider);
        updateFilter();
    }

    /**
     * Gets the search provider function.
     *
     * @return The search provider function
     */
    public Function<T, String> getSearchProvider() {
        return searchProvider.get();
    }

    /**
     * Gets the search provider property.
     *
     * @return The search provider property
     */
    public ObjectProperty<Function<T, String>> searchProviderProperty() {
        return searchProvider;
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
     * Sets the page size for pagination.
     *
     * @param pageSize The page size
     */
    public void setPageSize(int pageSize) {
        this.pageSize.set(pageSize);
    }

    /**
     * Gets the page size for pagination.
     *
     * @return The page size
     */
    public int getPageSize() {
        return pageSize.get();
    }

    /**
     * Gets the page size property.
     *
     * @return The page size property
     */
    public ObjectProperty<Integer> pageSizeProperty() {
        return pageSize;
    }

    /**
     * Adds a column to the table.
     *
     * @param title The column title
     * @param propertyName The property name to bind to
     * @return The created column
     */
    public TableColumn<T, String> addColumn(String title, String propertyName) {
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
     * Adds a column to the table with a custom cell factory.
     *
     * @param title The column title
     * @param cellFactory The cell factory for the column
     * @return The created column
     */
    public <S> TableColumn<T, S> addColumn(String title, Callback<TableColumn.CellDataFeatures<T, S>, javafx.beans.value.ObservableValue<S>> cellFactory) {
        TableColumn<T, S> column = new TableColumn<>();

        // Create a property for the title to allow dynamic updates
        StringProperty titleProperty = new SimpleStringProperty(title);
        column.textProperty().bind(titleProperty);
        columnTitles.put(column, titleProperty);

        // Set the cell value factory
        column.setCellValueFactory(cellFactory);

        // Add the column to the table
        tableView.getColumns().add(column);

        return column;
    }

    /**
     * Adds a column with a custom cell factory and cell value factory.
     *
     * @param title The column title
     * @param valueFactory The cell value factory
     * @param cellFactory The cell factory
     * @return The created column
     */
    public <S> TableColumn<T, S> addColumn(String title,
                                           Callback<TableColumn.CellDataFeatures<T, S>, javafx.beans.value.ObservableValue<S>> valueFactory,
                                           Callback<TableColumn<T, S>, TableCell<T, S>> cellFactory) {
        TableColumn<T, S> column = addColumn(title, valueFactory);
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
     * @param column The column to remove
     */
    public void removeColumn(TableColumn<T, ?> column) {
        tableView.getColumns().remove(column);
        columnTitles.remove(column);
    }

    /**
     * Clears all columns from the table.
     */
    public void clearColumns() {
        tableView.getColumns().clear();
        columnTitles.clear();
    }

    /**
     * Gets the selection model for the table.
     *
     * @return The selection model
     */
    public TableView.TableViewSelectionModel<T> getSelectionModel() {
        return tableView.getSelectionModel();
    }

    /**
     * Gets the selected item.
     *
     * @return The selected item, or null if none is selected
     */
    public T getSelectedItem() {
        return tableView.getSelectionModel().getSelectedItem();
    }

    /**
     * Gets the list of selected items.
     *
     * @return The list of selected items
     */
    public ObservableList<T> getSelectedItems() {
        return tableView.getSelectionModel().getSelectedItems();
    }

    /**
     * Updates the filter based on the search text.
     */
    private void updateFilter() {
        String searchText = searchField.getText().toLowerCase();
        Function<T, String> provider = searchProvider.get();

        if (searchText.isEmpty()) {
            filteredItems.setPredicate(null);
        } else if (provider != null) {
            filteredItems.setPredicate(item -> {
                String searchString = provider.apply(item);
                return searchString != null && searchString.toLowerCase().contains(searchText);
            });
        }

        updatePagination();
    }

    /**
     * Sets a custom filter predicate.
     *
     * @param predicate The filter predicate
     */
    public void setFilter(Predicate<T> predicate) {
        filteredItems.setPredicate(predicate);
        updatePagination();
    }

    /**
     * Clears the filter.
     */
    public void clearFilter() {
        searchField.clear();
        filteredItems.setPredicate(null);
        updatePagination();
    }

    /**
     * Updates the pagination based on the current number of filtered items.
     */
    private void updatePagination() {
        int itemCount = filteredItems.size();
        int pageCount = itemCount / pageSize.get();
        if (itemCount % pageSize.get() > 0) {
            pageCount++;
        }

        pagination.setPageCount(Math.max(1, pageCount));

        // Ensure current page is still valid
        if (pagination.getCurrentPageIndex() >= pageCount) {
            pagination.setCurrentPageIndex(Math.max(0, pageCount - 1));
        }

        updateTableItems();
    }

    /**
     * Updates the items shown in the table based on the current page.
     */
    private void updateTableItems() {
        int pageIndex = pagination.getCurrentPageIndex();
        int fromIndex = pageIndex * pageSize.get();
        int toIndex = Math.min(fromIndex + pageSize.get(), filteredItems.size());

        // Create a list view of the current page
        ObservableList<T> pageItems;
        if (fromIndex < toIndex) {
            pageItems = FXCollections.observableArrayList(
                    sortedItems.subList(fromIndex, toIndex));
        } else {
            pageItems = FXCollections.observableArrayList();
        }

        tableView.setItems(pageItems);
    }

    /**
     * Sets the sort order for the table.
     *
     * @param column The column to sort by
     * @param ascending True for ascending order, false for descending
     */
    public <S> void setSortOrder(TableColumn<T, S> column, boolean ascending) {
        tableView.getSortOrder().clear();
        column.setSortType(ascending ? TableColumn.SortType.ASCENDING : TableColumn.SortType.DESCENDING);
        tableView.getSortOrder().add(column);
        tableView.sort();
    }

    /**
     * Sets a comparator for a specific column.
     *
     * @param column The column
     * @param comparator The comparator
     */
    public <S> void setColumnComparator(TableColumn<T, S> column, Comparator<S> comparator) {
        column.setComparator(comparator);
    }

    /**
     * Goes to the first page of the pagination.
     */
    public void goToFirstPage() {
        pagination.setCurrentPageIndex(0);
    }

    /**
     * Goes to the last page of the pagination.
     */
    public void goToLastPage() {
        pagination.setCurrentPageIndex(pagination.getPageCount() - 1);
    }

    /**
     * Goes to a specific page of the pagination.
     *
     * @param pageIndex The page index
     */
    public void goToPage(int pageIndex) {
        if (pageIndex >= 0 && pageIndex < pagination.getPageCount()) {
            pagination.setCurrentPageIndex(pageIndex);
        }
    }

    /**
     * Refreshes the table display.
     */
    public void refresh() {
        tableView.refresh();
    }
}