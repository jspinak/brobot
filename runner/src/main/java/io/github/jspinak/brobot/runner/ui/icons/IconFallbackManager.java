package io.github.jspinak.brobot.runner.ui.icons;

import javafx.scene.Node;
import javafx.scene.control.Label;
import javafx.scene.control.Labeled;
import javafx.scene.layout.Pane;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

/**
 * Manages icon fallbacks to prevent pi symbol display issues.
 * This component ensures that when icons cannot be loaded or displayed,
 * appropriate text alternatives are shown instead of unicode fallback characters.
 */
@Slf4j
@Component
public class IconFallbackManager {
    
    /**
     * Processes a node tree to replace icon placeholders with text alternatives.
     * This prevents pi symbols from appearing when icon fonts or resources fail to load.
     * 
     * @param root The root node to process
     */
    public void processNodeTree(Node root) {
        if (root == null) return;
        
        // Process labeled nodes (Button, Label, etc.)
        if (root instanceof Labeled) {
            processLabeled((Labeled) root);
        }
        
        // Note: Tab is not a Node, so it cannot be processed here
        // Tabs must be processed separately through TabPane
        
        // Process container children
        if (root instanceof Pane) {
            Pane pane = (Pane) root;
            for (Node child : pane.getChildren()) {
                processNodeTree(child);
            }
        }
    }
    
    /**
     * Processes labeled components to ensure text fallbacks.
     * 
     * @param labeled The labeled component to process
     */
    private void processLabeled(Labeled labeled) {
        String text = labeled.getText();
        if (text != null) {
            // Remove any unicode characters that might render as pi
            String cleaned = cleanText(text);
            if (!cleaned.equals(text)) {
                labeled.setText(cleaned);
                log.debug("Cleaned text for {}: {} -> {}", 
                    labeled.getClass().getSimpleName(), text, cleaned);
            }
        }
        
        // Remove graphic if it might cause issues
        Node graphic = labeled.getGraphic();
        if (graphic != null && shouldRemoveGraphic(graphic)) {
            labeled.setGraphic(null);
            log.debug("Removed potentially problematic graphic from {}", 
                labeled.getClass().getSimpleName());
        }
    }
    
    
    /**
     * Cleans text by removing problematic unicode characters.
     * 
     * @param text The text to clean
     * @return Cleaned text
     */
    private String cleanText(String text) {
        if (text == null) return "";
        
        // Remove common unicode symbols that might render as pi
        return text
            .replaceAll("[\\u03C0\\u03A0]", "") // Greek pi symbols
            .replaceAll("[\\u2190-\\u21FF]", "") // Arrows
            .replaceAll("[\\u2600-\\u26FF]", "") // Miscellaneous symbols
            .replaceAll("[\\u2700-\\u27BF]", "") // Dingbats
            .replaceAll("[\\uE000-\\uF8FF]", "") // Private use area
            .trim();
    }
    
    /**
     * Determines if a graphic node should be removed.
     * 
     * @param graphic The graphic node to check
     * @return true if the graphic should be removed
     */
    private boolean shouldRemoveGraphic(Node graphic) {
        // Remove graphics that might be icon fonts or small images
        if (graphic instanceof Label) {
            Label label = (Label) graphic;
            String text = label.getText();
            // Check if it's likely an icon font
            return text != null && (text.length() == 1 || containsProblematicChars(text));
        }
        
        // Remove very small graphics that might be icons
        return graphic.getBoundsInLocal().getWidth() < 32 && 
               graphic.getBoundsInLocal().getHeight() < 32;
    }
    
    /**
     * Checks if text contains problematic characters.
     * 
     * @param text The text to check
     * @return true if problematic characters are found
     */
    private boolean containsProblematicChars(String text) {
        if (text == null) return false;
        
        for (char c : text.toCharArray()) {
            // Check for various unicode ranges that might cause issues
            if ((c >= 0x0370 && c <= 0x03FF) || // Greek
                (c >= 0x2190 && c <= 0x21FF) || // Arrows
                (c >= 0x2600 && c <= 0x26FF) || // Symbols
                (c >= 0x2700 && c <= 0x27BF) || // Dingbats
                (c >= 0xE000 && c <= 0xF8FF)) { // Private use
                return true;
            }
        }
        
        return false;
    }
    
    /**
     * Creates a text-only fallback for common icon types.
     * 
     * @param iconType The type of icon
     * @return Text representation
     */
    public static String getTextFallback(String iconType) {
        if (iconType == null) return "";
        
        switch (iconType.toLowerCase()) {
            case "play": return "Run";
            case "pause": return "Pause";
            case "stop": return "Stop";
            case "refresh": return "Refresh";
            case "import": return "Import";
            case "export": return "Export";
            case "settings": return "Settings";
            case "configuration": return "Config";
            case "automation": return "Auto";
            case "resources": return "Resources";
            case "logs": return "Logs";
            case "showcase": return "Demo";
            case "home": return "Home";
            case "search": return "Search";
            case "add": return "Add";
            case "delete": return "Delete";
            case "edit": return "Edit";
            case "save": return "Save";
            case "window": return "Window";
            default: return iconType;
        }
    }
}