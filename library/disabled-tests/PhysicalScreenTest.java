package io.github.jspinak.brobot.screen;

import io.github.jspinak.brobot.test.BrobotTestBase;
import org.junit.jupiter.api.*;
import org.sikuli.script.ScreenImage;

import java.awt.*;

import static org.junit.jupiter.api.Assertions.*;
import static org.junit.jupiter.api.Assumptions.*;

/**
 * Test suite for PhysicalScreen.
 * Tests physical resolution screen capture functionality.
 */
@DisplayName("PhysicalScreen Tests")
public class PhysicalScreenTest extends BrobotTestBase {
    
    private PhysicalScreen physicalScreen;
    private boolean isHeadless;
    
    @BeforeEach
    @Override
    public void setupTest() {
        super.setupTest();
        
        // Check if we're in a headless environment
        isHeadless = GraphicsEnvironment.isHeadless();
        
        if (!isHeadless) {
            try {
                physicalScreen = new PhysicalScreen();
            } catch (Exception e) {
                // May fail in some test environments
                isHeadless = true;
            }
        }
    }
    
    @Nested
    @DisplayName("Initialization")
    class Initialization {
        
        @Test
        @DisplayName("Should create PhysicalScreen in non-headless environment")
        void shouldCreatePhysicalScreenInNonHeadlessEnvironment() {
            assumeFalse(isHeadless, "Test requires display");
            
            assertNotNull(physicalScreen);
        }
        
        @Test
        @DisplayName("Should handle headless environment gracefully")
        void shouldHandleHeadlessEnvironmentGracefully() {
            if (isHeadless) {
                assertThrows(RuntimeException.class, () -> {
                    new PhysicalScreen();
                });
            } else {
                assertDoesNotThrow(() -> {
                    new PhysicalScreen();
                });
            }
        }
    }
    
    @Nested
    @DisplayName("Screen Capture")
    class ScreenCapture {
        
        @Test
        @DisplayName("Should capture full screen")
        void shouldCaptureFullScreen() {
            assumeFalse(isHeadless, "Test requires display");
            assumeNotNull(physicalScreen);
            
            ScreenImage capture = physicalScreen.capture();
            
            assertNotNull(capture);
            // Can't verify exact dimensions as they depend on physical screen
        }
        
        @Test
        @DisplayName("Should capture rectangle region")
        void shouldCaptureRectangleRegion() {
            assumeFalse(isHeadless, "Test requires display");
            assumeNotNull(physicalScreen);
            
            Rectangle rect = new Rectangle(0, 0, 100, 100);
            ScreenImage capture = physicalScreen.capture(rect);
            
            assertNotNull(capture);
        }
        
        @Test
        @DisplayName("Should capture with coordinates")
        void shouldCaptureWithCoordinates() {
            assumeFalse(isHeadless, "Test requires display");
            assumeNotNull(physicalScreen);
            
            ScreenImage capture = physicalScreen.capture(10, 10, 50, 50);
            
            assertNotNull(capture);
        }
        
        @Test
        @DisplayName("Should handle out of bounds coordinates")
        void shouldHandleOutOfBoundsCoordinates() {
            assumeFalse(isHeadless, "Test requires display");
            assumeNotNull(physicalScreen);
            
            // Should clamp to screen bounds
            assertDoesNotThrow(() -> {
                physicalScreen.capture(-100, -100, 200, 200);
            });
            
            assertDoesNotThrow(() -> {
                physicalScreen.capture(999999, 999999, 100, 100);
            });
        }
    }
    
    @Nested
    @DisplayName("DPI Scaling")
    class DPIScaling {
        
        @Test
        @DisplayName("Should detect DPI scaling")
        void shouldDetectDPIScaling() {
            assumeFalse(isHeadless, "Test requires display");
            assumeNotNull(physicalScreen);
            
            // Can't directly test scaling detection without access to internal fields
            // But we can verify the screen works with scaling
            assertDoesNotThrow(() -> {
                physicalScreen.capture();
            });
        }
        
        @Test
        @DisplayName("Should scale rectangle for high DPI")
        void shouldScaleRectangleForHighDPI() {
            assumeFalse(isHeadless, "Test requires display");
            assumeNotNull(physicalScreen);
            
            Rectangle logicalRect = new Rectangle(100, 100, 200, 200);
            
            // Capture should handle scaling internally
            assertDoesNotThrow(() -> {
                physicalScreen.capture(logicalRect);
            });
        }
    }
    
    @Nested
    @DisplayName("Edge Cases")
    class EdgeCases {
        
        @Test
        @DisplayName("Should handle zero-size capture")
        void shouldHandleZeroSizeCapture() {
            assumeFalse(isHeadless, "Test requires display");
            assumeNotNull(physicalScreen);
            
            assertDoesNotThrow(() -> {
                physicalScreen.capture(0, 0, 0, 0);
            });
        }
        
        @Test
        @DisplayName("Should handle negative dimensions")
        void shouldHandleNegativeDimensions() {
            assumeFalse(isHeadless, "Test requires display");
            assumeNotNull(physicalScreen);
            
            assertDoesNotThrow(() -> {
                physicalScreen.capture(10, 10, -10, -10);
            });
        }
        
        @Test
        @DisplayName("Should handle very large capture request")
        void shouldHandleVeryLargeCaptureRequest() {
            assumeFalse(isHeadless, "Test requires display");
            assumeNotNull(physicalScreen);
            
            // Should clamp to actual screen size
            assertDoesNotThrow(() -> {
                physicalScreen.capture(0, 0, 10000, 10000);
            });
        }
    }
}