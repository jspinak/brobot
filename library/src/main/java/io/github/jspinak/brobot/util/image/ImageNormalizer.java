package io.github.jspinak.brobot.util.image;

import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;

import javax.imageio.ImageIO;

import lombok.extern.slf4j.Slf4j;

/**
 * Normalizes images to ensure consistent bit depth and format for pattern matching.
 *
 * <p>This utility addresses common issues in GUI automation where images may have:
 *
 * <ul>
 *   <li>Different bit depths (24-bit RGB vs 32-bit ARGB)
 *   <li>Inconsistent alpha channel handling
 *   <li>Format variations between captured and saved images
 *   <li>Color space differences affecting pattern matching
 * </ul>
 *
 * <p>The normalizer provides methods to:
 *
 * <ul>
 *   <li>Convert images to standard RGB (24-bit) or ARGB (32-bit) formats
 *   <li>Handle transparent areas consistently
 *   <li>Diagnose format compatibility issues
 *   <li>Save and load images with consistent formatting
 * </ul>
 *
 * <p>This is particularly useful when working with:
 *
 * <ul>
 *   <li>Screenshots from different capture methods
 *   <li>Pattern images created in different tools
 *   <li>Cross-platform pattern matching
 *   <li>DPI-scaled environments
 * </ul>
 *
 * @since 1.0
 */
@Slf4j
public class ImageNormalizer {

    /** Default background color for transparent areas (dark gray) */
    public static final Color DEFAULT_BACKGROUND = new Color(30, 30, 30);

    /**
     * Normalizes an image to consistent RGB format (24-bit) without alpha channel. This ensures
     * saved and loaded images have the same bit depth.
     *
     * @param source The source image
     * @return A normalized RGB image, or null if source is null
     */
    public static BufferedImage normalizeToRGB(BufferedImage source) {
        return normalizeToRGB(source, DEFAULT_BACKGROUND);
    }

    /**
     * Normalizes an image to consistent RGB format (24-bit) without alpha channel, using a
     * specified background color for transparent areas.
     *
     * @param source The source image
     * @param backgroundColor The background color for transparent areas
     * @return A normalized RGB image, or null if source is null
     */
    public static BufferedImage normalizeToRGB(BufferedImage source, Color backgroundColor) {
        if (source == null) {
            return null;
        }

        // If already RGB without alpha, return as-is
        if (source.getType() == BufferedImage.TYPE_INT_RGB
                || source.getType() == BufferedImage.TYPE_3BYTE_BGR) {
            return source;
        }

        // Convert to RGB
        BufferedImage rgbImage =
                new BufferedImage(
                        source.getWidth(), source.getHeight(), BufferedImage.TYPE_INT_RGB);

        Graphics2D g = rgbImage.createGraphics();

        // Use high quality rendering to preserve image details
        g.setRenderingHint(
                RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_BILINEAR);
        g.setRenderingHint(RenderingHints.KEY_RENDERING, RenderingHints.VALUE_RENDER_QUALITY);
        g.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

        // Set background color for transparent areas
        g.setColor(backgroundColor);
        g.fillRect(0, 0, source.getWidth(), source.getHeight());

        // Draw the image
        g.drawImage(source, 0, 0, null);
        g.dispose();

        log.debug("Normalized image from type {} to RGB", getImageTypeName(source.getType()));

        return rgbImage;
    }

    /**
     * Normalizes an image to consistent ARGB format (32-bit) with alpha channel.
     *
     * @param source The source image
     * @return A normalized ARGB image, or null if source is null
     */
    public static BufferedImage normalizeToARGB(BufferedImage source) {
        if (source == null) {
            return null;
        }

        // If already ARGB, return as-is
        if (source.getType() == BufferedImage.TYPE_INT_ARGB) {
            return source;
        }

        // Convert to ARGB
        BufferedImage argbImage =
                new BufferedImage(
                        source.getWidth(), source.getHeight(), BufferedImage.TYPE_INT_ARGB);

        Graphics2D g = argbImage.createGraphics();

        // Use high quality rendering
        g.setRenderingHint(
                RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_BILINEAR);
        g.setRenderingHint(RenderingHints.KEY_RENDERING, RenderingHints.VALUE_RENDER_QUALITY);
        g.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

        // Clear with transparent background
        g.setComposite(AlphaComposite.Clear);
        g.fillRect(0, 0, source.getWidth(), source.getHeight());

        // Draw the image
        g.setComposite(AlphaComposite.SrcOver);
        g.drawImage(source, 0, 0, null);
        g.dispose();

        log.debug("Normalized image from type {} to ARGB", getImageTypeName(source.getType()));

        return argbImage;
    }

    /**
     * Saves an image with consistent RGB format to ensure matching compatibility.
     *
     * @param image The image to save
     * @param file The file to save to
     * @throws IOException If save fails
     */
    public static void saveNormalizedImage(BufferedImage image, File file) throws IOException {
        BufferedImage normalized = normalizeToRGB(image);
        ImageIO.write(normalized, "png", file);
        log.debug("Saved normalized RGB image to: {}", file.getPath());
    }

    /**
     * Loads and normalizes an image to RGB format.
     *
     * @param file The file to load from
     * @return The normalized image
     * @throws IOException If load fails
     */
    public static BufferedImage loadNormalizedImage(File file) throws IOException {
        BufferedImage loaded = ImageIO.read(file);
        if (loaded == null) {
            throw new IOException("Failed to load image from: " + file.getPath());
        }
        return normalizeToRGB(loaded);
    }

    /**
     * Checks if two images have compatible formats for matching.
     *
     * @param img1 First image
     * @param img2 Second image
     * @return true if formats are compatible, false otherwise
     */
    public static boolean areFormatsCompatible(BufferedImage img1, BufferedImage img2) {
        if (img1 == null || img2 == null) {
            return false;
        }

        int type1 = img1.getType();
        int type2 = img2.getType();

        // Check if both have alpha or both don't
        boolean hasAlpha1 = img1.getColorModel().hasAlpha();
        boolean hasAlpha2 = img2.getColorModel().hasAlpha();

        if (hasAlpha1 != hasAlpha2) {
            log.warn(
                    "Image format mismatch - Image1: {} (alpha: {}), Image2: {} (alpha: {})",
                    getImageTypeName(type1),
                    hasAlpha1,
                    getImageTypeName(type2),
                    hasAlpha2);
            return false;
        }

        return true;
    }

    /**
     * Diagnoses image format issues and logs detailed information.
     *
     * @param image The image to diagnose
     * @param label A label for the image in logs
     */
    public static void diagnoseImage(BufferedImage image, String label) {
        if (image == null) {
            log.info("[{}] Image is null", label);
            return;
        }

        log.info("[{}] Image diagnostics:", label);
        log.info("  - Dimensions: {}x{}", image.getWidth(), image.getHeight());
        log.info("  - Type: {} ({})", image.getType(), getImageTypeName(image.getType()));
        log.info("  - Has Alpha: {}", image.getColorModel().hasAlpha());
        log.info("  - Color Model: {}", image.getColorModel().getClass().getSimpleName());
        log.info("  - Num Components: {}", image.getColorModel().getNumComponents());
        log.info("  - Bits Per Pixel: {}", image.getColorModel().getPixelSize());
    }

    /**
     * Creates a diagnostic report string for an image.
     *
     * @param image The image to diagnose
     * @return A diagnostic report string
     */
    public static String getDiagnosticReport(BufferedImage image) {
        if (image == null) {
            return "Image is null";
        }

        StringBuilder report = new StringBuilder();
        report.append(String.format("Dimensions: %dx%d, ", image.getWidth(), image.getHeight()));
        report.append(String.format("Type: %s, ", getImageTypeName(image.getType())));
        report.append(String.format("Has Alpha: %s, ", image.getColorModel().hasAlpha()));
        report.append(String.format("Bits/Pixel: %d", image.getColorModel().getPixelSize()));

        return report.toString();
    }

    /**
     * Gets a human-readable name for a BufferedImage type constant.
     *
     * @param type The BufferedImage type constant
     * @return A human-readable description of the type
     */
    public static String getImageTypeName(int type) {
        switch (type) {
            case BufferedImage.TYPE_INT_RGB:
                return "RGB (24-bit)";
            case BufferedImage.TYPE_INT_ARGB:
                return "ARGB (32-bit)";
            case BufferedImage.TYPE_3BYTE_BGR:
                return "BGR (24-bit)";
            case BufferedImage.TYPE_4BYTE_ABGR:
                return "ABGR (32-bit)";
            case BufferedImage.TYPE_BYTE_GRAY:
                return "GRAY (8-bit)";
            case BufferedImage.TYPE_BYTE_BINARY:
                return "BINARY (1-bit)";
            case BufferedImage.TYPE_INT_BGR:
                return "BGR (24-bit int)";
            case BufferedImage.TYPE_BYTE_INDEXED:
                return "INDEXED (8-bit)";
            case BufferedImage.TYPE_USHORT_GRAY:
                return "GRAY (16-bit)";
            case BufferedImage.TYPE_USHORT_565_RGB:
                return "RGB 565 (16-bit)";
            case BufferedImage.TYPE_USHORT_555_RGB:
                return "RGB 555 (15-bit)";
            default:
                return "CUSTOM/UNKNOWN (type=" + type + ")";
        }
    }
}
