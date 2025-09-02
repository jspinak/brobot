package io.github.jspinak.brobot.logging.modular;

import io.github.jspinak.brobot.action.ActionResult;
import io.github.jspinak.brobot.test.BrobotTestBase;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.Instant;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

/**
 * Tests for QuietFormatter - QUIET verbosity level formatter.
 */
@ExtendWith(MockitoExtension.class)
@DisplayName("QuietFormatter Tests")
public class QuietFormatterTest extends BrobotTestBase {

    @Mock
    private ActionResult actionResult;
    
    @Mock
    private ActionResult.ActionExecutionContext context;
    
    private QuietFormatter formatter;
    
    @BeforeEach
    @Override
    public void setupTest() {
        super.setupTest();
        formatter = new QuietFormatter();
        
        // Setup default mocks
        when(actionResult.getExecutionContext()).thenReturn(context);
    }
    
    @Test
    @DisplayName("Should return QUIET verbosity level")
    void testGetVerbosityLevel() {
        assertEquals(ActionLogFormatter.VerbosityLevel.QUIET, formatter.getVerbosityLevel());
    }
    
    @Test
    @DisplayName("Should format successful action with minimal output")
    void testFormatSuccessfulAction() {
        // Arrange
        when(context.isSuccess()).thenReturn(true);
        when(context.getActionType()).thenReturn("CLICK");
        when(context.getObjectName()).thenReturn("Button");
        
        // Act
        String result = formatter.format(actionResult);
        
        // Assert
        assertNotNull(result);
        assertEquals("✓ Click Button", result);
    }
    
    @Test
    @DisplayName("Should format failed action with minimal output")
    void testFormatFailedAction() {
        // Arrange
        when(context.isSuccess()).thenReturn(false);
        when(context.getActionType()).thenReturn("FIND");
        when(context.getObjectName()).thenReturn("Element");
        
        // Act
        String result = formatter.format(actionResult);
        
        // Assert
        assertNotNull(result);
        assertEquals("✗ Find Element", result);
    }
    
    @Test
    @DisplayName("Should format action with state and object name")
    void testFormatWithStateAndObject() {
        // Arrange
        when(context.isSuccess()).thenReturn(true);
        when(context.getActionType()).thenReturn("CLICK");
        when(context.getStateName()).thenReturn("MainMenu");
        when(context.getObjectName()).thenReturn("Settings");
        
        // Act
        String result = formatter.format(actionResult);
        
        // Assert
        assertNotNull(result);
        assertEquals("✓ Click MainMenu.Settings", result);
    }
    
    @Test
    @DisplayName("Should handle missing object name")
    void testFormatWithoutObjectName() {
        // Arrange
        when(context.isSuccess()).thenReturn(true);
        when(context.getActionType()).thenReturn("WAIT");
        when(context.getStateName()).thenReturn("LoadingScreen");
        when(context.getObjectName()).thenReturn(null);
        
        // Act
        String result = formatter.format(actionResult);
        
        // Assert
        assertNotNull(result);
        assertEquals("✓ Wait LoadingScreen", result);
    }
    
    @Test
    @DisplayName("Should handle missing state name")
    void testFormatWithoutStateName() {
        // Arrange
        when(context.isSuccess()).thenReturn(true);
        when(context.getActionType()).thenReturn("TYPE");
        when(context.getStateName()).thenReturn(null);
        when(context.getObjectName()).thenReturn("TextField");
        
        // Act
        String result = formatter.format(actionResult);
        
        // Assert
        assertNotNull(result);
        assertEquals("✓ Type TextField", result);
    }
    
    @Test
    @DisplayName("Should handle missing both state and object names")
    void testFormatWithoutStateAndObject() {
        // Arrange
        when(context.isSuccess()).thenReturn(true);
        when(context.getActionType()).thenReturn("WAIT");
        when(context.getStateName()).thenReturn(null);
        when(context.getObjectName()).thenReturn(null);
        
        // Act
        String result = formatter.format(actionResult);
        
        // Assert
        assertNotNull(result);
        assertEquals("✓ Wait", result);
    }
    
    @Test
    @DisplayName("Should clean action type names")
    void testCleanActionType() {
        // Arrange
        when(context.isSuccess()).thenReturn(true);
        when(context.getObjectName()).thenReturn("Target");
        
        String[] actionTypes = {
            "FIND", "find", "Find",
            "CLICK", "click", "Click",
            "TYPE", "type", "Type"
        };
        
        String[] expectedOutputs = {
            "Find", "Find", "Find",
            "Click", "Click", "Click",
            "Type", "Type", "Type"
        };
        
        for (int i = 0; i < actionTypes.length; i++) {
            when(context.getActionType()).thenReturn(actionTypes[i]);
            
            // Act
            String result = formatter.format(actionResult);
            
            // Assert
            assertEquals("✓ " + expectedOutputs[i] + " Target", result,
                "Expected '" + expectedOutputs[i] + "' for input '" + actionTypes[i] + "'");
        }
    }
    
    @Test
    @DisplayName("Should handle null execution context")
    void testFormatWithNullContext() {
        // Arrange
        when(actionResult.getExecutionContext()).thenReturn(null);
        
        // Act
        String result = formatter.format(actionResult);
        
        // Assert
        assertNull(result);
    }
    
    @Test
    @DisplayName("Should handle empty action type")
    void testFormatWithEmptyActionType() {
        // Arrange
        when(context.isSuccess()).thenReturn(true);
        when(context.getActionType()).thenReturn("");
        when(context.getObjectName()).thenReturn("Element");
        
        // Act
        String result = formatter.format(actionResult);
        
        // Assert
        assertNotNull(result);
        assertEquals("✓ Element", result);
    }
    
    @Test
    @DisplayName("Should handle null action type")
    void testFormatWithNullActionType() {
        // Arrange
        when(context.isSuccess()).thenReturn(false);
        when(context.getActionType()).thenReturn(null);
        when(context.getObjectName()).thenReturn("Component");
        
        // Act
        String result = formatter.format(actionResult);
        
        // Assert
        assertNotNull(result);
        assertEquals("✗ Component", result);
    }
    
    @Test
    @DisplayName("Should format DRAG action")
    void testFormatDragAction() {
        // Arrange
        when(context.isSuccess()).thenReturn(true);
        when(context.getActionType()).thenReturn("DRAG");
        when(context.getStateName()).thenReturn("Canvas");
        when(context.getObjectName()).thenReturn("Item");
        
        // Act
        String result = formatter.format(actionResult);
        
        // Assert
        assertNotNull(result);
        assertEquals("✓ Drag Canvas.Item", result);
    }
    
    @Test
    @DisplayName("Should format VANISH action")
    void testFormatVanishAction() {
        // Arrange
        when(context.isSuccess()).thenReturn(true);
        when(context.getActionType()).thenReturn("VANISH");
        when(context.getObjectName()).thenReturn("LoadingIndicator");
        
        // Act
        String result = formatter.format(actionResult);
        
        // Assert
        assertNotNull(result);
        assertEquals("✓ Vanish LoadingIndicator", result);
    }
    
    @Test
    @DisplayName("Should log only failed actions by default")
    void testShouldLogOnlyFailures() {
        // Arrange
        when(context.isSuccess()).thenReturn(false);
        
        // Act & Assert - Failed action should be logged
        assertTrue(formatter.shouldLog(actionResult));
        
        // Arrange - Successful action
        when(context.isSuccess()).thenReturn(true);
        
        // Act & Assert - Successful action should not be logged
        assertFalse(formatter.shouldLog(actionResult));
    }
    
    @Test
    @DisplayName("Should handle null action result in shouldLog")
    void testShouldLogWithNullResult() {
        // Act & Assert
        assertFalse(formatter.shouldLog(null));
    }
    
    @Test
    @DisplayName("Should handle spaces in names")
    void testFormatWithSpacesInNames() {
        // Arrange
        when(context.isSuccess()).thenReturn(false);
        when(context.getActionType()).thenReturn("CLICK");
        when(context.getStateName()).thenReturn("Main Screen");
        when(context.getObjectName()).thenReturn("Login Button");
        
        // Act
        String result = formatter.format(actionResult);
        
        // Assert
        assertNotNull(result);
        assertEquals("✗ Click 'Main Screen'.'Login Button'", result);
    }
    
    @Test
    @DisplayName("Should handle special characters in names")
    void testFormatWithSpecialCharacters() {
        // Arrange
        when(context.isSuccess()).thenReturn(false);
        when(context.getActionType()).thenReturn("FIND");
        when(context.getStateName()).thenReturn("State-1");
        when(context.getObjectName()).thenReturn("Button_2.0");
        
        // Act
        String result = formatter.format(actionResult);
        
        // Assert
        assertNotNull(result);
        assertEquals("✗ Find State-1.Button_2.0", result);
    }
    
    @Test
    @DisplayName("Should not include timing information")
    void testNoTimingInformation() {
        // Arrange
        when(context.isSuccess()).thenReturn(false);
        when(context.getActionType()).thenReturn("CLICK");
        when(context.getObjectName()).thenReturn("Button");
        when(context.getDurationMs()).thenReturn(500L);
        when(context.getEndTime()).thenReturn(Instant.now());
        
        // Act
        String result = formatter.format(actionResult);
        
        // Assert
        assertNotNull(result);
        assertEquals("✗ Click Button", result);
        assertFalse(result.contains("500"));
        assertFalse(result.contains("ms"));
    }
    
    @Test
    @DisplayName("Should not include match count")
    void testNoMatchCount() {
        // Arrange
        when(context.isSuccess()).thenReturn(false);
        when(context.getActionType()).thenReturn("FIND");
        when(context.getObjectName()).thenReturn("Pattern");
        when(context.getMatchCount()).thenReturn(5);
        
        // Act
        String result = formatter.format(actionResult);
        
        // Assert
        assertNotNull(result);
        assertEquals("✗ Find Pattern", result);
        assertFalse(result.contains("5"));
        assertFalse(result.contains("match"));
    }
    
    @Test
    @DisplayName("Should not include failure reason")
    void testNoFailureReason() {
        // Arrange
        when(context.isSuccess()).thenReturn(false);
        when(context.getActionType()).thenReturn("CLICK");
        when(context.getObjectName()).thenReturn("Element");
        when(context.getFailureReason()).thenReturn("Element not found after 3 attempts");
        
        // Act
        String result = formatter.format(actionResult);
        
        // Assert
        assertNotNull(result);
        assertEquals("✗ Click Element", result);
        assertFalse(result.contains("not found"));
        assertFalse(result.contains("attempts"));
    }
}