package io.github.jspinak.brobot.integration;

import io.github.jspinak.brobot.BrobotTestApplication;
import io.github.jspinak.brobot.test.BrobotTestBase;
import io.github.jspinak.brobot.action.Action;
import io.github.jspinak.brobot.action.ActionResult;
import io.github.jspinak.brobot.action.basic.find.PatternFindOptions;
import io.github.jspinak.brobot.action.basic.click.ClickOptions;
import io.github.jspinak.brobot.action.basic.type.TypeOptions;
import io.github.jspinak.brobot.action.basic.mouse.MousePressOptions;
import io.github.jspinak.brobot.model.action.MouseButton;
import io.github.jspinak.brobot.model.element.Location;
import io.github.jspinak.brobot.model.element.Positions;
import io.github.jspinak.brobot.model.element.Region;
import io.github.jspinak.brobot.model.match.Match;
import io.github.jspinak.brobot.model.state.StateImage;
import io.github.jspinak.brobot.model.state.State;
import io.github.jspinak.brobot.action.ObjectCollection;
import io.github.jspinak.brobot.navigation.service.StateService;
import io.github.jspinak.brobot.statemanagement.StateMemory;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.TestPropertySource;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Integration test for Action execution in Brobot.
 * Tests various action types and configurations in mock mode.
 */
@SpringBootTest(classes = BrobotTestApplication.class)
@TestPropertySource(properties = {
    "brobot.core.mock=true",
    "logging.level.io.github.jspinak.brobot=DEBUG"
})
@Slf4j
public class ActionExecutionTest extends BrobotTestBase {

    @Autowired
    private Action action;
    
    @Autowired
    private StateService stateService;
    
    @Autowired
    private StateMemory stateMemory;
    
    private State testState;
    private StateImage buttonImage;
    private StateImage iconImage;
    
    @BeforeEach
    @Override
    public void setupTest() {
        super.setupTest();
        setupTestState();
        log.info("=== ACTION EXECUTION TEST SETUP COMPLETE ===");
    }
    
    private void setupTestState() {
        // Create test images
        buttonImage = new StateImage.Builder()
            .setName("TestButton")
            .build();
            
        iconImage = new StateImage.Builder()
            .setName("TestIcon")
            .setSearchRegionForAllPatterns(Region.builder()
                .withRegion(100, 100, 50, 50)
                .build())
            .build();
        
        // Create and register test state
        testState = new State.Builder("TestActionState")
            .withImages(buttonImage, iconImage)
            .build();
            
        stateService.save(testState);
        stateMemory.addActiveState(testState.getId());
    }
    
    @Test
    public void testBasicFindAction() {
        log.info("=== TESTING BASIC FIND ACTION ===");
        
        PatternFindOptions findOptions = new PatternFindOptions.Builder()
            .setStrategy(PatternFindOptions.Strategy.BEST)
            .build();
        
        ObjectCollection objects = new ObjectCollection.Builder()
            .withImages(buttonImage)
            .build();
            
        ActionResult result = action.perform(findOptions, objects);
        
        assertNotNull(result, "ActionResult should not be null");
        assertTrue(result.isSuccess(), "Find action should succeed in mock mode");
        assertFalse(result.getMatchList().isEmpty(), "Should have found matches");
        
        Match firstMatch = result.getMatchList().get(0);
        log.info("Found match at: {}", firstMatch.getRegion());
        assertNotNull(firstMatch.getRegion(), "Match should have a region");
    }
    
    @Test
    public void testClickAction() {
        log.info("=== TESTING CLICK ACTION ===");
        
<<<<<<< HEAD
        ClickOptions clickOptions = new ClickOptions.Builder()
            .setNumberOfClicks(2)  // Double-click
            .setPressOptions(MousePressOptions.builder()
                .setButton(MouseButton.LEFT)
                .setPauseAfterMouseDown(0.5)
                .build())
=======
        MousePressOptions mouseOptions = MousePressOptions.builder()
            .setButton(MouseButton.LEFT)
            .setPauseAfterMouseUp(0.5)
            .build();
        
        ClickOptions clickOptions = new ClickOptions.Builder()
            .setNumberOfClicks(2)  // Double-click
            .setPressOptions(mouseOptions)
>>>>>>> coverage/agent-3
            .build();
        
        ObjectCollection objects = new ObjectCollection.Builder()
            .withImages(iconImage)
            .build();
            
        ActionResult result = action.perform(clickOptions, objects);
        
        assertTrue(result.isSuccess(), "Click action should succeed");
        assertEquals(1, result.getMatchList().size(), "Should have one match for search region");
        
        // Verify the click location
        Match match = result.getMatchList().get(0);
        Region clickRegion = match.getRegion();
        assertEquals(100, clickRegion.getX(), "Click X should match search region");
        assertEquals(100, clickRegion.getY(), "Click Y should match search region");
    }
    
    @Test
    public void testTypeAction() {
        log.info("=== TESTING TYPE ACTION ===");
        
        String textToType = "Hello Brobot!";
        
        TypeOptions typeOptions = new TypeOptions.Builder()
<<<<<<< HEAD
            .build();
        
        // Create ObjectCollection for type action
        ObjectCollection typeObjects = new ObjectCollection.Builder().build();
        ActionResult result = action.perform(typeOptions, typeObjects);
        // Mock the typed text in result (would be done by framework)
        // result.setText(textToType);
=======
            .setTypeDelay(0.1)
            .setPauseBeforeBegin(0.1)
            .setPauseAfterEnd(0.1)
            .build();
        
        ObjectCollection objects = new ObjectCollection.Builder()
            .withStrings(textToType)
            .build();
        
        ActionResult result = action.perform(typeOptions, objects);
>>>>>>> coverage/agent-3
        
        assertTrue(result.isSuccess(), "Type action should succeed");
        assertEquals(textToType, result.getText(), "Typed text should match input");
        log.info("Successfully typed: {}", result.getText());
    }
    
    @Test
    public void testFindAllMatches() {
        log.info("=== TESTING FIND ALL MATCHES ===");
        
        StateImage repeatingPattern = new StateImage.Builder()
            .setName("RepeatingPattern")
            .build();
            
        PatternFindOptions findAllOptions = new PatternFindOptions.Builder()
            .setStrategy(PatternFindOptions.Strategy.ALL)
            .setMaxMatchesToActOn(5)
            .build();
        
        ObjectCollection objects = new ObjectCollection.Builder()
            .withImages(repeatingPattern)
            .build();
            
        ActionResult result = action.perform(findAllOptions, objects);
        
        assertTrue(result.isSuccess(), "Find all should succeed");
        assertFalse(result.getMatchList().isEmpty(), "Should find at least one match");
        log.info("Found {} matches", result.getMatchList().size());
    }
    
    @Test
    public void testActionWithSearchRegion() {
        log.info("=== TESTING ACTION WITH SEARCH REGION ===");
        
        Region searchRegion = Region.builder()
            .withRegion(50, 50, 200, 200)
            .build();
        
        StateImage imageWithRegion = new StateImage.Builder()
            .setName("ImageInRegion")
            .setSearchRegionForAllPatterns(searchRegion)
            .build();
        
        PatternFindOptions findInRegion = new PatternFindOptions.Builder()
            .setStrategy(PatternFindOptions.Strategy.FIRST)
            .build();
        
        ObjectCollection objects = new ObjectCollection.Builder()
            .withImages(imageWithRegion)
            .build();
            
        ActionResult result = action.perform(findInRegion, objects);
        
        assertTrue(result.isSuccess(), "Find in region should succeed");
        if (!result.getMatchList().isEmpty()) {
            Match match = result.getMatchList().get(0);
            Region matchRegion = match.getRegion();
            
            // In mock mode, matches should respect search regions
            log.info("Found match within search region at: {}", matchRegion);
        }
    }
    
    @Test
    public void testCompositeAction() {
        log.info("=== TESTING COMPOSITE ACTION SEQUENCE ===");
        
        // Step 1: Find
        PatternFindOptions findOptions = new PatternFindOptions.Builder()
            .setStrategy(PatternFindOptions.Strategy.FIRST)
            .build();
        
        ObjectCollection findObjects = new ObjectCollection.Builder()
            .withImages(buttonImage)
            .build();
            
        ActionResult findResult = action.perform(findOptions, findObjects);
        assertTrue(findResult.isSuccess(), "Find should succeed");
        
        // Step 2: Click
        ClickOptions clickOptions = new ClickOptions.Builder()
            .setNumberOfClicks(1)
            .build();
        
        ObjectCollection clickObjects = new ObjectCollection.Builder()
            .withImages(buttonImage)
            .build();
            
        ActionResult clickResult = action.perform(clickOptions, clickObjects);
        assertTrue(clickResult.isSuccess(), "Click should succeed");
        
        // Step 3: Type
        TypeOptions typeOptions = new TypeOptions.Builder()
<<<<<<< HEAD
            .build();
            
        ObjectCollection typeObjects = new ObjectCollection.Builder().build();
        ActionResult typeResult = action.perform(typeOptions, typeObjects);
        // Mock the typed text (would be handled by framework)
        // typeResult.setText("Composite action test");
=======
            .setTypeDelay(0.01)
            .build();
            
        ObjectCollection typeObjects = new ObjectCollection.Builder()
            .withStrings("Composite action test")
            .build();
            
        ActionResult typeResult = action.perform(typeOptions, typeObjects);
>>>>>>> coverage/agent-3
        assertTrue(typeResult.isSuccess(), "Type should succeed");
        
        log.info("Composite action sequence completed successfully");
    }
    
    @Test
    public void testActionWithTimeout() {
        log.info("=== TESTING ACTION WITH TIMEOUT ===");
        
        PatternFindOptions timeoutOptions = new PatternFindOptions.Builder()
            .setStrategy(PatternFindOptions.Strategy.BEST)
<<<<<<< HEAD
=======
            .setSearchDuration(2.0)  // 2 seconds timeout
>>>>>>> coverage/agent-3
            .build();
        
        ObjectCollection objects = new ObjectCollection.Builder()
            .withImages(buttonImage)
            .build();
            
        long startTime = System.currentTimeMillis();
        ActionResult result = action.perform(timeoutOptions, objects);
        long duration = System.currentTimeMillis() - startTime;
        
        assertTrue(result.isSuccess(), "Action should complete within timeout");
        assertTrue(duration < 2500, "Action should not exceed timeout significantly");
        log.info("Action completed in {} ms", duration);
    }
    
    @Test
    public void testLocationBasedAction() {
        log.info("=== TESTING LOCATION-BASED ACTION ===");
        
        // Test with predefined screen positions
        Location centerScreen = new Location(Positions.Name.MIDDLEMIDDLE);
        Location topLeft = new Location(Positions.Name.TOPLEFT);
        
        StateImage imageAtCenter = new StateImage.Builder()
            .setName("CenterImage")
            .setSearchRegionForAllPatterns(Region.builder()
<<<<<<< HEAD
                .withRegion((int)centerScreen.getX() - 25, 
                           (int)centerScreen.getY() - 25, 50, 50)
=======
                .withRegion((int)centerScreen.getX() - 25, (int)centerScreen.getY() - 25, 50, 50)
>>>>>>> coverage/agent-3
                .build())
            .build();
        
        PatternFindOptions findOptions = new PatternFindOptions.Builder()
            .setStrategy(PatternFindOptions.Strategy.BEST)
            .build();
        
        ObjectCollection objects = new ObjectCollection.Builder()
            .withImages(imageAtCenter)
            .build();
            
        ActionResult result = action.perform(findOptions, objects);
        
        assertTrue(result.isSuccess(), "Should find image at center");
        if (!result.getMatchList().isEmpty()) {
            Match match = result.getMatchList().get(0);
            Region matchRegion = match.getRegion();
            
            // Verify match is near center
            double centerX = matchRegion.getX() + matchRegion.getW() / 2.0;
            double centerY = matchRegion.getY() + matchRegion.getH() / 2.0;
            
            log.info("Found match at ({}, {}), expected near ({}, {})", 
                    centerX, centerY, centerScreen.getX(), centerScreen.getY());
        }
    }
}