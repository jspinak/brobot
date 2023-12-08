package io.github.jspinak.brobot.buildStateStructure.buildWithoutNames;

import io.github.jspinak.brobot.datatypes.primitives.match.Matches;
import lombok.Getter;
import lombok.Setter;
import org.bytedeco.opencv.opencv_core.Mat;

import java.util.*;

/**
 * The previous ScreenObservation contains the screenshot and the image providing the transition.
 * This screenshot and image transition to the following ScreenObservation.
 */
@Getter
@Setter
public class ScreenObservation {

    private int id;
    private Mat screenshot;
    private Mat dynamicPixelMask;
    private Matches matches; // all screenshots taken when evaluating dynamic pixels

    private List<TransitionImage> images = new ArrayList<>();
    private Set<Integer> states = new HashSet<>(); // the names of included states

    public boolean hasUnvisitedImages() {
        for (TransitionImage transitionImage : images) {
            if (!transitionImage.isChecked()) return true;
        }
        return false;
    }

    public List<TransitionImage> getUnvisitedImages() {
        return images.stream().filter(image -> !image.isChecked()).toList();
    }

    public void addState(Integer state) {
        states.add(state);
    }


}
