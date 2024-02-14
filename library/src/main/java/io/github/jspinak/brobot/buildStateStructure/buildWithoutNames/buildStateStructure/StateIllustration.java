package io.github.jspinak.brobot.buildStateStructure.buildWithoutNames.buildStateStructure;

import io.github.jspinak.brobot.datatypes.primitives.image.Image;
import io.github.jspinak.brobot.datatypes.primitives.image.ImageResponse;
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
    private Mat screenshot;
    private Image illustratedScreenshot;

    public StateIllustration(Mat screenshot) {
        this.screenshot = screenshot;
    }

    public StateIllustration(String illustrationFilename) {
        illustratedScreenshot = new Image(illustrationFilename);
    }

    public Mat getIllustratedScreenshotAsMat() {
        return illustratedScreenshot.getMatBGR();
    }

    public void setIllustratedScreenshot(Mat mat) {
        illustratedScreenshot = new Image(mat);
    }

    public ImageResponse toImageResponse() {
        return new ImageResponse(illustratedScreenshot);
    }

}
