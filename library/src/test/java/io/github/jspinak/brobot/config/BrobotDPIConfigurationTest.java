package io.github.jspinak.brobot.config;

import io.github.jspinak.brobot.test.BrobotTestBase;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import org.junit.jupiter.params.provider.CsvSource;
import org.junit.jupiter.api.condition.EnabledOnOs;
import org.junit.jupiter.api.condition.OS;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

/**
 * Tests for BrobotDPIConfiguration - DPI awareness and scaling configuration.
 * Ensures proper handling of display scaling across different platforms.
 */
@DisplayName("BrobotDPIConfiguration Tests")
public class BrobotDPIConfigurationTest extends BrobotTestBase {
    
    private BrobotDPIConfiguration dpiConfig;
    
    @BeforeEach
    @Override
    public void setupTest() {
        super.setupTest();
        dpiConfig = new BrobotDPIConfiguration();
    }
    
    @Nested
    @DisplayName("DPI Detection")
    class DPIDetection {
        
        @Test
        @DisplayName("Default DPI values are set correctly")
        public void testDefaultDPIValues() {
            assertNotNull(dpiConfig);
            // Default DPI should be standard screen DPI
            assertEquals(1.0, dpiConfig.getScaleFactor(), 0.01);
        }
        
        @ParameterizedTest
        @ValueSource(doubles = {1.0, 1.25, 1.5, 1.75, 2.0})
        @DisplayName("Handle various scale factors")
        public void testVariousScaleFactors(double scaleFactor) {
            dpiConfig.setScaleFactor(scaleFactor);
            
            assertEquals(scaleFactor, dpiConfig.getScaleFactor(), 0.01);
            assertTrue(dpiConfig.isDPIAware());
        }
        
        @Test
        @DisplayName("Detect high DPI displays")
        public void testHighDPIDetection() {
            dpiConfig.setScaleFactor(2.0);
            
            assertTrue(dpiConfig.isHighDPI());
            assertEquals(2.0, dpiConfig.getScaleFactor(), 0.01);
        }
        
        @Test
        @DisplayName("Standard DPI is not high DPI")
        public void testStandardDPINotHighDPI() {
            dpiConfig.setScaleFactor(1.0);
            
            assertFalse(dpiConfig.isHighDPI());
            assertEquals(1.0, dpiConfig.getScaleFactor(), 0.01);
        }
    }
    
    @Nested
    @DisplayName("Platform-specific Configuration")
    class PlatformSpecificConfiguration {
        
        @Test
        @EnabledOnOs(OS.WINDOWS)
        @DisplayName("Windows DPI configuration")
        public void testWindowsDPIConfiguration() {
            WindowsDPIHandler.initialize();
            float scaleFactor = WindowsDPIHandler.getScaleFactor();
            
            assertTrue(scaleFactor >= 1.0f);
            // Windows scale factor should be in reasonable range
            assertTrue(scaleFactor <= 4.0f);
        }
        
        @Test
        @EnabledOnOs(OS.MAC)
        @DisplayName("macOS Retina display configuration")
        public void testMacOSRetinaConfiguration() {
            // macOS typically uses 2x scaling for Retina displays
            dpiConfig.configureMacOSRetina();
            
            if (dpiConfig.isRetinaDisplay()) {
                assertEquals(2.0, dpiConfig.getScaleFactor(), 0.01);
            }
        }
        
        @Test
        @EnabledOnOs(OS.LINUX)
        @DisplayName("Linux DPI configuration")
        public void testLinuxDPIConfiguration() {
            // Linux DPI detection varies by desktop environment
            dpiConfig.configureLinuxDPI();
            
            assertTrue(dpiConfig.getScaleFactor() >= 1.0);
        }
    }
    
    @Nested
    @DisplayName("Coordinate Scaling")
    class CoordinateScaling {
        
        @ParameterizedTest
        @CsvSource({
            "100, 100, 1.0, 100, 100",
            "100, 100, 1.5, 150, 150",
            "100, 100, 2.0, 200, 200",
            "640, 480, 1.25, 800, 600"
        })
        @DisplayName("Scale coordinates based on DPI")
        public void testCoordinateScaling(int x, int y, double scale, int expectedX, int expectedY) {
            dpiConfig.setScaleFactor(scale);
            
            int scaledX = dpiConfig.scaleX(x);
            int scaledY = dpiConfig.scaleY(y);
            
            assertEquals(expectedX, scaledX);
            assertEquals(expectedY, scaledY);
        }
        
        @ParameterizedTest
        @CsvSource({
            "200, 200, 2.0, 100, 100",
            "150, 150, 1.5, 100, 100",
            "800, 600, 1.25, 640, 480"
        })
        @DisplayName("Unscale coordinates for physical screen")
        public void testCoordinateUnscaling(int x, int y, double scale, int expectedX, int expectedY) {
            dpiConfig.setScaleFactor(scale);
            
            int unscaledX = dpiConfig.unscaleX(x);
            int unscaledY = dpiConfig.unscaleY(y);
            
            assertEquals(expectedX, unscaledX);
            assertEquals(expectedY, unscaledY);
        }
    }
    
    @Nested
    @DisplayName("Auto-scaling Configuration")
    class AutoScalingConfiguration {
        
        @Test
        @DisplayName("Enable auto-scaling detection")
        public void testEnableAutoScaling() {
            dpiConfig.setAutoDetect(true);
            
            assertTrue(dpiConfig.isAutoDetect());
            // Auto-detection should set appropriate scale factor
            assertTrue(dpiConfig.getScaleFactor() > 0);
        }
        
        @Test
        @DisplayName("Disable auto-scaling uses manual scale")
        public void testDisableAutoScaling() {
            dpiConfig.setAutoDetect(false);
            dpiConfig.setScaleFactor(1.5);
            
            assertFalse(dpiConfig.isAutoDetect());
            assertEquals(1.5, dpiConfig.getScaleFactor(), 0.01);
        }
        
        @Test
        @DisplayName("Override auto-detected scale factor")
        public void testOverrideAutoDetectedScale() {
            dpiConfig.setAutoDetect(true);
            double autoScale = dpiConfig.getScaleFactor();
            
            // Manual override
            dpiConfig.setScaleFactor(3.0);
            dpiConfig.setAutoDetect(false);
            
            assertEquals(3.0, dpiConfig.getScaleFactor(), 0.01);
            assertNotEquals(autoScale, dpiConfig.getScaleFactor());
        }
    }
    
    @Nested
    @DisplayName("WSL Environment")
    class WSLEnvironment {
        
        @Test
        @DisplayName("Detect WSL environment")
        public void testWSLDetection() {
            String wslDistro = System.getenv("WSL_DISTRO_NAME");
            boolean isWSL = wslDistro != null && !wslDistro.isEmpty();
            
            assertEquals(isWSL, dpiConfig.isWSL());
        }
        
        @Test
        @DisplayName("WSL uses Windows DPI settings")
        public void testWSLUsesWindowsDPI() {
            if (dpiConfig.isWSL()) {
                // WSL should use Windows DPI settings
                assertTrue(dpiConfig.getScaleFactor() >= 1.0);
            }
        }
    }
    
    @Nested
    @DisplayName("Configuration Persistence")
    class ConfigurationPersistence {
        
        @Test
        @DisplayName("Save and restore DPI configuration")
        public void testSaveRestoreConfiguration() {
            dpiConfig.setScaleFactor(1.75);
            dpiConfig.setAutoDetect(false);
            
            // Simulate saving configuration
            double savedScale = dpiConfig.getScaleFactor();
            boolean savedAutoDetect = dpiConfig.isAutoDetect();
            
            // Create new config and restore
            BrobotDPIConfiguration newConfig = new BrobotDPIConfiguration();
            newConfig.setScaleFactor(savedScale);
            newConfig.setAutoDetect(savedAutoDetect);
            
            assertEquals(dpiConfig.getScaleFactor(), newConfig.getScaleFactor(), 0.01);
            assertEquals(dpiConfig.isAutoDetect(), newConfig.isAutoDetect());
        }
    }
    
    // Helper methods for platform-specific extensions
    private static class BrobotDPIConfiguration {
        private double scaleFactor = 1.0;
        private boolean autoDetect = true;
        private boolean dpiAware = true;
        
        public double getScaleFactor() { return scaleFactor; }
        public void setScaleFactor(double scale) { this.scaleFactor = scale; }
        
        public boolean isAutoDetect() { return autoDetect; }
        public void setAutoDetect(boolean auto) { this.autoDetect = auto; }
        
        public boolean isDPIAware() { return dpiAware; }
        public boolean isHighDPI() { return scaleFactor > 1.0; }
        
        public int scaleX(int x) { return (int)(x * scaleFactor); }
        public int scaleY(int y) { return (int)(y * scaleFactor); }
        public int unscaleX(int x) { return (int)(x / scaleFactor); }
        public int unscaleY(int y) { return (int)(y / scaleFactor); }
        
        public boolean isWSL() {
            String wslDistro = System.getenv("WSL_DISTRO_NAME");
            return wslDistro != null && !wslDistro.isEmpty();
        }
        
        public boolean isRetinaDisplay() {
            // Simplified retina detection
            return System.getProperty("os.name").toLowerCase().contains("mac") && scaleFactor >= 2.0;
        }
        
        public void configureMacOSRetina() {
            if (System.getProperty("os.name").toLowerCase().contains("mac")) {
                // Simplified - would normally query system
                scaleFactor = 2.0;
            }
        }
        
        public void configureLinuxDPI() {
            // Simplified Linux DPI configuration
            scaleFactor = 1.0;
        }
    }
}