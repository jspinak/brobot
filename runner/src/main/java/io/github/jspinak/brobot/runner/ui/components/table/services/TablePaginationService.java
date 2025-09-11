package io.github.jspinak.brobot.runner.ui.components.table.services;

import javafx.beans.property.IntegerProperty;
import javafx.beans.property.ObjectProperty;
import javafx.beans.property.SimpleIntegerProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.collections.transformation.SortedList;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Pagination;
import javafx.scene.control.TableView;

import org.springframework.stereotype.Service;

/**
 * Service for managing table pagination.
 *
 * @param <T> The type of items in the table
 */
@Service
public class TablePaginationService<T> {

    private final ObjectProperty<Integer> pageSize = new SimpleObjectProperty<>(25);
    private final IntegerProperty currentPage = new SimpleIntegerProperty(0);

    private Pagination pagination;
    private ComboBox<Integer> pageSizeSelector;
    private TableView<T> tableView;
    private SortedList<T> sortedItems;

    /**
     * Initializes the pagination controls.
     *
     * @param pagination The pagination control
     * @param pageSizeSelector The page size selector
     * @param tableView The table view
     */
    public void initialize(
            Pagination pagination, ComboBox<Integer> pageSizeSelector, TableView<T> tableView) {
        this.pagination = pagination;
        this.pageSizeSelector = pageSizeSelector;
        this.tableView = tableView;

        // Setup page size selector
        pageSizeSelector.getItems().addAll(10, 25, 50, 100);
        pageSizeSelector.setValue(pageSize.get());
        pageSizeSelector.valueProperty().bindBidirectional(pageSize);

        // Setup pagination
        pagination.setPageCount(1);
        pagination
                .currentPageIndexProperty()
                .addListener(
                        (obs, oldVal, newVal) -> {
                            currentPage.set(newVal.intValue());
                            updateTableItems();
                        });

        // Listen to page size changes
        pageSize.addListener((obs, oldVal, newVal) -> updatePagination());
    }

    /**
     * Sets the sorted list to paginate.
     *
     * @param sortedItems The sorted list
     */
    public void setSortedItems(SortedList<T> sortedItems) {
        this.sortedItems = sortedItems;
        updatePagination();
    }

    /** Updates the pagination based on the current number of items. */
    public void updatePagination() {
        if (sortedItems == null) {
            return;
        }

        int itemCount = sortedItems.size();
        int pageCount = calculatePageCount(itemCount);

        pagination.setPageCount(Math.max(1, pageCount));

        // Ensure current page is still valid
        if (pagination.getCurrentPageIndex() >= pageCount) {
            pagination.setCurrentPageIndex(Math.max(0, pageCount - 1));
        }

        updateTableItems();
    }

    /** Updates the items shown in the table based on the current page. */
    private void updateTableItems() {
        if (sortedItems == null || tableView == null) {
            return;
        }

        int pageIndex = pagination.getCurrentPageIndex();
        int fromIndex = pageIndex * pageSize.get();
        int toIndex = Math.min(fromIndex + pageSize.get(), sortedItems.size());

        // Create a list view of the current page
        ObservableList<T> pageItems;
        if (fromIndex < toIndex) {
            pageItems = FXCollections.observableArrayList(sortedItems.subList(fromIndex, toIndex));
        } else {
            pageItems = FXCollections.observableArrayList();
        }

        tableView.setItems(pageItems);
    }

    /**
     * Calculates the number of pages.
     *
     * @param itemCount The total number of items
     * @return The number of pages
     */
    private int calculatePageCount(int itemCount) {
        int size = pageSize.get();
        int pageCount = itemCount / size;
        if (itemCount % size > 0) {
            pageCount++;
        }
        return pageCount;
    }

    /**
     * Sets the page size.
     *
     * @param pageSize The page size
     */
    public void setPageSize(int pageSize) {
        this.pageSize.set(pageSize);
    }

    /**
     * Gets the page size.
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
     * Gets the current page index.
     *
     * @return The current page index
     */
    public int getCurrentPageIndex() {
        return pagination != null ? pagination.getCurrentPageIndex() : 0;
    }

    /** Goes to the first page. */
    public void goToFirstPage() {
        if (pagination != null) {
            pagination.setCurrentPageIndex(0);
        }
    }

    /** Goes to the last page. */
    public void goToLastPage() {
        if (pagination != null) {
            pagination.setCurrentPageIndex(pagination.getPageCount() - 1);
        }
    }

    /**
     * Goes to a specific page.
     *
     * @param pageIndex The page index
     */
    public void goToPage(int pageIndex) {
        if (pagination != null && pageIndex >= 0 && pageIndex < pagination.getPageCount()) {
            pagination.setCurrentPageIndex(pageIndex);
        }
    }

    /** Goes to the next page. */
    public void goToNextPage() {
        if (pagination != null) {
            int nextPage = pagination.getCurrentPageIndex() + 1;
            if (nextPage < pagination.getPageCount()) {
                pagination.setCurrentPageIndex(nextPage);
            }
        }
    }

    /** Goes to the previous page. */
    public void goToPreviousPage() {
        if (pagination != null) {
            int prevPage = pagination.getCurrentPageIndex() - 1;
            if (prevPage >= 0) {
                pagination.setCurrentPageIndex(prevPage);
            }
        }
    }

    /**
     * Gets the total number of pages.
     *
     * @return The page count
     */
    public int getPageCount() {
        return pagination != null ? pagination.getPageCount() : 0;
    }

    /**
     * Gets the total number of items.
     *
     * @return The item count
     */
    public int getTotalItemCount() {
        return sortedItems != null ? sortedItems.size() : 0;
    }

    /**
     * Gets the range of items shown on the current page.
     *
     * @return An array with [fromIndex, toIndex]
     */
    public int[] getCurrentPageRange() {
        if (sortedItems == null || pagination == null) {
            return new int[] {0, 0};
        }

        int pageIndex = pagination.getCurrentPageIndex();
        int fromIndex = pageIndex * pageSize.get();
        int toIndex = Math.min(fromIndex + pageSize.get(), sortedItems.size());

        return new int[] {fromIndex, toIndex};
    }
}
