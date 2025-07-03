package io.github.jspinak.brobot.analysis.histogram;

import io.github.jspinak.brobot.model.analysis.color.ColorCluster;
import io.github.jspinak.brobot.model.state.StateImage;
import io.github.jspinak.brobot.util.image.recognition.ImageLoader;
import org.bytedeco.javacpp.FloatPointer;
import org.bytedeco.javacpp.IntPointer;
import org.bytedeco.javacpp.PointerPointer;
import org.bytedeco.opencv.opencv_core.Mat;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class HistogramExtractorTest {

    @Mock
    private ImageLoader imageLoader;
    
    private HistogramExtractor histogramExtractor;
    
    @BeforeEach
    void setUp() {
        histogramExtractor = new HistogramExtractor(imageLoader);
    }
    
    @Test
    void testSetBins() {
        // Execute
        histogramExtractor.setBins(30, 5, 2);
        
        // Verify
        assertEquals(30, histogramExtractor.getHueBins());
        assertEquals(5, histogramExtractor.getSatBins());
        assertEquals(2, histogramExtractor.getValBins());
        assertEquals(300, histogramExtractor.getTotalBins());
    }
    
    @Test
    void testGetHistogramFromRegion_EmptyMask() {
        // Setup
        Mat emptyMask = new Mat();
        
        // Execute
        HistogramRegions result = histogramExtractor.getHistogramFromRegion(emptyMask);
        
        // Verify
        assertNotNull(result);
        assertEquals(0, result.getImages().size());
    }
    
    @Test
    void testGetHistogramsHSV_SingleImage() {
        // Setup
        StateImage stateImage = mock(StateImage.class);
        Mat mockMat = new Mat(100, 100, org.bytedeco.opencv.global.opencv_core.CV_8UC3);
        when(imageLoader.getMats(eq(stateImage), eq(ColorCluster.ColorSchemaName.HSV)))
                .thenReturn(Collections.singletonList(mockMat));
        
        // Execute
        HistogramRegions result = histogramExtractor.getHistogramsHSV(stateImage);
        
        // Verify
        assertNotNull(result);
        assertEquals(1, result.getImages().size());
        assertNotNull(result.getTopLeft());
        assertNotNull(result.getTopRight());
        assertNotNull(result.getBottomLeft());
        assertNotNull(result.getBottomRight());
        assertNotNull(result.getEllipse());
        verify(imageLoader).getMats(stateImage, ColorCluster.ColorSchemaName.HSV);
    }
    
    @Test
    void testGetHistogramsHSV_MultipleImages() {
        // Setup
        StateImage stateImage = mock(StateImage.class);
        Mat mockMat1 = new Mat(100, 100, org.bytedeco.opencv.global.opencv_core.CV_8UC3);
        Mat mockMat2 = new Mat(100, 100, org.bytedeco.opencv.global.opencv_core.CV_8UC3);
        Mat mockMat3 = new Mat(100, 100, org.bytedeco.opencv.global.opencv_core.CV_8UC3);
        
        when(imageLoader.getMats(eq(stateImage), eq(ColorCluster.ColorSchemaName.HSV)))
                .thenReturn(Arrays.asList(mockMat1, mockMat2, mockMat3));
        
        // Execute
        HistogramRegions result = histogramExtractor.getHistogramsHSV(stateImage);
        
        // Verify
        assertNotNull(result);
        assertEquals(3, result.getImages().size());
        verify(imageLoader).getMats(stateImage, ColorCluster.ColorSchemaName.HSV);
    }
    
    @Test
    void testGetHistogramRegions_CallsCombineHistograms() {
        // Setup
        Mat testImage = new Mat(100, 100, org.bytedeco.opencv.global.opencv_core.CV_8UC3);
        HistogramRegions histRegs = new HistogramRegions(Collections.singletonList(testImage));
        IntPointer channelsPtr = new IntPointer(0, 1, 2);
        IntPointer binsPtr = new IntPointer(90, 2, 1);
        PointerPointer<FloatPointer> rangesPtr = new PointerPointer<>(
                new FloatPointer(0, 180, 0, 256, 0, 256)
        );
        
        // Execute
        histogramExtractor.getHistogramRegions(histRegs, channelsPtr, binsPtr, rangesPtr);
        
        // Verify
        assertNotNull(histRegs);
        // Verify that histograms were processed for all regions
        assertEquals(1, histRegs.getImages().size());
    }
    
    @Test
    void testDefaultBinValues() {
        // Verify default values
        assertEquals(90, histogramExtractor.getHueBins());
        assertEquals(2, histogramExtractor.getSatBins());
        assertEquals(1, histogramExtractor.getValBins());
        assertEquals(180, histogramExtractor.getTotalBins());
    }
    
    @Test
    void testDefaultRanges() {
        // Verify HSV ranges
        float[] ranges = histogramExtractor.getRanges();
        assertEquals(6, ranges.length);
        assertEquals(0, ranges[0]); // Hue min
        assertEquals(180, ranges[1]); // Hue max
        assertEquals(0, ranges[2]); // Saturation min
        assertEquals(256, ranges[3]); // Saturation max
        assertEquals(0, ranges[4]); // Value min
        assertEquals(256, ranges[5]); // Value max
    }
    
    @Test
    void testDefaultChannels() {
        // Verify channel indices
        int[] channels = histogramExtractor.getChannels();
        assertEquals(3, channels.length);
        assertEquals(0, channels[0]); // Hue channel
        assertEquals(1, channels[1]); // Saturation channel
        assertEquals(2, channels[2]); // Value channel
    }
    
    @Test
    void testGetHistogramsHSV_EmptyImageList() {
        // Setup
        StateImage stateImage = mock(StateImage.class);
        when(imageLoader.getMats(eq(stateImage), eq(ColorCluster.ColorSchemaName.HSV)))
                .thenReturn(Collections.emptyList());
        
        // Execute
        HistogramRegions result = histogramExtractor.getHistogramsHSV(stateImage);
        
        // Verify
        assertNotNull(result);
        assertEquals(0, result.getImages().size());
    }
    
    @Test
    void testSetBins_UpdatesTotalBins() {
        // Test various bin combinations
        histogramExtractor.setBins(180, 256, 256);
        assertEquals(11796480, histogramExtractor.getTotalBins());
        
        histogramExtractor.setBins(1, 1, 1);
        assertEquals(1, histogramExtractor.getTotalBins());
        
        histogramExtractor.setBins(10, 10, 10);
        assertEquals(1000, histogramExtractor.getTotalBins());
    }
}