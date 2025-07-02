package io.github.jspinak.brobot.action.basic.click;

import io.github.jspinak.brobot.config.FrameworkSettings;
import io.github.jspinak.brobot.action.ActionOptions;
import io.github.jspinak.brobot.action.ActionConfig;
import io.github.jspinak.brobot.action.basic.find.Find;
import io.github.jspinak.brobot.action.basic.click.Click;
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
import io.github.jspinak.brobot.action.basic.mouse.MousePressOptions;
import io.github.jspinak.brobot.model.action.MouseButton;

import org.junit.jupiter.api.BeforeAll;
import org.mockito.ArgumentCaptor;
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
    private SingleClickExecutor clickLocationOnce;
    
    @Mock
    private TimeProvider time;
    
    @Mock
    private PostClickHandler afterClick;

    private Click click;

    @BeforeAll
    public static void setup() {
        System.setProperty("java.awt.headless", "false");
    }

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
        click = new Click(find, clickLocationOnce, time, afterClick);
        FrameworkSettings.mock = true;
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
        ActionResult matches = new ActionResult(actionOptions);
        
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
        ActionResult matches = new ActionResult(actionOptions);

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
        ActionResult matches = new ActionResult(actionOptions);

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
        verify(clickLocationOnce, times(3)).click(any(Location.class), any(ActionOptions.class));
        
        // Pause is called twice between the three clicks
        verify(time, times(2)).wait(anyDouble());
    }
    
    // Tests using new ActionConfig API
    
    @Test
    void perform_withClickOptions_shouldClickEachMatch() {
        // Setup
        StateImage stateImage = mock(StateImage.class);
        Pattern pattern = mock(Pattern.class);
        when(stateImage.getPatterns()).thenReturn(List.of(pattern));
        
        ObjectCollection objectCollection = new ObjectCollection.Builder()
                .withImages(stateImage)
                .build();
        
        // Using new ClickOptions API
        ClickOptions clickOptions = new ClickOptions.Builder()
                .setNumberOfClicks(1) // Single click (default)
                .setPauseAfterEnd(0.5)
                .build();

        // Create matches with ClickOptions
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
        assertEquals(2, matches.getMatchList().size());
        
        // Verify clicks happened for each match using ActionConfig
        ArgumentCaptor<ActionConfig> configCaptor = ArgumentCaptor.forClass(ActionConfig.class);
        verify(clickLocationOnce, times(2)).click(any(Location.class), configCaptor.capture());
        
        // Verify the captured config is ClickOptions
        ActionConfig capturedConfig = configCaptor.getValue();
        assertTrue(capturedConfig instanceof ClickOptions);
        assertEquals(0.5, capturedConfig.getPauseAfterEnd());
    }
    
    @Test
    void perform_withClickOptions_rightClick() {
        // Setup
        StateImage stateImage = mock(StateImage.class);
        Pattern pattern = mock(Pattern.class);
        when(stateImage.getPatterns()).thenReturn(List.of(pattern));
        
        ObjectCollection objectCollection = new ObjectCollection.Builder()
                .withImages(stateImage)
                .build();
        
        // Using new ClickOptions API for right-click
        ClickOptions clickOptions = new ClickOptions.Builder()
                .setPressOptions(new MousePressOptions.Builder()
                        .setButton(MouseButton.RIGHT))
                .build();

        // Create matches with ClickOptions
        ActionResult matches = new ActionResult(clickOptions);
        
        // Mock find to return one match
        doAnswer(invocation -> {
            ActionResult matchesArg = invocation.getArgument(0);
            matchesArg.add(new Match.Builder()
                    .setRegion(10, 10, 10, 10)
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
        
        // Verify right-click with ClickOptions
        ArgumentCaptor<ActionConfig> configCaptor = ArgumentCaptor.forClass(ActionConfig.class);
        verify(clickLocationOnce).click(any(Location.class), configCaptor.capture());
        
        ActionConfig capturedConfig = configCaptor.getValue();
        assertTrue(capturedConfig instanceof ClickOptions);
        ClickOptions capturedClickOptions = (ClickOptions) capturedConfig;
        assertEquals(MouseButton.RIGHT, capturedClickOptions.getMousePressOptions().getButton());
    }
    
    @Test
    void perform_withClickOptions_doubleClick() {
        // Setup
        StateImage stateImage = mock(StateImage.class);
        Pattern pattern = mock(Pattern.class);
        when(stateImage.getPatterns()).thenReturn(List.of(pattern));
        
        ObjectCollection objectCollection = new ObjectCollection.Builder()
                .withImages(stateImage)
                .build();
        
        // Using new ClickOptions API for double-click
        ClickOptions clickOptions = new ClickOptions.Builder()
                .setNumberOfClicks(2)
                .setPressOptions(new MousePressOptions.Builder()
                        .setPauseAfterMouseDown(0.1)
                        .setPauseBeforeMouseUp(0.05))
                .build();

        // Create matches with ClickOptions
        ActionResult matches = new ActionResult(clickOptions);
        
        // Mock find to return one match
        doAnswer(invocation -> {
            ActionResult matchesArg = invocation.getArgument(0);
            matchesArg.add(new Match.Builder()
                    .setRegion(10, 10, 10, 10)
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
        
        // Verify double-click with proper timing
        ArgumentCaptor<ActionConfig> configCaptor = ArgumentCaptor.forClass(ActionConfig.class);
        verify(clickLocationOnce).click(any(Location.class), configCaptor.capture());
        
        ActionConfig capturedConfig = configCaptor.getValue();
        assertTrue(capturedConfig instanceof ClickOptions);
        ClickOptions capturedClickOptions = (ClickOptions) capturedConfig;
        assertEquals(2, capturedClickOptions.getNumberOfClicks());
        assertEquals(0.1, capturedClickOptions.getMousePressOptions().getPauseAfterMouseDown());
        assertEquals(0.05, capturedClickOptions.getMousePressOptions().getPauseBeforeMouseUp());
    }
}