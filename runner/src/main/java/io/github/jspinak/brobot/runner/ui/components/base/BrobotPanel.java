package io.github.jspinak.brobot.runner.ui.components.base;

import atlantafx.base.theme.Styles;
import javafx.geometry.Insets;
import javafx.scene.layout.VBox;
import javafx.scene.effect.DropShadow;
import javafx.scene.paint.Color;

/**
 * Base panel class that ensures consistent styling across all panels.
 * Extends VBox and provides common functionality for all Brobot panels.
 */
public abstract class BrobotPanel extends VBox {
    
    protected static final int SPACING_SM = 8;
    protected static final int SPACING_MD = 12;
    protected static final int SPACING_LG = 16;
    protected static final int SPACING_XL = 24;
    
    protected static final Insets PADDING_SM = new Insets(8);
    protected static final Insets PADDING_MD = new Insets(12);
    protected static final Insets PADDING_LG = new Insets(16);
    protected static final Insets PADDING_XL = new Insets(24);
    
    public BrobotPanel() {
        this(SPACING_MD, PADDING_LG);
    }
    
    public BrobotPanel(int spacing, Insets padding) {
        setSpacing(spacing);
        setPadding(padding);
        
        // Apply card-like styling
        getStyleClass().addAll("brobot-panel", "card");
        setStyle("-fx-background-color: white; -fx-background-radius: 8px;");
        
        // Add subtle shadow for card effect
        DropShadow shadow = new DropShadow();
        shadow.setColor(Color.rgb(0, 0, 0, 0.1));
        shadow.setRadius(8);
        shadow.setOffsetY(2);
        setEffect(shadow);
        
        initialize();
    }
    
    /**
     * Initialize the panel. Subclasses should override this method
     * to set up their specific content.
     */
    protected abstract void initialize();
    
    /**
     * Apply consistent styling to this panel.
     */
    protected void applyPanelStyle(String... additionalStyles) {
        getStyleClass().addAll(additionalStyles);
    }
    
    /**
     * Create a styled section within the panel.
     */
    protected VBox createSection(String title, String titleStyle) {
        VBox section = new VBox(SPACING_SM);
        section.getStyleClass().add("panel-section");
        section.setPadding(new Insets(0, 0, SPACING_MD, 0));
        
        if (title != null && !title.isEmpty()) {
            var titleLabel = new javafx.scene.control.Label(title);
            titleLabel.getStyleClass().addAll(titleStyle, Styles.TITLE_4);
            section.getChildren().add(titleLabel);
        }
        
        return section;
    }
}