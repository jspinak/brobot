package io.github.jspinak.brobot.imageUtils;

import org.bytedeco.opencv.opencv_core.Scalar;

public enum OpenCVColor {
    BLUE(255, 0, 0, 255),
    GREEN(0, 255, 0, 255),
    RED(0, 0, 255, 255);
    // Add more colors as needed

    private final Scalar scalar;

    OpenCVColor(int blue, int green, int red, int alpha) {
        this.scalar = new Scalar(blue, green, red, alpha);
    }

    public Scalar getScalar() {
        return scalar;
    }
}

