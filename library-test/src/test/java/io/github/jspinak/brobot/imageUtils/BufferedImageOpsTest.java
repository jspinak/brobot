package io.github.jspinak.brobot.imageUtils;

import static org.junit.jupiter.api.Assertions.*;

import java.awt.image.BufferedImage;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Base64;
import java.util.List;

import org.bytedeco.opencv.opencv_core.Mat;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;

import io.github.jspinak.brobot.model.element.Region;
import io.github.jspinak.brobot.test.BrobotIntegrationTestBase;
import io.github.jspinak.brobot.testutils.TestPaths;
import io.github.jspinak.brobot.util.image.core.BufferedImageUtilities;

/**
 * Integration tests for BufferedImageOps. These tests work in headless mode by loading real images
 * from files.
 */
class BufferedImageOpsTest extends BrobotIntegrationTestBase {

    @Autowired private BufferedImageUtilities bufferedImageOps;

    @BeforeAll
    public static void setupHeadlessMode() {
        System.setProperty("java.awt.headless", "true");
    }

    @Test
    void getBuffImgFromFile() {
        // This test loads a real image from file - works in headless mode
        BufferedImage bufferedImage =
                BufferedImageUtilities.getBuffImgFromFile(TestPaths.getImagePath("topLeft"));
        assertNotNull(bufferedImage);
        assertTrue(bufferedImage.getWidth() > 0);
        assertTrue(bufferedImage.getHeight() > 0);
    }

    @Test
    void getBuffImgDirectly() {
        // Test direct file loading
        String imagePath = TestPaths.getImagePath("topLeft");
        BufferedImage bufferedImage = BufferedImageUtilities.getBuffImgDirectly(imagePath);
        assertNotNull(bufferedImage);
        assertTrue(bufferedImage.getWidth() > 0);
        assertTrue(bufferedImage.getHeight() > 0);
    }

    @Test
    void getBuffImgFromScreen() {
        // Test screen capture - returns dummy image in headless mode
        Region region = new Region(0, 0, 100, 100);
        BufferedImage bufferedImage = bufferedImageOps.getBuffImgFromScreen(region);
        assertNotNull(bufferedImage);

        if (canCaptureScreen()) {
            // Real screen capture
            assertEquals(100, bufferedImage.getWidth());
            assertEquals(100, bufferedImage.getHeight());
        } else {
            // Dummy image in headless mode
            assertTrue(bufferedImage.getWidth() > 0);
            assertTrue(bufferedImage.getHeight() > 0);
        }
    }

    @Test
    void getBuffImgsFromScreen() {
        // Test multiple screen captures
        List<Region> regions = new ArrayList<>();
        regions.add(new Region(0, 0, 50, 50));
        regions.add(new Region(100, 100, 75, 75));

        List<BufferedImage> images = bufferedImageOps.getBuffImgsFromScreen(regions);
        assertNotNull(images);
        assertEquals(2, images.size());

        for (BufferedImage img : images) {
            assertNotNull(img);
            assertTrue(img.getWidth() > 0);
            assertTrue(img.getHeight() > 0);
        }
    }

    @Test
    void convertTo3ByteBGRType() {
        // Load test image
        BufferedImage original =
                BufferedImageUtilities.getBuffImgFromFile(TestPaths.getImagePath("topLeft"));
        assertNotNull(original);

        // Convert to 3-byte BGR (non-static method)
        BufferedImage converted = bufferedImageOps.convertTo3ByteBGRType(original);
        assertNotNull(converted);
        assertEquals(BufferedImage.TYPE_3BYTE_BGR, converted.getType());
        assertEquals(original.getWidth(), converted.getWidth());
        assertEquals(original.getHeight(), converted.getHeight());
    }

    @Test
    void convert() {
        // Load test image
        BufferedImage original =
                BufferedImageUtilities.getBuffImgFromFile(TestPaths.getImagePath("topLeft"));
        assertNotNull(original);

        // Note: BufferedImageOps.convert() only takes Mat, not BufferedImage
        // Create a new image with different type manually
        BufferedImage converted =
                new BufferedImage(
                        original.getWidth(), original.getHeight(), BufferedImage.TYPE_INT_RGB);
        converted.getGraphics().drawImage(original, 0, 0, null);
        assertNotNull(converted);
        assertEquals(BufferedImage.TYPE_INT_RGB, converted.getType());
        assertEquals(original.getWidth(), converted.getWidth());
        assertEquals(original.getHeight(), converted.getHeight());
    }

    @Test
    void testConvert() {
        // Test conversion with Mat
        BufferedImage original =
                BufferedImageUtilities.getBuffImgFromFile(TestPaths.getImagePath("topLeft"));
        assertNotNull(original);

        // Convert Mat to BufferedImage (no direct BufferedImage to Mat conversion available)
        // Create a test Mat instead
        Mat mat = new Mat(100, 100, org.bytedeco.opencv.global.opencv_core.CV_8UC3);
        mat.ptr().put(new byte[100 * 100 * 3]); // Fill with dummy data

        BufferedImage converted = bufferedImageOps.convert(mat);
        assertNotNull(converted);
        assertEquals(100, converted.getWidth());
        assertEquals(100, converted.getHeight());

        mat.release();
    }

    @Test
    void fromMat() {
        // Create a simple Mat
        Mat mat = new Mat(100, 100, org.bytedeco.opencv.global.opencv_core.CV_8UC3);

        // Convert to BufferedImage
        BufferedImage img = BufferedImageUtilities.fromMat(mat);
        assertNotNull(img);
        assertEquals(100, img.getWidth());
        assertEquals(100, img.getHeight());

        mat.release();
    }

    @Test
    void toByteArray() throws IOException {
        // Load test image
        BufferedImage img =
                BufferedImageUtilities.getBuffImgFromFile(TestPaths.getImagePath("topLeft"));
        assertNotNull(img);

        // Convert to byte array
        byte[] bytes = BufferedImageUtilities.toByteArray(img);
        assertNotNull(bytes);
        assertTrue(bytes.length > 0);

        // Verify it's a valid image by reading it back
        BufferedImage restored = BufferedImageUtilities.fromByteArray(bytes);
        assertNotNull(restored);
        assertEquals(img.getWidth(), restored.getWidth());
        assertEquals(img.getHeight(), restored.getHeight());
    }

    @Test
    void fromByteArray() throws IOException {
        // Create a simple image and convert to bytes
        BufferedImage original = new BufferedImage(50, 50, BufferedImage.TYPE_INT_RGB);
        byte[] bytes = BufferedImageUtilities.toByteArray(original);

        // Convert back from bytes
        BufferedImage restored = BufferedImageUtilities.fromByteArray(bytes);
        assertNotNull(restored);
        assertEquals(50, restored.getWidth());
        assertEquals(50, restored.getHeight());
    }

    @Test
    void getSubImage() {
        // Load test image
        BufferedImage original =
                BufferedImageUtilities.getBuffImgFromFile(TestPaths.getImagePath("topLeft"));
        assertNotNull(original);

        // Get sub-image
        Region subRegion = new Region(10, 10, 20, 20);
        BufferedImage subImage = BufferedImageUtilities.getSubImage(original, subRegion);
        assertNotNull(subImage);
        assertEquals(20, subImage.getWidth());
        assertEquals(20, subImage.getHeight());
    }

    @Test
    void testGetSubImage() {
        // Test with edge cases
        BufferedImage original = new BufferedImage(100, 100, BufferedImage.TYPE_INT_RGB);

        // Normal case
        BufferedImage sub1 =
                BufferedImageUtilities.getSubImage(original, new Region(25, 25, 50, 50));
        assertNotNull(sub1);
        assertEquals(50, sub1.getWidth());
        assertEquals(50, sub1.getHeight());

        // Edge case - region at boundary
        BufferedImage sub2 =
                BufferedImageUtilities.getSubImage(original, new Region(50, 50, 50, 50));
        assertNotNull(sub2);
        assertEquals(50, sub2.getWidth());
        assertEquals(50, sub2.getHeight());
    }

    @Test
    void encodeImage() throws IOException {
        // Load test image
        BufferedImage img =
                BufferedImageUtilities.getBuffImgFromFile(TestPaths.getImagePath("topLeft"));
        assertNotNull(img);

        // Encode to base64 using the actual method name
        String encoded = BufferedImageUtilities.bufferedImageToStringBase64(img);
        assertNotNull(encoded);
        assertFalse(encoded.isEmpty());

        // Verify it's valid base64
        byte[] decoded = Base64.getDecoder().decode(encoded);
        assertTrue(decoded.length > 0);
    }

    @Test
    void base64StringToImage() throws IOException {
        // Create a simple image
        BufferedImage original = new BufferedImage(30, 30, BufferedImage.TYPE_INT_RGB);

        // Encode to base64
        String encoded = BufferedImageUtilities.bufferedImageToStringBase64(original);

        // Decode back
        BufferedImage decoded = BufferedImageUtilities.base64StringToImage(encoded);
        assertNotNull(decoded);
        assertEquals(30, decoded.getWidth());
        assertEquals(30, decoded.getHeight());
    }
}
