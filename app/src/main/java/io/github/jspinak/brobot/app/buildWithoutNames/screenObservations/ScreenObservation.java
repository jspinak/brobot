package io.github.jspinak.brobot.app.buildWithoutNames.screenObservations;

import io.github.jspinak.brobot.datatypes.primitives.image.Pattern;
import io.github.jspinak.brobot.datatypes.primitives.match.Matches;
import io.github.jspinak.brobot.datatypes.primitives.region.Region;
import lombok.Getter;
import lombok.Setter;
import org.bytedeco.opencv.opencv_core.Mat;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * The previous ScreenObservation contains the screenshot and the image providing the transition.
 * This screenshot and image transition to the following ScreenObservation.
 */
@Getter
@Setter
public class ScreenObservation {

    private int id;
    private Pattern pattern;
    private Mat screenshot;
    private Mat dynamicPixelMask;
    private Matches matches; // includes the SceneAnalysisCollection and all screenshots taken when evaluating dynamic pixels
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

    public List<Region> getImageRegions() {
        List<Region> imageMatches = new ArrayList<>();
        images.forEach(ti -> imageMatches.add(ti.getRegion()));
        return imageMatches;
    }

    @Override
    public String toString() {
        StringBuilder stateNames = new StringBuilder();
        int i = 0;
        for (Integer integer : states) {
            stateNames.append(integer.toString());
            i++;
            if (i<states.size()) stateNames.append(",");
        }
        return "id=" + id + " | images=" + images.size() + " | states=" + stateNames;
    }

}
