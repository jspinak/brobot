package io.github.jspinak.brobot.actions.methods.basicactions.click;

import io.github.jspinak.brobot.actions.actionOptions.ActionOptions;
import io.github.jspinak.brobot.actions.methods.basicactions.find.Find;
import io.github.jspinak.brobot.actions.methods.sikuliWrappers.mouse.ClickLocationOnce;
import io.github.jspinak.brobot.actions.methods.time.Time;
import io.github.jspinak.brobot.datatypes.primitives.location.Location;
import io.github.jspinak.brobot.datatypes.primitives.match.Match;
import io.github.jspinak.brobot.datatypes.primitives.match.Matches;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.mockito.stubbing.Answer;

import java.util.List;

import static org.mockito.Mockito.*;

class ClickTest {

    @InjectMocks
    private Click click;

    @Mock
    private Find find;
    @Mock
    private ClickLocationOnce clickLocationOnce;
    @Mock
    private Time time;
    @Mock
    private AfterClick afterClick;

    @Mock
    private Matches matches;
    @Mock
    private ActionOptions actionOptions;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
        when(matches.getActionOptions()).thenReturn(actionOptions);
    }

    @Test
    void perform_shouldClickEachMatch() {
        // Setup
        Match match1 = mock(Match.class);
        Match match2 = mock(Match.class);
        Location loc1 = mock(Location.class);
        Location loc2 = mock(Location.class);

        when(match1.getTarget()).thenReturn(loc1);
        when(match2.getTarget()).thenReturn(loc2);
        when(actionOptions.getTimesToRepeatIndividualAction()).thenReturn(1);

        // When find.perform is called, populate the matches object with our mock matches
        doAnswer((Answer<Void>) invocation -> {
            Matches matchesArg = invocation.getArgument(0);
            when(matchesArg.getMatchList()).thenReturn(List.of(match1, match2));
            return null;
        }).when(find).perform(any(Matches.class), any());

        // Action
        click.perform(matches);

        // Verification
        verify(find).perform(eq(matches), any());
        verify(clickLocationOnce).click(loc1, actionOptions);
        verify(clickLocationOnce).click(loc2, actionOptions);
        verify(match1).incrementTimesActedOn();
        verify(match2).incrementTimesActedOn();
        verify(time).wait(actionOptions.getPauseBetweenIndividualActions()); // called once between clicks
    }

    @Test
    void perform_shouldRespectMaxMatchesToActOn() {
        // Setup
        Match match1 = mock(Match.class);
        Match match2 = mock(Match.class);
        Location loc1 = mock(Location.class);
        Location loc2 = mock(Location.class);

        when(match1.getTarget()).thenReturn(loc1);
        when(match2.getTarget()).thenReturn(loc2);
        when(actionOptions.getMaxMatchesToActOn()).thenReturn(1); // Only act on the first match
        when(actionOptions.getTimesToRepeatIndividualAction()).thenReturn(1);

        doAnswer((Answer<Void>) invocation -> {
            Matches matchesArg = invocation.getArgument(0);
            when(matchesArg.getMatchList()).thenReturn(List.of(match1, match2));
            return null;
        }).when(find).perform(any(Matches.class), any());

        // Action
        click.perform(matches);

        // Verification
        verify(clickLocationOnce).click(loc1, actionOptions);
        verify(clickLocationOnce, never()).click(loc2, actionOptions); // Second match should not be clicked
    }

    @Test
    void perform_shouldRepeatClickForIndividualAction() {
        // Setup
        Match match1 = mock(Match.class);
        Location loc1 = mock(Location.class);

        when(match1.getTarget()).thenReturn(loc1);
        when(actionOptions.getTimesToRepeatIndividualAction()).thenReturn(3); // Click each match 3 times

        doAnswer((Answer<Void>) invocation -> {
            Matches matchesArg = invocation.getArgument(0);
            when(matchesArg.getMatchList()).thenReturn(List.of(match1));
            return null;
        }).when(find).perform(any(Matches.class), any());

        // Action
        click.perform(matches);

        // Verification
        verify(clickLocationOnce, times(3)).click(loc1, actionOptions);
        verify(match1, times(3)).incrementTimesActedOn();
        // pause is called twice between the three clicks
        verify(time, times(2)).wait(actionOptions.getPauseBetweenIndividualActions());
    }
}
