package io.github.jspinak.brobot.model.analysis.scene;

import io.github.jspinak.brobot.analysis.scene.SceneCombinationPopulator;
import io.github.jspinak.brobot.analysis.scene.SceneCombinationStore;
import io.github.jspinak.brobot.model.state.StateImage;
import lombok.Getter;
import lombok.Setter;
import org.bytedeco.opencv.opencv_core.Mat;

import java.util.ArrayList;
import java.util.List;

/**
 * Represents a combination of two scenes and the images that appear in both.
 * <p>
 * A scene combination identifies which images are common between two scenes,
 * helping to determine state transitions and image persistence across different
 * application states. The dynamic pixels matrix captures areas of change between
 * the two scenes, which can be used for motion detection and state change analysis.
 * 
 * @see SceneCombinationStore
 * @see SceneCombinationPopulator
 */
@Getter
@Setter
public class SceneCombination {

    @com.fasterxml.jackson.annotation.JsonIgnore
    private Mat dynamicPixels;
    private int scene1;
    private int scene2;
    private List<StateImage> images = new ArrayList<>();

    /**
     * Creates a new scene combination between two scenes.
     * 
     * @param dynamicPixels A matrix representing pixels that changed between the two scenes.
     *                      Can be used for motion analysis and state change detection.
     * @param scene1 The index of the first scene
     * @param scene2 The index of the second scene
     */
    public SceneCombination(Mat dynamicPixels, int scene1, int scene2) {
        this.dynamicPixels = dynamicPixels;
        this.scene1 = scene1;
        this.scene2 = scene2;
    }

    /**
     * Adds an image to this scene combination.
     * <p>
     * The added image represents an element that appears in both scenes
     * of this combination.
     * 
     * @param stateImage The image to add to this combination. Must not be null.
     */
    public void addImage(StateImage stateImage) {
        images.add(stateImage);
    }

    /**
     * Checks if this scene combination contains the specified image.
     * 
     * @param stateImage The image to check for
     * @return true if the image is part of this scene combination, false otherwise
     */
    public boolean contains(StateImage stateImage) {
        return images.contains(stateImage);
    }

    /**
     * Checks if this scene combination involves the specified scene.
     * 
     * @param scene The scene index to check
     * @return true if this combination includes the specified scene (as either scene1 or scene2),
     *         false otherwise
     */
    public boolean contains(int scene) {
        return scene1 == scene || scene2 == scene;
    }

    @Override
    public String toString() {
        StringBuilder stringBuilder = new StringBuilder();
        stringBuilder.append("Scenes: ").append(scene1).append(" ").append(scene2).append(" ");
        stringBuilder.append("Images: ");
        images.forEach(image -> stringBuilder.append(image).append(" "));
        return stringBuilder.toString();
    }

}
