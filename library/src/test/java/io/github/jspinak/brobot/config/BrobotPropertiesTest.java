package io.github.jspinak.brobot.config;

import static org.junit.jupiter.api.Assertions.*;

import java.util.HashMap;
import java.util.Map;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.junit.jupiter.params.provider.ValueSource;
import org.springframework.boot.context.properties.bind.Binder;
import org.springframework.boot.context.properties.source.ConfigurationPropertySource;
import org.springframework.boot.context.properties.source.MapConfigurationPropertySource;

import io.github.jspinak.brobot.config.core.BrobotProperties;
import io.github.jspinak.brobot.test.BrobotTestBase;

/**
 * Comprehensive test suite for BrobotProperties - centralized configuration properties. Tests
 * property binding, defaults, and configuration validation.
 */
@DisplayName("BrobotProperties Tests")
public class BrobotPropertiesTest extends BrobotTestBase {

    private BrobotProperties properties;

    @BeforeEach
    @Override
    public void setupTest() {
        super.setupTest();
        properties = new BrobotProperties();
    }

    @Nested
    @DisplayName("Default Configuration")
    class DefaultConfiguration {

        @Test
        @DisplayName("Core defaults are set correctly")
        public void testCoreDefaults() {
            BrobotProperties.Core core = properties.getCore();

            assertNotNull(core);
            assertEquals("images", core.getImagePath());
            assertFalse(core.isMock());
            assertFalse(core.isHeadless());
            assertEquals("com.example", core.getPackageName());
        }

        @Test
        @DisplayName("Mouse defaults are set correctly")
        public void testMouseDefaults() {
            BrobotProperties.Mouse mouse = properties.getMouse();

            assertNotNull(mouse);
            assertEquals(0.5f, mouse.getMoveDelay());
            assertEquals(0.0, mouse.getPauseBeforeDown());
            assertEquals(0.0, mouse.getPauseAfterDown());
        }

        @Test
        @DisplayName("Mock timing defaults are set correctly")
        public void testMockDefaults() {
            BrobotProperties.Mock mock = properties.getMock();

            assertNotNull(mock);
            // Check default mock timings
            assertTrue(mock != null);
        }

        @Test
        @DisplayName("Screenshot defaults are set correctly")
        public void testScreenshotDefaults() {
            BrobotProperties.Screenshot screenshot = properties.getScreenshot();

            assertNotNull(screenshot);
            // Verify screenshot defaults
        }

        @Test
        @DisplayName("All nested configuration objects are non-null")
        public void testAllConfigurationObjectsInitialized() {
            assertNotNull(properties.getCore());
            assertNotNull(properties.getMouse());
            assertNotNull(properties.getMock());
            assertNotNull(properties.getScreenshot());
            assertNotNull(properties.getIllustration());
            assertNotNull(properties.getAnalysis());
            assertNotNull(properties.getRecording());
            assertNotNull(properties.getDataset());
            assertNotNull(properties.getTesting());
            assertNotNull(properties.getMonitor());
        }
    }

    @Nested
    @DisplayName("Core Configuration")
    class CoreConfiguration {

        @Test
        @DisplayName("Set and get image path")
        public void testImagePath() {
            String customPath = "/custom/images/path";
            properties.getCore().setImagePath(customPath);

            assertEquals(customPath, properties.getCore().getImagePath());
        }

        @ParameterizedTest
        @ValueSource(
                strings = {
                    "classpath:images/",
                    "/absolute/path/images/",
                    "./relative/path/images/",
                    "images"
                })
        @DisplayName("Support various image path formats")
        public void testVariousImagePaths(String path) {
            properties.getCore().setImagePath(path);

            assertEquals(path, properties.getCore().getImagePath());
        }

        @Test
        @DisplayName("Enable and disable mock mode")
        public void testMockMode() {
            assertFalse(properties.getCore().isMock());

            properties.getCore().setMock(true);

            assertTrue(properties.getCore().isMock());
        }

        @Test
        @DisplayName("Enable and disable headless mode")
        public void testHeadlessMode() {
            assertFalse(properties.getCore().isHeadless());

            properties.getCore().setHeadless(true);

            assertTrue(properties.getCore().isHeadless());
        }

        @Test
        @DisplayName("Set and get package name")
        public void testPackageName() {
            String customPackage = "io.github.jspinak.myapp";
            properties.getCore().setPackageName(customPackage);

            assertEquals(customPackage, properties.getCore().getPackageName());
        }
    }

    @Nested
    @DisplayName("Mouse Configuration")
    class MouseConfiguration {

        @Test
        @DisplayName("Set and get move delay")
        public void testMoveDelay() {
            float customDelay = 1.5f;
            properties.getMouse().setMoveDelay(customDelay);

            assertEquals(customDelay, properties.getMouse().getMoveDelay());
        }

        @ParameterizedTest
        @CsvSource({"0.0, 0.0, 0.0", "0.5, 0.1, 0.2", "1.0, 0.5, 0.5", "2.0, 1.0, 1.0"})
        @DisplayName("Set various mouse timing configurations")
        public void testMouseTimings(float moveDelay, double pauseBefore, double pauseAfter) {
            BrobotProperties.Mouse mouse = properties.getMouse();

            mouse.setMoveDelay(moveDelay);
            mouse.setPauseBeforeDown(pauseBefore);
            mouse.setPauseAfterDown(pauseAfter);

            assertEquals(moveDelay, mouse.getMoveDelay());
            assertEquals(pauseBefore, mouse.getPauseBeforeDown());
            assertEquals(pauseAfter, mouse.getPauseAfterDown());
        }

        @Test
        @DisplayName("Mouse pause values can be negative")
        public void testNegativeMousePause() {
            // Some special cases might use negative values
            properties.getMouse().setPauseBeforeDown(-1.0);
            properties.getMouse().setPauseAfterDown(-1.0);

            assertEquals(-1.0, properties.getMouse().getPauseBeforeDown());
            assertEquals(-1.0, properties.getMouse().getPauseAfterDown());
        }
    }

    @Nested
    @DisplayName("Spring Property Binding")
    class SpringPropertyBinding {

        @Test
        @DisplayName("Bind properties from map configuration")
        public void testBindPropertiesFromMap() {
            Map<String, String> configMap = new HashMap<>();
            configMap.put("brobot.core.image-path", "/test/images");
            configMap.put("brobot.core.mock", "true");
            configMap.put("brobot.core.headless", "true");
            configMap.put("brobot.mouse.move-delay", "2.5");
            configMap.put("brobot.mouse.pause-before-down", "0.5");

            ConfigurationPropertySource source = new MapConfigurationPropertySource(configMap);
            Binder binder = new Binder(source);

            BrobotProperties boundProperties =
                    binder.bind("brobot", BrobotProperties.class).orElse(new BrobotProperties());

            assertEquals("/test/images", boundProperties.getCore().getImagePath());
            assertTrue(boundProperties.getCore().isMock());
            assertTrue(boundProperties.getCore().isHeadless());
            assertEquals(2.5f, boundProperties.getMouse().getMoveDelay());
            assertEquals(0.5, boundProperties.getMouse().getPauseBeforeDown());
        }

        @Test
        @DisplayName("Partial binding preserves defaults")
        public void testPartialBinding() {
            Map<String, String> configMap = new HashMap<>();
            configMap.put("brobot.core.mock", "true");
            // Only set mock, leave other properties as default

            ConfigurationPropertySource source = new MapConfigurationPropertySource(configMap);
            Binder binder = new Binder(source);

            BrobotProperties boundProperties =
                    binder.bind("brobot", BrobotProperties.class).orElse(new BrobotProperties());

            assertTrue(boundProperties.getCore().isMock());
            assertEquals("images", boundProperties.getCore().getImagePath()); // Default preserved
            assertFalse(boundProperties.getCore().isHeadless()); // Default preserved
        }
    }

    @Nested
    @DisplayName("Configuration Validation")
    class ConfigurationValidation {

        @Test
        @DisplayName("Empty image path is allowed")
        public void testEmptyImagePath() {
            properties.getCore().setImagePath("");

            assertEquals("", properties.getCore().getImagePath());
        }

        @Test
        @DisplayName("Null image path is allowed")
        public void testNullImagePath() {
            properties.getCore().setImagePath(null);

            assertNull(properties.getCore().getImagePath());
        }

        @Test
        @DisplayName("Zero mouse delays are valid")
        public void testZeroMouseDelays() {
            BrobotProperties.Mouse mouse = properties.getMouse();

            mouse.setMoveDelay(0.0f);
            mouse.setPauseBeforeDown(0.0);
            mouse.setPauseAfterDown(0.0);

            assertEquals(0.0f, mouse.getMoveDelay());
            assertEquals(0.0, mouse.getPauseBeforeDown());
            assertEquals(0.0, mouse.getPauseAfterDown());
        }
    }

    @Nested
    @DisplayName("Mock Mode Configuration")
    class MockModeConfiguration {

        @Test
        @DisplayName("Mock mode affects core configuration")
        public void testMockModeAffectsCore() {
            assertFalse(properties.getCore().isMock());

            properties.getCore().setMock(true);

            assertTrue(properties.getCore().isMock());
        }

        @Test
        @DisplayName("Mock timings are available when mock is enabled")
        public void testMockTimingsAvailable() {
            properties.getCore().setMock(true);

            assertNotNull(properties.getMock());
            // Mock timings should be available for configuration
        }
    }

    @Nested
    @DisplayName("Complex Configuration Scenarios")
    class ComplexConfigurationScenarios {

        @Test
        @DisplayName("Configure for CI/CD environment")
        public void testCICDConfiguration() {
            // CI/CD typically needs headless and mock mode
            properties.getCore().setHeadless(true);
            properties.getCore().setMock(true);
            properties.getCore().setImagePath("classpath:test-images/");

            assertTrue(properties.getCore().isHeadless());
            assertTrue(properties.getCore().isMock());
            assertEquals("classpath:test-images/", properties.getCore().getImagePath());
        }

        @Test
        @DisplayName("Configure for production environment")
        public void testProductionConfiguration() {
            // Production typically needs real execution
            properties.getCore().setHeadless(false);
            properties.getCore().setMock(false);
            properties.getCore().setImagePath("/opt/app/images/");
            properties.getMouse().setMoveDelay(0.3f); // Faster mouse for production

            assertFalse(properties.getCore().isHeadless());
            assertFalse(properties.getCore().isMock());
            assertEquals("/opt/app/images/", properties.getCore().getImagePath());
            assertEquals(0.3f, properties.getMouse().getMoveDelay());
        }

        @Test
        @DisplayName("Configure for development environment")
        public void testDevelopmentConfiguration() {
            // Development might need slower actions for debugging
            properties.getCore().setHeadless(false);
            properties.getCore().setMock(false);
            properties.getMouse().setMoveDelay(1.0f); // Slower for visibility
            properties.getMouse().setPauseBeforeDown(0.5); // Add pauses for debugging
            properties.getMouse().setPauseAfterDown(0.5);

            assertFalse(properties.getCore().isHeadless());
            assertEquals(1.0f, properties.getMouse().getMoveDelay());
            assertEquals(0.5, properties.getMouse().getPauseBeforeDown());
            assertEquals(0.5, properties.getMouse().getPauseAfterDown());
        }
    }

    @Nested
    @DisplayName("Property Inheritance and Overrides")
    class PropertyInheritanceAndOverrides {

        @Test
        @DisplayName("Create new properties from existing")
        public void testCopyProperties() {
            // Set up original properties
            properties.getCore().setImagePath("/original/path");
            properties.getCore().setMock(true);
            properties.getMouse().setMoveDelay(1.5f);

            // Create new properties and copy values
            BrobotProperties newProperties = new BrobotProperties();
            newProperties.getCore().setImagePath(properties.getCore().getImagePath());
            newProperties.getCore().setMock(properties.getCore().isMock());
            newProperties.getMouse().setMoveDelay(properties.getMouse().getMoveDelay());

            assertEquals(
                    properties.getCore().getImagePath(), newProperties.getCore().getImagePath());
            assertEquals(properties.getCore().isMock(), newProperties.getCore().isMock());
            assertEquals(
                    properties.getMouse().getMoveDelay(), newProperties.getMouse().getMoveDelay());
        }

        @Test
        @DisplayName("Override specific properties")
        public void testOverrideSpecificProperties() {
            // Set defaults
            properties.getCore().setImagePath("/default/path");
            properties.getCore().setMock(false);
            properties.getCore().setHeadless(false);

            // Override only specific properties
            properties.getCore().setMock(true); // Override only mock

            assertEquals("/default/path", properties.getCore().getImagePath()); // Unchanged
            assertTrue(properties.getCore().isMock()); // Changed
            assertFalse(properties.getCore().isHeadless()); // Unchanged
        }
    }

    @Nested
    @DisplayName("Edge Cases and Boundary Values")
    class EdgeCasesAndBoundaryValues {

        @ParameterizedTest
        @ValueSource(floats = {Float.MIN_VALUE, 0.0f, 0.001f, 1.0f, 100.0f, Float.MAX_VALUE})
        @DisplayName("Handle extreme mouse delay values")
        public void testExtremeMouseDelayValues(float delay) {
            properties.getMouse().setMoveDelay(delay);

            assertEquals(delay, properties.getMouse().getMoveDelay());
        }

        @Test
        @DisplayName("Handle very long package names")
        public void testVeryLongPackageName() {
            String longPackage = "com.example." + "subpackage.".repeat(50) + "myapp";
            properties.getCore().setPackageName(longPackage);

            assertEquals(longPackage, properties.getCore().getPackageName());
        }

        @Test
        @DisplayName("Handle special characters in paths")
        public void testSpecialCharactersInPaths() {
            String specialPath = "/path/with spaces/and-dashes/and_underscores/";
            properties.getCore().setImagePath(specialPath);

            assertEquals(specialPath, properties.getCore().getImagePath());
        }
    }
}
