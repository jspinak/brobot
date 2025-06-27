package io.github.jspinak.brobot.action.composite.multiple.finds;

import io.github.jspinak.brobot.action.ActionInterface;
import io.github.jspinak.brobot.action.ActionOptions;
import io.github.jspinak.brobot.action.ActionResult;
import io.github.jspinak.brobot.action.ObjectCollection;

import org.springframework.stereotype.Component;

/**
 * Orchestrates multiple find strategies based on the desired match filtering approach.
 * <p>
 * MultipleFinds serves as a strategy selector for complex find operations, choosing
 * between two distinct approaches based on the keepLargerMatches flag in ActionOptions.
 * This design allows users to select the most appropriate multi-find behavior for
 * their specific use case without changing their code structure.
 * 
 * <p>The two strategies are:</p>
 * <ul>
 *   <li><b>ConfirmedFinds (keepLargerMatches=true):</b> Validates initial matches
 *       through subsequent searches, keeping matches that are confirmed by all
 *       find operations. Best for eliminating false positives.</li>
 *   <li><b>NestedFinds (keepLargerMatches=false):</b> Performs hierarchical searches
 *       where each find operation searches within the results of the previous one.
 *       Best for finding elements within containers.</li>
 * </ul>
 * 
 * <p>Common use cases:</p>
 * <ul>
 *   <li><b>ConfirmedFinds mode:</b> Finding buttons that must have specific icons
 *       nearby, or validating matches by checking for expected context</li>
 *   <li><b>NestedFinds mode:</b> Finding a button within a specific dialog box,
 *       or locating text within a particular region of the screen</li>
 * </ul>
 * 
 * <p>This class exemplifies the Strategy pattern, providing a clean interface
 * for selecting between different multi-find algorithms at runtime.</p>
 * 
 * @see NestedFinds
 * @see ConfirmedFinds
 * @see ActionOptions#isKeepLargerMatches()
 */
@Component
public class MultipleFinds implements ActionInterface {

    private final NestedFinds nestedFinds;
    private final ConfirmedFinds confirmedFinds;

    public MultipleFinds(NestedFinds nestedFinds, ConfirmedFinds confirmedFinds) {
        this.nestedFinds = nestedFinds;
        this.confirmedFinds = confirmedFinds;
    }

    /**
     * Delegates to the appropriate multi-find strategy based on ActionOptions configuration.
     * <p>
     * This method examines the keepLargerMatches flag in the ActionOptions to determine
     * which multi-find algorithm to use. This simple delegation pattern allows for
     * runtime selection of complex find behaviors without exposing the complexity
     * to the caller.
     * 
     * <p><b>Strategy selection:</b>
     * <ul>
     *   <li><b>keepLargerMatches = true:</b> Uses ConfirmedFinds to validate matches
     *       through multiple find operations, keeping only confirmed results</li>
     *   <li><b>keepLargerMatches = false:</b> Uses NestedFinds to perform hierarchical
     *       searches within previous results</li>
     * </ul>
     * 
     * <p>The keepLargerMatches name is somewhat misleading - it actually controls
     * whether to use confirmation-based filtering (true) or nested searching (false),
     * not directly about match sizes.</p>
     * 
     * @param matches The ActionResult to populate with found matches. Contains the
     *                ActionOptions that determine which strategy to use.
     * @param objectCollections Variable array of search targets used by the selected
     *                          find strategy
     */
    public void perform(ActionResult matches, ObjectCollection... objectCollections) {
        ActionOptions actionOptions = matches.getActionOptions();
        if (actionOptions.isKeepLargerMatches()) {
            confirmedFinds.perform(matches, objectCollections);
            return;
        }
        nestedFinds.perform(matches, objectCollections);
    }
}
