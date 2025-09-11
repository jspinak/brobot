package io.github.jspinak.brobot.monitor;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

import java.awt.*;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.junit.jupiter.api.*;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.junit.jupiter.params.provider.ValueSource;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.sikuli.script.Screen;

import io.github.jspinak.brobot.config.core.BrobotProperties;
import io.github.jspinak.brobot.test.BrobotTestBase;
import io.github.jspinak.brobot.test.DisabledInCI;

/**
 * Comprehensive test suite for MonitorManager - manages multi-monitor support. Tests monitor
 * detection, selection, coordinate conversion, and operation mapping.
 */
@DisplayName("MonitorManager Tests")
@DisabledInCI
public class MonitorManagerTest extends BrobotTestBase {

    @Mock private BrobotProperties mockProperties;

    @Mock private BrobotProperties.Monitor mockMonitorProperties;

    private MonitorManager monitorManager;

    @BeforeEach
    @Override
    public void setupTest() {
        super.setupTest();
        MockitoAnnotations.openMocks(this);

        // Setup mock properties
        when(mockProperties.getMonitor()).thenReturn(mockMonitorProperties);
        when(mockMonitorProperties.isMultiMonitorEnabled()).thenReturn(true);
        when(mockMonitorProperties.isLogMonitorInfo()).thenReturn(false);
        when(mockMonitorProperties.getDefaultScreenIndex()).thenReturn(0);
        when(mockMonitorProperties.getOperationMonitorMap()).thenReturn(new HashMap<>());
    }

    @Nested
    @DisplayName("Monitor Detection")
    class MonitorDetection {

        @Test
        @DisplayName("Should detect available monitors")
        void shouldDetectAvailableMonitors() {
            monitorManager = new MonitorManager(mockProperties);

            int monitorCount = monitorManager.getMonitorCount();
            assertTrue(monitorCount > 0, "Should detect at least one monitor");

            // In mock mode or headless, should have at least default monitor
            MonitorManager.MonitorInfo info = monitorManager.getMonitorInfo(0);
            assertNotNull(info);
        }

        @Test
        @DisplayName("Should get primary monitor index")
        void shouldGetPrimaryMonitorIndex() {
            monitorManager = new MonitorManager(mockProperties);

            int primaryIndex = monitorManager.getPrimaryMonitorIndex();
            assertTrue(primaryIndex >= 0);
            assertTrue(primaryIndex < monitorManager.getMonitorCount());
        }

        @Test
        @DisplayName("Should get monitor info")
        void shouldGetMonitorInfo() {
            monitorManager = new MonitorManager(mockProperties);

            MonitorManager.MonitorInfo info = monitorManager.getMonitorInfo(0);
            assertNotNull(info);
            assertEquals(0, info.getIndex());
            assertNotNull(info.getBounds());
            assertNotNull(info.getDeviceId());
            assertTrue(info.getWidth() > 0);
            assertTrue(info.getHeight() > 0);
        }

        @Test
        @DisplayName("Should get all monitor info")
        void shouldGetAllMonitorInfo() {
            monitorManager = new MonitorManager(mockProperties);

            List<MonitorManager.MonitorInfo> allInfo = monitorManager.getAllMonitorInfo();
            assertNotNull(allInfo);
            assertFalse(allInfo.isEmpty());
            assertEquals(monitorManager.getMonitorCount(), allInfo.size());
        }
    }

    @Nested
    @DisplayName("Screen Management")
    class ScreenManagement {

        @BeforeEach
        void setup() {
            monitorManager = new MonitorManager(mockProperties);
        }

        @Test
        @DisplayName("Should get screen for valid monitor index")
        void shouldGetScreenForValidMonitorIndex() {
            Screen screen = monitorManager.getScreen(0);
            // In mock mode, screen might be null
            // In real mode with display, screen should be valid
            if (!GraphicsEnvironment.isHeadless()) {
                assertNotNull(screen);
            }
        }

        @Test
        @DisplayName("Should handle invalid monitor index")
        void shouldHandleInvalidMonitorIndex() {
            // Should fall back to primary monitor
            Screen screen = monitorManager.getScreen(999);
            // Won't throw exception, just uses primary
            // The screen might be null in mock mode, but shouldn't throw exception
            assertDoesNotThrow(() -> monitorManager.getScreen(999));
        }

        @Test
        @DisplayName("Should get screen by operation name")
        void shouldGetScreenByOperationName() {
            Map<String, Integer> operationMap = new HashMap<>();
            operationMap.put("test-operation", 0);
            when(mockMonitorProperties.getOperationMonitorMap()).thenReturn(operationMap);

            monitorManager = new MonitorManager(mockProperties);
            monitorManager.setOperationMonitor("test-operation", 0);

            Screen screen = monitorManager.getScreen("test-operation");
            // Verify operation mapping was used
            verify(mockMonitorProperties, atLeastOnce()).isLogMonitorInfo();
        }

        @Test
        @DisplayName("Should use default screen for unknown operation")
        void shouldUseDefaultScreenForUnknownOperation() {
            when(mockMonitorProperties.getDefaultScreenIndex()).thenReturn(0);

            Screen screen = monitorManager.getScreen("unknown-operation");

            verify(mockMonitorProperties).getDefaultScreenIndex();
        }

        @Test
        @DisplayName("Should get all screens")
        void shouldGetAllScreens() {
            List<Screen> screens = monitorManager.getAllScreens();

            assertNotNull(screens);
            // In headless mode, list might be empty
            if (!GraphicsEnvironment.isHeadless()) {
                assertEquals(monitorManager.getMonitorCount(), screens.size());
            }
        }
    }

    @Nested
    @DisplayName("Operation Monitor Mapping")
    class OperationMonitorMapping {

        @BeforeEach
        void setup() {
            monitorManager = new MonitorManager(mockProperties);
        }

        @Test
        @DisplayName("Should set operation monitor for valid index")
        void shouldSetOperationMonitorForValidIndex() {
            monitorManager.setOperationMonitor("click-operation", 0);

            Screen screen = monitorManager.getScreen("click-operation");
            // Operation should be mapped - verify no exceptions
            assertDoesNotThrow(() -> monitorManager.getScreen("click-operation"));
        }

        @Test
        @DisplayName("Should reject invalid monitor index for operation")
        void shouldRejectInvalidMonitorIndexForOperation() {
            monitorManager.setOperationMonitor("invalid-operation", 999);

            // Should not be set due to invalid index
            Screen screen = monitorManager.getScreen("invalid-operation");
            // Should use default instead
            verify(mockMonitorProperties).getDefaultScreenIndex();
        }

        @Test
        @DisplayName("Should handle multiple operation mappings")
        void shouldHandleMultipleOperationMappings() {
            monitorManager.setOperationMonitor("operation1", 0);
            monitorManager.setOperationMonitor("operation2", 0);
            monitorManager.setOperationMonitor("operation3", 0);

            Screen screen1 = monitorManager.getScreen("operation1");
            Screen screen2 = monitorManager.getScreen("operation2");
            Screen screen3 = monitorManager.getScreen("operation3");

            // All should be mapped - verify no exceptions
            assertDoesNotThrow(
                    () -> {
                        monitorManager.getScreen("operation1");
                        monitorManager.getScreen("operation2");
                        monitorManager.getScreen("operation3");
                    });
        }
    }

    @Nested
    @DisplayName("Coordinate Conversion")
    class CoordinateConversion {

        @BeforeEach
        void setup() {
            monitorManager = new MonitorManager(mockProperties);
        }

        @Test
        @DisplayName("Should convert global to monitor coordinates")
        void shouldConvertGlobalToMonitorCoordinates() {
            MonitorManager.MonitorInfo info = monitorManager.getMonitorInfo(0);
            Point globalPoint = new Point(info.getX() + 100, info.getY() + 50);

            Point monitorPoint = monitorManager.toMonitorCoordinates(globalPoint, 0);

            assertEquals(100, monitorPoint.x);
            assertEquals(50, monitorPoint.y);
        }

        @Test
        @DisplayName("Should convert monitor to global coordinates")
        void shouldConvertMonitorToGlobalCoordinates() {
            MonitorManager.MonitorInfo info = monitorManager.getMonitorInfo(0);
            Point monitorPoint = new Point(100, 50);

            Point globalPoint = monitorManager.toGlobalCoordinates(monitorPoint, 0);

            assertEquals(info.getX() + 100, globalPoint.x);
            assertEquals(info.getY() + 50, globalPoint.y);
        }

        @ParameterizedTest
        @CsvSource({"0, 0", "100, 50", "500, 300", "1920, 1080"})
        @DisplayName("Should handle various coordinate conversions")
        void shouldHandleVariousCoordinateConversions(int x, int y) {
            Point monitorPoint = new Point(x, y);

            Point globalPoint = monitorManager.toGlobalCoordinates(monitorPoint, 0);
            Point backToMonitor = monitorManager.toMonitorCoordinates(globalPoint, 0);

            assertEquals(monitorPoint.x, backToMonitor.x);
            assertEquals(monitorPoint.y, backToMonitor.y);
        }

        @Test
        @DisplayName("Should handle invalid monitor index in coordinate conversion")
        void shouldHandleInvalidMonitorIndexInCoordinateConversion() {
            Point point = new Point(100, 100);

            Point converted = monitorManager.toMonitorCoordinates(point, 999);
            assertEquals(point, converted); // Should return original

            Point global = monitorManager.toGlobalCoordinates(point, 999);
            assertEquals(point, global); // Should return original
        }
    }

    @Nested
    @DisplayName("Monitor Point Detection")
    class MonitorPointDetection {

        @BeforeEach
        void setup() {
            monitorManager = new MonitorManager(mockProperties);
        }

        @Test
        @DisplayName("Should find monitor containing point")
        void shouldFindMonitorContainingPoint() {
            MonitorManager.MonitorInfo info = monitorManager.getMonitorInfo(0);
            Point pointInMonitor =
                    new Point(
                            info.getX() + info.getWidth() / 2, info.getY() + info.getHeight() / 2);

            int monitorIndex = monitorManager.getMonitorAtPoint(pointInMonitor);

            assertEquals(0, monitorIndex);
        }

        @Test
        @DisplayName("Should return primary for point outside all monitors")
        void shouldReturnPrimaryForPointOutsideAllMonitors() {
            Point outsidePoint = new Point(Integer.MAX_VALUE, Integer.MAX_VALUE);

            int monitorIndex = monitorManager.getMonitorAtPoint(outsidePoint);

            assertEquals(0, monitorIndex); // Should default to primary
        }

        @ParameterizedTest
        @ValueSource(ints = {-100, -50, 0, 50, 100})
        @DisplayName("Should handle edge points")
        void shouldHandleEdgePoints(int offset) {
            MonitorManager.MonitorInfo info = monitorManager.getMonitorInfo(0);
            Point edgePoint = new Point(info.getX() + offset, info.getY() + offset);

            int monitorIndex = monitorManager.getMonitorAtPoint(edgePoint);

            if (offset >= 0) {
                assertEquals(0, monitorIndex);
            } else {
                // Outside bounds, should return 0 (default)
                assertEquals(0, monitorIndex);
            }
        }
    }

    @Nested
    @DisplayName("Validation Methods")
    class ValidationMethods {

        @BeforeEach
        void setup() {
            monitorManager = new MonitorManager(mockProperties);
        }

        @Test
        @DisplayName("Should validate monitor index correctly")
        void shouldValidateMonitorIndexCorrectly() {
            assertTrue(monitorManager.isValidMonitorIndex(0));
            assertFalse(monitorManager.isValidMonitorIndex(-1));
            assertFalse(monitorManager.isValidMonitorIndex(999));

            int lastValidIndex = monitorManager.getMonitorCount() - 1;
            assertTrue(monitorManager.isValidMonitorIndex(lastValidIndex));
            assertFalse(monitorManager.isValidMonitorIndex(lastValidIndex + 1));
        }

        @Test
        @DisplayName("Should have positive monitor count")
        void shouldHavePositiveMonitorCount() {
            int count = monitorManager.getMonitorCount();
            assertTrue(count > 0, "Should have at least one monitor");
        }
    }

    @Nested
    @DisplayName("Configuration Integration")
    class ConfigurationIntegration {

        @Test
        @DisplayName("Should respect multi-monitor enabled setting")
        void shouldRespectMultiMonitorEnabledSetting() {
            when(mockMonitorProperties.isMultiMonitorEnabled()).thenReturn(false);

            monitorManager = new MonitorManager(mockProperties);

            // Should still work but may log warning
            assertNotNull(monitorManager);
            assertTrue(monitorManager.getMonitorCount() > 0);
        }

        @Test
        @DisplayName("Should respect logging setting")
        void shouldRespectLoggingSetting() {
            when(mockMonitorProperties.isLogMonitorInfo()).thenReturn(true);

            monitorManager = new MonitorManager(mockProperties);
            Screen screen = monitorManager.getScreen(0);

            // The logging is internal behavior, just verify no exceptions
            assertDoesNotThrow(() -> monitorManager.getScreen(0));
        }

        @Test
        @DisplayName("Should use primary monitor when default index is -1")
        void shouldUsePrimaryMonitorWhenDefaultIndexIsNegativeOne() {
            when(mockMonitorProperties.getDefaultScreenIndex()).thenReturn(-1);

            monitorManager = new MonitorManager(mockProperties);
            Screen screen = monitorManager.getScreen((String) null);

            verify(mockMonitorProperties).getDefaultScreenIndex();
        }

        @Test
        @DisplayName("Should load operation map from properties")
        void shouldLoadOperationMapFromProperties() {
            Map<String, Integer> predefinedMap = new HashMap<>();
            predefinedMap.put("predefined-op", 0);
            when(mockMonitorProperties.getOperationMonitorMap()).thenReturn(predefinedMap);

            monitorManager = new MonitorManager(mockProperties);

            Screen screen = monitorManager.getScreen("predefined-op");
            verify(mockMonitorProperties, atLeastOnce()).getOperationMonitorMap();
        }
    }

    @Nested
    @DisplayName("Headless Mode Support")
    class HeadlessModeSupport {

        @Test
        @DisplayName("Should handle headless environment gracefully")
        void shouldHandleHeadlessEnvironmentGracefully() {
            // This test adapts to the environment
            monitorManager = new MonitorManager(mockProperties);

            if (GraphicsEnvironment.isHeadless()) {
                // In headless mode, should still have default monitor
                assertEquals(1, monitorManager.getMonitorCount());
                MonitorManager.MonitorInfo info = monitorManager.getMonitorInfo(0);
                assertNotNull(info);
                assertEquals("headless-default", info.getDeviceId());
            } else {
                // In non-headless mode, should detect real monitors
                assertTrue(monitorManager.getMonitorCount() > 0);
            }
        }

        @Test
        @DisplayName("Should return null Screen in headless mode")
        void shouldReturnNullScreenInHeadlessMode() {
            monitorManager = new MonitorManager(mockProperties);

            if (GraphicsEnvironment.isHeadless()) {
                Screen screen = monitorManager.getScreen(0);
                assertNull(screen);

                List<Screen> allScreens = monitorManager.getAllScreens();
                assertTrue(allScreens.isEmpty());
            }
        }
    }

    @Nested
    @DisplayName("MonitorInfo Class")
    class MonitorInfoClass {

        @Test
        @DisplayName("Should provide monitor dimensions")
        void shouldProvideMonitorDimensions() {
            Rectangle bounds = new Rectangle(100, 200, 1920, 1080);
            MonitorManager.MonitorInfo info =
                    new MonitorManager.MonitorInfo(1, bounds, "test-device");

            assertEquals(1, info.getIndex());
            assertEquals(bounds, info.getBounds());
            assertEquals("test-device", info.getDeviceId());
            assertEquals(1920, info.getWidth());
            assertEquals(1080, info.getHeight());
            assertEquals(100, info.getX());
            assertEquals(200, info.getY());
        }

        @ParameterizedTest
        @CsvSource({
            "0, 0, 0, 1920, 1080",
            "1, 1920, 0, 1920, 1080",
            "2, -1920, 0, 1920, 1080",
            "3, 0, 1080, 1920, 1080"
        })
        @DisplayName("Should handle various monitor configurations")
        void shouldHandleVariousMonitorConfigurations(
                int index, int x, int y, int width, int height) {
            Rectangle bounds = new Rectangle(x, y, width, height);
            MonitorManager.MonitorInfo info =
                    new MonitorManager.MonitorInfo(index, bounds, "device-" + index);

            assertEquals(index, info.getIndex());
            assertEquals(x, info.getX());
            assertEquals(y, info.getY());
            assertEquals(width, info.getWidth());
            assertEquals(height, info.getHeight());
        }
    }
}
