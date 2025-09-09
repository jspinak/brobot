package io.github.jspinak.brobot.debug;

import io.github.jspinak.brobot.debug.DebugTestBase;

import io.github.jspinak.brobot.test.BrobotTestBase;

import org.junit.jupiter.api.Test;
import io.github.jspinak.brobot.test.DisabledInCI;
import java.awt.*;

/**
 * Diagnostic test to understand screen configuration and potential offset issues.
 */
@DisabledInCI
public class ScreenDiagnosticsTest extends DebugTestBase {
    
    @Test
    public void diagnoseScreenConfiguration() {
        System.out.println("\n" + "=".repeat(80));
        System.out.println("SCREEN CONFIGURATION DIAGNOSTICS");
        System.out.println("=".repeat(80) + "\n");
        
        // Get GraphicsEnvironment
        GraphicsEnvironment ge = GraphicsEnvironment.getLocalGraphicsEnvironment();
        GraphicsDevice[] screens = ge.getScreenDevices();
        
        System.out.println("Number of screens detected: " + screens.length);
        System.out.println();
        
        for (int i = 0; i < screens.length; i++) {
            GraphicsDevice screen = screens[i];
            System.out.println("SCREEN " + i + " (" + screen.getIDstring() + "):");
            System.out.println("-".repeat(40));
            
            DisplayMode dm = screen.getDisplayMode();
            System.out.println("  Resolution: " + dm.getWidth() + "x" + dm.getHeight());
            System.out.println("  Bit depth: " + dm.getBitDepth());
            System.out.println("  Refresh rate: " + dm.getRefreshRate() + " Hz");
            
            GraphicsConfiguration gc = screen.getDefaultConfiguration();
            Rectangle bounds = gc.getBounds();
            System.out.println("  Bounds: " + bounds);
            System.out.println("    X offset: " + bounds.x);
            System.out.println("    Y offset: " + bounds.y);
            System.out.println("    Width: " + bounds.width);
            System.out.println("    Height: " + bounds.height);
            
            Insets insets = Toolkit.getDefaultToolkit().getScreenInsets(gc);
            System.out.println("  Screen insets (taskbar/panels):");
            System.out.println("    Top: " + insets.top);
            System.out.println("    Left: " + insets.left);
            System.out.println("    Bottom: " + insets.bottom);
            System.out.println("    Right: " + insets.right);
            
            System.out.println();
        }
        
        // Check virtual bounds (combined screen area)
        Rectangle virtualBounds = new Rectangle();
        for (GraphicsDevice screen : screens) {
            virtualBounds = virtualBounds.union(screen.getDefaultConfiguration().getBounds());
        }
        System.out.println("VIRTUAL SCREEN BOUNDS (all monitors combined):");
        System.out.println("-".repeat(40));
        System.out.println("  X: " + virtualBounds.x);
        System.out.println("  Y: " + virtualBounds.y);
        System.out.println("  Width: " + virtualBounds.width);
        System.out.println("  Height: " + virtualBounds.height);
        
        // Test coordinate conversion
        System.out.println("\nCOORDINATE TEST:");
        System.out.println("-".repeat(40));
        System.out.println("Lower left quarter should be:");
        System.out.println("  X: 0 to " + (virtualBounds.width / 2));
        System.out.println("  Y: " + (virtualBounds.height / 2) + " to " + virtualBounds.height);
        
        // Check if there's any offset
        if (virtualBounds.x != 0 || virtualBounds.y != 0) {
            System.out.println("\nWARNING: Screen has non-zero offset!");
            System.out.println("  This might cause highlighting misalignment");
            System.out.println("  Virtual screen starts at (" + virtualBounds.x + ", " + virtualBounds.y + ")");
        }
        
        // Test SikuliX screen detection
        System.out.println("\nSIKULIX SCREEN DETECTION:");
        System.out.println("-".repeat(40));
        try {
            org.sikuli.script.Screen sikuliScreen = new org.sikuli.script.Screen();
            System.out.println("  SikuliX Screen ID: " + sikuliScreen.getID());
            System.out.println("  SikuliX Bounds: x=" + sikuliScreen.x + ", y=" + sikuliScreen.y + 
                             ", w=" + sikuliScreen.w + ", h=" + sikuliScreen.h);
            
            // Test region creation
            org.sikuli.script.Region testRegion = new org.sikuli.script.Region(0, 540, 960, 540);
            System.out.println("\nTest Region (0, 540, 960, 540):");
            System.out.println("  SikuliX Region: " + testRegion);
            System.out.println("  Screen: " + testRegion.getScreen());
            
            // Check if region is on screen
            if (testRegion.getScreen() != null) {
                System.out.println("  Is on screen: " + testRegion.isValid());
                System.out.println("  Center: " + testRegion.getCenter());
            }
            
        } catch (Exception e) {
            System.out.println("  ERROR: Could not initialize SikuliX: " + e.getMessage());
        }
        
        System.out.println("\n" + "=".repeat(80));
        System.out.println("END DIAGNOSTICS");
        System.out.println("=".repeat(80) + "\n");
    }
}