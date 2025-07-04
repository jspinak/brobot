package io.github.jspinak.brobot.actions;

import io.github.jspinak.brobot.action.ActionOptions;
import io.github.jspinak.brobot.action.BasicActionRunner;
import io.github.jspinak.brobot.action.BrobotSettings;
import io.github.jspinak.brobot.config.ActionConfig;
import io.github.jspinak.brobot.datatypes.primitives.match.Match;
import io.github.jspinak.brobot.datatypes.primitives.region.Region;
import io.github.jspinak.brobot.datatypes.state.ObjectCollection;
import io.github.jspinak.brobot.services.Init;
import org.junit.jupiter.api.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.context.TestPropertySource;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

/**
 * Integration tests for the Action execution system.
 * 
 * These tests verify the integration between:
 * - ActionOptions and ActionConfig APIs (Dual API Support)
 * - BasicActionRunner and its dependencies
 * - Spring context and dependency injection
 * - Action execution pipeline
 */
@SpringBootTest
@TestPropertySource(properties = {
    "spring.main.lazy-initialization=true",
    "brobot.mock.enabled=true"
})
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
class ActionExecutionIntegrationTest {

    @Autowired
    private BasicActionRunner actionRunner;
    
    @Autowired
    private BrobotSettings brobotSettings;
    
    @MockBean
    private Init init;
    
    @BeforeEach
    void setUp() {
        // Configure mock mode
        when(init.setGlobalMock()).thenReturn(true);
        brobotSettings.mock = true;
    }
    
    @Test
    @Order(1)
    void testSpringContextLoads() {
        assertNotNull(actionRunner, "BasicActionRunner should be autowired");
        assertNotNull(brobotSettings, "BrobotSettings should be autowired");
    }
    
    @Test
    @Order(2)
    void testClickActionWithActionOptions() {
        // Setup
        ObjectCollection objectCollection = new ObjectCollection.Builder()
            .withRegions(new Region(100, 100, 50, 50))
            .build();
            
        ActionOptions actionOptions = new ActionOptions.Builder()
            .setAction(ActionOptions.Action.CLICK)
            .setClickUntil(ActionOptions.ClickUntil.OBJECTS_APPEAR)
            .setNumberOfActions(3)
            .build();
        
        // Execute
        Matches matches = actionRunner.perform(actionOptions, objectCollection);
        
        // Verify
        assertNotNull(matches);
        assertTrue(matches.isSuccess(), "Mock action should succeed");
        assertFalse(matches.getMatchList().isEmpty(), "Should have matches in mock mode");
    }
    
    @Test
    @Order(3)
    void testClickActionWithActionConfig() {
        // Setup
        ObjectCollection objectCollection = new ObjectCollection.Builder()
            .withRegions(new Region(200, 200, 100, 100))
            .build();
            
        ActionConfig actionConfig = ActionConfig.Builder.click()
            .setClickUntil(ActionOptions.ClickUntil.OBJECTS_VANISH)
            .setMaxWait(5.0)
            .setPauseAfterAction(0.5)
            .build();
        
        // Execute
        Matches matches = actionRunner.perform(actionConfig, objectCollection);
        
        // Verify
        assertNotNull(matches);
        assertTrue(matches.isSuccess(), "Mock action should succeed");
    }
    
    @Test
    @Order(4)
    void testFindActionWithMultipleRegions() {
        // Setup
        List<Region> searchRegions = List.of(
            new Region(0, 0, 100, 100),
            new Region(100, 100, 100, 100),
            new Region(200, 200, 100, 100)
        );
        
        ObjectCollection objectCollection = new ObjectCollection.Builder()
            .withRegions(searchRegions)
            .build();
            
        ActionConfig actionConfig = ActionConfig.Builder.find()
            .setFind(ActionOptions.Find.ALL)
            .setSearchRegions(searchRegions)
            .build();
        
        // Execute
        Matches matches = actionRunner.perform(actionConfig, objectCollection);
        
        // Verify
        assertNotNull(matches);
        assertTrue(matches.isSuccess());
        assertEquals(3, matches.getMatchList().size(), "Should find all regions in mock mode");
    }
    
    @Test
    @Order(5)
    void testDragActionWithActionOptions() {
        // Setup
        Region fromRegion = new Region(50, 50, 20, 20);
        Region toRegion = new Region(150, 150, 20, 20);
        
        ObjectCollection fromCollection = new ObjectCollection.Builder()
            .withRegions(fromRegion)
            .build();
            
        ObjectCollection toCollection = new ObjectCollection.Builder()
            .withRegions(toRegion)
            .build();
            
        ActionOptions actionOptions = new ActionOptions.Builder()
            .setAction(ActionOptions.Action.DRAG)
            .setDragToTargets(toCollection)
            .build();
        
        // Execute
        Matches matches = actionRunner.perform(actionOptions, fromCollection);
        
        // Verify
        assertNotNull(matches);
        assertTrue(matches.isSuccess(), "Mock drag action should succeed");
    }
    
    @Test
    @Order(6)
    void testTypeActionWithActionConfig() {
        // Setup
        Region targetRegion = new Region(100, 100, 200, 30);
        ObjectCollection objectCollection = new ObjectCollection.Builder()
            .withRegions(targetRegion)
            .build();
            
        ActionConfig actionConfig = ActionConfig.Builder.type()
            .setText("Hello Integration Test")
            .setTypePauses(true)
            .setPauseBeforeTyping(0.2)
            .build();
        
        // Execute
        Matches matches = actionRunner.perform(actionConfig, objectCollection);
        
        // Verify
        assertNotNull(matches);
        assertTrue(matches.isSuccess(), "Mock type action should succeed");
    }
    
    @Test
    @Order(7)
    void testMoveActionWithModifiers() {
        // Setup
        Region targetRegion = new Region(300, 300, 50, 50);
        ObjectCollection objectCollection = new ObjectCollection.Builder()
            .withRegions(targetRegion)
            .build();
            
        ActionConfig actionConfig = ActionConfig.Builder.move()
            .setModifiers("CTRL", "SHIFT")
            .setMoveMouseAfterAction(true)
            .build();
        
        // Execute
        Matches matches = actionRunner.perform(actionConfig, objectCollection);
        
        // Verify
        assertNotNull(matches);
        assertTrue(matches.isSuccess(), "Mock move action should succeed");
    }
    
    @Test
    @Order(8)
    void testVanishActionWithTimeout() {
        // Setup
        Region region = new Region(50, 50, 100, 100);
        ObjectCollection objectCollection = new ObjectCollection.Builder()
            .withRegions(region)
            .build();
            
        ActionConfig actionConfig = ActionConfig.Builder.vanish()
            .setMaxWait(3.0)
            .setPauseAfterAction(1.0)
            .build();
        
        // Execute
        Matches matches = actionRunner.perform(actionConfig, objectCollection);
        
        // Verify
        assertNotNull(matches);
        // In mock mode, vanish actions typically succeed
        assertTrue(matches.isSuccess(), "Mock vanish action should succeed");
    }
    
    @Test
    @Order(9)
    void testHighlightActionWithColor() {
        // Setup
        Region region = new Region(150, 150, 100, 100);
        ObjectCollection objectCollection = new ObjectCollection.Builder()
            .withRegions(region)
            .build();
            
        ActionConfig actionConfig = ActionConfig.Builder.highlight()
            .setHighlightColor("red")
            .setHighlightSeconds(2.0)
            .build();
        
        // Execute
        Matches matches = actionRunner.perform(actionConfig, objectCollection);
        
        // Verify
        assertNotNull(matches);
        assertTrue(matches.isSuccess(), "Mock highlight action should succeed");
    }
    
    @Test
    @Order(10)
    void testScrollActionWithDirection() {
        // Setup
        Region scrollRegion = new Region(200, 200, 400, 300);
        ObjectCollection objectCollection = new ObjectCollection.Builder()
            .withRegions(scrollRegion)
            .build();
            
        ActionConfig actionConfig = ActionConfig.Builder.scroll()
            .setScrollDirection(ActionOptions.ScrollDirection.DOWN)
            .setScrollAmount(5)
            .build();
        
        // Execute
        Matches matches = actionRunner.perform(actionConfig, objectCollection);
        
        // Verify
        assertNotNull(matches);
        assertTrue(matches.isSuccess(), "Mock scroll action should succeed");
    }
    
    @Test
    @Order(11)
    void testActionChainWithMultipleActions() {
        // Test that we can execute multiple actions in sequence
        Region region = new Region(100, 100, 100, 100);
        ObjectCollection objectCollection = new ObjectCollection.Builder()
            .withRegions(region)
            .build();
        
        // First action: Find
        ActionConfig findConfig = ActionConfig.Builder.find()
            .setFind(ActionOptions.Find.FIRST)
            .build();
        Matches findMatches = actionRunner.perform(findConfig, objectCollection);
        assertTrue(findMatches.isSuccess());
        
        // Second action: Click
        ActionConfig clickConfig = ActionConfig.Builder.click()
            .setNumberOfActions(2)
            .build();
        Matches clickMatches = actionRunner.perform(clickConfig, objectCollection);
        assertTrue(clickMatches.isSuccess());
        
        // Third action: Highlight
        ActionConfig highlightConfig = ActionConfig.Builder.highlight()
            .setHighlightSeconds(1.0)
            .build();
        Matches highlightMatches = actionRunner.perform(highlightConfig, objectCollection);
        assertTrue(highlightMatches.isSuccess());
    }
    
    @Test
    @Order(12)
    void testActionOptionsBackwardCompatibility() {
        // Verify that old ActionOptions API still works correctly
        Region region = new Region(50, 50, 50, 50);
        ObjectCollection objectCollection = new ObjectCollection.Builder()
            .withRegions(region)
            .build();
        
        // Test various ActionOptions configurations
        ActionOptions clickOptions = new ActionOptions.Builder()
            .setAction(ActionOptions.Action.CLICK)
            .setClickType(ActionOptions.ClickType.MID)
            .build();
        
        ActionOptions findOptions = new ActionOptions.Builder()
            .setAction(ActionOptions.Action.FIND)
            .setFind(ActionOptions.Find.EACH)
            .build();
        
        ActionOptions moveOptions = new ActionOptions.Builder()
            .setAction(ActionOptions.Action.MOVE)
            .setMoveMouseAfterAction(true)
            .build();
        
        // Execute all actions
        Matches clickMatches = actionRunner.perform(clickOptions, objectCollection);
        Matches findMatches = actionRunner.perform(findOptions, objectCollection);
        Matches moveMatches = actionRunner.perform(moveOptions, objectCollection);
        
        // Verify all succeed
        assertTrue(clickMatches.isSuccess());
        assertTrue(findMatches.isSuccess());
        assertTrue(moveMatches.isSuccess());
    }
}