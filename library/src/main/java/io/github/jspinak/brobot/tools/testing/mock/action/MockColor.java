package io.github.jspinak.brobot.tools.testing.mock.action;

import static org.bytedeco.opencv.global.opencv_core.CV_32F;

import java.util.Random;

import org.bytedeco.opencv.opencv_core.Mat;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import io.github.jspinak.brobot.config.core.BrobotProperties;
import io.github.jspinak.brobot.model.analysis.color.ColorCluster;
import io.github.jspinak.brobot.model.element.Pattern;
import io.github.jspinak.brobot.model.element.Region;
import io.github.jspinak.brobot.util.image.recognition.ImageLoader;

/**
 * Generates mock scene data for testing color-based operations.
 *
 * <p>MockColor creates synthetic scenes by randomly placing pattern images within a specified
 * region. This enables unit testing of color matching algorithms without requiring actual
 * screenshots or pre-captured scenes.
 *
 * <p>The mock generation process creates realistic test scenarios by:
 *
 * <ul>
 *   <li>Creating a blank canvas matching the region dimensions
 *   <li>Loading the pattern image from the configured package path
 *   <li>Randomly placing the pattern 0-9 times within the region
 * </ul>
 *
 * <p>This component is primarily used when BrobotProperties configuration is enabled, allowing
 * deterministic testing of color-based find operations.
 *
 * @see ImageLoader
 * @see BrobotProperties
 */
@Component
public class MockColor {

    private final BrobotProperties brobotProperties;
    private final ImageLoader getImage;

    @Autowired
    public MockColor(BrobotProperties brobotProperties, ImageLoader getImage) {
        this.brobotProperties = brobotProperties;
        this.getImage = getImage;
    }

    /**
     * Creates a mock scene by randomly placing a pattern image within a region.
     *
     * <p>Generates a synthetic scene for testing by:
     *
     * <ol>
     *   <li>Creating a blank Mat matching the region dimensions
     *   <li>Loading the pattern image from the configured path
     *   <li>Copying the pattern to random positions 0-9 times
     * </ol>
     *
     * <p>The random placement simulates realistic scenarios where patterns may appear multiple
     * times at various locations within a scene.
     *
     * <p>Note: Current implementation may have edge case issues with pattern placement near region
     * boundaries.
     *
     * @param image the pattern to place in the mock scene
     * @param region defines the dimensions of the mock scene
     * @return Mat containing the pattern randomly placed on a blank background
     */
    public Mat getMockMat(Pattern image, Region region) {
        Mat mat = new Mat(Mat.zeros(region.h(), region.w(), CV_32F));
        Mat img =
                getImage.getMatFromFilename(
                        brobotProperties.getCore().getPackageName() + "/" + image.getImgpath(),
                        ColorCluster.ColorSchemaName.BGR);
        int n = new Random().nextInt(10);
        for (int i = 0; i < n; i++) {
            int row = new Random().nextInt(region.h());
            int col = new Random().nextInt(region.w());
            img.copyTo(mat.rowRange(0, row).colRange(0, col));
        }
        return mat;
    }
}
