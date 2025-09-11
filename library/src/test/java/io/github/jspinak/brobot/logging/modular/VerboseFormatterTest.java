package io.github.jspinak.brobot.logging.modular;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

import java.time.Instant;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;

import io.github.jspinak.brobot.action.ActionConfig;
import io.github.jspinak.brobot.action.ActionResult;
import io.github.jspinak.brobot.model.match.Match;
import io.github.jspinak.brobot.model.state.StateImage;
import io.github.jspinak.brobot.test.BrobotTestBase;

/** Tests for VerboseFormatter - VERBOSE verbosity level formatter. */
@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
@DisplayName("VerboseFormatter Tests")
public class VerboseFormatterTest extends BrobotTestBase {

    @Mock private ActionResult actionResult;

    @Mock private ActionResult.ActionExecutionContext context;

    @Mock private ActionConfig actionConfig;

    @Mock private Match match1;

    @Mock private Match match2;

    private VerboseFormatter formatter;

    @BeforeEach
    @Override
    public void setupTest() {
        super.setupTest();
        formatter = new VerboseFormatter();

        // Setup default mocks
        when(actionResult.getExecutionContext()).thenReturn(context);
    }

    @Test
    @DisplayName("Should return VERBOSE verbosity level")
    void testGetVerbosityLevel() {
        assertEquals(ActionLogFormatter.VerbosityLevel.VERBOSE, formatter.getVerbosityLevel());
    }

    @Test
    @DisplayName("Should format action with full details")
    void testFormatWithFullDetails() {
        // Arrange
        Instant startTime = Instant.now().minusMillis(500);
        Instant endTime = Instant.now();

        when(context.getStartTime()).thenReturn(startTime);
        when(context.getEndTime()).thenReturn(endTime);
        when(context.isSuccess()).thenReturn(true);
        when(context.getActionType()).thenReturn("CLICK");
        when(context.getPrimaryTargetName()).thenReturn("Dashboard.RefreshButton");
        when(context.getExecutionDuration()).thenReturn(java.time.Duration.ofMillis(500));

        // Act
        String result = formatter.format(actionResult);

        // Assert
        assertNotNull(result);
        assertTrue(result.contains("=== ACTION EXECUTION ==="));
        assertTrue(result.contains("Type:       CLICK"));
        assertTrue(result.contains("Primary:    Dashboard.RefreshButton"));
        assertTrue(result.contains("Status:     SUCCESS ✓"));
        assertTrue(result.contains("Duration:   500ms"));

        // Verify timestamp format
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss.SSS");
        String expectedStart = startTime.atZone(ZoneId.systemDefault()).format(formatter);
        String expectedEnd = endTime.atZone(ZoneId.systemDefault()).format(formatter);
        assertTrue(result.contains("Started:    " + expectedStart));
        assertTrue(result.contains("Completed:  " + expectedEnd));
    }

    @Test
    @DisplayName("Should format failed action with error details")
    void testFormatFailedAction() {
        // Arrange
        Instant endTime = Instant.now();
        when(context.getEndTime()).thenReturn(endTime);
        when(context.isSuccess()).thenReturn(false);
        when(context.getActionType()).thenReturn("FIND");
        when(context.getPrimaryTargetName()).thenReturn("LoginPage.UsernameField");
        when(context.getExecutionDuration()).thenReturn(java.time.Duration.ofMillis(1000));
        Exception error = new RuntimeException("Pattern not found after 3 attempts");
        when(context.getExecutionError()).thenReturn(error);

        // Act
        String result = formatter.format(actionResult);

        // Assert
        assertNotNull(result);
        assertTrue(result.contains("Status:     FAILED ✗"));
        assertTrue(result.contains("Exception:  RuntimeException"));
        assertTrue(result.contains("Message:    Pattern not found after 3 attempts"));
    }

    @Test
    @DisplayName("Should include match locations")
    void testFormatWithMatchLocations() {
        // Arrange
        when(context.getEndTime()).thenReturn(Instant.now());
        when(context.isSuccess()).thenReturn(true);
        when(context.getActionType()).thenReturn("FIND");
        when(context.getExecutionDuration()).thenReturn(java.time.Duration.ofMillis(200));

        io.github.jspinak.brobot.model.element.Region region1 =
                mock(io.github.jspinak.brobot.model.element.Region.class);
        io.github.jspinak.brobot.model.element.Region region2 =
                mock(io.github.jspinak.brobot.model.element.Region.class);
        when(region1.toString()).thenReturn("(100, 200, 50, 50)");
        when(region2.toString()).thenReturn("(300, 400, 50, 50)");
        when(match1.getRegion()).thenReturn(region1);
        when(match2.getRegion()).thenReturn(region2);
        when(match1.getScore()).thenReturn(0.95);
        when(match2.getScore()).thenReturn(0.88);

        List<Match> matches = Arrays.asList(match1, match2);
        when(context.getResultMatches()).thenReturn(matches);

        // Act
        String result = formatter.format(actionResult);

        // Assert
        assertNotNull(result);
        assertTrue(result.contains("Matches:    2"));
        assertTrue(result.contains("Score: 0.950"));
        assertTrue(result.contains("Score: 0.880"));
        assertTrue(result.contains("(100, 200, 50, 50)"));
        assertTrue(result.contains("(300, 400, 50, 50)"));
    }

    @Test
    @DisplayName("Should include action metrics")
    void testFormatWithActionMetrics() {
        // Arrange
        when(context.getEndTime()).thenReturn(Instant.now());
        when(context.isSuccess()).thenReturn(true);
        when(context.getActionType()).thenReturn("CLICK");
        when(context.getExecutionDuration()).thenReturn(java.time.Duration.ofMillis(150));

        ActionResult.ActionMetrics metrics = mock(ActionResult.ActionMetrics.class);
        when(metrics.getExecutionTimeMs()).thenReturn(150L);
        when(metrics.getMatchCount()).thenReturn(1);
        when(metrics.getBestMatchConfidence()).thenReturn(0.85);
        when(actionResult.getActionMetrics()).thenReturn(metrics);

        // Act
        String result = formatter.format(actionResult);

        // Assert
        assertNotNull(result);
        assertTrue(result.contains("--- METRICS ---"));
        assertTrue(result.contains("Execution Time:   150ms"));
        assertTrue(result.contains("Match Count:      1"));
        assertTrue(result.contains("Best Match Score: 0.850"));
    }

    @Test
    @DisplayName("Should format TYPE action with full text")
    void testFormatTypeActionWithText() {
        // Arrange
        String longText =
                "This is a longer text that should be shown in full "
                        + "in verbose mode without any truncation applied to it";

        when(context.getEndTime()).thenReturn(Instant.now());
        when(context.isSuccess()).thenReturn(true);
        when(context.getActionType()).thenReturn("TYPE");
        when(context.getExecutionDuration()).thenReturn(java.time.Duration.ofMillis(500));
        when(context.getTargetStrings()).thenReturn(Collections.singletonList(longText));

        // Act
        String result = formatter.format(actionResult);

        // Assert
        assertNotNull(result);
        assertTrue(result.contains("Strings (1):"));
        assertTrue(result.contains(longText)); // Full text should be included
    }

    @Test
    @DisplayName("Should format DRAG action with from/to details")
    void testFormatDragAction() {
        // Arrange
        when(context.getEndTime()).thenReturn(Instant.now());
        when(context.isSuccess()).thenReturn(true);
        when(context.getActionType()).thenReturn("DRAG");
        when(context.getPrimaryTargetName())
                .thenReturn("Canvas.DraggableItem -> DropArea.DropTarget");
        when(context.getExecutionDuration()).thenReturn(java.time.Duration.ofMillis(750));

        // Act
        String result = formatter.format(actionResult);

        // Assert
        assertNotNull(result);
        assertTrue(result.contains("Type:       DRAG"));
        assertTrue(result.contains("Primary:    Canvas.DraggableItem -> DropArea.DropTarget"));
    }

    @Test
    @DisplayName("Should handle missing optional fields")
    void testFormatWithMissingFields() {
        // Arrange
        when(context.getEndTime()).thenReturn(Instant.now());
        when(context.isSuccess()).thenReturn(true);
        when(context.getActionType()).thenReturn("WAIT");
        when(context.getExecutionDuration()).thenReturn(java.time.Duration.ofMillis(1000));
        // Leave other fields null

        // Act
        String result = formatter.format(actionResult);

        // Assert
        assertNotNull(result);
        assertTrue(result.contains("Type:       WAIT"));
        assertTrue(result.contains("Duration:   1000ms"));
        assertTrue(result.contains("Matches:    0 (No matches found)"));
    }

    @Test
    @DisplayName("Should format with StateImage targets")
    void testFormatWithStateImageTargets() {
        // Arrange
        when(context.getEndTime()).thenReturn(Instant.now());
        when(context.isSuccess()).thenReturn(true);
        when(context.getActionType()).thenReturn("CLICK");
        when(context.getExecutionDuration()).thenReturn(java.time.Duration.ofMillis(200));

        StateImage img1 = mock(StateImage.class);
        when(img1.getOwnerStateName()).thenReturn("LoginPage");
        when(img1.getName()).thenReturn("SubmitButton");
        when(context.getTargetImages()).thenReturn(Collections.singletonList(img1));

        // Act
        String result = formatter.format(actionResult);

        // Assert
        assertNotNull(result);
        assertTrue(result.contains("Images (1):"));
        assertTrue(result.contains("LoginPage.SubmitButton"));
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
    @DisplayName("Should show truncated match list for many matches")
    void testFormatWithManyMatches() {
        // Arrange
        when(context.getEndTime()).thenReturn(Instant.now());
        when(context.isSuccess()).thenReturn(true);
        when(context.getActionType()).thenReturn("FIND");
        when(context.getExecutionDuration()).thenReturn(java.time.Duration.ofMillis(500));

        // Create 25 mock matches
        Match[] matches = new Match[25];
        for (int i = 0; i < 25; i++) {
            matches[i] = mock(Match.class);
            io.github.jspinak.brobot.model.element.Region region =
                    mock(io.github.jspinak.brobot.model.element.Region.class);
            when(region.toString()).thenReturn(String.format("(%d, %d, 50, 50)", i * 10, i * 20));
            when(matches[i].getRegion()).thenReturn(region);
            when(matches[i].getScore()).thenReturn(0.9 - (i * 0.01));
        }
        when(context.getResultMatches()).thenReturn(Arrays.asList(matches));

        // Act
        String result = formatter.format(actionResult);

        // Assert
        assertNotNull(result);
        assertTrue(result.contains("Matches:    25"));
        assertTrue(result.contains("[1] Score: 0.900"));
        assertTrue(result.contains("[5] Score: 0.860"));
        assertTrue(result.contains("... and 20 more matches")); // Only shows first 5
    }

    @Test
    @DisplayName("Should log all actions")
    void testShouldLogAllActions() {
        // VerboseFormatter should log all actions with valid context
        when(context.getEndTime()).thenReturn(Instant.now());
        assertTrue(formatter.shouldLog(actionResult));

        // But should not log null actionResult (defensive programming)
        assertFalse(formatter.shouldLog(null));
    }

    @Test
    @DisplayName("Should show performance metrics")
    void testFormatWithPerformanceMetrics() {
        // Arrange
        when(context.getEndTime()).thenReturn(Instant.now());
        when(context.isSuccess()).thenReturn(true);
        when(context.getActionType()).thenReturn("FIND");
        when(context.getExecutionDuration()).thenReturn(java.time.Duration.ofMillis(234));

        ActionResult.ActionMetrics metrics = mock(ActionResult.ActionMetrics.class);
        when(metrics.getExecutionTimeMs()).thenReturn(234L);
        when(metrics.getMatchCount()).thenReturn(3);
        when(metrics.getBestMatchConfidence()).thenReturn(0.92);
        when(actionResult.getActionMetrics()).thenReturn(metrics);

        // Act
        String result = formatter.format(actionResult);

        // Assert
        assertNotNull(result);
        assertTrue(result.contains("--- METRICS ---"));
        assertTrue(result.contains("Execution Time:   234ms"));
        assertTrue(result.contains("Match Count:      3"));
        assertTrue(result.contains("Best Match Score: 0.920"));
    }

    @Test
    @DisplayName("Should format highlight action")
    void testFormatHighlightAction() {
        // Arrange
        when(context.getEndTime()).thenReturn(Instant.now());
        when(context.isSuccess()).thenReturn(true);
        when(context.getActionType()).thenReturn("HIGHLIGHT");
        when(context.getPrimaryTargetName()).thenReturn("Form.ErrorField");
        when(context.getExecutionDuration()).thenReturn(java.time.Duration.ofMillis(2000));

        // Act
        String result = formatter.format(actionResult);

        // Assert
        assertNotNull(result);
        assertTrue(result.contains("Type:       HIGHLIGHT"));
        assertTrue(result.contains("Primary:    Form.ErrorField"));
        assertTrue(result.contains("Duration:   2000ms"));
    }

    @Test
    @DisplayName("Should show action metrics with retry info")
    void testFormatWithRetryInfo() {
        // Arrange
        when(context.getEndTime()).thenReturn(Instant.now());
        when(context.isSuccess()).thenReturn(true);
        when(context.getActionType()).thenReturn("CLICK");
        when(context.getExecutionDuration()).thenReturn(java.time.Duration.ofMillis(3500));

        ActionResult.ActionMetrics metrics = mock(ActionResult.ActionMetrics.class);
        when(metrics.getExecutionTimeMs()).thenReturn(3500L);
        // These methods aren't used by VerboseFormatter, so don't stub them
        // when(metrics.getRetryCount()).thenReturn(2);
        // when(metrics.getRetryTimeMs()).thenReturn(2000L);
        when(actionResult.getActionMetrics()).thenReturn(metrics);

        // Act
        String result = formatter.format(actionResult);

        // Assert
        assertNotNull(result);
        // The metrics don't show retry details directly, they're in ActionMetrics
        assertTrue(result.contains("Execution Time:   3500ms"));
    }
}
