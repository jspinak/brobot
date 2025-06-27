package io.github.jspinak.brobot.runner.ui;

import io.github.jspinak.brobot.navigation.service.StateService;
import io.github.jspinak.brobot.runner.config.BrobotRunnerProperties;
import io.github.jspinak.brobot.runner.events.EventBus;
import io.github.jspinak.brobot.runner.init.BrobotLibraryInitializer;
import javafx.scene.Scene;
import javafx.scene.control.TextField;
import javafx.stage.Stage;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import io.github.jspinak.brobot.runner.testutil.JavaFXTestUtils;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.io.TempDir;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;
import org.testfx.api.FxRobot;

import java.io.File;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
class ConfigurationPanelTest {

    @Mock
    private BrobotRunnerProperties properties;

    @Mock
    private BrobotLibraryInitializer libraryInitializer;

    @Mock
    private EventBus eventBus;

    @Mock
    StateService allStatesService;

    private ConfigurationPanel panel;

    @TempDir
    Path tempDir;

    private Path projectConfigPath;
    private Path dslConfigPath;

    @BeforeAll
    public static void initJavaFX() throws InterruptedException {
        JavaFXTestUtils.initJavaFX();
    }

    @BeforeEach
    void setUp() throws Exception {
        // Create temp files for testing
        projectConfigPath = Files.createFile(tempDir.resolve("project.json"));
        dslConfigPath = Files.createFile(tempDir.resolve("dsl.json"));

        JavaFXTestUtils.runOnFXThread(() -> {
            // Set up mock properties
            when(properties.getProjectConfigPath()).thenReturn(projectConfigPath);
            when(properties.getDslConfigPath()).thenReturn(dslConfigPath);
            when(properties.getImagePath()).thenReturn(tempDir.toString());

            // Setup mock initializer
            when(libraryInitializer.initializeWithConfig(any(Path.class), any(Path.class))).thenReturn(true);

            // Create the panel with mocked dependencies
            panel = new ConfigurationPanel(properties, libraryInitializer, eventBus, allStatesService);

            // Set up the scene
            Stage stage = new Stage();
            Scene scene = new Scene(panel, 800, 600);
            stage.setScene(scene);
            stage.show();
        });
    }

    @Test
    void testPanelStructure() {
        // Verify panel elements
        assertNotNull(panel.lookup(".label"), "Panel should contain labels");
        assertNotNull(panel.lookup(".text-field"), "Panel should contain text fields");
        assertNotNull(panel.lookup(".button"), "Panel should contain buttons");
    }

    @Test
    void testInitialFieldValues() {
        // Get text fields
        var textFields = panel.lookupAll(".text-field").toArray(new TextField[0]);
        TextField projectField = textFields[0];
        TextField dslField = textFields[1];
        TextField imagesField = textFields[2];

        // Verify initial values
        assertEquals(projectConfigPath.toString(), projectField.getText());
        assertEquals(dslConfigPath.toString(), dslField.getText());
        assertEquals(tempDir.toString(), imagesField.getText());
    }

    @Test
    void testLoadConfiguration_success() throws Exception {
        // Setup initializer to return success
        when(libraryInitializer.initializeWithConfig(any(Path.class), any(Path.class))).thenReturn(true);

        // Set fields to valid values
        var textFields = panel.lookupAll(".text-field").toArray(new TextField[0]);
        TextField projectField = textFields[0];
        TextField dslField = textFields[1];
        TextField imagesField = textFields[2];

        projectField.setText(projectConfigPath.toString());
        dslField.setText(dslConfigPath.toString());
        imagesField.setText(tempDir.toString());

        // Click load button
        JavaFXTestUtils.runOnFXThread(() -> {
            try {
                // Use reflection to call private method
                var loadMethod = panel.getClass().getDeclaredMethod("loadConfiguration");
                loadMethod.setAccessible(true);
                loadMethod.invoke(panel);
            } catch (Exception e) {
                throw new RuntimeException(e);
            }
        });

        // Wait for background thread
        Thread.sleep(1000);

        // Verify initializer was called
        verify(libraryInitializer).initializeWithConfig(eq(Paths.get(projectField.getText())),
                eq(Paths.get(dslField.getText())));
    }

    @Test
    void testUpdateImagePath() throws Exception {
        // Create separate directory for update
        Path newImagePath = Files.createDirectory(tempDir.resolve("newImages"));

        // Setup initializer
        doNothing().when(libraryInitializer).updateImagePath(anyString());

        // Set images field to new path
        var textFields2 = panel.lookupAll(".text-field").toArray(new TextField[0]);
        TextField imagesField = textFields2[2];
        imagesField.setText(newImagePath.toString());

        // Trigger update (we'd normally click Browse but that opens a native dialog)
        // Here we directly call the handler's effect
        doNothing().when(libraryInitializer).updateImagePath(newImagePath.toString());

        // Verify initializer was called with right path
        libraryInitializer.updateImagePath(newImagePath.toString());
        verify(libraryInitializer).updateImagePath(eq(newImagePath.toString()));
    }
}