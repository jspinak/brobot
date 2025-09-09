package io.github.jspinak.brobot.debug;

import io.github.jspinak.brobot.debug.DebugTestBase;

import io.github.jspinak.brobot.test.BrobotTestBase;
import io.github.jspinak.brobot.config.core.FrameworkSettings;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.Assumptions;
import org.sikuli.script.*;
import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.File;
import java.awt.Rectangle;
import java.awt.Robot;
import java.util.ArrayList;
import java.util.List;
import io.github.jspinak.brobot.test.DisabledInCI;

/**
 * Test to determine if there's a consistent offset pattern in SikuliX operations.
 * If the offset is consistent, we can create a compensation mechanism.
 */
@DisabledInCI
public class OffsetPatternAnalysisTest extends DebugTestBase {
    
    static class OffsetData {
        int expectedX, expectedY;
        int actualX, actualY;
        int offsetX, offsetY;
        String operation;
        
        OffsetData(String op, int expX, int expY, int actX, int actY) {
            this.operation = op;
            this.expectedX = expX;
            this.expectedY = expY;
            this.actualX = actX;
            this.actualY = actY;
            this.offsetX = actX - expX;
            this.offsetY = actY - expY;
        }
        
        @Override
        public String toString() {
            return String.format("%s: Expected(%d,%d) -> Actual(%d,%d) = Offset(%d,%d)",
                    operation, expectedX, expectedY, actualX, actualY, offsetX, offsetY);
        }
    }
    
    @Test
    public void analyzeOffsetPattern() throws Exception {
        System.out.println("\n================================================================================");
        System.out.println("OFFSET PATTERN ANALYSIS");
        System.out.println("Determining if coordinate offsets are consistent");
        System.out.println("================================================================================\n");
        
        Screen screen = new Screen();
        Robot robot = new Robot();
        List<OffsetData> offsets = new ArrayList<>();
        
        // Test 1: Capture and search at multiple locations
        System.out.println("--- Test 1: Multiple Location Pattern Search ---");
        
        int[][] testLocations = {
            {0, 0},      // Top-left corner
            {100, 0},    // Top edge
            {0, 100},    // Left edge
            {100, 100},  // Near top-left
            {500, 500},  // Center area
            {900, 500},  // Right side
            {500, 900},  // Bottom area
            {0, 500},    // Left middle
            {1800, 0},   // Top-right area
            {0, 900},    // Bottom-left area
        };
        
        for (int[] loc : testLocations) {
            int x = loc[0];
            int y = loc[1];
            
            // Skip if out of bounds
            if (x + 50 > screen.w || y + 50 > screen.h) continue;
            
            // Capture a small region
            Rectangle rect = new Rectangle(x, y, 50, 50);
            BufferedImage img = robot.createScreenCapture(rect);
            File file = new File(String.format("offset_test_%d_%d.png", x, y));
            ImageIO.write(img, "png", file);
            
            // Search for it
            Pattern p = new Pattern(file.getAbsolutePath()).similar(0.99);
            
            try {
                Match m = screen.find(p);
                
                OffsetData data = new OffsetData("FIND", x, y, m.x, m.y);
                offsets.add(data);
                System.out.println(data);
                
                file.delete();
            } catch (FindFailed e) {
                System.out.printf("Pattern at (%d,%d) not found%n", x, y);
                file.delete();
            }
        }
        
        // Test 2: Region-based searches
        System.out.println("\n--- Test 2: Region-Based Search Offsets ---");
        
        // Create regions at different locations
        Region[] testRegions = {
            new Region(0, 0, 200, 200),
            new Region(200, 200, 200, 200),
            new Region(400, 400, 200, 200),
            new Region(600, 600, 200, 200),
            new Region(800, 800, 200, 200)
        };
        
        for (Region region : testRegions) {
            if (region.x + region.w > screen.w || region.y + region.h > screen.h) continue;
            
            // Capture from the region
            Rectangle rect = new Rectangle(region.x, region.y, 50, 50);
            BufferedImage img = robot.createScreenCapture(rect);
            File file = new File(String.format("region_test_%d_%d.png", region.x, region.y));
            ImageIO.write(img, "png", file);
            
            Pattern p = new Pattern(file.getAbsolutePath()).similar(0.99);
            
            try {
                // Search within the specific region
                Match m = region.find(p);
                
                // The match coordinates should be relative to screen, not region
                OffsetData data = new OffsetData("REGION_FIND", region.x, region.y, m.x, m.y);
                offsets.add(data);
                System.out.println(data);
                
                file.delete();
            } catch (FindFailed e) {
                System.out.printf("Pattern in region (%d,%d) not found%n", region.x, region.y);
                file.delete();
            }
        }
        
        // Test 3: Highlight operations
        System.out.println("\n--- Test 3: Highlight Operation Offsets ---");
        System.out.println("Creating regions and noting where highlights appear:");
        System.out.println("(You need to observe manually where highlights appear)");
        
        for (int i = 0; i < 5; i++) {
            int x = i * 200;
            int y = i * 100;
            
            Region r = new Region(x, y, 100, 100);
            System.out.printf("Highlighting region at (%d,%d): %s%n", x, y, r);
            r.highlight(1);
            
            // Note: We can't automatically detect where highlight appears,
            // but we log the intended location for manual comparison
            
            Thread.sleep(1200);
        }
        
        // Analyze the collected offsets
        System.out.println("\n--- OFFSET ANALYSIS ---");
        
        if (offsets.isEmpty()) {
            System.out.println("No successful operations to analyze!");
            return;
        }
        
        // Calculate statistics
        int minOffsetX = Integer.MAX_VALUE, maxOffsetX = Integer.MIN_VALUE;
        int minOffsetY = Integer.MAX_VALUE, maxOffsetY = Integer.MIN_VALUE;
        double avgOffsetX = 0, avgOffsetY = 0;
        
        for (OffsetData data : offsets) {
            minOffsetX = Math.min(minOffsetX, data.offsetX);
            maxOffsetX = Math.max(maxOffsetX, data.offsetX);
            minOffsetY = Math.min(minOffsetY, data.offsetY);
            maxOffsetY = Math.max(maxOffsetY, data.offsetY);
            avgOffsetX += data.offsetX;
            avgOffsetY += data.offsetY;
        }
        
        avgOffsetX /= offsets.size();
        avgOffsetY /= offsets.size();
        
        System.out.println("\nOffset Statistics:");
        System.out.printf("  X Offset Range: %d to %d (spread: %d)%n", 
                minOffsetX, maxOffsetX, maxOffsetX - minOffsetX);
        System.out.printf("  Y Offset Range: %d to %d (spread: %d)%n", 
                minOffsetY, maxOffsetY, maxOffsetY - minOffsetY);
        System.out.printf("  Average Offset: (%.1f, %.1f)%n", avgOffsetX, avgOffsetY);
        
        // Check for consistency
        boolean consistentX = (maxOffsetX - minOffsetX) < 10;
        boolean consistentY = (maxOffsetY - minOffsetY) < 10;
        
        System.out.println("\nConsistency Analysis:");
        if (consistentX && consistentY) {
            System.out.println("✓ OFFSETS ARE CONSISTENT!");
            System.out.printf("  You can compensate with offset: (%d, %d)%n", 
                    (int)avgOffsetX, (int)avgOffsetY);
            
            // Generate compensation code
            System.out.println("\n--- SUGGESTED COMPENSATION ---");
            System.out.println("Add this to your search operations:");
            System.out.printf("  int compensationX = %d;%n", -(int)avgOffsetX);
            System.out.printf("  int compensationY = %d;%n", -(int)avgOffsetY);
            System.out.println("  Region searchRegion = new Region(");
            System.out.println("      originalX + compensationX,");
            System.out.println("      originalY + compensationY,");
            System.out.println("      width, height);");
            
        } else if (consistentX || consistentY) {
            System.out.println("⚠ OFFSETS ARE PARTIALLY CONSISTENT");
            if (consistentX) {
                System.out.printf("  X offset is consistent: %d%n", (int)avgOffsetX);
            }
            if (consistentY) {
                System.out.printf("  Y offset is consistent: %d%n", (int)avgOffsetY);
            }
        } else {
            System.out.println("✗ OFFSETS ARE INCONSISTENT");
            System.out.println("  The offset varies depending on location.");
            System.out.println("  This suggests a complex coordinate transformation.");
            
            // Try to find a pattern
            System.out.println("\nLooking for patterns:");
            
            // Check if offset correlates with position
            boolean correlatesWithX = checkCorrelation(offsets, true);
            boolean correlatesWithY = checkCorrelation(offsets, false);
            
            if (correlatesWithX) {
                System.out.println("  Offset appears to correlate with X position");
            }
            if (correlatesWithY) {
                System.out.println("  Offset appears to correlate with Y position");
            }
            
            if (correlatesWithX || correlatesWithY) {
                System.out.println("  This suggests a scaling or transformation issue");
                
                // Try to determine scale factor
                double scaleX = detectScale(offsets, true);
                double scaleY = detectScale(offsets, false);
                
                if (scaleX != 1.0 || scaleY != 1.0) {
                    System.out.printf("  Possible scale factors: X=%.3f, Y=%.3f%n", 
                            scaleX, scaleY);
                }
            }
        }
        
        // Show all collected data
        System.out.println("\n--- ALL OFFSET DATA ---");
        for (OffsetData data : offsets) {
            System.out.println(data);
        }
        
        System.out.println("\n================================================================================");
        System.out.println("ANALYSIS COMPLETE");
        System.out.println("================================================================================\n");
    }
    
    private boolean checkCorrelation(List<OffsetData> offsets, boolean checkX) {
        if (offsets.size() < 3) return false;
        
        // Simple correlation check: does offset increase with position?
        int increasingCount = 0;
        int totalComparisons = 0;
        
        for (int i = 0; i < offsets.size() - 1; i++) {
            OffsetData d1 = offsets.get(i);
            OffsetData d2 = offsets.get(i + 1);
            
            if (checkX) {
                if (d2.expectedX > d1.expectedX) {
                    totalComparisons++;
                    if (d2.offsetX > d1.offsetX) {
                        increasingCount++;
                    }
                }
            } else {
                if (d2.expectedY > d1.expectedY) {
                    totalComparisons++;
                    if (d2.offsetY > d1.offsetY) {
                        increasingCount++;
                    }
                }
            }
        }
        
        return totalComparisons > 0 && 
               (double)increasingCount / totalComparisons > 0.7;
    }
    
    private double detectScale(List<OffsetData> offsets, boolean checkX) {
        // Try to detect if there's a scaling factor
        // offset = actual - expected
        // if actual = expected * scale, then offset = expected * (scale - 1)
        // so scale = 1 + (offset / expected)
        
        double sumScale = 0;
        int count = 0;
        
        for (OffsetData data : offsets) {
            if (checkX && data.expectedX > 0) {
                double scale = (double)data.actualX / data.expectedX;
                sumScale += scale;
                count++;
            } else if (!checkX && data.expectedY > 0) {
                double scale = (double)data.actualY / data.expectedY;
                sumScale += scale;
                count++;
            }
        }
        
        return count > 0 ? sumScale / count : 1.0;
    }
}