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
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.ArrayList;
import java.util.List;

import static org.bytedeco.opencv.global.opencv_core.*;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("DynamicPixelFinder Tests")
public class DynamicPixelFinderTest extends BrobotTestBase {

    @Mock
    private ColorMatrixUtilities matOps3d;
    
    @Mock
    private ImageLoader imageLoader;
    
    @InjectMocks
    private DynamicPixelFinder dynamicPixelFinder;
    
    private Mat testMat1;
    private Mat testMat2;
    private Mat testMat3;
    
    @BeforeEach
    public void setUp() {
        super.setupTest();
        
        // Create test matrices with different values
        testMat1 = new Mat(100, 100, CV_8UC3, new Scalar(100, 100, 100, 0));
        testMat2 = new Mat(100, 100, CV_8UC3, new Scalar(150, 150, 150, 0));
        testMat3 = new Mat(100, 100, CV_8UC3, new Scalar(200, 200, 200, 0));
    }
    
    @AfterEach
    public void tearDown() {
        releaseIfNotNull(testMat1);
        releaseIfNotNull(testMat2);
        releaseIfNotNull(testMat3);
    }
    
    private void releaseIfNotNull(Mat mat) {
        if (mat != null && !mat.isNull()) {
            mat.release();
        }
    }
    
    @Test
    @DisplayName("Should return empty Mat for single image")
    void shouldReturnEmptyMatForSingleImage() {
        MatVector singleImage = new MatVector(testMat1);
        
        Mat result = dynamicPixelFinder.getDynamicPixelMask(singleImage);
        
        assertNotNull(result);
        assertTrue(result.empty());
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
        
        verify(matOps3d).cOmpare(testMat1, testMat2, CMP_NE);
        verify(matOps3d).bItwise_or(eq(comparisonResult), any(Mat.class));
    }
    
    @Test
    @DisplayName("Should combine multiple comparisons with bitwise OR")
    void shouldCombineMultipleComparisons() {
        MatVector matVector = new MatVector(testMat1, testMat2, testMat3);
        
        Mat comparison1 = new Mat(100, 100, CV_8UC3, new Scalar(255, 0, 0, 0));
        Mat comparison2 = new Mat(100, 100, CV_8UC3, new Scalar(0, 255, 0, 0));
        Mat combinedMask = new Mat(100, 100, CV_8UC3, new Scalar(255, 255, 0, 0));
        
        when(matOps3d.cOmpare(eq(testMat1), eq(testMat2), eq(CMP_NE)))
            .thenReturn(comparison1);
        when(matOps3d.cOmpare(eq(testMat1), eq(testMat3), eq(CMP_NE)))
            .thenReturn(comparison2);
        when(matOps3d.bItwise_or(any(Mat.class), any(Mat.class)))
            .thenReturn(combinedMask);
        
        Mat result = dynamicPixelFinder.getDynamicPixelMask(matVector);
        
        assertNotNull(result);
        assertEquals(combinedMask, result);
        
        verify(matOps3d, times(2)).cOmpare(any(Mat.class), any(Mat.class), eq(CMP_NE));
        verify(matOps3d, atLeast(2)).bItwise_or(any(Mat.class), any(Mat.class));
    }
    
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
        
        // Mock the bitwise_not operation for fixed pixels
        Mat result = dynamicPixelFinder.getFixedPixelMask(matVector);
        
        // Since getFixedPixelMask uses bitwise_not on the dynamic mask
        // we expect the inverse
        assertNotNull(result);
        
        verify(matOps3d).cOmpare(testMat1, testMat2, CMP_NE);
    }
    
    @Test
    @DisplayName("Should capture images from region over time")
    void shouldCaptureImagesFromRegionOverTime() {
        Region region = mock(Region.class);
        int captureCount = 3;
        int pauseMS = 100;
        
        Mat capture1 = new Mat(50, 50, CV_8UC3, new Scalar(100, 100, 100, 0));
        Mat capture2 = new Mat(50, 50, CV_8UC3, new Scalar(110, 110, 110, 0));
        Mat capture3 = new Mat(50, 50, CV_8UC3, new Scalar(120, 120, 120, 0));
        
        when(imageLoader.getMat(region))
            .thenReturn(capture1)
            .thenReturn(capture2)
            .thenReturn(capture3);
        
        MatVector result = dynamicPixelFinder.takeMats(region, captureCount, pauseMS);
        
        assertNotNull(result);
        assertEquals(captureCount, result.size());
        
        verify(imageLoader, times(captureCount)).getMat(region);
        
        // Clean up
        capture1.release();
        capture2.release();
        capture3.release();
    }
    
    @Test
    @DisplayName("Should handle null region for captures")
    void shouldHandleNullRegionForCaptures() {
        MatVector result = dynamicPixelFinder.takeMats(null, 3, 100);
        
        assertNotNull(result);
        assertTrue(result.empty() || result.size() == 0);
    }
    
    @Test
    @DisplayName("Should handle zero capture count")
    void shouldHandleZeroCaptureCount() {
        Region region = mock(Region.class);
        
        MatVector result = dynamicPixelFinder.takeMats(region, 0, 100);
        
        assertNotNull(result);
        assertTrue(result.empty());
        
        verify(imageLoader, never()).getMat(any(Region.class));
    }
    
    @Test
    @DisplayName("Should handle negative capture count")
    void shouldHandleNegativeCaptureCount() {
        Region region = mock(Region.class);
        
        MatVector result = dynamicPixelFinder.takeMats(region, -5, 100);
        
        assertNotNull(result);
        assertTrue(result.empty());
        
        verify(imageLoader, never()).getMat(any(Region.class));
    }
    
    @ParameterizedTest
    @ValueSource(ints = {0, 50, 100, 500, 1000})
    @DisplayName("Should respect pause duration between captures")
    void shouldRespectPauseDuration(int pauseMS) {
        Region region = mock(Region.class);
        Mat capture = new Mat(50, 50, CV_8UC3);
        
        when(imageLoader.getMat(region)).thenReturn(capture);
        
        long startTime = System.currentTimeMillis();
        MatVector result = dynamicPixelFinder.takeMats(region, 2, pauseMS);
        long endTime = System.currentTimeMillis();
        
        assertNotNull(result);
        assertEquals(2, result.size());
        
        // Verify that the pause was approximately respected
        // Allow some tolerance for execution time
        if (pauseMS > 0) {
            assertTrue(endTime - startTime >= pauseMS);
        }
        
        capture.release();
    }
    
    @Test
    @DisplayName("Should detect no dynamic pixels for identical images")
    void shouldDetectNoDynamicPixelsForIdenticalImages() {
        MatVector matVector = new MatVector(testMat1, testMat1, testMat1);
        Mat emptyComparison = new Mat(100, 100, CV_8UC3, new Scalar(0, 0, 0, 0));
        
        when(matOps3d.cOmpare(eq(testMat1), eq(testMat1), eq(CMP_NE)))
            .thenReturn(emptyComparison);
        when(matOps3d.bItwise_or(any(Mat.class), any(Mat.class)))
            .thenReturn(emptyComparison);
        
        Mat result = dynamicPixelFinder.getDynamicPixelMask(matVector);
        
        assertNotNull(result);
        assertEquals(emptyComparison, result);
        
        verify(matOps3d, times(2)).cOmpare(testMat1, testMat1, CMP_NE);
    }
    
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
    
    @Test
    @DisplayName("Should handle empty MatVector")
    void shouldHandleEmptyMatVector() {
        MatVector emptyVector = new MatVector();
        
        Mat result = dynamicPixelFinder.getDynamicPixelMask(emptyVector);
        
        assertNotNull(result);
        assertTrue(result.empty());
        
        verify(matOps3d, never()).cOmpare(any(Mat.class), any(Mat.class), anyInt());
    }
}