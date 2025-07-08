package io.github.jspinak.brobot.runner.ui.components;

import io.github.jspinak.brobot.runner.ui.theme.ThemeManager;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.TextArea;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;
import javafx.geometry.Insets;
import lombok.extern.slf4j.Slf4j;

/**
 * Debug version of theme toggle to diagnose issues
 */
@Slf4j
public class DebugThemeToggle extends Button {
    
    private final ThemeManager themeManager;
    private final TextArea debugOutput;
    private Stage debugStage;
    
    public DebugThemeToggle(ThemeManager themeManager) {
        this.themeManager = themeManager;
        this.debugOutput = new TextArea();
        debugOutput.setPrefRowCount(20);
        debugOutput.setEditable(false);
        debugOutput.setWrapText(true);
        debugOutput.setStyle("-fx-font-family: monospace; -fx-font-size: 12px; -fx-text-fill: black; -fx-control-inner-background: white;");
        
        setText(getButtonText());
        getStyleClass().add("theme-toggle");
        
        setOnAction(e -> toggleThemeWithDebug());
    }
    
    private String getButtonText() {
        return themeManager.getCurrentTheme() == ThemeManager.Theme.LIGHT 
            ? "🌙 Dark Mode" 
            : "☀️ Light Mode";
    }
    
    private void toggleThemeWithDebug() {
        log.info("=== THEME TOGGLE DEBUG START ===");
        debugOutput.appendText("=== THEME TOGGLE DEBUG START ===\n");
        
        // Current state
        ThemeManager.Theme currentTheme = themeManager.getCurrentTheme();
        log.info("Current theme: {}", currentTheme);
        debugOutput.appendText("Current theme: " + currentTheme + "\n");
        
        // Get scene information
        Scene scene = getScene();
        if (scene != null && scene.getRoot() != null) {
            var rootClasses = scene.getRoot().getStyleClass();
            log.info("Root style classes before: {}", rootClasses);
            debugOutput.appendText("Root style classes before: " + rootClasses + "\n");
            
            var stylesheets = scene.getStylesheets();
            log.info("Scene stylesheets: {}", stylesheets);
            debugOutput.appendText("Scene stylesheets:\n");
            stylesheets.forEach(s -> debugOutput.appendText("  - " + s + "\n"));
        }
        
        // Toggle theme
        ThemeManager.Theme newTheme = currentTheme == ThemeManager.Theme.LIGHT 
            ? ThemeManager.Theme.DARK 
            : ThemeManager.Theme.LIGHT;
        
        log.info("Switching to theme: {}", newTheme);
        debugOutput.appendText("Switching to theme: " + newTheme + "\n");
        
        themeManager.setTheme(newTheme);
        
        // Update button text
        setText(getButtonText());
        
        // Check state after toggle
        if (scene != null && scene.getRoot() != null) {
            var rootClasses = scene.getRoot().getStyleClass();
            log.info("Root style classes after: {}", rootClasses);
            debugOutput.appendText("Root style classes after: " + rootClasses + "\n");
            
            // Check computed styles
            var buttonStyle = getStyle();
            log.info("Button style: {}", buttonStyle);
            debugOutput.appendText("Button style: " + buttonStyle + "\n");
            
            // Check text fill
            var textFill = lookup(".text");
            if (textFill != null) {
                log.info("Text fill style: {}", textFill.getStyle());
                debugOutput.appendText("Text fill style: " + textFill.getStyle() + "\n");
            }
        }
        
        log.info("=== THEME TOGGLE DEBUG END ===");
        debugOutput.appendText("=== THEME TOGGLE DEBUG END ===\n\n");
        
        // Show debug window
        showDebugWindow();
    }
    
    private void showDebugWindow() {
        if (debugStage == null) {
            debugStage = new Stage();
            debugStage.setTitle("Theme Toggle Debug Output");
            VBox container = new VBox(10);
            container.setPadding(new Insets(10));
            container.getChildren().add(debugOutput);
            container.setStyle("-fx-background-color: white;");
            
            Scene debugScene = new Scene(container, 800, 600);
            debugStage.setScene(debugScene);
        }
        debugStage.show();
        debugStage.toFront();
    }
}