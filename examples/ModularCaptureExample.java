package examples;

import io.github.jspinak.brobot.capture.CaptureConfiguration;
import io.github.jspinak.brobot.capture.UnifiedCaptureService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.core.env.Environment;

import javax.imageio.ImageIO;
import java.awt.Rectangle;
import java.awt.image.BufferedImage;
import java.io.File;

/**
 * Demonstrates the modular capture system in Brobot.
 * 
 * This example shows how to:
 * 1. Switch between capture providers using properties
 * 2. Change providers at runtime
 * 3. Use the unified capture interface
 * 
 * To run with different providers, set the property:
 * 
 * For Robot (default):
 *   java -Dbrobot.capture.provider=ROBOT -jar app.jar
 * 
 * For FFmpeg:
 *   java -Dbrobot.capture.provider=FFMPEG -jar app.jar
 * 
 * For SikuliX:
 *   java -Dbrobot.capture.provider=SIKULIX -jar app.jar
 * 
 * Or modify application.properties:
 *   brobot.capture.provider=ROBOT
 */
@SpringBootApplication
@ComponentScan(basePackages = "io.github.jspinak.brobot")
public class ModularCaptureExample implements CommandLineRunner {
    
    @Autowired
    private UnifiedCaptureService captureService;
    
    @Autowired
    private CaptureConfiguration captureConfig;
    
    @Autowired
    private Environment env;
    
    public static void main(String[] args) {
        SpringApplication.run(ModularCaptureExample.class, args);
    }
    
    @Override
    public void run(String... args) throws Exception {
        System.out.println("\n=== Modular Capture System Example ===\n");
        
        // Show current configuration from properties
        System.out.println("1. Current Configuration (from properties):");
        System.out.println("   Provider: " + env.getProperty("brobot.capture.provider", "AUTO"));
        System.out.println("   Active: " + captureConfig.getCurrentProvider());
        System.out.println("   Physical Resolution: " + captureConfig.isCapturingPhysicalResolution());
        
        // Print full configuration report
        captureConfig.printConfigurationReport();
        
        // Demonstrate property-based configuration
        System.out.println("\n2. Property-Based Configuration:");
        System.out.println("   The provider is set via: brobot.capture.provider=" + 
                         env.getProperty("brobot.capture.provider"));
        System.out.println("   You can change this in application.properties");
        System.out.println("   or via command line: -Dbrobot.capture.provider=FFMPEG");
        
        // Capture with current provider
        System.out.println("\n3. Capturing with " + captureConfig.getCurrentProvider() + ":");
        BufferedImage screen = captureService.captureScreen();
        System.out.println("   Screen captured: " + screen.getWidth() + "x" + screen.getHeight());
        
        File outputFile = new File("modular-capture-" + 
            captureConfig.getCurrentProvider().toLowerCase() + ".png");
        ImageIO.write(screen, "png", outputFile);
        System.out.println("   Saved to: " + outputFile.getAbsolutePath());
        
        // Demonstrate runtime provider switching
        System.out.println("\n4. Runtime Provider Switching:");
        
        // Try Robot
        System.out.println("\n   Switching to Robot...");
        captureConfig.useRobot();
        testCapture("robot");
        
        // Try FFmpeg if available
        System.out.println("\n   Attempting to switch to FFmpeg...");
        try {
            captureConfig.useFFmpeg();
            testCapture("ffmpeg");
        } catch (IllegalStateException e) {
            System.out.println("   FFmpeg not available: " + e.getMessage());
        }
        
        // Try SikuliX
        System.out.println("\n   Switching to SikuliX...");
        captureConfig.useSikuliX();
        testCapture("sikulix");
        
        // Demonstrate preset modes
        System.out.println("\n5. Using Preset Capture Modes:");
        
        System.out.println("\n   Setting ROBOT_PHYSICAL mode:");
        captureConfig.setCaptureMode(CaptureConfiguration.CaptureMode.ROBOT_PHYSICAL);
        System.out.println("   Provider: " + captureConfig.getCurrentProvider());
        System.out.println("   Physical: " + captureConfig.isCapturingPhysicalResolution());
        
        // Show recommended configuration
        System.out.println("\n6. Recommended Configuration:");
        CaptureConfiguration.CaptureMode recommended = captureConfig.getRecommendedMode();
        System.out.println("   Recommended mode for this system: " + recommended);
        
        // Validate configuration
        System.out.println("\n7. Configuration Validation:");
        boolean isValid = captureConfig.validateConfiguration();
        System.out.println("   Configuration is " + (isValid ? "VALID" : "INVALID"));
        
        // Show how to check properties
        System.out.println("\n8. All Capture Properties:");
        captureConfig.getAllCaptureProperties().forEach((key, value) -> 
            System.out.println("   " + key + " = " + value));
        
        System.out.println("\n=== Example Complete ===");
        System.out.println("\nKey Takeaways:");
        System.out.println("• Change provider via property: brobot.capture.provider=ROBOT");
        System.out.println("• Switch at runtime: captureConfig.useRobot()");
        System.out.println("• All capture code uses the same interface");
        System.out.println("• No code changes needed when switching providers");
        System.out.println("• Automatic fallback if preferred provider fails");
    }
    
    private void testCapture(String providerName) {
        try {
            // Capture a small region to test
            Rectangle region = new Rectangle(100, 100, 200, 150);
            BufferedImage capture = captureService.captureRegion(region);
            System.out.println("   ✓ " + providerName + " captured region: " + 
                             capture.getWidth() + "x" + capture.getHeight());
        } catch (Exception e) {
            System.out.println("   ✗ " + providerName + " capture failed: " + e.getMessage());
        }
    }
}