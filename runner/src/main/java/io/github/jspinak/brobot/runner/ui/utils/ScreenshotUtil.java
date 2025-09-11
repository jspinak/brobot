package io.github.jspinak.brobot.runner.ui.utils;

import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import javafx.embed.swing.SwingFXUtils;
import javafx.scene.Scene;
import javafx.scene.image.WritableImage;
import javafx.stage.Stage;

import javax.imageio.ImageIO;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Utility class for taking screenshots of JavaFX scenes. This allows autonomous verification of UI
 * styling.
 */
public class ScreenshotUtil {
    private static final Logger logger = LoggerFactory.getLogger(ScreenshotUtil.class);
    private static final DateTimeFormatter TIMESTAMP_FORMAT =
            DateTimeFormatter.ofPattern("yyyy-MM-dd_HH-mm-ss");

    /**
     * Takes a screenshot of the given stage and saves it to a file.
     *
     * @param stage The stage to capture
     * @param filename The base filename (without extension)
     * @return The file path where the screenshot was saved, or null if failed
     */
    public static String captureStage(Stage stage, String filename) {
        if (stage == null || stage.getScene() == null) {
            logger.error("Cannot capture screenshot: stage or scene is null");
            return null;
        }

        Scene scene = stage.getScene();
        WritableImage image = scene.snapshot(null);

        return saveImage(image, filename);
    }

    /**
     * Takes a screenshot of the current scene.
     *
     * @param scene The scene to capture
     * @param filename The base filename (without extension)
     * @return The file path where the screenshot was saved, or null if failed
     */
    public static String captureScene(Scene scene, String filename) {
        if (scene == null) {
            logger.error("Cannot capture screenshot: scene is null");
            return null;
        }

        WritableImage image = scene.snapshot(null);
        return saveImage(image, filename);
    }

    /**
     * Takes a screenshot with automatic timestamp naming.
     *
     * @param stage The stage to capture
     * @return The file path where the screenshot was saved, or null if failed
     */
    public static String captureWithTimestamp(Stage stage) {
        String timestamp = LocalDateTime.now().format(TIMESTAMP_FORMAT);
        String filename = "brobot-screenshot-" + timestamp;
        return captureStage(stage, filename);
    }

    /**
     * Saves a WritableImage to a PNG file.
     *
     * @param image The image to save
     * @param filename The base filename (without extension)
     * @return The file path where the image was saved, or null if failed
     */
    private static String saveImage(WritableImage image, String filename) {
        try {
            // Create screenshots directory if it doesn't exist
            File screenshotDir = new File("screenshots");
            if (!screenshotDir.exists()) {
                screenshotDir.mkdirs();
            }

            // Convert to BufferedImage
            BufferedImage bufferedImage = SwingFXUtils.fromFXImage(image, null);

            // Save to file
            File outputFile = new File(screenshotDir, filename + ".png");
            ImageIO.write(bufferedImage, "png", outputFile);

            String absolutePath = outputFile.getAbsolutePath();
            logger.info("Screenshot saved to: {}", absolutePath);
            return absolutePath;

        } catch (IOException e) {
            logger.error("Failed to save screenshot", e);
            return null;
        }
    }

    /**
     * Takes a screenshot and logs detailed style information about the scene. This helps with
     * autonomous style verification.
     *
     * @param stage The stage to analyze
     * @param description A description for the screenshot
     */
    public static void captureAndAnalyze(Stage stage, String description) {
        if (stage == null || stage.getScene() == null) {
            logger.error("Cannot analyze: stage or scene is null");
            return;
        }

        Scene scene = stage.getScene();

        // Log style information
        logger.info("=== Style Analysis: {} ===", description);
        logger.info("Scene fill: {}", scene.getFill());
        logger.info("Scene stylesheets: {}", scene.getStylesheets());

        if (scene.getRoot() != null) {
            logger.info("Root style: {}", scene.getRoot().getStyle());
            logger.info("Root style classes: {}", scene.getRoot().getStyleClass());
        }

        // Perform regular overlap detection
        OverlapDetector.logOverlaps(scene);

        // Perform smart overlap detection (filters false positives)
        SmartOverlapDetector.logSmartOverlaps(scene);

        // Analyze spacing (minimum 8 pixels between elements)
        OverlapDetector.analyzeSpacing(scene, 8.0);

        // Perform comprehensive styling verification
        ComprehensiveStylingVerifier.VerificationResult verificationResult =
                ComprehensiveStylingVerifier.verify(scene);
        ComprehensiveStylingVerifier.logVerificationResults(verificationResult);

        // Analyze UI structure to understand duplication
        UIStructureAnalyzer.analyzeScene(scene);
        UIStructureAnalyzer.findDuplicateTextNodes(scene);

        // Take screenshot
        String filepath = captureWithTimestamp(stage);
        if (filepath != null) {
            logger.info("Screenshot saved for analysis: {}", filepath);
        }
    }
}
