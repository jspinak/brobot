package io.github.jspinak.brobot.capture.provider;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.IOException;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;

@ExtendWith(MockitoExtension.class)
class RobotCaptureProviderTest {

    private RobotCaptureProvider provider;

    @BeforeEach
    void setUp() {
        provider = new RobotCaptureProvider();
    }

    @Test
    void testProviderIsAvailable() {
        // Robot should always be available (unless in headless environment)
        boolean available = provider.isAvailable();

        GraphicsEnvironment ge = GraphicsEnvironment.getLocalGraphicsEnvironment();
        if (ge.isHeadlessInstance()) {
            assertFalse(available, "Provider should not be available in headless environment");
        } else {
            assertTrue(available, "Provider should be available in non-headless environment");
        }
    }

    @Test
    void testProviderName() {
        assertEquals("Robot", provider.getName());
    }

    @Test
    void testCaptureScreen() throws IOException {
        // Skip test in headless environment
        GraphicsEnvironment ge = GraphicsEnvironment.getLocalGraphicsEnvironment();
        if (ge.isHeadlessInstance()) {
            return;
        }

        BufferedImage capture = provider.captureScreen();

        assertNotNull(capture);
        assertTrue(capture.getWidth() > 0);
        assertTrue(capture.getHeight() > 0);

        // Check if scaling was applied
        boolean scaleToPhysical =
                (boolean) ReflectionTestUtils.getField(provider, "scaleToPhysical");
        if (scaleToPhysical) {
            // May have been scaled to physical resolution
            System.out.println("Captured at: " + capture.getWidth() + "x" + capture.getHeight());
        }
    }

    @Test
    void testCaptureRegion() throws IOException {
        // Skip test in headless environment
        GraphicsEnvironment ge = GraphicsEnvironment.getLocalGraphicsEnvironment();
        if (ge.isHeadlessInstance()) {
            return;
        }

        Rectangle region = new Rectangle(100, 100, 200, 150);
        BufferedImage capture = provider.captureRegion(region);

        assertNotNull(capture);

        // Check dimensions match or are scaled proportionally
        double detectedScale = (double) ReflectionTestUtils.getField(provider, "detectedScale");
        boolean scaleToPhysical =
                (boolean) ReflectionTestUtils.getField(provider, "scaleToPhysical");
        boolean scaleDetected = (boolean) ReflectionTestUtils.getField(provider, "scaleDetected");

        if (scaleToPhysical && scaleDetected) {
            // Dimensions should be scaled
            assertEquals((int) (region.width * detectedScale), capture.getWidth(), 1);
            assertEquals((int) (region.height * detectedScale), capture.getHeight(), 1);
        } else {
            // Dimensions should match exactly
            assertEquals(region.width, capture.getWidth());
            assertEquals(region.height, capture.getHeight());
        }
    }

    @Test
    void testScalingDetection() {
        // Test scaling detection logic
        ReflectionTestUtils.setField(provider, "expectedPhysicalWidth", 1920);
        ReflectionTestUtils.setField(provider, "expectedPhysicalHeight", 1080);

        // Simulate different screen sizes
        testScalingScenario(1536, 864, 1.25, "125% scaling");
        testScalingScenario(1280, 720, 1.5, "150% scaling");
        testScalingScenario(960, 540, 2.0, "200% scaling");
        testScalingScenario(1920, 1080, 1.0, "No scaling");
    }

    private void testScalingScenario(int width, int height, double expectedScale, String scenario) {
        // Create test image at logical resolution
        BufferedImage testImage = new BufferedImage(width, height, BufferedImage.TYPE_INT_RGB);

        // Test needsScaling method through reflection
        try {
            ReflectionTestUtils.setField(provider, "scaleDetected", true);
            ReflectionTestUtils.setField(provider, "detectedScale", expectedScale);

            java.lang.reflect.Method needsScaling =
                    provider.getClass().getDeclaredMethod("needsScaling", BufferedImage.class);
            needsScaling.setAccessible(true);

            boolean needs = (boolean) needsScaling.invoke(provider, testImage);

            if (expectedScale > 1.0) {
                assertTrue(needs, scenario + " should need scaling");
            } else {
                assertFalse(needs, scenario + " should not need scaling");
            }
        } catch (Exception e) {
            fail("Failed to test scaling scenario: " + e.getMessage());
        }
    }

    @Test
    void testResolutionType() {
        // Test resolution type reporting
        ReflectionTestUtils.setField(provider, "scaleToPhysical", true);
        ReflectionTestUtils.setField(provider, "scaleDetected", true);

        assertEquals(
                CaptureProvider.ResolutionType.PHYSICAL,
                provider.getResolutionType(),
                "Should report PHYSICAL when scaling to physical");

        ReflectionTestUtils.setField(provider, "scaleToPhysical", false);

        String javaVersion = System.getProperty("java.version");
        if (javaVersion.startsWith("1.8")) {
            assertEquals(
                    CaptureProvider.ResolutionType.PHYSICAL,
                    provider.getResolutionType(),
                    "Java 8 should report PHYSICAL");
        } else {
            assertEquals(
                    CaptureProvider.ResolutionType.LOGICAL,
                    provider.getResolutionType(),
                    "Java 21+ should report LOGICAL when not scaling");
        }
    }

    @Test
    void testImageScaling() {
        // Test the scaling algorithm
        BufferedImage source = new BufferedImage(100, 100, BufferedImage.TYPE_INT_RGB);
        Graphics2D g = source.createGraphics();
        g.setColor(Color.RED);
        g.fillRect(0, 0, 50, 50);
        g.setColor(Color.BLUE);
        g.fillRect(50, 50, 50, 50);
        g.dispose();

        // Test scaling through reflection
        try {
            java.lang.reflect.Method scaleImage =
                    provider.getClass()
                            .getDeclaredMethod(
                                    "scaleImage", BufferedImage.class, int.class, int.class);
            scaleImage.setAccessible(true);

            BufferedImage scaled = (BufferedImage) scaleImage.invoke(provider, source, 200, 200);

            assertNotNull(scaled);
            assertEquals(200, scaled.getWidth());
            assertEquals(200, scaled.getHeight());

            // Check that colors are preserved (roughly)
            Color topLeft = new Color(scaled.getRGB(25, 25));
            Color bottomRight = new Color(scaled.getRGB(175, 175));

            assertEquals(Color.RED.getRed(), topLeft.getRed(), 10);
            assertEquals(Color.BLUE.getBlue(), bottomRight.getBlue(), 10);

        } catch (Exception e) {
            fail("Failed to test image scaling: " + e.getMessage());
        }
    }

    @Test
    void testMultiScreenSupport() throws IOException {
        // Skip test in headless environment
        GraphicsEnvironment ge = GraphicsEnvironment.getLocalGraphicsEnvironment();
        if (ge.isHeadlessInstance()) {
            return;
        }

        GraphicsDevice[] devices = ge.getScreenDevices();

        for (int i = 0; i < devices.length; i++) {
            try {
                BufferedImage capture = provider.captureScreen(i);
                assertNotNull(capture, "Should capture screen " + i);
                assertTrue(capture.getWidth() > 0);
                assertTrue(capture.getHeight() > 0);
            } catch (IOException e) {
                // Some screens might not be accessible
                System.out.println("Could not capture screen " + i + ": " + e.getMessage());
            }
        }
    }
}
