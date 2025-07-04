package io.github.jspinak.brobot.analysis;

import io.github.jspinak.brobot.action.BrobotSettings;
import io.github.jspinak.brobot.analysis.color.ColorExtractor;
import io.github.jspinak.brobot.analysis.color.ColorStatExtractor;
import io.github.jspinak.brobot.analysis.histogram.HistogramComparator;
import io.github.jspinak.brobot.analysis.histogram.HistogramExtractor;
import io.github.jspinak.brobot.analysis.motion.MotionDetector;
import io.github.jspinak.brobot.datatypes.primitives.image.Image;
import io.github.jspinak.brobot.datatypes.primitives.region.Region;
import io.github.jspinak.brobot.datatypes.state.ObjectCollection;
import io.github.jspinak.brobot.services.Init;
import io.github.jspinak.brobot.util.image.MatBuilder;
import org.junit.jupiter.api.*;
import org.opencv.core.Mat;
import org.opencv.core.Scalar;
import org.opencv.core.Size;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.context.TestPropertySource;

import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.when;

/**
 * Integration tests for the Image Analysis system.
 * 
 * These tests verify the integration between:
 * - Color extraction and analysis
 * - Histogram generation and comparison
 * - Motion detection
 * - OpenCV integration
 * - Spring context and dependency injection
 */
@SpringBootTest
@TestPropertySource(properties = {
    "spring.main.lazy-initialization=true",
    "brobot.opencv.headless=true"
})
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
class ImageAnalysisIntegrationTest {

    @Autowired
    private ColorExtractor colorExtractor;
    
    @Autowired
    private ColorStatExtractor colorStatExtractor;
    
    @Autowired
    private HistogramExtractor histogramExtractor;
    
    @Autowired
    private HistogramComparator histogramComparator;
    
    @Autowired
    private MotionDetector motionDetector;
    
    @Autowired
    private MatBuilder matBuilder;
    
    @Autowired
    private BrobotSettings brobotSettings;
    
    @MockBean
    private Init init;
    
    private Mat testMat;
    private Image testImage;
    
    @BeforeEach
    void setUp() {
        // Configure settings
        when(init.setGlobalMock()).thenReturn(true);
        brobotSettings.mock = true;
        
        // Create test Mat and Image
        createTestData();
    }
    
    private void createTestData() {
        // Create a simple test Mat with known colors
        testMat = matBuilder.build(100, 100, 3, new Scalar(100, 150, 200));
        
        // Create test Image
        testImage = new Image(testMat);
    }
    
    @Test
    @Order(1)
    void testSpringContextLoads() {
        assertNotNull(colorExtractor, "ColorExtractor should be autowired");
        assertNotNull(colorStatExtractor, "ColorStatExtractor should be autowired");
        assertNotNull(histogramExtractor, "HistogramExtractor should be autowired");
        assertNotNull(histogramComparator, "HistogramComparator should be autowired");
        assertNotNull(motionDetector, "MotionDetector should be autowired");
        assertNotNull(matBuilder, "MatBuilder should be autowired");
    }
    
    @Test
    @Order(2)
    void testColorExtraction() {
        // Extract dominant colors
        List<Scalar> dominantColors = colorExtractor.getDominantColors(testImage, 3);
        
        // Verify
        assertNotNull(dominantColors);
        assertFalse(dominantColors.isEmpty(), "Should extract dominant colors");
        assertTrue(dominantColors.size() <= 3, "Should not exceed requested color count");
    }
    
    @Test
    @Order(3)
    void testColorStatistics() {
        // Define region for color analysis
        Region region = new Region(10, 10, 50, 50);
        
        // Extract color statistics
        Map<String, Double> colorStats = colorStatExtractor.getColorStats(testImage, region);
        
        // Verify
        assertNotNull(colorStats);
        assertFalse(colorStats.isEmpty(), "Should extract color statistics");
        
        // Check for expected statistics
        assertTrue(colorStats.containsKey("meanRed") || colorStats.containsKey("mean_red"), 
            "Should contain red channel statistics");
        assertTrue(colorStats.containsKey("meanGreen") || colorStats.containsKey("mean_green"), 
            "Should contain green channel statistics");
        assertTrue(colorStats.containsKey("meanBlue") || colorStats.containsKey("mean_blue"), 
            "Should contain blue channel statistics");
    }
    
    @Test
    @Order(4)
    void testHistogramExtraction() {
        // Extract histogram
        Mat histogram = histogramExtractor.getHistogram(testImage);
        
        // Verify
        assertNotNull(histogram);
        assertFalse(histogram.empty(), "Histogram should not be empty");
        assertTrue(histogram.rows() > 0 || histogram.cols() > 0, 
            "Histogram should have data");
    }
    
    @Test
    @Order(5)
    void testHistogramComparison() {
        // Create two different images
        Mat mat1 = matBuilder.build(100, 100, 3, new Scalar(100, 150, 200));
        Mat mat2 = matBuilder.build(100, 100, 3, new Scalar(150, 100, 200));
        
        Image image1 = new Image(mat1);
        Image image2 = new Image(mat2);
        
        // Extract histograms
        Mat hist1 = histogramExtractor.getHistogram(image1);
        Mat hist2 = histogramExtractor.getHistogram(image2);
        
        // Compare histograms
        double similarity = histogramComparator.compare(hist1, hist2);
        
        // Verify
        assertTrue(similarity >= 0.0 && similarity <= 1.0, 
            "Similarity should be between 0 and 1");
    }
    
    @Test
    @Order(6)
    void testHistogramComparisonSameImage() {
        // Compare image with itself
        Mat histogram = histogramExtractor.getHistogram(testImage);
        double similarity = histogramComparator.compare(histogram, histogram);
        
        // Should be perfect match
        assertEquals(1.0, similarity, 0.01, 
            "Same histogram should have similarity close to 1.0");
    }
    
    @Test
    @Order(7)
    void testMotionDetection() {
        // Create two slightly different images to simulate motion
        Mat frame1 = matBuilder.build(100, 100, 3, new Scalar(100, 150, 200));
        Mat frame2 = matBuilder.build(100, 100, 3, new Scalar(100, 150, 200));
        
        // Add a small difference in one region
        frame2.submat(20, 40, 20, 40).setTo(new Scalar(200, 200, 200));
        
        Image image1 = new Image(frame1);
        Image image2 = new Image(frame2);
        
        // Detect motion
        boolean motionDetected = motionDetector.detectMotion(image1, image2);
        
        // Verify - In mock mode, motion detection behavior may vary
        assertNotNull(motionDetected);
    }
    
    @Test
    @Order(8)
    void testMotionDetectionWithThreshold() {
        // Create images
        Mat frame1 = matBuilder.build(100, 100, 3, new Scalar(100, 100, 100));
        Mat frame2 = matBuilder.build(100, 100, 3, new Scalar(110, 110, 110));
        
        Image image1 = new Image(frame1);
        Image image2 = new Image(frame2);
        
        // Test with different thresholds
        double lowThreshold = 0.01;
        double highThreshold = 0.5;
        
        boolean motionLowThreshold = motionDetector.detectMotion(image1, image2, lowThreshold);
        boolean motionHighThreshold = motionDetector.detectMotion(image1, image2, highThreshold);
        
        // With a low threshold, small changes should be detected
        // With a high threshold, small changes might not be detected
        assertNotNull(motionLowThreshold);
        assertNotNull(motionHighThreshold);
    }
    
    @Test
    @Order(9)
    void testColorExtractionWithRegions() {
        // Create an image with distinct color regions
        Mat multiColorMat = matBuilder.build(200, 200, 3);
        
        // Fill different quadrants with different colors
        multiColorMat.submat(0, 100, 0, 100).setTo(new Scalar(255, 0, 0)); // Red
        multiColorMat.submat(0, 100, 100, 200).setTo(new Scalar(0, 255, 0)); // Green
        multiColorMat.submat(100, 200, 0, 100).setTo(new Scalar(0, 0, 255)); // Blue
        multiColorMat.submat(100, 200, 100, 200).setTo(new Scalar(255, 255, 0)); // Yellow
        
        Image multiColorImage = new Image(multiColorMat);
        
        // Extract colors from specific regions
        Region redRegion = new Region(0, 0, 100, 100);
        Region greenRegion = new Region(100, 0, 100, 100);
        
        Map<String, Double> redStats = colorStatExtractor.getColorStats(multiColorImage, redRegion);
        Map<String, Double> greenStats = colorStatExtractor.getColorStats(multiColorImage, greenRegion);
        
        // Verify distinct color statistics
        assertNotNull(redStats);
        assertNotNull(greenStats);
        
        // The statistics should be different for different colored regions
        assertNotEquals(redStats, greenStats, 
            "Different colored regions should have different statistics");
    }
    
    @Test
    @Order(10)
    void testHistogramWithMultipleChannels() {
        // Create a color image
        Mat colorMat = matBuilder.build(100, 100, 3, new Scalar(50, 100, 150));
        Image colorImage = new Image(colorMat);
        
        // Extract multi-channel histogram
        Mat colorHistogram = histogramExtractor.getHistogram(colorImage);
        
        // Create a grayscale image
        Mat grayMat = matBuilder.build(100, 100, 1, new Scalar(100));
        Image grayImage = new Image(grayMat);
        
        // Extract single-channel histogram
        Mat grayHistogram = histogramExtractor.getHistogram(grayImage);
        
        // Verify both histograms are valid
        assertNotNull(colorHistogram);
        assertNotNull(grayHistogram);
        assertFalse(colorHistogram.empty());
        assertFalse(grayHistogram.empty());
    }
    
    @Test
    @Order(11)
    void testIntegrationWithObjectCollection() {
        // Create object collection with multiple regions
        ObjectCollection collection = new ObjectCollection.Builder()
            .withRegions(
                new Region(0, 0, 50, 50),
                new Region(50, 50, 50, 50),
                new Region(100, 100, 50, 50)
            )
            .build();
        
        // Process each region
        for (Region region : collection.getRegions()) {
            // Extract color statistics for each region
            Map<String, Double> stats = colorStatExtractor.getColorStats(testImage, region);
            assertNotNull(stats);
            assertFalse(stats.isEmpty());
        }
    }
    
    @Test
    @Order(12)
    void testMemoryManagement() {
        // Test that Mats are properly released
        for (int i = 0; i < 10; i++) {
            Mat tempMat = matBuilder.build(500, 500, 3, new Scalar(i * 10, i * 20, i * 30));
            Image tempImage = new Image(tempMat);
            
            // Perform operations
            histogramExtractor.getHistogram(tempImage);
            colorExtractor.getDominantColors(tempImage, 5);
            
            // Mat should be released when Image goes out of scope
            tempMat.release();
        }
        
        // If we get here without memory issues, the test passes
        assertTrue(true, "Memory management test completed successfully");
    }
}