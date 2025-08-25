package io.github.jspinak.brobot.analysis.motion;

import io.github.jspinak.brobot.test.BrobotTestBase;
import io.github.jspinak.brobot.test.utils.MatTestUtils;
import org.bytedeco.javacpp.DoublePointer;
import org.bytedeco.opencv.opencv_core.Mat;
import org.bytedeco.opencv.opencv_core.MatVector;
import org.bytedeco.opencv.opencv_core.Scalar;
import org.junit.jupiter.api.*;
import org.junit.jupiter.api.Disabled;

import java.util.ArrayList;
import java.util.List;

import static org.bytedeco.opencv.global.opencv_core.*;
import static org.bytedeco.opencv.global.opencv_imgproc.*;
import static org.junit.jupiter.api.Assertions.*;

/**
 * Comprehensive test suite for PixelChangeDetector.
 * Tests configurable image processing pipeline for motion detection.
 */
@DisplayName("PixelChangeDetector Tests")
public class PixelChangeDetectorTest extends BrobotTestBase {
    
    private Mat testImage1;
    private Mat testImage2;
    private Mat testImage3;
    private MatVector testMatVector;
    
    @BeforeEach
    @Override
    public void setupTest() {
        super.setupTest();
        
        // Create test images with different patterns using safe utilities
        testImage1 = MatTestUtils.createGrayMat(100, 100, 50);
        testImage2 = MatTestUtils.createGrayMat(100, 100, 100);
        testImage3 = MatTestUtils.createGrayMat(100, 100, 150);
        
        // Validate all Mats before creating MatVector
        MatTestUtils.validateMat(testImage1, "testImage1");
        MatTestUtils.validateMat(testImage2, "testImage2");
        MatTestUtils.validateMat(testImage3, "testImage3");
        
        testMatVector = new MatVector(testImage1, testImage2, testImage3);
    }
    
    @AfterEach
    public void tearDown() {
        MatTestUtils.safeReleaseAll(testImage1, testImage2, testImage3);
        testMatVector = null;
    }
    
    private Mat createTestImage(int width, int height, int grayValue) {
        return MatTestUtils.createColorMat(height, width, grayValue, grayValue, grayValue);
    }
    
    private Mat createImageWithPattern(int width, int height) {
        // Use gradient for pattern
        Mat grayGradient = MatTestUtils.createGradientMat(height, width, true);
        Mat image = MatTestUtils.createSafeMat(height, width, CV_8UC3);
        
        // Convert single channel gradient to 3-channel
        Mat[] channels = new Mat[3];
        channels[0] = grayGradient;
        channels[1] = grayGradient.clone();
        channels[2] = grayGradient.clone();
        merge(new MatVector(channels), image);
        
        MatTestUtils.safeReleaseAll(channels);
        return image;
    }
    
    // Removed - using MatTestUtils.safeRelease instead
    
    @Nested
    @DisplayName("Builder Configuration")
    class BuilderConfiguration {
        
        @Test
        @DisplayName("Should build with minimal configuration")
        void shouldBuildWithMinimalConfiguration() {
            PixelChangeDetector detector = new PixelChangeDetector.Builder()
                .setMats(testMatVector)
                .build();
            
            assertNotNull(detector);
            assertNotNull(detector.getChangeMask());
            assertFalse(detector.isUseGrayscale());
            assertFalse(detector.isUseGaussianBlur());
            assertFalse(detector.isUseDilation());
            assertFalse(detector.isUseThreshold());
        }
        
        @Test
        @DisplayName("Should build with grayscale enabled")
        void shouldBuildWithGrayscaleEnabled() {
            PixelChangeDetector detector = new PixelChangeDetector.Builder()
                .setMats(testMatVector)
                .useGrayscale()
                .build();
            
            assertNotNull(detector);
            assertTrue(detector.isUseGrayscale());
            assertFalse(detector.getGrays().isEmpty());
            assertEquals(3, detector.getGrays().size());
        }
        
        @Test
        @DisplayName("Should build with Gaussian blur")
        void shouldBuildWithGaussianBlur() {
            PixelChangeDetector detector = new PixelChangeDetector.Builder()
                .setMats(testMatVector)
                .useGaussianBlur(5, 5, 0)
                .build();
            
            assertNotNull(detector);
            assertTrue(detector.isUseGaussianBlur());
            assertEquals(5, detector.getGaussianWidth());
            assertEquals(5, detector.getGaussianHeight());
            assertEquals(0, detector.getGaussianSigmaX());
            assertFalse(detector.getGaussians().isEmpty());
        }
        
        @Test
        @DisplayName("Should build with dilation")
        void shouldBuildWithDilation() {
            PixelChangeDetector detector = new PixelChangeDetector.Builder()
                .setMats(testMatVector)
                .useDilation(7, 7, 1)
                .build();
            
            assertNotNull(detector);
            assertTrue(detector.isUseDilation());
            assertEquals(7, detector.getDilationRows());
            assertEquals(7, detector.getDilationCols());
            assertEquals(1, detector.getDilationType());
            assertNotNull(detector.getDilation());
        }
        
        @Test
        @DisplayName("Should build with threshold")
        void shouldBuildWithThreshold() {
            PixelChangeDetector detector = new PixelChangeDetector.Builder()
                .setMats(testMatVector)
                .useThreshold(60, 255)
                .build();
            
            assertNotNull(detector);
            assertTrue(detector.isUseThreshold());
            assertEquals(60, detector.getThreshMin());
            assertEquals(255, detector.getThreshMax());
            assertNotNull(detector.getThreshold());
        }
        
        @Test
        @DisplayName("Should build with full pipeline")
        void shouldBuildWithFullPipeline() {
            PixelChangeDetector detector = new PixelChangeDetector.Builder()
                .setMats(testMatVector)
                .useGrayscale()
                .useGaussianBlur(3, 3, 0)
                .useDilation(5, 5, 1)
                .useThreshold(50, 255)
                .build();
            
            assertNotNull(detector);
            assertTrue(detector.isUseGrayscale());
            assertTrue(detector.isUseGaussianBlur());
            assertTrue(detector.isUseDilation());
            assertTrue(detector.isUseThreshold());
            assertNotNull(detector.getChangeMask());
        }
    }
    
    @Nested
    @DisplayName("Change Detection")
    class ChangeDetection {
        
        @Test
        @DisplayName("Should detect changes between different images")
        void shouldDetectChangesBetweenDifferentImages() {
            PixelChangeDetector detector = new PixelChangeDetector.Builder()
                .setMats(testMatVector)
                .build();
            
            Mat changeMask = detector.getChangeMask();
            assertNotNull(changeMask);
            assertFalse(changeMask.empty());
            
            // Should detect differences
            double sum = sumElems(changeMask).get();
            assertTrue(sum > 0, "Should detect changes between different images");
        }
        
        @Test
        @DisplayName("Should detect no changes for identical images")
        void shouldDetectNoChangesForIdenticalImages() {
            Mat same1 = MatTestUtils.createColorMat(100, 100, 128, 128, 128);
            Mat same2 = MatTestUtils.createColorMat(100, 100, 128, 128, 128);
            Mat same3 = MatTestUtils.createColorMat(100, 100, 128, 128, 128);
            
            // Validate all Mats
            MatTestUtils.validateMat(same1, "same1");
            MatTestUtils.validateMat(same2, "same2");
            MatTestUtils.validateMat(same3, "same3");
            
            MatVector identicalVector = new MatVector(same1, same2, same3);
            
            try {
                PixelChangeDetector detector = new PixelChangeDetector.Builder()
                    .setMats(identicalVector)
                    .build();
                
                Mat changeMask = detector.getChangeMask();
                double sum = sumElems(changeMask).get();
                assertEquals(0.0, sum, "Should detect no changes for identical images");
            } finally {
                MatTestUtils.safeReleaseAll(same1, same2, same3);
            }
        }
        
        @Test
        @DisplayName("Should detect changes with grayscale conversion")
        void shouldDetectChangesWithGrayscaleConversion() {
            PixelChangeDetector detector = new PixelChangeDetector.Builder()
                .setMats(testMatVector)
                .useGrayscale()
                .build();
            
            Mat changeMask = detector.getChangeMask();
            assertNotNull(changeMask);
            assertEquals(CV_8UC1, changeMask.type()); // Should be single channel
            
            double sum = sumElems(changeMask).get();
            assertTrue(sum > 0, "Should detect changes after grayscale conversion");
        }
    }
    
    @Nested
    @DisplayName("Threshold Behavior")
    class ThresholdBehavior {
        
        @Test
        @DisplayName("Should filter small changes with threshold")
        void shouldFilterSmallChangesWithThreshold() {
            // Create images with small differences using safe utilities
            Mat img1 = MatTestUtils.createColorMat(100, 100, 100, 100, 100);
            Mat img2 = MatTestUtils.createColorMat(100, 100, 110, 110, 110); // 10 difference
            
            MatTestUtils.validateMat(img1, "threshold img1");
            MatTestUtils.validateMat(img2, "threshold img2");
            
            MatVector smallChangeVector = new MatVector(img1, img2);
            
            try {
                // Without threshold
                PixelChangeDetector detectorNoThresh = new PixelChangeDetector.Builder()
                    .setMats(smallChangeVector)
                    .build();
                
                // With threshold higher than difference
                PixelChangeDetector detectorWithThresh = new PixelChangeDetector.Builder()
                    .setMats(smallChangeVector)
                    .useThreshold(20, 255) // Threshold > 10 difference
                    .build();
                
                Mat noThreshMask = detectorNoThresh.getChangeMask();
                Mat withThreshMask = detectorWithThresh.getThreshold();
                
                double sumNoThresh = sumElems(noThreshMask).get();
                double sumWithThresh = sumElems(withThreshMask).get();
                
                // Threshold should filter out small changes
                assertEquals(0.0, sumWithThresh, "Threshold should filter small changes");
                assertTrue(sumNoThresh > 0, "Without threshold should detect changes");
            } finally {
                MatTestUtils.safeReleaseAll(img1, img2);
            }
        }
        
        @Test
        @DisplayName("Should detect large changes with threshold")
        void shouldDetectLargeChangesWithThreshold() {
            // Create images with large differences using safe utilities
            Mat blackImg = MatTestUtils.createColorMat(100, 100, 0, 0, 0);
            Mat whiteImg = MatTestUtils.createColorMat(100, 100, 255, 255, 255);
            
            MatTestUtils.validateMat(blackImg, "blackImg");
            MatTestUtils.validateMat(whiteImg, "whiteImg");
            
            MatVector largeChangeVector = new MatVector(blackImg, whiteImg);
            
            try {
                PixelChangeDetector detector = new PixelChangeDetector.Builder()
                    .setMats(largeChangeVector)
                    .useThreshold(50, 255)
                    .build();
                
                Mat threshMask = detector.getThreshold();
                double sum = sumElems(threshMask).get();
                
                // Should detect significant changes
                double maxSum = 255.0 * 100 * 100;
                assertTrue(sum > maxSum * 0.9, "Should detect large changes above threshold");
            } finally {
                MatTestUtils.safeReleaseAll(blackImg, whiteImg);
            }
        }
    }
    
    @Nested
    @DisplayName("Gaussian Blur Effects")
    class GaussianBlurEffects {
        
        @Test
        @DisplayName("Should smooth noise with Gaussian blur")
        void shouldSmoothNoiseWithGaussianBlur() {
            // Create base image and noisy variant
            Mat base = MatTestUtils.createColorMat(100, 100, 100, 100, 100);
            Mat noisy = base.clone();
            
            // Add salt-and-pepper noise to one image
            for (int i = 0; i < 100; i++) {
                int x = (int)(Math.random() * 100);
                int y = (int)(Math.random() * 100);
                byte value = (byte)(Math.random() > 0.5 ? 255 : 0);
                noisy.ptr(y, x).put(value, value, value);
            }
            
            // Validate images
            MatTestUtils.validateMat(base, "base");
            MatTestUtils.validateMat(noisy, "noisy");
            
            MatVector noisyVector = new MatVector(base, noisy);
            
            try {
                // Without Gaussian blur
                PixelChangeDetector detectorNoBlur = new PixelChangeDetector.Builder()
                    .setMats(noisyVector)
                    .build();
                
                // With Gaussian blur
                PixelChangeDetector detectorWithBlur = new PixelChangeDetector.Builder()
                    .setMats(noisyVector)
                    .useGaussianBlur(5, 5, 0)
                    .build();
                
                Mat noBlurMask = detectorNoBlur.getChangeMask();
                Mat withBlurMask = detectorWithBlur.getChangeMask();
                
                double sumNoBlur = sumElems(noBlurMask).get();
                double sumWithBlur = sumElems(withBlurMask).get();
                
                // Blur should reduce noise-induced changes
                assertTrue(sumWithBlur < sumNoBlur, 
                    "Gaussian blur should reduce noise-induced changes");
            } finally {
                MatTestUtils.safeReleaseAll(base, noisy);
            }
        }
        
        private Mat createNoisyImage(int width, int height, int baseValue) {
            // Create base image with safe utilities
            Mat image = MatTestUtils.createColorMat(height, width, baseValue, baseValue, baseValue);
            
            // Add noise using OpenCV function with proper Mat parameters
            Mat noise = MatTestUtils.createSafeMat(height, width, CV_8UC3);
            Mat mean = new Mat(1, 1, CV_8UC3, new Scalar(0, 0, 0, 0));
            Mat stddev = new Mat(1, 1, CV_8UC3, new Scalar(10, 10, 10, 0));
            randn(noise, mean, stddev);
            add(image, noise, image);
            
            MatTestUtils.safeReleaseAll(noise, mean, stddev);
            MatTestUtils.validateMat(image, "createNoisyImage result");
            return image;
        }
    }
    
    @Nested
    @DisplayName("Dilation Effects")
    class DilationEffects {
        
        @Test
        @Disabled("Causes JVM crash - OpenCV native memory issue")
        @DisplayName("Should expand changed regions with dilation")
        void shouldExpandChangedRegionsWithDilation() {
            // Create images with small isolated changes
            Mat img1 = createTestImage(100, 100, 100);
            Mat img2 = createTestImage(100, 100, 100);
            // Add a small changed region
            rectangle(img2, new org.bytedeco.opencv.opencv_core.Point(45, 45),
                     new org.bytedeco.opencv.opencv_core.Point(55, 55),
                     new Scalar(200, 200, 200, 0), -1, 8, 0);
            MatVector vectorWithSmallChange = new MatVector(img1, img2);
            
            try {
                // Without dilation
                PixelChangeDetector detectorNoDilation = new PixelChangeDetector.Builder()
                    .setMats(vectorWithSmallChange)
                    .build();
                
                // With dilation
                PixelChangeDetector detectorWithDilation = new PixelChangeDetector.Builder()
                    .setMats(vectorWithSmallChange)
                    .useDilation(5, 5, 1)
                    .build();
                
                double sumNoDilation = sumElems(detectorNoDilation.getChangeMask()).get();
                double sumWithDilation = sumElems(detectorWithDilation.getDilation()).get();
                
                // Dilation should expand the changed region
                assertTrue(sumWithDilation > sumNoDilation,
                    "Dilation should expand changed regions");
            } finally {
                MatTestUtils.safeReleaseAll(img1, img2);
            }
        }
    }
    
    @Nested
    @DisplayName("Multiple Image Handling")
    class MultipleImageHandling {
        
        @Test
        @DisplayName("Should handle two images")
        void shouldHandleTwoImages() {
            MatVector twoImages = new MatVector(testImage1, testImage2);
            
            PixelChangeDetector detector = new PixelChangeDetector.Builder()
                .setMats(twoImages)
                .build();
            
            Mat changeMask = detector.getChangeMask();
            assertNotNull(changeMask);
            assertFalse(changeMask.empty());
        }
        
        @Test
        @DisplayName("Should handle many images")
        void shouldHandleManyImages() {
            Mat[] images = new Mat[10];
            for (int i = 0; i < 10; i++) {
                images[i] = createTestImage(50, 50, i * 25);
            }
            MatVector manyImages = new MatVector(images);
            
            try {
                PixelChangeDetector detector = new PixelChangeDetector.Builder()
                    .setMats(manyImages)
                    .build();
                
                Mat changeMask = detector.getChangeMask();
                assertNotNull(changeMask);
                assertEquals(50, changeMask.rows());
                assertEquals(50, changeMask.cols());
                
                // Should detect changes across the sequence
                double sum = sumElems(changeMask).get();
                assertTrue(sum > 0, "Should detect changes in varied sequence");
            } finally {
                for (Mat img : images) {
                    img.release();
                }
            }
        }
        
        @Test
        @DisplayName("Should find maximum difference across multiple images")
        void shouldFindMaximumDifferenceAcrossMultipleImages() {
            // Create sequence: black -> gray -> white
            Mat black = new Mat(50, 50, CV_8UC3, new Scalar(0, 0, 0, 0));
            Mat gray = new Mat(50, 50, CV_8UC3, new Scalar(128, 128, 128, 0));
            Mat white = new Mat(50, 50, CV_8UC3, new Scalar(255, 255, 255, 0));
            MatVector gradientVector = new MatVector(black, gray, white);
            
            try {
                PixelChangeDetector detector = new PixelChangeDetector.Builder()
                    .setMats(gradientVector)
                    .build();
                
                Mat changeMask = detector.getChangeMask();
                // Maximum difference is between black and white (255)
                double maxDiff = 255.0;
                
                // Check absolute difference captures max range
                Mat absDiff = detector.getAbsDiff();
                DoublePointer minVal = new DoublePointer(1);
                DoublePointer maxVal = new DoublePointer(1);
                minMaxLoc(absDiff, minVal, maxVal, null, null, null);
                
                assertEquals(maxDiff, maxVal.get(), 1.0,
                    "Should capture maximum difference across all images");
            } finally {
                black.release();
                gray.release();
                white.release();
            }
        }
    }
    
    @Nested
    @DisplayName("Edge Cases")
    class EdgeCases {
        
        @Test
        @DisplayName("Should handle single image")
        void shouldHandleSingleImage() {
            MatVector singleImage = new MatVector(testImage1);
            
            PixelChangeDetector detector = new PixelChangeDetector.Builder()
                .setMats(singleImage)
                .build();
            
            Mat changeMask = detector.getChangeMask();
            assertNotNull(changeMask);
            
            // Single image should have no changes
            double sum = sumElems(changeMask).get();
            assertEquals(0.0, sum, "Single image should have no changes");
        }
        
        @Test
        @DisplayName("Should handle empty MatVector")
        void shouldHandleEmptyMatVector() {
            MatVector emptyVector = new MatVector();
            
            PixelChangeDetector detector = new PixelChangeDetector.Builder()
                .setMats(emptyVector)
                .build();
            
            Mat changeMask = detector.getChangeMask();
            assertNotNull(changeMask);
            assertTrue(changeMask.empty());
        }
        
        @Test
        @DisplayName("Should handle very small images")
        void shouldHandleVerySmallImages() {
            Mat tiny1 = new Mat(1, 1, CV_8UC3, new Scalar(100, 100, 100, 0));
            Mat tiny2 = new Mat(1, 1, CV_8UC3, new Scalar(200, 200, 200, 0));
            MatVector tinyVector = new MatVector(tiny1, tiny2);
            
            try {
                PixelChangeDetector detector = new PixelChangeDetector.Builder()
                    .setMats(tinyVector)
                    .useGrayscale()
                    .useThreshold(50, 255)
                    .build();
                
                Mat changeMask = detector.getChangeMask();
                assertNotNull(changeMask);
                assertEquals(1, changeMask.rows());
                assertEquals(1, changeMask.cols());
            } finally {
                tiny1.release();
                tiny2.release();
            }
        }
    }
    
    @Nested
    @DisplayName("Debug Features")
    class DebugFeatures {
        
        @Test
        @DisplayName("Should support debug printing")
        void shouldSupportDebugPrinting() {
            PixelChangeDetector detector = new PixelChangeDetector.Builder()
                .setMats(testMatVector)
                .useGrayscale()
                .useGaussianBlur(3, 3, 0)
                .useThreshold(50, 255)
                .build();
            
            // Should not throw exception
            assertDoesNotThrow(() -> detector.print(5, 5, 1));
        }
    }
}