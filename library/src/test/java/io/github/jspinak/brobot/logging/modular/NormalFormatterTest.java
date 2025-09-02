package io.github.jspinak.brobot.logging.modular;

import io.github.jspinak.brobot.action.ActionResult;
import io.github.jspinak.brobot.action.ActionType;
import io.github.jspinak.brobot.model.state.StateImage;
import io.github.jspinak.brobot.test.BrobotTestBase;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.Arrays;
import java.util.Collections;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

/**
 * Tests for NormalFormatter - NORMAL verbosity level formatter.
 */
@ExtendWith(MockitoExtension.class)
@DisplayName("NormalFormatter Tests")
public class NormalFormatterTest extends BrobotTestBase {

    @Mock
    private ActionResult actionResult;
    
    @Mock
    private ActionResult.ActionExecutionContext context;
    
    @Mock
    private StateImage stateImage;
    
    private NormalFormatter formatter;
    
    @BeforeEach
    @Override
    public void setupTest() {
        super.setupTest();
        formatter = new NormalFormatter();
        
        // Setup default mocks
        when(actionResult.getExecutionContext()).thenReturn(context);
    }
    
    @Test
    @DisplayName("Should return NORMAL verbosity level")
    void testGetVerbosityLevel() {
        assertEquals(ActionLogFormatter.VerbosityLevel.NORMAL, formatter.getVerbosityLevel());
    }
    
    @Test
    @DisplayName("Should format successful FIND action")
    void testFormatSuccessfulFind() {
        // Arrange
        Instant endTime = Instant.now();
        when(context.getEndTime()).thenReturn(endTime);
        when(context.isSuccess()).thenReturn(true);
        when(context.getActionType()).thenReturn("FIND");
        when(context.getStateName()).thenReturn("MainMenu");
        when(context.getObjectName()).thenReturn("LoginButton");
        when(context.getDurationMs()).thenReturn(234L);
        when(context.getMatchCount()).thenReturn(1);
        
        // Act
        String result = formatter.format(actionResult);
        
        // Assert
        assertNotNull(result);
        assertTrue(result.contains("✓")); // Success symbol
        assertTrue(result.contains("Find"));
        assertTrue(result.contains("MainMenu.LoginButton"));
        assertTrue(result.contains("234ms"));
        assertTrue(result.contains("1 match"));
        
        // Verify timestamp format
        DateTimeFormatter timeFormatter = DateTimeFormatter.ofPattern("HH:mm:ss");
        String expectedTime = endTime.atZone(ZoneId.systemDefault()).format(timeFormatter);
        assertTrue(result.contains("[" + expectedTime + "]"));
    }
    
    @Test
    @DisplayName("Should format failed CLICK action")
    void testFormatFailedClick() {
        // Arrange
        Instant endTime = Instant.now();
        when(context.getEndTime()).thenReturn(endTime);
        when(context.isSuccess()).thenReturn(false);
        when(context.getActionType()).thenReturn("CLICK");
        when(context.getStateName()).thenReturn("Dialog");
        when(context.getObjectName()).thenReturn("SubmitButton");
        when(context.getDurationMs()).thenReturn(156L);
        when(context.getMatchCount()).thenReturn(0);
        when(context.getFailureReason()).thenReturn("No matches found");
        
        // Act
        String result = formatter.format(actionResult);
        
        // Assert
        assertNotNull(result);
        assertTrue(result.contains("✗")); // Failure symbol
        assertTrue(result.contains("Click"));
        assertTrue(result.contains("Dialog.SubmitButton"));
        assertTrue(result.contains("156ms"));
        assertTrue(result.contains("No matches found"));
    }
    
    @Test
    @DisplayName("Should format TYPE action with text")
    void testFormatTypeAction() {
        // Arrange
        Instant endTime = Instant.now();
        when(context.getEndTime()).thenReturn(endTime);
        when(context.isSuccess()).thenReturn(true);
        when(context.getActionType()).thenReturn("TYPE");
        when(context.getStateName()).thenReturn("Form");
        when(context.getObjectName()).thenReturn("TextField");
        when(context.getDurationMs()).thenReturn(100L);
        when(context.getText()).thenReturn("Hello World");
        
        // Act
        String result = formatter.format(actionResult);
        
        // Assert
        assertNotNull(result);
        assertTrue(result.contains("✓"));
        assertTrue(result.contains("Type"));
        assertTrue(result.contains("Form.TextField"));
        assertTrue(result.contains("100ms"));
        assertTrue(result.contains("'Hello World'"));
    }
    
    @Test
    @DisplayName("Should format DRAG action")
    void testFormatDragAction() {
        // Arrange
        Instant endTime = Instant.now();
        when(context.getEndTime()).thenReturn(endTime);
        when(context.isSuccess()).thenReturn(true);
        when(context.getActionType()).thenReturn("DRAG");
        when(context.getStateName()).thenReturn("Canvas");
        when(context.getObjectName()).thenReturn("Element");
        when(context.getDurationMs()).thenReturn(500L);
        when(context.getToStateName()).thenReturn("DropZone");
        when(context.getToObjectName()).thenReturn("Target");
        
        // Act
        String result = formatter.format(actionResult);
        
        // Assert
        assertNotNull(result);
        assertTrue(result.contains("✓"));
        assertTrue(result.contains("Drag"));
        assertTrue(result.contains("Canvas.Element"));
        assertTrue(result.contains("→ DropZone.Target"));
        assertTrue(result.contains("500ms"));
    }
    
    @Test
    @DisplayName("Should handle object name with spaces")
    void testFormatWithSpacesInNames() {
        // Arrange
        Instant endTime = Instant.now();
        when(context.getEndTime()).thenReturn(endTime);
        when(context.isSuccess()).thenReturn(true);
        when(context.getActionType()).thenReturn("CLICK");
        when(context.getStateName()).thenReturn("Main Screen");
        when(context.getObjectName()).thenReturn("Login Button");
        when(context.getDurationMs()).thenReturn(150L);
        
        // Act
        String result = formatter.format(actionResult);
        
        // Assert
        assertNotNull(result);
        assertTrue(result.contains("'Main Screen'.'Login Button'"));
    }
    
    @Test
    @DisplayName("Should handle missing state name")
    void testFormatWithoutStateName() {
        // Arrange
        Instant endTime = Instant.now();
        when(context.getEndTime()).thenReturn(endTime);
        when(context.isSuccess()).thenReturn(true);
        when(context.getActionType()).thenReturn("CLICK");
        when(context.getStateName()).thenReturn(null);
        when(context.getObjectName()).thenReturn("Button");
        when(context.getDurationMs()).thenReturn(100L);
        
        // Act
        String result = formatter.format(actionResult);
        
        // Assert
        assertNotNull(result);
        assertTrue(result.contains("Button"));
        assertFalse(result.contains(".Button")); // No dot prefix
    }
    
    @Test
    @DisplayName("Should handle missing object name")
    void testFormatWithoutObjectName() {
        // Arrange
        Instant endTime = Instant.now();
        when(context.getEndTime()).thenReturn(endTime);
        when(context.isSuccess()).thenReturn(true);
        when(context.getActionType()).thenReturn("WAIT");
        when(context.getStateName()).thenReturn("Screen");
        when(context.getObjectName()).thenReturn(null);
        when(context.getDurationMs()).thenReturn(1000L);
        
        // Act
        String result = formatter.format(actionResult);
        
        // Assert
        assertNotNull(result);
        assertTrue(result.contains("Wait"));
        assertTrue(result.contains("Screen"));
        assertTrue(result.contains("1000ms"));
    }
    
    @Test
    @DisplayName("Should clean action type names")
    void testCleanActionType() {
        // Arrange
        Instant endTime = Instant.now();
        when(context.getEndTime()).thenReturn(endTime);
        when(context.isSuccess()).thenReturn(true);
        when(context.getDurationMs()).thenReturn(100L);
        
        // Test various action type formats
        String[] actionTypes = {
            "FIND", "find", "Find",
            "CLICK", "click", "Click",
            "TYPE", "type", "Type",
            "DRAG", "drag", "Drag"
        };
        
        String[] expectedOutputs = {
            "Find", "Find", "Find",
            "Click", "Click", "Click",
            "Type", "Type", "Type",
            "Drag", "Drag", "Drag"
        };
        
        for (int i = 0; i < actionTypes.length; i++) {
            when(context.getActionType()).thenReturn(actionTypes[i]);
            
            // Act
            String result = formatter.format(actionResult);
            
            // Assert
            assertNotNull(result);
            assertTrue(result.contains(expectedOutputs[i]), 
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
    @DisplayName("Should handle missing timestamp")
    void testFormatWithoutTimestamp() {
        // Arrange
        when(context.getEndTime()).thenReturn(null);
        when(context.isSuccess()).thenReturn(true);
        when(context.getActionType()).thenReturn("CLICK");
        when(context.getObjectName()).thenReturn("Button");
        when(context.getDurationMs()).thenReturn(100L);
        
        // Act
        String result = formatter.format(actionResult);
        
        // Assert
        assertNotNull(result);
        assertFalse(result.startsWith("[")); // No timestamp bracket
        assertTrue(result.contains("✓"));
        assertTrue(result.contains("Click"));
    }
    
    @Test
    @DisplayName("Should format action with multiple matches")
    void testFormatWithMultipleMatches() {
        // Arrange
        Instant endTime = Instant.now();
        when(context.getEndTime()).thenReturn(endTime);
        when(context.isSuccess()).thenReturn(true);
        when(context.getActionType()).thenReturn("FIND");
        when(context.getStateName()).thenReturn("Grid");
        when(context.getObjectName()).thenReturn("Cell");
        when(context.getDurationMs()).thenReturn(300L);
        when(context.getMatchCount()).thenReturn(5);
        
        // Act
        String result = formatter.format(actionResult);
        
        // Assert
        assertNotNull(result);
        assertTrue(result.contains("5 matches"));
    }
    
    @Test
    @DisplayName("Should truncate long text")
    void testFormatWithLongText() {
        // Arrange
        String longText = "This is a very long text that should be truncated " +
                         "because it exceeds the maximum allowed length for display " +
                         "in the normal verbosity level output format";
        
        Instant endTime = Instant.now();
        when(context.getEndTime()).thenReturn(endTime);
        when(context.isSuccess()).thenReturn(true);
        when(context.getActionType()).thenReturn("TYPE");
        when(context.getObjectName()).thenReturn("Field");
        when(context.getDurationMs()).thenReturn(200L);
        when(context.getText()).thenReturn(longText);
        
        // Act
        String result = formatter.format(actionResult);
        
        // Assert
        assertNotNull(result);
        assertTrue(result.contains("'"));
        assertTrue(result.contains("...'")); // Should be truncated
        assertTrue(result.length() < 200); // Result should be reasonably sized
    }
    
    @Test
    @DisplayName("Should log all actions")
    void testShouldLogAllActions() {
        // NormalFormatter should log all actions
        assertTrue(formatter.shouldLog(actionResult));
        assertTrue(formatter.shouldLog(null)); // Even null should return true
    }
    
    @Test
    @DisplayName("Should format vanish action")
    void testFormatVanishAction() {
        // Arrange
        Instant endTime = Instant.now();
        when(context.getEndTime()).thenReturn(endTime);
        when(context.isSuccess()).thenReturn(true);
        when(context.getActionType()).thenReturn("VANISH");
        when(context.getStateName()).thenReturn("Popup");
        when(context.getObjectName()).thenReturn("LoadingSpinner");
        when(context.getDurationMs()).thenReturn(2000L);
        
        // Act
        String result = formatter.format(actionResult);
        
        // Assert
        assertNotNull(result);
        assertTrue(result.contains("✓"));
        assertTrue(result.contains("Vanish"));
        assertTrue(result.contains("Popup.LoadingSpinner"));
        assertTrue(result.contains("2000ms"));
    }
    
    @Test
    @DisplayName("Should handle special characters in names")
    void testFormatWithSpecialCharacters() {
        // Arrange
        Instant endTime = Instant.now();
        when(context.getEndTime()).thenReturn(endTime);
        when(context.isSuccess()).thenReturn(true);
        when(context.getActionType()).thenReturn("CLICK");
        when(context.getStateName()).thenReturn("State-1");
        when(context.getObjectName()).thenReturn("Button_2.0");
        when(context.getDurationMs()).thenReturn(100L);
        
        // Act
        String result = formatter.format(actionResult);
        
        // Assert
        assertNotNull(result);
        assertTrue(result.contains("State-1.Button_2.0"));
    }
    
    @Test
    @DisplayName("Should format action with empty action type")
    void testFormatWithEmptyActionType() {
        // Arrange
        Instant endTime = Instant.now();
        when(context.getEndTime()).thenReturn(endTime);
        when(context.isSuccess()).thenReturn(true);
        when(context.getActionType()).thenReturn("");
        when(context.getObjectName()).thenReturn("Target");
        when(context.getDurationMs()).thenReturn(100L);
        
        // Act
        String result = formatter.format(actionResult);
        
        // Assert
        assertNotNull(result);
        assertTrue(result.contains("✓"));
        assertTrue(result.contains("Target"));
        assertTrue(result.contains("100ms"));
    }
}