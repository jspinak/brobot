package io.github.jspinak.brobot.runner.automation;

import io.github.jspinak.brobot.datatypes.project.Button;
import io.github.jspinak.brobot.runner.config.BrobotRunnerProperties;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import java.util.function.Consumer;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.contains;
import static org.mockito.Mockito.*;

public class AutomationExecutorTest {

    @Mock
    private BrobotRunnerProperties properties;

    @Mock
    private Consumer<String> logCallback;

    private AutomationExecutor automationExecutor;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
        automationExecutor = new AutomationExecutor(properties);
        automationExecutor.setLogCallback(logCallback);
    }

    @Test
    void testExecuteAutomation() throws InterruptedException {
        // Create a test button
        Button button = new Button();
        button.setFunctionName("testFunction");

        // Execute the automation
        automationExecutor.executeAutomation(button);

        // Verify that logs are called
        verify(logCallback, atLeastOnce()).accept(anyString());

        // Allow execution to start
        Thread.sleep(100);

        // Assert that execution is in progress
        assertFalse(automationExecutor.isStopRequested());

        // Verify that start message is logged
        verify(logCallback).accept(contains("Executing automation function"));
    }

    @Test
    void testStopAllAutomation() {
        // Call stop
        automationExecutor.stopAllAutomation();

        // Verify stopRequested flag is set
        assertTrue(automationExecutor.isStopRequested());

        // Verify that stop message is logged
        verify(logCallback).accept(contains("Stop requested"));
    }

    @Test
    void testLogMethodWithCallback() {
        // Setup a log message
        String testMessage = "Test log message";

        // Call private log method via a public method
        automationExecutor.executeAutomation(createTestButton());

        // Verify callback was called
        verify(logCallback, atLeastOnce()).accept(anyString());
    }

    @Test
    void testLogMethodWithoutCallback() {
        // Remove callback
        automationExecutor.setLogCallback(null);

        // Execute should still work without exceptions
        assertDoesNotThrow(() -> automationExecutor.executeAutomation(createTestButton()));
    }

    private Button createTestButton() {
        Button button = new Button();
        button.setFunctionName("testFunction");
        return button;
    }
}