package io.github.jspinak.brobot.runner.ui.components.table.services;

import static org.junit.jupiter.api.Assertions.*;

import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;
import javafx.application.Platform;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.collections.transformation.FilteredList;
import javafx.collections.transformation.SortedList;
import javafx.embed.swing.JFXPanel;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Pagination;
import javafx.scene.control.TableView;

import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

class TablePaginationServiceTest {

    private TablePaginationService<String> paginationService;
    private Pagination pagination;
    private ComboBox<Integer> pageSizeSelector;
    private TableView<String> tableView;
    private ObservableList<String> items;
    private SortedList<String> sortedList;

    @BeforeAll
    static void initJFX() throws InterruptedException {
        final CountDownLatch latch = new CountDownLatch(1);
        Platform.startup(
                () -> {
                    new JFXPanel(); // initializes JavaFX environment
                    latch.countDown();
                });

        if (!latch.await(5, TimeUnit.SECONDS)) {
            throw new RuntimeException("Could not initialize JavaFX");
        }
    }

    @BeforeEach
    void setUp() throws InterruptedException {
        CountDownLatch latch = new CountDownLatch(1);
        Platform.runLater(
                () -> {
                    paginationService = new TablePaginationService<>();
                    pagination = new Pagination();
                    pageSizeSelector = new ComboBox<>();
                    tableView = new TableView<>();

                    // Create test data
                    items = FXCollections.observableArrayList();
                    for (int i = 1; i <= 100; i++) {
                        items.add("Item " + i);
                    }

                    FilteredList<String> filteredList = new FilteredList<>(items);
                    sortedList = new SortedList<>(filteredList);

                    latch.countDown();
                });
        latch.await(5, TimeUnit.SECONDS);
    }

    @Test
    void testInitialize() throws InterruptedException {
        CountDownLatch latch = new CountDownLatch(1);
        Platform.runLater(
                () -> {
                    paginationService.initialize(pagination, pageSizeSelector, tableView);

                    // Check page size selector is populated
                    assertTrue(pageSizeSelector.getItems().contains(10));
                    assertTrue(pageSizeSelector.getItems().contains(25));
                    assertTrue(pageSizeSelector.getItems().contains(50));
                    assertTrue(pageSizeSelector.getItems().contains(100));

                    // Check default page size
                    assertEquals(25, pageSizeSelector.getValue());
                    assertEquals(25, paginationService.getPageSize());

                    latch.countDown();
                });
        latch.await(5, TimeUnit.SECONDS);
    }

    @Test
    void testUpdatePagination() throws InterruptedException {
        CountDownLatch latch = new CountDownLatch(1);
        Platform.runLater(
                () -> {
                    paginationService.initialize(pagination, pageSizeSelector, tableView);
                    paginationService.setSortedItems(sortedList);

                    // With 100 items and page size 25, should have 4 pages
                    assertEquals(4, pagination.getPageCount());

                    // Change page size to 10
                    paginationService.setPageSize(10);
                    assertEquals(10, paginationService.getPageSize());

                    // Should now have 10 pages
                    assertEquals(10, pagination.getPageCount());

                    latch.countDown();
                });
        latch.await(5, TimeUnit.SECONDS);
    }

    @Test
    void testGoToFirstPage() throws InterruptedException {
        CountDownLatch latch = new CountDownLatch(1);
        Platform.runLater(
                () -> {
                    paginationService.initialize(pagination, pageSizeSelector, tableView);
                    paginationService.setSortedItems(sortedList);

                    // Go to page 2
                    paginationService.goToPage(2);
                    assertEquals(2, pagination.getCurrentPageIndex());

                    // Go to first page
                    paginationService.goToFirstPage();
                    assertEquals(0, pagination.getCurrentPageIndex());

                    latch.countDown();
                });
        latch.await(5, TimeUnit.SECONDS);
    }

    @Test
    void testGoToLastPage() throws InterruptedException {
        CountDownLatch latch = new CountDownLatch(1);
        Platform.runLater(
                () -> {
                    paginationService.initialize(pagination, pageSizeSelector, tableView);
                    paginationService.setSortedItems(sortedList);

                    // Go to last page
                    paginationService.goToLastPage();
                    assertEquals(
                            3, pagination.getCurrentPageIndex()); // 0-indexed, so 3 is the 4th page

                    latch.countDown();
                });
        latch.await(5, TimeUnit.SECONDS);
    }

    @Test
    void testGoToSpecificPage() throws InterruptedException {
        CountDownLatch latch = new CountDownLatch(1);
        Platform.runLater(
                () -> {
                    paginationService.initialize(pagination, pageSizeSelector, tableView);
                    paginationService.setSortedItems(sortedList);

                    // Go to page 2 (0-indexed)
                    paginationService.goToPage(2);
                    assertEquals(2, pagination.getCurrentPageIndex());

                    // Try to go to invalid page
                    paginationService.goToPage(10);
                    assertEquals(
                            2, pagination.getCurrentPageIndex()); // Should stay on current page

                    // Try negative page
                    paginationService.goToPage(-1);
                    assertEquals(
                            2, pagination.getCurrentPageIndex()); // Should stay on current page

                    latch.countDown();
                });
        latch.await(5, TimeUnit.SECONDS);
    }

    @Test
    void testGoToNextPage() throws InterruptedException {
        CountDownLatch latch = new CountDownLatch(1);
        Platform.runLater(
                () -> {
                    paginationService.initialize(pagination, pageSizeSelector, tableView);
                    paginationService.setSortedItems(sortedList);

                    assertEquals(0, pagination.getCurrentPageIndex());

                    // Go to next page
                    paginationService.goToNextPage();
                    assertEquals(1, pagination.getCurrentPageIndex());

                    // Go to last page and try to go next
                    paginationService.goToLastPage();
                    int lastPage = pagination.getCurrentPageIndex();
                    paginationService.goToNextPage();
                    assertEquals(
                            lastPage, pagination.getCurrentPageIndex()); // Should stay on last page

                    latch.countDown();
                });
        latch.await(5, TimeUnit.SECONDS);
    }

    @Test
    void testGoToPreviousPage() throws InterruptedException {
        CountDownLatch latch = new CountDownLatch(1);
        Platform.runLater(
                () -> {
                    paginationService.initialize(pagination, pageSizeSelector, tableView);
                    paginationService.setSortedItems(sortedList);

                    // Go to page 2
                    paginationService.goToPage(2);
                    assertEquals(2, pagination.getCurrentPageIndex());

                    // Go to previous page
                    paginationService.goToPreviousPage();
                    assertEquals(1, pagination.getCurrentPageIndex());

                    // Go to first page and try to go previous
                    paginationService.goToFirstPage();
                    paginationService.goToPreviousPage();
                    assertEquals(0, pagination.getCurrentPageIndex()); // Should stay on first page

                    latch.countDown();
                });
        latch.await(5, TimeUnit.SECONDS);
    }

    @Test
    void testGetCurrentPageRange() throws InterruptedException {
        CountDownLatch latch = new CountDownLatch(1);
        Platform.runLater(
                () -> {
                    paginationService.initialize(pagination, pageSizeSelector, tableView);
                    paginationService.setSortedItems(sortedList);
                    paginationService.setPageSize(25);

                    // First page (0-24)
                    int[] range = paginationService.getCurrentPageRange();
                    assertEquals(0, range[0]);
                    assertEquals(25, range[1]);

                    // Second page (25-49)
                    paginationService.goToPage(1);
                    range = paginationService.getCurrentPageRange();
                    assertEquals(25, range[0]);
                    assertEquals(50, range[1]);

                    // Last page (75-99)
                    paginationService.goToLastPage();
                    range = paginationService.getCurrentPageRange();
                    assertEquals(75, range[0]);
                    assertEquals(100, range[1]);

                    latch.countDown();
                });
        latch.await(5, TimeUnit.SECONDS);
    }

    @Test
    void testPageSizeProperty() throws InterruptedException {
        CountDownLatch latch = new CountDownLatch(1);
        Platform.runLater(
                () -> {
                    paginationService.initialize(pagination, pageSizeSelector, tableView);

                    // Test property binding
                    boolean[] propertyChanged = {false};
                    paginationService
                            .pageSizeProperty()
                            .addListener(
                                    (obs, oldVal, newVal) -> {
                                        propertyChanged[0] = true;
                                    });

                    paginationService.setPageSize(50);
                    assertTrue(propertyChanged[0]);
                    assertEquals(50, paginationService.getPageSize());

                    latch.countDown();
                });
        latch.await(5, TimeUnit.SECONDS);
    }

    @Test
    void testGetTotalItemCount() throws InterruptedException {
        CountDownLatch latch = new CountDownLatch(1);
        Platform.runLater(
                () -> {
                    paginationService.initialize(pagination, pageSizeSelector, tableView);
                    paginationService.setSortedItems(sortedList);

                    assertEquals(100, paginationService.getTotalItemCount());

                    // Test with no items set
                    TablePaginationService<String> emptyService = new TablePaginationService<>();
                    assertEquals(0, emptyService.getTotalItemCount());

                    latch.countDown();
                });
        latch.await(5, TimeUnit.SECONDS);
    }

    @Test
    void testTableItemsUpdate() throws InterruptedException {
        CountDownLatch latch = new CountDownLatch(1);
        Platform.runLater(
                () -> {
                    paginationService.initialize(pagination, pageSizeSelector, tableView);
                    paginationService.setSortedItems(sortedList);
                    paginationService.setPageSize(10);

                    // First page should show items 1-10
                    assertEquals(10, tableView.getItems().size());
                    assertEquals("Item 1", tableView.getItems().get(0));
                    assertEquals("Item 10", tableView.getItems().get(9));

                    // Go to second page
                    paginationService.goToPage(1);
                    assertEquals(10, tableView.getItems().size());
                    assertEquals("Item 11", tableView.getItems().get(0));
                    assertEquals("Item 20", tableView.getItems().get(9));

                    latch.countDown();
                });
        latch.await(5, TimeUnit.SECONDS);
    }
}
