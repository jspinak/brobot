package io.github.jspinak.brobot.actions.methods.basicactions.find;

import io.github.jspinak.brobot.actions.actionExecution.actionLifecycle.ActionLifecycleManagement;
import io.github.jspinak.brobot.actions.actionOptions.ActionOptions;
import io.github.jspinak.brobot.actions.methods.basicactions.find.color.pixelAnalysis.GetScenes;
import io.github.jspinak.brobot.actions.methods.sikuliWrappers.find.UseDefinedRegion;
import io.github.jspinak.brobot.datatypes.primitives.image.Image;
import io.github.jspinak.brobot.datatypes.state.stateObject.stateImage.StateImage;
import io.github.jspinak.brobot.datatypes.primitives.match.Match;
import io.github.jspinak.brobot.datatypes.primitives.match.Matches;
import io.github.jspinak.brobot.datatypes.state.ObjectCollection;
import org.springframework.stereotype.Component;

import java.util.*;

@Component
public class FindImages {

    private final UseDefinedRegion useDefinedRegion;
    private final ActionLifecycleManagement actionLifecycleManagement;
    private final GetScenes getScenes;
    private final FindPatternsIteration findPatternsIteration;

    public FindImages(UseDefinedRegion useDefinedRegion, ActionLifecycleManagement actionLifecycleManagement,
                      GetScenes getScenes, FindPatternsIteration findPatternsIteration) {
        this.useDefinedRegion = useDefinedRegion;
        this.actionLifecycleManagement = actionLifecycleManagement;
        this.getScenes = getScenes;
        this.findPatternsIteration = findPatternsIteration;
    }

    void findAll(Matches matches, List<ObjectCollection> objectCollections) {
        getImageMatches(matches, objectCollections);
    }

    void findBest(Matches matches, List<ObjectCollection> objectCollections) {
        getImageMatches(matches, objectCollections);
        matches.getBestMatch().ifPresent(match -> matches.setMatchList(List.of(match)));
    }

    void findEachStateObject(Matches matches, List<ObjectCollection> objectCollections) {
        getImageMatches(matches, objectCollections);
        List<Match> bestMatchPerStateObject = new ArrayList<>();
        Set<String> imageIds = matches.getUniqueImageIds();
        for (String id : imageIds) {
            List<Match> singleObjectMatchList = matches.getMatchObjectsWithTargetStateObject(id);
            Optional<Match> matchWithHighestScore = singleObjectMatchList.stream()
                    .max(java.util.Comparator.comparingDouble(Match::getScore));
            matchWithHighestScore.ifPresent(bestMatchPerStateObject::add);
        }
        matches.setMatchList(bestMatchPerStateObject);
    }

    /**
     * Finds the best match per scene.
     * @param matches all Match objects and the action configuration
     * @param objectCollections the StateObjects and scenes to use
     */
    void findEachScene(Matches matches, List<ObjectCollection> objectCollections) {
        getImageMatches(matches, objectCollections);
        matches.setMatchList(new ArrayList<>());
        matches.getSceneAnalysisCollection().getSceneAnalyses().forEach(sceneAnalysis -> {
            sceneAnalysis.getMatchList().stream()
                    .max(Comparator.comparingDouble(Match::getScore))
                    .ifPresent(matches::add);
        });
    }

    /**
     * The ActionLifecycleManagement takes care of the different Find types during execution.
     * @param matches contains ActionOptions and all matches found
     * @param objectCollections all objects to use with this action
     */
    void getImageMatches(Matches matches, List<ObjectCollection> objectCollections) {
        if (objectCollections.isEmpty()) return; // no images to search for
        ActionOptions actionOptions = matches.getActionOptions();
        if (actionOptions.isUseDefinedRegion()) {
            matches.addAllResults(useDefinedRegion.useRegion(matches, objectCollections.get(0)));
            return;
        }
        /*
        Execute the find until the exit condition is achieved. For example, a Find.VANISH will execute until
        the images are no longer found. The results for each execution are added to the Matches object.
         */
        List<StateImage> stateImages = objectCollections.get(0).getStateImages();
        while (actionLifecycleManagement.isOkToContinueAction(matches, stateImages.size())) {
            List<Image> scenes = getScenes.getScenes(actionOptions, objectCollections, 1, 0);
            findPatternsIteration.find(matches, stateImages, scenes);
            actionLifecycleManagement.incrementCompletedRepetitions(matches);
        }
    }

}
