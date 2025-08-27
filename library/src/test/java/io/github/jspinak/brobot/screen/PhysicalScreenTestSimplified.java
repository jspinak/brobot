package io.github.jspinak.brobot.screen;

import io.github.jspinak.brobot.test.BrobotTestBase;
import org.junit.jupiter.api.*;
import org.mockito.MockedConstruction;
import org.mockito.MockedStatic;
import org.sikuli.script.ScreenImage;

import java.awt.*;
import java.awt.image.BufferedImage;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

/**
 * Simplified test suite for PhysicalScreen focusing on core functionality.
 */
@DisplayName("PhysicalScreen Simplified Tests")
public class PhysicalScreenTestSimplified extends BrobotTestBase {
    
    private PhysicalScreen physicalScreen;
    private MockedStatic<GraphicsEnvironment> graphicsEnvMock;
    private MockedStatic<Toolkit> toolkitMock;
    private MockedConstruction<Robot> robotMock;
    
    @AfterEach
    void tearDown() {
        if (graphicsEnvMock != null) {
            graphicsEnvMock.close();
            graphicsEnvMock = null;
        }
        if (toolkitMock != null) {
            toolkitMock.close();
            toolkitMock = null;
        }
        if (robotMock != null) {
            robotMock.close();
            robotMock = null;
        }
    }
    
    @Test
    @DisplayName("Should create PhysicalScreen without scaling")
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
    @DisplayName("Should detect 200% DPI scaling")
    void shouldDetect200PercentScaling() {
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
    @DisplayName("Should detect 150% DPI scaling")
    void shouldDetect150PercentScaling() {
        // Given - 150% scaling
        setupMockEnvironment(2880, 1620, 1920, 1080);
        
        // When
        physicalScreen = new PhysicalScreen();
        
        // Then
        assertTrue(physicalScreen.isScalingCompensated());
        assertEquals(1.5f, physicalScreen.getScaleFactor(), 0.01f);
    }
    
    @Test
    @DisplayName("Should handle Robot creation failure")
    void shouldHandleRobotCreationFailure() {
        // Given
        setupMockEnvironmentWithRobotFailure();
        
        // When/Then
        assertThrows(RuntimeException.class, PhysicalScreen::new);
    }
    
    @Test
    @DisplayName("Should get physical resolution")
    void shouldGetPhysicalResolution() {
        // Given
        setupMockEnvironment(2560, 1440, 2560, 1440);
        
        // When
        physicalScreen = new PhysicalScreen();
        Dimension resolution = physicalScreen.getPhysicalResolution();
        
        // Then
        assertEquals(2560, resolution.width);
        assertEquals(1440, resolution.height);
    }
    
    private void setupMockEnvironment(int physicalWidth, int physicalHeight,
                                     int logicalWidth, int logicalHeight) {
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
    
    private void setupMockEnvironmentWithRobotFailure() {
        // Mock GraphicsEnvironment
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
        
        // Mock Toolkit
        Toolkit toolkit = mock(Toolkit.class);
        Dimension screenSize = new Dimension(1920, 1080);
        
        toolkitMock = mockStatic(Toolkit.class);
        toolkitMock.when(Toolkit::getDefaultToolkit).thenReturn(toolkit);
        when(toolkit.getScreenSize()).thenReturn(screenSize);
        
        // Mock Robot to throw AWTException
        robotMock = mockConstruction(Robot.class, (mock, context) -> {
            throw new AWTException("Mocked AWTException");
        });
    }
}