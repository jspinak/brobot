package io.github.jspinak.brobot.actions.methods.basicactions.find.states;

import io.github.jspinak.brobot.actions.methods.basicactions.find.color.pixelAnalysis.Scene;
import io.github.jspinak.brobot.datatypes.primitives.image.Pattern;
import io.github.jspinak.brobot.datatypes.state.ObjectCollection;
import io.github.jspinak.brobot.datatypes.state.stateObject.stateImage.StateImage;
import org.apache.pdfbox.debugger.ui.MapEntry;
import org.nd4j.common.util.Index;
import org.springframework.stereotype.Component;

import java.util.*;

@Component
public class SceneCombinations {

    private List<SceneCombination> sceneCombinationList = new ArrayList<>();
    private final TempStateRepo tempStateRepo;

    public SceneCombinations(TempStateRepo tempStateRepo) {
        this.tempStateRepo = tempStateRepo;
    }

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
     * Finds all StateImage objects associated with the SceneCombinations that include the target scene.
     * All other StateImage objects for that scene are not found on any other scene.
     * @param sceneIndex the index of the target scene
     * @param objectCollections all scenes and images
     * @return the images for the target scene that are found in other scenes
     */
    public Set<StateImage> getImagesInSceneCombinations(int sceneIndex, List<ObjectCollection> objectCollections) {
        List<StateImage> allPotentialImages = objectCollections.get(sceneIndex).getStateImages();
        Set<StateImage> stateImageSet = new HashSet<>();
        List<SceneCombination> allSceneCombinationsWithScene = getAllWithScene(sceneIndex);
        for (SceneCombination sceneCombination : allSceneCombinationsWithScene) {
            for (StateImage stateImage : sceneCombination.getImages()) {
                if (allPotentialImages.contains(stateImage)) stateImageSet.add(stateImage);
            }
        }
        return stateImageSet;
    }

    /**
     * Returns all SceneCombinations that have both the given scene and image.
     * @param sceneIndex the scene
     * @param stateImage the image
     * @return all SceneCombinations with this scene and image
     */
    public List<SceneCombination> getAllWithSceneAndImage(int sceneIndex, StateImage stateImage) {
        List<SceneCombination> combinationsWithBaseScene = getAllWithScene(sceneIndex);
        List<SceneCombination> combinationsWithSceneAndImage = new ArrayList<>();
        for (SceneCombination sceneCombination : combinationsWithBaseScene) {
            if (sceneCombination.contains(stateImage)) combinationsWithSceneAndImage.add(sceneCombination);
        }
        return combinationsWithSceneAndImage;
    }

    /**
     * Creates states based on the existence of a scene's images across other scenes.
     * Adds states to the temp state repo.
     *
     *      * The images in the SceneCombinations help us create and populate states with images. For example:
     *      * Scene 1 has images a,b,c,d,e,f
     *      * SceneCombination 1-2 has images a,b,c
     *      * SceneCombination 1-3 has images a,b,d
     *      * SceneCombination 1-4 has images b,d,e
     *      * This gives us 6 states:
     *      *   State 1 has image f
     *      *   State 1-2 has image c
     *      *   State 1-2-3 has image a
     *      *   State 1-2-3-4 has image b
     *      *   State 1-3-4 has image d
     *      *   State 1-4 has image e
     *
     * @param sceneIndex the target scene
     * @param objectCollections all images and scenes
     */
    public void createAndAddStatesForSceneToStateRepo(int sceneIndex, List<ObjectCollection> objectCollections) {
        List<StateImage> stateImages = objectCollections.get(sceneIndex).getStateImages();
        Map<StateImage, Set<Integer>> scenesPerImage = getScenesPerImage(sceneIndex, stateImages);
        for (Map.Entry<StateImage, Set<Integer>> entry : scenesPerImage.entrySet()) {
            tempStateRepo.addImage(entry.getKey(), entry.getValue());
        }
    }

    private Map<StateImage, Set<Integer>> getScenesPerImage(int sceneIndex, List<StateImage> images) {
        Map<StateImage, Set<Integer>> scenesPerImage = new HashMap<>();
        for (StateImage image : images) {
            // initialize the list and add the sceneIndex
            Set<Integer> scenes = new HashSet<>();
            scenes.add(sceneIndex);
            scenesPerImage.put(image, scenes);
            // add any other scenes
            List<SceneCombination> imageInTheseSceneCombinations = getAllWithSceneAndImage(sceneIndex, image);
            Set<Integer> imageInTheseScenes = getSceneCombinationIndices(imageInTheseSceneCombinations);
            scenes.addAll(imageInTheseScenes);
        }
        return scenesPerImage;
    }

    /**
     * Returns a set with all scene indices in a list of SceneCombinations.
     * For example, SceneCombinations 1-2, 1-3, 1-5 would return a set of {1,2,3,5}
     * @param sceneCombinations the combinations to consider
     * @return a set of the individual scene indices
     */
    private Set<Integer> getSceneCombinationIndices(List<SceneCombination> sceneCombinations) {
        Set<Integer> scenesInCombinations = new HashSet<>();
        for (SceneCombination sceneCombination : sceneCombinations) {
            scenesInCombinations.add(sceneCombination.getScene1());
            scenesInCombinations.add(sceneCombination.getScene2());
        }
        return scenesInCombinations;
    }
}
