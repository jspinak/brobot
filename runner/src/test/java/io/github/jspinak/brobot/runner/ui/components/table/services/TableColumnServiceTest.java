package io.github.jspinak.brobot.runner.ui.components.table.services;

import javafx.application.Platform;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;
import javafx.embed.swing.JFXPanel;
import javafx.scene.control.TableCell;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.util.Callback;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

import static org.junit.jupiter.api.Assertions.*;

class TableColumnServiceTest {
    
    private TableColumnService<TestItem> columnService;
    private TableView<TestItem> tableView;
    
    static class TestItem {
        private final StringProperty name = new SimpleStringProperty();
        private final StringProperty value = new SimpleStringProperty();
        
        public TestItem(String name, String value) {
            this.name.set(name);
            this.value.set(value);
        }
        
        public StringProperty nameProperty() { return name; }
        public StringProperty valueProperty() { return value; }
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
            columnService = new TableColumnService<>();
            tableView = new TableView<>();
            latch.countDown();
        });
        latch.await(5, TimeUnit.SECONDS);
    }
    
    @Test
    void testAddStringColumn() throws InterruptedException {
        CountDownLatch latch = new CountDownLatch(1);
        Platform.runLater(() -> {
            TableColumn<TestItem, String> column = columnService.addStringColumn(tableView, "Name", "name");
            
            assertNotNull(column);
            assertEquals("Name", column.getText());
            assertTrue(tableView.getColumns().contains(column));
            assertEquals(1, tableView.getColumns().size());
            
            latch.countDown();
        });
        latch.await(5, TimeUnit.SECONDS);
    }
    
    @Test
    void testAddColumnWithCellValueFactory() throws InterruptedException {
        CountDownLatch latch = new CountDownLatch(1);
        Platform.runLater(() -> {
            Callback<TableColumn.CellDataFeatures<TestItem, String>, javafx.beans.value.ObservableValue<String>> cellValueFactory = 
                param -> param.getValue().nameProperty();
            
            TableColumn<TestItem, String> column = columnService.addColumn(tableView, "Custom", cellValueFactory);
            
            assertNotNull(column);
            assertEquals("Custom", column.getText());
            assertTrue(tableView.getColumns().contains(column));
            
            latch.countDown();
        });
        latch.await(5, TimeUnit.SECONDS);
    }
    
    @Test
    void testAddColumnWithBothFactories() throws InterruptedException {
        CountDownLatch latch = new CountDownLatch(1);
        Platform.runLater(() -> {
            Callback<TableColumn.CellDataFeatures<TestItem, String>, javafx.beans.value.ObservableValue<String>> cellValueFactory = 
                param -> param.getValue().valueProperty();
            
            Callback<TableColumn<TestItem, String>, TableCell<TestItem, String>> cellFactory = 
                column -> new TableCell<TestItem, String>() {
                    @Override
                    protected void updateItem(String item, boolean empty) {
                        super.updateItem(item, empty);
                        setText(empty ? null : "Custom: " + item);
                    }
                };
            
            TableColumn<TestItem, String> column = columnService.addColumn(tableView, "Value", cellValueFactory, cellFactory);
            
            assertNotNull(column);
            assertEquals("Value", column.getText());
            assertNotNull(column.getCellFactory());
            assertTrue(tableView.getColumns().contains(column));
            
            latch.countDown();
        });
        latch.await(5, TimeUnit.SECONDS);
    }
    
    @Test
    void testSetColumnTitle() throws InterruptedException {
        CountDownLatch latch = new CountDownLatch(1);
        Platform.runLater(() -> {
            TableColumn<TestItem, String> column = columnService.addStringColumn(tableView, "Original", "name");
            
            columnService.setColumnTitle(column, "New Title");
            
            assertEquals("New Title", column.getText());
            
            latch.countDown();
        });
        latch.await(5, TimeUnit.SECONDS);
    }
    
    @Test
    void testRemoveColumn() throws InterruptedException {
        CountDownLatch latch = new CountDownLatch(1);
        Platform.runLater(() -> {
            TableColumn<TestItem, String> column1 = columnService.addStringColumn(tableView, "Col1", "name");
            TableColumn<TestItem, String> column2 = columnService.addStringColumn(tableView, "Col2", "value");
            
            assertEquals(2, tableView.getColumns().size());
            
            columnService.removeColumn(tableView, column1);
            
            assertEquals(1, tableView.getColumns().size());
            assertFalse(tableView.getColumns().contains(column1));
            assertTrue(tableView.getColumns().contains(column2));
            
            latch.countDown();
        });
        latch.await(5, TimeUnit.SECONDS);
    }
    
    @Test
    void testClearColumns() throws InterruptedException {
        CountDownLatch latch = new CountDownLatch(1);
        Platform.runLater(() -> {
            columnService.addStringColumn(tableView, "Col1", "name");
            columnService.addStringColumn(tableView, "Col2", "value");
            columnService.addStringColumn(tableView, "Col3", "name");
            
            assertEquals(3, tableView.getColumns().size());
            
            columnService.clearColumns(tableView);
            
            assertEquals(0, tableView.getColumns().size());
            
            latch.countDown();
        });
        latch.await(5, TimeUnit.SECONDS);
    }
    
    @Test
    void testSetColumnComparator() throws InterruptedException {
        CountDownLatch latch = new CountDownLatch(1);
        Platform.runLater(() -> {
            TableColumn<TestItem, String> column = columnService.addStringColumn(tableView, "Name", "name");
            
            // Set a custom comparator that reverses the order
            columnService.setColumnComparator(column, (s1, s2) -> s2.compareTo(s1));
            
            assertNotNull(column.getComparator());
            
            // Test the comparator
            assertEquals(1, column.getComparator().compare("A", "B"));
            assertEquals(-1, column.getComparator().compare("B", "A"));
            
            latch.countDown();
        });
        latch.await(5, TimeUnit.SECONDS);
    }
    
    @Test
    void testMultipleColumnsWithSameName() throws InterruptedException {
        CountDownLatch latch = new CountDownLatch(1);
        Platform.runLater(() -> {
            TableColumn<TestItem, String> column1 = columnService.addStringColumn(tableView, "Name", "name");
            TableColumn<TestItem, String> column2 = columnService.addStringColumn(tableView, "Name", "value");
            
            assertEquals(2, tableView.getColumns().size());
            
            // Both columns should have the same display text
            assertEquals("Name", column1.getText());
            assertEquals("Name", column2.getText());
            
            // But they can be independently renamed
            columnService.setColumnTitle(column1, "Name 1");
            columnService.setColumnTitle(column2, "Name 2");
            
            assertEquals("Name 1", column1.getText());
            assertEquals("Name 2", column2.getText());
            
            latch.countDown();
        });
        latch.await(5, TimeUnit.SECONDS);
    }
}