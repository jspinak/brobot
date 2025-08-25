package io.github.jspinak.brobot.logging.unified;

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
    
    @BeforeEach
    @Override
    public void setupTest() {
        super.setupTest();
        // Clear any existing context
        LoggingContext.clear();
    }
    
    @AfterEach
    public void tearDown() {
        // Clean up after each test
        LoggingContext.clear();
    }
    
    @Test
    public void testSessionIdManagement() {
        // Initially no session
        assertFalse(LoggingContext.hasSession());
        assertNull(LoggingContext.getSessionId());
        
        // Set session ID
        LoggingContext.setSessionId("test-session-123");
        assertTrue(LoggingContext.hasSession());
        assertEquals("test-session-123", LoggingContext.getSessionId());
        
        // Clear session
        LoggingContext.clearSession();
        assertFalse(LoggingContext.hasSession());
        assertNull(LoggingContext.getSessionId());
    }
    
    @Test
    public void testCurrentStateManagement() {
        // Initially no state
        assertNull(LoggingContext.getCurrentState());
        
        // Set current state
        LoggingContext.setCurrentState("LoginScreen");
        assertEquals("LoginScreen", LoggingContext.getCurrentState());
        
        // Update state
        LoggingContext.setCurrentState("Dashboard");
        assertEquals("Dashboard", LoggingContext.getCurrentState());
        
        // Clear context
        LoggingContext.clear();
        assertNull(LoggingContext.getCurrentState());
    }
    
    @Test
    public void testOperationStack() {
        // Initially empty
        assertNull(LoggingContext.getCurrentOperation());
        
        // Push operations
        LoggingContext.pushOperation("findImage");
        assertEquals("findImage", LoggingContext.getCurrentOperation());
        
        LoggingContext.pushOperation("clickButton");
        assertEquals("clickButton", LoggingContext.getCurrentOperation());
        
        LoggingContext.pushOperation("typeText");
        assertEquals("typeText", LoggingContext.getCurrentOperation());
        
        // Pop operations
        LoggingContext.popOperation();
        assertEquals("clickButton", LoggingContext.getCurrentOperation());
        
        LoggingContext.popOperation();
        assertEquals("findImage", LoggingContext.getCurrentOperation());
        
        LoggingContext.popOperation();
        assertNull(LoggingContext.getCurrentOperation());
        
        // Pop from empty stack should not throw
        assertDoesNotThrow(() -> LoggingContext.popOperation());
        assertNull(LoggingContext.getCurrentOperation());
    }
    
    @Test
    public void testMetadataManagement() {
        // Initially empty
        Map<String, Object> metadata = LoggingContext.getAllMetadata();
        assertTrue(metadata.isEmpty());
        
        // Add metadata
        LoggingContext.addMetadata("key1", "value1");
        LoggingContext.addMetadata("key2", 123);
        LoggingContext.addMetadata("key3", true);
        
        metadata = LoggingContext.getAllMetadata();
        assertEquals(3, metadata.size());
        assertEquals("value1", metadata.get("key1"));
        assertEquals(123, metadata.get("key2"));
        assertEquals(true, metadata.get("key3"));
        
        // Update existing metadata
        LoggingContext.addMetadata("key1", "updated");
        metadata = LoggingContext.getAllMetadata();
        assertEquals("updated", metadata.get("key1"));
        
        // Remove metadata
        LoggingContext.removeMetadata("key2");
        metadata = LoggingContext.getAllMetadata();
        assertEquals(2, metadata.size());
        assertFalse(metadata.containsKey("key2"));
        
        // Clear all metadata
        LoggingContext.clear();
        metadata = LoggingContext.getAllMetadata();
        assertTrue(metadata.isEmpty());
    }
    
    @Test
    public void testContextSnapshot() {
        // Set up context
        LoggingContext.setSessionId("snapshot-session");
        LoggingContext.setCurrentState("TestState");
        LoggingContext.pushOperation("testOp1");
        LoggingContext.pushOperation("testOp2");
        LoggingContext.addMetadata("meta1", "value1");
        LoggingContext.addMetadata("meta2", 42);
        
        // Create snapshot
        LoggingContext.Snapshot snapshot = LoggingContext.snapshot();
        
        // Verify snapshot captured the state
        assertNotNull(snapshot);
        assertEquals("snapshot-session", snapshot.getSessionId());
        assertEquals("TestState", snapshot.getCurrentState());
        assertEquals("testOp2", snapshot.getCurrentOperation());
        
        Map<String, Object> snapshotMetadata = snapshot.getMetadata();
        assertEquals(2, snapshotMetadata.size());
        assertEquals("value1", snapshotMetadata.get("meta1"));
        assertEquals(42, snapshotMetadata.get("meta2"));
        
        // Modify context
        LoggingContext.clear();
        LoggingContext.setSessionId("different-session");
        LoggingContext.setCurrentState("DifferentState");
        
        // Restore from snapshot
        LoggingContext.restore(snapshot);
        
        // Verify restoration
        assertEquals("snapshot-session", LoggingContext.getSessionId());
        assertEquals("TestState", LoggingContext.getCurrentState());
        assertEquals("testOp2", LoggingContext.getCurrentOperation());
        
        Map<String, Object> restoredMetadata = LoggingContext.getAllMetadata();
        assertEquals(2, restoredMetadata.size());
        assertEquals("value1", restoredMetadata.get("meta1"));
        assertEquals(42, restoredMetadata.get("meta2"));
    }
    
    @Test
    public void testWithContext() {
        // Set initial context
        LoggingContext.setSessionId("original-session");
        LoggingContext.setCurrentState("OriginalState");
        LoggingContext.addMetadata("original", true);
        
        // Execute with temporary context
        String result = LoggingContext.withContext(() -> {
            // Temporary context
            LoggingContext.setSessionId("temp-session");
            LoggingContext.setCurrentState("TempState");
            LoggingContext.addMetadata("temporary", true);
            
            // Verify temporary context is active
            assertEquals("temp-session", LoggingContext.getSessionId());
            assertEquals("TempState", LoggingContext.getCurrentState());
            assertTrue((Boolean) LoggingContext.getAllMetadata().get("temporary"));
            
            return "completed";
        });
        
        // Verify result
        assertEquals("completed", result);
        
        // Verify original context is restored
        assertEquals("original-session", LoggingContext.getSessionId());
        assertEquals("OriginalState", LoggingContext.getCurrentState());
        assertTrue((Boolean) LoggingContext.getAllMetadata().get("original"));
        assertFalse(LoggingContext.getAllMetadata().containsKey("temporary"));
    }
    
    @Test
    public void testWithContextException() {
        // Set initial context
        LoggingContext.setSessionId("original-session");
        LoggingContext.setCurrentState("OriginalState");
        
        // Execute with temporary context that throws exception
        assertThrows(RuntimeException.class, () -> {
            LoggingContext.withContext(() -> {
                LoggingContext.setSessionId("temp-session");
                LoggingContext.setCurrentState("TempState");
                throw new RuntimeException("Test exception");
            });
        });
        
        // Verify original context is still restored after exception
        assertEquals("original-session", LoggingContext.getSessionId());
        assertEquals("OriginalState", LoggingContext.getCurrentState());
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
                        // Wait for all threads to be ready
                        startLatch.await();
                        
                        // Set thread-specific context
                        String sessionId = "session-" + threadId;
                        String state = "state-" + threadId;
                        
                        LoggingContext.setSessionId(sessionId);
                        LoggingContext.setCurrentState(state);
                        LoggingContext.addMetadata("threadId", threadId);
                        
                        // Simulate some work
                        Thread.sleep(10);
                        
                        // Verify thread-local context is preserved
                        if (sessionId.equals(LoggingContext.getSessionId()) &&
                            state.equals(LoggingContext.getCurrentState()) &&
                            Integer.valueOf(threadId).equals(LoggingContext.getAllMetadata().get("threadId"))) {
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
        }
    }
    
    @Test
    public void testClearContext() {
        // Set up complex context
        LoggingContext.setSessionId("test-session");
        LoggingContext.setCurrentState("TestState");
        LoggingContext.pushOperation("op1");
        LoggingContext.pushOperation("op2");
        LoggingContext.addMetadata("key1", "value1");
        LoggingContext.addMetadata("key2", "value2");
        
        // Clear everything
        LoggingContext.clear();
        
        // Verify everything is cleared
        assertNull(LoggingContext.getSessionId());
        assertNull(LoggingContext.getCurrentState());
        assertNull(LoggingContext.getCurrentOperation());
        assertTrue(LoggingContext.getAllMetadata().isEmpty());
        assertFalse(LoggingContext.hasSession());
    }
    
    @Test
    public void testClearSession() {
        // Set up context with session
        LoggingContext.setSessionId("test-session");
        LoggingContext.setCurrentState("TestState");
        LoggingContext.pushOperation("operation");
        LoggingContext.addMetadata("key", "value");
        
        // Clear only session
        LoggingContext.clearSession();
        
        // Verify only session is cleared, other context remains
        assertNull(LoggingContext.getSessionId());
        assertFalse(LoggingContext.hasSession());
        
        // Other context should still be present
        assertEquals("TestState", LoggingContext.getCurrentState());
        assertEquals("operation", LoggingContext.getCurrentOperation());
        assertEquals("value", LoggingContext.getAllMetadata().get("key"));
    }
    
    @Test
    public void testNullHandling() {
        // Test null session ID
        assertDoesNotThrow(() -> LoggingContext.setSessionId(null));
        assertNull(LoggingContext.getSessionId());
        assertFalse(LoggingContext.hasSession());
        
        // Test null state
        assertDoesNotThrow(() -> LoggingContext.setCurrentState(null));
        assertNull(LoggingContext.getCurrentState());
        
        // Test null operation
        assertDoesNotThrow(() -> LoggingContext.pushOperation(null));
        assertNull(LoggingContext.getCurrentOperation());
        
        // Test null metadata key
        assertDoesNotThrow(() -> LoggingContext.addMetadata(null, "value"));
        
        // Test null metadata value
        assertDoesNotThrow(() -> LoggingContext.addMetadata("key", null));
        
        // Test removing null key
        assertDoesNotThrow(() -> LoggingContext.removeMetadata(null));
    }
    
    @Test
    public void testOperationStackDepth() {
        // Push many operations to test stack depth
        int depth = 100;
        for (int i = 0; i < depth; i++) {
            LoggingContext.pushOperation("operation-" + i);
        }
        
        // Verify current operation is the last pushed
        assertEquals("operation-" + (depth - 1), LoggingContext.getCurrentOperation());
        
        // Pop all operations
        for (int i = depth - 1; i >= 0; i--) {
            assertEquals("operation-" + i, LoggingContext.getCurrentOperation());
            LoggingContext.popOperation();
        }
        
        // Stack should be empty
        assertNull(LoggingContext.getCurrentOperation());
    }
    
    @Test
    public void testMetadataIsolation() {
        // Add metadata with mutable object
        Map<String, String> mutableMap = new java.util.HashMap<>();
        mutableMap.put("inner", "value");
        LoggingContext.addMetadata("map", mutableMap);
        
        // Get metadata and modify the original
        Map<String, Object> metadata1 = LoggingContext.getAllMetadata();
        mutableMap.put("inner", "modified");
        mutableMap.put("new", "added");
        
        // Get metadata again
        Map<String, Object> metadata2 = LoggingContext.getAllMetadata();
        
        // Original modification should affect the stored value
        // (shallow copy behavior)
        Map<String, String> storedMap = (Map<String, String>) metadata2.get("map");
        assertEquals("modified", storedMap.get("inner"));
        assertEquals("added", storedMap.get("new"));
    }
}