package io.github.jspinak.brobot.action.composite.multiple.finds;

import io.github.jspinak.brobot.action.ActionInterface;
import io.github.jspinak.brobot.action.ActionResult;
import io.github.jspinak.brobot.action.ObjectCollection;
import io.github.jspinak.brobot.action.ActionConfig;
import io.github.jspinak.brobot.action.ActionChainOptions;
import io.github.jspinak.brobot.action.internal.execution.ActionChainExecutor;
import io.github.jspinak.brobot.action.basic.find.PatternFindOptions;
import org.springframework.stereotype.Component;

/**
 * Modern implementation of nested finds using action chaining.
 * 
 * <p>NestedFinds performs hierarchical searches where each find operation
 * searches within the results of the previous find. This is useful for:
 * <ul>
 *   <li>Finding elements within specific UI containers</li>
 *   <li>Narrowing down search areas progressively</li>
 *   <li>Implementing hierarchical object detection</li>
 * </ul>
 * </p>
 * 
 * <p>Example usage:
 * <pre>{@code
 * // Find a button within a dialog within a window
 * NestedFindsOptions options = new NestedFindsOptions.Builder()
 *     .addFindStep(new PatternFindOptions.Builder()
 *         .setSimilarity(0.9)
 *         .build())
 *     .addFindStep(new PatternFindOptions.Builder()
 *         .setSimilarity(0.95)
 *         .build())
 *     .addFindStep(new PatternFindOptions.Builder()
 *         .setSimilarity(0.98)
 *         .build())
 *     .build();
 * 
 * action.perform(options,
 *     new ObjectCollection.Builder().withImages(windowImage).build(),
 *     new ObjectCollection.Builder().withImages(dialogImage).build(),
 *     new ObjectCollection.Builder().withImages(buttonImage).build());
 * }</pre>
 * </p>
 * 
 * @since 2.0
 */
@Component
public class NestedFinds implements ActionInterface {

    private final ActionChainExecutor actionChainExecutor;

    public NestedFinds(ActionChainExecutor actionChainExecutor) {
        this.actionChainExecutor = actionChainExecutor;
    }

    @Override
    public Type getActionType() {
        return Type.FIND;
    }

    /**
     * Performs nested find operations using the NESTED chaining strategy.
     * 
     * @param matches The ActionResult to populate with found matches
     * @param objectCollections Variable array of search targets. Each find action uses
     *                          the collection at its index.
     */
    @Override
    public void perform(ActionResult matches, ObjectCollection... objectCollections) {
        if (objectCollections.length == 0) {
            matches.setSuccess(false);
            return;
        }

        // Get the configuration
        ActionConfig config = matches.getActionConfig();
        NestedFindsOptions options = (config instanceof NestedFindsOptions) ?
            (NestedFindsOptions) config : createDefaultOptions(objectCollections.length);

        // Build the nested action chain
        ActionChainOptions.Builder chainBuilder = null;
        
        for (int i = 0; i < Math.min(options.getFindSteps().size(), objectCollections.length); i++) {
            PatternFindOptions findStep = options.getFindSteps().get(i);
            
            if (chainBuilder == null) {
                // First action in the chain
                chainBuilder = new ActionChainOptions.Builder(findStep);
                chainBuilder.setStrategy(ActionChainOptions.ChainingStrategy.NESTED);
            } else {
                // Add subsequent actions
                chainBuilder.then(findStep);
            }
        }

        if (chainBuilder == null) {
            matches.setSuccess(false);
            return;
        }

        // Execute the chain
        ActionChainOptions chainOptions = chainBuilder.build();
        ActionResult result = actionChainExecutor.executeChain(chainOptions, matches, objectCollections);
        
        // Copy results back to matches
        matches.setSuccess(result.isSuccess());
        matches.addAll(result.getMatchList());
        
        // Store initial matches for reference
        if (!result.getMatchList().isEmpty()) {
            matches.setInitialMatchList(result.getMatchList());
        }
    }
    
    /**
     * Creates default nested find options when none are provided.
     */
    private NestedFindsOptions createDefaultOptions(int steps) {
        NestedFindsOptions.Builder builder = new NestedFindsOptions.Builder();
        for (int i = 0; i < steps; i++) {
            builder.addFindStep(new PatternFindOptions.Builder().build());
        }
        return builder.build();
    }
}