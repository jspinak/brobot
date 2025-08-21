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
        
        // Create test histogram with 256 bins
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
    
}