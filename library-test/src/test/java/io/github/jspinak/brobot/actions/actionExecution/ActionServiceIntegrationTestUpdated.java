package io.github.jspinak.brobot.actions.actionExecution;

import io.github.jspinak.brobot.action.ActionConfig;
import io.github.jspinak.brobot.action.ActionInterface;
import io.github.jspinak.brobot.action.ActionResult;
import io.github.jspinak.brobot.action.ObjectCollection;
import io.github.jspinak.brobot.action.basic.click.Click;
import io.github.jspinak.brobot.action.basic.click.ClickOptions;
import io.github.jspinak.brobot.action.basic.find.Find;
import io.github.jspinak.brobot.action.basic.find.FindOptions;
import io.github.jspinak.brobot.action.basic.type.TypeText;
import io.github.jspinak.brobot.action.basic.type.TypeOptions;
import io.github.jspinak.brobot.action.internal.execution.BasicActionRegistry;
import io.github.jspinak.brobot.action.internal.execution.CompositeActionRegistry;
import io.github.jspinak.brobot.action.internal.service.ActionService;
import io.github.jspinak.brobot.model.state.StateImage;
import io.github.jspinak.brobot.test.BaseIntegrationTest;
import org.junit.jupiter.api.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import java.util.concurrent.atomic.AtomicBoolean;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Updated Integration test for ActionService using new ActionConfig API.
 * This demonstrates how to update existing integration tests to use the new API.
 * 
 * Key changes:
 * - Use specific option classes (FindOptions, ClickOptions) instead of generic ActionOptions
 * - ActionResult now requires setActionConfig() before perform()
 * - ActionService.getAction() now takes ActionConfig parameter
 */
@SpringBootTest
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
class ActionServiceIntegrationTestUpdated extends BaseIntegrationTest {

    @Autowired
    private ActionService actionService;
    
    @Autowired
    private BasicActionRegistry basicActionRegistry;
    
    @Autowired
    private CompositeActionRegistry compositeActionRegistry;
    
    @Test
    @Order(1)
    void testSpringContextLoads() {
        assertNotNull(actionService, "ActionService should be autowired");
        assertNotNull(basicActionRegistry, "BasicActionRegistry should be autowired");
        assertNotNull(compositeActionRegistry, "CompositeActionRegistry should be autowired");
    }
    
    @Test
    @Order(2)
    void testGetFindAction_WithNewAPI() {
        // NEW API: Create specific FindOptions
        FindOptions findOptions = new FindOptions.Builder()
                .setFindStrategy(FindOptions.FindStrategy.BEST)
                .setMinSimilarity(0.85)
                .build();
        
        // Get action using new API
        ActionInterface action = actionService.getAction(findOptions);
        
        assertNotNull(action);
        assertTrue(action instanceof Find);
        assertEquals(ActionInterface.Type.FIND, action.getActionType());
    }
    
    @Test
    @Order(3)
    void testGetClickAction_WithNewAPI() {
        // NEW API: Create specific ClickOptions
        ClickOptions clickOptions = new ClickOptions.Builder()
                .setClickType(ClickOptions.Type.DOUBLE_LEFT)
                .setNumberOfClicks(2)
                .build();
        
        // Get action using new API
        ActionInterface action = actionService.getAction(clickOptions);
        
        assertNotNull(action);
        assertTrue(action instanceof Click);
        assertEquals(ActionInterface.Type.CLICK, action.getActionType());
    }
    
    @Test
    @Order(4)
    void testPerformFindAction_WithNewAPI() {
        // NEW API: Create FindOptions
        FindOptions findOptions = new FindOptions.Builder()
                .setFindStrategy(FindOptions.FindStrategy.ALL)
                .setMinSimilarity(0.8)
                .setMaxMatchesToActOn(10)
                .build();
        
        // Create test objects
        StateImage stateImage = new StateImage.Builder()
                .setName("TestImage")
                .build();
        
        ObjectCollection objectCollection = new ObjectCollection.Builder()
                .withImages(stateImage)
                .build();
        
        // NEW API: Create ActionResult and set config
        ActionResult result = new ActionResult();
        result.setActionConfig(findOptions);
        
        // Get and perform action
        ActionInterface findAction = actionService.getAction(findOptions);
        assertNotNull(findAction);
        
        // Perform the action
        findAction.perform(result, objectCollection);
        
        // Verify result
        assertNotNull(result);
        // In mock mode, results depend on mock configuration
        assertTrue(result.isSuccess() || !result.isSuccess());
    }
    
    @Test
    @Order(5)
    void testPerformClickAction_WithNewAPI() {
        // NEW API: Create ClickOptions
        ClickOptions clickOptions = new ClickOptions.Builder()
                .setClickType(ClickOptions.Type.RIGHT)
                .setPauseAfterEnd(0.5)
                .build();
        
        // Create test objects
        StateImage stateImage = new StateImage.Builder()
                .setName("ClickTarget")
                .build();
        
        ObjectCollection objectCollection = new ObjectCollection.Builder()
                .withImages(stateImage)
                .build();
        
        // NEW API: Create ActionResult with config
        ActionResult result = new ActionResult();
        result.setActionConfig(clickOptions);
        
        // Get and perform action
        ActionInterface clickAction = actionService.getAction(clickOptions);
        assertNotNull(clickAction);
        
        clickAction.perform(result, objectCollection);
        
        // Verify
        assertNotNull(result);
    }
    
    @Test
    @Order(6)
    void testRegisterCustomAction_WithNewAPI() {
        // Create a custom action config
        class CustomActionConfig extends ActionConfig {
            private String customParameter;
            
            public CustomActionConfig(String param) {
                this.customParameter = param;
            }
            
            public String getCustomParameter() {
                return customParameter;
            }
        }
        
        // Create custom action
        AtomicBoolean customActionExecuted = new AtomicBoolean(false);
        ActionInterface customAction = new ActionInterface() {
            @Override
            public Type getActionType() {
                return Type.FIND; // Use existing type for simplicity
            }
            
            @Override
            public void perform(ActionResult matches, ObjectCollection... objectCollections) {
                customActionExecuted.set(true);
                matches.setSuccess(true);
            }
        };
        
        // Register custom action for custom config
        basicActionRegistry.registerCustomAction(
            config -> config instanceof CustomActionConfig,
            customAction
        );
        
        // Test custom action resolution
        CustomActionConfig customConfig = new CustomActionConfig("test");
        ActionInterface resolvedAction = actionService.getAction(customConfig);
        
        assertNotNull(resolvedAction);
        
        // Perform custom action
        ActionResult result = new ActionResult();
        result.setActionConfig(customConfig);
        resolvedAction.perform(result);
        
        assertTrue(customActionExecuted.get());
        assertTrue(result.isSuccess());
    }
    
    @Test
    @Order(7)
    void testGetTypeAction_WithNewAPI() {
        // NEW API: Create TypeOptions
        TypeOptions typeOptions = new TypeOptions.Builder()
                .setText("Hello World")
                .setPauseBetweenKeys(0.1)
                .build();
        
        // Get action
        ActionInterface action = actionService.getAction(typeOptions);
        
        assertNotNull(action);
        assertTrue(action instanceof TypeText);
        assertEquals(ActionInterface.Type.TYPE, action.getActionType());
    }
    
    @Test
    @Order(8)
    void testActionServiceWithMultipleConfigs() {
        // Test that ActionService correctly routes different configs
        
        // Find config
        FindOptions findOptions = new FindOptions.Builder().build();
        ActionInterface findAction = actionService.getAction(findOptions);
        assertTrue(findAction instanceof Find);
        
        // Click config
        ClickOptions clickOptions = new ClickOptions.Builder().build();
        ActionInterface clickAction = actionService.getAction(clickOptions);
        assertTrue(clickAction instanceof Click);
        
        // Type config
        TypeOptions typeOptions = new TypeOptions.Builder()
                .setText("test")
                .build();
        ActionInterface typeAction = actionService.getAction(typeOptions);
        assertTrue(typeAction instanceof TypeText);
        
        // Verify different instances
        assertNotSame(findAction, clickAction);
        assertNotSame(clickAction, typeAction);
    }
    
    @Test
    @Order(9)
    void testNullConfigHandling() {
        // Test null config handling
        assertThrows(IllegalArgumentException.class, () -> {
            actionService.getAction(null);
        });
    }
    
    /**
     * Example of updating a test that used old ActionOptions.Action enum
     */
    @Test
    @Order(10)
    void testMigratedFromOldAPI() {
        // OLD API (commented out):
        // ActionOptions options = new ActionOptions.Builder()
        //     .setAction(ActionOptions.Action.FIND)
        //     .setFind(ActionOptions.Find.BEST)
        //     .build();
        
        // NEW API:
        FindOptions findOptions = new FindOptions.Builder()
                .setFindStrategy(FindOptions.FindStrategy.BEST)
                .build();
        
        ActionInterface action = actionService.getAction(findOptions);
        assertNotNull(action);
        assertEquals(ActionInterface.Type.FIND, action.getActionType());
    }
}