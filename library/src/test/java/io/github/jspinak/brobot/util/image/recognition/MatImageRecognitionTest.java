package io.github.jspinak.brobot.util.image.recognition;

import io.github.jspinak.brobot.test.BrobotTestBase;
import io.github.jspinak.brobot.model.match.Match;
import org.bytedeco.opencv.opencv_core.Mat;
import org.bytedeco.opencv.opencv_core.Rect;
import org.bytedeco.opencv.opencv_core.Scalar;
import org.bytedeco.opencv.opencv_core.Point;
import org.junit.jupiter.api.*;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.junit.jupiter.params.provider.ValueSource;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;

import static org.bytedeco.opencv.global.opencv_core.*;
import static org.bytedeco.opencv.global.opencv_imgproc.*;
import static org.junit.jupiter.api.Assertions.*;

/**
 * Comprehensive test suite for MatImageRecognition.
 * Tests OpenCV-based template matching functionality.
 */
@ExtendWith(MockitoExtension.class)
@DisplayName("MatImageRecognition Tests")
public class MatImageRecognitionTest extends BrobotTestBase {
    
    private MatImageRecognition matImageRecognition;
    private Mat searchImage;
    private Mat template;
    
    @BeforeEach
    @Override
    public void setupTest() {
        super.setupTest();
        matImageRecognition = new MatImageRecognition();
        
        // Create a search image with a distinct pattern
        searchImage = new Mat(200, 200, CV_8UC3, new Scalar(0, 0, 0, 0));
        
        // Create template first - a 40x30 white rectangle
        template = new Mat(30, 40, CV_8UC3, new Scalar(0, 0, 0, 0));
        rectangle(template, new Point(0, 0), new Point(40, 30), new Scalar(255, 255, 255, 0));
        
        // Copy the template to the search image at position (50, 50)
        Mat roi = new Mat(searchImage, new Rect(50, 50, 40, 30));
        template.copyTo(roi);
    }
    
    @AfterEach
    public void tearDown() {
        releaseIfNotNull(searchImage);
        releaseIfNotNull(template);
    }
    
    private void releaseIfNotNull(Mat mat) {
        if (mat != null && !mat.isNull()) {
            mat.release();
        }
    }
    
    @Nested
    @DisplayName("Basic Template Matching")
    class BasicTemplateMatching {
        
        @Test
        @DisplayName("Should find exact template match")
        void shouldFindExactTemplateMatch() {
            Optional<Match> result = matImageRecognition.findTemplateMatch(template, searchImage, 0.9);
            
            assertTrue(result.isPresent());
            Match match = result.get();
            
            // Should find the template at position (50, 50)
            assertEquals(50, match.getRegion().x(), 2); // Allow small tolerance
            assertEquals(50, match.getRegion().y(), 2);
            assertEquals(40, match.getRegion().w());
            assertEquals(30, match.getRegion().h());
            assertTrue(match.getScore() > 0.99); // Should be near perfect match
        }
        
        @Test
        @DisplayName("Should not find match below threshold")
        void shouldNotFindMatchBelowThreshold() {
            // Create a significantly different template that won't match well
            Mat differentTemplate = new Mat(30, 40, CV_8UC3, new Scalar(0, 0, 0, 0));
            // Create a different pattern - horizontal stripes
            for (int y = 0; y < 30; y += 6) {
                rectangle(differentTemplate, new Point(0, y), new Point(40, y + 3), new Scalar(255, 255, 255, 0));
            }
            
            Optional<Match> result = matImageRecognition.findTemplateMatch(differentTemplate, searchImage, 0.99);
            
            assertFalse(result.isPresent());
            
            differentTemplate.release();
        }
        
        @Test
        @DisplayName("Should find partial match with lower threshold")
        void shouldFindPartialMatchWithLowerThreshold() {
            // Create a template with slight variation
            Mat partialTemplate = new Mat(30, 40, CV_8UC3, new Scalar(200, 200, 200, 0));
            
            Optional<Match> result = matImageRecognition.findTemplateMatch(partialTemplate, searchImage, 0.5);
            
            assertTrue(result.isPresent());
            Match match = result.get();
            assertTrue(match.getScore() >= 0.5);
            
            partialTemplate.release();
        }
    }
    
    @Nested
    @DisplayName("Threshold Variations")
    class ThresholdVariations {
        
        @ParameterizedTest
        @ValueSource(doubles = {0.0, 0.3, 0.5, 0.7, 0.9, 0.99})
        @DisplayName("Should respect different threshold values")
        void shouldRespectDifferentThresholds(double threshold) {
            Optional<Match> result = matImageRecognition.findTemplateMatch(template, searchImage, threshold);
            
            if (threshold <= 0.99) { // Our exact match should be > 0.99
                assertTrue(result.isPresent());
                assertTrue(result.get().getScore() >= threshold);
            }
        }
        
        @Test
        @DisplayName("Should handle threshold of 1.0")
        void shouldHandleThresholdOne() {
            // Perfect match threshold - rarely achieved in practice
            Optional<Match> result = matImageRecognition.findTemplateMatch(template, searchImage, 1.0);
            
            // Even exact matches might not reach 1.0 due to floating point precision
            // This test verifies no exceptions are thrown
            assertNotNull(result);
        }
        
        @Test
        @DisplayName("Should handle negative threshold")
        void shouldHandleNegativeThreshold() {
            // Negative threshold should always find a match
            Optional<Match> result = matImageRecognition.findTemplateMatch(template, searchImage, -1.0);
            
            assertTrue(result.isPresent());
        }
    }
    
    @Nested
    @DisplayName("Template Size Validation")
    class TemplateSizeValidation {
        
        @Test
        @DisplayName("Should reject template larger than search image")
        void shouldRejectTemplateLargerThanSearch() {
            Mat largeTemplate = new Mat(300, 300, CV_8UC3, new Scalar(255, 255, 255, 0));
            
            Optional<Match> result = matImageRecognition.findTemplateMatch(largeTemplate, searchImage, 0.7);
            
            assertFalse(result.isPresent());
            
            largeTemplate.release();
        }
        
        @Test
        @DisplayName("Should handle template same size as search image")
        void shouldHandleTemplateSameSizeAsSearch() {
            // Create a template that's the same size as search image
            Mat sameSizeTemplate = searchImage.clone();
            
            Optional<Match> result = matImageRecognition.findTemplateMatch(sameSizeTemplate, searchImage, 0.99);
            
            assertTrue(result.isPresent());
            assertEquals(0, result.get().getRegion().x());
            assertEquals(0, result.get().getRegion().y());
            // Should be a perfect match
            assertTrue(result.get().getScore() > 0.99);
            
            sameSizeTemplate.release();
        }
        
        @Test
        @DisplayName("Should handle single pixel template")
        void shouldHandleSinglePixelTemplate() {
            Mat pixelTemplate = new Mat(1, 1, CV_8UC3, new Scalar(255, 255, 255, 0));
            
            Optional<Match> result = matImageRecognition.findTemplateMatch(pixelTemplate, searchImage, 0.8);
            
            assertTrue(result.isPresent());
            // Should find white pixel within the white rectangle (50,50) to (90,80)
            Match match = result.get();
            // The template matching will find the best match location
            assertNotNull(match);
            assertTrue(match.getScore() >= 0.8);
            
            pixelTemplate.release();
        }
    }
    
    @Nested
    @DisplayName("Error Handling")
    class ErrorHandling {
        
        @Test
        @DisplayName("Should throw exception for empty template")
        void shouldThrowExceptionForEmptyTemplate() {
            Mat emptyTemplate = new Mat();
            
            assertThrows(RuntimeException.class, 
                () -> matImageRecognition.findTemplateMatch(emptyTemplate, searchImage, 0.7));
        }
        
        @Test
        @DisplayName("Should throw exception for empty search image")
        void shouldThrowExceptionForEmptySearchImage() {
            Mat emptySearch = new Mat();
            
            assertThrows(RuntimeException.class,
                () -> matImageRecognition.findTemplateMatch(template, emptySearch, 0.7));
        }
        
        @Test
        @DisplayName("Should throw exception for both empty")
        void shouldThrowExceptionForBothEmpty() {
            Mat emptyTemplate = new Mat();
            Mat emptySearch = new Mat();
            
            assertThrows(RuntimeException.class,
                () -> matImageRecognition.findTemplateMatch(emptyTemplate, emptySearch, 0.7));
        }
    }
    
    @Nested
    @DisplayName("Different Image Types")
    class DifferentImageTypes {
        
        @Test
        @DisplayName("Should match grayscale images")
        void shouldMatchGrayscaleImages() {
            Mat graySearch = new Mat(200, 200, CV_8UC1, new Scalar(0));
            Mat grayTemplate = new Mat(30, 40, CV_8UC1, new Scalar(0));
            
            // Create a distinctive pattern in the template
            // Fill with gradient pattern to make it unique
            for (int y = 0; y < 30; y++) {
                for (int x = 0; x < 40; x++) {
                    byte val = (byte) ((x + y) * 3);
                    grayTemplate.ptr(y, x).put(val);
                }
            }
            
            // Copy the template to the search image at position (70, 70)
            Mat roi = new Mat(graySearch, new Rect(70, 70, 40, 30));
            grayTemplate.copyTo(roi);
            
            Optional<Match> result = matImageRecognition.findTemplateMatch(grayTemplate, graySearch, 0.9);
            
            assertTrue(result.isPresent());
            Match match = result.get();
            assertEquals(70, match.getRegion().x(), 2);
            assertEquals(70, match.getRegion().y(), 2);
            
            graySearch.release();
            grayTemplate.release();
        }
        
        @Test
        @DisplayName("Should match multi-channel images")
        void shouldMatchMultiChannelImages() {
            // Test with 4-channel images (BGRA)
            Mat bgraSearch = new Mat(200, 200, CV_8UC4, new Scalar(0, 0, 0, 255));
            Mat bgraTemplate = new Mat(40, 60, CV_8UC4, new Scalar(0, 0, 0, 255));
            
            // Create a distinctive pattern in the template
            // Fill with gradient pattern to make it unique
            for (int y = 0; y < 40; y++) {
                for (int x = 0; x < 60; x++) {
                    byte b = (byte) (x * 4);
                    byte g = (byte) (y * 6);
                    byte r = (byte) ((x + y) * 3);
                    bgraTemplate.ptr(y, x).put(b, g, r, (byte) 255);
                }
            }
            
            // Copy the template to the search image at position (30, 30)
            Mat roi = new Mat(bgraSearch, new Rect(30, 30, 60, 40));
            bgraTemplate.copyTo(roi);
            
            Optional<Match> result = matImageRecognition.findTemplateMatch(bgraTemplate, bgraSearch, 0.9);
            
            assertTrue(result.isPresent());
            Match match = result.get();
            assertEquals(30, match.getRegion().x(), 2);
            assertEquals(30, match.getRegion().y(), 2);
            
            bgraSearch.release();
            bgraTemplate.release();
        }
    }
    
    @Nested
    @DisplayName("Match Position Accuracy")
    class MatchPositionAccuracy {
        
        @ParameterizedTest
        @CsvSource({
            "10, 10",
            "0, 0",
            "160, 170",
            "100, 50",
            "75, 125"
        })
        @DisplayName("Should find template at various positions")
        void shouldFindTemplateAtVariousPositions(int x, int y) {
            // Create search image with template at specific position
            Mat positionedSearch = new Mat(200, 200, CV_8UC3, new Scalar(0, 0, 0, 0));
            
            // Ensure we don't go out of bounds
            int actualX = Math.min(x, 200 - 40);
            int actualY = Math.min(y, 200 - 30);
            
            // Copy the template to the specific position
            Mat roi = new Mat(positionedSearch, new Rect(actualX, actualY, 40, 30));
            template.copyTo(roi);
            
            Optional<Match> result = matImageRecognition.findTemplateMatch(template, positionedSearch, 0.9);
            
            assertTrue(result.isPresent());
            assertEquals(actualX, result.get().getRegion().x(), 2);
            assertEquals(actualY, result.get().getRegion().y(), 2);
            
            positionedSearch.release();
        }
        
        @Test
        @DisplayName("Should find best match among multiple candidates")
        void shouldFindBestMatchAmongMultiple() {
            // Create search image with multiple similar patterns
            Mat multiSearch = new Mat(300, 300, CV_8UC3, new Scalar(0, 0, 0, 0));
            
            // Add exact match
            rectangle(multiSearch, new Point(50, 50), new Point(90, 80), new Scalar(255, 255, 255, 0));
            
            // Add partial matches
            rectangle(multiSearch, new Point(150, 50), new Point(190, 80), new Scalar(200, 200, 200, 0));
            rectangle(multiSearch, new Point(50, 150), new Point(90, 180), new Scalar(180, 180, 180, 0));
            
            Optional<Match> result = matImageRecognition.findTemplateMatch(template, multiSearch, 0.5);
            
            assertTrue(result.isPresent());
            // Should find the exact match at (50, 50)
            assertEquals(50, result.get().getRegion().x(), 2);
            assertEquals(50, result.get().getRegion().y(), 2);
            assertTrue(result.get().getScore() > 0.99);
            
            multiSearch.release();
        }
    }
    
    @Nested
    @DisplayName("Performance and Scalability")
    class PerformanceAndScalability {
        
        @ParameterizedTest
        @ValueSource(ints = {100, 500, 1000})
        @DisplayName("Should handle various image sizes efficiently")
        void shouldHandleVariousImageSizes(int size) {
            Mat largeSearch = new Mat(size, size, CV_8UC3, new Scalar(0, 0, 0, 0));
            Mat smallTemplate = new Mat(20, 20, CV_8UC3, new Scalar(255, 255, 255, 0));
            
            // Add template pattern to search image
            rectangle(largeSearch, new Point(size/2, size/2), new Point(size/2 + 20, size/2 + 20), new Scalar(255, 255, 255, 0));
            
            long startTime = System.currentTimeMillis();
            Optional<Match> result = matImageRecognition.findTemplateMatch(smallTemplate, largeSearch, 0.9);
            long endTime = System.currentTimeMillis();
            
            assertTrue(result.isPresent());
            // Should complete in reasonable time even for large images
            assertTrue(endTime - startTime < 1000, 
                "Template matching should complete within 1 second for " + size + "x" + size + " image");
            
            largeSearch.release();
            smallTemplate.release();
        }
        
        @Test
        @DisplayName("Should handle complex patterns efficiently")
        void shouldHandleComplexPatternsEfficiently() {
            // Create complex pattern with gradients
            Mat complexSearch = new Mat(400, 400, CV_8UC3, new Scalar(0, 0, 0, 0));
            Mat complexTemplate = new Mat(50, 50, CV_8UC3, new Scalar(0, 0, 0, 0));
            
            // Fill template with a unique pattern
            for (int i = 0; i < 50; i++) {
                for (int j = 0; j < 50; j++) {
                    byte val = (byte) ((i + j) % 256);
                    complexTemplate.ptr(i, j).put(val, val, val);
                }
            }
            
            // Copy template to search image at position (100, 100)
            Mat roi = new Mat(complexSearch, new Rect(100, 100, 50, 50));
            complexTemplate.copyTo(roi);
            
            long startTime = System.currentTimeMillis();
            Optional<Match> result = matImageRecognition.findTemplateMatch(complexTemplate, complexSearch, 0.9);
            long endTime = System.currentTimeMillis();
            
            assertTrue(result.isPresent());
            assertEquals(100, result.get().getRegion().x(), 2);
            assertEquals(100, result.get().getRegion().y(), 2);
            assertTrue(endTime - startTime < 500, "Complex pattern matching should be efficient");
            
            complexSearch.release();
            complexTemplate.release();
        }
    }
}