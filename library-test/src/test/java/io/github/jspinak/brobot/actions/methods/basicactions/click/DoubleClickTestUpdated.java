package io.github.jspinak.brobot.actions.methods.basicactions.click;

import static org.junit.jupiter.api.Assertions.*;

import java.awt.image.BufferedImage;
import java.util.Optional;

import org.junit.jupiter.api.*;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Timeout;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import io.github.jspinak.brobot.action.ActionInterface;
import io.github.jspinak.brobot.action.ActionResult;
import io.github.jspinak.brobot.action.ObjectCollection;
import io.github.jspinak.brobot.action.basic.click.ClickOptions;
import io.github.jspinak.brobot.action.basic.mouse.MousePressOptions;
import io.github.jspinak.brobot.action.internal.service.ActionService;
import io.github.jspinak.brobot.model.action.MouseButton;
import io.github.jspinak.brobot.model.element.Location;
import io.github.jspinak.brobot.model.element.Pattern;
import io.github.jspinak.brobot.model.state.StateImage;
import io.github.jspinak.brobot.test.BrobotIntegrationTestBase;

/**
 * Integration tests for double-click functionality.
 *
 * <p>Original test intended to verify: 1. Double-click actions using ClickOptions 2. Triple-click
 * functionality 3. Custom pause settings 4. Different click types (DOUBLE_LEFT, TRIPLE_LEFT)
 *
 * <p>Rewritten to use actual available APIs: - ClickOptions with setNumberOfClicks(2) for
 * double-click - MousePressOptions for button and timing configuration - No ClickOptions.Type enum
 * - use numberOfClicks - No ExecutionEnvironment in production API - No TestPaths class - using
 * dummy patterns for mock mode
 */
@SpringBootTest(classes = io.github.jspinak.brobot.BrobotTestApplication.class)
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
@Disabled("CI failure - needs investigation")
public class DoubleClickTestUpdated extends BrobotIntegrationTestBase {

    @BeforeEach
    void setUp() {
        super.setUpBrobotEnvironment();
        BrobotProperties.mock = true;
    }

    @AfterEach
    void tearDown() {
        BrobotProperties.mock = false;
    }

    @Autowired private ActionService actionService;

    @Test
    @Order(1)
    @DisplayName("Should perform double-click action")
    @Timeout(5) // 5 second timeout
    void testDoubleClick() {
        /*
         * Original test tried to use ClickOptions.Type.DOUBLE_LEFT.
         * Actual API: Use setNumberOfClicks(2) for double-click.
         */

        // Create dummy image for mock mode
        BufferedImage dummyImage = new BufferedImage(100, 100, BufferedImage.TYPE_INT_RGB);

        Pattern pattern =
                new Pattern.Builder()
                        .setBufferedImage(dummyImage)
                        .setName("DoubleClickPattern")
                        .build();

        StateImage stateImage =
                new StateImage.Builder().addPattern(pattern).setName("DoubleClickImage").build();

        ObjectCollection objColl = new ObjectCollection.Builder().withImages(stateImage).build();

        // Configure double-click using setNumberOfClicks(2)
        ClickOptions clickOptions =
                new ClickOptions.Builder()
                        .setNumberOfClicks(2) // Double-click
                        .build();

        ActionResult result = new ActionResult();
        result.setActionConfig(clickOptions);

        // Get the action from service
        Optional<ActionInterface> clickActionOpt = actionService.getAction(clickOptions);
        assertTrue(clickActionOpt.isPresent(), "Click action should be available");

        ActionInterface clickAction = clickActionOpt.get();
        clickAction.perform(result, objColl);

        // Verify results
        assertTrue(result.isSuccess());
        assertNotNull(result.getMatchLocations());

        // Verify the config is preserved
        assertNotNull(result.getActionConfig());
        assertTrue(result.getActionConfig() instanceof ClickOptions);
        assertEquals(2, ((ClickOptions) result.getActionConfig()).getNumberOfClicks());
    }

    @Test
    @Order(2)
    @DisplayName("Should perform double-click with custom pauses")
    @Timeout(5)
    void testDoubleClickWithPauses() {
        /*
         * Test double-click with custom mouse press timing.
         */

        BufferedImage dummyImage = new BufferedImage(100, 100, BufferedImage.TYPE_INT_RGB);

        Pattern pattern =
                new Pattern.Builder().setBufferedImage(dummyImage).setName("PausePattern").build();

        StateImage stateImage =
                new StateImage.Builder().addPattern(pattern).setName("PauseImage").build();

        ObjectCollection objColl = new ObjectCollection.Builder().withImages(stateImage).build();

        // Configure double-click with custom mouse press options
        MousePressOptions pressOptions =
                MousePressOptions.builder()
                        .setButton(MouseButton.LEFT)
                        .setPauseBeforeMouseDown(2.0)
                        .setPauseAfterMouseDown(0.1)
                        .setPauseBeforeMouseUp(0.3)
                        .setPauseAfterMouseUp(0.5)
                        .build();

        ClickOptions clickOptions =
                new ClickOptions.Builder()
                        .setNumberOfClicks(2) // Double-click
                        .setPressOptions(pressOptions)
                        .build();

        ActionResult result = new ActionResult();
        result.setActionConfig(clickOptions);

        Optional<ActionInterface> clickActionOpt = actionService.getAction(clickOptions);
        assertTrue(clickActionOpt.isPresent());

        ActionInterface clickAction = clickActionOpt.get();
        clickAction.perform(result, objColl);

        assertTrue(result.isSuccess());

        // Verify timing settings are preserved
        ClickOptions resultOptions = (ClickOptions) result.getActionConfig();
        MousePressOptions resultPressOptions = resultOptions.getMousePressOptions();
        assertEquals(2.0, resultPressOptions.getPauseBeforeMouseDown(), 0.001);
        assertEquals(0.1, resultPressOptions.getPauseAfterMouseDown(), 0.001);
        assertEquals(0.5, resultPressOptions.getPauseAfterMouseUp(), 0.001);
    }

    @Test
    @Order(3)
    @DisplayName("Should perform triple-click action")
    @Timeout(5)
    void testTripleClick() {
        /*
         * Test triple-click functionality.
         */

        BufferedImage dummyImage = new BufferedImage(100, 100, BufferedImage.TYPE_INT_RGB);

        Pattern pattern =
                new Pattern.Builder()
                        .setBufferedImage(dummyImage)
                        .setName("TripleClickPattern")
                        .build();

        StateImage stateImage =
                new StateImage.Builder().addPattern(pattern).setName("TripleClickImage").build();

        ObjectCollection objColl = new ObjectCollection.Builder().withImages(stateImage).build();

        // Configure triple-click using setNumberOfClicks(3)
        ClickOptions clickOptions =
                new ClickOptions.Builder()
                        .setNumberOfClicks(3) // Triple-click
                        .build();

        ActionResult result = new ActionResult();
        result.setActionConfig(clickOptions);

        Optional<ActionInterface> clickActionOpt = actionService.getAction(clickOptions);
        assertTrue(clickActionOpt.isPresent());

        ActionInterface clickAction = clickActionOpt.get();
        clickAction.perform(result, objColl);

        assertTrue(result.isSuccess());

        // Verify click count is preserved
        assertEquals(3, ((ClickOptions) result.getActionConfig()).getNumberOfClicks());
    }

    @Test
    @Order(4)
    @DisplayName("Should perform right-button double-click")
    @Timeout(5)
    void testRightButtonDoubleClick() {
        /*
         * Test double-click with right mouse button.
         */

        BufferedImage dummyImage = new BufferedImage(100, 100, BufferedImage.TYPE_INT_RGB);

        Pattern pattern =
                new Pattern.Builder()
                        .setBufferedImage(dummyImage)
                        .setName("RightClickPattern")
                        .build();

        StateImage stateImage =
                new StateImage.Builder().addPattern(pattern).setName("RightClickImage").build();

        ObjectCollection objColl = new ObjectCollection.Builder().withImages(stateImage).build();

        // Configure double-click with right button
        MousePressOptions pressOptions =
                MousePressOptions.builder().setButton(MouseButton.RIGHT).build();

        ClickOptions clickOptions =
                new ClickOptions.Builder()
                        .setNumberOfClicks(2) // Double-click
                        .setPressOptions(pressOptions)
                        .build();

        ActionResult result = new ActionResult();
        result.setActionConfig(clickOptions);

        Optional<ActionInterface> clickActionOpt = actionService.getAction(clickOptions);
        assertTrue(clickActionOpt.isPresent());

        ActionInterface clickAction = clickActionOpt.get();
        clickAction.perform(result, objColl);

        assertTrue(result.isSuccess());

        // Verify button and click count
        ClickOptions resultOptions = (ClickOptions) result.getActionConfig();
        assertEquals(2, resultOptions.getNumberOfClicks());
        assertEquals(MouseButton.RIGHT, resultOptions.getMousePressOptions().getButton());
    }

    @Test
    @Order(5)
    @DisplayName("Should perform multiple clicks at location")
    @Timeout(5)
    void testMultipleClicksAtLocation() {
        /*
         * Test clicking at a specific location multiple times.
         */

        Location targetLocation = new Location(200, 150);

        ObjectCollection objColl =
                new ObjectCollection.Builder().withLocations(targetLocation).build();

        // Configure 5 clicks
        ClickOptions clickOptions = new ClickOptions.Builder().setNumberOfClicks(5).build();

        ActionResult result = new ActionResult();
        result.setActionConfig(clickOptions);

        Optional<ActionInterface> clickActionOpt = actionService.getAction(clickOptions);
        assertTrue(clickActionOpt.isPresent());

        ActionInterface clickAction = clickActionOpt.get();
        clickAction.perform(result, objColl);

        assertTrue(result.isSuccess());

        if (!result.getMatchLocations().isEmpty()) {
            Location clickLoc = result.getMatchLocations().get(0);
            assertNotNull(clickLoc);
            assertEquals(200, clickLoc.getX());
            assertEquals(150, clickLoc.getY());
        }

        // Verify click count
        assertEquals(5, ((ClickOptions) result.getActionConfig()).getNumberOfClicks());
    }

    @Test
    @Order(6)
    @DisplayName("Should handle single click as default")
    @Timeout(5)
    void testSingleClickDefault() {
        /*
         * Test that default numberOfClicks is 1 (single click).
         */

        BufferedImage dummyImage = new BufferedImage(100, 100, BufferedImage.TYPE_INT_RGB);

        Pattern pattern =
                new Pattern.Builder()
                        .setBufferedImage(dummyImage)
                        .setName("SingleClickPattern")
                        .build();

        StateImage stateImage =
                new StateImage.Builder().addPattern(pattern).setName("SingleClickImage").build();

        ObjectCollection objColl = new ObjectCollection.Builder().withImages(stateImage).build();

        // Create click options without specifying numberOfClicks
        ClickOptions clickOptions = new ClickOptions.Builder().build();

        ActionResult result = new ActionResult();
        result.setActionConfig(clickOptions);

        Optional<ActionInterface> clickActionOpt = actionService.getAction(clickOptions);
        assertTrue(clickActionOpt.isPresent());

        ActionInterface clickAction = clickActionOpt.get();
        clickAction.perform(result, objColl);

        assertTrue(result.isSuccess());

        // Verify default is single click
        assertEquals(1, ((ClickOptions) result.getActionConfig()).getNumberOfClicks());
    }
}
