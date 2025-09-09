package io.github.jspinak.brobot.integration;

import io.github.jspinak.brobot.test.config.profile.IntegrationTestMinimalConfig;
import io.github.jspinak.brobot.test.IntegrationTestBase;
import org.springframework.test.context.ActiveProfiles;
import io.github.jspinak.brobot.action.Action;
import io.github.jspinak.brobot.action.ActionResult;
import io.github.jspinak.brobot.action.basic.find.PatternFindOptions;
import io.github.jspinak.brobot.action.basic.click.ClickOptions;
import io.github.jspinak.brobot.action.basic.type.TypeOptions;
import io.github.jspinak.brobot.action.composite.drag.DragOptions;
import io.github.jspinak.brobot.action.basic.mouse.MouseMoveOptions;
import io.github.jspinak.brobot.action.basic.mouse.MousePressOptions;
import io.github.jspinak.brobot.model.action.MouseButton;
import io.github.jspinak.brobot.action.ObjectCollection;
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
import org.junit.jupiter.api.condition.DisabledIfEnvironmentVariable;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Integration test for Action execution with various options and configurations.
 * Tests the complete action flow including pattern finding, clicking, typing, and dragging.
 */
@SpringBootTest(classes = IntegrationTestMinimalConfig.class)
@ActiveProfiles("integration-minimal")
@TestPropertySource(locations = "classpath:application-integration.properties")
@Slf4j
@DisabledIfEnvironmentVariable(named = "CI", matches = "true", disabledReason = "Integration test requires non-CI environment")
public class ActionExecutionIntegrationTest extends IntegrationTestBase {

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
    public void testClickWithOptions() {
        log.info("=== TESTING CLICK WITH OPTIONS ===");
        
        MousePressOptions mouseOptions = MousePressOptions.builder()
            .setButton(MouseButton.LEFT)
            .setPauseAfterMouseUp(0.5)
            .build();
        
        ClickOptions clickOptions = new ClickOptions.Builder()
            .setNumberOfClicks(2)  // Double-click
            .setPressOptions(mouseOptions)
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
            .setTypeDelay(0.1)
            .setPauseBeforeBegin(0.1)
            .setPauseAfterEnd(0.1)
            .build();
            
        ObjectCollection objects = new ObjectCollection.Builder()
            .withStrings(textToType)
            .build();
        ActionResult result = action.perform(typeOptions, objects);
        
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
            .setDelayBetweenMouseDownAndMove(0.5)
            .build();
            
        ObjectCollection objects = new ObjectCollection.Builder()
            .withLocations(fromLocation, toLocation)
            .build();
        ActionResult result = action.perform(dragOptions, objects);
        
        assertTrue(result.isSuccess(), "Drag action should succeed");
        log.info("Dragged from {} to {}", fromLocation, toLocation);
    }
    
    @Test
    public void testMoveAction() {
        log.info("=== TESTING MOVE ACTION ===");
        
        Location targetLocation = new Location(Positions.Name.MIDDLEMIDDLE);
        
        MouseMoveOptions moveOptions = new MouseMoveOptions.Builder()
            .setMoveMouseDelay(0.1f)
            .build();
            
        ObjectCollection objects = new ObjectCollection.Builder()
            .withLocations(targetLocation)
            .build();
        ActionResult result = action.perform(moveOptions, objects);
        
        assertTrue(result.isSuccess(), "Move action should succeed");
        assertFalse(result.getMatchList().isEmpty(), "Should have a match for move location");
        
        Match moveMatch = result.getMatchList().get(0);
        assertNotNull(moveMatch.getRegion(), "Move should have a target region");
        log.info("Moved to: X={}, Y={}", 
                moveMatch.getRegion().getX() + moveMatch.getRegion().getW()/2,
                moveMatch.getRegion().getY() + moveMatch.getRegion().getH()/2);
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
            
        ObjectCollection objects = new ObjectCollection.Builder()
            .withImages(repeatingPattern)
            .build();
        ActionResult result = action.perform(findAllOptions, objects);
        
        assertTrue(result.isSuccess(), "Find all should succeed");
        assertFalse(result.getMatchList().isEmpty(), "Should find at least one match");
        log.info("Found {} matches", result.getMatchList().size());
        
        // In mock mode, verify matches have different locations
        if (result.getMatchList().size() > 1) {
            Match first = result.getMatchList().get(0);
            Match second = result.getMatchList().get(1);
            assertNotEquals(first.getRegion().getX(), second.getRegion().getX(),
                          "Multiple matches should have different locations");
        }
    }
    
    @Test
    public void testActionWithSearchRegion() {
        log.info("=== TESTING ACTION WITH SEARCH REGION ===");
        
        Region searchRegion = Region.builder()
            .withRegion(50, 50, 200, 200)
            .build();
        
        StateImage imageWithSearchRegion = new StateImage.Builder()
            .setName("ButtonInRegion")
            .setSearchRegionForAllPatterns(searchRegion)
            .build();
        
        PatternFindOptions findInRegion = new PatternFindOptions.Builder()
            .build();
            
        ObjectCollection objects = new ObjectCollection.Builder()
            .withImages(imageWithSearchRegion)
            .build();
        ActionResult result = action.perform(findInRegion, objects);
        
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
            
        ObjectCollection objects = new ObjectCollection.Builder()
            .withImages(iconImage)
            .build();
        ActionResult result = action.perform(highlightOptions, objects);
        
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
            
        ObjectCollection findObjects = new ObjectCollection.Builder()
            .withStrings(textField)
            .build();
        ActionResult findResult = action.perform(findOptions, findObjects);
        assertTrue(findResult.isSuccess(), "Find should succeed");
        
        MousePressOptions pressOptions = MousePressOptions.builder()
            .setButton(MouseButton.LEFT)
            .build();
        ClickOptions clickOptions = new ClickOptions.Builder()
            .setPressOptions(pressOptions)
            .build();
            
        ObjectCollection clickObjects = new ObjectCollection.Builder()
            .withStrings(textField)
            .build();
        ActionResult clickResult = action.perform(clickOptions, clickObjects);
        assertTrue(clickResult.isSuccess(), "Click should succeed");
        
        TypeOptions typeOptions = new TypeOptions.Builder()
            .setTypeDelay(0.01)
            .build();
            
        ObjectCollection typeObjects = new ObjectCollection.Builder()
            .withStrings("Composite action test")
            .build();
        ActionResult typeResult = action.perform(typeOptions, typeObjects);
        assertTrue(typeResult.isSuccess(), "Type should succeed");
        
        log.info("Composite action sequence completed successfully");
    }
    
    @Test
    public void testActionWithTimeout() {
        log.info("=== TESTING ACTION WITH TIMEOUT ===");
        
        PatternFindOptions timeoutOptions = new PatternFindOptions.Builder()
            .setSearchDuration(2.0)  // 2 seconds timeout
            .build();
            
        long startTime = System.currentTimeMillis();
        ObjectCollection objects = new ObjectCollection.Builder()
            .withImages(buttonImage)
            .build();
        ActionResult result = action.perform(timeoutOptions, objects);
        long duration = System.currentTimeMillis() - startTime;
        
        assertTrue(result.isSuccess(), "Action should complete within timeout");
        assertTrue(duration < 2500, "Action should not exceed timeout significantly");
        log.info("Action completed in {} ms", duration);
    }
}