package io.github.jspinak.brobot.debug;

import io.github.jspinak.brobot.debug.DebugTestBase;

import org.junit.jupiter.api.Test;
import org.sikuli.script.*;
import org.sikuli.basics.Settings;
import javax.imageio.ImageIO;
import java.awt.*;
import java.awt.geom.AffineTransform;
import java.awt.image.BufferedImage;
import java.io.File;

/**
 * Analyze if the scaling/DPI is causing the pattern mismatch
 */
public class ScalingAnalysisTest extends DebugTestBase {
    
    @Test
    public void analyzeScaling() {
        System.out.println("=== SCALING ANALYSIS TEST ===\n");
        
        try {
            // Give user time to set up screen
            System.out.println("!!! SWITCH TO YOUR TARGET APPLICATION NOW !!!");
            System.out.println("You have 3 seconds...");
            Thread.sleep(3000);
            
            // Get system DPI information
            System.out.println("SYSTEM INFORMATION:");
            System.out.println("===================");
            
            GraphicsEnvironment ge = GraphicsEnvironment.getLocalGraphicsEnvironment();
            GraphicsDevice[] devices = ge.getScreenDevices();
            
            for (GraphicsDevice device : devices) {
                GraphicsConfiguration gc = device.getDefaultConfiguration();
                AffineTransform transform = gc.getDefaultTransform();
                double scaleX = transform.getScaleX();
                double scaleY = transform.getScaleY();
                
                System.out.println("Display: " + device.getIDstring());
                System.out.println("  Scale X: " + scaleX);
                System.out.println("  Scale Y: " + scaleY);
                System.out.println("  DPI Scale: " + (scaleX * 100) + "%");
                
                Rectangle bounds = gc.getBounds();
                System.out.println("  Bounds: " + bounds);
            }
            
            // Get toolkit screen info
            Toolkit toolkit = Toolkit.getDefaultToolkit();
            Dimension screenSize = toolkit.getScreenSize();
            int dpi = toolkit.getScreenResolution();
            System.out.println("\nToolkit Info:");
            System.out.println("  Screen size: " + screenSize.width + "x" + screenSize.height);
            System.out.println("  DPI: " + dpi);
            
            // SikuliX screen info
            Screen screen = new Screen();
            System.out.println("\nSikuliX Screen:");
            System.out.println("  Bounds: " + screen.getBounds());
            System.out.println("  Size: " + screen.w + "x" + screen.h);
            
            // Create output directory
            File outputDir = new File("scaling-analysis");
            outputDir.mkdirs();
            
            // Test pattern
            String patternPath = "images/prompt/claude-prompt-1.png";
            File patternFile = new File(patternPath);
            if (!patternFile.exists()) {
                System.out.println("Pattern file not found!");
                return;
            }
            
            BufferedImage originalPattern = ImageIO.read(patternFile);
            System.out.println("\nORIGINAL PATTERN:");
            System.out.println("  Size: " + originalPattern.getWidth() + "x" + originalPattern.getHeight());
            System.out.println("  File size: " + patternFile.length() + " bytes");
            
            // Save original
            File originalFile = new File(outputDir, "1_original_pattern.png");
            ImageIO.write(originalPattern, "png", originalFile);
            
            // Capture screen
            ScreenImage screenCapture = screen.capture();
            BufferedImage screenImage = screenCapture.getImage();
            System.out.println("\nSCREEN CAPTURE:");
            System.out.println("  Size: " + screenImage.getWidth() + "x" + screenImage.getHeight());
            
            // Find pattern at different thresholds
            System.out.println("\nFINDING PATTERN:");
            Finder finder = new Finder(screenImage);
            Pattern pattern = new Pattern(originalPattern).similar(0.5);
            finder.findAll(pattern);
            
            if (finder.hasNext()) {
                Match match = finder.next();
                System.out.println("  Found at: (" + match.x + ", " + match.y + ")");
                System.out.println("  Score: " + String.format("%.3f", match.getScore()));
                System.out.println("  Match size: " + match.w + "x" + match.h);
                
                // Extract matched region
                BufferedImage matchedRegion = screenImage.getSubimage(
                    match.x, match.y, match.w, match.h);
                
                File matchedFile = new File(outputDir, "2_matched_region.png");
                ImageIO.write(matchedRegion, "png", matchedFile);
                System.out.println("  Matched region saved");
                
                // Scale pattern to different sizes and test
                System.out.println("\nTESTING SCALED PATTERNS:");
                testScaledPatterns(screenImage, originalPattern, outputDir);
                
                // Test if the matched region would match better if scaled
                System.out.println("\nTESTING UPSCALED MATCH:");
                testUpscaledMatch(originalPattern, matchedRegion, outputDir);
                
            } else {
                System.out.println("  Pattern not found!");
            }
            
            // Test with different Settings.AlwaysResize values
            System.out.println("\nTESTING Settings.AlwaysResize:");
            float[] resizeValues = {0, 0.8f, 1.0f, 1.25f, 1.5f};
            
            for (float resize : resizeValues) {
                Settings.AlwaysResize = resize;
                System.out.println("\n  AlwaysResize = " + resize);
                
                Pattern p = new Pattern(originalPattern).similar(0.5);
                Match m = screen.exists(p, 0);
                
                if (m != null) {
                    System.out.println("    FOUND! Score: " + String.format("%.3f", m.getScore()));
                    System.out.println("    Location: " + m.getTarget());
                } else {
                    System.out.println("    Not found");
                }
            }
            
            System.out.println("\n=== ANALYSIS COMPLETE ===");
            System.out.println("Check the 'scaling-analysis' folder for saved images");
            
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
    
    private void testScaledPatterns(BufferedImage screenImage, BufferedImage originalPattern, File outputDir) {
        try {
            // Test different scales
            double[] scales = {0.8, 0.9, 1.0, 1.1, 1.2, 1.25};
            
            for (double scale : scales) {
                int newWidth = (int)(originalPattern.getWidth() * scale);
                int newHeight = (int)(originalPattern.getHeight() * scale);
                
                // Scale the pattern
                BufferedImage scaledPattern = new BufferedImage(newWidth, newHeight, BufferedImage.TYPE_INT_RGB);
                Graphics2D g2d = scaledPattern.createGraphics();
                g2d.setRenderingHint(RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_BILINEAR);
                g2d.drawImage(originalPattern, 0, 0, newWidth, newHeight, null);
                g2d.dispose();
                
                // Test matching
                Finder finder = new Finder(screenImage);
                Pattern pattern = new Pattern(scaledPattern).similar(0.5);
                finder.findAll(pattern);
                
                if (finder.hasNext()) {
                    Match match = finder.next();
                    System.out.println("  Scale " + scale + "x: FOUND with score " + 
                        String.format("%.3f", match.getScore()));
                    
                    // Save scaled pattern if it matches better
                    if (match.getScore() > 0.8) {
                        File scaledFile = new File(outputDir, "scaled_" + scale + "x_pattern.png");
                        ImageIO.write(scaledPattern, "png", scaledFile);
                    }
                } else {
                    System.out.println("  Scale " + scale + "x: Not found");
                }
                
                finder.destroy();
            }
        } catch (Exception e) {
            System.out.println("  Error testing scaled patterns: " + e.getMessage());
        }
    }
    
    private void testUpscaledMatch(BufferedImage originalPattern, BufferedImage matchedRegion, File outputDir) {
        try {
            // The matched region might be a downscaled version
            // Try upscaling it to original size and compare
            
            int origWidth = originalPattern.getWidth();
            int origHeight = originalPattern.getHeight();
            
            // Upscale matched region to original size
            BufferedImage upscaledMatch = new BufferedImage(origWidth, origHeight, BufferedImage.TYPE_INT_RGB);
            Graphics2D g2d = upscaledMatch.createGraphics();
            g2d.setRenderingHint(RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_BILINEAR);
            g2d.drawImage(matchedRegion, 0, 0, origWidth, origHeight, null);
            g2d.dispose();
            
            File upscaledFile = new File(outputDir, "3_upscaled_match.png");
            ImageIO.write(upscaledMatch, "png", upscaledFile);
            
            // Compare pixels
            int totalPixels = origWidth * origHeight;
            int differentPixels = 0;
            long totalDiff = 0;
            
            for (int y = 0; y < origHeight; y++) {
                for (int x = 0; x < origWidth; x++) {
                    int rgb1 = originalPattern.getRGB(x, y);
                    int rgb2 = upscaledMatch.getRGB(x, y);
                    
                    if (rgb1 != rgb2) {
                        differentPixels++;
                        
                        int r1 = (rgb1 >> 16) & 0xFF;
                        int g1 = (rgb1 >> 8) & 0xFF;
                        int b1 = rgb1 & 0xFF;
                        
                        int r2 = (rgb2 >> 16) & 0xFF;
                        int g2 = (rgb2 >> 8) & 0xFF;
                        int b2 = rgb2 & 0xFF;
                        
                        int diff = Math.abs(r1 - r2) + Math.abs(g1 - g2) + Math.abs(b1 - b2);
                        totalDiff += diff;
                    }
                }
            }
            
            System.out.println("  Upscaled match comparison:");
            System.out.println("    Different pixels: " + differentPixels + "/" + totalPixels + 
                " (" + String.format("%.1f%%", (differentPixels * 100.0 / totalPixels)) + ")");
            if (differentPixels > 0) {
                System.out.println("    Average difference: " + (totalDiff / differentPixels) + "/765");
            }
            
            // Create a side-by-side comparison
            BufferedImage comparison = new BufferedImage(origWidth * 3, origHeight, BufferedImage.TYPE_INT_RGB);
            Graphics2D g = comparison.createGraphics();
            g.drawImage(originalPattern, 0, 0, null);
            g.drawImage(matchedRegion, origWidth, 0, origWidth, origHeight, null);
            g.drawImage(upscaledMatch, origWidth * 2, 0, null);
            g.setColor(Color.WHITE);
            g.drawString("Original", 5, 15);
            g.drawString("Matched", origWidth + 5, 15);
            g.drawString("Upscaled", origWidth * 2 + 5, 15);
            g.dispose();
            
            File comparisonFile = new File(outputDir, "4_comparison.png");
            ImageIO.write(comparison, "png", comparisonFile);
            System.out.println("  Comparison image saved");
            
        } catch (Exception e) {
            System.out.println("  Error in upscaling test: " + e.getMessage());
        }
    }
}