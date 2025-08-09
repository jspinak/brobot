package io.github.jspinak.brobot.util.image.core;

import io.github.jspinak.brobot.model.element.Region;
import io.github.jspinak.brobot.config.ExecutionEnvironment;
import io.github.jspinak.brobot.config.SmartImageLoader;
import io.github.jspinak.brobot.tools.logging.ConsoleReporter;
import io.github.jspinak.brobot.util.file.FilenameUtils;
import io.github.jspinak.brobot.monitor.MonitorManager;
import io.github.jspinak.brobot.config.BrobotProperties;
import io.github.jspinak.brobot.util.image.capture.ScreenUtilities;

import org.bytedeco.opencv.global.opencv_imgcodecs;
import org.bytedeco.opencv.opencv_core.Mat;
import org.sikuli.script.Pattern;
import org.sikuli.script.Screen;
import org.sikuli.script.ScreenImage;
import org.springframework.stereotype.Component;
import org.springframework.beans.factory.annotation.Autowired;
import lombok.extern.slf4j.Slf4j;
import java.util.Base64;

import javax.imageio.ImageIO;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

@Slf4j
/**
 * Comprehensive BufferedImage operations for the Brobot GUI automation framework.
 * 
 * <p>BufferedImageUtilities provides a centralized set of utilities for working with Java's 
 * BufferedImage class, bridging the gap between various image representations used 
 * throughout the framework. It handles conversions, screen captures, file operations, 
 * and format transformations while abstracting platform-specific details.</p>
 * 
 * <p>Key operation categories:
 * <ul>
 *   <li><b>File Operations</b>: Loading images from disk with platform abstraction</li>
 *   <li><b>Screen Capture</b>: Capturing screen regions with headless/mock support</li>
 *   <li><b>Format Conversion</b>: Converting between BufferedImage, Mat, and byte arrays</li>
 *   <li><b>Image Manipulation</b>: Extracting sub-images and type conversions</li>
 *   <li><b>Serialization</b>: Base64 encoding/decoding for data transmission</li>
 * </ul>
 * </p>
 * 
 * <p>Platform abstraction features:
 * <ul>
 *   <li>Uses SikuliX for cross-platform file loading when available</li>
 *   <li>Falls back to direct file reading in headless environments</li>
 *   <li>Provides dummy images for mock/test scenarios</li>
 *   <li>Handles various image formats with automatic PNG extension</li>
 * </ul>
 * </p>
 * 
 * <p>Integration points:
 * <ul>
 *   <li><b>SikuliX</b>: Leverages Pattern class for platform-independent file access</li>
 *   <li><b>OpenCV/JavaCV</b>: Converts between Mat and BufferedImage formats</li>
 *   <li><b>Screen Capture</b>: Integrates with Screen class for region capture</li>
 *   <li><b>Environment Awareness</b>: Adapts behavior based on BrobotEnvironment</li>
 * </ul>
 * </p>
 * 
 * <p>Common use cases:
 * <ul>
 *   <li>Loading pattern images from files for visual matching</li>
 *   <li>Capturing screen regions for analysis or comparison</li>
 *   <li>Converting images for OpenCV processing</li>
 *   <li>Serializing images for network transmission or storage</li>
 *   <li>Extracting regions of interest from larger images</li>
 * </ul>
 * </p>
 * 
 * <p>In the model-based approach, BufferedImageUtilities serves as the foundation for all 
 * image handling operations. By providing consistent interfaces and handling edge cases 
 * like headless environments, it enables the framework to work reliably across different 
 * deployment scenarios while maintaining clean separation between image processing and 
 * automation logic.</p>
 * 
 * @since 1.0
 * @see Image
 * @see Pattern
 * @see Mat
 * @see ExecutionEnvironment
 */
@Component
public class BufferedImageUtilities {
    
    @Autowired(required = false)
    private MonitorManager monitorManager;
    
    @Autowired(required = false)
    private BrobotProperties properties;
    
    @Autowired(required = false)
    private SmartImageLoader smartImageLoader;
    
    private static BufferedImageUtilities instance;
    
    // Cache for environment info logging to avoid spam
    private static boolean environmentLogged = false;
    private static long lastMonitorLogTime = 0;
    private static final long MONITOR_LOG_INTERVAL = 60000; // Log monitor info at most once per minute
    
    @Autowired
    public BufferedImageUtilities() {
        instance = this;
    }
    
    /**
     * Get Screen object for a specific operation with monitor logging
     */
    private Screen getScreenForOperation(String operationName) {
        Screen screen = null;
        
        if (monitorManager != null) {
            screen = monitorManager.getScreen(operationName);
        }
        
        if (screen == null) {
            screen = new Screen(); // Fallback to primary
        }
        
        // Log monitor info if enabled
        if (properties != null && properties.getMonitor().isLogMonitorInfo()) {
            log.info("Using monitor {} for {} operation", screen.getID(), operationName);
        }
        
        return screen;
    }

    /**
     * Creates a new SikuliX Pattern and retrieves the BufferedImage from this Pattern.
     * @param path the filename of the image
     * @return the BufferedImage from file
     */
    public static BufferedImage getBuffImgFromFile(String path) {
        // Try SmartImageLoader first if available
        if (instance != null && instance.smartImageLoader != null) {
            try {
                SmartImageLoader.LoadResult result = instance.smartImageLoader.loadImage(path);
                if (result.isSuccess()) {
                    return instance.smartImageLoader.getFromCache(path);
                }
            } catch (Exception e) {
                ConsoleReporter.println("SmartImageLoader failed for " + path + ": " + e.getMessage());
            }
        }
        
        // Fall back to original implementation
        ExecutionEnvironment env = ExecutionEnvironment.getInstance();
        
        // In mock mode, always use dummy images
        if (env.isMockMode()) {
            return new BufferedImage(100, 100, BufferedImage.TYPE_INT_RGB);
        }
        
        // Try SikuliX Pattern first if we have display
        if (env.hasDisplay()) {
            try {
                Pattern sikuliPattern = new Pattern(path);
                BufferedImage bi = sikuliPattern.getBImage();
                if (bi != null) {
                    return bi;
                }
                ConsoleReporter.println("SikuliX Pattern returned null for: " + path);
            } catch (Exception e) {
                ConsoleReporter.println("SikuliX Pattern failed for " + path + ": " + e.getMessage());
            }
        }
        
        // Fall back to direct file reading
        return getBuffImgDirectly(path);
    }

    /**
     * To use this method, you need to deal with paths. In SikuliX, there is a bundle path.
     * SikuliX has code to abstract the OS. If you want Brobot to be platform independent, it should use SikuliX
     * as much as possible to retrieve BufferedImages from file.
     * @param path the path of the image
     * @return the BufferedImage from an image on file
     */
    public static BufferedImage getBuffImgDirectly(String path) {
        String pathWithExtension = FilenameUtils.addPngExtensionIfNeeded(path);
        File f = new File(pathWithExtension);
        
        // If file doesn't exist and path is relative, try common locations
        if (!f.exists() && !f.isAbsolute()) {
            // Try "images" folder first (common convention)
            File imagesFile = new File("images", pathWithExtension);
            if (imagesFile.exists()) {
                f = imagesFile;
                ConsoleReporter.println("Found in images folder: " + f.getAbsolutePath());
            } else {
                // Skip ImagePath.getPaths() since the return type is complex
                // We'll rely on bundle path and common locations
                
                // If still not found, try bundle path
                if (!f.exists()) {
                    try {
                        String bundlePath = org.sikuli.script.ImagePath.getBundlePath();
                        if (bundlePath != null && !bundlePath.isEmpty()) {
                            File bundleFile = new File(bundlePath, pathWithExtension);
                            if (bundleFile.exists()) {
                                f = bundleFile;
                                ConsoleReporter.println("Found in SikuliX bundle path: " + f.getAbsolutePath());
                            }
                        }
                    } catch (Exception e) {
                        // Ignore and continue with original file
                    }
                }
            }
        }
        
        if (!f.exists()) {
            ConsoleReporter.println("Image file not found: " + f.getAbsolutePath());
            ConsoleReporter.println("Searched in:");
            ConsoleReporter.println("  - Working directory images: " + new File("images").getAbsolutePath());
            try {
                String bundlePath = org.sikuli.script.ImagePath.getBundlePath();
                if (bundlePath != null) {
                    ConsoleReporter.println("  - SikuliX bundle path: " + bundlePath);
                }
            } catch (Exception e) {
                ConsoleReporter.println("  - Could not get bundle path");
            }
            return null;
        }
        
        ConsoleReporter.println("Loading image from: " + f.getAbsolutePath());
        
        try {
            BufferedImage img = ImageIO.read(f);
            if (img == null) {
                ConsoleReporter.println("ImageIO.read returned null for: " + f.getAbsolutePath());
            }
            return img;
        } catch (IOException e) {
            ConsoleReporter.println("IOException reading image: " + e.getMessage());
            throw new RuntimeException(e);
        }
    }

    public static BufferedImage getBufferedImageFromScreen(Region region) {
        ExecutionEnvironment env = ExecutionEnvironment.getInstance();
        
        // Log environment info only once per session
        if (!environmentLogged) {
            log.debug("[STATIC_SCREEN_CAPTURE] Environment info: {}", env.getEnvironmentInfo());
            log.debug("[STATIC_SCREEN_CAPTURE] canCaptureScreen: {}, hasDisplay: {}, mockMode: {}", 
                    env.canCaptureScreen(), env.hasDisplay(), env.isMockMode());
            environmentLogged = true;
        }
        
        // Only return dummy image if we're in mock mode or don't have display
        // canCaptureScreen() may be too restrictive for illustration generation
        if (env.isMockMode() || !env.hasDisplay()) {
            if (!environmentLogged) {
                log.warn("[STATIC_SCREEN_CAPTURE] Mock mode or no display - returning black dummy image");
            }
            // Return dummy only when screen capture not possible
            return new BufferedImage(region.w() > 0 ? region.w() : 1920, 
                                   region.h() > 0 ? region.h() : 1080, 
                                   BufferedImage.TYPE_INT_RGB);
        }
        
        try {
            // Use ScreenUtilities which has monitor support
            Screen screen = ScreenUtilities.getScreen("find");
            if (screen == null) {
                screen = new Screen(); // Fallback to primary monitor
            } else {
                // Only log monitor info periodically to reduce spam
                long now = System.currentTimeMillis();
                if (now - lastMonitorLogTime > MONITOR_LOG_INTERVAL) {
                    log.debug("Capturing from monitor {} for region: {}", screen.getID(), region);
                    lastMonitorLogTime = now;
                }
            }
            BufferedImage captured = screen.capture(region.sikuli()).getImage();
            // Only log successful capture occasionally
            if (!environmentLogged) {
                log.debug("[STATIC_SCREEN_CAPTURE] Successfully captured screen: {}x{}", 
                        captured.getWidth(), captured.getHeight());
            }
            return captured;
        } catch (Exception e) {
            log.error("[STATIC_SCREEN_CAPTURE] Failed to capture screen: {}", e.getMessage(), e);
            // Return dummy image on capture failure
            return new BufferedImage(region.w() > 0 ? region.w() : 1920, 
                                   region.h() > 0 ? region.h() : 1080, 
                                   BufferedImage.TYPE_INT_RGB);
        }
    }

    public BufferedImage getBuffImgFromScreen(Region region) {
        ExecutionEnvironment env = ExecutionEnvironment.getInstance();
        
        // Log environment info only once per session
        if (!environmentLogged) {
            log.debug("[SCREEN_CAPTURE] Environment info: {}", env.getEnvironmentInfo());
            log.debug("[SCREEN_CAPTURE] canCaptureScreen: {}, hasDisplay: {}, mockMode: {}", 
                    env.canCaptureScreen(), env.hasDisplay(), env.isMockMode());
            environmentLogged = true;
        }
        
        // Only return dummy image if we're in mock mode or don't have display
        // canCaptureScreen() may be too restrictive for illustration generation
        if (env.isMockMode() || !env.hasDisplay()) {
            if (!environmentLogged) {
                log.warn("[SCREEN_CAPTURE] Mock mode or no display - returning black dummy image");
            }
            // Return dummy only when screen capture not possible
            return new BufferedImage(region.w() > 0 ? region.w() : 1920, 
                                   region.h() > 0 ? region.h() : 1080, 
                                   BufferedImage.TYPE_INT_RGB);
        }
        
        try {
            // Use monitor-aware screen selection
            Screen screen = getScreenForOperation("find");
            // Only log monitor selection periodically
            long now = System.currentTimeMillis();
            if (now - lastMonitorLogTime > MONITOR_LOG_INTERVAL) {
                log.debug("[SCREEN_CAPTURE] Using screen {} to capture region {}", screen.getID(), region);
                lastMonitorLogTime = now;
            }
            BufferedImage captured = screen.capture(region.sikuli()).getImage();
            // Only log successful capture once
            if (!environmentLogged) {
                log.debug("[SCREEN_CAPTURE] Successfully captured screen: {}x{}", 
                        captured.getWidth(), captured.getHeight());
            }
            return captured;
        } catch (Exception e) {
            log.error("[SCREEN_CAPTURE] Failed to capture screen: {}", e.getMessage(), e);
            // Return dummy image on capture failure
            return new BufferedImage(region.w() > 0 ? region.w() : 1920, 
                                   region.h() > 0 ? region.h() : 1080, 
                                   BufferedImage.TYPE_INT_RGB);
        }
    }

    public List<BufferedImage> getBuffImgsFromScreen(List<Region> regions) {
        ExecutionEnvironment env = ExecutionEnvironment.getInstance();
        
        if (!env.canCaptureScreen()) {
            // Return dummy images only when screen capture not possible
            List<BufferedImage> bufferedImages = new ArrayList<>();
            regions.forEach(region -> bufferedImages.add(
                    new BufferedImage(region.w() > 0 ? region.w() : 100, 
                                    region.h() > 0 ? region.h() : 100, 
                                    BufferedImage.TYPE_INT_RGB)));
            return bufferedImages;
        }
        
        // Use monitor-aware screen selection
        Screen screen = getScreenForOperation("find-multiple");
        ScreenImage screenImage = screen.capture(); // uses IRobot
        List<BufferedImage> bufferedImages = new ArrayList<>();
        regions.forEach(region -> bufferedImages.add(
                screenImage.getSub(new Rectangle(region.x(), region.y(), region.w(), region.h())).getImage()));
        return bufferedImages;
    }

    public BufferedImage convertTo3ByteBGRType(BufferedImage image) {
        BufferedImage convertedImage = new BufferedImage(image.getWidth(), image.getHeight(),
                BufferedImage.TYPE_3BYTE_BGR);
        convertedImage.getGraphics().drawImage(image, 0, 0, null);
        return convertedImage;
    }

    public BufferedImage convert(Mat mat) {
        try {
            // Create a temporary file to hold the image
            java.io.File temp = java.io.File.createTempFile("javacv", ".png");
            temp.deleteOnExit();
            
            // Use JavaCV's imwrite to save the Mat to a file
            org.bytedeco.opencv.global.opencv_imgcodecs.imwrite(temp.getAbsolutePath(), mat);
            
            // Read the file back as a BufferedImage
            BufferedImage bufferedImage = ImageIO.read(temp);
            
            // Delete the temp file (may fail if file is still in use, but that's acceptable for temp files)
            temp.delete();
            
            return bufferedImage;
        } catch (IOException e) {
            throw new RuntimeException("Failed to convert JavaCV Mat to BufferedImage", e);
        }
    }
    
    public static BufferedImage fromMat(Mat mat) {
        // Most reliable method - use a temporary file
        try {
            // Create a temporary file
            java.io.File temp = java.io.File.createTempFile("javacv", ".png");
            String tempPath = temp.getAbsolutePath();
            
            // Save the mat to the temporary file
            opencv_imgcodecs.imwrite(tempPath, mat);
            
            // Read it back as a BufferedImage
            BufferedImage image = ImageIO.read(temp);
            
            // Delete the temporary file
            temp.delete();
            
            return image;
        } catch (IOException e) {
            throw new RuntimeException("Failed to convert JavaCV Mat to BufferedImage", e);
        }
    }

    public static byte[] toByteArray(BufferedImage bufferedImage) {
        String format = "png";
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        try {
            ImageIO.write(bufferedImage, format, baos);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        return baos.toByteArray();
    }

    public static BufferedImage fromByteArray(byte[] bytes) {
        ByteArrayInputStream bais = new ByteArrayInputStream(bytes);
        try {
            return ImageIO.read(bais);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    public static BufferedImage getSubImage(BufferedImage originalImage, Region region) {
        return getSubImage(originalImage, region.x(), region.y(), region.w(), region.h());
    }

    public static BufferedImage getSubImage(BufferedImage originalImage, int x, int y, int width, int height) {
        // Resize the region to be within the bounds of the original image
        x = Math.max(0, x);
        y = Math.max(0, y);
        width = Math.min(originalImage.getWidth() - x, width);
        height = Math.min(originalImage.getHeight() - y, height);
        // Get the sub-image using getSubimage method
        return originalImage.getSubimage(x, y, width, height);
    }

    /**
     * Helper method to convert BufferedImage to Base64 String
     * @param image the BufferedImage to covert
     * @return a Base64 String representing the BufferedImage
     */
    public static String bufferedImageToStringBase64(BufferedImage image) {
        try {
            ByteArrayOutputStream bos = new ByteArrayOutputStream();
            ImageIO.write(image, "png", bos);
            byte[] imageBytes = bos.toByteArray();
            return Base64.getEncoder().encodeToString(imageBytes);
        } catch (IOException e) {
            e.printStackTrace();
            return null;
        }
    }

    public static BufferedImage base64StringToImage(String base64String) {
        byte[] imageBytes = Base64.getDecoder().decode(base64String); // Decode Base64 String to byte array
        ByteArrayInputStream bis = new ByteArrayInputStream(imageBytes); // Create ByteArrayInputStream from the byte array
        BufferedImage image = null; // Read image from ByteArrayInputStream and create BufferedImage
        try {
            image = ImageIO.read(bis);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        try {
            bis.close(); // Close the ByteArrayInputStream
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        return image;
    }

    public static byte[] base64StringToByteArray(String base64String) {
        if (base64String == null || base64String.isEmpty()) {
            return null;
        }
        return toByteArray(base64StringToImage(base64String));
    }

}
