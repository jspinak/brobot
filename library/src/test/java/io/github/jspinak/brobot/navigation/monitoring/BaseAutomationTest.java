package io.github.jspinak.brobot.navigation.monitoring;

import io.github.jspinak.brobot.control.ExecutionController;
import io.github.jspinak.brobot.control.ExecutionState;
import io.github.jspinak.brobot.control.ExecutionStoppedException;
import io.github.jspinak.brobot.control.ThreadSafeExecutionController;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class BaseAutomationTest {

    @Mock
    private StateHandler stateHandler;
    
    @Mock
    private ExecutionController mockExecutionController;
    
    private TestableBaseAutomation automation;
    private TestableBaseAutomation automationWithMockController;
    
    // Concrete implementation for testing
    private static class TestableBaseAutomation extends BaseAutomation {
        TestableBaseAutomation(StateHandler stateHandler) {
            super(stateHandler);
        }
        
        TestableBaseAutomation(StateHandler stateHandler, ExecutionController executionController) {
            super(stateHandler, executionController);
        }
    }
    
    @BeforeEach
    void setUp() {
        automation = new TestableBaseAutomation(stateHandler);
        automationWithMockController = new TestableBaseAutomation(stateHandler, mockExecutionController);
    }
    
    @Test
    void testConstructorWithStateHandler() {
        assertNotNull(automation.stateHandler);
        assertEquals(stateHandler, automation.stateHandler);
        assertNotNull(automation.executionController);
        assertTrue(automation.executionController instanceof ThreadSafeExecutionController);
    }
    
    @Test
    void testConstructorWithExecutionController() {
        assertNotNull(automationWithMockController.stateHandler);
        assertEquals(stateHandler, automationWithMockController.stateHandler);
        assertNotNull(automationWithMockController.executionController);
        assertEquals(mockExecutionController, automationWithMockController.executionController);
    }
    
    @Test
    void testStart() {
        automationWithMockController.start();
        
        verify(mockExecutionController).start();
    }
    
    @Test
    void testPause() {
        automationWithMockController.pause();
        
        verify(mockExecutionController).pause();
    }
    
    @Test
    void testResume() {
        automationWithMockController.resume();
        
        verify(mockExecutionController).resume();
    }
    
    @Test
    void testStop() {
        automationWithMockController.stop();
        
        verify(mockExecutionController).stop();
    }
    
    @Test
    void testIsRunning() {
        when(mockExecutionController.isRunning()).thenReturn(true);
        
        assertTrue(automationWithMockController.isRunning());
        verify(mockExecutionController).isRunning();
    }
    
    @Test
    void testIsPaused() {
        when(mockExecutionController.isPaused()).thenReturn(true);
        
        assertTrue(automationWithMockController.isPaused());
        verify(mockExecutionController).isPaused();
    }
    
    @Test
    void testIsStopped() {
        when(mockExecutionController.isStopped()).thenReturn(true);
        
        assertTrue(automationWithMockController.isStopped());
        verify(mockExecutionController).isStopped();
    }
    
    @Test
    void testGetState() {
        when(mockExecutionController.getState()).thenReturn(ExecutionState.PAUSED);
        
        assertEquals(ExecutionState.PAUSED, automationWithMockController.getState());
        verify(mockExecutionController).getState();
    }
    
    @Test
    void testCheckPausePoint() throws Exception {
        automationWithMockController.checkPausePoint();
        
        verify(mockExecutionController).checkPausePoint();
    }
    
    @Test
    void testCheckPausePointThrowsExecutionStoppedException() throws Exception {
        doThrow(new ExecutionStoppedException("Stopped"))
                .when(mockExecutionController).checkPausePoint();
        
        assertThrows(ExecutionStoppedException.class, 
                () -> automationWithMockController.checkPausePoint());
    }
    
    @Test
    void testCheckPausePointThrowsInterruptedException() throws Exception {
        doThrow(new InterruptedException("Interrupted"))
                .when(mockExecutionController).checkPausePoint();
        
        assertThrows(InterruptedException.class, 
                () -> automationWithMockController.checkPausePoint());
    }
    
    @Test
    void testReset() {
        automationWithMockController.reset();
        
        verify(mockExecutionController).reset();
    }
    
    @Test
    void testExecutionStateTransitions() throws Exception {
        // Test that execution state transitions work correctly
        assertFalse(automation.isRunning());
        
        automation.start();
        assertTrue(automation.isRunning());
        
        automation.stop();
        Thread.sleep(100); // Give time for state transition
        assertFalse(automation.isRunning());
        
        automation.reset(); // Reset to IDLE before starting again
        automation.start();
        assertTrue(automation.isRunning());
        
        automation.reset();
        assertFalse(automation.isRunning());
    }
    
    @Test
    void testFullLifecycle() {
        // Using real controller to test full lifecycle
        assertEquals(ExecutionState.IDLE, automation.getState());
        assertFalse(automation.isRunning());
        
        automation.start();
        assertEquals(ExecutionState.RUNNING, automation.getState());
        assertTrue(automation.isRunning());
        
        automation.pause();
        assertEquals(ExecutionState.PAUSED, automation.getState());
        assertTrue(automation.isPaused());
        assertFalse(automation.isRunning());
        
        automation.resume();
        assertEquals(ExecutionState.RUNNING, automation.getState());
        assertTrue(automation.isRunning());
        
        automation.stop();
        assertTrue(automation.isStopped());
        
        automation.reset();
        assertEquals(ExecutionState.IDLE, automation.getState());
    }
    
    @Test
    void testConcurrentOperations() throws Exception {
        // Test thread safety with real controller
        Thread t1 = new Thread(() -> {
            for (int i = 0; i < 100; i++) {
                automation.start();
                automation.pause();
                automation.resume();
                automation.stop();
                automation.reset();
            }
        });
        
        Thread t2 = new Thread(() -> {
            for (int i = 0; i < 100; i++) {
                try {
                    automation.isRunning();
                    automation.isPaused();
                    automation.getState();
                } catch (Exception e) {
                    fail("Exception during concurrent read: " + e);
                }
            }
        });
        
        t1.start();
        t2.start();
        
        t1.join(5000);
        t2.join(5000);
        
        // Should end in a valid state
        assertNotNull(automation.getState());
    }
}