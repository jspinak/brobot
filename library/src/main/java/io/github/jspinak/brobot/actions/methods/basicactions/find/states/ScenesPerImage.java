package io.github.jspinak.brobot.actions.methods.basicactions.find.states;

import io.github.jspinak.brobot.datatypes.state.stateObject.stateImage.StateImage;
import lombok.Getter;

import java.util.HashSet;
import java.util.Set;

/**
 * Records all scenes associated with an image, as found in SceneCombinations.
 */
@Getter
public class ScenesPerImage {

    private StateImage stateImage;
    private Set<Integer> scenes = new HashSet<>();

    public ScenesPerImage(StateImage stateImage) {
        this.stateImage = stateImage;
    }

    public void addScene(int scene) {
        this.scenes.add(scene);
    }

    public void addScenes(Set<Integer> scenes) {
        for (int scene : scenes) addScene(scene);
    }
}
