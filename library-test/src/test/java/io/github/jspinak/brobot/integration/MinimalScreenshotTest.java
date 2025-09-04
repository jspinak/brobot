package io.github.jspinak.brobot.integration;

import io.github.jspinak.brobot.test.BrobotTestBase;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.condition.DisabledIf;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;

import io.github.jspinak.brobot.config.environment.ExecutionEnvironment;

import javax.imageio.ImageIO;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.File;

/**
 * Minimal test to diagnose black screenshots.
 */
@SpringBootTest // (classes = ClaudeAutomatorApplication.class not available)
@ActiveProfiles("linux")
public class MinimalScreenshotTest extends BrobotTestBase {

    @Test
    @DisabledIf("java.awt.GraphicsEnvironment#isHeadless")
    public void testMinimalScreenshot() throws Exception {
        System.out.println("\n=== MINIMAL SCREENSHOT TEST ===");
        
        // 1. Check environment
        System.out.println("java.awt.headless: " + System.getProperty("java.awt.headless"));
        System.out.println("ExecutionEnvironment.canCaptureScreen: " + ExecutionEnvironment.getInstance().canCaptureScreen());
        
        // 2. Capture with Robot
        Robot robot = new Robot();
        BufferedImage capture = robot.createScreenCapture(new Rectangle(0, 0, 200, 200));
        
        // 3. Save it
        File output = new File("minimal-screenshot.png");
        ImageIO.write(capture, "png", output);
        
        // 4. Analyze
        int blackCount = 0;
        for (int i = 0; i < 100; i++) {
            int x = (int)(Math.random() * capture.getWidth());
            int y = (int)(Math.random() * capture.getHeight());
            if (capture.getRGB(x, y) == 0xFF000000) blackCount++;
        }
        
        System.out.println("File saved: " + output.getAbsolutePath());
        System.out.println("Black pixels: " + blackCount + "%");
        
        if (blackCount > 90) {
            System.out.println("⚠️ Screenshot is BLACK!");
            
            // Additional debug info
            GraphicsEnvironment ge = GraphicsEnvironment.getLocalGraphicsEnvironment();
            System.out.println("Default screen device: " + ge.getDefaultScreenDevice().getIDstring());
            System.out.println("Number of screens: " + ge.getScreenDevices().length);
            
            // Check if running in special environment
            System.out.println("DISPLAY env: " + System.getenv("DISPLAY"));
            System.out.println("SSH_CONNECTION: " + System.getenv("SSH_CONNECTION"));
        } else {
            System.out.println("✓ Screenshot has content");
        }
    }
}