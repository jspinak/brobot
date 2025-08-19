package io.github.jspinak.brobot.config;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.MockedStatic;
import org.mockito.junit.jupiter.MockitoExtension;
import org.sikuli.basics.Settings;

import java.awt.*;
import java.io.ByteArrayOutputStream;
import java.io.PrintStream;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

/**
 * Comprehensive test suite for BrobotDPIConfiguration class.
 * Tests DPI scaling detection and compensation for pattern matching.
 * 
 * NOTE: These tests require proper GUI environment and mock static AWT/Swing methods.
 * Currently disabled as they conflict with actual static method calls.
 */
@ExtendWith(MockitoExtension.class)
@DisplayName("BrobotDPIConfiguration Tests")
class BrobotDPIConfigurationTest {
    
    private BrobotDPIConfiguration dpiConfiguration;
    private ByteArrayOutputStream outputStream;
    private PrintStream originalOut;
    
    @BeforeEach
    void setUp() {
        dpiConfiguration = new BrobotDPIConfiguration();
        
        // Capture console output
        outputStream = new ByteArrayOutputStream();
        originalOut = System.out;
        System.setOut(new PrintStream(outputStream));
        
        // Reset Settings.AlwaysResize before each test
        Settings.AlwaysResize = 1.0f;
    }
    
    @BeforeEach
    void tearDown() {
        System.setOut(originalOut);
    }
    
    @Nested
    @DisplayName("DPI Scaling Detection Tests")
    class DPIScalingDetectionTests {
        
        @Test
        @DisplayName("Should detect no scaling (100%)")
        void testNoScaling() {
            try (MockedStatic<GraphicsEnvironment> geMock = mockStatic(GraphicsEnvironment.class);
                 MockedStatic<Toolkit> toolkitMock = mockStatic(Toolkit.class)) {
                
                // Setup environment with no scaling
                GraphicsEnvironment mockEnv = mock(GraphicsEnvironment.class);
                GraphicsDevice mockDevice = mock(GraphicsDevice.class);
                DisplayMode mockDisplayMode = mock(DisplayMode.class);
                Toolkit mockToolkit = mock(Toolkit.class);
                
                geMock.when(GraphicsEnvironment::getLocalGraphicsEnvironment).thenReturn(mockEnv);
                when(mockEnv.getDefaultScreenDevice()).thenReturn(mockDevice);
                when(mockDevice.getDisplayMode()).thenReturn(mockDisplayMode);
                when(mockDisplayMode.getWidth()).thenReturn(1920);
                when(mockDisplayMode.getHeight()).thenReturn(1080);
                
                toolkitMock.when(Toolkit::getDefaultToolkit).thenReturn(mockToolkit);
                when(mockToolkit.getScreenSize()).thenReturn(new Dimension(1920, 1080));
                
                // Execute
                dpiConfiguration.configureDPIScaling();
                
                // Verify
                assertEquals(1.0f, Settings.AlwaysResize, 0.001f);
                String output = outputStream.toString();
                assertTrue(output.contains("No DPI scaling detected"));
            }
        }
        
        @Test
        @DisplayName("Should detect 125% scaling")
        void test125PercentScaling() {
            try (MockedStatic<GraphicsEnvironment> geMock = mockStatic(GraphicsEnvironment.class);
                 MockedStatic<Toolkit> toolkitMock = mockStatic(Toolkit.class)) {
                
                // Setup environment with 125% scaling
                GraphicsEnvironment mockEnv = mock(GraphicsEnvironment.class);
                GraphicsDevice mockDevice = mock(GraphicsDevice.class);
                DisplayMode mockDisplayMode = mock(DisplayMode.class);
                Toolkit mockToolkit = mock(Toolkit.class);
                
                geMock.when(GraphicsEnvironment::getLocalGraphicsEnvironment).thenReturn(mockEnv);
                when(mockEnv.getDefaultScreenDevice()).thenReturn(mockDevice);
                when(mockDevice.getDisplayMode()).thenReturn(mockDisplayMode);
                when(mockDisplayMode.getWidth()).thenReturn(1920);
                when(mockDisplayMode.getHeight()).thenReturn(1080);
                
                toolkitMock.when(Toolkit::getDefaultToolkit).thenReturn(mockToolkit);
                // 125% scaling: logical resolution is smaller
                when(mockToolkit.getScreenSize()).thenReturn(new Dimension(1536, 864));
                
                // Execute
                dpiConfiguration.configureDPIScaling();
                
                // Verify
                assertEquals(0.8f, Settings.AlwaysResize, 0.01f);
                String output = outputStream.toString();
                assertTrue(output.contains("DPI scaling detected"));
                assertTrue(output.contains("125%"));
            }
        }
        
        @Test
        @DisplayName("Should detect 150% scaling")
        void test150PercentScaling() {
            try (MockedStatic<GraphicsEnvironment> geMock = mockStatic(GraphicsEnvironment.class);
                 MockedStatic<Toolkit> toolkitMock = mockStatic(Toolkit.class)) {
                
                // Setup environment with 150% scaling
                GraphicsEnvironment mockEnv = mock(GraphicsEnvironment.class);
                GraphicsDevice mockDevice = mock(GraphicsDevice.class);
                DisplayMode mockDisplayMode = mock(DisplayMode.class);
                Toolkit mockToolkit = mock(Toolkit.class);
                
                geMock.when(GraphicsEnvironment::getLocalGraphicsEnvironment).thenReturn(mockEnv);
                when(mockEnv.getDefaultScreenDevice()).thenReturn(mockDevice);
                when(mockDevice.getDisplayMode()).thenReturn(mockDisplayMode);
                when(mockDisplayMode.getWidth()).thenReturn(1920);
                when(mockDisplayMode.getHeight()).thenReturn(1080);
                
                toolkitMock.when(Toolkit::getDefaultToolkit).thenReturn(mockToolkit);
                // 150% scaling: logical resolution is smaller
                when(mockToolkit.getScreenSize()).thenReturn(new Dimension(1280, 720));
                
                // Execute
                dpiConfiguration.configureDPIScaling();
                
                // Verify
                assertEquals(0.667f, Settings.AlwaysResize, 0.01f);
                String output = outputStream.toString();
                assertTrue(output.contains("DPI scaling detected"));
                assertTrue(output.contains("150%"));
            }
        }
        
        @Test
        @DisplayName("Should detect reverse scaling (rare)")
        void testReverseScaling() {
            try (MockedStatic<GraphicsEnvironment> geMock = mockStatic(GraphicsEnvironment.class);
                 MockedStatic<Toolkit> toolkitMock = mockStatic(Toolkit.class)) {
                
                // Setup environment with reverse scaling (logical > physical)
                GraphicsEnvironment mockEnv = mock(GraphicsEnvironment.class);
                GraphicsDevice mockDevice = mock(GraphicsDevice.class);
                DisplayMode mockDisplayMode = mock(DisplayMode.class);
                Toolkit mockToolkit = mock(Toolkit.class);
                
                geMock.when(GraphicsEnvironment::getLocalGraphicsEnvironment).thenReturn(mockEnv);
                when(mockEnv.getDefaultScreenDevice()).thenReturn(mockDevice);
                when(mockDevice.getDisplayMode()).thenReturn(mockDisplayMode);
                when(mockDisplayMode.getWidth()).thenReturn(1280);
                when(mockDisplayMode.getHeight()).thenReturn(720);
                
                toolkitMock.when(Toolkit::getDefaultToolkit).thenReturn(mockToolkit);
                when(mockToolkit.getScreenSize()).thenReturn(new Dimension(1920, 1080));
                
                // Execute
                dpiConfiguration.configureDPIScaling();
                
                // Verify
                assertEquals(1.5f, Settings.AlwaysResize, 0.01f);
                String output = outputStream.toString();
                assertTrue(output.contains("Reverse DPI scaling detected"));
            }
        }
    }
    
    @Nested
    @DisplayName("Environment Detection Tests")
    class EnvironmentDetectionTests {
        
        @Test
        @DisplayName("Should apply Windows default when detection fails")
        void testWindowsDefaultFallback() {
            try (MockedStatic<GraphicsEnvironment> geMock = mockStatic(GraphicsEnvironment.class);
                 MockedStatic<Toolkit> toolkitMock = mockStatic(Toolkit.class);
                 MockedStatic<System> sysMock = mockStatic(System.class)) {
                
                // Setup environment where detection fails
                GraphicsEnvironment mockEnv = mock(GraphicsEnvironment.class);
                GraphicsDevice mockDevice = mock(GraphicsDevice.class);
                DisplayMode mockDisplayMode = mock(DisplayMode.class);
                Toolkit mockToolkit = mock(Toolkit.class);
                
                geMock.when(GraphicsEnvironment::getLocalGraphicsEnvironment).thenReturn(mockEnv);
                when(mockEnv.getDefaultScreenDevice()).thenReturn(mockDevice);
                when(mockDevice.getDisplayMode()).thenReturn(mockDisplayMode);
                when(mockDisplayMode.getWidth()).thenReturn(0); // Invalid width
                
                toolkitMock.when(Toolkit::getDefaultToolkit).thenReturn(mockToolkit);
                when(mockToolkit.getScreenSize()).thenReturn(new Dimension(1920, 1080));
                
                sysMock.when(() -> System.getProperty("os.name")).thenReturn("Windows 10");
                sysMock.when(() -> System.getenv("WSL_DISTRO_NAME")).thenReturn(null);
                
                // Execute
                dpiConfiguration.configureDPIScaling();
                
                // Verify
                assertEquals(0.8f, Settings.AlwaysResize, 0.001f);
                String output = outputStream.toString();
                assertTrue(output.contains("Running on Windows/WSL"));
                assertTrue(output.contains("Applied default: Settings.AlwaysResize = 0.8f"));
            }
        }
        
        @Test
        @DisplayName("Should detect WSL environment")
        void testWSLDetection() {
            try (MockedStatic<GraphicsEnvironment> geMock = mockStatic(GraphicsEnvironment.class);
                 MockedStatic<Toolkit> toolkitMock = mockStatic(Toolkit.class);
                 MockedStatic<System> sysMock = mockStatic(System.class)) {
                
                // Setup WSL environment
                GraphicsEnvironment mockEnv = mock(GraphicsEnvironment.class);
                GraphicsDevice mockDevice = mock(GraphicsDevice.class);
                DisplayMode mockDisplayMode = mock(DisplayMode.class);
                Toolkit mockToolkit = mock(Toolkit.class);
                
                geMock.when(GraphicsEnvironment::getLocalGraphicsEnvironment).thenReturn(mockEnv);
                when(mockEnv.getDefaultScreenDevice()).thenReturn(mockDevice);
                when(mockDevice.getDisplayMode()).thenReturn(mockDisplayMode);
                when(mockDisplayMode.getWidth()).thenReturn(0); // Invalid width
                
                toolkitMock.when(Toolkit::getDefaultToolkit).thenReturn(mockToolkit);
                when(mockToolkit.getScreenSize()).thenReturn(new Dimension(1920, 1080));
                
                sysMock.when(() -> System.getProperty("os.name")).thenReturn("Linux");
                sysMock.when(() -> System.getenv("WSL_DISTRO_NAME")).thenReturn("Ubuntu");
                
                // Execute
                dpiConfiguration.configureDPIScaling();
                
                // Verify
                assertEquals(0.8f, Settings.AlwaysResize, 0.001f);
                String output = outputStream.toString();
                assertTrue(output.contains("Running on Windows/WSL"));
            }
        }
        
        @Test
        @DisplayName("Should not apply defaults on macOS")
        void testMacOSNoDefault() {
            try (MockedStatic<GraphicsEnvironment> geMock = mockStatic(GraphicsEnvironment.class);
                 MockedStatic<Toolkit> toolkitMock = mockStatic(Toolkit.class);
                 MockedStatic<System> sysMock = mockStatic(System.class)) {
                
                // Setup macOS environment
                GraphicsEnvironment mockEnv = mock(GraphicsEnvironment.class);
                GraphicsDevice mockDevice = mock(GraphicsDevice.class);
                DisplayMode mockDisplayMode = mock(DisplayMode.class);
                Toolkit mockToolkit = mock(Toolkit.class);
                
                geMock.when(GraphicsEnvironment::getLocalGraphicsEnvironment).thenReturn(mockEnv);
                when(mockEnv.getDefaultScreenDevice()).thenReturn(mockDevice);
                when(mockDevice.getDisplayMode()).thenReturn(mockDisplayMode);
                when(mockDisplayMode.getWidth()).thenReturn(0); // Invalid width
                
                toolkitMock.when(Toolkit::getDefaultToolkit).thenReturn(mockToolkit);
                when(mockToolkit.getScreenSize()).thenReturn(new Dimension(1920, 1080));
                
                sysMock.when(() -> System.getProperty("os.name")).thenReturn("Mac OS X");
                sysMock.when(() -> System.getenv("WSL_DISTRO_NAME")).thenReturn(null);
                
                // Execute
                dpiConfiguration.configureDPIScaling();
                
                // Verify
                assertEquals(1.0f, Settings.AlwaysResize, 0.001f); // Should remain at default
                String output = outputStream.toString();
                assertFalse(output.contains("Running on Windows/WSL"));
            }
        }
    }
    
    @Nested
    @DisplayName("Error Handling Tests")
    class ErrorHandlingTests {
        
        @Test
        @DisplayName("Should handle exception during configuration")
        void testExceptionHandling() {
            try (MockedStatic<GraphicsEnvironment> geMock = mockStatic(GraphicsEnvironment.class)) {
                
                // Make GraphicsEnvironment throw exception
                geMock.when(GraphicsEnvironment::getLocalGraphicsEnvironment)
                    .thenThrow(new RuntimeException("Graphics error"));
                
                // Execute
                dpiConfiguration.configureDPIScaling();
                
                // Verify fallback is applied
                assertEquals(0.8f, Settings.AlwaysResize, 0.001f);
                String output = outputStream.toString();
                assertTrue(output.contains("Error configuring DPI scaling"));
                assertTrue(output.contains("Applying fallback configuration"));
                assertTrue(output.contains("Fallback: Settings.AlwaysResize = 0.8f"));
            }
        }
        
        @Test
        @DisplayName("Should handle HeadlessException")
        void testHeadlessException() {
            try (MockedStatic<GraphicsEnvironment> geMock = mockStatic(GraphicsEnvironment.class)) {
                
                // Make GraphicsEnvironment throw HeadlessException
                geMock.when(GraphicsEnvironment::getLocalGraphicsEnvironment)
                    .thenThrow(new HeadlessException("No display"));
                
                // Execute
                dpiConfiguration.configureDPIScaling();
                
                // Verify fallback is applied
                assertEquals(0.8f, Settings.AlwaysResize, 0.001f);
                String output = outputStream.toString();
                assertTrue(output.contains("Error configuring DPI scaling"));
            }
        }
    }
    
    @Nested
    @DisplayName("Manual Scaling Tests")
    class ManualScalingTests {
        
        @Test
        @DisplayName("Should set manual scaling for 125%")
        void testManualScaling125() {
            BrobotDPIConfiguration.setManualScaling(125);
            
            assertEquals(0.8f, Settings.AlwaysResize, 0.001f);
            String output = outputStream.toString();
            assertTrue(output.contains("Manual DPI scaling set"));
            assertTrue(output.contains("Windows scaling: 125%"));
            assertTrue(output.contains("Settings.AlwaysResize: 0.8"));
        }
        
        @Test
        @DisplayName("Should set manual scaling for 150%")
        void testManualScaling150() {
            BrobotDPIConfiguration.setManualScaling(150);
            
            assertEquals(0.667f, Settings.AlwaysResize, 0.001f);
            String output = outputStream.toString();
            assertTrue(output.contains("Windows scaling: 150%"));
            assertTrue(output.contains("Settings.AlwaysResize: 0.66"));
        }
        
        @Test
        @DisplayName("Should set manual scaling for 175%")
        void testManualScaling175() {
            BrobotDPIConfiguration.setManualScaling(175);
            
            assertEquals(0.571f, Settings.AlwaysResize, 0.001f);
            String output = outputStream.toString();
            assertTrue(output.contains("Windows scaling: 175%"));
        }
        
        @Test
        @DisplayName("Should set manual scaling for 100%")
        void testManualScaling100() {
            BrobotDPIConfiguration.setManualScaling(100);
            
            assertEquals(1.0f, Settings.AlwaysResize, 0.001f);
            String output = outputStream.toString();
            assertTrue(output.contains("Windows scaling: 100%"));
            assertTrue(output.contains("Settings.AlwaysResize: 1.0"));
        }
        
        @Test
        @DisplayName("Should set manual scaling for 200%")
        void testManualScaling200() {
            BrobotDPIConfiguration.setManualScaling(200);
            
            assertEquals(0.5f, Settings.AlwaysResize, 0.001f);
            String output = outputStream.toString();
            assertTrue(output.contains("Windows scaling: 200%"));
            assertTrue(output.contains("Settings.AlwaysResize: 0.5"));
        }
    }
    
    @Nested
    @DisplayName("Console Output Tests")
    class ConsoleOutputTests {
        
        @Test
        @DisplayName("Should output configuration header")
        void testConfigurationHeader() {
            try (MockedStatic<GraphicsEnvironment> geMock = mockStatic(GraphicsEnvironment.class);
                 MockedStatic<Toolkit> toolkitMock = mockStatic(Toolkit.class)) {
                
                setupBasicEnvironment(geMock, toolkitMock);
                
                // Execute
                dpiConfiguration.configureDPIScaling();
                
                // Verify
                String output = outputStream.toString();
                assertTrue(output.contains("=== Brobot DPI Configuration ==="));
                assertTrue(output.contains("=== Current SikuliX Settings ==="));
            }
        }
        
        @Test
        @DisplayName("Should output resolution information")
        void testResolutionOutput() {
            try (MockedStatic<GraphicsEnvironment> geMock = mockStatic(GraphicsEnvironment.class);
                 MockedStatic<Toolkit> toolkitMock = mockStatic(Toolkit.class)) {
                
                setupBasicEnvironment(geMock, toolkitMock);
                
                // Execute
                dpiConfiguration.configureDPIScaling();
                
                // Verify
                String output = outputStream.toString();
                assertTrue(output.contains("Physical resolution: 1920x1080"));
                assertTrue(output.contains("Logical resolution: 1920x1080"));
            }
        }
        
        @Test
        @DisplayName("Should output current settings")
        void testCurrentSettingsOutput() {
            try (MockedStatic<GraphicsEnvironment> geMock = mockStatic(GraphicsEnvironment.class);
                 MockedStatic<Toolkit> toolkitMock = mockStatic(Toolkit.class)) {
                
                setupBasicEnvironment(geMock, toolkitMock);
                Settings.MinSimilarity = 0.7;
                
                // Execute
                dpiConfiguration.configureDPIScaling();
                
                // Verify
                String output = outputStream.toString();
                assertTrue(output.contains("Settings.AlwaysResize:"));
                assertTrue(output.contains("Settings.MinSimilarity:"));
            }
        }
        
        private void setupBasicEnvironment(MockedStatic<GraphicsEnvironment> geMock,
                                          MockedStatic<Toolkit> toolkitMock) {
            GraphicsEnvironment mockEnv = mock(GraphicsEnvironment.class);
            GraphicsDevice mockDevice = mock(GraphicsDevice.class);
            DisplayMode mockDisplayMode = mock(DisplayMode.class);
            Toolkit mockToolkit = mock(Toolkit.class);
            
            geMock.when(GraphicsEnvironment::getLocalGraphicsEnvironment).thenReturn(mockEnv);
            when(mockEnv.getDefaultScreenDevice()).thenReturn(mockDevice);
            when(mockDevice.getDisplayMode()).thenReturn(mockDisplayMode);
            when(mockDisplayMode.getWidth()).thenReturn(1920);
            when(mockDisplayMode.getHeight()).thenReturn(1080);
            
            toolkitMock.when(Toolkit::getDefaultToolkit).thenReturn(mockToolkit);
            when(mockToolkit.getScreenSize()).thenReturn(new Dimension(1920, 1080));
        }
    }
}