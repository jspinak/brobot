package io.github.jspinak.brobot.tools.testing.mock.action;

import io.github.jspinak.brobot.model.analysis.color.ColorCluster;
import io.github.jspinak.brobot.model.element.Pattern;
import io.github.jspinak.brobot.model.element.Region;
import io.github.jspinak.brobot.util.image.recognition.ImageLoader;
import io.github.jspinak.brobot.config.FrameworkSettings;

import org.bytedeco.opencv.opencv_core.Mat;
import org.springframework.stereotype.Component;

import java.util.Random;

import static org.bytedeco.opencv.global.opencv_core.CV_32F;

/**
 * Generates mock scene data for testing color-based operations.
 * 
 * <p>MockColor creates synthetic scenes by randomly placing pattern images
 * within a specified region. This enables unit testing of color matching
 * algorithms without requiring actual screenshots or pre-captured scenes.</p>
 * 
 * <p>The mock generation process creates realistic test scenarios by:</p>
 * <ul>
 *   <li>Creating a blank canvas matching the region dimensions</li>
 *   <li>Loading the pattern image from the configured package path</li>
 *   <li>Randomly placing the pattern 0-9 times within the region</li>
 * </ul>
 * 
 * <p>This component is primarily used when BrobotSettings.mock is enabled,
 * allowing deterministic testing of color-based find operations.</p>
 * 
 * @see ImageLoader
 * @see FrameworkSettings
 */
@Component
public class MockColor {

    private ImageLoader getImage;

    public MockColor(ImageLoader getImage) {
        this.getImage = getImage;
    }

    /**
     * Creates a mock scene by randomly placing a pattern image within a region.
     * 
     * <p>Generates a synthetic scene for testing by:</p>
     * <ol>
     *   <li>Creating a blank Mat matching the region dimensions</li>
     *   <li>Loading the pattern image from the configured path</li>
     *   <li>Copying the pattern to random positions 0-9 times</li>
     * </ol>
     * 
     * <p>The random placement simulates realistic scenarios where patterns
     * may appear multiple times at various locations within a scene.</p>
     * 
     * <p>Note: Current implementation may have edge case issues with pattern
     * placement near region boundaries.</p>
     * 
     * @param image the pattern to place in the mock scene
     * @param region defines the dimensions of the mock scene
     * @return Mat containing the pattern randomly placed on a blank background
     */
    public Mat getMockMat(Pattern image, Region region) {
        Mat mat = new Mat(Mat.zeros(region.h(), region.w(), CV_32F));
        Mat img = getImage.getMatFromFilename(
                FrameworkSettings.packageName+"/"+image.getImgpath(), ColorCluster.ColorSchemaName.BGR);
        int n = new Random().nextInt(10);
        for (int i=0; i<n; i++) {
            int row = new Random().nextInt(region.h());
            int col = new Random().nextInt(region.w());
            img.copyTo(mat.rowRange(0, row).colRange(0, col));
        }
        return mat;
    }
}
