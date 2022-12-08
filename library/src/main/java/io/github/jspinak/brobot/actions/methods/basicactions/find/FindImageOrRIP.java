package io.github.jspinak.brobot.actions.methods.basicactions.find;

import io.github.jspinak.brobot.actions.actionOptions.ActionOptions;
import io.github.jspinak.brobot.actions.methods.basicactions.find.color.pixelAnalysis.GetScenes;
import io.github.jspinak.brobot.actions.methods.basicactions.find.color.pixelAnalysis.Scene;
import io.github.jspinak.brobot.actions.methods.basicactions.find.color.pixelAnalysis.SceneAnalysis;
import io.github.jspinak.brobot.datatypes.primitives.match.Matches;
import io.github.jspinak.brobot.datatypes.state.ObjectCollection;
import io.github.jspinak.brobot.reports.Report;
import org.springframework.stereotype.Component;

import java.util.*;

/**
 * Sends the Image object to either FindImage or FindRIP depending on whether the Image location can vary.
 * Contains methods for Find options FIRST, EACH, ALL, BEST
 */
@Component
public class FindImageOrRIP {

    private GetScenes getScenes;

    private final Map<Boolean, FindImageObject> findMethod = new HashMap<>();

    public FindImageOrRIP(FindImage findImage, FindRIP findRIP, GetScenes getScenes) {
        this.getScenes = getScenes;
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
    public Matches find(ActionOptions actionOptions, List<ObjectCollection> objectCollections) {
        if (Report.minReportingLevel(Report.OutputLevel.LOW)) {
            System.out.format("Find.%s ", actionOptions.getFind());
        }
        Matches matches = new Matches();
        List<Scene> scenes = getScenes.getScenesNoScrenshots(objectCollections);
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
        List<Scene> scenesWithScreenshots = getScenes.getScenes(objectCollections);
        List<SceneAnalysis> sceneAnalyses = new ArrayList<>();
        scenesWithScreenshots.forEach(scene -> sceneAnalyses.add(new SceneAnalysis(scene)));
        matches.getSceneAnalysisCollection().setSceneAnalyses(sceneAnalyses);
        return matches;
    }

    /**
     * Searches all patterns and returns the Match with the best Score.
     * @param actionOptions holds the action configuration.
     * @param objectCollections images are taken from the first ObjectCollection.
     * @return a Matches object with either the best match or no matches.
     */
    public Matches best(ActionOptions actionOptions, List<ObjectCollection> objectCollections) {
        Matches matches = new Matches();
        find(actionOptions, objectCollections).getBestMatch().ifPresent(matches::add);
        return matches;
    }

    /**
     * Searches each Pattern separately and returns one Match per Pattern if found.
     * @param actionOptions holds the action configuration.
     * @param objectCollections images are taken from the first ObjectCollection.
     * @return a Matches object with all matches found.
     */
    public Matches each(ActionOptions actionOptions, List<ObjectCollection> objectCollections) {
        Matches matches = new Matches();
        objectCollections.get(0).getStateImages().forEach(image ->
                find(actionOptions, Collections.singletonList(image.asObjectCollection())).
                        getBestMatch().ifPresent(matches::add));
        return matches;
    }
}
