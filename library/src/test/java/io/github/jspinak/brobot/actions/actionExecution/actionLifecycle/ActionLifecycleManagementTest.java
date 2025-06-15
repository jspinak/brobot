package io.github.jspinak.brobot.actions.actionExecution.actionLifecycle;

import io.github.jspinak.brobot.actions.actionOptions.ActionOptions;
import io.github.jspinak.brobot.actions.methods.time.Time;
import io.github.jspinak.brobot.datatypes.primitives.image.Image;
import io.github.jspinak.brobot.datatypes.primitives.match.Match;
import io.github.jspinak.brobot.datatypes.primitives.match.Matches;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import java.time.Duration;
import java.time.LocalDateTime;
import java.util.Collections;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class ActionLifecycleManagementTest {

    @InjectMocks
    private ActionLifecycleManagement actionLifecycleManagement;

    @Mock
    private Time time;

    @Mock
    private Matches matches;

    @Mock
    private ActionOptions actionOptions;

    @Mock
    private ActionLifecycle actionLifecycle;

    @BeforeEach
    void setUp() {
        // Initialize mocks created with @Mock annotation
        MockitoAnnotations.openMocks(this);
        // Common stubbing for most tests
        when(matches.getActionOptions()).thenReturn(actionOptions);
        when(matches.getActionLifecycle()).thenReturn(actionLifecycle);
    }

    @Test
    void incrementCompletedRepetitions_shouldIncrement() {
        actionLifecycleManagement.incrementCompletedRepetitions(matches);
        // Verify that the increment method was called on the lifecycle object
        verify(actionLifecycle).incrementCompletedRepetitions();
    }

    @Test
    void incrementCompletedSequences_shouldIncrement() {
        actionLifecycleManagement.incrementCompletedSequences(matches);
        // Verify that the increment method was called on the lifecycle object
        verify(actionLifecycle).incrementCompletedSequences();
    }

    @Test
    void getCurrentDuration_shouldReturnCorrectDuration() {
        LocalDateTime startTime = LocalDateTime.of(2023, 1, 1, 12, 0, 0);
        LocalDateTime now = LocalDateTime.of(2023, 1, 1, 12, 0, 5);

        when(actionLifecycle.getStartTime()).thenReturn(startTime);
        when(time.now()).thenReturn(now);

        Duration duration = actionLifecycleManagement.getCurrentDuration(matches);
        assertEquals(5, duration.getSeconds());
    }

    @Test
    void getCompletedRepetitions_shouldReturnCorrectCount() {
        when(actionLifecycle.getCompletedRepetitions()).thenReturn(5);
        int reps = actionLifecycleManagement.getCompletedRepetitions(matches);
        assertEquals(5, reps);
    }

    @Test
    void isMoreSequencesAllowed_shouldReturnTrueWhenAllowed() {
        when(actionOptions.getMaxTimesToRepeatActionSequence()).thenReturn(5);
        when(actionLifecycle.getCompletedSequences()).thenReturn(4);
        assertTrue(actionLifecycleManagement.isMoreSequencesAllowed(matches));
    }

    @Test
    void isMoreSequencesAllowed_shouldReturnFalseWhenNotAllowed() {
        when(actionOptions.getMaxTimesToRepeatActionSequence()).thenReturn(5);
        when(actionLifecycle.getCompletedSequences()).thenReturn(5);
        assertFalse(actionLifecycleManagement.isMoreSequencesAllowed(matches));
    }

    @Test
    void isOkToContinueAction_shouldReturnFalseWhenTimeExpires() {
        // Setup: Time has expired
        when(actionOptions.getMaxWait()).thenReturn(10.0);
        when(actionLifecycle.getStartTime()).thenReturn(LocalDateTime.now().minusSeconds(11));
        when(time.now()).thenReturn(LocalDateTime.now());
        when(actionLifecycle.getCompletedRepetitions()).thenReturn(1);

        // Execution & Assertion
        assertFalse(actionLifecycleManagement.isOkToContinueAction(matches, 1));
    }

    @Test
    void isOkToContinueAction_shouldReturnFalseWhenMaxRepsReached() {
        // Setup: Max repetitions reached for a non-FIND action
        when(actionOptions.getAction()).thenReturn(ActionOptions.Action.CLICK);
        when(actionOptions.getMaxTimesToRepeatActionSequence()).thenReturn(3);
        when(actionLifecycle.getCompletedRepetitions()).thenReturn(3);

        // Execution & Assertion
        assertFalse(actionLifecycleManagement.isOkToContinueAction(matches, 1));
    }

    @Test
    void isOkToContinueAction_shouldReturnFalseForFindFirstWithMatch() {
        // Setup: FIND_FIRST action with a match found
        when(actionOptions.getFind()).thenReturn(ActionOptions.Find.FIRST);
        when(matches.isEmpty()).thenReturn(false); // A match has been found
        when(actionOptions.getMaxWait()).thenReturn(10.0);
        when(actionLifecycle.getStartTime()).thenReturn(LocalDateTime.now());
        when(time.now()).thenReturn(LocalDateTime.now());

        // Execution & Assertion
        assertFalse(actionLifecycleManagement.isOkToContinueAction(matches, 1));
    }

    @Test
    void isOkToContinueAction_shouldReturnFalseForFindEachWithAllMatches() {
        // Setup: FIND_EACH action and all patterns are found
        when(actionOptions.getFind()).thenReturn(ActionOptions.Find.EACH);
        when(actionOptions.getDoOnEach()).thenReturn(ActionOptions.DoOnEach.FIRST);

        // Mock two different search images to simulate two patterns
        Image image1 = mock(Image.class);
        Image image2 = mock(Image.class);

        Match match1 = mock(Match.class);
        when(match1.getSearchImage()).thenReturn(image1);

        Match match2 = mock(Match.class);
        when(match2.getSearchImage()).thenReturn(image2);

        when(matches.getMatchList()).thenReturn(List.of(match1, match2));

        // Time is still valid
        when(actionOptions.getMaxWait()).thenReturn(10.0);
        when(actionLifecycle.getStartTime()).thenReturn(LocalDateTime.now());
        when(time.now()).thenReturn(LocalDateTime.now());

        // We are searching for 2 patterns, and we found 2
        assertFalse(actionLifecycleManagement.isOkToContinueAction(matches, 2));
    }

    @Test
    void isOkToContinueAction_shouldReturnFalseWhenTextAppears() {
        // Setup: Condition is TEXT_APPEARS and text is found
        when(actionOptions.getGetTextUntil()).thenReturn(ActionOptions.GetTextUntil.TEXT_APPEARS);
        when(actionOptions.getTextToAppearOrVanish()).thenReturn("hello");

        Match matchWithText = mock(Match.class);
        when(matchWithText.getText()).thenReturn("hello world");
        when(matches.getMatchList()).thenReturn(List.of(matchWithText));

        // Time is still valid
        when(actionOptions.getMaxWait()).thenReturn(10.0);
        when(actionLifecycle.getStartTime()).thenReturn(LocalDateTime.now());
        when(time.now()).thenReturn(LocalDateTime.now());

        assertFalse(actionLifecycleManagement.isOkToContinueAction(matches, 1));
    }

    @Test
    void isOkToContinueAction_shouldContinueWhenTextAppearsButWrongText() {
        // Setup: Condition is TEXT_APPEARS but the found text doesn't match
        when(actionOptions.getGetTextUntil()).thenReturn(ActionOptions.GetTextUntil.TEXT_APPEARS);
        when(actionOptions.getTextToAppearOrVanish()).thenReturn("goodbye");

        Match matchWithText = mock(Match.class);
        when(matchWithText.getText()).thenReturn("hello world");
        when(matches.getMatchList()).thenReturn(List.of(matchWithText));

        // Time and reps are still valid
        when(actionOptions.getMaxWait()).thenReturn(10.0);
        when(actionLifecycle.getStartTime()).thenReturn(LocalDateTime.now());
        when(time.now()).thenReturn(LocalDateTime.now());

        assertTrue(actionLifecycleManagement.isOkToContinueAction(matches, 1));
    }

    @Test
    void isOkToContinueAction_shouldReturnFalseWhenTextVanishes() {
        // Setup: Condition is TEXT_VANISHES and no text is found in any match
        when(actionOptions.getGetTextUntil()).thenReturn(ActionOptions.GetTextUntil.TEXT_VANISHES);
        when(actionOptions.getTextToAppearOrVanish()).thenReturn("disappear");

        // No matches found, or matches have no text
        when(matches.getMatchList()).thenReturn(Collections.emptyList());

        // Time is still valid
        when(actionOptions.getMaxWait()).thenReturn(10.0);
        when(actionLifecycle.getStartTime()).thenReturn(LocalDateTime.now());
        when(time.now()).thenReturn(LocalDateTime.now());

        assertFalse(actionLifecycleManagement.isOkToContinueAction(matches, 1));
    }
}
