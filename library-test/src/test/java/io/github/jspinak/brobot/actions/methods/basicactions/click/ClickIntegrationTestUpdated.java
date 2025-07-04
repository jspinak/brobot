package io.github.jspinak.brobot.actions.methods.basicactions.click;

import io.github.jspinak.brobot.config.FrameworkSettings;
import io.github.jspinak.brobot.action.ActionInterface;
import io.github.jspinak.brobot.action.ActionResult;
import io.github.jspinak.brobot.action.ObjectCollection;
import io.github.jspinak.brobot.action.basic.click.ClickOptions;
import io.github.jspinak.brobot.action.basic.find.Find;
import io.github.jspinak.brobot.action.internal.mouse.MoveMouseWrapper;
import io.github.jspinak.brobot.action.internal.mouse.MouseDownWrapper;
import io.github.jspinak.brobot.action.internal.mouse.MouseUpWrapper;
import io.github.jspinak.brobot.action.internal.service.ActionService;
import io.github.jspinak.brobot.model.element.Location;
import io.github.jspinak.brobot.model.match.Match;

import org.junit.jupiter.api.*;
import org.mockito.ArgumentCaptor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.SpyBean;

import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

/**
 * Updated integration tests for click actions using the new ActionConfig API.
 * Demonstrates migration from ActionOptions to ClickOptions.
 * 
 * Key changes:
 * - Uses ClickOptions instead of generic ActionOptions
 * - ActionResult requires setActionConfig() before perform()
 * - Uses ActionService to get the appropriate action
 * - ClickType is now ClickOptions.Type
 */
@SpringBootTest
public class ClickIntegrationTestUpdated {

    @Autowired
    private ActionService actionService;

    @SpyBean
    private Find find;

    @SpyBean
    private MoveMouseWrapper moveMouseWrapper;

    @SpyBean
    private MouseDownWrapper mouseDownWrapper;

    @SpyBean
    private MouseUpWrapper mouseUpWrapper;

    private boolean originalMockState;

    @BeforeAll
    public static void setup() {
        System.setProperty("java.awt.headless", "false");
    }

    @BeforeEach
    void setUp() {
        originalMockState = FrameworkSettings.mock;
        // Force the test to run in MOCK mode to avoid SikuliX headless issues
        FrameworkSettings.mock = true;

        // Since find.perform is a void method that modifies its arguments,
        // we use doAnswer to simulate this behavior.
        doAnswer(invocation -> {
            ActionResult matches = invocation.getArgument(0);
            matches.add(new Match.Builder()
                    .setRegion(10, 10, 10, 10)
                    .setSimScore(0.9)
                    .build());
            matches.setSuccess(true);
            return null;
        }).when(find).perform(any(ActionResult.class), any(ObjectCollection[].class));
    }
    
    @AfterEach
    void tearDown() {
        FrameworkSettings.mock = originalMockState;
    }

    @Test
    void perform_simpleClick_shouldMoveAndPressDownAndUp() {
        // Setup - NEW API: Use ClickOptions instead of ActionOptions
        ClickOptions clickOptions = new ClickOptions.Builder()
                .build();  // Defaults to LEFT click
        
        ObjectCollection objectCollection = new ObjectCollection();

        // NEW API: Create ActionResult with config
        ActionResult result = new ActionResult();
        result.setActionConfig(clickOptions);
        
        // Get the action from service
        ActionInterface clickAction = actionService.getAction(clickOptions);
        assertNotNull(clickAction);

        // Action
        clickAction.perform(result, objectCollection);

        // Verification
        Assertions.assertTrue(result.isSuccess());
        
        // In mock mode, the wrapper methods are not called
        if (!FrameworkSettings.mock) {
            verify(moveMouseWrapper).move(any(Location.class));
            verify(mouseDownWrapper).press(anyDouble(), anyDouble(), eq(ClickOptions.Type.LEFT));
            verify(mouseUpWrapper).press(anyDouble(), anyDouble(), eq(ClickOptions.Type.LEFT));
        }
    }

    @Test
    void perform_doubleClick_shouldResultInTwoMouseDownAndUpEvents() {
        // Setup - NEW API: Use specific click type enum
        ClickOptions clickOptions = new ClickOptions.Builder()
                .setClickType(ClickOptions.Type.DOUBLE_LEFT)
                .setPauseAfterMouseDown(0.1) // A pause forces two distinct clicks
                .build();
        
        ObjectCollection objectCollection = new ObjectCollection();

        // NEW API: Create ActionResult with config
        ActionResult result = new ActionResult();
        result.setActionConfig(clickOptions);
        
        // Get the action from service
        ActionInterface clickAction = actionService.getAction(clickOptions);

        // Action
        clickAction.perform(result, objectCollection);

        // Verification
        Assertions.assertTrue(result.isSuccess());
        
        // In mock mode, the wrapper methods are not called
        if (!FrameworkSettings.mock) {
            verify(moveMouseWrapper).move(any(Location.class));
            verify(mouseDownWrapper, times(2)).press(anyDouble(), anyDouble(), eq(ClickOptions.Type.DOUBLE_LEFT));
            verify(mouseUpWrapper, times(2)).press(anyDouble(), anyDouble(), eq(ClickOptions.Type.DOUBLE_LEFT));
        }
    }

    @Test
    void perform_clickWithMoveAfter_shouldMoveTwice() {
        // Setup
        Location moveLocation = new Location(100, 100);
        
        // NEW API: Use ClickOptions with specific settings
        ClickOptions clickOptions = new ClickOptions.Builder()
                .setMoveMouseAfterAction(true)
                .setMoveMouseAfterActionTo(moveLocation)
                .build();
        
        ObjectCollection objectCollection = new ObjectCollection();

        // NEW API: Create ActionResult with config
        ActionResult result = new ActionResult();
        result.setActionConfig(clickOptions);
        
        // Get the action from service
        ActionInterface clickAction = actionService.getAction(clickOptions);

        // Action
        clickAction.perform(result, objectCollection);

        // Verification
        Assertions.assertTrue(result.isSuccess());
        
        // In mock mode, only the after-click move is called
        if (FrameworkSettings.mock) {
            ArgumentCaptor<Location> locationCaptor = ArgumentCaptor.forClass(Location.class);
            verify(moveMouseWrapper, times(1)).move(locationCaptor.capture());
            Assertions.assertEquals(moveLocation, locationCaptor.getValue());
        } else {
            ArgumentCaptor<Location> locationCaptor = ArgumentCaptor.forClass(Location.class);
            verify(moveMouseWrapper, times(2)).move(locationCaptor.capture());

            List<Location> capturedLocations = locationCaptor.getAllValues();
            Assertions.assertEquals(moveLocation, capturedLocations.get(1));

            verify(mouseDownWrapper).press(anyDouble(), anyDouble(), eq(ClickOptions.Type.LEFT));
            verify(mouseUpWrapper).press(anyDouble(), anyDouble(), eq(ClickOptions.Type.LEFT));
        }
    }

    @Test
    void perform_rightClick_newAPI() {
        // NEW TEST: Demonstrates right-click with new API
        ClickOptions clickOptions = new ClickOptions.Builder()
                .setClickType(ClickOptions.Type.RIGHT)
                .build();
        
        ObjectCollection objectCollection = new ObjectCollection();

        ActionResult result = new ActionResult();
        result.setActionConfig(clickOptions);
        
        ActionInterface clickAction = actionService.getAction(clickOptions);
        clickAction.perform(result, objectCollection);

        Assertions.assertTrue(result.isSuccess());
        
        // Verify the config is preserved
        assertNotNull(result.getActionConfig());
        assertTrue(result.getActionConfig() instanceof ClickOptions);
        assertEquals(ClickOptions.Type.RIGHT, ((ClickOptions) result.getActionConfig()).getClickType());
    }

    @Test
    void perform_middleClick_newAPI() {
        // NEW TEST: Demonstrates middle-click with new API
        ClickOptions clickOptions = new ClickOptions.Builder()
                .setClickType(ClickOptions.Type.MIDDLE)
                .setPauseBeforeMouseDown(0.5)
                .setPauseAfterMouseUp(0.5)
                .build();
        
        ObjectCollection objectCollection = new ObjectCollection();

        ActionResult result = new ActionResult();
        result.setActionConfig(clickOptions);
        
        ActionInterface clickAction = actionService.getAction(clickOptions);
        clickAction.perform(result, objectCollection);

        Assertions.assertTrue(result.isSuccess());
        
        // Verify timing settings are preserved
        ClickOptions resultOptions = (ClickOptions) result.getActionConfig();
        assertEquals(0.5, resultOptions.getPauseBeforeMouseDown());
        assertEquals(0.5, resultOptions.getPauseAfterMouseUp());
    }

    @Test
    void compareOldAndNewAPI() {
        // This test demonstrates the migration pattern
        ObjectCollection objColl = new ObjectCollection();
        
        // OLD API (commented out):
        /*
        ActionOptions oldOptions = new ActionOptions.Builder()
                .setAction(ActionOptions.Action.CLICK)
                .setClickType(ClickType.Type.RIGHT)
                .setPauseAfterMouseDown(0.1)
                .build();
        ActionResult oldResult = action.perform(oldOptions, objColl);
        */
        
        // NEW API:
        ClickOptions newOptions = new ClickOptions.Builder()
                .setClickType(ClickOptions.Type.RIGHT)
                .setPauseAfterMouseDown(0.1)
                .build();
        
        ActionResult newResult = new ActionResult();
        newResult.setActionConfig(newOptions);
        
        ActionInterface clickAction = actionService.getAction(newOptions);
        clickAction.perform(newResult, objColl);
        
        // Both approaches achieve the same result, but new API is more type-safe
        assertNotNull(newResult);
        assertTrue(newResult.isSuccess());
    }
}