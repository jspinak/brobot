package io.github.jspinak.brobot.runner.ui.config;

import io.github.jspinak.brobot.runner.events.EventBus;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.testfx.framework.junit5.ApplicationExtension;

import java.nio.file.Paths;
import java.time.LocalDateTime;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;

@ExtendWith({MockitoExtension.class, ApplicationExtension.class})
public class ConfigDetailsPanelTest {

    @Mock
    private EventBus eventBus;

    private ConfigDetailsPanel detailsPanel;

    @BeforeEach
    public void setUp() {
        detailsPanel = new ConfigDetailsPanel(eventBus);
    }

    @Test
    public void testSetConfiguration() {
        // Arrange
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

        // Act
        detailsPanel.setConfiguration(config);

        // Assert
        assertEquals(config, detailsPanel.getConfiguration());
    }

    @Test
    public void testClearConfiguration() {
        // Arrange
        ConfigEntry config = new ConfigEntry(
                "Test Config",
                "Test Project",
                Paths.get("/path/to/project_config.json"),
                Paths.get("/path/to/dsl_config.json"),
                Paths.get("/path/to/images"),
                LocalDateTime.now()
        );
        detailsPanel.setConfiguration(config);

        // Act
        detailsPanel.clearConfiguration();

        // Assert
        assertNull(detailsPanel.getConfiguration());
    }
}