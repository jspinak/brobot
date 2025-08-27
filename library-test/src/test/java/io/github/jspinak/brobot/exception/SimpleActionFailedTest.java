package io.github.jspinak.brobot.exception;

import io.github.jspinak.brobot.action.ActionInterface;
import io.github.jspinak.brobot.test.BrobotTestBase;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

public class SimpleActionFailedTest extends BrobotTestBase {
    
    @Test
    void testCreateException() {
        // Create a simple exception
        ActionFailedException exception = new ActionFailedException(
            ActionInterface.Type.CLICK,
            "Test message"
        );
        
        // Check basic properties
        assertNotNull(exception);
        assertEquals(ActionInterface.Type.CLICK, exception.getActionType());
        assertEquals("Test message", exception.getActionDetails());
        assertEquals("Action CLICK failed: Test message", exception.getMessage());
    }
    
    @Test
    void testToString() {
        // Create exception
        ActionFailedException exception = new ActionFailedException(
            ActionInterface.Type.FIND,
            "Pattern not found"
        );
        
        // Call toString - this is where StackOverflow might happen
        System.out.println("Calling toString()...");
        String result = exception.toString();
        System.out.println("Result: " + result);
        
        assertNotNull(result);
    }
}