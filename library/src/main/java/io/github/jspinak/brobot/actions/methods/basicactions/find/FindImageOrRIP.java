package io.github.jspinak.brobot.actions.methods.basicactions.find;

import io.github.jspinak.brobot.actions.actionExecution.actionLifecycle.ActionLifecycleManagement;
import io.github.jspinak.brobot.actions.actionOptions.ActionOptions;
import io.github.jspinak.brobot.actions.methods.basicactions.find.color.pixelAnalysis.GetScenes;
import io.github.jspinak.brobot.actions.methods.basicactions.find.color.pixelAnalysis.Scene;
import io.github.jspinak.brobot.actions.methods.basicactions.find.color.pixelAnalysis.SceneAnalysis;
import io.github.jspinak.brobot.actions.methods.sikuliWrappers.find.FindAll;
import io.github.jspinak.brobot.datatypes.primitives.image.StateImage_;
import io.github.jspinak.brobot.datatypes.primitives.match.MatchObject_;
import io.github.jspinak.brobot.datatypes.primitives.match.Matches;
import io.github.jspinak.brobot.datatypes.state.ObjectCollection;
import org.springframework.stereotype.Component;

import java.util.*;

/**
 * Sends the Image object to either FindImage or FindRIP depending on whether the Image location can vary.
 * Contains methods for Find options FIRST, EACH, ALL, BEST
 */
@Component
public class FindImageOrRIP {

    private GetScenes getScenes;
    private ActionLifecycleManagement actionLifecycleManagement;
    private final FindAll findAll;

    private final Map<Boolean, FindImageObject> findMethod = new HashMap<>();

    public FindImageOrRIP(FindImage findImage, FindRIP findRIP, GetScenes getScenes,
                          ActionLifecycleManagement actionLifecycleManagement,
                          FindAll findAll) {
        this.getScenes = getScenes;
        this.actionLifecycleManagement = actionLifecycleManagement;
        this.findAll = findAll;
        findMethod.put(false, findImage);
        findMethod.put(true, findRIP);
    }

    /**
     * For Find.FIRST or Find.ALL, depending on the ActionOptions.
     * Find on all scenes in the first objectCollectiion
     * @param actionOptions holds the action configuration.
     * @param objectCollections images are taken from the first ObjectCollection.
     * @return a Matches object with all matches found.
     */
    public void find(Matches matches, ActionOptions actionOptions, List<ObjectCollection> objectCollections) {
        actionLifecycleManagement.printActionOnce(matches.getActionId());
        List<Scene> scenes = getScenes.getScenes(actionOptions, objectCollections);
        //Report.println(scenes.size() + " scenes found");
        /*
        There won't be scenes if the action is performed on a screenshot. However, we need to give
        any MatchObjects an associated scene name. Screenshots have names "screenshot0.png", "screenshot1.png", etc.
        In this case, a screenshot will be taken and saved in the Matches object once the action is finished.
        The name of the screenshot will be "screenshot0" when only one screenshot is taken, which is every
        case in the current Brobot version involving execution with Sikuli. The MatchObjects should be given the
        scene name "screenshot0".
         */
        scenes.forEach(scene -> {
            objectCollections.get(0).getStateImages().forEach(image -> {
                matches.addAllResults(findMethod.get(image.isFixed()).
                        find(actionOptions, image, scene));
            });
        });
        // add each SceneAnalysis to Matches without doing a color analysis of the scene
        List<Scene> scenesWithScreenshots = getScenes.getScenes(actionOptions, objectCollections);
        List<SceneAnalysis> sceneAnalyses = new ArrayList<>();
        scenesWithScreenshots.forEach(scene -> sceneAnalyses.add(new SceneAnalysis(scene)));
        matches.getSceneAnalysisCollection().setSceneAnalyses(sceneAnalyses);
    }

    /**
     * If Find.FIRST, it returns the first positive results.
     * Otherwise, it returns all matches.
     */
    public List<MatchObject_> find_(Matches matches, ActionOptions actionOptions, List<StateImage_> stateImages, List<Scene> scenes,
                                    List<Matches> matchesList) {
        boolean allImagesFound = true;
        List<MatchObject_> matchObjects = new ArrayList<>();
        actionLifecycleManagement.printActionOnce(matches.getActionId());
        for (Scene scene : scenes) {
            for (int i=0; i<stateImages.size(); i++) {
                List<MatchObject_> patternMatches = findAll.find(stateImages.get(i), scene, actionOptions);
                patternMatches.forEach(matchesList.get(i)::add);
                matchObjects.addAll(patternMatches);
                if (patternMatches.isEmpty()) allImagesFound = false;
                if (stopAfterFound(actionOptions, matches)) return matchObjects;
            }
        }
        if (allImagesFound) actionLifecycleManagement.setAllImagesFound(matches.getActionId());
        return matchObjects;
    }

    public boolean stopAfterFound(ActionOptions actionOptions, Matches matches) {
        if (actionOptions.getFind() == ActionOptions.Find.ALL) return false;
        return !matches.isEmpty();
    }

    /**
     * Searches all patterns and returns the Match with the best Score.
     * @param actionOptions holds the action configuration.
     * @param objectCollections images are taken from the first ObjectCollection.
     */
    public void best(Matches matches, ActionOptions actionOptions, List<ObjectCollection> objectCollections) {
        find(matches, actionOptions, objectCollections);
        Matches best = new Matches();
        matches.getBestMatch().ifPresent(best::add);
        matches = best;
    }

    /**
     * Searches each Pattern separately and returns one Match per Pattern if found.
     * @param actionOptions holds the action configuration.
     * @param objectCollections images are taken from the first ObjectCollection.
     */
    public void each(Matches matches, ActionOptions actionOptions, List<ObjectCollection> objectCollections) {
        objectCollections.get(0).getStateImages().forEach(image -> {
            find(matches, actionOptions, Collections.singletonList(image.asObjectCollection()));
            matches.getBestMatch().ifPresent(matches::add);
        });
    }
}
