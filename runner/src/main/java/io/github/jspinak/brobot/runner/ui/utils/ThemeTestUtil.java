package io.github.jspinak.brobot.runner.ui.utils;

import io.github.jspinak.brobot.runner.ui.theme.ThemeManager;
import javafx.application.Platform;
import javafx.scene.Scene;
import javafx.stage.Stage;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Utility class for testing theme switching functionality
 */
public class ThemeTestUtil {
    private static final Logger logger = LoggerFactory.getLogger(ThemeTestUtil.class);
    
    /**
     * Tests theme switching and takes screenshots in both modes
     */
    public static void testThemeSwitching(Stage stage, ThemeManager themeManager) {
        Platform.runLater(() -> {
            logger.info("Starting theme switching test");
            
            // First screenshot in current mode (should be light)
            String currentTheme = themeManager.getCurrentTheme().toString();
            logger.info("Current theme: {}", currentTheme);
            ScreenshotUtil.captureAndAnalyze(stage, "Theme-Test-" + currentTheme);
            
            // Switch to dark mode
            logger.info("Switching to dark mode");
            themeManager.setTheme(ThemeManager.Theme.DARK);
            
            // Wait a bit for the theme to apply
            Platform.runLater(() -> {
                try {
                    Thread.sleep(1000);
                } catch (InterruptedException e) {
                    logger.error("Sleep interrupted", e);
                }
                
                // Take screenshot in dark mode
                String newTheme = themeManager.getCurrentTheme().toString();
                logger.info("Theme after switch: {}", newTheme);
                ScreenshotUtil.captureAndAnalyze(stage, "Theme-Test-" + newTheme);
                
                // Log CSS stylesheets applied
                Scene scene = stage.getScene();
                if (scene != null) {
                    logger.info("Applied stylesheets:");
                    scene.getStylesheets().forEach(css -> logger.info("  - {}", css));
                }
                
                // Log if dark class is applied
                if (scene != null && scene.getRoot() != null) {
                    boolean hasDarkClass = scene.getRoot().getStyleClass().contains("dark");
                    logger.info("Root has 'dark' class: {}", hasDarkClass);
                }
            });
        });
    }
}