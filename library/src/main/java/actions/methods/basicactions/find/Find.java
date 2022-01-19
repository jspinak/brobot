package actions.methods.basicactions.find;

import com.brobot.multimodule.actions.actionExecution.ActionInterface;
import com.brobot.multimodule.actions.actionOptions.ActionOptions;
import com.brobot.multimodule.actions.methods.sikuliWrappers.find.UseDefinedRegion;
import com.brobot.multimodule.actions.methods.time.Time;
import com.brobot.multimodule.database.primitives.match.Matches;
import com.brobot.multimodule.database.state.ObjectCollection;
import com.brobot.multimodule.manageStates.StateMemory;
import com.brobot.multimodule.mock.MockStatus;
import org.springframework.stereotype.Component;

/**
 * All find requests come here first and are then sent to a specific type of find method.
 *
 * <p>Keep in mind that brobot Image object can contain multiple patterns.
 * The different types of find methods are
 * First: Returns the first match found
 * Best: Returns the best scoring match from all matches
 * Each: Returns the first match found for each pattern
 * All: Returns all matches for all patterns
 * Custom: User-defined
 * </p>
 *
 * <p>In addition to Brobot Image Objects, ObjectCollections can contain:
 * Matches
 * Regions
 * Locations
 * These objects are converted directly to MatchObjects and added to the Matches object.
 * </p>
 *
 * <p>Uses only objects in the first ObjectCollection</p>
 *
 * <p>Author: Joshua Spinak</p>
 */
@Component
public class Find implements ActionInterface {

    private FindFunctions findFunctions;
    private StateMemory stateMemory;
    private Time time;
    private MockStatus mockStatus;
    private AddNonImageObjects addNonImageObjects;
    private AdjustMatches adjustMatches;
    private UseDefinedRegion useDefinedRegion;

    public Find(FindFunctions findFunctions, StateMemory stateMemory, Time time, MockStatus mockStatus,
                AddNonImageObjects addNonImageObjects, AdjustMatches adjustMatches,
                UseDefinedRegion useDefinedRegion) {
        this.findFunctions = findFunctions;
        this.stateMemory = stateMemory;
        this.time = time;
        this.mockStatus = mockStatus;
        this.addNonImageObjects = addNonImageObjects;
        this.adjustMatches = adjustMatches;
        this.useDefinedRegion = useDefinedRegion;
    }

    /**
     * Find is called outside of Action.perform(...) when used in another Action. This is done
     * to avoid creating Snapshots for each Find sequence. When called directly, the following
     * operations do not occur:
     * - Wait.pauseBeforeBegin
     * - Matches.setSuccess
     * - Matches.setDuration
     * - Matches.saveSnapshots
     * - Wait.pauseAfterEnd
     */
    public Matches perform(ActionOptions actionOptions, ObjectCollection... objectCollections) {
        time.setFindStartTime(); // for when Find is called outside of Action.perform
        Matches matches = new Matches();
        matches.setMaxMatches(actionOptions.getMaxMatchesToActOn());
        if (objectCollections.length == 0) return matches;
        ObjectCollection objectCollection = objectCollections[0];
        getImageMatches(actionOptions, matches, objectCollection);
        stateMemory.adjustActiveStatesWithMatches(matches);
        matches.addAll(addNonImageObjects.getOtherObjectsDirectlyAsMatchObjects(objectCollection));
        matches.getMatches().forEach(m -> adjustMatches.adjust(m, actionOptions));
        return matches;
    }

    private void getImageMatches(ActionOptions actionOptions, Matches matches,
                                    ObjectCollection objectCollection) {
        if (actionOptions.isUseDefinedRegion()) {
            matches.addAll(useDefinedRegion.useRegion(objectCollection));
            return;
        }
        int timesSearched = 0;
        while (continueSearching(actionOptions, matches, timesSearched, objectCollection)) {
            mockStatus.addMockPerformed();
            matches.addAll(findFunctions.get(actionOptions)
                    .apply(actionOptions, objectCollection.getStateImages()));
            timesSearched++;
        }
    }

    private boolean continueSearching(ActionOptions actionOptions, Matches matches,
                                      int timesSearched, ObjectCollection objectCollection) {
        if (timesSearched == 0) return true;
        if (time.expired(actionOptions.getAction(), actionOptions.getMaxWait())) return false;
        return matches.isEmpty() && !objectCollection.getStateImages().isEmpty();
    }

}