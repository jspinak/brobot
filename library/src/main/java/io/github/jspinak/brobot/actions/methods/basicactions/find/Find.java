package io.github.jspinak.brobot.actions.methods.basicactions.find;

import io.github.jspinak.brobot.actions.actionExecution.ActionInterface;
import io.github.jspinak.brobot.actions.actionExecution.MatchesInitializer;
import io.github.jspinak.brobot.actions.actionExecution.actionLifecycle.ActionLifecycleManagement;
import io.github.jspinak.brobot.actions.actionOptions.ActionOptions;
import io.github.jspinak.brobot.actions.methods.basicactions.find.color.pixelAnalysis.GetScenes;
import io.github.jspinak.brobot.actions.methods.basicactions.find.color.pixelAnalysis.Scene;
import io.github.jspinak.brobot.actions.methods.basicactions.find.color.pixelAnalysis.SceneAnalysis;
import io.github.jspinak.brobot.actions.methods.basicactions.find.color.pixelAnalysis.SceneAnalysisCollection;
import io.github.jspinak.brobot.actions.methods.basicactions.find.color.profiles.SetAllProfiles;
import io.github.jspinak.brobot.actions.methods.sikuliWrappers.find.UseDefinedRegion;
import io.github.jspinak.brobot.datatypes.primitives.image.StateImage_;
import io.github.jspinak.brobot.datatypes.primitives.match.MatchObject_;
import io.github.jspinak.brobot.datatypes.primitives.match.Matches;
import io.github.jspinak.brobot.datatypes.state.ObjectCollection;
import io.github.jspinak.brobot.datatypes.state.stateObject.stateImage.StateImage;
import io.github.jspinak.brobot.manageStates.StateMemory;
import io.github.jspinak.brobot.mock.MockStatus;
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
    private final FindImageOrRIP findImageOrRIP;
    private final GetScenes getScenes;
    private final MatchesInitializer matchesInitializer;

    public Find(FindFunctions findFunctions, StateMemory stateMemory, MockStatus mockStatus,
                AddNonImageObjects addNonImageObjects, AdjustMatches adjustMatches,
                UseDefinedRegion useDefinedRegion, SetAllProfiles setAllProfiles,
                ActionLifecycleManagement actionLifecycleManagement, OffsetOps offsetOps,
                FindImageOrRIP findImageOrRIP, GetScenes getScenes, MatchesInitializer matchesInitializer) {
        this.findFunctions = findFunctions;
        this.stateMemory = stateMemory;
        this.mockStatus = mockStatus;
        this.addNonImageObjects = addNonImageObjects;
        this.adjustMatches = adjustMatches;
        this.useDefinedRegion = useDefinedRegion;
        this.setAllProfiles = setAllProfiles;
        this.actionLifecycleManagement = actionLifecycleManagement;
        this.offsetOps = offsetOps;
        this.findImageOrRIP = findImageOrRIP;
        this.getScenes = getScenes;
        this.matchesInitializer = matchesInitializer;
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
    public void perform(Matches matches, ActionOptions actionOptions, ObjectCollection... objectCollections) {
        //int actionId = actionLifecycleManagement.newActionLifecycle(actionOptions);
        createColorProfilesWhenNecessary(actionOptions, objectCollections);
        matches.setMaxMatches(actionOptions.getMaxMatchesToActOn());
        offsetOps.addOffsetAsOnlyMatch(List.of(objectCollections), matches, actionOptions, true);
        if (objectCollections.length == 0) return;
        getImageMatches(matches, actionOptions, objectCollections);
        if (matches.hasImageMatches()) stateMemory.adjustActiveStatesWithMatches(matches);
        Matches nonImageMatches = addNonImageObjects.getOtherObjectsDirectlyAsMatchObjects(objectCollections[0]);
        matches.addMatchObjects(nonImageMatches);
        matches.getMatches().forEach(m -> adjustMatches.adjust(m, actionOptions));
        offsetOps.addOffsetAsLastMatch(matches, actionOptions);
    }

    private void createColorProfilesWhenNecessary(ActionOptions actionOptions, ObjectCollection... objectCollections) {
        if (!actionOptions.getFindActions().contains(ActionOptions.Find.COLOR)) return;
        List<StateImage> imgs = new ArrayList<>();
        if (objectCollections.length >= 1) imgs.addAll(objectCollections[0].getStateImages());
        if (objectCollections.length >= 2) imgs.addAll(objectCollections[1].getStateImages());
        List<StateImage> imagesWithoutColorProfiles = new ArrayList<>();
        for (StateImage img : imgs) {
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
        /*
        Execute the find until the exit condition is achieved. For example, a Find.VANISH will execute until
        the images are no longer found. The results for each execution are added to the Matches object.
         */
        while (actionLifecycleManagement.continueActionIfNotFound(matches)) {
            //Report.println(""+ actionLifecycleManagement.getCompletedRepetitions(actionId));
            mockStatus.addMockPerformed();
            Matches matches1 = new Matches();
            findFunctions.get(actionOptions).accept(matches1, actionOptions, List.of(objectCollections));
            matches.addAllResults(matches1);
            actionLifecycleManagement.incrementCompletedRepetitions(matches.getActionId());
        }
    }

    /**
     * The ActionLifecycleManagement takes care of the different Find types during execution.
     * @param actionOptions the action's options
     * @param objectCollections all objects to use with this action
     * @return the matches
     */
    private Matches getImageMatches(Matches matches, ActionOptions actionOptions,
                                    ObjectCollection... objectCollections) {
        if (actionOptions.isUseDefinedRegion()) {
            matches.addAllResults(useDefinedRegion.useRegion(objectCollections[0]));
            return matches;
        }
        /*
        Find.EACH requires a Matches object for each image. For example, if there are 3 images, Find.EACH will
        return 3 MatchObjects.
         */
        List<Matches> matchesList = new ArrayList<>();
        List<StateImage_> stateImages = objectCollections[0].getStateImage_s();
        stateImages.forEach(sI -> matchesList.add(new Matches()));
        /*
        Execute the find until the exit condition is achieved. For example, a Find.VANISH will execute until
        the images are no longer found. The results for each execution are added to the Matches object.
         */
        Matches allMatches = new Matches();
        while (actionLifecycleManagement.continueActionIfNotFound(matches)) {
            List<Scene> scenes = getScenes.getScenes(actionOptions, List.of(objectCollections), 1, 0);
            List<MatchObject_> matchesObjects = findImageOrRIP.find_(matches, actionOptions, stateImages, scenes, matchesList);
            scenes.forEach(scene -> {
                SceneAnalysis sceneAnalysis = new SceneAnalysis(scene);
                sceneAnalysis.setMatchList(matches.getMatches());
                matches.getSceneAnalysisCollection().add(sceneAnalysis);
            });
            matchesObjects.forEach(allMatches::add);
            actionLifecycleManagement.incrementCompletedRepetitions(matches.getActionId());
        }
        if (actionOptions.getFind() == ActionOptions.Find.BEST || actionOptions.getFind() == ActionOptions.Find.FIRST) {
            allMatches.getBestMatch().ifPresent(matches::add);
        }
        if (actionOptions.getFind() == ActionOptions.Find.ALL) {
            matchesList.forEach(matches::addMatchObjects);
        }
        if (actionOptions.getFind() == ActionOptions.Find.EACH) {
            matchesList.forEach(m -> m.getBestMatch().ifPresent(matches::add));
        }
        return matches;
    }

}