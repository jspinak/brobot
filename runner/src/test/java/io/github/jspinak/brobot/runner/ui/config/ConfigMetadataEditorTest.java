package io.github.jspinak.brobot.runner.ui.config;

import io.github.jspinak.brobot.datatypes.project.Project;
import io.github.jspinak.brobot.runner.events.EventBus;
import io.github.jspinak.brobot.runner.events.LogEvent;
import io.github.jspinak.brobot.services.ProjectManager;
import javafx.application.Platform;
import javafx.scene.Scene;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;
import javafx.stage.Stage;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;
import org.testfx.framework.junit5.ApplicationExtension;
import org.testfx.framework.junit5.Start;

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.nio.file.Paths;
import java.time.LocalDateTime;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith({MockitoExtension.class, ApplicationExtension.class})
public class ConfigMetadataEditorTest {

    private EventBus eventBus;
    private ProjectManager projectManager;
    private ConfigMetadataEditor editor;
    private Stage stage;

    @Start
    public void start(Stage stage) {
        this.stage = stage;

        // Initialize mocks
        eventBus = mock(EventBus.class);
        projectManager = mock(ProjectManager.class);
    }

    private void createEditor() {
        // Create a fresh editor instance for each test
        CountDownLatch latch = new CountDownLatch(1);
        Platform.runLater(() -> {
            editor = new ConfigMetadataEditor(eventBus, projectManager);
            stage.setScene(new Scene(editor, 800, 600));
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
    public void testSetConfiguration() throws Exception {
        // Create the editor
        createEditor();

        // Prepare test data
        ConfigEntry config = new ConfigEntry(
                "Test Config",
                "Test Project",
                Paths.get("/path/to/project_config.json"),
                Paths.get("/path/to/dsl_config.json"),
                Paths.get("/path/to/images"),
                LocalDateTime.now()
        );
        config.setDescription("Test Description");
        config.setAuthor("Test Author");
        config.setVersion("1.0.0");

        Project mockProject = mock(Project.class);
        when(mockProject.getName()).thenReturn("Test Project");
        when(mockProject.getVersion()).thenReturn("1.0.0");
        when(mockProject.getAuthor()).thenReturn("Test Author");
        when(mockProject.getDescription()).thenReturn("Test Description");
        when(projectManager.getActiveProject()).thenReturn(mockProject);

        // Execute test on JavaFX thread
        CountDownLatch testLatch = new CountDownLatch(1);
        Platform.runLater(() -> {
            try {
                // Set configuration
                editor.setConfiguration(config);

                // Access private fields using reflection
                Field projectNameField = ConfigMetadataEditor.class.getDeclaredField("projectNameField");
                Field versionField = ConfigMetadataEditor.class.getDeclaredField("versionField");
                Field authorField = ConfigMetadataEditor.class.getDeclaredField("authorField");
                Field descriptionArea = ConfigMetadataEditor.class.getDeclaredField("descriptionArea");
                Field currentConfig = ConfigMetadataEditor.class.getDeclaredField("currentConfig");

                projectNameField.setAccessible(true);
                versionField.setAccessible(true);
                authorField.setAccessible(true);
                descriptionArea.setAccessible(true);
                currentConfig.setAccessible(true);

                // Assert fields have been set correctly
                assertEquals(config, currentConfig.get(editor));
                assertEquals("Test Project", ((TextField)projectNameField.get(editor)).getText());
                assertEquals("1.0.0", ((TextField)versionField.get(editor)).getText());
                assertEquals("Test Author", ((TextField)authorField.get(editor)).getText());
                assertEquals("Test Description", ((TextArea)descriptionArea.get(editor)).getText());

                testLatch.countDown();
            } catch (Exception e) {
                fail("Exception during test: " + e.getMessage());
            }
        });

        assertTrue(testLatch.await(5, TimeUnit.SECONDS), "Test operation timed out");
    }

    @Test
    public void testClear() throws Exception {
        // Create the editor
        createEditor();

        // Prepare test data
        ConfigEntry config = new ConfigEntry(
                "Test Config",
                "Test Project",
                Paths.get("/path/to/project_config.json"),
                Paths.get("/path/to/dsl_config.json"),
                Paths.get("/path/to/images"),
                LocalDateTime.now()
        );

        // Execute test on JavaFX thread
        CountDownLatch testLatch = new CountDownLatch(1);
        Platform.runLater(() -> {
            try {
                // Set initial configuration
                editor.setConfiguration(config);

                // Access private fields using reflection
                Field projectNameField = ConfigMetadataEditor.class.getDeclaredField("projectNameField");
                Field versionField = ConfigMetadataEditor.class.getDeclaredField("versionField");
                Field authorField = ConfigMetadataEditor.class.getDeclaredField("authorField");
                Field descriptionArea = ConfigMetadataEditor.class.getDeclaredField("descriptionArea");
                Field currentConfig = ConfigMetadataEditor.class.getDeclaredField("currentConfig");
                Field statusLabel = ConfigMetadataEditor.class.getDeclaredField("statusLabel");

                projectNameField.setAccessible(true);
                versionField.setAccessible(true);
                authorField.setAccessible(true);
                descriptionArea.setAccessible(true);
                currentConfig.setAccessible(true);
                statusLabel.setAccessible(true);

                // Clear the editor
                editor.clear();

                // Assert fields have been cleared
                assertNull(currentConfig.get(editor));
                assertEquals("", ((TextField)projectNameField.get(editor)).getText());
                assertEquals("", ((TextField)versionField.get(editor)).getText());
                assertEquals("", ((TextField)authorField.get(editor)).getText());
                assertEquals("", ((TextArea)descriptionArea.get(editor)).getText());

                testLatch.countDown();
            } catch (Exception e) {
                fail("Exception during test: " + e.getMessage());
            }
        });

        assertTrue(testLatch.await(5, TimeUnit.SECONDS), "Test operation timed out");
    }

    @Test
    public void testExtractJsonValue() throws Exception {
        // Create the editor
        createEditor();

        // Execute test on JavaFX thread
        CountDownLatch testLatch = new CountDownLatch(1);
        Platform.runLater(() -> {
            try {
                // Access the private method using reflection
                Method extractJsonValueMethod = ConfigMetadataEditor.class.getDeclaredMethod(
                        "extractJsonValue", String.class, String.class);
                extractJsonValueMethod.setAccessible(true);

                // Test cases
                String json = "{\"name\": \"Test Project\", \"version\": \"1.0.0\", \"author\": \"Test Author\"}";

                // Act & Assert
                assertEquals("Test Project", extractJsonValueMethod.invoke(editor, json, "name"));
                assertEquals("1.0.0", extractJsonValueMethod.invoke(editor, json, "version"));
                assertEquals("Test Author", extractJsonValueMethod.invoke(editor, json, "author"));
                assertEquals("", extractJsonValueMethod.invoke(editor, json, "nonexistent"));
                assertEquals("", extractJsonValueMethod.invoke(editor, "", "name"));
                assertEquals("", extractJsonValueMethod.invoke(editor, null, "name"));

                testLatch.countDown();
            } catch (Exception e) {
                fail("Exception during test: " + e.getMessage());
            }
        });

        assertTrue(testLatch.await(5, TimeUnit.SECONDS), "Test operation timed out");
    }

    @Test
    public void testUpdateJsonValue() throws Exception {
        // Create the editor
        createEditor();

        // Execute test on JavaFX thread
        CountDownLatch testLatch = new CountDownLatch(1);
        Platform.runLater(() -> {
            try {
                // Access the private method using reflection
                Method updateJsonValueMethod = ConfigMetadataEditor.class.getDeclaredMethod(
                        "updateJsonValue", String.class, String.class, String.class);
                updateJsonValueMethod.setAccessible(true);

                // Test cases
                String json = "{\"name\": \"Test Project\", \"version\": \"1.0.0\", \"author\": \"Test Author\"}";

                // Act
                String updatedJson = (String) updateJsonValueMethod.invoke(editor, json, "name", "Updated Project");
                String addedJson = (String) updateJsonValueMethod.invoke(editor, json, "description", "Added Description");
                String removedJson = (String) updateJsonValueMethod.invoke(editor, json, "author", "");

                // Assert
                assertTrue(updatedJson.contains("\"name\": \"Updated Project\""));
                assertTrue(addedJson.contains("\"description\": \"Added Description\""));
                assertFalse(removedJson.contains("\"author\": \"Test Author\""));

                testLatch.countDown();
            } catch (Exception e) {
                fail("Exception during test: " + e.getMessage());
            }
        });

        assertTrue(testLatch.await(5, TimeUnit.SECONDS), "Test operation timed out");
    }

    @Test
    public void testSaveChanges() throws Exception {
        // Create the editor
        createEditor();

        // Execute test on JavaFX thread
        CountDownLatch testLatch = new CountDownLatch(1);
        Platform.runLater(() -> {
            try {
                // Arrange
                ConfigEntry config = new ConfigEntry(
                        "Test Config",
                        "Test Project",
                        Paths.get("/path/to/project_config.json"),
                        Paths.get("/path/to/dsl_config.json"),
                        Paths.get("/path/to/images"),
                        LocalDateTime.now()
                );

                // Set fields via reflection
                Field projectNameField = ConfigMetadataEditor.class.getDeclaredField("projectNameField");
                Field versionField = ConfigMetadataEditor.class.getDeclaredField("versionField");
                Field authorField = ConfigMetadataEditor.class.getDeclaredField("authorField");
                Field descriptionArea = ConfigMetadataEditor.class.getDeclaredField("descriptionArea");
                Field currentConfig = ConfigMetadataEditor.class.getDeclaredField("currentConfig");
                Field hasUnsavedChanges = ConfigMetadataEditor.class.getDeclaredField("hasUnsavedChanges");

                projectNameField.setAccessible(true);
                versionField.setAccessible(true);
                authorField.setAccessible(true);
                descriptionArea.setAccessible(true);
                currentConfig.setAccessible(true);
                hasUnsavedChanges.setAccessible(true);

                editor.setConfiguration(config);

                // Set new values
                ((TextField)projectNameField.get(editor)).setText("Updated Project");
                ((TextField)versionField.get(editor)).setText("2.0.0");
                ((TextField)authorField.get(editor)).setText("Updated Author");
                ((TextArea)descriptionArea.get(editor)).setText("Updated Description");
                hasUnsavedChanges.set(editor, true);

                // Mock project
                Project mockProject = mock(Project.class);
                when(projectManager.getActiveProject()).thenReturn(mockProject);

                // Create spy to avoid file operations
                ConfigMetadataEditor spyEditor = spy(editor);
                doNothing().when(spyEditor).updateProjectConfigFile(any(ConfigEntry.class));

                // Access saveChanges method
                Method saveChangesMethod = ConfigMetadataEditor.class.getDeclaredMethod("saveChanges");
                saveChangesMethod.setAccessible(true);

                // Act
                saveChangesMethod.invoke(spyEditor);

                // Assert
                verify(spyEditor).updateProjectConfigFile(config);
                verify(eventBus).publish(any(LogEvent.class));
                assertEquals("Updated Project", config.getProject());
                assertEquals("2.0.0", config.getVersion());
                assertEquals("Updated Author", config.getAuthor());
                assertEquals("Updated Description", config.getDescription());
                assertFalse((boolean)hasUnsavedChanges.get(spyEditor));

                testLatch.countDown();
            } catch (Exception e) {
                fail("Exception during test: " + e.getMessage());
            }
        });

        assertTrue(testLatch.await(5, TimeUnit.SECONDS), "Test operation timed out");
    }
}