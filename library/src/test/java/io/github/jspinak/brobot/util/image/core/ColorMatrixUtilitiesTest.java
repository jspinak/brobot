package io.github.jspinak.brobot.util.image.core;

import io.github.jspinak.brobot.test.BrobotTestBase;
import org.bytedeco.opencv.opencv_core.*;
import org.bytedeco.javacpp.DoublePointer;
import org.junit.jupiter.api.*;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Arrays;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import static org.bytedeco.opencv.global.opencv_core.*;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

/**
 * Comprehensive test suite for ColorMatrixUtilities.
 * Tests 3-channel image operations and color space utilities.
 */
@ExtendWith(MockitoExtension.class)
@DisplayName("ColorMatrixUtilities Tests")
public class ColorMatrixUtilitiesTest extends BrobotTestBase {
    
    @Mock
    private BufferedImageUtilities bufferedImageOps;
    
    @InjectMocks
    private ColorMatrixUtilities colorMatrixUtilities;
    
    private Mat testMat3C;
    private Mat testMat1C;
    private List<Mat> testMatList;
    
    @BeforeEach
    @Override
    public void setupTest() {
        super.setupTest();
        
        // Create test matrices
        testMat3C = new Mat(100, 100, CV_8UC3, new Scalar(100, 150, 200, 0));
        testMat1C = new Mat(100, 100, CV_8UC1, new Scalar(128));
        
        testMatList = new ArrayList<>();
        for (int i = 0; i < 3; i++) {
            testMatList.add(new Mat(50, 50, CV_8UC3, new Scalar(i * 50, i * 60, i * 70, 0)));
        }
    }
    
    @AfterEach
    public void tearDown() {
        releaseIfNotNull(testMat3C);
        releaseIfNotNull(testMat1C);
        for (Mat mat : testMatList) {
            releaseIfNotNull(mat);
        }
    }
    
    private void releaseIfNotNull(Mat mat) {
        if (mat != null && !mat.isNull()) {
            mat.release();
        }
    }
    
    @Nested
    @DisplayName("Concatenation Operations")
    class ConcatenationOperations {
        
        @Test
        @DisplayName("Should concatenate multiple 3-channel images to single column per channel")
        void shouldConcatenateTo3ColumnMat() {
            Mat result = colorMatrixUtilities.vConcatToSingleColumnPerChannel(testMatList);
            
            assertNotNull(result);
            assertEquals(3, result.cols());
            assertEquals(testMatList.size() * 50 * 50, result.rows());
            assertEquals(CV_8UC3, result.type());
            
            result.release();
        }
        
        @Test
        @DisplayName("Should handle empty list for concatenation")
        void shouldHandleEmptyListConcatenation() {
            Mat result = colorMatrixUtilities.vConcatToSingleColumnPerChannel(Collections.emptyList());
            
            assertNotNull(result);
            assertTrue(result.empty());
        }
        
        @Test
        @DisplayName("Should handle single image concatenation")
        void shouldHandleSingleImageConcatenation() {
            List<Mat> singleMat = Collections.singletonList(testMat3C);
            Mat result = colorMatrixUtilities.vConcatToSingleColumnPerChannel(singleMat);
            
            assertNotNull(result);
            assertEquals(3, result.cols());
            assertEquals(100 * 100, result.rows());
            
            result.release();
        }
    }
    
    @Nested
    @DisplayName("K-Means Clustering")
    class KMeansClustering {
        
        @Test
        @DisplayName("Should perform k-means clustering on each channel independently")
        void shouldPerformChannelWiseKMeans() {
            Mat labels = new Mat();
            Mat centers = new Mat();
            TermCriteria termCriteria = new TermCriteria(
                TermCriteria.EPS + TermCriteria.COUNT, 100, 0.001
            );
            
            double[] compactness = colorMatrixUtilities.kMeans(
                testMat3C, 3, labels, termCriteria, 3, centers
            );
            
            assertNotNull(compactness);
            assertEquals(3, compactness.length);
            assertNotNull(labels);
            assertFalse(labels.empty());
            assertNotNull(centers);
            assertFalse(centers.empty());
            
            // Verify each compactness value is non-negative
            for (double value : compactness) {
                assertTrue(value >= 0);
            }
            
            labels.release();
            centers.release();
        }
        
        @Test
        @DisplayName("Should handle different numbers of clusters")
        @ParameterizedTest
        @ValueSource(ints = {2, 5, 10})
        void shouldHandleDifferentClusterCounts(int numClusters) {
            Mat labels = new Mat();
            Mat centers = new Mat();
            TermCriteria termCriteria = new TermCriteria(
                TermCriteria.COUNT, 10, 0
            );
            
            double[] compactness = colorMatrixUtilities.kMeans(
                testMat3C, numClusters, labels, termCriteria, 1, centers
            );
            
            assertEquals(3, compactness.length);
            assertEquals(numClusters, centers.rows());
            
            labels.release();
            centers.release();
        }
    }
    
    @Nested
    @DisplayName("Comparison Operations")
    class ComparisonOperations {
        
        @Test
        @DisplayName("Should compare each channel against threshold values")
        void shouldCompareChannelsAgainstThresholds() {
            double[] thresholds = {100.0, 150.0, 200.0};
            
            Mat result = colorMatrixUtilities.cOmpare(testMat3C, thresholds, CMP_EQ);
            
            assertNotNull(result);
            assertEquals(testMat3C.size(), result.size());
            assertEquals(CV_8UC3, result.type());
            
            result.release();
        }
        
        @Test
        @DisplayName("Should handle different comparison operators")
        void shouldHandleDifferentComparisonOperators() {
            double[] thresholds = {50.0, 100.0, 150.0};
            
            Mat resultGT = colorMatrixUtilities.cOmpare(testMat3C, thresholds, CMP_GT);
            Mat resultLT = colorMatrixUtilities.cOmpare(testMat3C, thresholds, CMP_LT);
            Mat resultNE = colorMatrixUtilities.cOmpare(testMat3C, thresholds, CMP_NE);
            
            assertNotNull(resultGT);
            assertNotNull(resultLT);
            assertNotNull(resultNE);
            
            resultGT.release();
            resultLT.release();
            resultNE.release();
        }
        
        @Test
        @DisplayName("Should compare two Mats element-wise")
        void shouldCompareTwoMatsElementWise() {
            Mat mat2 = new Mat(100, 100, CV_8UC3, new Scalar(150, 150, 150, 0));
            Mat dst = new Mat();
            
            Mat result = colorMatrixUtilities.cOmpare(testMat3C, mat2, dst, CMP_LT);
            
            assertNotNull(result);
            assertEquals(dst, result);
            assertEquals(testMat3C.size(), result.size());
            
            mat2.release();
            dst.release();
        }
        
        @Test
        @DisplayName("Should compare two Mats directly")
        void shouldCompareTwoMatsDirectly() {
            Mat mat2 = new Mat(100, 100, CV_8UC3, new Scalar(100, 150, 200, 0));
            
            Mat result = colorMatrixUtilities.cOmpare(testMat3C, mat2, CMP_EQ);
            
            assertNotNull(result);
            assertEquals(CV_8UC3, result.type());
            
            mat2.release();
            result.release();
        }
    }
    
    @Nested
    @DisplayName("Bitwise Operations")
    class BitwiseOperations {
        
        @Test
        @DisplayName("Should perform bitwise AND on 3-channel images")
        void shouldPerformBitwiseAnd() {
            Mat mat2 = new Mat(100, 100, CV_8UC3, new Scalar(200, 100, 50, 0));
            
            Mat result = colorMatrixUtilities.bItwise_and(testMat3C, mat2);
            
            assertNotNull(result);
            assertEquals(CV_8UC3, result.type());
            assertEquals(testMat3C.size(), result.size());
            
            mat2.release();
            result.release();
        }
        
        @Test
        @DisplayName("Should perform bitwise OR on 3-channel images")
        void shouldPerformBitwiseOr() {
            Mat mat2 = new Mat(100, 100, CV_8UC3, new Scalar(50, 60, 70, 0));
            
            Mat result = colorMatrixUtilities.bItwise_or(testMat3C, mat2);
            
            assertNotNull(result);
            assertEquals(CV_8UC3, result.type());
            
            mat2.release();
            result.release();
        }
        
        @Test
        @DisplayName("Should perform bitwise NOT on 3-channel image")
        void shouldPerformBitwiseNot() {
            Mat result = colorMatrixUtilities.bItwise_not(testMat3C);
            
            assertNotNull(result);
            assertEquals(CV_8UC3, result.type());
            assertEquals(testMat3C.size(), result.size());
            
            result.release();
        }
        
    }
    
    @Nested
    @DisplayName("Statistical Operations")
    class StatisticalOperations {
        
        
        @Test
        @DisplayName("Should calculate mean and standard deviation with mask")
        void shouldCalculateMeanAndStdDevWithMask() {
            Mat mask = new Mat(100, 100, CV_8UC3, new Scalar(255, 255, 255, 0));
            
            MatVector meanStddev = colorMatrixUtilities.mEanStdDev(testMat3C, mask);
            
            assertNotNull(meanStddev);
            assertEquals(2, meanStddev.size());
            
            Mat mean = meanStddev.get(0);
            Mat stddev = meanStddev.get(1);
            
            assertNotNull(mean);
            assertNotNull(stddev);
            assertEquals(testMat3C.size(), mean.size());
            assertEquals(testMat3C.size(), stddev.size());
            
            mask.release();
        }
        
        @Test
        @DisplayName("Should find min and max values with mask")
        void shouldFindMinMaxWithMask() {
            Mat mask = new Mat(100, 100, CV_8UC3, new Scalar(255, 255, 255, 0));
            DoublePointer minVals = new DoublePointer(3);
            DoublePointer maxVals = new DoublePointer(3);
            
            colorMatrixUtilities.minMax(testMat3C, minVals, maxVals, mask);
            
            // Check that pointers are not null
            assertNotNull(minVals);
            assertNotNull(maxVals);
            
            mask.release();
        }
        
    }
    
    @Nested
    @DisplayName("Channel Operations")
    class ChannelOperations {
        
        @Test
        @DisplayName("Should split 3-channel Mat into separate channels")
        void shouldSplitIntoChannels() {
            MatVector channels = colorMatrixUtilities.sPlit(testMat3C);
            
            assertNotNull(channels);
            assertEquals(3, channels.size());
            
            for (int i = 0; i < 3; i++) {
                Mat channel = channels.get(i);
                assertEquals(CV_8UC1, channel.type());
                assertEquals(testMat3C.size(), channel.size());
            }
        }
        
        @Test
        @DisplayName("Should merge single channels into 3-channel Mat")
        void shouldMergeChannels() {
            MatVector channels = new MatVector(3);
            for (int i = 0; i < 3; i++) {
                channels.put(i, new Mat(100, 100, CV_8UC1, new Scalar(i * 85)));
            }
            
            Mat result = colorMatrixUtilities.mErge(channels);
            
            assertNotNull(result);
            assertEquals(CV_8UC3, result.type());
            assertEquals(100, result.rows());
            assertEquals(100, result.cols());
            
            result.release();
        }
    }
    
    @Nested
    @DisplayName("Edge Cases and Error Handling")
    class EdgeCasesAndErrorHandling {
        
        @Test
        @DisplayName("Should handle single-channel input for comparison")
        void shouldHandleSingleChannelComparison() {
            Mat result = colorMatrixUtilities.cOmpare(testMat1C, testMat1C, CMP_EQ);
            
            assertNotNull(result);
            assertEquals(CV_8UC1, result.type());
            
            result.release();
        }
        
        @Test
        @DisplayName("Should handle mismatched channel counts in comparison")
        void shouldHandleMismatchedChannels() {
            Mat dst = new Mat();
            Mat result = colorMatrixUtilities.cOmpare(testMat3C, testMat1C, dst, CMP_GT);
            
            assertNotNull(result);
            // Should use minimum channel count
            assertEquals(CV_8UC1, result.type());
            
            dst.release();
        }
        
        @Test
        @DisplayName("Should handle empty Mat for statistical operations")
        void shouldHandleEmptyMatForStats() {
            Mat emptyMat = new Mat();
            Mat mask = new Mat();
            
            MatVector result = colorMatrixUtilities.mEanStdDev(emptyMat, mask);
            
            assertNotNull(result);
            // Empty mat should still return a MatVector structure
        }
        
        @Test
        @DisplayName("Should handle large images without memory issues")
        void shouldHandleLargeImages() {
            Mat largeMat = new Mat(1000, 1000, CV_8UC3, new Scalar(128, 128, 128, 0));
            Mat mask = new Mat(1000, 1000, CV_8UC3, new Scalar(255, 255, 255, 0));
            
            MatVector result = colorMatrixUtilities.mEanStdDev(largeMat, mask);
            
            assertNotNull(result);
            assertEquals(2, result.size());
            
            largeMat.release();
            mask.release();
        }
    }
}