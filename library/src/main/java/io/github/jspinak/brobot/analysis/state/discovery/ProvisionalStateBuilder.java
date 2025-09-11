package io.github.jspinak.brobot.analysis.state.discovery;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

import org.springframework.stereotype.Component;

import io.github.jspinak.brobot.action.ObjectCollection;
import io.github.jspinak.brobot.analysis.scene.SceneCombinationStore;
import io.github.jspinak.brobot.model.analysis.scene.SceneCombination;
import io.github.jspinak.brobot.model.analysis.state.discovery.ImageSceneMap;
import io.github.jspinak.brobot.model.state.StateImage;

/**
 * Populates the temporary state repository with states derived from scene combinations.
 *
 * <p>This component analyzes how images appear across different scenes and creates states based on
 * their distribution patterns. It uses scene combination data to determine which images belong to
 * which states, facilitating more accurate state identification during automation.
 *
 * @see ProvisionalStateStore
 * @see SceneCombinationStore
 * @see ImageSceneMap
 */
@Component
public class ProvisionalStateBuilder {

    private final SceneCombinationStore sceneCombinations;
    private final ProvisionalStateStore tempStateRepo;

    public ProvisionalStateBuilder(
            SceneCombinationStore sceneCombinations, ProvisionalStateStore tempStateRepo) {
        this.sceneCombinations = sceneCombinations;
        this.tempStateRepo = tempStateRepo;
    }

    /**
     * Creates states based on the existence of a scene's images across other scenes.
     *
     * <p>This method analyzes the distribution of images across different scene combinations to
     * create logical states. Each state is populated with images that share the same scene
     * membership pattern. The temporary state repository is modified by adding these newly created
     * states.
     *
     * <p><b>Example:</b>
     *
     * <pre>
     * Scene 1 has images a,b,c,d,e,f
     * SceneCombination 1-2 has images a,b,c
     * SceneCombination 1-3 has images a,b,d
     * SceneCombination 1-4 has images b,d,e
     *
     * This generates 6 states:
     *   State 1 has image f (only in scene 1)
     *   State 1-2 has image c (in scenes 1 and 2)
     *   State 1-2-3 has image a (in scenes 1, 2, and 3)
     *   State 1-2-3-4 has image b (in all scenes)
     *   State 1-3-4 has image d (in scenes 1, 3, and 4)
     *   State 1-4 has image e (in scenes 1 and 4)
     * </pre>
     *
     * @param objectCollections The list of object collections containing all images and scenes to
     *     be analyzed. Must not be null or empty.
     */
    public void createAndAddStatesForSceneToStateRepo(List<ObjectCollection> objectCollections) {
        List<StateImage> stateImages = sceneCombinations.getAllImages();
        for (int i = 0; i < objectCollections.size(); i++) {
            // first, find the images in the ObjectCollection that appear in the SceneCollections
            List<StateImage> imagesInObjectAndSceneCollections = new ArrayList<>();
            objectCollections
                    .get(i)
                    .getStateImages()
                    .forEach(
                            img -> {
                                if (stateImages.contains(img))
                                    imagesInObjectAndSceneCollections.add(img);
                            });
            getScenesPerImage(i, imagesInObjectAndSceneCollections)
                    .forEach(tempStateRepo::addImage);
        }
    }

    /**
     * Determines which scenes contain each of the provided images.
     *
     * <p>For each image, this method creates a {@link ImageSceneMap} object that tracks all the
     * scenes where the image appears. It starts with the current scene index and then searches
     * through all scene combinations to find additional scenes containing the image.
     *
     * @param sceneIndex The index of the current scene being processed
     * @param images The list of images to analyze for scene membership
     * @return A list of {@link ImageSceneMap} objects, one for each input image, containing all
     *     scenes where that image appears
     */
    private List<ImageSceneMap> getScenesPerImage(int sceneIndex, List<StateImage> images) {
        List<ImageSceneMap> allScenesPerImage = new ArrayList<>();
        for (StateImage image : images) {
            // initialize the list and add the sceneIndex
            ImageSceneMap scenes = new ImageSceneMap(image);
            scenes.addScene(sceneIndex);
            // add any other scenes
            List<SceneCombination> imageInTheseSceneCombinations =
                    sceneCombinations.getAllWithSceneAndImage(sceneIndex, image);
            Set<Integer> imageInTheseScenes =
                    sceneCombinations.getSceneCombinationIndices(imageInTheseSceneCombinations);
            scenes.addScenes(imageInTheseScenes);
            // add ScenesPerImage object to the list
            allScenesPerImage.add(scenes);
        }
        return allScenesPerImage;
    }
}
