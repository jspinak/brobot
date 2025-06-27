package io.github.jspinak.brobot.analysis.motion;

import org.bytedeco.opencv.opencv_core.Mat;
import org.bytedeco.opencv.opencv_core.MatVector;
import org.springframework.context.annotation.Primary;
import org.springframework.stereotype.Component;

import io.github.jspinak.brobot.util.image.core.ColorMatrixUtilities;

/**
 * Primary implementation of {@link FindDynamicPixels} that detects pixel changes
 * across a sequence of images to identify motion.
 * 
 * <p>This class analyzes a collection of images to find pixels that have changed
 * between frames, creating masks that distinguish between dynamic (moving) and
 * fixed (stationary) pixels. It uses grayscale conversion and thresholding to
 * efficiently detect changes.</p>
 * 
 * <p>Key features:
 * <ul>
 * <li>Detects pixel changes using grayscale conversion and thresholding</li>
 * <li>Generates binary masks for both dynamic and fixed pixels</li>
 * <li>Configurable threshold values for sensitivity adjustment</li>
 * <li>Serves as the primary motion detection implementation</li>
 * </ul></p>
 * 
 * @see FindDynamicPixels
 * @see PixelChangeDetector
 * @see ColorMatrixUtilities
 */
@Primary
@Component
public class ChangedPixels implements FindDynamicPixels {

    private final ColorMatrixUtilities matOps3d;

    /**
     * Constructs a ChangedPixels instance with the specified matrix operations utility.
     * 
     * @param matOps3d utility for 3D matrix operations including bitwise operations
     */
    public ChangedPixels(ColorMatrixUtilities matOps3d) {
        this.matOps3d = matOps3d;
    }

    /**
     * Creates a binary mask identifying pixels that change across the image sequence.
     * Uses grayscale conversion and a threshold of 50 to detect significant changes.
     * White pixels (255) in the returned mask indicate dynamic/changing pixels.
     * 
     * @param matVector collection of images to analyze for pixel changes
     * @return binary Mat where dynamic pixels are white (255) and static pixels are black (0)
     */
    public Mat getDynamicPixelMask(MatVector matVector) {
        PixelChangeDetector pixelChangeDetector = new PixelChangeDetector.Builder()
                .setMats(matVector)
                .useGrayscale()
                .useThreshold(50, 255)
                .build();
        return pixelChangeDetector.getChangeMask();
    }

    /**
     * Creates a binary mask identifying pixels that remain unchanged across the image sequence.
     * This is the inverse of the dynamic pixel mask.
     * White pixels (255) in the returned mask indicate fixed/stationary pixels.
     * 
     * @param matVector collection of images to analyze for pixel changes
     * @return binary Mat where fixed pixels are white (255) and dynamic pixels are black (0)
     */
    public Mat getFixedPixelMask(MatVector matVector) {
        return matOps3d.bItwise_not(getDynamicPixelMask(matVector));
    }
}
