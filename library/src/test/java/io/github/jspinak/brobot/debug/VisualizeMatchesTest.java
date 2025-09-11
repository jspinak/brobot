package io.github.jspinak.brobot.debug;

import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.File;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

import javax.imageio.ImageIO;

import org.junit.jupiter.api.Test;
import org.sikuli.script.*;

import io.github.jspinak.brobot.test.BrobotTestBase;
import io.github.jspinak.brobot.test.DisabledInCI;

/** Test to visually show what's being matched by saving annotated screenshots */
@DisabledInCI
public class VisualizeMatchesTest extends BrobotTestBase {

    @Test
    public void visualizeWhatIsBeingMatched() {
        System.out.println("=== VISUALIZE MATCHES TEST ===\n");

        try {
            // Give user time to switch to the target application
            System.out.println("!!! SWITCH TO YOUR TARGET APPLICATION NOW !!!");
            System.out.println("You have 5 seconds to make the target screen visible...");
            for (int i = 5; i > 0; i--) {
                System.out.println(i + "...");
                Thread.sleep(1000);
            }
            System.out.println("Capturing screen now!\n");

            // Create output directory
            File outputDir = new File("match-visualization");
            if (!outputDir.exists()) {
                outputDir.mkdirs();
            }

            // Test patterns
            String[] patterns = {
                "images/prompt/claude-prompt-1.png", "images/working/claude-icon-1.png"
            };

            for (String patternPath : patterns) {
                System.out.println("\n--- Analyzing: " + patternPath + " ---");

                // Load pattern
                File patternFile = new File(patternPath);
                if (!patternFile.exists()) {
                    System.out.println("Pattern file not found: " + patternPath);
                    continue;
                }

                BufferedImage patternImg = ImageIO.read(patternFile);
                System.out.println(
                        "Pattern size: " + patternImg.getWidth() + "x" + patternImg.getHeight());

                // Save the pattern for reference
                String patternName = patternFile.getName().replace(".png", "");
                File patternOutput = new File(outputDir, "pattern_" + patternName + ".png");
                ImageIO.write(patternImg, "png", patternOutput);
                System.out.println("Saved pattern to: " + patternOutput.getPath());

                // Capture screen
                Screen screen = new Screen();
                ScreenImage screenCapture = screen.capture();
                BufferedImage screenImg = screenCapture.getImage();
                System.out.println(
                        "Screen size: " + screenImg.getWidth() + "x" + screenImg.getHeight());

                // Create pattern
                Pattern pattern = new Pattern(patternImg);

                // Test with different thresholds and visualize matches
                double[] thresholds = {0.99, 0.90, 0.70, 0.50};

                for (double threshold : thresholds) {
                    System.out.println("\nTesting threshold: " + threshold);

                    // Copy screen image for annotation
                    BufferedImage annotated = copyImage(screenImg);
                    Graphics2D g = annotated.createGraphics();
                    g.setStroke(new BasicStroke(2));

                    // Find matches
                    pattern = pattern.similar(threshold);
                    Finder finder = new Finder(screenImg);
                    finder.findAll(pattern);

                    int matchCount = 0;
                    double bestScore = 0;
                    Match bestMatch = null;

                    while (finder.hasNext()) {
                        Match match = finder.next();
                        matchCount++;

                        if (match.getScore() > bestScore) {
                            bestScore = match.getScore();
                            bestMatch = match;
                        }

                        // Color based on score
                        Color color;
                        if (match.getScore() >= 0.95) {
                            color = Color.GREEN; // Excellent match
                        } else if (match.getScore() >= 0.80) {
                            color = Color.YELLOW; // Good match
                        } else if (match.getScore() >= 0.60) {
                            color = Color.ORANGE; // Moderate match
                        } else {
                            color = Color.RED; // Poor match
                        }

                        g.setColor(color);

                        // Draw rectangle around match
                        g.drawRect(match.x, match.y, match.w, match.h);

                        // Label with score (only for first 10 matches)
                        if (matchCount <= 10) {
                            g.setColor(Color.BLACK);
                            g.fillRect(match.x, match.y - 20, 50, 18);
                            g.setColor(color);
                            g.drawString(
                                    String.format("%.2f", match.getScore()),
                                    match.x + 2,
                                    match.y - 5);
                        }

                        // Print details for first 5 matches
                        if (matchCount <= 5) {
                            System.out.println(
                                    "  Match #"
                                            + matchCount
                                            + ": score="
                                            + String.format("%.3f", match.getScore())
                                            + " at ("
                                            + match.x
                                            + ", "
                                            + match.y
                                            + ")");

                            // Extract and save what was matched
                            if (match.x >= 0
                                    && match.y >= 0
                                    && match.x + match.w <= screenImg.getWidth()
                                    && match.y + match.h <= screenImg.getHeight()) {

                                BufferedImage matchedRegion =
                                        screenImg.getSubimage(match.x, match.y, match.w, match.h);

                                String matchFilename =
                                        String.format(
                                                "matched_%s_t%.2f_m%d_s%.3f.png",
                                                patternName,
                                                threshold,
                                                matchCount,
                                                match.getScore());
                                File matchFile = new File(outputDir, matchFilename);
                                ImageIO.write(matchedRegion, "png", matchFile);
                                System.out.println(
                                        "    Saved matched region to: " + matchFile.getName());
                            }
                        }
                    }

                    finder.destroy();

                    // Draw legend
                    g.setColor(Color.BLACK);
                    g.fillRect(10, 10, 250, 100);
                    g.setColor(Color.WHITE);
                    g.drawString("Threshold: " + threshold, 20, 30);
                    g.drawString("Matches: " + matchCount, 20, 50);
                    if (bestScore > 0) {
                        g.drawString("Best score: " + String.format("%.3f", bestScore), 20, 70);
                    }
                    g.setColor(Color.GREEN);
                    g.drawString("Green = >0.95", 20, 90);
                    g.setColor(Color.YELLOW);
                    g.drawString("Yellow = >0.80", 120, 90);

                    g.dispose();

                    // Save annotated screenshot
                    String timestamp =
                            LocalDateTime.now().format(DateTimeFormatter.ofPattern("HHmmss"));
                    String filename =
                            String.format(
                                    "screen_%s_t%.2f_%s.png", patternName, threshold, timestamp);
                    File outputFile = new File(outputDir, filename);
                    ImageIO.write(annotated, "png", outputFile);

                    System.out.println("Total matches: " + matchCount);
                    if (bestScore > 0) {
                        System.out.println("Best score: " + String.format("%.3f", bestScore));
                    }
                    System.out.println("Saved visualization to: " + outputFile.getName());

                    // If we found a good match at high threshold, extract it for comparison
                    if (threshold >= 0.90 && bestMatch != null && bestScore >= 0.90) {
                        System.out.println("\n=== COMPARING BEST MATCH WITH PATTERN ===");
                        compareImages(
                                patternImg,
                                screenImg.getSubimage(
                                        bestMatch.x, bestMatch.y, bestMatch.w, bestMatch.h),
                                outputDir,
                                patternName);
                    }
                }
            }

            System.out.println(
                    "\n=== All visualizations saved to: " + outputDir.getAbsolutePath() + " ===");

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void compareImages(
            BufferedImage pattern, BufferedImage matched, File outputDir, String name) {
        try {
            System.out.println(
                    "Pattern: "
                            + pattern.getWidth()
                            + "x"
                            + pattern.getHeight()
                            + " type="
                            + pattern.getType());
            System.out.println(
                    "Matched: "
                            + matched.getWidth()
                            + "x"
                            + matched.getHeight()
                            + " type="
                            + matched.getType());

            // Create side-by-side comparison
            int width = pattern.getWidth() + matched.getWidth() + 20;
            int height = Math.max(pattern.getHeight(), matched.getHeight()) + 40;

            BufferedImage comparison = new BufferedImage(width, height, BufferedImage.TYPE_INT_RGB);
            Graphics2D g = comparison.createGraphics();
            g.setColor(Color.WHITE);
            g.fillRect(0, 0, width, height);

            // Draw pattern
            g.setColor(Color.BLACK);
            g.drawString("PATTERN", 10, 20);
            g.drawImage(pattern, 10, 30, null);
            g.drawRect(10, 30, pattern.getWidth(), pattern.getHeight());

            // Draw matched
            g.drawString("MATCHED", pattern.getWidth() + 20, 20);
            g.drawImage(matched, pattern.getWidth() + 20, 30, null);
            g.drawRect(pattern.getWidth() + 20, 30, matched.getWidth(), matched.getHeight());

            g.dispose();

            File comparisonFile = new File(outputDir, "comparison_" + name + ".png");
            ImageIO.write(comparison, "png", comparisonFile);
            System.out.println("Saved comparison to: " + comparisonFile.getName());

            // Sample some pixels for comparison
            System.out.println("\nPixel comparison (5 sample points):");
            int[] sampleX = {
                0,
                pattern.getWidth() / 2,
                pattern.getWidth() - 1,
                pattern.getWidth() / 4,
                3 * pattern.getWidth() / 4
            };
            int[] sampleY = {
                0,
                pattern.getHeight() / 2,
                pattern.getHeight() - 1,
                pattern.getHeight() / 4,
                3 * pattern.getHeight() / 4
            };

            for (int i = 0; i < 5; i++) {
                int x = Math.min(sampleX[i], pattern.getWidth() - 1);
                int y = Math.min(sampleY[i], pattern.getHeight() - 1);

                int patternPixel = pattern.getRGB(x, y);
                int matchedPixel = matched.getRGB(x, y);

                System.out.printf(
                        "  (%d,%d): Pattern=%08X, Matched=%08X, %s\n",
                        x,
                        y,
                        patternPixel,
                        matchedPixel,
                        patternPixel == matchedPixel ? "SAME" : "DIFFERENT");
            }

        } catch (Exception e) {
            System.out.println("Error comparing images: " + e.getMessage());
        }
    }

    private BufferedImage copyImage(BufferedImage source) {
        BufferedImage copy =
                new BufferedImage(source.getWidth(), source.getHeight(), source.getType());
        Graphics2D g = copy.createGraphics();
        g.drawImage(source, 0, 0, null);
        g.dispose();
        return copy;
    }
}
