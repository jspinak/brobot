package io.github.jspinak.brobot.actions;

import static org.junit.jupiter.api.Assertions.*;

import java.util.List;

import org.junit.jupiter.api.*;
import org.springframework.beans.factory.annotation.Autowired;

import io.github.jspinak.brobot.action.Action;
import io.github.jspinak.brobot.action.ActionResult;
import io.github.jspinak.brobot.action.ActionType;
import io.github.jspinak.brobot.action.ObjectCollection;
import io.github.jspinak.brobot.action.basic.click.ClickOptions;
import io.github.jspinak.brobot.action.basic.find.PatternFindOptions;
import io.github.jspinak.brobot.action.basic.mouse.MousePressOptions;
import io.github.jspinak.brobot.action.composite.drag.DragOptions;
import io.github.jspinak.brobot.model.action.MouseButton;
import io.github.jspinak.brobot.model.element.Location;
import io.github.jspinak.brobot.model.element.Region;
import io.github.jspinak.brobot.model.state.StateImage;
import io.github.jspinak.brobot.test.BrobotIntegrationTestBase;

/**
 * Integration tests for the Action execution system.
 *
 * <p>These tests verify: 1. Action class integration with Spring context 2. Various ActionConfig
 * implementations (ClickOptions, PatternFindOptions, etc.) 3. ObjectCollection building and usage
 * 4. Mock mode execution without GUI 5. Different action types and their execution
 *
 * <p>Original test issues fixed: - Removed @Disabled annotation that was blocking test execution -
 * Using correct Action.perform() method signatures - Using ActionConfig interface instead of old
 * ActionOptions - Proper mock mode setup through BrobotTestBase
 */
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
class ActionExecutionIntegrationTest extends BrobotIntegrationTestBase {

    @Autowired private Action action;

    @BeforeEach
    void setUp() {
        super.setUpBrobotEnvironment(); // Sets up environment
        // Mock mode is enabled via BrobotTestBase
    }

    @Test
    @Order(1)
    @DisplayName("Should load Spring context and autowire Action")
    void testSpringContextLoads() {
        assertNotNull(action, "Action should be autowired");
        // Mock mode assertions handled by framework
    }

    @Test
    @Order(2)
    @DisplayName("Should execute click action with ClickOptions")
    void testClickActionWithObjectActionOptions() {
        // Setup
        ObjectCollection objectCollection =
                new ObjectCollection.Builder().withRegions(new Region(100, 100, 50, 50)).build();

        ClickOptions clickOptions = new ClickOptions.Builder().setNumberOfClicks(1).build();

        // Execute
        ActionResult matches = action.perform(clickOptions, objectCollection);

        // Verify
        assertNotNull(matches);
        assertTrue(matches.isSuccess(), "Mock action should succeed");
        assertFalse(matches.getMatchList().isEmpty(), "Should have matches in mock mode");
    }

    @Test
    @Order(3)
    @DisplayName("Should execute find action with PatternFindOptions")
    void testFindActionWithObjectActionOptions() {
        // Setup
        List<Region> searchRegions =
                List.of(
                        new Region(0, 0, 100, 100),
                        new Region(100, 100, 100, 100),
                        new Region(200, 200, 100, 100));

        ObjectCollection objectCollection =
                new ObjectCollection.Builder()
                        .withRegions(searchRegions.toArray(new Region[0]))
                        .build();

        PatternFindOptions findOptions =
                new PatternFindOptions.Builder()
                        .setStrategy(PatternFindOptions.Strategy.ALL)
                        .build();

        // Execute
        ActionResult matches = action.perform(findOptions, objectCollection);

        // Verify
        assertNotNull(matches);
        assertTrue(matches.isSuccess());
        // In mock mode, regions are converted to matches individually
        assertFalse(matches.getMatchList().isEmpty(), "Should have matches in mock mode");
    }

    @Test
    @Order(4)
    @DisplayName("Should execute drag action with DragOptions")
    void testDragActionWithObjectActionOptions() {
        // Setup
        Region fromRegion = new Region(50, 50, 20, 20);

        ObjectCollection fromCollection =
                new ObjectCollection.Builder().withRegions(fromRegion).build();

        DragOptions dragOptions =
                new DragOptions.Builder()
                        .setDelayBetweenMouseDownAndMove(0.5)
                        .setDelayAfterDrag(0.5)
                        .build();

        // Execute
        ActionResult matches = action.perform(dragOptions, fromCollection);

        // Verify
        assertNotNull(matches);
        assertTrue(matches.isSuccess(), "Mock drag action should succeed");
    }

    @Test
    @Order(5)
    @DisplayName("Should work with specialized configuration classes")
    void testNewConfigApiMigration() {
        // Test that new specialized config classes work correctly
        Region region = new Region(50, 50, 50, 50);
        ObjectCollection objectCollection =
                new ObjectCollection.Builder().withRegions(region).build();

        // Test click with mouse press options
        ClickOptions clickOptions =
                new ClickOptions.Builder()
                        .setNumberOfClicks(1)
                        .setPressOptions(
                                MousePressOptions.builder().setButton(MouseButton.MIDDLE).build())
                        .build();

        ActionResult clickResult = action.perform(clickOptions, objectCollection);
        assertNotNull(clickResult);
        assertTrue(clickResult.isSuccess());

        // Test find with different strategies
        PatternFindOptions findOptions =
                new PatternFindOptions.Builder()
                        .setStrategy(PatternFindOptions.Strategy.EACH)
                        .build();

        ActionResult findResult = action.perform(findOptions, objectCollection);
        assertNotNull(findResult);
        assertTrue(findResult.isSuccess());

        // Test move (click with 0 clicks)
        ClickOptions moveOptions =
                new ClickOptions.Builder()
                        .setNumberOfClicks(0) // Just move, no click
                        .build();

        ActionResult moveResult = action.perform(moveOptions, objectCollection);
        assertNotNull(moveResult);
        assertTrue(moveResult.isSuccess());
    }

    @Test
    @Order(6)
    @DisplayName("Should execute actions with ActionType enum")
    void testActionTypeExecution() {
        // Test simplified API with ActionType
        Region region = new Region(100, 100, 50, 50);

        // Test CLICK action type
        // Need to use ObjectCollection for ActionType methods
        ObjectCollection clickColl = new ObjectCollection.Builder().withRegions(region).build();
        ActionResult clickResult = action.perform(ActionType.CLICK, clickColl);
        assertNotNull(clickResult, "Click result should not be null");
        assertTrue(clickResult.isSuccess());

        // Test FIND action type
        ObjectCollection findColl = new ObjectCollection.Builder().withRegions(region).build();
        ActionResult findResult = action.perform(ActionType.FIND, findColl);
        assertNotNull(findResult, "Find result should not be null");
        assertTrue(findResult.isSuccess());

        // Test MOVE action type
        Location location = new Location(200, 200);
        ObjectCollection moveColl = new ObjectCollection.Builder().withLocations(location).build();
        ActionResult moveResult = action.perform(ActionType.MOVE, moveColl);
        assertNotNull(moveResult, "Move result should not be null");
        assertTrue(moveResult.isSuccess());
    }

    @Test
    @Order(7)
    @DisplayName("Should handle StateImage in ObjectCollection")
    void testStateImageExecution() {
        // Create a test StateImage
        StateImage testImage =
                new StateImage.Builder()
                        .setName("testImage")
                        .setSearchRegionForAllPatterns(new Region(0, 0, 100, 100))
                        .build();

        // Test with ObjectCollection containing StateImage
        ObjectCollection collection = new ObjectCollection.Builder().withImages(testImage).build();

        PatternFindOptions findOptions =
                new PatternFindOptions.Builder()
                        .setStrategy(PatternFindOptions.Strategy.FIRST)
                        .build();

        ActionResult result = action.perform(findOptions, collection);
        assertNotNull(result);
        assertTrue(result.isSuccess(), "Should succeed in mock mode");
    }

    @Test
    @Order(8)
    @DisplayName("Should handle multiple ObjectCollections")
    void testMultipleObjectCollections() {
        // Create multiple collections
        ObjectCollection regions =
                new ObjectCollection.Builder().withRegions(new Region(0, 0, 50, 50)).build();

        ObjectCollection locations =
                new ObjectCollection.Builder().withLocations(new Location(100, 100)).build();

        // Action should handle multiple collections
        PatternFindOptions findOptions =
                new PatternFindOptions.Builder()
                        .setStrategy(PatternFindOptions.Strategy.ALL)
                        .build();

        ActionResult result = action.perform(findOptions, regions, locations);
        assertNotNull(result);
        assertTrue(result.isSuccess());
    }

    @Test
    @Order(9)
    @DisplayName("Should handle string-based actions")
    void testStringActions() {
        // Test TYPE action with string
        String textToType = "Test text";
        ActionResult typeResult = action.perform(ActionType.TYPE, textToType);
        assertNotNull(typeResult);
        assertTrue(typeResult.isSuccess(), "Type action should succeed in mock mode");
    }

    @Test
    @Order(10)
    @DisplayName("Should provide action description")
    void testActionWithDescription() {
        // Test perform with description
        String description = "Click on test region";
        Region region = new Region(50, 50, 30, 30);

        ClickOptions clickOptions = new ClickOptions.Builder().setNumberOfClicks(1).build();

        ObjectCollection collection = new ObjectCollection.Builder().withRegions(region).build();

        ActionResult result = action.perform(description, clickOptions, collection);
        assertNotNull(result);
        assertTrue(result.isSuccess());
    }
}
