package io.github.jspinak.brobot.debug;

import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;

import javax.imageio.ImageIO;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Component;

import io.github.jspinak.brobot.action.ActionResult;
import io.github.jspinak.brobot.action.ObjectCollection;
import io.github.jspinak.brobot.capture.BrobotScreenCapture;
import io.github.jspinak.brobot.model.element.Pattern;
import io.github.jspinak.brobot.model.element.Region;
import io.github.jspinak.brobot.model.match.Match;
import io.github.jspinak.brobot.model.state.StateImage;

import lombok.extern.slf4j.Slf4j;

/**
 * Creates visual debugging output with annotated screenshots. Highlights search regions, matches,
 * and provides visual comparison grids.
 */
@Slf4j
@Component
@ConditionalOnProperty(name = "brobot.debug.image.visual.enabled", havingValue = "true")
public class VisualDebugRenderer {

    @Autowired(required = false)
    private ImageDebugConfig config;

    @Autowired(required = false)
    private BrobotScreenCapture screenCapture;

    private static final DateTimeFormatter TIMESTAMP_FORMAT =
            DateTimeFormatter.ofPattern("yyyyMMdd_HHmmss");

    // Visual annotation colors
    private static final Color COLOR_SEARCH_REGION =
            new Color(0, 128, 255, 100); // Blue semi-transparent
    private static final Color COLOR_MATCH_SUCCESS =
            new Color(0, 255, 0, 150); // Green semi-transparent
    private static final Color COLOR_MATCH_FAILED =
            new Color(255, 0, 0, 100); // Red semi-transparent
    private static final Color COLOR_BEST_MATCH = new Color(255, 255, 0, 200); // Yellow
    private static final Color TEXT_BACKGROUND =
            new Color(0, 0, 0, 180); // Dark background for text
    private static final Color TEXT_COLOR = Color.WHITE;

    /** Create an annotated screenshot showing search regions and matches. */
    public BufferedImage createAnnotatedScreenshot(
            ObjectCollection collection, ActionResult result, Region searchRegion) {

        if (screenCapture == null) {
            log.warn("BrobotScreenCapture not available for visual debugging");
            return null;
        }

        BufferedImage screenshot = screenCapture.capture();
        if (screenshot == null) {
            log.warn("Could not capture screenshot for visual debugging");
            return null;
        }

        // Create a copy to draw on
        BufferedImage annotated =
                new BufferedImage(
                        screenshot.getWidth(), screenshot.getHeight(), BufferedImage.TYPE_INT_ARGB);

        Graphics2D g2d = annotated.createGraphics();
        setupGraphics(g2d);

        // Draw the original screenshot
        g2d.drawImage(screenshot, 0, 0, null);

        // Draw search region if configured
        if (config.getVisual().isShowSearchRegions() && searchRegion != null) {
            drawSearchRegion(g2d, searchRegion);
        }

        // Draw matches
        if (result != null && !result.getMatchList().isEmpty()) {
            drawMatches(g2d, result.getMatchList(), collection);
        }

        // Draw failed regions if no matches found
        if (config.getVisual().isShowFailedRegions() && (result == null || !result.isSuccess())) {
            drawFailedRegions(g2d, searchRegion);
        }

        // Draw legend
        drawLegend(g2d, annotated.getWidth(), annotated.getHeight());

        g2d.dispose();
        return annotated;
    }

    /** Create a comparison grid showing pattern vs matched regions. */
    public BufferedImage createComparisonGrid(
            StateImage stateImage, List<Match> matches, BufferedImage screenshot) {

        if (stateImage.getPatterns().isEmpty() || matches.isEmpty()) {
            return null;
        }

        Pattern pattern = stateImage.getPatterns().get(0);
        BufferedImage patternImage = pattern.getBImage();
        if (patternImage == null) {
            return null;
        }

        int gridCols = Math.min(matches.size(), 4);
        int gridRows = (int) Math.ceil(matches.size() / (double) gridCols);

        int cellWidth = Math.max(patternImage.getWidth(), 200);
        int cellHeight = Math.max(patternImage.getHeight(), 200);
        int padding = 10;

        int gridWidth = (cellWidth + padding) * gridCols + padding;
        int gridHeight = (cellHeight + padding) * gridRows + padding + 50; // Extra space for header

        BufferedImage grid = new BufferedImage(gridWidth, gridHeight, BufferedImage.TYPE_INT_RGB);
        Graphics2D g2d = grid.createGraphics();
        setupGraphics(g2d);

        // White background
        g2d.setColor(Color.WHITE);
        g2d.fillRect(0, 0, gridWidth, gridHeight);

        // Draw header
        g2d.setColor(Color.BLACK);
        g2d.setFont(new Font("Arial", Font.BOLD, 16));
        g2d.drawString("Pattern vs Matches Comparison", padding, 30);

        // Draw pattern in first cell
        g2d.setColor(Color.DARK_GRAY);
        g2d.drawString("PATTERN", padding, 55);
        g2d.drawImage(patternImage, padding, 60, null);
        g2d.setColor(COLOR_SEARCH_REGION);
        g2d.setStroke(new BasicStroke(2));
        g2d.drawRect(padding - 1, 59, patternImage.getWidth() + 2, patternImage.getHeight() + 2);

        // Draw matches
        int col = 1, row = 0;
        for (int i = 0; i < matches.size(); i++) {
            if (col >= gridCols) {
                col = 0;
                row++;
            }

            Match match = matches.get(i);
            int x = padding + col * (cellWidth + padding);
            int y = 60 + row * (cellHeight + padding);

            // Extract matched region from screenshot
            BufferedImage matchRegion = extractMatchRegion(screenshot, match);
            if (matchRegion != null) {
                g2d.setColor(Color.DARK_GRAY);
                g2d.setFont(new Font("Arial", Font.PLAIN, 12));
                g2d.drawString(
                        String.format("Match %d (%.1f%%)", i + 1, match.getScore() * 100),
                        x,
                        y - 5);

                // Scale to fit cell if needed
                if (matchRegion.getWidth() > cellWidth || matchRegion.getHeight() > cellHeight) {
                    matchRegion = scaleImage(matchRegion, cellWidth, cellHeight);
                }

                g2d.drawImage(matchRegion, x, y, null);

                // Draw border based on score
                g2d.setStroke(new BasicStroke(2));
                if (i == 0 && config.getVisual().isHighlightBestMatch()) {
                    g2d.setColor(COLOR_BEST_MATCH);
                } else {
                    g2d.setColor(COLOR_MATCH_SUCCESS);
                }
                g2d.drawRect(x - 1, y - 1, matchRegion.getWidth() + 2, matchRegion.getHeight() + 2);
            }

            col++;
        }

        g2d.dispose();
        return grid;
    }

    /** Create a heatmap showing similarity scores across the search region. */
    public BufferedImage createHeatmap(
            BufferedImage screenshot, Pattern pattern, Region searchRegion) {

        if (!config.getVisual().isCreateHeatmap()) {
            return null;
        }

        // This would require access to the raw similarity scores from the find operation
        // For now, returning null as this would need deeper integration with Find
        log.debug("Heatmap generation not yet implemented");
        return null;
    }

    /** Save visual debug output to disk. */
    public void saveVisualDebug(
            BufferedImage image, String type, String sessionId, int operationId) {

        if (image == null || !config.shouldSaveFiles()) {
            return;
        }

        try {
            Path outputDir = Paths.get(config.getOutputDir(), sessionId, "visual");
            outputDir.toFile().mkdirs();

            String filename =
                    String.format(
                            "%s_%03d_%s.png",
                            type, operationId, LocalDateTime.now().format(TIMESTAMP_FORMAT));

            File outputFile = outputDir.resolve(filename).toFile();
            ImageIO.write(image, "png", outputFile);

            log.debug("Saved visual debug: {}", outputFile.getAbsolutePath());
        } catch (IOException e) {
            log.error("Failed to save visual debug output", e);
        }
    }

    private void setupGraphics(Graphics2D g2d) {
        g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        g2d.setRenderingHint(
                RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_ON);
        g2d.setRenderingHint(RenderingHints.KEY_RENDERING, RenderingHints.VALUE_RENDER_QUALITY);
    }

    private void drawSearchRegion(Graphics2D g2d, Region region) {
        g2d.setColor(COLOR_SEARCH_REGION);
        g2d.setStroke(
                new BasicStroke(
                        2,
                        BasicStroke.CAP_ROUND,
                        BasicStroke.JOIN_ROUND,
                        0,
                        new float[] {10, 5},
                        0));
        g2d.drawRect(region.getX(), region.getY(), region.w(), region.h());

        // Draw label
        g2d.setFont(new Font("Arial", Font.BOLD, 12));
        String label = "Search Region";
        FontMetrics fm = g2d.getFontMetrics();
        int labelWidth = fm.stringWidth(label);
        int labelHeight = fm.getHeight();

        g2d.setColor(TEXT_BACKGROUND);
        g2d.fillRect(
                region.getX(), region.getY() - labelHeight - 2, labelWidth + 4, labelHeight + 2);

        g2d.setColor(TEXT_COLOR);
        g2d.drawString(label, region.getX() + 2, region.getY() - 4);
    }

    private void drawMatches(Graphics2D g2d, List<Match> matches, ObjectCollection collection) {
        int matchIndex = 0;
        Match bestMatch = null;
        double bestScore = 0;

        // Find best match
        for (Match match : matches) {
            if (match.getScore() > bestScore) {
                bestScore = match.getScore();
                bestMatch = match;
            }
        }

        // Draw all matches
        for (Match match : matches) {
            matchIndex++;
            Region target = match.getRegion();

            // Choose color based on match type
            Color matchColor;
            int strokeWidth;
            if (match == bestMatch && config.getVisual().isHighlightBestMatch()) {
                matchColor = COLOR_BEST_MATCH;
                strokeWidth = 3;
            } else {
                matchColor = COLOR_MATCH_SUCCESS;
                strokeWidth = 2;
            }

            // Draw rectangle
            g2d.setColor(matchColor);
            g2d.setStroke(new BasicStroke(strokeWidth));
            g2d.drawRect(target.getX(), target.getY(), target.w(), target.h());

            // Draw score if configured
            if (config.getVisual().isShowMatchScores()) {
                String scoreText = String.format("#%d: %.1f%%", matchIndex, match.getScore() * 100);
                drawTextWithBackground(
                        g2d, scoreText, target.getX() + 2, target.getY() + target.h() - 4);
            }
        }
    }

    private void drawFailedRegions(Graphics2D g2d, Region searchRegion) {
        if (searchRegion == null) return;

        g2d.setColor(COLOR_MATCH_FAILED);
        g2d.setStroke(new BasicStroke(2));

        // Draw X across the search region
        g2d.drawLine(
                searchRegion.getX(),
                searchRegion.getY(),
                searchRegion.getX() + searchRegion.w(),
                searchRegion.getY() + searchRegion.h());
        g2d.drawLine(
                searchRegion.getX() + searchRegion.w(),
                searchRegion.getY(),
                searchRegion.getX(),
                searchRegion.getY() + searchRegion.h());

        // Draw "NOT FOUND" label
        String label = "NOT FOUND";
        drawTextWithBackground(
                g2d,
                label,
                searchRegion.getX() + searchRegion.w() / 2 - 40,
                searchRegion.getY() + searchRegion.h() / 2);
    }

    private void drawLegend(Graphics2D g2d, int width, int height) {
        int legendX = width - 200;
        int legendY = height - 100;

        // Background
        g2d.setColor(new Color(255, 255, 255, 200));
        g2d.fillRect(legendX, legendY, 180, 80);
        g2d.setColor(Color.BLACK);
        g2d.drawRect(legendX, legendY, 180, 80);

        // Title
        g2d.setFont(new Font("Arial", Font.BOLD, 12));
        g2d.drawString("Legend", legendX + 10, legendY + 15);

        // Items
        g2d.setFont(new Font("Arial", Font.PLAIN, 11));
        int y = legendY + 30;

        // Search region
        g2d.setColor(COLOR_SEARCH_REGION);
        g2d.fillRect(legendX + 10, y - 8, 10, 10);
        g2d.setColor(Color.BLACK);
        g2d.drawString("Search Region", legendX + 25, y);

        // Match
        y += 15;
        g2d.setColor(COLOR_MATCH_SUCCESS);
        g2d.fillRect(legendX + 10, y - 8, 10, 10);
        g2d.setColor(Color.BLACK);
        g2d.drawString("Match Found", legendX + 25, y);

        // Best match
        if (config.getVisual().isHighlightBestMatch()) {
            y += 15;
            g2d.setColor(COLOR_BEST_MATCH);
            g2d.fillRect(legendX + 10, y - 8, 10, 10);
            g2d.setColor(Color.BLACK);
            g2d.drawString("Best Match", legendX + 25, y);
        }

        // Failed
        if (config.getVisual().isShowFailedRegions()) {
            y += 15;
            g2d.setColor(COLOR_MATCH_FAILED);
            g2d.fillRect(legendX + 10, y - 8, 10, 10);
            g2d.setColor(Color.BLACK);
            g2d.drawString("Not Found", legendX + 25, y);
        }
    }

    private void drawTextWithBackground(Graphics2D g2d, String text, int x, int y) {
        g2d.setFont(new Font("Arial", Font.PLAIN, 11));
        FontMetrics fm = g2d.getFontMetrics();
        int textWidth = fm.stringWidth(text);
        int textHeight = fm.getHeight();

        // Draw background
        g2d.setColor(TEXT_BACKGROUND);
        g2d.fillRect(x - 2, y - textHeight + 2, textWidth + 4, textHeight);

        // Draw text
        g2d.setColor(TEXT_COLOR);
        g2d.drawString(text, x, y);
    }

    private BufferedImage extractMatchRegion(BufferedImage screenshot, Match match) {
        if (screenshot == null || match == null) return null;

        Region target = match.getRegion();
        int x = Math.max(0, target.getX());
        int y = Math.max(0, target.getY());
        int width = Math.min(target.w(), screenshot.getWidth() - x);
        int height = Math.min(target.h(), screenshot.getHeight() - y);

        if (width <= 0 || height <= 0) return null;

        return screenshot.getSubimage(x, y, width, height);
    }

    private BufferedImage scaleImage(BufferedImage original, int maxWidth, int maxHeight) {
        double scaleX = (double) maxWidth / original.getWidth();
        double scaleY = (double) maxHeight / original.getHeight();
        double scale = Math.min(scaleX, scaleY);

        int newWidth = (int) (original.getWidth() * scale);
        int newHeight = (int) (original.getHeight() * scale);

        BufferedImage scaled = new BufferedImage(newWidth, newHeight, BufferedImage.TYPE_INT_RGB);
        Graphics2D g2d = scaled.createGraphics();
        setupGraphics(g2d);
        g2d.drawImage(original, 0, 0, newWidth, newHeight, null);
        g2d.dispose();

        return scaled;
    }
}
