package io.github.jspinak.brobot.actions.methods.basicactions.find;

import io.github.jspinak.brobot.actions.actionExecution.ActionInterface;
import io.github.jspinak.brobot.actions.actionOptions.ActionOptions;
import io.github.jspinak.brobot.actions.methods.basicactions.find.color.profiles.SetAllProfiles;
import io.github.jspinak.brobot.actions.methods.basicactions.find.matchManagement.AddNonImageObjects;
import io.github.jspinak.brobot.actions.methods.basicactions.find.matchManagement.AdjustMatches;
import io.github.jspinak.brobot.actions.methods.basicactions.find.matchManagement.OffsetOps;
import io.github.jspinak.brobot.datatypes.state.stateObject.stateImage.StateImage;
import io.github.jspinak.brobot.datatypes.primitives.match.Matches;
import io.github.jspinak.brobot.datatypes.state.ObjectCollection;
import io.github.jspinak.brobot.manageStates.StateMemory;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;

/**
 * All find requests come here first and are then sent to a specific type of find method.
 *
 * <p>Keep in mind that brobot Image objects can contain multiple patterns.
 * Some types of find methods:
 *   First: Returns the first match found per pattern
 *   Best: Returns the best scoring match from all matches
 *   Each: Returns the first or best match found for each pattern
 *   All: Returns all matches for all patterns
 *   Custom: User-defined
 * </p>
 *
 * <p>In addition to Brobot Image Objects, ObjectCollections can contain:
 *   Matches
 *   Regions
 *   Locations
 * These objects are converted directly to MatchObjects and added to the Matches object.
 * </p>
 *
 * <p>Uses only objects in the first ObjectCollection</p>
 *
 * <p>Author: Joshua Spinak</p>
 */
@Component
public class Find implements ActionInterface {

    private final FindFunctions findFunctions;
    private final StateMemory stateMemory;
    private final AddNonImageObjects addNonImageObjects;
    private final AdjustMatches adjustMatches;
    private final SetAllProfiles setAllProfiles;
    private final OffsetOps offsetOps;

    public Find(FindFunctions findFunctions, StateMemory stateMemory,
                AddNonImageObjects addNonImageObjects, AdjustMatches adjustMatches,
                SetAllProfiles setAllProfiles, OffsetOps offsetOps) {
        this.findFunctions = findFunctions;
        this.stateMemory = stateMemory;
        this.addNonImageObjects = addNonImageObjects;
        this.adjustMatches = adjustMatches;
        this.setAllProfiles = setAllProfiles;
        this.offsetOps = offsetOps;
    }

    /**
     * Find is called sometimes outside of Action.perform(...) when used in another Action. This is done
     * to avoid creating Snapshots for each Find sequence. When called directly, the following
     * operations do not occur:
     * - Wait.pauseBeforeBegin
     * - Matches.setSuccess
     * - Matches.setDuration
     * - Matches.saveSnapshots
     * - Wait.pauseAfterEnd
     */
    public void perform(Matches matches, ObjectCollection... objectCollections) {
        ActionOptions actionOptions = matches.getActionOptions();
        //int actionId = actionLifecycleManagement.newActionLifecycle(actionOptions);
        createColorProfilesWhenNecessary(actionOptions, objectCollections);
        matches.setMaxMatches(actionOptions.getMaxMatchesToActOn());
        offsetOps.addOffsetAsOnlyMatch(List.of(objectCollections), matches, true);
        findFunctions.get(actionOptions).accept(matches, List.of(objectCollections));
        stateMemory.adjustActiveStatesWithMatches(matches);
        Matches nonImageMatches = addNonImageObjects.getOtherObjectsDirectlyAsMatchObjects(objectCollections[0]);
        matches.addMatchObjects(nonImageMatches);
        matches.getMatchList().forEach(m -> adjustMatches.adjust(m, actionOptions));
        offsetOps.addOffsetAsLastMatch(matches, actionOptions);
    }

    private void createColorProfilesWhenNecessary(ActionOptions actionOptions, ObjectCollection... objectCollections) {
        if (!actionOptions.getFindActions().contains(ActionOptions.Find.COLOR)) return;
        List<StateImage> imgs = new ArrayList<>();
        if (objectCollections.length >= 1) imgs.addAll(objectCollections[0].getStateImage_s());
        if (objectCollections.length >= 2) imgs.addAll(objectCollections[1].getStateImage_s());
        List<StateImage> imagesWithoutColorProfiles = new ArrayList<>();
        for (StateImage img : imgs) {
            if (img.getKmeansProfilesAllSchemas() == null) {
                imagesWithoutColorProfiles.add(img);
            }
        }
        imagesWithoutColorProfiles.forEach(setAllProfiles::setMatsAndColorProfiles);
    }

}