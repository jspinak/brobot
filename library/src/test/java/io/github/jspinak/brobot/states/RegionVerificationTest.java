package io.github.jspinak.brobot.states;

import static org.junit.jupiter.api.Assertions.*;

import org.junit.jupiter.api.Test;

import io.github.jspinak.brobot.model.element.Region;
import io.github.jspinak.brobot.test.BrobotTestBase;

/** Test to verify RegionBuilder calculations for screen quarters. */
public class RegionVerificationTest extends BrobotTestBase {

    @Test
    public void testLowerLeftQuarter() {
        // Create lower left quarter using RegionBuilder
        Region lowerLeftQuarter =
                Region.builder()
                        .withScreenPercentage(
                                0.0, 0.5, 0.5, 0.5) // x=0%, y=50%, width=50%, height=50%
                        .build();

        // Assuming a 1920x1080 screen (based on console output)
        // Expected values:
        // X: 0 (0% of 1920 = 0)
        // Y: 540 (50% of 1080 = 540)
        // Width: 960 (50% of 1920 = 960)
        // Height: 540 (50% of 1080 = 540)

        System.out.println("Lower Left Quarter Region:");
        System.out.println("  X: " + lowerLeftQuarter.getX() + " (expected: 0)");
        System.out.println("  Y: " + lowerLeftQuarter.getY() + " (expected: 540 for 1080p screen)");
        System.out.println(
                "  Width: " + lowerLeftQuarter.getW() + " (expected: 960 for 1920 width)");
        System.out.println(
                "  Height: " + lowerLeftQuarter.getH() + " (expected: 540 for 1080 height)");
        System.out.println("  String representation: " + lowerLeftQuarter.toString());

        // The region should be in the lower left quarter
        assertTrue(lowerLeftQuarter.getX() == 0, "X should be at left edge");
        assertTrue(lowerLeftQuarter.getY() > 0, "Y should be in lower half");
        assertTrue(lowerLeftQuarter.getW() > 0, "Width should be positive");
        assertTrue(lowerLeftQuarter.getH() > 0, "Height should be positive");
    }

    @Test
    public void testAllQuarters() {
        System.out.println("\n=== Screen Quarter Regions ===");

        // Upper left quarter
        Region upperLeft = Region.builder().withScreenPercentage(0.0, 0.0, 0.5, 0.5).build();
        System.out.println("Upper Left:  " + upperLeft.toString());

        // Upper right quarter
        Region upperRight = Region.builder().withScreenPercentage(0.5, 0.0, 0.5, 0.5).build();
        System.out.println("Upper Right: " + upperRight.toString());

        // Lower left quarter
        Region lowerLeft = Region.builder().withScreenPercentage(0.0, 0.5, 0.5, 0.5).build();
        System.out.println("Lower Left:  " + lowerLeft.toString());

        // Lower right quarter
        Region lowerRight = Region.builder().withScreenPercentage(0.5, 0.5, 0.5, 0.5).build();
        System.out.println("Lower Right: " + lowerRight.toString());

        // Full screen for reference
        Region fullScreen = Region.builder().fullScreen().build();
        System.out.println("Full Screen: " + fullScreen.toString());

        System.out.println("\n=== Visual Layout ===");
        System.out.println("+----------------+----------------+");
        System.out.println("|  Upper Left    |  Upper Right   |");
        System.out.println("|  (0,0)         |  (" + upperRight.getX() + ",0)       |");
        System.out.println(
                "|  "
                        + upperLeft.getW()
                        + "x"
                        + upperLeft.getH()
                        + "       |  "
                        + upperRight.getW()
                        + "x"
                        + upperRight.getH()
                        + "       |");
        System.out.println("+----------------+----------------+");
        System.out.println("|  Lower Left    |  Lower Right   |");
        System.out.println(
                "|  (0,"
                        + lowerLeft.getY()
                        + ")       |  ("
                        + lowerRight.getX()
                        + ","
                        + lowerRight.getY()
                        + ")     |");
        System.out.println(
                "|  "
                        + lowerLeft.getW()
                        + "x"
                        + lowerLeft.getH()
                        + "       |  "
                        + lowerRight.getW()
                        + "x"
                        + lowerRight.getH()
                        + "       |");
        System.out.println("+----------------+----------------+");
    }
}
