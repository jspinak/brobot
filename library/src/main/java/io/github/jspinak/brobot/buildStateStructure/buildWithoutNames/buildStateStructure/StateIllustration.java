package io.github.jspinak.brobot.buildStateStructure.buildWithoutNames.buildStateStructure;

import lombok.Getter;
import lombok.Setter;
import org.bytedeco.opencv.opencv_core.Mat;

/**
 * States can be illustrated in various ways.
 * 1. Perhaps the easiest way to illustrate a state is to show its boundaries in a screenshot.
 *    The state boundaries, transition images, and other images, can all have different colored bounding boxes.
 */
@Getter
@Setter
public class StateIllustration {
    private Mat screenshot;
    private Mat illustratedScreenshot;

    public StateIllustration(Mat screenshot) {
        this.screenshot = screenshot;
    }

}
