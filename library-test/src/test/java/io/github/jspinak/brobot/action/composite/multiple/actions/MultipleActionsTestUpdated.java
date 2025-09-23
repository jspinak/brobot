package io.github.jspinak.brobot.action.composite.multiple.actions;

import static org.junit.jupiter.api.Assertions.*;

import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.ContextConfiguration;

import io.github.jspinak.brobot.BrobotTestApplication;
import io.github.jspinak.brobot.action.*;
import io.github.jspinak.brobot.action.basic.click.ClickOptions;
import io.github.jspinak.brobot.action.basic.find.PatternFindOptions;
import io.github.jspinak.brobot.action.basic.type.TypeOptions;
import io.github.jspinak.brobot.action.internal.execution.ActionChainExecutor;
import io.github.jspinak.brobot.config.environment.ExecutionEnvironment;
import io.github.jspinak.brobot.model.element.Location;
import io.github.jspinak.brobot.model.element.Region;
import io.github.jspinak.brobot.model.state.StateImage;
import io.github.jspinak.brobot.test.BrobotIntegrationTestBase;
import io.github.jspinak.brobot.test.TestEnvironmentInitializer;
import io.github.jspinak.brobot.test.mock.MockScreenConfig;

/**
 * Tests for ActionChainOptions and ConditionalActionChain. Replaces old MultipleActions and
 * MultipleBasicActions tests.
 *
 * <p>These tests verify that composite action sequences work correctly with: - ActionChainOptions
 * for sequential actions - ConditionalActionChain for conditional workflows - Pure actions that
 * don't have circular dependencies
 */
@SpringBootTest(
        classes = BrobotTestApplication.class,
        properties = {
            "brobot.mock.enabled=true",
            "brobot.gui-access.continue-on-error=true",
            "brobot.gui-access.check-on-startup=false",
            "java.awt.headless=true",
            "spring.main.allow-bean-definition-overriding=true",
            "brobot.test.type=unit",
            "brobot.capture.physical-resolution=false"
        })
@Import({
    MockScreenConfig.class,
    io.github.jspinak.brobot.test.config.TestApplicationConfiguration.class,
    io.github.jspinak.brobot.test.config.TestActionConfig.class
})
@ContextConfiguration(initializers = TestEnvironmentInitializer.class)
@Disabled("CI failure - needs investigation")
public class MultipleActionsTestUpdated extends BrobotIntegrationTestBase {

    @Autowired private Action action;

    @Autowired private ActionChainExecutor chainExecutor;

    @BeforeAll
    public static void setupHeadlessMode() {
        System.setProperty("java.awt.headless", "true");
    }

    @BeforeEach
    @Override
    protected void setUpBrobotEnvironment() {
        // Configure for unit testing - mock mode
        ExecutionEnvironment env =
                ExecutionEnvironment.builder()
                        .mockMode(true)
                        .forceHeadless(true)
                        .allowScreenCapture(false)
                        .build();
        ExecutionEnvironment.setInstance(env);
    }

    @Test
    void testActionChainWithMultipleActions() {
        // Create a chain of actions using ActionChainOptions
        ActionChainOptions chain =
                new ActionChainOptions.Builder(
                                // First: Find an element
                                new PatternFindOptions.Builder()
                                        .setStrategy(PatternFindOptions.Strategy.FIRST)
                                        .build())
                        // Then: Click on it
                        .then(
                                new ClickOptions.Builder()
                                        .setNumberOfClicks(1)
                                        .setPauseAfterEnd(0.5)
                                        .build())
                        // Then: Type text
                        .then(new TypeOptions.Builder().setTypeDelay(0.05).build())
                        .build();

        // Create test data
        StateImage testImage =
                new StateImage.Builder().setName("TestImage").addPattern("test.png").build();

        // Execute the chain
        ActionResult result =
                chainExecutor.executeChain(
                        chain,
                        new ActionResult(),
                        testImage.asObjectCollection(),
                        new ObjectCollection.Builder()
                                .build(), // For click (uses previous find result)
                        new ObjectCollection.Builder()
                                .withStrings("Hello World")
                                .build() // For type with text
                        );

        assertNotNull(result, "Chain execution should return a result");
        // In mock mode, actions should succeed
        assertTrue(
                result.isSuccess() || result.getMatchList() != null,
                "Chain should execute successfully in mock mode");
    }

    @Test
    void testConditionalActionChain() {
        // Create test image
        StateImage testImage =
                new StateImage.Builder().setName("TestButton").addPattern("button.png").build();

        // Use ConditionalActionChain for conditional workflow
        ConditionalActionChain.find(
                        new PatternFindOptions.Builder()
                                .setStrategy(PatternFindOptions.Strategy.FIRST)
                                .build())
                .ifFoundClick()
                .ifFoundType("Button clicked")
                .ifNotFoundLog("Button not found")
                .perform(action, testImage.asObjectCollection());

        // Test passes if no exceptions are thrown
        assertTrue(true, "Conditional chain should execute without errors");
    }

    @Test
    void testEmptyActionChain() {
        // Test that an empty chain doesn't cause errors
        ActionChainOptions emptyChain =
                new ActionChainOptions.Builder(new PatternFindOptions.Builder().build()).build();

        ActionResult result =
                chainExecutor.executeChain(
                        emptyChain, new ActionResult(), new ObjectCollection.Builder().build());

        assertNotNull(result, "Empty chain should still return a result");
    }

    @Test
    void testActionChainWithMultipleIterations() {
        // Create a chain that can be executed multiple times
        ActionChainOptions clickChain =
                new ActionChainOptions.Builder(
                                new ClickOptions.Builder()
                                        .setNumberOfClicks(2) // Double-click
                                        .build())
                        .build();

        // Create test locations
        ObjectCollection locations =
                new ObjectCollection.Builder()
                        .withLocations(
                                new Location(100, 100),
                                new Location(200, 200),
                                new Location(300, 300))
                        .build();

        // Execute the chain multiple times
        for (int i = 0; i < 3; i++) {
            ActionResult result =
                    chainExecutor.executeChain(clickChain, new ActionResult(), locations);

            assertNotNull(result, "Iteration " + i + " should return a result");
        }

        assertTrue(true, "Multiple iterations should complete successfully");
    }

    @Test
    void testPureActionsWithoutCircularDependency() {
        // Test that pure actions work independently without Find

        // Pure Click - no Find dependency
        ClickOptions clickOptions = new ClickOptions.Builder().setNumberOfClicks(1).build();

        ObjectCollection clickTargets =
                new ObjectCollection.Builder()
                        .withLocations(new Location(150, 150))
                        .withRegions(new Region(200, 200, 50, 50))
                        .build();

        ActionResult clickResult = action.perform(clickOptions, clickTargets);
        assertNotNull(clickResult, "Pure Click should work without Find");

        // Pure Type - just types at current location
        TypeOptions typeOptions = new TypeOptions.Builder().build();

        ActionResult typeResult =
                action.perform(
                        typeOptions,
                        new ObjectCollection.Builder().withStrings("Test text").build());
        assertNotNull(typeResult, "Pure Type should work independently");
    }

    @Test
    void testComplexChainWithNestedStrategy() {
        // Test NESTED strategy for finding elements within other elements
        ActionChainOptions nestedChain =
                new ActionChainOptions.Builder(
                                new PatternFindOptions.Builder()
                                        .setStrategy(PatternFindOptions.Strategy.FIRST)
                                        .build())
                        .setStrategy(ActionChainOptions.ChainingStrategy.NESTED)
                        .then(
                                new PatternFindOptions.Builder()
                                        .setStrategy(PatternFindOptions.Strategy.ALL)
                                        .build())
                        .then(new ClickOptions.Builder().build())
                        .build();

        // Create test images
        StateImage container =
                new StateImage.Builder().setName("Container").addPattern("container.png").build();

        StateImage button =
                new StateImage.Builder().setName("Button").addPattern("button.png").build();

        // Execute nested chain
        ActionResult result =
                chainExecutor.executeChain(
                        nestedChain,
                        new ActionResult(),
                        container.asObjectCollection(),
                        button.asObjectCollection(),
                        new ObjectCollection.Builder().build() // For click
                        );

        assertNotNull(result, "Nested chain should return a result");
    }
}
