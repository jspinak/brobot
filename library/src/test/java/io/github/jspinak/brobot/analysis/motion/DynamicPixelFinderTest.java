package io.github.jspinak.brobot.analysis.motion;

import io.github.jspinak.brobot.model.element.Region;
import io.github.jspinak.brobot.test.BrobotTestBase;
import io.github.jspinak.brobot.util.image.core.ColorMatrixUtilities;
import io.github.jspinak.brobot.util.image.recognition.ImageLoader;
import org.bytedeco.opencv.opencv_core.Mat;
import org.bytedeco.opencv.opencv_core.MatVector;
import org.bytedeco.opencv.opencv_core.Scalar;
import org.junit.jupiter.api.*;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.bytedeco.opencv.global.opencv_core.*;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

/**
 * Comprehensive test suite for DynamicPixelFinder.
 * Tests dynamic and fixed pixel detection through direct pixel comparison.
 */
@ExtendWith(MockitoExtension.class)
@DisplayName("DynamicPixelFinder Tests")
public class DynamicPixelFinderTest extends BrobotTestBase {
    
    @Mock
    private ColorMatrixUtilities matOps3d;
    
    @Mock
    private ImageLoader imageLoader;
    
    private DynamicPixelFinder dynamicPixelFinder;
    private Mat testImage1;
    private Mat testImage2;
    private Mat testImage3;
    private MatVector testMatVector;
    
    @BeforeEach
    @Override
    public void setupTest() {
        super.setupTest();
        dynamicPixelFinder = new DynamicPixelFinder(matOps3d, imageLoader);
        
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
        @DisplayName("Should create with required dependencies")
        void shouldCreateWithRequiredDependencies() {
            DynamicPixelFinder finder = new DynamicPixelFinder(matOps3d, imageLoader);
            assertNotNull(finder);
        }
    }
    
    @Nested
    @DisplayName("Dynamic Pixel Detection from MatVector")
    class DynamicPixelDetectionFromMatVector {
        
        @Test
        @DisplayName("Should detect dynamic pixels from multiple images")
        void shouldDetectDynamicPixelsFromMultipleImages() {
            // Setup mocks
            Mat comparisonMask1 = new Mat(100, 100, CV_8UC1);
            Mat comparisonMask2 = new Mat(100, 100, CV_8UC1);
            Mat combinedMask = new Mat(100, 100, CV_8UC1);
            
            // Fill masks with test data
            comparisonMask1.setTo(new Scalar(255)); // All pixels different
            comparisonMask2.setTo(new Scalar(128)); // Some pixels different
            combinedMask.setTo(new Scalar(255)); // Combined result
            
            when(matOps3d.cOmpare(eq(testImage1), eq(testImage2), eq(CMP_NE)))
                .thenReturn(comparisonMask1);
            when(matOps3d.cOmpare(eq(testImage1), eq(testImage3), eq(CMP_NE)))
                .thenReturn(comparisonMask2);
            when(matOps3d.bItwise_or(any(Mat.class), any(Mat.class)))
                .thenReturn(combinedMask);
            
            Mat result = dynamicPixelFinder.getDynamicPixelMask(testMatVector);
            
            assertNotNull(result);
            assertEquals(combinedMask, result);
            
            // Verify comparisons were made
            verify(matOps3d, times(2)).cOmpare(any(Mat.class), any(Mat.class), eq(CMP_NE));
            verify(matOps3d, atLeastOnce()).bItwise_or(any(Mat.class), any(Mat.class));
            
            comparisonMask1.release();
            comparisonMask2.release();
        }
        
        @Test
        @DisplayName("Should return empty Mat for single image")
        void shouldReturnEmptyMatForSingleImage() {
            MatVector singleImage = new MatVector(testImage1);
            
            Mat result = dynamicPixelFinder.getDynamicPixelMask(singleImage);
            
            assertNotNull(result);
            assertTrue(result.empty());
            
            // No comparisons should be made
            verify(matOps3d, never()).cOmpare(any(Mat.class), any(Mat.class), anyInt());
        }
        
        @Test
        @DisplayName("Should compare all images to first image")
        void shouldCompareAllImagesToFirstImage() {
            Mat mask1 = new Mat(100, 100, CV_8UC1, new Scalar(0));
            Mat mask2 = new Mat(100, 100, CV_8UC1, new Scalar(0));
            Mat combined = new Mat(100, 100, CV_8UC1, new Scalar(0));
            
            when(matOps3d.cOmpare(eq(testImage1), any(Mat.class), eq(CMP_NE)))
                .thenReturn(mask1, mask2);
            when(matOps3d.bItwise_or(any(Mat.class), any(Mat.class)))
                .thenReturn(combined);
            
            Mat result = dynamicPixelFinder.getDynamicPixelMask(testMatVector);
            
            assertNotNull(result);
            
            // Verify first image is used as reference
            verify(matOps3d).cOmpare(testImage1, testImage2, CMP_NE);
            verify(matOps3d).cOmpare(testImage1, testImage3, CMP_NE);
            
            mask1.release();
            mask2.release();
            combined.release();
        }
        
        @Test
        @DisplayName("Should combine masks with bitwise OR")
        void shouldCombineMasksWithBitwiseOr() {
            Mat mask1 = createMaskWithPattern(100, 100, true, false); // Top half white
            Mat mask2 = createMaskWithPattern(100, 100, false, true); // Bottom half white
            Mat expectedCombined = new Mat(100, 100, CV_8UC1, new Scalar(255)); // All white
            
            when(matOps3d.cOmpare(any(Mat.class), any(Mat.class), eq(CMP_NE)))
                .thenReturn(mask1, mask2);
            when(matOps3d.bItwise_or(eq(mask1), any(Mat.class)))
                .thenReturn(mask1);
            when(matOps3d.bItwise_or(eq(mask2), eq(mask1)))
                .thenReturn(expectedCombined);
            
            Mat result = dynamicPixelFinder.getDynamicPixelMask(testMatVector);
            
            assertNotNull(result);
            verify(matOps3d, atLeastOnce()).bItwise_or(any(Mat.class), any(Mat.class));
            
            mask1.release();
            mask2.release();
            expectedCombined.release();
        }
        
        private Mat createMaskWithPattern(int width, int height, boolean topHalf, boolean bottomHalf) {
            Mat mask = new Mat(height, width, CV_8UC1);
            for (int i = 0; i < height; i++) {
                for (int j = 0; j < width; j++) {
                    boolean inTopHalf = i < height / 2;
                    byte value = ((inTopHalf && topHalf) || (!inTopHalf && bottomHalf)) 
                        ? (byte)255 : (byte)0;
                    mask.ptr(i, j).put(value);
                }
            }
            return mask;
        }
    }
    
    @Nested
    @DisplayName("Fixed Pixel Detection from MatVector")
    class FixedPixelDetectionFromMatVector {
        
        @Test
        @DisplayName("Should detect fixed pixels as inverse of dynamic pixels")
        void shouldDetectFixedPixelsAsInverseOfDynamic() {
            Mat dynamicMask = new Mat(100, 100, CV_8UC1, new Scalar(255));
            Mat fixedMask = new Mat(100, 100, CV_8UC1, new Scalar(0));
            
            when(matOps3d.cOmpare(any(Mat.class), any(Mat.class), eq(CMP_NE)))
                .thenReturn(dynamicMask);
            when(matOps3d.bItwise_or(any(Mat.class), any(Mat.class)))
                .thenReturn(dynamicMask);
            when(matOps3d.bItwise_not(eq(dynamicMask)))
                .thenReturn(fixedMask);
            
            Mat result = dynamicPixelFinder.getFixedPixelMask(testMatVector);
            
            assertNotNull(result);
            assertEquals(fixedMask, result);
            verify(matOps3d).bItwise_not(any(Mat.class));
            
            dynamicMask.release();
            fixedMask.release();
        }
        
        @Test
        @DisplayName("Should handle single image for fixed pixels")
        void shouldHandleSingleImageForFixedPixels() {
            MatVector singleImage = new MatVector(testImage1);
            Mat emptyMask = new Mat();
            Mat fullMask = new Mat(100, 100, CV_8UC1, new Scalar(255));
            
            when(matOps3d.bItwise_not(any(Mat.class)))
                .thenReturn(fullMask);
            
            Mat result = dynamicPixelFinder.getFixedPixelMask(singleImage);
            
            assertNotNull(result);
            // Single image should have all pixels as fixed
            verify(matOps3d).bItwise_not(any(Mat.class));
            
            fullMask.release();
        }
    }
    
    @Nested
    @DisplayName("Time-based Capture and Detection")
    class TimeBasedCaptureAndDetection {
        
        @Test
        @DisplayName("Should capture and detect dynamic pixels over time")
        void shouldCaptureAndDetectDynamicPixelsOverTime() {
            Region testRegion = new Region(0, 0, 100, 100);
            double intervalSeconds = 0.5;
            double totalSeconds = 2.0;
            
            Mat capturedImage1 = createTestImage(100, 100, 50);
            Mat capturedImage2 = createTestImage(100, 100, 100);
            Mat capturedImage3 = createTestImage(100, 100, 150);
            Mat capturedImage4 = createTestImage(100, 100, 200);
            
            MatVector capturedImages = new MatVector(
                capturedImage1, capturedImage2, capturedImage3, capturedImage4);
            
            Mat expectedMask = new Mat(100, 100, CV_8UC1, new Scalar(255));
            
            when(imageLoader.getMatsFromScreen(eq(testRegion), eq(intervalSeconds), eq(totalSeconds)))
                .thenReturn(capturedImages);
            when(matOps3d.cOmpare(any(Mat.class), any(Mat.class), eq(CMP_NE)))
                .thenReturn(expectedMask);
            when(matOps3d.bItwise_or(any(Mat.class), any(Mat.class)))
                .thenReturn(expectedMask);
            
            Mat result = dynamicPixelFinder.getDynamicPixelMask(testRegion, intervalSeconds, totalSeconds);
            
            assertNotNull(result);
            verify(imageLoader).getMatsFromScreen(testRegion, intervalSeconds, totalSeconds);
            verify(matOps3d, atLeastOnce()).cOmpare(any(Mat.class), any(Mat.class), eq(CMP_NE));
            
            capturedImage1.release();
            capturedImage2.release();
            capturedImage3.release();
            capturedImage4.release();
            expectedMask.release();
        }
        
        @Test
        @DisplayName("Should capture and detect fixed pixels over time")
        void shouldCaptureAndDetectFixedPixelsOverTime() {
            Region testRegion = new Region(100, 100, 200, 200);
            double intervalSeconds = 1.0;
            double totalSeconds = 5.0;
            
            MatVector capturedImages = new MatVector(testImage1, testImage2);
            Mat dynamicMask = new Mat(200, 200, CV_8UC1, new Scalar(128));
            Mat fixedMask = new Mat(200, 200, CV_8UC1, new Scalar(127));
            
            when(imageLoader.getMatsFromScreen(eq(testRegion), eq(intervalSeconds), eq(totalSeconds)))
                .thenReturn(capturedImages);
            when(matOps3d.cOmpare(any(Mat.class), any(Mat.class), eq(CMP_NE)))
                .thenReturn(dynamicMask);
            when(matOps3d.bItwise_or(any(Mat.class), any(Mat.class)))
                .thenReturn(dynamicMask);
            
            // Mock bitwise_not for the internal call
            doAnswer(invocation -> {
                Mat src = invocation.getArgument(0);
                Mat dst = invocation.getArgument(1);
                dst.create(src.size(), src.type());
                dst.setTo(new Scalar(127)); // Inverted value
                return null;
            }).when(matOps3d).bItwise_not(any(Mat.class));
            
            Mat result = dynamicPixelFinder.getFixedPixelMask(testRegion, intervalSeconds, totalSeconds);
            
            assertNotNull(result);
            verify(imageLoader).getMatsFromScreen(testRegion, intervalSeconds, totalSeconds);
            
            dynamicMask.release();
            if (result != null && !result.isNull()) result.release();
        }
        
        @Test
        @DisplayName("Should handle different capture intervals")
        void shouldHandleDifferentCaptureIntervals() {
            Region testRegion = new Region(0, 0, 50, 50);
            
            // Test with short interval
            double shortInterval = 0.1;
            double shortTotal = 0.5;
            
            MatVector shortCapture = new MatVector(testImage1, testImage2);
            when(imageLoader.getMatsFromScreen(eq(testRegion), eq(shortInterval), eq(shortTotal)))
                .thenReturn(shortCapture);
            
            Mat mask = new Mat(50, 50, CV_8UC1);
            when(matOps3d.cOmpare(any(Mat.class), any(Mat.class), eq(CMP_NE)))
                .thenReturn(mask);
            when(matOps3d.bItwise_or(any(Mat.class), any(Mat.class)))
                .thenReturn(mask);
            
            Mat result1 = dynamicPixelFinder.getDynamicPixelMask(testRegion, shortInterval, shortTotal);
            assertNotNull(result1);
            
            // Test with long interval
            double longInterval = 2.0;
            double longTotal = 10.0;
            
            MatVector longCapture = new MatVector(testImage1, testImage2, testImage3);
            when(imageLoader.getMatsFromScreen(eq(testRegion), eq(longInterval), eq(longTotal)))
                .thenReturn(longCapture);
            
            Mat result2 = dynamicPixelFinder.getDynamicPixelMask(testRegion, longInterval, longTotal);
            assertNotNull(result2);
            
            verify(imageLoader).getMatsFromScreen(testRegion, shortInterval, shortTotal);
            verify(imageLoader).getMatsFromScreen(testRegion, longInterval, longTotal);
            
            mask.release();
        }
    }
    
    @Nested
    @DisplayName("Edge Cases")
    class EdgeCases {
        
        @Test
        @DisplayName("Should handle empty MatVector")
        void shouldHandleEmptyMatVector() {
            MatVector emptyVector = new MatVector();
            
            // Since size is 0, should return empty Mat
            Mat result = dynamicPixelFinder.getDynamicPixelMask(emptyVector);
            
            assertNotNull(result);
            assertTrue(result.empty());
            
            verify(matOps3d, never()).cOmpare(any(Mat.class), any(Mat.class), anyInt());
        }
        
        @Test
        @DisplayName("Should handle identical images")
        void shouldHandleIdenticalImages() {
            Mat identical1 = createTestImage(50, 50, 128);
            Mat identical2 = createTestImage(50, 50, 128);
            MatVector identicalVector = new MatVector(identical1, identical2);
            
            Mat noChangeMask = new Mat(50, 50, CV_8UC1, new Scalar(0)); // All black (no changes)
            
            when(matOps3d.cOmpare(eq(identical1), eq(identical2), eq(CMP_NE)))
                .thenReturn(noChangeMask);
            when(matOps3d.bItwise_or(any(Mat.class), any(Mat.class)))
                .thenReturn(noChangeMask);
            
            try {
                Mat result = dynamicPixelFinder.getDynamicPixelMask(identicalVector);
                
                assertNotNull(result);
                // Should show no dynamic pixels
                verify(matOps3d).cOmpare(identical1, identical2, CMP_NE);
            } finally {
                identical1.release();
                identical2.release();
                noChangeMask.release();
            }
        }
        
        @Test
        @DisplayName("Should handle very large image sequences")
        void shouldHandleVeryLargeImageSequences() {
            // Create a large sequence
            Mat[] largeSequence = new Mat[20];
            for (int i = 0; i < 20; i++) {
                largeSequence[i] = createTestImage(100, 100, i * 10);
            }
            MatVector largeVector = new MatVector(largeSequence);
            
            Mat mask = new Mat(100, 100, CV_8UC1, new Scalar(255));
            when(matOps3d.cOmpare(any(Mat.class), any(Mat.class), eq(CMP_NE)))
                .thenReturn(mask);
            when(matOps3d.bItwise_or(any(Mat.class), any(Mat.class)))
                .thenReturn(mask);
            
            try {
                Mat result = dynamicPixelFinder.getDynamicPixelMask(largeVector);
                
                assertNotNull(result);
                // Should make 19 comparisons (all images compared to first)
                verify(matOps3d, times(19)).cOmpare(any(Mat.class), any(Mat.class), eq(CMP_NE));
            } finally {
                for (Mat img : largeSequence) {
                    img.release();
                }
                mask.release();
            }
        }
        
        @Test
        @DisplayName("Should handle region with zero dimensions")
        void shouldHandleRegionWithZeroDimensions() {
            Region zeroRegion = new Region(0, 0, 0, 0);
            
            MatVector emptyCapture = new MatVector();
            when(imageLoader.getMatsFromScreen(eq(zeroRegion), anyDouble(), anyDouble()))
                .thenReturn(emptyCapture);
            
            Mat result = dynamicPixelFinder.getDynamicPixelMask(zeroRegion, 1.0, 5.0);
            
            assertNotNull(result);
            assertTrue(result.empty());
        }
    }
    
    @Nested
    @DisplayName("Performance Considerations")
    class PerformanceConsiderations {
        
        @Test
        @DisplayName("Should efficiently process high-resolution images")
        void shouldEfficientlyProcessHighResolutionImages() {
            Mat hd1 = new Mat(1080, 1920, CV_8UC3, new Scalar(50, 50, 50, 0));
            Mat hd2 = new Mat(1080, 1920, CV_8UC3, new Scalar(100, 100, 100, 0));
            MatVector hdVector = new MatVector(hd1, hd2);
            
            Mat hdMask = new Mat(1080, 1920, CV_8UC1, new Scalar(255));
            
            when(matOps3d.cOmpare(any(Mat.class), any(Mat.class), eq(CMP_NE)))
                .thenReturn(hdMask);
            when(matOps3d.bItwise_or(any(Mat.class), any(Mat.class)))
                .thenReturn(hdMask);
            
            try {
                long startTime = System.currentTimeMillis();
                Mat result = dynamicPixelFinder.getDynamicPixelMask(hdVector);
                long endTime = System.currentTimeMillis();
                
                assertNotNull(result);
                
                // Should complete quickly (mocked operations)
                assertTrue((endTime - startTime) < 1000,
                    "HD processing took too long: " + (endTime - startTime) + "ms");
            } finally {
                hd1.release();
                hd2.release();
                hdMask.release();
            }
        }
    }
}