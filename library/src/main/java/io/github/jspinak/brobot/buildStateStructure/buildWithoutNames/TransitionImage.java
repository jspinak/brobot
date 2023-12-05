package io.github.jspinak.brobot.buildStateStructure.buildWithoutNames;

import io.github.jspinak.brobot.datatypes.primitives.image.Image;
import lombok.Getter;
import lombok.Setter;
import org.bytedeco.opencv.opencv_core.Mat;
import org.sikuli.script.Match;

import java.util.*;

/**
 * This image transitions to the screenshot with the given id.
 * In an environment with static states, there may be many images that transition to a given screenshot.
 */
@Getter
@Setter
public class TransitionImage {

    private int indexInRepo; // index in the TransitionImageRepo
    private boolean checked = false; // when checked, if id == 0 then it doesn't transition anywhere
    private Map<Integer, Integer> fromScreenToScreen = new HashMap<>(); // transitions from and to the screenshots with these ids
    private List<Integer> screensFound = new ArrayList<>();
    private Match match; // has initial location, SikuliX image (null when created), and text
    private Mat image; // the underlying image
    private int ownerState; // the owner state. there is only one owner state.
    private Set<Integer> transitionsTo = new HashSet<>(); // all states transitioned to

    public TransitionImage(Match match) {
        this.checked = false;
        this.match = match;
    }
}
