package io.github.jspinak.brobot.app.buildWithoutNames.screenTransitions;

import lombok.Getter;
import lombok.Setter;
import org.bytedeco.opencv.opencv_core.Mat;

/**
 * A DecisionMat contains an analysis of the differences between two screens, and helps to determine if the most
 * recently captured screen is the same or the result of a transition.
 */
@Setter
@Getter
public class DecisionMat {

    private Mat analysis;
    private Mat changedPixels;
    private Mat combinedMats;
    private int numberOfChangedPixels;
    private int screenComparedTo;

    public String getFilename() {
        return "decisionMat changed = " + numberOfChangedPixels + " compared to = " + screenComparedTo;
    }
}
