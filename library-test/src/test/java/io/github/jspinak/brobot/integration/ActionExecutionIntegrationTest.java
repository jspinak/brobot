package io.github.jspinak.brobot.integration;

import io.github.jspinak.brobot.BrobotTestApplication;
import io.github.jspinak.brobot.test.BrobotTestBase;
import io.github.jspinak.brobot.action.Action;
import io.github.jspinak.brobot.action.ActionResult;
import io.github.jspinak.brobot.action.ObjectCollection;
import io.github.jspinak.brobot.action.basic.find.PatternFindOptions;
import io.github.jspinak.brobot.action.basic.find.FindStrategy;
import io.github.jspinak.brobot.action.basic.click.ClickOptions;
import io.github.jspinak.brobot.action.basic.type.TypeOptions;
import io.github.jspinak.brobot.action.composite.drag.DragOptions;
import io.github.jspinak.brobot.action.basic.mouse.MouseMoveOptions;
import io.github.jspinak.brobot.action.basic.highlight.HighlightOptions;
import io.github.jspinak.brobot.model.element.Location;
import io.github.jspinak.brobot.model.element.Position;
import io.github.jspinak.brobot.model.element.Positions;
import io.github.jspinak.brobot.model.element.Region;
import io.github.jspinak.brobot.model.element.SearchRegions;
import io.github.jspinak.brobot.model.match.Match;
import io.github.jspinak.brobot.model.state.StateImage;
import io.github.jspinak.brobot.model.state.StateString;
import io.github.jspinak.brobot.model.state.State;
import io.github.jspinak.brobot.navigation.service.StateService;
import io.github.jspinak.brobot.statemanagement.StateMemory;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.TestPropertySource;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Integration test for Action execution with various options and configurations.
 * Tests the complete action flow including pattern finding, clicking, typing, and dragging.
 */
@SpringBootTest(classes = BrobotTestApplication.class)
@TestPropertySource(properties = {
    "brobot.core.mock=true",
    "logging.level.io.github.jspinak.brobot=DEBUG"
})
@Slf4j
public class ActionExecutionIntegrationTest extends BrobotTestBase {

    @Autowired
    private Action action;
    
    @Autowired
    private StateService stateService;
    
    @Autowired
    private StateMemory stateMemory;
    
    private State testState;
    private StateImage buttonImage;
    private StateImage iconImage;
    private StateString textField;
    
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
            
        textField = new StateString.Builder()
            .setName("TextField")
            .setString("Enter text here")
            .build();
        
        // Create and register test state
        testState = new State.Builder("TestActionState")
            .withImages(buttonImage, iconImage)
            .withStrings(textField)
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
            
        ObjectCollection collection = new ObjectCollection.Builder()
            .withImages(buttonImage)
            .build();
        ActionResult result = action.perform(findOptions, collection);
        
        assertNotNull(result, "ActionResult should not be null");
        assertTrue(result.isSuccess(), "Find action should succeed in mock mode");
        assertFalse(result.getMatchList().isEmpty(), "Should have found matches");
        
        Match firstMatch = result.getMatchList().get(0);
        log.info("Found match at: {}", firstMatch.getRegion());
        assertNotNull(firstMatch.getRegion(), "Match should have a region");
    }
    
    @Test
    public void testClickWithOptions() {
        log.info("=== TESTING CLICK WITH OPTIONS ===");
        
        ClickOptions clickOptions = new ClickOptions.Builder()
            .setNumberOfClicks(2)  // Double-click
            .setPauseAfterEnd(.5)
            .build();
            
        ObjectCollection collection = new ObjectCollection.Builder()
            .withImages(iconImage)
            .build();
        ActionResult result = action.perform(clickOptions, collection);
        
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
            .setPauseBeforeBegin(0.1)
            .setPauseAfterEnd(0.1)
            .build();
            
        ObjectCollection collection = new ObjectCollection.Builder()
            .withStrings(textToType)
            .build();
        ActionResult result = action.perform(typeOptions, collection);
        
        assertTrue(result.isSuccess(), "Type action should succeed");
        assertEquals(textToType, result.getText(), "Typed text should match input");
        log.info("Successfully typed: {}", result.getText());
    }
    
    @Test
    public void testDragAction() {
        log.info("=== TESTING DRAG ACTION ===");
        
        Location fromLocation = new Location(100, 100);
        Location toLocation = new Location(200, 200);
        
        DragOptions dragOptions = new DragOptions.Builder()
            .setDelayBetweenMouseDownAndMove(0.1)
            .setDelayAfterDrag(0.5)
            .build();
            
        ObjectCollection fromCollection = new ObjectCollection.Builder()
            .withLocations(fromLocation)
            .build();
        ObjectCollection toCollection = new ObjectCollection.Builder()
            .withLocations(toLocation)
            .build();
        ActionResult result = action.perform(dragOptions, fromCollection, toCollection);
        
        assertTrue(result.isSuccess(), "Drag action should succeed");
        log.info("Dragged from {} to {}", fromLocation, toLocation);
    }
    
    @Test
    public void testMoveAction() {
        log.info("=== TESTING MOVE ACTION ===");
        
        Location targetLocation = new Location(Positions.Name.MIDDLEMIDDLE);
        
        MouseMoveOptions moveOptions = new MouseMoveOptions.Builder()
            .build();
            
        ObjectCollection collection = new ObjectCollection.Builder()
            .withLocations(targetLocation)
            .build();
        ActionResult result = action.perform(moveOptions, collection);
        
        assertTrue(result.isSuccess(), "Move action should succeed");
        assertFalse(result.getMatchList().isEmpty(), "Should have a match for move location");
        
        Match moveMatch = result.getMatchList().get(0);
        assertNotNull(moveMatch.getRegion(), "Move should have a target region");
        log.info("Moved to: {}", moveMatch.getRegion());
    }
    
    @Test
    public void testFindAllMatches() {
        log.info("=== TESTING FIND ALL MATCHES ===");
        
        // Create multiple similar images
        StateImage repeatingPattern = new StateImage.Builder()
            .setName("RepeatingPattern")
            .build();
            
        PatternFindOptions findAllOptions = new PatternFindOptions.Builder()
            .setStrategy(PatternFindOptions.Strategy.ALL)
            .setMaxMatchesToActOn(5)
            .build();
            
        ObjectCollection collection = new ObjectCollection.Builder()
            .withImages(repeatingPattern)
            .build();
        ActionResult result = action.perform(findAllOptions, collection);
        
        assertTrue(result.isSuccess(), "Find all should succeed");
        assertFalse(result.getMatchList().isEmpty(), "Should find at least one match");
        log.info("Found {} matches", result.getMatchList().size());
        
        // In mock mode, verify matches have different locations
        if (result.getMatchList().size() > 1) {
            Match first = result.getMatchList().get(0);
            Match second = result.getMatchList().get(1);
            assertNotEquals(first.getRegion().getLocation(), second.getRegion().getLocation(),
                          "Multiple matches should have different locations");
        }
    }
    
    @Test
    public void testActionWithSearchRegion() {
        log.info("=== TESTING ACTION WITH SEARCH REGION ===");
        
        Region searchRegion = Region.builder()
            .withRegion(50, 50, 200, 200)
            .build();
        
        SearchRegions searchRegions = new SearchRegions();
        searchRegions.addSearchRegions(searchRegion);
        
        PatternFindOptions findInRegion = new PatternFindOptions.Builder()
            .setSearchRegions(searchRegions)
            .build();
            
        ObjectCollection buttonCollection = new ObjectCollection.Builder()
            .withImages(buttonImage)
            .build();
        ActionResult result = action.perform(findInRegion, buttonCollection);
        
        assertTrue(result.isSuccess(), "Find in region should succeed");
        if (!result.getMatchList().isEmpty()) {
            Match match = result.getMatchList().get(0);
            Region matchRegion = match.getRegion();
            
            // Verify match is within search region
            assertTrue(matchRegion.getX() >= searchRegion.getX(), 
                      "Match X should be within search region");
            assertTrue(matchRegion.getY() >= searchRegion.getY(), 
                      "Match Y should be within search region");
            log.info("Found match within search region at: {}", matchRegion);
        }
    }
    
    @Test
    public void testHighlightAction() {
        log.info("=== TESTING HIGHLIGHT ACTION ===");
        
        HighlightOptions highlightOptions = new HighlightOptions.Builder()
            .setHighlightSeconds(1.0)
            .setHighlightColor("RED")
            .build();
            
        ObjectCollection iconCollection = new ObjectCollection.Builder()
            .withImages(iconImage)
            .build();
        ActionResult result = action.perform(highlightOptions, iconCollection);
        
        assertTrue(result.isSuccess(), "Highlight action should succeed");
        log.info("Highlighted {} for 1 second", iconImage.getName());
    }
    
    @Test
    public void testCompositeAction() {
        log.info("=== TESTING COMPOSITE ACTION ===");
        
        // Find, then click, then type
        PatternFindOptions findOptions = new PatternFindOptions.Builder()
            .setStrategy(PatternFindOptions.Strategy.FIRST)
            .build();
            
        ObjectCollection findCollection = new ObjectCollection.Builder()
            .withStrings(textField)
            .build();
        ActionResult findResult = action.perform(findOptions, findCollection);
        assertTrue(findResult.isSuccess(), "Find should succeed");
        
        ClickOptions clickOptions = new ClickOptions.Builder()
            .setNumberOfClicks(1)
            .build();
            
        ObjectCollection clickCollection = new ObjectCollection.Builder()
            .withStrings(textField)
            .build();
        ActionResult clickResult = action.perform(clickOptions, clickCollection);
        assertTrue(clickResult.isSuccess(), "Click should succeed");
        
        TypeOptions typeOptions = new TypeOptions.Builder()
            .build();
            
        ObjectCollection typeCollection = new ObjectCollection.Builder()
            .withStrings("Composite action test")
            .build();
        ActionResult typeResult = action.perform(typeOptions, typeCollection);
        assertTrue(typeResult.isSuccess(), "Type should succeed");
        
        log.info("Composite action sequence completed successfully");
    }
    
    @Test
    public void testActionWithTimeout() {
        log.info("=== TESTING ACTION WITH TIMEOUT ===");
        
        PatternFindOptions timeoutOptions = new PatternFindOptions.Builder()
            .setPauseAfterEnd(2.0)  // 2 seconds wait
            .build();
            
        long startTime = System.currentTimeMillis();
        ObjectCollection buttonTimeoutCollection = new ObjectCollection.Builder()
            .withImages(buttonImage)
            .build();
        ActionResult result = action.perform(timeoutOptions, buttonTimeoutCollection);
        long duration = System.currentTimeMillis() - startTime;
        
        assertTrue(result.isSuccess(), "Action should complete within timeout");
        assertTrue(duration < 2500, "Action should not exceed timeout significantly");
        log.info("Action completed in {} ms", duration);
    }
}