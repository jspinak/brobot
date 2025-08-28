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
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.bytedeco.opencv.global.opencv_core.*;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

/**
 * Comprehensive test suite for DynamicPixelFinder.
 * Tests pixel-level change detection across image sequences.
 */
@ExtendWith(MockitoExtension.class)
@DisplayName("DynamicPixelFinder Tests")
public class DynamicPixelFinderTest extends BrobotTestBase {

    @Mock
    private ColorMatrixUtilities matOps3d;
    
    @Mock
    private ImageLoader getImage;
    
    @InjectMocks
    private DynamicPixelFinder dynamicPixelFinder;
    
    private Mat testMat1;
    private Mat testMat2;
    private Mat testMat3;
    
    @BeforeEach
    @Override
    public void setupTest() {
        super.setupTest();
        
        // Create test matrices with different values
        testMat1 = new Mat(100, 100, CV_8UC3, new Scalar(100, 100, 100, 0));
        testMat2 = new Mat(100, 100, CV_8UC3, new Scalar(150, 150, 150, 0));
        testMat3 = new Mat(100, 100, CV_8UC3, new Scalar(200, 200, 200, 0));
    }
    
    @AfterEach
    void tearDown() {
        releaseIfNotNull(testMat1);
        releaseIfNotNull(testMat2);
        releaseIfNotNull(testMat3);
    }
    
    private void releaseIfNotNull(Mat mat) {
        if (mat != null && !mat.isNull()) {
            mat.release();
        }
    }
    
    @Nested
    @DisplayName("Dynamic Pixel Detection")
    class DynamicPixelDetection {
        
        @Test
        @DisplayName("Should return empty Mat for single image")
        void shouldReturnEmptyMatForSingleImage() {
            MatVector singleImage = new MatVector(testMat1);
            
            Mat result = dynamicPixelFinder.getDynamicPixelMask(singleImage);
            
            assertNotNull(result);
            assertTrue(result.empty());
        }
        
        @Test
        @DisplayName("Should return empty Mat for no images")
        void shouldReturnEmptyMatForNoImages() {
            MatVector emptyVector = new MatVector();
            
            Mat result = dynamicPixelFinder.getDynamicPixelMask(emptyVector);
            
            assertNotNull(result);
            assertTrue(result.empty());
            
            verify(matOps3d, never()).cOmpare(any(Mat.class), any(Mat.class), anyInt());
        }
        
        @Test
        @DisplayName("Should find dynamic pixels between two different images")
        void shouldFindDynamicPixelsBetweenTwoImages() {
            MatVector matVector = new MatVector(testMat1, testMat2);
            Mat comparisonResult = new Mat(100, 100, CV_8UC3, new Scalar(255, 255, 255, 0));
            
            when(matOps3d.cOmpare(any(Mat.class), any(Mat.class), eq(CMP_NE)))
                .thenReturn(comparisonResult);
            when(matOps3d.bItwise_or(any(Mat.class), any(Mat.class)))
                .thenReturn(comparisonResult);
            
            Mat result = dynamicPixelFinder.getDynamicPixelMask(matVector);
            
            assertNotNull(result);
            assertEquals(comparisonResult, result);
            
            verify(matOps3d).cOmpare(any(Mat.class), any(Mat.class), eq(CMP_NE));
            verify(matOps3d).bItwise_or(any(Mat.class), any(Mat.class));
        }
        
        @Test
        @DisplayName("Should combine multiple comparisons with bitwise OR")
        void shouldCombineMultipleComparisons() {
            MatVector matVector = new MatVector(testMat1, testMat2, testMat3);
            
            Mat comparison1 = new Mat(100, 100, CV_8UC3, new Scalar(255, 0, 0, 0));
            Mat comparison2 = new Mat(100, 100, CV_8UC3, new Scalar(0, 255, 0, 0));
            Mat combinedMask = new Mat(100, 100, CV_8UC3, new Scalar(255, 255, 0, 0));
            
            when(matOps3d.cOmpare(any(Mat.class), any(Mat.class), eq(CMP_NE)))
                .thenReturn(comparison1, comparison2);
            when(matOps3d.bItwise_or(any(Mat.class), any(Mat.class)))
                .thenReturn(combinedMask);
            
            Mat result = dynamicPixelFinder.getDynamicPixelMask(matVector);
            
            assertNotNull(result);
            assertEquals(combinedMask, result);
            
            verify(matOps3d, times(2)).cOmpare(any(Mat.class), any(Mat.class), eq(CMP_NE));
            verify(matOps3d, atLeast(2)).bItwise_or(any(Mat.class), any(Mat.class));
        }
        
        @Test
        @DisplayName("Should detect no dynamic pixels for identical images")
        void shouldDetectNoDynamicPixelsForIdenticalImages() {
            MatVector matVector = new MatVector(testMat1, testMat1, testMat1);
            Mat emptyComparison = new Mat(100, 100, CV_8UC3, new Scalar(0, 0, 0, 0));
            
            when(matOps3d.cOmpare(any(Mat.class), any(Mat.class), eq(CMP_NE)))
                .thenReturn(emptyComparison);
            when(matOps3d.bItwise_or(any(Mat.class), any(Mat.class)))
                .thenReturn(emptyComparison);
            
            Mat result = dynamicPixelFinder.getDynamicPixelMask(matVector);
            
            assertNotNull(result);
            assertEquals(emptyComparison, result);
            
            verify(matOps3d, times(2)).cOmpare(any(Mat.class), any(Mat.class), eq(CMP_NE));
        }
    }
    
    @Nested
    @DisplayName("Fixed Pixel Detection")
    class FixedPixelDetection {
        
        @Test
        @DisplayName("Should find fixed pixels (inverse of dynamic pixels)")
        void shouldFindFixedPixels() {
            MatVector matVector = new MatVector(testMat1, testMat2);
            Mat dynamicMask = new Mat(100, 100, CV_8UC1, new Scalar(255));
            Mat fixedMask = new Mat(100, 100, CV_8UC1, new Scalar(0));
            
            when(matOps3d.cOmpare(any(Mat.class), any(Mat.class), eq(CMP_NE)))
                .thenReturn(dynamicMask);
            when(matOps3d.bItwise_or(any(Mat.class), any(Mat.class)))
                .thenReturn(dynamicMask);
            when(matOps3d.bItwise_not(dynamicMask))
                .thenReturn(fixedMask);
            
            Mat result = dynamicPixelFinder.getFixedPixelMask(matVector);
            
            assertNotNull(result);
            assertEquals(fixedMask, result);
            
            verify(matOps3d).cOmpare(any(Mat.class), any(Mat.class), eq(CMP_NE));
            verify(matOps3d).bItwise_not(any(Mat.class));
        }
    }
    
    @Nested
    @DisplayName("Time-based Capture")
    class TimeBasedCapture {
        
        @Test
        @DisplayName("Should capture images from region over time for dynamic pixels")
        void shouldCaptureImagesFromRegionForDynamicPixels() {
            Region region = mock(Region.class);
            double intervalSeconds = 0.5;
            double totalSecondsToRun = 2.0;
            
            MatVector capturedMats = new MatVector();
            capturedMats.push_back(testMat1);
            capturedMats.push_back(testMat2);
            capturedMats.push_back(testMat3);
            
            Mat dynamicMask = new Mat(100, 100, CV_8UC3, new Scalar(255, 255, 255, 0));
            
            when(getImage.getMatsFromScreen(region, intervalSeconds, totalSecondsToRun))
                .thenReturn(capturedMats);
            when(matOps3d.cOmpare(any(Mat.class), any(Mat.class), eq(CMP_NE)))
                .thenReturn(dynamicMask);
            when(matOps3d.bItwise_or(any(Mat.class), any(Mat.class)))
                .thenReturn(dynamicMask);
            
            Mat result = dynamicPixelFinder.getDynamicPixelMask(region, intervalSeconds, totalSecondsToRun);
            
            assertNotNull(result);
            assertEquals(dynamicMask, result);
            
            verify(getImage).getMatsFromScreen(region, intervalSeconds, totalSecondsToRun);
            verify(matOps3d, atLeastOnce()).cOmpare(any(Mat.class), any(Mat.class), eq(CMP_NE));
        }
        
        @Test
        @DisplayName("Should capture images from region over time for fixed pixels")
        void shouldCaptureImagesFromRegionForFixedPixels() {
            Region region = mock(Region.class);
            double intervalSeconds = 0.5;
            double totalSecondsToRun = 2.0;
            
            MatVector capturedMats = new MatVector();
            capturedMats.push_back(testMat1);
            capturedMats.push_back(testMat2);
            
            Mat dynamicMask = new Mat(100, 100, CV_8UC3, new Scalar(255, 255, 255, 0));
            Mat fixedMask = new Mat(100, 100, CV_8UC3, new Scalar(0, 0, 0, 0));
            
            when(getImage.getMatsFromScreen(region, intervalSeconds, totalSecondsToRun))
                .thenReturn(capturedMats);
            when(matOps3d.cOmpare(any(Mat.class), any(Mat.class), eq(CMP_NE)))
                .thenReturn(dynamicMask);
            when(matOps3d.bItwise_or(any(Mat.class), any(Mat.class)))
                .thenReturn(dynamicMask);
            
            // Mock for bitwise_not is not needed since it's a static method
            // The production code uses static bitwise_not directly
            
            Mat result = dynamicPixelFinder.getFixedPixelMask(region, intervalSeconds, totalSecondsToRun);
            
            assertNotNull(result);
            // We can't easily test the actual result since bitwise_not is static
            // but we can verify the flow
            
            verify(getImage).getMatsFromScreen(region, intervalSeconds, totalSecondsToRun);
            verify(matOps3d, atLeastOnce()).cOmpare(any(Mat.class), any(Mat.class), eq(CMP_NE));
        }
    }
    
    @Nested
    @DisplayName("Large Collections")
    class LargeCollections {
        
        @Test
        @DisplayName("Should handle large image collections")
        void shouldHandleLargeImageCollections() {
            MatVector largeVector = new MatVector();
            for (int i = 0; i < 10; i++) {
                largeVector.push_back(new Mat(100, 100, CV_8UC3, new Scalar(i * 20, i * 20, i * 20, 0)));
            }
            
            Mat comparisonResult = new Mat(100, 100, CV_8UC3, new Scalar(255, 255, 255, 0));
            
            when(matOps3d.cOmpare(any(Mat.class), any(Mat.class), eq(CMP_NE)))
                .thenReturn(comparisonResult);
            when(matOps3d.bItwise_or(any(Mat.class), any(Mat.class)))
                .thenReturn(comparisonResult);
            
            Mat result = dynamicPixelFinder.getDynamicPixelMask(largeVector);
            
            assertNotNull(result);
            assertFalse(result.empty());
            
            // Should compare first image with all others
            verify(matOps3d, times(9)).cOmpare(any(Mat.class), any(Mat.class), eq(CMP_NE));
            
            // Clean up
            for (int i = 0; i < largeVector.size(); i++) {
                largeVector.get(i).release();
            }
        }
    }
    
    @Nested
    @DisplayName("Interface Implementation")
    class InterfaceImplementation {
        
        @Test
        @DisplayName("Should implement FindDynamicPixels interface")
        void shouldImplementFindDynamicPixelsInterface() {
            assertTrue(dynamicPixelFinder instanceof FindDynamicPixels);
        }
        
        @Test
        @DisplayName("Should provide getDynamicPixelMask method from interface")
        void shouldProvideDynamicPixelMaskMethod() {
            MatVector matVector = new MatVector(testMat1);
            Mat result = dynamicPixelFinder.getDynamicPixelMask(matVector);
            assertNotNull(result);
        }
        
        @Test
        @DisplayName("Should provide getFixedPixelMask method from interface")
        void shouldProvideFixedPixelMaskMethod() {
            MatVector matVector = new MatVector(testMat1);
            // For single image, getDynamicPixelMask returns empty Mat
            // and bItwise_not on empty should also return empty
            when(matOps3d.bItwise_not(any(Mat.class))).thenReturn(new Mat());
            
            Mat result = dynamicPixelFinder.getFixedPixelMask(matVector);
            assertNotNull(result);
        }
    }
}