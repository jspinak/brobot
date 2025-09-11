package io.github.jspinak.brobot.util.image.core;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

import java.awt.*;
import java.awt.image.BufferedImage;
import java.lang.reflect.Field;

import org.junit.jupiter.api.*;
import org.junit.jupiter.api.condition.DisabledIfEnvironmentVariable;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import io.github.jspinak.brobot.logging.unified.BrobotLogger;
import io.github.jspinak.brobot.logging.unified.LogBuilder;
import io.github.jspinak.brobot.test.BrobotTestBase;

/**
 * Test suite for ScreenCaptureValidator. Tests screen capture validation and headless detection
 * functionality in a way that works in headless environments.
 */
@ExtendWith(MockitoExtension.class)
@DisplayName("ScreenCaptureValidator Tests")
@DisabledIfEnvironmentVariable(
        named = "CI",
        matches = "true",
        disabledReason = "Test incompatible with CI environment")
public class ScreenCaptureValidatorTest extends BrobotTestBase {

    @Mock private BrobotLogger logger;

    @Mock private LogBuilder logBuilder;

    private ScreenCaptureValidator validator;
    private BufferedImage testImage;

    @BeforeEach
    @Override
    public void setupTest() {
        super.setupTest();

        // Create real validator instance
        validator = new ScreenCaptureValidator();

        // Setup logger mock chain with lenient stubbing
        lenient().when(logger.log()).thenReturn(logBuilder);
        lenient().when(logBuilder.level(any())).thenReturn(logBuilder);
        lenient().when(logBuilder.message(anyString())).thenReturn(logBuilder);
        lenient().when(logBuilder.metadata(anyString(), any())).thenReturn(logBuilder);
        lenient().when(logBuilder.error(any(Throwable.class))).thenReturn(logBuilder);
        lenient().doNothing().when(logBuilder).log();

        // Inject the mock logger using reflection
        try {
            Field loggerField = ScreenCaptureValidator.class.getDeclaredField("logger");
            loggerField.setAccessible(true);
            loggerField.set(validator, logger);
        } catch (Exception e) {
            // Logger is optional, so ignore if field doesn't exist
        }

        // Create a test image with non-black pixels
        testImage = new BufferedImage(100, 100, BufferedImage.TYPE_INT_RGB);
        Graphics2D g = testImage.createGraphics();
        g.setColor(Color.WHITE);
        g.fillRect(0, 0, 100, 100);
        g.dispose();
    }

    @Nested
    @DisplayName("Pixel Value Checking")
    class PixelValueChecking {

        @Test
        @DisplayName("Should handle null image")
        void shouldHandleNullImage() {
            boolean result = validator.checkPixelValues(null);
            assertFalse(result, "Null image should return false");
        }

        @Test
        @DisplayName("Should detect all-black image")
        void shouldDetectAllBlackImage() {
            BufferedImage blackImage = new BufferedImage(100, 100, BufferedImage.TYPE_INT_RGB);
            // Default BufferedImage is all black

            boolean result = validator.checkPixelValues(blackImage);

            assertFalse(result, "All-black image should return false");
        }

        @Test
        @DisplayName("Should detect non-black pixels in normal image")
        void shouldDetectNonBlackPixels() {
            boolean result = validator.checkPixelValues(testImage);

            assertTrue(result, "Image with white pixels should return true");
        }

        @Test
        @DisplayName("Should detect single non-black pixel")
        void shouldDetectSingleNonBlackPixel() {
            BufferedImage image = new BufferedImage(100, 100, BufferedImage.TYPE_INT_RGB);
            image.setRGB(50, 50, Color.WHITE.getRGB());

            boolean result = validator.checkPixelValues(image);

            assertTrue(result, "Image with single white pixel should return true");
        }

        @ParameterizedTest
        @ValueSource(ints = {10, 100, 500, 1000})
        @DisplayName("Should efficiently check various sizes")
        void shouldEfficientlyCheckVariousSizes(int size) {
            BufferedImage largeImage = new BufferedImage(size, size, BufferedImage.TYPE_INT_RGB);
            // Add one white pixel
            largeImage.setRGB(0, 0, Color.WHITE.getRGB());

            long startTime = System.currentTimeMillis();
            boolean result = validator.checkPixelValues(largeImage);
            long endTime = System.currentTimeMillis();

            assertTrue(result);
            assertTrue(
                    endTime - startTime < 100,
                    "Should complete in less than 100ms for " + size + "x" + size + " image");
        }
    }

    @Nested
    @DisplayName("Edge Cases")
    class EdgeCases {

        @Test
        @DisplayName("Should handle tiny images")
        void shouldHandleTinyImages() {
            BufferedImage tinyImage = new BufferedImage(1, 1, BufferedImage.TYPE_INT_RGB);
            tinyImage.setRGB(0, 0, Color.RED.getRGB());

            boolean result = validator.checkPixelValues(tinyImage);

            assertTrue(result);
        }

        @Test
        @DisplayName("Should handle transparent images")
        void shouldHandleTransparentImages() {
            BufferedImage transparentImage = new BufferedImage(10, 10, BufferedImage.TYPE_INT_ARGB);
            transparentImage.setRGB(5, 5, new Color(255, 255, 255, 128).getRGB());

            boolean result = validator.checkPixelValues(transparentImage);

            assertTrue(
                    result, "Image with non-black pixels should return true regardless of alpha");
        }

        @Test
        @DisplayName("Should handle grayscale images")
        void shouldHandleGrayscaleImages() {
            BufferedImage grayImage = new BufferedImage(10, 10, BufferedImage.TYPE_BYTE_GRAY);
            Graphics2D g = grayImage.createGraphics();
            g.setColor(Color.GRAY);
            g.fillRect(0, 0, 10, 10);
            g.dispose();

            boolean result = validator.checkPixelValues(grayImage);

            assertTrue(result);
        }
    }

    @Nested
    @DisplayName("Validation Result")
    class ValidationResultTests {

        @Test
        @DisplayName("Should create valid result with all fields")
        void shouldCreateValidResult() {
            ScreenCaptureValidator.ValidationResult result =
                    new ScreenCaptureValidator.ValidationResult(
                            true, "Success", testImage, false, true);

            assertTrue(result.isValid());
            assertEquals("Success", result.getMessage());
            assertEquals(testImage, result.getCapturedImage());
            assertFalse(result.isHeadless());
            assertTrue(result.hasNonBlackPixels());
        }

        @Test
        @DisplayName("Should create invalid result for headless mode")
        void shouldCreateInvalidResultForHeadless() {
            ScreenCaptureValidator.ValidationResult result =
                    new ScreenCaptureValidator.ValidationResult(
                            false, "Headless mode", null, true, false);

            assertFalse(result.isValid());
            assertTrue(result.isHeadless());
            assertNull(result.getCapturedImage());
        }
    }

    @Nested
    @DisplayName("Headless Mode Detection")
    class HeadlessModeDetection {

        @Test
        @DisplayName("Should detect current environment headless state")
        void shouldDetectCurrentEnvironmentHeadlessState() {
            // This test adapts to the actual environment
            boolean isHeadless = validator.detectHeadlessMode();

            // In CI/headless environments, this should be true
            // In desktop environments with display, this should be false
            assertNotNull(isHeadless);

            // If GraphicsEnvironment reports headless, our validator should too
            if (GraphicsEnvironment.isHeadless()) {
                assertTrue(
                        isHeadless,
                        "Should detect headless when GraphicsEnvironment.isHeadless() is true");
            }
        }

        @Test
        @DisplayName("Should detect headless from system property")
        void shouldDetectHeadlessFromSystemProperty() {
            String originalValue = System.getProperty("java.awt.headless");
            try {
                System.setProperty("java.awt.headless", "true");

                boolean isHeadless = validator.detectHeadlessMode();

                assertTrue(isHeadless);
            } finally {
                // Restore original value
                if (originalValue != null) {
                    System.setProperty("java.awt.headless", originalValue);
                } else {
                    System.clearProperty("java.awt.headless");
                }
            }
        }
    }

    @Nested
    @DisplayName("Environment Detection")
    class EnvironmentDetection {

        @Test
        @DisplayName("Should check for DISPLAY variable")
        void shouldCheckDisplayVariable() {
            // Test the hasDisplay method via reflection
            try {
                java.lang.reflect.Method hasDisplayMethod =
                        ScreenCaptureValidator.class.getDeclaredMethod("hasDisplay");
                hasDisplayMethod.setAccessible(true);

                boolean hasDisplay = (boolean) hasDisplayMethod.invoke(validator);

                // Just verify the method executes without error - don't assert on
                // environment-specific values
                // The actual value depends on the test environment
                assertNotNull(hasDisplay);
            } catch (Exception e) {
                fail("Failed to test hasDisplay method: " + e.getMessage());
            }
        }

        @Test
        @DisplayName("Should detect WSL environment variables")
        void shouldDetectWSLEnvironment() {
            // Test the isRunningInWSL method via reflection
            try {
                java.lang.reflect.Method isWSLMethod =
                        ScreenCaptureValidator.class.getDeclaredMethod("isRunningInWSL");
                isWSLMethod.setAccessible(true);

                boolean isWSL = (boolean) isWSLMethod.invoke(validator);

                // Result depends on actual environment
                assertNotNull(isWSL);

                // If WSL environment variables exist, should detect WSL
                if (System.getenv("WSL_DISTRO_NAME") != null) {
                    assertTrue(isWSL, "Should detect WSL when WSL_DISTRO_NAME is set");
                }
            } catch (Exception e) {
                fail("Failed to test isRunningInWSL method: " + e.getMessage());
            }
        }
    }

    @Nested
    @DisplayName("Screen Capture Validation")
    class ScreenCaptureValidation {

        @Test
        @DisplayName("Should return invalid result in headless mode")
        void shouldReturnInvalidInHeadless() {
            // Create a spy that always reports headless
            ScreenCaptureValidator spyValidator = spy(validator);
            doReturn(true).when(spyValidator).detectHeadlessMode();

            ScreenCaptureValidator.ValidationResult result = spyValidator.validateScreenCapture();

            assertFalse(result.isValid());
            assertTrue(result.isHeadless());
            assertEquals("Screen capture not available in headless mode", result.getMessage());
        }

        @Test
        @DisplayName("Should handle screen capture exception")
        void shouldHandleScreenCaptureException() throws AWTException {
            // Create a spy that throws exception on capture
            ScreenCaptureValidator spyValidator = spy(validator);
            doReturn(false).when(spyValidator).detectHeadlessMode();
            doThrow(new AWTException("Test exception")).when(spyValidator).captureScreen();

            ScreenCaptureValidator.ValidationResult result = spyValidator.validateScreenCapture();

            assertFalse(result.isValid());
            assertTrue(result.getMessage().contains("Exception during screen capture"));
        }

        @Test
        @DisplayName("Should validate successful capture with non-black pixels")
        void shouldValidateSuccessfulCapture() throws AWTException {
            // Create a spy that returns our test image
            ScreenCaptureValidator spyValidator = spy(validator);
            doReturn(false).when(spyValidator).detectHeadlessMode();
            doReturn(testImage).when(spyValidator).captureScreen();

            ScreenCaptureValidator.ValidationResult result = spyValidator.validateScreenCapture();

            assertTrue(result.isValid());
            assertEquals("Screen capture validated successfully", result.getMessage());
            assertEquals(testImage, result.getCapturedImage());
            assertTrue(result.hasNonBlackPixels());
        }
    }

    @Nested
    @DisplayName("Test Screen Capture")
    class TestScreenCapture {

        @Test
        @DisplayName("Should test and log success")
        void shouldTestAndLogSuccess() throws AWTException {
            ScreenCaptureValidator spyValidator = spy(validator);
            doReturn(false).when(spyValidator).detectHeadlessMode();
            doReturn(testImage).when(spyValidator).captureScreen();

            boolean result = spyValidator.testScreenCapture();

            assertTrue(result);
            verify(logger, atLeastOnce()).log();
        }

        @Test
        @DisplayName("Should test and log failure")
        void shouldTestAndLogFailure() {
            ScreenCaptureValidator spyValidator = spy(validator);
            doReturn(true).when(spyValidator).detectHeadlessMode();

            boolean result = spyValidator.testScreenCapture();

            assertFalse(result);
            verify(logger, atLeastOnce()).log();
        }
    }

    @Nested
    @DisplayName("Diagnostic Information")
    class DiagnosticInformation {

        @Test
        @DisplayName("Should generate diagnostic info")
        void shouldGenerateDiagnosticInfo() {
            String diagnostics = validator.getDiagnosticInfo();

            assertNotNull(diagnostics);
            assertTrue(diagnostics.contains("Screen Capture Diagnostics"));
            assertTrue(diagnostics.contains("OS:"));
            assertTrue(diagnostics.contains("Java Version:"));
            assertTrue(diagnostics.contains("Headless Property:"));
        }

        @Test
        @DisplayName("Should include WSL detection in diagnostics")
        void shouldIncludeWSLDetection() {
            String diagnostics = validator.getDiagnosticInfo();

            assertTrue(diagnostics.contains("WSL:"));
        }

        @Test
        @DisplayName("Should include SSH detection in diagnostics")
        void shouldIncludeSSHDetection() {
            String diagnostics = validator.getDiagnosticInfo();

            assertTrue(diagnostics.contains("SSH Session:"));
        }
    }
}
