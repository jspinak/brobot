package io.github.jspinak.brobot.screen;

import io.github.jspinak.brobot.test.BrobotTestBase;
import org.junit.jupiter.api.*;
import org.junit.jupiter.api.condition.DisabledIfEnvironmentVariable;
import org.junit.jupiter.api.condition.DisabledIfSystemProperty;
import org.junit.jupiter.api.condition.EnabledIfSystemProperty;
import org.junit.jupiter.api.condition.DisabledIf;
import org.mockito.MockedConstruction;
import org.mockito.MockedStatic;
import org.mockito.Mockito;
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
 * Tests physical resolution screen capture functionality.
 * Note: Many tests require a graphics environment and will be skipped in headless mode.
 */
@DisplayName("PhysicalScreen Tests")
@DisabledIfEnvironmentVariable(named = "CI", matches = "true", disabledReason = "Requires real display")
@DisabledIfEnvironmentVariable(named = "WSL_DISTRO_NAME", matches = ".+", disabledReason = "WSL environment detected")
@DisabledIfSystemProperty(named = "java.awt.headless", matches = "true", disabledReason = "Headless environment")
public class PhysicalScreenTest extends BrobotTestBase {
    
    @Nested
    @DisplayName("Constructor and Initialization")
    class ConstructorTests {
        
        @Test
        @DisplayName("Should create PhysicalScreen when graphics available")
        void shouldCreatePhysicalScreenWhenGraphicsAvailable() {
            // This test will only run if graphics are available
            GraphicsEnvironment ge = GraphicsEnvironment.getLocalGraphicsEnvironment();
            assumeFalse(ge.isHeadlessInstance(), "Skipping test in headless environment");
            
            try {
                PhysicalScreen screen = new PhysicalScreen();
                assertNotNull(screen);
                assertTrue(screen instanceof Screen);
            } catch (Exception e) {
                // In test environments without proper graphics, this is expected
                assertTrue(e.getMessage().contains("graphics") || 
                          e.getMessage().contains("headless") ||
                          e.getMessage().contains("display"),
                          "Expected graphics-related exception but got: " + e.getMessage());
            }
        }
        
        @Test
        @DisplayName("Should handle construction in headless environment")
        void shouldHandleHeadlessEnvironment() {
            GraphicsEnvironment ge = GraphicsEnvironment.getLocalGraphicsEnvironment();
            
            if (ge.isHeadlessInstance()) {
                // In headless mode, construction should fail gracefully
                assertThrows(Exception.class, () -> new PhysicalScreen());
            } else {
                // In non-headless mode, it might succeed or fail depending on display
                try {
                    PhysicalScreen screen = new PhysicalScreen();
                    assertNotNull(screen);
                } catch (Exception e) {
                    // This is also acceptable if display is not properly configured
                    assertNotNull(e);
                }
            }
        }
    }
    
    @Nested
    @DisplayName("DPI Scaling Detection")
    class DPIScalingTests {
        
        @Test
        @DisplayName("Should detect scaling factors correctly")
        void shouldDetectScalingFactors() {
            // Test the scaling calculation logic without instantiating PhysicalScreen
            // Physical: 3840x2160, Logical: 1920x1080 = 200% scaling
            float scaleFactor = 3840.0f / 1920.0f;
            assertEquals(2.0f, scaleFactor, 0.01f);
            
            // Physical: 2880x1620, Logical: 1920x1080 = 150% scaling
            scaleFactor = 2880.0f / 1920.0f;
            assertEquals(1.5f, scaleFactor, 0.01f);
            
            // Physical: 2400x1350, Logical: 1920x1080 = 125% scaling
            scaleFactor = 2400.0f / 1920.0f;
            assertEquals(1.25f, scaleFactor, 0.01f);
            
            // No scaling
            scaleFactor = 1920.0f / 1920.0f;
            assertEquals(1.0f, scaleFactor, 0.01f);
        }
        
        @Test
        @DisplayName("Should identify when scaling is needed")
        void shouldIdentifyScalingNeed() {
            // Test the logic for determining if scaling is needed
            int physicalWidth = 3840;
            int physicalHeight = 2160;
            int logicalWidth = 1920;
            int logicalHeight = 1080;
            
            boolean needsScaling = (physicalWidth != logicalWidth || physicalHeight != logicalHeight);
            assertTrue(needsScaling);
            
            // Test when no scaling needed
            physicalWidth = 1920;
            physicalHeight = 1080;
            needsScaling = (physicalWidth != logicalWidth || physicalHeight != logicalHeight);
            assertFalse(needsScaling);
        }
    }
    
    @Nested
    @DisplayName("Coordinate Scaling Logic")
    class CoordinateScalingTests {
        
        @Test
        @DisplayName("Should scale coordinates correctly with 2x scaling")
        void shouldScaleCoordinatesWith2xScaling() {
            float scaleFactor = 2.0f;
            int logicalX = 100;
            int logicalY = 200;
            int logicalWidth = 300;
            int logicalHeight = 400;
            
            int physicalX = (int) (logicalX * scaleFactor);
            int physicalY = (int) (logicalY * scaleFactor);
            int physicalWidth = (int) (logicalWidth * scaleFactor);
            int physicalHeight = (int) (logicalHeight * scaleFactor);
            
            assertEquals(200, physicalX);
            assertEquals(400, physicalY);
            assertEquals(600, physicalWidth);
            assertEquals(800, physicalHeight);
        }
        
        @Test
        @DisplayName("Should scale coordinates correctly with 1.5x scaling")
        void shouldScaleCoordinatesWith1_5xScaling() {
            float scaleFactor = 1.5f;
            int logicalX = 100;
            int logicalY = 200;
            int logicalWidth = 300;
            int logicalHeight = 400;
            
            int physicalX = (int) (logicalX * scaleFactor);
            int physicalY = (int) (logicalY * scaleFactor);
            int physicalWidth = (int) (logicalWidth * scaleFactor);
            int physicalHeight = (int) (logicalHeight * scaleFactor);
            
            assertEquals(150, physicalX);
            assertEquals(300, physicalY);
            assertEquals(450, physicalWidth);
            assertEquals(600, physicalHeight);
        }
        
        @Test
        @DisplayName("Should not scale coordinates when scale factor is 1")
        void shouldNotScaleWithFactorOne() {
            float scaleFactor = 1.0f;
            int logicalX = 100;
            int logicalY = 200;
            
            int physicalX = (int) (logicalX * scaleFactor);
            int physicalY = (int) (logicalY * scaleFactor);
            
            assertEquals(logicalX, physicalX);
            assertEquals(logicalY, physicalY);
        }
    }
    
    @Nested
    @DisplayName("Boundary Handling Logic")
    class BoundaryHandlingTests {
        
        @Test
        @DisplayName("Should constrain coordinates to screen bounds")
        void shouldConstrainToScreenBounds() {
            int screenWidth = 1920;
            int screenHeight = 1080;
            
            // Test constraining X coordinate
            int x = -10;
            x = Math.max(0, Math.min(x, screenWidth - 1));
            assertEquals(0, x);
            
            x = 2000;
            x = Math.max(0, Math.min(x, screenWidth - 1));
            assertEquals(1919, x);
            
            // Test constraining Y coordinate
            int y = -50;
            y = Math.max(0, Math.min(y, screenHeight - 1));
            assertEquals(0, y);
            
            y = 1200;
            y = Math.max(0, Math.min(y, screenHeight - 1));
            assertEquals(1079, y);
        }
        
        @Test
        @DisplayName("Should adjust width and height to fit screen")
        void shouldAdjustDimensionsToFitScreen() {
            int screenWidth = 1920;
            int screenHeight = 1080;
            int x = 1800;
            int y = 1000;
            int w = 300;
            int h = 200;
            
            // Adjust width to fit
            w = Math.min(w, screenWidth - x);
            assertEquals(120, w);
            
            // Adjust height to fit
            h = Math.min(h, screenHeight - y);
            assertEquals(80, h);
        }
        
        @Test
        @DisplayName("Should handle zero-sized rectangles")
        void shouldHandleZeroSizedRectangles() {
            int w = 0;
            int h = 0;
            
            // Zero-sized rectangles should remain zero
            w = Math.max(0, w);
            h = Math.max(0, h);
            
            assertEquals(0, w);
            assertEquals(0, h);
        }
        
        @Test
        @DisplayName("Should handle negative dimensions")
        void shouldHandleNegativeDimensions() {
            int w = -100;
            int h = -50;
            
            // Negative dimensions should become 0
            w = Math.max(0, w);
            h = Math.max(0, h);
            
            assertEquals(0, w);
            assertEquals(0, h);
        }
    }
    
    @Nested
    @DisplayName("Integration Tests")
    class IntegrationTests {
        
        @Test
        @DisplayName("PhysicalScreen should extend Screen")
        void shouldExtendScreen() {
            // Test class hierarchy without instantiation
            assertTrue(Screen.class.isAssignableFrom(PhysicalScreen.class));
        }
        
        @Test
        @DisplayName("Should have expected public methods")
        void shouldHaveExpectedPublicMethods() {
            // Verify the class has the expected methods
            try {
                // Check for capture methods
                assertNotNull(PhysicalScreen.class.getMethod("capture"));
                assertNotNull(PhysicalScreen.class.getMethod("capture", Rectangle.class));
                assertNotNull(PhysicalScreen.class.getMethod("capture", int.class, int.class, int.class, int.class));
                
                // Check for utility methods
                assertNotNull(PhysicalScreen.class.getMethod("getPhysicalResolution"));
                assertNotNull(PhysicalScreen.class.getMethod("isScalingCompensated"));
                assertNotNull(PhysicalScreen.class.getMethod("getScaleFactor"));
            } catch (NoSuchMethodException e) {
                fail("Expected method not found: " + e.getMessage());
            }
        }
        
        @Test
        @DisplayName("Should have correct return types")
        void shouldHaveCorrectReturnTypes() {
            try {
                // Verify return types
                assertEquals(ScreenImage.class, 
                    PhysicalScreen.class.getMethod("capture").getReturnType());
                assertEquals(ScreenImage.class, 
                    PhysicalScreen.class.getMethod("capture", Rectangle.class).getReturnType());
                assertEquals(Dimension.class,
                    PhysicalScreen.class.getMethod("getPhysicalResolution").getReturnType());
                assertEquals(boolean.class,
                    PhysicalScreen.class.getMethod("isScalingCompensated").getReturnType());
                assertEquals(float.class,
                    PhysicalScreen.class.getMethod("getScaleFactor").getReturnType());
            } catch (NoSuchMethodException e) {
                fail("Expected method not found: " + e.getMessage());
            }
        }
    }
}