package io.github.jspinak.brobot.actions.methods.basicactions.find.motion;

import org.bytedeco.opencv.opencv_core.Mat;
import org.bytedeco.opencv.opencv_core.MatVector;

public interface FindDynamicPixels {

    Mat getDynamicPixelMask(MatVector matVector);
    Mat getFixedPixelMask(MatVector matVector);
}
