package io.github.jspinak.brobot.screen;

import io.github.jspinak.brobot.test.BrobotTestBase;
import org.junit.jupiter.api.*;
import org.junit.jupiter.api.condition.DisabledIfSystemProperty;
import org.junit.jupiter.api.condition.EnabledIfSystemProperty;
import org.mockito.MockedConstruction;
import org.mockito.MockedStatic;
import org.sikuli.script.Screen;
import org.sikuli.script.ScreenImage;

import java.awt.*;
import java.awt.image.BufferedImage;

import static org.junit.jupiter.api.Assertions.*;
import static org.junit.jupiter.api.Assumptions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.Mockito.*;

/**
 * Comprehensive test suite for PhysicalScreen.
 * Tests physical resolution screen capture functionality with proper mocking
 * to ensure tests work in headless environments.
 */
@DisplayName("PhysicalScreen Tests")
public class PhysicalScreenTest extends BrobotTestBase {
    
    private PhysicalScreen physicalScreen;
    private MockedStatic<GraphicsEnvironment> graphicsEnvMock;
    private MockedStatic<Toolkit> toolkitMock;
    private MockedConstruction<Robot> robotMock;
    
    @BeforeEach
    @Override
    public void setupTest() {
        super.setupTest();
    }
    
    @AfterEach
    void tearDown() {
        if (graphicsEnvMock != null) {
            graphicsEnvMock.close();
        }
        if (toolkitMock != null) {
            toolkitMock.close();
        }
        if (robotMock != null) {
            robotMock.close();
        }
    }
    
    @Nested
    @DisplayName("Initialization in Mock Mode")
    class InitializationInMockMode {
        
        @Test
        @DisplayName("Should create PhysicalScreen with same physical and logical resolutions")
        void shouldCreatePhysicalScreenWithoutScaling() {
            // Given
            setupMockEnvironment(1920, 1080, 1920, 1080);
            
            // When
            physicalScreen = new PhysicalScreen();
            
            // Then
            assertNotNull(physicalScreen);
            assertFalse(physicalScreen.isScalingCompensated());
            assertEquals(1.0f, physicalScreen.getScaleFactor());
            assertEquals(1920, physicalScreen.getPhysicalResolution().width);
            assertEquals(1080, physicalScreen.getPhysicalResolution().height);
        }
        
        @Test
        @DisplayName("Should detect DPI scaling when resolutions differ")
        void shouldDetectDpiScaling() {
            // Given - 4K physical, 1080p logical (200% scaling)
            setupMockEnvironment(3840, 2160, 1920, 1080);
            
            // When
            physicalScreen = new PhysicalScreen();
            
            // Then
            assertNotNull(physicalScreen);
            assertTrue(physicalScreen.isScalingCompensated());
            assertEquals(2.0f, physicalScreen.getScaleFactor(), 0.01f);
            assertEquals(3840, physicalScreen.getPhysicalResolution().width);
            assertEquals(2160, physicalScreen.getPhysicalResolution().height);
        }
        
        @Test
        @DisplayName("Should handle 150% DPI scaling")
        void shouldHandle150PercentScaling() {
            // Given - 150% scaling
            setupMockEnvironment(2880, 1620, 1920, 1080);
            
            // When
            physicalScreen = new PhysicalScreen();
            
            // Then
            assertTrue(physicalScreen.isScalingCompensated());
            assertEquals(1.5f, physicalScreen.getScaleFactor(), 0.01f);
        }
        
        @Test
        @DisplayName("Should handle 125% DPI scaling")
        void shouldHandle125PercentScaling() {
            // Given - 125% scaling
            setupMockEnvironment(2400, 1350, 1920, 1080);
            
            // When
            physicalScreen = new PhysicalScreen();
            
            // Then
            assertTrue(physicalScreen.isScalingCompensated());
            assertEquals(1.25f, physicalScreen.getScaleFactor(), 0.01f);
        }
    }
    
    @Nested
    @DisplayName("Screen Capture Operations")
    class ScreenCaptureOperations {
        
        private BufferedImage mockImage;
        private Robot mockRobotInstance;
        private MockedConstruction<Robot> localRobotMock;
        
        @BeforeEach
        void setup() {
            mockImage = new BufferedImage(100, 100, BufferedImage.TYPE_INT_RGB);
        }
        
        @AfterEach
        void cleanup() {
            if (localRobotMock != null) {
                localRobotMock.close();
            }
        }
        
        @Test
        @DisplayName("Should capture full screen at physical resolution")
        void shouldCaptureFullScreenAtPhysicalResolution() {
            // Given
            setupMockEnvironment(1920, 1080, 1920, 1080);
            setupRobotMock();
            
            physicalScreen = new PhysicalScreen();
            
            // When
            ScreenImage capture = physicalScreen.capture();
            
            // Then
            assertNotNull(capture);
            verify(mockRobotInstance).createScreenCapture(
                argThat(rect -> rect.x == 0 && rect.y == 0 
                    && rect.width == 1920 && rect.height == 1080)
            );
        }
        
        @Test
        @DisplayName("Should capture rectangle region without scaling")
        void shouldCaptureRectangleWithoutScaling() {
            // Given
            setupMockEnvironment(1920, 1080, 1920, 1080);
            setupRobotMock();
            
            physicalScreen = new PhysicalScreen();
            Rectangle rect = new Rectangle(100, 200, 300, 400);
            
            // When
            ScreenImage capture = physicalScreen.capture(rect);
            
            // Then
            assertNotNull(capture);
            verify(mockRobotInstance).createScreenCapture(
                argThat(r -> r.x == 100 && r.y == 200 
                    && r.width == 300 && r.height == 400)
            );
        }
        
        @Test
        @DisplayName("Should scale rectangle coordinates for DPI scaling")
        void shouldScaleRectangleForDpiScaling() {
            // Given - 200% scaling
            setupMockEnvironment(3840, 2160, 1920, 1080);
            setupRobotMock();
            
            physicalScreen = new PhysicalScreen();
            Rectangle logicalRect = new Rectangle(100, 100, 200, 200);
            
            // When
            ScreenImage capture = physicalScreen.capture(logicalRect);
            
            // Then
            assertNotNull(capture);
            // Should scale coordinates by 2x
            verify(mockRobotInstance).createScreenCapture(
                argThat(r -> r.x == 200 && r.y == 200 
                    && r.width == 400 && r.height == 400)
            );
        }
        
        @Test
        @DisplayName("Should capture with x,y,w,h coordinates")
        void shouldCaptureWithCoordinates() {
            // Given
            setupMockEnvironment(1920, 1080, 1920, 1080);
            setupRobotMock();
            
            physicalScreen = new PhysicalScreen();
            
            // When
            ScreenImage capture = physicalScreen.capture(50, 60, 70, 80);
            
            // Then
            assertNotNull(capture);
            verify(mockRobotInstance).createScreenCapture(
                argThat(r -> r.x == 50 && r.y == 60 
                    && r.width == 70 && r.height == 80)
            );
        }
        
        @Test
        @DisplayName("Should scale x,y,w,h coordinates for DPI")
        void shouldScaleCoordinatesForDpi() {
            // Given - 150% scaling
            setupMockEnvironment(2880, 1620, 1920, 1080);
            setupRobotMock();
            
            physicalScreen = new PhysicalScreen();
            
            // When
            ScreenImage capture = physicalScreen.capture(100, 100, 100, 100);
            
            // Then
            assertNotNull(capture);
            // Should scale by 1.5x
            verify(mockRobotInstance).createScreenCapture(
                argThat(r -> r.x == 150 && r.y == 150 
                    && r.width == 150 && r.height == 150)
            );
        }
        
        @Test
        @DisplayName("Should clamp capture bounds to screen dimensions")
        void shouldClampCaptureBounds() {
            // Given
            setupMockEnvironment(1920, 1080, 1920, 1080);
            setupRobotMock();
            
            physicalScreen = new PhysicalScreen();
            
            // When - Request capture beyond screen bounds
            ScreenImage capture = physicalScreen.capture(1900, 1000, 200, 200);
            
            // Then - Should clamp to screen bounds
            assertNotNull(capture);
            verify(mockRobotInstance).createScreenCapture(
                argThat(r -> r.x == 1900 && r.y == 1000 
                    && r.width == 20 && r.height == 80)
            );
        }
        
        @Test
        @DisplayName("Should handle negative coordinates by clamping to zero")
        void shouldHandleNegativeCoordinates() {
            // Given
            setupMockEnvironment(1920, 1080, 1920, 1080);
            setupRobotMock();
            
            physicalScreen = new PhysicalScreen();
            
            // When
            ScreenImage capture = physicalScreen.capture(-50, -100, 200, 300);
            
            // Then
            assertNotNull(capture);
            verify(mockRobotInstance).createScreenCapture(
                argThat(r -> r.x == 0 && r.y == 0 
                    && r.width == 200 && r.height == 300)
            );
        }
        
        private void setupRobotMock() {
            localRobotMock = mockConstruction(Robot.class, (mock, context) -> {
                mockRobotInstance = mock;
                when(mock.createScreenCapture(any(Rectangle.class)))
                    .thenReturn(mockImage);
            });
        }
    }
    
    @Nested
    @DisplayName("Error Handling")
    class ErrorHandling {
        
        @Test
        @DisplayName("Should throw RuntimeException when Robot creation fails")
        void shouldThrowWhenRobotCreationFails() {
            // Given
            GraphicsEnvironment graphicsEnv = mock(GraphicsEnvironment.class);
            GraphicsDevice device = mock(GraphicsDevice.class);
            DisplayMode displayMode = mock(DisplayMode.class);
            
            graphicsEnvMock = mockStatic(GraphicsEnvironment.class);
            graphicsEnvMock.when(GraphicsEnvironment::getLocalGraphicsEnvironment)
                .thenReturn(graphicsEnv);
            when(graphicsEnv.getDefaultScreenDevice()).thenReturn(device);
            when(device.getDisplayMode()).thenReturn(displayMode);
            when(displayMode.getWidth()).thenReturn(1920);
            when(displayMode.getHeight()).thenReturn(1080);
            
            // Make Robot constructor throw AWTException
            robotMock = mockConstruction(Robot.class, (mock, context) -> {
                throw new AWTException("Mocked AWTException");
            });
            
            // When/Then
            assertThrows(RuntimeException.class, PhysicalScreen::new);
        }
        
        @Test
        @DisplayName("Should fallback to super.capture on capture error")
        void shouldFallbackOnCaptureError() {
            // Given
            setupMockEnvironment(1920, 1080, 1920, 1080);
            
            robotMock = mockConstruction(Robot.class, (mock, context) -> {
                when(mock.createScreenCapture(any(Rectangle.class)))
                    .thenThrow(new RuntimeException("Capture failed"));
            });
            
            // Mock the parent Screen class behavior
            physicalScreen = spy(new PhysicalScreen());
            doReturn(mock(ScreenImage.class))
                .when((Screen) physicalScreen).capture(anyInt(), anyInt(), anyInt(), anyInt());
            
            // When
            ScreenImage result = physicalScreen.capture(0, 0, 100, 100);
            
            // Then
            assertNotNull(result);
            verify((Screen) physicalScreen).capture(0, 0, 100, 100);
        }
    }
    
    @Nested
    @DisplayName("Integration Tests")
    class IntegrationTests {
        
        @Test
        @DisplayName("Should work with various screen resolutions")
        void shouldWorkWithVariousResolutions() {
            int[][] resolutions = {
                {1920, 1080},  // Full HD
                {2560, 1440},  // QHD
                {3840, 2160},  // 4K
                {1366, 768},   // Common laptop
                {1280, 720}    // HD
            };
            
            for (int[] res : resolutions) {
                setupMockEnvironment(res[0], res[1], res[0], res[1]);
                
                PhysicalScreen screen = new PhysicalScreen();
                
                assertEquals(res[0], screen.getPhysicalResolution().width);
                assertEquals(res[1], screen.getPhysicalResolution().height);
                assertFalse(screen.isScalingCompensated());
                
                // Clean up for next iteration
                graphicsEnvMock.close();
                toolkitMock.close();
            }
        }
        
        @Test
        @DisplayName("Should handle common DPI scaling scenarios")
        void shouldHandleCommonDpiScalings() {
            Object[][] scenarios = {
                {3840, 2160, 1920, 1080, 2.0f},   // 200% scaling on 4K
                {2880, 1620, 1920, 1080, 1.5f},   // 150% scaling
                {2400, 1350, 1920, 1080, 1.25f},  // 125% scaling
                {1920, 1200, 1600, 1000, 1.2f},   // 120% scaling
                {3200, 1800, 2560, 1440, 1.25f}   // 125% on QHD
            };
            
            for (Object[] scenario : scenarios) {
                setupMockEnvironment((int)scenario[0], (int)scenario[1], 
                                   (int)scenario[2], (int)scenario[3]);
                
                PhysicalScreen screen = new PhysicalScreen();
                
                assertTrue(screen.isScalingCompensated());
                assertEquals((float)scenario[4], screen.getScaleFactor(), 0.01f);
                
                // Clean up for next iteration
                graphicsEnvMock.close();
                toolkitMock.close();
            }
        }
    }
    
    // Helper method to setup mock environment
    private void setupMockEnvironment(int physicalWidth, int physicalHeight,
                                     int logicalWidth, int logicalHeight) {
        // Close any existing mocks first
        if (graphicsEnvMock != null) {
            graphicsEnvMock.close();
        }
        if (toolkitMock != null) {
            toolkitMock.close();
        }
        if (robotMock != null) {
            robotMock.close();
        }
        
        // Mock GraphicsEnvironment
        GraphicsEnvironment graphicsEnv = mock(GraphicsEnvironment.class);
        GraphicsDevice device = mock(GraphicsDevice.class);
        DisplayMode displayMode = mock(DisplayMode.class);
        
        graphicsEnvMock = mockStatic(GraphicsEnvironment.class);
        graphicsEnvMock.when(GraphicsEnvironment::getLocalGraphicsEnvironment)
            .thenReturn(graphicsEnv);
        when(graphicsEnv.getDefaultScreenDevice()).thenReturn(device);
        when(device.getDisplayMode()).thenReturn(displayMode);
        when(displayMode.getWidth()).thenReturn(physicalWidth);
        when(displayMode.getHeight()).thenReturn(physicalHeight);
        
        // Mock Toolkit for logical resolution
        Toolkit toolkit = mock(Toolkit.class);
        Dimension screenSize = new Dimension(logicalWidth, logicalHeight);
        
        toolkitMock = mockStatic(Toolkit.class);
        toolkitMock.when(Toolkit::getDefaultToolkit).thenReturn(toolkit);
        when(toolkit.getScreenSize()).thenReturn(screenSize);
        
        // Mock Robot creation (without throwing exception)
        robotMock = mockConstruction(Robot.class);
    }
}