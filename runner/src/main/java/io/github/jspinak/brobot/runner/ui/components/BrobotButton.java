package io.github.jspinak.brobot.runner.ui.components;

import javafx.scene.control.Button;
import javafx.scene.control.ContentDisplay;
import javafx.scene.Node;
import javafx.geometry.Insets;

/**
 * Custom button component that ensures proper styling and prevents overlap detection issues.
 * This button uses a specific structure that the overlap detector can properly handle.
 */
public class BrobotButton extends Button {
    
    /**
     * Creates a button with text only.
     */
    public BrobotButton(String text) {
        super(text);
        initializeButton();
    }
    
    /**
     * Creates a button with text and graphic.
     */
    public BrobotButton(String text, Node graphic) {
        super(text);
        setGraphic(graphic);
        initializeButton();
    }
    
    /**
     * Creates an empty button.
     */
    public BrobotButton() {
        super();
        initializeButton();
    }
    
    /**
     * Initializes the button with proper styling to prevent overlap issues.
     */
    private void initializeButton() {
        // Set a specific style class for identification
        getStyleClass().add("brobot-button");
        
        // Ensure proper content display
        setContentDisplay(ContentDisplay.LEFT);
        
        // Set minimum sizes to prevent cramping
        setMinHeight(32);
        setMinWidth(80);
        
        // Add padding to ensure text doesn't touch edges
        setPadding(new Insets(8, 16, 8, 16));
        
        // Prevent text wrapping which can cause overlap detection issues
        setWrapText(false);
        
        // Set a specific ID pattern for the internal label
        // This helps the overlap detector identify and skip internal components
        setId("brobot-btn-" + System.nanoTime());
    }
    
    /**
     * Factory method for primary buttons.
     */
    public static BrobotButton primary(String text) {
        BrobotButton button = new BrobotButton(text);
        button.getStyleClass().add("primary");
        return button;
    }
    
    /**
     * Factory method for secondary buttons.
     */
    public static BrobotButton secondary(String text) {
        BrobotButton button = new BrobotButton(text);
        button.getStyleClass().add("secondary");
        return button;
    }
    
    /**
     * Factory method for accent buttons.
     */
    public static BrobotButton accent(String text) {
        BrobotButton button = new BrobotButton(text);
        button.getStyleClass().add("accent");
        return button;
    }
    
    /**
     * Factory method for icon buttons.
     */
    public static BrobotButton icon(String text, Node graphic) {
        BrobotButton button = new BrobotButton(text, graphic);
        button.getStyleClass().add("icon-button");
        return button;
    }
}