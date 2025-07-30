package io.github.jspinak.brobot.action.basic.click;

import io.github.jspinak.brobot.config.FrameworkSettings;
import io.github.jspinak.brobot.action.ActionConfig;
import io.github.jspinak.brobot.action.basic.find.Find;
import io.github.jspinak.brobot.action.basic.click.Click;
import io.github.jspinak.brobot.action.basic.click.ClickOptions;
import io.github.jspinak.brobot.action.RepetitionOptions;
import io.github.jspinak.brobot.action.internal.mouse.PostClickHandler;
import io.github.jspinak.brobot.action.internal.mouse.SingleClickExecutor;
import io.github.jspinak.brobot.model.element.Pattern;
import io.github.jspinak.brobot.model.element.Location;
import io.github.jspinak.brobot.model.element.Position;
import io.github.jspinak.brobot.model.match.Match;
import io.github.jspinak.brobot.action.ActionResult;
import io.github.jspinak.brobot.action.ObjectCollection;
import io.github.jspinak.brobot.model.state.StateImage;
import io.github.jspinak.brobot.tools.testing.mock.time.TimeProvider;
import io.github.jspinak.brobot.action.internal.factory.ActionResultFactory;

import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

/**
 * Test class for Click using the new ActionConfig API (ClickOptions).
 */
class ClickTestV2 {

    @Mock
    private Find find;
    
    @Mock
    private SingleClickExecutor clickLocationOnce;
    
    @Mock
    private TimeProvider time;
    
    @Mock
    private PostClickHandler afterClick;
    
    @Mock
    private ActionResultFactory actionResultFactory;

    private Click click;

    @BeforeAll
    public static void setup() {
        System.setProperty("java.awt.headless", "false");
    }

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
        click = new Click(find, clickLocationOnce, time, afterClick, actionResultFactory);
        FrameworkSettings.mock = true;
        
        // Default mock behavior for actionResultFactory
        when(actionResultFactory.init(any(ActionConfig.class), anyString(), any()))
                .thenAnswer(invocation -> new ActionResult());
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
        
        // Using new ClickOptions API
        ClickOptions clickOptions = new ClickOptions.Builder()
                .setRepetition(RepetitionOptions.builder()
                    .timesToRepeatIndividualAction(1))
                .build();

        // Create matches with clickOptions
        ActionResult matches = new ActionResult(clickOptions);
        
        // Mock find to return two matches
        doAnswer(invocation -> {
            ActionResult matchesArg = invocation.getArgument(0);
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
        }).when(find).perform(any(ActionResult.class), any(ObjectCollection[].class));

        // Action
        click.perform(matches, objectCollection);

        // Verification
        assertTrue(matches.isSuccess());
        assertEquals(2, matches.getMatchList().size());
        
        // Verify clicks happened for each match
        verify(clickLocationOnce, times(2)).click(any(Location.class), any(ClickOptions.class));
        
        // Verify pause between individual actions
        verify(time, times(1)).wait(anyDouble());
    }

    @Test
    void perform_shouldClickAllFoundMatches() {
        // Setup
        StateImage stateImage = mock(StateImage.class);
        Pattern pattern = mock(Pattern.class);
        when(stateImage.getPatterns()).thenReturn(List.of(pattern));
        
        ObjectCollection objectCollection = new ObjectCollection.Builder()
                .withImages(stateImage)
                .build();
        
        // Using new ClickOptions API
        // Note: In the new architecture, Click uses Find internally.
        // To limit matches, you would need to configure the Find operation separately.
        ClickOptions clickOptions = new ClickOptions.Builder()
                .setRepetition(RepetitionOptions.builder()
                    .timesToRepeatIndividualAction(1))
                .build();

        // Create matches with clickOptions
        ActionResult matches = new ActionResult(clickOptions);

        // Mock find to return only one match (simulating Find's maxMatchesToActOn behavior)
        doAnswer(invocation -> {
            ActionResult matchesArg = invocation.getArgument(0);
            matchesArg.add(new Match.Builder()
                    .setRegion(10, 10, 10, 10)
                    .setPosition(new Position(50, 50)) // Center position
                    .setSimScore(0.9)
                    .build());
            // Find would have limited to 1 match based on its own configuration
            matchesArg.setSuccess(true);
            return null;
        }).when(find).perform(any(ActionResult.class), any(ObjectCollection[].class));

        // Action
        click.perform(matches, objectCollection);

        // Verification
        assertTrue(matches.isSuccess());
        
        // Should click once (the single match returned by Find)
        verify(clickLocationOnce, times(1)).click(any(Location.class), any(ClickOptions.class));
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
        
        // Using new ClickOptions API
        ClickOptions clickOptions = new ClickOptions.Builder()
                .setRepetition(RepetitionOptions.builder()
                    .timesToRepeatIndividualAction(3)) // Click each match 3 times
                .build();

        // Create matches with clickOptions
        ActionResult matches = new ActionResult(clickOptions);

        // Mock find to return one match
        doAnswer(invocation -> {
            ActionResult matchesArg = invocation.getArgument(0);
            matchesArg.add(new Match.Builder()
                    .setRegion(10, 10, 10, 10)
                    .setPosition(new Position(50, 50)) // Center position
                    .setSimScore(0.9)
                    .build());
            matchesArg.setSuccess(true);
            return null;
        }).when(find).perform(any(ActionResult.class), any(ObjectCollection[].class));

        // Action
        click.perform(matches, objectCollection);

        // Verification
        assertTrue(matches.isSuccess());
        
        // Should click 3 times for the single match
        verify(clickLocationOnce, times(3)).click(any(Location.class), any(ClickOptions.class));
        
        // Pause is called twice between the three clicks
        verify(time, times(2)).wait(anyDouble());
    }

    @Test
    void perform_shouldUseCustomPauseBetweenActions() {
        // Setup
        StateImage stateImage = mock(StateImage.class);
        Pattern pattern = mock(Pattern.class);
        when(stateImage.getPatterns()).thenReturn(List.of(pattern));
        
        ObjectCollection objectCollection = new ObjectCollection.Builder()
                .withImages(stateImage)
                .build();
        
        // Using new ClickOptions API with custom pause
        ClickOptions clickOptions = new ClickOptions.Builder()
                .setRepetition(RepetitionOptions.builder()
                    .pauseBetweenIndividualActions(2.5)) // Custom pause
                .build();

        // Create matches with clickOptions
        ActionResult matches = new ActionResult(clickOptions);

        // Mock find to return two matches
        doAnswer(invocation -> {
            ActionResult matchesArg = invocation.getArgument(0);
            matchesArg.add(new Match.Builder()
                    .setRegion(10, 10, 10, 10)
                    .setPosition(new Position(50, 50))
                    .setSimScore(0.9)
                    .build());
            matchesArg.add(new Match.Builder()
                    .setRegion(30, 30, 10, 10)
                    .setPosition(new Position(50, 50))
                    .setSimScore(0.9)
                    .build());
            matchesArg.setSuccess(true);
            return null;
        }).when(find).perform(any(ActionResult.class), any(ObjectCollection[].class));

        // Action
        click.perform(matches, objectCollection);

        // Verification
        assertTrue(matches.isSuccess());
        
        // Verify custom pause was used
        verify(time).wait(2.5);
    }
    
    @Test
    void perform_shouldThrowExceptionWhenNotGivenClickOptions() {
        // Setup
        ObjectCollection objectCollection = new ObjectCollection.Builder().build();
        
        // Create matches without ClickOptions (simulating incorrect usage)
        ActionResult matches = new ActionResult();

        // Action & Verification
        assertThrows(IllegalArgumentException.class, () -> {
            click.perform(matches, objectCollection);
        });
    }
}