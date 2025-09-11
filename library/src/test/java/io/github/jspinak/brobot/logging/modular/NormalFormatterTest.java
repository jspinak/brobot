package io.github.jspinak.brobot.logging.modular;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

import java.time.Instant;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.Arrays;
import java.util.Collections;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import io.github.jspinak.brobot.action.ActionResult;
import io.github.jspinak.brobot.model.match.Match;
import io.github.jspinak.brobot.model.state.StateImage;
import io.github.jspinak.brobot.test.BrobotTestBase;

/** Tests for NormalFormatter - NORMAL verbosity level formatter. */
@ExtendWith(MockitoExtension.class)
@DisplayName("NormalFormatter Tests")
public class NormalFormatterTest extends BrobotTestBase {

    @Mock private ActionResult actionResult;

    @Mock private ActionResult.ActionExecutionContext context;

    @Mock private StateImage stateImage;

    private NormalFormatter formatter;

    @BeforeEach
    @Override
    public void setupTest() {
        super.setupTest();
        formatter = new NormalFormatter();

        // Setup default mocks with lenient stubbing to avoid unnecessary stubbing exceptions
        lenient().when(actionResult.getExecutionContext()).thenReturn(context);
    }

    @Test
    @DisplayName("Should return NORMAL verbosity level")
    void testGetVerbosityLevel() {
        // This test doesn't use mocks, so create a fresh formatter instance
        NormalFormatter testFormatter = new NormalFormatter();
        assertEquals(ActionLogFormatter.VerbosityLevel.NORMAL, testFormatter.getVerbosityLevel());
    }

    @Test
    @DisplayName("Should format successful FIND action")
    void testFormatSuccessfulFind() {
        // Arrange
        Instant endTime = Instant.now();
        when(context.getEndTime()).thenReturn(endTime);
        when(context.isSuccess()).thenReturn(true);
        when(context.getActionType()).thenReturn("FIND");
        when(context.getExecutionDuration()).thenReturn(java.time.Duration.ofMillis(234));

        StateImage img = mock(StateImage.class);
        when(img.getOwnerStateName()).thenReturn("MainMenu");
        when(img.getName()).thenReturn("LoginButton");
        when(context.getTargetImages()).thenReturn(Collections.singletonList(img));

        Match match = mock(Match.class);
        when(context.getResultMatches()).thenReturn(Collections.singletonList(match));

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
        when(context.getExecutionDuration()).thenReturn(java.time.Duration.ofMillis(156));

        StateImage img = mock(StateImage.class);
        when(img.getOwnerStateName()).thenReturn("Dialog");
        when(img.getName()).thenReturn("SubmitButton");
        when(context.getTargetImages()).thenReturn(Collections.singletonList(img));
        when(context.getResultMatches()).thenReturn(Collections.emptyList());

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
        when(context.getExecutionDuration()).thenReturn(java.time.Duration.ofMillis(100));
        when(context.getTargetStrings()).thenReturn(Collections.singletonList("Hello World"));

        // Act
        String result = formatter.format(actionResult);

        // Assert
        assertNotNull(result);
        assertTrue(result.contains("✓"));
        assertTrue(result.contains("Type"));
        assertTrue(result.contains("\"Hello World\""));
        assertTrue(result.contains("100ms"));
    }

    @Test
    @DisplayName("Should format DRAG action")
    void testFormatDragAction() {
        // Arrange
        Instant endTime = Instant.now();
        when(context.getEndTime()).thenReturn(endTime);
        when(context.isSuccess()).thenReturn(true);
        when(context.getActionType()).thenReturn("DRAG");
        when(context.getExecutionDuration()).thenReturn(java.time.Duration.ofMillis(500));

        StateImage img = mock(StateImage.class);
        when(img.getOwnerStateName()).thenReturn("Canvas");
        when(img.getName()).thenReturn("Element");
        when(context.getTargetImages()).thenReturn(Collections.singletonList(img));

        // Act
        String result = formatter.format(actionResult);

        // Assert
        assertNotNull(result);
        assertTrue(result.contains("✓"));
        assertTrue(result.contains("Drag"));
        assertTrue(result.contains("Canvas.Element"));
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
        when(context.getExecutionDuration()).thenReturn(java.time.Duration.ofMillis(150));

        StateImage img = mock(StateImage.class);
        when(img.getOwnerStateName()).thenReturn("Main Screen");
        when(img.getName()).thenReturn("Login Button");
        when(context.getTargetImages()).thenReturn(Collections.singletonList(img));

        // Act
        String result = formatter.format(actionResult);

        // Assert
        assertNotNull(result);
        assertTrue(result.contains("Main Screen.Login Button"));
    }

    @Test
    @DisplayName("Should handle missing state name")
    void testFormatWithoutStateName() {
        // Arrange
        Instant endTime = Instant.now();
        when(context.getEndTime()).thenReturn(endTime);
        when(context.isSuccess()).thenReturn(true);
        when(context.getActionType()).thenReturn("CLICK");
        when(context.getExecutionDuration()).thenReturn(java.time.Duration.ofMillis(100));

        StateImage img = mock(StateImage.class);
        when(img.getOwnerStateName()).thenReturn(null);
        when(img.getName()).thenReturn("Button");
        when(context.getTargetImages()).thenReturn(Collections.singletonList(img));

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
        when(context.getExecutionDuration()).thenReturn(java.time.Duration.ofMillis(1000));

        StateImage img = mock(StateImage.class);
        when(img.getOwnerStateName()).thenReturn("Screen");
        when(img.getName()).thenReturn(null);
        when(context.getTargetImages()).thenReturn(Collections.singletonList(img));

        // Act
        String result = formatter.format(actionResult);

        // Assert
        assertNotNull(result);
        assertTrue(result.contains("Wait"));
        assertTrue(result.contains("Screen.Image")); // Default name when null
        assertTrue(result.contains("1000ms"));
    }

    @Test
    @DisplayName("Should clean action type names")
    void testCleanActionType() {
        // Arrange
        Instant endTime = Instant.now();
        when(context.getEndTime()).thenReturn(endTime);
        when(context.isSuccess()).thenReturn(true);
        when(context.getExecutionDuration()).thenReturn(java.time.Duration.ofMillis(100));

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
            assertTrue(
                    result.contains(expectedOutputs[i]),
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
        // According to NormalFormatter.shouldLog(), it requires endTime to be non-null
        // But if we want to test the formatting without a timestamp in the output,
        // we need to look at the actual implementation - it checks context.getEndTime() != null
        // If endTime is null, shouldLog() will return false unless it's a significant action
        // Let's set up a significant action that will log even without endTime
        when(context.getEndTime()).thenReturn(null);
        when(context.getStartTime()).thenReturn(Instant.now());
        when(context.isSuccess()).thenReturn(true);
        when(context.getActionType()).thenReturn("CLICK");
        when(context.getExecutionDuration()).thenReturn(java.time.Duration.ofMillis(100));

        StateImage img = mock(StateImage.class);
        when(img.getName()).thenReturn("Button");
        when(context.getTargetImages()).thenReturn(Collections.singletonList(img));

        // Act
        String result = formatter.format(actionResult);

        // Assert
        // Since shouldLog() checks for endTime != null OR (startTime != null &&
        // isSignificantAction),
        // and CLICK with targets is significant, it should format
        assertNotNull(result);
        assertFalse(result.startsWith("[")); // No timestamp bracket since endTime is null
        assertTrue(result.contains("✓"));
        assertTrue(result.contains("Click"));
        assertTrue(result.contains("Button"));
    }

    @Test
    @DisplayName("Should format action with multiple matches")
    void testFormatWithMultipleMatches() {
        // Arrange
        Instant endTime = Instant.now();
        when(context.getEndTime()).thenReturn(endTime);
        when(context.isSuccess()).thenReturn(true);
        when(context.getActionType()).thenReturn("FIND");
        when(context.getExecutionDuration()).thenReturn(java.time.Duration.ofMillis(300));

        StateImage img = mock(StateImage.class);
        when(img.getOwnerStateName()).thenReturn("Grid");
        when(img.getName()).thenReturn("Cell");
        when(context.getTargetImages()).thenReturn(Collections.singletonList(img));

        Match[] matches = new Match[5];
        for (int i = 0; i < 5; i++) {
            matches[i] = mock(Match.class);
        }
        when(context.getResultMatches()).thenReturn(Arrays.asList(matches));

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
        String longText =
                "This is a very long text that should be truncated "
                        + "because it exceeds the maximum allowed length for display "
                        + "in the normal verbosity level output format";

        Instant endTime = Instant.now();
        when(context.getEndTime()).thenReturn(endTime);
        when(context.isSuccess()).thenReturn(true);
        when(context.getActionType()).thenReturn("TYPE");
        when(context.getExecutionDuration()).thenReturn(java.time.Duration.ofMillis(200));
        when(context.getTargetStrings()).thenReturn(Collections.singletonList(longText));

        // Act
        String result = formatter.format(actionResult);

        // Assert
        assertNotNull(result);
        assertTrue(result.contains("\""));
        assertTrue(result.contains("...\"")); // Should be truncated
        assertTrue(result.contains("completed"));
    }

    @Test
    @DisplayName("Should log completed actions")
    void testShouldLogCompletedActions() {
        // NormalFormatter should log completed actions
        when(context.getEndTime()).thenReturn(Instant.now());
        assertTrue(formatter.shouldLog(actionResult));

        // Should return false for null
        assertFalse(formatter.shouldLog(null));
    }

    @Test
    @DisplayName("Should format vanish action")
    void testFormatVanishAction() {
        // Arrange
        Instant endTime = Instant.now();
        when(context.getEndTime()).thenReturn(endTime);
        when(context.isSuccess()).thenReturn(true);
        when(context.getActionType()).thenReturn("VANISH");
        when(context.getExecutionDuration()).thenReturn(java.time.Duration.ofMillis(2000));

        StateImage img = mock(StateImage.class);
        when(img.getOwnerStateName()).thenReturn("Popup");
        when(img.getName()).thenReturn("LoadingSpinner");
        when(context.getTargetImages()).thenReturn(Collections.singletonList(img));

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
        when(context.getExecutionDuration()).thenReturn(java.time.Duration.ofMillis(100));

        StateImage img = mock(StateImage.class);
        when(img.getOwnerStateName()).thenReturn("State-1");
        when(img.getName()).thenReturn("Button_2.0");
        when(context.getTargetImages()).thenReturn(Collections.singletonList(img));

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
        when(context.getExecutionDuration()).thenReturn(java.time.Duration.ofMillis(100));
        when(context.getPrimaryTargetName()).thenReturn("Target");

        // Act
        String result = formatter.format(actionResult);

        // Assert
        assertNotNull(result);
        assertTrue(result.contains("✓"));
        assertTrue(result.contains("Target"));
        assertTrue(result.contains("100ms"));
    }
}
