package io.github.jspinak.brobot.integration;

import io.github.jspinak.brobot.BrobotTestApplication;
import io.github.jspinak.brobot.test.BrobotTestBase;
import io.github.jspinak.brobot.action.Action;
import io.github.jspinak.brobot.action.ActionResult;
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
            .setDefinedRegion(Region.builder()
                .withRegion(100, 100, 50, 50)
                .build())
            .build();
            
        textField = new StateString.Builder()
            .setName("TextField")
            .setString("Enter text here")
            .build();
        
        // Create and register test state
        testState = new State.Builder("TestActionState")
            .addImage(buttonImage)
            .addImage(iconImage)
            .addString(textField)
            .build();
            
        stateService.save(testState);
        stateMemory.addActiveState(testState.getId());
    }
    
    @Test
    public void testBasicFindAction() {
        log.info("=== TESTING BASIC FIND ACTION ===");
        
        PatternFindOptions findOptions = new PatternFindOptions.Builder()
            .setFind(Type.BEST)
            .build();
            
        ActionResult result = action.perform(findOptions, buttonImage);
        
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
            .setClickType(ClickOptions.ClickType.LEFT)
            .setMultiClick(2)  // Double-click
            .setPauseAfterClick(.5)
            .build();
            
        ActionResult result = action.perform(clickOptions, iconImage);
        
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
            .setText(textToType)
            .setPauseBeforeTyping(0.1)
            .setPauseAfterTyping(0.1)
            .build();
            
        ActionResult result = action.perform(typeOptions, textField);
        
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
            .setFromLocation(fromLocation)
            .setToLocation(toLocation)
            .setPauseDuringDrag(0.5)
            .build();
            
        ActionResult result = action.perform(dragOptions);
        
        assertTrue(result.isSuccess(), "Drag action should succeed");
        log.info("Dragged from {} to {}", fromLocation, toLocation);
    }
    
    @Test
    public void testMoveAction() {
        log.info("=== TESTING MOVE ACTION ===");
        
        Location targetLocation = new Location(Positions.Name.MIDDLEMIDDLE);
        
        MouseMoveOptions moveOptions = new MouseMoveOptions.Builder()
            .setLocation(targetLocation)
            .build();
            
        ActionResult result = action.perform(moveOptions);
        
        assertTrue(result.isSuccess(), "Move action should succeed");
        assertFalse(result.getMatchList().isEmpty(), "Should have a match for move location");
        
        Match moveMatch = result.getMatchList().get(0);
        assertNotNull(moveMatch.getRegion(), "Move should have a target region");
        log.info("Moved to: {}", moveMatch.getRegion().getCenter());
    }
    
    @Test
    public void testFindAllMatches() {
        log.info("=== TESTING FIND ALL MATCHES ===");
        
        // Create multiple similar images
        StateImage repeatingPattern = new StateImage.Builder()
            .setName("RepeatingPattern")
            .build();
            
        PatternFindOptions findAllOptions = new PatternFindOptions.Builder()
            .setFind(Type.ALL)
            .setMaxMatches(5)
            .build();
            
        ActionResult result = action.perform(findAllOptions, repeatingPattern);
        
        assertTrue(result.isSuccess(), "Find all should succeed");
        assertFalse(result.getMatchList().isEmpty(), "Should find at least one match");
        log.info("Found {} matches", result.getMatchList().size());
        
        // In mock mode, verify matches have different locations
        if (result.getMatchList().size() > 1) {
            Match first = result.getMatchList().get(0);
            Match second = result.getMatchList().get(1);
            assertNotEquals(first.getRegion().getCenter(), second.getRegion().getCenter(),
                          "Multiple matches should have different locations");
        }
    }
    
    @Test
    public void testActionWithSearchRegion() {
        log.info("=== TESTING ACTION WITH SEARCH REGION ===");
        
        Region searchRegion = Region.builder()
            .withRegion(50, 50, 200, 200)
            .build();
        
        PatternFindOptions findInRegion = new PatternFindOptions.Builder()
            .setSearchRegions(searchRegion)
            .build();
            
        ActionResult result = action.perform(findInRegion, buttonImage);
        
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
            .setHighlight(true)
            .setHighlightSeconds(1.0)
            .setHighlightColor("RED")
            .build();
            
        ActionResult result = action.perform(highlightOptions, iconImage);
        
        assertTrue(result.isSuccess(), "Highlight action should succeed");
        log.info("Highlighted {} for 1 second", iconImage.getName());
    }
    
    @Test
    public void testCompositeAction() {
        log.info("=== TESTING COMPOSITE ACTION ===");
        
        // Find, then click, then type
        PatternFindOptions findOptions = new PatternFindOptions.Builder()
            .setFind(Type.FIRST)
            .build();
            
        ActionResult findResult = action.perform(findOptions, textField);
        assertTrue(findResult.isSuccess(), "Find should succeed");
        
        ClickOptions clickOptions = new ClickOptions.Builder()
            .setClickType(ClickOptions.ClickType.LEFT)
            .build();
            
        ActionResult clickResult = action.perform(clickOptions, textField);
        assertTrue(clickResult.isSuccess(), "Click should succeed");
        
        TypeOptions typeOptions = new TypeOptions.Builder()
            .setText("Composite action test")
            .build();
            
        ActionResult typeResult = action.perform(typeOptions);
        assertTrue(typeResult.isSuccess(), "Type should succeed");
        
        log.info("Composite action sequence completed successfully");
    }
    
    @Test
    public void testActionWithTimeout() {
        log.info("=== TESTING ACTION WITH TIMEOUT ===");
        
        PatternFindOptions timeoutOptions = new PatternFindOptions.Builder()
            .setMaxWait(2.0)  // 2 seconds timeout
            .build();
            
        long startTime = System.currentTimeMillis();
        ActionResult result = action.perform(timeoutOptions, buttonImage);
        long duration = System.currentTimeMillis() - startTime;
        
        assertTrue(result.isSuccess(), "Action should complete within timeout");
        assertTrue(duration < 2500, "Action should not exceed timeout significantly");
        log.info("Action completed in {} ms", duration);
    }
}