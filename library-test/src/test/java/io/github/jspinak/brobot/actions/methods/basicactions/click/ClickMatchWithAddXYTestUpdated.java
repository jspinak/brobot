package io.github.jspinak.brobot.actions.methods.basicactions.click;

import io.github.jspinak.brobot.config.FrameworkSettings;
import io.github.jspinak.brobot.action.ActionInterface;
import io.github.jspinak.brobot.action.ActionResult;
import io.github.jspinak.brobot.action.ObjectCollection;
import io.github.jspinak.brobot.action.basic.click.ClickOptions;
import io.github.jspinak.brobot.action.internal.service.ActionService;
import io.github.jspinak.brobot.model.element.Pattern;
import io.github.jspinak.brobot.model.element.Location;
import io.github.jspinak.brobot.model.element.Position;
import io.github.jspinak.brobot.model.state.StateImage;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import io.github.jspinak.brobot.testutils.TestPaths;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

/**
 * Updated tests for click functionality with position adjustments using new ActionConfig API.
 * Demonstrates migration from ActionOptions to ClickOptions.
 * 
 * Key changes:
 * - Uses ClickOptions instead of generic ActionOptions
 * - ActionResult requires setActionConfig() before perform()
 * - Uses ActionService to get the appropriate action
 * - Click type is now ClickOptions.Type
 */
@SpringBootTest
public class ClickMatchWithAddXYTestUpdated {

    @BeforeAll
    public static void setup() {
        System.setProperty("java.awt.headless", "false");
        FrameworkSettings.mock = true;
    }

    @Autowired
    ActionService actionService;

    /*
    Clicking should be a unit test. You don't want to actually click on the screen.
    Unit tests are performed by adding screenshots to BrobotSettings.screenshots.
     */
    @Test
    void setPositionWithClickOptions() {
        FrameworkSettings.screenshots.add(TestPaths.getScreenshotPath("floranext0"));
        StateImage topLeft = new StateImage.Builder()
                .addPattern(new Pattern.Builder()
                        .setFilename(TestPaths.getImagePath("topLeft"))
                        .setTargetPosition(new Position(100, 100))
                        .build())
                .build();
        ObjectCollection objColl = new ObjectCollection.Builder()
                .withImages(topLeft)
                .build();
        
        // NEW API: Use ClickOptions
        ClickOptions clickOptions = new ClickOptions.Builder()
                .setClickType(ClickOptions.Type.LEFT)
                .build();
        
        // Create ActionResult with config
        ActionResult result = new ActionResult();
        result.setActionConfig(clickOptions);
        
        // Get the action from service
        ActionInterface clickAction = actionService.getAction(clickOptions);
        assertNotNull(clickAction);
        
        // Perform the action
        clickAction.perform(result, objColl);
        
        Location loc1 = result.getMatchLocations().get(0);
        System.out.println(loc1);
        assertEquals(77, loc1.getCalculatedY());
    }

    @Test
    void addXYWithClickOptions() {
        FrameworkSettings.screenshots.add(TestPaths.getScreenshotPath("floranext0"));
        StateImage topLeft = new StateImage.Builder()
                .addPattern(new Pattern.Builder()
                        .setFilename(TestPaths.getImagePath("topLeft"))
                        .build())
                .build();
        ObjectCollection objColl = new ObjectCollection.Builder()
                .withImages(topLeft)
                .build();
        
        // NEW API: Use ClickOptions with offset adjustments
        ClickOptions clickOptions = new ClickOptions.Builder()
                .setClickType(ClickOptions.Type.LEFT)
                .setOffsetX(0)
                .setOffsetY(30)
                .build();
        
        ActionResult result = new ActionResult();
        result.setActionConfig(clickOptions);
        
        ActionInterface clickAction = actionService.getAction(clickOptions);
        clickAction.perform(result, objColl);
        
        Location loc1 = result.getMatchLocations().get(0);
        System.out.println(loc1);
        assertEquals(85, loc1.getCalculatedY());
    }
    
    @Test
    void testDifferentClickTypes() {
        FrameworkSettings.screenshots.add(TestPaths.getScreenshotPath("floranext0"));
        StateImage image = new StateImage.Builder()
                .addPattern(new Pattern.Builder()
                        .setFilename(TestPaths.getImagePath("topLeft"))
                        .build())
                .build();
        ObjectCollection objColl = new ObjectCollection.Builder()
                .withImages(image)
                .build();
        
        // Test RIGHT click
        ClickOptions rightClickOptions = new ClickOptions.Builder()
                .setClickType(ClickOptions.Type.RIGHT)
                .build();
        
        ActionResult rightClickResult = new ActionResult();
        rightClickResult.setActionConfig(rightClickOptions);
        
        ActionInterface rightClickAction = actionService.getAction(rightClickOptions);
        rightClickAction.perform(rightClickResult, objColl);
        
        assertNotNull(rightClickResult);
        assertEquals(ClickOptions.Type.RIGHT, 
            ((ClickOptions) rightClickResult.getActionConfig()).getClickType());
        
        // Test DOUBLE click
        ClickOptions doubleClickOptions = new ClickOptions.Builder()
                .setClickType(ClickOptions.Type.DOUBLE_LEFT)
                .build();
        
        ActionResult doubleClickResult = new ActionResult();
        doubleClickResult.setActionConfig(doubleClickOptions);
        
        ActionInterface doubleClickAction = actionService.getAction(doubleClickOptions);
        doubleClickAction.perform(doubleClickResult, objColl);
        
        assertNotNull(doubleClickResult);
        assertEquals(ClickOptions.Type.DOUBLE_LEFT, 
            ((ClickOptions) doubleClickResult.getActionConfig()).getClickType());
    }
    
    @Test
    void testClickWithPauses() {
        FrameworkSettings.screenshots.add(TestPaths.getScreenshotPath("floranext0"));
        StateImage image = new StateImage.Builder()
                .addPattern(new Pattern.Builder()
                        .setFilename(TestPaths.getImagePath("topLeft"))
                        .build())
                .build();
        ObjectCollection objColl = new ObjectCollection.Builder()
                .withImages(image)
                .build();
        
        // Test click with pauses
        ClickOptions clickWithPausesOptions = new ClickOptions.Builder()
                .setClickType(ClickOptions.Type.LEFT)
                .setPauseBeforeMouseDown(0.5)
                .setPauseAfterMouseDown(0.2)
                .setPauseAfterMouseUp(0.3)
                .build();
        
        ActionResult result = new ActionResult();
        result.setActionConfig(clickWithPausesOptions);
        
        ActionInterface clickAction = actionService.getAction(clickWithPausesOptions);
        clickAction.perform(result, objColl);
        
        assertNotNull(result);
        ClickOptions resultOptions = (ClickOptions) result.getActionConfig();
        assertEquals(0.5, resultOptions.getPauseBeforeMouseDown(), 0.001);
        assertEquals(0.2, resultOptions.getPauseAfterMouseDown(), 0.001);
        assertEquals(0.3, resultOptions.getPauseAfterMouseUp(), 0.001);
    }
    
    @Test
    void compareOldAndNewAPI() {
        // This test demonstrates the migration pattern
        
        // OLD API (commented out):
        /*
        ActionOptions oldOptions = new ActionOptions.Builder()
                .setAction(ActionOptions.Action.CLICK)
                .setAddX(10)
                .setAddY(20)
                .build();
        */
        
        // NEW API:
        ClickOptions newOptions = new ClickOptions.Builder()
                .setClickType(ClickOptions.Type.LEFT)
                .setOffsetX(10)
                .setOffsetY(20)
                .build();
        
        // Both achieve the same result, but new API is more type-safe
        assertNotNull(newOptions);
        assertEquals(10, newOptions.getOffsetX());
        assertEquals(20, newOptions.getOffsetY());
    }
}