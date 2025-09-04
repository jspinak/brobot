package io.github.jspinak.brobot.integration;

import io.github.jspinak.brobot.test.BrobotTestBase;

import io.github.jspinak.brobot.model.element.Region;
import io.github.jspinak.brobot.util.image.debug.CaptureDebugger;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

/**
 * Test class to debug screen capture issues using CaptureDebugger.
 */
@SpringBootTest // (classes = ClaudeAutomatorApplication.class not available)
public class CaptureDebugTest extends BrobotTestBase {
    
    @Autowired
    private CaptureDebugger captureDebugger;
    
    @Test
    public void testCaptureDebug() {
        System.out.println("\n" + "=".repeat(80));
        System.out.println("STARTING CAPTURE DEBUG TEST");
        System.out.println("=".repeat(80));
        
        // Test a specific region where we expect to find patterns
        // This is the claude-prompt button area
        Region testRegion = new Region(600, 900, 200, 100);
        
        // Path to a pattern that should match (adjust as needed)
        String patternPath = "images/claude-prompt.png";
        
        // Run comprehensive debug
        captureDebugger.debugCapture(testRegion, patternPath);
        
        System.out.println("\nCapture debug test completed. Check debug-captures directory for results.");
    }
    
    @Test
    public void testFullScreenCapture() {
        System.out.println("\n" + "=".repeat(80));
        System.out.println("TESTING FULL SCREEN CAPTURE");
        System.out.println("=".repeat(80));
        
        // Test full screen capture
        Region fullScreen = new Region(0, 0, 1920, 1080);
        
        // Run debug without pattern matching
        captureDebugger.debugCapture(fullScreen, null);
        
        System.out.println("\nFull screen capture test completed.");
    }
}