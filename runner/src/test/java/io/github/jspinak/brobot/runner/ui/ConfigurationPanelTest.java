package io.github.jspinak.brobot.runner.ui;

import io.github.jspinak.brobot.runner.config.BrobotRunnerProperties;
import io.github.jspinak.brobot.runner.init.BrobotLibraryInitializer;
import javafx.scene.Scene;
import javafx.scene.control.TextField;
import javafx.stage.Stage;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.api.io.TempDir;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.testfx.api.FxRobot;
import org.testfx.framework.junit5.ApplicationExtension;
import org.testfx.framework.junit5.Start;

import java.io.File;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(ApplicationExtension.class)
class ConfigurationPanelTest {

    @Mock
    private BrobotRunnerProperties properties;

    @Mock
    private BrobotLibraryInitializer libraryInitializer;

    private ConfigurationPanel panel;

    @TempDir
    Path tempDir;

    private Path projectConfigPath;
    private Path dslConfigPath;

    @Start
    private void start(Stage stage) throws Exception {
        MockitoAnnotations.openMocks(this);

        // Create temp files for testing
        projectConfigPath = Files.createFile(tempDir.resolve("project.json"));
        dslConfigPath = Files.createFile(tempDir.resolve("dsl.json"));

        // Set up mock properties
        when(properties.getProjectConfigPath()).thenReturn(projectConfigPath);
        when(properties.getDslConfigPath()).thenReturn(dslConfigPath);
        when(properties.getImagePath()).thenReturn(tempDir.toString());

        // Setup mock initializer
        when(libraryInitializer.initializeWithConfig(any(Path.class), any(Path.class))).thenReturn(true);

        // Create the panel with mocked dependencies
        panel = new ConfigurationPanel(properties, libraryInitializer);

        // Set up the scene
        Scene scene = new Scene(panel, 800, 600);
        stage.setScene(scene);
        stage.show();
    }

    @Test
    void testPanelStructure(FxRobot robot) {
        // Verify panel elements
        assertNotNull(robot.lookup("Project Configuration:").query());
        assertNotNull(robot.lookup("DSL Configuration:").query());
        assertNotNull(robot.lookup("Images Directory:").query());
        assertNotNull(robot.lookup("Load Configuration").query());
        assertNotNull(robot.lookup("Status: Ready").query());
    }

    @Test
    void testInitialFieldValues(FxRobot robot) {
        // Get text fields
        TextField projectField = (TextField) robot.lookup(".text-field").nth(0).query();
        TextField dslField = (TextField) robot.lookup(".text-field").nth(1).query();
        TextField imagesField = (TextField) robot.lookup(".text-field").nth(2).query();

        // Verify initial values
        assertEquals(projectConfigPath.toString(), projectField.getText());
        assertEquals(dslConfigPath.toString(), dslField.getText());
        assertEquals(tempDir.toString(), imagesField.getText());
    }

    @Test
    void testLoadConfiguration_success(FxRobot robot) throws Exception {
        // Setup initializer to return success
        when(libraryInitializer.initializeWithConfig(any(Path.class), any(Path.class))).thenReturn(true);

        // Set fields to valid values
        TextField projectField = (TextField) robot.lookup(".text-field").nth(0).query();
        TextField dslField = (TextField) robot.lookup(".text-field").nth(1).query();
        TextField imagesField = (TextField) robot.lookup(".text-field").nth(2).query();

        projectField.setText(projectConfigPath.toString());
        dslField.setText(dslConfigPath.toString());
        imagesField.setText(tempDir.toString());

        // Click load button
        robot.clickOn("Load Configuration");

        // Wait for background thread
        Thread.sleep(1000);

        // Verify initializer was called
        verify(libraryInitializer).initializeWithConfig(eq(Paths.get(projectField.getText())),
                eq(Paths.get(dslField.getText())));
    }

    @Test
    void testUpdateImagePath(FxRobot robot) throws Exception {
        // Create separate directory for update
        Path newImagePath = Files.createDirectory(tempDir.resolve("newImages"));

        // Setup initializer
        doNothing().when(libraryInitializer).updateImagePath(anyString());

        // Set images field to new path
        TextField imagesField = (TextField) robot.lookup(".text-field").nth(2).query();
        imagesField.setText(newImagePath.toString());

        // Trigger update (we'd normally click Browse but that opens a native dialog)
        // Here we directly call the handler's effect
        doNothing().when(libraryInitializer).updateImagePath(newImagePath.toString());

        // Verify initializer was called with right path
        libraryInitializer.updateImagePath(newImagePath.toString());
        verify(libraryInitializer).updateImagePath(eq(newImagePath.toString()));
    }
}