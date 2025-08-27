package io.github.jspinak.brobot.config;

import io.github.jspinak.brobot.test.BrobotTestBase;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.mockito.MockedStatic;
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
 * This test validates that Brobot correctly detects and compensates for DPI scaling
 * to maintain high pattern matching accuracy (0.94+) even with Windows display scaling.
 */
@DisplayName("BrobotDPIConfiguration Tests")
class BrobotDPIConfigurationTest extends BrobotTestBase {
    
    private BrobotDPIConfiguration dpiConfiguration;
    private ByteArrayOutputStream outputStream;
    private ByteArrayOutputStream errorStream;
    private PrintStream originalOut;
    private PrintStream originalErr;
    
    @BeforeEach
    @Override
    public void setupTest() {
        super.setupTest(); // Enable mock mode from BrobotTestBase
        dpiConfiguration = new BrobotDPIConfiguration();
        
        // Capture console output and error streams
        outputStream = new ByteArrayOutputStream();
        errorStream = new ByteArrayOutputStream();
        originalOut = System.out;
        originalErr = System.err;
        System.setOut(new PrintStream(outputStream));
        System.setErr(new PrintStream(errorStream));
        
        // Reset Settings.AlwaysResize before each test
        Settings.AlwaysResize = 1.0f;
    }
    
    @AfterEach
    void tearDown() {
        System.setOut(originalOut);
        System.setErr(originalErr);
        // Reset to mock mode default
        Settings.AlwaysResize = 1.0f;
    }
    
    @Nested
    @DisplayName("DPI Scaling Detection Tests")
    class DPIScalingDetectionTests {
        
        @Test
        @DisplayName("Should detect no scaling (100%)")
        void testNoScaling() {
            // Create Dimension before mocking
            Dimension screenDimension = new Dimension(1920, 1080);
            
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
                when(mockToolkit.getScreenSize()).thenReturn(screenDimension);
                
                // Execute
                dpiConfiguration.configureDPIScaling();
                
                // Verify - in mock the sizes are equal so no scaling detected
                // But if we're in WSL, the default 0.8f may still be applied
                String output = outputStream.toString();
                
                if (System.getenv("WSL_DISTRO_NAME") != null) {
                    // In WSL, even with no scaling detected, default is applied
                    assertTrue(output.contains("DPI scaling detection inconclusive") || 
                              output.contains("No DPI scaling detected"));
                    // May have WSL default applied
                    assertTrue(Settings.AlwaysResize == 1.0f || Settings.AlwaysResize == 0.8f);
                } else {
                    assertEquals(1.0f, Settings.AlwaysResize, 0.001f);
                    assertTrue(output.contains("No DPI scaling detected"));
                }
            }
        }
        
        @Test
        @DisplayName("Should detect 125% scaling")
        void test125PercentScaling() {
            // Create Dimension before mocking
            Dimension screenDimension = new Dimension(1536, 864); // 125% scaling: logical resolution is smaller
            
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
                when(mockToolkit.getScreenSize()).thenReturn(screenDimension);
                
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
            // Create Dimension before mocking
            Dimension screenDimension = new Dimension(1280, 720); // 150% scaling: logical resolution is smaller
            
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
                when(mockToolkit.getScreenSize()).thenReturn(screenDimension);
                
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
            // Create Dimension before mocking
            Dimension screenDimension = new Dimension(1920, 1080);
            
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
                when(mockToolkit.getScreenSize()).thenReturn(screenDimension);
                
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
        @DisplayName("Should apply default when detection fails on current OS")
        void testDetectionFailureFallback() {
            // Create Dimension before mocking
            Dimension screenDimension = new Dimension(1920, 1080);
            
            try (MockedStatic<GraphicsEnvironment> geMock = mockStatic(GraphicsEnvironment.class);
                 MockedStatic<Toolkit> toolkitMock = mockStatic(Toolkit.class)) {
                
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
                when(mockToolkit.getScreenSize()).thenReturn(screenDimension);
                
                // Execute
                dpiConfiguration.configureDPIScaling();
                
                // Verify - on Windows/WSL it applies 0.8f, on other OS it stays 1.0f
                String osName = System.getProperty("os.name").toLowerCase();
                boolean isWindows = osName.contains("win");
                boolean isWSL = System.getenv("WSL_DISTRO_NAME") != null;
                
                if (isWindows || isWSL) {
                    assertEquals(0.8f, Settings.AlwaysResize, 0.001f);
                    String output = outputStream.toString();
                    assertTrue(output.contains("Running on Windows/WSL"));
                } else {
                    // On Mac/Linux without WSL, no default applied
                    assertEquals(1.0f, Settings.AlwaysResize, 0.001f);
                }
            }
        }
        
        @Test
        @DisplayName("Should detect current environment type")
        void testEnvironmentDetection() {
            // Create Dimension before mocking
            Dimension screenDimension = new Dimension(1920, 1080);
            
            try (MockedStatic<GraphicsEnvironment> geMock = mockStatic(GraphicsEnvironment.class);
                 MockedStatic<Toolkit> toolkitMock = mockStatic(Toolkit.class)) {
                
                // Setup environment
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
                when(mockToolkit.getScreenSize()).thenReturn(screenDimension);
                
                // Execute
                dpiConfiguration.configureDPIScaling();
                
                // Verify based on actual environment
                String output = outputStream.toString();
                assertTrue(output.contains("Brobot DPI Configuration"));
                
                // Check if we're in WSL
                if (System.getenv("WSL_DISTRO_NAME") != null) {
                    // In WSL, should detect as no scaling since mock returns same sizes
                    assertTrue(output.contains("No DPI scaling detected") || 
                              output.contains("DPI scaling detection"));
                }
            }
        }
        
        @Test
        @DisplayName("Should handle non-Windows platforms appropriately")
        void testNonWindowsPlatformHandling() {
            // Create Dimension before mocking
            Dimension screenDimension = new Dimension(1920, 1080);
            
            try (MockedStatic<GraphicsEnvironment> geMock = mockStatic(GraphicsEnvironment.class);
                 MockedStatic<Toolkit> toolkitMock = mockStatic(Toolkit.class)) {
                
                // Setup environment
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
                when(mockToolkit.getScreenSize()).thenReturn(screenDimension);
                
                // Execute
                dpiConfiguration.configureDPIScaling();
                
                // Verify
                String output = outputStream.toString();
                String osName = System.getProperty("os.name").toLowerCase();
                
                if (!osName.contains("win") && System.getenv("WSL_DISTRO_NAME") == null) {
                    // On non-Windows platforms, should detect no scaling with these mock values
                    assertTrue(output.contains("No DPI scaling detected"));
                    assertEquals(1.0f, Settings.AlwaysResize, 0.001f);
                }
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
                String errorOutput = errorStream.toString();
                assertTrue(errorOutput.contains("Error configuring DPI scaling"));
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
                String errorOutput = errorStream.toString();
                assertTrue(errorOutput.contains("Error configuring DPI scaling"));
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
            
            // Create Dimension first before mocking Toolkit
            Dimension screenDimension = new Dimension(1920, 1080);
            
            geMock.when(GraphicsEnvironment::getLocalGraphicsEnvironment).thenReturn(mockEnv);
            when(mockEnv.getDefaultScreenDevice()).thenReturn(mockDevice);
            when(mockDevice.getDisplayMode()).thenReturn(mockDisplayMode);
            when(mockDisplayMode.getWidth()).thenReturn(1920);
            when(mockDisplayMode.getHeight()).thenReturn(1080);
            
            toolkitMock.when(Toolkit::getDefaultToolkit).thenReturn(mockToolkit);
            when(mockToolkit.getScreenSize()).thenReturn(screenDimension);
        }
    }
}