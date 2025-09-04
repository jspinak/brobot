package io.github.jspinak.brobot.logging.unified;

import io.github.jspinak.brobot.model.state.State;
import io.github.jspinak.brobot.test.BrobotTestBase;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Map;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

import static org.junit.jupiter.api.Assertions.*;

@ExtendWith(MockitoExtension.class)
public class LoggingContextTest extends BrobotTestBase {
    
    private LoggingContext loggingContext;
    
    @BeforeEach
    @Override
    public void setupTest() {
        super.setupTest();
        loggingContext = new LoggingContext();
        // Clear any existing context
        loggingContext.clear();
    }
    
    @AfterEach
    public void tearDown() {
        // Clean up after each test
        loggingContext.clear();
    }
    
    @Test
    public void testSessionIdManagement() {
        // Initially no session
        assertFalse(loggingContext.hasSession());
        assertNull(loggingContext.getSessionId());
        
        // Set session ID
        loggingContext.setSessionId("test-session-123");
        assertTrue(loggingContext.hasSession());
        assertEquals("test-session-123", loggingContext.getSessionId());
        
        // Clear session
        loggingContext.clearSession();
        assertFalse(loggingContext.hasSession());
        assertNull(loggingContext.getSessionId());
    }
    
    @Test
    public void testCurrentStateManagement() {
        // Initially no state
        assertNull(loggingContext.getCurrentState());
        
        // Set current state
        State loginState = new State();
        loginState.setName("LoginScreen");
        loggingContext.setCurrentState(loginState);
        assertEquals("LoginScreen", loggingContext.getCurrentState().getName());
        
        // Update state
        State dashboardState = new State();
        dashboardState.setName("Dashboard");
        loggingContext.setCurrentState(dashboardState);
        assertEquals("Dashboard", loggingContext.getCurrentState().getName());
        
        // Clear context
        loggingContext.clear();
        assertNull(loggingContext.getCurrentState());
    }
    
    @Test
    public void testOperationStack() {
        // Initially empty
        assertNull(loggingContext.getCurrentOperation());
        
        // Push operations
        loggingContext.pushOperation("findImage");
        assertEquals("findImage", loggingContext.getCurrentOperation());
        
        loggingContext.pushOperation("clickButton");
        assertEquals("clickButton", loggingContext.getCurrentOperation());
        
        loggingContext.pushOperation("typeText");
        assertEquals("typeText", loggingContext.getCurrentOperation());
        
        // Pop operations
        loggingContext.popOperation();
        assertEquals("clickButton", loggingContext.getCurrentOperation());
        
        loggingContext.popOperation();
        assertEquals("findImage", loggingContext.getCurrentOperation());
        
        loggingContext.popOperation();
        assertNull(loggingContext.getCurrentOperation());
        
        // Pop from empty stack should not throw
        assertDoesNotThrow(() -> loggingContext.popOperation());
        assertNull(loggingContext.getCurrentOperation());
    }
    
    @Test
    public void testMetadataManagement() {
        // Initially empty
        Map<String, Object> metadata = loggingContext.getAllMetadata();
        assertTrue(metadata.isEmpty());
        
        // Add metadata
        loggingContext.addMetadata("key1", "value1");
        loggingContext.addMetadata("key2", 123);
        loggingContext.addMetadata("key3", true);
        
        metadata = loggingContext.getAllMetadata();
        assertEquals(3, metadata.size());
        assertEquals("value1", metadata.get("key1"));
        assertEquals(123, metadata.get("key2"));
        assertEquals(true, metadata.get("key3"));
        
        // Update existing metadata
        loggingContext.addMetadata("key1", "updated");
        metadata = loggingContext.getAllMetadata();
        assertEquals("updated", metadata.get("key1"));
        
        // Remove metadata
        loggingContext.removeMetadata("key2");
        metadata = loggingContext.getAllMetadata();
        assertEquals(2, metadata.size());
        assertFalse(metadata.containsKey("key2"));
        
        // Clear all metadata
        loggingContext.clear();
        metadata = loggingContext.getAllMetadata();
        assertTrue(metadata.isEmpty());
    }
    
    @Test
    public void testContextSnapshot() {
        // Set up context
        loggingContext.setSessionId("snapshot-session");
        State testState = new State();
        testState.setName("TestState");
        loggingContext.setCurrentState(testState);
        loggingContext.pushOperation("testOp1");
        loggingContext.pushOperation("testOp2");
        loggingContext.addMetadata("meta1", "value1");
        loggingContext.addMetadata("meta2", 42);
        
        // Create snapshot
        LoggingContext.Context snapshot = loggingContext.snapshot();
        
        // Verify snapshot captured the state
        assertNotNull(snapshot);
        // Note: Context doesn't expose getters directly, verify through restore
        
        // Modify context
        loggingContext.clear();
        loggingContext.setSessionId("different-session");
        State differentState = new State();
        differentState.setName("DifferentState");
        loggingContext.setCurrentState(differentState);
        
        // Restore from snapshot
        loggingContext.restore(snapshot);
        
        // Verify restoration
        assertEquals("snapshot-session", loggingContext.getSessionId());
        assertEquals("TestState", loggingContext.getCurrentState().getName());
        assertEquals("testOp2", loggingContext.getCurrentOperation());
        
        Map<String, Object> restoredMetadata = loggingContext.getAllMetadata();
        assertEquals(2, restoredMetadata.size());
        assertEquals("value1", restoredMetadata.get("meta1"));
        assertEquals(42, restoredMetadata.get("meta2"));
    }
    
    @Test
    public void testWithContext() {
        // Set initial context
        loggingContext.setSessionId("original-session");
        State originalState = new State();
        originalState.setName("OriginalState");
        loggingContext.setCurrentState(originalState);
        loggingContext.addMetadata("original", true);
        
        // Create temporary context
        LoggingContext.Context tempContext = loggingContext.snapshot();
        loggingContext.clear();
        loggingContext.setSessionId("temp-session");
        State tempState = new State();
        tempState.setName("TempState");
        loggingContext.setCurrentState(tempState);
        loggingContext.addMetadata("temporary", true);
        LoggingContext.Context modifiedTempContext = loggingContext.snapshot();
        
        // Execute with temporary context
        loggingContext.restore(tempContext); // Restore original first
        final String[] result = {null};
        loggingContext.withContext(modifiedTempContext, () -> {
            // Verify temporary context is active
            assertEquals("temp-session", loggingContext.getSessionId());
            assertEquals("TempState", loggingContext.getCurrentState().getName());
            assertTrue((Boolean) loggingContext.getAllMetadata().get("temporary"));
            
            result[0] = "completed";
        });
        
        // Verify result
        assertEquals("completed", result[0]);
        
        // Verify original context is restored
        assertEquals("original-session", loggingContext.getSessionId());
        assertEquals("OriginalState", loggingContext.getCurrentState().getName());
        assertTrue((Boolean) loggingContext.getAllMetadata().get("original"));
        assertFalse(loggingContext.getAllMetadata().containsKey("temporary"));
    }
    
    @Test
    public void testWithContextException() {
        // Set initial context
        loggingContext.setSessionId("original-session");
        State originalState = new State();
        originalState.setName("OriginalState");
        loggingContext.setCurrentState(originalState);
        
        // Create temporary context
        LoggingContext.Context tempContext = loggingContext.snapshot();
        loggingContext.clear();
        loggingContext.setSessionId("temp-session");
        State tempState = new State();
        tempState.setName("TempState");
        loggingContext.setCurrentState(tempState);
        LoggingContext.Context modifiedTempContext = loggingContext.snapshot();
        
        // Restore original and execute with temporary context that throws exception
        loggingContext.restore(tempContext);
        assertThrows(RuntimeException.class, () -> {
            loggingContext.withContext(modifiedTempContext, () -> {
                throw new RuntimeException("Test exception");
            });
        });
        
        // Verify original context is still restored after exception
        assertEquals("original-session", loggingContext.getSessionId());
        assertEquals("OriginalState", loggingContext.getCurrentState().getName());
    }
    
    @Test
    public void testThreadLocalIsolation() throws InterruptedException {
        int threadCount = 10;
        CountDownLatch startLatch = new CountDownLatch(1);
        CountDownLatch completeLatch = new CountDownLatch(threadCount);
        AtomicInteger successCount = new AtomicInteger(0);
        
        ExecutorService executor = Executors.newFixedThreadPool(threadCount);
        
        try {
            // Submit tasks to different threads
            for (int i = 0; i < threadCount; i++) {
                final int threadId = i;
                executor.submit(() -> {
                    try {
                        // Each thread gets its own LoggingContext instance
                        LoggingContext threadContext = new LoggingContext();
                        
                        // Wait for all threads to be ready
                        startLatch.await();
                        
                        // Set thread-specific context
                        String sessionId = "session-" + threadId;
                        State threadState = new State();
                        threadState.setName("state-" + threadId);
                        
                        threadContext.setSessionId(sessionId);
                        threadContext.setCurrentState(threadState);
                        threadContext.addMetadata("threadId", threadId);
                        
                        // Simulate some work
                        Thread.sleep(10);
                        
                        // Verify thread-local context is preserved
                        if (sessionId.equals(threadContext.getSessionId()) &&
                            ("state-" + threadId).equals(threadContext.getCurrentState().getName()) &&
                            Integer.valueOf(threadId).equals(threadContext.getAllMetadata().get("threadId"))) {
                            successCount.incrementAndGet();
                        }
                        
                    } catch (Exception e) {
                        e.printStackTrace();
                    } finally {
                        completeLatch.countDown();
                    }
                });
            }
            
            // Start all threads simultaneously
            startLatch.countDown();
            
            // Wait for all threads to complete
            assertTrue(completeLatch.await(5, TimeUnit.SECONDS));
            
            // Verify all threads maintained isolated context
            assertEquals(threadCount, successCount.get());
            
        } finally {
            executor.shutdown();
            try {
                if (!executor.awaitTermination(2, TimeUnit.SECONDS)) {
                    executor.shutdownNow();
                }
            } catch (InterruptedException e) {
                executor.shutdownNow();
                Thread.currentThread().interrupt();
            }
        }
    }
    
    @Test
    public void testClearContext() {
        // Set up complex context
        loggingContext.setSessionId("test-session");
        State testState = new State();
        testState.setName("TestState");
        loggingContext.setCurrentState(testState);
        loggingContext.pushOperation("op1");
        loggingContext.pushOperation("op2");
        loggingContext.addMetadata("key1", "value1");
        loggingContext.addMetadata("key2", "value2");
        
        // Clear everything
        loggingContext.clear();
        
        // Verify everything is cleared
        assertNull(loggingContext.getSessionId());
        assertNull(loggingContext.getCurrentState());
        assertNull(loggingContext.getCurrentOperation());
        assertTrue(loggingContext.getAllMetadata().isEmpty());
        assertFalse(loggingContext.hasSession());
    }
    
    @Test
    public void testClearSession() {
        // Set up context with session
        loggingContext.setSessionId("test-session");
        State testState = new State();
        testState.setName("TestState");
        loggingContext.setCurrentState(testState);
        loggingContext.pushOperation("operation");
        loggingContext.addMetadata("key", "value");
        
        // Clear only session
        loggingContext.clearSession();
        
        // Verify only session is cleared and operations cleared
        assertNull(loggingContext.getSessionId());
        assertFalse(loggingContext.hasSession());
        assertNull(loggingContext.getCurrentOperation()); // Operations are cleared too
        
        // Other context should still be present
        assertEquals("TestState", loggingContext.getCurrentState().getName());
        assertEquals("value", loggingContext.getAllMetadata().get("key"));
    }
    
    @Test
    public void testNullHandling() {
        // Test null session ID
        assertDoesNotThrow(() -> loggingContext.setSessionId(null));
        assertNull(loggingContext.getSessionId());
        assertFalse(loggingContext.hasSession());
        
        // Test null state
        assertDoesNotThrow(() -> loggingContext.setCurrentState(null));
        assertNull(loggingContext.getCurrentState());
        
        // Note: ArrayDeque.push() doesn't accept null values, so we skip testing null operations
        // This is expected behavior - operations should not be null
        
        // ConcurrentHashMap doesn't allow null keys or values
        // Test that null key throws NPE (expected behavior)
        assertThrows(NullPointerException.class, () -> loggingContext.addMetadata(null, "value"));
        
        // Test that null value also throws NPE for ConcurrentHashMap (expected behavior)
        assertThrows(NullPointerException.class, () -> loggingContext.addMetadata("key", null));
        
        // Test removing null key - will throw NPE (expected behavior for ConcurrentHashMap)
        assertThrows(NullPointerException.class, () -> loggingContext.removeMetadata(null));
    }
    
    @Test
    public void testOperationStackDepth() {
        // Push many operations to test stack depth
        int depth = 100;
        for (int i = 0; i < depth; i++) {
            loggingContext.pushOperation("operation-" + i);
        }
        
        // Verify current operation is the last pushed
        assertEquals("operation-" + (depth - 1), loggingContext.getCurrentOperation());
        
        // Pop all operations
        for (int i = depth - 1; i >= 0; i--) {
            assertEquals("operation-" + i, loggingContext.getCurrentOperation());
            loggingContext.popOperation();
        }
        
        // Stack should be empty
        assertNull(loggingContext.getCurrentOperation());
    }
    
    @Test
    public void testMetadataIsolation() {
        // Add metadata with mutable object
        Map<String, String> mutableMap = new java.util.HashMap<>();
        mutableMap.put("inner", "value");
        loggingContext.addMetadata("map", mutableMap);
        
        // Get metadata and modify the original
        Map<String, Object> metadata1 = loggingContext.getAllMetadata();
        mutableMap.put("inner", "modified");
        mutableMap.put("new", "added");
        
        // Get metadata again
        Map<String, Object> metadata2 = loggingContext.getAllMetadata();
        
        // Original modification should affect the stored value
        // (shallow copy behavior)
        Map<String, String> storedMap = (Map<String, String>) metadata2.get("map");
        assertEquals("modified", storedMap.get("inner"));
        assertEquals("added", storedMap.get("new"));
    }
}