package io.github.jspinak.brobot.actions.methods.basicactions.find.states;

import io.github.jspinak.brobot.datatypes.state.ObjectCollection;
import io.github.jspinak.brobot.datatypes.state.stateObject.stateImage.StateImage;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

@Component
public class PopulateTempStateRepo {

    private final SceneCombinations sceneCombinations;
    private final TempStateRepo tempStateRepo;

    public PopulateTempStateRepo(SceneCombinations sceneCombinations, TempStateRepo tempStateRepo) {
        this.sceneCombinations = sceneCombinations;
        this.tempStateRepo = tempStateRepo;
    }

    /**
     * Creates states based on the existence of a scene's images across other scenes.
     * Adds states to the temp state repo.
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
     * @param objectCollections all images and scenes
     */
    public void createAndAddStatesForSceneToStateRepo(List<ObjectCollection> objectCollections) {
        List<StateImage> stateImages = sceneCombinations.getAllImages();
        for (int i=0; i<objectCollections.size(); i++) {
            // first, find the images in the ObjectCollection that appear in the SceneCollections
            List<StateImage> imagesInObjectAndSceneCollections = new ArrayList<>();
            objectCollections.get(i).getStateImages().forEach(img -> {
                if (stateImages.contains(img)) imagesInObjectAndSceneCollections.add(img);
            });
            getScenesPerImage(i, imagesInObjectAndSceneCollections).forEach(tempStateRepo::addImage);
        }
    }

    private List<ScenesPerImage> getScenesPerImage(int sceneIndex, List<StateImage> images) {
        List<ScenesPerImage> allScenesPerImage = new ArrayList<>();
        for (StateImage image : images) {
            // initialize the list and add the sceneIndex
            ScenesPerImage scenes = new ScenesPerImage(image);
            scenes.addScene(sceneIndex);
            // add any other scenes
            List<SceneCombination> imageInTheseSceneCombinations = sceneCombinations.getAllWithSceneAndImage(sceneIndex, image);
            Set<Integer> imageInTheseScenes = sceneCombinations.getSceneCombinationIndices(imageInTheseSceneCombinations);
            scenes.addScenes(imageInTheseScenes);
            // add ScenesPerImage object to the list
            allScenesPerImage.add(scenes);
        }
        return allScenesPerImage;
    }
}
