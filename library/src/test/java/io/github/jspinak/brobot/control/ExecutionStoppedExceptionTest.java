package io.github.jspinak.brobot.control;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class ExecutionStoppedExceptionTest {

    @Test
    void testDefaultConstructor() {
        ExecutionStoppedException exception = new ExecutionStoppedException();
        assertEquals("Execution has been stopped", exception.getMessage());
        assertNull(exception.getCause());
    }

    @Test
    void testMessageConstructor() {
        String customMessage = "Custom stop message";
        ExecutionStoppedException exception = new ExecutionStoppedException(customMessage);
        assertEquals(customMessage, exception.getMessage());
        assertNull(exception.getCause());
    }

    @Test
    void testCauseConstructor() {
        Throwable cause = new RuntimeException("Original cause");
        ExecutionStoppedException exception = new ExecutionStoppedException(cause);
        assertEquals("java.lang.RuntimeException: Original cause", exception.getMessage());
        assertEquals(cause, exception.getCause());
    }

    @Test
    void testMessageAndCauseConstructor() {
        String customMessage = "Stop with cause";
        Throwable cause = new IllegalStateException("State error");
        ExecutionStoppedException exception = new ExecutionStoppedException(customMessage, cause);
        assertEquals(customMessage, exception.getMessage());
        assertEquals(cause, exception.getCause());
    }

    @Test
    void testExceptionIsRuntimeException() {
        ExecutionStoppedException exception = new ExecutionStoppedException();
        assertTrue(exception instanceof RuntimeException);
    }

    @Test
    void testExceptionCanBeThrown() {
        assertThrows(ExecutionStoppedException.class, () -> {
            throw new ExecutionStoppedException("Test throw");
        });
    }

    @Test
    void testExceptionInCatchBlock() {
        String result = "";
        try {
            throw new ExecutionStoppedException("Stopped");
        } catch (ExecutionStoppedException e) {
            result = "Caught: " + e.getMessage();
        }
        assertEquals("Caught: Stopped", result);
    }

    @Test
    void testExceptionChaining() {
        Exception root = new Exception("Root cause");
        RuntimeException middle = new RuntimeException("Middle layer", root);
        ExecutionStoppedException top = new ExecutionStoppedException("Top level", middle);
        
        assertEquals("Top level", top.getMessage());
        assertEquals(middle, top.getCause());
        assertEquals(root, top.getCause().getCause());
    }

    @Test
    void testStackTraceAvailable() {
        ExecutionStoppedException exception = new ExecutionStoppedException();
        assertNotNull(exception.getStackTrace());
        assertTrue(exception.getStackTrace().length > 0);
        
        // Verify this test method is in the stack trace
        boolean foundThisMethod = false;
        for (StackTraceElement element : exception.getStackTrace()) {
            if (element.getMethodName().equals("testStackTraceAvailable")) {
                foundThisMethod = true;
                break;
            }
        }
        assertTrue(foundThisMethod);
    }
}