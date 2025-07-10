package io.github.jspinak.brobot.runner.ui.components;

import io.github.jspinak.brobot.runner.ui.theme.ThemeManager;
import javafx.scene.control.Button;
import lombok.extern.slf4j.Slf4j;

/**
 * Simple theme toggle button for switching between light and dark modes
 */
@Slf4j
public class SimpleThemeToggle extends Button {
    
    private final ThemeManager themeManager;
    
    public SimpleThemeToggle(ThemeManager themeManager) {
        this.themeManager = themeManager;
        
        setText(getButtonText());
        getStyleClass().add("theme-toggle");
        
        setOnAction(e -> toggleTheme());
    }
    
    private String getButtonText() {
        return themeManager.getCurrentTheme() == ThemeManager.Theme.LIGHT 
            ? "Dark Mode" 
            : "Light Mode";
    }
    
    private void toggleTheme() {
        log.info("=== THEME TOGGLE START ===");
        
        // Current state
        ThemeManager.Theme currentTheme = themeManager.getCurrentTheme();
        log.info("Current theme: {}", currentTheme);
        
        // Get scene information for logging
        if (getScene() != null && getScene().getRoot() != null) {
            var rootClasses = getScene().getRoot().getStyleClass();
            log.info("Root style classes before: {}", rootClasses);
            
            var stylesheets = getScene().getStylesheets();
            log.info("Scene stylesheets: {}", stylesheets);
        }
        
        // Toggle theme
        ThemeManager.Theme newTheme = currentTheme == ThemeManager.Theme.LIGHT 
            ? ThemeManager.Theme.DARK 
            : ThemeManager.Theme.LIGHT;
        
        log.info("Switching to theme: {}", newTheme);
        
        themeManager.setTheme(newTheme);
        
        // Update button text
        setText(getButtonText());
        
        // Check state after toggle
        if (getScene() != null && getScene().getRoot() != null) {
            var rootClasses = getScene().getRoot().getStyleClass();
            log.info("Root style classes after: {}", rootClasses);
        }
        
        log.info("=== THEME TOGGLE END ===");
    }
}