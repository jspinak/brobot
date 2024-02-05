package io.github.jspinak.brobot.actions.methods.basicactions.find.states;

import io.github.jspinak.brobot.actions.actionOptions.ActionOptions;
import io.github.jspinak.brobot.datatypes.primitives.region.Region;
import io.github.jspinak.brobot.datatypes.state.ObjectCollection;
import io.github.jspinak.brobot.datatypes.state.stateObject.stateImage.StateImage;
import io.github.jspinak.brobot.imageUtils.MatOps;
import org.bytedeco.opencv.opencv_core.Mat;
import org.bytedeco.opencv.opencv_core.Rect;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Optional;

import static org.bytedeco.opencv.global.opencv_core.countNonZero;

@Component
public class PopulateSceneCombinations {

    /**
     * Store StateImage objects in every SceneCombination where they are found.
     * For example, a StateImage object originating in Scene1, will be included in the SceneCombinations
     * 1-2 and 1-3 when its location contains only fixed pixels in these combinations.
     * @param sceneCombinations all SceneCombination objects
     * @param objectCollections all ObjectCollection objects
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

    private void addImageToCombinationIfFound(int sceneIndex, StateImage stateImage, SceneCombination sceneCombination) {
        if (!sceneCombination.contains(sceneIndex)) return; // only check combinations with the originating scene
        Region imageRegion = stateImage.getLargestDefinedFixedRegionOrNewRegion(); // there should be only one Pattern
        if (regionHasOnlyFixedPixels(imageRegion, sceneCombination)) sceneCombination.addImage(stateImage);
    }

    private boolean regionHasOnlyFixedPixels(Region imageRegion, SceneCombination sceneCombination) {
        Optional<Mat> matCombinationRegion = MatOps.applyIfOk(sceneCombination.getDynamicPixels(),
                new Rect(imageRegion.x(), imageRegion.y(), imageRegion.w(), imageRegion.h()));
        return matCombinationRegion.isPresent() && countNonZero(matCombinationRegion.get()) == 0;
    }
}
