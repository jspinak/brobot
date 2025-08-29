package io.github.jspinak.brobot.tools.testing.wrapper;

import io.github.jspinak.brobot.model.state.StateImage;
import io.github.jspinak.brobot.model.match.Match;
import io.github.jspinak.brobot.model.element.Region;
import io.github.jspinak.brobot.config.ExecutionMode;
import io.github.jspinak.brobot.config.FrameworkSettings;
import io.github.jspinak.brobot.tools.testing.mock.action.MockHistogram;
import io.github.jspinak.brobot.analysis.histogram.SingleRegionHistogramExtractor;
import io.github.jspinak.brobot.test.BrobotTestBase;
import org.bytedeco.opencv.opencv_core.Mat;
import org.bytedeco.opencv.opencv_core.Scalar;
import org.junit.jupiter.api.*;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import static org.bytedeco.opencv.global.opencv_core.CV_8UC3;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

/**
 * Comprehensive test suite for HistogramWrapper.
 * Tests routing between mock and live histogram implementations.
 */
@ExtendWith(MockitoExtension.class)
@DisplayName("HistogramWrapper Tests")
public class HistogramWrapperTest extends BrobotTestBase {
    
    @Mock
    private ExecutionMode executionMode;
    
    @Mock
    private MockHistogram mockHistogram;
    
    @Mock
    private SingleRegionHistogramExtractor histogramExtractor;
    
    private HistogramWrapper histogramWrapper;
    
    @Mock
    private StateImage stateImage;
    
    private Mat sceneHSV;
    
    private List<Region> regions;
    
    @BeforeEach
    @Override
    public void setupTest() {
        super.setupTest();
        histogramWrapper = new HistogramWrapper(executionMode, mockHistogram, histogramExtractor);
        
        // Create test HSV Mat
        sceneHSV = new Mat(100, 100, CV_8UC3, new Scalar(120, 200, 150, 0));
        
        // Create test regions
        regions = Arrays.asList(
            new Region(0, 0, 50, 50),
            new Region(50, 50, 50, 50)
        );
    }
    
    @AfterEach
    public void tearDown() {
        if (sceneHSV != null && !sceneHSV.isNull()) {
            sceneHSV.release();
        }
    }
    
    @Nested
    @DisplayName("Mock Mode Routing")
    class MockModeRouting {
        
        @BeforeEach
        void setupMockMode() {
            when(executionMode.isMock()).thenReturn(true);
        }
        
        @Test
        @DisplayName("Should route findHistogram to mock implementation in mock mode")
        void shouldRouteFindHistogramToMockInMockMode() {
            List<Match> mockMatches = Arrays.asList(
                mock(Match.class),
                mock(Match.class)
            );
            when(mockHistogram.getMockHistogramMatches(stateImage, regions)).thenReturn(mockMatches);
            
            List<Match> result = histogramWrapper.findHistogram(stateImage, sceneHSV, regions);
            
            assertEquals(mockMatches, result);
            verify(mockHistogram).getMockHistogramMatches(stateImage, regions);
            verify(histogramExtractor, never()).findAll(any(), any(), any());
        }
        
        @Test
        @DisplayName("Should handle empty regions in mock mode")
        void shouldHandleEmptyRegionsInMockMode() {
            List<Region> emptyRegions = Collections.emptyList();
            when(mockHistogram.getMockHistogramMatches(stateImage, emptyRegions))
                .thenReturn(Collections.emptyList());
            
            List<Match> result = histogramWrapper.findHistogram(stateImage, sceneHSV, emptyRegions);
            
            assertTrue(result.isEmpty());
            verify(mockHistogram).getMockHistogramMatches(stateImage, emptyRegions);
        }
        
        @Test
        @DisplayName("Should handle null StateImage in mock mode")
        void shouldHandleNullStateImageInMockMode() {
            when(mockHistogram.getMockHistogramMatches(null, regions))
                .thenReturn(Collections.emptyList());
            
            List<Match> result = histogramWrapper.findHistogram(null, sceneHSV, regions);
            
            assertNotNull(result);
            assertTrue(result.isEmpty());
            verify(mockHistogram).getMockHistogramMatches(null, regions);
        }
        
        @Test
        @DisplayName("Should ignore scene HSV in mock mode")
        void shouldIgnoreSceneHSVInMockMode() {
            List<Match> mockMatches = Collections.singletonList(mock(Match.class));
            when(mockHistogram.getMockHistogramMatches(stateImage, regions)).thenReturn(mockMatches);
            
            // Call with null HSV - should still work in mock mode
            List<Match> result = histogramWrapper.findHistogram(stateImage, null, regions);
            
            assertEquals(mockMatches, result);
            verify(mockHistogram).getMockHistogramMatches(stateImage, regions);
        }
    }
    
    @Nested
    @DisplayName("Live Mode Routing")
    class LiveModeRouting {
        
        @BeforeEach
        void setupLiveMode() {
            when(executionMode.isMock()).thenReturn(false);
        }
        
        @Test
        @DisplayName("Should route findHistogram to live implementation in live mode")
        void shouldRouteFindHistogramToLiveInLiveMode() {
            List<Match> liveMatches = Arrays.asList(
                mock(Match.class),
                mock(Match.class),
                mock(Match.class)
            );
            when(histogramExtractor.findAll(regions, stateImage, sceneHSV)).thenReturn(liveMatches);
            
            List<Match> result = histogramWrapper.findHistogram(stateImage, sceneHSV, regions);
            
            assertEquals(liveMatches, result);
            verify(histogramExtractor).findAll(regions, stateImage, sceneHSV);
            verify(mockHistogram, never()).getMockHistogramMatches(any(), any());
        }
        
        @Test
        @DisplayName("Should handle empty results from live implementation")
        void shouldHandleEmptyResultsFromLive() {
            when(histogramExtractor.findAll(regions, stateImage, sceneHSV))
                .thenReturn(Collections.emptyList());
            
            List<Match> result = histogramWrapper.findHistogram(stateImage, sceneHSV, regions);
            
            assertTrue(result.isEmpty());
            verify(histogramExtractor).findAll(regions, stateImage, sceneHSV);
        }
        
        @Test
        @DisplayName("Should pass all parameters to live implementation")
        void shouldPassAllParametersToLive() {
            when(histogramExtractor.findAll(regions, stateImage, sceneHSV))
                .thenReturn(Collections.emptyList());
            
            histogramWrapper.findHistogram(stateImage, sceneHSV, regions);
            
            verify(histogramExtractor).findAll(eq(regions), eq(stateImage), eq(sceneHSV));
        }
        
        @Test
        @DisplayName("Should handle null regions in live mode")
        void shouldHandleNullRegionsInLiveMode() {
            when(histogramExtractor.findAll(null, stateImage, sceneHSV))
                .thenReturn(Collections.emptyList());
            
            List<Match> result = histogramWrapper.findHistogram(stateImage, sceneHSV, null);
            
            assertNotNull(result);
            verify(histogramExtractor).findAll(null, stateImage, sceneHSV);
        }
    }
    
    @Nested
    @DisplayName("Mode Switching")
    class ModeSwitching {
        
        @Test
        @DisplayName("Should switch between mock and live modes correctly")
        void shouldSwitchBetweenModes() {
            // Setup mock matches
            List<Match> mockMatches = Collections.singletonList(mock(Match.class));
            when(mockHistogram.getMockHistogramMatches(stateImage, regions)).thenReturn(mockMatches);
            
            // Setup live matches
            List<Match> liveMatches = Arrays.asList(mock(Match.class), mock(Match.class));
            when(histogramExtractor.findAll(regions, stateImage, sceneHSV)).thenReturn(liveMatches);
            
            // First call in mock mode
            when(executionMode.isMock()).thenReturn(true);
            List<Match> mockResult = histogramWrapper.findHistogram(stateImage, sceneHSV, regions);
            assertEquals(1, mockResult.size());
            
            // Switch to live mode
            when(executionMode.isMock()).thenReturn(false);
            List<Match> liveResult = histogramWrapper.findHistogram(stateImage, sceneHSV, regions);
            assertEquals(2, liveResult.size());
            
            // Verify both implementations were called
            verify(mockHistogram).getMockHistogramMatches(stateImage, regions);
            verify(histogramExtractor).findAll(regions, stateImage, sceneHSV);
        }
        
        @Test
        @DisplayName("Should maintain consistency during mode switches")
        void shouldMaintainConsistencyDuringModeSwitches() {
            Match mockMatch = mock(Match.class);
            Match liveMatch = mock(Match.class);
            
            when(mockHistogram.getMockHistogramMatches(stateImage, regions))
                .thenReturn(Collections.singletonList(mockMatch));
            when(histogramExtractor.findAll(regions, stateImage, sceneHSV))
                .thenReturn(Collections.singletonList(liveMatch));
            
            // Alternate between modes
            for (int i = 0; i < 10; i++) {
                boolean isMock = i % 2 == 0;
                when(executionMode.isMock()).thenReturn(isMock);
                
                List<Match> result = histogramWrapper.findHistogram(stateImage, sceneHSV, regions);
                
                assertEquals(1, result.size());
                if (isMock) {
                    assertEquals(mockMatch, result.get(0));
                } else {
                    assertEquals(liveMatch, result.get(0));
                }
            }
        }
    }
    
    @Nested
    @DisplayName("HSV Mat Handling")
    class HSVMatHandling {
        
        @Test
        @DisplayName("Should handle different HSV Mat sizes")
        void shouldHandleDifferentHSVMatSizes() {
            when(executionMode.isMock()).thenReturn(false);
            
            // Test with different Mat sizes
            Mat smallHSV = new Mat(10, 10, CV_8UC3);
            Mat largeHSV = new Mat(1000, 1000, CV_8UC3);
            
            when(histogramExtractor.findAll(regions, stateImage, smallHSV))
                .thenReturn(Collections.singletonList(mock(Match.class)));
            when(histogramExtractor.findAll(regions, stateImage, largeHSV))
                .thenReturn(Arrays.asList(mock(Match.class), mock(Match.class)));
            
            List<Match> smallResult = histogramWrapper.findHistogram(stateImage, smallHSV, regions);
            assertEquals(1, smallResult.size());
            
            List<Match> largeResult = histogramWrapper.findHistogram(stateImage, largeHSV, regions);
            assertEquals(2, largeResult.size());
            
            // Clean up
            smallHSV.release();
            largeHSV.release();
        }
        
        @Test
        @DisplayName("Should handle empty HSV Mat")
        void shouldHandleEmptyHSVMat() {
            when(executionMode.isMock()).thenReturn(false);
            
            Mat emptyHSV = new Mat();
            when(histogramExtractor.findAll(regions, stateImage, emptyHSV))
                .thenReturn(Collections.emptyList());
            
            List<Match> result = histogramWrapper.findHistogram(stateImage, emptyHSV, regions);
            
            assertTrue(result.isEmpty());
            verify(histogramExtractor).findAll(regions, stateImage, emptyHSV);
        }
    }
    
    @Nested
    @DisplayName("Region List Handling")
    class RegionListHandling {
        
        @Test
        @DisplayName("Should handle single region")
        void shouldHandleSingleRegion() {
            List<Region> singleRegion = Collections.singletonList(new Region(0, 0, 100, 100));
            when(executionMode.isMock()).thenReturn(true);
            when(mockHistogram.getMockHistogramMatches(stateImage, singleRegion))
                .thenReturn(Collections.singletonList(mock(Match.class)));
            
            List<Match> result = histogramWrapper.findHistogram(stateImage, sceneHSV, singleRegion);
            
            assertEquals(1, result.size());
        }
        
        @Test
        @DisplayName("Should handle many regions")
        void shouldHandleManyRegions() {
            List<Region> manyRegions = new ArrayList<>();
            for (int i = 0; i < 100; i++) {
                manyRegions.add(new Region(i, i, 10, 10));
            }
            
            when(executionMode.isMock()).thenReturn(true);
            List<Match> manyMatches = new ArrayList<>();
            for (int i = 0; i < 100; i++) {
                manyMatches.add(mock(Match.class));
            }
            when(mockHistogram.getMockHistogramMatches(stateImage, manyRegions))
                .thenReturn(manyMatches);
            
            List<Match> result = histogramWrapper.findHistogram(stateImage, sceneHSV, manyRegions);
            
            assertEquals(100, result.size());
        }
        
        @Test
        @DisplayName("Should handle overlapping regions")
        void shouldHandleOverlappingRegions() {
            List<Region> overlappingRegions = Arrays.asList(
                new Region(0, 0, 60, 60),
                new Region(40, 40, 60, 60)
            );
            
            when(executionMode.isMock()).thenReturn(false);
            when(histogramExtractor.findAll(overlappingRegions, stateImage, sceneHSV))
                .thenReturn(Arrays.asList(mock(Match.class), mock(Match.class)));
            
            List<Match> result = histogramWrapper.findHistogram(stateImage, sceneHSV, overlappingRegions);
            
            assertEquals(2, result.size());
            verify(histogramExtractor).findAll(overlappingRegions, stateImage, sceneHSV);
        }
    }
    
    @Nested
    @DisplayName("Error Handling")
    class ErrorHandling {
        
        @Test
        @DisplayName("Should propagate exceptions from mock implementation")
        void shouldPropagateExceptionsFromMock() {
            when(executionMode.isMock()).thenReturn(true);
            when(mockHistogram.getMockHistogramMatches(stateImage, regions))
                .thenThrow(new RuntimeException("Mock histogram error"));
            
            assertThrows(RuntimeException.class, 
                () -> histogramWrapper.findHistogram(stateImage, sceneHSV, regions));
        }
        
        @Test
        @DisplayName("Should propagate exceptions from live implementation")
        void shouldPropagateExceptionsFromLive() {
            when(executionMode.isMock()).thenReturn(false);
            when(histogramExtractor.findAll(regions, stateImage, sceneHSV))
                .thenThrow(new RuntimeException("Live histogram error"));
            
            assertThrows(RuntimeException.class, 
                () -> histogramWrapper.findHistogram(stateImage, sceneHSV, regions));
        }
        
        @Test
        @DisplayName("Should handle out of memory errors gracefully")
        void shouldHandleOutOfMemoryErrors() {
            when(executionMode.isMock()).thenReturn(false);
            when(histogramExtractor.findAll(regions, stateImage, sceneHSV))
                .thenThrow(new OutOfMemoryError("Histogram processing OOM"));
            
            assertThrows(OutOfMemoryError.class, 
                () -> histogramWrapper.findHistogram(stateImage, sceneHSV, regions));
        }
    }
    
    @Nested
    @DisplayName("Performance Characteristics")
    class PerformanceCharacteristics {
        
        @Test
        @DisplayName("Mock mode should bypass HSV processing")
        void mockModeShouldBypassHSVProcessing() {
            when(executionMode.isMock()).thenReturn(true);
            when(mockHistogram.getMockHistogramMatches(stateImage, regions))
                .thenReturn(Collections.emptyList());
            
            // Create a large HSV Mat that would be expensive to process
            Mat largeHSV = new Mat(10000, 10000, CV_8UC3);
            
            long startTime = System.nanoTime();
            histogramWrapper.findHistogram(stateImage, largeHSV, regions);
            long duration = System.nanoTime() - startTime;
            
            // Should complete quickly since HSV is ignored in mock mode
            assertTrue(duration < 100_000_000); // Less than 100ms
            
            // Verify HSV was never passed to mock
            verify(mockHistogram).getMockHistogramMatches(stateImage, regions);
            
            largeHSV.release();
        }
    }
    
    @Nested
    @DisplayName("Integration with Framework")
    class FrameworkIntegration {
        
        @Test
        @DisplayName("Should respect FrameworkSettings configuration")
        void shouldRespectFrameworkSettings() {
            // Create real ExecutionMode to test integration
            ExecutionMode realExecutionMode = new ExecutionMode();
            HistogramWrapper realWrapper = new HistogramWrapper(
                realExecutionMode, mockHistogram, histogramExtractor);
            
            // Setup responses
            when(mockHistogram.getMockHistogramMatches(stateImage, regions))
                .thenReturn(Collections.singletonList(mock(Match.class)));
            when(histogramExtractor.findAll(regions, stateImage, sceneHSV))
                .thenReturn(Arrays.asList(mock(Match.class), mock(Match.class)));
            
            // Test with mock mode enabled
            FrameworkSettings.mock = true;
            FrameworkSettings.screenshots.clear();
            List<Match> mockResult = realWrapper.findHistogram(stateImage, sceneHSV, regions);
            assertEquals(1, mockResult.size());
            
            // Test with mock mode disabled
            FrameworkSettings.mock = false;
            List<Match> liveResult = realWrapper.findHistogram(stateImage, sceneHSV, regions);
            assertEquals(2, liveResult.size());
            
            // Reset to test default
            FrameworkSettings.mock = true;
        }
    }
}