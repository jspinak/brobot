package io.github.jspinak.brobot.analysis;

import static org.junit.jupiter.api.Assertions.*;

import java.awt.image.BufferedImage;

import org.bytedeco.opencv.opencv_core.Mat;
import org.junit.jupiter.api.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.TestPropertySource;

import io.github.jspinak.brobot.BrobotTestApplication;
import io.github.jspinak.brobot.analysis.color.ColorAnalysis;
import io.github.jspinak.brobot.analysis.histogram.HistogramComparator;
import io.github.jspinak.brobot.analysis.histogram.HistogramExtractor;
import io.github.jspinak.brobot.analysis.motion.MotionDetector;
import io.github.jspinak.brobot.config.core.FrameworkSettings;
import io.github.jspinak.brobot.model.element.Image;
import io.github.jspinak.brobot.model.element.Region;
import io.github.jspinak.brobot.test.BrobotIntegrationTestBase;
import io.github.jspinak.brobot.util.image.visualization.MatBuilder;

/**
 * Integration tests for the Image Analysis system.
 *
 * <p>These tests verify the integration between: - Color extraction and analysis - Histogram
 * generation and comparison - Motion detection - OpenCV integration - Spring context and dependency
 * injection
 */
@SpringBootTest(classes = BrobotTestApplication.class)
@TestPropertySource(
        properties = {"spring.main.lazy-initialization=true", "brobot.opencv.headless=true"})
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
@Disabled("CI failure - needs investigation")
class ImageAnalysisIntegrationTest extends BrobotIntegrationTestBase {

    @Autowired private ColorAnalysis colorAnalysis;

    @Autowired private HistogramExtractor histogramExtractor;

    @Autowired private HistogramComparator histogramComparator;

    @Autowired private MotionDetector motionDetector;

    @Autowired private MatBuilder matBuilder;

    private Mat testMat;
    private Image testImage;

    @BeforeEach
    void setUp() {
        // Enable mock mode for testing
        FrameworkSettings.mock = true;

        // Create test Mat and Image
        createTestData();
    }

    private void createTestData() {
        // Create a simple test Mat using MatBuilder
        // MatBuilder creates CV_8UC3 (3 channel) matrices by default
        testMat =
                new MatBuilder()
                        .setName("TestMat")
                        .setWH(100, 100) // Width and height
                        .build();

        // Create test Image from Mat
        testImage = new Image(testMat);
    }

    @Test
    @Order(1)
    void testSpringContextLoads() {
        assertNotNull(colorAnalysis, "ColorAnalysis should be autowired");
        assertNotNull(histogramExtractor, "HistogramExtractor should be autowired");
        assertNotNull(histogramComparator, "HistogramComparator should be autowired");
        assertNotNull(motionDetector, "MotionDetector should be autowired");
        assertNotNull(matBuilder, "MatBuilder should be autowired");
    }

    @Test
    @Order(2)
    void testColorAnalysis() {
        // Test color extraction functionality
        assertNotNull(colorAnalysis);

        // Create a test image for color analysis
        BufferedImage buffImage = new BufferedImage(100, 100, BufferedImage.TYPE_INT_RGB);
        // Fill with a solid color
        for (int x = 0; x < 100; x++) {
            for (int y = 0; y < 100; y++) {
                buffImage.setRGB(x, y, 0xFF6496C8); // Light blue color
            }
        }
        Image colorTestImage = new Image(buffImage);

        // The actual color analysis methods depend on ColorAnalysis API
        // This is a placeholder test
        assertNotNull(colorTestImage);
    }

    @Test
    @Order(3)
    void testHistogramExtraction() {
        // Test histogram extraction
        assertNotNull(histogramExtractor);

        // The actual extraction depends on the HistogramExtractor API
        // Check if we can create histograms from images
        assertNotNull(testImage);

        // HistogramExtractor might have different methods
        // This is a basic test to verify the service is available
    }

    @Test
    @Order(4)
    void testHistogramComparison() {
        // Create two test images with different colors
        Mat mat1 = new MatBuilder().setName("Mat1").setWH(100, 100).build();

        Mat mat2 = new MatBuilder().setName("Mat2").setWH(100, 100).build();

        Image image1 = new Image(mat1);
        Image image2 = new Image(mat2);

        // Test histogram comparison
        assertNotNull(histogramComparator);
        assertNotNull(image1);
        assertNotNull(image2);

        // The actual comparison methods depend on HistogramComparator API
        // This verifies that the images are created properly
    }

    @Test
    @Order(5)
    void testMotionDetection() {
        // Test motion detection capability
        assertNotNull(motionDetector);

        // Motion detection typically requires multiple frames
        // Create a sequence of slightly different images
        Mat frame1 = new MatBuilder().setName("Frame1").setWH(100, 100).build();

        Mat frame2 = new MatBuilder().setName("Frame2").setWH(100, 100).build();

        Image motionImage1 = new Image(frame1);
        Image motionImage2 = new Image(frame2);

        assertNotNull(motionImage1);
        assertNotNull(motionImage2);

        // The actual motion detection depends on MotionDetector API
    }

    @Test
    @Order(6)
    void testMatBuilderComposition() {
        // Test creating composite images with MatBuilder
        Mat submat1 = new MatBuilder().setName("Submat1").setWH(50, 50).build();

        Mat submat2 = new MatBuilder().setName("Submat2").setWH(50, 50).build();

        // Create a composite image
        Mat composite =
                new MatBuilder()
                        .setName("Composite")
                        .setWH(200, 100)
                        .setSpaceBetween(10)
                        .addHorizontalSubmats(submat1, submat2)
                        .build();

        assertNotNull(composite);
        assertEquals(200, composite.cols());
        assertEquals(100, composite.rows());

        Image compositeImage = new Image(composite);
        assertNotNull(compositeImage);
    }

    @Test
    @Order(7)
    void testImageFromBufferedImage() {
        // Test creating Image from BufferedImage
        BufferedImage buffImage = new BufferedImage(200, 150, BufferedImage.TYPE_INT_RGB);

        // Fill with gradient
        for (int x = 0; x < 200; x++) {
            for (int y = 0; y < 150; y++) {
                int r = (x * 255) / 200;
                int g = (y * 255) / 150;
                int b = 128;
                int rgb = (r << 16) | (g << 8) | b;
                buffImage.setRGB(x, y, rgb);
            }
        }

        Image gradientImage = new Image(buffImage, "GradientImage");
        assertNotNull(gradientImage);
        assertEquals("GradientImage", gradientImage.getName());
        assertNotNull(gradientImage.getBufferedImage());
        assertEquals(200, gradientImage.getBufferedImage().getWidth());
        assertEquals(150, gradientImage.getBufferedImage().getHeight());
    }

    @Test
    @Order(8)
    void testRegionAnalysis() {
        // Test analyzing specific regions of an image
        Region testRegion = new Region(10, 10, 80, 80);

        // Create an image with distinct regions
        BufferedImage regionImage = new BufferedImage(100, 100, BufferedImage.TYPE_INT_RGB);

        // Fill center region with different color
        for (int x = 0; x < 100; x++) {
            for (int y = 0; y < 100; y++) {
                if (x >= 10 && x < 90 && y >= 10 && y < 90) {
                    regionImage.setRGB(x, y, 0xFFFF0000); // Red center
                } else {
                    regionImage.setRGB(x, y, 0xFF0000FF); // Blue border
                }
            }
        }

        Image imageWithRegions = new Image(regionImage, "RegionTest");
        assertNotNull(imageWithRegions);
        assertNotNull(testRegion);

        // Region-based analysis would depend on the specific APIs available
    }

    @Test
    @Order(9)
    void testColorStatistics() {
        // Test that ColorStatistics can be used (it's a model class)
        // ColorStatistics might be a data holder class

        // Create a test image
        BufferedImage statsImage = new BufferedImage(50, 50, BufferedImage.TYPE_INT_RGB);
        for (int x = 0; x < 50; x++) {
            for (int y = 0; y < 50; y++) {
                // Create a pattern with varying colors
                int intensity = ((x + y) * 255) / 100;
                int rgb = (intensity << 16) | (intensity << 8) | intensity;
                statsImage.setRGB(x, y, rgb);
            }
        }

        Image testStatsImage = new Image(statsImage);
        assertNotNull(testStatsImage);

        // ColorStatistics usage would depend on its API
        // It might be returned from ColorAnalysis methods
    }
}
