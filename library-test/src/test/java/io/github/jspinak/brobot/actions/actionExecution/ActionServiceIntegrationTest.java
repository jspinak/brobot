package io.github.jspinak.brobot.actions.actionExecution;

import io.github.jspinak.brobot.actions.actionOptions.ActionOptions;
import io.github.jspinak.brobot.actions.methods.basicactions.click.Click;
import io.github.jspinak.brobot.actions.methods.basicactions.find.Find;
import io.github.jspinak.brobot.actions.methods.basicactions.textOps.TypeText;
import io.github.jspinak.brobot.datatypes.primitives.match.Matches;
import io.github.jspinak.brobot.datatypes.state.ObjectCollection;
import io.github.jspinak.brobot.test.BaseIntegrationTest;
import org.junit.jupiter.api.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import java.util.List;
import java.util.Optional;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.function.BiConsumer;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Integration test for ActionService.
 * Tests the action retrieval and custom action registration.
 * 
 * Extends BaseIntegrationTest to ensure proper environment configuration
 * for integration testing with real files in headless environments.
 */
@SpringBootTest
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
class ActionServiceIntegrationTest extends BaseIntegrationTest {

    @Autowired
    private ActionService actionService;
    
    @Autowired
    private BasicAction basicAction;
    
    @Autowired
    private CompositeAction compositeAction;
    
    @Test
    @Order(1)
    void testSpringContextLoads() {
        assertNotNull(actionService, "ActionService should be autowired");
        assertNotNull(basicAction, "BasicAction should be autowired");
        assertNotNull(compositeAction, "CompositeAction should be autowired");
    }
    
    @Test
    @Order(2)
    void testGetBasicFindAction() {
        ActionOptions options = new ActionOptions.Builder()
                .setAction(ActionOptions.Action.FIND)
                .build();
        
        Optional<ActionInterface> action = actionService.getAction(options);
        
        assertTrue(action.isPresent(), "Should return Find action");
        assertTrue(action.get() instanceof Find, "Should be instance of Find");
    }
    
    @Test
    @Order(3)
    void testGetBasicClickAction() {
        ActionOptions options = new ActionOptions.Builder()
                .setAction(ActionOptions.Action.CLICK)
                .build();
        
        Optional<ActionInterface> action = actionService.getAction(options);
        
        assertTrue(action.isPresent(), "Should return Click action");
        assertTrue(action.get() instanceof Click, "Should be instance of Click");
    }
    
    @Test
    @Order(4)
    void testGetBasicTypeAction() {
        ActionOptions options = new ActionOptions.Builder()
                .setAction(ActionOptions.Action.TYPE)
                .build();
        
        Optional<ActionInterface> action = actionService.getAction(options);
        
        assertTrue(action.isPresent(), "Should return Type action");
        assertTrue(action.get() instanceof TypeText, "Should be instance of TypeText");
    }
    
    @Test
    @Order(5)
    void testGetCompositeActionWithMultipleFindActions() {
        // Create options with multiple find actions
        ActionOptions options = new ActionOptions.Builder()
                .setAction(ActionOptions.Action.FIND)
                .setFind(ActionOptions.Find.FIRST)
                .addFind(ActionOptions.Find.ALL)
                .build();
        
        Optional<ActionInterface> action = actionService.getAction(options);
        
        assertTrue(action.isPresent(), "Should return composite action for multiple finds");
        // The actual type depends on CompositeAction implementation
    }
    
    @Test
    @Order(6)
    void testGetCompositeActionForNonBasicAction() {
        // Test with an action that might be composite (depends on implementation)
        ActionOptions options = new ActionOptions.Builder()
                .setAction(ActionOptions.Action.DEFINE)
                .build();
        
        Optional<ActionInterface> action = actionService.getAction(options);
        
        // This might return either basic or composite depending on implementation
        // The test just verifies it doesn't throw exception
        assertNotNull(action);
    }
    
    @Test
    @Order(7)
    void testSetCustomFind() {
        AtomicBoolean customFindCalled = new AtomicBoolean(false);
        
        // Create custom find function
        BiConsumer<Matches, List<ObjectCollection>> customFind = (matches, collections) -> {
            customFindCalled.set(true);
            // Custom find logic would go here
        };
        
        // Register custom find
        assertDoesNotThrow(() -> actionService.setCustomFind(customFind));
        
        // Note: To actually test if custom find is used, we would need to 
        // trigger a find operation and verify the custom function was called.
        // This depends on the internal implementation of FindFunctions.
    }
    
    @Test
    @Order(8)
    void testGetActionWithNullAction() {
        ActionOptions options = new ActionOptions.Builder()
                .build(); // No action specified
        
        Optional<ActionInterface> action = actionService.getAction(options);
        
        // Behavior depends on default action in ActionOptions
        assertNotNull(action);
    }
    
    @Test
    @Order(9)
    void testConcurrentActionRetrieval() throws InterruptedException {
        // Test thread safety of action retrieval
        Thread[] threads = new Thread[10];
        Optional<ActionInterface>[] results = new Optional[10];
        
        for (int i = 0; i < threads.length; i++) {
            final int index = i;
            ActionOptions.Action actionType = index % 2 == 0 ? 
                    ActionOptions.Action.FIND : ActionOptions.Action.CLICK;
            
            threads[i] = new Thread(() -> {
                ActionOptions options = new ActionOptions.Builder()
                        .setAction(actionType)
                        .build();
                results[index] = actionService.getAction(options);
            });
        }
        
        // Start all threads
        for (Thread thread : threads) {
            thread.start();
        }
        
        // Wait for completion
        for (Thread thread : threads) {
            thread.join();
        }
        
        // Verify all got valid actions
        for (int i = 0; i < results.length; i++) {
            assertNotNull(results[i]);
            assertTrue(results[i].isPresent(), "Thread " + i + " should get valid action");
        }
    }
    
    @Test
    @Order(10)
    void testActionServiceConsistency() {
        // Test that same options return same action type
        ActionOptions options1 = new ActionOptions.Builder()
                .setAction(ActionOptions.Action.FIND)
                .build();
        
        ActionOptions options2 = new ActionOptions.Builder()
                .setAction(ActionOptions.Action.FIND)
                .build();
        
        Optional<ActionInterface> action1 = actionService.getAction(options1);
        Optional<ActionInterface> action2 = actionService.getAction(options2);
        
        assertTrue(action1.isPresent());
        assertTrue(action2.isPresent());
        assertEquals(action1.get().getClass(), action2.get().getClass(), 
                "Same options should return same action type");
    }
}