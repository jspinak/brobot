package io.github.jspinak.brobot.action.composite;

import org.springframework.stereotype.Component;

import io.github.jspinak.brobot.action.ActionChainOptions;
import io.github.jspinak.brobot.action.ActionConfig;
import io.github.jspinak.brobot.action.ActionInterface;
import io.github.jspinak.brobot.action.ActionResult;
import io.github.jspinak.brobot.action.ObjectCollection;
import io.github.jspinak.brobot.action.internal.execution.ActionChainExecutor;

/**
 * Modern implementation of Click and Type composite action using the fluent API.
 *
 * <p>This composite action performs the common pattern of:
 *
 * <ol>
 *   <li>Finding a target element (e.g., text field)
 *   <li>Clicking on it to focus
 *   <li>Typing text into it
 * </ol>
 *
 * <p>Usage:
 *
 * <pre>{@code
 * // Create the configuration
 * ClickAndTypeOptions options = new ClickAndTypeOptions.Builder()
 *     .setFindOptions(new PatternFindOptions.Builder()
 *         .setSimilarity(0.9)
 *         .build())
 *     .setClickOptions(new ClickOptions.Builder()
 *         .setNumberOfClicks(1)
 *         .build())
 *     .setTypeOptions(new TypeOptions.Builder()
 *         .setPauseBeforeBegin(0.5)
 *         .build())
 *     .build();
 *
 * // Use with Action
 * action.perform(options,
 *     new ObjectCollection.Builder()
 *         .withImages(textFieldImage)
 *         .withStrings("text to type")
 *         .build());
 * }</pre>
 *
 * @since 2.0
 */
@Component
public class ClickAndType implements ActionInterface {

    private final ActionChainExecutor actionChainExecutor;

    public ClickAndType(ActionChainExecutor actionChainExecutor) {
        this.actionChainExecutor = actionChainExecutor;
    }

    @Override
    public Type getActionType() {
        return Type.CLICK; // Return CLICK as the primary action type
    }

    @Override
    public void perform(ActionResult matches, ObjectCollection... objectCollections) {
        if (objectCollections.length < 1) {
            matches.setSuccess(false);
            return;
        }

        // Get the configuration
        ActionConfig config = matches.getActionConfig();
        ClickAndTypeOptions options =
                (config instanceof ClickAndTypeOptions)
                        ? (ClickAndTypeOptions) config
                        : new ClickAndTypeOptions.Builder().build();

        // Extract the object collection
        ObjectCollection collection = objectCollections[0];

        // Build the action chain: Find → Click → Type
        ActionChainOptions chainOptions =
                new ActionChainOptions.Builder(options.getFindOptions())
                        .setStrategy(ActionChainOptions.ChainingStrategy.NESTED)
                        .then(options.getClickOptions())
                        .then(options.getTypeOptions())
                        .build();

        // Execute the chain
        ActionResult result = actionChainExecutor.executeChain(chainOptions, matches, collection);

        // Copy results back to matches
        matches.setSuccess(result.isSuccess());
        matches.addAll(result.getMatchList());
    }
}
