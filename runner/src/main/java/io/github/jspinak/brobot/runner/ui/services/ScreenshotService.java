package io.github.jspinak.brobot.runner.ui.services;

import io.github.jspinak.brobot.runner.ui.utils.ScreenshotUtil;
import io.github.jspinak.brobot.runner.ui.utils.DesktopScreenshotUtil;
import io.github.jspinak.brobot.util.image.capture.ScreenshotCapture;
import io.github.jspinak.brobot.util.image.capture.ScreenshotRecorder;
import io.github.jspinak.brobot.util.file.SaveToFile;
import javafx.stage.Stage;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.io.File;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.concurrent.CompletableFuture;
import javafx.application.Platform;

/**
 * Service that provides screenshot capture functionality for the Desktop Runner.
 * Integrates both JavaFX scene capture and Brobot's desktop screenshot capabilities.
 */
@Service
public class ScreenshotService {
    private static final Logger logger = LoggerFactory.getLogger(ScreenshotService.class);
    private static final DateTimeFormatter TIMESTAMP_FORMAT = DateTimeFormatter.ofPattern("yyyy-MM-dd_HH-mm-ss");
    
    @Autowired(required = false)
    private ScreenshotCapture screenshotCapture;
    
    @Autowired(required = false)
    private ScreenshotRecorder screenshotRecorder;
    
    @Autowired(required = false)
    private SaveToFile saveToFile;
    
    private Stage primaryStage;
    private boolean initialized = false;
    
    /**
     * Sets the primary stage for JavaFX screenshot capture.
     */
    public void setPrimaryStage(Stage primaryStage) {
        this.primaryStage = primaryStage;
        
        // Initialize desktop screenshot capability
        if (!initialized) {
            initialized = DesktopScreenshotUtil.initialize();
            if (!initialized) {
                logger.warn("Desktop screenshot capability not fully initialized - screenshots may fail");
            }
        }
    }
    
    /**
     * Takes a screenshot of the JavaFX application window.
     * 
     * @return The file path where the screenshot was saved, or null if failed
     */
    public String captureApplication() {
        if (primaryStage == null) {
            logger.error("Primary stage not set. Cannot capture application screenshot.");
            return null;
        }
        
        String timestamp = LocalDateTime.now().format(TIMESTAMP_FORMAT);
        String filename = "brobot-runner-" + timestamp;
        return ScreenshotUtil.captureStage(primaryStage, filename);
    }
    
    /**
     * Takes a screenshot of the desktop (full screen or active monitor).
     * Uses pure Java AWT Robot for reliability and runs asynchronously to prevent freezing.
     * 
     * @param baseFileName Base name for the screenshot file
     * @return The file path where the screenshot was saved, or null if failed
     */
    public String captureDesktop(String baseFileName) {
        try {
            // Ensure we're not on the JavaFX Application Thread
            if (Platform.isFxApplicationThread()) {
                logger.info("Capturing desktop screenshot from JavaFX thread - using async method");
                
                CompletableFuture<String> future = CompletableFuture.supplyAsync(() -> {
                    String timestamp = LocalDateTime.now().format(TIMESTAMP_FORMAT);
                    String filename = baseFileName + "-" + timestamp;
                    return DesktopScreenshotUtil.captureAllScreens(filename);
                });
                
                return future.get();
            } else {
                // Not on FX thread, can capture directly
                String timestamp = LocalDateTime.now().format(TIMESTAMP_FORMAT);
                String filename = baseFileName + "-" + timestamp;
                return DesktopScreenshotUtil.captureAllScreens(filename);
            }
        } catch (Exception e) {
            logger.error("Failed to capture desktop screenshot", e);
            return null;
        }
    }
    
    /**
     * Takes a screenshot of the desktop asynchronously.
     * This method returns immediately and performs the capture in the background.
     * 
     * @param baseFileName Base name for the screenshot file
     * @param callback Called when screenshot is complete (or fails)
     */
    public void captureDesktopAsync(String baseFileName, java.util.function.Consumer<String> callback) {
        String timestamp = LocalDateTime.now().format(TIMESTAMP_FORMAT);
        String filename = baseFileName + "-" + timestamp;
        
        DesktopScreenshotUtil.captureAllScreensAsync(filename)
            .thenAccept(path -> {
                if (callback != null) {
                    // Call callback on FX thread if needed
                    if (Platform.isFxApplicationThread()) {
                        callback.accept(path);
                    } else {
                        Platform.runLater(() -> callback.accept(path));
                    }
                }
            })
            .exceptionally(ex -> {
                logger.error("Async desktop capture failed", ex);
                if (callback != null) {
                    Platform.runLater(() -> callback.accept(null));
                }
                return null;
            });
    }
    
    /**
     * Takes a screenshot of the desktop using AWT Robot.
     * This captures the primary screen regardless of mouse position.
     * 
     * @param fileName Name for the screenshot file (without extension)
     * @return The file path where the screenshot was saved, or null if failed
     */
    public String captureDesktopPrimaryScreen(String fileName) {
        // Use pure Java desktop capture
        return DesktopScreenshotUtil.capturePrimaryScreen(fileName);
    }
    
    /**
     * Starts continuous screenshot recording at specified intervals.
     * 
     * @param baseFileName Base name for screenshot files
     * @param intervalSeconds Interval between screenshots in seconds
     */
    public void startRecording(String baseFileName, double intervalSeconds) {
        if (screenshotRecorder == null || saveToFile == null) {
            logger.error("Screenshot recording not available. Missing required components.");
            return;
        }
        
        int delayMillis = (int) (intervalSeconds * 1000);
        screenshotRecorder.startCapturing(saveToFile, baseFileName, delayMillis);
        logger.info("Started screenshot recording with {} second intervals", intervalSeconds);
    }
    
    /**
     * Stops continuous screenshot recording.
     */
    public void stopRecording() {
        if (screenshotRecorder == null) {
            logger.error("Screenshot recorder not available.");
            return;
        }
        
        screenshotRecorder.stopCapturing();
        logger.info("Stopped screenshot recording");
    }
    
    /**
     * Takes a timed series of screenshots.
     * 
     * @param duration Duration in seconds to capture screenshots
     * @param frequency Frequency of capture in seconds (e.g., 0.5 = twice per second)
     */
    public void captureTimedSeries(int duration, double frequency) {
        if (screenshotRecorder == null) {
            logger.error("Screenshot recorder not available.");
            return;
        }
        
        logger.info("Starting timed screenshot series: {} seconds at {} second intervals", duration, frequency);
        screenshotRecorder.capture(duration, frequency);
        logger.info("Completed timed screenshot series");
    }
    
    /**
     * Takes both application and desktop screenshots.
     * Useful for comparing JavaFX rendering with desktop appearance.
     * 
     * @param baseFileName Base name for the screenshots
     * @return Array with [applicationPath, desktopPath], or null values if failed
     */
    public String[] captureBoth(String baseFileName) {
        String[] paths = new String[2];
        
        // Capture application
        paths[0] = captureApplication();
        
        // Capture desktop
        paths[1] = captureDesktop(baseFileName);
        
        return paths;
    }
    
    /**
     * Takes a screenshot of a specific monitor.
     * 
     * @param monitorIndex The monitor index (0 for primary)
     * @param fileName Name for the screenshot file (without extension)
     * @return The file path where the screenshot was saved, or null if failed
     */
    public String captureMonitor(int monitorIndex, String fileName) {
        return DesktopScreenshotUtil.captureMonitor(monitorIndex, fileName);
    }
    
    /**
     * Lists available monitors for debugging.
     */
    public void listAvailableMonitors() {
        DesktopScreenshotUtil.listMonitors();
    }
    
    /**
     * Ensures the screenshots directory exists.
     */
    public void ensureScreenshotDirectory() {
        File screenshotDir = new File("runner/screenshots");
        if (!screenshotDir.exists()) {
            if (screenshotDir.mkdirs()) {
                logger.info("Created screenshots directory");
            } else {
                logger.error("Failed to create screenshots directory");
            }
        }
    }
}