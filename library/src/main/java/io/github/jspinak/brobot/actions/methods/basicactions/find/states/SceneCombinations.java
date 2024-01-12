package io.github.jspinak.brobot.actions.methods.basicactions.find.states;

import io.github.jspinak.brobot.datatypes.state.stateObject.stateImage.StateImage;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

@Component
public class SceneCombinations {

    private final List<SceneCombination> sceneCombinationList = new ArrayList<>();

    public void addSceneCombinations(List<SceneCombination> sceneCombinations) {
        sceneCombinationList.addAll(sceneCombinations);
    }

    /**
     * Given an index, return all SceneCombinations that contain that scene.
     * Each SceneCombination has two scenes.
     * @param sceneIndex the index to search for
     * @return all SceneCombinations with this index
     */
    public List<SceneCombination> getAllWithScene(int sceneIndex) {
        List<SceneCombination> combinations = new ArrayList<>();
        sceneCombinationList.forEach(sc -> {
            if (sc.getScene1() == sceneIndex || sc.getScene2() == sceneIndex) combinations.add(sc);
        });
        return combinations;
    }

    /**
     * Returns all SceneCombinations that have both the given scene and image.
     * @param sceneIndex the scene
     * @param stateImage the image
     * @return all SceneCombinations with this scene and image
     */
    List<SceneCombination> getAllWithSceneAndImage(int sceneIndex, StateImage stateImage) {
        List<SceneCombination> combinationsWithBaseScene = getAllWithScene(sceneIndex);
        List<SceneCombination> combinationsWithSceneAndImage = new ArrayList<>();
        for (SceneCombination sceneCombination : combinationsWithBaseScene) {
            if (sceneCombination.contains(stateImage)) combinationsWithSceneAndImage.add(sceneCombination);
        }
        return combinationsWithSceneAndImage;
    }

    /**
     * Returns a set with all scene indices in a list of SceneCombinations.
     * For example, SceneCombinations 1-2, 1-3, 1-5 would return a set of {1,2,3,5}
     * @param sceneCombinations the combinations to consider
     * @return a set of the individual scene indices
     */
     Set<Integer> getSceneCombinationIndices(List<SceneCombination> sceneCombinations) {
        Set<Integer> scenesInCombinations = new HashSet<>();
        for (SceneCombination sceneCombination : sceneCombinations) {
            scenesInCombinations.add(sceneCombination.getScene1());
            scenesInCombinations.add(sceneCombination.getScene2());
        }
        return scenesInCombinations;
    }

    @Override
    public String toString() {
         StringBuilder stringBuilder = new StringBuilder();
         for (SceneCombination sceneCombination : sceneCombinationList) {
             stringBuilder.append(sceneCombination).append("\n");
         }
         return stringBuilder.toString();
    }

    public List<StateImage> getAllImages() {
        List<StateImage> allImages = new ArrayList<>();
        sceneCombinationList.forEach(sceneCombination -> allImages.addAll(sceneCombination.getImages()));
        return allImages;
    }
}
