package io.github.jspinak.brobot.action.basic.find;

import io.github.jspinak.brobot.action.ActionInterface;
import io.github.jspinak.brobot.action.ActionResult;
import io.github.jspinak.brobot.action.ObjectCollection;
import org.springframework.stereotype.Component;

import java.util.Arrays;
import java.util.List;
import java.util.function.BiConsumer;

/**
 * Executes custom find operations using a provided strategy function.
 * <p>
 * This class allows for implementing specialized find logic that doesn't fit
 * into the standard pattern-matching or color-based find operations. It provides
 * a way to inject custom behavior while still integrating with the Brobot
 * action framework.
 * <p>
 * Common use cases include:
 * <ul>
 *   <li>Complex multi-stage find operations</li>
 *   <li>Finds that require external data or services</li>
 *   <li>Temporary or experimental find algorithms</li>
 *   <li>Finds with dynamic behavior based on runtime conditions</li>
 * </ul>
 * 
 * <p><b>Example usage:</b></p>
 * <pre>{@code
 * // Create a custom find that looks for elements in a specific order
 * BiConsumer<ActionResult, List<ObjectCollection>> customStrategy = (result, collections) -> {
 *     // Custom find logic here
 *     for (ObjectCollection collection : collections) {
 *         // Process each collection
 *         // Add matches to result
 *     }
 * };
 * 
 * // Execute the custom find
 * CustomFind customFind = new CustomFind();
 * ActionResult result = new ActionResult();
 * customFind.perform(result, customStrategy, objectCollection1, objectCollection2);
 * }</pre>
 *
 * @see Find
 * @see ActionInterface
 */
@Component
public class CustomFind implements ActionInterface {

    @Override
    public Type getActionType() {
        return Type.FIND;
    }

    /**
     * Executes a custom find operation using the provided strategy.
     * <p>
     * This method delegates the actual find logic to the provided BiConsumer,
     * which receives the ActionResult to populate and the list of ObjectCollections
     * to search within.
     *
     * @param actionResult The result object to populate with matches
     * @param findStrategy The custom find logic to execute
     * @param objectCollections Variable number of collections to search
     */
    public void perform(ActionResult actionResult, BiConsumer<ActionResult, List<ObjectCollection>> findStrategy, 
                       ObjectCollection... objectCollections) {
        if (findStrategy == null) {
            throw new IllegalArgumentException("Find strategy cannot be null");
        }
        
        List<ObjectCollection> collections = Arrays.asList(objectCollections);
        findStrategy.accept(actionResult, collections);
    }

    /**
     * Standard perform method for ActionInterface compatibility.
     * <p>
     * This method extracts the custom find strategy from the ActionOptions
     * if one was provided, otherwise throws an exception.
     *
     * @param actionResult The result object containing action options
     * @param objectCollections Variable number of collections to search
     * @throws UnsupportedOperationException if no custom find strategy is provided
     */
    @Override
    public void perform(ActionResult actionResult, ObjectCollection... objectCollections) {
        throw new UnsupportedOperationException(
            "CustomFind requires a find strategy. Use perform(ActionResult, BiConsumer, ObjectCollection...) instead."
        );
    }
}