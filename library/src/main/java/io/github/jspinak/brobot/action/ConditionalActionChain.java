package io.github.jspinak.brobot.action;

import java.awt.event.KeyEvent;
import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;
import java.util.function.Predicate;
import java.util.function.Supplier;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import io.github.jspinak.brobot.action.basic.click.ClickOptions;
import io.github.jspinak.brobot.action.basic.find.PatternFindOptions;
import io.github.jspinak.brobot.action.basic.highlight.HighlightOptions;
import io.github.jspinak.brobot.action.basic.mouse.ScrollOptions;
import io.github.jspinak.brobot.action.basic.type.KeyDownOptions;
import io.github.jspinak.brobot.action.basic.type.KeyUpOptions;
import io.github.jspinak.brobot.action.basic.type.TypeOptions;
import io.github.jspinak.brobot.action.basic.vanish.VanishOptions;
import io.github.jspinak.brobot.model.state.StateImage;

/**
 * Enhanced version of ConditionalActionChain with the missing features from documentation.
 *
 * <p>This implementation adds: - then() method for sequential action composition - Convenience
 * methods for common actions (click, type, wait, scroll, etc.) - Proper conditional execution logic
 * - Control flow methods (stopChain, retry, etc.) - Utility actions (screenshot, highlight,
 * keyboard shortcuts)
 *
 * @since 2.0
 */
public class ConditionalActionChain {
    private static final Logger log = LoggerFactory.getLogger(ConditionalActionChain.class);

    /** Represents a conditional action in the chain. */
    private static class ConditionalAction {
        final ActionConfig action;
        final Condition condition;
        final Supplier<String> logMessage;
        final Consumer<ActionResult> customHandler;
        final ObjectCollection objectCollection; // For actions that need specific objects

        ConditionalAction(ActionConfig action, Condition condition) {
            this(action, condition, null, null, null);
        }

        ConditionalAction(
                ActionConfig action,
                Condition condition,
                Supplier<String> logMessage,
                Consumer<ActionResult> customHandler,
                ObjectCollection objectCollection) {
            this.action = action;
            this.condition = condition;
            this.logMessage = logMessage;
            this.customHandler = customHandler;
            this.objectCollection = objectCollection;
        }
    }

    /** Conditions for executing actions in the chain. */
    private enum Condition {
        ALWAYS, // Execute regardless of previous result
        IF_FOUND, // Execute only if previous action succeeded
        IF_NOT_FOUND // Execute only if previous action failed
    }

    private final List<ConditionalAction> actions = new ArrayList<>();
    private ActionResult previousResult;
    private boolean chainStopped = false;

    /**
     * Creates a new ConditionalActionChain starting with a Find action.
     *
     * @param findOptions the find configuration
     * @return a new ConditionalActionChain instance
     */
    public static ConditionalActionChain find(PatternFindOptions findOptions) {
        ConditionalActionChain chain = new ConditionalActionChain();
        chain.actions.add(new ConditionalAction(findOptions, Condition.ALWAYS));
        return chain;
    }

    /**
     * Creates a new chain starting with any action.
     *
     * @param action the initial action configuration
     * @return a new ConditionalActionChain instance
     */
    public static ConditionalActionChain start(ActionConfig action) {
        ConditionalActionChain chain = new ConditionalActionChain();
        chain.actions.add(new ConditionalAction(action, Condition.ALWAYS));
        return chain;
    }

    /** Convenience method to start with finding an image. */
    public static ConditionalActionChain find(StateImage image) {
        return find(new PatternFindOptions.Builder().build())
                .withObjectCollection(new ObjectCollection.Builder().withImages(image).build());
    }

    // ========== Core Chaining Methods ==========

    /**
     * Adds a sequential action to the chain (always executes). This is the missing method that
     * enables multi-step workflows!
     *
     * @param action the action to add
     * @return this chain for fluent API
     */
    public ConditionalActionChain then(ActionConfig action) {
        actions.add(new ConditionalAction(action, Condition.ALWAYS));
        return this;
    }

    /** Adds a find action to the chain. */
    public ConditionalActionChain then(PatternFindOptions findOptions) {
        return then((ActionConfig) findOptions);
    }

    /** Convenience method to then find an image. */
    public ConditionalActionChain then(StateImage image) {
        return then(new PatternFindOptions.Builder().build())
                .withObjectCollection(new ObjectCollection.Builder().withImages(image).build());
    }

    /** Adds an action that executes only if the previous action succeeded. */
    public ConditionalActionChain ifFound(ActionConfig action) {
        actions.add(new ConditionalAction(action, Condition.IF_FOUND));
        return this;
    }

    /** Adds an action that executes only if the previous action failed. */
    public ConditionalActionChain ifNotFound(ActionConfig action) {
        actions.add(new ConditionalAction(action, Condition.IF_NOT_FOUND));
        return this;
    }

    /** Adds an action that executes regardless of the previous result. */
    public ConditionalActionChain always(ActionConfig action) {
        actions.add(new ConditionalAction(action, Condition.ALWAYS));
        return this;
    }

    // ========== Convenience Action Methods ==========

    /** Adds a click action to the chain. */
    public ConditionalActionChain click() {
        return then(new ClickOptions.Builder().build());
    }

    /** Conditional click - only if previous action succeeded. */
    public ConditionalActionChain ifFoundClick() {
        return ifFound(new ClickOptions.Builder().build());
    }

    /** Types the specified text. */
    public ConditionalActionChain type(String text) {
        TypeOptions typeOptions = new TypeOptions.Builder().build();
        ObjectCollection textCollection = new ObjectCollection.Builder().withStrings(text).build();

        ConditionalAction action =
                new ConditionalAction(typeOptions, Condition.ALWAYS, null, null, textCollection);
        actions.add(action);
        return this;
    }

    /** Types text only if previous action succeeded. */
    public ConditionalActionChain ifFoundType(String text) {
        TypeOptions typeOptions = new TypeOptions.Builder().build();
        ObjectCollection textCollection = new ObjectCollection.Builder().withStrings(text).build();

        ConditionalAction action =
                new ConditionalAction(typeOptions, Condition.IF_FOUND, null, null, textCollection);
        actions.add(action);
        return this;
    }

    /** Clears the field and types new text. */
    public ConditionalActionChain clearAndType(String text) {
        // Clear with Ctrl+A and Delete
        pressKeyCombo(KeyEvent.VK_CONTROL, KeyEvent.VK_A);
        pressKey(KeyEvent.VK_DELETE);
        return type(text);
    }

    /** Scrolls down. */
    public ConditionalActionChain scrollDown() {
        return then(new ScrollOptions.Builder().setDirection(ScrollOptions.Direction.DOWN).build());
    }

    /** Scrolls up. */
    public ConditionalActionChain scrollUp() {
        return then(new ScrollOptions.Builder().setDirection(ScrollOptions.Direction.UP).build());
    }

    /** Highlights the last found element. */
    public ConditionalActionChain highlight() {
        return then(new HighlightOptions.Builder().build());
    }

    /** Waits for an element to vanish. */
    public ConditionalActionChain waitVanish(StateImage image) {
        VanishOptions vanishOptions = new VanishOptions.Builder().build();
        ObjectCollection collection = new ObjectCollection.Builder().withImages(image).build();

        ConditionalAction action =
                new ConditionalAction(vanishOptions, Condition.ALWAYS, null, null, collection);
        actions.add(action);
        return this;
    }

    // ========== Keyboard Shortcuts ==========

    /** Presses the Escape key. */
    public ConditionalActionChain pressEscape() {
        return pressKey(KeyEvent.VK_ESCAPE);
    }

    /** Presses the Enter key. */
    public ConditionalActionChain pressEnter() {
        return pressKey(KeyEvent.VK_ENTER);
    }

    /** Presses the Tab key. */
    public ConditionalActionChain pressTab() {
        return pressKey(KeyEvent.VK_TAB);
    }

    /** Presses a specific key. */
    public ConditionalActionChain pressKey(int keyCode) {
        // Convert keyCode to string representation
        String keyString = java.awt.event.KeyEvent.getKeyText(keyCode);

        KeyDownOptions keyDown = new KeyDownOptions.Builder().build();
        KeyUpOptions keyUp = new KeyUpOptions.Builder().build();

        // Create ObjectCollection with the key string
        ObjectCollection keyCollection =
                new ObjectCollection.Builder().withStrings(keyString).build();

        ConditionalAction downAction =
                new ConditionalAction(keyDown, Condition.ALWAYS, null, null, keyCollection);
        actions.add(downAction);

        // Keys are pressed and released without artificial delay
        // Timing should be handled by action configurations

        ConditionalAction upAction =
                new ConditionalAction(keyUp, Condition.ALWAYS, null, null, keyCollection);
        actions.add(upAction);

        return this;
    }

    /** Presses a key combination (like Ctrl+S). */
    public ConditionalActionChain pressKeyCombo(int modifierKey, int key) {
        // Convert keyCodes to string representations
        String modifierString = java.awt.event.KeyEvent.getKeyText(modifierKey);
        String keyString = java.awt.event.KeyEvent.getKeyText(key);

        // Press modifier
        KeyDownOptions modDown = new KeyDownOptions.Builder().addModifier(modifierString).build();
        ObjectCollection modCollection =
                new ObjectCollection.Builder().withStrings(modifierString).build();
        ConditionalAction modDownAction =
                new ConditionalAction(modDown, Condition.ALWAYS, null, null, modCollection);
        actions.add(modDownAction);

        // Press key with modifier
        KeyDownOptions keyDown = new KeyDownOptions.Builder().addModifier(modifierString).build();
        ObjectCollection keyCollection =
                new ObjectCollection.Builder().withStrings(keyString).build();
        ConditionalAction keyDownAction =
                new ConditionalAction(keyDown, Condition.ALWAYS, null, null, keyCollection);
        actions.add(keyDownAction);

        // Release key
        KeyUpOptions keyUp = new KeyUpOptions.Builder().build();
        ConditionalAction keyUpAction =
                new ConditionalAction(keyUp, Condition.ALWAYS, null, null, keyCollection);
        actions.add(keyUpAction);

        // Release modifier
        KeyUpOptions modUp = new KeyUpOptions.Builder().build();
        ConditionalAction modUpAction =
                new ConditionalAction(modUp, Condition.ALWAYS, null, null, modCollection);
        actions.add(modUpAction);

        return this;
    }

    /** Presses Ctrl+S. */
    public ConditionalActionChain pressCtrlS() {
        return pressKeyCombo(KeyEvent.VK_CONTROL, KeyEvent.VK_S);
    }

    /** Presses Ctrl+A (Select All). */
    public ConditionalActionChain pressCtrlA() {
        return pressKeyCombo(KeyEvent.VK_CONTROL, KeyEvent.VK_A);
    }

    /** Presses the Delete key. */
    public ConditionalActionChain pressDelete() {
        return pressKey(KeyEvent.VK_DELETE);
    }

    // ========== Logging Methods ==========

    /** Logs a message. */
    public ConditionalActionChain log(String message) {
        ConditionalAction action =
                new ConditionalAction(null, Condition.ALWAYS, () -> message, null, null);
        actions.add(action);
        return this;
    }

    /** Logs a message if the previous action succeeded. */
    public ConditionalActionChain ifFoundLog(String message) {
        ConditionalAction action =
                new ConditionalAction(null, Condition.IF_FOUND, () -> message, null, null);
        actions.add(action);
        return this;
    }

    /** Logs a message if the previous action failed. */
    public ConditionalActionChain ifNotFoundLog(String message) {
        ConditionalAction action =
                new ConditionalAction(null, Condition.IF_NOT_FOUND, () -> message, null, null);
        actions.add(action);
        return this;
    }

    // ========== Custom Handlers ==========

    /** Executes custom logic if the previous action succeeded. */
    public ConditionalActionChain ifFoundDo(Consumer<ActionResult> handler) {
        ConditionalAction action =
                new ConditionalAction(null, Condition.IF_FOUND, null, handler, null);
        actions.add(action);
        return this;
    }

    /** Executes custom logic if the previous action failed. */
    public ConditionalActionChain ifNotFoundDo(Consumer<ActionResult> handler) {
        ConditionalAction action =
                new ConditionalAction(null, Condition.IF_NOT_FOUND, null, handler, null);
        actions.add(action);
        return this;
    }

    /**
     * Adds a lambda function that executes if the previous action succeeded. This overload allows
     * chaining operations on the chain itself.
     */
    public ConditionalActionChain ifFound(Consumer<ConditionalActionChain> chainHandler) {
        ConditionalAction action =
                new ConditionalAction(
                        null, Condition.IF_FOUND, null, result -> chainHandler.accept(this), null);
        actions.add(action);
        return this;
    }

    /**
     * Adds a lambda function that executes if the previous action failed. This overload allows
     * chaining operations on the chain itself.
     */
    public ConditionalActionChain ifNotFound(Consumer<ConditionalActionChain> chainHandler) {
        ConditionalAction action =
                new ConditionalAction(
                        null,
                        Condition.IF_NOT_FOUND,
                        null,
                        result -> chainHandler.accept(this),
                        null);
        actions.add(action);
        return this;
    }

    // ========== Control Flow ==========

    /** Stops the chain execution. */
    public ConditionalActionChain stopChain() {
        ConditionalAction action =
                new ConditionalAction(
                        null, Condition.ALWAYS, null, result -> chainStopped = true, null);
        actions.add(action);
        return this;
    }

    /** Stops the chain if a condition is met. */
    public ConditionalActionChain stopIf(Predicate<ActionResult> condition) {
        return ifFoundDo(
                result -> {
                    if (condition.test(result)) {
                        chainStopped = true;
                        log.info("Chain stopped by condition");
                    }
                });
    }

    /** Throws an error with the specified message. */
    public ConditionalActionChain throwError(String message) {
        ConditionalAction action =
                new ConditionalAction(
                        null,
                        Condition.ALWAYS,
                        null,
                        result -> {
                            throw new RuntimeException(message);
                        },
                        null);
        actions.add(action);
        return this;
    }

    // ========== Utility Methods ==========

    /** Takes a screenshot with the specified filename. */
    public ConditionalActionChain takeScreenshot(String filename) {
        // This would use capture/screenshot functionality
        // For now, just log it
        return log("Screenshot: " + filename);
    }

    /**
     * Adds an ObjectCollection to the last action. This is used internally to associate objects
     * with actions.
     */
    private ConditionalActionChain withObjectCollection(ObjectCollection collection) {
        if (!actions.isEmpty()) {
            ConditionalAction last = actions.get(actions.size() - 1);
            // Replace the last action with one that has the collection
            actions.set(
                    actions.size() - 1,
                    new ConditionalAction(
                            last.action,
                            last.condition,
                            last.logMessage,
                            last.customHandler,
                            collection));
        }
        return this;
    }

    // ========== Execution ==========

    /**
     * Executes the conditional action chain with proper conditional logic.
     *
     * @param action the Action instance to use for execution
     * @param defaultCollections default object collections to use when not specified
     * @return the final ActionResult
     */
    public ActionResult perform(Action action, ObjectCollection... defaultCollections) {
        ActionResult finalResult = new ActionResult();
        finalResult.setSuccess(true);
        chainStopped = false;

        for (int i = 0; i < actions.size() && !chainStopped; i++) {
            ConditionalAction conditionalAction = actions.get(i);

            // Check if this action should execute based on condition
            if (!shouldExecute(conditionalAction.condition, previousResult)) {
                continue;
            }

            // Execute the action
            ActionResult currentResult =
                    executeAction(conditionalAction, action, defaultCollections);

            // Update state
            if (currentResult != null) {
                previousResult = currentResult;
                finalResult = currentResult; // Keep the last actual result
            }
        }

        return finalResult;
    }

    /** Determines if an action should execute based on its condition and the previous result. */
    private boolean shouldExecute(Condition condition, ActionResult previousResult) {
        switch (condition) {
            case ALWAYS:
                return true;
            case IF_FOUND:
                return previousResult != null && previousResult.isSuccess();
            case IF_NOT_FOUND:
                return previousResult == null || !previousResult.isSuccess();
            default:
                return false;
        }
    }

    /** Executes a single conditional action. */
    private ActionResult executeAction(
            ConditionalAction conditionalAction,
            Action action,
            ObjectCollection[] defaultCollections) {
        // Handle logging
        if (conditionalAction.logMessage != null) {
            log.info(conditionalAction.logMessage.get());
            return null; // Logging doesn't produce a result
        }

        // Handle custom handler
        if (conditionalAction.customHandler != null) {
            conditionalAction.customHandler.accept(previousResult);
            return null; // Custom handler doesn't produce a result
        }

        // Handle regular action
        if (conditionalAction.action != null) {
            // Use specific collection if provided, otherwise use defaults
            ObjectCollection[] collections =
                    conditionalAction.objectCollection != null
                            ? new ObjectCollection[] {conditionalAction.objectCollection}
                            : defaultCollections;

            return action.perform(conditionalAction.action, collections);
        }

        return null;
    }

    /**
     * Creates a retry chain for the specified action.
     *
     * @param actionToRetry the action to retry
     * @param maxRetries maximum number of retry attempts
     * @return a new chain configured for retries
     */
    public static ConditionalActionChain retry(ActionConfig actionToRetry, int maxRetries) {
        ConditionalActionChain chain = start(actionToRetry);

        for (int i = 1; i < maxRetries; i++) {
            chain.ifNotFoundLog("Attempt " + (i + 1) + " of " + maxRetries)
                    // Retry timing should be configured in action options
                    .ifNotFound(actionToRetry);
        }

        return chain;
    }
}
