package io.github.jspinak.brobot.runner.ui.log;

import javafx.scene.Node;
import javafx.scene.control.Label;
import javafx.scene.layout.Pane;
import javafx.scene.layout.StackPane;
import javafx.scene.shape.Line;
import javafx.scene.shape.Polygon;
import javafx.stage.Stage;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import io.github.jspinak.brobot.runner.testutil.JavaFXTestUtils;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.testfx.util.WaitForAsyncUtils;

import java.lang.reflect.Field;
import java.util.Arrays;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

@ExtendWith({}) // No extension needed since we're initializing JavaFX manually
public class StateVisualizationPanelTest {

    private LogViewerPanel.StateVisualizationPanel stateVisualizationPanel;
    private Pane stateCanvas;
    private Label titleLabel;

    @BeforeAll
    public static void initJavaFX() throws InterruptedException {
        JavaFXTestUtils.initJavaFX();
    }

    @BeforeEach
    void setUp() throws InterruptedException {
        JavaFXTestUtils.runOnFXThread(() -> {
            // This setup correctly initializes the panel for all tests
            stateVisualizationPanel = createStateVisualizationPanel();
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
        });
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
        List<String> fromStates = Arrays.asList("LoginState");
        List<String> toStates = Arrays.asList("HomeState", "DashboardState");

        stateVisualizationPanel.setStates(fromStates, toStates);
        WaitForAsyncUtils.waitForFxEvents(); // Wait for UI update

        assertEquals("State Transition: LoginState → HomeState, DashboardState", titleLabel.getText());
        assertFalse(stateCanvas.getChildren().isEmpty());
        assertTrue(stateCanvas.getChildren().stream().anyMatch(node -> node instanceof StackPane));
        assertTrue(stateCanvas.getChildren().stream().anyMatch(node -> node instanceof Line));
        assertTrue(stateCanvas.getChildren().stream().anyMatch(node -> node instanceof Polygon));
    }

    @Test
    public void testSetCurrentState() {
        // FIX: Use the class-level instance, not a new one.
        stateVisualizationPanel.setCurrentState("ActiveState");
        WaitForAsyncUtils.waitForFxEvents(); // Wait for UI update

        // FIX: Assert on the correct titleLabel instance.
        assertEquals("Current State: ActiveState", titleLabel.getText());
        assertFalse(stateCanvas.getChildren().isEmpty());
    }

    @Test
    public void testClearStates() {
        stateVisualizationPanel.setCurrentState("TestState");
        WaitForAsyncUtils.waitForFxEvents();
        assertFalse(stateCanvas.getChildren().isEmpty());

        stateVisualizationPanel.clearStates();
        WaitForAsyncUtils.waitForFxEvents();

        assertEquals("State Visualization", titleLabel.getText());
        assertTrue(stateCanvas.getChildren().isEmpty());
    }

    @Test
    public void testEmptyStatesList() {
        stateVisualizationPanel.setStates(List.of(), List.of());
        WaitForAsyncUtils.waitForFxEvents(); // Wait for UI update
        assertEquals("State Transition:  → ", titleLabel.getText());
    }

    @Test
    public void testMultipleFromStates() {
        List<String> fromStates = Arrays.asList("LoginState", "RegistrationState", "WelcomeState");
        List<String> toStates = Arrays.asList("HomeState");

        stateVisualizationPanel.setStates(fromStates, toStates);
        WaitForAsyncUtils.waitForFxEvents(); // Wait for UI update

        assertEquals("State Transition: LoginState, RegistrationState, WelcomeState → HomeState", titleLabel.getText());

        long circleCount = stateCanvas.getChildren().stream().filter(node -> node instanceof StackPane).count();
        assertEquals(fromStates.size() + toStates.size(), circleCount,
                "Should have a StackPane (with a Circle) for each state");
    }

    @Test
    public void testComplexStateTransition() {
        List<String> fromStates = List.of("InitialState");
        List<String> toStates = List.of("StateA", "StateB", "StateC");
        stateVisualizationPanel.setStates(fromStates, toStates);
        WaitForAsyncUtils.waitForFxEvents();

        assertEquals("State Transition: InitialState → StateA, StateB, StateC", titleLabel.getText());

        int stateLabels = 0;
        for (Node node : stateCanvas.getChildren()) {
            if (node instanceof StackPane stackPane) {
                for (Node childNode : stackPane.getChildren()) {
                    if (childNode instanceof Label label) {
                        if (fromStates.contains(label.getText()) || toStates.contains(label.getText())) {
                            stateLabels++;
                        }
                    }
                }
            }
        }
        assertEquals(fromStates.size() + toStates.size(), stateLabels, "Should have a label for each state");
    }

    private LogViewerPanel.StateVisualizationPanel createStateVisualizationPanel() {
        try {
            Class<?> panelClass = Class.forName("io.github.jspinak.brobot.runner.ui.log.LogViewerPanel$StateVisualizationPanel");
            return (LogViewerPanel.StateVisualizationPanel) panelClass.getDeclaredConstructor().newInstance();
        } catch (Exception e) {
            fail("Failed to create StateVisualizationPanel: " + e.getMessage());
            return null;
        }
    }
}