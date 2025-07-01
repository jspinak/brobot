package io.github.jspinak.brobot.action.examples;

import io.github.jspinak.brobot.action.*;
import io.github.jspinak.brobot.action.basic.find.PatternFindOptions;
import io.github.jspinak.brobot.action.basic.vanish.VanishOptions;
import io.github.jspinak.brobot.action.basic.click.ClickOptions;
import io.github.jspinak.brobot.action.basic.type.TypeOptions;
import io.github.jspinak.brobot.action.basic.mouse.MouseMoveOptions;
import io.github.jspinak.brobot.action.internal.execution.ActionChainExecutor;
import io.github.jspinak.brobot.model.state.StateImage;
import org.springframework.stereotype.Component;

/**
 * Examples demonstrating how to use ActionChainOptions to replace deprecated composite patterns.
 * <p>
 * This class provides practical examples of migrating from older composite action patterns
 * (like MultipleActionsObject, ActionResultCombo) to the modern ActionChainOptions approach.
 * Each example shows both the old way and the new way to help developers migrate their code.
 * 
 * <p>Key benefits of ActionChainOptions:</p>
 * <ul>
 *   <li>Unified API for all action compositions</li>
 *   <li>Better type safety with ActionConfig</li>
 *   <li>Clear chaining strategies (NESTED, CONFIRM)</li>
 *   <li>Proper result flow between actions</li>
 * </ul>
 * 
 * @see ActionChainOptions
 * @see ActionChainExecutor
 */
@Component
public class ActionChainExamples {
    
    private final Action action;
    private final ActionChainExecutor chainExecutor;
    
    public ActionChainExamples(Action action, ActionChainExecutor chainExecutor) {
        this.action = action;
        this.chainExecutor = chainExecutor;
    }
    
    /**
     * Example 1: Simple Sequential Actions (replaces MultipleActionsObject)
     * <p>
     * Shows how to chain multiple actions that execute in sequence.
     * Common for form filling, multi-step operations.
     */
    public ActionResult fillLoginForm(StateImage usernameField, String username,
                                      StateImage passwordField, String password,
                                      StateImage loginButton) {
        
        // Old way (with MultipleActionsObject):
        // MultipleActionsObject mao = new MultipleActionsObject();
        // mao.add(new ActionParameters(clickOptions, usernameField.asObjectCollection()));
        // mao.add(new ActionParameters(typeOptions, new ObjectCollection.Builder().withStrings(username).build()));
        // ...
        
        // New way with ActionChainOptions:
        ActionChainOptions loginChain = new ActionChainOptions.Builder(
            // First: Click username field
            new ClickOptions.Builder()
                .setPauseAfterEnd(0.2)
                .build())
            // Then: Type username
            .then(new TypeOptions.Builder()
                .setTypeDelay(0.05)
                .build())
            // Then: Click password field
            .then(new ClickOptions.Builder()
                .setPauseAfterEnd(0.2)
                .build())
            // Then: Type password
            .then(new TypeOptions.Builder()
                .setTypeDelay(0.05)
                .build())
            // Finally: Click login button
            .then(new ClickOptions.Builder()
                .setPauseAfterEnd(1.0)
                .build())
            .build();
        
        // Execute the chain with appropriate ObjectCollections
        return chainExecutor.executeChain(loginChain, new ActionResult(),
            usernameField.asObjectCollection(),
            new ObjectCollection.Builder().withStrings(username).build(),
            passwordField.asObjectCollection(),
            new ObjectCollection.Builder().withStrings(password).build(),
            loginButton.asObjectCollection()
        );
    }
    
    /**
     * Example 2: Click and Verify Pattern (replaces ActionResultCombo)
     * <p>
     * Shows how to click something and verify the result appears.
     * Common for testing UI interactions.
     */
    public ActionResult clickAndVerify(StateImage buttonToClick, StateImage expectedResult) {
        
        // Old way (with ActionResultCombo):
        // ActionResultCombo combo = new ActionResultCombo();
        // combo.setActionOptions(clickOptions);
        // combo.setResultOptions(findOptions);
        // ...
        
        // New way with ActionChainOptions:
        ActionChainOptions clickVerifyChain = new ActionChainOptions.Builder(
            new ClickOptions.Builder()
                .setPauseAfterEnd(0.5)
                .build())
            .then(new PatternFindOptions.Builder()
                .setStrategy(PatternFindOptions.Strategy.FIRST)
                .setPauseBeforeBegin(3.0) // Wait before searching
                .build())
            .build();
        
        return chainExecutor.executeChain(clickVerifyChain, new ActionResult(),
            buttonToClick.asObjectCollection(),
            expectedResult.asObjectCollection()
        );
    }
    
    /**
     * Example 3: Nested Search Pattern
     * <p>
     * Shows how to use NESTED strategy to find elements within other elements.
     * Common for hierarchical UI navigation.
     */
    public ActionResult findButtonInDialog(StateImage dialog, StateImage button) {
        
        // Find dialog first, then search for button within dialog bounds
        ActionChainOptions nestedSearch = new ActionChainOptions.Builder(
            new PatternFindOptions.Builder()
                .setStrategy(PatternFindOptions.Strategy.FIRST)
                .build())
            .setStrategy(ActionChainOptions.ChainingStrategy.NESTED)
            .then(new PatternFindOptions.Builder()
                .setStrategy(PatternFindOptions.Strategy.FIRST)
                .build())
            .build();
        
        return chainExecutor.executeChain(nestedSearch, new ActionResult(),
            dialog.asObjectCollection(),
            button.asObjectCollection()
        );
    }
    
    /**
     * Example 4: Confirm Pattern
     * <p>
     * Shows how to use CONFIRM strategy to validate matches.
     * Useful for reducing false positives.
     */
    public ActionResult findAndConfirm(StateImage primaryImage, StateImage confirmationImage) {
        
        // Find primary image, then confirm by finding related confirmation image
        ActionChainOptions confirmChain = new ActionChainOptions.Builder(
            new PatternFindOptions.Builder()
                .setStrategy(PatternFindOptions.Strategy.ALL)
                .setSimilarity(0.7) // Lower threshold for initial search
                .build())
            .setStrategy(ActionChainOptions.ChainingStrategy.CONFIRM)
            .then(new PatternFindOptions.Builder()
                .setStrategy(PatternFindOptions.Strategy.FIRST)
                .setSimilarity(0.9) // Higher threshold for confirmation
                .build())
            .build();
        
        return chainExecutor.executeChain(confirmChain, new ActionResult(),
            primaryImage.asObjectCollection(),
            confirmationImage.asObjectCollection()
        );
    }
    
    /**
     * Example 5: Complex Workflow
     * <p>
     * Shows how to build more complex workflows with multiple action types.
     * This example: Find element → Move to it → Click → Verify result
     */
    public ActionResult complexWorkflow(StateImage target, StateImage expectedResult) {
        
        ActionChainOptions workflow = new ActionChainOptions.Builder(
            // Step 1: Find the target
            new PatternFindOptions.Builder()
                .setStrategy(PatternFindOptions.Strategy.BEST)
                .build())
            // Step 2: Move mouse to target (hover)
            .then(new MouseMoveOptions.Builder()
                .setPauseAfterEnd(0.5) // Hover delay
                .build())
            // Step 3: Click the target
            .then(new ClickOptions.Builder()
                .setPauseAfterEnd(0.3)
                .build())
            // Step 4: Verify the expected result appears
            .then(new PatternFindOptions.Builder()
                .setStrategy(PatternFindOptions.Strategy.FIRST)
                .setPauseBeforeBegin(2.0) // Wait before searching
                .build())
            .build();
        
        return chainExecutor.executeChain(workflow, new ActionResult(),
            target.asObjectCollection(),
            target.asObjectCollection(), // Same target for move
            target.asObjectCollection(), // Same target for click
            expectedResult.asObjectCollection()
        );
    }
    
    /**
     * Example 6: Reusable Chain Builder
     * <p>
     * Shows how to create reusable chain patterns for common operations.
     */
    public static class ChainPatterns {
        
        /**
         * Creates a standard click-and-verify chain.
         */
        public static ActionChainOptions clickAndVerify(double verifyTimeout) {
            return new ActionChainOptions.Builder(
                new ClickOptions.Builder().build())
                .then(new PatternFindOptions.Builder()
                    .setPauseBeforeBegin(verifyTimeout)
                    .build())
                .build();
        }
        
        /**
         * Creates a type-and-verify chain.
         */
        public static ActionChainOptions typeAndVerify(double typeDelay, double verifyTimeout) {
            return new ActionChainOptions.Builder(
                new TypeOptions.Builder()
                    .setTypeDelay(typeDelay)
                    .build())
                .then(new PatternFindOptions.Builder()
                    .setPauseBeforeBegin(verifyTimeout)
                    .build())
                .build();
        }
        
        /**
         * Creates a find-click-vanish chain (for dismissing elements).
         */
        public static ActionChainOptions dismissElement() {
            return new ActionChainOptions.Builder(
                new PatternFindOptions.Builder()
                    .setStrategy(PatternFindOptions.Strategy.FIRST)
                    .build())
                .then(new ClickOptions.Builder()
                    .setPauseAfterEnd(0.3)
                    .build())
                .then(new VanishOptions.Builder()
                    .setTimeout(2.0)
                    .build())
                .build();
        }
    }
    
    /**
     * Example 7: Dynamic Chain Building
     * <p>
     * Shows how to build chains dynamically based on conditions.
     */
    public ActionChainOptions buildDynamicChain(boolean needsAuthentication, 
                                                boolean needsConfirmation) {
        
        ActionChainOptions.Builder builder = new ActionChainOptions.Builder(
            new PatternFindOptions.Builder().build()
        );
        
        if (needsAuthentication) {
            builder.then(new ClickOptions.Builder().build())
                   .then(new TypeOptions.Builder().build());
        }
        
        builder.then(new ClickOptions.Builder().build());
        
        if (needsConfirmation) {
            builder.then(new PatternFindOptions.Builder()
                .setPauseBeforeBegin(3.0)
                .build());
        }
        
        return builder.build();
    }
}