package io.github.jspinak.brobot.util.image.constants;

import org.bytedeco.opencv.opencv_core.Scalar;

/**
 * Predefined color constants for OpenCV operations in BGR format.
 *
 * <p>This enum provides commonly used colors as OpenCV Scalar objects, properly formatted for
 * OpenCV's BGR (Blue-Green-Red) color space. Each color includes full opacity (alpha = 255) by
 * default.
 *
 * <p>Color format:
 *
 * <ul>
 *   <li>OpenCV uses BGR order, not RGB
 *   <li>Values range from 0-255 for each channel
 *   <li>Alpha channel is included (255 = fully opaque)
 *   <li>Format: Scalar(Blue, Green, Red, Alpha)
 * </ul>
 *
 * <p>Usage examples:
 *
 * <ul>
 *   <li>Drawing operations: rectangle(), circle(), line()
 *   <li>Color matching and thresholding
 *   <li>Visualization and debugging
 *   <li>UI element highlighting
 * </ul>
 *
 * <p>Common pitfall: Remember that OpenCV uses BGR, not RGB. So RED is (0,0,255) not (255,0,0) as
 * you might expect from RGB systems.
 *
 * <p>To add more colors, follow the pattern: COLORNAME(blue_value, green_value, red_value, 255)
 *
 * @see Scalar
 */
public enum BgrColorConstants {
    /** Pure blue color (BGR: 255, 0, 0). */
    BLUE(255, 0, 0, 255),

    /** Pure green color (BGR: 0, 255, 0). */
    GREEN(0, 255, 0, 255),

    /** Pure red color (BGR: 0, 0, 255). Note: In BGR format, red has blue=0, green=0, red=255. */
    RED(0, 0, 255, 255);

    // Add more colors as needed

    /** The OpenCV Scalar representation of this color. */
    private final Scalar scalar;

    /**
     * Constructs a color with specified BGR and alpha values.
     *
     * <p>The parameters follow OpenCV's BGR color order convention. All values should be in the
     * range 0-255.
     *
     * @param blue blue channel intensity (0-255)
     * @param green green channel intensity (0-255)
     * @param red red channel intensity (0-255)
     * @param alpha opacity value (0-255, where 255 is fully opaque)
     */
    BgrColorConstants(int blue, int green, int red, int alpha) {
        this.scalar = new Scalar(blue, green, red, alpha);
    }

    /**
     * Returns the OpenCV Scalar representation of this color.
     *
     * <p>The returned Scalar can be used directly in OpenCV drawing and image processing functions
     * that require color parameters.
     *
     * @return Scalar containing BGR color values and alpha channel
     */
    public Scalar getScalar() {
        return scalar;
    }
}
