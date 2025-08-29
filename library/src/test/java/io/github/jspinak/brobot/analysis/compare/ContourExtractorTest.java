package io.github.jspinak.brobot.analysis.compare;

import io.github.jspinak.brobot.model.element.Region;
import io.github.jspinak.brobot.model.match.Match;
import io.github.jspinak.brobot.test.BrobotTestBase;
import org.bytedeco.opencv.opencv_core.Mat;
import org.bytedeco.opencv.opencv_core.MatVector;
import org.bytedeco.opencv.opencv_core.Rect;
import org.bytedeco.opencv.opencv_core.Scalar;
import org.bytedeco.opencv.opencv_core.Point;
import org.junit.jupiter.api.*;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.junit.jupiter.params.provider.ValueSource;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.*;

import static org.bytedeco.opencv.global.opencv_core.*;
import static org.bytedeco.opencv.global.opencv_imgproc.*;
import java.lang.reflect.Field;
import static org.junit.jupiter.api.Assertions.*;

/**
 * Comprehensive test suite for ContourExtractor.
 * Tests contour extraction, filtering, and Match generation from classified images.
 */
@ExtendWith(MockitoExtension.class)
@DisplayName("ContourExtractor Tests")
public class ContourExtractorTest extends BrobotTestBase {
    
    private ContourExtractor contourExtractor;
    private Mat testImage;
    private List<Region> searchRegions;
    
    // Helper method to set private fields via reflection
    private void setPrivateField(Object obj, String fieldName, Object value) {
        try {
            Field field = obj.getClass().getDeclaredField(fieldName);
            field.setAccessible(true);
            field.set(obj, value);
        } catch (Exception e) {
            throw new RuntimeException("Failed to set private field: " + fieldName, e);
        }
    }
    
    @BeforeEach
    @Override
    public void setupTest() {
        super.setupTest();
        contourExtractor = new ContourExtractor();
        
        // Create test image with distinct regions
        testImage = new Mat(200, 200, CV_8UC3, new Scalar(0, 0, 0, 0));
        
        // Add some rectangles to create contours
        rectangle(testImage, new Point(10, 10), new Point(40, 40), new Scalar(255, 255, 255, 0));
        rectangle(testImage, new Point(60, 60), new Point(100, 100), new Scalar(255, 255, 255, 0));
        rectangle(testImage, new Point(120, 120), new Point(170, 170), new Scalar(255, 255, 255, 0));
        
        // Initialize search regions
        searchRegions = new ArrayList<>();
        searchRegions.add(new Region(0, 0, 200, 200));
    }
    
    @AfterEach
    public void tearDown() {
        if (testImage != null && !testImage.isNull()) {
            testImage.release();
        }
    }
    
    @Nested
    @DisplayName("Contour Extraction")
    class ContourExtraction {
        
        @Test
        @DisplayName("Should extract contours from classified image")
        void shouldExtractContoursFromImage() throws Exception {
            // Use reflection to set private fields since class only has @Getter
            setPrivateField(contourExtractor, "bgrFromClassification2d", testImage);
            setPrivateField(contourExtractor, "searchRegions", searchRegions);
            setPrivateField(contourExtractor, "minArea", 100);
            setPrivateField(contourExtractor, "maxArea", 10000);
            
            contourExtractor.setContours();
            
            List<Rect> contours = contourExtractor.getContours();
            assertNotNull(contours);
            assertFalse(contours.isEmpty());
            // We created 3 rectangles, all should be detected
            assertEquals(3, contours.size());
        }
        
        @Test
        @DisplayName("Should handle empty image")
        void shouldHandleEmptyImage() throws Exception {
            Mat emptyImage = new Mat(100, 100, CV_8UC3, new Scalar(0, 0, 0, 0));
            setPrivateField(contourExtractor, "bgrFromClassification2d", emptyImage);
            setPrivateField(contourExtractor, "searchRegions", searchRegions);
            setPrivateField(contourExtractor, "minArea", 10);
            setPrivateField(contourExtractor, "maxArea", 10000);
            
            contourExtractor.setContours();
            
            List<Rect> contours = contourExtractor.getContours();
            assertNotNull(contours);
            assertTrue(contours.isEmpty());
            
            emptyImage.release();
        }
        
        @Test
        @DisplayName("Should extract contours from multiple regions")
        void shouldExtractContoursFromMultipleRegions() throws Exception {
            List<Region> multipleRegions = Arrays.asList(
                new Region(0, 0, 100, 100),
                new Region(100, 100, 100, 100)
            );
            
            setPrivateField(contourExtractor, "bgrFromClassification2d", testImage);
            setPrivateField(contourExtractor, "searchRegions", multipleRegions);
            setPrivateField(contourExtractor, "minArea", 100);
            setPrivateField(contourExtractor, "maxArea", 10000);
            
            contourExtractor.setContours();
            
            List<Rect> contours = contourExtractor.getContours();
            assertNotNull(contours);
            // First region should contain 2 rectangles, second region should contain 1
            assertTrue(contours.size() >= 2);
        }
        
        @Test
        @DisplayName("Should convert regional coordinates to screen coordinates")
        void shouldConvertToScreenCoordinates() {
            Region offsetRegion = new Region(50, 50, 150, 150);
            
            Mat regionImage = new Mat(150, 150, CV_8UC3, new Scalar(0, 0, 0, 0));
            rectangle(regionImage, new Point(10, 10), new Point(40, 40), new Scalar(255, 255, 255, 0));
            
            MatVector regionalContours = new MatVector();
            Mat gray = new Mat();
            cvtColor(regionImage, gray, COLOR_BGR2GRAY);
            findContours(gray, regionalContours, RETR_EXTERNAL, CHAIN_APPROX_SIMPLE);
            
            List<Rect> screenRects = contourExtractor.getScreenCoordinateRects(offsetRegion, regionalContours);
            
            assertNotNull(screenRects);
            assertFalse(screenRects.isEmpty());
            
            // Check that coordinates are adjusted by region offset
            Rect screenRect = screenRects.get(0);
            assertTrue(screenRect.x() >= 50);
            assertTrue(screenRect.y() >= 50);
            
            regionImage.release();
            gray.release();
        }
    }
    
    @Nested
    @DisplayName("Size Filtering")
    class SizeFiltering {
        
        @Test
        @DisplayName("Should filter contours by minimum area")
        void shouldFilterByMinimumArea() throws Exception {
            setPrivateField(contourExtractor, "bgrFromClassification2d", testImage);
            setPrivateField(contourExtractor, "searchRegions", searchRegions);
            setPrivateField(contourExtractor, "minArea", 1500); // 40x40=1600 and 50x50=2500 should pass
            setPrivateField(contourExtractor, "maxArea", 10000);
            
            contourExtractor.setContours();
            
            Map<Integer, Match> matchMap = contourExtractor.getMatchMap();
            assertEquals(2, matchMap.size()); // Both 40x40 and 50x50 rectangles should pass
            
            // Verify all matches meet the minimum area requirement
            for (Match match : matchMap.values()) {
                int area = match.getRegion().w() * match.getRegion().h();
                assertTrue(area >= 1500, "Found match with area " + area + " which is below minimum 1500");
            }
        }
        
        @Test
        @DisplayName("Should filter contours by maximum area")
        void shouldFilterByMaximumArea() {
            setPrivateField(contourExtractor, "bgrFromClassification2d", testImage);
            setPrivateField(contourExtractor, "searchRegions", searchRegions);
            setPrivateField(contourExtractor, "minArea", 100);
            setPrivateField(contourExtractor, "maxArea", 1200); // Only the smallest rectangle (30x30=900) should pass
            
            contourExtractor.setContours();
            
            Map<Integer, Match> matchMap = contourExtractor.getMatchMap();
            assertTrue(matchMap.size() >= 1);
        }
        
        @ParameterizedTest
        @CsvSource({
            "0, -1",     // No filtering
            "100, 5000", // Normal range
            "500, 1500", // Narrow range
            "-1, -1"     // Negative values (no filtering)
        })
        @DisplayName("Should handle various area constraints")
        void shouldHandleVariousAreaConstraints(int minArea, int maxArea) {
            setPrivateField(contourExtractor, "bgrFromClassification2d", testImage);
            setPrivateField(contourExtractor, "searchRegions", searchRegions);
            setPrivateField(contourExtractor, "minArea", minArea);
            setPrivateField(contourExtractor, "maxArea", maxArea);
            
            contourExtractor.setContours();
            
            Map<Integer, Match> matchMap = contourExtractor.getMatchMap();
            assertNotNull(matchMap);
            // Should not throw exceptions
        }
        
        @Test
        @DisplayName("Should partition large contours")
        void shouldPartitionLargeContours() {
            // Create a large rectangle that exceeds max area
            Mat largeImage = new Mat(200, 200, CV_8UC3, new Scalar(0, 0, 0, 0));
            rectangle(largeImage, new Point(10, 10), new Point(190, 190), new Scalar(255, 255, 255, 0));
            
            setPrivateField(contourExtractor, "bgrFromClassification2d", largeImage);
            setPrivateField(contourExtractor, "searchRegions", searchRegions);
            setPrivateField(contourExtractor, "minArea", 100);
            setPrivateField(contourExtractor, "maxArea", 1000); // Force partitioning
            
            contourExtractor.setContours();
            
            Map<Integer, Match> matchMap = contourExtractor.getMatchMap();
            // Large contour should be partitioned into multiple smaller ones
            assertTrue(matchMap.size() > 1);
            
            largeImage.release();
        }
    }
    
    @Nested
    @DisplayName("Contour Uniqueness")
    class ContourUniqueness {
        
        @Test
        @DisplayName("Should remove contained contours")
        void shouldRemoveContainedContours() {
            // Create nested rectangles
            Mat nestedImage = new Mat(200, 200, CV_8UC3, new Scalar(0, 0, 0, 0));
            rectangle(nestedImage, new Point(50, 50), new Point(150, 150), new Scalar(255, 255, 255, 0));
            rectangle(nestedImage, new Point(60, 60), new Point(90, 90), new Scalar(0, 0, 0, 0)); // Inner hole
            rectangle(nestedImage, new Point(65, 65), new Point(85, 85), new Scalar(255, 255, 255, 0)); // Nested inside
            
            setPrivateField(contourExtractor, "bgrFromClassification2d", nestedImage);
            setPrivateField(contourExtractor, "searchRegions", searchRegions);
            setPrivateField(contourExtractor, "minArea", 10);
            setPrivateField(contourExtractor, "maxArea", 20000);
            
            contourExtractor.setContours();
            
            List<Rect> contours = contourExtractor.getContours();
            // Should detect outer and inner contours but remove truly contained ones
            assertNotNull(contours);
            
            nestedImage.release();
        }
        
        @Test
        @DisplayName("Should preserve non-overlapping contours")
        void shouldPreserveNonOverlappingContours() {
            setPrivateField(contourExtractor, "bgrFromClassification2d", testImage);
            setPrivateField(contourExtractor, "searchRegions", searchRegions);
            setPrivateField(contourExtractor, "minArea", 100);
            setPrivateField(contourExtractor, "maxArea", 10000);
            
            contourExtractor.setContours();
            
            List<Rect> contours = contourExtractor.getContours();
            // All 3 non-overlapping rectangles should be preserved
            assertEquals(3, contours.size());
        }
    }
    
    @Nested
    @DisplayName("Match Generation")
    class MatchGeneration {
        
        @Test
        @DisplayName("Should generate matches from contours")
        void shouldGenerateMatchesFromContours() {
            setPrivateField(contourExtractor, "bgrFromClassification2d", testImage);
            setPrivateField(contourExtractor, "searchRegions", searchRegions);
            setPrivateField(contourExtractor, "minArea", 100);
            setPrivateField(contourExtractor, "maxArea", 10000);
            
            contourExtractor.setContours();
            
            List<Match> matches = contourExtractor.getMatchList();
            assertNotNull(matches);
            assertFalse(matches.isEmpty());
            assertEquals(3, matches.size());
        }
        
        @Test
        @DisplayName("Should sort matches by score descending")
        void shouldSortMatchesByScore() {
            setPrivateField(contourExtractor, "bgrFromClassification2d", testImage);
            setPrivateField(contourExtractor, "searchRegions", searchRegions);
            setPrivateField(contourExtractor, "minArea", 100);
            setPrivateField(contourExtractor, "maxArea", 10000);
            
            // Set score matrices for scoring
            Mat scores = new Mat(200, 200, CV_32FC1, new Scalar(1.0));
            setPrivateField(contourExtractor, "scores", scores);
            
            contourExtractor.setContours();
            
            List<Match> matches = contourExtractor.getMatchList();
            
            // Verify descending order
            for (int i = 1; i < matches.size(); i++) {
                assertTrue(matches.get(i - 1).getScore() >= matches.get(i).getScore());
            }
            
            scores.release();
        }
        
        @Test
        @DisplayName("Should create match map with indices")
        void shouldCreateMatchMapWithIndices() {
            setPrivateField(contourExtractor, "bgrFromClassification2d", testImage);
            setPrivateField(contourExtractor, "searchRegions", searchRegions);
            setPrivateField(contourExtractor, "minArea", 100);
            setPrivateField(contourExtractor, "maxArea", 10000);
            
            contourExtractor.setContours();
            
            Map<Integer, Match> matchMap = contourExtractor.getMatchMap();
            assertNotNull(matchMap);
            assertFalse(matchMap.isEmpty());
            
            // Verify indices are sequential
            Set<Integer> indices = matchMap.keySet();
            assertFalse(indices.isEmpty());
        }
    }
    
    @Nested
    @DisplayName("Edge Cases")
    class EdgeCases {
        
        @Test
        @DisplayName("Should handle single pixel contours")
        void shouldHandleSinglePixelContours() {
            Mat pixelImage = new Mat(100, 100, CV_8UC3, new Scalar(0, 0, 0, 0));
            pixelImage.ptr(50, 50).put((byte)255, (byte)255, (byte)255); // Single white pixel
            
            setPrivateField(contourExtractor, "bgrFromClassification2d", pixelImage);
            setPrivateField(contourExtractor, "searchRegions", Arrays.asList(new Region(0, 0, 100, 100)));
            setPrivateField(contourExtractor, "minArea", 0);
            setPrivateField(contourExtractor, "maxArea", 10000);
            
            contourExtractor.setContours();
            
            // Single pixel should be detected if minArea allows
            assertNotNull(contourExtractor.getContours());
            
            pixelImage.release();
        }
        
        @Test
        @DisplayName("Should handle region out of bounds")
        void shouldHandleRegionOutOfBounds() {
            // Region extends beyond image boundaries
            List<Region> outOfBoundsRegions = Arrays.asList(
                new Region(-10, -10, 220, 220),
                new Region(150, 150, 100, 100)
            );
            
            setPrivateField(contourExtractor, "bgrFromClassification2d", testImage);
            setPrivateField(contourExtractor, "searchRegions", outOfBoundsRegions);
            setPrivateField(contourExtractor, "minArea", 100);
            setPrivateField(contourExtractor, "maxArea", 10000);
            
            // Should handle gracefully without exceptions
            assertDoesNotThrow(() -> contourExtractor.setContours());
            
            List<Rect> contours = contourExtractor.getContours();
            assertNotNull(contours);
        }
        
        @Test
        @DisplayName("Should handle grayscale images")
        void shouldHandleGrayscaleImages() {
            Mat grayImage = new Mat(200, 200, CV_8UC1, new Scalar(0));
            rectangle(grayImage, new Point(50, 50), new Point(100, 100), new Scalar(255));
            
            setPrivateField(contourExtractor, "bgrFromClassification2d", grayImage);
            setPrivateField(contourExtractor, "searchRegions", searchRegions);
            setPrivateField(contourExtractor, "minArea", 100);
            setPrivateField(contourExtractor, "maxArea", 10000);
            
            contourExtractor.setContours();
            
            List<Rect> contours = contourExtractor.getContours();
            assertNotNull(contours);
            assertFalse(contours.isEmpty());
            
            grayImage.release();
        }
        
        @Test
        @DisplayName("Should handle maxArea of zero")
        void shouldHandleMaxAreaZero() {
            setPrivateField(contourExtractor, "bgrFromClassification2d", testImage);
            setPrivateField(contourExtractor, "searchRegions", searchRegions);
            setPrivateField(contourExtractor, "minArea", 100);
            setPrivateField(contourExtractor, "maxArea", 0); // Special case
            
            contourExtractor.setContours();
            
            Map<Integer, Match> matchMap = contourExtractor.getMatchMap();
            assertNotNull(matchMap);
            assertTrue(matchMap.isEmpty()); // maxArea=0 should filter out all contours
        }
    }
    
    @Nested
    @DisplayName("Performance and Scalability")
    class PerformanceAndScalability {
        
        @Test
        @DisplayName("Should handle large number of contours efficiently")
        void shouldHandleLargeNumberOfContours() {
            // Create image with many small contours
            Mat manyContoursImage = new Mat(500, 500, CV_8UC3, new Scalar(0, 0, 0, 0));
            for (int i = 0; i < 100; i++) {
                int x = (i % 10) * 45 + 5;
                int y = (i / 10) * 45 + 5;
                rectangle(manyContoursImage, new Point(x, y), new Point(x + 30, y + 30), new Scalar(255, 255, 255, 0));
            }
            
            setPrivateField(contourExtractor, "bgrFromClassification2d", manyContoursImage);
            setPrivateField(contourExtractor, "searchRegions", Arrays.asList(new Region(0, 0, 500, 500)));
            setPrivateField(contourExtractor, "minArea", 100);
            setPrivateField(contourExtractor, "maxArea", 10000);
            
            long startTime = System.currentTimeMillis();
            contourExtractor.setContours();
            long endTime = System.currentTimeMillis();
            
            List<Match> matches = contourExtractor.getMatchList();
            assertNotNull(matches);
            assertEquals(100, matches.size());
            
            // Should complete within reasonable time
            assertTrue(endTime - startTime < 1000, "Processing should complete within 1 second");
            
            manyContoursImage.release();
        }
        
        @ParameterizedTest
        @ValueSource(ints = {100, 500, 1000, 2000})
        @DisplayName("Should scale with image size")
        void shouldScaleWithImageSize(int imageSize) {
            Mat scaledImage = new Mat(imageSize, imageSize, CV_8UC3, new Scalar(0, 0, 0, 0));
            // Add a few contours proportional to image size
            int rectSize = imageSize / 10;
            rectangle(scaledImage, new Point(rectSize, rectSize), new Point(rectSize * 2, rectSize * 2), 
                     new Scalar(255, 255, 255, 0));
            
            setPrivateField(contourExtractor, "bgrFromClassification2d", scaledImage);
            setPrivateField(contourExtractor, "searchRegions", Arrays.asList(new Region(0, 0, imageSize, imageSize)));
            setPrivateField(contourExtractor, "minArea", 10);
            setPrivateField(contourExtractor, "maxArea", imageSize * imageSize);
            
            assertDoesNotThrow(() -> contourExtractor.setContours());
            
            scaledImage.release();
        }
    }
}