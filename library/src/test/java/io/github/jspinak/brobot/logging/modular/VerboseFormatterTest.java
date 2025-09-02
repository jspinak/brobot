package io.github.jspinak.brobot.logging.modular;

import io.github.jspinak.brobot.action.ActionResult;
import io.github.jspinak.brobot.action.ActionConfig;
import io.github.jspinak.brobot.model.element.Location;
import io.github.jspinak.brobot.model.match.Match;
import io.github.jspinak.brobot.test.BrobotTestBase;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.Instant;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

/**
 * Tests for VerboseFormatter - VERBOSE verbosity level formatter.
 */
@ExtendWith(MockitoExtension.class)
@DisplayName("VerboseFormatter Tests")
public class VerboseFormatterTest extends BrobotTestBase {

    @Mock
    private ActionResult actionResult;
    
    @Mock
    private ActionResult.ActionExecutionContext context;
    
    @Mock
    private ActionConfig actionConfig;
    
    @Mock
    private Match match1;
    
    @Mock
    private Match match2;
    
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
        when(context.getStateName()).thenReturn("Dashboard");
        when(context.getObjectName()).thenReturn("RefreshButton");
        when(context.getDurationMs()).thenReturn(500L);
        when(context.getMatchCount()).thenReturn(1);
        when(context.getSimilarity()).thenReturn(0.95);
        
        // Act
        String result = formatter.format(actionResult);
        
        // Assert
        assertNotNull(result);
        assertTrue(result.contains("══════════════════")); // Header separator
        assertTrue(result.contains("Action: CLICK"));
        assertTrue(result.contains("Target: Dashboard.RefreshButton"));
        assertTrue(result.contains("Status: ✓ SUCCESS"));
        assertTrue(result.contains("Duration: 500ms"));
        assertTrue(result.contains("Matches: 1"));
        assertTrue(result.contains("Similarity: 0.95"));
        
        // Verify timestamp format
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss.SSS");
        String expectedStart = startTime.atZone(ZoneId.systemDefault()).format(formatter);
        String expectedEnd = endTime.atZone(ZoneId.systemDefault()).format(formatter);
        assertTrue(result.contains("Started: " + expectedStart));
        assertTrue(result.contains("Ended:   " + expectedEnd));
    }
    
    @Test
    @DisplayName("Should format failed action with error details")
    void testFormatFailedAction() {
        // Arrange
        Instant endTime = Instant.now();
        when(context.getEndTime()).thenReturn(endTime);
        when(context.isSuccess()).thenReturn(false);
        when(context.getActionType()).thenReturn("FIND");
        when(context.getStateName()).thenReturn("LoginPage");
        when(context.getObjectName()).thenReturn("UsernameField");
        when(context.getDurationMs()).thenReturn(1000L);
        when(context.getFailureReason()).thenReturn("Pattern not found after 3 attempts");
        when(context.getErrorDetails()).thenReturn("Similarity threshold: 0.8, Best match: 0.65");
        
        // Act
        String result = formatter.format(actionResult);
        
        // Assert
        assertNotNull(result);
        assertTrue(result.contains("Status: ✗ FAILED"));
        assertTrue(result.contains("Failure: Pattern not found after 3 attempts"));
        assertTrue(result.contains("Details: Similarity threshold: 0.8, Best match: 0.65"));
    }
    
    @Test
    @DisplayName("Should include match locations")
    void testFormatWithMatchLocations() {
        // Arrange
        when(context.getEndTime()).thenReturn(Instant.now());
        when(context.isSuccess()).thenReturn(true);
        when(context.getActionType()).thenReturn("FIND");
        when(context.getDurationMs()).thenReturn(200L);
        
        Location loc1 = new Location(100, 200);
        Location loc2 = new Location(300, 400);
        when(match1.getLocation()).thenReturn(loc1);
        when(match2.getLocation()).thenReturn(loc2);
        when(match1.getScore()).thenReturn(0.95);
        when(match2.getScore()).thenReturn(0.88);
        
        List<Match> matches = Arrays.asList(match1, match2);
        when(context.getMatches()).thenReturn(matches);
        when(context.getMatchCount()).thenReturn(2);
        
        // Act
        String result = formatter.format(actionResult);
        
        // Assert
        assertNotNull(result);
        assertTrue(result.contains("Match Locations:"));
        assertTrue(result.contains("1. (100, 200) [score: 0.95]"));
        assertTrue(result.contains("2. (300, 400) [score: 0.88]"));
    }
    
    @Test
    @DisplayName("Should include action configuration details")
    void testFormatWithActionConfig() {
        // Arrange
        when(context.getEndTime()).thenReturn(Instant.now());
        when(context.isSuccess()).thenReturn(true);
        when(context.getActionType()).thenReturn("CLICK");
        when(context.getDurationMs()).thenReturn(150L);
        when(context.getActionConfig()).thenReturn(actionConfig);
        
        when(actionConfig.getSimilarity()).thenReturn(0.85);
        when(actionConfig.getPauseAfter()).thenReturn(500);
        when(actionConfig.getMaxWait()).thenReturn(3000);
        
        // Act
        String result = formatter.format(actionResult);
        
        // Assert
        assertNotNull(result);
        assertTrue(result.contains("Configuration:"));
        assertTrue(result.contains("Similarity: 0.85"));
        assertTrue(result.contains("Pause After: 500ms"));
        assertTrue(result.contains("Max Wait: 3000ms"));
    }
    
    @Test
    @DisplayName("Should format TYPE action with full text")
    void testFormatTypeActionWithText() {
        // Arrange
        String longText = "This is a longer text that should be shown in full " +
                         "in verbose mode without any truncation applied to it";
        
        when(context.getEndTime()).thenReturn(Instant.now());
        when(context.isSuccess()).thenReturn(true);
        when(context.getActionType()).thenReturn("TYPE");
        when(context.getDurationMs()).thenReturn(500L);
        when(context.getText()).thenReturn(longText);
        
        // Act
        String result = formatter.format(actionResult);
        
        // Assert
        assertNotNull(result);
        assertTrue(result.contains("Text Input:"));
        assertTrue(result.contains(longText)); // Full text should be included
    }
    
    @Test
    @DisplayName("Should format DRAG action with from/to details")
    void testFormatDragAction() {
        // Arrange
        when(context.getEndTime()).thenReturn(Instant.now());
        when(context.isSuccess()).thenReturn(true);
        when(context.getActionType()).thenReturn("DRAG");
        when(context.getStateName()).thenReturn("Canvas");
        when(context.getObjectName()).thenReturn("DraggableItem");
        when(context.getToStateName()).thenReturn("DropArea");
        when(context.getToObjectName()).thenReturn("DropTarget");
        when(context.getDurationMs()).thenReturn(750L);
        
        // Act
        String result = formatter.format(actionResult);
        
        // Assert
        assertNotNull(result);
        assertTrue(result.contains("From: Canvas.DraggableItem"));
        assertTrue(result.contains("To:   DropArea.DropTarget"));
    }
    
    @Test
    @DisplayName("Should handle missing optional fields")
    void testFormatWithMissingFields() {
        // Arrange
        when(context.getEndTime()).thenReturn(Instant.now());
        when(context.isSuccess()).thenReturn(true);
        when(context.getActionType()).thenReturn("WAIT");
        when(context.getDurationMs()).thenReturn(1000L);
        // Leave other fields null
        
        // Act
        String result = formatter.format(actionResult);
        
        // Assert
        assertNotNull(result);
        assertTrue(result.contains("Action: WAIT"));
        assertTrue(result.contains("Duration: 1000ms"));
        assertTrue(result.contains("Target: (none)"));
        assertFalse(result.contains("Similarity:")); // Should not show null similarity
        assertFalse(result.contains("Configuration:")); // Should not show null config
    }
    
    @Test
    @DisplayName("Should show state transitions")
    void testFormatWithStateTransition() {
        // Arrange
        when(context.getEndTime()).thenReturn(Instant.now());
        when(context.isSuccess()).thenReturn(true);
        when(context.getActionType()).thenReturn("CLICK");
        when(context.getDurationMs()).thenReturn(200L);
        when(context.getFromState()).thenReturn("LoginPage");
        when(context.getToState()).thenReturn("Dashboard");
        
        // Act
        String result = formatter.format(actionResult);
        
        // Assert
        assertNotNull(result);
        assertTrue(result.contains("State Transition:"));
        assertTrue(result.contains("LoginPage → Dashboard"));
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
        when(context.getDurationMs()).thenReturn(500L);
        when(context.getMatchCount()).thenReturn(25);
        
        // Create 25 mock matches
        Match[] matches = new Match[25];
        for (int i = 0; i < 25; i++) {
            matches[i] = mock(Match.class);
            when(matches[i].getLocation()).thenReturn(new Location(i * 10, i * 20));
            when(matches[i].getScore()).thenReturn(0.9 - (i * 0.01));
        }
        when(context.getMatches()).thenReturn(Arrays.asList(matches));
        
        // Act
        String result = formatter.format(actionResult);
        
        // Assert
        assertNotNull(result);
        assertTrue(result.contains("Match Locations: (showing first 10 of 25)"));
        assertTrue(result.contains("1. (0, 0)"));
        assertTrue(result.contains("10. (90, 180)"));
        assertFalse(result.contains("11. ")); // Should not show 11th match
    }
    
    @Test
    @DisplayName("Should log all actions")
    void testShouldLogAllActions() {
        // VerboseFormatter should log all actions
        assertTrue(formatter.shouldLog(actionResult));
        assertTrue(formatter.shouldLog(null));
    }
    
    @Test
    @DisplayName("Should show performance metrics")
    void testFormatWithPerformanceMetrics() {
        // Arrange
        when(context.getEndTime()).thenReturn(Instant.now());
        when(context.isSuccess()).thenReturn(true);
        when(context.getActionType()).thenReturn("FIND");
        when(context.getDurationMs()).thenReturn(234L);
        when(context.getSearchTimeMs()).thenReturn(180L);
        when(context.getPreprocessingTimeMs()).thenReturn(30L);
        when(context.getPostprocessingTimeMs()).thenReturn(24L);
        
        // Act
        String result = formatter.format(actionResult);
        
        // Assert
        assertNotNull(result);
        assertTrue(result.contains("Performance:"));
        assertTrue(result.contains("Search: 180ms"));
        assertTrue(result.contains("Preprocessing: 30ms"));
        assertTrue(result.contains("Postprocessing: 24ms"));
    }
    
    @Test
    @DisplayName("Should format highlight action")
    void testFormatHighlightAction() {
        // Arrange
        when(context.getEndTime()).thenReturn(Instant.now());
        when(context.isSuccess()).thenReturn(true);
        when(context.getActionType()).thenReturn("HIGHLIGHT");
        when(context.getStateName()).thenReturn("Form");
        when(context.getObjectName()).thenReturn("ErrorField");
        when(context.getDurationMs()).thenReturn(2000L);
        when(context.getHighlightColor()).thenReturn("RED");
        when(context.getHighlightSeconds()).thenReturn(2);
        
        // Act
        String result = formatter.format(actionResult);
        
        // Assert
        assertNotNull(result);
        assertTrue(result.contains("Action: HIGHLIGHT"));
        assertTrue(result.contains("Highlight:"));
        assertTrue(result.contains("Color: RED"));
        assertTrue(result.contains("Duration: 2s"));
    }
    
    @Test
    @DisplayName("Should show retry information")
    void testFormatWithRetryInfo() {
        // Arrange
        when(context.getEndTime()).thenReturn(Instant.now());
        when(context.isSuccess()).thenReturn(true);
        when(context.getActionType()).thenReturn("CLICK");
        when(context.getDurationMs()).thenReturn(3500L);
        when(context.getRetryCount()).thenReturn(2);
        when(context.getMaxRetries()).thenReturn(3);
        
        // Act
        String result = formatter.format(actionResult);
        
        // Assert
        assertNotNull(result);
        assertTrue(result.contains("Retries: 2/3"));
    }
}