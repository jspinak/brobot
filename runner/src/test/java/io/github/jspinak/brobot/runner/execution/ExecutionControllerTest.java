package io.github.jspinak.brobot.runner.execution;

import io.github.jspinak.brobot.datatypes.project.Button;
import io.github.jspinak.brobot.runner.resources.ResourceManager;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.Duration;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.Consumer;
import java.util.function.Supplier;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
class ExecutionControllerTest {

    @Mock
    private ResourceManager resourceManager;

    private ExecutionController executionController;
    private List<String> logMessages;

    @BeforeEach
    void setUp() {
        executionController = new ExecutionController(resourceManager);
        logMessages = new ArrayList<>();
        executionController.setLogCallback(logMessages::add);
    }

    @AfterEach
    void tearDown() {
        executionController.shutdown();
    }

    // Add a test to verify ResourceManager interaction
    @Test
    void testResourceManagerRegistration() {
        verify(resourceManager).registerResource(any(ExecutionController.class), anyString());
    }

    @Test
    void testInitialState() {
        assertEquals(ExecutionState.IDLE, executionController.getStatus().getState());
        assertFalse(executionController.isRunning());
        assertFalse(executionController.isPaused());
    }

    @Test
    void testStartExecution() throws Exception {
        // Create a simple task
        Supplier<String> task = () -> {
            try {
                Thread.sleep(100);
                return "Completed";
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                return "Interrupted";
            }
        };

        // Start execution
        Future<String> future = executionController.startExecution(task, Duration.ofSeconds(10), null);

        // Verify execution started
        assertTrue(executionController.isRunning());

        // Wait for completion
        String result = future.get(5, TimeUnit.SECONDS);

        // Verify execution completed successfully
        assertEquals("Completed", result);
        assertEquals(ExecutionState.COMPLETED, executionController.getStatus().getState());
        assertFalse(executionController.isRunning());
    }

    @Test
    void testStartExecutionWithStatusConsumer() throws Exception {
        // Track status updates
        AtomicInteger statusUpdateCount = new AtomicInteger(0);
        Consumer<ExecutionStatus> statusConsumer = status -> statusUpdateCount.incrementAndGet();

        // Create a simple task
        Supplier<Void> task = () -> {
            try {
                Thread.sleep(100);
                return null;
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                return null;
            }
        };

        // Start execution
        Future<Void> future = executionController.startExecution(task, Duration.ofSeconds(10), statusConsumer);

        // Wait for completion
        future.get(5, TimeUnit.SECONDS);

        // Verify status consumer was called
        assertTrue(statusUpdateCount.get() > 0);
    }

    @Test
    void testStopExecution() throws Exception {
        // Create a long-running task
        AtomicBoolean taskCompleted = new AtomicBoolean(false);

        Supplier<Void> task = () -> {
            try {
                for (int i = 0; i < 10; i++) {
                    Thread.sleep(100);
                }
                return null;
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                return null;
            } finally {
                taskCompleted.set(true);
            }
        };

        // Start execution
        Future<Void> future = executionController.startExecution(task, Duration.ofSeconds(10), null);

        // Give it a moment to start
        Thread.sleep(200);

        // Stop execution
        executionController.stopExecution();

        // Wait for completion
        try {
            future.get(5, TimeUnit.SECONDS);
        } catch (ExecutionException e) {
            // Execution may throw an exception when stopped, which is expected
        }

        // Verify execution was stopped
        assertEquals(ExecutionState.STOPPED, executionController.getStatus().getState());
        assertFalse(executionController.isRunning());
    }

    @Test
    void testPauseAndResumeExecution() throws Exception {
        // Create a counter to track task progress
        AtomicInteger counter = new AtomicInteger(0);

        // Create a task that increments counter
        Supplier<Integer> task = () -> {
            try {
                for (int i = 0; i < 5; i++) {
                    // Prüfen, ob pausiert wurde
                    while (executionController.isPaused()) {
                        Thread.sleep(10);
                    }
                    counter.incrementAndGet();
                    Thread.sleep(100);
                }
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            }
            return counter.get();
        };

        // Start execution
        Future<Integer> future = executionController.startExecution(task, Duration.ofSeconds(10), null);

        // Give it a moment to start
        Thread.sleep(150);

        // Pause execution
        executionController.pauseExecution();

        // Verify execution is paused
        assertTrue(executionController.isPaused());
        assertEquals(ExecutionState.PAUSED, executionController.getStatus().getState());

        // Record counter value
        int counterAfterPause = counter.get();

        // Wait a moment to ensure task is truly paused
        Thread.sleep(300);

        // Counter should not have increased
        assertEquals(counterAfterPause, counter.get());

        // Resume execution
        executionController.resumeExecution();

        // Verify execution is resumed
        assertFalse(executionController.isPaused());
        assertEquals(ExecutionState.RUNNING, executionController.getStatus().getState());

        // Wait for completion
        Integer result = future.get(5, TimeUnit.SECONDS);

        // Verify execution completed successfully
        assertEquals(5, result.intValue());
        assertEquals(ExecutionState.COMPLETED, executionController.getStatus().getState());
    }

    @Test
    void testExecutionWithTimeout() throws Exception {
        // Task, das lange läuft
        Supplier<Void> task = () -> {
            try {
                Thread.sleep(3000);
                return null;
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                return null;
            }
        };

        // Start mit kurzem Timeout
        executionController.startExecution(task, Duration.ofMillis(200), null);

        // Warte auf Statuswechsel auf TIMEOUT
        long start = System.currentTimeMillis();
        while (!executionController.getStatus().getState().equals(ExecutionState.TIMEOUT)
                && System.currentTimeMillis() - start < 5000) {
            Thread.sleep(50);
        }

        // Prüfe, ob TIMEOUT erreicht wurde
        assertEquals(ExecutionState.TIMEOUT, executionController.getStatus().getState());
        assertFalse(executionController.isRunning());
    }

    @Test
    void testExecuteAutomation() throws Exception {
        // Create a button for testing
        Button button = new Button();
        button.setFunctionName("TestFunction");
        button.setLabel("Test Button");

        // Track if task was executed
        AtomicBoolean taskExecuted = new AtomicBoolean(false);

        // Create runnable
        Runnable automationTask = () -> {
            try {
                Thread.sleep(100);
                taskExecuted.set(true);
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            }
        };

        // Execute automation
        executionController.executeAutomation(button, automationTask, 5000, null);

        // Wait for execution to complete
        while (executionController.isRunning()) {
            Thread.sleep(100);
        }

        // Verify task was executed
        assertTrue(taskExecuted.get());
        assertEquals(ExecutionState.COMPLETED, executionController.getStatus().getState());
    }

    @Test
    void testConcurrentExecutionPrevented() {
        // Create a long-running task
        Supplier<Void> task1 = () -> {
            try {
                Thread.sleep(1000);
                return null;
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                return null;
            }
        };

        // Start first execution
        executionController.startExecution(task1, Duration.ofSeconds(10), null);

        // Try to start second execution
        assertThrows(IllegalStateException.class, () -> {
            executionController.startExecution(task1, Duration.ofSeconds(10), null);
        });
    }

    @Test
    void testLogCallbackInvoked() throws Exception {
        // Create a simple task
        Supplier<Void> task = () -> {
            try {
                Thread.sleep(100);
                return null;
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                return null;
            }
        };

        // Start execution
        Future<Void> future = executionController.startExecution(task, Duration.ofSeconds(10), null);

        // Wait for completion
        future.get(5, TimeUnit.SECONDS);

        // Verify log messages were recorded
        assertFalse(logMessages.isEmpty());
        // Starting message should be included
        assertTrue(logMessages.stream().anyMatch(msg -> msg.contains("Starting automation execution")));
        // Completion message should be included
        assertTrue(logMessages.stream().anyMatch(msg -> msg.contains("Execution completed successfully")));
    }
}