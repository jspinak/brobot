package io.github.jspinak.brobot.runner.ui.log;

import javafx.scene.Node;
import javafx.scene.control.Label;
import javafx.scene.layout.Pane;
import javafx.scene.shape.Circle;
import javafx.scene.shape.Line;
import javafx.scene.shape.Polygon;
import javafx.stage.Stage;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.testfx.framework.junit5.ApplicationExtension;
import org.testfx.framework.junit5.Start;

import java.lang.reflect.Field;
import java.util.Arrays;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Tests for the StateVisualizationPanel inner class.
 */
@ExtendWith(ApplicationExtension.class)
public class StateVisualizationPanelTest {

    private LogViewerPanel.StateVisualizationPanel stateVisualizationPanel;
    private Pane stateCanvas;
    private Label titleLabel;

    @Start
    private void start(Stage stage) {
        // Create the panel
        stateVisualizationPanel = createStateVisualizationPanel();

        // Access the private fields through reflection
        try {
            Field canvasField = LogViewerPanel.StateVisualizationPanel.class.getDeclaredField("stateCanvas");
            canvasField.setAccessible(true);
            stateCanvas = (Pane) canvasField.get(stateVisualizationPanel);

            Field titleField = LogViewerPanel.StateVisualizationPanel.class.getDeclaredField("titleLabel");
            titleField.setAccessible(true);
            titleLabel = (Label) titleField.get(stateVisualizationPanel);
        } catch (Exception e) {
            fail("Failed to access private fields: " + e.getMessage());
        }
    }

    @Test
    public void testInitialization() {
        assertNotNull(stateVisualizationPanel);
        assertNotNull(stateCanvas);
        assertNotNull(titleLabel);
        assertEquals("State Visualization", titleLabel.getText());
    }

    @Test
    public void testSetStates() {
        // Define test states
        List<String> fromStates = Arrays.asList("LoginState");
        List<String> toStates = Arrays.asList("HomeState", "DashboardState");

        // Set states
        stateVisualizationPanel.setStates(fromStates, toStates);

        // Verify title is updated
        assertEquals("State Transition: LoginState → HomeState, DashboardState", titleLabel.getText());

        // Verify canvas children
        assertFalse(stateCanvas.getChildren().isEmpty());

        // Check for state circles
        boolean foundCircles = false;
        for (Node node : stateCanvas.getChildren()) {
            if (node instanceof Circle) {
                foundCircles = true;
                break;
            }
        }
        assertTrue(foundCircles, "Should have Circle nodes for states");

        // Check for arrow line
        boolean foundLine = false;
        for (Node node : stateCanvas.getChildren()) {
            if (node instanceof Line) {
                foundLine = true;
                break;
            }
        }
        assertTrue(foundLine, "Should have Line node for arrow");

        // Check for arrow head
        boolean foundPolygon = false;
        for (Node node : stateCanvas.getChildren()) {
            if (node instanceof Polygon) {
                foundPolygon = true;
                break;
            }
        }
        assertTrue(foundPolygon, "Should have Polygon node for arrow head");
    }

    @Test
    public void testSetCurrentState() {
        // Set current state
        stateVisualizationPanel.setCurrentState("ActiveState");

        // Verify title is updated
        assertEquals("Current State: ActiveState", titleLabel.getText());

        // Verify canvas children
        assertFalse(stateCanvas.getChildren().isEmpty());

        // Check for state circle
        boolean foundCircle = false;
        for (Node node : stateCanvas.getChildren()) {
            if (node instanceof Circle) {
                foundCircle = true;
                break;
            }
        }
        assertTrue(foundCircle, "Should have Circle node for current state");

        // Check for state label
        boolean foundStateLabel = false;
        for (Node node : stateCanvas.getChildren()) {
            if (node instanceof Label && ((Label) node).getText().equals("ActiveState")) {
                foundStateLabel = true;
                break;
            }
        }
        assertTrue(foundStateLabel, "Should have Label node for current state");
    }

    @Test
    public void testClearStates() {
        // First set some states
        stateVisualizationPanel.setCurrentState("TestState");

        // Verify there are children
        assertFalse(stateCanvas.getChildren().isEmpty());

        // Clear states
        stateVisualizationPanel.clearStates();

        // Verify title is reset
        assertEquals("State Visualization", titleLabel.getText());

        // Verify canvas is cleared
        assertTrue(stateCanvas.getChildren().isEmpty());
    }

    @Test
    public void testEmptyStatesList() {
        // Test with empty lists
        stateVisualizationPanel.setStates(List.of(), List.of());

        // Title should still update
        assertEquals("State Transition:  → ", titleLabel.getText());
    }

    @Test
    public void testMultipleFromStates() {
        // Test with multiple from states
        List<String> fromStates = Arrays.asList("LoginState", "RegistrationState", "WelcomeState");
        List<String> toStates = Arrays.asList("HomeState");

        stateVisualizationPanel.setStates(fromStates, toStates);

        // Verify title
        assertEquals("State Transition: LoginState, RegistrationState, WelcomeState → HomeState", titleLabel.getText());

        // Verify multiple circles for from states
        int circleCount = 0;
        for (Node node : stateCanvas.getChildren()) {
            if (node instanceof Circle) {
                circleCount++;
            }
        }

        // Should have at least fromStates.size() + toStates.size() circles
        assertTrue(circleCount >= fromStates.size() + toStates.size(),
                "Should have at least " + (fromStates.size() + toStates.size()) + " circles, but found " + circleCount);
    }

    @Test
    public void testComplexStateTransition() {
        // Test a complex state transition visualization
        List<String> fromStates = Arrays.asList("InitialState");
        List<String> toStates = Arrays.asList("StateA", "StateB", "StateC");

        stateVisualizationPanel.setStates(fromStates, toStates);

        // Verify title
        assertEquals("State Transition: InitialState → StateA, StateB, StateC", titleLabel.getText());

        // Count state labels
        int stateLabels = 0;
        for (Node node : stateCanvas.getChildren()) {
            if (node instanceof Label) {
                Label label = (Label) node;
                if (label.getText().equals("InitialState") ||
                        label.getText().equals("StateA") ||
                        label.getText().equals("StateB") ||
                        label.getText().equals("StateC")) {
                    stateLabels++;
                }
            }
        }

        // Should have a label for each state
        assertEquals(fromStates.size() + toStates.size(), stateLabels,
                "Should have a label for each state");
    }

    /**
     * Helper method to create a StateVisualizationPanel instance.
     * This works around the fact that it's a private inner class.
     */
    private LogViewerPanel.StateVisualizationPanel createStateVisualizationPanel() {
        try {
            // Using reflection to access the private constructor
            Class<?> stateVisualizationPanelClass = Class.forName(
                    "io.github.jspinak.brobot.runner.ui.log.LogViewerPanel$StateVisualizationPanel");
            return (LogViewerPanel.StateVisualizationPanel) stateVisualizationPanelClass.newInstance();
        } catch (Exception e) {
            fail("Failed to create StateVisualizationPanel: " + e.getMessage());
            return null;
        }
    }
}