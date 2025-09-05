package io.github.jspinak.brobot.patterncapture.capture;

import io.github.jspinak.brobot.patterncapture.ui.CaptureOverlay;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import javax.swing.*;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.util.function.Consumer;

/**
 * Controller that coordinates the screen capture process.
 * 
 * Manages the capture overlay and handles the capture workflow:
 * 1. Show overlay
 * 2. User selects region
 * 3. Capture selected area
 * 4. Return image to callback
 */
@Component
public class CaptureController {
    
    @Autowired
    private CaptureOverlay captureOverlay;
    
    private Consumer<BufferedImage> currentCallback;
    
    public CaptureController() {
        // Constructor
    }
    
    /**
     * Start the capture process with optional delay
     * 
     * @param callback Function to call with captured image (null if cancelled)
     */
    public void startCapture(Consumer<BufferedImage> callback) {
        startCapture(callback, 0);
    }
    
    /**
     * Start the capture process with specified delay
     * 
     * @param callback Function to call with captured image (null if cancelled)
     * @param delayMs Delay in milliseconds before showing overlay
     */
    public void startCapture(Consumer<BufferedImage> callback, int delayMs) {
        currentCallback = callback;
        
        // Clear any existing listeners
        captureOverlay.removeCaptureListener(captureListener);
        captureOverlay.addCaptureListener(captureListener);
        
        if (delayMs > 0) {
            // Use timer for delay
            Timer timer = new Timer(delayMs, e -> {
                captureOverlay.startCapture();
            });
            timer.setRepeats(false);
            timer.start();
        } else {
            // Start immediately
            SwingUtilities.invokeLater(() -> captureOverlay.startCapture());
        }
    }
    
    private final CaptureOverlay.CaptureListener captureListener = new CaptureOverlay.CaptureListener() {
        @Override
        public void onCaptureComplete(BufferedImage image, Rectangle bounds) {
            System.out.println("Capture complete: " + bounds.width + "x" + bounds.height);
            
            if (currentCallback != null) {
                SwingUtilities.invokeLater(() -> currentCallback.accept(image));
            }
        }
        
        @Override
        public void onCaptureCancelled() {
            System.out.println("Capture cancelled");
            
            if (currentCallback != null) {
                SwingUtilities.invokeLater(() -> currentCallback.accept(null));
            }
        }
    };
    
    /**
     * Cancel any ongoing capture
     */
    public void cancelCapture() {
        captureOverlay.setVisible(false);
        if (currentCallback != null) {
            currentCallback.accept(null);
            currentCallback = null;
        }
    }
    
    /**
     * Check if capture is currently in progress
     */
    public boolean isCapturing() {
        return captureOverlay.isVisible();
    }
}