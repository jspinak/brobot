package io.github.jspinak.brobot.actions.methods.basicactions.find.states;

import io.github.jspinak.brobot.datatypes.primitives.match.Match;
import io.github.jspinak.brobot.datatypes.primitives.match.Matches;
import io.github.jspinak.brobot.datatypes.state.ObjectCollection;
import org.springframework.stereotype.Component;

import java.util.List;

import static org.bytedeco.opencv.global.opencv_core.countNonZero;

@Component
public class FindStates {

    private final GetSceneCombinations getSceneCombinations;
    private final SceneCombinations sceneCombinations;
    private final TempStateRepo tempStateRepo;
    private final PopulateTempStateRepo populateTempStateRepo;
    private final PopulateSceneCombinations populateSceneCombinations;

    public FindStates(GetSceneCombinations getSceneCombinations, SceneCombinations sceneCombinations,
                      TempStateRepo tempStateRepo, PopulateTempStateRepo populateTempStateRepo,
                      PopulateSceneCombinations populateSceneCombinations) {
        this.getSceneCombinations = getSceneCombinations;
        this.sceneCombinations = sceneCombinations;
        this.tempStateRepo = tempStateRepo;
        this.populateTempStateRepo = populateTempStateRepo;
        this.populateSceneCombinations = populateSceneCombinations;
    }

    /**
     * Each ObjectCollection has images and a scene and represents one screenshot with state images and potential links.
     * @param matches has the ActionOptions and stores the final results
     * @param objectCollections all images and scenes
     */
    public void find(Matches matches, List<ObjectCollection> objectCollections) {
        List<SceneCombination> sceneCombinationList = getSceneCombinations.getAllSceneCombinations(objectCollections);
        populateSceneCombinations.populateSceneCombinationsWithImages(
                sceneCombinationList, objectCollections, matches.getActionOptions());
        sceneCombinations.addSceneCombinations(sceneCombinationList);
        populateTempStateRepo.createAndAddStatesForSceneToStateRepo(objectCollections);
        tempStateRepo.getAllStateImages().forEach(stateImage -> {
            Match matchFromPattern = new Match.Builder()
                .setRegion(stateImage.getLargestDefinedFixedRegionOrNewRegion())
                .setBufferedImage(stateImage.getPatterns().get(0).getBImage()) // must be an exact match
                .setStateObjectData(stateImage)
                .setSearchImage(stateImage.getPatterns().get(0).getBImage())
                .setName(stateImage.getName())
                .setSimScore(.99)
                .build();
            int minSize = matches.getActionOptions().getMinArea();
            if (minSize <= matchFromPattern.size()) matches.add(matchFromPattern);
        });
    }

}
