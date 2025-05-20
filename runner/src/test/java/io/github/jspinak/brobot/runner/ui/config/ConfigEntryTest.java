package io.github.jspinak.brobot.runner.ui.config;

import org.junit.jupiter.api.Test;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.LocalDateTime;

import static org.junit.jupiter.api.Assertions.*;

public class ConfigEntryTest {

    @Test
    public void testConfigEntryCreation() {
        // Arrange
        String name = "Test Config";
        String project = "Test Project";
        Path projectConfigPath = Paths.get("/path/to/project_config.json");
        Path dslConfigPath = Paths.get("/path/to/dsl_config.json");
        Path imagePath = Paths.get("/path/to/images");
        LocalDateTime lastModified = LocalDateTime.now();

        // Act
        ConfigEntry configEntry = new ConfigEntry(
                name,
                project,
                projectConfigPath,
                dslConfigPath,
                imagePath,
                lastModified
        );

        // Assert
        assertEquals(name, configEntry.getName());
        assertEquals(project, configEntry.getProject());
        assertEquals(projectConfigPath, configEntry.getProjectConfigPath());
        assertEquals(dslConfigPath, configEntry.getDslConfigPath());
        assertEquals(imagePath, configEntry.getImagePath());
        assertEquals(lastModified, configEntry.getLastModified());
    }

    @Test
    public void testGetFileNames() {
        // Arrange
        ConfigEntry configEntry = new ConfigEntry(
                "Test Config",
                "Test Project",
                Paths.get("/path/to/project_config.json"),
                Paths.get("/path/to/dsl_config.json"),
                Paths.get("/path/to/images"),
                LocalDateTime.now()
        );

        // Act & Assert
        assertEquals("project_config.json", configEntry.getProjectConfigFileName());
        assertEquals("dsl_config.json", configEntry.getDslConfigFileName());
        assertEquals("images", configEntry.getImageDirectoryName());
    }

    @Test
    public void testSettersAndGetters() {
        // Arrange
        ConfigEntry configEntry = new ConfigEntry(
                "Test Config",
                "Test Project",
                Paths.get("/path/to/project_config.json"),
                Paths.get("/path/to/dsl_config.json"),
                Paths.get("/path/to/images"),
                LocalDateTime.now()
        );

        // Act
        configEntry.setName("Updated Config");
        configEntry.setProject("Updated Project");
        configEntry.setDescription("Test Description");
        configEntry.setAuthor("Test Author");
        configEntry.setVersion("1.0.0");

        // Assert
        assertEquals("Updated Config", configEntry.getName());
        assertEquals("Updated Project", configEntry.getProject());
        assertEquals("Test Description", configEntry.getDescription());
        assertEquals("Test Author", configEntry.getAuthor());
        assertEquals("1.0.0", configEntry.getVersion());
    }
}