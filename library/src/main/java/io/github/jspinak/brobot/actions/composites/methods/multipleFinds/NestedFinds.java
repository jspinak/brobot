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

    public Matches perform(ActionOptions actionOptions, ObjectCollection... objectCollections) {
        if (objectCollections.length == 0) return new Matches();
        Matches matches = new Matches();
        for (int i=0; i<actionOptions.getFindActions().size(); i++) {
            ObjectCollection objColl = getObjColl(i, objectCollections);
            actionOptions.setFind(actionOptions.getFindActions().get(i));
            matches = find.perform(actionOptions, objColl);
            if (matches.isEmpty()) return matches; // no need to go further
            setSearchRegions(matches, actionOptions);
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
