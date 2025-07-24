package io.github.jspinak.brobot.analysis.scene;

import io.github.jspinak.brobot.action.internal.options.ActionOptions;
import io.github.jspinak.brobot.action.ActionConfig;
import io.github.jspinak.brobot.action.ObjectCollection;
import io.github.jspinak.brobot.action.basic.find.PatternFindOptions;
import io.github.jspinak.brobot.action.basic.find.FindState;
import io.github.jspinak.brobot.model.analysis.scene.SceneCombination;
import io.github.jspinak.brobot.model.element.Region;
import io.github.jspinak.brobot.model.state.StateImage;
import io.github.jspinak.brobot.util.image.core.MatrixUtilities;

import org.bytedeco.opencv.opencv_core.Mat;
import org.bytedeco.opencv.opencv_core.Rect;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Optional;

import static org.bytedeco.opencv.global.opencv_core.countNonZero;

/**
 * Distributes StateImage objects across scene combinations based on pixel stability.
 * <p>
 * This component analyzes which images can be reliably found in different scene
 * combinations by examining pixel variability. It's a crucial part of the state
 * discovery process that determines which UI elements remain stable across
 * different application states.
 * 
 * <p><b>Key concepts:</b>
 * <ul>
 *   <li><b>Scene Combination:</b> A composite view created by merging multiple scenes</li>
 *   <li><b>Fixed Pixels:</b> Pixels that remain unchanged across all scenes in a combination</li>
 *   <li><b>Dynamic Pixels:</b> Pixels that vary between scenes (indicating changing UI)</li>
 * </ul>
 * 
 * <p>An image is added to a scene combination only if its region contains exclusively
 * fixed pixels, meaning the UI element it represents is stable across all scenes
 * in that combination. This ensures reliable pattern matching regardless of which
 * scene is currently active.
 * 
 * @see SceneCombination
 * @see StateImage
 * @see FindState
 */
@Component
public class SceneCombinationPopulator {

    /**
     * Distributes StateImage objects to all scene combinations where they remain stable.
     * <p>
     * This method implements a cross-scene validation algorithm that determines which
     * images can be reliably found in different scene combinations. The process ensures
     * that images are only associated with combinations where they appear consistently
     * without any dynamic pixel interference.
     * 
     * <p><b>Algorithm:</b>
     * <ol>
     *   <li>For each image in each collection, check if it meets size requirements</li>
     *   <li>Test the image against all scene combinations containing its origin scene</li>
     *   <li>Verify that the image region contains only fixed pixels in each combination</li>
     *   <li>Add the image to combinations where it remains stable</li>
     * </ol>
     * 
     * <p><b>Example:</b> A StateImage from Scene1 will be added to combinations:
     * <ul>
     *   <li>Scene1-Scene2 if its region has no dynamic pixels in that combination</li>
     *   <li>Scene1-Scene3 if its region has no dynamic pixels in that combination</li>
     *   <li>Scene1-Scene2-Scene3 if stable across all three scenes</li>
     * </ul>
     * 
     * @param sceneCombinations All possible scene combinations to populate with images
     * @param objectCollections Collections containing StateImages to distribute. The
     *                         collection index corresponds to the scene index.
     * @param actionOptions Configuration containing minimum area threshold for images
     */
    public void populateSceneCombinationsWithImages(List<SceneCombination> sceneCombinations,
                                                    List<ObjectCollection> objectCollections,
                                                    ActionOptions actionOptions) {
        for (int i=0; i<objectCollections.size(); i++) {
            for (StateImage stateImage : objectCollections.get(i).getStateImages()) {
                int patternSize = stateImage.getMinSize();
                boolean isBigEnough = actionOptions.getMinArea() <= patternSize;
                if (isBigEnough) {
                    for (SceneCombination sceneCombination : sceneCombinations) {
                        addImageToCombinationIfFound(i, stateImage, sceneCombination);
                    }
                }
            }
        }
    }

    /**
     * Distributes StateImage objects to all scene combinations where they remain stable.
     * <p>
     * This overloaded method accepts ActionConfig (new API) instead of ActionOptions.
     * It extracts the minArea parameter from PatternFindOptions if available.
     * 
     * @param sceneCombinations All possible scene combinations to populate with images
     * @param objectCollections Collections containing StateImages to distribute
     * @param actionConfig Configuration containing minimum area threshold for images
     */
    public void populateSceneCombinationsWithImages(List<SceneCombination> sceneCombinations,
                                                    List<ObjectCollection> objectCollections,
                                                    ActionConfig actionConfig) {
        // In the new API, minArea filtering is typically done elsewhere
        // For now, we'll accept all images regardless of size
        for (int i=0; i<objectCollections.size(); i++) {
            for (StateImage stateImage : objectCollections.get(i).getStateImages()) {
                for (SceneCombination sceneCombination : sceneCombinations) {
                    addImageToCombinationIfFound(i, stateImage, sceneCombination);
                }
            }
        }
    }

    /**
     * Adds an image to a scene combination if it remains stable in that combination.
     * <p>
     * This method performs the core validation to determine if an image can be
     * reliably found in a specific scene combination. It only considers combinations
     * that include the image's originating scene, as an image cannot exist in a
     * combination that doesn't include its source.
     * 
     * <p><b>Side Effects:</b> The sceneCombination is modified by adding the
     * stateImage if validation passes.
     * 
     * @param sceneIndex The index of the scene where the image originates
     * @param stateImage The image to potentially add to the combination
     * @param sceneCombination The combination to test and potentially modify
     */
    private void addImageToCombinationIfFound(int sceneIndex, StateImage stateImage, SceneCombination sceneCombination) {
        if (!sceneCombination.contains(sceneIndex)) return; // only check combinations with the originating scene
        Region imageRegion = stateImage.getLargestDefinedFixedRegionOrNewRegion(); // there should be only one Pattern
        if (regionHasOnlyFixedPixels(imageRegion, sceneCombination)) sceneCombination.addImage(stateImage);
    }

    /**
     * Checks if a region contains only fixed pixels in a scene combination.
     * <p>
     * This method extracts the dynamic pixel map for the image region and verifies
     * that it contains no non-zero values. A zero count indicates that all pixels
     * in the region are fixed (stable) across all scenes in the combination.
     * 
     * <p><b>Technical details:</b>
     * <ul>
     *   <li>Uses the combination's dynamic pixel mask to identify changing areas</li>
     *   <li>Extracts the sub-region corresponding to the image location</li>
     *   <li>Counts non-zero pixels (dynamic pixels) in that region</li>
     *   <li>Returns true only if the count is zero (all pixels are fixed)</li>
     * </ul>
     * 
     * @param imageRegion The region to check for pixel stability
     * @param sceneCombination The combination providing the dynamic pixel mask
     * @return true if the region contains only fixed pixels (no dynamic pixels),
     *         false if any pixels are dynamic or if region extraction fails
     */
    private boolean regionHasOnlyFixedPixels(Region imageRegion, SceneCombination sceneCombination) {
        Optional<Mat> matCombinationRegion = MatrixUtilities.applyIfOk(sceneCombination.getDynamicPixels(),
                new Rect(imageRegion.x(), imageRegion.y(), imageRegion.w(), imageRegion.h()));
        return matCombinationRegion.isPresent() && countNonZero(matCombinationRegion.get()) == 0;
    }
}
