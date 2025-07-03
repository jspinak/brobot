package io.github.jspinak.brobot.action.composite.multiple.finds;

import io.github.jspinak.brobot.action.ActionInterface;
import io.github.jspinak.brobot.action.basic.find.Find;
import io.github.jspinak.brobot.action.internal.find.scene.SceneAnalysisCollectionBuilder;
import io.github.jspinak.brobot.action.ActionResult;
import io.github.jspinak.brobot.action.ObjectCollection;
import io.github.jspinak.brobot.tools.logging.ConsoleReporter;
import io.github.jspinak.brobot.model.analysis.scene.SceneAnalyses;
import io.github.jspinak.brobot.model.element.SearchRegions;

import org.springframework.stereotype.Component;

import java.util.Arrays;

/**
 * Performs cascading find operations where each result must be confirmed by subsequent searches.
 * <p>
 * ConfirmedFinds implements a validation pattern where initial matches are progressively
 * refined through multiple find operations. Each subsequent find operation searches within
 * the regions of previous matches, creating a nested search that confirms or rejects
 * candidates. Only matches that are validated by all find operations in the sequence
 * are returned as confirmed results.
 * 
 * <p>Key features:</p>
 * <ul>
 *   <li>Cascading search regions - each find searches within previous results</li>
 *   <li>Progressive validation - matches must be confirmed at each step</li>
 *   <li>Scene analysis collection for color-based validation</li>
 *   <li>Early termination when no matches remain</li>
 * </ul>
 * 
 * <p>Common use cases:</p>
 * <ul>
 *   <li>Finding UI elements within specific containers</li>
 *   <li>Validating matches by checking for expected nearby elements</li>
 *   <li>Implementing hierarchical searches (find window, then find button in window)</li>
 *   <li>Confirming matches through multiple visual criteria</li>
 * </ul>
 * 
 * <p><b>ObjectCollection mapping:</b> The nth find action uses the nth ObjectCollection
 * if available, otherwise uses the last provided ObjectCollection. This allows different
 * search criteria at each validation level.</p>
 * 
 * <p>This composite action is essential for robust GUI automation where simple pattern
 * matching might yield false positives that need additional validation.</p>
 * 
 * @see NestedFinds
 * @see MultipleFinds
 * @see Find
 * @see SceneAnalyses
 */
@Component
public class ConfirmedFinds implements ActionInterface {

    private final Find find;
    private final SceneAnalysisCollectionBuilder getSceneAnalysisCollection;

    public ConfirmedFinds(Find find, SceneAnalysisCollectionBuilder getSceneAnalysisCollection) {
        this.find = find;
        this.getSceneAnalysisCollection = getSceneAnalysisCollection;
    }

    @Override
    public ActionInterface.Type getActionType() {
        return ActionInterface.Type.FIND;
    }

    /**
     * Executes a sequence of find operations where each must confirm the previous results.
     * <p>
     * This method implements a cascading validation pattern:
     * <ol>
     *   <li>Performs initial find operation using first ObjectCollection</li>
     *   <li>Creates scene analysis for color-based validation</li>
     *   <li>For each subsequent find action in the sequence:
     *       <ul>
     *         <li>Uses previous matches as search regions</li>
     *         <li>Performs find with corresponding ObjectCollection</li>
     *         <li>Keeps only matches confirmed by both operations</li>
     *         <li>Stops early if no matches remain</li>
     *       </ul>
     *   </li>
     * </ol>
     * 
     * <p><b>Important behaviors:</b>
     * <ul>
     *   <li>Requires at least one ObjectCollection to operate</li>
     *   <li>Modifies the input ActionResult throughout execution</li>
     *   <li>Prints progress information to console for debugging</li>
     *   <li>Non-match results are accumulated for analysis</li>
     * </ul>
     * 
     * @param matches The ActionResult to populate with confirmed matches. Modified
     *                throughout execution with search regions and results.
     * @param objectCollections Variable array of search targets. Index n is used with
     *                          find action n, falling back to the last collection.
     */
    public void perform(ActionResult matches, ObjectCollection... objectCollections) {
        if (objectCollections.length == 0) return;
        SceneAnalyses sceneAnalysisCollection = getSceneAnalysisCollection.
                get(Arrays.asList(objectCollections),1, 0, matches.getActionOptions());
        find.perform(matches, objectCollections);
        matches.setSceneAnalysisCollection(sceneAnalysisCollection);
        if (matches.isEmpty()) {
            ConsoleReporter.println("no matches.");
            return;
        }
        for (int i=1; i<matches.getActionOptions().getFindActions().size(); i++) {
            setSearchRegions(matches);
            matches.getActionOptions().setFind(matches.getActionOptions().getFindActions().get(i));
            ActionResult matches2 = new ActionResult();
            find.perform(matches2, objectCollections);
            matches = matches.getConfirmedMatches(matches2);
            matches.addNonMatchResults(matches2);
            ConsoleReporter.println("# confirmed finds: " + matches.size());
            if (matches.isEmpty()) break; // no need to go further
        }
    }

    /**
     * Selects the appropriate ObjectCollection for a given find operation index.
     * <p>
     * This method implements a fallback strategy where each find action uses its
     * corresponding ObjectCollection by index. If there aren't enough collections,
     * it falls back to the last available collection. This allows flexible mapping
     * of search criteria to validation levels.
     * 
     * <p>Examples:
     * <ul>
     *   <li>3 finds, 3 collections: Each find uses its own collection</li>
     *   <li>3 finds, 2 collections: Find 1→Collection 1, Find 2→Collection 2, Find 3→Collection 2</li>
     *   <li>3 finds, 1 collection: All finds use Collection 1</li>
     * </ul>
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
     * Configures search regions based on previous match results.
     * <p>
     * This method extracts the regions from all current matches and sets them as
     * the search areas for the next find operation. This creates the cascading
     * effect where each find operation searches only within the areas identified
     * by the previous operation.
     * 
     * <p>The method modifies the ActionOptions within the provided ActionResult
     * by replacing its search regions with the match regions.</p>
     * 
     * @param matches The ActionResult containing matches whose regions will become
     *                the new search areas. The ActionOptions within this object
     *                are modified.
     */
    private void setSearchRegions(ActionResult matches) {
        SearchRegions searchRegions = new SearchRegions();
        searchRegions.addSearchRegions(matches.getMatchRegions());
        matches.getActionOptions().setSearchRegions(searchRegions);
    }
}
