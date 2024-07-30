package io.github.jspinak.brobot.illustratedHistory;

import io.github.jspinak.brobot.datatypes.primitives.image.Image;
import lombok.Getter;
import lombok.Setter;
import org.bytedeco.opencv.opencv_core.Mat;

/**
 * States can be illustrated in various ways.
 * 1. Perhaps the easiest way to illustrate a state is to show its boundaries in a screenshot.
 * 2. Place images in their found locations.
 * The state boundaries, transition images, and other images, can all have different colored bounding boxes.
 */
@Getter
@Setter
public class StateIllustration {
    private Image screenshot; // the screenshot where the state is found
    private Image illustratedScreenshot; // the screenshot showing the state boundaries and image locations

    public StateIllustration(Image image) {
        this.screenshot = image;
        this.illustratedScreenshot = image;
    }
    public StateIllustration(Mat screenshot) {
        this.screenshot = new Image(screenshot);
        this.illustratedScreenshot = new Image(screenshot);
    }

    public StateIllustration(String illustrationFilename) {
        this.screenshot = new Image(illustrationFilename);
        this.illustratedScreenshot = new Image(illustrationFilename);
    }

    public StateIllustration() {} // for mapping

    public Mat getScreenshotAsMat() {
        return screenshot.getMatBGR();
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
