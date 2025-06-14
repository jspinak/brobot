package io.github.jspinak.brobot.runner.automation;

import io.github.jspinak.brobot.datatypes.project.Button;
import io.github.jspinak.brobot.runner.config.BrobotRunnerProperties;
import io.github.jspinak.brobot.runner.events.EventBus;
import io.github.jspinak.brobot.runner.events.ExecutionEventPublisher;
import io.github.jspinak.brobot.runner.execution.ExecutionController;
import io.github.jspinak.brobot.runner.execution.ExecutionState;
import io.github.jspinak.brobot.runner.execution.ExecutionStatus;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class AutomationExecutorTest {

    @Mock
    private BrobotRunnerProperties properties;

    @Mock
    private ExecutionController executionController;

    @Mock
    private EventBus eventBus;

    @Mock
    private ExecutionEventPublisher executionEventPublisher;

    private AutomationExecutor automationExecutor;
    private List<String> logMessages;

    @BeforeEach
    void setUp() {
        // Set up the log callback
        logMessages = new ArrayList<>();

        // Set up the controller mock to capture log callback
        doAnswer(invocation -> {
            Consumer<String> logCallback = invocation.getArgument(0);
            logCallback.accept("Test log message");
            return null;
        }).when(executionController).setLogCallback(any());

        // Create the executor with mocks
        automationExecutor = new AutomationExecutor(properties, executionController, eventBus, executionEventPublisher);
        automationExecutor.setLogCallback(logMessages::add);
    }

    @Test
    void testExecuteAutomation() {
        // Create a button for testing
        Button button = new Button();
        button.setFunctionName("TestFunction");
        button.setLabel("Test Button");

        // Capture the runnable passed to the execution controller
        ArgumentCaptor<Runnable> runnableCaptor = ArgumentCaptor.forClass(Runnable.class);

        // Execute automation
        automationExecutor.executeAutomation(button);

        // Verify execution controller was called
        verify(executionController).executeAutomation(
                eq(button),
                runnableCaptor.capture(),
                anyLong(),
                any()
        );

        // The logging happens here
        ExecutionStatus mockStatus = new ExecutionStatus();
        mockStatus.setState(ExecutionState.RUNNING);
        when(executionController.getStatus()).thenReturn(mockStatus);

        Runnable capturedRunnable = runnableCaptor.getValue();
        capturedRunnable.run();

        // Verify log messages
        assertTrue(logMessages.stream().anyMatch(msg -> msg.contains("Execution step")));
    }

    @Test
    void testStopAllAutomation() {
        // Stop automation
        automationExecutor.stopAllAutomation();

        // Verify execution controller was called
        verify(executionController).stopExecution();

        // Verify log message
        assertTrue(logMessages.stream().anyMatch(msg -> msg.contains("Stop requested for all automation")));
    }

    @Test
    void testPauseAutomation() {
        // Pause automation
        automationExecutor.pauseAutomation();

        // Verify execution controller was called
        verify(executionController).pauseExecution();

        // Verify log message
        assertTrue(logMessages.stream().anyMatch(msg -> msg.contains("Pause requested for automation")));
    }

    @Test
    void testResumeAutomation() {
        // Resume automation
        automationExecutor.resumeAutomation();

        // Verify execution controller was called
        verify(executionController).resumeExecution();

        // Verify log message
        assertTrue(logMessages.stream().anyMatch(msg -> msg.contains("Resume requested for automation")));
    }

    @Test
    void testGetExecutionStatus() {
        // Create mock status
        ExecutionStatus mockStatus = new ExecutionStatus();
        mockStatus.setState(ExecutionState.RUNNING);

        // Configure mock
        when(executionController.getStatus()).thenReturn(mockStatus);

        // Get status
        ExecutionStatus status = automationExecutor.getExecutionStatus();

        // Verify correct status was returned
        assertEquals(ExecutionState.RUNNING, status.getState());
    }

    @Test
    void testStatusUpdateCallback() {
        // Create status consumer captor
        ArgumentCaptor<Consumer<ExecutionStatus>> consumerCaptor = ArgumentCaptor.forClass(Consumer.class);

        // **FIX**: Stub the getStatusConsumer() method to return a non-null consumer
        when(executionEventPublisher.getStatusConsumer()).thenReturn(status -> {});

        // Create a button for testing
        Button button = new Button();
        button.setFunctionName("TestFunction");

        // Execute automation
        automationExecutor.executeAutomation(button);

        // Verify the status consumer was passed
        verify(executionController).executeAutomation(
                any(Button.class),
                any(Runnable.class),
                anyLong(),
                consumerCaptor.capture()
        );

        // Get the captured consumer and assert it's not null
        Consumer<ExecutionStatus> statusConsumer = consumerCaptor.getValue();
        assertNotNull(statusConsumer, "The captured status consumer should not be null.");

        // Test the consumer with a status update
        ExecutionStatus testStatus = new ExecutionStatus();
        testStatus.setState(ExecutionState.RUNNING);
        statusConsumer.accept(testStatus);

        // No exceptions should have been thrown
    }
}