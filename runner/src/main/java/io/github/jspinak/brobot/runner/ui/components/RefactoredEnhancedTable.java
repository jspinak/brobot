package io.github.jspinak.brobot.runner.ui.components;

import io.github.jspinak.brobot.runner.ui.components.table.services.*;
import javafx.beans.property.ObjectProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.collections.transformation.FilteredList;
import javafx.collections.transformation.SortedList;
import javafx.scene.control.*;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.util.Callback;
import lombok.Getter;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.Comparator;
import java.util.function.Function;
import java.util.function.Predicate;

/**
 * Refactored enhanced table component that delegates responsibilities to specialized services.
 *
 * @param <T> The type of items in the table
 */
@Component
public class RefactoredEnhancedTable<T> extends VBox {
    
    // The underlying TableView
    @Getter
    private final TableView<T> tableView;
    
    // Data
    @Getter
    private final ObservableList<T> items = FXCollections.observableArrayList();
    private FilteredList<T> filteredItems;
    private SortedList<T> sortedItems;
    
    // UI Components
    private TextField searchField;
    private Pagination pagination;
    private ComboBox<Integer> pageSizeSelector;
    
    // Services
    private final TableColumnService<T> columnService;
    private final TableFilterService<T> filterService;
    private final TablePaginationService<T> paginationService;
    private final TableSortingService<T> sortingService;
    private final TableSelectionService<T> selectionService;
    private final EnhancedTableUIFactory uiFactory;
    
    /**
     * Creates a new RefactoredEnhancedTable using default service instances.
     */
    public RefactoredEnhancedTable() {
        this(new TableColumnService<>(),
             new TableFilterService<>(),
             new TablePaginationService<>(),
             new TableSortingService<>(),
             new TableSelectionService<>(),
             new EnhancedTableUIFactory());
    }
    
    /**
     * Creates a new RefactoredEnhancedTable with injected services.
     */
    @Autowired
    public RefactoredEnhancedTable(
            TableColumnService<T> columnService,
            TableFilterService<T> filterService,
            TablePaginationService<T> paginationService,
            TableSortingService<T> sortingService,
            TableSelectionService<T> selectionService,
            EnhancedTableUIFactory uiFactory) {
        
        this.columnService = columnService;
        this.filterService = filterService;
        this.paginationService = paginationService;
        this.sortingService = sortingService;
        this.selectionService = selectionService;
        this.uiFactory = uiFactory;
        
        // Create UI components
        tableView = uiFactory.createTableView();
        
        // Initialize the table
        initializeTable();
    }
    
    /**
     * Initializes the table components and services.
     */
    private void initializeTable() {
        // Create UI components
        EnhancedTableUIFactory.ToolbarComponents toolbarComponents = uiFactory.createToolbarComponents();
        searchField = toolbarComponents.getSearchField();
        pageSizeSelector = toolbarComponents.getPageSizeSelector();
        HBox toolbar = toolbarComponents.getToolbar();
        
        pagination = uiFactory.createPagination();
        HBox paginationBox = uiFactory.createPaginationBox(pagination);
        
        // Initialize services
        filteredItems = filterService.initializeFiltering(items);
        sortedItems = sortingService.initializeSorting(filteredItems);
        
        filterService.setSearchField(searchField);
        filterService.setOnFilterChanged(() -> {
            paginationService.goToFirstPage();
            paginationService.updatePagination();
        });
        
        sortingService.bindToTableView(tableView);
        paginationService.initialize(pagination, pageSizeSelector, tableView);
        paginationService.setSortedItems(sortedItems);
        selectionService.initialize(tableView);
        
        // Assemble UI
        uiFactory.assembleTableUI(this, toolbar, tableView, paginationBox);
        
        // Initialize data
        paginationService.updatePagination();
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
        paginationService.updatePagination();
    }
    
    /**
     * Sets the search provider function.
     *
     * @param searchProvider The search provider function
     */
    public void setSearchProvider(Function<T, String> searchProvider) {
        filterService.setSearchProvider(searchProvider);
    }
    
    /**
     * Gets the search provider function.
     *
     * @return The search provider function
     */
    public Function<T, String> getSearchProvider() {
        return filterService.getSearchProvider();
    }
    
    /**
     * Gets the search provider property.
     *
     * @return The search provider property
     */
    public ObjectProperty<Function<T, String>> searchProviderProperty() {
        return filterService.searchProviderProperty();
    }
    
    /**
     * Sets the selection mode for the table.
     *
     * @param mode The selection mode
     */
    public void setSelectionMode(SelectionMode mode) {
        selectionService.setSelectionMode(mode);
    }
    
    /**
     * Gets the selection mode for the table.
     *
     * @return The selection mode
     */
    public SelectionMode getSelectionMode() {
        return selectionService.getSelectionMode();
    }
    
    /**
     * Gets the selection mode property.
     *
     * @return The selection mode property
     */
    public ObjectProperty<SelectionMode> selectionModeProperty() {
        return selectionService.selectionModeProperty();
    }
    
    /**
     * Sets the page size for pagination.
     *
     * @param pageSize The page size
     */
    public void setPageSize(int pageSize) {
        paginationService.setPageSize(pageSize);
    }
    
    /**
     * Gets the page size for pagination.
     *
     * @return The page size
     */
    public int getPageSize() {
        return paginationService.getPageSize();
    }
    
    /**
     * Gets the page size property.
     *
     * @return The page size property
     */
    public ObjectProperty<Integer> pageSizeProperty() {
        return paginationService.pageSizeProperty();
    }
    
    /**
     * Adds a column to the table.
     *
     * @param title The column title
     * @param propertyName The property name to bind to
     * @return The created column
     */
    public TableColumn<T, String> addColumn(String title, String propertyName) {
        return columnService.addStringColumn(tableView, title, propertyName);
    }
    
    /**
     * Adds a column with a custom cell factory.
     *
     * @param <S> The type of the column data
     * @param title The column title
     * @param cellFactory The cell factory for the column
     * @return The created column
     */
    public <S> TableColumn<T, S> addColumn(String title, 
            Callback<TableColumn.CellDataFeatures<T, S>, javafx.beans.value.ObservableValue<S>> cellFactory) {
        return columnService.addColumn(tableView, title, cellFactory);
    }
    
    /**
     * Adds a column with custom cell factory and cell value factory.
     *
     * @param <S> The type of the column data
     * @param title The column title
     * @param valueFactory The cell value factory
     * @param cellFactory The cell factory
     * @return The created column
     */
    public <S> TableColumn<T, S> addColumn(String title,
            Callback<TableColumn.CellDataFeatures<T, S>, javafx.beans.value.ObservableValue<S>> valueFactory,
            Callback<TableColumn<T, S>, TableCell<T, S>> cellFactory) {
        return columnService.addColumn(tableView, title, valueFactory, cellFactory);
    }
    
    /**
     * Sets the title of a column.
     *
     * @param column The column
     * @param title The new title
     */
    public void setColumnTitle(TableColumn<T, ?> column, String title) {
        columnService.setColumnTitle(column, title);
    }
    
    /**
     * Removes a column from the table.
     *
     * @param column The column to remove
     */
    public void removeColumn(TableColumn<T, ?> column) {
        columnService.removeColumn(tableView, column);
    }
    
    /**
     * Clears all columns from the table.
     */
    public void clearColumns() {
        columnService.clearColumns(tableView);
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
        return selectionService.getSelectedItem();
    }
    
    /**
     * Gets the list of selected items.
     *
     * @return The list of selected items
     */
    public ObservableList<T> getSelectedItems() {
        return selectionService.getSelectedItems();
    }
    
    /**
     * Sets a custom filter predicate.
     *
     * @param predicate The filter predicate
     */
    public void setFilter(Predicate<T> predicate) {
        filterService.setFilter(predicate);
    }
    
    /**
     * Clears the filter.
     */
    public void clearFilter() {
        filterService.clearFilter();
    }
    
    /**
     * Sets the sort order for the table.
     *
     * @param <S> The type of the column data
     * @param column The column to sort by
     * @param ascending True for ascending order, false for descending
     */
    public <S> void setSortOrder(TableColumn<T, S> column, boolean ascending) {
        sortingService.setSortOrder(column, ascending);
    }
    
    /**
     * Sets a comparator for a specific column.
     *
     * @param <S> The type of the column data
     * @param column The column
     * @param comparator The comparator
     */
    public <S> void setColumnComparator(TableColumn<T, S> column, Comparator<S> comparator) {
        columnService.setColumnComparator(column, comparator);
    }
    
    /**
     * Goes to the first page of the pagination.
     */
    public void goToFirstPage() {
        paginationService.goToFirstPage();
    }
    
    /**
     * Goes to the last page of the pagination.
     */
    public void goToLastPage() {
        paginationService.goToLastPage();
    }
    
    /**
     * Goes to a specific page of the pagination.
     *
     * @param pageIndex The page index
     */
    public void goToPage(int pageIndex) {
        paginationService.goToPage(pageIndex);
    }
    
    /**
     * Refreshes the table display.
     */
    public void refresh() {
        tableView.refresh();
    }
}