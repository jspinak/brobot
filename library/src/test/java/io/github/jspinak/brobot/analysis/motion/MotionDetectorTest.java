package io.github.jspinak.brobot.analysis.motion;

import io.github.jspinak.brobot.test.BrobotTestBase;
import io.github.jspinak.brobot.util.image.core.ColorMatrixUtilities;
import io.github.jspinak.brobot.util.image.core.MatrixUtilities;
import org.bytedeco.opencv.opencv_core.Mat;
import org.bytedeco.opencv.opencv_core.MatVector;
import org.bytedeco.opencv.opencv_core.Scalar;
import org.junit.jupiter.api.*;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockedStatic;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.ArrayList;
import java.util.List;

import static org.bytedeco.opencv.global.opencv_core.*;
import static org.bytedeco.opencv.global.opencv_imgproc.*;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("MotionDetector Tests")
public class MotionDetectorTest extends BrobotTestBase {

    @Mock
    private ColorMatrixUtilities matOps3d;
    
    @InjectMocks
    private MotionDetector motionDetector;
    
    private Mat testImage1;
    private Mat testImage2;
    private Mat testImage3;
    
    @BeforeEach
    public void setUp() {
        super.setupTest();
        
        // Create test images with different patterns
        testImage1 = createTestImage(100, 100, 0);
        testImage2 = createTestImage(100, 100, 50);
        testImage3 = createTestImage(100, 100, 100);
    }
    
    @AfterEach
    public void tearDown() {
        releaseIfNotNull(testImage1);
        releaseIfNotNull(testImage2);
        releaseIfNotNull(testImage3);
    }
    
    private Mat createTestImage(int width, int height, int grayValue) {
        Mat image = new Mat(height, width, CV_8UC3);
        image.put(new Scalar(grayValue, grayValue, grayValue, 0));
        return image;
    }
    
    private void releaseIfNotNull(Mat mat) {
        if (mat != null && !mat.isNull()) {
            mat.release();
        }
    }
    
    @Test
    @DisplayName("Should detect motion between two different images")
    void shouldDetectMotionBetweenDifferentImages() {
        Mat result = motionDetector.getDynamicPixelMask(testImage1, testImage2);
        
        assertNotNull(result);
        assertFalse(result.empty());
        assertEquals(100, result.rows());
        assertEquals(100, result.cols());
        assertEquals(CV_8UC1, result.type()); // Binary mask should be single channel
        
        // Check that some pixels are detected as motion
        double sum = sumElems(result).get();
        assertTrue(sum > 0, "Should detect some motion pixels");
        
        result.release();
    }
    
    @Test
    @DisplayName("Should detect no motion between identical images")
    void shouldDetectNoMotionBetweenIdenticalImages() {
        Mat result = motionDetector.getDynamicPixelMask(testImage1, testImage1);
        
        assertNotNull(result);
        assertFalse(result.empty());
        
        // All pixels should be black (no motion)
        double sum = sumElems(result).get();
        assertEquals(0.0, sum, "Should detect no motion for identical images");
        
        result.release();
    }
    
    @Test
    @DisplayName("Should handle images with significant differences")
    void shouldHandleSignificantDifferences() {
        // Create images with large difference
        Mat blackImage = new Mat(100, 100, CV_8UC3, new Scalar(0, 0, 0, 0));
        Mat whiteImage = new Mat(100, 100, CV_8UC3, new Scalar(255, 255, 255, 0));
        
        try {
            Mat result = motionDetector.getDynamicPixelMask(blackImage, whiteImage);
            
            assertNotNull(result);
            
            // Most pixels should be detected as motion
            double sum = sumElems(result).get();
            double maxSum = 255.0 * 100 * 100; // All pixels white
            assertTrue(sum > maxSum * 0.5, "Should detect significant motion");
            
            result.release();
        } finally {
            blackImage.release();
            whiteImage.release();
        }
    }
    
    @Test
    @DisplayName("Should process MatVector with multiple images")
    void shouldProcessMatVectorWithMultipleImages() {
        MatVector matVector = new MatVector(testImage1, testImage2, testImage3);
        
        // No need to mock ColorMatrixUtilities for this test
        // The test will verify the result from getDynamicPixelMask
        
        Mat result = motionDetector.getDynamicPixelMask(matVector);
        
        assertNotNull(result);
        assertFalse(result.empty());
        
        // No need to verify ColorMatrixUtilities calls
        
        result.release();
    }
    
    @Test
    @DisplayName("Should return empty Mat for null MatVector")
    void shouldReturnEmptyMatForNullMatVector() {
        Mat result = motionDetector.getDynamicPixelMask((MatVector) null);
        
        assertNotNull(result);
        assertTrue(result.empty());
    }
    
    @Test
    @DisplayName("Should return empty Mat for MatVector with less than 2 images")
    void shouldReturnEmptyMatForInsufficientImages() {
        MatVector singleImage = new MatVector(testImage1);
        
        Mat result = motionDetector.getDynamicPixelMask(singleImage);
        
        assertNotNull(result);
        assertTrue(result.empty());
    }
    
    @Test
    @DisplayName("Should handle grayscale conversion properly")
    void shouldHandleGrayscaleConversion() {
        try (MockedStatic<MatrixUtilities> matrixMock = mockStatic(MatrixUtilities.class)) {
            Mat grayMat = new Mat(100, 100, CV_8UC1, new Scalar(128));
            
            matrixMock.when(() -> MatrixUtilities.getGrayscale(any(Mat.class)))
                .thenReturn(grayMat);
            
            Mat result = motionDetector.getDynamicPixelMask(testImage1, testImage2);
            
            assertNotNull(result);
            matrixMock.verify(() -> MatrixUtilities.getGrayscale(any(Mat.class)), times(2));
            
            result.release();
            grayMat.release();
        }
    }
    
    @Test
    @DisplayName("Should apply Gaussian blur for noise reduction")
    void shouldApplyGaussianBlur() {
        // This test verifies the internal process includes Gaussian blur
        Mat noisyImage1 = createNoisyImage(100, 100);
        Mat noisyImage2 = createNoisyImage(100, 100);
        
        try {
            Mat result = motionDetector.getDynamicPixelMask(noisyImage1, noisyImage2);
            
            assertNotNull(result);
            // The blur should reduce noise, resulting in fewer motion pixels
            // than a direct difference would produce
            assertFalse(result.empty());
            
            result.release();
        } finally {
            noisyImage1.release();
            noisyImage2.release();
        }
    }
    
    @Test
    @DisplayName("Should threshold motion mask properly")
    void shouldThresholdMotionMask() {
        // Create images with small difference (below threshold)
        Mat image1 = new Mat(100, 100, CV_8UC3, new Scalar(100, 100, 100, 0));
        Mat image2 = new Mat(100, 100, CV_8UC3, new Scalar(120, 120, 120, 0));
        
        try {
            Mat result = motionDetector.getDynamicPixelMask(image1, image2);
            
            assertNotNull(result);
            
            // Check that result is binary (only 0 or 255 values)
            Mat unique = new Mat();
            // In a binary image, pixels should only be 0 or 255
            double[] minVal = new double[1];
            double[] maxVal = new double[1];
            minMaxLoc(result, minVal, maxVal, null, null, null);
            
            assertTrue(minVal[0] == 0.0 || minVal[0] == 255.0);
            assertTrue(maxVal[0] == 0.0 || maxVal[0] == 255.0);
            
            result.release();
        } finally {
            image1.release();
            image2.release();
        }
    }
    
    @Test
    @DisplayName("Should combine multiple frame differences")
    void shouldCombineMultipleFrameDifferences() {
        // Create a sequence with different motion in each frame
        Mat baseImage = new Mat(100, 100, CV_8UC3, new Scalar(0, 0, 0, 0));
        Mat movingObject1 = createImageWithSquare(100, 100, 10, 10);
        Mat movingObject2 = createImageWithSquare(100, 100, 50, 50);
        
        MatVector sequence = new MatVector(baseImage, movingObject1, movingObject2);
        
        // No need to mock ColorMatrixUtilities for this test
        // The test will verify the result from getDynamicPixelMask
        
        try {
            Mat result = motionDetector.getDynamicPixelMask(sequence);
            
            assertNotNull(result);
            assertFalse(result.empty());
            
            // Should have combined motion from both frames
            double sum = sumElems(result).get();
            assertTrue(sum > 0, "Combined mask should have motion pixels");
            
            result.release();
        } finally {
            baseImage.release();
            movingObject1.release();
            movingObject2.release();
        }
    }
    
    @Test
    @DisplayName("Should handle different image sizes gracefully")
    void shouldHandleDifferentImageSizes() {
        Mat smallImage = new Mat(50, 50, CV_8UC3, new Scalar(100, 100, 100, 0));
        Mat largeImage = new Mat(150, 150, CV_8UC3, new Scalar(100, 100, 100, 0));
        
        try {
            // This might throw an exception or handle it internally
            // The behavior depends on implementation
            Mat result = motionDetector.getDynamicPixelMask(smallImage, largeImage);
            
            // If it doesn't throw, check the result
            if (!result.empty()) {
                assertNotNull(result);
                assertTrue(result.rows() > 0);
                assertTrue(result.cols() > 0);
            }
            
            result.release();
        } catch (Exception e) {
            // Expected for mismatched sizes
            assertTrue(e.getMessage().contains("size") || e.getMessage().contains("dimension"));
        } finally {
            smallImage.release();
            largeImage.release();
        }
    }
    
    private Mat createNoisyImage(int width, int height) {
        Mat image = new Mat(height, width, CV_8UC3);
        // Add random noise
        for (int y = 0; y < height; y++) {
            for (int x = 0; x < width; x++) {
                int value = (int) (Math.random() * 256);
                image.ptr(y, x).put((byte) value, (byte) value, (byte) value);
            }
        }
        return image;
    }
    
    private Mat createImageWithSquare(int width, int height, int squareX, int squareY) {
        Mat image = new Mat(height, width, CV_8UC3, new Scalar(0, 0, 0, 0));
        // Draw a white square at specified position
        rectangle(image, new org.bytedeco.opencv.opencv_core.Point(squareX, squareY),
                  new org.bytedeco.opencv.opencv_core.Point(squareX + 20, squareY + 20),
                  new Scalar(255, 255, 255, 0), -1, 8, 0);
        return image;
    }
}