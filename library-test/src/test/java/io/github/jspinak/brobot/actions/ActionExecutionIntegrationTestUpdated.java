package io.github.jspinak.brobot.actions;

import io.github.jspinak.brobot.action.ActionInterface;
import io.github.jspinak.brobot.action.ActionResult;
import io.github.jspinak.brobot.action.ObjectCollection;
import io.github.jspinak.brobot.action.basic.click.ClickOptions;
import io.github.jspinak.brobot.action.basic.find.PatternFindOptions;
import io.github.jspinak.brobot.action.basic.type.TypeOptions;
import io.github.jspinak.brobot.action.basic.drag.DragOptions;
import io.github.jspinak.brobot.action.internal.service.ActionService;
import io.github.jspinak.brobot.config.FrameworkSettings;
import io.github.jspinak.brobot.model.element.Region;
import io.github.jspinak.brobot.model.element.Pattern;
import io.github.jspinak.brobot.model.element.StateImage;
import io.github.jspinak.brobot.config.ExecutionEnvironment;
import org.junit.jupiter.api.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.TestPropertySource;

import java.awt.image.BufferedImage;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Integration tests for the Action execution system using new ActionConfig API.
 * 
 * These tests verify the integration between:
 * - New ActionConfig API (ClickOptions, PatternFindOptions, etc.)
 * - ActionService and its dependencies
 * - Spring context and dependency injection
 * - Action execution pipeline
 * 
 * Key changes from old API:
 * - Uses specific config classes instead of generic ActionOptions
 * - ActionResult requires setActionConfig() before perform()
 * - ActionService.getAction() returns appropriate action implementation
 * - Mock mode configuration through FrameworkSettings and ExecutionEnvironment
 */
@SpringBootTest
@TestPropertySource(properties = {
    "spring.main.lazy-initialization=true"
})
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
class ActionExecutionIntegrationTestUpdated {

    @Autowired
    private ActionService actionService;
    
    @BeforeAll
    static void setUpHeadless() {
        System.setProperty("java.awt.headless", "true");
    }
    
    @BeforeEach
    void setUp() {
        // Configure for unit testing with mock mode
        ExecutionEnvironment env = ExecutionEnvironment.builder()
                .mockMode(true)
                .forceHeadless(true)
                .allowScreenCapture(false)
                .build();
        ExecutionEnvironment.setInstance(env);
        
        FrameworkSettings.mock = true;
    }
    
    @Test
    @Order(1)
    void testSpringContextLoads() {
        assertNotNull(actionService, "ActionService should be autowired");
    }
    
    @Test
    @Order(2)
    void testClickActionWithNewAPI() {
        // Setup
        ObjectCollection objectCollection = new ObjectCollection.Builder()
            .withRegions(new Region(100, 100, 50, 50))
            .build();
            
        // NEW API: Use ClickOptions
        ClickOptions clickOptions = new ClickOptions.Builder()
            .setClickType(ClickOptions.Type.LEFT)
            .setNumberOfClicks(1)
            .setPauseAfterEnd(0.5)
            .build();
        
        ActionResult result = new ActionResult();
        result.setActionConfig(clickOptions);
        
        // Execute
        ActionInterface clickAction = actionService.getAction(clickOptions);
        clickAction.perform(result, objectCollection);
        
        // Verify
        assertNotNull(result);
        assertTrue(result.isSuccess(), "Mock action should succeed");
        assertFalse(result.getMatchList().isEmpty(), "Should have matches in mock mode");
    }
    
    @Test
    @Order(3)
    void testFindActionWithNewAPI() {
        // Setup - create test pattern
        BufferedImage dummyImage = new BufferedImage(100, 100, BufferedImage.TYPE_INT_RGB);
        Pattern pattern = new Pattern.Builder()
                .setBufferedImage(dummyImage)
                .setName("TestPattern")
                .build();
                
        StateImage stateImage = new StateImage.Builder()
                .addPattern(pattern)
                .setName("TestImage")
                .build();
            
        ObjectCollection objectCollection = new ObjectCollection.Builder()
            .withImages(stateImage)
            .build();
            
        // NEW API: Use PatternFindOptions
        PatternFindOptions findOptions = new PatternFindOptions.Builder()
            .setStrategy(PatternFindOptions.Strategy.ALL)
            .setSimilarity(0.8)
            .setCaptureImage(true)
            .build();
        
        ActionResult result = new ActionResult();
        result.setActionConfig(findOptions);
        
        // Execute
        ActionInterface findAction = actionService.getAction(findOptions);
        findAction.perform(result, objectCollection);
        
        // Verify
        assertNotNull(result);
        assertNotNull(result.getMatchList());
        assertEquals(findOptions, result.getActionConfig());
    }
    
    @Test
    @Order(4)
    void testTypeActionWithNewAPI() {
        // Setup
        ObjectCollection objectCollection = new ObjectCollection.Builder()
            .withStrings("Hello World")
            .build();
            
        // NEW API: Use TypeOptions
        TypeOptions typeOptions = new TypeOptions.Builder()
            .setModifierDelay(0.1)
            .setPauseAfterEnd(0.5)
            .build();
        
        ActionResult result = new ActionResult();
        result.setActionConfig(typeOptions);
        
        // Execute
        ActionInterface typeAction = actionService.getAction(typeOptions);
        typeAction.perform(result, objectCollection);
        
        // Verify
        assertNotNull(result);
        assertTrue(result.isSuccess(), "Type action should succeed in mock mode");
    }
    
    @Test
    @Order(5)
    void testDragActionWithNewAPI() {
        // Setup
        Region fromRegion = new Region(100, 100, 50, 50);
        Region toRegion = new Region(300, 300, 50, 50);
        
        ObjectCollection objectCollection = new ObjectCollection.Builder()
            .withRegions(fromRegion, toRegion)
            .build();
            
        // NEW API: Use DragOptions
        DragOptions dragOptions = new DragOptions.Builder()
            .setFromIndex(0)
            .setToIndex(1)
            .setPauseAfterEnd(1.0)
            .build();
        
        ActionResult result = new ActionResult();
        result.setActionConfig(dragOptions);
        
        // Execute
        ActionInterface dragAction = actionService.getAction(dragOptions);
        dragAction.perform(result, objectCollection);
        
        // Verify
        assertNotNull(result);
        assertTrue(result.isSuccess(), "Drag action should succeed in mock mode");
    }
    
    @Test
    @Order(6)
    void testMultipleActionsInSequence() {
        // Setup
        BufferedImage dummyImage = new BufferedImage(100, 100, BufferedImage.TYPE_INT_RGB);
        Pattern pattern = new Pattern.Builder()
                .setBufferedImage(dummyImage)
                .setName("SequenceTestPattern")
                .build();
                
        StateImage stateImage = new StateImage.Builder()
                .addPattern(pattern)
                .setName("SequenceTestImage")
                .build();
            
        ObjectCollection findCollection = new ObjectCollection.Builder()
            .withImages(stateImage)
            .build();
            
        // 1. Find
        PatternFindOptions findOptions = new PatternFindOptions.Builder()
            .setStrategy(PatternFindOptions.Strategy.FIRST)
            .build();
            
        ActionResult findResult = new ActionResult();
        findResult.setActionConfig(findOptions);
        
        ActionInterface findAction = actionService.getAction(findOptions);
        findAction.perform(findResult, findCollection);
        
        assertTrue(findResult.isSuccess(), "Find should succeed");
        
        // 2. Click on found match
        ObjectCollection clickCollection = new ObjectCollection.Builder()
            .withMatches(findResult.getMatchList())
            .build();
            
        ClickOptions clickOptions = new ClickOptions.Builder()
            .setClickType(ClickOptions.Type.LEFT)
            .build();
            
        ActionResult clickResult = new ActionResult();
        clickResult.setActionConfig(clickOptions);
        
        ActionInterface clickAction = actionService.getAction(clickOptions);
        clickAction.perform(clickResult, clickCollection);
        
        assertTrue(clickResult.isSuccess(), "Click should succeed");
        
        // 3. Type
        ObjectCollection typeCollection = new ObjectCollection.Builder()
            .withStrings("Test sequence")
            .build();
            
        TypeOptions typeOptions = new TypeOptions.Builder()
            .build();
            
        ActionResult typeResult = new ActionResult();
        typeResult.setActionConfig(typeOptions);
        
        ActionInterface typeAction = actionService.getAction(typeOptions);
        typeAction.perform(typeResult, typeCollection);
        
        assertTrue(typeResult.isSuccess(), "Type should succeed");
    }
    
    @Test
    @Order(7)
    void testQuickFindPreset() {
        // Setup
        BufferedImage dummyImage = new BufferedImage(100, 100, BufferedImage.TYPE_INT_RGB);
        Pattern pattern = new Pattern.Builder()
                .setBufferedImage(dummyImage)
                .build();
                
        StateImage stateImage = new StateImage.Builder()
                .addPattern(pattern)
                .build();
            
        ObjectCollection objectCollection = new ObjectCollection.Builder()
            .withImages(stateImage)
            .build();
            
        // Use factory method for quick search
        PatternFindOptions quickFindOptions = PatternFindOptions.forQuickSearch();
        
        ActionResult result = new ActionResult();
        result.setActionConfig(quickFindOptions);
        
        // Execute
        ActionInterface findAction = actionService.getAction(quickFindOptions);
        findAction.perform(result, objectCollection);
        
        // Verify preset values
        assertEquals(PatternFindOptions.Strategy.FIRST, quickFindOptions.getStrategy());
        assertEquals(0.7, quickFindOptions.getSimilarity(), 0.001);
        assertFalse(quickFindOptions.isCaptureImage());
        assertEquals(1, quickFindOptions.getMaxMatchesToActOn());
    }
    
    @AfterEach
    void tearDown() {
        // Reset to default
        FrameworkSettings.mock = false;
    }
}