package io.github.jspinak.brobot.actions.composites.methods.multipleFinds;

import io.github.jspinak.brobot.actions.actionExecution.ActionInterface;
import io.github.jspinak.brobot.actions.actionOptions.ActionOptions;
import io.github.jspinak.brobot.actions.methods.basicactions.find.Find;
import io.github.jspinak.brobot.datatypes.primitives.match.MatchObject;
import io.github.jspinak.brobot.datatypes.primitives.match.Matches;
import io.github.jspinak.brobot.datatypes.state.ObjectCollection;
import io.github.jspinak.brobot.datatypes.state.stateObject.stateImageObject.SearchRegions;
import io.github.jspinak.brobot.reports.Report;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;

/**
 * Performs nested Find actions, in the order they appear in the FindActions list in
 * ActionOptions. The matches found by the previous Action are used as SearchRegions
 * for the following Action. The corresponding ObjectCollection is used with the Find Action
 * (for example, ObjectCollection #2 with Find Action #2), unless there is no corresponding
 * ObjectCollection, in which case the last ObjectCollection parameter will be used.
 */
@Component
public class NestedFinds implements ActionInterface {

    private Find find;

    public NestedFinds(Find find) {
        this.find = find;
    }

    /**
     * Here, nested and confirmed finds seem to have an object collection for each find. That would allow
     * the first find to be on a different object collection than the second find. This doesn't work with
     * the current implementation of color finds though, which have a collection for target images, a collection
     * for background images, and a collection for scenes.
     *
     * @param actionOptions the Action's configuration
     * @param objectCollections the images and scenes to use
     * @return the image matches
     */
    public Matches perform(ActionOptions actionOptions, ObjectCollection... objectCollections) {
        if (objectCollections.length == 0) return new Matches();
        List<MatchObject> initialMatches = new ArrayList<>();
        Matches matches = new Matches();
        for (int i=0; i<actionOptions.getFindActions().size(); i++) {
            Report.println("nested find #" + i + ": " + actionOptions.getFindActions().get(i));
            actionOptions.setFind(actionOptions.getFindActions().get(i));
            matches = find.perform(actionOptions, objectCollections);
            if (matches.isEmpty()) return matches; // no need to go further
            if (i==0) initialMatches.addAll(matches.getMatchObjects());
            if (i<actionOptions.getFindActions().size()-1) setSearchRegions(matches, actionOptions);
        }
        matches.setInitialMatchObjects(initialMatches);
        return matches;
    }

    private ObjectCollection getObjColl(int findIndex, ObjectCollection... objectCollections) {
        int index = Math.min(findIndex, objectCollections.length - 1);
        return objectCollections[index];
    }

    private void setSearchRegions(Matches matches, ActionOptions actionOptions) {
        SearchRegions searchRegions = new SearchRegions();
        searchRegions.addSearchRegions(matches.getMatchRegions());
        actionOptions.setSearchRegions(searchRegions);
    }
}
