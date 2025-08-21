package io.github.jspinak.brobot.startup;

import io.github.jspinak.brobot.test.BrobotTestBase;
import org.junit.jupiter.api.*;

import java.awt.*;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

/**
 * Test suite for BrobotStartup.
 * Tests physical resolution configuration and DPI awareness settings.
 */
@DisplayName("BrobotStartup Tests")
public class BrobotStartupTest extends BrobotTestBase {
    
    private BrobotStartup brobotStartup;
    
    @BeforeEach
    @Override
    public void setupTest() {
        super.setupTest();
        brobotStartup = new BrobotStartup();
    }
    
    @Nested
    @DisplayName("Static Initialization")
    class StaticInitialization {
        
        @Test
        @DisplayName("Should set DPI awareness properties")
        void shouldSetDpiAwarenessProperties() {
            // These should have been set by the static initializer
            assertEquals("false", System.getProperty("sun.java2d.dpiaware"));
            assertEquals("1.0", System.getProperty("sun.java2d.uiScale"));
            assertEquals("false", System.getProperty("sun.java2d.uiScale.enabled"));
        }
        
        @Test
        @DisplayName("Should set Windows-specific properties")
        void shouldSetWindowsSpecificProperties() {
            assertEquals("1.0", System.getProperty("sun.java2d.win.uiScaleX"));
            assertEquals("1.0", System.getProperty("sun.java2d.win.uiScaleY"));
            assertEquals("false", System.getProperty("sun.java2d.dpiaware.override"));
            assertEquals("false", System.getProperty("sun.java2d.win.uiScale.enabled"));
        }
        
        @Test
        @DisplayName("Should disable headless mode")
        void shouldDisableHeadlessMode() {
            assertEquals("false", System.getProperty("java.awt.headless"));
        }
        
        @Test
        @DisplayName("Should configure rendering properties")
        void shouldConfigureRenderingProperties() {
            assertEquals("false", System.getProperty("sun.java2d.noddraw"));
            assertEquals("false", System.getProperty("sun.java2d.d3d"));
        }
    }
    
    @Nested
    @DisplayName("Post-Construct Verification")
    class PostConstructVerification {
        
        @Test
        @DisplayName("Should verify resolution on init")
        void shouldVerifyResolutionOnInit() {
            // Should not throw exceptions
            assertDoesNotThrow(() -> brobotStartup.init());
        }
        
        @Test
        @DisplayName("Should handle headless environment gracefully")
        void shouldHandleHeadlessEnvironmentGracefully() {
            // Even in headless test environment, should not crash
            assertDoesNotThrow(() -> brobotStartup.init());
        }
    }
    
    @Nested
    @DisplayName("Resolution Detection")
    class ResolutionDetection {
        
        @Test
        @DisplayName("Should detect screen dimensions")
        void shouldDetectScreenDimensions() {
            // In mock mode or headless, this should still work without crashing
            assertDoesNotThrow(() -> {
                if (!GraphicsEnvironment.isHeadless()) {
                    Dimension screenSize = Toolkit.getDefaultToolkit().getScreenSize();
                    assertTrue(screenSize.width > 0);
                    assertTrue(screenSize.height > 0);
                }
            });
        }
        
        @Test
        @DisplayName("Should handle GraphicsEnvironment errors")
        void shouldHandleGraphicsEnvironmentErrors() {
            // Even with potential errors, init should complete
            assertDoesNotThrow(() -> brobotStartup.init());
        }
    }
    
    @Nested
    @DisplayName("Configuration Persistence")
    class ConfigurationPersistence {
        
        @Test
        @DisplayName("Properties should persist after initialization")
        void propertiesShouldPersistAfterInitialization() {
            // Create new instance - static block already ran
            BrobotStartup newStartup = new BrobotStartup();
            
            // Properties should still be set
            assertEquals("false", System.getProperty("sun.java2d.dpiaware"));
            assertEquals("1.0", System.getProperty("sun.java2d.uiScale"));
            
            // Init should work
            assertDoesNotThrow(() -> newStartup.init());
        }
        
        @Test
        @DisplayName("Multiple initializations should be idempotent")
        void multipleInitializationsShouldBeIdempotent() {
            // First init
            brobotStartup.init();
            String dpiaware = System.getProperty("sun.java2d.dpiaware");
            
            // Second init
            brobotStartup.init();
            assertEquals(dpiaware, System.getProperty("sun.java2d.dpiaware"));
        }
    }
}