package io.github.jspinak.brobot.runner.ui.components.table.services;

import static org.junit.jupiter.api.Assertions.*;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;
import java.util.function.Consumer;
import javafx.application.Platform;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.embed.swing.JFXPanel;
import javafx.scene.control.SelectionMode;
import javafx.scene.control.TableView;

import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

class TableSelectionServiceTest {

    private TableSelectionService<String> selectionService;
    private TableView<String> tableView;
    private ObservableList<String> items;

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
                    selectionService = new TableSelectionService<>();
                    tableView = new TableView<>();
                    items =
                            FXCollections.observableArrayList(
                                    "Item 1", "Item 2", "Item 3", "Item 4", "Item 5");
                    tableView.setItems(items);

                    latch.countDown();
                });
        latch.await(5, TimeUnit.SECONDS);
    }

    @Test
    void testInitialize() throws InterruptedException {
        CountDownLatch latch = new CountDownLatch(1);
        Platform.runLater(
                () -> {
                    selectionService.initialize(tableView);

                    // Default selection mode should be SINGLE
                    assertEquals(
                            SelectionMode.SINGLE, tableView.getSelectionModel().getSelectionMode());
                    assertEquals(SelectionMode.SINGLE, selectionService.getSelectionMode());

                    latch.countDown();
                });
        latch.await(5, TimeUnit.SECONDS);
    }

    @Test
    void testSetSelectionMode() throws InterruptedException {
        CountDownLatch latch = new CountDownLatch(1);
        Platform.runLater(
                () -> {
                    selectionService.initialize(tableView);

                    // Change to MULTIPLE
                    selectionService.setSelectionMode(SelectionMode.MULTIPLE);
                    assertEquals(
                            SelectionMode.MULTIPLE,
                            tableView.getSelectionModel().getSelectionMode());
                    assertEquals(SelectionMode.MULTIPLE, selectionService.getSelectionMode());

                    latch.countDown();
                });
        latch.await(5, TimeUnit.SECONDS);
    }

    @Test
    void testSelectItem() throws InterruptedException {
        CountDownLatch latch = new CountDownLatch(1);
        Platform.runLater(
                () -> {
                    selectionService.initialize(tableView);

                    // Select by item
                    selectionService.select("Item 2");
                    assertEquals("Item 2", selectionService.getSelectedItem());
                    assertEquals(1, selectionService.getSelectedIndex());

                    // Select by index
                    selectionService.select(3);
                    assertEquals("Item 4", selectionService.getSelectedItem());
                    assertEquals(3, selectionService.getSelectedIndex());

                    latch.countDown();
                });
        latch.await(5, TimeUnit.SECONDS);
    }

    @Test
    void testSelectMultipleItems() throws InterruptedException {
        CountDownLatch latch = new CountDownLatch(1);
        Platform.runLater(
                () -> {
                    selectionService.initialize(tableView);
                    selectionService.setSelectionMode(SelectionMode.MULTIPLE);

                    List<String> itemsToSelect = Arrays.asList("Item 1", "Item 3", "Item 5");
                    selectionService.selectAll(itemsToSelect);

                    ObservableList<String> selected = selectionService.getSelectedItems();
                    assertEquals(3, selected.size());
                    assertTrue(selected.contains("Item 1"));
                    assertTrue(selected.contains("Item 3"));
                    assertTrue(selected.contains("Item 5"));

                    latch.countDown();
                });
        latch.await(5, TimeUnit.SECONDS);
    }

    @Test
    void testClearSelection() throws InterruptedException {
        CountDownLatch latch = new CountDownLatch(1);
        Platform.runLater(
                () -> {
                    selectionService.initialize(tableView);

                    selectionService.select(2);
                    assertNotNull(selectionService.getSelectedItem());

                    selectionService.clearSelection();
                    assertNull(selectionService.getSelectedItem());
                    assertEquals(-1, selectionService.getSelectedIndex());

                    latch.countDown();
                });
        latch.await(5, TimeUnit.SECONDS);
    }

    @Test
    void testSelectAll() throws InterruptedException {
        CountDownLatch latch = new CountDownLatch(1);
        Platform.runLater(
                () -> {
                    selectionService.initialize(tableView);
                    selectionService.setSelectionMode(SelectionMode.MULTIPLE);

                    selectionService.selectAll();

                    ObservableList<String> selected = selectionService.getSelectedItems();
                    assertEquals(items.size(), selected.size());

                    latch.countDown();
                });
        latch.await(5, TimeUnit.SECONDS);
    }

    @Test
    void testIsSelected() throws InterruptedException {
        CountDownLatch latch = new CountDownLatch(1);
        Platform.runLater(
                () -> {
                    selectionService.initialize(tableView);

                    selectionService.select("Item 2");

                    assertTrue(selectionService.isSelected("Item 2"));
                    assertFalse(selectionService.isSelected("Item 1"));
                    assertFalse(selectionService.isSelected("Item 3"));

                    latch.countDown();
                });
        latch.await(5, TimeUnit.SECONDS);
    }

    @Test
    void testGetSelectedIndices() throws InterruptedException {
        CountDownLatch latch = new CountDownLatch(1);
        Platform.runLater(
                () -> {
                    selectionService.initialize(tableView);
                    selectionService.setSelectionMode(SelectionMode.MULTIPLE);

                    selectionService.select(0);
                    selectionService.select(2);
                    selectionService.select(4);

                    ObservableList<Integer> indices = selectionService.getSelectedIndices();
                    assertEquals(3, indices.size());
                    assertTrue(indices.contains(0));
                    assertTrue(indices.contains(2));
                    assertTrue(indices.contains(4));

                    latch.countDown();
                });
        latch.await(5, TimeUnit.SECONDS);
    }

    @Test
    void testSelectionListeners() throws InterruptedException {
        CountDownLatch latch = new CountDownLatch(1);
        Platform.runLater(
                () -> {
                    selectionService.initialize(tableView);

                    List<String> selectedItems = new ArrayList<>();
                    Consumer<String> listener = selectedItems::add;

                    selectionService.addSelectionListener(listener);

                    // Select items
                    selectionService.select("Item 1");
                    selectionService.select("Item 2");

                    // Should have been notified twice
                    assertEquals(2, selectedItems.size());
                    assertEquals("Item 1", selectedItems.get(0));
                    assertEquals("Item 2", selectedItems.get(1));

                    // Remove listener
                    selectionService.removeSelectionListener(listener);
                    selectionService.select("Item 3");

                    // Should still have only 2 items
                    assertEquals(2, selectedItems.size());

                    latch.countDown();
                });
        latch.await(5, TimeUnit.SECONDS);
    }

    @Test
    void testMultiSelectionListeners() throws InterruptedException {
        CountDownLatch latch = new CountDownLatch(1);
        Platform.runLater(
                () -> {
                    selectionService.initialize(tableView);
                    selectionService.setSelectionMode(SelectionMode.MULTIPLE);

                    List<List<String>> notifications = new ArrayList<>();
                    Consumer<List<String>> listener =
                            items -> notifications.add(new ArrayList<>(items));

                    selectionService.addMultiSelectionListener(listener);

                    // Select multiple items
                    selectionService.selectAll(Arrays.asList("Item 1", "Item 3"));

                    // Should have received notifications
                    assertTrue(notifications.size() > 0);

                    // Remove listener
                    selectionService.removeMultiSelectionListener(listener);
                    int notificationCount = notifications.size();

                    selectionService.select("Item 5");

                    // Should not have received new notifications
                    assertEquals(notificationCount, notifications.size());

                    latch.countDown();
                });
        latch.await(5, TimeUnit.SECONDS);
    }

    @Test
    void testFocusSelected() throws InterruptedException {
        CountDownLatch latch = new CountDownLatch(1);
        Platform.runLater(
                () -> {
                    selectionService.initialize(tableView);

                    // Select an item
                    selectionService.select(2);

                    // Focus on selected
                    selectionService.focusSelected();

                    // The focused index should match the selected index
                    assertEquals(2, tableView.getFocusModel().getFocusedIndex());

                    latch.countDown();
                });
        latch.await(5, TimeUnit.SECONDS);
    }

    @Test
    void testSelectionModeProperty() throws InterruptedException {
        CountDownLatch latch = new CountDownLatch(1);
        Platform.runLater(
                () -> {
                    selectionService.initialize(tableView);

                    boolean[] propertyChanged = {false};
                    selectionService
                            .selectionModeProperty()
                            .addListener(
                                    (obs, oldVal, newVal) -> {
                                        propertyChanged[0] = true;
                                    });

                    selectionService.setSelectionMode(SelectionMode.MULTIPLE);

                    assertTrue(propertyChanged[0]);
                    assertEquals(
                            SelectionMode.MULTIPLE, selectionService.selectionModeProperty().get());

                    latch.countDown();
                });
        latch.await(5, TimeUnit.SECONDS);
    }

    @Test
    void testSelectAllInSingleMode() throws InterruptedException {
        CountDownLatch latch = new CountDownLatch(1);
        Platform.runLater(
                () -> {
                    selectionService.initialize(tableView);
                    // Keep SINGLE selection mode

                    selectionService.selectAll();

                    // Should not select all items in SINGLE mode
                    ObservableList<String> selected = selectionService.getSelectedItems();
                    assertTrue(selected.size() <= 1);

                    latch.countDown();
                });
        latch.await(5, TimeUnit.SECONDS);
    }
}
