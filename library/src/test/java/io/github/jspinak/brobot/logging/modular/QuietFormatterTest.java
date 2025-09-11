package io.github.jspinak.brobot.logging.modular;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

import java.time.Instant;
import java.util.Collections;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import io.github.jspinak.brobot.action.ActionResult;
import io.github.jspinak.brobot.model.state.StateImage;
import io.github.jspinak.brobot.test.BrobotTestBase;

/** Tests for QuietFormatter - QUIET verbosity level formatter. */
@ExtendWith(MockitoExtension.class)
@DisplayName("QuietFormatter Tests")
public class QuietFormatterTest extends BrobotTestBase {

    @Mock private ActionResult actionResult;

    @Mock private ActionResult.ActionExecutionContext context;

    private QuietFormatter formatter;

    @BeforeEach
    @Override
    public void setupTest() {
        super.setupTest();
        formatter = new QuietFormatter();

        // Setup default mocks with lenient stubbing to avoid unnecessary stubbing exceptions
        lenient().when(actionResult.getExecutionContext()).thenReturn(context);
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
        when(context.getEndTime()).thenReturn(Instant.now());
        when(context.getExecutionDuration()).thenReturn(java.time.Duration.ofMillis(100));
        when(context.getTargetImages()).thenReturn(Collections.emptyList());
        when(context.getTargetStrings()).thenReturn(Collections.emptyList());
        when(context.getTargetRegions()).thenReturn(Collections.emptyList());
        when(context.getPrimaryTargetName()).thenReturn("Button");

        // Act
        String result = formatter.format(actionResult);

        // Assert
        assertNotNull(result);
        assertEquals("✓ Click Button • 100ms", result);
    }

    @Test
    @DisplayName("Should format failed action with minimal output")
    void testFormatFailedAction() {
        // Arrange
        when(context.isSuccess()).thenReturn(false);
        when(context.getActionType()).thenReturn("FIND");
        when(context.getEndTime()).thenReturn(Instant.now());
        when(context.getExecutionDuration()).thenReturn(java.time.Duration.ofMillis(200));
        when(context.getTargetImages()).thenReturn(Collections.emptyList());
        when(context.getTargetStrings()).thenReturn(Collections.emptyList());
        when(context.getTargetRegions()).thenReturn(Collections.emptyList());
        when(context.getPrimaryTargetName()).thenReturn("Element");

        // Act
        String result = formatter.format(actionResult);

        // Assert
        assertNotNull(result);
        assertEquals("✗ Find Element • 200ms", result);
    }

    @Test
    @DisplayName("Should format action with state and object name")
    void testFormatWithStateAndObject() {
        // Arrange
        when(context.isSuccess()).thenReturn(true);
        when(context.getActionType()).thenReturn("CLICK");
        when(context.getEndTime()).thenReturn(Instant.now());
        when(context.getExecutionDuration()).thenReturn(java.time.Duration.ofMillis(50));

        StateImage img = mock(StateImage.class);
        when(img.getOwnerStateName()).thenReturn("MainMenu");
        when(img.getName()).thenReturn("Settings");
        when(context.getTargetImages()).thenReturn(Collections.singletonList(img));

        // Act
        String result = formatter.format(actionResult);

        // Assert
        assertNotNull(result);
        assertEquals("✓ Click MainMenu.Settings • 50ms", result);
    }

    @Test
    @DisplayName("Should handle missing object name")
    void testFormatWithoutObjectName() {
        // Arrange
        when(context.isSuccess()).thenReturn(true);
        when(context.getActionType()).thenReturn("WAIT");
        when(context.getEndTime()).thenReturn(Instant.now());
        when(context.getExecutionDuration()).thenReturn(java.time.Duration.ofMillis(1000));

        StateImage img = mock(StateImage.class);
        when(img.getOwnerStateName()).thenReturn("LoadingScreen");
        when(img.getName()).thenReturn(null);
        when(context.getTargetImages()).thenReturn(Collections.singletonList(img));

        // Act
        String result = formatter.format(actionResult);

        // Assert
        assertNotNull(result);
        assertEquals("✓ Wait LoadingScreen.Image • 1000ms", result);
    }

    @Test
    @DisplayName("Should handle missing state name")
    void testFormatWithoutStateName() {
        // Arrange
        when(context.isSuccess()).thenReturn(true);
        when(context.getActionType()).thenReturn("TYPE");
        when(context.getEndTime()).thenReturn(Instant.now());
        when(context.getExecutionDuration()).thenReturn(java.time.Duration.ofMillis(300));

        StateImage img = mock(StateImage.class);
        when(img.getOwnerStateName()).thenReturn(null);
        when(img.getName()).thenReturn("TextField");
        when(context.getTargetImages()).thenReturn(Collections.singletonList(img));

        // Act
        String result = formatter.format(actionResult);

        // Assert
        assertNotNull(result);
        assertEquals("✓ Type TextField • 300ms", result);
    }

    @Test
    @DisplayName("Should handle missing both state and object names")
    void testFormatWithoutStateAndObject() {
        // Arrange
        when(context.isSuccess()).thenReturn(true);
        when(context.getActionType()).thenReturn("WAIT");
        when(context.getEndTime()).thenReturn(Instant.now());
        when(context.getExecutionDuration()).thenReturn(null); // Test null duration too
        when(context.getTargetImages()).thenReturn(Collections.emptyList());
        when(context.getTargetStrings()).thenReturn(Collections.emptyList());
        when(context.getTargetRegions()).thenReturn(Collections.emptyList());
        when(context.getPrimaryTargetName()).thenReturn(null);

        // Act
        String result = formatter.format(actionResult);

        // Assert
        assertNotNull(result);
        assertEquals("✓ Wait • 0ms", result);
    }

    @Test
    @DisplayName("Should clean action type names")
    void testCleanActionType() {
        // Arrange
        when(context.isSuccess()).thenReturn(true);
        when(context.getEndTime()).thenReturn(Instant.now());
        when(context.getExecutionDuration()).thenReturn(java.time.Duration.ofMillis(100));
        when(context.getTargetImages()).thenReturn(Collections.emptyList());
        when(context.getTargetStrings()).thenReturn(Collections.emptyList());
        when(context.getTargetRegions()).thenReturn(Collections.emptyList());
        when(context.getPrimaryTargetName()).thenReturn("Target");

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
            assertEquals(
                    "✓ " + expectedOutputs[i] + " Target • 100ms",
                    result,
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
        when(context.getEndTime()).thenReturn(Instant.now());
        when(context.getExecutionDuration()).thenReturn(java.time.Duration.ofMillis(100));
        when(context.getTargetImages()).thenReturn(Collections.emptyList());
        when(context.getTargetStrings()).thenReturn(Collections.emptyList());
        when(context.getTargetRegions()).thenReturn(Collections.emptyList());
        when(context.getPrimaryTargetName()).thenReturn("Element");

        // Act
        String result = formatter.format(actionResult);

        // Assert
        assertNotNull(result);
        assertEquals("✓ Action Element • 100ms", result);
    }

    @Test
    @DisplayName("Should handle null action type")
    void testFormatWithNullActionType() {
        // Arrange
        when(context.isSuccess()).thenReturn(false);
        when(context.getActionType()).thenReturn(null);
        when(context.getEndTime()).thenReturn(Instant.now());
        when(context.getExecutionDuration()).thenReturn(java.time.Duration.ofMillis(100));
        when(context.getTargetImages()).thenReturn(Collections.emptyList());
        when(context.getTargetStrings()).thenReturn(Collections.emptyList());
        when(context.getTargetRegions()).thenReturn(Collections.emptyList());
        when(context.getPrimaryTargetName()).thenReturn("Component");

        // Act
        String result = formatter.format(actionResult);

        // Assert
        assertNotNull(result);
        assertEquals("✗ Action Component • 100ms", result);
    }

    @Test
    @DisplayName("Should format DRAG action")
    void testFormatDragAction() {
        // Arrange
        when(context.isSuccess()).thenReturn(true);
        when(context.getActionType()).thenReturn("DRAG");
        when(context.getEndTime()).thenReturn(Instant.now());
        when(context.getExecutionDuration()).thenReturn(java.time.Duration.ofMillis(500));

        StateImage img = mock(StateImage.class);
        when(img.getOwnerStateName()).thenReturn("Canvas");
        when(img.getName()).thenReturn("Item");
        when(context.getTargetImages()).thenReturn(Collections.singletonList(img));

        // Act
        String result = formatter.format(actionResult);

        // Assert
        assertNotNull(result);
        assertEquals("✓ Drag Canvas.Item • 500ms", result);
    }

    @Test
    @DisplayName("Should format VANISH action")
    void testFormatVanishAction() {
        // Arrange
        when(context.isSuccess()).thenReturn(true);
        when(context.getActionType()).thenReturn("VANISH");
        when(context.getEndTime()).thenReturn(Instant.now());
        when(context.getExecutionDuration()).thenReturn(java.time.Duration.ofMillis(2000));
        when(context.getTargetImages()).thenReturn(Collections.emptyList());
        when(context.getTargetStrings()).thenReturn(Collections.emptyList());
        when(context.getTargetRegions()).thenReturn(Collections.emptyList());
        when(context.getPrimaryTargetName()).thenReturn("LoadingIndicator");

        // Act
        String result = formatter.format(actionResult);

        // Assert
        assertNotNull(result);
        assertEquals("✓ Vanish LoadingIndicator • 2000ms", result);
    }

    @Test
    @DisplayName("Should log only completed actions")
    void testShouldLogCompletedActions() {
        // Arrange - action with end time
        when(context.getEndTime()).thenReturn(Instant.now());

        // Act & Assert - Should be logged
        assertTrue(formatter.shouldLog(actionResult));

        // Arrange - action without end time
        when(context.getEndTime()).thenReturn(null);

        // Act & Assert - Should not be logged
        assertFalse(formatter.shouldLog(actionResult));
    }

    @Test
    @DisplayName("Should handle null action result in shouldLog")
    void testShouldLogWithNullResult() {
        // Act & Assert
        assertFalse(formatter.shouldLog(null));
    }

    @Test
    @DisplayName("Should not need spaces quoted in names")
    void testFormatWithSpacesInNames() {
        // Arrange
        when(context.isSuccess()).thenReturn(false);
        when(context.getActionType()).thenReturn("CLICK");
        when(context.getEndTime()).thenReturn(Instant.now());
        when(context.getExecutionDuration()).thenReturn(java.time.Duration.ofMillis(100));

        StateImage img = mock(StateImage.class);
        when(img.getOwnerStateName()).thenReturn("Main Screen");
        when(img.getName()).thenReturn("Login Button");
        when(context.getTargetImages()).thenReturn(Collections.singletonList(img));

        // Act
        String result = formatter.format(actionResult);

        // Assert
        assertNotNull(result);
        // QuietFormatter doesn't quote names with spaces based on implementation
        assertEquals("✗ Click Main Screen.Login Button • 100ms", result);
    }

    @Test
    @DisplayName("Should handle special characters in names")
    void testFormatWithSpecialCharacters() {
        // Arrange
        when(context.isSuccess()).thenReturn(false);
        when(context.getActionType()).thenReturn("FIND");
        when(context.getEndTime()).thenReturn(Instant.now());
        when(context.getExecutionDuration()).thenReturn(java.time.Duration.ofMillis(150));

        StateImage img = mock(StateImage.class);
        when(img.getOwnerStateName()).thenReturn("State-1");
        when(img.getName()).thenReturn("Button_2.0");
        when(context.getTargetImages()).thenReturn(Collections.singletonList(img));

        // Act
        String result = formatter.format(actionResult);

        // Assert
        assertNotNull(result);
        assertEquals("✗ Find State-1.Button_2.0 • 150ms", result);
    }

    @Test
    @DisplayName("Should always include timing information")
    void testAlwaysIncludesTiming() {
        // Arrange
        when(context.isSuccess()).thenReturn(false);
        when(context.getActionType()).thenReturn("CLICK");
        when(context.getEndTime()).thenReturn(Instant.now());
        when(context.getExecutionDuration()).thenReturn(java.time.Duration.ofMillis(500));
        when(context.getTargetImages()).thenReturn(Collections.emptyList());
        when(context.getTargetStrings()).thenReturn(Collections.emptyList());
        when(context.getTargetRegions()).thenReturn(Collections.emptyList());
        when(context.getPrimaryTargetName()).thenReturn("Button");

        // Act
        String result = formatter.format(actionResult);

        // Assert
        assertNotNull(result);
        assertEquals("✗ Click Button • 500ms", result);
        assertTrue(result.contains("500ms"));
    }

    @Test
    @DisplayName("Should format with text target")
    void testFormatWithTextTarget() {
        // Arrange
        when(context.isSuccess()).thenReturn(true);
        when(context.getActionType()).thenReturn("TYPE");
        when(context.getEndTime()).thenReturn(Instant.now());
        when(context.getExecutionDuration()).thenReturn(java.time.Duration.ofMillis(100));
        when(context.getTargetStrings()).thenReturn(Collections.singletonList("Hello World"));

        // Act
        String result = formatter.format(actionResult);

        // Assert
        assertNotNull(result);
        assertEquals("✓ Type \"Hello World\" • 100ms", result);
    }

    @Test
    @DisplayName("Should truncate long text")
    void testFormatWithLongText() {
        // Arrange
        String longText =
                "This is a very long text that exceeds the maximum allowed length for display";
        when(context.isSuccess()).thenReturn(true);
        when(context.getActionType()).thenReturn("TYPE");
        when(context.getEndTime()).thenReturn(Instant.now());
        when(context.getExecutionDuration()).thenReturn(java.time.Duration.ofMillis(100));
        when(context.getTargetStrings()).thenReturn(Collections.singletonList(longText));

        // Act
        String result = formatter.format(actionResult);

        // Assert
        assertNotNull(result);
        // Text should be truncated at 50 chars (47 + "...") and wrapped in quotes
        assertTrue(
                result.contains("\"This is a very long text that exceeds the maxim...\""),
                "Result should contain truncated text. Actual: " + result);
        assertTrue(result.contains("• 100ms"));
    }
}
