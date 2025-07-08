package io.github.jspinak.brobot.runner.ui.components;

import io.github.jspinak.brobot.runner.ui.theme.ThemeManager;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.TextArea;
import lombok.extern.slf4j.Slf4j;

/**
 * Simple theme toggle with alert-based debugging
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
        
        StringBuilder debugInfo = new StringBuilder();
        debugInfo.append("Current theme: ").append(currentTheme).append("\n\n");
        
        // Get scene information
        if (getScene() != null && getScene().getRoot() != null) {
            var rootClasses = getScene().getRoot().getStyleClass();
            log.info("Root style classes before: {}", rootClasses);
            debugInfo.append("Root style classes before: ").append(rootClasses).append("\n\n");
            
            var stylesheets = getScene().getStylesheets();
            log.info("Scene stylesheets: {}", stylesheets);
            debugInfo.append("Scene stylesheets:\n");
            stylesheets.forEach(s -> debugInfo.append("  - ").append(s).append("\n"));
        }
        
        // Toggle theme
        ThemeManager.Theme newTheme = currentTheme == ThemeManager.Theme.LIGHT 
            ? ThemeManager.Theme.DARK 
            : ThemeManager.Theme.LIGHT;
        
        log.info("Switching to theme: {}", newTheme);
        debugInfo.append("\nSwitching to theme: ").append(newTheme).append("\n");
        
        themeManager.setTheme(newTheme);
        
        // Update button text
        setText(getButtonText());
        
        // Check state after toggle
        if (getScene() != null && getScene().getRoot() != null) {
            var rootClasses = getScene().getRoot().getStyleClass();
            log.info("Root style classes after: {}", rootClasses);
            debugInfo.append("\nRoot style classes after: ").append(rootClasses).append("\n");
        }
        
        log.info("=== THEME TOGGLE END ===");
        
        // Show debug info in alert
        showDebugAlert(debugInfo.toString());
    }
    
    private void showDebugAlert(String debugInfo) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle("Theme Toggle Debug");
        alert.setHeaderText("Theme switching information:");
        
        TextArea textArea = new TextArea(debugInfo);
        textArea.setEditable(false);
        textArea.setWrapText(true);
        textArea.setMaxWidth(Double.MAX_VALUE);
        textArea.setMaxHeight(Double.MAX_VALUE);
        textArea.setStyle("-fx-font-family: monospace; -fx-font-size: 12px;");
        
        alert.getDialogPane().setContent(textArea);
        alert.getDialogPane().setMinHeight(400);
        alert.setResizable(true);
        
        alert.showAndWait();
    }
}