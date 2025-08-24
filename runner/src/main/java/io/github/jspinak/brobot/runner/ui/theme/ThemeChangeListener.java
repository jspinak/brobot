package io.github.jspinak.brobot.runner.ui.theme;

/**
 * Listener interface for theme change events.
 */
@FunctionalInterface
public interface ThemeChangeListener {
    
    /**
     * Called when the theme changes.
     * 
     * @param oldTheme The previous theme
     * @param newTheme The new theme
     */
    void onThemeChanged(ThemeManager.Theme oldTheme, ThemeManager.Theme newTheme);
}