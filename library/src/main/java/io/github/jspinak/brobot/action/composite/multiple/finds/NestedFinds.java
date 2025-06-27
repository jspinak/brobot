package io.github.jspinak.brobot.action.composite.multiple.finds;

import io.github.jspinak.brobot.action.ActionInterface;
import io.github.jspinak.brobot.action.ActionOptions;
import io.github.jspinak.brobot.action.basic.find.Find;
import io.github.jspinak.brobot.action.ActionResult;
import io.github.jspinak.brobot.action.ObjectCollection;
import io.github.jspinak.brobot.tools.logging.ConsoleReporter;
import io.github.jspinak.brobot.model.element.SearchRegions;
import io.github.jspinak.brobot.model.match.Match;

import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;

/**
 * Executes hierarchical find operations where each search is constrained to previous results.
 * <p>
 * NestedFinds implements a progressive refinement pattern where each find operation
 * searches within the regions discovered by the previous operation. This creates a
 * hierarchical search that progressively narrows down results, making it ideal for
 * finding elements within specific containers or validating spatial relationships
 * between UI components.
 * 
 * <p>Key features:</p>
 * <ul>
 *   <li>Sequential search refinement - each find searches within previous matches</li>
 *   <li>Hierarchical containment validation</li>
 *   <li>Preserves initial match list for reference</li>
 *   <li>Early termination when no matches found</li>
 * </ul>
 * 
 * <p>Common use cases:</p>
 * <ul>
 *   <li>Finding buttons within specific dialogs or panels</li>
 *   <li>Locating text within table cells or form fields</li>
 *   <li>Validating parent-child relationships in UI hierarchies</li>
 *   <li>Implementing "find X within Y" search patterns</li>
 * </ul>
 * 
 * <p><b>Differences from ConfirmedFinds:</b></p>
 * <ul>
 *   <li>NestedFinds progressively narrows search areas</li>
 *   <li>ConfirmedFinds validates matches through multiple criteria</li>
 *   <li>NestedFinds preserves hierarchy information</li>
 *   <li>ConfirmedFinds focuses on match validation</li>
 * </ul>
 * 
 * <p>The nested approach is particularly powerful for complex UI layouts where
 * elements can only be uniquely identified by their container context.</p>
 * 
 * @see ConfirmedFinds
 * @see MultipleFinds
 * @see Find
 * @see SearchRegions
 */
@Component
public class NestedFinds implements ActionInterface {

    private final Find find;

    public NestedFinds(Find find) {
        this.find = find;
    }

    /**
     * Executes a sequence of find operations with progressively narrowing search regions.
     * <p>
     * This method implements a hierarchical search pattern where each find operation
     * is constrained to search within the regions found by the previous operation.
     * This creates a nested search structure that's particularly effective for finding
     * elements within specific containers or validating spatial containment relationships.
     * 
     * <p><b>Execution flow:</b></p>
     * <ol>
     *   <li>Performs first find using the full search area</li>
     *   <li>Saves initial matches for later reference</li>
     *   <li>For each subsequent find operation:
     *       <ul>
     *         <li>Updates search regions to previous match areas</li>
     *         <li>Performs find within those constrained regions</li>
     *         <li>Exits early if no matches found</li>
     *       </ul>
     *   </li>
     *   <li>Preserves initial match list in results</li>
     * </ol>
     * 
     * <p><b>ObjectCollection mapping:</b> Each find action uses its corresponding
     * ObjectCollection by index. If there are fewer collections than find actions,
     * the last collection is reused. This allows different search criteria at each
     * nesting level while gracefully handling partial specifications.</p>
     * 
     * <p><b>Important behaviors:</b></p>
     * <ul>
     *   <li>Modifies the input ActionResult throughout execution</li>
     *   <li>ActionOptions are modified to update search regions</li>
     *   <li>Early termination on empty results (optimization)</li>
     *   <li>Debug output shows nesting level and find type</li>
     * </ul>
     * 
     * <p><b>Note on color finds:</b> The current implementation has limitations with
     * color-based finds which require separate collections for targets, backgrounds,
     * and scenes. This multi-collection approach doesn't map cleanly to the nested
     * find pattern.</p>
     * 
     * @param matches The ActionResult to populate with nested search results. Both
     *                the matches and the ActionOptions within are modified during execution.
     * @param objectCollections Variable array of search targets. Each find action uses
     *                          the collection at its index, reusing the last if needed.
     */
    public void perform(ActionResult matches, ObjectCollection... objectCollections) {
        ActionOptions actionOptions = matches.getActionOptions();
        if (objectCollections.length == 0) return;
        List<Match> initialMatches = new ArrayList<>();
        for (int i=0; i<actionOptions.getFindActions().size(); i++) {
            ConsoleReporter.println("nested find #" + i + ": " + actionOptions.getFindActions().get(i));
            actionOptions.setFind(actionOptions.getFindActions().get(i));
            find.perform(matches, objectCollections);
            if (matches.isEmpty()) return; // no need to go further
            if (i==0) initialMatches.addAll(matches.getMatchList());
            if (i<actionOptions.getFindActions().size()-1) setSearchRegions(matches, actionOptions);
        }
        matches.setInitialMatchList(initialMatches);
    }

    /**
     * Selects the appropriate ObjectCollection for a given find operation index.
     * <p>
     * This method implements a fallback strategy where each find action uses its
     * corresponding ObjectCollection by index. If there aren't enough collections,
     * it falls back to the last available collection. This allows flexible mapping
     * of search criteria to nesting levels.
     * 
     * <p>Note: This method is currently unused but provides a pattern for future
     * enhancements where different ObjectCollections could be used at each nesting level.</p>
     * 
     * @param findIndex The index of the current find operation (0-based)
     * @param objectCollections The available object collections
     * @return The ObjectCollection to use for this find operation
     */
    private ObjectCollection getObjColl(int findIndex, ObjectCollection... objectCollections) {
        int index = Math.min(findIndex, objectCollections.length - 1);
        return objectCollections[index];
    }

    /**
     * Updates search regions to constrain the next find operation to previous match areas.
     * <p>
     * This method is the core of the nested find behavior. It extracts regions from
     * all current matches and sets them as the search areas for the next find operation.
     * This creates the hierarchical search pattern where each level searches only
     * within the areas identified by the previous level.
     * 
     * <p>The method modifies the provided ActionOptions by replacing its search regions,
     * which affects all subsequent find operations until the regions are updated again.</p>
     * 
     * @param matches The ActionResult containing matches whose regions will become
     *                the new search areas
     * @param actionOptions The ActionOptions to modify with new search regions. This
     *                      parameter is modified by replacing its search regions.
     */
    private void setSearchRegions(ActionResult matches, ActionOptions actionOptions) {
        SearchRegions searchRegions = new SearchRegions();
        searchRegions.addSearchRegions(matches.getMatchRegions());
        actionOptions.setSearchRegions(searchRegions);
    }
}
