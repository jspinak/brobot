package io.github.jspinak.brobot.actions.methods.basicactions.click;

import io.github.jspinak.brobot.actions.BrobotSettings;
import io.github.jspinak.brobot.actions.actionOptions.ActionOptions;
import io.github.jspinak.brobot.actions.methods.basicactions.find.Find;
import io.github.jspinak.brobot.actions.methods.sikuliWrappers.mouse.ClickLocationOnce;
import io.github.jspinak.brobot.actions.methods.time.Time;
import io.github.jspinak.brobot.datatypes.primitives.image.Pattern;
import io.github.jspinak.brobot.datatypes.primitives.location.Location;
import io.github.jspinak.brobot.datatypes.primitives.location.Position;
import io.github.jspinak.brobot.datatypes.primitives.match.Match;
import io.github.jspinak.brobot.datatypes.primitives.match.Matches;
import io.github.jspinak.brobot.datatypes.state.ObjectCollection;
import io.github.jspinak.brobot.datatypes.state.stateObject.stateImage.StateImage;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class ClickTest {

    @Mock
    private Find find;
    
    @Mock
    private ClickLocationOnce clickLocationOnce;
    
    @Mock
    private Time time;
    
    @Mock
    private AfterClick afterClick;

    private Click click;
    private boolean originalMockState;

    @BeforeAll
    public static void setup() {
        System.setProperty("java.awt.headless", "false");
    }

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
        click = new Click(find, clickLocationOnce, time, afterClick);
        originalMockState = BrobotSettings.mock;
        BrobotSettings.mock = true;
    }

    @Test
    void perform_shouldClickEachMatch() {
        // Setup
        StateImage stateImage = mock(StateImage.class);
        Pattern pattern = mock(Pattern.class);
        when(stateImage.getPatterns()).thenReturn(List.of(pattern));
        
        ObjectCollection objectCollection = new ObjectCollection.Builder()
                .withImages(stateImage)
                .build();
        
        ActionOptions actionOptions = new ActionOptions.Builder()
                .setAction(ActionOptions.Action.CLICK)
                .setTimesToRepeatIndividualAction(1)
                .build();

        // Create matches with actionOptions
        Matches matches = new Matches(actionOptions);
        
        // Mock find to return two matches
        doAnswer(invocation -> {
            Matches matchesArg = invocation.getArgument(0);
            matchesArg.add(new Match.Builder()
                    .setRegion(10, 10, 10, 10)
                    .setPosition(new Position(50, 50)) // Center position
                    .setSimScore(0.9)
                    .build());
            matchesArg.add(new Match.Builder()
                    .setRegion(30, 30, 10, 10)
                    .setPosition(new Position(50, 50)) // Center position
                    .setSimScore(0.9)
                    .build());
            matchesArg.setSuccess(true);
            return null;
        }).when(find).perform(any(Matches.class), any(ObjectCollection[].class));

        // Action
        click.perform(matches, objectCollection);

        // Verification
        assertTrue(matches.isSuccess());
        assertEquals(2, matches.getMatchList().size());
        
        // Verify clicks happened for each match
        verify(clickLocationOnce, times(2)).click(any(Location.class), any(ActionOptions.class));
        
        // Verify pause between individual actions
        verify(time, times(1)).wait(anyDouble());
    }

    @Test
    void perform_shouldRespectMaxMatchesToActOn() {
        // Setup
        StateImage stateImage = mock(StateImage.class);
        Pattern pattern = mock(Pattern.class);
        when(stateImage.getPatterns()).thenReturn(List.of(pattern));
        
        ObjectCollection objectCollection = new ObjectCollection.Builder()
                .withImages(stateImage)
                .build();
        
        ActionOptions actionOptions = new ActionOptions.Builder()
                .setAction(ActionOptions.Action.CLICK)
                .setMaxMatchesToActOn(1) // Only act on the first match
                .setTimesToRepeatIndividualAction(1)
                .build();

        // Create matches with actionOptions
        Matches matches = new Matches(actionOptions);

        // Mock find to return two matches
        doAnswer(invocation -> {
            Matches matchesArg = invocation.getArgument(0);
            matchesArg.add(new Match.Builder()
                    .setRegion(10, 10, 10, 10)
                    .setPosition(new Position(50, 50)) // Center position
                    .setSimScore(0.9)
                    .build());
            matchesArg.add(new Match.Builder()
                    .setRegion(30, 30, 10, 10)
                    .setPosition(new Position(50, 50)) // Center position
                    .setSimScore(0.9)
                    .build());
            matchesArg.setSuccess(true);
            return null;
        }).when(find).perform(any(Matches.class), any(ObjectCollection[].class));

        // Action
        click.perform(matches, objectCollection);

        // Verification
        assertTrue(matches.isSuccess());
        
        // Should only click once due to maxMatchesToActOn
        verify(clickLocationOnce, times(1)).click(any(Location.class), any(ActionOptions.class));
    }

    @Test
    void perform_shouldRepeatClickForIndividualAction() {
        // Setup
        StateImage stateImage = mock(StateImage.class);
        Pattern pattern = mock(Pattern.class);
        when(stateImage.getPatterns()).thenReturn(List.of(pattern));
        
        ObjectCollection objectCollection = new ObjectCollection.Builder()
                .withImages(stateImage)
                .build();
        
        ActionOptions actionOptions = new ActionOptions.Builder()
                .setAction(ActionOptions.Action.CLICK)
                .setTimesToRepeatIndividualAction(3) // Click each match 3 times
                .build();

        // Create matches with actionOptions
        Matches matches = new Matches(actionOptions);

        // Mock find to return one match
        doAnswer(invocation -> {
            Matches matchesArg = invocation.getArgument(0);
            matchesArg.add(new Match.Builder()
                    .setRegion(10, 10, 10, 10)
                    .setPosition(new Position(50, 50)) // Center position
                    .setSimScore(0.9)
                    .build());
            matchesArg.setSuccess(true);
            return null;
        }).when(find).perform(any(Matches.class), any(ObjectCollection[].class));

        // Action
        click.perform(matches, objectCollection);

        // Verification
        assertTrue(matches.isSuccess());
        
        // Should click 3 times for the single match
        verify(clickLocationOnce, times(3)).click(any(Location.class), any(ActionOptions.class));
        
        // Pause is called twice between the three clicks
        verify(time, times(2)).wait(anyDouble());
    }
}