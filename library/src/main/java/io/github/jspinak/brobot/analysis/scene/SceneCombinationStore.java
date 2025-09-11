package io.github.jspinak.brobot.analysis.scene;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.springframework.stereotype.Component;

import io.github.jspinak.brobot.model.analysis.scene.SceneCombination;
import io.github.jspinak.brobot.model.state.StateImage;

/**
 * Repository for managing and querying collections of scene combinations.
 *
 * <p>This class serves as a centralized storage and query interface for {@link SceneCombination}
 * objects. It provides methods to find combinations based on specific criteria such as scene
 * indices or images. This is particularly useful in state detection algorithms where you need to
 * find all combinations that involve a particular scene or image.
 *
 * <p>The class maintains an internal list of scene combinations and provides various query methods
 * to filter and retrieve relevant combinations based on different criteria.
 *
 * @see SceneCombination
 * @see StateImage
 */
@Component
public class SceneCombinationStore {

    private final List<SceneCombination> sceneCombinationList = new ArrayList<>();

    /**
     * Adds multiple scene combinations to the repository.
     *
     * <p>This method appends the provided scene combinations to the internal list. The combinations
     * are added in the order they appear in the input list.
     *
     * @param sceneCombinations List of {@link SceneCombination} objects to add. Must not be null.
     *     Can be empty.
     */
    public void addSceneCombinations(List<SceneCombination> sceneCombinations) {
        sceneCombinationList.addAll(sceneCombinations);
    }

    /**
     * Retrieves all scene combinations that include a specific scene.
     *
     * <p>This method searches through all stored combinations and returns those where either scene1
     * or scene2 matches the provided index. This is useful for finding all combinations that
     * involve a particular scene, which helps in understanding how that scene relates to other
     * scenes in the system.
     *
     * @param sceneIndex The index of the scene to search for. Can be any valid integer.
     * @return A new list containing all {@link SceneCombination} objects that include the specified
     *     scene index. Returns an empty list if no matches are found.
     */
    public List<SceneCombination> getAllWithScene(int sceneIndex) {
        List<SceneCombination> combinations = new ArrayList<>();
        sceneCombinationList.forEach(
                sc -> {
                    if (sc.getScene1() == sceneIndex || sc.getScene2() == sceneIndex)
                        combinations.add(sc);
                });
        return combinations;
    }

    /**
     * Retrieves scene combinations that contain both a specific scene and image.
     *
     * <p>This method performs a two-step filtering process: first finding all combinations that
     * include the specified scene index, then filtering those to include only combinations that
     * also contain the specified image. This is particularly useful for finding combinations where
     * a specific image appears in a specific scene context.
     *
     * @param sceneIndex The index of the scene to match. Can be any valid integer.
     * @param stateImage The {@link StateImage} to match. Must not be null.
     * @return A new list of {@link SceneCombination} objects that contain both the specified scene
     *     and image. Returns an empty list if no matches are found.
     */
    public List<SceneCombination> getAllWithSceneAndImage(int sceneIndex, StateImage stateImage) {
        List<SceneCombination> combinationsWithBaseScene = getAllWithScene(sceneIndex);
        List<SceneCombination> combinationsWithSceneAndImage = new ArrayList<>();
        for (SceneCombination sceneCombination : combinationsWithBaseScene) {
            if (sceneCombination.contains(stateImage))
                combinationsWithSceneAndImage.add(sceneCombination);
        }
        return combinationsWithSceneAndImage;
    }

    /**
     * Extracts all unique scene indices from a list of scene combinations.
     *
     * <p>This method collects all scene indices (both scene1 and scene2) from the provided
     * combinations and returns them as a set, ensuring each index appears only once. This is useful
     * for determining which scenes are involved in a given set of combinations.
     *
     * <p>For example, if the input contains combinations [1-2, 1-3, 1-5], the result would be the
     * set {1, 2, 3, 5}.
     *
     * @param sceneCombinations List of {@link SceneCombination} objects to process. Must not be
     *     null. Can be empty.
     * @return A {@link Set} containing all unique scene indices found in the combinations. Returns
     *     an empty set if the input list is empty.
     */
    public Set<Integer> getSceneCombinationIndices(List<SceneCombination> sceneCombinations) {
        Set<Integer> scenesInCombinations = new HashSet<>();
        for (SceneCombination sceneCombination : sceneCombinations) {
            scenesInCombinations.add(sceneCombination.getScene1());
            scenesInCombinations.add(sceneCombination.getScene2());
        }
        return scenesInCombinations;
    }

    /**
     * Returns a string representation of all stored scene combinations.
     *
     * <p>Each combination is represented on a separate line using its toString() method. This is
     * useful for debugging and logging purposes to see all combinations currently stored in the
     * repository.
     *
     * @return A string containing all scene combinations, with each combination on a separate line.
     *     Returns an empty string if no combinations are stored.
     */
    @Override
    public String toString() {
        StringBuilder stringBuilder = new StringBuilder();
        for (SceneCombination sceneCombination : sceneCombinationList) {
            stringBuilder.append(sceneCombination).append("\n");
        }
        return stringBuilder.toString();
    }

    /**
     * Retrieves all images from all stored scene combinations.
     *
     * <p>This method collects all {@link StateImage} objects from every scene combination in the
     * repository. Note that the same image may appear multiple times in the returned list if it
     * exists in multiple combinations. The images are added in the order they appear in each
     * combination.
     *
     * @return A new list containing all {@link StateImage} objects from all combinations. May
     *     contain duplicates. Returns an empty list if no combinations are stored or if all
     *     combinations have no images.
     */
    public List<StateImage> getAllImages() {
        List<StateImage> allImages = new ArrayList<>();
        sceneCombinationList.forEach(
                sceneCombination -> allImages.addAll(sceneCombination.getImages()));
        return allImages;
    }
}
