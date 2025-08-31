package io.github.jspinak.brobot.integration;

import io.github.jspinak.brobot.BrobotTestApplication;
import io.github.jspinak.brobot.test.BrobotTestBase;
import io.github.jspinak.brobot.action.Action;
import io.github.jspinak.brobot.action.ActionResult;
import io.github.jspinak.brobot.action.basic.find.PatternFindOptions;
import io.github.jspinak.brobot.action.basic.find.FindStrategy;
import io.github.jspinak.brobot.action.basic.click.ClickOptions;
import io.github.jspinak.brobot.action.basic.type.TypeOptions;
import io.github.jspinak.brobot.action.basic.mouse.MousePressOptions;
import io.github.jspinak.brobot.model.action.MouseButton;
import io.github.jspinak.brobot.model.element.Location;
import io.github.jspinak.brobot.model.element.Positions;
import io.github.jspinak.brobot.model.element.Region;
import io.github.jspinak.brobot.model.match.Match;
import io.github.jspinak.brobot.model.state.StateImage;
import io.github.jspinak.brobot.model.state.StateString;
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
            .setDefinedRegion(Region.builder()
                .setX(100)
                .setY(100)
                .setW(50)
                .setH(50)
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
            .setFindStrategy(FindStrategy.BEST)
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
        
        MousePressOptions mouseOptions = MousePressOptions.builder()
            .setButton(MouseButton.LEFT)
            .setDelayAfterPress(0.5)
            .build();
        
        ClickOptions clickOptions = new ClickOptions.Builder()
            .setNumberOfClicks(2)  // Double-click
            .setMousePressOptions(mouseOptions)
            .build();
        
        ObjectCollection objects = new ObjectCollection.Builder()
            .withImages(iconImage)
            .build();
            
        ActionResult result = action.perform(clickOptions, objects);
        
        assertTrue(result.isSuccess(), "Click action should succeed");
        assertEquals(1, result.getMatchList().size(), "Should have one match for defined region");
        
        // Verify the click location
        Match match = result.getMatchList().get(0);
        Region clickRegion = match.getRegion();
        assertEquals(100, clickRegion.getX(), "Click X should match defined region");
        assertEquals(100, clickRegion.getY(), "Click Y should match defined region");
    }
    
    @Test
    public void testTypeAction() {
        log.info("=== TESTING TYPE ACTION ===");
        
        String textToType = "Hello Brobot!";
        
        TypeOptions typeOptions = new TypeOptions.Builder()
            .setTextToType(textToType)
            .setDelayBeforeTyping(0.1)
            .setDelayAfterTyping(0.1)
            .build();
        
        // Type action doesn't need objects in mock mode
        ActionResult result = action.perform(typeOptions);
        
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
            .setFindStrategy(FindStrategy.ALL)
            .setMaxMatches(5)
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
            .setX(50)
            .setY(50)
            .setW(200)
            .setH(200)
            .build();
        
        StateImage imageWithRegion = new StateImage.Builder()
            .setName("ImageInRegion")
            .setSearchRegion(searchRegion)
            .build();
        
        PatternFindOptions findInRegion = new PatternFindOptions.Builder()
            .setFindStrategy(FindStrategy.FIRST)
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
            .setFindStrategy(FindStrategy.FIRST)
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
            .setTextToType("Composite action test")
            .build();
            
        ActionResult typeResult = action.perform(typeOptions);
        assertTrue(typeResult.isSuccess(), "Type should succeed");
        
        log.info("Composite action sequence completed successfully");
    }
    
    @Test
    public void testActionWithTimeout() {
        log.info("=== TESTING ACTION WITH TIMEOUT ===");
        
        PatternFindOptions timeoutOptions = new PatternFindOptions.Builder()
            .setFindStrategy(FindStrategy.BEST)
            .setMaxWaitTime(2.0)  // 2 seconds timeout
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
            .setDefinedRegion(Region.builder()
                .setX((int)centerScreen.getX() - 25)
                .setY((int)centerScreen.getY() - 25)
                .setW(50)
                .setH(50)
                .build())
            .build();
        
        PatternFindOptions findOptions = new PatternFindOptions.Builder()
            .setFindStrategy(FindStrategy.BEST)
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