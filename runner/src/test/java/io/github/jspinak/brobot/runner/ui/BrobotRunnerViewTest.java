package io.github.jspinak.brobot.runner.ui;

import io.github.jspinak.brobot.runner.config.BrobotRunnerProperties;
import io.github.jspinak.brobot.runner.init.BrobotLibraryInitializer;
import javafx.scene.Scene;
import javafx.scene.control.TabPane;
import javafx.stage.Stage;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.api.io.TempDir;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.testfx.api.FxRobot;
import org.testfx.framework.junit5.ApplicationExtension;
import org.testfx.framework.junit5.Start;
import org.testfx.util.WaitForAsyncUtils;

import java.nio.file.Path;
import java.nio.file.Paths;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.Mockito.when;

@ExtendWith(ApplicationExtension.class)
class BrobotRunnerViewTest {

    @Mock
    private BrobotRunnerProperties properties;

    @Mock
    private BrobotLibraryInitializer libraryInitializer;

    private BrobotRunnerView view;

    @TempDir
    Path tempDir; // Create a temporary directory for paths

    @Start
    private void start(Stage stage) {
        MockitoAnnotations.openMocks(this);

        // Set up mock properties to return non-null values
        // These are the paths that ConfigurationPanel will try to access
        when(properties.getProjectConfigPath()).thenReturn(Paths.get(tempDir.toString(), "project.json"));
        when(properties.getDslConfigPath()).thenReturn(Paths.get(tempDir.toString(), "dsl.json"));
        when(properties.getImagePath()).thenReturn(tempDir.toString());
        when(properties.getConfigPath()).thenReturn(tempDir.toString());
        when(properties.getLogPath()).thenReturn(tempDir.toString());

        // Create the view with mocked dependencies
        view = new BrobotRunnerView(properties, libraryInitializer);

        // Set up the scene
        Scene scene = new Scene(view, 800, 600);
        stage.setScene(scene);
        stage.show();

        // Wait for JavaFX initialization
        WaitForAsyncUtils.waitForFxEvents();
    }

    @Test
    void testTabsCreated(FxRobot robot) {
        // Get the tab pane
        TabPane tabPane = (TabPane) view.getCenter();

        // Verify that tabs are created
        assertNotNull(tabPane, "TabPane should exist");
        assertEquals(2, tabPane.getTabs().size(), "There should be 2 tabs");
        assertEquals("Configuration", tabPane.getTabs().get(0).getText(), "First tab should be Configuration");
        assertEquals("Automation", tabPane.getTabs().get(1).getText(), "Second tab should be Automation");
    }

    @Test
    void testTabSwitching(FxRobot robot) {
        // Get the tab pane
        TabPane tabPane = (TabPane) view.getCenter();

        // Ensure we start on the first tab
        tabPane.getSelectionModel().select(0);
        assertEquals(0, tabPane.getSelectionModel().getSelectedIndex(), "Should start on first tab");

        // Test tab switching
        robot.clickOn("Automation");
        WaitForAsyncUtils.waitForFxEvents(); // Wait for UI update
        assertEquals(1, tabPane.getSelectionModel().getSelectedIndex(), "Should now be on Automation tab");

        robot.clickOn("Configuration");
        WaitForAsyncUtils.waitForFxEvents(); // Wait for UI update
        assertEquals(0, tabPane.getSelectionModel().getSelectedIndex(), "Should now be back on Configuration tab");
    }

    @Test
    void testTabContents(FxRobot robot) {
        // First verify Configuration tab contents
        WaitForAsyncUtils.waitForFxEvents();

        // Switch to Configuration tab
        robot.clickOn("Configuration");
        WaitForAsyncUtils.waitForFxEvents();

        // Look for specific elements in the Configuration tab
        // Note: Elements might be wrapped in ScrollPane or other containers, use careful selectors
        assertNotNull(robot.lookup(".button").queryAll().stream()
                        .filter(b -> b.toString().contains("Load Configuration"))
                        .findFirst()
                        .orElse(null),
                "Load Configuration button should exist");

        // Switch to Automation tab
        robot.clickOn("Automation");
        WaitForAsyncUtils.waitForFxEvents();

        // Look for specific elements in the Automation tab
        assertNotNull(robot.lookup(".button").queryAll().stream()
                        .filter(b -> b.toString().contains("Refresh Automation Buttons"))
                        .findFirst()
                        .orElse(null),
                "Refresh Automation Buttons should exist");
    }
}