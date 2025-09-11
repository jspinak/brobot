package io.github.jspinak.brobot.util.image;

import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

import javax.imageio.ImageIO;

import lombok.extern.slf4j.Slf4j;

/**
 * Utility for scaling pattern images to handle DPI and resolution differences.
 *
 * <p>This utility is particularly useful for:
 *
 * <ul>
 *   <li>Handling DPI scaling differences between development and production environments
 *   <li>Pre-scaling patterns for better performance compared to runtime scaling
 *   <li>Creating pattern variants for different screen resolutions
 *   <li>Testing pattern matching at different scales
 * </ul>
 *
 * <p>The scaler preserves image quality using bilinear interpolation and anti-aliasing, ensuring
 * that scaled patterns maintain their matching characteristics.
 *
 * @since 1.0
 */
@Slf4j
public class PatternScaler {

    /**
     * Scales all PNG patterns in a directory and saves them with a suffix.
     *
     * @param sourceDir The directory containing pattern images
     * @param scaleFactor The scale factor (e.g., 0.8 for 80%, 1.25 for 125%)
     * @param suffix The suffix to append to scaled files (e.g., "-80", "-125")
     */
    public static void scalePatterns(String sourceDir, float scaleFactor, String suffix) {
        try {
            Path sourcePath = Paths.get(sourceDir);
            if (!Files.exists(sourcePath)) {
                log.error("Source directory does not exist: {}", sourceDir);
                return;
            }

            Files.walk(sourcePath)
                    .filter(Files::isRegularFile)
                    .filter(p -> p.toString().toLowerCase().endsWith(".png"))
                    .filter(p -> !p.getFileName().toString().contains(suffix)) // Avoid re-scaling
                    .forEach(path -> scaleAndSaveImage(path, scaleFactor, suffix));

            log.info("Pattern scaling complete for directory: {}", sourceDir);

        } catch (IOException e) {
            log.error("Error scaling patterns in directory: " + sourceDir, e);
        }
    }

    /**
     * Scales all PNG patterns in a directory and saves them in a target directory.
     *
     * @param sourceDir The directory containing pattern images
     * @param targetDir The directory to save scaled images
     * @param scaleFactor The scale factor (e.g., 0.8 for 80%, 1.25 for 125%)
     * @param suffix Optional suffix to append to scaled files (can be null)
     */
    public static void scalePatterns(
            String sourceDir, String targetDir, float scaleFactor, String suffix) {
        try {
            Path sourcePath = Paths.get(sourceDir);
            Path targetPath = Paths.get(targetDir);

            if (!Files.exists(sourcePath)) {
                log.error("Source directory does not exist: {}", sourceDir);
                return;
            }

            // Create target directory if it doesn't exist
            if (!Files.exists(targetPath)) {
                Files.createDirectories(targetPath);
            }

            Files.walk(sourcePath)
                    .filter(Files::isRegularFile)
                    .filter(p -> p.toString().toLowerCase().endsWith(".png"))
                    .forEach(path -> scaleAndSaveImage(path, targetPath, scaleFactor, suffix));

            log.info("Pattern scaling complete: {} -> {}", sourceDir, targetDir);

        } catch (IOException e) {
            log.error("Error scaling patterns from {} to {}", sourceDir, targetDir, e);
        }
    }

    /** Scales a single image and saves it with a suffix in the same directory. */
    private static void scaleAndSaveImage(Path imagePath, float scaleFactor, String suffix) {
        try {
            BufferedImage scaled = scaleImage(imagePath, scaleFactor);
            if (scaled == null) return;

            File inputFile = imagePath.toFile();
            String outputName = generateOutputName(inputFile.getName(), suffix);
            File outputFile = new File(inputFile.getParent(), outputName);

            ImageIO.write(scaled, "PNG", outputFile);

            log.debug(
                    "Scaled {} at {}% -> {}",
                    inputFile.getName(), (int) (scaleFactor * 100), outputName);

        } catch (IOException e) {
            log.error("Error scaling image: " + imagePath, e);
        }
    }

    /** Scales a single image and saves it in the target directory. */
    private static void scaleAndSaveImage(
            Path imagePath, Path targetDir, float scaleFactor, String suffix) {
        try {
            BufferedImage scaled = scaleImage(imagePath, scaleFactor);
            if (scaled == null) return;

            String outputName = generateOutputName(imagePath.getFileName().toString(), suffix);
            File outputFile = targetDir.resolve(outputName).toFile();

            ImageIO.write(scaled, "PNG", outputFile);

            log.debug(
                    "Scaled {} at {}% -> {}",
                    imagePath.getFileName(),
                    (int) (scaleFactor * 100),
                    outputFile.getAbsolutePath());

        } catch (IOException e) {
            log.error("Error scaling image: " + imagePath, e);
        }
    }

    /**
     * Scales a BufferedImage by the given factor.
     *
     * @param image The image to scale
     * @param scaleFactor The scale factor
     * @return The scaled image
     */
    public static BufferedImage scaleImage(BufferedImage image, float scaleFactor) {
        if (image == null) return null;

        int newWidth = Math.round(image.getWidth() * scaleFactor);
        int newHeight = Math.round(image.getHeight() * scaleFactor);

        BufferedImage scaled = new BufferedImage(newWidth, newHeight, BufferedImage.TYPE_INT_ARGB);
        Graphics2D g2d = scaled.createGraphics();

        // Set high quality rendering hints
        g2d.setRenderingHint(
                RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_BILINEAR);
        g2d.setRenderingHint(RenderingHints.KEY_RENDERING, RenderingHints.VALUE_RENDER_QUALITY);
        g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

        // Draw scaled image
        g2d.drawImage(image, 0, 0, newWidth, newHeight, null);
        g2d.dispose();

        return scaled;
    }

    /** Helper method to scale an image file. */
    private static BufferedImage scaleImage(Path imagePath, float scaleFactor) throws IOException {
        File inputFile = imagePath.toFile();
        BufferedImage original = ImageIO.read(inputFile);

        if (original == null) {
            log.warn("Could not read image: {}", imagePath);
            return null;
        }

        return scaleImage(original, scaleFactor);
    }

    /** Generates output filename with suffix. */
    private static String generateOutputName(String originalName, String suffix) {
        if (suffix == null || suffix.isEmpty()) {
            return originalName;
        }

        int lastDot = originalName.lastIndexOf('.');
        if (lastDot == -1) {
            return originalName + suffix;
        }

        String nameWithoutExt = originalName.substring(0, lastDot);
        String extension = originalName.substring(lastDot);
        return nameWithoutExt + suffix + extension;
    }
}
