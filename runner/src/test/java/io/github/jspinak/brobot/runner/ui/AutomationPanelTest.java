package io.github.jspinak.brobot.runner.ui;

import io.github.jspinak.brobot.datatypes.project.AutomationUI;
import io.github.jspinak.brobot.datatypes.project.Button;
import io.github.jspinak.brobot.datatypes.project.Project;
import io.github.jspinak.brobot.runner.automation.AutomationExecutor;
import io.github.jspinak.brobot.runner.config.BrobotRunnerProperties;
import io.github.jspinak.brobot.runner.events.EventBus;
import io.github.jspinak.brobot.services.ProjectManager;
import javafx.application.Platform;
import javafx.scene.Node;
import javafx.scene.Scene;
import javafx.scene.control.TextArea;
import javafx.stage.Stage;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.condition.DisabledIfSystemProperty;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.context.ApplicationContext;
import org.testfx.api.FxRobot;
import org.testfx.framework.junit5.ApplicationExtension;
import org.testfx.framework.junit5.Start;
import org.testfx.util.WaitForAsyncUtils;

import java.lang.reflect.Field;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(ApplicationExtension.class)
class AutomationPanelTest {

    @Mock
    private ApplicationContext context;

    @Mock
    private ProjectManager projectManager;

    @Mock
    private BrobotRunnerProperties properties;

    @Mock
    private AutomationExecutor automationExecutor;

    @Mock
    private EventBus eventBus;

    private AutomationPanel automationPanel;
    private Stage stage;

    @Start
    private void start(Stage stage) {
        this.stage = stage;

        // Initialize mocks
        MockitoAnnotations.openMocks(this);

        try {
            // Create the panel with proper mocked dependencies
            automationPanel = new AutomationPanel(context, projectManager, properties, automationExecutor, eventBus);

            // Set up the scene
            Scene scene = new Scene(automationPanel, 800, 600);
            stage.setScene(scene);
            stage.show();

            // Ensure JavaFX is fully initialized
            WaitForAsyncUtils.waitForFxEvents();
        } catch (Exception e) {
            fail("Failed to initialize AutomationPanel: " + e.getMessage());
        }

    }

    /**
     * Tests that the panel contains the expected UI elements after construction.
     */
    @Test
    void testPanelContainsExpectedElements(FxRobot robot) {
        // Wait for UI to fully render
        WaitForAsyncUtils.waitForFxEvents();

        // Verify that essential UI elements exist
        assertNotNull(findTextArea(robot), "TextArea should exist");
        assertNotNull(robot.lookup("Refresh Automation Buttons").tryQuery().orElse(null),
                "Refresh button should exist");
        assertNotNull(robot.lookup("Stop All Automation").tryQuery().orElse(null),
                "Stop button should exist");
    }

    /**
     * Tests refreshing automation buttons when no project is loaded.
     */
    @Test
    void testRefreshAutomationButtons_noProject(FxRobot robot) {
        // Setup - no project
        when(projectManager.getActiveProject()).thenReturn(null);

        // Click the refresh button safely
        Node refreshButton = robot.lookup("Refresh Automation Buttons").queryButton();
        robot.clickOn(refreshButton);

        // Wait for UI updates
        WaitForAsyncUtils.waitForFxEvents();

        // Verify log message
        TextArea logArea = findTextArea(robot);
        assertNotNull(logArea, "TextArea should exist");
        assertTrue(logArea.getText().contains("No project loaded"),
                "Log should indicate no project is loaded");
    }

    /**
     * Tests refreshing automation buttons when a project with buttons is loaded.
     */
    @Test
    void testRefreshAutomationButtons_withButtons(FxRobot robot) {
        // Set up mock project with buttons
        Project project = new Project();

        // Create AutomationUI instance (the correct type based on Project.java)
        AutomationUI automationUI = new AutomationUI();

        // Create test buttons
        Button button1 = new Button();
        button1.setLabel("Test Button 1");
        button1.setCategory("Test Category");
        button1.setFunctionName("testFunction1");

        Button button2 = new Button();
        button2.setLabel("Test Button 2");
        button2.setCategory("Test Category");
        button2.setFunctionName("testFunction2");

        // Add buttons to the automation UI
        List<Button> buttons = Arrays.asList(button1, button2);
        automationUI.setButtons(buttons);

        // Set the automation UI on the project
        project.setAutomation(automationUI);

        // Configure mock to return our project
        when(projectManager.getActiveProject()).thenReturn(project);

        // Run UI update on the JavaFX thread and wait for completion
        syncExec(() -> automationPanel.refreshAutomationButtons());

        // Wait for UI updates
        WaitForAsyncUtils.waitForFxEvents();

        // Verify TextArea contains correct message
        TextArea logArea = findTextArea(robot);
        assertNotNull(logArea, "TextArea should exist");
        assertTrue(logArea.getText().contains("Found 2 automation functions"),
                "Log should indicate 2 automation functions were found");
    }

    /**
     * Tests the stop all automation functionality.
     */
    @Test
    void testStopAllAutomation(FxRobot robot) throws IllegalAccessException, NoSuchFieldException {
        Field isRunningField = AutomationPanel.class.getDeclaredField("isRunning");
        isRunningField.setAccessible(true);
        isRunningField.set(automationPanel, true);

        // Find and click the stop button
        Node stopButton = robot.lookup("Stop All Automation").queryButton();
        robot.clickOn(stopButton);

        // Wait for UI updates
        WaitForAsyncUtils.waitForFxEvents();

        // Verify that the executor was called
        verify(automationExecutor, timeout(1000)).stopAllAutomation();
    }

    /**
     * Tests the log method of AutomationPanel.
     * This test adds a message to the log and verifies it appears in the TextArea.
     */
    @Test
    @DisabledIfSystemProperty(
            named = "testfx.headless",
            matches = "true",
            disabledReason = "This test can be unstable in headless environments"
    )
    void testLog(FxRobot robot) {
        // Create a unique test message to identify in log
        final String testMessage = "Test log message " + System.currentTimeMillis();

        // Add the message to the log on the JavaFX thread and wait for completion
        syncExec(() -> automationPanel.log(testMessage));

        // Find TextArea and verify content
        TextArea logArea = findTextArea(robot);
        assertNotNull(logArea, "TextArea should exist");

        // Allow some time for text to appear (with timeout)
        try {
            WaitForAsyncUtils.waitFor(5, TimeUnit.SECONDS, () ->
                    logArea.getText().contains(testMessage)
            );
        } catch (TimeoutException e) {
            fail("Test message did not appear in log within timeout: " + e.getMessage());
        }

        assertTrue(logArea.getText().contains(testMessage),
                "Log area should contain the test message");
    }

    /**
     * Tests the log method with a different approach that may be more reliable
     * in headless testing environments.
     */
    @Test
    void testLogAltMethod(FxRobot robot) {
        // Create a unique test message
        final String testMessage = "Alt log test " + System.currentTimeMillis();

        // Use a different approach to log the message
        syncExec(() -> {
            automationPanel.log(testMessage);
            // Force layout update
            automationPanel.layout();
        });

        // Wait for UI updates
        WaitForAsyncUtils.waitForFxEvents();

        // Get log text through direct access rather than UI query
        String logText = syncGet(() -> findTextArea(robot).getText());

        // Verify log content
        assert logText != null;
        assertTrue(logText.contains(testMessage),
                "Log area should contain the test message");
    }

    // ---- Helper Methods ----

    /**
     * Finds the TextArea in the AutomationPanel.
     */
    private TextArea findTextArea(FxRobot robot) {
        return robot.lookup(".text-area").queryAs(TextArea.class);
    }

    /**
     * Executes a runnable on the JavaFX thread and waits for completion.
     */
    private void syncExec(Runnable action) {
        if (Platform.isFxApplicationThread()) {
            action.run();
        } else {
            try {
                WaitForAsyncUtils.waitForAsyncFx(2000, action);
            } catch (Exception e) {
                fail("Failed to execute action on JavaFX thread: " + e.getMessage());
            }
        }
    }

    /**
     * Gets a value from the JavaFX thread.
     */
    private <T> T syncGet(java.util.function.Supplier<T> supplier) {
        if (Platform.isFxApplicationThread()) {
            return supplier.get();
        } else {
            try {
                return WaitForAsyncUtils.waitForAsyncFx(2000, supplier::get);
            } catch (Exception e) {
                fail("Failed to get value from JavaFX thread: " + e.getMessage());
                return null; // Won't reach here due to fail() above
            }
        }
    }

    @Test
    void testConstruction() {
        // Look for the TextArea
        assertNotNull(automationPanel.lookup(".text-area"), "TextArea should exist");

        // Look for the Refresh button by its ID
        assertNotNull(automationPanel.lookup("#refreshAutomationButtons"),
                "Refresh button should exist");

        // Look for the Stop button by its ID
        assertNotNull(automationPanel.lookup("#stopAllAutomation"),
                "Stop button should exist");
    }

}