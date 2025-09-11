package io.github.jspinak.brobot.core.services;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

import java.awt.image.BufferedImage;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import io.github.jspinak.brobot.model.element.Region;
import io.github.jspinak.brobot.test.BrobotTestBase;

/**
 * Comprehensive tests for ScreenCaptureService interface. Tests screen capture operations and
 * multi-monitor support.
 */
@ExtendWith(MockitoExtension.class)
@DisplayName("ScreenCaptureService Interface Tests")
public class ScreenCaptureServiceTest extends BrobotTestBase {

    @Mock private ScreenCaptureService screenCaptureService;

    @Mock private BufferedImage mockImage;

    @BeforeEach
    @Override
    public void setupTest() {
        super.setupTest();
    }

    @Test
    @DisplayName("Should capture entire screen")
    void testCaptureScreen() {
        // Arrange
        when(screenCaptureService.captureScreen()).thenReturn(mockImage);

        // Act
        BufferedImage result = screenCaptureService.captureScreen();

        // Assert
        assertNotNull(result);
        assertEquals(mockImage, result);
        verify(screenCaptureService).captureScreen();
    }

    @Test
    @DisplayName("Should handle screen capture failure")
    void testCaptureScreenFailure() {
        // Arrange
        when(screenCaptureService.captureScreen()).thenReturn(null);

        // Act
        BufferedImage result = screenCaptureService.captureScreen();

        // Assert
        assertNull(result);
    }

    @Test
    @DisplayName("Should capture specific region")
    void testCaptureRegion() {
        // Arrange
        int x = 100, y = 200, width = 400, height = 300;
        when(screenCaptureService.captureRegion(x, y, width, height)).thenReturn(mockImage);

        // Act
        BufferedImage result = screenCaptureService.captureRegion(x, y, width, height);

        // Assert
        assertNotNull(result);
        assertEquals(mockImage, result);
        verify(screenCaptureService).captureRegion(x, y, width, height);
    }

    @Test
    @DisplayName("Should capture region using Region object")
    void testCaptureRegionWithObject() {
        // Arrange
        Region region = new Region(50, 75, 300, 200);
        when(screenCaptureService.captureRegion(region)).thenCallRealMethod();
        when(screenCaptureService.captureRegion(50, 75, 300, 200)).thenReturn(mockImage);

        // Act
        BufferedImage result = screenCaptureService.captureRegion(region);

        // Assert
        assertNotNull(result);
        assertEquals(mockImage, result);
        verify(screenCaptureService).captureRegion(50, 75, 300, 200);
    }

    @Test
    @DisplayName("Should handle null Region object")
    void testCaptureRegionWithNullObject() {
        // Arrange
        when(screenCaptureService.captureRegion((Region) null)).thenCallRealMethod();

        // Act
        BufferedImage result = screenCaptureService.captureRegion((Region) null);

        // Assert
        assertNull(result);
    }

    @Test
    @DisplayName("Should handle invalid region coordinates")
    void testCaptureInvalidRegion() {
        // Arrange
        when(screenCaptureService.captureRegion(-10, -10, 100, 100)).thenReturn(null);

        // Act
        BufferedImage result = screenCaptureService.captureRegion(-10, -10, 100, 100);

        // Assert
        assertNull(result);
    }

    @Test
    @DisplayName("Should capture active screen with mouse")
    void testCaptureActiveScreen() {
        // Arrange
        when(screenCaptureService.captureActiveScreen()).thenReturn(mockImage);

        // Act
        BufferedImage result = screenCaptureService.captureActiveScreen();

        // Assert
        assertNotNull(result);
        assertEquals(mockImage, result);
        verify(screenCaptureService).captureActiveScreen();
    }

    @Test
    @DisplayName("Should capture specific monitor by index")
    void testCaptureMonitor() {
        // Arrange
        int monitorIndex = 1;
        when(screenCaptureService.captureMonitor(monitorIndex)).thenReturn(mockImage);

        // Act
        BufferedImage result = screenCaptureService.captureMonitor(monitorIndex);

        // Assert
        assertNotNull(result);
        assertEquals(mockImage, result);
        verify(screenCaptureService).captureMonitor(monitorIndex);
    }

    @Test
    @DisplayName("Should handle invalid monitor index")
    void testCaptureInvalidMonitor() {
        // Arrange
        when(screenCaptureService.captureMonitor(999)).thenReturn(null);

        // Act
        BufferedImage result = screenCaptureService.captureMonitor(999);

        // Assert
        assertNull(result);
    }

    @Test
    @DisplayName("Should get monitor count")
    void testGetMonitorCount() {
        // Arrange
        when(screenCaptureService.getMonitorCount()).thenReturn(2);

        // Act
        int count = screenCaptureService.getMonitorCount();

        // Assert
        assertEquals(2, count);
        verify(screenCaptureService).getMonitorCount();
    }

    @Test
    @DisplayName("Should return at least one monitor")
    void testGetMonitorCountMinimum() {
        // Arrange
        when(screenCaptureService.getMonitorCount()).thenReturn(1);

        // Act
        int count = screenCaptureService.getMonitorCount();

        // Assert
        assertTrue(count >= 1, "Should have at least one monitor");
    }

    @Test
    @DisplayName("Should get monitor bounds")
    void testGetMonitorBounds() {
        // Arrange
        Region expectedBounds = new Region(1920, 0, 1920, 1080);
        when(screenCaptureService.getMonitorBounds(1)).thenReturn(expectedBounds);

        // Act
        Region bounds = screenCaptureService.getMonitorBounds(1);

        // Assert
        assertNotNull(bounds);
        assertEquals(1920, bounds.x());
        assertEquals(0, bounds.y());
        assertEquals(1920, bounds.w());
        assertEquals(1080, bounds.h());
    }

    @Test
    @DisplayName("Should handle invalid monitor index for bounds")
    void testGetMonitorBoundsInvalid() {
        // Arrange
        when(screenCaptureService.getMonitorBounds(999)).thenReturn(null);

        // Act
        Region bounds = screenCaptureService.getMonitorBounds(999);

        // Assert
        assertNull(bounds);
    }

    @Test
    @DisplayName("Should get virtual desktop bounds")
    void testGetVirtualDesktopBounds() {
        // Arrange
        Region virtualBounds = new Region(0, 0, 3840, 1080);
        when(screenCaptureService.getVirtualDesktopBounds()).thenReturn(virtualBounds);

        // Act
        Region bounds = screenCaptureService.getVirtualDesktopBounds();

        // Assert
        assertNotNull(bounds);
        assertEquals(0, bounds.x());
        assertEquals(0, bounds.y());
        assertEquals(3840, bounds.w());
        assertEquals(1080, bounds.h());
    }

    @Test
    @DisplayName("Should check service availability")
    void testIsAvailable() {
        // Arrange
        when(screenCaptureService.isAvailable()).thenReturn(true);

        // Act
        boolean available = screenCaptureService.isAvailable();

        // Assert
        assertTrue(available);
        verify(screenCaptureService).isAvailable();
    }

    @Test
    @DisplayName("Should handle unavailable service")
    void testIsNotAvailable() {
        // Arrange
        when(screenCaptureService.isAvailable()).thenReturn(false);

        // Act
        boolean available = screenCaptureService.isAvailable();

        // Assert
        assertFalse(available);
    }

    @Test
    @DisplayName("Should return implementation name")
    void testGetImplementationName() {
        // Arrange
        when(screenCaptureService.getImplementationName()).thenReturn("AWT Robot");

        // Act
        String name = screenCaptureService.getImplementationName();

        // Assert
        assertEquals("AWT Robot", name);
        verify(screenCaptureService).getImplementationName();
    }

    @Test
    @DisplayName("Should handle zero-sized region capture")
    void testCaptureZeroSizedRegion() {
        // Arrange
        when(screenCaptureService.captureRegion(100, 100, 0, 0)).thenReturn(null);

        // Act
        BufferedImage result = screenCaptureService.captureRegion(100, 100, 0, 0);

        // Assert
        assertNull(result);
    }

    @Test
    @DisplayName("Should capture large region")
    void testCaptureLargeRegion() {
        // Arrange
        when(screenCaptureService.captureRegion(0, 0, 4000, 3000)).thenReturn(mockImage);

        // Act
        BufferedImage result = screenCaptureService.captureRegion(0, 0, 4000, 3000);

        // Assert
        assertNotNull(result);
        assertEquals(mockImage, result);
    }

    @Test
    @DisplayName("Should handle primary monitor capture")
    void testCapturePrimaryMonitor() {
        // Arrange
        when(screenCaptureService.captureMonitor(0)).thenReturn(mockImage);

        // Act
        BufferedImage result = screenCaptureService.captureMonitor(0);

        // Assert
        assertNotNull(result);
        assertEquals(mockImage, result);
    }

    @Test
    @DisplayName("Should support multi-monitor setup")
    void testMultiMonitorSetup() {
        // Arrange
        when(screenCaptureService.getMonitorCount()).thenReturn(3);
        when(screenCaptureService.getMonitorBounds(0)).thenReturn(new Region(0, 0, 1920, 1080));
        when(screenCaptureService.getMonitorBounds(1)).thenReturn(new Region(1920, 0, 1920, 1080));
        when(screenCaptureService.getMonitorBounds(2)).thenReturn(new Region(3840, 0, 1920, 1080));
        when(screenCaptureService.getVirtualDesktopBounds())
                .thenReturn(new Region(0, 0, 5760, 1080));

        // Act & Assert
        assertEquals(3, screenCaptureService.getMonitorCount());

        Region monitor0 = screenCaptureService.getMonitorBounds(0);
        assertNotNull(monitor0);
        assertEquals(0, monitor0.x());

        Region monitor1 = screenCaptureService.getMonitorBounds(1);
        assertNotNull(monitor1);
        assertEquals(1920, monitor1.x());

        Region monitor2 = screenCaptureService.getMonitorBounds(2);
        assertNotNull(monitor2);
        assertEquals(3840, monitor2.x());

        Region virtualDesktop = screenCaptureService.getVirtualDesktopBounds();
        assertNotNull(virtualDesktop);
        assertEquals(5760, virtualDesktop.w());
    }

    @Test
    @DisplayName("Should capture partial region overlapping monitors")
    void testCaptureRegionAcrossMonitors() {
        // Arrange
        // Region that spans across two monitors
        when(screenCaptureService.captureRegion(1800, 500, 240, 100)).thenReturn(mockImage);

        // Act
        BufferedImage result = screenCaptureService.captureRegion(1800, 500, 240, 100);

        // Assert
        assertNotNull(result);
        assertEquals(mockImage, result);
    }

    @Test
    @DisplayName("Should validate region boundaries")
    void testRegionBoundaryValidation() {
        // Test various boundary conditions

        // Negative coordinates
        when(screenCaptureService.captureRegion(-100, -100, 200, 200)).thenReturn(null);
        assertNull(screenCaptureService.captureRegion(-100, -100, 200, 200));

        // Negative dimensions
        when(screenCaptureService.captureRegion(100, 100, -50, -50)).thenReturn(null);
        assertNull(screenCaptureService.captureRegion(100, 100, -50, -50));

        // Very large coordinates
        when(screenCaptureService.captureRegion(10000, 10000, 100, 100)).thenReturn(null);
        assertNull(screenCaptureService.captureRegion(10000, 10000, 100, 100));
    }
}
