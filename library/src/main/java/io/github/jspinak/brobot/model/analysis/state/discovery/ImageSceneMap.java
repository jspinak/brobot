package io.github.jspinak.brobot.model.analysis.state.discovery;

import io.github.jspinak.brobot.analysis.scene.SceneCombinationPopulator;
import io.github.jspinak.brobot.model.analysis.scene.SceneCombination;
import io.github.jspinak.brobot.model.state.StateImage;
import lombok.Getter;

import java.util.HashSet;
import java.util.Set;

/**
 * Associates a {@link StateImage} with all the scenes in which it appears.
 * <p>
 * This class maintains a mapping between an image and the set of scene identifiers
 * where that image has been found. It's used during scene analysis to track which
 * images appear in which scenes, facilitating scene-based image matching strategies.
 * 
 * @see SceneCombination
 * @see SceneCombinationPopulator
 */
@Getter
public class ImageSceneMap {

    private StateImage stateImage;
    private Set<Integer> scenes = new HashSet<>();

    /**
     * Creates a new ScenesPerImage instance for the specified image.
     * 
     * @param stateImage The image to associate with scenes. Must not be null.
     */
    public ImageSceneMap(StateImage stateImage) {
        this.stateImage = stateImage;
    }

    /**
     * Adds a scene identifier to this image's scene collection.
     * <p>
     * If the scene is already associated with this image, this method has no effect
     * (as the underlying collection is a Set).
     * 
     * @param scene The scene identifier to add
     */
    public void addScene(int scene) {
        this.scenes.add(scene);
    }

    /**
     * Adds multiple scene identifiers to this image's scene collection.
     * <p>
     * This is a convenience method that internally calls {@link #addScene(int)}
     * for each scene in the provided set. Duplicate scenes are automatically
     * handled by the underlying Set implementation.
     * 
     * @param scenes The collection of scene identifiers to add. Must not be null.
     */
    public void addScenes(Set<Integer> scenes) {
        for (int scene : scenes) addScene(scene);
    }
}
