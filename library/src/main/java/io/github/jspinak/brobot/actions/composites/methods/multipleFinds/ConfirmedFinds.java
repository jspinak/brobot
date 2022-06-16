package io.github.jspinak.brobot.actions.composites.methods.multipleFinds;

import io.github.jspinak.brobot.actions.actionExecution.ActionInterface;
import io.github.jspinak.brobot.actions.actionOptions.ActionOptions;
import io.github.jspinak.brobot.actions.methods.basicactions.find.Find;
import io.github.jspinak.brobot.datatypes.primitives.match.Matches;
import io.github.jspinak.brobot.datatypes.state.ObjectCollection;
import io.github.jspinak.brobot.datatypes.state.stateObject.stateImageObject.SearchRegions;
import org.springframework.stereotype.Component;

/**
 * Performs nested Find actions, in the order they appear in the FindActions list in
 * ActionOptions. The matches found by the first Action are used as SearchRegions
 * for the following Actions. The corresponding ObjectCollection is used with the Find Action
 * (for example, ObjectCollection #2 with Find Action #2), unless there is no corresponding
 * ObjectCollection, in which case the last ObjectCollection parameter will be used. All Find
 * operations after the first Find operation as used to confirm or reject matches in the first
 * Find operation.
 */
@Component
public class ConfirmedFinds implements ActionInterface {

    private Find find;

    public ConfirmedFinds(Find find) {
        this.find = find;
    }

    public Matches perform(ActionOptions actionOptions, ObjectCollection... objectCollections) {
        if (objectCollections.length == 0) return new Matches();
        Matches matches = find.perform(actionOptions, objectCollections);
        if (matches.isEmpty()) return matches;
        for (int i=1; i<actionOptions.getFindActions().size(); i++) {
            setSearchRegions(matches, actionOptions);
            actionOptions.setFind(actionOptions.getFindActions().get(i));
            ObjectCollection objColl = getObjColl(i, objectCollections);
            Matches matches2 = find.perform(actionOptions, objColl);
            matches = matches.getConfirmedMatches(matches2);
        }
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
