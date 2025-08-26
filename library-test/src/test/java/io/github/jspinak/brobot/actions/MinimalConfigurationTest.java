package io.github.jspinak.brobot.actions;

import io.github.jspinak.brobot.action.ActionInterface;
import io.github.jspinak.brobot.action.ActionResult;
import io.github.jspinak.brobot.action.ObjectCollection;
import io.github.jspinak.brobot.action.basic.find.Find;
import io.github.jspinak.brobot.action.basic.find.PatternFindOptions;
import io.github.jspinak.brobot.action.basic.click.Click;
import io.github.jspinak.brobot.action.basic.click.ClickOptions;
import io.github.jspinak.brobot.action.internal.service.ActionService;
import io.github.jspinak.brobot.config.MinimalTestConfiguration;
import io.github.jspinak.brobot.model.element.Region;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.ActiveProfiles;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Test that uses minimal configuration to avoid circular dependencies.
 * This demonstrates how to test Brobot actions without full Spring context.
 */
@SpringBootTest(classes = {MinimalTestConfiguration.class})
@ActiveProfiles("minimal-test")
public class MinimalConfigurationTest {

    @Autowired
    private Find find;
    
    @Autowired
    private Click click;
    
    @Autowired
    private ActionService actionService;
    
    @Test
    void testMinimalContextLoads() {
        assertNotNull(find, "Find should be autowired");
        assertNotNull(click, "Click should be autowired");
        assertNotNull(actionService, "ActionService should be autowired");
    }
    
    @Test
    void testFindWithMinimalConfiguration() {
        // Create test objects
        ObjectCollection objColl = new ObjectCollection.Builder()
            .withRegions(new Region(100, 100, 50, 50))
            .build();
            
        PatternFindOptions findOptions = new PatternFindOptions.Builder()
            .setStrategy(PatternFindOptions.Strategy.FIRST)
            .build();
            
        ActionResult result = new ActionResult();
        result.setActionConfig(findOptions);
        result.setActionLifecycle(new io.github.jspinak.brobot.action.internal.execution.ActionLifecycle(
            java.time.LocalDateTime.now(), 30.0));
        
        // Execute find
        find.perform(result, objColl);
        
        // Verify results
        assertTrue(result.isSuccess(), "Mock find should succeed");
        assertFalse(result.getMatchList().isEmpty(), "Should have matches");
        assertEquals(0.95, result.getMatchList().get(0).getScore(), 0.01);
    }
    
    @Test
    void testClickWithMinimalConfiguration() {
        // Create test objects
        ObjectCollection objColl = new ObjectCollection.Builder()
            .withRegions(new Region(200, 200, 30, 30))
            .build();
            
        ClickOptions clickOptions = new ClickOptions.Builder()
            .setNumberOfClicks(1)
            .build();
            
        ActionResult result = new ActionResult();
        result.setActionConfig(clickOptions);
        result.setActionLifecycle(new io.github.jspinak.brobot.action.internal.execution.ActionLifecycle(
            java.time.LocalDateTime.now(), 30.0));
        
        // Execute click
        click.perform(result, objColl);
        
        // Verify results
        assertTrue(result.isSuccess(), "Mock click should succeed");
        // Click success is verified through result.isSuccess()
    }
    
    @Test
    void testActionServiceWithMinimalConfiguration() {
        // Test that ActionService returns correct action types
        PatternFindOptions findOptions = new PatternFindOptions.Builder().build();
        Optional<ActionInterface> findAction = actionService.getAction(findOptions);
        assertTrue(findAction.isPresent(), "Should return Find action");
        
        ClickOptions clickOptions = new ClickOptions.Builder().build();
        Optional<ActionInterface> clickAction = actionService.getAction(clickOptions);
        assertTrue(clickAction.isPresent(), "Should return Click action");
    }
}