package io.github.jspinak.brobot.action.internal.execution;

import io.github.jspinak.brobot.action.internal.options.ActionOptions;
import io.github.jspinak.brobot.action.ActionResult;
import io.github.jspinak.brobot.model.element.Image;
import io.github.jspinak.brobot.model.element.Pattern;
import io.github.jspinak.brobot.model.match.Match;
import io.github.jspinak.brobot.tools.testing.mock.time.TimeProvider;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;

import java.awt.image.BufferedImage;

import java.time.Duration;
import java.time.LocalDateTime;

import static io.github.jspinak.brobot.action.internal.options.ActionOptions.GetTextUntil.*;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
class ActionLifecycleManagementTest {

    @Mock
    private TimeProvider timeProvider;
    
    private ActionLifecycleManagement lifecycleManagement;
    private ActionResult actionResult;
    private ActionOptions actionOptions;
    private ActionLifecycle actionLifecycle;
    private LocalDateTime startTime;
    
    @BeforeEach
    void setUp() {
        lifecycleManagement = new ActionLifecycleManagement(timeProvider);
        startTime = LocalDateTime.now();
        actionOptions = new ActionOptions();
        actionOptions.setMaxWait(10.0); // Set default max wait to match ActionLifecycle
        actionLifecycle = new ActionLifecycle(startTime, 10.0);
        actionResult = new ActionResult();
        actionResult.setActionOptions(actionOptions);
        actionResult.setActionLifecycle(actionLifecycle);
    }
    
    @Test
    void testIncrementCompletedRepetitions() {
        // Initial state
        assertEquals(0, actionLifecycle.getCompletedRepetitions());
        
        // Execute
        lifecycleManagement.incrementCompletedRepetitions(actionResult);
        
        // Verify
        assertEquals(1, actionLifecycle.getCompletedRepetitions());
    }
    
    @Test
    void testIncrementCompletedSequences() {
        // Initial state
        assertEquals(0, actionLifecycle.getCompletedSequences());
        
        // Execute
        lifecycleManagement.incrementCompletedSequences(actionResult);
        
        // Verify
        assertEquals(1, actionLifecycle.getCompletedSequences());
    }
    
    @Test
    void testGetCurrentDuration() {
        // Setup
        LocalDateTime currentTime = startTime.plusSeconds(5);
        when(timeProvider.now()).thenReturn(currentTime);
        
        // Execute
        Duration duration = lifecycleManagement.getCurrentDuration(actionResult);
        
        // Verify
        assertEquals(5, duration.getSeconds());
    }
    
    @Test
    void testGetCompletedRepetitions() {
        // Setup
        actionLifecycle.incrementCompletedRepetitions();
        actionLifecycle.incrementCompletedRepetitions();
        
        // Execute
        int reps = lifecycleManagement.getCompletedRepetitions(actionResult);
        
        // Verify
        assertEquals(2, reps);
    }
    
    @Test
    void testIsMoreSequencesAllowed_WithinLimit() {
        // Setup
        actionOptions.setMaxTimesToRepeatActionSequence(3);
        actionLifecycle.incrementCompletedSequences();
        
        // Execute
        boolean allowed = lifecycleManagement.isMoreSequencesAllowed(actionResult);
        
        // Verify
        assertTrue(allowed);
    }
    
    @Test
    void testIsMoreSequencesAllowed_AtLimit() {
        // Setup
        actionOptions.setMaxTimesToRepeatActionSequence(2);
        actionLifecycle.incrementCompletedSequences();
        actionLifecycle.incrementCompletedSequences();
        
        // Execute
        boolean allowed = lifecycleManagement.isMoreSequencesAllowed(actionResult);
        
        // Verify
        assertFalse(allowed);
    }
    
    @Test
    void testIsOkToContinueAction_FirstRepetition() {
        // Setup - first repetition is always allowed
        // No need to mock time for first repetition
        
        // Execute
        boolean okToContinue = lifecycleManagement.isOkToContinueAction(actionResult, 1);
        
        // Verify
        assertTrue(okToContinue);
    }
    
    @Test
    void testIsOkToContinueAction_TimeExceeded() {
        // Setup
        actionOptions.setMaxWait(5.0);
        actionLifecycle.incrementCompletedRepetitions();
        when(timeProvider.now()).thenReturn(startTime.plusSeconds(10));
        
        // Execute
        boolean okToContinue = lifecycleManagement.isOkToContinueAction(actionResult, 1);
        
        // Verify
        assertFalse(okToContinue);
    }
    
    @Test
    void testIsOkToContinueAction_MaxRepetitionsReached() {
        // Setup
        actionOptions.setMaxTimesToRepeatActionSequence(2);
        actionOptions.setAction(ActionOptions.Action.CLICK); // Not FIND
        actionLifecycle.incrementCompletedRepetitions();
        actionLifecycle.incrementCompletedRepetitions();
        // No need to mock time - we're under the time limit
        
        // Execute
        boolean okToContinue = lifecycleManagement.isOkToContinueAction(actionResult, 1);
        
        // Verify
        assertFalse(okToContinue);
    }
    
    @Test
    void testIsOkToContinueAction_FindActionIgnoresRepLimit() {
        // Setup
        actionOptions.setMaxTimesToRepeatActionSequence(1);
        actionOptions.setAction(ActionOptions.Action.FIND);
        actionOptions.setMaxWait(60.0); // Ensure we have enough time
        actionLifecycle.incrementCompletedRepetitions();
        actionLifecycle.incrementCompletedRepetitions();
        when(timeProvider.now()).thenReturn(startTime.plusSeconds(1));
        
        // Execute
        boolean okToContinue = lifecycleManagement.isOkToContinueAction(actionResult, 1);
        
        // Verify
        assertTrue(okToContinue); // FIND actions can exceed rep limit
    }
    
    @Test
    void testIsFindFirstAndAtLeastOneMatchFound_Success() {
        // Setup
        actionOptions.setFind(ActionOptions.Find.FIRST);
        Match match = new Match.Builder().build();
        actionResult.add(match);
        
        // Execute
        boolean found = lifecycleManagement.isFindFirstAndAtLeastOneMatchFound(actionResult);
        
        // Verify
        assertTrue(found);
    }
    
    @Test
    void testIsFindFirstAndAtLeastOneMatchFound_NoMatches() {
        // Setup
        actionOptions.setFind(ActionOptions.Find.FIRST);
        
        // Execute
        boolean found = lifecycleManagement.isFindFirstAndAtLeastOneMatchFound(actionResult);
        
        // Verify
        assertFalse(found);
    }
    
    @Test
    void testIsFindEachFirstAndEachPatternFound_AllFound() {
        // Setup
        actionOptions.setFind(ActionOptions.Find.EACH);
        actionOptions.setDoOnEach(ActionOptions.DoOnEach.FIRST);
        
        // Create distinct images to ensure they're recognized as different
        BufferedImage mockBuffImage1 = new BufferedImage(10, 10, BufferedImage.TYPE_INT_RGB);
        BufferedImage mockBuffImage2 = new BufferedImage(20, 20, BufferedImage.TYPE_INT_RGB);
        Image image1 = new Image(mockBuffImage1, "image1");
        Image image2 = new Image(mockBuffImage2, "image2");
        
        Match match1 = new Match.Builder()
                .setSearchImage(image1)
                .build();
        Match match2 = new Match.Builder()
                .setSearchImage(image2)
                .build();
                
        actionResult.add(match1);
        actionResult.add(match2);
        
        // Execute
        boolean allFound = lifecycleManagement.isFindEachFirstAndEachPatternFound(actionResult, 2);
        
        // Verify
        assertTrue(allFound);
    }
    
    @Test
    void testIsFindEachFirstAndEachPatternFound_NotAllFound() {
        // Setup
        actionOptions.setFind(ActionOptions.Find.EACH);
        actionOptions.setDoOnEach(ActionOptions.DoOnEach.FIRST);
        
        BufferedImage mockBuffImage1 = new BufferedImage(10, 10, BufferedImage.TYPE_INT_RGB);
        Image image1 = new Image(mockBuffImage1, "image1");
        
        Match match1 = new Match.Builder()
                .setSearchImage(image1)
                .build();
        Match match2 = new Match.Builder()
                .setSearchImage(image1) // Same image
                .build();
                
        actionResult.add(match1);
        actionResult.add(match2);
        
        // Execute
        boolean allFound = lifecycleManagement.isFindEachFirstAndEachPatternFound(actionResult, 2);
        
        // Verify
        assertFalse(allFound); // Only found 1 unique image, expected 2
    }
    
    @Test
    void testTextCondition_TextAppears() {
        // Setup
        actionOptions.setGetTextUntil(TEXT_APPEARS);
        actionOptions.setTextToAppearOrVanish("target");
        
        Match match = new Match.Builder()
                .setText("This contains target text")
                .build();
        actionResult.add(match);
        
        actionLifecycle.incrementCompletedRepetitions();
        when(timeProvider.now()).thenReturn(startTime.plusSeconds(1));
        
        // Execute
        boolean okToContinue = lifecycleManagement.isOkToContinueAction(actionResult, 1);
        
        // Verify
        assertFalse(okToContinue); // Should stop when text appears
    }
    
    @Test
    void testTextCondition_TextVanishes() {
        // Setup
        actionOptions.setGetTextUntil(TEXT_VANISHES);
        actionOptions.setTextToAppearOrVanish("target");
        
        Match match = new Match.Builder()
                .setText("This does not contain the word")
                .build();
        actionResult.add(match);
        
        actionLifecycle.incrementCompletedRepetitions();
        when(timeProvider.now()).thenReturn(startTime.plusSeconds(1));
        
        // Execute
        boolean okToContinue = lifecycleManagement.isOkToContinueAction(actionResult, 1);
        
        // Verify
        assertFalse(okToContinue); // Should stop when text vanishes
    }
    
    @Test
    void testTextCondition_None() {
        // Setup
        actionOptions.setGetTextUntil(NONE);
        actionOptions.setMaxWait(60.0); // Ensure we have enough time
        
        actionLifecycle.incrementCompletedRepetitions();
        when(timeProvider.now()).thenReturn(startTime.plusSeconds(1));
        
        // Execute
        boolean okToContinue = lifecycleManagement.isOkToContinueAction(actionResult, 1);
        
        // Verify
        assertTrue(okToContinue); // Text conditions don't apply
    }
    
    @Test
    void testTextCondition_EmptyTextToFind_ChecksForAnyText() {
        // Setup
        actionOptions.setGetTextUntil(TEXT_APPEARS);
        actionOptions.setTextToAppearOrVanish(""); // Empty means any text
        
        Match match = new Match.Builder()
                .setText("Some text")
                .build();
        actionResult.add(match);
        
        actionLifecycle.incrementCompletedRepetitions();
        when(timeProvider.now()).thenReturn(startTime.plusSeconds(1));
        
        // Execute
        boolean okToContinue = lifecycleManagement.isOkToContinueAction(actionResult, 1);
        
        // Verify
        assertFalse(okToContinue); // Should stop when any text appears
    }
    
    @Test
    void testPrintActionOnce() {
        // Setup
        assertFalse(actionLifecycle.isPrinted());
        
        // Execute first call
        lifecycleManagement.printActionOnce(actionResult);
        
        // Verify
        assertTrue(actionLifecycle.isPrinted());
        
        // Execute second call - should not print again
        lifecycleManagement.printActionOnce(actionResult);
        
        // Verify still only printed once
        assertTrue(actionLifecycle.isPrinted());
    }
    
    @Test
    void testComplexScenario_MultipleConditions() {
        // Setup - Find Each First with text condition
        actionOptions.setFind(ActionOptions.Find.EACH);
        actionOptions.setDoOnEach(ActionOptions.DoOnEach.FIRST);
        actionOptions.setGetTextUntil(TEXT_APPEARS);
        actionOptions.setTextToAppearOrVanish("found");
        actionOptions.setMaxWait(10.0);
        
        // Create distinct images to ensure they're recognized as different
        BufferedImage mockBuffImage1 = new BufferedImage(10, 10, BufferedImage.TYPE_INT_RGB);
        BufferedImage mockBuffImage2 = new BufferedImage(20, 20, BufferedImage.TYPE_INT_RGB);
        Image image1 = new Image(mockBuffImage1, "image1");
        Image image2 = new Image(mockBuffImage2, "image2");
        
        // First check - not all patterns found yet
        Match match1 = new Match.Builder()
                .setSearchImage(image1)
                .setText("searching...")
                .build();
        actionResult.add(match1);
        
        actionLifecycle.incrementCompletedRepetitions();
        when(timeProvider.now()).thenReturn(startTime.plusSeconds(2));
        
        boolean okToContinue = lifecycleManagement.isOkToContinueAction(actionResult, 2);
        assertTrue(okToContinue); // Should continue - not all patterns found
        
        // Second check - all patterns found but text not yet
        Match match2 = new Match.Builder()
                .setSearchImage(image2)
                .setText("still searching...")
                .build();
        actionResult.add(match2);
        
        okToContinue = lifecycleManagement.isOkToContinueAction(actionResult, 2);
        assertFalse(okToContinue); // Should stop - all patterns found
    }
}