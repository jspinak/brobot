package io.github.jspinak.brobot.runner.testutils;

import io.github.jspinak.brobot.runner.ui.config.ConfigEntry;
import java.lang.reflect.Field;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.LocalDateTime;

/**
 * Helper class for test utilities.
 */
public class TestHelper {
    
    /**
     * Injects a value into a private field of an object using reflection.
     * 
     * @param target The object to inject into
     * @param fieldName The name of the field
     * @param value The value to inject
     */
    public static void injectField(Object target, String fieldName, Object value) {
        try {
            Field field = target.getClass().getDeclaredField(fieldName);
            field.setAccessible(true);
            field.set(target, value);
        } catch (NoSuchFieldException | IllegalAccessException e) {
            throw new RuntimeException("Failed to inject field: " + fieldName, e);
        }
    }
    
    /**
     * Creates an instance of ExampleLabelManagedPanel with injected dependencies.
     */
    public static io.github.jspinak.brobot.runner.ui.panels.ExampleLabelManagedPanel createExamplePanel(
            io.github.jspinak.brobot.runner.ui.management.LabelManager labelManager,
            io.github.jspinak.brobot.runner.ui.management.UIUpdateManager uiUpdateManager) {
        
        var panel = new io.github.jspinak.brobot.runner.ui.panels.ExampleLabelManagedPanel();
        injectField(panel, "labelManager", labelManager);
        injectField(panel, "updateManager", uiUpdateManager);
        return panel;
    }
    
    /**
     * Creates a test ConfigEntry with default values.
     */
    public static ConfigEntry createTestConfigEntry() {
        return new ConfigEntry(
            "TestConfig",
            "TestProject", 
            Paths.get("/test/project/config.properties"),
            Paths.get("/test/dsl/config.json"),
            Paths.get("/test/images"),
            LocalDateTime.now()
        );
    }
    
    /**
     * Creates a test ConfigEntry with custom values.
     */
    public static ConfigEntry createTestConfigEntry(String name, String project) {
        ConfigEntry entry = new ConfigEntry(
            name,
            project,
            Paths.get("/test/" + project + "/config.properties"),
            Paths.get("/test/" + project + "/dsl.json"),
            Paths.get("/test/" + project + "/images"),
            LocalDateTime.now()
        );
        entry.setDescription("Test configuration for " + name);
        entry.setAuthor("Test Author");
        entry.setVersion("1.0.0");
        return entry;
    }
    
    /**
     * Creates a test ExecutionStatus with common values.
     */
    public static io.github.jspinak.brobot.runner.execution.ExecutionStatus createTestExecutionStatus(
            io.github.jspinak.brobot.runner.execution.ExecutionState state,
            String currentOperation,
            double progress,
            String currentState) {
        var status = new io.github.jspinak.brobot.runner.execution.ExecutionStatus();
        status.setState(state);
        status.setCurrentOperation(currentOperation);
        status.setProgress(progress);
        status.setCurrentState(currentState);
        status.setStartTime(java.time.Instant.now());
        return status;
    }
}