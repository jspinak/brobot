package io.github.jspinak.brobot.runner.ui.theme;

import javafx.application.Platform;
import javafx.scene.Scene;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Primary;
import org.springframework.stereotype.Component;

import java.net.URL;
import java.util.*;
import java.util.concurrent.CopyOnWriteArrayList;

/**
 * Unified theme manager that properly handles theme switching
 */
@Slf4j
@Component
@Primary
public class UnifiedThemeManager extends ThemeManager {
    
    private static final String BASE_CSS = "/css/base.css";
    private static final String COMPONENTS_CSS = "/css/components.css";
    private static final String LAYOUTS_CSS = "/css/layouts.css";
    private static final String LIGHT_THEME_CSS = "/css/themes/light-theme.css";
    private static final String DARK_THEME_CSS = "/css/themes/dark-theme.css";
    private static final String THEME_TRANSITION_FIX_CSS = "/css/theme-transition-fix.css";
    private static final String THEME_TEXT_FIX_CSS = "/css/theme-text-fix.css";
    private static final String REMOVE_BORDERS_CSS = "/css/remove-excessive-borders.css";
    private static final String COMPREHENSIVE_CLEANUP_CSS = "/css/comprehensive-style-cleanup.css";
    private static final String DARK_MODE_TAB_FIX_CSS = "/css/dark-mode-tab-fix.css";
    
    private final List<Scene> registeredScenes = new CopyOnWriteArrayList<>();
    private final Map<Theme, List<String>> themeStylesheets = new HashMap<>();
    
    @Override
    public void initialize() {
        super.initialize();
        
        // Prepare stylesheets for each theme
        prepareThemeStylesheets();
        
        log.info("UnifiedThemeManager initialized");
    }
    
    private void prepareThemeStylesheets() {
        // Common stylesheets for both themes
        List<String> commonCss = Arrays.asList(
            BASE_CSS,
            COMPONENTS_CSS,
            LAYOUTS_CSS,
            THEME_TRANSITION_FIX_CSS,
            THEME_TEXT_FIX_CSS,
            REMOVE_BORDERS_CSS,
            COMPREHENSIVE_CLEANUP_CSS
        );
        
        // Light theme stylesheets
        List<String> lightCss = new ArrayList<>(commonCss);
        lightCss.add(LIGHT_THEME_CSS);
        themeStylesheets.put(Theme.LIGHT, lightCss);
        
        // Dark theme stylesheets
        List<String> darkCss = new ArrayList<>(commonCss);
        darkCss.add(DARK_THEME_CSS);
        darkCss.add(DARK_MODE_TAB_FIX_CSS);
        themeStylesheets.put(Theme.DARK, darkCss);
    }
    
    @Override
    public void setTheme(Theme theme) {
        if (theme == null || theme == getCurrentTheme()) return;
        
        log.info("Changing theme from {} to {}", getCurrentTheme(), theme);
        
        Theme oldTheme = getCurrentTheme();
        
        // Update the theme property (using parent's property)
        super.currentThemeProperty().set(theme);
        
        // Apply theme to all registered scenes
        Platform.runLater(() -> {
            for (Scene scene : registeredScenes) {
                applyThemeToScene(scene, theme);
            }
            
            // Notify listeners
            notifyThemeChangeListeners(oldTheme, theme);
        });
    }
    
    @Override
    public void registerScene(Scene scene) {
        if (scene == null || registeredScenes.contains(scene)) return;
        
        registeredScenes.add(scene);
        applyThemeToScene(scene, getCurrentTheme());
        
        log.debug("Registered scene, total registered: {}", registeredScenes.size());
    }
    
    @Override
    public void unregisterScene(Scene scene) {
        registeredScenes.remove(scene);
        log.debug("Unregistered scene, total registered: {}", registeredScenes.size());
    }
    
    private void applyThemeToScene(Scene scene, Theme theme) {
        if (scene == null || theme == null) return;
        
        var root = scene.getRoot();
        if (root == null) return;
        
        // Clear all theme-related classes
        root.getStyleClass().removeAll("light", "dark", "light-theme", "dark-theme");
        
        // Add appropriate theme classes
        if (theme == Theme.LIGHT) {
            root.getStyleClass().addAll("light", "light-theme");
        } else {
            root.getStyleClass().addAll("dark", "dark-theme");
        }
        
        // Update stylesheets
        scene.getStylesheets().clear();
        
        List<String> cssFiles = themeStylesheets.get(theme);
        for (String cssFile : cssFiles) {
            URL url = getClass().getResource(cssFile);
            if (url != null) {
                scene.getStylesheets().add(url.toExternalForm());
            } else {
                log.warn("CSS file not found: {}", cssFile);
            }
        }
        
        // Force style refresh
        root.applyCss();
        
        log.info("Applied {} theme to scene. Root classes: {}", theme, root.getStyleClass());
        log.debug("Scene stylesheets: {}", scene.getStylesheets());
    }
    
    private void notifyThemeChangeListeners(Theme oldTheme, Theme newTheme) {
        for (IThemeChangeListener listener : getThemeChangeListeners()) {
            try {
                listener.onThemeChanged(oldTheme, newTheme);
            } catch (Exception e) {
                log.error("Error notifying theme change listener", e);
            }
        }
    }
}