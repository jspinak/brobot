package io.github.jspinak.brobot.action.composite.multiple.actions;

import io.github.jspinak.brobot.BrobotTestApplication;
import io.github.jspinak.brobot.action.ActionResult;
import io.github.jspinak.brobot.action.ObjectCollection;
import io.github.jspinak.brobot.action.basic.click.ClickOptions;
import io.github.jspinak.brobot.action.basic.find.PatternFindOptions;
import io.github.jspinak.brobot.action.basic.type.TypeOptions;
import io.github.jspinak.brobot.config.ExecutionEnvironment;
import io.github.jspinak.brobot.model.element.Pattern;
import io.github.jspinak.brobot.model.element.Location;
import io.github.jspinak.brobot.test.BrobotIntegrationTestBase;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Tests for MultipleActions and MultipleBasicActions with ActionConfig API.
 * Verifies that composite action sequences work correctly with the new API.
 */
@SpringBootTest(classes = BrobotTestApplication.class)
public class MultipleActionsTestUpdated extends BrobotIntegrationTestBase {

    @BeforeAll
    public static void setupHeadlessMode() {
        System.setProperty("java.awt.headless", "true");
    }

    @BeforeEach
    @Override
    protected void setUpBrobotEnvironment() {
        // Configure for unit testing - mock mode
        ExecutionEnvironment env = ExecutionEnvironment.builder()
                .mockMode(true)
                .forceHeadless(true)
                .allowScreenCapture(false)
                .build();
        ExecutionEnvironment.setInstance(env);
    }

    @Autowired
    private MultipleActions multipleActions;

    @Autowired
    private MultipleBasicActions multipleBasicActions;

    @Test
    void testMultipleActionsWithActionConfig() {
        // Create a sequence: Find -> Click -> Type
        MultipleActionsObjectV2 workflow = new MultipleActionsObjectV2();
        
        // Add find action
        PatternFindOptions findOptions = new PatternFindOptions.Builder()
                .setStrategy(PatternFindOptions.Strategy.FIRST)
                .build();
        Pattern pattern = new Pattern.Builder()
                .build();
        ObjectCollection findTargets = new ObjectCollection.Builder()
                .withPatterns(pattern)
                .build();
        workflow.add(new ActionParametersV2(findOptions, findTargets));
        
        // Add click action
        ClickOptions clickOptions = new ClickOptions.Builder()
                .setClickType(ClickOptions.Type.LEFT)
                .build();
        Location clickLocation = new Location(100, 100);
        ObjectCollection clickTargets = new ObjectCollection.Builder()
                .withLocations(clickLocation)
                .build();
        workflow.add(new ActionParametersV2(clickOptions, clickTargets));
        
        // Add type action
        TypeOptions typeOptions = new TypeOptions.Builder()
                .setTypeDelay(0.1)
                .build();
        ObjectCollection typeTargets = new ObjectCollection.Builder()
                .withStrings("Hello World")
                .build();
        workflow.add(new ActionParametersV2(typeOptions, typeTargets));
        
        // Execute the workflow
        ActionResult result = multipleActions.perform(workflow);
        
        // Verify execution completed
        assertNotNull(result);
        assertNotNull(result.getActionConfig());
        // Last action was Type, so config should be TypeOptions
        assertTrue(result.getActionConfig() instanceof TypeOptions);
    }

    @Test
    void testMultipleBasicActionsWithActionConfig() {
        // Create a simple sequence with repetition
        MultipleActionsObjectV2 workflow = new MultipleActionsObjectV2();
        workflow.setTimesToRepeat(2); // Repeat the sequence twice
        
        // Add click action
        ClickOptions clickOptions = new ClickOptions.Builder()
                .setClickType(ClickOptions.Type.DOUBLE_LEFT)
                .setPauseBeforeBegin(1.0)
                .build();
        Location location = new Location(200, 200);
        ObjectCollection targets = new ObjectCollection.Builder()
                .withLocations(location)
                .build();
        workflow.add(new ActionParametersV2(clickOptions, targets));
        
        // Execute with MultipleBasicActions
        ActionResult result = multipleBasicActions.perform(workflow);
        
        // Verify execution
        assertNotNull(result);
        assertNotNull(result.getActionConfig());
        assertTrue(result.getActionConfig() instanceof ClickOptions);
        
        // Verify the click options were preserved
        ClickOptions resultOptions = (ClickOptions) result.getActionConfig();
        assertEquals(ClickOptions.Type.DOUBLE_LEFT, resultOptions.getClickType());
        assertEquals(1.0, resultOptions.getPauseBeforeBegin());
    }

    @Test
    void testEmptyWorkflow() {
        // Test with empty workflow
        MultipleActionsObjectV2 emptyWorkflow = new MultipleActionsObjectV2();
        
        ActionResult result = multipleActions.perform(emptyWorkflow);
        
        // Should return empty result
        assertNotNull(result);
        assertNull(result.getActionConfig());
        assertTrue(result.isEmpty());
    }

    @Test
    void testWorkflowWithMultipleIterations() {
        MultipleActionsObjectV2 workflow = new MultipleActionsObjectV2();
        workflow.setTimesToRepeat(3);
        
        // Add a find action
        PatternFindOptions findOptions = new PatternFindOptions.Builder()
                .setStrategy(PatternFindOptions.Strategy.ALL)
                .build();
        Pattern pattern = new Pattern.Builder()
                .build();
        ObjectCollection targets = new ObjectCollection.Builder()
                .withPatterns(pattern)
                .build();
        workflow.add(new ActionParametersV2(findOptions, targets));
        
        // Execute
        ActionResult result = multipleBasicActions.perform(workflow);
        
        // Verify the action was executed (would be repeated 3 times)
        assertNotNull(result);
        assertTrue(result.getActionConfig() instanceof PatternFindOptions);
    }
}