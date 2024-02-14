package io.github.jspinak.brobot.actions.composites.methods.multipleFinds;

import io.github.jspinak.brobot.actions.actionExecution.ActionInterface;
import io.github.jspinak.brobot.actions.methods.basicactions.find.Find;
import io.github.jspinak.brobot.actions.methods.basicactions.find.color.pixelAnalysis.GetSceneAnalysisCollection;
import io.github.jspinak.brobot.actions.methods.basicactions.find.color.pixelAnalysis.SceneAnalysisCollection;
import io.github.jspinak.brobot.datatypes.primitives.match.Matches;
import io.github.jspinak.brobot.datatypes.state.ObjectCollection;
import io.github.jspinak.brobot.datatypes.primitives.region.SearchRegions;
import io.github.jspinak.brobot.reports.Report;
import org.springframework.stereotype.Component;

import java.util.Arrays;

/**
 * Performs nested Find actions, in the order they appear in the FindActions list in
 * ActionOptions. The matches found by the first Action are used as SearchRegions
 * for the following Actions. The corresponding ObjectCollection is used with the Find Action
 * (for example, ObjectCollection #2 with Find Action #2), unless there is no corresponding
 * ObjectCollection, in which case the last ObjectCollection parameter will be used. All Find
 * operations after the first Find operation as used to confirm or reject matches in the first
 * Find operation. Final confirmed matches are found by every find operation.
 */
@Component
public class ConfirmedFinds implements ActionInterface {

    private final Find find;
    private final GetSceneAnalysisCollection getSceneAnalysisCollection;

    public ConfirmedFinds(Find find, GetSceneAnalysisCollection getSceneAnalysisCollection) {
        this.find = find;
        this.getSceneAnalysisCollection = getSceneAnalysisCollection;
    }

    public void perform(Matches matches, ObjectCollection... objectCollections) {
        if (objectCollections.length == 0) return;
        SceneAnalysisCollection sceneAnalysisCollection = getSceneAnalysisCollection.
                get(Arrays.asList(objectCollections),1, 0, matches.getActionOptions());
        find.perform(matches, objectCollections);
        matches.setSceneAnalysisCollection(sceneAnalysisCollection);
        if (matches.isEmpty()) {
            Report.println("no matches.");
            return;
        }
        for (int i=1; i<matches.getActionOptions().getFindActions().size(); i++) {
            setSearchRegions(matches);
            matches.getActionOptions().setFind(matches.getActionOptions().getFindActions().get(i));
            Matches matches2 = new Matches();
            find.perform(matches2, objectCollections);
            matches = matches.getConfirmedMatches(matches2);
            matches.addNonMatchResults(matches2);
            Report.println("# confirmed finds: " + matches.size());
            if (matches.isEmpty()) break; // no need to go further
        }
    }

    private ObjectCollection getObjColl(int findIndex, ObjectCollection... objectCollections) {
        int index = Math.min(findIndex, objectCollections.length - 1);
        return objectCollections[index];
    }

    private void setSearchRegions(Matches matches) {
        SearchRegions searchRegions = new SearchRegions();
        searchRegions.addSearchRegions(matches.getMatchRegions());
        matches.getActionOptions().setSearchRegions(searchRegions);
    }
}
