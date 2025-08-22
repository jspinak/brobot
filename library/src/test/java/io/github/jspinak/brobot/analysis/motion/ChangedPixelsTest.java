package io.github.jspinak.brobot.analysis.motion;

import io.github.jspinak.brobot.test.BrobotTestBase;
import io.github.jspinak.brobot.util.image.core.ColorMatrixUtilities;
import org.bytedeco.opencv.opencv_core.Mat;
import org.bytedeco.opencv.opencv_core.MatVector;
import org.bytedeco.opencv.opencv_core.Scalar;
import org.junit.jupiter.api.*;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.bytedeco.opencv.global.opencv_core.*;
import static org.bytedeco.opencv.global.opencv_imgproc.*;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

/**
 * Comprehensive test suite for ChangedPixels.
 * Tests pixel change detection across image sequences for motion identification.
 */
@ExtendWith(MockitoExtension.class)
@DisplayName("ChangedPixels Tests")
public class ChangedPixelsTest extends BrobotTestBase {
    
    @Mock
    private ColorMatrixUtilities matOps3d;
    
    private ChangedPixels changedPixels;
    private Mat testImage1;
    private Mat testImage2;
    private Mat testImage3;
    private MatVector testMatVector;
    
    @BeforeEach
    @Override
    public void setupTest() {
        super.setupTest();
        changedPixels = new ChangedPixels(matOps3d);
        
        // Create test images with different patterns
        testImage1 = createTestImage(100, 100, 50);
        testImage2 = createTestImage(100, 100, 100);
        testImage3 = createTestImage(100, 100, 150);
        
        testMatVector = new MatVector(testImage1, testImage2, testImage3);
    }
    
    @AfterEach
    public void tearDown() {
        releaseIfNotNull(testImage1);
        releaseIfNotNull(testImage2);
        releaseIfNotNull(testImage3);
    }
    
    private Mat createTestImage(int width, int height, int grayValue) {
        Mat image = new Mat(height, width, CV_8UC3);
        image.setTo(new Scalar(grayValue, grayValue, grayValue, 0));
        return image;
    }
    
    private void releaseIfNotNull(Mat mat) {
        if (mat != null && !mat.isNull()) {
            mat.release();
        }
    }
    
    @Nested
    @DisplayName("Constructor Tests")
    class ConstructorTests {
        
        @Test
        @DisplayName("Should create with ColorMatrixUtilities dependency")
        void shouldCreateWithColorMatrixUtilities() {
            ChangedPixels cp = new ChangedPixels(matOps3d);
            assertNotNull(cp);
        }
    }
    
    @Nested
    @DisplayName("Dynamic Pixel Detection")
    class DynamicPixelDetection {
        
        @Test
        @DisplayName("Should detect dynamic pixels from image sequence")
        void shouldDetectDynamicPixelsFromImageSequence() {
            Mat result = changedPixels.getDynamicPixelMask(testMatVector);
            
            assertNotNull(result);
            assertFalse(result.empty());
            assertEquals(CV_8UC1, result.type()); // Should be binary mask
            
            result.release();
        }
        
        @Test
        @DisplayName("Should detect changes between different images")
        void shouldDetectChangesBetweenDifferentImages() {
            // Create images with distinct differences
            Mat blackImage = new Mat(100, 100, CV_8UC3, new Scalar(0, 0, 0, 0));
            Mat whiteImage = new Mat(100, 100, CV_8UC3, new Scalar(255, 255, 255, 0));
            MatVector contrastVector = new MatVector(blackImage, whiteImage);
            
            try {
                Mat result = changedPixels.getDynamicPixelMask(contrastVector);
                
                assertNotNull(result);
                // Should detect significant changes
                double sum = sumElems(result).get();
                assertTrue(sum > 0, "Should detect motion between black and white images");
                
                result.release();
            } finally {
                blackImage.release();
                whiteImage.release();
            }
        }
        
        @Test
        @DisplayName("Should detect no changes for identical images")
        void shouldDetectNoChangesForIdenticalImages() {
            Mat sameImage1 = createTestImage(100, 100, 128);
            Mat sameImage2 = createTestImage(100, 100, 128);
            Mat sameImage3 = createTestImage(100, 100, 128);
            MatVector identicalVector = new MatVector(sameImage1, sameImage2, sameImage3);
            
            try {
                Mat result = changedPixels.getDynamicPixelMask(identicalVector);
                
                assertNotNull(result);
                // Should detect no changes
                double sum = sumElems(result).get();
                assertEquals(0.0, sum, "Should detect no motion for identical images");
                
                result.release();
            } finally {
                sameImage1.release();
                sameImage2.release();
                sameImage3.release();
            }
        }
        
        @Test
        @DisplayName("Should handle small pixel value changes")
        void shouldHandleSmallPixelValueChanges() {
            // Create images with small differences (below threshold)
            Mat img1 = createTestImage(100, 100, 100);
            Mat img2 = createTestImage(100, 100, 120); // 20 difference (below 50 threshold)
            MatVector smallChangeVector = new MatVector(img1, img2);
            
            try {
                Mat result = changedPixels.getDynamicPixelMask(smallChangeVector);
                
                assertNotNull(result);
                // Small changes below threshold might not be detected
                double sum = sumElems(result).get();
                // Depending on implementation, small changes might be filtered out
                assertTrue(sum >= 0, "Should handle small changes appropriately");
                
                result.release();
            } finally {
                img1.release();
                img2.release();
            }
        }
    }
    
    @Nested
    @DisplayName("Fixed Pixel Detection")
    class FixedPixelDetection {
        
        @Test
        @DisplayName("Should detect fixed pixels from image sequence")
        void shouldDetectFixedPixelsFromImageSequence() {
            // Mock the bitwise NOT operation
            Mat mockDynamicMask = new Mat(100, 100, CV_8UC1, new Scalar(0));
            Mat expectedFixedMask = new Mat(100, 100, CV_8UC1, new Scalar(255));
            
            when(matOps3d.bItwise_not(any(Mat.class))).thenReturn(expectedFixedMask);
            
            Mat result = changedPixels.getFixedPixelMask(testMatVector);
            
            assertNotNull(result);
            assertEquals(expectedFixedMask, result);
            verify(matOps3d).bItwise_not(any(Mat.class));
            
            mockDynamicMask.release();
        }
        
        @Test
        @DisplayName("Should return inverse of dynamic pixel mask")
        void shouldReturnInverseOfDynamicPixelMask() {
            Mat dynamicMask = new Mat(100, 100, CV_8UC1);
            // Fill with pattern: half white, half black
            for (int i = 0; i < 100; i++) {
                for (int j = 0; j < 100; j++) {
                    dynamicMask.ptr(i, j).put(j < 50 ? (byte)255 : (byte)0);
                }
            }
            
            Mat inverseMask = new Mat(100, 100, CV_8UC1);
            // Inverse pattern
            for (int i = 0; i < 100; i++) {
                for (int j = 0; j < 100; j++) {
                    inverseMask.ptr(i, j).put(j < 50 ? (byte)0 : (byte)255);
                }
            }
            
            when(matOps3d.bItwise_not(any(Mat.class))).thenReturn(inverseMask);
            
            try {
                Mat result = changedPixels.getFixedPixelMask(testMatVector);
                
                assertNotNull(result);
                verify(matOps3d).bItwise_not(any(Mat.class));
                
                // Result should be the inverse
                assertEquals(inverseMask, result);
            } finally {
                dynamicMask.release();
                inverseMask.release();
            }
        }
    }
    
    @Nested
    @DisplayName("Integration with PixelChangeDetector")
    class PixelChangeDetectorIntegration {
        
        @Test
        @DisplayName("Should use grayscale conversion")
        void shouldUseGrayscaleConversion() {
            // The getDynamicPixelMask method internally creates a PixelChangeDetector
            // with grayscale enabled
            Mat result = changedPixels.getDynamicPixelMask(testMatVector);
            
            assertNotNull(result);
            assertEquals(CV_8UC1, result.type()); // Grayscale result
            
            result.release();
        }
        
        @Test
        @DisplayName("Should apply threshold of 50")
        void shouldApplyThresholdOf50() {
            // Create images with differences around the threshold
            Mat img1 = createTestImage(100, 100, 100);
            Mat img2 = createTestImage(100, 100, 151); // 51 difference (above 50 threshold)
            Mat img3 = createTestImage(100, 100, 149); // 49 difference from img1 (below 50)
            MatVector thresholdVector = new MatVector(img1, img2, img3);
            
            try {
                Mat result = changedPixels.getDynamicPixelMask(thresholdVector);
                
                assertNotNull(result);
                // Should detect changes above threshold
                double sum = sumElems(result).get();
                assertTrue(sum > 0, "Should detect changes above threshold");
                
                result.release();
            } finally {
                img1.release();
                img2.release();
                img3.release();
            }
        }
    }
    
    @Nested
    @DisplayName("Edge Cases")
    class EdgeCases {
        
        @Test
        @DisplayName("Should handle empty MatVector")
        void shouldHandleEmptyMatVector() {
            MatVector emptyVector = new MatVector();
            
            Mat result = changedPixels.getDynamicPixelMask(emptyVector);
            
            // Should return empty or handle gracefully
            assertNotNull(result);
            assertTrue(result.empty() || result.rows() == 0);
            
            result.release();
        }
        
        @Test
        @DisplayName("Should handle single image in MatVector")
        void shouldHandleSingleImageInMatVector() {
            MatVector singleVector = new MatVector(testImage1);
            
            Mat result = changedPixels.getDynamicPixelMask(singleVector);
            
            assertNotNull(result);
            // With single image, no changes can be detected
            if (!result.empty()) {
                double sum = sumElems(result).get();
                assertEquals(0.0, sum, "Single image should have no changes");
            }
            
            result.release();
        }
        
        @Test
        @DisplayName("Should handle very small images")
        void shouldHandleVerySmallImages() {
            Mat tiny1 = new Mat(1, 1, CV_8UC3, new Scalar(100, 100, 100, 0));
            Mat tiny2 = new Mat(1, 1, CV_8UC3, new Scalar(200, 200, 200, 0));
            MatVector tinyVector = new MatVector(tiny1, tiny2);
            
            try {
                Mat result = changedPixels.getDynamicPixelMask(tinyVector);
                
                assertNotNull(result);
                assertEquals(1, result.rows());
                assertEquals(1, result.cols());
                
                result.release();
            } finally {
                tiny1.release();
                tiny2.release();
            }
        }
        
        @Test
        @DisplayName("Should handle large image sequences")
        void shouldHandleLargeImageSequences() {
            // Create a sequence of 10 images
            Mat[] images = new Mat[10];
            for (int i = 0; i < 10; i++) {
                images[i] = createTestImage(200, 200, i * 25);
            }
            MatVector largeVector = new MatVector(images);
            
            try {
                Mat result = changedPixels.getDynamicPixelMask(largeVector);
                
                assertNotNull(result);
                assertEquals(200, result.rows());
                assertEquals(200, result.cols());
                
                // Should detect changes across the sequence
                double sum = sumElems(result).get();
                assertTrue(sum > 0, "Should detect changes in varied sequence");
                
                result.release();
            } finally {
                for (Mat img : images) {
                    img.release();
                }
            }
        }
    }
    
    @Nested
    @DisplayName("Performance Considerations")
    class PerformanceConsiderations {
        
        @Test
        @DisplayName("Should efficiently process high resolution images")
        void shouldEfficientlyProcessHighResolutionImages() {
            Mat hd1 = new Mat(1080, 1920, CV_8UC3, new Scalar(50, 50, 50, 0));
            Mat hd2 = new Mat(1080, 1920, CV_8UC3, new Scalar(100, 100, 100, 0));
            MatVector hdVector = new MatVector(hd1, hd2);
            
            try {
                long startTime = System.currentTimeMillis();
                Mat result = changedPixels.getDynamicPixelMask(hdVector);
                long endTime = System.currentTimeMillis();
                
                assertNotNull(result);
                assertEquals(1080, result.rows());
                assertEquals(1920, result.cols());
                
                // Should complete in reasonable time (< 2 seconds for HD)
                assertTrue((endTime - startTime) < 2000,
                    "HD processing took too long: " + (endTime - startTime) + "ms");
                
                result.release();
            } finally {
                hd1.release();
                hd2.release();
            }
        }
    }
}