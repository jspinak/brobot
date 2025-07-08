package io.github.jspinak.brobot.runner.ui.automation.models;

import lombok.Builder;
import lombok.Data;

import java.util.function.Consumer;

/**
 * Configuration model for automation buttons.
 * Contains all the properties needed to create and style an automation button.
 */
@Data
@Builder
public class AutomationButtonConfig {
    
    /**
     * Unique identifier for the automation.
     */
    private final String name;
    
    /**
     * Display name shown on the button.
     */
    private final String displayName;
    
    /**
     * Optional description for tooltip.
     */
    private final String description;
    
    /**
     * Category this automation belongs to.
     */
    private final String category;
    
    /**
     * Optional icon to display on the button.
     */
    private final String icon;
    
    /**
     * Custom style class to apply.
     */
    private final String styleClass;
    
    /**
     * Preferred width for the button.
     */
    @Builder.Default
    private final double preferredWidth = 150.0;
    
    /**
     * Action to execute when button is clicked.
     * Receives the automation name as parameter.
     */
    private final Consumer<String> onAction;
    
    /**
     * Whether this automation requires confirmation before running.
     */
    @Builder.Default
    private final boolean requiresConfirmation = false;
    
    /**
     * Hotkey combination for this automation.
     */
    private final String hotkey;
    
    /**
     * Whether this automation is currently enabled.
     */
    @Builder.Default
    private final boolean enabled = true;
    
    /**
     * Priority/order for display (lower numbers appear first).
     */
    @Builder.Default
    private final int displayOrder = 0;
    
    /**
     * Tags for filtering/grouping.
     */
    private final String[] tags;
    
    /**
     * Creates a simple button config with minimal properties.
     */
    public static AutomationButtonConfig simple(String name, String displayName, Consumer<String> onAction) {
        return AutomationButtonConfig.builder()
                .name(name)
                .displayName(displayName)
                .onAction(onAction)
                .build();
    }
    
    /**
     * Creates a button config for a category.
     */
    public static AutomationButtonConfig forCategory(String category, String displayName, Consumer<String> onAction) {
        return AutomationButtonConfig.builder()
                .name(category + "_" + displayName.toLowerCase().replace(" ", "_"))
                .displayName(displayName)
                .category(category)
                .onAction(onAction)
                .build();
    }
    
    /**
     * Checks if this button has a specific tag.
     */
    public boolean hasTag(String tag) {
        if (tags == null) {
            return false;
        }
        
        for (String t : tags) {
            if (t.equalsIgnoreCase(tag)) {
                return true;
            }
        }
        
        return false;
    }
    
    /**
     * Gets a display-friendly category name.
     */
    public String getCategoryDisplayName() {
        if (category == null) {
            return "Uncategorized";
        }
        
        // Convert snake_case or camelCase to Title Case
        return category.replaceAll("_", " ")
                      .replaceAll("([a-z])([A-Z])", "$1 $2")
                      .substring(0, 1).toUpperCase() + 
                      category.substring(1);
    }
}