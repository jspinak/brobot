package io.github.jspinak.brobot.analysis.histogram;

import io.github.jspinak.brobot.test.BrobotTestBase;
import org.bytedeco.opencv.opencv_core.Mat;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.AfterEach;

import java.util.List;

import static org.bytedeco.opencv.global.opencv_core.*;
import static org.junit.jupiter.api.Assertions.*;

/**
 * Comprehensive test suite for HistogramRegion.
 * Tests management of masks and histograms for image regions.
 */
@DisplayName("HistogramRegion Tests")
public class HistogramRegionTest extends BrobotTestBase {
    
    private HistogramRegion histogramRegion;
    private Mat testMask1;
    private Mat testMask2;
    private Mat testHistogram1;
    private Mat testHistogram2;
    private Mat combinedHistogram;
    
    @BeforeEach
    @Override
    public void setupTest() {
        super.setupTest();
        histogramRegion = new HistogramRegion();
        
        // Create test masks (binary images)
        testMask1 = new Mat(100, 100, CV_8UC1);
        testMask1.ptr().put(new byte[10000]); // All zeros initially
        // Set some pixels to white (255) to create a mask
        for (int i = 0; i < 50; i++) {
            for (int j = 0; j < 50; j++) {
                testMask1.ptr(i, j).put((byte)255);
            }
        }
        
        testMask2 = new Mat(100, 100, CV_8UC1);
        testMask2.ptr().put(new byte[10000]);
        // Different mask pattern
        for (int i = 50; i < 100; i++) {
            for (int j = 50; j < 100; j++) {
                testMask2.ptr(i, j).put((byte)255);
            }
        }
        
        // Create test histograms
        testHistogram1 = new Mat(256, 1, CV_32F);
        for (int i = 0; i < 256; i++) {
            testHistogram1.ptr(i, 0).putFloat((float)i / 256.0f);
        }
        
        testHistogram2 = new Mat(256, 1, CV_32F);
        for (int i = 0; i < 256; i++) {
            testHistogram2.ptr(i, 0).putFloat((float)(255 - i) / 256.0f);
        }
        
        // Create combined histogram
        combinedHistogram = new Mat(256, 1, CV_32F);
        for (int i = 0; i < 256; i++) {
            float combined = (testHistogram1.ptr(i, 0).getFloat() + 
                            testHistogram2.ptr(i, 0).getFloat()) / 2.0f;
            combinedHistogram.ptr(i, 0).putFloat(combined);
        }
    }
    
    @AfterEach
    public void tearDown() {
        // Release OpenCV Mat objects to prevent memory leaks
        if (testMask1 != null && !testMask1.isNull()) testMask1.release();
        if (testMask2 != null && !testMask2.isNull()) testMask2.release();
        if (testHistogram1 != null && !testHistogram1.isNull()) testHistogram1.release();
        if (testHistogram2 != null && !testHistogram2.isNull()) testHistogram2.release();
        if (combinedHistogram != null && !combinedHistogram.isNull()) combinedHistogram.release();
    }
    
    @Nested
    @DisplayName("Constructor and Initialization")
    class ConstructorTests {
        
        @Test
        @DisplayName("Should create empty histogram region")
        void shouldCreateEmptyHistogramRegion() {
            HistogramRegion region = new HistogramRegion();
            
            assertNotNull(region);
            assertNotNull(region.getMasks());
            assertNotNull(region.getHistograms());
            assertTrue(region.getMasks().isEmpty());
            assertTrue(region.getHistograms().isEmpty());
            assertNull(region.getHistogram());
        }
        
        @Test
        @DisplayName("Should initialize with empty lists")
        void shouldInitializeWithEmptyLists() {
            assertEquals(0, histogramRegion.getMasks().size());
            assertEquals(0, histogramRegion.getHistograms().size());
        }
    }
    
    @Nested
    @DisplayName("Mask Management")
    class MaskManagement {
        
        @Test
        @DisplayName("Should add single mask")
        void shouldAddSingleMask() {
            histogramRegion.getMasks().add(testMask1);
            
            assertEquals(1, histogramRegion.getMasks().size());
            assertEquals(testMask1, histogramRegion.getMasks().get(0));
        }
        
        @Test
        @DisplayName("Should add multiple masks")
        void shouldAddMultipleMasks() {
            histogramRegion.getMasks().add(testMask1);
            histogramRegion.getMasks().add(testMask2);
            
            assertEquals(2, histogramRegion.getMasks().size());
            assertEquals(testMask1, histogramRegion.getMasks().get(0));
            assertEquals(testMask2, histogramRegion.getMasks().get(1));
        }
        
        @Test
        @DisplayName("Should set mask list")
        void shouldSetMaskList() {
            List<Mat> masks = List.of(testMask1, testMask2);
            histogramRegion.setMasks(masks);
            
            assertEquals(2, histogramRegion.getMasks().size());
            assertTrue(histogramRegion.getMasks().contains(testMask1));
            assertTrue(histogramRegion.getMasks().contains(testMask2));
        }
        
        @Test
        @DisplayName("Should clear masks")
        void shouldClearMasks() {
            histogramRegion.getMasks().add(testMask1);
            histogramRegion.getMasks().add(testMask2);
            assertEquals(2, histogramRegion.getMasks().size());
            
            histogramRegion.getMasks().clear();
            assertEquals(0, histogramRegion.getMasks().size());
        }
    }
    
    @Nested
    @DisplayName("Histogram Management")
    class HistogramManagement {
        
        @Test
        @DisplayName("Should add single histogram")
        void shouldAddSingleHistogram() {
            histogramRegion.getHistograms().add(testHistogram1);
            
            assertEquals(1, histogramRegion.getHistograms().size());
            assertEquals(testHistogram1, histogramRegion.getHistograms().get(0));
        }
        
        @Test
        @DisplayName("Should add multiple histograms")
        void shouldAddMultipleHistograms() {
            histogramRegion.getHistograms().add(testHistogram1);
            histogramRegion.getHistograms().add(testHistogram2);
            
            assertEquals(2, histogramRegion.getHistograms().size());
            assertEquals(testHistogram1, histogramRegion.getHistograms().get(0));
            assertEquals(testHistogram2, histogramRegion.getHistograms().get(1));
        }
        
        @Test
        @DisplayName("Should set histogram list")
        void shouldSetHistogramList() {
            List<Mat> histograms = List.of(testHistogram1, testHistogram2);
            histogramRegion.setHistograms(histograms);
            
            assertEquals(2, histogramRegion.getHistograms().size());
            assertTrue(histogramRegion.getHistograms().contains(testHistogram1));
            assertTrue(histogramRegion.getHistograms().contains(testHistogram2));
        }
        
        @Test
        @DisplayName("Should set combined histogram")
        void shouldSetCombinedHistogram() {
            histogramRegion.setHistogram(combinedHistogram);
            
            assertNotNull(histogramRegion.getHistogram());
            assertEquals(combinedHistogram, histogramRegion.getHistogram());
        }
        
        @Test
        @DisplayName("Should replace combined histogram")
        void shouldReplaceCombinedHistogram() {
            histogramRegion.setHistogram(testHistogram1);
            assertEquals(testHistogram1, histogramRegion.getHistogram());
            
            histogramRegion.setHistogram(combinedHistogram);
            assertEquals(combinedHistogram, histogramRegion.getHistogram());
        }
    }
    
    @Nested
    @DisplayName("Complete Region Setup")
    class CompleteRegionSetup {
        
        @Test
        @DisplayName("Should setup complete region with masks and histograms")
        void shouldSetupCompleteRegion() {
            // Add masks
            histogramRegion.getMasks().add(testMask1);
            histogramRegion.getMasks().add(testMask2);
            
            // Add individual histograms
            histogramRegion.getHistograms().add(testHistogram1);
            histogramRegion.getHistograms().add(testHistogram2);
            
            // Set combined histogram
            histogramRegion.setHistogram(combinedHistogram);
            
            // Verify complete setup
            assertEquals(2, histogramRegion.getMasks().size());
            assertEquals(2, histogramRegion.getHistograms().size());
            assertNotNull(histogramRegion.getHistogram());
            
            // Verify data integrity
            assertEquals(testMask1, histogramRegion.getMasks().get(0));
            assertEquals(testMask2, histogramRegion.getMasks().get(1));
            assertEquals(testHistogram1, histogramRegion.getHistograms().get(0));
            assertEquals(testHistogram2, histogramRegion.getHistograms().get(1));
            assertEquals(combinedHistogram, histogramRegion.getHistogram());
        }
        
        @Test
        @DisplayName("Should maintain consistency between masks and histograms count")
        void shouldMaintainConsistency() {
            // In a typical use case, masks and histograms should have same count
            histogramRegion.getMasks().add(testMask1);
            histogramRegion.getMasks().add(testMask2);
            histogramRegion.getHistograms().add(testHistogram1);
            histogramRegion.getHistograms().add(testHistogram2);
            
            assertEquals(histogramRegion.getMasks().size(), 
                        histogramRegion.getHistograms().size(),
                        "Should have same number of masks and histograms");
        }
    }
    
    @Nested
    @DisplayName("Edge Cases")
    class EdgeCases {
        
        @Test
        @DisplayName("Should handle null mask addition")
        void shouldHandleNullMask() {
            histogramRegion.getMasks().add(null);
            
            assertEquals(1, histogramRegion.getMasks().size());
            assertNull(histogramRegion.getMasks().get(0));
        }
        
        @Test
        @DisplayName("Should handle null histogram addition")
        void shouldHandleNullHistogram() {
            histogramRegion.getHistograms().add(null);
            
            assertEquals(1, histogramRegion.getHistograms().size());
            assertNull(histogramRegion.getHistograms().get(0));
        }
        
        @Test
        @DisplayName("Should handle null combined histogram")
        void shouldHandleNullCombinedHistogram() {
            histogramRegion.setHistogram(null);
            
            assertNull(histogramRegion.getHistogram());
        }
        
        @Test
        @DisplayName("Should handle empty Mat objects")
        void shouldHandleEmptyMats() {
            Mat emptyMat = new Mat();
            
            histogramRegion.getMasks().add(emptyMat);
            histogramRegion.getHistograms().add(emptyMat);
            histogramRegion.setHistogram(emptyMat);
            
            assertEquals(1, histogramRegion.getMasks().size());
            assertEquals(1, histogramRegion.getHistograms().size());
            assertTrue(histogramRegion.getMasks().get(0).empty());
            assertTrue(histogramRegion.getHistograms().get(0).empty());
            assertTrue(histogramRegion.getHistogram().empty());
        }
    }
    
    @Nested
    @DisplayName("Use Cases")
    class UseCases {
        
        @Test
        @DisplayName("Should represent top-left corner region")
        void shouldRepresentTopLeftCorner() {
            // Create mask for top-left corner (25% of image)
            Mat topLeftMask = new Mat(100, 100, CV_8UC1, new opencv_core.Scalar(0));
            for (int i = 0; i < 50; i++) {
                for (int j = 0; j < 50; j++) {
                    topLeftMask.ptr(i, j).put((byte)255);
                }
            }
            
            try {
                histogramRegion.getMasks().add(topLeftMask);
                
                // Verify mask represents top-left region
                Mat mask = histogramRegion.getMasks().get(0);
                // Check top-left is white (255)
                assertEquals((byte)255, mask.ptr(25, 25).get());
                // Check bottom-right is black (0)
                assertEquals((byte)0, mask.ptr(75, 75).get());
            } finally {
                topLeftMask.release();
            }
        }
        
        @Test
        @DisplayName("Should represent center ellipse region")
        void shouldRepresentCenterEllipse() {
            // Create a simple circular mask in center
            Mat ellipseMask = new Mat(100, 100, CV_8UC1, new opencv_core.Scalar(0));
            
            // Simple circle approximation
            int centerX = 50;
            int centerY = 50;
            int radius = 30;
            
            for (int i = 0; i < 100; i++) {
                for (int j = 0; j < 100; j++) {
                    int dx = i - centerX;
                    int dy = j - centerY;
                    if (dx * dx + dy * dy <= radius * radius) {
                        ellipseMask.ptr(i, j).put((byte)255);
                    }
                }
            }
            
            try {
                histogramRegion.getMasks().add(ellipseMask);
                
                // Verify center is white
                Mat mask = histogramRegion.getMasks().get(0);
                assertEquals((byte)255, mask.ptr(50, 50).get());
                // Verify corners are black
                assertEquals((byte)0, mask.ptr(0, 0).get());
                assertEquals((byte)0, mask.ptr(99, 99).get());
            } finally {
                ellipseMask.release();
            }
        }
    }
}