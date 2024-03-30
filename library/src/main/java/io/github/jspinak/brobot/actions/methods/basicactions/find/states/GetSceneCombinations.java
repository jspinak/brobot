package io.github.jspinak.brobot.actions.methods.basicactions.find.states;

import io.github.jspinak.brobot.actions.actionExecution.MatchesInitializer;
import io.github.jspinak.brobot.actions.actionOptions.ActionOptions;
import io.github.jspinak.brobot.actions.methods.basicactions.find.fixedAndDynamicPixels.FindDynamicPixelMatches;
import io.github.jspinak.brobot.datatypes.primitives.image.Pattern;
import io.github.jspinak.brobot.datatypes.primitives.match.Matches;
import io.github.jspinak.brobot.datatypes.state.ObjectCollection;
import org.bytedeco.opencv.opencv_core.Mat;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@Component
public class GetSceneCombinations {

    private final FindDynamicPixelMatches findDynamicPixelMatches;
    private final MatchesInitializer matchesInitializer;

    public GetSceneCombinations(FindDynamicPixelMatches findDynamicPixelMatches, MatchesInitializer matchesInitializer) {
        this.findDynamicPixelMatches = findDynamicPixelMatches;
        this.matchesInitializer = matchesInitializer;
    }

    /**
     * Initializes and returns all SceneCombination objects with the dynamic pixel mask and both scene indices.
     * @param objectCollections all scenes and images
     * @return a list of initialized SceneCombination objects without images
     */
    public List<SceneCombination> getAllSceneCombinations(List<ObjectCollection> objectCollections) {
        List<SceneCombination> sceneCombinations = new ArrayList<>();
        // get the dynamic pixel mat for all screen combinations
        for (int i = 0; i < objectCollections.size(); i++) { // Iterate through each ObjectCollection
            ObjectCollection objectCollection1 = objectCollections.get(i);
            // Compare with every ObjectCollection, including itself (this is useful for images that are only in one scene)
            for (int j = i; j < objectCollections.size(); j++) {
                ObjectCollection objectCollection2 = objectCollections.get(j);
                Optional<Mat> resultMat = getDynamicPixelMat(objectCollection1, objectCollection2); // Perform the comparison
                if (resultMat.isPresent()) sceneCombinations.add(new SceneCombination(resultMat.get(), i, j));
            }
        }
        return sceneCombinations;
    }

    /**
     * Extract the scenes from both object collections and find the dynamic pixels.
     * @param objectCollection1 contains the first scene
     * @param objectCollection2 contains the second scene
     * @return dynamic pixel mat
     */
    public Optional<Mat> getDynamicPixelMat(ObjectCollection objectCollection1, ObjectCollection objectCollection2) {
        if (objectCollection1.getScenes().isEmpty() || objectCollection2.getScenes().isEmpty()) return Optional.empty();
        Pattern scene1 = objectCollection1.getScenes().get(0);
        Pattern scene2 = objectCollection2.getScenes().get(0);
        ObjectCollection objectCollection = new ObjectCollection.Builder()
                .withScenes(scene1, scene2)
                .build();
        ActionOptions actionOptions = new ActionOptions.Builder()
                .setAction(ActionOptions.Action.FIND)
                .setFind(ActionOptions.Find.DYNAMIC_PIXELS)
                .build();
        Matches matches = matchesInitializer.init(actionOptions, "dynamic pixels for state creation", objectCollection);
        findDynamicPixelMatches.find(matches, List.of(objectCollection));
        return Optional.ofNullable(matches.getMask());
    }

    /**
     * Finds the first SceneCombination with different scenes.
     * Primarily used in testing. May return null.
     * @param sceneCombinations The SceneCombinations to query.
     * @return the first SceneCombination with non-matching scenes.
     */
    public SceneCombination getSceneCombinationWithDifferentScenes(List<SceneCombination> sceneCombinations) {
        SceneCombination sceneCombinationWithDifferentScenes = null;
        for (SceneCombination sceneCombination : sceneCombinations) {
            if (sceneCombination.getScene1() != sceneCombination.getScene2()) {
                sceneCombinationWithDifferentScenes = sceneCombination;
                break;
            }
        }
        return sceneCombinationWithDifferentScenes;
    }
}
