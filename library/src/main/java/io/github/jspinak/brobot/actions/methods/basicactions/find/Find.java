package io.github.jspinak.brobot.actions.methods.basicactions.find;

import io.github.jspinak.brobot.actions.actionExecution.ActionInterface;
import io.github.jspinak.brobot.actions.actionExecution.actionLifecycle.ActionLifecycleManagement;
import io.github.jspinak.brobot.actions.actionOptions.ActionOptions;
import io.github.jspinak.brobot.actions.methods.basicactions.find.color.profiles.SetAllProfiles;
import io.github.jspinak.brobot.actions.methods.sikuliWrappers.find.UseDefinedRegion;
import io.github.jspinak.brobot.datatypes.primitives.match.Matches;
import io.github.jspinak.brobot.datatypes.state.ObjectCollection;
import io.github.jspinak.brobot.datatypes.state.stateObject.stateImageObject.StateImageObject;
import io.github.jspinak.brobot.manageStates.StateMemory;
import io.github.jspinak.brobot.mock.MockStatus;
import io.github.jspinak.brobot.reports.Report;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;

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
    private MockStatus mockStatus;
    private AddNonImageObjects addNonImageObjects;
    private AdjustMatches adjustMatches;
    private UseDefinedRegion useDefinedRegion;
    private SetAllProfiles setAllProfiles;
    private ActionLifecycleManagement actionLifecycleManagement;
    private OffsetOps offsetOps;

    int actionId;

    public Find(FindFunctions findFunctions, StateMemory stateMemory, MockStatus mockStatus,
                AddNonImageObjects addNonImageObjects, AdjustMatches adjustMatches,
                UseDefinedRegion useDefinedRegion, SetAllProfiles setAllProfiles,
                ActionLifecycleManagement actionLifecycleManagement, OffsetOps offsetOps) {
        this.findFunctions = findFunctions;
        this.stateMemory = stateMemory;
        this.mockStatus = mockStatus;
        this.addNonImageObjects = addNonImageObjects;
        this.adjustMatches = adjustMatches;
        this.useDefinedRegion = useDefinedRegion;
        this.setAllProfiles = setAllProfiles;
        this.actionLifecycleManagement = actionLifecycleManagement;
        this.offsetOps = offsetOps;
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
        actionId = actionLifecycleManagement.newActionLifecycle(actionOptions);
        createColorProfilesWhenNecessary(actionOptions, objectCollections);
        Matches matches = new Matches();
        matches.setMaxMatches(actionOptions.getMaxMatchesToActOn());
        offsetOps.addOffsetAsOnlyMatch(List.of(objectCollections), matches, actionOptions, true);
        if (objectCollections.length == 0) return matches;
        getImageMatches(actionOptions, matches, objectCollections);
        if (matches.hasImageMatches()) stateMemory.adjustActiveStatesWithMatches(matches);
        Matches nonImageMatches = addNonImageObjects.getOtherObjectsDirectlyAsMatchObjects(objectCollections[0]);
        matches.addMatchObjects(nonImageMatches);
        matches.getMatches().forEach(m -> adjustMatches.adjust(m, actionOptions));
        offsetOps.addOffsetAsLastMatch(matches, actionOptions);
        return matches;
    }

    private void createColorProfilesWhenNecessary(ActionOptions actionOptions, ObjectCollection... objectCollections) {
        if (!actionOptions.getFindActions().contains(ActionOptions.Find.COLOR)) return;
        List<StateImageObject> imgs = new ArrayList<>();
        if (objectCollections.length >= 1) imgs.addAll(objectCollections[0].getStateImages());
        if (objectCollections.length >= 2) imgs.addAll(objectCollections[1].getStateImages());
        List<StateImageObject> imagesWithoutColorProfiles = new ArrayList<>();
        for (StateImageObject img : imgs) {
            if (img.getDynamicImage().getInsideKmeansProfiles() == null) {
                imagesWithoutColorProfiles.add(img);
            }
        }
        imagesWithoutColorProfiles.forEach(img -> setAllProfiles.setMatsAndColorProfiles(img));
    }

    private boolean containsImages(ObjectCollection... objectCollections) {
        for (ObjectCollection objColl : objectCollections) {
            if (!objColl.getStateImages().isEmpty()) return true;
        }
        return false;
    }

    private void getImageMatches(ActionOptions actionOptions, Matches matches,
                                    ObjectCollection... objectCollections) {
        if (actionOptions.isUseDefinedRegion()) {
            matches.addAllResults(useDefinedRegion.useRegion(objectCollections[0]));
            return;
        }
        while (actionLifecycleManagement.continueActionIfNotFound(actionId, matches)) {
            mockStatus.addMockPerformed();
            Matches matches1 = findFunctions.get(actionOptions).apply(actionOptions, List.of(objectCollections));
            matches.addAllResults(matches1);
            actionLifecycleManagement.incrementCompletedRepetitions(actionId);
        }
    }

}