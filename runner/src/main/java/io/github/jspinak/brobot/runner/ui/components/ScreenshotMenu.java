package io.github.jspinak.brobot.runner.ui.components;

import io.github.jspinak.brobot.util.image.capture.ScreenshotCapture;
import io.github.jspinak.brobot.util.file.SaveToFile;
import javafx.scene.control.*;
import javafx.stage.Stage;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

/**
 * Menu component for screenshot functionality.
 * Provides a simple menu with screenshot capture options.
 */
@Component
public class ScreenshotMenu {
    private static final Logger logger = LoggerFactory.getLogger(ScreenshotMenu.class);
    private static final DateTimeFormatter TIMESTAMP_FORMAT = DateTimeFormatter.ofPattern("yyyy-MM-dd_HH-mm-ss");
    
    @Autowired(required = false)
    private ScreenshotCapture screenshotCapture;
    
    @Autowired(required = false)
    private SaveToFile saveToFile;
    
    private Stage primaryStage;
    
    /**
     * Sets the primary stage for JavaFX screenshot capture.
     */
    public void setPrimaryStage(Stage primaryStage) {
        this.primaryStage = primaryStage;
    }
    
    /**
     * Creates a screenshot menu for the menu bar.
     * 
     * @return A Menu with screenshot options
     */
    public Menu createScreenshotMenu() {
        Menu screenshotMenu = new Menu("Screenshot");
        
        // JavaFX App Screenshot
        MenuItem captureAppItem = new MenuItem("Capture Application (F12)");
        captureAppItem.setOnAction(e -> captureJavaFXApp());
        
        // Desktop Screenshot
        MenuItem captureDesktopItem = new MenuItem("Capture Desktop");
        captureDesktopItem.setOnAction(e -> captureDesktop());
        captureDesktopItem.setDisable(screenshotCapture == null);
        
        // Separator
        SeparatorMenuItem separator = new SeparatorMenuItem();
        
        // Open screenshot folder
        MenuItem openFolderItem = new MenuItem("Open Screenshots Folder");
        openFolderItem.setOnAction(e -> openScreenshotsFolder());
        
        screenshotMenu.getItems().addAll(
            captureAppItem,
            captureDesktopItem,
            separator,
            openFolderItem
        );
        
        return screenshotMenu;
    }
    
    /**
     * Captures the JavaFX application window.
     */
    private void captureJavaFXApp() {
        if (primaryStage == null) {
            showAlert("Screenshot Error", "Primary stage not available for capture.");
            return;
        }
        
        try {
            String timestamp = LocalDateTime.now().format(TIMESTAMP_FORMAT);
            String filename = "brobot-runner-" + timestamp;
            String filePath = io.github.jspinak.brobot.runner.ui.utils.ScreenshotUtil.captureStage(primaryStage, filename);
            
            if (filePath != null) {
                showInfo("Screenshot Saved", "Application screenshot saved to:\n" + filePath);
            } else {
                showAlert("Screenshot Failed", "Failed to capture application screenshot.");
            }
        } catch (Exception e) {
            logger.error("Error capturing JavaFX app", e);
            showAlert("Screenshot Error", "Error: " + e.getMessage());
        }
    }
    
    /**
     * Captures the desktop screen.
     */
    private void captureDesktop() {
        if (screenshotCapture == null) {
            showAlert("Not Available", "Desktop screenshot capture is not available.");
            return;
        }
        
        try {
            String timestamp = LocalDateTime.now().format(TIMESTAMP_FORMAT);
            String filename = "desktop-" + timestamp;
            String filePath = screenshotCapture.captureScreenshot(filename);
            
            if (filePath != null) {
                showInfo("Screenshot Saved", "Desktop screenshot saved to:\n" + filePath);
            } else {
                showAlert("Screenshot Failed", "Failed to capture desktop screenshot.");
            }
        } catch (Exception e) {
            logger.error("Error capturing desktop", e);
            showAlert("Screenshot Error", "Error: " + e.getMessage());
        }
    }
    
    /**
     * Opens the screenshots folder in the system file explorer.
     */
    private void openScreenshotsFolder() {
        try {
            java.io.File screenshotsDir = new java.io.File("screenshots");
            if (!screenshotsDir.exists()) {
                screenshotsDir.mkdirs();
            }
            
            // Use Desktop.open() to open the folder
            if (java.awt.Desktop.isDesktopSupported()) {
                java.awt.Desktop.getDesktop().open(screenshotsDir);
            } else {
                showAlert("Not Supported", "Opening folders is not supported on this system.");
            }
        } catch (Exception e) {
            logger.error("Error opening screenshots folder", e);
            showAlert("Error", "Failed to open screenshots folder: " + e.getMessage());
        }
    }
    
    /**
     * Shows an error alert dialog.
     */
    private void showAlert(String title, String content) {
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(content);
        alert.showAndWait();
    }
    
    /**
     * Shows an information alert dialog.
     */
    private void showInfo(String title, String content) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(content);
        alert.showAndWait();
    }
}