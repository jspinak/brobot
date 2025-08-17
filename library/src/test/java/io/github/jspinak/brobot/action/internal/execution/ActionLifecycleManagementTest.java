package io.github.jspinak.brobot.action.internal.execution;

import io.github.jspinak.brobot.action.ActionResult;
import io.github.jspinak.brobot.action.ActionConfig;
import io.github.jspinak.brobot.action.basic.find.PatternFindOptions;
import io.github.jspinak.brobot.model.element.Image;
import io.github.jspinak.brobot.model.match.Match;
import io.github.jspinak.brobot.tools.testing.mock.time.TimeProvider;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;

import java.time.Duration;
import java.time.LocalDateTime;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

/**
 * Tests for ActionLifecycleManagement with the modern ActionConfig API.
 * Many ActionOptions-specific features have been simplified or removed.
 */
@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
class ActionLifecycleManagementTest {

    @Mock
    private TimeProvider timeProvider;
    
    private ActionLifecycleManagement lifecycleManagement;
    private ActionResult actionResult;
    private ActionConfig actionConfig;
    private ActionLifecycle actionLifecycle;
    private LocalDateTime startTime;
    
    @BeforeEach
    void setUp() {
        lifecycleManagement = new ActionLifecycleManagement(timeProvider);
        startTime = LocalDateTime.now();
        actionConfig = new PatternFindOptions.Builder()
                .setSearchDuration(10.0) // Set default search duration
                .build();
        actionLifecycle = new ActionLifecycle(startTime, 10.0);
        actionResult = new ActionResult();
        actionResult.setActionConfig(actionConfig);
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
        assertEquals(5.0, duration.getSeconds(), 0.1);
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
    void testIsMoreSequencesAllowed_FirstSequence() {
        // Setup - first sequence should be allowed
        assertEquals(0, actionLifecycle.getCompletedSequences());
        
        // Execute
        boolean allowed = lifecycleManagement.isMoreSequencesAllowed(actionResult);
        
        // Verify
        assertTrue(allowed);
    }
    
    @Test
    void testIsMoreSequencesAllowed_AfterFirstSequence() {
        // Setup - with ActionConfig, only one sequence is typically allowed
        actionLifecycle.incrementCompletedSequences();
        
        // Execute
        boolean allowed = lifecycleManagement.isMoreSequencesAllowed(actionResult);
        
        // Verify - should not allow more sequences after the first
        assertFalse(allowed);
    }
    
    @Test
    void testIsOkToContinueAction_FirstRepetition() {
        // Setup - first repetition is always allowed
        
        // Execute
        boolean okToContinue = lifecycleManagement.isOkToContinueAction(actionResult, 1);
        
        // Verify
        assertTrue(okToContinue);
    }
    
    @Test
    void testIsOkToContinueAction_TimeExceeded() {
        // Setup - simulate time exceeded
        actionLifecycle.incrementCompletedRepetitions();
        when(timeProvider.now()).thenReturn(startTime.plusSeconds(15));
        
        // Execute
        boolean okToContinue = lifecycleManagement.isOkToContinueAction(actionResult, 1);
        
        // Verify
        assertFalse(okToContinue);
    }
    
    @Test
    void testIsOkToContinueAction_WithMatches() {
        // Setup - add a match to the results
        Match match = mock(Match.class);
        actionResult.add(match);
        
        // With PatternFindOptions.Strategy.FIRST, finding one match should stop
        PatternFindOptions findOptions = new PatternFindOptions.Builder()
                .setStrategy(PatternFindOptions.Strategy.FIRST)
                .build();
        actionResult.setActionConfig(findOptions);
        
        // Execute
        boolean okToContinue = lifecycleManagement.isOkToContinueAction(actionResult, 1);
        
        // Verify - should not continue after finding first match
        assertFalse(okToContinue);
    }
    
    @Test
    void testIsOkToContinueAction_AllStrategy() {
        // Setup - with ALL strategy, continue searching
        PatternFindOptions findOptions = new PatternFindOptions.Builder()
                .setStrategy(PatternFindOptions.Strategy.ALL)
                .build();
        actionResult.setActionConfig(findOptions);
        actionResult.setActionLifecycle(actionLifecycle);
        
        // Execute
        boolean okToContinue = lifecycleManagement.isOkToContinueAction(actionResult, 1);
        
        // Verify - should continue with ALL strategy
        assertTrue(okToContinue);
    }
    
    @Test
    void testPrintActionOnce_FirstExecution() {
        // Setup
        assertFalse(actionLifecycle.isPrinted());
        
        // Execute
        lifecycleManagement.printActionOnce(actionResult);
        
        // Verify
        assertTrue(actionLifecycle.isPrinted());
    }
    
    @Test
    void testPrintActionOnce_SecondExecution() {
        // Setup
        actionLifecycle.setPrinted(true);
        
        // Execute
        lifecycleManagement.printActionOnce(actionResult);
        
        // Verify - should still be true
        assertTrue(actionLifecycle.isPrinted());
    }
    
    @Test
    void testGetCurrentDuration_WithNullLifecycle() {
        // Setup
        actionResult.setActionLifecycle(null);
        
        // Execute and Verify - should throw NPE or handle gracefully
        assertThrows(NullPointerException.class, () -> {
            lifecycleManagement.getCurrentDuration(actionResult);
        });
    }
    
    @Test
    void testIsMoreSequencesAllowed_WithNullLifecycle() {
        // Setup
        actionResult.setActionLifecycle(null);
        
        // Execute
        boolean allowed = lifecycleManagement.isMoreSequencesAllowed(actionResult);
        
        // Verify
        assertFalse(allowed);
    }
    
    @Test
    void testIncrementCompletedRepetitions_MultipleTimes() {
        // Execute multiple increments
        lifecycleManagement.incrementCompletedRepetitions(actionResult);
        lifecycleManagement.incrementCompletedRepetitions(actionResult);
        lifecycleManagement.incrementCompletedRepetitions(actionResult);
        
        // Verify
        assertEquals(3, actionLifecycle.getCompletedRepetitions());
    }
    
    @Test
    void testIncrementCompletedSequences_MultipleTimes() {
        // Execute multiple increments
        lifecycleManagement.incrementCompletedSequences(actionResult);
        lifecycleManagement.incrementCompletedSequences(actionResult);
        
        // Verify
        assertEquals(2, actionLifecycle.getCompletedSequences());
    }
    
    @Test
    void testIsOkToContinueAction_WithMultipleImages() {
        // Setup - test with multiple images parameter
        PatternFindOptions findOptions = new PatternFindOptions.Builder()
                .setStrategy(PatternFindOptions.Strategy.EACH)
                .build();
        actionResult.setActionConfig(findOptions);
        
        // Add matches for some images
        actionResult.add(mock(Match.class));
        actionResult.add(mock(Match.class));
        
        // Execute with 3 images total
        boolean okToContinue = lifecycleManagement.isOkToContinueAction(actionResult, 3);
        
        // Verify - should continue as not all images found
        assertTrue(okToContinue);
    }
    
    @Test
    void testIsOkToContinueAction_AllImagesFound() {
        // Setup - EACH strategy with all images found
        PatternFindOptions findOptions = new PatternFindOptions.Builder()
                .setStrategy(PatternFindOptions.Strategy.EACH)
                .build();
        actionResult.setActionConfig(findOptions);
        
        // Create distinct images for the matches
        Image image1 = mock(Image.class);
        Image image2 = mock(Image.class);
        
        // Create matches with distinct search images
        Match match1 = mock(Match.class);
        when(match1.getSearchImage()).thenReturn(image1);
        Match match2 = mock(Match.class);
        when(match2.getSearchImage()).thenReturn(image2);
        
        // Add matches for all 2 images
        actionResult.add(match1);
        actionResult.add(match2);
        
        // Execute with 2 images total
        boolean okToContinue = lifecycleManagement.isOkToContinueAction(actionResult, 2);
        
        // Verify - should not continue as all images found
        assertFalse(okToContinue);
    }
}