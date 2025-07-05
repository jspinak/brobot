package io.github.jspinak.brobot.actions;

import io.github.jspinak.brobot.action.Action;
import io.github.jspinak.brobot.action.basic.click.ClickOptions;
import io.github.jspinak.brobot.action.basic.find.PatternFindOptions;
import io.github.jspinak.brobot.action.composite.drag.DragOptions;
import io.github.jspinak.brobot.action.basic.mouse.MousePressOptions;
import io.github.jspinak.brobot.model.action.MouseButton;
import io.github.jspinak.brobot.action.ActionResult;
import io.github.jspinak.brobot.config.FrameworkSettings;
import io.github.jspinak.brobot.model.element.Region;
import io.github.jspinak.brobot.action.ObjectCollection;
import io.github.jspinak.brobot.config.FrameworkInitializer;
import org.junit.jupiter.api.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.context.TestPropertySource;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

import io.github.jspinak.brobot.model.state.StateImage;
import io.github.jspinak.brobot.BrobotTestApplication;

/**
 * Integration tests for the Action execution system.
 * 
 * These tests verify the integration between:
 * - ActionOptions API
 * - Action class and its dependencies
 * - Spring context and dependency injection
 * - Action execution pipeline
 */
@SpringBootTest(classes = BrobotTestApplication.class)
@TestPropertySource(properties = {
    "spring.main.lazy-initialization=true",
    "brobot.mock.enabled=true",
    "brobot.illustration.disabled=true",
    "brobot.scene.analysis.disabled=true"
})
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
class ActionExecutionIntegrationTest {

    @Autowired
    private Action action;
    
    @MockBean
    private FrameworkInitializer frameworkInitializer;
    
    @BeforeEach
    void setUp() {
        // Configure mock mode
        FrameworkSettings.mock = true;
        // Disable history saving to avoid memory issues with Mat cloning
        FrameworkSettings.drawFind = false;
        FrameworkSettings.drawClick = false;
    }
    
    @Test
    @Order(1)
    void testSpringContextLoads() {
        assertNotNull(action, "Action should be autowired");
        assertTrue(FrameworkSettings.mock, "Mock mode should be enabled");
    }
    
    @Test
    @Order(2)
    @Disabled("Temporarily disabled due to memory issues with Mat cloning")
    void testClickActionWithActionOptions() {
        // Setup
        ObjectCollection objectCollection = new ObjectCollection.Builder()
            .withRegions(new Region(100, 100, 50, 50))
            .build();
            
        ClickOptions clickOptions = new ClickOptions.Builder()
            .setNumberOfClicks(1)
            .build();
        
        // Execute
        ActionResult matches = action.perform(clickOptions, objectCollection);
        
        // Verify
        assertNotNull(matches);
        assertTrue(matches.isSuccess(), "Mock action should succeed");
        assertFalse(matches.getMatchList().isEmpty(), "Should have matches in mock mode");
    }
    
    @Test
    @Order(3)
    void testFindActionWithActionOptions() {
        // Setup
        List<Region> searchRegions = List.of(
            new Region(0, 0, 100, 100),
            new Region(100, 100, 100, 100),
            new Region(200, 200, 100, 100)
        );
        
        ObjectCollection objectCollection = new ObjectCollection.Builder()
            .withRegions(searchRegions.toArray(new Region[0]))
            .build();
            
        PatternFindOptions findOptions = new PatternFindOptions.Builder()
            .setStrategy(PatternFindOptions.Strategy.ALL)
            .build();
        
        // Execute
        ActionResult matches = action.perform(findOptions, objectCollection);
        
        // Verify
        assertNotNull(matches);
        assertTrue(matches.isSuccess());
        assertEquals(3, matches.getMatchList().size(), "Should find all regions in mock mode");
    }
    
    @Test
    @Order(4)
    void testDragActionWithActionOptions() {
        // Setup
        Region fromRegion = new Region(50, 50, 20, 20);
        
        ObjectCollection fromCollection = new ObjectCollection.Builder()
            .withRegions(fromRegion)
            .build();
            
        DragOptions dragOptions = new DragOptions.Builder()
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
    void testNewConfigApiMigration() {
        // Test that new specialized config classes work correctly
        Region region = new Region(50, 50, 50, 50);
        ObjectCollection objectCollection = new ObjectCollection.Builder()
            .withRegions(region)
            .build();
        
        // Test various specialized configuration classes
        ClickOptions clickOptions = new ClickOptions.Builder()
            .setNumberOfClicks(1)
            .setPressOptions(new MousePressOptions.Builder()
                .setButton(MouseButton.MIDDLE))
            .build();
        
        PatternFindOptions findOptions = new PatternFindOptions.Builder()
            .setStrategy(PatternFindOptions.Strategy.EACH)
            .build();
        
        // For move, use click with 0 clicks (just moves mouse)
        ClickOptions moveOptions = new ClickOptions.Builder()
            .setNumberOfClicks(0) // Just move, no click
            .build();
        
        // Execute all actions
        ActionResult clickMatches = action.perform(clickOptions, objectCollection);
        ActionResult findMatches = action.perform(findOptions, objectCollection);
        ActionResult moveMatches = action.perform(moveOptions, objectCollection);
        
        // Verify all succeed
        assertTrue(clickMatches.isSuccess());
        assertTrue(findMatches.isSuccess());
        assertTrue(moveMatches.isSuccess());
    }
}