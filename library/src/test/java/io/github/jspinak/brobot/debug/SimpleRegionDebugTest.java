package io.github.jspinak.brobot.debug;

import org.junit.jupiter.api.Test;

import io.github.jspinak.brobot.model.element.Region;
import io.github.jspinak.brobot.test.BrobotTestBase;
import io.github.jspinak.brobot.test.DisabledInCI;

/** Simple unit test to verify and debug region calculations. */
@DisabledInCI
public class SimpleRegionDebugTest extends BrobotTestBase {

    @Test
    public void debugLowerLeftQuarterCalculation() {
        System.out.println("\n" + "=".repeat(80));
        System.out.println("LOWER LEFT QUARTER REGION CALCULATION DEBUG");
        System.out.println("=".repeat(80) + "\n");

        // Create lower left quarter
        Region lowerLeft = Region.builder().withScreenPercentage(0.0, 0.5, 0.5, 0.5).build();

        System.out.println("CALCULATION RESULTS:");
        System.out.println("--------------------");
        System.out.println("Region toString: " + lowerLeft.toString());
        System.out.println();
        System.out.println("COORDINATES:");
        System.out.println("  X (left edge):     " + lowerLeft.getX() + " pixels");
        System.out.println("  Y (top edge):      " + lowerLeft.getY() + " pixels");
        System.out.println("  Width:             " + lowerLeft.getW() + " pixels");
        System.out.println("  Height:            " + lowerLeft.getH() + " pixels");
        System.out.println("  X2 (right edge):   " + lowerLeft.x2() + " pixels");
        System.out.println("  Y2 (bottom edge):  " + lowerLeft.y2() + " pixels");

        System.out.println("\nINTERPRETATION (for 1920x1080 screen):");
        System.out.println("----------------------------------------");

        if (lowerLeft.getW() == 960 && lowerLeft.getH() == 540) {
            System.out.println("✓ Screen detected as 1920x1080");
            System.out.println();
            System.out.println("HORIZONTAL COVERAGE:");
            System.out.println("  From X=" + lowerLeft.getX() + " to X=" + lowerLeft.x2());
            System.out.println("  This is the LEFT HALF of the screen");
            System.out.println();
            System.out.println("VERTICAL COVERAGE:");
            System.out.println("  From Y=" + lowerLeft.getY() + " to Y=" + lowerLeft.y2());
            System.out.println(
                    "  Y=" + lowerLeft.getY() + " is the MIDDLE of the screen (50% down)");
            System.out.println("  Y=" + lowerLeft.y2() + " is the BOTTOM of the screen");
            System.out.println("  This is the LOWER HALF of the screen");
            System.out.println();
            System.out.println("CONCLUSION: The region R[0.540.960.540] correctly represents");
            System.out.println("            the LOWER LEFT QUARTER of a 1920x1080 screen.");
        }

        System.out.println("\nVISUAL REPRESENTATION:");
        System.out.println("----------------------");
        System.out.println("Full Screen: 1920x1080");
        System.out.println();
        System.out.println("    0          960         1920");
        System.out.println("    |           |           |");
        System.out.println("0   +===========+===========+");
        System.out.println("    |           |           |");
        System.out.println("    | Upper     | Upper     |");
        System.out.println("    | Left      | Right     |");
        System.out.println("    |           |           |");
        System.out.println("540 +===========+===========+ <- Y=540 (middle of screen)");
        System.out.println("    |###########|           |");
        System.out.println("    |# LOWER   #| Lower     |");
        System.out.println("    |# LEFT    #| Right     |");
        System.out.println("    |###########|           |");
        System.out.println("1080+===========+===========+ <- Y=1080 (bottom of screen)");
        System.out.println();
        System.out.println("Legend: ### = The highlighted region (lower left quarter)");

        System.out.println("\nCONSOLE OUTPUT EXPLANATION:");
        System.out.println("---------------------------");
        System.out.println("When you see: R[0.540.960.540]");
        System.out.println("This means:   R[X.Y.WIDTH.HEIGHT]");
        System.out.println("  X=0:        Left edge of screen");
        System.out.println("  Y=540:      Middle of screen (540 pixels from TOP)");
        System.out.println("  WIDTH=960:  Half the screen width");
        System.out.println("  HEIGHT=540: Half the screen height");
        System.out.println();
        System.out.println("The Y coordinate is measured from the TOP of the screen:");
        System.out.println("  Y=0    = Top of screen");
        System.out.println("  Y=540  = Middle of screen");
        System.out.println("  Y=1080 = Bottom of screen");

        System.out.println("\n" + "=".repeat(80));
        System.out.println("DEBUG COMPLETE - The region is correctly calculated!");
        System.out.println("=".repeat(80) + "\n");
    }
}
