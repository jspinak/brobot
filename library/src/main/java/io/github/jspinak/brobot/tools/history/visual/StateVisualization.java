package io.github.jspinak.brobot.tools.history.visual;

import org.bytedeco.opencv.opencv_core.Mat;

import io.github.jspinak.brobot.model.element.Image;

import lombok.Getter;
import lombok.Setter;

/**
 * States can be illustrated in various ways. 1. Perhaps the easiest way to illustrate a state is to
 * show its boundaries in a screenshot. 2. Place images in their found locations. The state
 * boundaries, transition images, and other images, can all have different colored bounding boxes.
 * This is no longer part of the State object: the State has a Scene, which is used by the React
 * frontend as the background image, over which the state images are highlighted.
 */
@Getter
@Setter
public class StateVisualization {
    private Image
            screenshotUsableArea; // the usable area of the screenshot where the state is found
    private Image illustratedScreenshot; // the usable area showing the state boundaries and image

    // locations

    public StateVisualization(Image image) {
        this.screenshotUsableArea = image;
        this.illustratedScreenshot = image;
    }

    public StateVisualization(Mat screenshotUsableArea) {
        this.screenshotUsableArea = new Image(screenshotUsableArea);
        this.illustratedScreenshot = new Image(screenshotUsableArea);
    }

    public StateVisualization(String illustrationFilename) {
        this.screenshotUsableArea = new Image(illustrationFilename);
        this.illustratedScreenshot = new Image(illustrationFilename);
    }

    public StateVisualization() {} // for mapping

    public Mat getScreenshotAsMat() {
        return screenshotUsableArea.getMatBGR();
    }

    public Mat getIllustratedScreenshotAsMat() {
        return illustratedScreenshot.getMatBGR();
    }

    public void setIllustratedScreenshot(Image image) {
        illustratedScreenshot = image;
    }

    public void setIllustratedScreenshot(Mat mat) {
        illustratedScreenshot = new Image(mat);
    }
}
