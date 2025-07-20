package io.github.jspinak.brobot.runner.ui.utils;

import java.util.HashMap;
import java.util.Map;

/**
 * Maps button labels to programmatic icon names and provides clean labels.
 * This ensures consistent icon usage across the application.
 */
public class IconMapper {
    
    // Map of label text to icon name for IconRegistry
    private static final Map<String, String> LABEL_TO_ICON = new HashMap<>();
    
    // Map of original labels (with emoji) to clean labels
    private static final Map<String, String> CLEAN_LABELS = new HashMap<>();
    
    static {
        // Configuration icons
        CLEAN_LABELS.put("📁 Import", "Import");
        LABEL_TO_ICON.put("Import", "folder");
        
        CLEAN_LABELS.put("🔄 Refresh", "Refresh");
        LABEL_TO_ICON.put("Refresh", "refresh");
        
        CLEAN_LABELS.put("🔧 Change...", "Change...");
        LABEL_TO_ICON.put("Change...", "settings");
        
        CLEAN_LABELS.put("📂 Open Folder", "Open Folder");
        LABEL_TO_ICON.put("Open Folder", "folder-open");
        
        // Control icons
        CLEAN_LABELS.put("▶ Run", "Run");
        CLEAN_LABELS.put("▶ Start", "Start");
        CLEAN_LABELS.put("▶ Resume", "Resume");
        CLEAN_LABELS.put("▶ Play", "Play");
        LABEL_TO_ICON.put("Run", "play");
        LABEL_TO_ICON.put("Start", "play");
        LABEL_TO_ICON.put("Resume", "play");
        LABEL_TO_ICON.put("Play", "play");
        
        CLEAN_LABELS.put("⏸ Pause", "Pause");
        LABEL_TO_ICON.put("Pause", "pause");
        
        CLEAN_LABELS.put("⏹ Stop", "Stop");
        LABEL_TO_ICON.put("Stop", "stop");
        
        CLEAN_LABELS.put("🗔 New Window", "New Window");
        LABEL_TO_ICON.put("New Window", "window");
        
        CLEAN_LABELS.put("⌨️ Hotkeys", "Hotkeys");
        LABEL_TO_ICON.put("Hotkeys", "keyboard");
        
        // Theme icons
        CLEAN_LABELS.put("🌙 Dark Mode", "Dark Mode");
        LABEL_TO_ICON.put("Dark Mode", "moon");
        
        CLEAN_LABELS.put("☀️ Light Mode", "Light Mode");
        LABEL_TO_ICON.put("Light Mode", "sun");
        
        // Other icons
        CLEAN_LABELS.put("+ New Configuration", "New Configuration");
        LABEL_TO_ICON.put("New Configuration", "add");
        
        // Window control
        CLEAN_LABELS.put("🪟 Window", "Window");
        LABEL_TO_ICON.put("Window", "window");
        
        // Log level icons
        CLEAN_LABELS.put("🔍", "TRACE");
        LABEL_TO_ICON.put("TRACE", "search");
        
        CLEAN_LABELS.put("🐛", "DEBUG");
        LABEL_TO_ICON.put("DEBUG", "bug");
        
        CLEAN_LABELS.put("ℹ️", "INFO");
        LABEL_TO_ICON.put("INFO", "info");
        
        CLEAN_LABELS.put("⚠️", "WARNING");
        LABEL_TO_ICON.put("WARNING", "warning");
        
        CLEAN_LABELS.put("❌", "ERROR");
        LABEL_TO_ICON.put("ERROR", "error");
    }
    
    /**
     * Get clean version of button label (without emoji)
     * @param labelWithIcon The label that may contain an emoji icon
     * @return The label without emoji
     */
    public static String getCleanLabel(String labelWithIcon) {
        return CLEAN_LABELS.getOrDefault(labelWithIcon, labelWithIcon);
    }
    
    /**
     * Get the icon name for a given label
     * @param label The button label (clean or with emoji)
     * @return The icon name for IconRegistry, or null if no icon mapped
     */
    public static String getIconName(String label) {
        // First try the label as-is
        String iconName = LABEL_TO_ICON.get(label);
        if (iconName != null) {
            return iconName;
        }
        
        // Try getting the clean label first, then look up the icon
        String cleanLabel = getCleanLabel(label);
        return LABEL_TO_ICON.get(cleanLabel);
    }
    
    /**
     * Check if icons should be removed (can be made configurable)
     * @return true if icons should be removed
     */
    public static boolean shouldRemoveIcons() {
        // Always show icons - we're using programmatically generated icons
        return false;
    }
    
    /**
     * Process a label, removing emojis and returning clean text
     * @param label The original label
     * @return The clean label without emojis
     */
    public static String processLabel(String label) {
        return getCleanLabel(label);
    }
}