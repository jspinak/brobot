package io.github.jspinak.brobot.runner.config;

import lombok.Data;

import io.github.jspinak.brobot.runner.events.EventBus;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.api.io.TempDir;
import org.mockito.Mock;
import org.mockito.Spy;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;

import java.io.FileInputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Properties;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@Data
public class ApplicationConfigTest {

    private ApplicationConfig applicationConfig;

    @Spy
    private BrobotRunnerProperties properties;

    @Mock
    private EventBus eventBus;

    @TempDir
    Path tempDir;

    @BeforeEach
    public void setUp() {
        // Initialize properties with test values
        properties = spy(new BrobotRunnerProperties());

        // Create the ApplicationConfig directly with mocked dependencies
        applicationConfig = new ApplicationConfig(eventBus, properties);
    }

    @AfterEach
    public void tearDown() throws Exception {
        // Use reflection to access stopAutosave
        ReflectionTestUtils.invokeMethod(applicationConfig, "stopAutosave");

        // Delete config files to prevent interference between tests
        Path configPath = tempDir.resolve("app.properties");
        Files.deleteIfExists(configPath);
    }

    @Test
    public void testInitialize() throws Exception {
        // Override the config path for this test
        when(properties.getConfigPath()).thenReturn(tempDir.toString());

        // Set the config file path via reflection
        ReflectionTestUtils.setField(applicationConfig, "configFilePath", tempDir.resolve("app.properties"));

        // Call initialize directly
        ReflectionTestUtils.invokeMethod(applicationConfig, "initialize");

        // Verify directories were created
        Path configFilePath = tempDir.resolve("app.properties");
        assertTrue(Files.exists(configFilePath.getParent()));
    }

    @Test
    public void testLoadDefaults() throws Exception {
        // Override the config path for this test
        when(properties.getConfigPath()).thenReturn(tempDir.toString());

        // Set the config file path via reflection
        ReflectionTestUtils.setField(applicationConfig, "configFilePath", tempDir.resolve("app.properties"));

        // Call initialize directly
        ReflectionTestUtils.invokeMethod(applicationConfig, "initialize");

        // Get access to appProperties field
        Properties appProperties = (Properties) ReflectionTestUtils.getField(applicationConfig, "appProperties");

        // Verify default values were set
        assertNotNull(appProperties);
        assertEquals("1.0.0", appProperties.getProperty("app.version"));
        assertEquals("light", appProperties.getProperty("ui.theme"));
        assertEquals("1.0", appProperties.getProperty("ui.fontScale"));
        assertEquals("false", appProperties.getProperty("automation.autoStart"));
    }

    @Test
    public void testSaveAndLoadConfiguration() throws Exception {
        // Override the config path for this test
        when(properties.getConfigPath()).thenReturn(tempDir.toString());

        // Set the config file path via reflection
        ReflectionTestUtils.setField(applicationConfig, "configFilePath", tempDir.resolve("app.properties"));

        // Call initialize directly
        ReflectionTestUtils.invokeMethod(applicationConfig, "initialize");

        // Set some properties
        applicationConfig.setString("test.key", "test value");
        applicationConfig.setInt("test.int", 42);
        applicationConfig.setBoolean("test.bool", true);

        // Save the configuration
        applicationConfig.saveConfiguration();

        // Create a new properties object and load the saved config
        Properties props = new Properties();
        Path configFilePath = tempDir.resolve("app.properties");
        try (FileInputStream fis = new FileInputStream(configFilePath.toFile())) {
            props.load(fis);
        }

        // Verify properties were saved correctly
        assertEquals("test value", props.getProperty("test.key"));
        assertEquals("42", props.getProperty("test.int"));
        assertEquals("true", props.getProperty("test.bool"));

        // Create a new ApplicationConfig instance with the same test path
        ApplicationConfig newConfig = new ApplicationConfig(mock(EventBus.class), properties);
        ReflectionTestUtils.setField(newConfig, "configFilePath", tempDir.resolve("app.properties"));

        // Load the configuration
        ReflectionTestUtils.invokeMethod(newConfig, "loadConfiguration");

        // Get appProperties field from the new instance
        Properties loadedProps = (Properties) ReflectionTestUtils.getField(newConfig, "appProperties");

        // Verify properties were loaded
        assertNotNull(loadedProps);
        assertEquals("test value", loadedProps.getProperty("test.key"));
        assertEquals("42", loadedProps.getProperty("test.int"));
        assertEquals("true", loadedProps.getProperty("test.bool"));
    }

    @Test
    public void testStringProperties() throws Exception {
        // Override the config path for this test
        when(properties.getConfigPath()).thenReturn(tempDir.toString());

        // Set the config file path via reflection
        ReflectionTestUtils.setField(applicationConfig, "configFilePath", tempDir.resolve("app.properties"));

        // Call initialize directly
        ReflectionTestUtils.invokeMethod(applicationConfig, "initialize");

        // Test default value
        assertEquals("default", applicationConfig.getString("nonexistent.key", "default"));

        // Test setting and getting a value
        applicationConfig.setString("test.key", "test value");
        assertEquals("test value", applicationConfig.getString("test.key", "default"));
    }

    @Test
    public void testBooleanProperties() throws Exception {
        // Override the config path for this test
        when(properties.getConfigPath()).thenReturn(tempDir.toString());

        // Set the config file path via reflection
        ReflectionTestUtils.setField(applicationConfig, "configFilePath", tempDir.resolve("app.properties"));

        // Call initialize directly
        ReflectionTestUtils.invokeMethod(applicationConfig, "initialize");

        // Test default value
        assertTrue(applicationConfig.getBoolean("nonexistent.key", true));
        assertFalse(applicationConfig.getBoolean("nonexistent.key", false));

        // Test setting and getting a value
        applicationConfig.setBoolean("test.bool", true);
        assertTrue(applicationConfig.getBoolean("test.bool", false));

        applicationConfig.setBoolean("test.bool", false);
        assertFalse(applicationConfig.getBoolean("test.bool", true));
    }

    @Test
    public void testShutdown() throws Exception {
        // Override the config path for this test
        when(properties.getConfigPath()).thenReturn(tempDir.toString());

        // Set the config file path via reflection
        ReflectionTestUtils.setField(applicationConfig, "configFilePath", tempDir.resolve("app.properties"));

        // Call initialize directly
        ReflectionTestUtils.invokeMethod(applicationConfig, "initialize");

        // Set some properties
        applicationConfig.setString("test.key", "test value");

        // Create a spy to verify stopAutosave is called
        ApplicationConfig spy = spy(applicationConfig);
        doNothing().when(spy).stopAutosave();

        // Call shutdown
        spy.shutdown();

        // Verify properties were saved
        Path configFilePath = tempDir.resolve("app.properties");
        assertTrue(Files.exists(configFilePath));

        // Verify stopAutosave was called
        verify(spy).stopAutosave();
    }
}