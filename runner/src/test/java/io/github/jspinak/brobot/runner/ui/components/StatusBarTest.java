package io.github.jspinak.brobot.runner.ui.components;

import javafx.scene.Node;
import javafx.scene.control.Label;
import javafx.scene.control.ProgressBar;
import javafx.scene.layout.HBox;
import javafx.stage.Stage;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.testfx.framework.junit5.ApplicationExtension;
import org.testfx.framework.junit5.Start;
import org.testfx.util.WaitForAsyncUtils;

import static org.junit.jupiter.api.Assertions.*;

@ExtendWith(ApplicationExtension.class)
public class StatusBarTest {

    private StatusBar statusBar;

    @Start
    private void start(Stage stage) {
        // No need to show the stage, but we need the JavaFX toolkit to be initialized
    }

    @BeforeEach
    public void setUp() {
        // Create a real StatusBar instance
        statusBar = new StatusBar();

        // Wait for JavaFX operations to complete
        WaitForAsyncUtils.waitForFxEvents();
    }

    @Test
    public void testSetStatusMessage() {
        // Set status message
        statusBar.setStatusMessage("Test Message");

        // Verify the value
        assertEquals("Test Message", statusBar.getStatusMessage());
    }

    @Test
    public void testSetProgress() {
        // Set progress
        statusBar.setProgress(0.5);

        // Verify the value
        assertEquals(0.5, statusBar.getProgress());

        // Get the ProgressBar instance
        ProgressBar progressBar = findProgressBar(statusBar);
        assertNotNull(progressBar, "ProgressBar should exist");

        // Ensure it's visible
        assertTrue(progressBar.isVisible(), "ProgressBar should be visible");
    }

    @Test
    public void testShowIndeterminateProgress() {
        // Show indeterminate progress
        statusBar.showIndeterminateProgress();

        // Get the ProgressBar instance
        ProgressBar progressBar = findProgressBar(statusBar);
        assertNotNull(progressBar, "ProgressBar should exist");

        // Verify it's visible and in indeterminate mode
        assertTrue(progressBar.isVisible(), "ProgressBar should be visible");
        assertEquals(-1.0, statusBar.getProgress(), "Progress should be indeterminate (-1.0)");
    }

    @Test
    public void testHideProgress() {
        // First make it visible
        statusBar.setProgress(0.5);

        // Then hide progress
        statusBar.hideProgress();

        // Get the ProgressBar instance
        ProgressBar progressBar = findProgressBar(statusBar);
        assertNotNull(progressBar, "ProgressBar should exist");

        // Verify it's not visible
        assertFalse(progressBar.isVisible(), "ProgressBar should not be visible");
    }

    @Test
    public void testAddStatusItem() {
        // Create a real Label as the status item
        Label testItem = new Label("Test Item");

        // Add the item
        statusBar.addStatusItem(testItem);

        // Verify it was added to the list
        assertTrue(statusBar.getStatusItems().contains(testItem),
                "Status items should contain the added item");
    }

    @Test
    public void testAddStatusIndicator() {
        // Add an indicator
        Label indicator = statusBar.addStatusIndicator("Test Indicator", "warning");

        // Verify it was created with the right properties
        assertEquals("Test Indicator", indicator.getText(), "Indicator should have the correct text");
        assertTrue(indicator.getStyleClass().contains("status-indicator"),
                "Indicator should have status-indicator style class");
        assertTrue(indicator.getStyleClass().contains("warning"),
                "Indicator should have warning style class");

        // Verify it was added to status items
        assertTrue(statusBar.getStatusItems().contains(indicator),
                "Status items should contain the added indicator");
    }

    @Test
    public void testRemoveStatusItem() {
        // Create and add an item
        Label testItem = new Label("Test Item");
        statusBar.addStatusItem(testItem);

        // Verify it was added
        assertTrue(statusBar.getStatusItems().contains(testItem),
                "Status items should contain the added item");

        // Remove the item
        statusBar.removeStatusItem(testItem);

        // Verify it was removed
        assertFalse(statusBar.getStatusItems().contains(testItem),
                "Status items should not contain the removed item");
    }

    @Test
    public void testClearStatusItems() {
        // Add several items
        statusBar.addStatusItem(new Label("Item 1"));
        statusBar.addStatusItem(new Label("Item 2"));
        statusBar.addStatusItem(new Label("Item 3"));

        // Verify items were added
        assertEquals(3, statusBar.getStatusItems().size(), "Should have 3 status items");

        // Clear items
        statusBar.clearStatusItems();

        // Verify all items were removed
        assertEquals(0, statusBar.getStatusItems().size(), "Should have no status items after clear");
    }

    @Test
    public void testCreateOnlineIndicator() {
        // Create online indicator
        Label onlineIndicator = statusBar.createOnlineIndicator(true);

        // Verify its properties
        assertEquals("Online", onlineIndicator.getText(), "Online indicator should show 'Online'");
        assertTrue(onlineIndicator.getStyleClass().contains("online"),
                "Online indicator should have 'online' style class");

        // Create offline indicator
        Label offlineIndicator = statusBar.createOnlineIndicator(false);

        // Verify its properties
        assertEquals("Offline", offlineIndicator.getText(), "Offline indicator should show 'Offline'");
        assertTrue(offlineIndicator.getStyleClass().contains("offline"),
                "Offline indicator should have 'offline' style class");
    }

    @Test
    public void testCreateWarningIndicator() {
        // Create warning indicator
        Label warningIndicator = statusBar.createWarningIndicator("Warning Message");

        // Verify its properties
        assertEquals("Warning Message", warningIndicator.getText(),
                "Warning indicator should show the warning message");
        assertTrue(warningIndicator.getStyleClass().contains("warning"),
                "Warning indicator should have 'warning' style class");
    }

    // Helper method to find the ProgressBar in the StatusBar
    private ProgressBar findProgressBar(StatusBar statusBar) {
        for (Node node : statusBar.getChildren()) {
            if (node instanceof ProgressBar) {
                return (ProgressBar) node;
            }
        }
        return null;
    }

    // Helper method to find the HBox containing status items
    private HBox findItemsBox(StatusBar statusBar) {
        for (Node node : statusBar.getChildren()) {
            if (node instanceof HBox && !(node instanceof StatusBar)) {
                return (HBox) node;
            }
        }
        return null;
    }

    // Helper method to find the message Label
    private Label findMessageLabel(StatusBar statusBar) {
        for (Node node : statusBar.getChildren()) {
            if (node instanceof Label) {
                return (Label) node;
            }
        }
        return null;
    }
}