package io.github.jspinak.brobot.runner.execution;

import lombok.Data;

import io.github.jspinak.brobot.control.ExecutionController;
import io.github.jspinak.brobot.control.ExecutionState;
import io.github.jspinak.brobot.control.ExecutionStoppedException;
import io.github.jspinak.brobot.navigation.monitoring.AutomationScript;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.Timeout;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.Consumer;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@Data
class AutomationExecutorTest {

    @Mock
    private ExecutionController executionController;
    
    @Mock
    private AutomationScript automationScript;
    
    @Mock
    private Consumer<ExecutionState> stateChangeListener;
    
    private AutomationExecutor automationExecutor;
    
    @BeforeEach
    void setUp() {
        automationExecutor = new AutomationExecutor(executionController);
        automationExecutor.setStateChangeListener(stateChangeListener);
    }
    
    @Test
    void testStartExecutionWithRunnable() throws Exception {
        AtomicBoolean taskRan = new AtomicBoolean(false);
        CountDownLatch taskCompleted = new CountDownLatch(1);
        
        Runnable task = () -> {
            taskRan.set(true);
            taskCompleted.countDown();
        };
        
        automationExecutor.startExecution(task);
        
        assertTrue(taskCompleted.await(2, TimeUnit.SECONDS));
        assertTrue(taskRan.get());
        
        verify(executionController).reset();
        verify(executionController).start();
        verify(stateChangeListener).accept(ExecutionState.RUNNING);
        verify(stateChangeListener).accept(ExecutionState.IDLE);
    }
    
    @Test
    void testStartExecutionWithAutomationScript() throws Exception {
        when(automationScript.isRunning()).thenReturn(true, true, false);
        
        automationExecutor.startExecution(automationScript);
        
        Thread.sleep(500); // Give time for execution
        
        verify(executionController).reset();
        verify(executionController).start();
        verify(automationScript).start();
        verify(stateChangeListener).accept(ExecutionState.RUNNING);
    }
    
    @Test
    void testStartExecutionWhileRunning() {
        // Start first execution
        CountDownLatch blockingLatch = new CountDownLatch(1);
        automationExecutor.startExecution(() -> {
            try {
                blockingLatch.await();
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            }
        });
        
        // Try to start another
        assertThrows(IllegalStateException.class, () -> 
            automationExecutor.startExecution(() -> {})
        );
        
        blockingLatch.countDown(); // Let first execution finish
    }
    
    @Test
    void testPause() {
        automationExecutor.pause();
        
        verify(executionController).pause();
        verify(stateChangeListener).accept(ExecutionState.PAUSED);
    }
    
    @Test
    void testResume() {
        automationExecutor.resume();
        
        verify(executionController).resume();
        verify(stateChangeListener).accept(ExecutionState.RUNNING);
    }
    
    @Test
    @Timeout(5)
    void testStopWithoutWaiting() {
        CountDownLatch taskStarted = new CountDownLatch(1);
        CountDownLatch stopSignal = new CountDownLatch(1);
        
        automationExecutor.startExecution(() -> {
            taskStarted.countDown();
            try {
                stopSignal.await();
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            }
        });
        
        try {
            taskStarted.await();
            automationExecutor.stop();
            
            verify(executionController).stop();
            verify(stateChangeListener).accept(ExecutionState.STOPPING);
            
        } finally {
            stopSignal.countDown();
        }
    }
    
    @Test
    @Timeout(5)
    void testStopWithWaiting() throws Exception {
        AtomicBoolean taskCompleted = new AtomicBoolean(false);
        
        automationExecutor.startExecution(() -> {
            try {
                Thread.sleep(100);
                taskCompleted.set(true);
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            }
        });
        
        Thread.sleep(50); // Let task start
        boolean stopped = automationExecutor.stop(true, 2);
        
        assertTrue(stopped);
        assertTrue(taskCompleted.get());
        verify(executionController).stop();
        verify(stateChangeListener).accept(ExecutionState.STOPPED);
    }
    
    @Test
    void testStopWithTimeout() throws Exception {
        CountDownLatch blockingLatch = new CountDownLatch(1);
        
        automationExecutor.startExecution(() -> {
            try {
                blockingLatch.await(); // Block forever
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            }
        });
        
        Thread.sleep(100); // Let task start
        boolean stopped = automationExecutor.stop(true, 1);
        
        assertFalse(stopped); // Should timeout
        verify(executionController).stop();
        
        blockingLatch.countDown(); // Clean up
    }
    
    @Test
    void testStopWithScript() throws Exception {
        when(automationScript.isRunning()).thenReturn(true, false);
        
        automationExecutor.startExecution(automationScript);
        Thread.sleep(100);
        
        automationExecutor.stop();
        
        verify(automationScript).stop();
        verify(executionController).stop();
    }
    
    @Test
    void testExecutionStoppedException() throws Exception {
        CountDownLatch exceptionThrown = new CountDownLatch(1);
        
        automationExecutor.startExecution(() -> {
            throw new ExecutionStoppedException("Test stop");
        });
        
        Thread.sleep(200); // Wait for execution
        
        verify(stateChangeListener).accept(ExecutionState.RUNNING);
        verify(stateChangeListener).accept(ExecutionState.IDLE);
        verify(executionController, times(2)).reset(); // Once at start, once at end
    }
    
    @Test
    void testGeneralException() throws Exception {
        automationExecutor.startExecution(() -> {
            throw new RuntimeException("Test error");
        });
        
        Thread.sleep(200); // Wait for execution
        
        verify(stateChangeListener).accept(ExecutionState.RUNNING);
        verify(stateChangeListener).accept(ExecutionState.IDLE);
        verify(executionController, times(2)).reset();
    }
    
    @Test
    void testInterruptedException() throws Exception {
        CountDownLatch taskStarted = new CountDownLatch(1);
        Thread executionThread = null;
        
        automationExecutor.startExecution(() -> {
            taskStarted.countDown();
            try {
                Thread.sleep(10000); // Long sleep
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            }
        });
        
        taskStarted.await();
        Thread.sleep(100);
        
        // Stop forcefully which should interrupt
        automationExecutor.stop(true, 1);
        
        verify(executionController).stop();
    }
    
    @Test
    void testGetState() {
        when(executionController.getState()).thenReturn(ExecutionState.PAUSED);
        
        assertEquals(ExecutionState.PAUSED, automationExecutor.getState());
        verify(executionController).getState();
    }
    
    @Test
    void testIsPaused() {
        when(executionController.isPaused()).thenReturn(true);
        
        assertTrue(automationExecutor.isPaused());
        verify(executionController).isPaused();
    }
    
    @Test
    void testIsRunning() {
        when(executionController.isRunning()).thenReturn(true);
        
        assertTrue(automationExecutor.isRunning());
        verify(executionController).isRunning();
    }
    
    @Test
    void testIsExecutionInProgress() throws Exception {
        assertFalse(automationExecutor.isExecutionInProgress());
        
        CountDownLatch taskRunning = new CountDownLatch(1);
        CountDownLatch continueTask = new CountDownLatch(1);
        
        automationExecutor.startExecution(() -> {
            taskRunning.countDown();
            try {
                continueTask.await();
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            }
        });
        
        taskRunning.await();
        assertTrue(automationExecutor.isExecutionInProgress());
        
        continueTask.countDown();
        Thread.sleep(100);
        assertFalse(automationExecutor.isExecutionInProgress());
    }
    
    @Test
    void testStateChangeListenerException() throws Exception {
        // Make listener throw exception
        doThrow(new RuntimeException("Listener error"))
            .when(stateChangeListener).accept(any());
        
        // Should not affect execution
        automationExecutor.startExecution(() -> {});
        
        Thread.sleep(100);
        
        verify(executionController).start();
        verify(executionController, times(2)).reset();
    }
    
    @Test
    void testShutdown() throws Exception {
        // Start a task
        CountDownLatch taskRunning = new CountDownLatch(1);
        automationExecutor.startExecution(() -> {
            taskRunning.countDown();
            try {
                Thread.sleep(1000);
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            }
        });
        
        taskRunning.await();
        
        // Shutdown should stop execution
        automationExecutor.shutdown();
        
        verify(executionController).stop();
        assertFalse(automationExecutor.isExecutionInProgress());
    }
    
    @Test
    void testConcurrentOperations() throws Exception {
        int numOperations = 10;
        CountDownLatch allStarted = new CountDownLatch(1);
        CountDownLatch allDone = new CountDownLatch(numOperations);
        AtomicInteger successCount = new AtomicInteger(0);
        
        // Start execution
        automationExecutor.startExecution(() -> {
            try {
                Thread.sleep(500);
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            }
        });
        
        // Perform concurrent operations
        for (int i = 0; i < numOperations; i++) {
            new Thread(() -> {
                try {
                    allStarted.await();
                    
                    // Mix of operations
                    automationExecutor.pause();
                    Thread.sleep(10);
                    automationExecutor.resume();
                    automationExecutor.getState();
                    automationExecutor.isPaused();
                    automationExecutor.isRunning();
                    
                    successCount.incrementAndGet();
                } catch (Exception e) {
                    e.printStackTrace();
                } finally {
                    allDone.countDown();
                }
            }).start();
        }
        
        allStarted.countDown();
        assertTrue(allDone.await(5, TimeUnit.SECONDS));
        assertEquals(numOperations, successCount.get());
        
        automationExecutor.stop();
    }
}