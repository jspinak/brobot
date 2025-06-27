package io.github.jspinak.brobot.runner.ui.config;

import javafx.application.Platform;
import javafx.scene.Scene;
import javafx.scene.control.TreeItem;
import javafx.stage.Stage;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;
import org.testfx.framework.junit5.ApplicationExtension;
import org.testfx.framework.junit5.Start;

import io.github.jspinak.brobot.runner.json.validation.model.ValidationResult;
import io.github.jspinak.brobot.runner.json.validation.model.ValidationSeverity;

import java.lang.reflect.Field;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

import static org.junit.jupiter.api.Assertions.*;

@ExtendWith({MockitoExtension.class, ApplicationExtension.class})
public class ValidationResultsPanelTest {

    private ValidationResultsPanel panel;
    private Stage stage;

    @Start
    public void start(Stage stage) {
        this.stage = stage;
    }

    private void createPanel() {
        // Create a fresh panel instance for each test
        CountDownLatch latch = new CountDownLatch(1);
        Platform.runLater(() -> {
            panel = new ValidationResultsPanel();
            stage.setScene(new Scene(panel, 800, 600));
            stage.show();
            latch.countDown();
        });

        try {
            assertTrue(latch.await(5, TimeUnit.SECONDS), "JavaFX operation timed out");
        } catch (InterruptedException e) {
            fail("Test interrupted: " + e.getMessage());
        }
    }

    @Test
    public void testSetValidationResult() throws Exception {
        // Create panel
        createPanel();

        // Create test validation result with one of each severity
        ValidationResult result = new ValidationResult();

        // Add a critical error
        result.addError(
                "CRITICAL_ERROR",
                "A critical error occurred",
                ValidationSeverity.CRITICAL
        );

        // Add a warning
        result.addError(
                "WARNING_ERROR",
                "A warning occurred",
                ValidationSeverity.WARNING
        );

        // Add a regular error
        result.addError(
                "ERROR_CODE",
                "A regular error occurred",
                ValidationSeverity.ERROR
        );

        // Add an info message
        result.addError(
                "INFO_MESSAGE",
                "An informational message",
                ValidationSeverity.INFO
        );

        // Set validation result on the panel
        final CountDownLatch setResultLatch = new CountDownLatch(1);
        Platform.runLater(() -> {
            panel.setValidationResult(result);
            setResultLatch.countDown();
        });

        assertTrue(setResultLatch.await(5, TimeUnit.SECONDS), "Setting result timed out");

        // Now verify the tree structure in a separate JavaFX action
        final CountDownLatch verifyLatch = new CountDownLatch(1);
        Platform.runLater(() -> {
            try {
                // Access the tree items using reflection
                Field rootItemField = ValidationResultsPanel.class.getDeclaredField("rootItem");
                rootItemField.setAccessible(true);
                TreeItem<ValidationResultsPanel.ValidationItem> rootItem =
                        (TreeItem<ValidationResultsPanel.ValidationItem>) rootItemField.get(panel);

                // Verify there are some categories
                assertTrue(rootItem.getChildren().size() > 0, "Root should have children");

                // Collect all errors into a map by their error code for easy verification
                Map<String, ValidationResultsPanel.ValidationItem> errorMap = new HashMap<>();

                // Traverse the tree and find all error items
                for (TreeItem<ValidationResultsPanel.ValidationItem> category : rootItem.getChildren()) {
                    System.out.println("Category: " + category.getValue().getMessage());

                    for (TreeItem<ValidationResultsPanel.ValidationItem> errorItem : category.getChildren()) {
                        ValidationResultsPanel.ValidationItem item = errorItem.getValue();
                        System.out.println("  Error: " + item.getMessage() + ", Location: " + item.getLocation());
                        errorMap.put(item.getMessage(), item);
                    }
                }

                // Verify we found all our errors
                assertTrue(errorMap.containsKey("CRITICAL_ERROR"), "Critical error should be present");
                assertTrue(errorMap.containsKey("WARNING_ERROR"), "Warning should be present");
                assertTrue(errorMap.containsKey("ERROR_CODE"), "Error should be present");
                assertTrue(errorMap.containsKey("INFO_MESSAGE"), "Info message should be present");

                // Verify the error messages are in the expected fields
                assertEquals("A critical error occurred", errorMap.get("CRITICAL_ERROR").getLocation());
                assertEquals("A warning occurred", errorMap.get("WARNING_ERROR").getLocation());
                assertEquals("A regular error occurred", errorMap.get("ERROR_CODE").getLocation());
                assertEquals("An informational message", errorMap.get("INFO_MESSAGE").getLocation());

                verifyLatch.countDown();
            } catch (Exception e) {
                e.printStackTrace();
                fail("Exception during test: " + e.getMessage());
            }
        });

        assertTrue(verifyLatch.await(5, TimeUnit.SECONDS), "Test verification timed out");
    }

    @Test
    public void testClearResults() throws Exception {
        // Create panel
        createPanel();

        // Create test validation result
        ValidationResult result = new ValidationResult();
        result.addError(
                "ERROR",
                "An error occurred",
                ValidationSeverity.ERROR
        );

        // Set results and verify in separate JavaFX actions
        CountDownLatch setResultLatch = new CountDownLatch(1);
        Platform.runLater(() -> {
            panel.setValidationResult(result);
            setResultLatch.countDown();
        });

        assertTrue(setResultLatch.await(5, TimeUnit.SECONDS), "Setting result timed out");

        // Now verify
        CountDownLatch verifyLatch = new CountDownLatch(1);
        Platform.runLater(() -> {
            try {
                // Access the tree items using reflection
                Field rootItemField = ValidationResultsPanel.class.getDeclaredField("rootItem");
                rootItemField.setAccessible(true);
                TreeItem<ValidationResultsPanel.ValidationItem> rootItem =
                        (TreeItem<ValidationResultsPanel.ValidationItem>) rootItemField.get(panel);

                // Verify that we have items before clearing
                assertTrue(rootItem.getChildren().size() > 0, "Tree should have items before clearing");

                // Clear the results
                panel.clearResults();

                // Assert
                assertEquals(0, rootItem.getChildren().size(), "Tree items should be cleared");

                verifyLatch.countDown();
            } catch (Exception e) {
                fail("Exception during test: " + e.getMessage());
            }
        });

        assertTrue(verifyLatch.await(5, TimeUnit.SECONDS), "Test verification timed out");
    }

    @Test
    public void testValidationItemClass() {
        // Create the validation item directly
        String message = "Test Message";
        String location = "Test Location";
        ValidationSeverity severity = ValidationSeverity.WARNING;

        ValidationResultsPanel.ValidationItem item =
                new ValidationResultsPanel.ValidationItem(message, location, severity);

        assertEquals(message, item.getMessage());
        assertEquals(location, item.getLocation());
        assertEquals(severity, item.getSeverity());
        assertEquals(message, item.toString());
    }

    @Test
    public void testHandleNullOrEmptyResult() throws Exception {
        // Create panel
        createPanel();

        // Split actions into separate JavaFX calls to avoid timing issues
        CountDownLatch setNullLatch = new CountDownLatch(1);
        Platform.runLater(() -> {
            try {
                // Test with null result
                panel.setValidationResult(null);
                setNullLatch.countDown();
            } catch (Exception e) {
                fail("Exception during test: " + e.getMessage());
            }
        });

        assertTrue(setNullLatch.await(5, TimeUnit.SECONDS), "Setting null result timed out");

        // Verify null result
        CountDownLatch verifyNullLatch = new CountDownLatch(1);
        Platform.runLater(() -> {
            try {
                // Access the tree items using reflection
                Field rootItemField = ValidationResultsPanel.class.getDeclaredField("rootItem");
                rootItemField.setAccessible(true);
                TreeItem<ValidationResultsPanel.ValidationItem> rootItem =
                        (TreeItem<ValidationResultsPanel.ValidationItem>) rootItemField.get(panel);

                // Assert
                assertEquals(0, rootItem.getChildren().size(), "Tree should be empty with null result");
                verifyNullLatch.countDown();
            } catch (Exception e) {
                fail("Exception during test: " + e.getMessage());
            }
        });

        assertTrue(verifyNullLatch.await(5, TimeUnit.SECONDS), "Verifying null result timed out");

        // Test with empty result
        CountDownLatch setEmptyLatch = new CountDownLatch(1);
        Platform.runLater(() -> {
            ValidationResult emptyResult = new ValidationResult();
            panel.setValidationResult(emptyResult);
            setEmptyLatch.countDown();
        });

        assertTrue(setEmptyLatch.await(5, TimeUnit.SECONDS), "Setting empty result timed out");

        // Verify empty result
        CountDownLatch verifyEmptyLatch = new CountDownLatch(1);
        Platform.runLater(() -> {
            try {
                // Access the tree items using reflection
                Field rootItemField = ValidationResultsPanel.class.getDeclaredField("rootItem");
                rootItemField.setAccessible(true);
                TreeItem<ValidationResultsPanel.ValidationItem> rootItem =
                        (TreeItem<ValidationResultsPanel.ValidationItem>) rootItemField.get(panel);

                // Assert
                assertEquals(0, rootItem.getChildren().size(), "Tree should be empty with empty result");
                verifyEmptyLatch.countDown();
            } catch (Exception e) {
                fail("Exception during test: " + e.getMessage());
            }
        });

        assertTrue(verifyEmptyLatch.await(5, TimeUnit.SECONDS), "Verifying empty result timed out");
    }
}