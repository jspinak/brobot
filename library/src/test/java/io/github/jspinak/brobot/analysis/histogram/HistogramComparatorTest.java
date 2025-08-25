package io.github.jspinak.brobot.analysis.histogram;

import io.github.jspinak.brobot.model.element.Region;
import io.github.jspinak.brobot.model.match.Match;
import io.github.jspinak.brobot.model.state.StateImage;
import io.github.jspinak.brobot.test.BrobotTestBase;
import org.bytedeco.opencv.opencv_core.Mat;
import org.junit.jupiter.api.*;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.ArrayList;
import java.util.List;

import static org.bytedeco.opencv.global.opencv_core.*;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;
import static org.mockito.Mockito.lenient;

@ExtendWith(MockitoExtension.class)
@DisplayName("HistogramComparator Tests")
public class HistogramComparatorTest extends BrobotTestBase {

    @Mock
    private HistogramExtractor histogramExtractor;
    
    @Mock
    private HistogramRegions histRegions1;
    
    @Mock
    private HistogramRegions histRegions2;
    
    @Mock
    private HistogramRegion topLeft1, topRight1, bottomLeft1, bottomRight1, ellipse1;
    
    @Mock
    private HistogramRegion topLeft2, topRight2, bottomLeft2, bottomRight2, ellipse2;
    
    @InjectMocks
    private HistogramComparator histogramComparator;
    
    private Mat testHistogram;
    private Mat indexedColumn;
    
    @BeforeEach
    public void setUp() {
        super.setupTest();
        
        // Create test histogram with 256 bins (1 column, 256 rows)
        testHistogram = new Mat(256, 1, CV_32F);
        for (int i = 0; i < 256; i++) {
            testHistogram.ptr(i, 0).putFloat(i / 256.0f);
        }
        
        // Create indexed column
        indexedColumn = new Mat(256, 1, CV_32F);
        for (int i = 0; i < 256; i++) {
            indexedColumn.ptr(i, 0).putFloat((float)i);
        }
    }
    
    @AfterEach
    public void tearDown() {
        if (testHistogram != null && !testHistogram.isNull()) {
            testHistogram.release();
        }
        if (indexedColumn != null && !indexedColumn.isNull()) {
            indexedColumn.release();
        }
    }
    
    @Test
    @DisplayName("Should compare histogram regions and return similarity score")
    void shouldCompareHistogramRegions() {
        // Setup mock histogram regions
        when(histRegions1.getTopLeft()).thenReturn(topLeft1);
        when(histRegions1.getTopRight()).thenReturn(topRight1);
        when(histRegions1.getBottomLeft()).thenReturn(bottomLeft1);
        when(histRegions1.getBottomRight()).thenReturn(bottomRight1);
        when(histRegions1.getEllipse()).thenReturn(ellipse1);
        
        when(histRegions2.getTopLeft()).thenReturn(topLeft2);
        when(histRegions2.getTopRight()).thenReturn(topRight2);
        when(histRegions2.getBottomLeft()).thenReturn(bottomLeft2);
        when(histRegions2.getBottomRight()).thenReturn(bottomRight2);
        when(histRegions2.getEllipse()).thenReturn(ellipse2);
        
        // Return test histograms
        when(topLeft1.getHistogram()).thenReturn(testHistogram);
        when(topRight1.getHistogram()).thenReturn(testHistogram);
        when(bottomLeft1.getHistogram()).thenReturn(testHistogram);
        when(bottomRight1.getHistogram()).thenReturn(testHistogram);
        when(ellipse1.getHistogram()).thenReturn(testHistogram);
        
        when(topLeft2.getHistogram()).thenReturn(testHistogram);
        when(topRight2.getHistogram()).thenReturn(testHistogram);
        when(bottomLeft2.getHistogram()).thenReturn(testHistogram);
        when(bottomRight2.getHistogram()).thenReturn(testHistogram);
        when(ellipse2.getHistogram()).thenReturn(testHistogram);
        
        double result = histogramComparator.compare(histRegions1, histRegions2, indexedColumn);
        
        // EMD of identical histograms should be 0
        assertEquals(0.0, result, 0.01);
    }
    
    @Test
    @DisplayName("Should compute different EMD for different histograms")
    void shouldComputeDifferentEMD() {
        // Create different histograms
        Mat histogram1 = new Mat(256, 1, CV_32F);
        Mat histogram2 = new Mat(256, 1, CV_32F);
        
        try {
            // Fill with different distributions
            for (int i = 0; i < 256; i++) {
                histogram1.ptr(i, 0).putFloat(i < 128 ? 1.0f : 0.0f);
                histogram2.ptr(i, 0).putFloat(i >= 128 ? 1.0f : 0.0f);
            }
            
            // Setup mocks
            when(histRegions1.getTopLeft()).thenReturn(topLeft1);
            when(histRegions1.getTopRight()).thenReturn(topRight1);
            when(histRegions1.getBottomLeft()).thenReturn(bottomLeft1);
            when(histRegions1.getBottomRight()).thenReturn(bottomRight1);
            when(histRegions1.getEllipse()).thenReturn(ellipse1);
            
            when(histRegions2.getTopLeft()).thenReturn(topLeft2);
            when(histRegions2.getTopRight()).thenReturn(topRight2);
            when(histRegions2.getBottomLeft()).thenReturn(bottomLeft2);
            when(histRegions2.getBottomRight()).thenReturn(bottomRight2);
            when(histRegions2.getEllipse()).thenReturn(ellipse2);
            
            when(topLeft1.getHistogram()).thenReturn(histogram1);
            when(topRight1.getHistogram()).thenReturn(histogram1);
            when(bottomLeft1.getHistogram()).thenReturn(histogram1);
            when(bottomRight1.getHistogram()).thenReturn(histogram1);
            when(ellipse1.getHistogram()).thenReturn(histogram1);
            
            when(topLeft2.getHistogram()).thenReturn(histogram2);
            when(topRight2.getHistogram()).thenReturn(histogram2);
            when(bottomLeft2.getHistogram()).thenReturn(histogram2);
            when(bottomRight2.getHistogram()).thenReturn(histogram2);
            when(ellipse2.getHistogram()).thenReturn(histogram2);
            
            double result = histogramComparator.compare(histRegions1, histRegions2, indexedColumn);
            
            // Different histograms should have non-zero EMD
            assertTrue(result > 0.0);
        } finally {
            histogram1.release();
            histogram2.release();
        }
    }
    
    @Test
    @DisplayName("Should compare all regions and return matches")
    void shouldCompareAllRegions() {
        StateImage stateImage = mock(StateImage.class);
        Mat sceneHSV = new Mat(300, 300, CV_8UC3);
        List<Region> regions = new ArrayList<>();
        
        Region region1 = mock(Region.class);
        Region region2 = mock(Region.class);
        
        when(region1.getJavaCVRect()).thenReturn(new org.bytedeco.opencv.opencv_core.Rect(0, 0, 100, 100));
        when(region2.getJavaCVRect()).thenReturn(new org.bytedeco.opencv.opencv_core.Rect(100, 100, 100, 100));
        
        regions.add(region1);
        regions.add(region2);
        
        when(histogramExtractor.getHistogramsHSV(stateImage)).thenReturn(histRegions1);
        when(histogramExtractor.getHistogramFromRegion(any(Mat.class))).thenReturn(histRegions2);
        when(histogramExtractor.getHueBins()).thenReturn(256);
        
        setupHistogramMocks();
        
        // Add combined histogram mock
        HistogramRegion combined = mock(HistogramRegion.class);
        when(histRegions2.getCombined()).thenReturn(combined);
        when(combined.getHistogram()).thenReturn(testHistogram);
        
        try {
            List<Match> matches = histogramComparator.compareAll(stateImage, regions, sceneHSV);
            
            assertNotNull(matches);
            assertEquals(2, matches.size());
        } finally {
            sceneHSV.release();
        }
    }
    
    @Test
    @DisplayName("Should handle empty region list")
    void shouldHandleEmptyRegionList() {
        StateImage stateImage = mock(StateImage.class);
        Mat sceneHSV = new Mat(300, 300, CV_8UC3);
        List<Region> regions = new ArrayList<>();
        
        when(histogramExtractor.getHistogramsHSV(stateImage)).thenReturn(histRegions1);
        when(histogramExtractor.getHueBins()).thenReturn(256);
        
        try {
            List<Match> matches = histogramComparator.compareAll(stateImage, regions, sceneHSV);
            
            assertNotNull(matches);
            assertTrue(matches.isEmpty());
        } finally {
            sceneHSV.release();
        }
    }
    
    @Test
    @DisplayName("Should process multiple regions")
    void shouldProcessMultipleRegions() {
        StateImage stateImage = mock(StateImage.class);
        Mat sceneHSV = new Mat(500, 500, CV_8UC3);
        List<Region> regions = new ArrayList<>();
        
        // Add 5 regions
        for (int i = 0; i < 5; i++) {
            Region region = mock(Region.class);
            when(region.getJavaCVRect()).thenReturn(new org.bytedeco.opencv.opencv_core.Rect(i*10, i*10, 100, 100));
            regions.add(region);
        }
        
        when(histogramExtractor.getHistogramsHSV(stateImage)).thenReturn(histRegions1);
        when(histogramExtractor.getHistogramFromRegion(any(Mat.class))).thenReturn(histRegions2);
        when(histogramExtractor.getHueBins()).thenReturn(256);
        
        setupHistogramMocks();
        
        // Add combined histogram mock
        HistogramRegion combined = mock(HistogramRegion.class);
        when(histRegions2.getCombined()).thenReturn(combined);
        when(combined.getHistogram()).thenReturn(testHistogram);
        
        try {
            List<Match> matches = histogramComparator.compareAll(stateImage, regions, sceneHSV);
            
            assertNotNull(matches);
            assertEquals(5, matches.size());
        } finally {
            sceneHSV.release();
        }
    }
    
    @Test
    @DisplayName("Should sort matches by score")
    void shouldSortMatchesByScore() {
        StateImage stateImage = mock(StateImage.class);
        Mat sceneHSV = new Mat(300, 300, CV_8UC3);
        List<Region> regions = new ArrayList<>();
        
        Region region1 = mock(Region.class);
        Region region2 = mock(Region.class);
        Region region3 = mock(Region.class);
        
        when(region1.getJavaCVRect()).thenReturn(new org.bytedeco.opencv.opencv_core.Rect(0, 0, 100, 100));
        when(region2.getJavaCVRect()).thenReturn(new org.bytedeco.opencv.opencv_core.Rect(100, 0, 100, 100));
        when(region3.getJavaCVRect()).thenReturn(new org.bytedeco.opencv.opencv_core.Rect(200, 0, 100, 100));
        
        regions.add(region1);
        regions.add(region2);
        regions.add(region3);
        
        when(histogramExtractor.getHistogramsHSV(stateImage)).thenReturn(histRegions1);
        when(histogramExtractor.getHistogramFromRegion(any(Mat.class))).thenReturn(histRegions2);
        when(histogramExtractor.getHueBins()).thenReturn(256);
        
        setupHistogramMocks();
        
        // Add combined histogram mock
        HistogramRegion combined = mock(HistogramRegion.class);
        when(histRegions2.getCombined()).thenReturn(combined);
        when(combined.getHistogram()).thenReturn(testHistogram);
        
        try {
            List<Match> matches = histogramComparator.compareAll(stateImage, regions, sceneHSV);
            
            assertNotNull(matches);
            assertEquals(3, matches.size());
            // Verify sorted order (best matches first - lowest score)
            for (int i = 1; i < matches.size(); i++) {
                assertTrue(matches.get(i-1).getScore() <= matches.get(i).getScore());
            }
        } finally {
            sceneHSV.release();
        }
    }
    
    private void setupHistogramMocks() {
        when(histRegions1.getTopLeft()).thenReturn(topLeft1);
        when(histRegions1.getTopRight()).thenReturn(topRight1);
        when(histRegions1.getBottomLeft()).thenReturn(bottomLeft1);
        when(histRegions1.getBottomRight()).thenReturn(bottomRight1);
        when(histRegions1.getEllipse()).thenReturn(ellipse1);
        
        when(histRegions2.getTopLeft()).thenReturn(topLeft2);
        when(histRegions2.getTopRight()).thenReturn(topRight2);
        when(histRegions2.getBottomLeft()).thenReturn(bottomLeft2);
        when(histRegions2.getBottomRight()).thenReturn(bottomRight2);
        when(histRegions2.getEllipse()).thenReturn(ellipse2);
        
        when(topLeft1.getHistogram()).thenReturn(testHistogram);
        when(topRight1.getHistogram()).thenReturn(testHistogram);
        when(bottomLeft1.getHistogram()).thenReturn(testHistogram);
        when(bottomRight1.getHistogram()).thenReturn(testHistogram);
        when(ellipse1.getHistogram()).thenReturn(testHistogram);
        
        when(topLeft2.getHistogram()).thenReturn(testHistogram);
        when(topRight2.getHistogram()).thenReturn(testHistogram);
        when(bottomLeft2.getHistogram()).thenReturn(testHistogram);
        when(bottomRight2.getHistogram()).thenReturn(testHistogram);
        when(ellipse2.getHistogram()).thenReturn(testHistogram);
    }
    
    @Nested
    @DisplayName("Edge Cases and Error Handling")
    class EdgeCasesAndErrorHandling {
        
        @Test
        @DisplayName("Should handle null histogram regions")
        void shouldHandleNullHistogramRegions() {
            // The compare method doesn't handle null inputs, so we expect NPE
            assertThrows(NullPointerException.class, () -> {
                histogramComparator.compare(null, histRegions2, indexedColumn);
            });
        }
        
        @Test
        @DisplayName("Should handle null histograms in regions")
        void shouldHandleNullHistogramsInRegions() {
            lenient().when(histRegions1.getTopLeft()).thenReturn(topLeft1);
            lenient().when(histRegions2.getTopLeft()).thenReturn(topLeft2);
            lenient().when(topLeft1.getHistogram()).thenReturn(null);
            lenient().when(topLeft2.getHistogram()).thenReturn(testHistogram);
            
            // The compare method will fail immediately when trying to process null histogram
            assertThrows(NullPointerException.class, () -> {
                histogramComparator.compare(histRegions1, histRegions2, indexedColumn);
            });
        }
        
        @Test
        @DisplayName("Should handle empty histogram Mat")
        void shouldHandleEmptyHistogramMat() {
            // Create empty histogram with correct type but zero rows
            Mat emptyHistogram = new Mat(0, 1, CV_32F);
            
            try {
                lenient().when(histRegions1.getTopLeft()).thenReturn(topLeft1);
                lenient().when(histRegions2.getTopLeft()).thenReturn(topLeft2);
                lenient().when(topLeft1.getHistogram()).thenReturn(emptyHistogram);
                lenient().when(topLeft2.getHistogram()).thenReturn(testHistogram);
                
                // Empty histograms will cause OpenCV error
                assertThrows(RuntimeException.class, () -> {
                    histogramComparator.compare(histRegions1, histRegions2, indexedColumn);
                });
            } finally {
                emptyHistogram.release();
            }
        }
        
        @Test
        @DisplayName("Should handle mismatched histogram sizes")
        void shouldHandleMismatchedHistogramSizes() {
            Mat smallHistogram = new Mat(128, 1, CV_32F);
            Mat largeHistogram = new Mat(512, 1, CV_32F);
            
            try {
                // Fill histograms
                for (int i = 0; i < 128; i++) {
                    smallHistogram.ptr(i, 0).putFloat(i / 128.0f);
                }
                for (int i = 0; i < 512; i++) {
                    largeHistogram.ptr(i, 0).putFloat(i / 512.0f);
                }
                
                lenient().when(histRegions1.getTopLeft()).thenReturn(topLeft1);
                lenient().when(histRegions2.getTopLeft()).thenReturn(topLeft2);
                lenient().when(topLeft1.getHistogram()).thenReturn(smallHistogram);
                lenient().when(topLeft2.getHistogram()).thenReturn(largeHistogram);
                
                // Create appropriate indexed column for smaller histogram
                Mat customIndexedColumn = new Mat(128, 1, CV_32F);
                for (int i = 0; i < 128; i++) {
                    customIndexedColumn.ptr(i, 0).putFloat((float)i);
                }
                
                try {
                    // Mismatched sizes will cause OpenCV error in hconcat
                    assertThrows(RuntimeException.class, () -> {
                        histogramComparator.compare(histRegions1, histRegions2, customIndexedColumn);
                    });
                } finally {
                    customIndexedColumn.release();
                }
            } finally {
                smallHistogram.release();
                largeHistogram.release();
            }
        }
        
        @SuppressWarnings("unused")
        private void setupPartialMocks() {
            when(histRegions1.getTopRight()).thenReturn(topRight1);
            when(histRegions1.getBottomLeft()).thenReturn(bottomLeft1);
            when(histRegions1.getBottomRight()).thenReturn(bottomRight1);
            when(histRegions1.getEllipse()).thenReturn(ellipse1);
            
            when(histRegions2.getTopRight()).thenReturn(topRight2);
            when(histRegions2.getBottomLeft()).thenReturn(bottomLeft2);
            when(histRegions2.getBottomRight()).thenReturn(bottomRight2);
            when(histRegions2.getEllipse()).thenReturn(ellipse2);
            
            when(topRight1.getHistogram()).thenReturn(testHistogram);
            when(bottomLeft1.getHistogram()).thenReturn(testHistogram);
            when(bottomRight1.getHistogram()).thenReturn(testHistogram);
            when(ellipse1.getHistogram()).thenReturn(testHistogram);
            
            when(topRight2.getHistogram()).thenReturn(testHistogram);
            when(bottomLeft2.getHistogram()).thenReturn(testHistogram);
            when(bottomRight2.getHistogram()).thenReturn(testHistogram);
            when(ellipse2.getHistogram()).thenReturn(testHistogram);
        }
    }
    
    @Nested
    @DisplayName("Performance Considerations")
    class PerformanceConsiderations {
        
        @Test
        @DisplayName("Should handle large region lists efficiently")
        void shouldHandleLargeRegionListsEfficiently() {
            StateImage stateImage = mock(StateImage.class);
            // Create and initialize the scene Mat with actual data
            Mat sceneHSV = new Mat(1000, 1000, CV_8UC3);
            // Initialize with some data to avoid issues
            sceneHSV.ptr().put(new byte[1000 * 1000 * 3]);
            
            List<Region> regions = new ArrayList<>();
            
            // Create 100 regions within bounds
            for (int i = 0; i < 100; i++) {
                Region region = mock(Region.class);
                // Ensure regions stay within the 1000x1000 scene bounds
                int x = (i % 10) * 90;  // Max x = 810, with width 50 = 860 < 1000
                int y = (i / 10) * 90;  // Max y = 810, with height 50 = 860 < 1000
                when(region.getJavaCVRect()).thenReturn(
                    new org.bytedeco.opencv.opencv_core.Rect(x, y, 50, 50));
                regions.add(region);
            }
            
            when(histogramExtractor.getHistogramsHSV(stateImage)).thenReturn(histRegions1);
            when(histogramExtractor.getHistogramFromRegion(any(Mat.class))).thenReturn(histRegions2);
            when(histogramExtractor.getHueBins()).thenReturn(256);
            
            setupHistogramMocks();
            
            HistogramRegion combined = mock(HistogramRegion.class);
            when(histRegions2.getCombined()).thenReturn(combined);
            when(combined.getHistogram()).thenReturn(testHistogram);
            
            try {
                long startTime = System.currentTimeMillis();
                List<Match> matches = histogramComparator.compareAll(stateImage, regions, sceneHSV);
                long endTime = System.currentTimeMillis();
                
                assertNotNull(matches);
                assertEquals(100, matches.size());
                
                // Should complete in reasonable time (< 10 seconds for 100 regions)
                assertTrue((endTime - startTime) < 10000, 
                    "Processing 100 regions took too long: " + (endTime - startTime) + "ms");
            } finally {
                sceneHSV.release();
            }
        }
    }
    
}