package io.github.jspinak.brobot.runner.events;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class ConfigurationEventTest {

    @Test
    void constructor_ShouldSetAllProperties() {
        // Arrange
        Object source = new Object();
        String configName = "test-config";
        String details = "Test configuration details";
        Exception error = new RuntimeException("Test exception");

        // Act
        ConfigurationEvent event = new ConfigurationEvent(
                BrobotEvent.EventType.CONFIG_LOADED, source, configName, details, error);

        // Assert
        assertEquals(BrobotEvent.EventType.CONFIG_LOADED, event.getEventType());
        assertSame(source, event.getSource());
        assertEquals(configName, event.getConfigName());
        assertEquals(details, event.getDetails());
        assertSame(error, event.getError());
    }

    @Test
    void factoryMethod_Loaded_ShouldCreateCorrectEvent() {
        // Arrange
        Object source = new Object();
        String configName = "test-config";
        String details = "Configuration successfully loaded";

        // Act
        ConfigurationEvent event = ConfigurationEvent.loaded(source, configName, details);

        // Assert
        assertEquals(BrobotEvent.EventType.CONFIG_LOADED, event.getEventType());
        assertSame(source, event.getSource());
        assertEquals(configName, event.getConfigName());
        assertEquals(details, event.getDetails());
        assertNull(event.getError());
    }

    @Test
    void factoryMethod_LoadingFailed_ShouldCreateCorrectEvent() {
        // Arrange
        Object source = new Object();
        String configName = "test-config";
        String details = "Configuration loading failed";
        Exception error = new RuntimeException("Test exception");

        // Act
        ConfigurationEvent event = ConfigurationEvent.loadingFailed(source, configName, details, error);

        // Assert
        assertEquals(BrobotEvent.EventType.CONFIG_LOADING_FAILED, event.getEventType());
        assertSame(source, event.getSource());
        assertEquals(configName, event.getConfigName());
        assertEquals(details, event.getDetails());
        assertSame(error, event.getError());
    }
}