package io.github.jspinak.brobot.analysis.histogram;

import io.github.jspinak.brobot.model.analysis.color.ColorCluster;
import io.github.jspinak.brobot.model.state.StateImage;
import io.github.jspinak.brobot.test.BrobotTestBase;
import io.github.jspinak.brobot.util.image.recognition.ImageLoader;
import org.bytedeco.javacpp.FloatPointer;
import org.bytedeco.javacpp.IntPointer;
import org.bytedeco.javacpp.PointerPointer;
import org.bytedeco.opencv.opencv_core.Mat;
import org.bytedeco.opencv.opencv_core.Scalar;
import org.junit.jupiter.api.*;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.ArrayList;
import java.util.List;

import static org.bytedeco.opencv.global.opencv_core.*;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

/**
 * Comprehensive test suite for HistogramExtractor.
 * Tests histogram calculation and management for image regions in HSV color space.
 */
@ExtendWith(MockitoExtension.class)
@DisplayName("HistogramExtractor Tests")
public class HistogramExtractorTest extends BrobotTestBase {
    
    @Mock
    private ImageLoader imageLoader;
    
    private HistogramExtractor histogramExtractor;
    private Mat testImageHSV;
    private Mat testMask;
    private List<Mat> testImages;
    
    @BeforeEach
    @Override
    public void setupTest() {
        super.setupTest();
        histogramExtractor = new HistogramExtractor(imageLoader);
        
        // Create test HSV image (3 channels)
        testImageHSV = new Mat(200, 300, CV_8UC3);
        // Fill with gradient values
        for (int i = 0; i < 200; i++) {
            for (int j = 0; j < 300; j++) {
                testImageHSV.ptr(i, j).put(
                    (byte)(j * 180 / 300),  // Hue: 0-180
                    (byte)(i * 255 / 200),  // Saturation: 0-255
                    (byte)128               // Value: constant
                );
            }
        }
        
        // Create test mask
        testMask = new Mat(200, 300, CV_8UC1);
        testMask.setTo(new Scalar(255)); // All white (full mask)
        
        testImages = new ArrayList<>();
        testImages.add(testImageHSV);
    }
    
    @AfterEach
    public void tearDown() {
        if (testImageHSV != null && !testImageHSV.isNull()) testImageHSV.release();
        if (testMask != null && !testMask.isNull()) testMask.release();
    }
    
    @Nested
    @DisplayName("Constructor and Configuration")
    class ConstructorAndConfiguration {
        
        @Test
        @DisplayName("Should initialize with default bin values")
        void shouldInitializeWithDefaultBinValues() {
            assertEquals(90, histogramExtractor.getHueBins());
            assertEquals(2, histogramExtractor.getSatBins());
            assertEquals(1, histogramExtractor.getValBins());
            assertEquals(180, histogramExtractor.getTotalBins());
        }
        
        @Test
        @DisplayName("Should have correct HSV ranges")
        void shouldHaveCorrectHSVRanges() {
            float[] ranges = histogramExtractor.getRanges();
            
            // Hue range: 0-180
            assertEquals(0, ranges[0], 0.001);
            assertEquals(180, ranges[1], 0.001);
            
            // Saturation range: 0-256
            assertEquals(0, ranges[2], 0.001);
            assertEquals(256, ranges[3], 0.001);
            
            // Value range: 0-256
            assertEquals(0, ranges[4], 0.001);
            assertEquals(256, ranges[5], 0.001);
        }
        
        @Test
        @DisplayName("Should have correct channel indices")
        void shouldHaveCorrectChannelIndices() {
            int[] channels = histogramExtractor.getChannels();
            
            assertEquals(0, channels[0]); // Hue channel
            assertEquals(1, channels[1]); // Saturation channel
            assertEquals(2, channels[2]); // Value channel
        }
    }
    
    @Nested
    @DisplayName("Bin Configuration")
    class BinConfiguration {
        
        @Test
        @DisplayName("Should set custom bin values")
        void shouldSetCustomBinValues() {
            histogramExtractor.setBins(180, 256, 256);
            
            assertEquals(180, histogramExtractor.getHueBins());
            assertEquals(256, histogramExtractor.getSatBins());
            assertEquals(256, histogramExtractor.getValBins());
            assertEquals(180 * 256 * 256, histogramExtractor.getTotalBins());
        }
        
        @Test
        @DisplayName("Should update total bins when setting custom values")
        void shouldUpdateTotalBinsWhenSettingCustomValues() {
            histogramExtractor.setBins(30, 32, 32);
            
            assertEquals(30 * 32 * 32, histogramExtractor.getTotalBins());
        }
        
        @Test
        @DisplayName("Should handle minimal bin configuration")
        void shouldHandleMinimalBinConfiguration() {
            histogramExtractor.setBins(1, 1, 1);
            
            assertEquals(1, histogramExtractor.getHueBins());
            assertEquals(1, histogramExtractor.getSatBins());
            assertEquals(1, histogramExtractor.getValBins());
            assertEquals(1, histogramExtractor.getTotalBins());
        }
    }
    
    @Nested
    @DisplayName("Histogram Extraction from Region")
    class HistogramExtractionFromRegion {
        
        @Test
        @DisplayName("Should extract histogram from masked region")
        void shouldExtractHistogramFromMaskedRegion() {
            HistogramRegions result = histogramExtractor.getHistogramFromRegion(testMask);
            
            assertNotNull(result);
            assertEquals(1, result.getImages().size());
            assertEquals(testMask, result.getImages().get(0));
        }
        
        @Test
        @DisplayName("Should initialize histogram regions for single mask")
        void shouldInitializeHistogramRegionsForSingleMask() {
            HistogramRegions result = histogramExtractor.getHistogramFromRegion(testMask);
            
            assertNotNull(result.getTopLeft());
            assertNotNull(result.getTopRight());
            assertNotNull(result.getBottomLeft());
            assertNotNull(result.getBottomRight());
            assertNotNull(result.getEllipse());
            assertNotNull(result.getCombined());
        }
        
        @Test
        @DisplayName("Should create masks for all regions")
        void shouldCreateMasksForAllRegions() {
            HistogramRegions result = histogramExtractor.getHistogramFromRegion(testMask);
            
            assertEquals(1, result.getTopLeft().getMasks().size());
            assertEquals(1, result.getTopRight().getMasks().size());
            assertEquals(1, result.getBottomLeft().getMasks().size());
            assertEquals(1, result.getBottomRight().getMasks().size());
            assertEquals(1, result.getEllipse().getMasks().size());
        }
    }
    
    @Nested
    @DisplayName("HSV Histogram Extraction")
    class HSVHistogramExtraction {
        
        @Test
        @DisplayName("Should extract HSV histograms from StateImage")
        void shouldExtractHSVHistogramsFromStateImage() {
            StateImage stateImage = mock(StateImage.class);
            when(imageLoader.getMats(eq(stateImage), eq(ColorCluster.ColorSchemaName.HSV)))
                .thenReturn(testImages);
            
            HistogramRegions result = histogramExtractor.getHistogramsHSV(stateImage);
            
            assertNotNull(result);
            assertEquals(1, result.getImages().size());
            verify(imageLoader).getMats(stateImage, ColorCluster.ColorSchemaName.HSV);
        }
        
        @Test
        @DisplayName("Should initialize all histogram regions")
        void shouldInitializeAllHistogramRegions() {
            StateImage stateImage = mock(StateImage.class);
            when(imageLoader.getMats(eq(stateImage), eq(ColorCluster.ColorSchemaName.HSV)))
                .thenReturn(testImages);
            
            HistogramRegions result = histogramExtractor.getHistogramsHSV(stateImage);
            
            assertNotNull(result.getTopLeft().getHistogram());
            assertNotNull(result.getTopRight().getHistogram());
            assertNotNull(result.getBottomLeft().getHistogram());
            assertNotNull(result.getBottomRight().getHistogram());
            assertNotNull(result.getEllipse().getHistogram());
        }
        
        @Test
        @DisplayName("Should handle multiple images in StateImage")
        void shouldHandleMultipleImagesInStateImage() {
            StateImage stateImage = mock(StateImage.class);
            Mat image2 = new Mat(150, 250, CV_8UC3);
            Mat image3 = new Mat(100, 200, CV_8UC3);
            
            try {
                List<Mat> multipleImages = List.of(testImageHSV, image2, image3);
                when(imageLoader.getMats(eq(stateImage), eq(ColorCluster.ColorSchemaName.HSV)))
                    .thenReturn(multipleImages);
                
                HistogramRegions result = histogramExtractor.getHistogramsHSV(stateImage);
                
                assertEquals(3, result.getImages().size());
                assertEquals(3, result.getTopLeft().getMasks().size());
                assertEquals(3, result.getEllipse().getMasks().size());
            } finally {
                image2.release();
                image3.release();
            }
        }
    }
    
    @Nested
    @DisplayName("Histogram Region Processing")
    class HistogramRegionProcessing {
        
        @Test
        @DisplayName("Should process histogram regions with channels and bins")
        void shouldProcessHistogramRegionsWithChannelsAndBins() {
            IntPointer channelsPtr = new IntPointer(histogramExtractor.getChannels());
            IntPointer binsPtr = new IntPointer(
                histogramExtractor.getHueBins(),
                histogramExtractor.getSatBins(),
                histogramExtractor.getValBins()
            );
            PointerPointer<FloatPointer> rangesPtr = 
                new PointerPointer<>(histogramExtractor.getRanges());
            
            HistogramRegions histRegs = new HistogramRegions(testImageHSV);
            
            histogramExtractor.getHistogramRegions(histRegs, channelsPtr, binsPtr, rangesPtr);
            
            // Should have processed histograms for all regions
            assertFalse(histRegs.getTopLeft().getHistograms().isEmpty());
            assertFalse(histRegs.getTopRight().getHistograms().isEmpty());
            assertFalse(histRegs.getBottomLeft().getHistograms().isEmpty());
            assertFalse(histRegs.getBottomRight().getHistograms().isEmpty());
            assertFalse(histRegs.getEllipse().getHistograms().isEmpty());
        }
        
        @Test
        @DisplayName("Should combine histograms after processing")
        void shouldCombineHistogramsAfterProcessing() {
            IntPointer channelsPtr = new IntPointer(histogramExtractor.getChannels());
            IntPointer binsPtr = new IntPointer(
                histogramExtractor.getHueBins(),
                histogramExtractor.getSatBins(),
                histogramExtractor.getValBins()
            );
            PointerPointer<FloatPointer> rangesPtr = 
                new PointerPointer<>(histogramExtractor.getRanges());
            
            HistogramRegions histRegs = new HistogramRegions(testImageHSV);
            
            histogramExtractor.getHistogramRegions(histRegs, channelsPtr, binsPtr, rangesPtr);
            
            // Combined histograms should be set
            assertNotNull(histRegs.getCombined().getHistogram());
            assertFalse(histRegs.getCombined().getHistogram().empty());
        }
    }
    
    @Nested
    @DisplayName("Edge Cases")
    class EdgeCases {
        
        @Test
        @DisplayName("Should handle empty image list")
        void shouldHandleEmptyImageList() {
            StateImage stateImage = mock(StateImage.class);
            when(imageLoader.getMats(eq(stateImage), eq(ColorCluster.ColorSchemaName.HSV)))
                .thenReturn(new ArrayList<>());
            
            HistogramRegions result = histogramExtractor.getHistogramsHSV(stateImage);
            
            assertNotNull(result);
            assertEquals(0, result.getImages().size());
        }
        
        @Test
        @DisplayName("Should handle single channel configuration")
        void shouldHandleSingleChannelConfiguration() {
            histogramExtractor.setBins(256, 1, 1);
            
            assertEquals(256, histogramExtractor.getTotalBins());
            
            HistogramRegions result = histogramExtractor.getHistogramFromRegion(testMask);
            assertNotNull(result);
        }
        
        @Test
        @DisplayName("Should handle very small images")
        void shouldHandleVerySmallImages() {
            Mat smallImage = new Mat(10, 10, CV_8UC3);
            
            try {
                HistogramRegions result = histogramExtractor.getHistogramFromRegion(smallImage);
                
                assertNotNull(result);
                assertEquals(1, result.getImages().size());
                assertEquals(10, result.getImageSizes().get(0).w());
                assertEquals(10, result.getImageSizes().get(0).h());
            } finally {
                smallImage.release();
            }
        }
        
        @Test
        @DisplayName("Should handle grayscale-like HSV configuration")
        void shouldHandleGrayscaleLikeHSVConfiguration() {
            // Configure for grayscale-like processing (only value channel matters)
            histogramExtractor.setBins(1, 1, 256);
            
            assertEquals(256, histogramExtractor.getTotalBins());
            assertEquals(1, histogramExtractor.getHueBins());
            assertEquals(1, histogramExtractor.getSatBins());
            assertEquals(256, histogramExtractor.getValBins());
        }
    }
    
    @Nested
    @DisplayName("Performance Considerations")
    class PerformanceConsiderations {
        
        @Test
        @DisplayName("Should handle high resolution histogram bins")
        void shouldHandleHighResolutionHistogramBins() {
            histogramExtractor.setBins(180, 256, 256);
            
            assertEquals(180 * 256 * 256, histogramExtractor.getTotalBins());
            
            // Should still be able to process
            HistogramRegions result = histogramExtractor.getHistogramFromRegion(testMask);
            assertNotNull(result);
        }
        
        @Test
        @DisplayName("Should efficiently process multiple large images")
        void shouldEfficientlyProcessMultipleLargeImages() {
            StateImage stateImage = mock(StateImage.class);
            List<Mat> largeImages = new ArrayList<>();
            
            // Create 5 large images
            for (int i = 0; i < 5; i++) {
                Mat largeImage = new Mat(500, 500, CV_8UC3);
                largeImage.setTo(new Scalar(90, 128, 200));
                largeImages.add(largeImage);
            }
            
            try {
                when(imageLoader.getMats(eq(stateImage), eq(ColorCluster.ColorSchemaName.HSV)))
                    .thenReturn(largeImages);
                
                long startTime = System.currentTimeMillis();
                HistogramRegions result = histogramExtractor.getHistogramsHSV(stateImage);
                long endTime = System.currentTimeMillis();
                
                assertNotNull(result);
                assertEquals(5, result.getImages().size());
                
                // Should complete in reasonable time (< 2 seconds for 5 large images)
                assertTrue((endTime - startTime) < 2000,
                    "Processing took too long: " + (endTime - startTime) + "ms");
            } finally {
                for (Mat img : largeImages) {
                    img.release();
                }
            }
        }
    }
}