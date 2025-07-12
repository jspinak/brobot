package io.github.jspinak.brobot.runner.ui.testing;

import io.github.jspinak.brobot.runner.testutils.JavaFXTestBase;
import javafx.embed.swing.SwingFXUtils;
import javafx.scene.Node;
import javafx.scene.Scene;
import javafx.scene.SnapshotParameters;
import javafx.scene.image.WritableImage;
import javafx.scene.paint.Color;
import javafx.stage.Stage;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.condition.DisabledIfEnvironmentVariable;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Visual regression testing framework for UI components.
 * Captures screenshots and compares them against baseline images.
 */
@DisabledIfEnvironmentVariable(named = "CI", matches = "true", disabledReason = "Visual tests require display")
public abstract class VisualRegressionTest extends JavaFXTestBase {
    
    private static final String BASELINE_DIR = "src/test/resources/visual-baselines";
    private static final String OUTPUT_DIR = "build/visual-regression";
    private static final String DIFF_DIR = "build/visual-regression/diffs";
    private static final double DEFAULT_THRESHOLD = 0.01; // 1% difference allowed
    
    private Path baselinePath;
    private Path outputPath;
    private Path diffPath;
    
    @BeforeEach
    void setUpVisualRegression() throws IOException {
        // Create directories if they don't exist
        baselinePath = Paths.get(BASELINE_DIR);
        outputPath = Paths.get(OUTPUT_DIR);
        diffPath = Paths.get(DIFF_DIR);
        
        Files.createDirectories(baselinePath);
        Files.createDirectories(outputPath);
        Files.createDirectories(diffPath);
    }
    
    /**
     * Captures a screenshot of a node and compares it against baseline.
     *
     * @param node The node to capture
     * @param testName Name for the test/screenshot
     * @return true if the images match within threshold
     */
    protected boolean captureAndCompare(Node node, String testName) {
        return captureAndCompare(node, testName, DEFAULT_THRESHOLD);
    }
    
    /**
     * Captures a screenshot of a node and compares it against baseline.
     *
     * @param node The node to capture
     * @param testName Name for the test/screenshot
     * @param threshold Allowed difference percentage (0.0 to 1.0)
     * @return true if the images match within threshold
     */
    protected boolean captureAndCompare(Node node, String testName, double threshold) {
        try {
            // Capture current screenshot
            BufferedImage currentImage = captureNode(node);
            
            // Save current screenshot
            File currentFile = new File(outputPath.toFile(), testName + ".png");
            ImageIO.write(currentImage, "png", currentFile);
            
            // Check if baseline exists
            File baselineFile = new File(baselinePath.toFile(), testName + ".png");
            if (!baselineFile.exists()) {
                // First run - create baseline
                ImageIO.write(currentImage, "png", baselineFile);
                System.out.println("Created baseline image: " + baselineFile.getPath());
                return true;
            }
            
            // Load baseline image
            BufferedImage baselineImage = ImageIO.read(baselineFile);
            
            // Compare images
            ImageComparison comparison = compareImages(baselineImage, currentImage);
            
            // Save diff image if there are differences
            if (comparison.getDifferencePercentage() > 0) {
                File diffFile = new File(diffPath.toFile(), testName + "-diff.png");
                ImageIO.write(comparison.getDiffImage(), "png", diffFile);
            }
            
            // Log results
            System.out.println(String.format(
                "Visual regression test '%s': %.2f%% difference (threshold: %.2f%%)",
                testName,
                comparison.getDifferencePercentage() * 100,
                threshold * 100
            ));
            
            return comparison.getDifferencePercentage() <= threshold;
            
        } catch (IOException e) {
            fail("Failed to capture/compare screenshot: " + e.getMessage());
            return false;
        }
    }
    
    /**
     * Captures a screenshot of a scene.
     *
     * @param scene The scene to capture
     * @param testName Name for the test/screenshot
     * @return true if the images match within threshold
     */
    protected boolean captureAndCompare(Scene scene, String testName) {
        return captureAndCompare(scene.getRoot(), testName);
    }
    
    /**
     * Captures a node as a BufferedImage.
     */
    private BufferedImage captureNode(Node node) {
        WritableImage writableImage = node.snapshot(new SnapshotParameters(), null);
        return SwingFXUtils.fromFXImage(writableImage, null);
    }
    
    /**
     * Compares two images and returns comparison results.
     */
    private ImageComparison compareImages(BufferedImage baseline, BufferedImage current) {
        // Check dimensions
        if (baseline.getWidth() != current.getWidth() || 
            baseline.getHeight() != current.getHeight()) {
            return new ImageComparison(1.0, createDiffImage(baseline, current));
        }
        
        int width = baseline.getWidth();
        int height = baseline.getHeight();
        BufferedImage diffImage = new BufferedImage(width, height, BufferedImage.TYPE_INT_RGB);
        
        int diffPixels = 0;
        int totalPixels = width * height;
        
        for (int x = 0; x < width; x++) {
            for (int y = 0; y < height; y++) {
                int baselineRGB = baseline.getRGB(x, y);
                int currentRGB = current.getRGB(x, y);
                
                if (baselineRGB != currentRGB) {
                    diffPixels++;
                    // Highlight differences in red
                    diffImage.setRGB(x, y, 0xFF0000);
                } else {
                    // Copy original pixel with reduced brightness
                    int gray = (baselineRGB >> 16) & 0xFF;
                    gray = gray / 2; // Darken
                    diffImage.setRGB(x, y, (gray << 16) | (gray << 8) | gray);
                }
            }
        }
        
        double differencePercentage = (double) diffPixels / totalPixels;
        return new ImageComparison(differencePercentage, diffImage);
    }
    
    /**
     * Creates a diff image when dimensions don't match.
     */
    private BufferedImage createDiffImage(BufferedImage baseline, BufferedImage current) {
        int maxWidth = Math.max(baseline.getWidth(), current.getWidth());
        int maxHeight = Math.max(baseline.getHeight(), current.getHeight());
        
        BufferedImage diffImage = new BufferedImage(maxWidth, maxHeight, BufferedImage.TYPE_INT_RGB);
        
        // Fill with red to indicate complete mismatch
        for (int x = 0; x < maxWidth; x++) {
            for (int y = 0; y < maxHeight; y++) {
                diffImage.setRGB(x, y, 0xFF0000);
            }
        }
        
        return diffImage;
    }
    
    /**
     * Updates baseline images with current screenshots.
     * Use this when UI changes are intentional.
     */
    protected void updateBaseline(String testName) {
        try {
            File currentFile = new File(outputPath.toFile(), testName + ".png");
            File baselineFile = new File(baselinePath.toFile(), testName + ".png");
            
            if (currentFile.exists()) {
                Files.copy(currentFile.toPath(), baselineFile.toPath(), 
                    java.nio.file.StandardCopyOption.REPLACE_EXISTING);
                System.out.println("Updated baseline image: " + baselineFile.getPath());
            } else {
                System.err.println("No current image found to update baseline: " + testName);
            }
        } catch (IOException e) {
            fail("Failed to update baseline: " + e.getMessage());
        }
    }
    
    /**
     * Batch update all baselines.
     * Use with caution - only when all UI changes are verified.
     */
    protected void updateAllBaselines() {
        try {
            Files.list(outputPath)
                .filter(path -> path.toString().endsWith(".png"))
                .forEach(path -> {
                    String filename = path.getFileName().toString();
                    String testName = filename.substring(0, filename.length() - 4);
                    updateBaseline(testName);
                });
        } catch (IOException e) {
            fail("Failed to update baselines: " + e.getMessage());
        }
    }
    
    /**
     * Generates a visual regression report.
     */
    protected void generateReport() {
        try {
            List<String> reportLines = new ArrayList<>();
            reportLines.add("# Visual Regression Test Report");
            reportLines.add("Generated: " + LocalDateTime.now().format(
                DateTimeFormatter.ISO_LOCAL_DATE_TIME));
            reportLines.add("");
            
            // Find all diff images
            if (Files.exists(diffPath)) {
                Files.list(diffPath)
                    .filter(path -> path.toString().endsWith("-diff.png"))
                    .forEach(path -> {
                        String filename = path.getFileName().toString();
                        String testName = filename.substring(0, filename.length() - 9); // Remove "-diff.png"
                        
                        reportLines.add("## Test: " + testName);
                        reportLines.add("- Baseline: " + BASELINE_DIR + "/" + testName + ".png");
                        reportLines.add("- Current: " + OUTPUT_DIR + "/" + testName + ".png");
                        reportLines.add("- Diff: " + DIFF_DIR + "/" + filename);
                        reportLines.add("");
                    });
            }
            
            // Write report
            Path reportPath = outputPath.resolve("visual-regression-report.md");
            Files.write(reportPath, reportLines);
            System.out.println("Visual regression report generated: " + reportPath);
            
        } catch (IOException e) {
            System.err.println("Failed to generate report: " + e.getMessage());
        }
    }
    
    /**
     * Result of image comparison.
     */
    private static class ImageComparison {
        private final double differencePercentage;
        private final BufferedImage diffImage;
        
        public ImageComparison(double differencePercentage, BufferedImage diffImage) {
            this.differencePercentage = differencePercentage;
            this.diffImage = diffImage;
        }
        
        public double getDifferencePercentage() {
            return differencePercentage;
        }
        
        public BufferedImage getDiffImage() {
            return diffImage;
        }
    }
    
    /**
     * Waits for CSS animations to complete.
     */
    protected void waitForAnimations() {
        try {
            Thread.sleep(500); // Wait for typical CSS transitions
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
    }
    
    /**
     * Takes a screenshot with a specific theme applied.
     */
    protected boolean captureWithTheme(Node node, String testName, String theme) {
        // Apply theme
        node.getStyleClass().removeIf(style -> style.startsWith("theme-"));
        node.getStyleClass().add("theme-" + theme);
        
        // Wait for theme to apply
        waitForAnimations();
        
        // Capture with theme-specific name
        return captureAndCompare(node, testName + "-" + theme);
    }
}