package io.github.jspinak.brobot.analysis.motion;

import io.github.jspinak.brobot.util.image.core.ColorMatrixUtilities;
import io.github.jspinak.brobot.util.image.core.MatrixUtilities;
import org.bytedeco.opencv.opencv_core.Mat;
import org.bytedeco.opencv.opencv_core.MatVector;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.MockedStatic;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.bytedeco.opencv.global.opencv_core.*;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class MotionDetectorTest {

    @Mock
    private ColorMatrixUtilities colorMatrixUtilities;
    
    private MotionDetector motionDetector;
    
    @BeforeEach
    void setUp() {
        motionDetector = new MotionDetector(colorMatrixUtilities);
    }
    
    @Test
    void testGetDynamicPixelMask_IdenticalImages() {
        // Setup - create two identical images
        Mat image1 = new Mat(100, 100, CV_8UC3);
        Mat image2 = new Mat(100, 100, CV_8UC3);
        image1.ptr().fill(100); // Fill with same value
        image2.ptr().fill(100);
        
        try (MockedStatic<MatrixUtilities> mockedStatic = mockStatic(MatrixUtilities.class)) {
            Mat grayImage = new Mat(100, 100, CV_8UC1);
            grayImage.ptr().fill(100);
            mockedStatic.when(() -> MatrixUtilities.getGrayscale(any())).thenReturn(grayImage);
            
            // Execute
            Mat result = motionDetector.getDynamicPixelMask(image1, image2);
            
            // Verify - should be all black (no motion)
            assertNotNull(result);
            assertEquals(100, result.rows());
            assertEquals(100, result.cols());
            assertEquals(CV_8UC1, result.type());
        }
    }
    
    @Test
    void testGetDynamicPixelMask_DifferentImages() {
        // Setup - create two different images
        Mat image1 = new Mat(100, 100, CV_8UC3);
        Mat image2 = new Mat(100, 100, CV_8UC3);
        image1.ptr().fill(0);   // Black image
        image2.ptr().fill(255); // White image
        
        try (MockedStatic<MatrixUtilities> mockedStatic = mockStatic(MatrixUtilities.class)) {
            Mat grayImage1 = new Mat(100, 100, CV_8UC1);
            Mat grayImage2 = new Mat(100, 100, CV_8UC1);
            grayImage1.ptr().fill(0);
            grayImage2.ptr().fill(255);
            
            mockedStatic.when(() -> MatrixUtilities.getGrayscale(image1)).thenReturn(grayImage1);
            mockedStatic.when(() -> MatrixUtilities.getGrayscale(image2)).thenReturn(grayImage2);
            
            // Execute
            Mat result = motionDetector.getDynamicPixelMask(image1, image2);
            
            // Verify - should detect motion
            assertNotNull(result);
            assertEquals(100, result.rows());
            assertEquals(100, result.cols());
            assertEquals(CV_8UC1, result.type());
        }
    }
    
    @Test
    void testGetDynamicPixelMask_MatVector_NullInput() {
        // Execute
        Mat result = motionDetector.getDynamicPixelMask((MatVector) null);
        
        // Verify
        assertNotNull(result);
        assertTrue(result.empty());
    }
    
    @Test
    void testGetDynamicPixelMask_MatVector_SingleImage() {
        // Setup
        Mat image = new Mat(100, 100, CV_8UC3);
        MatVector matVector = new MatVector(image);
        
        // Execute
        Mat result = motionDetector.getDynamicPixelMask(matVector);
        
        // Verify - should return empty Mat since we need at least 2 images
        assertNotNull(result);
        assertTrue(result.empty());
    }
    
    @Test
    void testGetDynamicPixelMask_MatVector_TwoImages() {
        // Setup
        Mat image1 = new Mat(50, 50, CV_8UC3);
        Mat image2 = new Mat(50, 50, CV_8UC3);
        image1.ptr().fill(100);
        image2.ptr().fill(150);
        
        MatVector matVector = new MatVector(image1, image2);
        
        try (MockedStatic<MatrixUtilities> mockedStatic = mockStatic(MatrixUtilities.class)) {
            Mat grayImage1 = new Mat(50, 50, CV_8UC1);
            Mat grayImage2 = new Mat(50, 50, CV_8UC1);
            grayImage1.ptr().fill(100);
            grayImage2.ptr().fill(150);
            
            mockedStatic.when(() -> MatrixUtilities.getGrayscale(image1)).thenReturn(grayImage1);
            mockedStatic.when(() -> MatrixUtilities.getGrayscale(image2)).thenReturn(grayImage2);
            
            // Execute
            Mat result = motionDetector.getDynamicPixelMask(matVector);
            
            // Verify
            assertNotNull(result);
            assertFalse(result.empty());
            assertEquals(50, result.rows());
            assertEquals(50, result.cols());
        }
    }
    
    @Test
    void testGetDynamicPixelMask_MatVector_MultipleImages() {
        // Setup
        Mat image1 = new Mat(30, 30, CV_8UC3);
        Mat image2 = new Mat(30, 30, CV_8UC3);
        Mat image3 = new Mat(30, 30, CV_8UC3);
        Mat image4 = new Mat(30, 30, CV_8UC3);
        
        image1.ptr().fill(0);
        image2.ptr().fill(50);
        image3.ptr().fill(100);
        image4.ptr().fill(150);
        
        MatVector matVector = new MatVector(image1, image2, image3, image4);
        
        try (MockedStatic<MatrixUtilities> mockedStatic = mockStatic(MatrixUtilities.class)) {
            // Mock grayscale conversions
            Mat gray1 = new Mat(30, 30, CV_8UC1);
            Mat gray2 = new Mat(30, 30, CV_8UC1);
            Mat gray3 = new Mat(30, 30, CV_8UC1);
            Mat gray4 = new Mat(30, 30, CV_8UC1);
            
            gray1.ptr().fill(0);
            gray2.ptr().fill(50);
            gray3.ptr().fill(100);
            gray4.ptr().fill(150);
            
            mockedStatic.when(() -> MatrixUtilities.getGrayscale(image1)).thenReturn(gray1);
            mockedStatic.when(() -> MatrixUtilities.getGrayscale(image2)).thenReturn(gray2);
            mockedStatic.when(() -> MatrixUtilities.getGrayscale(image3)).thenReturn(gray3);
            mockedStatic.when(() -> MatrixUtilities.getGrayscale(image4)).thenReturn(gray4);
            
            // Execute
            Mat result = motionDetector.getDynamicPixelMask(matVector);
            
            // Verify
            assertNotNull(result);
            assertFalse(result.empty());
            assertEquals(30, result.rows());
            assertEquals(30, result.cols());
            // Should combine masks from comparing image1 with images 2, 3, and 4
        }
    }
    
    @Test
    void testGetFixedPixelMask() {
        // Setup
        Mat image1 = new Mat(20, 20, CV_8UC3);
        Mat image2 = new Mat(20, 20, CV_8UC3);
        image1.ptr().fill(100);
        image2.ptr().fill(100); // Same as image1
        
        MatVector matVector = new MatVector(image1, image2);
        
        Mat expectedDynamicMask = new Mat(20, 20, CV_8UC1);
        expectedDynamicMask.ptr().fill(0); // No motion
        
        Mat expectedFixedMask = new Mat(20, 20, CV_8UC1);
        expectedFixedMask.ptr().fill(255); // All pixels are fixed
        
        when(colorMatrixUtilities.bItwise_not(any(Mat.class))).thenReturn(expectedFixedMask);
        
        try (MockedStatic<MatrixUtilities> mockedStatic = mockStatic(MatrixUtilities.class)) {
            Mat grayImage = new Mat(20, 20, CV_8UC1);
            grayImage.ptr().fill(100);
            mockedStatic.when(() -> MatrixUtilities.getGrayscale(any())).thenReturn(grayImage);
            
            // Execute
            Mat result = motionDetector.getFixedPixelMask(matVector);
            
            // Verify
            assertNotNull(result);
            assertEquals(expectedFixedMask, result);
            verify(colorMatrixUtilities).bItwise_not(any(Mat.class));
        }
    }
    
    @Test
    void testGetFixedPixelMask_EmptyVector() {
        // Setup
        MatVector emptyVector = new MatVector();
        
        Mat emptyMat = new Mat();
        when(colorMatrixUtilities.bItwise_not(any(Mat.class))).thenReturn(emptyMat);
        
        // Execute
        Mat result = motionDetector.getFixedPixelMask(emptyVector);
        
        // Verify
        assertNotNull(result);
        verify(colorMatrixUtilities).bItwise_not(any(Mat.class));
    }
}