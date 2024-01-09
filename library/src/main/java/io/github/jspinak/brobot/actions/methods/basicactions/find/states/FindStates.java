package io.github.jspinak.brobot.actions.methods.basicactions.find.states;

import io.github.jspinak.brobot.datatypes.primitives.match.Match;
import io.github.jspinak.brobot.datatypes.primitives.match.Matches;
import io.github.jspinak.brobot.datatypes.primitives.region.Region;
import io.github.jspinak.brobot.datatypes.state.ObjectCollection;
import io.github.jspinak.brobot.datatypes.state.stateObject.stateImage.StateImage;
import io.github.jspinak.brobot.imageUtils.MatOps;
import org.bytedeco.opencv.opencv_core.Mat;
import org.bytedeco.opencv.opencv_core.Rect;
import org.springframework.stereotype.Component;

import java.util.*;

import static org.bytedeco.opencv.global.opencv_core.countNonZero;

@Component
public class FindStates {

    private final GetSceneCombinations getSceneCombinations;
    private final SceneCombinations sceneCombinations;
    private final TempStateRepo tempStateRepo;

    public FindStates(GetSceneCombinations getSceneCombinations,
                      SceneCombinations sceneCombinations, TempStateRepo tempStateRepo) {
        this.getSceneCombinations = getSceneCombinations;
        this.sceneCombinations = sceneCombinations;
        this.tempStateRepo = tempStateRepo;
    }

    /**
     * Each ObjectCollection has images and a scene and represents one screenshot with state images and potential links.
     * @param matches has the ActionOptions and stores the final results
     * @param objectCollections all images and scenes
     */
    public void find(Matches matches, List<ObjectCollection> objectCollections) {
        List<SceneCombination> sceneCombinationList = getSceneCombinations.getAllSceneCombinations(objectCollections);
        populateSceneCombinationsWithImages(sceneCombinationList, objectCollections);
        sceneCombinations.addSceneCombinations(sceneCombinationList);
        for (int i=0; i<objectCollections.size(); i++) {
            sceneCombinations.createAndAddStatesForSceneToStateRepo(i, objectCollections);
        }
        tempStateRepo.getAllStateImages().forEach(stateImage -> matches.add(
                new Match.Builder()
                        .setMatch(stateImage.getLargestDefinedFixedRegionOrNewRegion())
                        .setStateObject(stateImage)
                        .setPattern(stateImage.getPatterns().get(0))
                        .setName(stateImage.getName())
                        .setSimScore(.99)
                        .build()
        ));
    }

    /**
     * Store StateImage objects in every SceneCombination where they are found.
     * For example, a StateImage object originating in Scene1, will be included in the SceneCombinations
     * 1-2 and 1-3 when its location contains only fixed pixels in these combinations.
     * @param sceneCombinations all SceneCombination objects
     * @param objectCollections all ObjectCollection objects
     */
    private void populateSceneCombinationsWithImages(List<SceneCombination> sceneCombinations,
                                                     List<ObjectCollection> objectCollections) {
        for (int i=0; i<objectCollections.size(); i++) {
            for (StateImage stateImage : objectCollections.get(i).getStateImages()) {
                for (SceneCombination sceneCombination : sceneCombinations) {
                    addImageToCombinationIfFound(i, stateImage, sceneCombination);
                }
            }
        }
    }

    private void addImageToCombinationIfFound(int sceneIndex, StateImage stateImage, SceneCombination sceneCombination) {
        if (sceneCombination.getScene1() != sceneIndex) return; // only check combinations with the originating scene
        Region imageRegion = stateImage.getLargestDefinedFixedRegionOrNewRegion(); // there should be only one Pattern
        Optional<Mat> matCombinationRegion = MatOps.applyIfOk(sceneCombination.getDynamicPixels(),
                new Rect(imageRegion.x, imageRegion.y, imageRegion.w, imageRegion.h));
        if (matCombinationRegion.isPresent() && countNonZero(matCombinationRegion.get()) == 0) sceneCombination.addImage(stateImage);
    }

}
