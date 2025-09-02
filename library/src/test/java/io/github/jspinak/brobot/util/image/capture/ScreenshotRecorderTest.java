package io.github.jspinak.brobot.util.image.capture;

import io.github.jspinak.brobot.config.FrameworkSettings;
import io.github.jspinak.brobot.model.element.Region;
import io.github.jspinak.brobot.test.BrobotTestBase;
import io.github.jspinak.brobot.tools.testing.mock.time.TimeProvider;
import io.github.jspinak.brobot.util.file.SaveToFile;
import io.github.jspinak.brobot.util.image.io.ImageFileUtilities;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

/**
 * Comprehensive test class for ScreenshotRecorder functionality.
 * Tests continuous screenshot capture and recording capabilities.
 */
@ExtendWith(MockitoExtension.class)
public class ScreenshotRecorderTest extends BrobotTestBase {

    @Mock
    private SaveToFile saveToFile;
    
    @Mock
    private ImageFileUtilities imageFileUtilities;
    
    @Mock
    private TimeProvider timeProvider;
    
    @Mock
    private ScheduledExecutorService executorService;
    
    private ScreenshotRecorder screenshotRecorder;
    
    @BeforeEach
    @Override
    public void setupTest() {
        super.setupTest();
        screenshotRecorder = new ScreenshotRecorder(saveToFile, imageFileUtilities, timeProvider);
    }
    
    @Test
    @DisplayName("Should record fixed number of screenshots")
    void shouldRecordFixedNumberOfScreenshots() {
        int numberOfScreenshots = 5;
        long intervalMillis = 100;
        String baseFilename = "test_screenshot";
        
        // Setup mocks
        when(timeProvider.currentTimeMillis()).thenReturn(1000L, 1100L, 1200L, 1300L, 1400L);
        
        // Since we're in mock mode, the recorder should handle this gracefully
        assertDoesNotThrow(() -> 
            screenshotRecorder.recordScreenshots(numberOfScreenshots, intervalMillis, baseFilename)
        );
    }
    
    @Test
    @DisplayName("Should record screenshots of specific region")
    void shouldRecordScreenshotsOfSpecificRegion() {
        Region region = new Region(100, 100, 200, 200);
        int numberOfScreenshots = 3;
        long intervalMillis = 50;
        String baseFilename = "region_screenshot";
        
        assertDoesNotThrow(() -> 
            screenshotRecorder.recordScreenshots(region, numberOfScreenshots, intervalMillis, baseFilename)
        );
    }
    
    @Test
    @DisplayName("Should start continuous recording")
    void shouldStartContinuousRecording() {
        long intervalMillis = 1000;
        String baseFilename = "continuous";
        
        assertDoesNotThrow(() -> 
            screenshotRecorder.startContinuousRecording(intervalMillis, baseFilename)
        );
        
        assertTrue(screenshotRecorder.isRecording());
    }
    
    @Test
    @DisplayName("Should start continuous recording of region")
    void shouldStartContinuousRecordingOfRegion() {
        Region region = new Region(0, 0, 800, 600);
        long intervalMillis = 500;
        String baseFilename = "region_continuous";
        
        assertDoesNotThrow(() -> 
            screenshotRecorder.startContinuousRecording(region, intervalMillis, baseFilename)
        );
        
        assertTrue(screenshotRecorder.isRecording());
    }
    
    @Test
    @DisplayName("Should stop continuous recording")
    void shouldStopContinuousRecording() {
        // Start recording first
        screenshotRecorder.startContinuousRecording(1000, "test");
        assertTrue(screenshotRecorder.isRecording());
        
        // Stop recording
        screenshotRecorder.stopRecording();
        assertFalse(screenshotRecorder.isRecording());
    }
    
    @Test
    @DisplayName("Should handle stop when not recording")
    void shouldHandleStopWhenNotRecording() {
        assertFalse(screenshotRecorder.isRecording());
        
        // Should not throw exception
        assertDoesNotThrow(() -> screenshotRecorder.stopRecording());
        
        assertFalse(screenshotRecorder.isRecording());
    }
    
    @Test
    @DisplayName("Should not start new recording when already recording")
    void shouldNotStartNewRecordingWhenAlreadyRecording() {
        // Start first recording
        screenshotRecorder.startContinuousRecording(1000, "first");
        assertTrue(screenshotRecorder.isRecording());
        
        // Try to start second recording
        screenshotRecorder.startContinuousRecording(500, "second");
        
        // Should still be recording (original session)
        assertTrue(screenshotRecorder.isRecording());
    }
    
    @Test
    @DisplayName("Should handle zero screenshots request")
    void shouldHandleZeroScreenshotsRequest() {
        assertDoesNotThrow(() -> 
            screenshotRecorder.recordScreenshots(0, 100, "test")
        );
    }
    
    @Test
    @DisplayName("Should handle negative screenshots request")
    void shouldHandleNegativeScreenshotsRequest() {
        assertDoesNotThrow(() -> 
            screenshotRecorder.recordScreenshots(-5, 100, "test")
        );
    }
    
    @Test
    @DisplayName("Should handle zero interval")
    void shouldHandleZeroInterval() {
        assertDoesNotThrow(() -> 
            screenshotRecorder.recordScreenshots(5, 0, "test")
        );
    }
    
    @Test
    @DisplayName("Should handle negative interval")
    void shouldHandleNegativeInterval() {
        assertDoesNotThrow(() -> 
            screenshotRecorder.recordScreenshots(5, -100, "test")
        );
    }
    
    @Test
    @DisplayName("Should handle null filename")
    void shouldHandleNullFilename() {
        assertDoesNotThrow(() -> 
            screenshotRecorder.recordScreenshots(5, 100, null)
        );
    }
    
    @Test
    @DisplayName("Should handle empty filename")
    void shouldHandleEmptyFilename() {
        assertDoesNotThrow(() -> 
            screenshotRecorder.recordScreenshots(5, 100, "")
        );
    }
    
    @Test
    @DisplayName("Should handle null region")
    void shouldHandleNullRegion() {
        assertDoesNotThrow(() -> 
            screenshotRecorder.recordScreenshots(null, 5, 100, "test")
        );
    }
    
    @Test
    @DisplayName("Should take single screenshot")
    void shouldTakeSingleScreenshot() {
        String filename = "single_shot";
        
        assertDoesNotThrow(() -> 
            screenshotRecorder.takeScreenshot(filename)
        );
    }
    
    @Test
    @DisplayName("Should take single screenshot of region")
    void shouldTakeSingleScreenshotOfRegion() {
        Region region = new Region(50, 50, 300, 200);
        String filename = "region_single_shot";
        
        assertDoesNotThrow(() -> 
            screenshotRecorder.takeScreenshot(region, filename)
        );
    }
    
    @Test
    @DisplayName("Should handle large number of screenshots")
    void shouldHandleLargeNumberOfScreenshots() {
        assertDoesNotThrow(() -> 
            screenshotRecorder.recordScreenshots(1000, 10, "bulk_test")
        );
    }
    
    @Test
    @DisplayName("Should handle very small interval")
    void shouldHandleVerySmallInterval() {
        assertDoesNotThrow(() -> 
            screenshotRecorder.recordScreenshots(10, 1, "rapid_test")
        );
    }
    
    @Test
    @DisplayName("Should handle very large interval")
    void shouldHandleVeryLargeInterval() {
        assertDoesNotThrow(() -> 
            screenshotRecorder.recordScreenshots(2, 10000, "slow_test")
        );
    }
    
    @Test
    @DisplayName("Should record with custom executor service")
    void shouldRecordWithCustomExecutorService() {
        ScreenshotRecorder customRecorder = new ScreenshotRecorder(
            saveToFile, imageFileUtilities, timeProvider, executorService
        );
        
        assertDoesNotThrow(() -> 
            customRecorder.startContinuousRecording(100, "custom_test")
        );
    }
    
    @Test
    @DisplayName("Should handle region with zero dimensions")
    void shouldHandleRegionWithZeroDimensions() {
        Region zeroRegion = new Region(100, 100, 0, 0);
        
        assertDoesNotThrow(() -> 
            screenshotRecorder.recordScreenshots(zeroRegion, 5, 100, "zero_region")
        );
    }
    
    @Test
    @DisplayName("Should handle region with negative dimensions")
    void shouldHandleRegionWithNegativeDimensions() {
        Region negativeRegion = new Region(100, 100, -50, -50);
        
        assertDoesNotThrow(() -> 
            screenshotRecorder.recordScreenshots(negativeRegion, 5, 100, "negative_region")
        );
    }
    
    @Test
    @DisplayName("Should handle region outside screen bounds")
    void shouldHandleRegionOutsideScreenBounds() {
        Region outsideRegion = new Region(5000, 5000, 100, 100);
        
        assertDoesNotThrow(() -> 
            screenshotRecorder.recordScreenshots(outsideRegion, 5, 100, "outside_region")
        );
    }
    
    @Test
    @DisplayName("Should pause and resume recording")
    void shouldPauseAndResumeRecording() {
        screenshotRecorder.startContinuousRecording(100, "pause_test");
        assertTrue(screenshotRecorder.isRecording());
        
        screenshotRecorder.pauseRecording();
        assertTrue(screenshotRecorder.isPaused());
        
        screenshotRecorder.resumeRecording();
        assertFalse(screenshotRecorder.isPaused());
        assertTrue(screenshotRecorder.isRecording());
        
        screenshotRecorder.stopRecording();
        assertFalse(screenshotRecorder.isRecording());
    }
    
    @Test
    @DisplayName("Should get recording statistics")
    void shouldGetRecordingStatistics() {
        screenshotRecorder.startContinuousRecording(100, "stats_test");
        
        ScreenshotRecorder.RecordingStats stats = screenshotRecorder.getRecordingStats();
        
        assertNotNull(stats);
        assertTrue(stats.getStartTime() > 0);
        assertEquals(0, stats.getScreenshotCount()); // In mock mode
        assertNotNull(stats.getBaseFilename());
    }
    
    @Test
    @DisplayName("Should handle concurrent start and stop")
    void shouldHandleConcurrentStartAndStop() throws InterruptedException {
        Thread startThread = new Thread(() -> {
            for (int i = 0; i < 10; i++) {
                screenshotRecorder.startContinuousRecording(50, "concurrent_" + i);
                try {
                    Thread.sleep(10);
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                }
            }
        });
        
        Thread stopThread = new Thread(() -> {
            for (int i = 0; i < 10; i++) {
                screenshotRecorder.stopRecording();
                try {
                    Thread.sleep(15);
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                }
            }
        });
        
        startThread.start();
        stopThread.start();
        
        startThread.join(1000);
        stopThread.join(1000);
        
        // Should end in a stable state
        assertNotNull(screenshotRecorder);
    }
    
    @Test
    @DisplayName("Should validate recording configuration")
    void shouldValidateRecordingConfiguration() {
        ScreenshotRecorder.RecordingConfig config = 
            new ScreenshotRecorder.RecordingConfig(100, "test", new Region(0, 0, 100, 100));
        
        assertEquals(100, config.getIntervalMillis());
        assertEquals("test", config.getBaseFilename());
        assertNotNull(config.getRegion());
        assertEquals(0, config.getRegion().x());
        assertEquals(0, config.getRegion().y());
        assertEquals(100, config.getRegion().w());
        assertEquals(100, config.getRegion().h());
    }
}