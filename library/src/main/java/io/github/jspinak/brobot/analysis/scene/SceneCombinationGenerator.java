package io.github.jspinak.brobot.analysis.scene;

import io.github.jspinak.brobot.action.ActionConfig;
import io.github.jspinak.brobot.action.ActionType;
import io.github.jspinak.brobot.action.basic.find.PatternFindOptions;
import io.github.jspinak.brobot.action.basic.find.motion.FindDynamicPixelMatches;
import io.github.jspinak.brobot.action.internal.factory.ActionResultFactory;
import io.github.jspinak.brobot.model.analysis.scene.SceneCombination;
import io.github.jspinak.brobot.model.element.Scene;
import io.github.jspinak.brobot.action.ActionResult;
import io.github.jspinak.brobot.action.ObjectCollection;

import org.bytedeco.opencv.opencv_core.Mat;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

/**
 * Generates combinations of scenes for dynamic pixel analysis.
 * <p>
 * This class is responsible for creating all possible combinations of scenes from a list of
 * {@link ObjectCollection} objects and analyzing dynamic pixels between them. Dynamic pixels
 * are pixels that change between scenes and are important for identifying unique visual
 * elements that distinguish one scene from another.
 * <p>
 * The primary use case is for automated state detection where different screens or states
 * in an application need to be identified based on their visual differences.
 *
 * @see SceneCombination
 * @see FindDynamicPixelMatches
 * @see Scene
 */
@Component
public class SceneCombinationGenerator {

    private final FindDynamicPixelMatches findDynamicPixelMatches;
    private final ActionResultFactory matchesInitializer;

    public SceneCombinationGenerator(FindDynamicPixelMatches findDynamicPixelMatches, ActionResultFactory matchesInitializer) {
        this.findDynamicPixelMatches = findDynamicPixelMatches;
        this.matchesInitializer = matchesInitializer;
    }

    /**
     * Generates all possible scene combinations from the provided object collections.
     * <p>
     * This method creates combinations by pairing each scene with every other scene,
     * including itself. For each combination, it calculates a dynamic pixel mask that
     * identifies pixels that differ between the two scenes. The resulting combinations
     * are useful for state detection algorithms that need to distinguish between
     * different application states based on visual differences.
     * <p>
     * The algorithm uses an upper triangular matrix approach (i &lt;= j) to avoid
     * duplicate combinations while still allowing same-scene combinations.
     *
     * @param objectCollections List of {@link ObjectCollection} objects, each containing
     *                         a scene to be analyzed. Must not be null.
     * @return A list of {@link SceneCombination} objects, each containing a dynamic
     *         pixel mask and indices of the two scenes being compared. Returns an
     *         empty list if no valid combinations can be created.
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
     * Computes the dynamic pixel mask between two scenes.
     * <p>
     * This method extracts the first scene from each {@link ObjectCollection} and
     * performs dynamic pixel analysis to identify pixels that differ between the scenes.
     * The resulting mask is a binary Mat where dynamic (changing) pixels are marked.
     * <p>
     * The method uses {@link FindDynamicPixelMatches} with specific action options
     * configured for dynamic pixel detection. If either collection doesn't contain
     * a scene, the method returns an empty Optional.
     *
     * @param objectCollection1 The first {@link ObjectCollection} containing a scene.
     *                         Must not be null.
     * @param objectCollection2 The second {@link ObjectCollection} containing a scene.
     *                         Must not be null.
     * @return An {@link Optional} containing the dynamic pixel Mat if both collections
     *         have scenes and analysis succeeds, or {@link Optional#empty()} if either
     *         collection lacks scenes or if the analysis produces no result.
     */
    public Optional<Mat> getDynamicPixelMat(ObjectCollection objectCollection1, ObjectCollection objectCollection2) {
        if (objectCollection1.getScenes().isEmpty() || objectCollection2.getScenes().isEmpty()) return Optional.empty();
        Scene scene1 = objectCollection1.getScenes().get(0);
        Scene scene2 = objectCollection2.getScenes().get(0);
        ObjectCollection objectCollection = new ObjectCollection.Builder()
                .withScenes(scene1, scene2)
                .build();
        // Use PatternFindOptions for dynamic pixel finding
        ActionConfig actionConfig = new PatternFindOptions.Builder()
                .setStrategy(PatternFindOptions.Strategy.ALL) // Find all dynamic pixels
                .build();
        ActionResult matches = matchesInitializer.init(actionConfig, "dynamic pixels for state creation", objectCollection);
        findDynamicPixelMatches.find(matches, List.of(objectCollection));
        return Optional.ofNullable(matches.getMask());
    }

    /**
     * Finds the first scene combination where the two scenes have different indices.
     * <p>
     * This utility method is primarily used for testing purposes to quickly obtain
     * a combination of two different scenes. It iterates through the provided list
     * and returns the first combination where scene1 index differs from scene2 index.
     *
     * @param sceneCombinations The list of {@link SceneCombination} objects to search.
     *                         Must not be null.
     * @return The first {@link SceneCombination} with different scene indices, or
     *         null if no such combination exists or if all combinations are
     *         same-scene combinations.
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
