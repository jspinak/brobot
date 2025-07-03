package io.github.jspinak.brobot.analysis.histogram;

import io.github.jspinak.brobot.model.state.StateImage;
import io.github.jspinak.brobot.model.element.Region;
import io.github.jspinak.brobot.model.match.Match;
import org.bytedeco.opencv.opencv_core.Mat;
import org.bytedeco.opencv.opencv_core.Rect;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class HistogramComparatorTest {

    @Mock
    private HistogramExtractor histogramExtractor;
    
    @Mock
    private HistogramRegions histogramRegions1;
    
    @Mock
    private HistogramRegions histogramRegions2;
    
    @Mock
    private HistogramRegion topLeft1, topRight1, bottomLeft1, bottomRight1, ellipse1;
    
    @Mock
    private HistogramRegion topLeft2, topRight2, bottomLeft2, bottomRight2, ellipse2;
    
    private HistogramComparator histogramComparator;
    
    @BeforeEach
    void setUp() {
        histogramComparator = new HistogramComparator(histogramExtractor);
    }
    
    @Test
    void testCompare_EmptyHistograms() {
        // Setup
        Mat emptyHist = new Mat();
        Mat indexedColumn = new Mat(new int[]{0, 1, 2});
        
        when(histogramRegions1.getTopLeft()).thenReturn(topLeft1);
        when(histogramRegions1.getTopRight()).thenReturn(topRight1);
        when(histogramRegions1.getBottomLeft()).thenReturn(bottomLeft1);
        when(histogramRegions1.getBottomRight()).thenReturn(bottomRight1);
        when(histogramRegions1.getEllipse()).thenReturn(ellipse1);
        
        when(histogramRegions2.getTopLeft()).thenReturn(topLeft2);
        when(histogramRegions2.getTopRight()).thenReturn(topRight2);
        when(histogramRegions2.getBottomLeft()).thenReturn(bottomLeft2);
        when(histogramRegions2.getBottomRight()).thenReturn(bottomRight2);
        when(histogramRegions2.getEllipse()).thenReturn(ellipse2);
        
        when(topLeft1.getHistogram()).thenReturn(emptyHist);
        when(topRight1.getHistogram()).thenReturn(emptyHist);
        when(bottomLeft1.getHistogram()).thenReturn(emptyHist);
        when(bottomRight1.getHistogram()).thenReturn(emptyHist);
        when(ellipse1.getHistogram()).thenReturn(emptyHist);
        
        when(topLeft2.getHistogram()).thenReturn(emptyHist);
        when(topRight2.getHistogram()).thenReturn(emptyHist);
        when(bottomLeft2.getHistogram()).thenReturn(emptyHist);
        when(bottomRight2.getHistogram()).thenReturn(emptyHist);
        when(ellipse2.getHistogram()).thenReturn(emptyHist);
        
        // Execute
        double result = histogramComparator.compare(histogramRegions1, histogramRegions2, indexedColumn);
        
        // Verify
        assertEquals(0.0, result, 0.001);
    }
    
    @Test
    void testCompareAll_SingleRegion() {
        // Setup
        StateImage stateImage = mock(StateImage.class);
        Region region = new Region(0, 0, 100, 100);
        Mat sceneHSV = new Mat(200, 200, org.bytedeco.opencv.global.opencv_core.CV_8UC3);
        
        HistogramRegions imageHistograms = mock(HistogramRegions.class);
        HistogramRegions sceneHistograms = mock(HistogramRegions.class);
        HistogramRegion combined = mock(HistogramRegion.class);
        Mat combinedHist = new Mat();
        
        when(histogramExtractor.getHistogramsHSV(stateImage)).thenReturn(imageHistograms);
        when(histogramExtractor.getHueBins()).thenReturn(90);
        when(histogramExtractor.getHistogramFromRegion(any(Mat.class))).thenReturn(sceneHistograms);
        when(sceneHistograms.getCombined()).thenReturn(combined);
        when(combined.getHistogram()).thenReturn(combinedHist);
        
        // Mock histogram regions for comparison
        setupMockHistogramRegions(imageHistograms);
        setupMockHistogramRegions(sceneHistograms);
        
        List<Region> regions = Arrays.asList(region);
        
        // Execute
        List<Match> matches = histogramComparator.compareAll(stateImage, regions, sceneHSV);
        
        // Verify
        assertNotNull(matches);
        assertEquals(1, matches.size());
        Match match = matches.get(0);
        assertEquals(region, match.getRegion());
        assertEquals(stateImage, match.getStateObjectData());
        assertNotNull(match.getHistogram());
        assertTrue(match.getScore() >= 0);
        
        verify(histogramExtractor).getHistogramsHSV(stateImage);
        verify(histogramExtractor).getHistogramFromRegion(any(Mat.class));
    }
    
    @Test
    void testCompareAll_MultipleRegions_SortedByScore() {
        // Setup
        StateImage stateImage = mock(StateImage.class);
        Region region1 = new Region(0, 0, 50, 50);
        Region region2 = new Region(50, 50, 50, 50);
        Region region3 = new Region(100, 100, 50, 50);
        Mat sceneHSV = new Mat(200, 200, org.bytedeco.opencv.global.opencv_core.CV_8UC3);
        
        HistogramRegions imageHistograms = mock(HistogramRegions.class);
        HistogramRegions sceneHistograms1 = mock(HistogramRegions.class);
        HistogramRegions sceneHistograms2 = mock(HistogramRegions.class);
        HistogramRegions sceneHistograms3 = mock(HistogramRegions.class);
        
        HistogramRegion combined1 = mock(HistogramRegion.class);
        HistogramRegion combined2 = mock(HistogramRegion.class);
        HistogramRegion combined3 = mock(HistogramRegion.class);
        
        when(histogramExtractor.getHistogramsHSV(stateImage)).thenReturn(imageHistograms);
        when(histogramExtractor.getHueBins()).thenReturn(90);
        
        // Set up different scores for each region
        when(histogramExtractor.getHistogramFromRegion(any(Mat.class)))
                .thenReturn(sceneHistograms1, sceneHistograms2, sceneHistograms3);
        
        when(sceneHistograms1.getCombined()).thenReturn(combined1);
        when(sceneHistograms2.getCombined()).thenReturn(combined2);
        when(sceneHistograms3.getCombined()).thenReturn(combined3);
        
        when(combined1.getHistogram()).thenReturn(new Mat());
        when(combined2.getHistogram()).thenReturn(new Mat());
        when(combined3.getHistogram()).thenReturn(new Mat());
        
        setupMockHistogramRegions(imageHistograms);
        setupMockHistogramRegions(sceneHistograms1);
        setupMockHistogramRegions(sceneHistograms2);
        setupMockHistogramRegions(sceneHistograms3);
        
        List<Region> regions = Arrays.asList(region1, region2, region3);
        
        // Execute
        List<Match> matches = histogramComparator.compareAll(stateImage, regions, sceneHSV);
        
        // Verify
        assertNotNull(matches);
        assertEquals(3, matches.size());
        
        // Verify sorted by score (ascending)
        for (int i = 1; i < matches.size(); i++) {
            assertTrue(matches.get(i - 1).getScore() <= matches.get(i).getScore());
        }
        
        verify(histogramExtractor, times(3)).getHistogramFromRegion(any(Mat.class));
    }
    
    @Test
    void testCompareAll_EmptyRegionList() {
        // Setup
        StateImage stateImage = mock(StateImage.class);
        Mat sceneHSV = new Mat(200, 200, org.bytedeco.opencv.global.opencv_core.CV_8UC3);
        List<Region> regions = new ArrayList<>();
        
        HistogramRegions imageHistograms = mock(HistogramRegions.class);
        when(histogramExtractor.getHistogramsHSV(stateImage)).thenReturn(imageHistograms);
        when(histogramExtractor.getHueBins()).thenReturn(90);
        
        // Execute
        List<Match> matches = histogramComparator.compareAll(stateImage, regions, sceneHSV);
        
        // Verify
        assertNotNull(matches);
        assertTrue(matches.isEmpty());
        verify(histogramExtractor).getHistogramsHSV(stateImage);
        verify(histogramExtractor, never()).getHistogramFromRegion(any(Mat.class));
    }
    
    @Test
    void testCompareAll_MatchPropertiesSet() {
        // Setup
        StateImage stateImage = mock(StateImage.class);
        Region region = new Region(10, 20, 30, 40);
        Mat sceneHSV = new Mat(200, 200, org.bytedeco.opencv.global.opencv_core.CV_8UC3);
        
        HistogramRegions imageHistograms = mock(HistogramRegions.class);
        HistogramRegions sceneHistograms = mock(HistogramRegions.class);
        HistogramRegion combined = mock(HistogramRegion.class);
        Mat combinedHist = new Mat();
        
        when(histogramExtractor.getHistogramsHSV(stateImage)).thenReturn(imageHistograms);
        when(histogramExtractor.getHueBins()).thenReturn(90);
        when(histogramExtractor.getHistogramFromRegion(any(Mat.class))).thenReturn(sceneHistograms);
        when(sceneHistograms.getCombined()).thenReturn(combined);
        when(combined.getHistogram()).thenReturn(combinedHist);
        
        setupMockHistogramRegions(imageHistograms);
        setupMockHistogramRegions(sceneHistograms);
        
        List<Region> regions = Arrays.asList(region);
        
        // Execute
        List<Match> matches = histogramComparator.compareAll(stateImage, regions, sceneHSV);
        
        // Verify
        assertEquals(1, matches.size());
        Match match = matches.get(0);
        
        // Verify all match properties are set correctly
        assertEquals(region, match.getRegion());
        assertEquals(stateImage, match.getStateObjectData());
        assertEquals(combinedHist, match.getHistogram());
        assertNotNull(match.getScore());
    }
    
    private void setupMockHistogramRegions(HistogramRegions histogramRegions) {
        HistogramRegion mockTopLeft = mock(HistogramRegion.class);
        HistogramRegion mockTopRight = mock(HistogramRegion.class);
        HistogramRegion mockBottomLeft = mock(HistogramRegion.class);
        HistogramRegion mockBottomRight = mock(HistogramRegion.class);
        HistogramRegion mockEllipse = mock(HistogramRegion.class);
        
        when(histogramRegions.getTopLeft()).thenReturn(mockTopLeft);
        when(histogramRegions.getTopRight()).thenReturn(mockTopRight);
        when(histogramRegions.getBottomLeft()).thenReturn(mockBottomLeft);
        when(histogramRegions.getBottomRight()).thenReturn(mockBottomRight);
        when(histogramRegions.getEllipse()).thenReturn(mockEllipse);
        
        when(mockTopLeft.getHistogram()).thenReturn(new Mat());
        when(mockTopRight.getHistogram()).thenReturn(new Mat());
        when(mockBottomLeft.getHistogram()).thenReturn(new Mat());
        when(mockBottomRight.getHistogram()).thenReturn(new Mat());
        when(mockEllipse.getHistogram()).thenReturn(new Mat());
    }
}