package io.github.jspinak.brobot.actions.actionExecution;

import io.github.jspinak.brobot.action.basic.find.PatternFindOptions;
import io.github.jspinak.brobot.action.basic.click.ClickOptions;
import io.github.jspinak.brobot.action.basic.type.TypeOptions;
import io.github.jspinak.brobot.action.ActionConfig;
import io.github.jspinak.brobot.action.basic.find.Find;
import io.github.jspinak.brobot.action.basic.click.Click;
import io.github.jspinak.brobot.action.basic.type.TypeText;
import io.github.jspinak.brobot.action.ActionResult;
import io.github.jspinak.brobot.action.ObjectCollection;
import org.junit.jupiter.api.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.TestPropertySource;

import java.util.List;
import java.util.Optional;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.function.BiConsumer;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Integration test for Action execution.
 * Tests the action execution with the new ActionConfig API.
 */
@SpringBootTest(classes = io.github.jspinak.brobot.BrobotTestApplication.class)
@TestPropertySource(properties = {
    "spring.main.lazy-initialization=true",
    "brobot.mock.enabled=true",
    "brobot.illustration.disabled=true",
    "brobot.scene.analysis.disabled=true"
})
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
class ActionServiceIntegrationTest {

    @Autowired
    private io.github.jspinak.brobot.action.Action action;
    
    @Autowired
    private Find find;
    
    @Autowired
    private Click click;
    
    @Autowired
    private TypeText typeText;
    
    @Test
    @Order(1)
    void testSpringContextLoads() {
        assertNotNull(action, "Action should be autowired");
        assertNotNull(find, "Find should be autowired");
        assertNotNull(click, "Click should be autowired");
        assertNotNull(typeText, "TypeText should be autowired");
    }
    
    @Test
    @Order(2)
    void testBasicFindAction() {
        PatternFindOptions options = new PatternFindOptions.Builder()
                .build();
        
        ObjectCollection collection = new ObjectCollection.Builder()
                .build();
        
        ActionResult result = action.perform(options, collection);
        
        assertNotNull(result, "Should return result");
    }
    
    @Test
    @Order(3)
    void testBasicClickAction() {
        ClickOptions options = new ClickOptions.Builder()
                .build();
        
        ObjectCollection collection = new ObjectCollection.Builder()
                .build();
        
        ActionResult result = action.perform(options, collection);
        
        assertNotNull(result, "Should return result");
    }
    
    @Test
    @Order(4)
    void testBasicTypeAction() {
        TypeOptions options = new TypeOptions.Builder()
                .build();
        
        ObjectCollection collection = new ObjectCollection.Builder()
                .withStrings("test")
                .build();
        
        ActionResult result = action.perform(options, collection);
        
        assertNotNull(result, "Should return result");
    }
    
    @Test
    @Order(5)
    void testFindActionWithDifferentStrategies() {
        // Test FIRST strategy
        PatternFindOptions firstOptions = new PatternFindOptions.Builder()
                .setStrategy(PatternFindOptions.Strategy.FIRST)
                .build();
        
        // Test ALL strategy
        PatternFindOptions allOptions = new PatternFindOptions.Builder()
                .setStrategy(PatternFindOptions.Strategy.ALL)
                .build();
        
        ObjectCollection collection = new ObjectCollection.Builder()
                .build();
        
        ActionResult firstResult = action.perform(firstOptions, collection);
        ActionResult allResult = action.perform(allOptions, collection);
        
        assertNotNull(firstResult, "Should return result for FIRST strategy");
        assertNotNull(allResult, "Should return result for ALL strategy");
    }
    
    @Test
    @Order(6)
    void testClickActionWithMultipleClicks() {
        // Test click with multiple clicks
        ClickOptions options = new ClickOptions.Builder()
                .setNumberOfClicks(2)
                .build();
        
        ObjectCollection collection = new ObjectCollection.Builder()
                .build();
        
        ActionResult result = action.perform(options, collection);
        
        assertNotNull(result, "Should return result for double click");
    }
    
    @Test
    @Order(7)
    void testActionWithPauseOptions() {
        // Test action with pause options
        PatternFindOptions options = new PatternFindOptions.Builder()
                .setPauseBeforeBegin(0.5)
                .setPauseAfterEnd(0.5)
                .build();
        
        ObjectCollection collection = new ObjectCollection.Builder()
                .build();
        
        ActionResult result = action.perform(options, collection);
        
        assertNotNull(result, "Should return result with pause options");
    }
    
    @Test
    @Order(8)
    void testActionWithEmptyCollection() {
        PatternFindOptions options = new PatternFindOptions.Builder()
                .build();
        
        ObjectCollection emptyCollection = new ObjectCollection();
        
        ActionResult result = action.perform(options, emptyCollection);
        
        assertNotNull(result, "Should handle empty collection gracefully");
        assertTrue(result.isEmpty(), "Result should be empty for empty collection");
    }
    
    @Test
    @Order(9)
    void testConcurrentActionExecution() throws InterruptedException {
        // Test thread safety of action execution
        Thread[] threads = new Thread[10];
        ActionResult[] results = new ActionResult[10];
        
        ObjectCollection collection = new ObjectCollection.Builder()
                .build();
        
        for (int i = 0; i < threads.length; i++) {
            final int index = i;
            ActionConfig config = index % 2 == 0 ? 
                    new PatternFindOptions.Builder().build() : 
                    new ClickOptions.Builder().build();
            
            threads[i] = new Thread(() -> {
                results[index] = action.perform(config, collection);
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
        
        // Verify all got valid results
        for (int i = 0; i < results.length; i++) {
            assertNotNull(results[i], "Thread " + i + " should get valid result");
        }
    }
    
    @Test
    @Order(10)
    void testActionConsistency() {
        // Test that same options return consistent results
        PatternFindOptions options = new PatternFindOptions.Builder()
                .build();
        
        ObjectCollection collection = new ObjectCollection.Builder()
                .build();
        
        ActionResult result1 = action.perform(options, collection);
        ActionResult result2 = action.perform(options, collection);
        
        assertNotNull(result1);
        assertNotNull(result2);
        // In mock mode, results should be consistent
        assertEquals(result1.isSuccess(), result2.isSuccess(), 
                "Same options should return consistent results in mock mode");
    }
}