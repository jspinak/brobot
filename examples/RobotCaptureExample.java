package examples;

import io.github.jspinak.brobot.capture.BrobotCaptureService;
import io.github.jspinak.brobot.capture.provider.CaptureProvider;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.ComponentScan;

import javax.imageio.ImageIO;
import java.awt.Rectangle;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;

/**
 * Example demonstrating the Robot-based physical resolution capture.
 * 
 * This example shows how Brobot now uses Robot as the default capture provider,
 * automatically scaling captures to physical resolution to ensure proper
 * pattern matching with images created at physical resolution.
 */
@SpringBootApplication
@ComponentScan(basePackages = "io.github.jspinak.brobot")
public class RobotCaptureExample implements CommandLineRunner {
    
    @Autowired
    private BrobotCaptureService captureService;
    
    public static void main(String[] args) {
        SpringApplication.run(RobotCaptureExample.class, args);
    }
    
    @Override
    public void run(String... args) throws Exception {
        System.out.println("\n=== Robot Capture Example ===\n");
        
        // Show provider information
        System.out.println("Provider Information:");
        System.out.println(captureService.getProvidersInfo());
        
        // Get active provider details
        CaptureProvider activeProvider = captureService.getActiveProvider();
        System.out.println("\nActive Provider: " + activeProvider.getName());
        System.out.println("Resolution Type: " + activeProvider.getResolutionType());
        
        // Capture full screen
        System.out.println("\n1. Capturing full screen...");
        BufferedImage screenCapture = captureService.captureScreen();
        System.out.println("   Captured: " + screenCapture.getWidth() + "x" + screenCapture.getHeight());
        
        // Save to file for verification
        File screenFile = new File("robot-capture-screen.png");
        ImageIO.write(screenCapture, "png", screenFile);
        System.out.println("   Saved to: " + screenFile.getAbsolutePath());
        
        // Capture a region
        System.out.println("\n2. Capturing region (100,100,400,300)...");
        Rectangle region = new Rectangle(100, 100, 400, 300);
        BufferedImage regionCapture = captureService.captureRegion(region);
        System.out.println("   Captured: " + regionCapture.getWidth() + "x" + regionCapture.getHeight());
        
        // Save region capture
        File regionFile = new File("robot-capture-region.png");
        ImageIO.write(regionCapture, "png", regionFile);
        System.out.println("   Saved to: " + regionFile.getAbsolutePath());
        
        // Demonstrate provider switching (if FFmpeg is available)
        System.out.println("\n3. Testing provider switching...");
        try {
            captureService.setProvider("FFMPEG");
            System.out.println("   Switched to FFmpeg provider");
            
            BufferedImage ffmpegCapture = captureService.captureScreen();
            System.out.println("   FFmpeg captured: " + ffmpegCapture.getWidth() + "x" + ffmpegCapture.getHeight());
            
            // Switch back to Robot
            captureService.setProvider("ROBOT");
            System.out.println("   Switched back to Robot provider");
        } catch (Exception e) {
            System.out.println("   FFmpeg not available: " + e.getMessage());
        }
        
        // Display resolution detection information
        System.out.println("\n4. Resolution Information:");
        System.out.println("   Java Version: " + System.getProperty("java.version"));
        System.out.println("   DPI Aware: " + !System.getProperty("sun.java2d.dpiaware", "true").equals("false"));
        
        if (screenCapture.getWidth() == 1920) {
            System.out.println("   ✓ Capturing at physical resolution (1920x1080)");
        } else if (screenCapture.getWidth() == 1536) {
            System.out.println("   ! Logical resolution detected (1536x864)");
            System.out.println("   ! Robot provider should scale this to 1920x1080");
        }
        
        System.out.println("\n=== Example Complete ===\n");
        System.out.println("Key Points:");
        System.out.println("- Robot is now the default capture provider");
        System.out.println("- Automatically scales logical resolution to physical (if DPI scaling detected)");
        System.out.println("- Ensures pattern matching works with images created at physical resolution");
        System.out.println("- FFmpeg remains available as an alternative for true physical capture");
        System.out.println("- Configuration via application.properties or brobot-defaults.properties");
    }
}