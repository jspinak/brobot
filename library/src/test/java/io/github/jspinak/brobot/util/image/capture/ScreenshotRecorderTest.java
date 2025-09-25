package io.github.jspinak.brobot.util.image.capture;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import io.github.jspinak.brobot.test.BrobotTestBase;
import io.github.jspinak.brobot.test.annotations.DisabledInHeadlessEnvironment;
import io.github.jspinak.brobot.tools.testing.wrapper.TimeWrapper;
import io.github.jspinak.brobot.util.file.SaveToFile;
import io.github.jspinak.brobot.util.image.io.ImageFileUtilities;

/**
 * Comprehensive test class for ScreenshotRecorder functionality. Tests continuous screenshot
 * capture and recording capabilities.
 */
@ExtendWith(MockitoExtension.class)
@DisabledInHeadlessEnvironment("Screenshot recorder requires real display for continuous capture")
public class ScreenshotRecorderTest extends BrobotTestBase {

    @Mock private SaveToFile saveToFile;

    @Mock private ImageFileUtilities imageFileUtilities;

    @Mock private TimeWrapper timeWrapper;

    @Mock private io.github.jspinak.brobot.capture.BrobotCaptureService captureService;

    @Mock private io.github.jspinak.brobot.config.core.BrobotProperties brobotProperties;

    private ScreenshotRecorder screenshotRecorder;

    @BeforeEach
    @Override
    public void setupTest() {
        super.setupTest();
        screenshotRecorder =
                new ScreenshotRecorder(
                        brobotProperties, imageFileUtilities, timeWrapper, captureService);
    }

    @Test
    @DisplayName("Should capture screenshots for fixed duration")
    void shouldCaptureFixedDurationScreenshots() {
        // Mock the wait method
        doNothing().when(timeWrapper).wait(anyDouble());

        // Use the actual capture method
        assertDoesNotThrow(
                () -> screenshotRecorder.capture(2, 0.1) // 2 seconds, 0.1 second intervals
                );

        // Verify wait was called
        verify(timeWrapper, atLeastOnce()).wait(anyDouble());
    }

    @Test
    @DisplayName("Should start continuous capturing")
    void shouldStartContinuousCapturing() {
        String baseFilename = "test_screenshot";
        int intervalMillis = 1000;

        assertDoesNotThrow(
                () -> screenshotRecorder.startCapturing(saveToFile, baseFilename, intervalMillis));

        // Stop capturing to prevent resource leaks in tests
        screenshotRecorder.stopCapturing();
    }

    @Test
    @DisplayName("Should stop continuous capturing")
    void shouldStopContinuousCapturing() {
        // Start capturing first
        screenshotRecorder.startCapturing(saveToFile, "test", 1000);

        // Stop capturing
        assertDoesNotThrow(() -> screenshotRecorder.stopCapturing());
    }

    @Test
    @DisplayName("Should handle stop when not capturing")
    void shouldHandleStopWhenNotCapturing() {
        // Should not throw exception when stopping without starting
        assertDoesNotThrow(() -> screenshotRecorder.stopCapturing());
    }

    @Test
    @DisplayName("Should handle zero duration capture")
    void shouldHandleZeroDurationCapture() {
        doNothing().when(timeWrapper).wait(anyDouble());

        assertDoesNotThrow(() -> screenshotRecorder.capture(0, 0.1));
    }

    @Test
    @DisplayName("Should handle very small intervals")
    void shouldHandleVerySmallIntervals() {
        doNothing().when(timeWrapper).wait(anyDouble());

        assertDoesNotThrow(
                () -> screenshotRecorder.capture(1, 0.001) // Very fast intervals
                );
    }
}
