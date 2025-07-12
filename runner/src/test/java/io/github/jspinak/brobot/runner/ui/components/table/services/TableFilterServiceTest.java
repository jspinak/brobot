package io.github.jspinak.brobot.runner.ui.components.table.services;

import javafx.application.Platform;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.collections.transformation.FilteredList;
import javafx.embed.swing.JFXPanel;
import javafx.scene.control.TextField;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;
import java.util.function.Function;
import java.util.function.Predicate;

import static org.junit.jupiter.api.Assertions.*;

class TableFilterServiceTest {
    
    private TableFilterService<TestItem> filterService;
    private ObservableList<TestItem> items;
    private FilteredList<TestItem> filteredList;
    
    static class TestItem {
        private final String name;
        private final String category;
        
        public TestItem(String name, String category) {
            this.name = name;
            this.category = category;
        }
        
        public String getName() { return name; }
        public String getCategory() { return category; }
        
        @Override
        public String toString() {
            return name + " (" + category + ")";
        }
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
            filterService = new TableFilterService<>();
            items = FXCollections.observableArrayList(
                new TestItem("Apple", "Fruit"),
                new TestItem("Banana", "Fruit"),
                new TestItem("Carrot", "Vegetable"),
                new TestItem("Date", "Fruit"),
                new TestItem("Eggplant", "Vegetable")
            );
            latch.countDown();
        });
        latch.await(5, TimeUnit.SECONDS);
    }
    
    @Test
    void testInitializeFiltering() throws InterruptedException {
        CountDownLatch latch = new CountDownLatch(1);
        Platform.runLater(() -> {
            filteredList = filterService.initializeFiltering(items);
            
            assertNotNull(filteredList);
            assertEquals(items.size(), filteredList.size());
            
            // All items should be visible initially
            for (int i = 0; i < items.size(); i++) {
                assertEquals(items.get(i), filteredList.get(i));
            }
            
            latch.countDown();
        });
        latch.await(5, TimeUnit.SECONDS);
    }
    
    @Test
    void testSearchProviderFiltering() throws InterruptedException {
        CountDownLatch latch = new CountDownLatch(1);
        Platform.runLater(() -> {
            filteredList = filterService.initializeFiltering(items);
            
            // Set search provider
            Function<TestItem, String> searchProvider = TestItem::getName;
            filterService.setSearchProvider(searchProvider);
            
            // Set search text
            TextField searchField = new TextField();
            filterService.setSearchField(searchField);
            
            // Search for items containing "a"
            searchField.setText("a");
            
            // Should find Apple, Banana, Date, Eggplant (4 items)
            assertEquals(4, filteredList.size());
            assertTrue(filteredList.stream().allMatch(item -> 
                item.getName().toLowerCase().contains("a")));
            
            latch.countDown();
        });
        latch.await(5, TimeUnit.SECONDS);
    }
    
    @Test
    void testCaseInsensitiveSearch() throws InterruptedException {
        CountDownLatch latch = new CountDownLatch(1);
        Platform.runLater(() -> {
            filteredList = filterService.initializeFiltering(items);
            filterService.setSearchProvider(TestItem::getName);
            
            TextField searchField = new TextField();
            filterService.setSearchField(searchField);
            
            // Search with uppercase
            searchField.setText("APP");
            
            // Should find Apple
            assertEquals(1, filteredList.size());
            assertEquals("Apple", filteredList.get(0).getName());
            
            latch.countDown();
        });
        latch.await(5, TimeUnit.SECONDS);
    }
    
    @Test
    void testCustomFilter() throws InterruptedException {
        CountDownLatch latch = new CountDownLatch(1);
        Platform.runLater(() -> {
            filteredList = filterService.initializeFiltering(items);
            
            // Set custom filter for vegetables only
            Predicate<TestItem> vegetableFilter = item -> "Vegetable".equals(item.getCategory());
            filterService.setFilter(vegetableFilter);
            
            // Should find Carrot and Eggplant
            assertEquals(2, filteredList.size());
            assertTrue(filteredList.stream().allMatch(item -> 
                "Vegetable".equals(item.getCategory())));
            
            latch.countDown();
        });
        latch.await(5, TimeUnit.SECONDS);
    }
    
    @Test
    void testClearFilter() throws InterruptedException {
        CountDownLatch latch = new CountDownLatch(1);
        Platform.runLater(() -> {
            filteredList = filterService.initializeFiltering(items);
            
            // Apply a filter
            filterService.setFilter(item -> item.getName().startsWith("A"));
            assertEquals(1, filteredList.size());
            
            // Clear the filter
            filterService.clearFilter();
            
            // All items should be visible again
            assertEquals(items.size(), filteredList.size());
            
            latch.countDown();
        });
        latch.await(5, TimeUnit.SECONDS);
    }
    
    @Test
    void testOnFilterChangedCallback() throws InterruptedException {
        CountDownLatch latch = new CountDownLatch(1);
        Platform.runLater(() -> {
            filteredList = filterService.initializeFiltering(items);
            
            boolean[] callbackCalled = {false};
            filterService.setOnFilterChanged(() -> callbackCalled[0] = true);
            
            TextField searchField = new TextField();
            filterService.setSearchField(searchField);
            filterService.setSearchProvider(TestItem::getName);
            
            // Trigger filter change
            searchField.setText("test");
            
            assertTrue(callbackCalled[0]);
            
            latch.countDown();
        });
        latch.await(5, TimeUnit.SECONDS);
    }
    
    @Test
    void testSearchProviderProperty() throws InterruptedException {
        CountDownLatch latch = new CountDownLatch(1);
        Platform.runLater(() -> {
            Function<TestItem, String> provider1 = TestItem::getName;
            Function<TestItem, String> provider2 = TestItem::getCategory;
            
            filterService.setSearchProvider(provider1);
            assertEquals(provider1, filterService.getSearchProvider());
            
            // Test property
            filterService.searchProviderProperty().set(provider2);
            assertEquals(provider2, filterService.getSearchProvider());
            
            latch.countDown();
        });
        latch.await(5, TimeUnit.SECONDS);
    }
    
    @Test
    void testFilterPredicate() throws InterruptedException {
        CountDownLatch latch = new CountDownLatch(1);
        Platform.runLater(() -> {
            filteredList = filterService.initializeFiltering(items);
            
            // Get the filter predicate
            Predicate<? super TestItem> predicate = filterService.getFilterPredicate();
            assertNotNull(predicate);
            
            // Initially all items should pass
            assertTrue(items.stream().allMatch(predicate));
            
            // Set a filter
            filterService.setFilter(item -> item.getName().length() > 5);
            predicate = filterService.getFilterPredicate();
            
            // Test the predicate directly
            assertTrue(predicate.test(new TestItem("Banana", "Fruit")));
            assertFalse(predicate.test(new TestItem("Apple", "Fruit")));
            
            latch.countDown();
        });
        latch.await(5, TimeUnit.SECONDS);
    }
    
    @Test
    void testEmptySearchText() throws InterruptedException {
        CountDownLatch latch = new CountDownLatch(1);
        Platform.runLater(() -> {
            filteredList = filterService.initializeFiltering(items);
            filterService.setSearchProvider(TestItem::getName);
            
            TextField searchField = new TextField();
            filterService.setSearchField(searchField);
            
            // Set and then clear search text
            searchField.setText("Apple");
            assertEquals(1, filteredList.size());
            
            searchField.setText("");
            assertEquals(items.size(), filteredList.size());
            
            latch.countDown();
        });
        latch.await(5, TimeUnit.SECONDS);
    }
}