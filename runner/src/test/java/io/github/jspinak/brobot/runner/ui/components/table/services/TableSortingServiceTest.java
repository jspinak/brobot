package io.github.jspinak.brobot.runner.ui.components.table.services;

import javafx.application.Platform;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.collections.transformation.FilteredList;
import javafx.collections.transformation.SortedList;
import javafx.embed.swing.JFXPanel;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.cell.PropertyValueFactory;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.Comparator;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

import static org.junit.jupiter.api.Assertions.*;

class TableSortingServiceTest {
    
    private TableSortingService<TestItem> sortingService;
    private TableView<TestItem> tableView;
    private ObservableList<TestItem> items;
    private FilteredList<TestItem> filteredList;
    private SortedList<TestItem> sortedList;
    
    static class TestItem {
        private String name;
        private int value;
        
        public TestItem(String name, int value) {
            this.name = name;
            this.value = value;
        }
        
        public String getName() { return name; }
        public void setName(String name) { this.name = name; }
        public int getValue() { return value; }
        public void setValue(int value) { this.value = value; }
    }
    
    @BeforeAll
    static void initJFX() throws InterruptedException {
        final CountDownLatch latch = new CountDownLatch(1);
        Platform.startup(() -> {
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
        Platform.runLater(() -> {
            sortingService = new TableSortingService<>();
            tableView = new TableView<>();
            
            // Create test data
            items = FXCollections.observableArrayList(
                new TestItem("Charlie", 30),
                new TestItem("Alice", 10),
                new TestItem("Bob", 20),
                new TestItem("David", 40)
            );
            
            filteredList = new FilteredList<>(items);
            
            latch.countDown();
        });
        latch.await(5, TimeUnit.SECONDS);
    }
    
    @Test
    void testInitializeSorting() throws InterruptedException {
        CountDownLatch latch = new CountDownLatch(1);
        Platform.runLater(() -> {
            sortedList = sortingService.initializeSorting(filteredList);
            
            assertNotNull(sortedList);
            assertEquals(filteredList.size(), sortedList.size());
            
            // Items should be in original order
            assertEquals("Charlie", sortedList.get(0).getName());
            assertEquals("Alice", sortedList.get(1).getName());
            assertEquals("Bob", sortedList.get(2).getName());
            assertEquals("David", sortedList.get(3).getName());
            
            latch.countDown();
        });
        latch.await(5, TimeUnit.SECONDS);
    }
    
    @Test
    void testBindToTableView() throws InterruptedException {
        CountDownLatch latch = new CountDownLatch(1);
        Platform.runLater(() -> {
            sortedList = sortingService.initializeSorting(filteredList);
            sortingService.bindToTableView(tableView);
            
            // Create columns
            TableColumn<TestItem, String> nameColumn = new TableColumn<>("Name");
            nameColumn.setCellValueFactory(new PropertyValueFactory<>("name"));
            tableView.getColumns().add(nameColumn);
            
            // Set table items
            tableView.setItems(sortedList);
            
            // Sort by name
            tableView.getSortOrder().add(nameColumn);
            tableView.sort();
            
            // Check sorted order
            assertEquals("Alice", sortedList.get(0).getName());
            assertEquals("Bob", sortedList.get(1).getName());
            assertEquals("Charlie", sortedList.get(2).getName());
            assertEquals("David", sortedList.get(3).getName());
            
            latch.countDown();
        });
        latch.await(5, TimeUnit.SECONDS);
    }
    
    @Test
    void testSetSortOrder() throws InterruptedException {
        CountDownLatch latch = new CountDownLatch(1);
        Platform.runLater(() -> {
            sortedList = sortingService.initializeSorting(filteredList);
            sortingService.bindToTableView(tableView);
            tableView.setItems(sortedList);
            
            // Create columns
            TableColumn<TestItem, String> nameColumn = new TableColumn<>("Name");
            nameColumn.setCellValueFactory(new PropertyValueFactory<>("name"));
            
            TableColumn<TestItem, Integer> valueColumn = new TableColumn<>("Value");
            valueColumn.setCellValueFactory(new PropertyValueFactory<>("value"));
            
            tableView.getColumns().addAll(nameColumn, valueColumn);
            
            // Sort by name ascending
            sortingService.setSortOrder(nameColumn, true);
            assertEquals(1, tableView.getSortOrder().size());
            assertEquals(nameColumn, tableView.getSortOrder().get(0));
            assertEquals(TableColumn.SortType.ASCENDING, nameColumn.getSortType());
            
            // Sort by value descending
            sortingService.setSortOrder(valueColumn, false);
            assertEquals(1, tableView.getSortOrder().size()); // Should replace previous sort
            assertEquals(valueColumn, tableView.getSortOrder().get(0));
            assertEquals(TableColumn.SortType.DESCENDING, valueColumn.getSortType());
            
            latch.countDown();
        });
        latch.await(5, TimeUnit.SECONDS);
    }
    
    @Test
    void testAddToSortOrder() throws InterruptedException {
        CountDownLatch latch = new CountDownLatch(1);
        Platform.runLater(() -> {
            sortedList = sortingService.initializeSorting(filteredList);
            sortingService.bindToTableView(tableView);
            
            TableColumn<TestItem, String> nameColumn = new TableColumn<>("Name");
            TableColumn<TestItem, Integer> valueColumn = new TableColumn<>("Value");
            tableView.getColumns().addAll(nameColumn, valueColumn);
            
            // Add name column to sort order
            sortingService.addToSortOrder(nameColumn, true);
            assertEquals(1, tableView.getSortOrder().size());
            
            // Add value column to sort order
            sortingService.addToSortOrder(valueColumn, false);
            assertEquals(2, tableView.getSortOrder().size());
            assertTrue(tableView.getSortOrder().contains(nameColumn));
            assertTrue(tableView.getSortOrder().contains(valueColumn));
            
            latch.countDown();
        });
        latch.await(5, TimeUnit.SECONDS);
    }
    
    @Test
    void testRemoveFromSortOrder() throws InterruptedException {
        CountDownLatch latch = new CountDownLatch(1);
        Platform.runLater(() -> {
            sortedList = sortingService.initializeSorting(filteredList);
            sortingService.bindToTableView(tableView);
            
            TableColumn<TestItem, String> nameColumn = new TableColumn<>("Name");
            TableColumn<TestItem, Integer> valueColumn = new TableColumn<>("Value");
            tableView.getColumns().addAll(nameColumn, valueColumn);
            
            // Add both columns to sort order
            sortingService.addToSortOrder(nameColumn, true);
            sortingService.addToSortOrder(valueColumn, false);
            assertEquals(2, tableView.getSortOrder().size());
            
            // Remove name column
            sortingService.removeFromSortOrder(nameColumn);
            assertEquals(1, tableView.getSortOrder().size());
            assertFalse(tableView.getSortOrder().contains(nameColumn));
            assertTrue(tableView.getSortOrder().contains(valueColumn));
            
            latch.countDown();
        });
        latch.await(5, TimeUnit.SECONDS);
    }
    
    @Test
    void testClearSortOrder() throws InterruptedException {
        CountDownLatch latch = new CountDownLatch(1);
        Platform.runLater(() -> {
            sortedList = sortingService.initializeSorting(filteredList);
            sortingService.bindToTableView(tableView);
            
            TableColumn<TestItem, String> nameColumn = new TableColumn<>("Name");
            TableColumn<TestItem, Integer> valueColumn = new TableColumn<>("Value");
            tableView.getColumns().addAll(nameColumn, valueColumn);
            
            // Add columns to sort order
            sortingService.addToSortOrder(nameColumn, true);
            sortingService.addToSortOrder(valueColumn, false);
            assertEquals(2, tableView.getSortOrder().size());
            
            // Clear sort order
            sortingService.clearSortOrder();
            assertEquals(0, tableView.getSortOrder().size());
            
            latch.countDown();
        });
        latch.await(5, TimeUnit.SECONDS);
    }
    
    @Test
    void testToggleSortOrder() throws InterruptedException {
        CountDownLatch latch = new CountDownLatch(1);
        Platform.runLater(() -> {
            sortedList = sortingService.initializeSorting(filteredList);
            sortingService.bindToTableView(tableView);
            
            TableColumn<TestItem, String> nameColumn = new TableColumn<>("Name");
            tableView.getColumns().add(nameColumn);
            
            // First toggle - should add as ascending
            sortingService.toggleSortOrder(nameColumn);
            assertTrue(sortingService.isInSortOrder(nameColumn));
            assertEquals(TableColumn.SortType.ASCENDING, nameColumn.getSortType());
            
            // Second toggle - should change to descending
            sortingService.toggleSortOrder(nameColumn);
            assertTrue(sortingService.isInSortOrder(nameColumn));
            assertEquals(TableColumn.SortType.DESCENDING, nameColumn.getSortType());
            
            // Third toggle - should change back to ascending
            sortingService.toggleSortOrder(nameColumn);
            assertTrue(sortingService.isInSortOrder(nameColumn));
            assertEquals(TableColumn.SortType.ASCENDING, nameColumn.getSortType());
            
            latch.countDown();
        });
        latch.await(5, TimeUnit.SECONDS);
    }
    
    @Test
    void testSetCustomComparator() throws InterruptedException {
        CountDownLatch latch = new CountDownLatch(1);
        Platform.runLater(() -> {
            sortedList = sortingService.initializeSorting(filteredList);
            
            // Set custom comparator that sorts by value in reverse order
            Comparator<TestItem> customComparator = (t1, t2) -> 
                Integer.compare(t2.getValue(), t1.getValue());
            
            sortingService.setCustomComparator(customComparator);
            
            // Check sorted order - should be sorted by value descending
            assertEquals(40, sortedList.get(0).getValue()); // David
            assertEquals(30, sortedList.get(1).getValue()); // Charlie
            assertEquals(20, sortedList.get(2).getValue()); // Bob
            assertEquals(10, sortedList.get(3).getValue()); // Alice
            
            latch.countDown();
        });
        latch.await(5, TimeUnit.SECONDS);
    }
    
    @Test
    void testGetSortOrder() throws InterruptedException {
        CountDownLatch latch = new CountDownLatch(1);
        Platform.runLater(() -> {
            sortedList = sortingService.initializeSorting(filteredList);
            sortingService.bindToTableView(tableView);
            
            TableColumn<TestItem, String> nameColumn = new TableColumn<>("Name");
            tableView.getColumns().add(nameColumn);
            
            // Initially empty
            assertEquals(0, sortingService.getSortOrder().size());
            
            // Add to sort order
            sortingService.setSortOrder(nameColumn, true);
            assertEquals(1, sortingService.getSortOrder().size());
            assertTrue(sortingService.getSortOrder().contains(nameColumn));
            
            latch.countDown();
        });
        latch.await(5, TimeUnit.SECONDS);
    }
    
    @Test
    void testGetSortType() throws InterruptedException {
        CountDownLatch latch = new CountDownLatch(1);
        Platform.runLater(() -> {
            sortedList = sortingService.initializeSorting(filteredList);
            sortingService.bindToTableView(tableView);
            
            TableColumn<TestItem, String> nameColumn = new TableColumn<>("Name");
            tableView.getColumns().add(nameColumn);
            
            // Not in sort order
            assertNull(sortingService.getSortType(nameColumn));
            
            // Add to sort order as ascending
            sortingService.setSortOrder(nameColumn, true);
            assertEquals(TableColumn.SortType.ASCENDING, sortingService.getSortType(nameColumn));
            
            // Change to descending
            sortingService.setSortOrder(nameColumn, false);
            assertEquals(TableColumn.SortType.DESCENDING, sortingService.getSortType(nameColumn));
            
            latch.countDown();
        });
        latch.await(5, TimeUnit.SECONDS);
    }
}