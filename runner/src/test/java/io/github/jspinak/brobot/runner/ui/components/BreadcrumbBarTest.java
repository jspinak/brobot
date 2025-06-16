package io.github.jspinak.brobot.runner.ui.components;

import io.github.jspinak.brobot.runner.testutil.JavaFXTestUtils;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.function.Consumer;

import static org.junit.jupiter.api.Assertions.*;

public class BreadcrumbBarTest {

    private BreadcrumbBar breadcrumbBar;
    private boolean callbackInvoked;

    @BeforeAll
    public static void initJavaFX() throws InterruptedException {
        JavaFXTestUtils.initJavaFX();
    }

    @BeforeEach
    public void setUp() throws InterruptedException {
        JavaFXTestUtils.runOnFXThread(() -> {
            breadcrumbBar = new BreadcrumbBar();
            callbackInvoked = false;
        });
    }

    @Test
    public void testAddItem() {
        Consumer<BreadcrumbBar.BreadcrumbItem> onClick = item -> callbackInvoked = true;

        // Add an item
        BreadcrumbBar.BreadcrumbItem item = breadcrumbBar.addItem("Home", onClick);

        // Verify the item was added
        assertEquals(1, breadcrumbBar.getItems().size());
        assertEquals("Home", item.getText());
        assertEquals(onClick, item.getOnClick());
        assertEquals(item, breadcrumbBar.getActiveItem());
    }

    @Test
    public void testAddMultipleItems() {
        // Add multiple items
        BreadcrumbBar.BreadcrumbItem item1 = breadcrumbBar.addItem("Home", null);
        BreadcrumbBar.BreadcrumbItem item2 = breadcrumbBar.addItem("Products", null);
        BreadcrumbBar.BreadcrumbItem item3 = breadcrumbBar.addItem("Electronics", null);

        // Verify items were added in order
        List<BreadcrumbBar.BreadcrumbItem> items = breadcrumbBar.getItems();
        assertEquals(3, items.size());
        assertEquals(item1, items.get(0));
        assertEquals(item2, items.get(1));
        assertEquals(item3, items.get(2));

        // Verify the last item is active
        assertEquals(item3, breadcrumbBar.getActiveItem());
    }

    @Test
    public void testRemoveItem() {
        // Add items
        BreadcrumbBar.BreadcrumbItem item1 = breadcrumbBar.addItem("Home", null);
        BreadcrumbBar.BreadcrumbItem item2 = breadcrumbBar.addItem("Products", null);

        // Verify items were added
        assertEquals(2, breadcrumbBar.getItems().size());
        assertEquals(item2, breadcrumbBar.getActiveItem());

        // Remove the active item
        breadcrumbBar.removeItem(item2);

        // Verify the item was removed and the active item updated
        assertEquals(1, breadcrumbBar.getItems().size());
        assertEquals(item1, breadcrumbBar.getActiveItem());

        // Remove the remaining item
        breadcrumbBar.removeItem(item1);

        // Verify all items are removed
        assertEquals(0, breadcrumbBar.getItems().size());
        assertNull(breadcrumbBar.getActiveItem());
    }

    @Test
    public void testClearItems() {
        // Add multiple items
        breadcrumbBar.addItem("Home", null);
        breadcrumbBar.addItem("Products", null);
        breadcrumbBar.addItem("Electronics", null);

        // Verify items were added
        assertEquals(3, breadcrumbBar.getItems().size());
        assertNotNull(breadcrumbBar.getActiveItem());

        // Clear all items
        breadcrumbBar.clearItems();

        // Verify items were cleared
        assertEquals(0, breadcrumbBar.getItems().size());
        assertNull(breadcrumbBar.getActiveItem());
    }

    @Test
    public void testSetPath() {
        // Create items
        BreadcrumbBar.BreadcrumbItem item1 = new BreadcrumbBar.BreadcrumbItem("Home", null);
        BreadcrumbBar.BreadcrumbItem item2 = new BreadcrumbBar.BreadcrumbItem("Products", null);
        BreadcrumbBar.BreadcrumbItem item3 = new BreadcrumbBar.BreadcrumbItem("Electronics", null);

        // Create a path
        List<BreadcrumbBar.BreadcrumbItem> path = List.of(item1, item2, item3);

        // Set the path
        breadcrumbBar.setPath(path);

        // Verify the path was set
        assertEquals(3, breadcrumbBar.getItems().size());
        assertEquals(path, breadcrumbBar.getItems());
        assertEquals(item3, breadcrumbBar.getActiveItem());
    }

    @Test
    public void testSetActiveItem() {
        // Add multiple items
        BreadcrumbBar.BreadcrumbItem item1 = breadcrumbBar.addItem("Home", null);
        BreadcrumbBar.BreadcrumbItem item2 = breadcrumbBar.addItem("Products", null);
        BreadcrumbBar.BreadcrumbItem item3 = breadcrumbBar.addItem("Electronics", null);

        // Verify the last item is active
        assertEquals(item3, breadcrumbBar.getActiveItem());

        // Set an earlier item as active
        breadcrumbBar.setActiveItem(item1);

        // Verify the active item was updated
        assertEquals(item1, breadcrumbBar.getActiveItem());
    }

    @Test
    public void testSetActiveItemByIndex() {
        // Add multiple items
        breadcrumbBar.addItem("Home", null);
        breadcrumbBar.addItem("Products", null);
        breadcrumbBar.addItem("Electronics", null);

        // Verify the last item is active
        assertEquals("Electronics", breadcrumbBar.getActiveItem().getText());

        // Set an earlier item as active by index
        breadcrumbBar.setActiveItem(0);

        // Verify the active item was updated
        assertEquals("Home", breadcrumbBar.getActiveItem().getText());
    }

    @Test
    public void testBreadcrumbItemWithData() {
        // Create item with data
        Object data = new Object();
        BreadcrumbBar.BreadcrumbItem item = new BreadcrumbBar.BreadcrumbItem("Test", null, data);

        // Verify the data was set
        assertEquals(data, item.getData());

        // Update the data
        Object newData = new Object();
        item.setData(newData);

        // Verify the data was updated
        assertEquals(newData, item.getData());
    }

    @Test
    public void testItemOnClickCallback() {
        // Create a callback that tracks invocation
        Consumer<BreadcrumbBar.BreadcrumbItem> onClick = item -> callbackInvoked = true;

        // Add an item with the callback
        BreadcrumbBar.BreadcrumbItem item = new BreadcrumbBar.BreadcrumbItem("Test", onClick);

        // Invoke the callback
        item.getOnClick().accept(item);

        // Verify the callback was invoked
        assertTrue(callbackInvoked);
    }

    @Test
    public void testObservableListAccess() {
        // Add items directly to the observable list
        ObservableList<BreadcrumbBar.BreadcrumbItem> items = breadcrumbBar.itemsProperty();
        items.add(new BreadcrumbBar.BreadcrumbItem("Test1", null));
        items.add(new BreadcrumbBar.BreadcrumbItem("Test2", null));

        // Verify items were added
        assertEquals(2, breadcrumbBar.getItems().size());
        assertEquals("Test1", breadcrumbBar.getItems().get(0).getText());
        assertEquals("Test2", breadcrumbBar.getItems().get(1).getText());
    }
}